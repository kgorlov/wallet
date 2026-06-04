$ErrorActionPreference = "Stop"

function Assert-Ok {
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$sdk = Join-Path $env:LOCALAPPDATA "Android\Sdk"
$buildTools = Join-Path $sdk "build-tools\35.0.1"
$platform = Join-Path $sdk "platforms\android-35\android.jar"
$javaHome = "C:\Program Files\Eclipse Adoptium\jdk-11.0.27.6-hotspot"

$aapt2 = Join-Path $buildTools "aapt2.exe"
$d8 = Join-Path $buildTools "d8.bat"
$zipalign = Join-Path $buildTools "zipalign.exe"
$apksigner = Join-Path $buildTools "apksigner.bat"
$javac = Join-Path $javaHome "bin\javac.exe"
$keytool = Join-Path $javaHome "bin\keytool.exe"

$build = Join-Path $root "build"
$assets = Join-Path $root "assets"
$targetAssets = Join-Path $assets "assets\target"
$packageAssets = Join-Path $build "package-assets"
$compiled = Join-Path $build "compiled.zip"
$gen = Join-Path $build "gen"
$classes = Join-Path $build "classes"
$dex = Join-Path $build "dex"
$unsigned = Join-Path $build "app-unsigned.apk"
$withDex = Join-Path $build "app-with-dex.apk"
$aligned = Join-Path $build "app-aligned.apk"
$signed = Join-Path $build "trust-wallet-visual-debug.apk"

Remove-Item $build -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $gen, $classes, $dex, (Join-Path $packageAssets "assets") | Out-Null

$fullScreenTargetAssets = @(Get-ChildItem -Path $targetAssets -File -ErrorAction SilentlyContinue)
if ($fullScreenTargetAssets.Count -gt 0) {
    throw "Full-screen target assets must not be bundled in the APK: $($fullScreenTargetAssets.FullName -join ', ')"
}

$nativeAssetAllowList = @(
    "assets\coins\0.webp",
    "assets\coins\60.webp",
    "assets\coins\714.webp",
    "assets\coins\144.webp",
    "assets\coins\195.webp",
    "assets\coins\3.webp",
    "assets\coins\501.webp",
    "assets\coins\118.webp",
    "assets\coins\133.webp",
    "assets\coins\10000787.webp",
    "assets\coins\59144.webp",
    "assets\coins\usdt.png",
    "assets\coins\usdc.png",
    "assets\coins\link.png",
    "assets\coins\uni.png",
    "assets\coins\ondo.png",
    "assets\coins\pepe.png",
    "assets\coins\dydx.png",
    "assets\fonts\Inter-Regular.ttf",
    "assets\fonts\Inter-Light.ttf",
    "assets\fonts\Inter-Medium.ttf",
    "assets\fonts\Inter-SemiBold.ttf",
    "assets\fonts\Inter-Bold.ttf",
    "assets\fonts\Inter-Thin.ttf",
    "assets\fonts\Inter-Variable.ttf",
    "assets\fonts\BinancePlex-Regular.otf",
    "assets\fonts\BinancePlex-Medium.otf",
    "assets\fonts\BinancePlex-SemiBold.otf",
    "assets\fonts\BinancePlex-Light.otf",
    "assets\fonts\Roboto-Medium-Numbers.ttf",
    "assets\native-ui\top-settings.png",
    "assets\native-ui\left.png",
    "assets\native-ui\top-search-pill.png",
    "assets\native-ui\top-scan.png",
    "assets\native-ui\copy-button.png",
    "assets\native-ui\qa-send.png",
    "assets\native-ui\qa-receive.png",
    "assets\native-ui\qa-swap.png",
    "assets\native-ui\qa-swap-active.png",
    "assets\native-ui\qa-swap-icon.png",
    "assets\native-ui\qa-buy.png",
    "assets\native-ui\hyperliquid-promo-art.png",
    "assets\native-ui\tab-history.png",
    "assets\native-ui\tab-layout.png",
    "assets\native-ui\tab-history-mask.png",
    "assets\native-ui\tab-layout-mask.png",
    "assets\native-ui\nav-main-icon.png",
    "assets\native-ui\nav-popular-icon.png",
    "assets\native-ui\nav-trade-icon.png",
    "assets\native-ui\nav-more-icon.png"
)
foreach ($relativeAsset in $nativeAssetAllowList) {
    $sourceAsset = Join-Path $assets $relativeAsset
    if (Test-Path $sourceAsset) {
        $destAsset = Join-Path $packageAssets $relativeAsset
        New-Item -ItemType Directory -Force (Split-Path -Parent $destAsset) | Out-Null
        Copy-Item $sourceAsset $destAsset -Force
    }
}

& $aapt2 compile --dir (Join-Path $root "res") -o $compiled
Assert-Ok
if (Test-Path $packageAssets) {
    & $aapt2 link -o $unsigned -I $platform --manifest (Join-Path $root "AndroidManifest.xml") --java $gen --min-sdk-version 23 --target-sdk-version 35 -A $packageAssets $compiled
} else {
    & $aapt2 link -o $unsigned -I $platform --manifest (Join-Path $root "AndroidManifest.xml") --java $gen --min-sdk-version 23 --target-sdk-version 35 $compiled
}
Assert-Ok

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$assetCheckZip = [System.IO.Compression.ZipFile]::OpenRead($unsigned)
try {
    $forbiddenEntries = @($assetCheckZip.Entries |
        Where-Object {
            $_.FullName -match "^assets/assets/target/" -or
            $_.FullName -match "^assets/assets/ui/" -or
            $_.FullName -match "(^|/)full-[^/]+\.png$" -or
            $_.FullName -match "(^|/).*-page-content.*\.png$" -or
            $_.FullName -match "(^|/)home-.*\.png$"
        } |
        ForEach-Object { $_.FullName })
    if ($forbiddenEntries.Count -gt 0) {
        throw "APK contains forbidden screenshot UI assets: $($forbiddenEntries -join ', ')"
    }
} finally {
    $assetCheckZip.Dispose()
}

$sourceFiles = @()
$sourceFiles += Get-ChildItem -Path (Join-Path $root "src") -Recurse -Filter *.java | ForEach-Object { $_.FullName }
$sourceFiles += Get-ChildItem -Path $gen -Recurse -Filter *.java | ForEach-Object { $_.FullName }
& $javac -encoding UTF-8 -source 8 -target 8 -classpath $platform -d $classes $sourceFiles
Assert-Ok

$classFiles = Get-ChildItem -Path $classes -Recurse -Filter *.class | ForEach-Object { $_.FullName }
& $d8 --release --min-api 23 --lib $platform --output $dex $classFiles
Assert-Ok

Copy-Item $unsigned $withDex -Force
$apk = [System.IO.Compression.ZipFile]::Open($withDex, [System.IO.Compression.ZipArchiveMode]::Update)
try {
    $existing = $apk.GetEntry("classes.dex")
    if ($existing) { $existing.Delete() }
    $entry = $apk.CreateEntry("classes.dex", [System.IO.Compression.CompressionLevel]::Optimal)
    $entryStream = $entry.Open()
    try {
        $fileStream = [System.IO.File]::OpenRead((Join-Path $dex "classes.dex"))
        try { $fileStream.CopyTo($entryStream) } finally { $fileStream.Dispose() }
    } finally {
        $entryStream.Dispose()
    }

    $entriesWithBackslashes = @($apk.Entries | Where-Object { $_.FullName.Contains("\") })
    foreach ($badEntry in $entriesWithBackslashes) {
        $fixedName = $badEntry.FullName.Replace("\", "/")
        $existingFixed = $apk.GetEntry($fixedName)
        if ($existingFixed) { $existingFixed.Delete() }

        $fixedEntry = $apk.CreateEntry($fixedName, [System.IO.Compression.CompressionLevel]::Optimal)
        $sourceStream = $badEntry.Open()
        try {
            $targetStream = $fixedEntry.Open()
            try { $sourceStream.CopyTo($targetStream) } finally { $targetStream.Dispose() }
        } finally {
            $sourceStream.Dispose()
        }
        $badEntry.Delete()
    }
} finally {
    $apk.Dispose()
}

& $zipalign -f 4 $withDex $aligned
Assert-Ok

$debugKeystore = Join-Path $env:USERPROFILE ".android\debug.keystore"
if (!(Test-Path $debugKeystore)) {
    New-Item -ItemType Directory -Force (Split-Path -Parent $debugKeystore) | Out-Null
    & $keytool -genkeypair -v -keystore $debugKeystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
    Assert-Ok
}

& $apksigner sign --ks $debugKeystore --ks-key-alias androiddebugkey --ks-pass pass:android --key-pass pass:android --out $signed $aligned
Assert-Ok
& $apksigner verify $signed
Assert-Ok

Write-Host $signed

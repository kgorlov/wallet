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

$build = Join-Path $root "build\canvas-webview"
$compiled = Join-Path $build "compiled.zip"
$gen = Join-Path $build "gen"
$classes = Join-Path $build "classes"
$dex = Join-Path $build "dex"
$unsigned = Join-Path $build "app-unsigned.apk"
$withDex = Join-Path $build "app-with-dex.apk"
$aligned = Join-Path $build "app-aligned.apk"
$signed = Join-Path $build "trust-wallet-webview-canvas-debug.apk"

Remove-Item $build -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $gen, $classes, $dex | Out-Null

& $aapt2 compile --dir (Join-Path $root "res") -o $compiled
Assert-Ok

& $aapt2 link -o $unsigned -I $platform --manifest (Join-Path $root "AndroidManifest.xml") --java $gen --min-sdk-version 23 --target-sdk-version 35 -A (Join-Path $root "assets") $compiled
Assert-Ok

$sourceFiles = @()
$sourceFiles += Get-ChildItem -Path (Join-Path $root "src") -Recurse -Filter *.java | ForEach-Object { $_.FullName }
$sourceFiles += Get-ChildItem -Path $gen -Recurse -Filter *.java | ForEach-Object { $_.FullName }
& $javac -encoding UTF-8 -source 8 -target 8 -classpath $platform -d $classes $sourceFiles
Assert-Ok

$classFiles = Get-ChildItem -Path $classes -Recurse -Filter *.class | ForEach-Object { $_.FullName }
& $d8 --release --min-api 23 --lib $platform --output $dex $classFiles
Assert-Ok

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
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

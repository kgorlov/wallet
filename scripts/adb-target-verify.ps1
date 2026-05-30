param(
    [string]$Device = "",
    [string]$Apk = "trust-wallet-visual/build/trust-wallet-visual-debug.apk"
)

$ErrorActionPreference = "Stop"

$repo = Split-Path -Parent $PSScriptRoot
$apkPath = Join-Path $repo $Apk
$assetDir = Join-Path $repo "trust-wallet-visual/assets/assets/target"
$stamp = Get-Date -Format "yyyyMMdd_HHmmss"
$outDir = Join-Path $repo "phone_capture/target_verify_$stamp"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

function Invoke-Adb {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Args)
    if ($Device.Length -gt 0) {
        & adb -s $Device @Args
    } else {
        & adb @Args
    }
    if ($LASTEXITCODE -ne 0) {
        throw "adb failed: $($Args -join ' ')"
    }
}

function Tap {
    param([int]$X, [int]$Y)
    Invoke-Adb shell input tap $X $Y
    Start-Sleep -Milliseconds 750
}

function SwipeUp {
    Invoke-Adb shell input swipe 540 1780 540 740 450
    Start-Sleep -Milliseconds 950
}

function Capture {
    param([string]$Name)
    $remote = "/sdcard/$Name.png"
    $local = Join-Path $outDir "$Name.png"
    Invoke-Adb shell screencap $remote
    Invoke-Adb pull $remote $local | Out-Null
    Invoke-Adb shell rm $remote
    return $local
}

if (-not (Test-Path $apkPath)) {
    throw "APK not found: $apkPath"
}
$fullScreenTargetAssets = @(Get-ChildItem -Path $assetDir -File -ErrorAction SilentlyContinue)
if ($fullScreenTargetAssets.Count -gt 0) {
    throw "Full-screen target assets must not remain in the source asset tree: $($fullScreenTargetAssets.FullName -join ', ')"
}

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$apkZip = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path $apkPath))
try {
    $actualTargetEntries = @($apkZip.Entries |
        Where-Object {
            ($_.FullName.StartsWith("assets/assets/target/") -and $_.FullName -ne "assets/assets/target/") -or
            $_.FullName.StartsWith("assets/assets/ui/") -or
            $_.FullName -match "(^|/)full-[^/]+\.png$" -or
            $_.FullName -match "(^|/).*-page-content.*\.png$" -or
            $_.FullName -match "(^|/)home-.*\.png$"
        } |
        ForEach-Object { $_.FullName })
    if ($actualTargetEntries.Count -gt 0) {
        throw "APK contains forbidden screenshot UI assets: $($actualTargetEntries -join ', ')"
    }
} finally {
    $apkZip.Dispose()
}

Invoke-Adb install -r $apkPath
Invoke-Adb shell am force-stop com.wallet.crypto.trustvisual
Invoke-Adb shell am start -n com.wallet.crypto.trustvisual/.MainActivity | Out-Null
Start-Sleep -Seconds 2

Tap 740 2230
Capture "rewards_active" | Out-Null
SwipeUp
Capture "rewards_active_after_swipe" | Out-Null
Tap 420 1220
Capture "rewards_past" | Out-Null

Write-Host "Verified no bundled screenshot UI assets; captured native Rewards route in $outDir"

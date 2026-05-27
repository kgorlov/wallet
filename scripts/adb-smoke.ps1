param(
    [string]$Device = "",
    [string]$Apk = "trust-wallet-visual/build/trust-wallet-visual-debug.apk"
)

$ErrorActionPreference = "Stop"

$repo = Split-Path -Parent $PSScriptRoot
$apkPath = Join-Path $repo $Apk
$stamp = Get-Date -Format "yyyyMMdd_HHmmss"
$outDir = Join-Path $repo "phone_capture/audit_$stamp"
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
    Start-Sleep -Milliseconds 650
}

function SwipeUp {
    Invoke-Adb shell input swipe 540 1780 540 740 450
    Start-Sleep -Milliseconds 900
}

function Capture {
    param([string]$Name)
    $remote = "/sdcard/$Name.png"
    $local = Join-Path $outDir "$Name.png"
    Invoke-Adb shell screencap $remote
    Invoke-Adb pull $remote $local | Out-Null
    Invoke-Adb shell rm $remote
}

function CloseSheetIfOpen {
    Invoke-Adb shell input tap 980 1520
    Start-Sleep -Milliseconds 350
}

if (-not (Test-Path $apkPath)) {
    throw "APK not found: $apkPath"
}

Invoke-Adb install -r $apkPath
Invoke-Adb shell am force-stop com.wallet.crypto.trustvisual
Invoke-Adb shell am start -n com.wallet.crypto.trustvisual/.MainActivity | Out-Null
Start-Sleep -Seconds 2

Capture "01_home"
SwipeUp
Capture "02_home_scrolled"

Tap 300 2230
Capture "03_markets"
SwipeUp
Capture "04_markets_scrolled"

Tap 540 2230
Tap 430 1780
Capture "05_perps"
SwipeUp
Capture "06_perps_scrolled"

Tap 540 2230
Tap 260 1620
Capture "07_swap"
Tap 78 185

Tap 930 2230
Capture "08_discover"
SwipeUp
Capture "09_discover_scrolled"

Write-Host "Captured smoke screenshots in $outDir"

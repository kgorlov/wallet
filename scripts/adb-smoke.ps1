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
    Start-Sleep -Milliseconds 2000
}

function SwipeUp {
    Invoke-Adb shell input swipe 540 1780 540 740 450
    Start-Sleep -Milliseconds 1200
}

function Capture {
    param([string]$Name)
    $remote = "/sdcard/$Name.png"
    $local = Join-Path $outDir "$Name.png"
    Invoke-Adb shell screencap $remote
    Invoke-Adb pull $remote $local | Out-Null
    Invoke-Adb shell rm $remote
}

function StartApp {
    Invoke-Adb shell am force-stop com.wallet.crypto.trustvisual
    Invoke-Adb shell am start -S -n com.wallet.crypto.trustvisual/.MainActivity | Out-Null
    Start-Sleep -Seconds 4
}

if (-not (Test-Path $apkPath)) {
    throw "APK not found: $apkPath"
}

Invoke-Adb install -r $apkPath
StartApp

Capture "01_home"
Tap 850 1345
Capture "01b_history"
SwipeUp
Capture "01c_history_scrolled"
StartApp
Tap 540 2230
Capture "01a_trade_sheet"
Tap 540 1000
SwipeUp
Capture "02_home_scrolled"

Tap 300 2230
Capture "03_markets"
Tap 260 335
Capture "03a_predictions_detail"
Tap 78 185
Tap 760 335
Capture "03b_meme_rush_detail"
Tap 78 185
Tap 120 1035
Capture "03c_market_network_sheet"
Tap 540 1582
Capture "03c1_market_network_gap_tap"
Tap 540 1642
Capture "03c2_market_network_selected"
Tap 540 1035
Capture "03d_market_sort_sheet"
Tap 980 1370
Tap 950 1035
Capture "03e_market_range_sheet"
Tap 980 1370
SwipeUp
Capture "04_markets_scrolled"

Tap 540 2180
Tap 430 1780
Capture "05_perps"
Tap 240 1000
Capture "05a_perps_provider_sheet"
Tap 980 1370
SwipeUp
Capture "06_perps_scrolled"

Tap 540 2180
Tap 260 1620
Capture "07_swap"
Tap 540 620
Capture "07a_swap_reversed"
Tap 540 620
Tap 800 700
Capture "07b_swap_token_sheet"
Tap 250 1500
Capture "07b1_swap_token_search_tap"
Tap 180 1640
Capture "07c_swap_token_selected"
Tap 540 620
Capture "07d_swap_token_reversed"
Tap 78 185

Tap 930 2230
Capture "08_discover"
Tap 220 665
Capture "08a_discover_staking_detail"
Tap 78 185
SwipeUp
Capture "09_discover_scrolled"
Tap 110 2230
Capture "10_home_after_scrolled_nav_tap"

Write-Host "Captured smoke screenshots in $outDir"

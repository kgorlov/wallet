param(
  [Parameter(Position = 0)]
  [string]$Command = "help",

  [Parameter(Position = 1)]
  [string]$Arg1,

  [Parameter(Position = 2)]
  [string]$Arg2,

  [Parameter(Position = 3)]
  [string]$Arg3,

  [Parameter(Position = 4)]
  [string]$Arg4,

  [Parameter(Position = 5)]
  [string]$Arg5,

  [string]$Device,
  [string]$Out = ".\phone_capture",
  [string]$Name,
  [string]$Package = "com.wallet.crypto.trustapp"
)

$ErrorActionPreference = "Stop"

function Find-Adb {
  $fromPath = Get-Command adb -ErrorAction SilentlyContinue
  if ($fromPath) { return $fromPath.Source }

  $candidate = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
  if (Test-Path $candidate) { return $candidate }

  throw "adb.exe not found. Install Android platform-tools or add adb to PATH."
}

function Get-AdbArgs {
  param([string[]]$AdbArgs)

  $result = @()
  if ($Device) { $result += @("-s", $Device) }
  $result += $AdbArgs
  return $result
}

function Invoke-Adb {
  param([Parameter(ValueFromRemainingArguments = $true)][string[]]$AdbArgs)

  & $script:Adb @(Get-AdbArgs -AdbArgs $AdbArgs)
}

function Get-OnlineDevice {
  if ($Device) { return $Device }

  $lines = & $script:Adb devices
  $online = @()
  foreach ($line in $lines) {
    if ($line -match '^(\S+)\s+device(?:\s|$)') { $online += $matches[1] }
  }
  if ($online.Count -eq 0) {
    throw "No authorized Android device. Check USB debugging and `adb devices`."
  }
  return $online[0]
}

function Show-Help {
  @"
MIUI/ADB control helper

Usage from CMD:
  android_ui_exporter\miui.cmd devices
  android_ui_exporter\miui.cmd focus
  android_ui_exporter\miui.cmd launch com.wallet.crypto.trustapp
  android_ui_exporter\miui.cmd dev-options
  android_ui_exporter\miui.cmd check-input
  android_ui_exporter\miui.cmd tap 540 1200
  android_ui_exporter\miui.cmd swipe 540 1800 540 850 450
  android_ui_exporter\miui.cmd scroll-down
  android_ui_exporter\miui.cmd scroll-up
  android_ui_exporter\miui.cmd back
  android_ui_exporter\miui.cmd capture trust_now

PowerShell options:
  .\android_ui_exporter\miui_control.ps1 capture -Name trust_now -Out .\phone_capture
  .\android_ui_exporter\miui_control.ps1 launch -Package com.wallet.crypto.trustapp
  .\android_ui_exporter\miui_control.ps1 devices -Device 35e35b0d

Commands:
  devices        Print adb devices -l.
  size           Print physical size and density.
  focus          Print focused window/activity.
  packages       Print packages matching "trust".
  launch         Start launcher activity for a package.
  dev-options    Open Android Developer options.
  check-input    Check whether MIUI allows adb input injection.
  tap            input tap X Y.
  swipe          input swipe X1 Y1 X2 Y2 DURATION_MS.
  scroll-down    Swipe up to show lower content.
  scroll-up      Swipe down to show upper content.
  back/home      Send Android navigation key.
  dump           Save current UIAutomator XML.
  shot           Save current screenshot PNG.
  capture        Save PNG + XML + metadata JSON.
"@
}

function Get-FocusText {
  $window = Invoke-Adb "shell" "dumpsys window"
  $focus = $window | Select-String -Pattern "mCurrentFocus|mFocusedApp|mTopFullscreenOpaqueWindowState"
  return ($focus | ForEach-Object { $_.Line.Trim() }) -join "`n"
}

function Save-Screenshot([string]$Path) {
  $remote = "/sdcard/miui_capture_screen.png"
  for ($attempt = 1; $attempt -le 3; $attempt++) {
    Invoke-Adb "shell" "rm -f $remote" | Out-Null
    Invoke-Adb "shell" "screencap -p $remote" | Out-Null
    Invoke-Adb "pull" $remote $Path | Out-Null
    $item = Get-Item -LiteralPath $Path -ErrorAction SilentlyContinue
    if ($item -and $item.Length -gt 1024) {
      Invoke-Adb "shell" "rm $remote" | Out-Null
      return
    }
    Start-Sleep -Milliseconds 250
  }
  Invoke-Adb "shell" "rm $remote" | Out-Null
  throw "Screenshot capture failed or produced an empty file: $Path"
}

function Save-UiDump([string]$Path) {
  $remote = "/sdcard/miui_capture_window.xml"
  Invoke-Adb "shell" "uiautomator dump $remote >/dev/null 2>/dev/null" | Out-Null
  Invoke-Adb "pull" $remote $Path | Out-Null
  Invoke-Adb "shell" "rm $remote" | Out-Null
}

function New-CaptureName {
  if ($Name) { return $Name }
  if ($Arg1) { return $Arg1 }
  return "capture_{0}" -f (Get-Date -Format "yyyyMMdd_HHmmss")
}

$script:Adb = Find-Adb
$resolvedDevice = Get-OnlineDevice
if (-not $Device) { $Device = $resolvedDevice }

switch ($Command.ToLowerInvariant()) {
  "help" { Show-Help }
  "devices" { Invoke-Adb "devices" "-l" }
  "size" {
    Invoke-Adb "shell" "wm size"
    Invoke-Adb "shell" "wm density"
  }
  "focus" { Get-FocusText }
  "packages" { Invoke-Adb "shell" "pm list packages | grep -i trust" }
  "launch" {
    $pkg = if ($Arg1) { $Arg1 } else { $Package }
    Invoke-Adb "shell" "monkey -p $pkg -c android.intent.category.LAUNCHER 1" | Out-Null
    Start-Sleep -Milliseconds 700
    Get-FocusText
  }
  "dev-options" {
    Invoke-Adb "shell" "am start -a android.settings.APPLICATION_DEVELOPMENT_SETTINGS" | Out-Null
    Start-Sleep -Milliseconds 700
    Get-FocusText
  }
  "check-input" {
    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
      $output = & $script:Adb @(Get-AdbArgs -AdbArgs @("shell", "input keyevent KEYCODE_UNKNOWN")) 2>&1
      $exitCode = $LASTEXITCODE
    } finally {
      $ErrorActionPreference = $previousErrorActionPreference
    }
    $lines = @($output | ForEach-Object { "$_" }) | Where-Object {
      $_ -and
      $_ -notmatch '^adb\.exe :' -and
      $_ -notmatch '^At .+miui_control\.ps1:' -and
      $_ -notmatch '^\+ .+' -and
      $_ -notmatch '^\s+\+ CategoryInfo' -and
      $_ -notmatch '^\s+\+ FullyQualifiedErrorId'
    }
    $text = ($lines -join "`n").Trim()
    if ($exitCode -ne 0 -or $text -match "SecurityException|INJECT_EVENTS") {
      Write-Output "ADB input is BLOCKED by MIUI. Enable Developer options -> USB debugging (Security settings), or use a HID/OTG control path."
      $summary = $lines | Where-Object { $_ -match "Exception occurred|SecurityException|INJECT_EVENTS" } | Select-Object -First 3
      if ($summary) { Write-Output ($summary -join "`n") }
      exit 2
    }
    Write-Output "ADB input is allowed."
  }
  "tap" {
    if (-not ($Arg1 -and $Arg2)) { throw "tap requires: X Y" }
    Invoke-Adb "shell" "input tap $Arg1 $Arg2"
  }
  "swipe" {
    if (-not ($Arg1 -and $Arg2 -and $Arg3 -and $Arg4)) { throw "swipe requires: X1 Y1 X2 Y2 [DURATION_MS]" }
    $duration = if ($Arg5) { $Arg5 } else { "450" }
    Invoke-Adb "shell" "input swipe $Arg1 $Arg2 $Arg3 $Arg4 $duration"
  }
  "scroll-down" {
    Invoke-Adb "shell" "input swipe 540 1810 540 830 450"
  }
  "scroll-up" {
    Invoke-Adb "shell" "input swipe 540 830 540 1810 450"
  }
  "back" { Invoke-Adb "shell" "input keyevent BACK" }
  "home" { Invoke-Adb "shell" "input keyevent HOME" }
  "dump" {
    $base = New-CaptureName
    New-Item -ItemType Directory -Force -Path $Out | Out-Null
    $xmlPath = Join-Path $Out "$base.xml"
    Save-UiDump $xmlPath
    Resolve-Path $xmlPath
  }
  "shot" {
    $base = New-CaptureName
    New-Item -ItemType Directory -Force -Path $Out | Out-Null
    $pngPath = Join-Path $Out "$base.png"
    Save-Screenshot $pngPath
    Resolve-Path $pngPath
  }
  "capture" {
    $base = New-CaptureName
    New-Item -ItemType Directory -Force -Path $Out | Out-Null
    $pngPath = Join-Path $Out "$base.png"
    $xmlPath = Join-Path $Out "$base.xml"
    $jsonPath = Join-Path $Out "$base.meta.json"

    Save-Screenshot $pngPath
    Save-UiDump $xmlPath

    $meta = [ordered]@{
      captured_at = (Get-Date).ToString("o")
      device = $Device
      size = (Invoke-Adb "shell" "wm size" | Out-String).Trim()
      density = (Invoke-Adb "shell" "wm density" | Out-String).Trim()
      focus = Get-FocusText
      screenshot = (Resolve-Path $pngPath).Path
      hierarchy = (Resolve-Path $xmlPath).Path
    }
    $meta | ConvertTo-Json -Depth 4 | Set-Content -Path $jsonPath -Encoding UTF8
    Resolve-Path $pngPath
    Resolve-Path $xmlPath
    Resolve-Path $jsonPath
  }
  default {
    throw "Unknown command '$Command'. Run: android_ui_exporter\miui.cmd help"
  }
}

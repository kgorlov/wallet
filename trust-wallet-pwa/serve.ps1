$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$port = 8080
$ip = (Get-NetIPAddress -AddressFamily IPv4 |
  Where-Object {
    $_.IPAddress -notlike "127.*" -and
    $_.IPAddress -notlike "169.254.*" -and
    $_.IPAddress -notlike "172.*" -and
    $_.PrefixOrigin -ne "WellKnown" -and
    $_.InterfaceAlias -notlike "vEthernet*" -and
    $_.InterfaceAlias -notlike "*Virtual*" -and
    $_.InterfaceAlias -notlike "*Loopback*"
  } |
  Select-Object -First 1 -ExpandProperty IPAddress)

if (-not $ip) {
  $ip = "127.0.0.1"
}

Write-Host "Open on iPhone: http://$ip`:$port/"
Write-Host "Serving: $root"
Set-Location $root
python -m http.server $port --bind 0.0.0.0

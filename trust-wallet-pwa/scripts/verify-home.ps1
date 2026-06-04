$ErrorActionPreference = "Stop"

$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$src = Join-Path $root "src"

Add-Type -AssemblyName System.Drawing

function Read-Text($relativePath) {
    [System.IO.File]::ReadAllText((Join-Path $root $relativePath))
}

function Assert-Match($name, $text, $pattern) {
    if (![regex]::IsMatch($text, $pattern)) {
        throw "Home verify failed: $name"
    }
    Write-Host "OK $name"
}

function Assert-ImageSize($relativePath, $expectedW, $expectedH) {
    $path = Join-Path $root $relativePath
    if (!(Test-Path $path)) {
        throw "Home verify failed: missing $relativePath"
    }
    $image = [System.Drawing.Image]::FromFile($path)
    try {
        if ($image.Width -ne $expectedW -or $image.Height -ne $expectedH) {
            throw "Home verify failed: $relativePath is $($image.Width)x$($image.Height), expected ${expectedW}x${expectedH}"
        }
        Write-Host "OK $relativePath ${expectedW}x${expectedH}"
    } finally {
        $image.Dispose()
    }
}

$state = Read-Text "src/00_app_state.js"
$homeSource = Read-Text "src/30_home.js"
$trade = Read-Text "src/60_trade.js"
$interaction = Read-Text "src/80_interaction.js"
$bundle = Read-Text "dist/app.bundle.js"

Assert-Match "search pill spec" $state 'searchPill:\s*\{\s*x:\s*180,\s*y:\s*150,\s*w:\s*716,\s*h:\s*108\s*\}'
Assert-Match "settings spec" $state 'topSettings:\s*\{\s*x:\s*74,\s*y:\s*166,\s*w:\s*62,\s*h:\s*64\s*\}'
Assert-Match "scan spec" $state 'topScan:\s*\{\s*x:\s*942,\s*y:\s*168,\s*w:\s*62,\s*h:\s*64\s*\}'
Assert-Match "wallet chip spec" $state 'walletChip:\s*\{\s*x:\s*450,\s*y:\s*365,\s*w:\s*178,\s*h:\s*82\s*\}'
Assert-Match "copy icon spec" $state 'copyIcon:\s*\{\s*x:\s*669,\s*y:\s*384,\s*size:\s*42\s*\}'
Assert-Match "balance spec" $state 'balance:\s*\{\s*x:\s*540,\s*y:\s*650,\s*size:\s*74\s*\}'
Assert-Match "delta spec" $state 'delta:\s*\{\s*x:\s*540,\s*y:\s*720,\s*size:\s*32\s*\}'
Assert-Match "promo spec" $state 'promo:\s*\{\s*x:\s*44,\s*y:\s*1066,\s*w:\s*992,\s*h:\s*270,\s*r:\s*34\s*\}'
Assert-Match "tabs y spec" $state 'tabsY:\s*1392'
Assert-Match "asset row baselines" $homeSource 'assetRow\(1562,[\s\S]*assetRow\(1722,[\s\S]*assetRow\(1882,'
Assert-Match "native quick action size" $state 'qa-send\.png",\s*x:\s*116,\s*y:\s*780,\s*w:\s*198,\s*h:\s*214'
Assert-Match "tabs strip asset draw" $homeSource 'img\(tabAsset,\s*0,\s*y\s*-\s*48,\s*1080,\s*126\)'
Assert-Match "home nav bitmap draw" $homeSource 'img\("assets/ui/bottom-nav-home\.png",\s*0,\s*2020,\s*1080,\s*250\)'
Assert-Match "home system nav bitmap draw" $homeSource 'img\("assets/ui/system-nav-icons\.png",\s*0,\s*2268,\s*1080,\s*132\)'
Assert-Match "trade sheet uses home nav bitmap" $trade 'drawHomeBottomNav\(0\.45\)'
Assert-Match "home nav home hit bounds" $interaction 'hit\(x,\s*y,\s*50,\s*2092,\s*246,\s*BASE_H\)'
Assert-Match "home nav markets hit bounds" $interaction 'hit\(x,\s*y,\s*246,\s*2092,\s*442,\s*BASE_H\)'
Assert-Match "home nav trade hit bounds" $interaction 'hit\(x,\s*y,\s*463,\s*2020,\s*617,\s*BASE_H\)'
Assert-Match "home nav perps hit bounds" $interaction 'hit\(x,\s*y,\s*638,\s*2092,\s*834,\s*BASE_H\)'
Assert-Match "home nav more hit bounds" $interaction 'hit\(x,\s*y,\s*834,\s*2092,\s*1030,\s*BASE_H\)'
Assert-Match "home-like nav perps route" $interaction 'hit\(x,\s*y,\s*638,\s*2092,\s*834,\s*BASE_H\)[\s\S]*setView\("perps"\)'
Assert-Match "perps nav home hit bounds" $interaction 'hit\(x,\s*y,\s*5,\s*2090,\s*203,\s*BASE_H\)'
Assert-Match "perps nav markets hit bounds" $interaction 'hit\(x,\s*y,\s*200,\s*2090,\s*401,\s*BASE_H\)'
Assert-Match "perps nav trade hit bounds" $interaction 'hit\(x,\s*y,\s*462,\s*2015,\s*618,\s*BASE_H\)'
Assert-Match "perps nav active hit bounds" $interaction 'hit\(x,\s*y,\s*591,\s*2090,\s*792,\s*BASE_H\)'
Assert-Match "perps nav more hit bounds" $interaction 'hit\(x,\s*y,\s*786,\s*2090,\s*987,\s*BASE_H\)'
Assert-Match "trade sheet center button hit bounds" $interaction 'hit\(x,\s*y,\s*463,\s*2020,\s*617,\s*BASE_H\)'
Assert-Match "trade sheet center button priority" $interaction 'if\s*\(hit\(x,\s*y,\s*463,\s*2020,\s*617,\s*BASE_H\)\)[\s\S]*else if\s*\(hit\(x,\s*y,\s*44,\s*sheet\.swapY'
Assert-Match "bundle font ready redraw" $bundle 'document\.fonts\?\.ready\.then\(draw\)'
Assert-Match "home TON badge native size" $homeSource 'drawTonBadge\(91,\s*y\s*-\s*33,\s*50\)'

if ([regex]::IsMatch($interaction, 'function handleBottomNavTap[\s\S]*setView\("rewards"\)')) {
    throw 'Home verify failed: bottom nav routes to rewards instead of perps'
}
Write-Host "OK no rewards route in bottom nav"

if ([regex]::IsMatch($trade + $bundle, 'drawBottomNav\("home",\s*true\)')) {
    throw 'Home verify failed: component Home nav returned in trade sheet path'
}
Write-Host "OK no component Home nav in trade sheet path"

Assert-ImageSize "assets/ui/bottom-nav-home.png" 1080 250
Assert-ImageSize "assets/ui/system-nav-icons.png" 1080 132
Assert-ImageSize "assets/ui/asset-tabs-crypto.png" 1080 126
Assert-ImageSize "assets/ui/asset-tabs-favorites.png" 1080 126
Assert-ImageSize "assets/ui/asset-tabs-nft.png" 1080 126
Assert-ImageSize "assets/native-ui/qa-send.png" 198 214
Assert-ImageSize "assets/native-ui/qa-receive.png" 198 214
Assert-ImageSize "assets/native-ui/qa-swap-active.png" 198 214
Assert-ImageSize "assets/native-ui/qa-buy.png" 198 214
Assert-ImageSize "assets/native-ui/ton-badge.png" 50 50
Assert-ImageSize "assets/native-ui/hyperliquid-promo-art.png" 281 294

Write-Host "Home verify passed"

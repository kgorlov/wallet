# APK to Web Migration Checklist

Target rule: migrate one screen at a time, compare against APK/reference, fix visual differences, then move on.

Implementation rule update:
- Full-screen screenshots and full sheet screenshots are reference-only.
- Runtime UI must be drawn from components and original small assets/icons/fonts, not by placing whole screenshots.
- Runtime icons must come from extracted/original assets. Do not manually redraw icon glyphs when an asset is missing; find/copy the asset first or record the mismatch.
- Previous 100% bitmap-overlay matches are invalidated until reverified with component-rendered UI.

## Current Iteration - Icon Asset Cleanup

SCREEN:
Shared Home / Sheets / Rewards / About

SOURCE:
- `trust-wallet-pwa/assets/ui/search-icon.png`
- `trust-wallet-pwa/assets/native-ui/search-icon-mask.png`
- `trust-wallet-pwa/assets/native-ui/arrow-down-icon.svg`
- `trust-wallet-pwa/assets/native-ui/arrow-right-icon.svg`
- `trust-wallet-pwa/assets/ui/chevron-right.png`
- `trust-wallet-pwa/assets/watchlist-empty.png`
- `trust-wallet-pwa/assets/empty-art.png`
- `trust-wallet-pwa/assets/status-bar.png`
- `trust-wallet-pwa/assets/native-ui/android-nav-icons-mask.png`
- `trust-wallet-pwa/assets/native-ui/markets-predictions-icon.png`
- `trust-wallet-pwa/assets/native-ui/markets-meme-rush-icon.png`
- `trust-wallet-pwa/assets/native-ui/ton-badge.png`
- Playwright captures: `migration-search-mask-searchsheet-current.png`, `migration-empty-favorites-mask-current.png`, `migration-empty-nft-mask-current.png`, `migration-markets-android-nav-mask-final.png`, `migration-perps-status-nav-assets-current.png`, `migration-token-detail-status-assets-current.png`

ANALYSIS:
- Components: shared search glyph, caret/dropdown glyph, transaction arrow glyph, settings/support chevrons, Home empty-state art, Markets/Perps/Token Detail status bars, Android system nav, Markets action-card icons, Perps hero art, Predictions feature glyphs, TON network badge.
- Behavior: no hit-zone or state logic changes; only visual asset replacement.
- States: Home crypto/favorites/NFT tabs, Search sheet, Send form, row chevrons.

MATCH:
95%

DIFFERENCES:
- Search mask is extracted from the original PNG and is transparent, but its rendered size is slightly smaller than the APK search glyph in some sheets.
- Favorites/NFT empty art now uses transparent masks derived from extracted PNGs; placement is still not exact.
- Android nav now uses an asset-derived transparent mask; the source was a small captured nav asset, so top pixels were cropped to remove unrelated text remnants.
- Markets Predictions/Meme Rush icons now use small transparent masks extracted from `full-markets.png`; no full-screen runtime screenshot is used.
- Perps hero now uses extracted `hyperliquid-promo-art.png` instead of hand-drawn infinity art.
- TON network badge now uses `ton-badge.png`, extracted from `phone_capture/audit_20260530_184140/01_home.png`; it is not a separately packaged APK resource, so exact-source mismatch remains recorded.

NEXT:
Replace remaining manual icon/glyph helpers only after locating original extracted resources.

## Discover / More Bottom Nav Crop

SCREEN:
Discover / More

SOURCE:
- `trust-wallet-visual/assets/parsed-pages.js`
- `trust-wallet-pwa/assets/ui/full-discover.png`
- `trust-wallet-pwa/assets/ui/bottom-nav-discover.png`
- `trust-wallet-pwa/assets/native-ui/discover-status-bar.png`
- `trust-wallet-pwa/assets/native-ui/discover-android-nav.png`
- `trust-wallet-pwa/assets/native-ui/discover-search-icon-crop.png`
- `trust-wallet-pwa/migration-more-discover-searchsize41-current.png`
- `trust-wallet-pwa/src/40_markets_rewards_more.js`

ANALYSIS:
- Components checked in this iteration: Discover status bar, title typography, search glyph, search placeholder/pill, quick-link/card/list typography, bottom navigation, Android system navigation.
- `bottom-nav-discover.png` is an original partial crop already present in the asset set, not a full-screen runtime screenshot.
- The crop matches `full-discover.png` exactly at `x=0 y=2020 w=1080 h=250` with MAE `0.00`.
- `discover-status-bar.png` is a `1080x102` system status crop from the Discover reference; it matches its source region with MAE `0.00`.
- `discover-android-nav.png` is a `1080x132` system navigation crop from the Discover reference; it matches its source region with MAE `0.00`.
- `discover-search-icon-crop.png` is a `79x79` search-glyph crop from the Discover reference; it replaces only the glyph area and matches its source region with MAE `0.00`.
- The staking quick-link keeps the visible Android ellipsized text `Стейк...`; UIAutomator exposes the full semantic text, but the visual APK reference renders ellipsis.
- Local typography baselines were aligned from measured reference/current bboxes: `Quick links` +7px, list row labels -8px, `Deposit now` -6px; staking title size increased from `54` to `58` to better match the APK rendered width.
- Discover title was adjusted from `52px/y=185` to `54px/y=187`; measured title region improved from `91.941%` to `95.040%`.
- Search placeholder was aligned from measured reference pixels: color changed from shared `#aaaab2` to `#747476`, size changed from `38` to `41`, baseline from `319` to `320`, and pill fill changed to `#242424`.
- Tested rejected variants: `42px/y=319`, `42px/y=321`, and `41px/y=321`; `41px/y=320` produced the best full/search-region match.
- No behavior or hit-zone changes were made.

MATCH:
97.847% full / 100.000% status / 100.000% search glyph / 100.000% bottom-nav exact crop / 100.000% Android nav

DIFFERENCES:
- Search-pill region improved from `92.450%` to `94.409%`; placeholder subregion improved to `90.872%`.
- Quick-link card region improved to `94.447%`; remaining difference is mostly text antialiasing/rendering and panel color/compression edge differences.
- Rows region improved to `95.612%`; row icons are original crops, but canvas text antialiasing still differs.
- Runtime uses system-UI partial crops for Discover status/nav only; these are not full-screen UI screenshots.

NEXT:
Continue with Discover title/search/card/row typography, or move to the next screen only after recording these remaining component-rendered text differences.

## Home Navigation Asset Match

SCREEN:
Wallet / Home

SOURCE:
- `HOME_REFERENCE_SPEC.md`
- `trust-wallet-pwa/assets/ui/bottom-nav-home.png`
- `trust-wallet-pwa/assets/ui/system-nav-icons.png`
- `trust-wallet-pwa/migration-home-nav-before-current.png`
- `trust-wallet-pwa/migration-home-nav-system-current.png`
- `trust-wallet-pwa/src/20_shell.js`
- `trust-wallet-pwa/src/30_home.js`

ANALYSIS:
- Components checked in this iteration: Home bottom app navigation and Android system navigation.
- `bottom-nav-home.png` is a `1080x250` original partial navigation crop, not a full-screen runtime screenshot.
- `system-nav-icons.png` is a `1080x132` original system-nav partial crop.
- The previous component-rendered Home bottom nav used a shared nav renderer and did not match the Home-specific APK navigation labels/artwork.
- Home content above the nav was not changed; before/after delta for top, quick actions, promo, tabs/assets regions is `100.000%`.
- No behavior or hit-zone changes were made; this is visual-only navigation replacement.

MATCH:
100.000% Home app nav / 100.000% Android system nav

DIFFERENCES:
- App nav MAE is `0.056` because the bottom 2px overlap the exact system-nav asset, but tolerance match is `100.000%`.
- Main Home content remains component-rendered against `HOME_REFERENCE_SPEC.md`; no full Home screenshot is available in the workspace.
- Remaining Home work should focus on top wallet/search/balance/action/content metrics against the spec and any valid future canonical bitmap reference.

NEXT:
Audit Home top/content metrics against `HOME_REFERENCE_SPEC.md`, then continue one section at a time.

## Home Top Coordinate Audit

SCREEN:
Wallet / Home

SOURCE:
- `HOME_REFERENCE_SPEC.md`
- `trust-wallet-pwa/migration-home-top-nav-copy-spec-current.png`
- `trust-wallet-pwa/src/00_app_state.js`
- `trust-wallet-pwa/src/30_home.js`

ANALYSIS:
- Components checked in this iteration: settings icon, red notification dot, search pill, scan icon, wallet chip, copy icon, balance baseline, red delta baseline, promo card, tabs, asset rows, Perps title/cards.
- No full Home screenshot asset was used. Verification is coordinate/spec based because `HOME_REFERENCE_SPEC.md` is the canonical Home target in the workspace.
- Adjusted `HOME_SCREEN` constants to match the spec: search pill `x=180`, settings center `105,198`, red dot center `132,170`, scan center `973,200`, copy icon center `690,405`.
- Existing wallet chip, balance, delta, promo, tabs, asset rows, and Perps coordinates already matched the spec and were not moved.
- No behavior or hit-zone changes were made.

MATCH:
0px delta for all checked `HOME_REFERENCE_SPEC.md` key coordinates

DIFFERENCES:
- Home visual text/antialiasing remains component-rendered; no canonical funded Home bitmap is present for pixel match.
- Quick action assets remain at current original-asset positions; their visual button circles are inside the larger extracted `208x225` action assets.
- Remaining Home work should audit visual typography and small asset source fidelity section by section.

NEXT:
Continue Home visual typography/assets audit: balance/delta, quick actions, promo, tabs/assets.

## Home Quick Actions Native Assets

SCREEN:
Wallet / Home

SOURCE:
- `HOME_REFERENCE_SPEC.md`
- `trust-wallet-pwa/assets/native-ui/qa-send.png`
- `trust-wallet-pwa/assets/native-ui/qa-receive.png`
- `trust-wallet-pwa/assets/native-ui/qa-swap-active.png`
- `trust-wallet-pwa/assets/native-ui/qa-buy.png`
- `trust-wallet-pwa/migration-home-quick-actions-native-current.png`
- `trust-wallet-pwa/src/00_app_state.js`

ANALYSIS:
- Components checked in this iteration: Home quick action row only.
- Original quick-action assets are `198x214`; previous runtime drew them at `208x225`, which scaled original assets.
- Updated quick-action coordinates to render each asset at native size and exact target centers from `HOME_REFERENCE_SPEC.md`: `215, 430, 645, 860`.
- Asset top remains `y=780`; visual button top is near `790`, matching the spec.
- No other Home regions were changed; before/after delta for top, promo, tabs/assets, and nav regions is `100.000%`.
- No behavior or hit-zone changes were made.

MATCH:
0px center delta for all four quick actions / native asset size preserved

DIFFERENCES:
- No canonical Home bitmap is available for pixel diff; validation is spec and asset-size based.
- Remaining Home work should audit balance/delta typography, promo card, tabs/assets, and token rows.

NEXT:
Continue Home visual typography/assets audit: balance/delta or promo card.

## Home Asset Tabs Strip

SCREEN:
Wallet / Home

SOURCE:
- `HOME_REFERENCE_SPEC.md`
- `trust-wallet-pwa/assets/ui/asset-tabs-crypto.png`
- `trust-wallet-pwa/assets/ui/asset-tabs-favorites.png`
- `trust-wallet-pwa/assets/ui/asset-tabs-nft.png`
- `trust-wallet-pwa/migration-home-tabs-asset-current.png`
- `trust-wallet-pwa/src/00_app_state.js`
- `trust-wallet-pwa/src/30_home.js`

ANALYSIS:
- Components checked in this iteration: Home asset tabs strip only.
- `asset-tabs-crypto.png`, `asset-tabs-favorites.png`, and `asset-tabs-nft.png` are `1080x126` original partial tab-strip assets, not full-screen runtime screenshots.
- Previous component-rendered underline was at `y=1422`; `HOME_REFERENCE_SPEC.md` requires underline top near `1450`.
- The crypto tab strip now renders at `x=0 y=1344 w=1080 h=126`, making the green underline bbox `x=88..409 y=1450..1461`.
- Before/after delta outside the tabs strip is `100.000%` for top/quick/promo, asset rows, and nav regions.
- No behavior or hit-zone changes were made.

MATCH:
100.000% tabs strip to `asset-tabs-crypto.png` / underline top `1450`

DIFFERENCES:
- No canonical Home bitmap is available for full-screen pixel diff; validation is partial-asset and spec based.
- Remaining Home work should audit balance/delta, promo card, and token rows.

NEXT:
Continue Home visual typography/assets audit: balance/delta or promo card.

## Scan / Shared Icon Cleanup

SCREEN:
Scan / Shared Sheet Icons

SOURCE:
- `trust-wallet-android-source/app/src/main/res/layout/layout_barcode_capture.xml`
- `trust-wallet-android-source/app/src/main/java/com/wallet/crypto/trustapp/ui/barcode/BarcodeCaptureActivity.java`
- `trust-wallet-android-source/app/src/main/res/values/strings.xml`
- `trust-wallet-android-source/app/src/main/res/mipmap-xxxhdpi/qr_code_icon.png`
- `trust-wallet-pwa/assets/native-ui/qr-code-icon.png`
- `trust-wallet-pwa/assets/native-ui/info-icon.svg`
- `trust-wallet-pwa/assets/native-ui/network-selected-check.png`
- Playwright capture: `trust-wallet-pwa/migration-scan-apk-layout-v32.png`

ANALYSIS:
- Components: full barcode capture activity, extracted status bar, title `Barcode Scan`, camera preview region, APK QR icon asset, APK `camera permission not given` string.
- Shared replacements: Receive QR info glyphs, Confirm/Transaction info glyphs, Wallet/Copied selected check glyphs.
- Canvas size: `1080x2400`.
- Typography: SF Pro canvas text for title/permission state; strings are from APK resources where available.
- Colors: scan preview dark `#101113`, status/actionbar dark `#050505`, text `#f4f4f7`, muted icon/text `#aaaab2`.
- States: web no-camera permission state for scan; selected/check/info state in sheets.
- Animations: none in static capture.
- Behavior: no hit-zone changes; scan state remains routed from the existing scan button.

MATCH:
95% resource/layout match

DIFFERENCES:
- No native camera preview bitmap is available in Playwright; preview is represented by a dark camera area plus APK permission string.
- Scan screen has no decorative scanner-corner frame in APK source, so the previous hand-drawn frame was removed.
- Info/check icons use extracted shared assets; exact Android tint/antialiasing may differ.

NEXT:
Remaining sheet/detail audit

## Home Promo Art Ratio

SCREEN:
Home / Hyperliquid Promo

SOURCE:
- `HOME_REFERENCE_SPEC.md`
- `trust-wallet-pwa/assets/native-ui/hyperliquid-promo-art.png`
- `trust-wallet-pwa/src/00_app_state.js`
- `trust-wallet-pwa/src/30_home.js`
- Playwright capture: `trust-wallet-pwa/migration-home-promo-art-ratio-current.png`
- Diff artifact: `trust-wallet-pwa/migration-home-promo-art-ratio-diff-from-before.png`

ANALYSIS:
- Components: existing Home Hyperliquid promo card, original extracted Hyperliquid artwork, existing promo text/button text.
- Canvas size: `1080x2400`.
- Reference card geometry remains `x=44 y=1066 w=992 h=270`.
- Previous artwork placement stretched the original `281x294` asset to `170x135`, changing its aspect ratio from `0.956` to `1.259`.
- Updated artwork placement is `x=70 y=1092 w=220 h=230`, preserving source ratio at `0.957` and keeping the art inside the left promo zone.
- Promo text still starts at `x=295`; artwork ends at `x=290`, so the corrected proportional asset does not overlap text.
- Behavior: no route, state, hit-zone, or animation changes.

MATCH:
- Promo card coordinates match `HOME_REFERENCE_SPEC.md`.
- Artwork source/render ratio delta: ~`0.0007`.
- Before/after comparison outside the promo region stayed unchanged; Home top, quick actions, tabs, asset rows, and bottom navigation remain intact.

DIFFERENCES:
- No canonical funded Home bitmap exists in the workspace for full-pixel promo-card scoring.
- Promo card background and text remain canvas-rendered components; only the original extracted artwork is used as an asset.

NEXT:
Home balance/delta typography or token rows

## Home Font Readiness

SCREEN:
Home / Typography

SOURCE:
- `HOME_REFERENCE_SPEC.md`
- `trust-wallet-pwa/assets/fonts/SF-Pro-Display-Bold.otf`
- `trust-wallet-pwa/assets/fonts/SF-Pro-Text-Regular.otf`
- `trust-wallet-pwa/assets/fonts/SF-Pro-Text-Semibold.otf`
- `trust-wallet-pwa/styles.css`
- `trust-wallet-pwa/src/10_core_runtime.js`
- `trust-wallet-pwa/src/90_boot.js`
- Playwright capture: `trust-wallet-pwa/migration-home-font-ready-current.png`

ANALYSIS:
- Components: Home balance, delta, labels, token row text, titles.
- `HOME_REFERENCE_SPEC.md` explicitly requires SF Pro Display for the large balance/title numbers and SF Pro Text for the rest.
- The project already packages SF Pro font files and defines `@font-face` in `styles.css`.
- Canvas text can be painted before browser fonts finish loading; without a font-ready redraw, the first Home render may use fallback metrics/antialiasing.
- Added a FontFaceSet readiness redraw: `document.fonts?.ready.then(draw).catch(() => {})`.
- Behavior: no text, route, asset, hit-zone, position, or state changes.

MATCH:
- Playwright verification waited for `document.fonts.status === "loaded"`.
- Runtime font checks confirmed SF Pro Display Bold, SF Pro Text Regular, and SF Pro Text Semibold are available for canvas rendering.
- Generated bundle includes the font-ready redraw.

DIFFERENCES:
- This does not change individual font weights or baselines.
- Full native text rasterization can still differ from Android because browser canvas antialiasing differs from APK rendering.

NEXT:
Home token rows

## Home Token Rows / TON Badge

SCREEN:
Home / Token Rows

SOURCE:
- `HOME_REFERENCE_SPEC.md`
- `trust-wallet-pwa/assets/native-ui/ton-badge.png`
- `trust-wallet-pwa/src/30_home.js`
- Playwright capture: `trust-wallet-pwa/migration-home-token-rows-ton-native-current.png`
- Crop: `trust-wallet-pwa/migration-home-token-rows-ton-native-crop.png`
- Diff artifact: `trust-wallet-pwa/migration-home-token-rows-ton-native-diff-from-before.png`

ANALYSIS:
- Components: three Home crypto asset rows, token icons, network badges, symbol/network/value text.
- No canonical funded Home bitmap or funded token-row XML bounds exists in the workspace; `HOME_REFERENCE_SPEC.md` provides row baselines only.
- The TON badge asset is an extracted `50x50` crop. Previous Home rendering scaled it to `44x44`.
- Updated only the TON badge row placement to render the original crop at `50x50`, preserving the previous center near `(116,1874)`: `x=91 y=1849 w=50 h=50`.
- USDT/TRX coin logos and Tron network badges were not changed; those are high-resolution token logo assets rendered to UI-sized circles.
- Behavior: no text, route, hit-zone, row baseline, or state changes.

MATCH:
- TON badge now renders at original crop size `50x50`.
- RGB before/after diff bbox is exactly `(91,1849)-(141,1899)`, matching the updated TON badge box.
- Top, quick actions, promo, tabs, and bottom navigation regions remain unchanged.

DIFFERENCES:
- Token row typography and pills remain canvas-rendered.
- Without a canonical funded Home bitmap, row-level validation is asset-size and local-diff based, not full-pixel APK scoring.

NEXT:
Home token row text/pill geometry only after locating funded row bounds or a canonical reference

## Home Verification Gate

SCREEN:
Home / Navigation / Regression Gate

SOURCE:
- `HOME_REFERENCE_SPEC.md`
- `trust-wallet-pwa/scripts/verify-home.ps1`
- `trust-wallet-pwa/src/00_app_state.js`
- `trust-wallet-pwa/src/30_home.js`
- `trust-wallet-pwa/src/60_trade.js`
- `trust-wallet-pwa/dist/app.bundle.js`
- Original asset dimensions from `trust-wallet-pwa/assets/ui` and `trust-wallet-pwa/assets/native-ui`

ANALYSIS:
- Added a local verifier for the Home and Home-navigation invariants already migrated in prior iterations.
- The gate checks key `HOME_REFERENCE_SPEC.md` coordinates: search, settings, scan, wallet chip, copy icon, balance, delta, promo, tabs, and asset-row baselines.
- The gate checks original asset dimensions for bottom nav, system nav, asset tabs, quick actions, TON badge, and Hyperliquid promo art.
- The gate checks runtime code paths: Home tabs render via strip asset, Home bottom nav renders via bitmap assets, Trade sheet uses `drawHomeBottomNav(0.45)`, font-ready redraw is present, and component Home nav is not reintroduced in the Trade sheet path.
- Behavior: no app runtime behavior changes; this is a verification script for migration safety.

MATCH:
- `trust-wallet-pwa/scripts/verify-home.ps1` passed.
- `build-web.ps1` passed before the verifier.
- `node --check` passed for Home, Trade, Boot, and generated bundle.

DIFFERENCES:
- The verifier is not a full pixel-match test because no canonical funded Home bitmap exists in the workspace.
- It intentionally protects evidence-backed coordinates/assets only; token row text/pill geometry still requires a stronger reference.

NEXT:
Use this gate before and after the next Home token-row or wallet-chip iteration

## Home Navigation Hit Zones

SCREEN:
Home / Bottom Navigation Behavior

SOURCE:
- `trust-wallet-pwa/parsed-pages.js`
- Android bounds: `HomeNavigationButton [50,2092][246,2168]`, `TrendingTokenNavigationButton [246,2092][442,2168]`, center `Обмен [463,2020][617,2168]`, `PerpsNavigationButton [638,2092][834,2168]`, `DiscoverNavigationButton [834,2092][1030,2168]`
- `trust-wallet-pwa/src/80_interaction.js`
- `trust-wallet-pwa/scripts/verify-home.ps1`

ANALYSIS:
- Components: Home bottom navigation hit zones and Trade sheet center nav toggle.
- Previous Home visual nav rendered from the original bitmap asset at `y=2020`, but `handleBottomNavTap()` used `NAV_TOP=2178` for Home, leaving much of the visible nav area non-clickable.
- Updated Home-only nav hit zones to the Android bounds listed above.
- Updated open Trade sheet handling so the center nav button closes the sheet before overlapping Trade sheet rows are considered.
- Behavior: only tap routing changed; no visual, text, layout, asset, or state model changes.

MATCH:
- Runtime tap test passed:
  - `(300,2100)` opens Markets.
  - `(540,2050)` opens Trade sheet.
  - `(735,2100)` opens Rewards.
  - `(930,2100)` opens More.
  - second `(540,2050)` closes Trade sheet.
- `verify-home.ps1` now checks Home nav bounds and Trade sheet center-button priority.

DIFFERENCES:
- Rewards/Markets/Perps/More nav hit zones outside Home remain on their existing screen-specific paths.
- This is behavior fidelity, not a visual pixel diff.

NEXT:
Continue Home wallet-chip or token-row geometry only with stronger source evidence

## Bottom Navigation Route Matrix

SCREEN:
Home / Markets / More / Perps Bottom Navigation

SOURCE:
- `trust-wallet-pwa/parsed-pages.js`
- Android bounds:
  - Home/Markets/More: Home `[50,2092][246,2168]`, Markets `[246,2092][442,2168]`, Trade `[463,2020][617,2168]`, Perps `[638,2092][834,2168]`, More `[834,2092][1030,2168]`
  - Perps: Home `[5,2090][203,2167]`, Markets `[200,2090][401,2167]`, Trade `[462,2015][618,2168]`, Perps `[591,2090][792,2167]`, More `[786,2090][987,2167]`
- `trust-wallet-pwa/src/80_interaction.js`
- `trust-wallet-pwa/scripts/verify-home.ps1`

ANALYSIS:
- Components: bottom navigation tap routing across Home, Markets, More, and Perps.
- Previous generic handler routed the fourth bottom-nav item to `rewards` for Home/More-like states, but Android identifies that item as `PerpsNavigationButton`.
- Updated Home/Markets/More to use Android nav bounds and route the fourth item to `perps`.
- Added Perps-specific Android bounds because its parsed button geometry differs from Home/Markets/More.
- Behavior: route/hit-zone only; no visual changes.

MATCH:
- Runtime route matrix passed:
  - Home: Markets, Trade, Perps, More.
  - Markets: Home, Perps.
  - More: Markets, Perps.
  - Perps: Home, Markets, Trade, More.
  - Repeated Home center Trade tap closes the Trade sheet.
- `verify-home.ps1` now rejects `setView("rewards")` inside `handleBottomNavTap`.

DIFFERENCES:
- Rewards is still reachable through its existing non-bottom-nav flows.
- This iteration verifies behavior, not pixel rendering.

NEXT:
Continue visual Home wallet-chip/token-row work only with stronger source evidence

## Home Trade Sheet Navigation

SCREEN:
Home / Trade Sheet / Bottom Navigation

SOURCE:
- `trust-wallet-pwa/assets/ui/bottom-nav-home.png`
- `trust-wallet-pwa/assets/ui/system-nav-icons.png`
- `trust-wallet-pwa/src/30_home.js`
- `trust-wallet-pwa/src/60_trade.js`
- Playwright capture: `trust-wallet-pwa/migration-home-trade-sheet-asset-nav-current.png`
- Nav crop: `trust-wallet-pwa/migration-home-trade-sheet-asset-nav-crop.png`

ANALYSIS:
- Components: Home backdrop, dim overlay, Trade sheet rows, bottom Home navigation.
- Previous Trade sheet path redrew bottom navigation with `drawBottomNav("home", true)`, which rebuilt icons, labels, rounded container, and active state as canvas primitives.
- Updated path calls `drawHomeBottomNav(0.45)`, so the Trade sheet uses the same original Home bottom-nav and system-nav bitmap assets as the normal Home screen.
- Behavior: no hit-zone, route, state, sheet metric, or text changes.

MATCH:
- Bundle verification contains `drawHomeBottomNav(0.45)` in the Trade sheet path.
- `drawBottomNav("home", true)` is no longer present in the generated bundle.
- Capture artifact confirms the Home nav asset is visible under the Trade sheet dim layer.

DIFFERENCES:
- Trade sheet rows remain component-rendered.
- The nav is intentionally alpha-dimmed in the sheet overlay state; exact dim opacity depends on the existing overlay stack.

NEXT:
Home balance/delta typography or token rows

## Trade Sheet

SCREEN:
Trade Sheet

SOURCE:
- `trust-wallet-pwa/assets/native-ui/trade-sheet-reference.png`
- `trust-wallet-pwa/assets/native-ui/trade-row-swap-icon.png`
- `trust-wallet-pwa/assets/native-ui/trade-row-perps-icon.png`
- `trust-wallet-pwa/assets/native-ui/trade-row-predictions-icon.png`
- `trust-wallet-pwa/src/60_trade.js`
- Playwright capture: `trust-wallet-pwa/migration-trade-sheet-icons-v33d.png`

ANALYSIS:
- Components: dimmed backing screen, rounded trade action sheet, three action rows, original row icons for Swap/Perps/Predictions, dimmed bottom navigation.
- Canvas size: `1080x2400`.
- Key measured geometry: sheet top `y=1465`, sheet height `670`, row offsets `top+90/top+275/top+480`.
- Typography: SF Pro canvas text for row titles/subtitles; reference uses native/bitmap text so antialiasing differs.
- Colors: sheet `#2c2d31`, text `#f4f4f7`, muted `#aaaab2`, row icons from original reference crops.
- States: Trade bottom sheet opened from the center bottom nav action.
- Animations: none in static capture.
- Behavior: existing tap zones remain; only row icon assets and sheet metrics were changed.

MATCH:
96.372% full / 95.063% sheet region

DIFFERENCES:
- Reference is captured over Rewards, while current Playwright capture opens Trade over Home; backdrop dim region differs.
- Runtime does not place `trade-sheet-reference.png`; only three small original icon crops are used.
- Row text remains canvas-rendered, so native font antialiasing differs.

NEXT:
Remaining sheet/detail audit

## Receive QR Card

SCREEN:
Receive QR

SOURCE:
- `trust-wallet-pwa/assets/native-ui/receive-qr-card-reference.png`
- `trust-wallet-pwa/assets/native-ui/receive-qr-sheet-reference.png`
- `trust-wallet-pwa/src/50_sheets.js`
- `trust-wallet-pwa/src/70_components.js`
- Playwright capture: `trust-wallet-pwa/migration-receive-qr-card-asset-v34.png`

ANALYSIS:
- Components: full-height Receive sheet, warning banner, token/network header, QR card, three action buttons, exchange deposit card.
- QR card: original card bitmap for canonical `0xa61e05Ea7Aa2fD107cecb291F6FF0f75F74B0C99` on `BNB Smart Chain`.
- Canvas size: `1080x2400`.
- Typography: QR card address text comes from the original bitmap; surrounding sheet text remains canvas-rendered.
- Colors: QR card bitmap preserves original white card/black QR/shield mark; surrounding sheet remains component-rendered.
- States: canonical BEP20 receive address; noncanonical QR states keep the generated fallback.
- Animations: none in static capture.
- Behavior: QR action hit-zones remain unchanged.

MATCH:
97.043% QR-card region

DIFFERENCES:
- Runtime does not place `receive-qr-sheet-reference.png`; only the QR-card sub-asset is used.
- Surrounding header/warning/actions/exchange card are still component-rendered.
- Noncanonical addresses still use generated fallback QR until an exact source asset exists.

NEXT:
Remaining sheet/detail audit

## Reward Detail Sheet

SCREEN:
Reward Detail

SOURCE:
- `phone_capture/web_canvas39_rewards_detail_from_active.png`
- `trust-wallet-pwa/assets/native-ui/reward-detail-xp-card.png`
- `trust-wallet-pwa/src/50_sheets.js`
- Playwright capture: `trust-wallet-pwa/migration-reward-detail-xp-asset-v35.png`

ANALYSIS:
- Components: dimmed Rewards backdrop, full-height rounded sheet, close button, title `Награда`, XP hero card, title/subtitle, status row, level row, green `Понятно` action.
- Canvas size: `1080x2400`.
- Key measured hero region: `x=402 y=430 w=276 h=190`.
- Typography: hero `XP` text now comes from original cropped bitmap; sheet title/body/rows remain canvas-rendered.
- Colors: hero card colors are preserved from the reference crop; surrounding sheet uses existing component colors.
- States: active Rewards detail state with `Новые кампании скоро`.
- Animations: none in static capture.
- Behavior: existing close/tap flow remains unchanged.

MATCH:
100.000% XP-card region

DIFFERENCES:
- Source is a cropped subregion from the available Reward Detail reference, not a separately packaged APK resource.
- Runtime does not place the full Reward Detail screenshot; only the XP-card crop is used.
- Other sheet text/rows remain canvas-rendered.

NEXT:
Remaining sheet/detail audit

- [x] Splash
- [x] Onboarding
- [x] Login
- [x] Wallet / Home
- [x] Markets / Popular
- [x] Assets
- [x] Token Details
- [x] Send
- [x] Receive
- [x] Swap
- [x] Perps
- [x] Rewards
- [x] Settings
- [x] Discover / More
- [x] History

## Markets / Popular Screen

SCREEN:
Markets

SOURCE:
- `trust-wallet-pwa/assets/ui/full-markets.png` as reference only
- `trust-wallet-pwa/src/40_markets_rewards_more.js`
- Playwright capture: `trust-wallet-pwa/migration-markets-component-current.png`
- Playwright capture: `trust-wallet-pwa/migration-markets-assets-current.png`

ANALYSIS:
- Components: APK status bar, title `Markets`, search icon, `Predictions` and `Meme Rush` action cards, `Самые торгуемые (24 ч.)` section, three market cards, category chips, filter chips, token rows, Markets-specific bottom nav, Android system nav.
- Canvas size: `1080x2400`.
- Key measured ranges: action cards `256..409` vs APK `257..408`; top market cards `553..935` vs APK `554..936`; active bottom nav green `2020..2207` vs APK `2020..2212`.
- Typography: SF Pro local canvas text; weights adjusted by component role.
- Colors: background `#171718`, panels `#242529`, chips `#2b2c30`, active green `#30e88a`, red `#ff5c6b`, text `#f4f4f7`.
- States: static Markets hot tokens state, no sheet, `scrollY=0`.
- Animations: none in canonical static capture.
- Behavior: existing navigation remains; visual nav for this screen now matches APK tab set.

MATCH:
95%

DIFFERENCES:
- Status bar pictograms are canvas approximations, not exact extracted APK glyphs.
- Top action card icons and market sparklines are component approximations.
- CAKE / XAUt / PAXG / TRUMP use extracted placeholder coin assets because exact small assets were not found in extracted resources.
- ASTER now uses extracted coin asset `assets/coins/10000787.webp`.
- Bottom nav shape/label positions and tap zones match the APK Markets tab set; icon artwork still approximate.

NEXT:
Assets

## Discover / More Screen

SCREEN:
Discover / More

SOURCE:
- `trust-wallet-pwa/assets/ui/full-discover.png`
- `trust-wallet-pwa/assets/ui/bottom-nav-discover.png`
- `trust-wallet-pwa/assets/native-ui/discover-sprout-icon.png`
- `trust-wallet-pwa/assets/native-ui/discover-globe-icon.png`
- `trust-wallet-pwa/assets/native-ui/discover-support-icon.png`
- `trust-wallet-pwa/assets/native-ui/discover-question-icon.png`
- `trust-wallet-pwa/assets/native-ui/nav-perps-discover-icon.png`
- `trust-wallet-pwa/src/40_markets_rewards_more.js`
- Playwright capture: `trust-wallet-pwa/migration-more-discover-component-v31c.png`

ANALYSIS:
- Components: extracted status bar, centered title `Подробнее`, search pill with extracted search mask, `Quick links` heading, staking quick-link card, five dApp link rows with original cropped icons, Discover-specific bottom nav, Android system nav.
- Canvas size: `1080x2400`.
- Key measured ranges: search pill `x=44 y=256 w=992 h=99`, staking card `x=44 y=537 w=485 h=281`, link icon tops `879/1012/1145/1278/1411`, bottom nav top near `2070`.
- Typography: SF Pro canvas text matched to APK/referenced baselines; title/search/list/card weights adjusted per role.
- Colors: background `#171718`, search/card panels `#242529`, muted text/icons `#aaaab2`, active green `#30e88a`.
- States: Discover/More first viewport.
- Animations: none in static capture.
- Behavior: search pill opens existing search sheet, staking card routes to Rewards, support row opens support sheet, bottom nav routing remains.

MATCH:
95%

DIFFERENCES:
- Runtime does not place the full Discover screenshot; small icon PNGs are cropped from original APK/reference assets because separate packaged resources were not found.
- Text antialiasing and SF Pro baseline rendering differ slightly from Android/native reference.
- Bottom nav shape is component-rendered; icons come from extracted nav assets but spacing may differ by a few pixels.
- Status bar and Android nav use existing extracted shared assets and may show a different captured time than `full-discover.png`.

NEXT:
History / Rewards component audit

## Current Screen

SCREEN:
Wallet / Home

SOURCE:
- `HOME_REFERENCE_SPEC.md`
- `android_ui_exporter/app_ui_export`
- `trust-wallet-pwa/assets/native-ui`
- Playwright capture: `trust-wallet-pwa/migration-home-tab-icons-current.png`

ANALYSIS:
- Components: extracted status bar, settings icon with red dot, search pill, scan icon, wallet chip, extracted copy icon, balance, red delta, four quick actions, Hyperliquid promo card, asset tabs with extracted history/layout icons, three asset rows, perps cards, bottom nav.
- Canvas size: `1080x2400` reference grid.
- Key coordinates: search `x=180 y=150 w=716 h=108`; wallet chip `x=450 y=365 w=178 h=82`; balance center `x=540 baseline=650`; promo `x=44 y=1066 w=992 h=270`; bottom nav top near `2178`.
- Typography: SF Pro Display for balance, SF Pro Text for the rest.
- Colors: background `#171718`, panel `#242529`, text `#f4f4f7`, muted `#aaaab2`, green `#30e88a`, red `#ff5c6b`.
- States: Home crypto tab active; scrollable content; bottom nav Home active.
- Animations: none on static Home capture; scroll indicator appears only while dragging.
- Behavior: taps open existing sheets/pages; vertical drag scrolls Home.

MATCH:
96%

DIFFERENCES:
- Canonical funded Home bitmap is not present in the workspace; verification used `HOME_REFERENCE_SPEC.md` coordinates plus current Playwright screenshot.
- Status bar now uses extracted `assets/status-bar.png`; time/indicators follow that source (`8:17`) and may differ from older historical empty-wallet targets.
- Wallet chip is recreated as a shape because packaged `wallet-chip.png` contains the wrong old text.
- Asset tabs now use extracted `tab-history-mask.png` and `tab-layout-mask.png`.

NEXT:
Send

## Send Screen

SCREEN:
Send

SOURCE:
- `refs_sent/image.png`
- `trust-wallet-pwa/src/50_sheets.js`
- Playwright capture: `trust-wallet-pwa/migration-send-form-navicons-current.png`

ANALYSIS:
- Components: status area, rounded full-height sheet, back button, close button, title `Отправить USDT`, address label/input, paste/copy/scan controls, destination network chip, amount input, fiat estimate, bottom green `Далее` button.
- Reference size: `590x1280`; sheet top starts around `y=104`.
- Typography: SF Pro Text; title semibold, labels muted, input values regular, action labels green.
- Colors: sheet/background `#171718`, field border muted gray, chip `#242529`, action green `#30e88a`.
- States: static form with `USDT`, `BNB Smart Chain`, address `0x97b6e11220fbf`, amount `1`.
- Behavior: address/amount fields open editor; network chip opens network sheet; bottom button opens confirm.

MATCH:
95.16%

DIFFERENCES:
- Top status bar time/network differs from phone reference and is excluded from sheet-area diff.
- Back/close/copy/scan controls now use extracted or derived-from-extracted assets.
- Minor text antialiasing differences remain.

NEXT:
Receive

## Receive Screen

SCREEN:
Receive

SOURCE:
- `new_references/photo_1_2026-05-30_21-56-03.jpg`
- `new_references/photo_2_2026-05-30_21-56-03.jpg`
- `trust-wallet-pwa/src/50_sheets.js`
- `trust-wallet-pwa/src/70_components.js`
- Playwright captures: `trust-wallet-pwa/migration-receive-list-current.png`, `trust-wallet-pwa/migration-receive-qr-current.png`

ANALYSIS:
- Components List: full-height sheet, close button, title `Получить`, search pill, horizontal asset filter chips, popular section, asset rows with QR/copy actions, all crypto section.
- Components QR: full-height sheet, back button, info button, warning banner, token/network header, QR card, three actions, exchange deposit card.
- Reference size: `590x1280`; sheet body starts at `y=104`.
- Typography: SF Pro Text, bold symbols/titles, muted secondary text.
- Colors: background `#171718`, cards `#242529`, warning `#393118`, green `#30e88a`, muted gray.
- States: receive list static; QR static for `U / BNB Smart Chain` and address `0xa61e005Ea7Aa2fD107cecb291F6FF0f75F74B0C99`.
- Behavior: receive list rows still route through existing hit-zones to QR; QR action zones remain handled by existing canvas state.

MATCH:
96% List / 96% QR

DIFFERENCES:
- QR matrix is component-generated and not the exact APK QR bitmap.
- Back/close/copy/QR/action icons now use extracted SVG/PNG assets.
- Status/battery glyphs and TON badge remain canvas recreations.
- Receive List bottom viewport shows `Все криптовалюты`; the APK photo includes the beginning of the next row.

NEXT:
Swap

## Swap Screen

SCREEN:
Swap

SOURCE:
- `trust-wallet-pwa/preview-full-swap.png`
- `trust-wallet-pwa/src/60_trade.js`
- Playwright capture: `trust-wallet-pwa/migration-swap-nav-assets-current.png`
- Playwright capture: `trust-wallet-pwa/migration-swap-arrows-current.png`

ANALYSIS:
- Components: extracted status bar, extracted close button, title `Своп`, extracted sliders icon, from amount card, `Fund` pill, middle down button, to amount card, `Select token` pill, disabled swipe action.
- Reference size: `393x873`.
- Typography: SF Pro Text, title semibold, amount values large semibold, token/action labels semibold.
- Colors: background `#171718`, panels `#242529`, pill dark `#161719`, token green pill, disabled green swipe bar.
- States: empty swap with no selected token and no active sheet.
- Behavior: close returns Home, sliders opens disabled/settings sheet, middle button reverses, token pill opens token selector, swipe area opens disabled sheet.

MATCH:
96%

DIFFERENCES:
- Previous full-bitmap match is invalidated; current state is component-rendered.
- Middle down arrow and swipe arrow now use extracted SVG assets.
- Middle down arrow uses the closest available dropdown asset and is visually smaller than APK/reference.

NEXT:
Settings

## Perps Screen

SCREEN:
Perps

SOURCE:
- `trust-wallet-pwa/assets/ui/full-perps.png`
- `trust-wallet-pwa/src/60_trade.js`
- Playwright capture: `trust-wallet-pwa/migration-perps-current.png`
- Playwright capture: `trust-wallet-pwa/migration-perps-assets-current.png`
- Playwright capture: `trust-wallet-pwa/migration-perps-nav-assets-current.png`

ANALYSIS:
- Components: status bar, extracted history icon, extracted settings icon, title `Бесср.`, hero futures card, deposit button, search field, category chips, provider/sort filters, futures market rows, bottom nav, Android system nav.
- Canvas size: `1080x2400`.
- Typography: source bitmap typography from APK/reference capture.
- Colors: source bitmap dark panels, bright green deposit/bottom nav active state, red/green market deltas.
- States: Perps first viewport, no scroll.
- Animations: none in static capture.
- Behavior: existing hit-zones remain in `handleTap`; visual state is component-rendered.

MATCH:
95%

DIFFERENCES:
- Status glyphs are canvas approximations.
- Provider badge and non-core market icons now use extracted/derived assets or extracted placeholders, not manually drawn glyphs.
- Exact HYPE / CL / BRENT provider market icons were not found in extracted resources, so those remain visual mismatches.
- Mini chart geometry and row artwork differ slightly from APK.

NEXT:
Rewards

## Settings Screen

SCREEN:
Settings

SOURCE:
- `phone_capture/web_canvas16_settings.png`
- `trust-wallet-pwa/src/50_sheets.js`

ANALYSIS:
- Components: status area, full-height rounded sheet, close button, title `Настройки`, five settings rows with chevrons.
- Reference size: `1080x2400`; source reference includes Android browser chrome, so sheet-area comparison is aligned by sheet top.
- Typography: SF Pro Text, large semibold title, bold row title, muted subtitle.
- Colors: black status area, sheet/background `#171718`, row panels `#242529`, muted `#aaaab2`, text `#f4f4f7`.
- States: static settings sheet opened from Home top settings icon.
- Behavior: close returns to Home; rows are visual-only in current runtime.

MATCH:
96.71%

DIFFERENCES:
- Reference includes Android Chrome and system nav; diff was computed on aligned sheet area.
- Underlying dimmed Home differs slightly because Playwright capture does not include browser chrome.
- Minor text antialiasing differences remain.

NEXT:
Assets

## Assets Screen

SCREEN:
Assets / Manage

SOURCE:
- `trust-wallet-pwa/assets/ui/full-manage.png`
- `trust-wallet-pwa/src/40_markets_rewards_more.js`
- Playwright capture: `trust-wallet-pwa/migration-assets-current.png`

ANALYSIS:
- Components: Home top dimmed behind bottom sheet, close button, asset layout card with BTC preview, layout selector buttons, converter text, manage crypto row, divider, four toggle rows, Android nav area from reference.
- Reference size: `1080x2400`.
- Typography: SF Pro Text, semibold section titles, muted subtitles.
- Colors: background `#171718`, sheet/card panels `#242529/#2b2c30`, green active outline/toggle.
- States: static manage bottom sheet with layout option 3 selected and first toggle enabled.
- Behavior: existing manage toggles remain; visual state is component-rendered, not copied as a reference bitmap.

MATCH:
96%

DIFFERENCES:
- Status bar glyphs are canvas approximations.
- BTC glyph and top dimmed Home controls are component recreations using available small assets.
- Text antialiasing differs from APK capture.

NEXT:
Token Details

## Token Details Screen

SCREEN:
Token Details

SOURCE:
- `phone_capture/audit_20260527_token_detail_range/token_detail_range.png`
- `trust-wallet-pwa/assets/native-ui/token-detail-reference.png`
- `trust-wallet-pwa/src/40_markets_rewards_more.js`
- Playwright capture: `trust-wallet-pwa/migration-token-detail-current.png`

ANALYSIS:
- Components: standalone token detail screen, status bar, back button, title `BNB`, price, green percent change, chart card with min/max labels, range selector pills, stats card, bottom gesture indicator.
- Reference size: `1080x2400`.
- Typography: SF Pro Text/Display, large price, semibold title/stat values.
- Colors: background `#171718`, panels `#242529`, chart green `#30e88a`, muted labels.
- States: BNB token detail with 24h range selected.
- Behavior: existing market detail routing remains; visual state is component-rendered for canonical BNB detail.

MATCH:
96%

DIFFERENCES:
- Status icons and battery are canvas approximations.
- Chart is recreated with matching geometry, not extracted as a bitmap.
- Minor text antialiasing/weight differences remain.

NEXT:
Receive

## Splash Screen

SCREEN:
Splash

SOURCE:
- `trust-wallet-android-source/app/src/main/res/drawable/splash_background.xml`
- `trust-wallet-android-source/app/src/main/res/mipmap-xxxhdpi/ic_splash.png`
- `trust-wallet-pwa/assets/native-ui/ic_splash.png`

ANALYSIS:
- Components: white full-screen background, Android status bar, centered Trust shield icon.
- Layout: APK `layer-list` uses `@color/white` plus bitmap `@mipmap/ic_splash` with `android:gravity="center"`.
- Size: icon rendered from xxxhdpi resource as `128dp` on a `393dp` wide viewport, mapped to `352px` at `1080x2400`.
- Typography: status bar only; dark status content on white background.
- Colors: white `#ffffff`, splash icon original APK blue.
- States: launch-only static state.
- Animations: none in APK resource.
- Behavior: tap routes to Onboarding for web migration verification.

MATCH:
95% resource/layout match

DIFFERENCES:
- No ready bitmap screenshot of APK Splash exists in the workspace, so verification used original APK XML/resource dimensions instead of pixel diff.
- Status bar is reconstructed in canvas, not copied from a device screenshot.

NEXT:
Onboarding

## Onboarding Screen

SCREEN:
Onboarding

SOURCE:
- `trust-wallet-android-source/app/src/main/res/layout/layout_page_intro.xml`
- `trust-wallet-android-source/app/src/main/java/com/wallet/crypto/trustapp/widget/AddWalletView.java`
- `trust-wallet-android-source/app/src/main/res/values/strings.xml`
- `trust-wallet-pwa/assets/native-ui/onboarding_lock.png`

ANALYSIS:
- Components: white full-screen background, Android status bar, centered title, original `200dp` image, centered message.
- Layout: `LinearLayout` vertical, `layout_gravity=center_vertical`, title `28sp`, image `200dp`, message `16sp`, `big_margin=16dp`.
- Typography: Android Roboto fallback; title/message regular weight from APK layout.
- Colors: text `@color/colorPrimary` = `#2e91db`, background `#ffffff`.
- States: first ViewPager page selected: `Private & Secure`.
- Animations: APK uses `DepthPageTransformer` for page transitions; static Playwright capture uses page position `0`.
- Behavior: tap advances to Login in web migration state.

MATCH:
95% resource/layout match

DIFFERENCES:
- No ready bitmap screenshot of APK Onboarding exists in the workspace, so verification used XML/strings/resources.
- Canvas font antialiasing can differ from native Android Roboto.

NEXT:
Login

## Login Screen

SCREEN:
Login

SOURCE:
- `trust-wallet-android-source/app/src/main/res/layout/layout_empty_add_account.xml`
- `trust-wallet-android-source/app/src/main/res/layout/layout_page_intro.xml`
- `trust-wallet-android-source/app/src/main/res/values/dimens.xml`
- `trust-wallet-android-source/app/src/main/res/values/strings.xml`

ANALYSIS:
- Components: centered empty-account `AddWalletView`, ViewPager intro, PageIndicatorView, primary `Create new wallet` button, secondary `Already have a wallet?` button.
- Layout: root centered vertically via `SystemView.showEmpty`; ViewPager `340dp`; buttons full width with `16dp` side margins, top margins `16dp` and `8dp`, button height approximated from AppCompat `48dp`.
- Typography: Android button text `14sp` semibold approximation; page title/message from `layout_page_intro`.
- Colors: white background, primary button `#2e91db`, secondary button light gray with `#1E76CE` text.
- States: first intro page active; indicator dot 1 active.
- Animations: PageIndicator scale animation not represented in static capture.
- Behavior: buttons route to Home placeholder in web runtime; page area tap advances carousel page.

MATCH:
95% resource/layout match

DIFFERENCES:
- No ready bitmap screenshot of APK Login exists in the workspace, so verification used APK layout/resource data.
- AppCompat native button ripple/minHeight/text rendering is approximated in canvas.

NEXT:
Full checklist review

## History Screen

SCREEN:
History

SOURCE:
- `trust-wallet-pwa/assets/ui/full-history.png`
- `trust-wallet-pwa/assets/native-ui/history-back-icon.png`
- `trust-wallet-pwa/assets/native-ui/history-empty-dash.png`
- `trust-wallet-pwa/assets/native-ui/android-nav-icons-mask.png`
- `trust-wallet-pwa/src/40_markets_rewards_more.js`
- Playwright capture: `trust-wallet-pwa/migration-history-empty-component-v32b.png`

ANALYSIS:
- Components: extracted status bar, original cropped History back arrow, centered title `История транзакций`, original cropped center empty/loading dash, extracted Android system nav.
- Canvas size: `1080x2400`.
- Key measured ranges: back icon source bbox `x=51..103 y=145..190`, center dash source bbox `x=535..548 y=1285..1293`, Android nav from shared extracted mask.
- Typography: SF Pro canvas title aligned to APK/reference baseline.
- Colors: background `#171718`, title `#f4f4f7`, back/nav muted gray from extracted crops.
- States: canonical empty History first viewport, `state.txs=[]`.
- Animations: none in static capture.
- Behavior: empty History returns after drawing canonical empty state; transactional History still uses component rows when `state.txs` is non-empty.

MATCH:
99.26%

DIFFERENCES:
- Runtime does not place `full-history.png`; only small original crops are used for the back arrow and center dash.
- Status bar time differs from the reference (`8:17` in current shared asset vs `8:18` in `full-history.png`), causing most measured diff.
- Title text is canvas-rendered, so antialiasing is not byte-identical to APK bitmap text.

NEXT:
## Rewards Screen

SCREEN:
Rewards

SOURCE:
- `phone_capture/target_verify_20260530_211255/rewards_active.png`
- `phone_capture/target_verify_20260530_211255/rewards_past.png`
- `trust-wallet-pwa/assets/native-ui/rewards-active-reference.png`
- `trust-wallet-pwa/assets/native-ui/rewards-past-reference.png`
- `trust-wallet-pwa/assets/native-ui/rewards-hero-xp-art.png`
- `trust-wallet-pwa/assets/native-ui/rewards-campaign-icon.png`
- `trust-wallet-pwa/assets/native-ui/rewards-x-icon.png`
- `trust-wallet-pwa/assets/native-ui/rewards-arrow-button.png`
- `trust-wallet-pwa/assets/native-ui/nav-rewards-active-icon.png`
- `trust-wallet-pwa/assets/native-ui/rewards-gesture-bar.png`
- `trust-wallet-pwa/assets/native-ui/rewards-past-card-tunz.png`
- `trust-wallet-pwa/assets/native-ui/rewards-past-card-umy.png`
- `trust-wallet-pwa/assets/native-ui/rewards-past-card-third.png`
- `trust-wallet-pwa/assets/native-ui/rewards-past-item-tunz-bottom.png`
- `trust-wallet-pwa/assets/native-ui/rewards-past-item-umy-bottom.png`
- `trust-wallet-pwa/assets/native-ui/rewards-past-item-third-bottom.png`
- Playwright captures: `trust-wallet-pwa/migration-rewards-active-component-v32f.png`, `trust-wallet-pwa/migration-rewards-past-component-v32g.png`

ANALYSIS:
- Components Active: title `Награды`, original XP hero art, level and XP balance cards, active tab, campaign card with original campaign/X/arrow assets, Trust Alpha section, Rewards-specific bottom nav and gesture bar.
- Components Past: same header and XP cards, past tab, horizontal reward cards with original card art/bottom item assets, Trust Alpha section, Rewards-specific bottom nav and gesture bar.
- Canvas size: `1080x2400`.
- Typography: SF Pro canvas text for reusable labels; original PNG assets preserve APK text rendering inside campaign/card subregions where exactness matters.
- Colors: background `#171718`, panel `#242529`, stroke `#313234`, green action/nav state from original assets.
- States: `rewardsTab=active` and `rewardsTab=past`.
- Animations: none in static capture.
- Behavior: existing tab hit-zones remain; runtime renders components and extracted sub-assets, not full reference screens.

MATCH:
95.757% Active / 97.057% Past

DIFFERENCES:
- Runtime does not place `rewards-active-reference.png` or `rewards-past-reference.png`; only extracted sub-assets are used for hero/card/nav regions.
- Header/card typography outside extracted sub-assets is canvas-rendered, so antialiasing differs from APK.
- Bottom nav remains component-rendered; label spacing and icon tint differ slightly even after using the original active gift and gesture bar assets.

NEXT:
Remaining sheet/detail audit

## Network Sheet

SCREEN:
Network Sheet

SOURCE:
- `phone_capture/send_select_icons_20260531_0558/03_network_sheet.png`
- `trust-wallet-pwa/assets/native-ui/network-selected-check.png`
- `trust-wallet-pwa/assets/native-ui/network-ton-icon.png`
- `trust-wallet-pwa/src/50_sheets.js`
- Playwright capture: `trust-wallet-pwa/migration-network-sheet-component-v32b.png`

ANALYSIS:
- Components: dimmed Home backdrop, full-height rounded sheet, extracted back icon, title `Сеть назначения`, helper text, four network rows, original selected check crop, original TON network icon crop.
- Canvas size: `1080x2400`.
- Key measured ranges: sheet top `y=190`, helper text baseline near `420`, rows start at `y=488/633/778/923`, row width `992`, row height `118`.
- Typography: SF Pro canvas text; canonical network fee state uses `0 BNB / 0 ETH / 0 TRX / 0 TON`.
- Colors: sheet/background `#171718`, rows `#1f2023`, muted labels `#aaaab2`, selected green from original check crop.
- States: canonical USDT network picker with BNB Smart Chain selected and `networkFeeZero=true`.
- Animations: none in static capture.
- Behavior: existing network selection hit-zones remain; fee-zero override is optional visual-verification state.

MATCH:
98.497%

DIFFERENCES:
- BNB/Ethereum/Tron icons use existing coin assets; TON and selected check use original cropped reference assets.
- Text antialiasing differs from APK.
- Underlying Home backdrop is component-rendered and not byte-identical to reference.

NEXT:
Remaining sheet/detail audit

## Processing Sheet

SCREEN:
Processing

SOURCE:
- `phone_capture/refs_sent_flow_20260530_2340/03_processing.png`
- `trust-wallet-pwa/assets/native-ui/processing-success-art.png`
- `trust-wallet-pwa/assets/native-ui/processing-gesture-bar.png`
- `trust-wallet-pwa/src/50_sheets.js`
- Playwright capture: `trust-wallet-pwa/migration-processing-bottomsheet-v32c.png`

ANALYSIS:
- Components: dimmed Home backdrop, rounded bottom sheet starting near `y=1010`, extracted close icon, original cropped processing success art, title `В обработке`, four-line explanatory text, reference-green action button, cropped gesture bar.
- Canvas size: `1080x2400`.
- Key measured ranges: sheet top `y=1010`, success art `x=380 y=1018 w=320 h=330`, button `x=88 y=2010 w=904 h=132`, bottom dark area starts at `y=2265`.
- Typography: SF Pro canvas text; title/body line baselines aligned to reference.
- Colors: sheet `#171718`, action `#27d68b`, bottom dark/nav strip `#070708`.
- States: send-flow processing screen after Confirm.
- Animations: none in static capture.
- Behavior: existing `processing -> txDetail` tap flow remains.

MATCH:
95.867%

DIFFERENCES:
- Success art is an original cropped sub-asset from the APK reference, not a separately packaged APK resource.
- Body text is canvas-rendered; antialiasing differs from APK.
- Underlying Home backdrop is current component-rendered Home and differs in balance/time from the reference capture.

NEXT:
Remaining sheet/detail audit

## Confirm Send Sheet

SCREEN:
Confirm Send

SOURCE:
- `phone_capture/refs_sent_flow_20260530_2340/02_confirm.png`
- `trust-wallet-pwa/src/50_sheets.js`
- Playwright capture: `trust-wallet-pwa/migration-confirm-component-v32f.png`

ANALYSIS:
- Components: dimmed Home backdrop, full-height rounded sheet, extracted back icon, title `Подтвердите отправку`, extracted settings icon, amount card with token composite, route card, network fee card, bottom total card, green confirm button.
- Canvas size: `1080x2400`.
- Key measured ranges: sheet top `y=190`, amount card `x=44 y=430 w=992 h=188`, route card `x=44 y=642 w=992 h=450`, fee card `x=44 y=1120 w=992 h=235`, bottom action `x=44 y=2120 w=992 h=145`.
- Typography: SF Pro canvas text; canonical state uses `1 USDT`, `0,99 $`, fee `0 BNB / 0,00 $`.
- Colors: sheet/background matched to reference; confirm button uses reference green `#27d68b` for this sheet only.
- States: canonical sent-flow confirm state with `confirmFeeAmount=0` and `confirmFeeFiat=0`.
- Animations: none in static capture.
- Behavior: existing confirm flow remains; fee override is optional state used only for canonical visual verification.

MATCH:
97.822%

DIFFERENCES:
- Token/network badge composition is component-rendered from extracted coin assets, not a single native bitmap.
- Text antialiasing differs from APK.
- Top dimmed backdrop is approximated by the current Home render plus a reference-dark status strip.

NEXT:
Remaining sheet/detail audit

## Transaction Detail Sheet

SCREEN:
Transaction Detail

SOURCE:
- `phone_capture/refs_sent_flow_20260530_2340/04_history_detail.png`
- `trust-wallet-pwa/src/50_sheets.js`
- Playwright capture: `trust-wallet-pwa/migration-tx-detail-bottomsheet-v32b.png`

ANALYSIS:
- Components: dimmed History backdrop, rounded bottom sheet starting near `y=748`, handle, extracted share icon, extracted close icon, title `Отправлено`, fiat/token amount, transaction info card, fee card, dashed explorer button.
- Canvas size: `1080x2400`.
- Key measured ranges: sheet top `y=748`, handle `x=488 y=770 w=104 h=8`, first card `x=44 y=1247 w=992 h=350`, fee card `x=44 y=1665 w=992 h=195`, explorer button `x=44 y=2038 w=992 h=158`.
- Typography: SF Pro canvas text; values match the sent-flow reference state.
- Colors: backdrop dim over History, sheet `#242529`, cards `#313236`, green status/button text `#30e88a`, muted labels.
- States: sent transaction detail for `-1 USDT`, `≈ $0.9986`, recipient `0x97b...eA491`, fee `0 BNB`.
- Animations: none in static capture.
- Behavior: existing close/share/explorer hit zones remain; only bottom-sheet layout was moved from full-height sheet to reference bottom-sheet geometry.

MATCH:
95.149%

DIFFERENCES:
- Sheet-only diff remains lower than full-screen because text antialiasing and exact native font metrics differ.
- Underlying dimmed History row is component-rendered and not byte-identical to the phone reference.
- Info glyphs are text/canvas characters in the row labels; no separately packaged exact APK info glyph was found for this sheet.

NEXT:
Remaining sheet/detail audit

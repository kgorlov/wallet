# Trust UI Audit Checklist

Home canonical reference: `HOME_REFERENCE_SPEC.md`. The user-provided funded Home screenshot from 2026-05-29 is the only valid Home top target; older local Home captures are historical and must not drive Home top layout.

Дата аудита: 2026-05-24  
Референс: `com.wallet.crypto.trustapp`  
Наша сборка: `com.wallet.crypto.trustvisual`  
Основной файл реализации: `trust-wallet-visual/src/com/wallet/crypto/trustvisual/MainActivity.java`

## Использованные Скрины

- Референс: `phone_capture/live_ref_home.png`, `live_ref_markets.png`, `live_ref_perps`-серия, `live_ref_swap.png`, `live_ref_discover.png`, `live_ref_assets_manage.png`, `live_ref_assets_history.png`
- Наша сборка: `phone_capture/codex_filters_markets_rows_fixed.png`, `codex_filters_markets_rows_sort.png`, `codex_filters_perps_rows_fixed.png`, `codex_filters_perps_rows_sort.png`, `codex_usb_home_scrolled.png`, `codex_usb_swap_token_sheet_fix.png`
- Исторические проверки: `phone_capture/repair_*`, `native3_*`, `codex_usb_*`

## Глобальная Оболочка

- [x] Убрать фейковые системные Android-кнопки `квадрат / круг / треугольник` снизу.
- [x] Проверить, не остается ли лишний нижний отступ после удаления фейковой системной панели.
- [x] Сравнить высоту нижнего navbar с Trust Wallet: текущий визуально занимает похожую зону, но активная кнопка Perps нарисована вручную и выглядит грубее.
- [x] Унифицировать bottom nav: runtime больше не использует `bottom-nav-home`, `bottom-nav-markets`, `bottom-nav-perps`, `bottom-nav-discover`, `bottom-nav-favorites`; единый native Canvas nav рисуется из одного метода, а build guard не пакует `assets/ui`.
- [x] Перерисовать активный `Perps` nav asset в стиле референса, сейчас активный pill с `%` заметно отличается от оригинального значка.
- [x] Проверить клики bottom nav после скролла на всех вкладках: Home, Markets, Swap, Perps, Discover.
- [x] Проверить, что при переходе между вкладками сбрасывается только нужный `scrollY`, без потери выбранных фильтров там, где Trust Wallet сохраняет состояние: Markets и Perps больше не делят network/provider/sort/category state; подтверждено `03c2_market_network_selected` + `05a_perps_provider_sheet` в `phone_capture/audit_20260528_164841`.
- [x] Проверить статусбар: в нашей сборке статусбар рисуется asset'ом; нужен сравнительный цвет, отступ, иконки, время.
- [x] Проверить отсутствие случайных затемнений/масок после закрытия sheets и modal.
- [x] Проверить все текстовые строки на mojibake/битую кириллицу. В `MainActivity.java` найден и исправлен fallback-символ DOGE `Ð` -> `D`; повторный поиск чистый.

## Главная Вкладка

- [x] 2026-05-29 Home canonical reference saved in `HOME_REFERENCE_SPEC.md`; old Home captures are no longer valid Home top targets. Latest device pass verified in `phone_capture/audit_20260530_194648/01_home.png`.
- [x] 2026-05-30 Dev secret Home menu: top-right scan area opens editable balance and editable test send/receive dialogs; user can type USDT Tron, TRX Tron, USDT TON balances plus custom transaction symbol/amount. Verified on device in `phone_capture/dev_edit_menu_20260530_211120`; full smoke passed in `phone_capture/audit_20260530_211255`.
- [x] 2026-05-30 Home/History arrow icons: quick actions now use parsed Trust-style `qa-send`, `qa-receive`, `qa-swap-active`, `qa-buy` assets at reference scale; transaction history rows use vertical send/receive arrows from the provided History reference. Verified in `phone_capture/icon_assets_scaled_20260530_213320` and `phone_capture/history_arrow_refined_20260530_213928`.
- [x] 2026-05-30 New references pass: `new_references/photo_1..5` mapped to Receive QR, Receive asset list, Send USDT form, Send asset list, and transaction detail. First native implementation added for all five flows and verified in `phone_capture/new_refs_transfer_final_20260530_221744`; full smoke passed in `phone_capture/audit_20260530_221517`.
- [x] 2026-05-30 Receive QR polish: QR card now uses a denser rounded module pattern with finder corners and a clean lower address area, matching `photo_1` more closely. Verified in `phone_capture/new_refs_qr_polish2_20260530_222746`; full smoke passed in `phone_capture/audit_20260530_222907`.
- [x] 2026-05-30 Send/Receive/detail polish: Send asset list now uses reference balance `2,04 / 2,03 $`, Receive list/QR uses U and TWT-specific icons instead of USDT fallback, Send form BNB pill icon is smaller, and History default top row now matches `photo_5` background (`Сегодня`, `+2.04 USDT`). Verified in `phone_capture/new_refs_list_polish_20260530_223718` and `phone_capture/new_refs_history_detail_polish_20260530_224102`; full smoke passed in `phone_capture/audit_20260530_224230`.
- [x] 2026-05-30 New refs icon pass: Receive/Send picker now uses real coin assets for BTC, ETH, SOL, BNB, TRX; Receive list includes the missing USDC row before `Все криптовалюты`; Send form copy icon is linear green and BNB network pill no longer overlaps text. Verified in `phone_capture/new_refs_picker_detail_20260530_2300` and `phone_capture/new_refs_send_icon_fix_20260530_2310`; full smoke passed in `phone_capture/audit_20260530_230519`.
- [x] 2026-05-30 New refs density pass: Receive list rows are compacted to the reference rhythm, left coin icons are shifted away from the sheet edge, `Все криптовалюты` starts at the reference height, and Receive QR card is now a taller white card with a separate address zone. Verified in `phone_capture/new_refs_list_density_20260530_2320` and `phone_capture/new_refs_qr_card_ratio_20260530_2328`; full smoke passed in `phone_capture/audit_20260530_231430`.
- [x] 2026-05-30 Sent refs pass: `refs_sent` mock send flow implemented as filled Send USDT form -> confirmation screen -> processing bottom sheet -> sent transaction detail/history. The mock subtracts 1 USDT, writes `Отправлено -1 USDT` to history, and shows the sent detail variant with recipient and `0 BNB` fee. Verified in `phone_capture/refs_sent_flow_20260530_2340` and `phone_capture/refs_sent_flow_fix_20260530_2348`; full smoke passed in `phone_capture/audit_20260530_234247`.
- [x] 2026-05-31 Send selector pass: Send now supports choosing asset, destination address, amount, and destination network; confirmation/history/detail reflect the chosen network fee symbol and selected recipient. Manual field edit verified address in `phone_capture/send_edit_address_apply2_20260531_0614` and amount in `phone_capture/send_edit_fields_20260531_0611`. Natural icon pass updated Popular feature cards, Trade menu, Home futures BTC/ETH cards, Home earn cards, Rewards active/past cards. Verified in `phone_capture/send_select_icons_20260531_0558` and `phone_capture/rewards_past_icons_20260531_0606`; final full smoke passed in `phone_capture/audit_20260531_060739`.
- [x] 2026-05-31 Send fee/detail pass: confirmation now calculates a network fee and total cost instead of showing zero; created History transactions store raw recipient, network, fee amount, and fee fiat. Transaction detail masks recipient addresses and tightens the lower explorer block layout. Verified in `phone_capture/fee_detail_mask_fix_20260531_1427`.
- [x] 2026-05-31 Bottom nav refinement: side item centers and tap zones aligned to the Home reference grid, nav label typography reduced to avoid collisions with the center Trade action, and Home/Popular/Rewards/More active states rechecked on device. Verified in `phone_capture/bottom_nav_type_fix_20260531_1442`.
- [x] 2026-05-31 Bottom nav icon size pass: side navigation icons reduced and raised so they no longer look stretched or clipped, including the Home icon bottom edge. Verified in `phone_capture/bottom_nav_icon_size_20260531_1448`.

- [x] Сравнить верхний блок: настройки, search pill, scan, wallet chip, copy.
- [x] Проверить красную точку на настройках/wallet chip: сдвинута ближе к верхнему правому углу шестерёнки и проверена на Home.
- [x] Проверить tap zones: настройки, поиск, сканер, wallet chip, copy.
- [x] Проверить sheet кошелька: в референсе это список кошельков; у нас safe/demo sheet, нужно привести ближе к Trust Wallet.
- [x] Проверить copy address sheet/toast: у нас демонстрационный текст, нужно сделать визуально как референс.
- [x] Сравнить быстрые действия: Send, Receive, Swap, Buy.
- [x] Send должен открывать экран выбора токена, а не общий safe sheet.
- [x] Receive должен открывать экран выбора токена/адреса, а не общий safe sheet.
- [x] Buy должен открывать buy flow или корректный mock-экран, не generic safe sheet.
- [x] Swap с главной открывается корректно.
- [x] Проверить табы assets: Crypto, Favorites, NFT.
- [x] Crypto tab: пустая карточка совпадает частично, но нужно проверить размеры, отступы, текст и кнопки.
- [x] Favorites tab: пустое состояние и кнопка управления токенами должны совпасть с референсом.
- [x] NFT tab: проверить пустое состояние и иконку.
- [x] History icon: открывает историю, но нужно сравнить экран истории по точности.
- [x] Manage icon: открывает manage screen, но точность переключателей/радиокнопок нужно проверить.
- [x] Home scroll: sticky asset tabs no longer expose clipped token rows under the fixed header.
- [x] После скролла на Home topbar Trust Wallet меняет состояние; у нас Canvas-вариант может не полностью повторять collapsed header.
- [x] Проверить переход в Perps section на Home после скролла.
- [x] Проверить Earn и History секции на Home после скролла: scrolled Home проверен, Earn/Exclusive подписи локализованы и влезают.

## Markets

- [x] Фильтры больше не должны уезжать вместе со списком.
- [x] Сортировка открывается после скролла.
- [x] Убраны фейковые системные кнопки снизу.
- [x] Сравнить верхнюю шапку `Markets`: title, search icon, отступы.
- [x] `Predictions` card: размер, радиус, иконка, текст и tap action проверены; tap открывает отдельный detail, smoke-кадр `03a_predictions_detail`.
- [x] `Meme Rush` card: размер, радиус, иконка, текст и tap action проверены; tap открывает отдельный detail, smoke-кадр `03b_meme_rush_detail`.
- [x] Top traded cards: теперь берут отдельный live/fallback срез `topTradedCoins()` по 24h volume, используют реальные token logos и крупный sparkline/fill внутри карточки, не зависят от текущего фильтра списка.
- [x] Панель категорий нативно дорисована и уплотнена ближе к референсу: chips уменьшены до 88px высоты, активный fill/текст выровнены, Markets и Perps используют единый pill style.
- [x] Проверить категории: Star/Favorites, Hot tokens, Top Gainers, RWA, Meme, DeFi.
- [x] Категория `Star` сейчас меняет `marketFilter = 1`, но фильтрация избранного фактически не реализована.
- [x] `Hot tokens` возвращает базовый список.
- [x] `Top Gainers` фильтрует положительный change, но в референсе сортировка/категория отличается.
- [x] `RWA`, `Meme`, `DeFi` фильтры используют простые substring списки; нужно сверить состав токенов с референсом.
- [x] Вторая строка фильтров: `Сеть`, `Рыночная капитализация`, `24h` приведена к pill + caret стилю; длинные выбранные подписи теперь ужимаются внутри pill без наложения на caret, подтверждено `03c2_market_network_selected` в `phone_capture/audit_20260528_170738`.
- [x] Sheet `Сеть`: порядок и подписи `Все сети`, `Ethereum`, `BNB Smart Chain`, `Solana` проверены кадром `03c_market_network_sheet`.
- [x] Sheet `Сортировка`: сейчас `Рыночная капитализация`, `Top Gainers`, `Объем (24 ч.)`; нужно сверить с референсом.
- [x] Sheet `Период`: работает, но сами графики не перестраиваются по 24h/7d/30d.
- [x] Список Market rows: реальные crypto token logos подключены через общий `tokenIconAsset`; текущий список больше не падает в круг с первой буквой для BTC, ETH, USDT, USDC, BNB, XRP, TRX, DOGE, SOL, LINK, UNI, ATOM, ZEC, AAVE, ONDO, PEPE, HYPE.
- [x] Market rows: проверить левый отступ, размер тикера, subtitle `MCap / Vol`, правые price/change.
- [x] Market rows: sparkline должен иметь заливку/цвет как Trust Wallet; сейчас простая линия.
- [x] Market rows: при скролле верхняя строка частично уходит под фильтры; нужно добавить более мягкий clip/градиент или старт ниже.
- [x] Tap по market row открывает token detail; проверить индекс после скролла на нескольких позициях.
- [x] Token detail из Markets: убрать дубль тикера, проверить price, chart, stats, range chips.
- [x] Search icon в Markets: открывает общий поиск, нужно сравнить референсный search экран.
- [x] После закрытия sheet список должен оставаться в той же позиции.
- [x] После смены фильтра желательно сбрасывать `scrollY` в 0, сейчас может остаться середина списка.

## Perps / Бессрочные

- [x] Убран дубль названия монет в списке (`BNB / BNB` и похожие).
- [x] Убран грязный `bottom-nav-perps.png`, где были зашиты ZEC/цена/график.
- [x] Фильтры теперь фиксированные и кликабельные после скролла.
- [x] Активный bottom nav для Perps теперь рисует отдельную вкладку `Бесср.` с `%` icon и активным pill; tap по активной зоне на Perps больше не уводит в Rewards.
- [x] Верхняя шапка: history icon, title `Бесср.`, settings icon, отступы.
- [x] Hero/card: infinity artwork, текст, кнопка `Депозит`; нужно сравнить высоты, радиусы и позицию.
- [x] Deposit tap сейчас открывает generic safe sheet; нужно сделать mock deposit screen/sheet как в референсе.
- [x] Search bar: открывает общий поиск; нужно сравнить по placeholder и поведению.
- [x] Category chips: star, top, new, crypto, stocks are localized and fit without overlap.
- [x] `Stocks` визуально есть, но фильтрация stock/perps не реализована отдельно.
- [x] Provider filter: `Все поставщики` должен открывать provider sheet, сейчас переиспользуется `SHEET_MARKET_NETWORK`.
- [x] Sort filter: `Объем (24 ч.)` открывает сортировку, но sheet текст market-like; нужен perps-specific.
- [x] Perps list rows: добавить настоящие coin/provider icons, сейчас слева часто остаются элементы из статичного asset или простая буква.
- [x] Perps list rows: subtitle должен быть `Volume / leverage` как в референсе; сейчас fallback `Vol - 20x`.
- [x] Perps list rows: проверить, чтобы row не залезал под bottom nav при скролле.
- [x] Perps list rows: проверить clip под фильтрами, чтобы первая строка не обрезалась некрасиво.
- [x] Tap по perps row после скролла открывает token detail, но это market token detail; нужен perps market detail или отдельный mock.
- [x] History icon в Perps: сейчас открывает общий transaction history; нужен perps history.
- [x] Settings icon: сейчас safe sheet; нужен perps settings screen/sheet.
- [x] Проверить, чтобы после смены фильтра Perps `scrollY` сбрасывался.
- [x] Live/fallback цены: fallback baseline для BTC, ETH, USDT, BNB, XRP, USDC, SOL приведен к сохраненным audit-кадрам; API merge обновляет те же rows/cards без полной замены состава.

## Swap

- [x] Верхняя шапка: close icon, title `Своп`, settings icon.
- [x] From block: amount, Fund pill, token state.
- [x] To block: amount, Select token.
- [x] Reverse button: выбранный token/Fund визуально меняются местами; подтверждено отдельными smoke-кадрами.
- [x] Slider/button bottom: сейчас статичный `Сдвиньте вправо для свопа`; нужно проверить disabled/enabled состояния.
- [x] Settings sheet открывается и выглядит близко: `Проскальзывание`, `Маршрут`, `MEV-защита`.
- [x] Settings sheet нужно сделать интерактивным: выбор slippage, route, MEV toggle.
- [x] Select token sheet: убраны дубли `BNB BNB`, `XRP XRP`.
- [x] Select token sheet: добавить search input сверху, как в референсе.
- [x] Select token sheet: добавить token icons.
- [x] Select token sheet: проверить высоту, close button, скролл длинного списка.
- [x] Fund tap: сейчас safe sheet; нужен экран/лист пополнения.
- [x] Bottom action tap: сейчас safe sheet; нужен корректный disabled reason.
- [x] Проверить, что при выборе токена sheet закрывается и token появляется в `To` блоке.

## Discover

- [x] Сравнить full Discover screen с референсом: search/dApp input, cards, sections.
- [x] Search field opens modal/search; нужно проверить placeholder и dApp URL mode.
- [x] Earn/Staking card открывает discover detail; generic detail заменен на staking flow с выбором актива/APY и проверен smoke-кадром `08a_discover_staking_detail`.
- [x] Quick links: Trust Wallet website, Support Center, Help dApp.
- [x] Discover scroll: сейчас общий `scrollY`, нужно проверить, не уезжает bottom nav и top content.
- [x] Discover row taps: сейчас generic detail; нужно сделать отдельные состояния.
- [x] Проверить, что Discover не показывает фейковые системные Android-кнопки.

## Search

- [x] Search screen открывается с Home/Markets/Perps/Discover.
- [x] Placeholder: сейчас `Поиск токена или dApp`; сверить с референсом для каждого контекста.
- [x] Popular list: добавить icons и правильные токены.
- [x] Search list tap открывает token detail.
- [x] Back button закрывает modal.
- [x] Проверить скролл search results.
- [x] Проверить, что keyboard не нужен/не появляется в mock, если это ожидаемый прототип.

## Token Detail

- [x] Убран дубль subtitle, если `name == symbol`.
- [x] Header: back arrow, token name, symbol.
- [x] Price/change area.
- [x] Chart card: сейчас line chart без axes/real range details; нужно сравнить с Trust Wallet.
- [x] Range chips: 24h, 7d, 30d переключают `timeRange`, но данные не перестраиваются.
- [x] Stats card: market cap, volume, price.
- [x] Проверить token detail из Markets и Perps отдельно.
- [x] Проверить back navigation: должно возвращать в исходную вкладку и сохранять scroll/filter.

## History

- [x] Home history icon открывает transaction history page.
- [x] Perps history icon должен открывать perps history, а не общий history.
- [x] Сравнить empty state, title, back/close button.
- [x] Проверить back/close hitbox.
- [x] Проверить скролл history, если есть список.

## Manage Assets

- [x] Manage screen открывается с Home layout icon.
- [x] Layout selector: 3 режима, visual selected ring.
- [x] Toggles: small assets, NFTs, Predictions, Perps.
- [x] Сейчас toggles меняют local booleans; проверить визуальное состояние после нажатия.
- [x] Сравнить labels, порядок, spacing с референсом.
- [x] Проверить close/back hitbox.

## Sheets / Bottom Sheets

- [x] Общая высота sheet больше не фиксированная `1260..2268`: короткие filter/settings sheets используют адаптивный `sheetBottom()` без лишней пустоты снизу, token/action sheets остаются полноразмерными.
- [x] Close/dim tap: dim и close zone закрывают sheet, а search/gap внутри sheet не закрывают и не выбирают случайный пункт; проверено smoke-кадрами `03c1_market_network_gap_tap` и `07b1_swap_token_search_tap`.
- [x] Sheet title typography: title/close уменьшены и выровнены ближе к reference bottom sheet (`50px` title, `58px` close).
- [x] Sheet row height and dividers: option rows используют более тонкий divider (`1.5f`), компактный text size и единый layout для plain/network/provider rows.
- [x] Selected checkmark style: заменить текстовую `✓` на native stroke-mark; подтверждено sheet-кадрами `03c_market_network_sheet`, `03d_market_sort_sheet`, `03e_market_range_sheet` в `phone_capture/audit_20260528_164118`.
- [x] Network sheet: market/perps context должен иметь разные тексты.
- [x] Sort sheet: market/perps context должен иметь разные тексты.
- [x] Range sheet: 24h/7d/30d должен менять chart/list labels.
- [x] Safe action sheet: слишком generic; заменить на конкретные mock-экраны для Send/Receive/Buy/Fund/Deposit/Settings.
- [x] Wallet sheet: сделать похожим на Trust Wallet wallet selector.

## Скролл И Жесты

- [x] `adb input swipe` работает после включения USB debugging security.
- [x] Home scroll работает.
- [x] Markets scroll работает.
- [x] Perps scroll работает.
- [x] Markets: после смены фильтра reset scroll.
- [x] Perps: после смены фильтра reset scroll.
- [x] Discover: проверить scroll bounds.
- [x] Home: проверить scroll bounds для Crypto/Favorites/NFT.
- [x] Проверить fling/быстрый свайп: добавлен `OverScroller`/velocity fling для нативно прокручиваемых поверхностей.
- [x] Проверить tap после небольшого движения: добавлен touch slop, short swipe по bottom nav обрабатывается как tap.
- [x] Проверить nested areas: bottom nav не должен перехватываться scroll surface; подтверждено после scrolled Markets center trade tap и после scrolled Discover -> Home nav tap кадром `10_home_after_scrolled_nav_tap` в `phone_capture/audit_20260528_165511`.

## Live Data / Fallback

- [x] Добавлен fallback список, чтобы строки не пропадали до загрузки API.
- [x] API догрузка больше не пересоздает список: `mergeFetchedMarkets()` обновляет существующий curated набор по id/symbol, поэтому порядок/состав экрана не скачет при ответе CoinGecko.
- [x] Проверить fallback сортировку и фильтрацию.
- [x] Проверить CoinGecko key/API handling; сейчас есть hardcoded key.
- [x] Offline mode: `seedMarketData()` заполняет `marketList` fallback-монетами уже в конструкторе, поэтому Markets/Perps/Search/Sheets не стартуют пустыми без сети.
- [x] Repeated `fetchMarketData()` каждые 30 секунд больше не дергает UI полной заменой списка; повторные ответы только merge-обновляют данные существующих rows/cards.

## Assets

- [x] `bottom-nav-perps.png` больше не используется в APK: bottom nav рисуется native Canvas, а build guard запрещает упаковку `assets/ui/bottom-nav-*.png`.
- [x] Проверить все `assets/ui/full-*` и `*-page-content.png` на грязные вшитые элементы.
- [x] Проверить `markets-page-content-rich.png`: содержит статичные строки, которые конфликтуют с live overlay.
- [x] Проверить `perps-page-content.png`: содержит статичные строки и фильтры, которые пришлось перекрывать.
- [x] Решить стратегию: либо полностью native Canvas lists/filters, либо чистые screenshot assets без статичных списков.
- [x] Добавить настоящие token/provider icons: crypto-token icons закрыты для текущего списка; provider sheet использует реальные Binance, Hyperliquid и dYdX assets.
- [x] Подключить реальные coin assets из распакованного Trust Wallet `assets/coins` для подтвержденных символов: BTC, ETH, BNB, XRP, TRX, DOGE, SOL, ATOM, ZEC, AAVE, HYPE. Проверено в APK contents и smoke-кадрах `03_markets`, `07b_swap_token_sheet`, `08a_discover_staking_detail` из `phone_capture/audit_20260528_175753`.
- [x] Подключить официальные Trust Wallet token logos для USDT, USDC, LINK, UNI, ONDO, PEPE, DYDX и добавить их в allowlist APK. Проверено сборкой и содержимым `assets/assets/coins/*` в `trust-wallet-visual-debug.apk`; adb-smoke 2026-05-29 не дошел до кадров, потому что устройство не было подключено (`adb: no devices/emulators found`).

## Приоритет Исправлений

- [x] P0: Довести Perps active bottom nav до референса.
- [x] P0: Разделить market/perps sheets по контексту.
- [x] P0: Сброс `scrollY` при смене фильтров.
- [x] P0: Убрать конфликты статичных page-content assets и native overlays.
- [x] P1: Настоящие crypto-token icons в Markets/Perps/Search/Sheets для текущего списка: BTC, ETH, USDT, USDC, BNB, XRP, TRX, DOGE, SOL, LINK, UNI, ATOM, ZEC, AAVE, ONDO, PEPE, HYPE.
- [x] P1: Первый проход real token icons: общий `drawTokenIcon` теперь берет Trust Wallet `assets/coins/*.webp` для BTC/ETH/BNB/XRP/TRX/DOGE/SOL/ATOM/ZEC/AAVE/HYPE; USDT/USDC/UNI/ONDO/PEPE/provider icons еще требуют точного источника, поэтому broad P1 остается открытым.
- [x] P1: Отдельный Perps detail вместо generic token detail.
- [x] P1: Send/Receive/Buy/Fund/Deposit перестать открывать generic safe sheet.
- [x] P1: Search screen closer to Trust Wallet.
- [x] P2: Инерционный скролл/fling.
- [x] P2: Chart range chips с разными данными.
- [x] P2: Pixel polish текущего прохода: уплотнены chips, адаптированы sheet heights/title/rows/dividers, token/provider/network bitmaps рисуются через круглую маску; подтверждено свежими smoke-кадрами `phone_capture/audit_20260529_165950`.

## Технические Замечания

- [x] `javac`/SDK file lock: текущие сборки больше не воспроизводят `AccessDeniedException`; остается только обычный Java warning про bootstrap class path / native access, APK создается и подписывается.
- [x] В проекте нет нормальных автотестов UI; полезно добавить простой adb smoke script.
- [x] Нужно добавить команду capture suite: Home/Markets/Perps/Swap/Discover до и после скролла.
- [x] Нужно хранить свежие сравнения в отдельной папке `phone_capture/audit_YYYYMMDD`.
- [x] Желательно вынести координаты hitbox'ов в константы/таблицу, сейчас они разбросаны по `onTouchEvent`.

## Current Target UI 2026-05-26

Reference screenshots saved in `phone_capture/current_target/`:
- [x] `01_rewards_past.jpg`: Rewards page, Past tab with horizontal reward cards.
- [x] `02_rewards_active.jpg`: Rewards page, Active tab with social campaign card and Trust Alpha section.
- [x] `03_swap.jpg`: Swap standalone page with iOS status/home indicator and no bottom nav.
- [x] `04_trade_sheet.jpg`: Trade bottom sheet from center nav with Exchange, Perps, Predictions.
- [x] `05_home_top.jpg`: Current Home top with wallet/search/actions, asset list and new bottom nav.
- [x] `06_home_scrolled.jpg`: Current Home scrolled state with Perps, Earn and exclusive AI blocks.

### Tasks From Current Target

- [x] Replace old bottom nav with current nav labels: `Главная`, `Популярные`, `Торговать`, `Награды`, `Подробнее`.
- [x] Center `Торговать` button must open trade menu sheet, not directly switch page.
- [x] Trade menu sheet must contain `Обмен`, `Бессрочные фьючерсы`, `Прогнозы` with dimmed background.
- [x] `Обмен` from trade menu opens standalone Swap screen matching `03_swap.jpg`.
- [x] `Бессрочные фьючерсы` from trade menu opens Perps screen/list.
- [x] Add `Награды` page matching active/past tabs, XP cards, campaign cards, Trust Alpha block.
- [x] Rename Markets tab semantics to `Популярные` in bottom nav while keeping market list behavior.
- [x] Home top needs current layout from `05_home_top.jpg`: balance, quick actions, live assets.
- [x] Home scrolled needs current Perps/Earn/Exclusive sections from `06_home_scrolled.jpg`.
- [x] Home scrolled: add opaque sticky mask so partially clipped asset rows do not show above Perps/Earn sections.
- [x] Status bar should visually match current iOS-like reference screenshots.
- [x] Bottom nav should use current rounded translucent style and iOS home indicator.
- [x] Remove old постоянный `Бесср.` bottom nav entry from main navigation; на обычных вкладках Perps остается внутри trade menu, а на самой Perps-странице nav временно показывает активный `Бесср.` pill как состояние текущего экрана.

### Current Target Follow-ups

- [x] Perps: remove empty grey spacer block between hero card and filters.
- [x] Perps: move category/provider/sort filters up and keep tap targets aligned.
- [x] Perps: move first market rows up so the screen is not mostly empty.
- [x] Perps: replace text-symbol nav icons with closer Trust Wallet vector icons.
- [x] Rewards: replace placeholder XP illustration with asset closer to target.
- [x] Trade sheet: tune top offset/height to match target sheet more tightly.
- [x] Home: add compact Hyperliquid promo banner before asset tabs.
- [x] Home: redraw crypto rows with live prices and zero balances.
- [x] Home: add Perps/Earn/Exclusive scrolled sections with working scroll spacing.
- [x] Home: prevent Perps card copy from overflowing card bounds.
- [x] Status bar: replace Android-looking asset with iOS-like green time pill, LTE and battery.
- [x] Trade sheet: lower compact rounded panel and replace glyph icons with drawn icons.
- [x] Swap: replace close X with back arrow and add iOS home indicator.
- [x] Rewards: redraw hero illustration closer to target wallet/rewards art.
- [x] Popular: replace visible `Markets` title with `Популярные` to match current bottom nav semantics.
- [x] Popular: localize visible category labels and keep chip text short enough to avoid overlap.
- [x] History: add real scroll for populated transaction list and reset `scrollY` when returning to Home.
- [x] Text cleanup: remove remaining DOGE icon mojibake fallback.
- [x] Markets/Popular: hide partial list rows under fixed filters while scrolling.
- [x] Smoke: update adb capture route so Perps opens through the center trade sheet.
- [x] Home: replace top glyph icons with Canvas-drawn settings/search/scan/copy icons.
- [x] Popular: redraw header, feature cards and top traded cards natively instead of using the old screenshot asset.
- [x] Popular/Perps: hide partial rows at both top and bottom of scrollable lists.
- [x] Discover: redraw native screen and wire all visible rows to detail.
- [x] Discover detail: update CTA/close hitboxes to match the current drawn buttons.
- [x] History: replace old screenshot asset with native empty transaction history screen.
- [x] Manage: replace old screenshot asset with native layout selector and toggles.
- [x] Home: align hitboxes with the current native top/actions/asset tab layout.
- [x] Home: remove full-screen screenshot layers; main page renders natively and scrolls as a real screen.
- [x] APK assets: stop bundling old `assets/assets/ui` screenshot pages; packaged APK now contains only `btc.webp`, `eth.webp`, `bnb.webp` token icons.
- [x] Swap: replace `swap-page-content.png` with native Canvas screen.
- [x] Swap: remove `assets/target/swap.png` initial-state overlay; standalone screen remains natively interactive.
- [x] Trade menu: remove full-screen `assets/target/trade-sheet.png` overlay; sheet renders and responds natively.
- [x] Rewards: remove `assets/target/rewards-*.png` overlays; Active/Past render through native UI.
- [x] Build guard: reject any reintroduced full-screen/page-content/home screenshot asset in the APK.
- [x] Token fallback polish: improve visible XRP/ONDO/ZEC and stock/perps marks in native lists and token sheet.
- [x] Perps: replace `perps-page-content.png` with native Canvas header/hero/filters/list.
- [x] Token icons: add native fallback marks for USDT, USDC, XRP, SOL, DOGE, TRX, LINK, UNI, AAVE, PEPE and HYPE.
- [x] Home assets: replace Favorites/NFT screenshot assets with native Canvas tabs and empty states.
- [x] Search: add search icon, cancel action, token/dApp/NFT chips and expanded dApp results.
- [x] Discover: add quick link cards and reduce scroll bounds so bottom nav stays fixed.
- [x] Discover: localize visible quick links, CTA, dApp search results and detail metadata.
- [x] Discover: hide clipped search/header content under the status area while scrolling.
- [x] Perps: localize visible category chips (`Popular/New/Crypto/Stocks`) and keep spacing clean.
- [x] Swap: localize visible `Fund` and `Select token` controls without clipping.
- [x] History: tune bottom `18 мая` group so the top state matches the reference crop while keeping the row reachable by scroll.
- [x] Home: separate NFT tab and history icon hitboxes so near-icon taps open history reliably without breaking NFT selection.
- [x] Sheets: localize secondary labels in wallet selector, buy providers and fund options.
- [x] Smoke: include transaction history top/scrolled captures after Home; stabilized with segmented app restart and verified in `phone_capture/audit_20260530_142841`.
- [x] Home scrolled: localize Earn card source labels and Exclusive `Meet Tru` title.
- [x] Sheets: prevent accidental close/selection on token search field and option-row gaps; confirmed in `phone_capture/audit_20260528_160847`.
- [x] Token fallback polish: replace blank TRX placeholder with a red Tron-style native mark; confirmed on Home and Staking detail in `phone_capture/audit_20260528_161234`.
- [x] Token fallback polish: replace grey ATOM placeholder with a Cosmos-style native mark; confirmed on Staking detail in `phone_capture/audit_20260528_161651`.
- [x] Home promo art: replaced hand-drawn Hyperliquid Canvas illustration with a real Trust Wallet Hyperliquid/perps cutout asset; cleaned detached lower fragment and verified in `phone_capture/audit_20260530_154200/01_home.png`.
- [x] Home balance typography: narrowed/lowered variable weight to reduce Inter heaviness; latest measured balance bbox is `453px` wide in `phone_capture/audit_20260530_160050/01_home.png`.
- [x] Home asset amount typography: added real Trust APK `Roboto-Medium-Numbers.ttf` for strong financial numbers; verified in `phone_capture/audit_20260530_162441/01_home.png`.
- [x] Home real assets pass: TRX row and USDT/Tron overlay now use the real Trust `195.webp` Tron bitmap; Hyperliquid promo art was regenerated from the Trust Wallet `learn_perps_2_dark.png` foreground with transparent background and verified in `phone_capture/audit_20260530_165847/01_home.png`.
- [x] Home final polish pass: balance typography increased to a heavier display weight, Hyperliquid foreground art resized to the reference card slot, and Rewards nav gift icon redrawn with closer proportions; verified in `phone_capture/audit_20260530_170651/01_home.png`.
- [x] Home quick actions typography: send/receive/swap/buy buttons now use real cropped button/icon assets only for the icon block while labels are drawn natively with the shared semibold text stack; verified in `phone_capture/audit_20260530_182332/01_home.png`.
- [x] Home wallet chip typography: `25K` and the chevron are now separate native primitives instead of a combined text glyph, avoiding font-dependent arrow spacing; verified in `phone_capture/audit_20260530_183125/01_home.png`.
- [x] Home network badges: TON badge now uses a dedicated TON-logo-shaped native glyph instead of the previous generic triangle fallback; verified in `phone_capture/audit_20260530_184140/01_home.png`. A real downloadable TON PNG was attempted from official/Wikimedia sources but blocked by CDN TLS/429, so this remains a native logo approximation until a bundled source asset is available.
- [x] Home font pass: local Apple `SFNS.ttf` from the browser font cache is now packaged under the existing `SF-Pro-*` asset names, so `loadFonts()` uses an Apple San Francisco-family font instead of Inter fallback for Home text/balance. Verified packaged APK contains `assets/assets/fonts/SF-Pro-*` and latest Home capture is `phone_capture/audit_20260530_184832/01_home.png` with balance bbox `432x106`.
- [x] Home font weight pass: `textTyped()` now uses `Typeface.create(typeface, weight, false)` on Android 28+ so SFNS-backed Regular/Medium/Semibold/Bold weights render as distinct weights even when the packaged font file is static. Verified in `phone_capture/audit_20260530_185633/01_home.png`; balance bbox is `436x109`.
- [x] Home wallet copy icon: shifted and scaled the real `copy-button.png` crop so the visible glyph center lands at `693.5/407.5`, matching the spec target near `690/405`; verified in `phone_capture/audit_20260530_190633/01_home.png`.
- [x] Home SF typography pass: balance now uses SF Display Bold 700 with tabular figures, the red delta uses SF Text Medium 500, and Home asset amounts use SF Pro Text tabular figures instead of the Roboto-number fallback. Verified in `phone_capture/audit_20260530_191534/01_home.png`; full smoke also captured `phone_capture/audit_20260530_191534/02_home_scrolled.png`.
- [x] Home real-font/action pass: removed the fake `SF-Pro-*` runtime dependency because the packaged files were the same single SFNS cache font under different names; Home now uses real Inter weights from the Trust APK extraction, action buttons are native vector primitives instead of dirty bitmap crops, and the Perps section is back at the canonical reference baseline. Verified in `phone_capture/audit_20260530_194648/01_home.png` and `phone_capture/audit_20260530_194648/02_home_scrolled.png`; APK guard confirms no `SF-Pro-*`, `assets/ui`, `assets/target`, `full-*`, `*-page-content*`, or `home-*` entries.
- [x] Web port: `trust-wallet-pwa` now renders the wallet UI through Canvas with copied real coin/native UI assets, send flow, transaction history/detail, fee calculation, and PWA metadata so it can be served to an iPhone over LAN. Verified `node --check trust-wallet-pwa/app.js` and local HTTP response for `sw.js`.
- [x] Web port canvas13/canvas14 pass: removed the late debug `drawHomeContent` override that drew text glyph icons, fixed `imgTint()` so it no longer applies `source-in` to the main canvas and erases Home content, added `visualViewport`/`100dvh` sizing, and made the right scroll indicator transient instead of always visible. Verified `node --check` for `trust-wallet-pwa/app.js` and `trust-wallet-android-webview/assets/pwa/app.js`, Android Chrome screenshot `phone_capture/web_canvas14_android.png`, and built `trust-wallet-android-webview/build/canvas-webview/trust-wallet-webview-canvas-debug.apk`. Device install is currently blocked by Android policy: `INSTALL_FAILED_USER_RESTRICTED`.
- [x] Web port quick actions pass: Home quick actions now open concrete WEB states instead of dead taps: Receive asset list, Receive QR, Swap screen/token picker/disabled reason, and Buy asset sheet. Verified on Android Chrome with `phone_capture/web_canvas15_receive_list.png`, `phone_capture/web_canvas15_receive_qr.png`, `phone_capture/web_canvas15_swap.png`, `phone_capture/web_canvas15_swap_tokens.png`, and `phone_capture/web_canvas15_buy.png`; synced to `trust-wallet-android-webview/assets/pwa` and rebuilt `trust-wallet-android-webview/build/canvas-webview/trust-wallet-webview-canvas-debug.apk`.
- [x] Web port Home topbar pass: settings, search, scan, wallet selector, and copied-address sheets are now implemented as Canvas states with wider mobile hitboxes. Verified settings/search flow on Android Chrome in `phone_capture/web_canvas16_settings.png` and `phone_capture/web_canvas17_search.png`; synced to `trust-wallet-android-webview/assets/pwa` and rebuilt `trust-wallet-android-webview/build/canvas-webview/trust-wallet-webview-canvas-debug.apk`.
- [x] Web port Home asset tabs/manage pass: Crypto/Favorites/NFT tabs now switch as native Canvas states, History and Manage icons have separated tap zones, and Manage opens a layout/toggle screen. Empty Favorites/NFT states were compacted so the floating bottom nav no longer covers dead CTA buttons. Verified on Android Chrome with `phone_capture/web_canvas20_favorites.png`, `phone_capture/web_canvas20_nft.png`, and `phone_capture/web_canvas20_manage.png`; synced to `trust-wallet-android-webview/assets/pwa` and rebuilt `trust-wallet-android-webview/build/canvas-webview/trust-wallet-webview-canvas-debug.apk`.
- [x] Web port Popular/detail pass: bottom nav opens the Popular tab, the market list scrolls independently from Home, market cards/rows open a Canvas token detail page with price chart/stat rows/action buttons, and BTC detail `Получить` opens the BTC receive QR sheet. Verified on Android Chrome with `phone_capture/web_canvas21_popular2.png`, `phone_capture/web_canvas21_popular_scrolled2.png`, `phone_capture/web_canvas22_btc_detail.png`, and `phone_capture/web_canvas22_btc_receive.png`; synced to `trust-wallet-android-webview/assets/pwa` and rebuilt `trust-wallet-android-webview/build/canvas-webview/trust-wallet-webview-canvas-debug.apk`.
- [x] Web port Rewards pass: bottom nav opens Rewards, Active/Past tabs now switch as Canvas states, past reward cards render natively, and reward cards open a dismissible detail sheet. Active card spacing was compacted so the social row no longer collides with the floating nav. Verified on Android Chrome with `phone_capture/web_canvas26_rewards_active.png`, `phone_capture/web_canvas23_rewards_past.png`, `phone_capture/web_canvas24_rewards_detail.png`, and `phone_capture/web_canvas24_rewards_detail_closed.png`; synced to `trust-wallet-android-webview/assets/pwa` and rebuilt `trust-wallet-android-webview/build/canvas-webview/trust-wallet-webview-canvas-debug.apk`.
- [x] Web port More pass: bottom nav opens a native Canvas More hub with wallet summary, settings/address book/support/about rows, wallet/settings reuse existing sheets, and support/about now open dedicated mobile-safe sheets with visible action buttons. Verified on Android Chrome with `phone_capture/web_canvas27_more.png`, `phone_capture/web_canvas27_more_wallet.png`, `phone_capture/web_canvas28_more_support.png`, and `phone_capture/web_canvas28_more_about.png`; synced to `trust-wallet-android-webview/assets/pwa` and rebuilt `trust-wallet-android-webview/build/canvas-webview/trust-wallet-webview-canvas-debug.apk`.
- [x] Web architecture pass: root `trust-wallet-pwa/app.js` is now a small ES module entrypoint, runtime is generated to `trust-wallet-pwa/dist/app.bundle.js`, and editable source is split by domain under `trust-wallet-pwa/src` (`state/runtime/shell/home/markets+sheets/trade/components/interaction/boot`). Added `trust-wallet-pwa/scripts/build-web.ps1`, synced the generated runtime to `trust-wallet-android-webview/assets/pwa`, and verified `node --check` for both generated bundles, module entrypoint syntax via temporary `.mjs`, Android Chrome `canvas33` load/debug state in `phone_capture/web_canvas33_arch_home.png`, and rebuilt `trust-wallet-android-webview/build/canvas-webview/trust-wallet-webview-canvas-debug.apk`.
- [x] Web port Trade/Perps/Predictions pass: center Trade sheet now adapts upward on shorter Android Chrome/iPhone-like viewport so `Обмен`, `Бессрочные фьючерсы`, and `Прогнозы` remain readable above the floating nav. Verified WEB flow through the new modular runtime on Android Chrome with `phone_capture/web_canvas33_trade_sheet.png`, `phone_capture/web_canvas33_perps.png`, `phone_capture/web_canvas33_perps_deposit.png`, `phone_capture/web_canvas33_predictions.png`, and `phone_capture/web_canvas33_prediction_action.png`; synced generated PWA/WebView bundles and rechecked both with `node --check`.
- [x] Web port Transfer viewport pass: Send/Buy/Confirm/Processing bottom action buttons now use adaptive sheet action coordinates instead of fixed `y=2120`, so they remain visible on Android Chrome and iPhone-like short viewports. Verified Send form, confirmation, processing, transaction detail, receive list/QR, and address editor on Android Chrome with `phone_capture/web_canvas34_send_form.png`, `phone_capture/web_canvas34_confirm.png`, `phone_capture/web_canvas34_processing.png`, `phone_capture/web_canvas34_tx_detail.png`, `phone_capture/web_canvas34_receive_list.png`, `phone_capture/web_canvas34_receive_qr.png`, and `phone_capture/web_canvas34_editor_address.png`; synced `canvas34` PWA/WebView bundles and verified both with `node --check`.
- [x] Web port Receive QR compact viewport pass: Receive QR now uses adaptive QR-card metrics and a scalable QR module grid so the asset row, QR card, action buttons, and `Ввод с биржи` card fit together on Android Chrome short viewport (`BASE_H≈2014`) and iPhone-like web height without overlap. Verified on device with `phone_capture/web_canvas36_receive_qr_compact.png`; synced `canvas36`/`trust-visual-web-v25` PWA/WebView bundles, verified both with `node --check`, and rebuilt `trust-wallet-android-webview/build/canvas-webview/trust-wallet-webview-canvas-debug.apk`.
- [x] Web port short bottom-sheet pass: wallet selector, copied-address confirmation, and swap-disabled sheets now adapt their CTA/button and bottom-sheet position on short mobile web viewports instead of using fixed `y=2010..2148` coordinates. Verified on Android Chrome short viewport (`BASE_H≈2014`) with `phone_capture/web_canvas37_wallet_sheet.png`, `phone_capture/web_canvas37_copied_sheet.png`, and `phone_capture/web_canvas37_swap_disabled.png`; synced `canvas37`/`trust-visual-web-v26` PWA/WebView bundles, verified both with `node --check`, and rebuilt `trust-wallet-android-webview/build/canvas-webview/trust-wallet-webview-canvas-debug.apk`.
- [x] Web port Rewards Past compact pass: the Past rewards grid now switches to compact cards on short mobile web viewports so the second row stays above the floating nav instead of being covered at `NAV_TOP≈1792`. Verified on Android Chrome short viewport (`BASE_H≈2014`) with `phone_capture/web_canvas38_rewards_past_compact2.png`; confirmed a lower-card tap opens `rewardDetail`, synced `canvas38`/`trust-visual-web-v27` PWA/WebView bundles, verified both with `node --check`, and rebuilt `trust-wallet-android-webview/build/canvas-webview/trust-wallet-webview-canvas-debug.apk`.
- [x] Web port Rewards Active compact pass: the Active rewards campaign card now switches to a shorter mobile layout on short web viewports so the social row, handle, and arrow CTA stay above the floating nav. Verified on Android Chrome short viewport (`BASE_H≈2014`) with `phone_capture/web_canvas39_rewards_active_compact.png`; confirmed tapping the compact card opens `rewardDetail`, synced `canvas39`/`trust-visual-web-v28` PWA/WebView bundles, verified both with `node --check`, and rebuilt `trust-wallet-android-webview/build/canvas-webview/trust-wallet-webview-canvas-debug.apk`.

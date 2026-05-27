# Trust UI Audit Checklist

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
- [ ] Унифицировать bottom nav assets: `bottom-nav-home`, `bottom-nav-markets`, `bottom-nav-perps`, `bottom-nav-discover`, `bottom-nav-favorites`.
- [x] Перерисовать активный `Perps` nav asset в стиле референса, сейчас активный pill с `%` заметно отличается от оригинального значка.
- [x] Проверить клики bottom nav после скролла на всех вкладках: Home, Markets, Swap, Perps, Discover.
- [ ] Проверить, что при переходе между вкладками сбрасывается только нужный `scrollY`, без потери выбранных фильтров там, где Trust Wallet сохраняет состояние.
- [x] Проверить статусбар: в нашей сборке статусбар рисуется asset'ом; нужен сравнительный цвет, отступ, иконки, время.
- [x] Проверить отсутствие случайных затемнений/масок после закрытия sheets и modal.
- [ ] Проверить все текстовые строки на mojibake/битую кириллицу. В `MainActivity.java` большая часть уже UTF-8, но старые строки могли остаться.

## Главная Вкладка

- [x] Сравнить верхний блок: настройки, search pill, scan, wallet chip, copy.
- [ ] Проверить красную точку на настройках/wallet chip: позиция и наличие совпадают не полностью.
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
- [ ] Home scroll: работает, но нужно довести sticky-поведение верхних asset tabs как в Trust Wallet.
- [x] После скролла на Home topbar Trust Wallet меняет состояние; у нас Canvas-вариант может не полностью повторять collapsed header.
- [x] Проверить переход в Perps section на Home после скролла.
- [ ] Проверить Earn и History секции на Home после скролла: в референсе другие блоки и состояния.

## Markets

- [x] Фильтры больше не должны уезжать вместе со списком.
- [x] Сортировка открывается после скролла.
- [x] Убраны фейковые системные кнопки снизу.
- [x] Сравнить верхнюю шапку `Markets`: title, search icon, отступы.
- [ ] `Predictions` card: размер, радиус, иконка, текст, tap action.
- [ ] `Meme Rush` card: размер, радиус, иконка, текст, tap action.
- [ ] Top traded cards: у нас частично статичные/частично live; нужно привести к референсу по карточкам, логотипам и графикам.
- [ ] Панель категорий сейчас нативно дорисована: визуально стала рабочей, но не совпадает пиксельно с референсом.
- [x] Проверить категории: Star/Favorites, Hot tokens, Top Gainers, RWA, Meme, DeFi.
- [x] Категория `Star` сейчас меняет `marketFilter = 1`, но фильтрация избранного фактически не реализована.
- [x] `Hot tokens` возвращает базовый список.
- [x] `Top Gainers` фильтрует положительный change, но в референсе сортировка/категория отличается.
- [x] `RWA`, `Meme`, `DeFi` фильтры используют простые substring списки; нужно сверить состав токенов с референсом.
- [ ] Вторая строка фильтров: `Сеть`, `Рыночная капитализация`, `24h` должна совпадать по ширине и визуальному стилю.
- [ ] Sheet `Сеть`: сейчас общие пункты `Все сети`, `Ethereum`, `BNB Smart Chain`, `Solana`; нужно проверить порядок и подписи как в Trust Wallet.
- [x] Sheet `Сортировка`: сейчас `Рыночная капитализация`, `Top Gainers`, `Объем (24 ч.)`; нужно сверить с референсом.
- [x] Sheet `Период`: работает, но сами графики не перестраиваются по 24h/7d/30d.
- [ ] Список Market rows: нужно добавить настоящие логотипы токенов, сейчас круг с первой буквой.
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
- [ ] Активный bottom nav для Perps нужно перерисовать ближе к референсу; текущий `%` вместо оригинальной иконки.
- [x] Верхняя шапка: history icon, title `Бесср.`, settings icon, отступы.
- [x] Hero/card: infinity artwork, текст, кнопка `Депозит`; нужно сравнить высоты, радиусы и позицию.
- [x] Deposit tap сейчас открывает generic safe sheet; нужно сделать mock deposit screen/sheet как в референсе.
- [x] Search bar: открывает общий поиск; нужно сравнить по placeholder и поведению.
- [ ] Category chips: Star, Popular, New, Crypto, Stocks.
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
- [ ] Проверить, что live/fallback цены не противоречат референсу на одном экране.

## Swap

- [x] Верхняя шапка: close icon, title `Своп`, settings icon.
- [x] From block: amount, Fund pill, token state.
- [x] To block: amount, Select token.
- [ ] Reverse button: меняет направление, но визуально нужно сравнить состояние.
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
- [ ] Earn/Staking card открывает discover detail; нужно заменить generic detail на референсный flow.
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
- [ ] Проверить скролл history, если есть список.

## Manage Assets

- [x] Manage screen открывается с Home layout icon.
- [x] Layout selector: 3 режима, visual selected ring.
- [x] Toggles: small assets, NFTs, Predictions, Perps.
- [x] Сейчас toggles меняют local booleans; проверить визуальное состояние после нажатия.
- [x] Сравнить labels, порядок, spacing с референсом.
- [x] Проверить close/back hitbox.

## Sheets / Bottom Sheets

- [ ] Общая высота sheet сейчас `1260..2268`; сравнить с Trust Wallet на разных sheets.
- [ ] Close tap работает через `y < 1260` или close zone; нужно проверить, не закрывается ли sheet случайно при tap по dim area.
- [ ] Sheet title typography.
- [ ] Sheet row height and dividers.
- [ ] Selected checkmark style.
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
- [ ] Проверить fling/быстрый свайп, сейчас обработка `ACTION_MOVE` без инерции.
- [ ] Проверить tap после небольшого движения: `moved` threshold может съедать легкие taps.
- [ ] Проверить nested areas: bottom nav не должен перехватываться scroll surface.

## Live Data / Fallback

- [x] Добавлен fallback список, чтобы строки не пропадали до загрузки API.
- [ ] Проверить, когда API догружается, нет ли визуального скачка списка.
- [x] Проверить fallback сортировку и фильтрацию.
- [x] Проверить CoinGecko key/API handling; сейчас есть hardcoded key.
- [ ] Проверить offline mode: все вкладки должны выглядеть заполненными.
- [ ] Проверить repeated `fetchMarketData()` каждые 30 секунд: не дергает ли UI слишком заметно.

## Assets

- [ ] Перегенерировать `bottom-nav-perps.png` качественно, не через ручной System.Drawing patch.
- [x] Проверить все `assets/ui/full-*` и `*-page-content.png` на грязные вшитые элементы.
- [x] Проверить `markets-page-content-rich.png`: содержит статичные строки, которые конфликтуют с live overlay.
- [x] Проверить `perps-page-content.png`: содержит статичные строки и фильтры, которые пришлось перекрывать.
- [x] Решить стратегию: либо полностью native Canvas lists/filters, либо чистые screenshot assets без статичных списков.
- [ ] Добавить настоящие token/provider icons.

## Приоритет Исправлений

- [x] P0: Довести Perps active bottom nav до референса.
- [x] P0: Разделить market/perps sheets по контексту.
- [x] P0: Сброс `scrollY` при смене фильтров.
- [x] P0: Убрать конфликты статичных page-content assets и native overlays.
- [ ] P1: Настоящие token icons в Markets/Perps/Search/Sheets.
- [x] P1: Отдельный Perps detail вместо generic token detail.
- [x] P1: Send/Receive/Buy/Fund/Deposit перестать открывать generic safe sheet.
- [x] P1: Search screen closer to Trust Wallet.
- [ ] P2: Инерционный скролл/fling.
- [x] P2: Chart range chips с разными данными.
- [ ] P2: Pixel polish: font sizes, exact colors, radii, spacing.

## Технические Замечания

- [ ] `javac` иногда печатает `AccessDeniedException` при закрытии `android.jar`, но APK создается, подписывается и устанавливается. Нужно отдельно разобраться с JDK/SDK file lock.
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
- [x] Status bar should visually match current iOS-like reference screenshots.
- [x] Bottom nav should use current rounded translucent style and iOS home indicator.
- [x] Remove old `Бесср.` bottom nav entry from main navigation; Perps is now inside trade menu.

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
- [x] Swap: replace `swap-page-content.png` with native Canvas screen.
- [x] Perps: replace `perps-page-content.png` with native Canvas header/hero/filters/list.
- [x] Token icons: add native fallback marks for USDT, USDC, XRP, SOL, DOGE, TRX, LINK, UNI, AAVE, PEPE and HYPE.
- [x] Home assets: replace Favorites/NFT screenshot assets with native Canvas tabs and empty states.
- [x] Search: add search icon, cancel action, token/dApp/NFT chips and expanded dApp results.
- [x] Discover: add quick link cards and reduce scroll bounds so bottom nav stays fixed.

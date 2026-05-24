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
- [ ] Сравнить высоту нижнего navbar с Trust Wallet: текущий визуально занимает похожую зону, но активная кнопка Perps нарисована вручную и выглядит грубее.
- [ ] Унифицировать bottom nav assets: `bottom-nav-home`, `bottom-nav-markets`, `bottom-nav-perps`, `bottom-nav-discover`, `bottom-nav-favorites`.
- [ ] Перерисовать активный `Perps` nav asset в стиле референса, сейчас активный pill с `%` заметно отличается от оригинального значка.
- [ ] Проверить клики bottom nav после скролла на всех вкладках: Home, Markets, Swap, Perps, Discover.
- [ ] Проверить, что при переходе между вкладками сбрасывается только нужный `scrollY`, без потери выбранных фильтров там, где Trust Wallet сохраняет состояние.
- [ ] Проверить статусбар: в нашей сборке статусбар рисуется asset'ом; нужен сравнительный цвет, отступ, иконки, время.
- [ ] Проверить отсутствие случайных затемнений/масок после закрытия sheets и modal.
- [ ] Проверить все текстовые строки на mojibake/битую кириллицу. В `MainActivity.java` большая часть уже UTF-8, но старые строки могли остаться.

## Главная Вкладка

- [ ] Сравнить верхний блок: настройки, search pill, scan, wallet chip, copy.
- [ ] Проверить красную точку на настройках/wallet chip: позиция и наличие совпадают не полностью.
- [ ] Проверить tap zones: настройки, поиск, сканер, wallet chip, copy.
- [ ] Проверить sheet кошелька: в референсе это список кошельков; у нас safe/demo sheet, нужно привести ближе к Trust Wallet.
- [ ] Проверить copy address sheet/toast: у нас демонстрационный текст, нужно сделать визуально как референс.
- [ ] Сравнить быстрые действия: Send, Receive, Swap, Buy.
- [x] Send должен открывать экран выбора токена, а не общий safe sheet.
- [x] Receive должен открывать экран выбора токена/адреса, а не общий safe sheet.
- [x] Buy должен открывать buy flow или корректный mock-экран, не generic safe sheet.
- [ ] Swap с главной открывается корректно.
- [ ] Проверить табы assets: Crypto, Favorites, NFT.
- [ ] Crypto tab: пустая карточка совпадает частично, но нужно проверить размеры, отступы, текст и кнопки.
- [ ] Favorites tab: пустое состояние и кнопка управления токенами должны совпасть с референсом.
- [ ] NFT tab: проверить пустое состояние и иконку.
- [ ] History icon: открывает историю, но нужно сравнить экран истории по точности.
- [ ] Manage icon: открывает manage screen, но точность переключателей/радиокнопок нужно проверить.
- [ ] Home scroll: работает, но нужно довести sticky-поведение верхних asset tabs как в Trust Wallet.
- [ ] После скролла на Home topbar Trust Wallet меняет состояние; у нас Canvas-вариант может не полностью повторять collapsed header.
- [ ] Проверить переход в Perps section на Home после скролла.
- [ ] Проверить Earn и History секции на Home после скролла: в референсе другие блоки и состояния.

## Markets

- [x] Фильтры больше не должны уезжать вместе со списком.
- [x] Сортировка открывается после скролла.
- [x] Убраны фейковые системные кнопки снизу.
- [ ] Сравнить верхнюю шапку `Markets`: title, search icon, отступы.
- [ ] `Predictions` card: размер, радиус, иконка, текст, tap action.
- [ ] `Meme Rush` card: размер, радиус, иконка, текст, tap action.
- [ ] Top traded cards: у нас частично статичные/частично live; нужно привести к референсу по карточкам, логотипам и графикам.
- [ ] Панель категорий сейчас нативно дорисована: визуально стала рабочей, но не совпадает пиксельно с референсом.
- [ ] Проверить категории: Star/Favorites, Hot tokens, Top Gainers, RWA, Meme, DeFi.
- [ ] Категория `Star` сейчас меняет `marketFilter = 1`, но фильтрация избранного фактически не реализована.
- [ ] `Hot tokens` возвращает базовый список.
- [ ] `Top Gainers` фильтрует положительный change, но в референсе сортировка/категория отличается.
- [ ] `RWA`, `Meme`, `DeFi` фильтры используют простые substring списки; нужно сверить состав токенов с референсом.
- [ ] Вторая строка фильтров: `Сеть`, `Рыночная капитализация`, `24h` должна совпадать по ширине и визуальному стилю.
- [ ] Sheet `Сеть`: сейчас общие пункты `Все сети`, `Ethereum`, `BNB Smart Chain`, `Solana`; нужно проверить порядок и подписи как в Trust Wallet.
- [ ] Sheet `Сортировка`: сейчас `Рыночная капитализация`, `Top Gainers`, `Объем (24 ч.)`; нужно сверить с референсом.
- [ ] Sheet `Период`: работает, но сами графики не перестраиваются по 24h/7d/30d.
- [ ] Список Market rows: нужно добавить настоящие логотипы токенов, сейчас круг с первой буквой.
- [ ] Market rows: проверить левый отступ, размер тикера, subtitle `MCap / Vol`, правые price/change.
- [ ] Market rows: sparkline должен иметь заливку/цвет как Trust Wallet; сейчас простая линия.
- [ ] Market rows: при скролле верхняя строка частично уходит под фильтры; нужно добавить более мягкий clip/градиент или старт ниже.
- [ ] Tap по market row открывает token detail; проверить индекс после скролла на нескольких позициях.
- [ ] Token detail из Markets: убрать дубль тикера, проверить price, chart, stats, range chips.
- [ ] Search icon в Markets: открывает общий поиск, нужно сравнить референсный search экран.
- [ ] После закрытия sheet список должен оставаться в той же позиции.
- [x] После смены фильтра желательно сбрасывать `scrollY` в 0, сейчас может остаться середина списка.

## Perps / Бессрочные

- [x] Убран дубль названия монет в списке (`BNB / BNB` и похожие).
- [x] Убран грязный `bottom-nav-perps.png`, где были зашиты ZEC/цена/график.
- [x] Фильтры теперь фиксированные и кликабельные после скролла.
- [ ] Активный bottom nav для Perps нужно перерисовать ближе к референсу; текущий `%` вместо оригинальной иконки.
- [ ] Верхняя шапка: history icon, title `Бесср.`, settings icon, отступы.
- [ ] Hero/card: infinity artwork, текст, кнопка `Депозит`; нужно сравнить высоты, радиусы и позицию.
- [x] Deposit tap сейчас открывает generic safe sheet; нужно сделать mock deposit screen/sheet как в референсе.
- [ ] Search bar: открывает общий поиск; нужно сравнить по placeholder и поведению.
- [ ] Category chips: Star, Popular, New, Crypto, Stocks.
- [ ] `Stocks` визуально есть, но фильтрация stock/perps не реализована отдельно.
- [x] Provider filter: `Все поставщики` должен открывать provider sheet, сейчас переиспользуется `SHEET_MARKET_NETWORK`.
- [x] Sort filter: `Объем (24 ч.)` открывает сортировку, но sheet текст market-like; нужен perps-specific.
- [ ] Perps list rows: добавить настоящие coin/provider icons, сейчас слева часто остаются элементы из статичного asset или простая буква.
- [ ] Perps list rows: subtitle должен быть `Volume / leverage` как в референсе; сейчас fallback `Vol - 20x`.
- [ ] Perps list rows: проверить, чтобы row не залезал под bottom nav при скролле.
- [ ] Perps list rows: проверить clip под фильтрами, чтобы первая строка не обрезалась некрасиво.
- [ ] Tap по perps row после скролла открывает token detail, но это market token detail; нужен perps market detail или отдельный mock.
- [ ] History icon в Perps: сейчас открывает общий transaction history; нужен perps history.
- [ ] Settings icon: сейчас safe sheet; нужен perps settings screen/sheet.
- [ ] Проверить, чтобы после смены фильтра Perps `scrollY` сбрасывался.
- [ ] Проверить, что live/fallback цены не противоречат референсу на одном экране.

## Swap

- [ ] Верхняя шапка: close icon, title `Своп`, settings icon.
- [ ] From block: amount, Fund pill, token state.
- [ ] To block: amount, Select token.
- [ ] Reverse button: меняет направление, но визуально нужно сравнить состояние.
- [ ] Slider/button bottom: сейчас статичный `Сдвиньте вправо для свопа`; нужно проверить disabled/enabled состояния.
- [ ] Settings sheet открывается и выглядит близко: `Проскальзывание`, `Маршрут`, `MEV-защита`.
- [ ] Settings sheet нужно сделать интерактивным: выбор slippage, route, MEV toggle.
- [ ] Select token sheet: убраны дубли `BNB BNB`, `XRP XRP`.
- [x] Select token sheet: добавить search input сверху, как в референсе.
- [x] Select token sheet: добавить token icons.
- [ ] Select token sheet: проверить высоту, close button, скролл длинного списка.
- [x] Fund tap: сейчас safe sheet; нужен экран/лист пополнения.
- [ ] Bottom action tap: сейчас safe sheet; нужен корректный disabled reason.
- [ ] Проверить, что при выборе токена sheet закрывается и token появляется в `To` блоке.

## Discover

- [ ] Сравнить full Discover screen с референсом: search/dApp input, cards, sections.
- [ ] Search field opens modal/search; нужно проверить placeholder и dApp URL mode.
- [ ] Earn/Staking card открывает discover detail; нужно заменить generic detail на референсный flow.
- [ ] Quick links: Trust Wallet website, Support Center, Help dApp.
- [ ] Discover scroll: сейчас общий `scrollY`, нужно проверить, не уезжает bottom nav и top content.
- [ ] Discover row taps: сейчас generic detail; нужно сделать отдельные состояния.
- [ ] Проверить, что Discover не показывает фейковые системные Android-кнопки.

## Search

- [ ] Search screen открывается с Home/Markets/Perps/Discover.
- [ ] Placeholder: сейчас `Поиск токена или dApp`; сверить с референсом для каждого контекста.
- [ ] Popular list: добавить icons и правильные токены.
- [ ] Search list tap открывает token detail.
- [ ] Back button закрывает modal.
- [ ] Проверить скролл search results.
- [ ] Проверить, что keyboard не нужен/не появляется в mock, если это ожидаемый прототип.

## Token Detail

- [ ] Убран дубль subtitle, если `name == symbol`.
- [ ] Header: back arrow, token name, symbol.
- [ ] Price/change area.
- [ ] Chart card: сейчас line chart без axes/real range details; нужно сравнить с Trust Wallet.
- [ ] Range chips: 24h, 7d, 30d переключают `timeRange`, но данные не перестраиваются.
- [ ] Stats card: market cap, volume, price.
- [ ] Проверить token detail из Markets и Perps отдельно.
- [ ] Проверить back navigation: должно возвращать в исходную вкладку и сохранять scroll/filter.

## History

- [ ] Home history icon открывает transaction history page.
- [ ] Perps history icon должен открывать perps history, а не общий history.
- [ ] Сравнить empty state, title, back/close button.
- [ ] Проверить back/close hitbox.
- [ ] Проверить скролл history, если есть список.

## Manage Assets

- [ ] Manage screen открывается с Home layout icon.
- [ ] Layout selector: 3 режима, visual selected ring.
- [ ] Toggles: small assets, NFTs, Predictions, Perps.
- [ ] Сейчас toggles меняют local booleans; проверить визуальное состояние после нажатия.
- [ ] Сравнить labels, порядок, spacing с референсом.
- [ ] Проверить close/back hitbox.

## Sheets / Bottom Sheets

- [ ] Общая высота sheet сейчас `1260..2268`; сравнить с Trust Wallet на разных sheets.
- [ ] Close tap работает через `y < 1260` или close zone; нужно проверить, не закрывается ли sheet случайно при tap по dim area.
- [ ] Sheet title typography.
- [ ] Sheet row height and dividers.
- [ ] Selected checkmark style.
- [x] Network sheet: market/perps context должен иметь разные тексты.
- [x] Sort sheet: market/perps context должен иметь разные тексты.
- [ ] Range sheet: 24h/7d/30d должен менять chart/list labels.
- [ ] Safe action sheet: слишком generic; заменить на конкретные mock-экраны для Send/Receive/Buy/Fund/Deposit/Settings.
- [ ] Wallet sheet: сделать похожим на Trust Wallet wallet selector.

## Скролл И Жесты

- [x] `adb input swipe` работает после включения USB debugging security.
- [x] Home scroll работает.
- [x] Markets scroll работает.
- [x] Perps scroll работает.
- [x] Markets: после смены фильтра reset scroll.
- [x] Perps: после смены фильтра reset scroll.
- [ ] Discover: проверить scroll bounds.
- [ ] Home: проверить scroll bounds для Crypto/Favorites/NFT.
- [ ] Проверить fling/быстрый свайп, сейчас обработка `ACTION_MOVE` без инерции.
- [ ] Проверить tap после небольшого движения: `moved` threshold может съедать легкие taps.
- [ ] Проверить nested areas: bottom nav не должен перехватываться scroll surface.

## Live Data / Fallback

- [x] Добавлен fallback список, чтобы строки не пропадали до загрузки API.
- [ ] Проверить, когда API догружается, нет ли визуального скачка списка.
- [ ] Проверить fallback сортировку и фильтрацию.
- [ ] Проверить CoinGecko key/API handling; сейчас есть hardcoded key.
- [ ] Проверить offline mode: все вкладки должны выглядеть заполненными.
- [ ] Проверить repeated `fetchMarketData()` каждые 30 секунд: не дергает ли UI слишком заметно.

## Assets

- [ ] Перегенерировать `bottom-nav-perps.png` качественно, не через ручной System.Drawing patch.
- [ ] Проверить все `assets/ui/full-*` и `*-page-content.png` на грязные вшитые элементы.
- [ ] Проверить `markets-page-content-rich.png`: содержит статичные строки, которые конфликтуют с live overlay.
- [ ] Проверить `perps-page-content.png`: содержит статичные строки и фильтры, которые пришлось перекрывать.
- [ ] Решить стратегию: либо полностью native Canvas lists/filters, либо чистые screenshot assets без статичных списков.
- [ ] Добавить настоящие token/provider icons.

## Приоритет Исправлений

- [ ] P0: Довести Perps active bottom nav до референса.
- [x] P0: Разделить market/perps sheets по контексту.
- [x] P0: Сброс `scrollY` при смене фильтров.
- [ ] P0: Убрать конфликты статичных page-content assets и native overlays.
- [ ] P1: Настоящие token icons в Markets/Perps/Search/Sheets.
- [ ] P1: Отдельный Perps detail вместо generic token detail.
- [x] P1: Send/Receive/Buy/Fund/Deposit перестать открывать generic safe sheet.
- [ ] P1: Search screen closer to Trust Wallet.
- [ ] P2: Инерционный скролл/fling.
- [ ] P2: Chart range chips с разными данными.
- [ ] P2: Pixel polish: font sizes, exact colors, radii, spacing.

## Технические Замечания

- [ ] `javac` иногда печатает `AccessDeniedException` при закрытии `android.jar`, но APK создается, подписывается и устанавливается. Нужно отдельно разобраться с JDK/SDK file lock.
- [ ] В проекте нет нормальных автотестов UI; полезно добавить простой adb smoke script.
- [ ] Нужно добавить команду capture suite: Home/Markets/Perps/Swap/Discover до и после скролла.
- [ ] Нужно хранить свежие сравнения в отдельной папке `phone_capture/audit_YYYYMMDD`.
- [ ] Желательно вынести координаты hitbox'ов в константы/таблицу, сейчас они разбросаны по `onTouchEvent`.

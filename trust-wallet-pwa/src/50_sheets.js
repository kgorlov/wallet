function drawSheet() {
  if (state.sheet === "sendList") drawSendList();
  else if (state.sheet === "sendForm") drawSendForm();
  else if (state.sheet === "receiveList") drawReceiveList();
  else if (state.sheet === "receiveQr") drawReceiveQr();
  else if (state.sheet === "buy") drawBuySheet();
  else if (state.sheet === "swapToken") drawSwapTokenSheet();
  else if (state.sheet === "swapDisabled") drawSwapDisabledSheet();
  else if (state.sheet === "wallet") drawWalletSheet();
  else if (state.sheet === "copied") drawCopiedSheet();
  else if (state.sheet === "settings") drawSettingsSheet();
  else if (state.sheet === "search") drawSearchSheet();
  else if (state.sheet === "scan") drawScanSheet();
  else if (state.sheet === "support") drawSupportSheet();
  else if (state.sheet === "about") drawAboutSheet();
  else if (state.sheet === "perpsDeposit") drawPerpsDepositSheet();
  else if (state.sheet === "perpsSettings") drawPerpsSettingsSheet();
  else if (state.sheet === "perpsHistory") drawPerpsHistorySheet();
  else if (state.sheet === "predictionAction") drawPredictionActionSheet();
  else if (state.sheet === "rewardDetail") drawRewardDetailSheet();
  else if (state.sheet === "network") drawNetworkSheet();
  else if (state.sheet === "confirm") drawConfirm();
  else if (state.sheet === "processing") drawProcessing();
  else if (state.sheet === "txDetail") drawTxDetail();
  else if (state.sheet === "trade") drawTradeSheet();
}

function sheetBg() {
  rect(0, 0, 1080, 190, "#050505", 0);
  drawStatus();
  rect(0, 190, 1080, 2210, BG, 36);
}

function sheetActionY(defaultY = 2120) {
  return Math.min(defaultY, BASE_H - 260);
}

function bottomSheetTop(defaultTop, height, minTop = 190) {
  return Math.min(defaultTop, Math.max(minTop, BASE_H - height));
}

function receiveQrMetrics() {
  const compact = BASE_H < 2020;
  const short = BASE_H < 2180;
  const cardSize = compact ? 520 : short ? 576 : 636;
  const cardY = compact ? 700 : short ? 700 : 700;
  const actionY = cardY + cardSize + (compact ? 210 : short ? 225 : 242);
  const exchangeY = Math.min(actionY + (compact ? 205 : short ? 230 : 264), BASE_H - 300);
  return {
    cardX: Math.round((1080 - cardSize) / 2),
    cardY,
    cardSize,
    actionY,
    exchangeY: Math.max(actionY + 160, exchangeY)
  };
}

function receiveListLayout() {
  const compact = BASE_H < 2180;
  return {
    startY: compact ? 815 : 812,
    step: compact ? 125 : 158,
    footerOffset: compact ? 48 : 90,
    rowHitTop: 92,
    rowHitBottom: compact ? 45 : 64
  };
}

function receiveListOptions() {
  return [
    ["USDT", "BNB Smart Chain", "0xa61e0...74B0C99", "assets/coins/usdt.png", "assets/coins/714.webp"],
    ["BTC", "Bitcoin", "bc1q0ml...e4qapxh", "assets/coins/0.webp"],
    ["ETH", "Ethereum", "0xa61e0...74B0C99", "assets/coins/60.webp"],
    ["SOL", "Solana", "BVqTeke...L4fyWiW", "assets/coins/501.webp"],
    ["TWT", "BNB Smart Chain", "0xa61e0...74B0C99", "assets/native-ui/ic_splash.png", "assets/coins/714.webp"],
    ["BNB", "BNB Smart Chain", "0xa61e0...74B0C99", "assets/coins/714.webp"],
    ["USDT", "Ethereum", "0xa61e0...74B0C99", "assets/coins/usdt.png", "assets/coins/60.webp"],
    ["USDC", "Ethereum", "0xa61e0...74B0C99", "assets/coins/usdc.png", "assets/coins/60.webp"]
  ];
}

function drawSendList() {
  sheetBg();
  iconClose(100, 285, MUTED, 48);
  text("Отправить", 540, 285, 48, TEXT, "center", 500);
  rect(44, 350, 992, 110, "#2b2c30", 55);
  drawSearch(104, 405, MUTED, 0.8);
  text("Поиск", 165, 420, 38, MUTED, "left", 400);
  sendRow(710, "USDT", "BNB Smart Chain", "827,582", "826,24 $", "assets/coins/usdt.png", "assets/coins/714.webp");
  sendRow(885, "TRX", "Tron", "82,4835", "30,80 $", "assets/coins/195.webp");
  sendRow(1060, "USDT", "TON", "4,356", "4,35 $", "assets/coins/usdt.png", "ton");
}

function sendRow(y, symbol, network, bal, fiat, coin, badge) {
  circleImg(coin, 44, y - 70, 104);
  if (badge === "ton") drawTonBadge(105, y + 2, 42);
  else if (badge) circleImg(badge, 105, y + 2, 42);
  text(symbol === "USDT_TON" ? "USDT" : symbol, 186, y - 8, 42, TEXT, "left", 600);
  tokenPill(340, y - 48, network);
  text(assetName(symbol), 186, y + 42, 34, MUTED, "left", 400);
  text(bal, 980, y - 12, 40, TEXT, "right", 500);
  text(fiat, 980, y + 42, 32, MUTED, "right", 400);
}

function assetName(symbol) {
  if (symbol === "BTC") return "Bitcoin";
  if (symbol === "ETH") return "Ethereum";
  if (symbol === "SOL") return "Solana";
  if (symbol === "BNB") return "BNB";
  if (symbol === "TRX") return "TRON";
  if (symbol === "USDC") return "USD Coin";
  return "Tether USD";
}

function drawReceiveList() {
  const layout = receiveListLayout();
  sheetBg();
  drawReceiveStatus();
  iconClose(100, 285, MUTED, 48);
  text("Получить", 540, 285, 48, TEXT, "center", 500);
  drawAssetPickerHeader();
  text("Популярное", 44, 690, 36, MUTED, "left", 600);
  receiveListOptions().forEach((row, index) => {
    receiveRow(layout.startY + index * layout.step, ...row);
  });
  text("Все криптовалюты", 44, layout.startY + receiveListOptions().length * layout.step + layout.footerOffset, 36, MUTED, "left", 600);
}

function drawAssetPickerHeader() {
  rect(44, 350, 992, 110, "#2b2c30", 55);
  drawSearch(104, 405, MUTED, 0.8);
  text("Поиск", 165, 420, 38, MUTED, "left", 400);
  const chips = [
    ["Все", null],
    ["BTC", "assets/coins/0.webp"],
    ["ETH", "assets/coins/60.webp"],
    ["SOL", "assets/coins/501.webp"],
    ["BNB", "assets/coins/714.webp"],
    ["TRX", "assets/coins/195.webp"],
    ["ARB", "assets/coins/10000787.webp"],
    ["BLUE", "assets/coins/118.webp"],
  ];
  chips.forEach(([label, coin], i) => {
    const x = 44 + i * 126;
    if (i === 0) {
      strokeRect(x, 512, 92, 92, GREEN, 12, 3);
      text(label, x + 46, 570, 36, GREEN, "center", 600);
    } else {
      circleImg(coin, x, 512, 92);
    }
  });
}

function receiveDisplaySymbol(symbol = state.receiveAsset, network = state.receiveNetwork) {
  return symbol === "USDT" && network === "BNB Smart Chain" ? "U" : symbol;
}

function drawReceiveStatus() {
  rect(0, 0, 1080, 190, "#050505", 0);
  text("21:53", 132, 80, 34, TEXT, "left", 700);
  for (let i = 0; i < 4; i++) rect(770 + i * 13, 82 - i * 9, 8, 11 + i * 9, TEXT, 2);
  text("LTE", 850, 78, 31, TEXT, "left", 600);
  strokeRect(936, 45, 58, 30, TEXT, 8, 3);
  rect(997, 55, 4, 10, TEXT, 2);
  rect(945, 52, 38, 16, "#f3f3f4", 4);
  text("65", 964, 69, 20, "#171718", "center", 800);
}

function receiveRow(y, symbol, network, address, coin, badge = null) {
  circleImg(coin, 44, y - 94, 96);
  if (badge) circleImg(badge, 94, y - 44, 42);
  text(receiveDisplaySymbol(symbol, network), 186, y - 52, 43, TEXT, "left", 700);
  tokenPill(310, y - 98, network);
  text(address, 186, y, 36, MUTED, "left", 400);
  imgTint("assets/native-ui/qr-code-icon.png", 836, y - 78, 48, 48, MUTED);
  imgTint("assets/native-ui/copy-icon.svg", 960, y - 82, 48, 48, MUTED);
}

function drawReceiveQr() {
  const layout = receiveQrMetrics();
  sheetBg();
  drawReceiveStatus();
  iconBack(70, 285, MUTED, 56);
  text("Получить", 540, 285, 48, TEXT, "center", 500);
  imgTint("assets/native-ui/info-icon.svg", 954, 261, 48, 48, MUTED);
  rect(44, 352, 992, 188, "#393118", 26);
  text(`Отправляйте только активы ${receiveDisplaySymbol()} (BEP20) на этот`, 138, 420, 34, TEXT, "left", 400);
  text("адрес. Остальные активы будут безвозвратно", 138, 465, 34, TEXT, "left", 400);
  text("утеряны.", 138, 510, 34, TEXT, "left", 400);
  imgTint("assets/native-ui/info-icon.svg", 72, 422, 46, 46, "#efbe26");
  circleImg("assets/coins/usdt.png", 298, 583, 104);
  circleImg("assets/coins/714.webp", 360, 645, 42);
  text(receiveDisplaySymbol(), 412, 650, 42, TEXT, "left", 700);
  tokenPill(520, 602, state.receiveNetwork);
  drawQrCard(layout.cardX, layout.cardY, layout.cardSize);
  receiveAction(264, layout.actionY, 0, "Копировать");
  receiveAction(540, layout.actionY, 1, "Укажите сумму");
  receiveAction(816, layout.actionY, 2, "Поделиться");
  rect(44, layout.exchangeY, 992, 220, PANEL, 24);
  rect(92, layout.exchangeY + 60, 96, 96, "#30915e", 48);
  drawTxArrow(140, layout.exchangeY + 108, false);
  text("Ввод с биржи", 248, layout.exchangeY + 102, 42, TEXT, "left", 600);
  text("Прямой перевод с вашего счета", 248, layout.exchangeY + 162, 38, MUTED, "left", 400);
}

function drawBuySheet() {
  const actionY = sheetActionY();
  sheetBg();
  iconClose(100, 285, MUTED, 48);
  text("Покупка", 540, 285, 48, TEXT, "center", 500);
  rect(44, 360, 992, 220, PANEL, 30);
  text("Выберите актив", 88, 450, 42, TEXT, "left", 600);
  text("Покупка в UI-прототипе открывает локальный список активов.", 88, 512, 32, MUTED, "left", 400);
  receiveRow(760, "USDT", "BNB Smart Chain", "Купить Tether USD", "assets/coins/usdt.png", "assets/coins/714.webp");
  receiveRow(920, "BTC", "Bitcoin", "Купить Bitcoin", "assets/coins/0.webp");
  receiveRow(1080, "ETH", "Ethereum", "Купить Ethereum", "assets/coins/60.webp");
  rect(44, actionY, 992, 145, GREEN, 72);
  text("Продолжить", 540, actionY + 88, 42, "#121814", "center", 500);
}

function drawWalletSheet() {
  rect(0, 1140, 1080, 1260, BG, 42);
  rect(488, 1185, 104, 8, "#8b8c92", 4);
  text("Кошельки", 540, 1290, 48, TEXT, "center", 600);
  walletRow(1450, "Основной кошелек", "Multi-Coin Wallet", true);
  walletRow(1625, "Добавить кошелек", "Создать или импортировать", false);
  const actionY = sheetActionY(2050);
  rect(88, actionY, 904, 126, GREEN, 63);
  text("Управление кошельками", 540, actionY + 78, 40, "#121814", "center", 600);
}

function walletRow(y, title, subtitle, selected) {
  rect(44, y - 88, 992, 138, PANEL, 28);
  rect(88, y - 52, 72, 72, selected ? "#1c4b2e" : "#303136", 36);
  text(selected ? "25" : "+", 124, y - 6, selected ? 28 : 48, selected ? GREEN : TEXT, "center", 700);
  text(title, 190, y - 22, 40, TEXT, "left", 600);
  text(subtitle, 190, y + 28, 31, MUTED, "left", 400);
  if (selected) img("assets/native-ui/network-selected-check.png", 940, y - 34, 58, 58);
}

function drawCopiedSheet() {
  const top = bottomSheetTop(1575, 825);
  const actionY = Math.min(top + 495, BASE_H - 260);
  rect(0, top, 1080, 825, BG, 42);
  text("Адрес", 540, top + 130, 48, TEXT, "center", 600);
  rect(88, top + 240, 904, 145, PANEL, 28);
  rect(128, top + 273, 78, 78, "#1c4b2e", 39);
  img("assets/native-ui/network-selected-check.png", 138, top + 286, 58, 58);
  text("Адрес скопирован", 240, top + 300, 40, TEXT, "left", 600);
  text("0xa61e0...74B0C99", 240, top + 350, 32, MUTED, "left", 400);
  rect(88, actionY, 904, 126, GREEN, 63);
  text("Готово", 540, actionY + 78, 40, "#121814", "center", 600);
}

function drawSettingsSheet() {
  sheetBg();
  iconClose(100, 285, MUTED, 48);
  text("Настройки", 540, 285, 48, TEXT, "center", 600);
  settingsRow(470, "Кошельки", "Основной кошелек");
  settingsRow(625, "Безопасность", "Пароль, Face ID");
  settingsRow(780, "Уведомления", "Настройки оповещений");
  settingsRow(935, "Предпочтения", "Валюта, язык, тема");
  settingsRow(1090, "Поддержка", "Справочный центр");
}

function settingsRow(y, title, subtitle) {
  rect(44, y - 88, 992, 124, PANEL, 28);
  text(title, 92, y - 25, 39, TEXT, "left", 600);
  text(subtitle, 92, y + 24, 30, MUTED, "left", 400);
  imgTint("assets/ui/chevron-right.png", 958, y - 22, 44, 44, MUTED);
}

function drawSearchSheet() {
  sheetBg();
  iconBack(70, 285, MUTED, 56);
  text("Поиск", 540, 285, 48, TEXT, "center", 600);
  rect(44, 350, 992, 110, "#2b2c30", 55);
  drawSearch(104, 405, MUTED, 0.8);
  text("Поиск токена или dApp", 165, 420, 38, MUTED, "left", 400);
  text("Популярное", 44, 570, 36, MUTED, "left", 600);
  searchRow(725, "BTC", "Bitcoin", "$73,964", "+0.39%", GREEN, "assets/coins/0.webp");
  searchRow(900, "ETH", "Ethereum", "$2,031", "+0.64%", GREEN, "assets/coins/60.webp");
  searchRow(1075, "USDT", "Tether USD", "$0.9985", "-0.03%", RED, "assets/coins/usdt.png");
  searchRow(1250, "BNB", "BNB", "$739.26", "+12.48%", GREEN, "assets/coins/714.webp");
}

function drawSupportSheet() {
  sheetBg();
  iconClose(100, 285, MUTED, 48);
  text("Поддержка", 540, 285, 48, TEXT, "center", 600);
  rect(44, 390, 992, 180, PANEL, 32);
  text("Чем можем помочь?", 88, 470, 44, TEXT, "left", 600);
  text("Поиск по справочному центру", 88, 525, 32, MUTED, "left", 400);
  supportRow(730, "Связаться с поддержкой", "Ответим в защищенном чате");
  supportRow(890, "Статус сервисов", "Сети, API и провайдеры");
  supportRow(1050, "Сообщить о проблеме", "Приложите детали и скриншот");
  rect(88, 1710, 904, 126, GREEN, 63);
  text("Открыть центр помощи", 540, 1788, 38, "#121814", "center", 700);
}

function supportRow(y, title, subtitle) {
  rect(44, y - 88, 992, 124, PANEL, 28);
  text(title, 92, y - 25, 38, TEXT, "left", 600);
  text(subtitle, 92, y + 24, 30, MUTED, "left", 400);
  imgTint("assets/ui/chevron-right.png", 958, y - 22, 44, 44, MUTED);
}

function drawAboutSheet() {
  sheetBg();
  iconClose(100, 285, MUTED, 48);
  text("О приложении", 540, 285, 48, TEXT, "center", 600);
  rect(430, 410, 220, 220, "#193e2c", 58);
  circleImg("assets/native-ui/reward-tier-bronze.png", 470, 450, 140);
  text("Trust Wallet WEB", 540, 735, 48, TEXT, "center", 700);
  text("Canvas PWA prototype", 540, 790, 32, MUTED, "center", 400);
  supportRow(980, "Версия интерфейса", "canvas40");
  supportRow(1140, "Service worker", "trust-visual-web-v35");
  supportRow(1300, "Сеть теста", "LAN / Android Chrome");
  rect(88, 1710, 904, 126, "#232427", 63);
  text("Готово", 540, 1788, 40, TEXT, "center", 600);
}

function searchRow(y, symbol, name, price, pct, color, coin) {
  circleImg(coin, 44, y - 70, 88);
  text(symbol, 160, y - 18, 44, TEXT, "left", 600);
  text(name, 160, y + 34, 32, MUTED, "left", 400);
  text(price, 1030, y - 18, 40, TEXT, "right", 600);
  text(pct, 1030, y + 38, 32, color, "right", 500);
}

function drawScanSheet() {
  rect(0, 0, 1080, BASE_H, "#050505", 0);
  drawStatus();
  text("Barcode Scan", 540, 285, 48, TEXT, "center", 500);
  rect(0, 350, 1080, BASE_H - 350, "#101113", 0);
  imgTint("assets/native-ui/qr-code-icon.png", 476, 1040, 128, 128, MUTED);
  text("camera permission not given", 540, 1235, 38, MUTED, "center", 400);
}

function drawRewardDetailSheet() {
  sheetBg();
  iconClose(100, 285, MUTED, 48);
  text("Награда", 540, 285, 48, TEXT, "center", 600);
  img("assets/native-ui/reward-detail-xp-card.png", 402, 430, 276, 190);
  text(state.rewardsTab === "past" ? "Trust Alpha" : "Новые кампании скоро", 540, 735, 46, TEXT, "center", 600);
  text(state.rewardsTab === "past" ? "100 XP уже начислены" : "Следите за заданиями и получайте XP", 540, 795, 34, MUTED, "center", 400);
  rect(88, 930, 904, 112, "#232427", 32);
  text("Статус", 130, 1002, 34, MUTED, "left", 400);
  text(state.rewardsTab === "past" ? "Завершено" : "Скоро", 950, 1002, 34, TEXT, "right", 600);
  rect(88, 1085, 904, 112, "#232427", 32);
  text("Уровень", 130, 1157, 34, MUTED, "left", 400);
  text("Bronze", 950, 1157, 34, TEXT, "right", 600);
  rect(88, 1710, 904, 126, GREEN, 63);
  text("Понятно", 540, 1788, 40, "#121814", "center", 700);
}

function drawSendForm() {
  const actionY = sheetActionY() - 20;
  sheetBg();
  iconBack(70, 285, MUTED, 56);
  text(`Отправить ${sendAssetDisplay()}`, 540, 285, 48, TEXT, "center", 500);
  iconClose(998, 285, MUTED, 48);
  text("Адрес или доменное имя", 44, 408, 36, MUTED, "left", 400);
  strokeRect(44, 443, 992, 158, "#5a5b60", 14, 3);
  text(ellipsizeMiddle(state.sendAddress, 21), 88, 543, 42, TEXT, "left", 400);
  rect(493, 504, 54, 54, "#c7c8cf", 27);
  iconClose(525, 543, "#1b1b1d", 24);
  text("Вставить", 590, 543, 40, GREEN, "left", 500);
  imgTint("assets/native-ui/copy-icon.svg", 830, 498, 54, 54, GREEN);
  imgTint("assets/native-ui/qr-code-icon.png", 918, 510, 66, 66, GREEN);
  text("Сеть назначения", 44, 698, 36, MUTED, "left", 400);
  rect(44, 748, 505, 92, "#242529", 46);
  drawNetworkIcon(88, 794, state.sendNetwork, 60);
  text(state.sendNetwork, 142, 808, 36, MUTED, "left", 500);
  drawCaret(495, 778, MUTED);
  text("Сумма", 44, 910, 36, MUTED, "left", 400);
  strokeRect(44, 955, 992, 150, "#5a5b60", 14, 3);
  text(amount(state.sendAmount), 88, 1047, 42, TEXT, "left", 400);
  rect(604, 1007, 54, 54, "#c7c8cf", 27);
  iconClose(631, 1047, "#1b1b1d", 24);
  text(sendAssetDisplay(), 720, 1047, 42, MUTED, "left", 600);
  text("Макс", 895, 1047, 42, GREEN, "left", 500);
  text(`≈ $${money(state.sendAmount * sendAssetPrice())}`, 44, 1190, 34, TEXT, "left", 400);
  rect(44, actionY, 992, 145, GREEN, 72);
  text("Далее", 540, actionY + 88, 42, "#121814", "center", 500);
}

function drawNetworkSheet() {
  sheetBg();
  rect(0, 0, 1080, 105, "#070708", 0);
  iconBack(70, 285, MUTED, 56);
  text("Сеть назначения", 540, 285, 48, TEXT, "center", 500);
  text(`Выберите сеть для ${sendAssetDisplay()}`, 44, 420, 36, MUTED, "left", 400);
  ["BNB Smart Chain", "Ethereum", "Tron", "TON"].forEach((net, i) => {
    const y = 560 + i * 145;
    rect(44, y - 72, 992, 118, "#1f2023", 26);
    if (net === "TON") img("assets/native-ui/network-ton-icon.png", 70, y - 45, 64, 64);
    else drawNetworkIcon(102, y - 13, net, 66);
    text(net, 186, y, 42, TEXT, "left", 500);
    const feeText = state.networkFeeZero ? `0 ${feeSymbol(net)}` : `${amount(feeAmount(net), 8)} ${feeSymbol(net)}`;
    text(`Комиссия: ${feeText}`, 186, y + 45, 30, MUTED, "left", 400);
    if (net === state.sendNetwork) img("assets/native-ui/network-selected-check.png", 930, y - 18, 60, 60);
  });
}

function drawConfirm() {
  const fee = state.confirmFeeAmount ?? feeAmount();
  const feeUsd = state.confirmFeeFiat ?? feeFiat();
  const total = state.sendAmount * sendAssetPrice() + feeUsd;
  const actionY = sheetActionY();
  const totalY = actionY - 180;
  sheetBg();
  rect(0, 0, 1080, 105, "#070708", 0);
  iconBack(70, 285, MUTED, 56);
  text("Подтвердите отправку", 540, 285, 48, TEXT, "center", 500);
  imgTint("assets/native-ui/settings-icon.svg", 978, 248, 44, 44, GREEN);
  rect(44, 430, 992, 188, PANEL, 28);
  drawTokenComposite(102, 524, state.sendAsset, state.sendNetwork);
  text(`${money(state.sendAmount * sendAssetPrice())} $`, 222, 512, 42, TEXT, "left", 600);
  text(`${amount(state.sendAmount)} ${sendAssetDisplay()}`, 222, 565, 36, MUTED, "left", 400);
  rect(44, 642, 992, 450, PANEL, 28);
  confirmRow(760, "Из", "Основной кошелек", "0xa61e0...74B0C99");
  confirmRow(900, "На", ellipsizeAddress(state.sendAddress));
  confirmRow(1040, "Сеть", state.sendNetwork);
  rect(44, 1120, 992, 235, PANEL, 28);
  text("Комиссия сети", 88, 1240, 38, MUTED, "left", 500);
  imgTint("assets/native-ui/info-icon.svg", 337, 1208, 42, 42, MUTED);
  drawNetworkIcon(840, 1215, state.sendNetwork, 42);
  text(`${money(feeUsd)} $`, 992, 1226, 38, TEXT, "right", 600);
  text(`${amount(fee, 8)} ${feeSymbol()}`, 992, 1282, 34, MUTED, "right", 400);
  line(0, totalY - 50, 1080, totalY - 50, "#2d2e32", 2);
  rect(44, totalY, 992, 134, PANEL, 30);
  text("Общая стоимость", 88, totalY + 80, 36, MUTED, "left", 500);
  text(`${money(total)} $`, 992, totalY + 80, 40, TEXT, "right", 600);
  rect(44, actionY, 992, 145, "#27d68b", 72);
  text("Подтвердить", 540, actionY + 88, 42, "#121814", "center", 500);
}

function confirmRow(y, label, value, sub = null) {
  text(label, 88, y, 36, MUTED, "left", 500);
  text(value, 992, y, 38, TEXT, "right", 500);
  if (sub) text(sub, 992, y + 52, 34, MUTED, "right", 400);
}

function drawProcessing() {
  const top = 1010;
  rect(0, top, 1080, BASE_H - top, "#171718", 38);
  iconClose(998, 1040, MUTED, 56);
  img("assets/native-ui/processing-success-art.png", 380, 1018, 320, 330);
  text("В обработке", 540, 1482, 54, TEXT, "center", 500);
  text("Транзакция выполняется! В настоящее", 540, 1584, 38, MUTED, "center", 400);
  text("время проводится валидация в", 540, 1636, 38, MUTED, "center", 400);
  text("блокчейне. Это может занять несколько", 540, 1688, 38, MUTED, "center", 400);
  text("минут.", 540, 1740, 38, MUTED, "center", 400);
  rect(88, 2010, 904, 132, "#27d68b", 66);
  text("Детали транзакции", 540, 2092, 42, "#121814", "center", 500);
  rect(0, 2265, 1080, BASE_H - 2265, "#070708", 0);
  img("assets/native-ui/processing-gesture-bar.png", 330, 2360, 420, 35);
}

function drawTxDetail() {
  const tx = state.selectedTx || state.txs[0];
  const top = 748;
  rect(0, top, 1080, BASE_H - top, "#242529", 46);
  rect(488, 770, 104, 8, "#b5b5ba", 4);
  imgTint("assets/native-ui/share-icon.svg", 62, 817, 52, 52, MUTED);
  iconClose(990, 864, MUTED, 48);
  text(tx?.title || "Отправлено", 540, 883, 46, TEXT, "center", 500);
  text(tx?.fiat || "≈ $1,00", 540, 1038, 58, TEXT, "center", 500);
  text(tx?.amount || "-1 USDT", 540, 1108, 36, MUTED, "center", 400);
  rect(44, 1247, 992, 350, "#313236", 26);
  txLine(1332, "Дата", tx?.date || "30 мая 2026 г. 23:20");
  txLine(1435, "Статус", "Завершено", GREEN, true);
  txLine(1538, tx?.sent ? "Получатель" : "Отправитель", ellipsizeAddress(tx?.rawAddress || state.sendAddress));
  rect(44, 1665, 992, 195, "#313236", 26);
  text("Комиссия сети", 88, 1767, 34, MUTED, "left", 400);
  imgTint("assets/native-ui/info-icon.svg", 296, 1739, 38, 38, MUTED);
  text(tx?.feeAmount || `${amount(feeAmount(), 8)} ${feeSymbol()}`, 992, 1760, 36, TEXT, "right", 400);
  if (tx?.feeFiat !== "") text(tx?.feeFiat || `≈ $${money(feeFiat())}`, 992, 1813, 34, MUTED, "right", 400);
  strokeRect(44, 2038, 992, 158, "#3d3e42", 18, 2, [8, 8]);
  text("Посмотреть в обозревателе блоков", 540, 2134, 36, GREEN, "center", 500);
}

function txLine(y, label, value, color = TEXT, info = false) {
  text(label, 88, y, 34, MUTED, "left", 400);
  if (info) imgTint("assets/native-ui/info-icon.svg", 202, y - 28, 38, 38, MUTED);
  text(value, 992, y, 36, color, "right", 400);
}


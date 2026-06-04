function drawTradeSheet() {
  const sheet = tradeSheetMetrics();
  rect(44, sheet.top, 992, sheet.height, "#2c2d31", 42);
  tradeRow(sheet.swapY, "Обмен", "Обменивайте любую\nкриптовалюту мгновенно", 0);
  tradeRow(sheet.perpsY, "Бессрочные фьючерсы", "Торгуйте на рынке вне зависимости от\nего направления", 1);
  tradeRow(sheet.predictionsY, "Прогнозы", "Торгуйте на событиях реального мира", 2);
  drawHomeBottomNav(0.45);
}

function tradeSheetMetrics() {
  const height = 670;
  const top = Math.min(1465, Math.max(940, NAV_TOP - height - 42));
  return {
    top,
    height,
    swapY: top + 90,
    perpsY: top + 275,
    predictionsY: top + 480,
  };
}

function drawSwapPage() {
  iconClose(76, 190, MUTED, 48);
  text("Своп", 540, 190, 52, TEXT, "center", 600);
  imgTint("assets/native-ui/tab-layout-mask.png", 959, 144, 62, 49, MUTED);
  rect(44, 296, 992, 294, PANEL, 34);
  text("0", 92, 428, 84, TEXT, "left", 600);
  if (state.swapReversed && state.swapToken) drawSwapTokenPill(646, 350, state.swapToken);
  else drawFundPill(705, 350);
  rect(44, 616, 992, 188, PANEL, 34);
  text("0", 92, 732, 70, MUTED, "left", 600);
  if (state.swapReversed && state.swapToken) drawFundPill(705, 650);
  else drawSwapTokenPill(646, 650, state.swapToken || "Select token");
  rect(496, 568, 88, 88, "#161719", 44);
  imgTint("assets/native-ui/arrow-down-icon.svg", 516, 604, 48, 48, state.swapReversed ? GREEN : MUTED);
  rect(44, BASE_H - 328, 992, 132, "#22673d", 66);
  rect(44, BASE_H - 328, 250, 132, "#42be70", 66);
  imgTint("assets/native-ui/arrow-right-icon.svg", 119, BASE_H - 273, 56, 56, "#8ce6aa");
  text("Сдвиньте вправо для свопа", 575, BASE_H - 246, 38, "#91b49b", "center", 600);
}

function drawPerpsPage() {
  drawPerpsStatus();
  imgTint("assets/native-ui/clock-icon.svg", 50, 141, 56, 56, TEXT);
  text("Бесср.", 540, 185, 52, TEXT, "center", 700);
  imgTint("assets/native-ui/settings-icon.svg", 976, 146, 48, 48, TEXT);
  rect(44, 236, 992, 555, PANEL, 38);
  drawInfinityArt(540, 398);
  text("Торговля бесср. фьючерсами", 540, 552, 45, TEXT, "center", 700);
  rect(88, 616, 904, 132, GREEN, 66);
  text("Депозит", 540, 698, 44, "#07150f", "center", 800);
  rect(44, 870, 992, 106, "#2b2c30", 53);
  drawSearch(105, 924, MUTED, 0.8);
  text("Искать рынки", 143, 937, 38, MUTED, "left", 700);
  perpsCategoryRow();
  perpsFilterRow();
  perpsRow(1388, "BTC", "$76,720", "+1.47%", "assets/coins/0.webp", "$2.43B Vol · 40x", "green");
  perpsRow(1564, "ETH", "$2,120.6", "+2.43%", "assets/coins/60.webp", "$1.11B Vol · 25x", "green");
  perpsRow(1740, "HYPE", "$59.820", "+7.58%", "assets/native-ui/hyperliquid-provider-icon.png", "$705.63M Vol · 10x", "green");
  perpsRow(1916, "CL", "$89.721", "-9.23%", "assets/coins/144.webp", "$421.39M Vol · 20x", "red");
  perpsRow(2092, "ZEC", "$633.94", "+2.41%", "assets/coins/133.webp", "$310.44M Vol · 10x", "green");
  perpsRow(2268, "BRENT", "$65.12", "-0.30%", "assets/coins/118.webp", "$242.18M Vol · 20x", "red");
  drawPerpsBottomNav();
  drawAndroidNavBar();
}

function perpsRow(y, symbol, price, pct, coin, meta, trend) {
  drawPerpsMarketIcon(symbol, coin, 44, y - 70, 88);
  text(symbol, 185, y - 20, 48, TEXT, "left", 600);
  text(meta, 185, y + 42, 34, MUTED, "left", 500);
  sparkFilled(552, y - 30, 164, trend === "red" ? RED : GREEN, 72);
  rect(696, y + 12, 14, 14, trend === "red" ? RED : GREEN, 7);
  text(price, 1030, y - 20, 46, TEXT, "right", 600);
  text(pct, 1030, y + 48, 34, trend === "red" ? RED : GREEN, "right", 600);
}

function drawPerpsStatus() {
  drawStatus();
}

function drawInfinityArt(cx, cy) {
  img("assets/native-ui/hyperliquid-promo-art.png", cx - 180, cy - 115, 360, 230);
}

function perpsCategoryRow() {
  rect(44, 1066, 99, 99, "#2b2c30", 50);
  text("☆", 94, 1132, 56, MUTED, "center", 600);
  rect(165, 1066, 235, 99, "#656966", 50);
  text("Popular", 282, 1130, 39, TEXT, "center", 700);
  rect(420, 1066, 178, 99, "#2b2c30", 50);
  text("New", 509, 1130, 39, MUTED, "center", 700);
  rect(618, 1066, 244, 99, "#2b2c30", 50);
  text("Crypto", 740, 1130, 39, MUTED, "center", 700);
  rect(882, 1066, 220, 99, "#2b2c30", 50);
  text("Stocks", 992, 1130, 39, MUTED, "center", 700);
}

function perpsFilterRow() {
  rect(44, 1210, 462, 66, "#2b2c30", 33);
  drawProviderPair(96, 1243);
  text("Все поставщики", 160, 1255, 34, MUTED, "left", 700);
  drawCaret(446, 1231, MUTED);
  rect(710, 1210, 326, 66, "#2b2c30", 33);
  text("Объем (24 ч.)", 738, 1255, 34, MUTED, "left", 700);
  imgTint("assets/native-ui/arrow-down-icon.svg", 970, 1233, 42, 42, MUTED);
}

function drawProviderPair(cx, cy) {
  rect(cx - 30, cy - 30, 60, 60, "#2c2d31", 30);
  imgTint("assets/native-ui/nav-trade-icon.png", cx - 18, cy - 17, 36, 36, "#91ffea");
  circleImg("assets/coins/714.webp", cx + 22, cy - 27, 60);
}

function drawPerpsMarketIcon(symbol, coin, x, y, size) {
  if (coin) circleImg(coin, x, y, size);
  else {
    rect(x, y, size, size, "#101719", size / 2);
  }
  drawProviderBadge(x + 64, y + 62);
}

function drawProviderBadge(x, y) {
  rect(x, y, 46, 46, "#003f39", 12);
  imgTint("assets/native-ui/nav-trade-icon.png", x + 10, y + 11, 26, 24, "#91ffea");
}

function drawPerpsBottomNav() {
  const y = NAV_TOP - 108;
  ctx.save();
  ctx.fillStyle = "rgba(24,25,27,0.92)";
  pathRoundRect(8, y, 1064, 182, 92);
  ctx.fill();
  strokeRect(8, y, 1064, 182, "#3a3b3e", 92, 3);
  marketsNavItem(150, "home", "Главная", false);
  marketsNavItem(378, "popular", "Рынки", false);
  marketsNavItem(728, "perps", "Бессрочные..", true);
  marketsNavItem(925, "more", "Подробнее", false);
  rect(470, y - 50, 140, 140, GREEN, 70);
  imgTint("assets/native-ui/nav-trade-icon.png", 506, y - 12, 68, 69, "#07150f");
  text("Обмен", 540, y + 148, 26, MUTED, "center", 600);
  ctx.restore();
}

function drawPredictionsPage() {
  iconBack(76, 190, MUTED, 56);
  text("Прогнозы", 540, 190, 52, TEXT, "center", 600);
  drawSearch(990, 168, MUTED, 0.95);
  rect(44, 310, 992, 250, PANEL, 36);
  drawFeatureIcon(150, 435, 0);
  text("Рынки событий", 255, 405, 44, TEXT, "left", 600);
  text("Торгуйте исходами реального мира", 255, 462, 32, MUTED, "left", 400);
  rect(44, 640, 300, 86, "#2b2c30", 43);
  text("Активные", 194, 695, 32, TEXT, "center", 600);
  text("Скоро", 430, 695, 32, MUTED, "center", 500);
  predictionCard(44, 820, "BTC выше $80K к пятнице?", "Да 54% · Нет 46%");
  predictionCard(44, 1135, "ETH закроет день в плюсе?", "Да 61% · Нет 39%");
  predictionCard(44, 1450, "SOL пробьет $100?", "Да 33% · Нет 67%");
  drawBottomNav("trade");
}

function predictionCard(x, y, title, odds) {
  rect(x, y, 992, 260, PANEL, 34);
  rect(x + 36, y + 42, 112, 112, "#243247", 28);
  imgTint("assets/native-ui/markets-predictions-icon.png", x + 54, y + 56, 74, 82, GREEN);
  text(title, x + 190, y + 88, 38, TEXT, "left", 600);
  text(odds, x + 190, y + 142, 30, MUTED, "left", 400);
  rect(x + 190, y + 178, 170, 60, "#1c4b2e", 30);
  text("Да", x + 275, y + 218, 28, GREEN, "center", 700);
  rect(x + 382, y + 178, 170, 60, "#2b2c30", 30);
  text("Нет", x + 467, y + 218, 28, TEXT, "center", 600);
}

function drawFundPill(x, y) {
  rect(x, y, 287, 116, "#161719", 58);
  rect(x + 25, y + 20, 76, 76, "#3d3e42", 38);
  text("+", x + 63, y + 74, 54, TEXT, "center", 400);
  text("Fund", x + 130, y + 74, 38, TEXT, "left", 600);
}

function drawSwapTokenPill(x, y, label) {
  rect(x, y, 346, 103, "#2a583a", 52);
  text(label, x + 173, y + 66, label.length > 11 ? 34 : 42, GREEN, "center", 600);
}

function drawSwapTokenSheet() {
  sheetBg();
  iconClose(100, 285, MUTED, 48);
  text("Выбрать токен", 540, 285, 48, TEXT, "center", 500);
  rect(44, 350, 992, 110, "#2b2c30", 55);
  drawSearch(104, 405, MUTED, 0.8);
  text("Поиск", 165, 420, 38, MUTED, "left", 400);
  sendRow(700, "BTC", "Bitcoin", "0", "0,00 $", "assets/coins/0.webp");
  sendRow(875, "ETH", "Ethereum", "0", "0,00 $", "assets/coins/60.webp");
  sendRow(1050, "SOL", "Solana", "0", "0,00 $", "assets/coins/501.webp");
  sendRow(1225, "BNB", "BNB Smart Chain", "0", "0,00 $", "assets/coins/714.webp");
}

function drawSwapDisabledSheet() {
  const top = bottomSheetTop(1450, 950);
  const actionY = Math.min(top + 560, BASE_H - 260);
  rect(0, top, 1080, 950, BG, 42);
  iconClose(998, top + 120, MUTED, 48);
  text("Своп недоступен", 540, top + 210, 52, TEXT, "center", 600);
  text(state.swapToken ? "Недостаточно средств для свопа." : "Выберите токен, чтобы продолжить.", 540, top + 310, 38, MUTED, "center", 400);
  rect(88, actionY, 904, 132, GREEN, 66);
  text("Понятно", 540, actionY + 82, 42, "#121814", "center", 600);
}

function drawPerpsDepositSheet() {
  sheetBg();
  iconClose(100, 285, MUTED, 48);
  text("Депозит", 540, 285, 48, TEXT, "center", 600);
  rect(44, 420, 992, 180, PANEL, 32);
  text("Пополнить фьючерсный счет", 88, 500, 42, TEXT, "left", 600);
  text("Средства останутся в локальном WEB-прототипе", 88, 552, 31, MUTED, "left", 400);
  receiveRow(780, "USDT", "BNB Smart Chain", "0,00 $", "assets/coins/usdt.png", "assets/coins/714.webp");
  receiveRow(940, "USDC", "Ethereum", "0,00 $", "assets/coins/usdc.png", "assets/coins/60.webp");
  rect(88, 1710, 904, 126, GREEN, 63);
  text("Выбрать актив", 540, 1788, 40, "#07150f", "center", 700);
}

function drawPerpsSettingsSheet() {
  sheetBg();
  iconClose(100, 285, MUTED, 48);
  text("Настройки фьючерсов", 540, 285, 48, TEXT, "center", 600);
  settingsRow(470, "Кредитное плечо", "До 20x по умолчанию");
  settingsRow(625, "Режим маржи", "Изолированная");
  settingsRow(780, "Подтверждение ордера", "Всегда показывать");
  settingsRow(935, "Провайдер", "Все поставщики");
  rect(88, 1710, 904, 126, "#232427", 63);
  text("Готово", 540, 1788, 40, TEXT, "center", 600);
}

function drawPerpsHistorySheet() {
  sheetBg();
  iconClose(100, 285, MUTED, 48);
  text("История фьючерсов", 540, 285, 48, TEXT, "center", 600);
  rect(44, 520, 992, 360, PANEL, 36);
  text("Открытых позиций нет", 540, 650, 46, TEXT, "center", 600);
  text("Ваши ордера и сделки появятся здесь.", 540, 715, 34, MUTED, "center", 400);
  rect(88, 1710, 904, 126, "#232427", 63);
  text("Понятно", 540, 1788, 40, TEXT, "center", 600);
}

function drawPredictionActionSheet() {
  sheetBg();
  iconClose(100, 285, MUTED, 48);
  text("Прогноз", 540, 285, 48, TEXT, "center", 600);
  rect(44, 430, 992, 260, PANEL, 36);
  text("BTC выше $80K к пятнице?", 88, 535, 42, TEXT, "left", 600);
  text("Выберите исход. Реальная торговля отключена.", 88, 590, 32, MUTED, "left", 400);
  rect(88, 820, 420, 112, GREEN, 56);
  text("Да", 298, 892, 40, "#07150f", "center", 700);
  rect(572, 820, 420, 112, "#232427", 56);
  text("Нет", 782, 892, 40, TEXT, "center", 600);
  rect(88, 1710, 904, 126, "#232427", 63);
  text("Закрыть", 540, 1788, 40, TEXT, "center", 600);
}

function tradeRow(y, title, subtitle, icon) {
  const iconPath = icon === 0
    ? "assets/native-ui/trade-row-swap-icon.png"
    : icon === 1
      ? "assets/native-ui/trade-row-perps-icon.png"
      : "assets/native-ui/trade-row-predictions-icon.png";
  const iconH = icon === 1 ? 150 : 130;
  img(iconPath, 66, y - (icon === 0 ? 70 : 100), 130, iconH);
  text(title, 200, y - 42, 46, TEXT, "left", 500);
  subtitle.split("\n").forEach((s, i) => text(s, 200, y + 15 + i * 48, 34, MUTED, "left", 400));
}


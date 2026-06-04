function drawPopular() {

  ctx.save();
  ctx.translate(0, -state.scrollY);
  drawMarketsStatus();
  text("Markets", 540, 185, 48, TEXT, "center", 700);
  drawSearch(990, 180, MUTED, 0.95);
  rect(44, 256, 485, 154, PANEL, 42);
  imgTint("assets/native-ui/markets-predictions-icon.png", 86, 284, 73, 81, TEXT);
  text("Predictions", 182, 349, 44, TEXT, "left", 700);
  rect(551, 256, 485, 154, PANEL, 42);
  imgTint("assets/native-ui/markets-meme-rush-icon.png", 584, 284, 90, 83, TEXT);
  text("Meme Rush", 690, 349, 44, TEXT, "left", 700);
  text("Самые торгуемые (24 ч.)", 44, 515, 45, TEXT, "left", 700);
  marketCard(44, 553, "Ethereum", "$2,115.75", "+2.65%", GREEN, "assets/coins/60.webp");
  marketCard(392, 553, "BNB Smart..", "$654.31", "+0.89%", GREEN, "assets/coins/714.webp");
  marketCard(740, 553, "Solana", "$85.86", "+1.79%", GREEN, "assets/coins/501.webp");
  marketsCategoryRow();
  filterRow();
  marketRow(1307, "LINK", "$9.53", "+0.02%", GREEN, "assets/coins/link.png", "$9.53B MCap · $6.54M Vol");
  marketRow(1488, "CAKE", "$1.41", "-0.00%", RED, "assets/coins/714.webp", "$6.12B MCap · $1.35M Vol");
  marketRow(1669, "ASTER", "$0.6803", "+0.02%", GREEN, "assets/coins/10000787.webp", "$5.44B MCap · $3.46M Vol");
  marketRow(1850, "XAUt", "$4,377.91", "-0.03%", RED, "assets/coins/118.webp", "$3.10B MCap · $2.65M Vol");
  marketRow(2031, "PAXG", "$4,544.82", "+0.02%", GREEN, "assets/coins/133.webp", "$944.2M MCap · $8.1M Vol");
  marketRow(2212, "TRUMP", "$--", "-0.00%", RED, "assets/coins/144.webp", "$1.4B MCap · $1.1M Vol");
  ctx.restore();
  drawScrollIndicator();
  drawMarketsBottomNav();
  drawAndroidNavBar();
}

function drawMarketDetail() {
  drawStatus();
  iconBack(74, 180, MUTED, 56);
  text("BNB", 540, 184, 58, TEXT, "center", 700);
  text("$657.46", 540, 410, 82, TEXT, "center", 500, true);
  text("+0.11%", 540, 490, 42, GREEN, "center", 700);
  drawTokenDetailChart();
  drawTokenRangeChips();
  drawTokenStatsCard();
  rect(346, 2320, 388, 12, "#ffffff", 6);
}

function drawTokenDetailChart() {
  rect(44, 610, 992, 520, PANEL, 36);
  const x = 92;
  const y = 692;
  const w = 900;
  const h = 330;
  const pts = [0.05, 0.16, 0.14, 0.06, 0.07, 0.62, 0.58, 0.96, 1.0, 0.72, 0.72, 0.84, 0.9, 0.78];
  const base = y + h;
  ctx.save();
  ctx.beginPath();
  pts.forEach((p, i) => {
    const px = x + (w * i) / (pts.length - 1);
    const py = y + p * h;
    if (i === 0) ctx.moveTo(px, base);
    ctx.lineTo(px, py);
  });
  ctx.lineTo(x + w, base);
  ctx.closePath();
  ctx.fillStyle = "rgba(48,232,138,0.20)";
  ctx.fill();
  ctx.beginPath();
  pts.forEach((p, i) => {
    const px = x + (w * i) / (pts.length - 1);
    const py = y + p * h;
    if (i === 0) ctx.moveTo(px, py);
    else ctx.lineTo(px, py);
  });
  ctx.strokeStyle = GREEN;
  ctx.lineWidth = 6;
  ctx.lineJoin = "miter";
  ctx.stroke();
  ctx.restore();
  text("24h", 92, 1060, 31, MUTED, "left", 700);
  text("Min $654.72", 540, 1060, 31, MUTED, "center", 700);
  text("Max $662.66", 988, 1060, 31, MUTED, "right", 700);
}

function drawTokenRangeChips() {
  rect(88, 1190, 112, 70, "#676d6a", 35);
  text("24h", 144, 1236, 33, TEXT, "center", 700);
  rect(230, 1190, 112, 70, "#242529", 35);
  text("7d", 286, 1236, 33, TEXT, "center", 700);
  rect(372, 1190, 122, 70, "#242529", 35);
  text("30d", 433, 1236, 33, TEXT, "center", 700);
}

function drawTokenStatsCard() {
  rect(44, 1348, 992, 334, PANEL, 36);
  text("Рыночная капитализация", 88, 1435, 40, MUTED, "left", 700);
  text("$88.60B", 992, 1435, 42, TEXT, "right", 700);
  text("Объем (24 ч.)", 88, 1549, 40, MUTED, "left", 700);
  text("$1.05B", 992, 1549, 42, TEXT, "right", 700);
  text("Цена", 88, 1663, 40, MUTED, "left", 700);
  text("$657.46", 992, 1663, 42, TEXT, "right", 700);
}

function statRow(y, label, value) {
  line(44, y - 66, 1036, y - 66, "#242528", 2);
  text(label, 44, y, 34, MUTED, "left", 400);
  text(value, 1036, y, 36, TEXT, "right", 600);
}

function drawMarketsStatus() {
  drawStatus();
}

function marketCard(x, y, name, price, pct, color, coin) {
  rect(x, y, 330, 383, PANEL, 36);
  text(name, x + 50, y + 76, 32, MUTED, "left", 400);
  drawTopMarketCoin(name, x + 230, y + 38, 58, coin);
  text(price, x + 50, y + 155, 48, TEXT, "left", 500);
  text(pct, x + 50, y + 215, 38, color, "left", 500);
  sparkFilled(x + 58, y + 255, 230, color, 86);
}

function drawTopMarketCoin(name, x, y, size, coin) {
  circleImg(coin, x, y, size);
}

function marketsCategoryRow() {
  rect(44, 988, 99, 99, "#2b2c30", 50);
  text("★", 94, 1052, 54, MUTED, "center", 600);
  rect(165, 988, 220, 99, "#656966", 50);
  text("Hot tokens", 275, 1052, 36, TEXT, "center", 700);
  rect(405, 988, 240, 99, "#2b2c30", 50);
  text("Top Gainers", 525, 1052, 36, TEXT, "center", 700);
  rect(665, 988, 146, 99, "#2b2c30", 50);
  text("RWA", 738, 1052, 36, TEXT, "center", 700);
  rect(831, 988, 164, 99, "#2b2c30", 50);
  text("Meme", 913, 1052, 36, TEXT, "center", 700);
  rect(1015, 988, 164, 99, "#2b2c30", 50);
  text("DeFi", 1097, 1052, 36, TEXT, "center", 700);
}

function filterRow() {
  rect(44, 1130, 190, 66, "#2b2c30", 33);
  text("Сеть", 78, 1174, 34, MUTED, "left", 600);
  drawCaret(176, 1153, MUTED);
  rect(309, 1130, 532, 66, "#2b2c30", 33);
  text("Рыночная капитализация", 342, 1174, 34, MUTED, "left", 600);
  imgTint("assets/native-ui/arrow-down-icon.svg", 764, 1154, 42, 42, MUTED);
  rect(863, 1130, 173, 66, "#2b2c30", 33);
  text("24h", 896, 1174, 34, MUTED, "left", 600);
  drawCaret(980, 1153, MUTED);
}

function marketRow(y, symbol, price, pct, color, coin, meta = "$99.62B MCap · $3.33B Vol") {
  drawMarketIcon(symbol, coin, 44, y - 70, 88);
  text(symbol, 160, y - 18, 48, TEXT, "left", 500);
  text(meta, 160, y + 36, 30, MUTED, "left", 400);
  sparkFilled(760, y - 23, 150, color, 72);
  text(price, 1030, y - 20, 46, TEXT, "right", 500);
  text(pct, 1030, y + 48, 34, color, "right", 500);
}

function drawMarketIcon(symbol, coin, x, y, size) {
  if (coin) circleImg(coin, x, y, size);
  else {
    rect(x, y, size, size, "#050505", size / 2);
    line(x + 22, y + 58, x + 66, y + 20, TEXT, 7);
    line(x + 22, y + 20, x + 66, y + 58, TEXT, 7);
  }
  if (symbol === "LINK" || symbol === "XAUt") circleImg("assets/coins/60.webp", x + 62, y + 60, 38);
  if (symbol === "CAKE" || symbol === "ASTER") circleImg("assets/coins/714.webp", x + 62, y + 60, 38);
}

function sparkFilled(x, y, width, color, height = 72) {
  const pts = color === RED
    ? [0.18, 0.38, 0.82, 0.78, 0.45, 0.35, 0.12, 0.2, 0.18, 0.28]
    : [0.86, 0.72, 0.62, 0.52, 0.20, 0.36, 0.30, 0.22, 0.28, 0.38];
  const base = y + height;
  ctx.save();
  ctx.beginPath();
  pts.forEach((p, i) => {
    const px = x + (width * i) / (pts.length - 1);
    const py = y + p * height;
    if (i === 0) ctx.moveTo(px, base);
    ctx.lineTo(px, py);
  });
  ctx.lineTo(x + width, base);
  ctx.closePath();
  ctx.fillStyle = color === RED ? "rgba(255,92,107,0.22)" : "rgba(48,232,138,0.20)";
  ctx.fill();
  ctx.beginPath();
  pts.forEach((p, i) => {
    const px = x + (width * i) / (pts.length - 1);
    const py = y + p * height;
    if (i === 0) ctx.moveTo(px, py);
    else ctx.lineTo(px, py);
  });
  ctx.strokeStyle = color;
  ctx.lineWidth = 6;
  ctx.lineCap = "round";
  ctx.lineJoin = "round";
  ctx.stroke();
  ctx.restore();
}

function drawAndroidNavBar(yOverride = null) {
  const y = yOverride == null ? Math.max(BASE_H - 82, 2320) : yOverride;
  rect(0, y, 1080, BASE_H - y, BG, 0);
  img("assets/native-ui/android-nav-icons-mask.png", 0, y - 18, 1080, 132);
}

function drawMarketsBottomNav() {
  const y = NAV_TOP - 108;
  ctx.save();
  ctx.fillStyle = "rgba(24,25,27,0.92)";
  pathRoundRect(8, y, 1064, 182, 92);
  ctx.fill();
  strokeRect(8, y, 1064, 182, "#3a3b3e", 92, 3);
  marketsNavItem(150, "home", "Главная", false);
  marketsNavItem(378, "popular", "Рынки", true);
  marketsNavItem(728, "perps", "Бессрочные..", false);
  marketsNavItem(925, "more", "Подробнее", false);
  rect(470, y - 50, 140, 140, GREEN, 70);
  imgTint("assets/native-ui/nav-trade-icon.png", 506, y - 12, 68, 69, "#07150f");
  text("Обмен", 540, y + 148, 26, MUTED, "center", 600);
  ctx.restore();
}

function marketsNavItem(cx, type, label, active) {
  const y = NAV_TOP - 108;
  if (active) rect(cx - 104, y + 10, 208, 152, "#1c4b2e", 76);
  const color = active ? GREEN : MUTED;
  if (type === "home") imgTint("assets/native-ui/nav-main-icon.png", cx - 28, y + 30, 56, 45, color);
  else if (type === "popular") imgTint("assets/native-ui/nav-popular-icon.png", cx - 29, y + 28, 58, 51, color);
  else if (type === "more") imgTint("assets/native-ui/nav-more-icon.png", cx - 27, y + 29, 54, 48, color);
  else circleImg("assets/native-ui/hyperliquid-provider-icon.png", cx - 29, y + 25, 58);
  text(label, cx, y + 138, label.length > 8 ? 23 : 25, color, "center", 600);
}

function drawRewards() {
  rect(0, 0, 1080, 105, BG, 0);
  text("Награды", 540, 205, 50, TEXT, "center", 500);
  drawRewardsHero();
  strokeRect(44, 805, 472, 220, "#313234", 24, 2);
  text("Уровень", 112, 875, 34, MUTED, "left", 400);
  text("100 XP", 112, 950, 46, TEXT, "left", 400);
  text("до Bronze", 112, 1000, 40, TEXT, "left", 400);
  strokeRect(564, 805, 472, 220, "#313234", 24, 2);
  text("Баланс XP", 632, 900, 34, MUTED, "left", 400);
  text("0 XP", 632, 975, 48, TEXT, "left", 400);
  text("Получите XP", 44, 1115, 46, TEXT, "left", 500);
  drawRewardsTabs();
  if (state.rewardsTab === "past") drawRewardsPast();
  else drawRewardsActive();
  drawRewardsBottomNav();
}

function drawRewardsBottomNav() {
  const y = NAV_TOP;
  ctx.save();
  ctx.fillStyle = "rgba(24,25,27,0.92)";
  pathRoundRect(8, y, 1064, 182, 92);
  ctx.fill();
  strokeRect(8, y, 1064, 182, "#3a3b3e", 92, 3);
  navItem(150, "home", "Главная", false);
  navItem(378, "popular", "Популярные", false);
  navItem(728, "rewards", "Награды", true);
  navItem(925, "more", "Подробнее", false);
  rect(470, y - 50, 140, 140, GREEN, 70);
  img("assets/native-ui/nav-trade-icon.png", 506, y - 12, 68, 69);
  text("Торговать", 540, y + 148, 26, TEXT, "center", 500);
  ctx.restore();
  img("assets/native-ui/rewards-gesture-bar.png", 330, 2310, 420, 80);
}

function drawRewardsTabs() {
  rect(44, 1200, 230, 92, state.rewardsTab === "active" ? "#2b2c30" : BG, 46);
  text("Активно", 160, 1260, 38, state.rewardsTab === "active" ? TEXT : MUTED, "center", 500);
  rect(300, 1200, 230, 92, state.rewardsTab === "past" ? "#2b2c30" : BG, 46);
  text("Прошлые", 415, 1260, 38, state.rewardsTab === "past" ? TEXT : MUTED, "center", 500);
}

function drawRewardsActive() {
  if (BASE_H < 2180) {
    rect(44, 1320, 992, 390, PANEL, 34);
    img("assets/native-ui/reward-shield-bronze.png", 106, 1358, 132, 149);
    text("Новые кампании скоро", 310, 1398, 32, MUTED, "left", 400);
    text("Подпишитесь на нас в", 310, 1452, 40, TEXT, "left", 400);
    text("социальных сетях и", 310, 1502, 40, TEXT, "left", 400);
    text("следите за новостями!", 310, 1552, 40, TEXT, "left", 400);
    line(88, 1588, 992, 1588, "#424345", 2);
    rect(88, 1628, 80, 80, "#000", 22);
    text("X", 128, 1680, 35, TEXT, "center", 500);
    text("@TrustWallet", 200, 1680, 35, TEXT, "left", 500);
    rect(902, 1628, 78, 78, "#245b3d", 39);
    text("→", 941, 1680, 42, GREEN, "center", 500);
    return;
  }
  rect(44, 1340, 992, 520, PANEL, 36);
  img("assets/native-ui/rewards-campaign-icon.png", 82, 1370, 208, 195);
  text("Новые кампании скоро", 350, 1435, 34, MUTED, "left", 400);
  text("Подпишитесь на нас в", 350, 1500, 44, TEXT, "left", 400);
  text("социальных сетях и", 350, 1554, 44, TEXT, "left", 400);
  text("следите за новостями!", 350, 1608, 44, TEXT, "left", 400);
  line(88, 1704, 992, 1704, "#424345", 2);
  img("assets/native-ui/rewards-x-icon.png", 78, 1744, 116, 122);
  text("@TrustWallet", 210, 1825, 38, TEXT, "left", 500);
  img("assets/native-ui/rewards-arrow-button.png", 878, 1744, 124, 124);
  text("Trust Alpha", 44, 1958, 48, TEXT, "left", 500);
  text("›", 990, 1958, 62, MUTED, "right", 500);
  rect(44, 2048, 230, 92, "#2b2c30", 46);
  text("Активно", 160, 2108, 38, TEXT, "center", 500);
  text("Прошлые", 415, 2108, 38, MUTED, "center", 500);
}

function drawRewardsPast() {
  pastRewardPromo(44, 1340, "#007752", "Tunz", "3GB", "Free 3GB Global eSIM", "(7 days) with Tunz", "1000XP", "assets/native-ui/rewards-past-card-tunz.png", 464, "assets/native-ui/rewards-past-item-tunz-bottom.png", 464);
  pastRewardPromo(532, 1340, "#ed2b7b", "U", "$50", "$50 hotel coupon", "with Umy", "800XP", "assets/native-ui/rewards-past-card-umy.png", 464, "assets/native-ui/rewards-past-item-umy-bottom.png", 464);
  pastRewardPromo(1020, 1340, "#2c9cca", "", "", "Travel reward", "", "400XP", "assets/native-ui/rewards-past-card-third.png", 60, "assets/native-ui/rewards-past-item-third-bottom.png", 60);
  text("Trust Alpha", 44, 2050, 48, TEXT, "left", 500);
  text("›", 990, 2050, 62, MUTED, "right", 500);
}

function pastRewardPromo(x, y, color, top, mid, title1, title2, xp, artPath = null, artW = 464, bottomPath = null, bottomW = 464) {
  if (artPath) img(artPath, x, y, artW, 300);
  else {
    rect(x, y, 464, 300, color, 28);
    if (top) text(top, x + 232, y + 105, 58, "#ffffff", "center", 500);
    if (mid) text(mid, x + 232, y + 195, 58, "#ffffff", "center", 500);
  }
  if (bottomPath) {
    img(bottomPath, x, y + 300, bottomW, 352);
    return;
  }
  text(title1, x, y + 350, 39, TEXT, "left", 400);
  if (title2) text(title2, x, y + 402, 39, TEXT, "left", 400);
  text(xp, x, y + 505, 42, TEXT, "left", 400);
  rect(x, y + 568, 464, 92, "#165b38", 46);
  text("Посмотреть", x + 232, y + 626, 36, GREEN, "center", 500);
}

function rewardCard(x, y, title, xp, status, tint) {
  rect(x, y, 480, 315, PANEL, 34);
  rect(x + 34, y + 34, 108, 108, tint, 28);
  rect(x + 63, y + 60, 50, 50, GREEN, 16);
  text("XP", x + 88, y + 94, 20, "#07150f", "center", 700);
  text(title, x + 34, y + 190, 38, TEXT, "left", 600);
  text(xp, x + 34, y + 240, 34, GREEN, "left", 600);
  rect(x + 286, y + 212, 154, 62, "#2b2c30", 31);
  text(status, x + 363, y + 253, 25, MUTED, "center", 500);
}

function rewardCardCompact(x, y, title, xp, status, tint) {
  rect(x, y, 480, 166, PANEL, 28);
  rect(x + 26, y + 26, 62, 62, tint, 18);
  rect(x + 43, y + 43, 28, 28, GREEN, 9);
  text("XP", x + 57, y + 62, 12, "#07150f", "center", 700);
  text(title, x + 118, y + 56, 29, TEXT, "left", 600);
  text(xp, x + 118, y + 98, 27, GREEN, "left", 600);
  rect(x + 300, y + 96, 140, 48, "#2b2c30", 24);
  text(status, x + 370, y + 127, 21, MUTED, "center", 500);
}

function drawMore() {
  img("assets/native-ui/discover-status-bar.png", 0, 0, 1080, 102);
  text("Подробнее", 540, 187, 54, TEXT, "center", 500);
  rect(44, 256, 992, 99, "#242424", 50);
  img("assets/native-ui/discover-search-icon-crop.png", 60, 265, 79, 79);
  text("Найти или ввести URL-адрес dApp", 155, 320, 41, "#747476", "left", 500);

  text("Quick links", 44, 472, 44, TEXT, "left", 700);
  rect(44, 537, 485, 281, PANEL, 34);
  text("Заработок", 88, 625, 34, MUTED, "left", 700);
  text("Стейк...", 88, 696, 58, TEXT, "left", 700);
  text("Deposit now", 88, 766, 34, GREEN, "left", 700);
  img("assets/native-ui/discover-sprout-icon.png", 348, 606, 146, 146);

  discoverLinkRow(879, "Trust Wallet website", "assets/native-ui/discover-globe-icon.png");
  discoverLinkRow(1012, "Центр поддержки", "assets/native-ui/discover-support-icon.png");
  discoverLinkRow(1145, "Use dApp securely for your apps", "assets/native-ui/discover-question-icon.png");
  discoverLinkRow(1278, "What is DeFi?", "assets/native-ui/discover-question-icon.png");
  discoverLinkRow(1411, "What is Token Approval?", "assets/native-ui/discover-question-icon.png");
  drawDiscoverBottomNav();
}

function discoverLinkRow(iconY, label, iconPath) {
  img(iconPath, 30, iconY - 17, 86, 86);
  text(label, 143, iconY + 42, 40, TEXT, "left", 500);
}

function drawDiscoverBottomNav() {
  img("assets/ui/bottom-nav-discover.png", 0, 2020, 1080, 250);
  img("assets/native-ui/discover-android-nav.png", 0, 2268, 1080, 132);
}

function discoverPerpsNavItem(cx, label) {
  const y = NAV_TOP - 108;
  img("assets/native-ui/nav-perps-discover-icon.png", cx - 50, y + 24, 100, 72);
  text(label, cx, y + 138, 23, MUTED, "center", 600);
}

function drawHistory() {
  text("История транзакций", 540, 185, 48, TEXT, "center", 600);
  if (!state.txs.length) {
    img("assets/native-ui/history-back-icon.png", 35, 128, 85, 80);
    img("assets/native-ui/history-empty-dash.png", 528, 1278, 28, 22);
    drawAndroidNavBar();
    return;
  }
  iconBack(72, 185, MUTED, 56);
  rect(44, 302, 330, 98, "#2b2b2e", 49);
  text("Фильтры", 130, 365, 36, MUTED, "left", 400);
  rect(396, 302, 318, 98, "#2b2b2e", 49);
  text("Все сети", 482, 365, 36, MUTED, "left", 400);
  text("Сегодня", 44, 500, 42, TEXT, "left", 600);
  let y = 612;
  state.txs.forEach((tx, i) => {
    historyRow(y, tx.sent, tx.title, tx.address, tx.amount, tx.fiat);
    if (i === 0 && tx.sent) {
      strokeRect(44, y + 112, 992, 158, "#cfcfd4", 28, 2, [8, 8]);
      text("Не можете найти транзакцию? В обозреватель", 540, y + 208, 32, GREEN, "center", 400);
      y += 360;
    } else y += 203;
  });
}

function drawManagePage() {
  drawManageHomeBackdrop();
  rect(0, 409, 1080, BASE_H - 409, BG, 46);
  imgTint("assets/native-ui/close-icon.svg", 976, 478, 48, 48, MUTED);

  rect(44, 598, 992, 520, "#2b2c30", 34);
  text("Расположение актива", 88, 667, 40, "#d7d7dc", "left", 700);
  drawManageAssetPreview();
  drawManageLayoutDots();

  text("Конвертер малых балансов", 88, 1244, 43, TEXT, "left", 700);
  text("Конвертируйте маленькие суммы в нативный токен", 88, 1298, 32, MUTED, "left", 400);
  text("Управлять криптовалютами", 88, 1438, 43, TEXT, "left", 700);
  text("›", 932, 1438, 60, MUTED, "center", 400);
  line(44, 1523, 1036, 1523, "#343538", 2);

  manageSheetRow(1625, "Скрыть активы < 0,01 USD", true);
  manageSheetRow(1799, "Скрыть NFT", false);
  manageSheetRow(1975, "Hide Predictions", false);
  manageSheetRow(2149, "Hide Perps", false);
  drawAndroidNavBar(2281);
}

function drawManageHomeBackdrop() {
  rect(0, 0, 1080, 409, BG, 0);
  ctx.save();
  ctx.globalAlpha = 0.38;
  drawManageStatus();
  img("assets/native-ui/top-settings.png", 44, 140, 78, 78);
  rect(111, 131, 16, 16, "#ff4f5e", 8);
  img("assets/native-ui/top-search-pill.png", 143, 116, 816, 106);
  img("assets/native-ui/top-scan.png", 987, 142, 68, 68);
  rect(318, 295, 448, 88, "#2d2e31", 44);
  text("Основной кошелек 1  ›", 540, 350, 34, TEXT, "center", 700);
  imgTint("assets/native-ui/copy-icon.svg", 792, 313, 44, 44, MUTED);
  ctx.restore();
  rect(0, 0, 1080, 409, "rgba(0,0,0,0.44)", 0);
  drawManageStatus();
}

function drawManageStatus() {
  img("assets/status-bar.png", 0, 0, 1080, 102);
}

function drawManageAssetPreview() {
  rect(90, 722, 898, 193, "#08120d", 18);
  rect(132, 766, 110, 110, "#ff9816", 55);
  text("₿", 187, 847, 72, "#fff", "center", 800);
  text("BTC", 286, 808, 43, TEXT, "left", 700);
  rect(384, 767, 158, 58, "#2c2d30", 29);
  text("Bitcoin", 463, 806, 32, TEXT, "center", 700);
  text("$108,200.00", 286, 862, 36, MUTED, "left", 600);
  text("+12.00%", 530, 862, 36, GREEN, "left", 600);
  text("0.5", 946, 808, 43, TEXT, "right", 700);
  text("$54,100.00", 946, 862, 38, MUTED, "right", 600);
}

function drawManageLayoutDots() {
  manageLayoutDot(358, 1020, "1", false);
  manageLayoutDot(540, 1020, "2", false);
  manageLayoutDot(722, 1020, "3", true);
}

function manageLayoutDot(cx, cy, label, selected) {
  rect(cx - 58, cy - 58, 116, 116, "#07100b", 58);
  if (selected) strokeRect(cx - 58, cy - 58, 116, 116, GREEN, 58, 7);
  text(label, cx, cy + 18, 44, TEXT, "center", 700);
}

function manageSheetRow(y, title, on) {
  text(title, 88, y, 45, TEXT, "left", 700);
  drawToggle(807, y - 54, on);
}

function drawLayoutChoice(cx, cy, layout, label, selected) {
  rect(cx - 132, cy - 128, 264, 250, selected ? "#233e2d" : PANEL, 34);
  strokeRect(cx - 132, cy - 128, 264, 250, selected ? GREEN : "#36373a", 34, 4);
  const color = selected ? GREEN : MUTED;
  if (layout === 0) {
    for (let i = 0; i < 3; i++) {
      const rowY = cy - 58 + i * 42;
      strokeRect(cx - 78, rowY - 12, 24, 24, color, 12, 5);
      line(cx - 38, rowY, cx + 72, rowY, color, 5);
    }
  } else if (layout === 1) {
    strokeRect(cx - 76, cy - 72, 152, 68, color, 16, 5);
    strokeRect(cx - 76, cy + 18, 152, 50, color, 16, 5);
  } else {
    strokeRect(cx - 82, cy - 72, 74, 74, color, 16, 5);
    strokeRect(cx + 8, cy - 72, 74, 74, color, 16, 5);
    strokeRect(cx - 82, cy + 18, 164, 50, color, 16, 5);
  }
  text(label, cx, cy + 158, 32, color, "center", 600);
}

function manageRow(y, title, subtitle, on) {
  text(title, 88, y, 38, TEXT, "left", 600);
  text(subtitle, 88, y + 48, 28, MUTED, "left", 400);
  drawToggle(805, y - 40, on);
  line(88, y + 98, 992, y + 98, "#343538", 2);
}

function drawToggle(x, y, on) {
  rect(x, y, 143, 78, on ? GREEN : "#74757a", 39);
  rect(on ? x + 73 : x + 12, y + 11, 56, 56, "#fff", 28);
}

function historyRow(y, sent, title, addr, amt, fiat) {
  rect(44, y - 56, 108, 108, "#2e2f33", 54);
  drawTxArrow(98, y, sent);
  text(title, 186, y - 10, 40, TEXT, "left", 600);
  text(addr, 186, y + 40, 32, MUTED, "left", 400);
  text(amt, 1030, y - 10, 38, amt.startsWith("+") ? GREEN : TEXT, "right", 600);
  text(fiat, 1030, y + 40, 30, MUTED, "right", 400);
}


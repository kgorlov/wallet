function drawHome() {
  ctx.save();
  ctx.translate(0, -state.scrollY);
  drawHomeContent();
  ctx.restore();
  drawScrollIndicator();
  drawHomeBottomNav();
}

function drawHomeContent() {
  const h = HOME_SCREEN;
  img("assets/native-ui/top-settings.png", h.topSettings.x, h.topSettings.y, h.topSettings.w, h.topSettings.h);
  rect(h.notificationDot.x, h.notificationDot.y, h.notificationDot.size, h.notificationDot.size, "#ff4f5e", h.notificationDot.size / 2);
  img("assets/native-ui/top-search-pill.png", h.searchPill.x, h.searchPill.y, h.searchPill.w, h.searchPill.h);
  img("assets/native-ui/top-scan.png", h.topScan.x, h.topScan.y, h.topScan.w, h.topScan.h);
  rect(h.walletChip.x, h.walletChip.y, h.walletChip.w, h.walletChip.h, "#2d2e31", h.walletChip.h / 2);
  text("25K ›", h.walletText.x, h.walletText.y, h.walletText.size, TEXT, "center", 700);
  imgTint("assets/native-ui/copy-icon.svg", h.copyIcon.x, h.copyIcon.y, h.copyIcon.size, h.copyIcon.size, MUTED);
  text("861,40 $", h.balance.x, h.balance.y, h.balance.size, TEXT, "center", 700, true);
  drawDownTriangle(h.deltaTriangle.x, h.deltaTriangle.y, RED);
  text("0,004894 $ (-0.00%)", h.delta.x, h.delta.y, h.delta.size, RED, "center", 600);

  h.quickActions.forEach((action) => img(action.path, action.x, action.y, action.w, action.h));

  rect(h.promo.x, h.promo.y, h.promo.w, h.promo.h, PANEL, h.promo.r);
  rect(h.promo.x, h.promo.y, 90, 66, "#ffd42a", 0);
  img("assets/native-ui/hyperliquid-promo-art.png", h.promoArt.x, h.promoArt.y, h.promoArt.w, h.promoArt.h);
  text("Hyperliquid live with 200+", h.promoTextX, 1147, 34, TEXT, "left", 600);
  text("markets, 0% markup on fees", h.promoTextX, 1197, 34, TEXT, "left", 600);
  text("Explore now →", h.promoTextX, 1265, 30, GREEN, "left", 500);

  drawHomeTabs(h.tabsY);
  if (state.assetTab === "favorites") {
    drawHomeEmptyAssets(true);
    return;
  }
  if (state.assetTab === "nft") {
    drawHomeNftEmpty();
    return;
  }
  drawAssetRows();
  text("Бессрочные фьючерсы  ›", 44, h.perpsTitleY, 42, TEXT, "left", 600);
  homePerpsCard(44, h.perpsCardsY, "BTC", "$12K Vol", "assets/coins/0.webp");
  homePerpsCard(440, h.perpsCardsY, "ETH", "$193K Vol", "assets/coins/60.webp");
  text("Заработок  ›", 44, h.earnTitleY, 42, TEXT, "left", 600);
  earnCard(44, h.earnCardsY, "ATOM", "30.82% APY");
  earnCard(440, h.earnCardsY, "SOL", "25.43% APY");
}

function drawHomeTabs(y) {
  const tabAsset = state.assetTab === "favorites"
    ? "assets/ui/asset-tabs-favorites.png"
    : state.assetTab === "nft"
      ? "assets/ui/asset-tabs-nft.png"
      : "assets/ui/asset-tabs-crypto.png";
  img(tabAsset, 0, y - 48, 1080, 126);
}

function drawAssetRows() {
  assetRow(1562, "USDT", "Tron", "827,582", "826,24 $", "assets/coins/usdt.png", "assets/coins/195.webp");
  assetRow(1722, "TRX", "Tron", "82,4835", "30,80 $", "assets/coins/195.webp");
  assetRow(1882, "USDT", "TON", "4,356", "4,35 $", "assets/coins/usdt.png", "ton");
}

function assetRow(y, symbol, network, balance, fiat, coin, badge) {
  circleImg(coin, 44, y - 76, 96);
  if (badge === "ton") drawTonBadge(91, y - 33, 50);
  else if (badge) circleImg(badge, 92, y - 31, 44);
  text(symbol, 160, y - 30, 38, TEXT, "left", 700);
  tokenPill(285, y - 63, network);
  text("0,99 $   -0.00%", 160, y + 18, 30, MUTED, "left", 400);
  text(balance, 1030, y - 28, 36, TEXT, "right", 600);
  text(fiat, 1030, y + 22, 29, MUTED, "right", 400);
}

function drawHomeEmptyAssets(favorites) {
  rect(44, 1455, 992, 320, PANEL, 36);
  if (favorites) img("assets/native-ui/watchlist-empty-mask.png", 450, 1466, 180, 180);
  else img("assets/native-ui/empty-art-mask.png", 423, 1450, 234, 171);
  text(favorites ? "Нет избранных токенов" : "Активов пока нет", 540, 1668, 44, TEXT, "center", 600);
  text(favorites ? "Добавьте токены в избранное, чтобы быстро" : "Добавьте токены или получите криптовалюту,", 540, 1723, 31, MUTED, "center", 400);
  text(favorites ? "следить за их балансом и ценой." : "чтобы они появились в списке.", 540, 1764, 31, MUTED, "center", 400);
}

function drawHomeNftEmpty() {
  rect(44, 1455, 992, 320, PANEL, 36);
  img("assets/native-ui/empty-art-mask.png", 423, 1450, 234, 171);
  text("NFT не найдены", 540, 1704, 46, TEXT, "center", 600);
  text("Коллекции появятся после получения.", 540, 1758, 32, MUTED, "center", 400);
}

function homePerpsCard(x, y, symbol, volume, coin) {
  rect(x, y, 360, 255, PANEL, 28);
  circleImg(coin, x + 44, y + 42, 76);
  text(symbol, x + 135, y + 92, 38, TEXT, "left", 600);
  text(`Торгуйте ${symbol} с`, x + 44, y + 150, 27, MUTED, "left", 400);
  text("кредитным плечом", x + 44, y + 188, 27, MUTED, "left", 400);
  text("до 200x", x + 44, y + 226, 27, MUTED, "left", 400);
  text(volume, x + 44, y + 260, 28, MUTED, "left", 400);
}

function earnCard(x, y, symbol, apy) {
  rect(x, y, 360, 290, PANEL, 28);
  if (symbol === "SOL") circleImg("assets/coins/501.webp", x + 44, y + 36, 82);
  else drawAtom(x + 44, y + 36, 82);
  rect(x + 98, y + 82, 54, 34, "#183a2a", 17);
  text("APY", x + 125, y + 106, 18, GREEN, "center", 500);
  text("Заработайте до", x + 44, y + 175, 34, TEXT, "left", 600);
  text(apy, x + 44, y + 222, 34, TEXT, "left", 600);
  text(symbol === "SOL" ? "в Juno" : "в Stargaze", x + 44, y + 266, 30, MUTED, "left", 400);
}

function drawHomeBottomNav(alpha = 1) {
  ctx.save();
  ctx.globalAlpha *= alpha;
  img("assets/ui/bottom-nav-home.png", 0, 2020, 1080, 250);
  img("assets/ui/system-nav-icons.png", 0, 2268, 1080, 132);
  ctx.restore();
}


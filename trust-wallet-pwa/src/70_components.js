function drawBottomNav(active, dimmed = false) {
  const y = NAV_TOP;
  ctx.save();
  if (dimmed) ctx.globalAlpha = 0.45;
  ctx.fillStyle = "rgba(24,25,27,0.92)";
  pathRoundRect(8, y, 1064, 182, 92);
  ctx.fill();
  strokeRect(8, y, 1064, 182, "#3a3b3e", 92, 3);
  navItem(150, "home", "Главная", active === "home");
  navItem(378, "popular", "Рынки", active === "popular");
  navItem(728, "rewards", "Награды", active === "rewards");
  navItem(925, "more", "Подробнее", active === "more");
  rect(470, y - 50, 140, 140, GREEN, 70);
  img("assets/native-ui/nav-trade-icon.png", 506, y - 12, 68, 69);
  text("Торговать", 540, y + 148, 26, TEXT, "center", 500);
  ctx.restore();
}

function navItem(cx, type, label, active) {
  const y = NAV_TOP;
  if (active) rect(cx - 104, y + 10, 208, 152, "#1c4b2e", 76);
  const color = active ? GREEN : MUTED;
  if (type === "home") imgTint("assets/native-ui/nav-main-icon.png", cx - 28, y + 30, 56, 45, color);
  else if (type === "popular") imgTint("assets/native-ui/nav-popular-icon.png", cx - 29, y + 28, 58, 51, color);
  else if (type === "more") imgTint("assets/native-ui/nav-more-icon.png", cx - 27, y + 29, 54, 48, color);
  else if (active) img("assets/native-ui/nav-rewards-active-icon.png", cx - 37, y + 22, 74, 74);
  else imgTint("assets/native-ui/nav-rewards-active-icon.png", cx - 37, y + 22, 74, 74, color);
  text(label, cx, y + 138, label.length > 8 ? 23 : 25, color, "center", 500);
}
function imgTint(path, x, y, w, h, color) {
  const image = assets[path];
  if (!image) return;
  const key = `${path}|${Math.round(w)}x${Math.round(h)}|${color}`;
  let mask = tintedAssets[key];
  if (!mask) {
    mask = document.createElement("canvas");
    mask.width = Math.max(1, Math.round(w));
    mask.height = Math.max(1, Math.round(h));
    const maskCtx = mask.getContext("2d");
    maskCtx.drawImage(image, 0, 0, mask.width, mask.height);
    maskCtx.globalCompositeOperation = "source-in";
    maskCtx.fillStyle = color;
    maskCtx.fillRect(0, 0, mask.width, mask.height);
    tintedAssets[key] = mask;
  }
  ctx.save();
  ctx.drawImage(mask, x, y, w, h);
  ctx.restore();
}

function imgTintRotated(path, cx, cy, w, h, color, angle) {
  const image = assets[path];
  if (!image) return;
  const key = `${path}|${Math.round(w)}x${Math.round(h)}|${color}`;
  let mask = tintedAssets[key];
  if (!mask) {
    mask = document.createElement("canvas");
    mask.width = Math.max(1, Math.round(w));
    mask.height = Math.max(1, Math.round(h));
    const maskCtx = mask.getContext("2d");
    maskCtx.drawImage(image, 0, 0, mask.width, mask.height);
    maskCtx.globalCompositeOperation = "source-in";
    maskCtx.fillStyle = color;
    maskCtx.fillRect(0, 0, mask.width, mask.height);
    tintedAssets[key] = mask;
  }
  ctx.save();
  ctx.translate(cx, cy);
  ctx.rotate(angle);
  ctx.drawImage(mask, -w / 2, -h / 2, w, h);
  ctx.restore();
}

function dim() {
  rect(0, 0, BASE_W, BASE_H, "rgba(0,0,0,0.70)", 0);
}

function drawScrollIndicator() {
  if (Date.now() > scrollIndicatorUntil) return;
  const h = 665;
  rect(1063, 175, 6, h, "rgba(220,220,226,0.72)", 3);
}

function drawTokenComposite(cx, cy, symbol, network) {
  const coin = coinPath(symbol);
  circleImg(coin, cx - 52, cy - 52, 104);
  if (network === "BNB Smart Chain") circleImg("assets/coins/714.webp", cx + 23, cy + 20, 42);
  else if (network === "TON") drawTonBadge(cx + 23, cy + 20, 42);
  else if (network === "Ethereum") circleImg("assets/coins/60.webp", cx + 23, cy + 20, 42);
  else if (network === "Tron") circleImg("assets/coins/195.webp", cx + 23, cy + 20, 42);
}

function coinPath(symbol) {
  if (symbol === "TRX") return "assets/coins/195.webp";
  if (symbol === "BTC") return "assets/coins/0.webp";
  if (symbol === "ETH") return "assets/coins/60.webp";
  if (symbol === "SOL") return "assets/coins/501.webp";
  if (symbol === "BNB") return "assets/coins/714.webp";
  if (symbol === "USDC") return "assets/coins/usdc.png";
  return "assets/coins/usdt.png";
}

function tokenPill(x, y, label) {
  const w = Math.max(88, label.length * 14 + 36);
  rect(x, y, w, 48, "#333438", 24);
  text(label, x + w / 2, y + 32, 24, "#c8c8ce", "center", 500);
}

function drawNetworkIcon(cx, cy, network, size) {
  if (network === "Ethereum") circleImg("assets/coins/60.webp", cx - size / 2, cy - size / 2, size);
  else if (network === "Tron") circleImg("assets/coins/195.webp", cx - size / 2, cy - size / 2, size);
  else if (network === "TON") drawTonBadge(cx - size / 2, cy - size / 2, size);
  else circleImg("assets/coins/714.webp", cx - size / 2, cy - size / 2, size);
}

function drawTonBadge(x, y, size) {
  img("assets/native-ui/ton-badge.png", x, y, size, size);
}

function drawAtom(x, y, size) {
  rect(x, y, size, size, "#050505", size / 2);
  line(x + 18, y + 64, x + 64, y + 18, TEXT, 7);
  ctx.strokeStyle = TEXT;
  ctx.lineWidth = 5;
  ctx.beginPath();
  ctx.arc(x + size / 2, y + size / 2, 23, 0, Math.PI * 2);
  ctx.stroke();
}

function drawQrCard(x, y, size) {
  const extra = 124;
  const canonicalAddress = "0xa61e05Ea7Aa2fD107cecb291F6FF0f75F74B0C99";
  if (state.receiveNetwork === "BNB Smart Chain" && state.receiveAddress === canonicalAddress) {
    img("assets/native-ui/receive-qr-card-reference.png", x, y, size, Math.round(size * 411 / 346));
    return;
  }
  rect(x, y, size, size + extra, "#f4f4f7", 28);
  const cells = 26;
  const module = Math.floor((size - 88) / cells);
  const qrSize = module * cells;
  const qrX = x + Math.round((size - qrSize) / 2);
  const qrY = y + 42;
  rect(qrX, qrY, module * cells, module * cells, "#ffffff", 0);
  drawFinder(qrX, qrY, module);
  drawFinder(qrX + module * 19, qrY, module);
  drawFinder(qrX, qrY + module * 19, module);
  for (let row = 0; row < cells; row++) {
    for (let col = 0; col < cells; col++) {
      const inFinder = (col < 7 && row < 7) || (col > 18 && row < 7) || (col < 7 && row > 18);
      if (inFinder) continue;
      if (((row * 13 + col * 7 + row * col) % 5) < 2) {
        rect(qrX + col * module + 3, qrY + row * module + 3, module - 6, module - 6, "#171718", 4);
      }
    }
  }
  line(x + 72, y + size + 42, x + size - 72, y + size + 42, "#d6d6dc", 2);
  const address = state.receiveAddress || "";
  const topLine = address.length > 28 ? address.slice(0, 28) : address;
  const bottomLine = address.length > 28 ? address.slice(28) : "";
  text(topLine, x + size / 2, y + size + 82, 31, "#171718", "center", 600);
  if (bottomLine) text(bottomLine, x + size / 2, y + size + 120, 31, "#171718", "center", 600);
}

function drawFinder(x, y, module) {
  rect(x, y, module * 7, module * 7, "#171718", 10);
  rect(x + module, y + module, module * 5, module * 5, "#ffffff", 8);
  rect(x + module * 2, y + module * 2, module * 3, module * 3, "#171718", 6);
}

function receiveAction(cx, y, type, label) {
  rect(cx - 62, y - 78, 124, 124, "#242529", 32);
  if (type === 0) imgTint("assets/native-ui/copy-icon.svg", cx - 24, y - 40, 48, 48, TEXT);
  else if (type === 1) imgTint("assets/native-ui/upload-icon.svg", cx - 25, y - 40, 50, 50, TEXT);
  else imgTint("assets/native-ui/share-icon.svg", cx - 25, y - 40, 50, 50, TEXT);
  text(label, cx, y + 92, 30, TEXT, "center", 600);
}

function iconClose(cx, cy, color = MUTED, size = 48) {
  imgTint("assets/native-ui/close-icon.svg", cx - size / 2, cy - size / 2, size, size, color);
}

function iconBack(cx, cy, color = MUTED, size = 56) {
  imgTint("assets/native-ui/back-icon.svg", cx - size / 2, cy - size / 2, size, size, color);
}

function drawSearch(cx, cy, color, s) {
  const size = 86 * s;
  imgTint("assets/native-ui/search-icon-mask.png", cx - size * 0.58, cy - size * 0.58, size, size, color);
}

function drawCaret(x, y, color) {
  imgTint("assets/native-ui/arrow-down-icon.svg", x - 3, y - 7, 42, 42, color);
}

function drawDownTriangle(x, y, color) {
  imgTint("assets/native-ui/arrow-down-icon.svg", x - 18, y - 18, 36, 36, color);
}

function drawTxArrow(cx, cy, up) {
  imgTintRotated("assets/native-ui/arrow-right-icon.svg", cx, cy, 62, 62, MUTED, up ? -Math.PI / 2 : Math.PI / 2);
}

function drawFeatureIcon(cx, cy, type) {
  rect(cx - 44, cy - 44, 88, 88, type === 0 ? "#204c3a" : "#342652", 28);
  if (type === 0) {
    imgTint("assets/native-ui/markets-predictions-icon.png", cx - 31, cy - 36, 62, 69, GREEN);
  } else {
    circleImg("assets/coins/pepe.png", cx - 33, cy - 33, 50);
    circleImg("assets/coins/3.webp", cx - 2, cy - 24, 48);
    imgTint("assets/native-ui/markets-meme-rush-icon.png", cx - 4, cy - 4, 42, 39, "#ffbe46");
  }
}

function spark(x, y, width, color) {
  ctx.strokeStyle = color;
  ctx.lineWidth = 7;
  ctx.lineCap = "round";
  ctx.beginPath();
  const pts = [0.65, 0.75, 0.3, 0.15, 0.22, 0.1, 0.36, 0.42];
  pts.forEach((p, i) => {
    const px = x + (width * i) / (pts.length - 1);
    const py = y + p * 70;
    if (i === 0) ctx.moveTo(px, py);
    else ctx.lineTo(px, py);
  });
  ctx.stroke();
}

function drawRewardsHero() {
  img("assets/native-ui/rewards-hero-xp-art.png", 320, 330, 440, 320);
}


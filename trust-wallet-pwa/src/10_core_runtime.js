function loadAssets() {
  return Promise.all(assetPaths.map((path) => new Promise((resolve) => {
    const img = new Image();
    img.onload = () => {
      assets[path] = img;
      draw();
      resolve();
    };
    img.onerror = () => {
      resolve();
      draw();
    };
    img.src = path;
  })));
}

function resize() {
  const dpr = window.devicePixelRatio || 1;
  const viewport = viewportSize();
  canvas.width = Math.round(viewport.width * dpr);
  canvas.height = Math.round(viewport.height * dpr);
  canvas.style.width = `${viewport.width}px`;
  canvas.style.height = `${viewport.height}px`;
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  scale = viewport.width / BASE_W;
  BASE_H = viewport.height / scale;
  NAV_TOP = BASE_H - 222;
  ox = (viewport.width - BASE_W * scale) / 2;
  oy = 0;
  draw();
}

function viewportSize() {
  const vv = window.visualViewport;
  return {
    width: vv ? vv.width : window.innerWidth,
    height: vv ? vv.height : window.innerHeight,
  };
}

function toBase(evt) {
  const t = evt.touches ? evt.touches[0] : evt;
  return {
    x: (t.clientX - ox) / scale,
    y: (t.clientY - oy) / scale,
  };
}

function font(size, weight = 400, display = false) {
  return `${weight} ${size}px "${display ? "SF Pro Display Local" : "SF Pro Text Local"}", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif`;
}

function text(value, x, y, size, color = TEXT, align = "left", weight = 400, display = false) {
  ctx.fillStyle = color;
  ctx.font = font(size, weight, display);
  ctx.textAlign = align;
  ctx.textBaseline = "alphabetic";
  ctx.fillText(value, x, y);
}

function rect(x, y, w, h, color, r = 0) {
  ctx.fillStyle = color;
  if (r <= 0) {
    ctx.fillRect(x, y, w, h);
    return;
  }
  ctx.beginPath();
  if (ctx.roundRect) ctx.roundRect(x, y, w, h, r);
  else pathRoundRect(x, y, w, h, r);
  ctx.fill();
}

function strokeRect(x, y, w, h, color, r = 0, lw = 2, dash = null) {
  ctx.strokeStyle = color;
  ctx.lineWidth = lw;
  ctx.setLineDash(dash || []);
  ctx.beginPath();
  if (r <= 0) ctx.rect(x, y, w, h);
  else if (ctx.roundRect) ctx.roundRect(x, y, w, h, r);
  else pathRoundRect(x, y, w, h, r);
  ctx.stroke();
  ctx.setLineDash([]);
}

function pathRoundRect(x, y, w, h, r) {
  const rr = Math.min(r, w / 2, h / 2);
  ctx.beginPath();
  ctx.moveTo(x + rr, y);
  ctx.lineTo(x + w - rr, y);
  ctx.quadraticCurveTo(x + w, y, x + w, y + rr);
  ctx.lineTo(x + w, y + h - rr);
  ctx.quadraticCurveTo(x + w, y + h, x + w - rr, y + h);
  ctx.lineTo(x + rr, y + h);
  ctx.quadraticCurveTo(x, y + h, x, y + h - rr);
  ctx.lineTo(x, y + rr);
  ctx.quadraticCurveTo(x, y, x + rr, y);
  ctx.closePath();
}

function img(path, x, y, w, h, alpha = 1) {
  const image = assets[path];
  if (!image) return;
  ctx.save();
  ctx.globalAlpha = alpha;
  ctx.drawImage(image, x, y, w, h);
  ctx.restore();
}

function circleImg(path, x, y, size) {
  const image = assets[path];
  ctx.save();
  ctx.beginPath();
  ctx.arc(x + size / 2, y + size / 2, size / 2, 0, Math.PI * 2);
  ctx.clip();
  if (image) ctx.drawImage(image, x, y, size, size);
  else rect(x, y, size, size, "#333", size / 2);
  ctx.restore();
}

function line(x1, y1, x2, y2, color = TEXT, width = 6) {
  ctx.strokeStyle = color;
  ctx.lineWidth = width;
  ctx.lineCap = "round";
  ctx.lineJoin = "round";
  ctx.beginPath();
  ctx.moveTo(x1, y1);
  ctx.lineTo(x2, y2);
  ctx.stroke();
}

function money(v) {
  return v.toLocaleString("ru-RU", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function amount(v, max = 6) {
  return v.toLocaleString("ru-RU", { maximumFractionDigits: max }).replace(/\u00a0/g, " ");
}

function ellipsizeMiddle(value, max) {
  if (!value) return "";
  if (value.length <= max) return value;
  const left = Math.max(4, Math.floor((max - 3) / 2));
  const right = Math.max(4, max - 3 - left);
  return `${value.slice(0, left)}...${value.slice(-right)}`;
}

function ellipsizeAddress(value) {
  return ellipsizeMiddle(value, 13);
}

function feeSymbol(network = state.sendNetwork) {
  if (network === "Ethereum") return "ETH";
  if (network === "Tron") return "TRX";
  if (network === "TON") return "TON";
  return "BNB";
}

function feeAmount(network = state.sendNetwork, value = state.sendAmount) {
  const safe = Math.max(0.000001, value);
  if (network === "Ethereum") return 0.0012 + safe * 0.00001;
  if (network === "Tron") return 1.1 + safe * 0.001;
  if (network === "TON") return 0.045 + safe * 0.0004;
  return 0.00021 + safe * 0.000002;
}

function feeFiat(network = state.sendNetwork, value = state.sendAmount) {
  const f = feeAmount(network, value);
  if (network === "Ethereum") return f * 2031;
  if (network === "Tron") return f * state.balances.trxPrice;
  if (network === "TON") return f * 2.95;
  return f * 739;
}

function sendAssetDisplay() {
  return state.sendAsset === "USDT_TON" ? "USDT" : state.sendAsset;
}

function sendAssetPrice() {
  if (state.sendAsset === "TRX") return state.balances.trxPrice;
  if (state.sendAsset === "USDT_TON") return state.balances.tonUsdtPrice;
  return state.balances.usdtPrice;
}

function sendBalance() {
  if (state.sendAsset === "TRX") return state.balances.trx;
  if (state.sendAsset === "USDT_TON") return state.balances.usdtTon;
  return state.balances.usdtTron;
}

function setSendAsset(symbol) {
  state.sendAsset = symbol;
  state.sendNetwork = symbol === "TRX" ? "Tron" : symbol === "USDT_TON" ? "TON" : "BNB Smart Chain";
  state.sendAmount = Math.min(Math.max(0.0001, state.sendAmount), Math.max(0.0001, sendBalance()));
}

function setReceiveAsset(symbol, network, address) {
  state.receiveAsset = symbol;
  state.receiveNetwork = network;
  state.receiveAddress = address;
}


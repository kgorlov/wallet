const CACHE_NAME = "trust-visual-web-v29";
const ASSETS = [
  "./",
  "index.html",
  "styles.css",
  "app.js",
  "dist/app.bundle.js",
  "manifest.webmanifest",
  "assets/icon.png",
  "assets/native-ui/top-settings.png",
  "assets/native-ui/top-search-pill.png",
  "assets/native-ui/top-scan.png",
  "assets/native-ui/wallet-chip.png",
  "assets/native-ui/copy-button.png",
  "assets/native-ui/hyperliquid-promo-art.png",
  "assets/native-ui/qa-send.png",
  "assets/native-ui/qa-receive.png",
  "assets/native-ui/qa-swap-active.png",
  "assets/native-ui/qa-swap-icon.png",
  "assets/native-ui/qa-buy.png",
  "assets/native-ui/nav-main-icon.png",
  "assets/native-ui/nav-popular-icon.png",
  "assets/native-ui/nav-trade-icon.png",
  "assets/native-ui/nav-more-icon.png",
  "assets/coins/usdt.png",
  "assets/coins/195.webp",
  "assets/coins/714.webp",
  "assets/coins/60.webp",
  "assets/coins/0.webp",
  "assets/coins/501.webp",
  "assets/coins/3.webp",
  "assets/coins/59144.webp",
  "assets/coins/pepe.png",
  "assets/coins/usdc.png",
  "assets/fonts/SF-Pro-Display-Bold.otf",
  "assets/fonts/SF-Pro-Display-Semibold.otf",
  "assets/fonts/SF-Pro-Text-Medium.otf",
  "assets/fonts/SF-Pro-Text-Regular.otf",
  "assets/fonts/SF-Pro-Text-Semibold.otf"
];

self.addEventListener("install", (event) => {
  event.waitUntil(caches.open(CACHE_NAME).then((cache) => cache.addAll(ASSETS)));
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((keys) => Promise.all(keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))))
  );
  self.clients.claim();
});

self.addEventListener("fetch", (event) => {
  event.respondWith(
    fetch(event.request)
      .then((response) => {
        const clone = response.clone();
        caches.open(CACHE_NAME).then((cache) => cache.put(event.request, clone));
        return response;
      })
      .catch(() => caches.match(event.request))
  );
});

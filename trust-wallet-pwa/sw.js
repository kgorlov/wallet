const CACHE_NAME = "trust-visual-web-v35";
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
  "assets/native-ui/arrow-right-icon.svg",
  "assets/native-ui/arrow-down-icon.svg",
  "assets/native-ui/search-icon-mask.png",
  "assets/native-ui/watchlist-empty-mask.png",
  "assets/native-ui/empty-art-mask.png",
  "assets/native-ui/android-nav-icons-mask.png",
  "assets/native-ui/markets-predictions-icon.png",
  "assets/native-ui/markets-meme-rush-icon.png",
  "assets/native-ui/ton-badge.png",
  "assets/native-ui/discover-sprout-icon.png",
  "assets/native-ui/discover-globe-icon.png",
  "assets/native-ui/discover-support-icon.png",
  "assets/native-ui/discover-question-icon.png",
  "assets/native-ui/nav-perps-discover-icon.png",
  "assets/native-ui/history-empty-dash.png",
  "assets/native-ui/history-back-icon.png",
  "assets/native-ui/rewards-hero-xp-art.png",
  "assets/native-ui/rewards-campaign-icon.png",
  "assets/native-ui/rewards-x-icon.png",
  "assets/native-ui/rewards-arrow-button.png",
  "assets/native-ui/nav-rewards-active-icon.png",
  "assets/native-ui/rewards-gesture-bar.png",
  "assets/native-ui/rewards-past-card-tunz.png",
  "assets/native-ui/rewards-past-card-umy.png",
  "assets/native-ui/rewards-past-card-third.png",
  "assets/native-ui/rewards-past-item-tunz-bottom.png",
  "assets/native-ui/rewards-past-item-umy-bottom.png",
  "assets/native-ui/rewards-past-item-third-bottom.png",
  "assets/native-ui/processing-success-art.png",
  "assets/native-ui/processing-gesture-bar.png",
  "assets/native-ui/network-selected-check.png",
  "assets/native-ui/network-ton-icon.png",
  "assets/native-ui/receive-qr-card-reference.png",
  "assets/native-ui/reward-detail-xp-card.png",
  "assets/ui/chevron-right.png",
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
  "assets/native-ui/trade-row-swap-icon.png",
  "assets/native-ui/trade-row-perps-icon.png",
  "assets/native-ui/trade-row-predictions-icon.png",
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

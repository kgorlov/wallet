const BASE_W = 1080;
let BASE_H = 2400;
let NAV_TOP = 2178;
const GREEN = "#30e88a";
const BG = "#171718";
const PANEL = "#242529";
const PANEL2 = "#202124";
const MUTED = "#aaaab2";
const TEXT = "#f4f4f7";
const RED = "#ff5c6b";

const HOME_SCREEN = {
  topSettings: { x: 74, y: 166, w: 62, h: 64 },
  notificationDot: { x: 124, y: 162, size: 16 },
  searchPill: { x: 180, y: 150, w: 716, h: 108 },
  topScan: { x: 942, y: 168, w: 62, h: 64 },
  walletChip: { x: 450, y: 365, w: 178, h: 82 },
  walletText: { x: 540, y: 416, size: 35 },
  copyIcon: { x: 669, y: 384, size: 42 },
  balance: { x: 540, y: 650, size: 74 },
  deltaTriangle: { x: 320, y: 700 },
  delta: { x: 540, y: 720, size: 32 },
  quickActions: [
    { path: "assets/native-ui/qa-send.png", x: 116, y: 780, w: 198, h: 214 },
    { path: "assets/native-ui/qa-receive.png", x: 331, y: 780, w: 198, h: 214 },
    { path: "assets/native-ui/qa-swap-active.png", x: 546, y: 780, w: 198, h: 214 },
    { path: "assets/native-ui/qa-buy.png", x: 761, y: 780, w: 198, h: 214 },
  ],
  promo: { x: 44, y: 1066, w: 992, h: 270, r: 34 },
  promoArt: { x: 70, y: 1092, w: 220, h: 230 },
  promoTextX: 295,
  tabsY: 1392,
  perpsTitleY: 2110,
  perpsCardsY: 2165,
  earnTitleY: 2570,
  earnCardsY: 2625,
};

const canvas = document.getElementById("wallet");
const ctx = canvas.getContext("2d");
const editor = document.getElementById("editor");
const editorLabel = document.getElementById("editorLabel");
const editorInput = document.getElementById("editorInput");
const editorCancel = document.getElementById("editorCancel");
const editorApply = document.getElementById("editorApply");

const assets = {};
const tintedAssets = {};
const assetPaths = [
  "assets/status-bar.png",
  "assets/native-ui/discover-status-bar.png",
  "assets/native-ui/discover-android-nav.png",
  "assets/native-ui/discover-search-icon-crop.png",
  "assets/native-ui/top-settings.png",
  "assets/native-ui/settings-icon.svg",
  "assets/native-ui/settings-email.png",
  "assets/native-ui/settings-terms.png",
  "assets/native-ui/settings-backup.png",
  "assets/native-ui/email-icon.svg",
  "assets/native-ui/info-icon.svg",
  "assets/native-ui/close-icon.svg",
  "assets/native-ui/back-icon.svg",
  "assets/native-ui/clock-icon.svg",
  "assets/native-ui/arrow-right-icon.svg",
  "assets/native-ui/arrow-down-icon.svg",
  "assets/native-ui/search-icon-mask.png",
  "assets/ui/chevron-right.png",
  "assets/native-ui/top-search-pill.png",
  "assets/native-ui/top-scan.png",
  "assets/native-ui/wallet-chip.png",
  "assets/native-ui/copy-button.png",
  "assets/native-ui/copy-icon.svg",
  "assets/native-ui/qr-code-icon.png",
  "assets/native-ui/share-icon.svg",
  "assets/native-ui/upload-icon.svg",
  "assets/native-ui/hyperliquid-promo-art.png",
  "assets/native-ui/hyperliquid-provider-icon.png",
  "assets/native-ui/reward-shield-bronze.png",
  "assets/native-ui/reward-shield-silver.png",
  "assets/native-ui/reward-shield-gold.png",
  "assets/native-ui/reward-tier-bronze.png",
  "assets/native-ui/trust-alpha-vip-dark.png",
  "assets/native-ui/qa-send.png",
  "assets/native-ui/qa-receive.png",
  "assets/native-ui/qa-swap-active.png",
  "assets/native-ui/qa-swap-icon.png",
  "assets/native-ui/qa-buy.png",
  "assets/native-ui/ic_splash.png",
  "assets/native-ui/onboarding_lock.png",
  "assets/native-ui/onboarding_erc20.png",
  "assets/native-ui/onboarding_open_source.png",
  "assets/native-ui/onboarding_rocket.png",
  "assets/native-ui/nav-main-icon.png",
  "assets/native-ui/nav-popular-icon.png",
  "assets/native-ui/nav-trade-icon.png",
  "assets/native-ui/nav-more-icon.png",
  "assets/ui/bottom-nav-home.png",
  "assets/ui/system-nav-icons.png",
  "assets/native-ui/trade-row-swap-icon.png",
  "assets/native-ui/trade-row-perps-icon.png",
  "assets/native-ui/trade-row-predictions-icon.png",
  "assets/ui/asset-tabs-crypto.png",
  "assets/ui/asset-tabs-favorites.png",
  "assets/ui/asset-tabs-nft.png",
  "assets/native-ui/tab-history-mask.png",
  "assets/native-ui/tab-layout-mask.png",
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
  "assets/ui/bottom-nav-discover.png",
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
  "assets/token_logo.png",
  "assets/coins/usdt.png",
  "assets/coins/195.webp",
  "assets/coins/714.webp",
  "assets/coins/60.webp",
  "assets/coins/0.webp",
  "assets/coins/501.webp",
  "assets/coins/3.webp",
  "assets/coins/59144.webp",
  "assets/coins/link.png",
  "assets/coins/10000787.webp",
  "assets/coins/118.webp",
  "assets/coins/133.webp",
  "assets/coins/144.webp",
  "assets/coins/pepe.png",
  "assets/coins/usdc.png",
];

const state = {
  view: "home",
  sheet: null,
  scrollY: 0,
  assetTab: "crypto",
  rewardsTab: "active",
  previousView: "home",
  selectedMarket: "BTC",
  page: 0,
  onboardingPage: 0,
  sendAsset: "USDT",
  sendNetwork: "BNB Smart Chain",
  sendAddress: "0x97b6e11220fbf",
  sendAmount: 1,
  receiveAsset: "USDT",
  receiveNetwork: "BNB Smart Chain",
  receiveAddress: "0xa61e0...74B0C99",
  swapToken: null,
  swapReversed: false,
  manageLayout: 0,
  hideSmallAssets: false,
  hideNft: false,
  hidePredictions: false,
  hidePerps: false,
  txs: [],
  selectedTx: null,
  balances: {
    usdtTron: 827.582,
    trx: 82.4835,
    usdtTon: 4.356,
    usdtPrice: 0.99,
    trxPrice: 0.3734,
    tonUsdtPrice: 0.9985,
  },
};

const marketAssets = {
  USDT: { name: "Tether", price: "$0.9985", change: "-0.03%", color: RED, icon: "assets/coins/usdt.png", stats: ["$142.1B", "$55.3B", "148.2B USDT"] },
  BTC: { name: "Bitcoin", price: "$73,964", change: "+0.39%", color: GREEN, icon: "assets/coins/0.webp", stats: ["$1.46T", "$38.8B", "19.8M BTC"] },
  ETH: { name: "Ethereum", price: "$2,031", change: "+0.64%", color: GREEN, icon: "assets/coins/60.webp", stats: ["$244.6B", "$18.7B", "120.7M ETH"] },
  BNB: { name: "BNB", price: "$739.26", change: "+12.48%", color: GREEN, icon: "assets/coins/714.webp", stats: ["$102.8B", "$3.4B", "139.3M BNB"] },
  XRP: { name: "XRP", price: "$1.34", change: "-1.17%", color: RED, icon: null, stats: ["$76.4B", "$2.1B", "57.1B XRP"] },
  USDC: { name: "USDC", price: "$0.9996", change: "-0.01%", color: RED, icon: "assets/coins/usdc.png", stats: ["$60.2B", "$8.4B", "60.3B USDC"] },
  SOL: { name: "Solana", price: "$83.02", change: "+0.25%", color: GREEN, icon: "assets/coins/501.webp", stats: ["$45.9B", "$2.9B", "552.7M SOL"] },
};

let scale = 1;
let ox = 0;
let oy = 0;
let lastY = 0;
let dragging = false;
let moved = false;
let editorMode = null;
let scrollIndicatorUntil = 0;


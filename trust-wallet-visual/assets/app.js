const BASE_WIDTH = 1080;
const BASE_HEIGHT = 2400;

const screen = document.querySelector(".wallet-screen");
const scrollPane = document.querySelector(".main-scroll");
const assetTabs = Array.from(document.querySelectorAll(".asset-tab[data-tab]"));
const navHome = document.querySelector(".nav-home");
const navMarkets = document.querySelector(".nav-markets");
const navSwap = document.querySelector(".nav-swap");
const navPerps = document.querySelector(".nav-perps");
const navDiscover = document.querySelector(".nav-discover");
const historyTab = document.querySelector(".history-tab");
const manageTab = document.querySelector(".layout-tab");
const dynamicTitle = document.querySelector(".dynamic-title");
const dynamicContent = document.querySelector(".dynamic-content");
const dynamicBack = document.querySelector(".dynamic-back");
const sheet = document.querySelector(".action-sheet");
const sheetTitle = document.querySelector(".sheet-title");
const sheetContent = document.querySelector(".sheet-content");
const sheetBackdrop = document.querySelector(".sheet-backdrop");
const sheetClose = document.querySelector(".sheet-close");
const toast = document.querySelector(".toast");

const PAGE_CLASSES = ["is-perps", "is-markets", "is-swap", "is-discover", "is-history", "is-manage", "is-dynamic"];
const FULL_PAGE_CLASSES = ["is-swap", "is-history", "is-manage"];
let currentView = "home";
let previousView = "home";
let toastTimer = 0;

const navTargets = {
  HomeNavigationButton: "home",
  TrendingTokenNavigationButton: "markets",
  PerpsNavigationButton: "perps",
  DiscoverNavigationButton: "discover",
};

function pageElementFor(name) {
  if (name === "perps") return document.querySelector(".perps-page");
  return document.querySelector(`.${name}-page`);
}

function isNavLike(item) {
  return item.rid in navTargets || item.text === "Обмен" || item.desc === "Обмен";
}

function parsedLabel(item) {
  return item.text || item.desc || item.rid || "";
}

function parsedClass(item, page) {
  const label = parsedLabel(item);
  const classes = ["parsed-node", `parsed-${item.role}`];
  if (item.clickable) classes.push("is-clickable");
  if (item.scrollable) classes.push("is-scrollable");
  if (item.selected) classes.push("is-selected");
  if (isNavLike(item)) classes.push("parsed-nav-hit");
  if (item.rid) classes.push(`rid-${item.rid.replace(/[^a-zA-Z0-9_-]/g, "-")}`);
  if (label === pageTitles[page]) classes.push("parsed-title");
  if (label === "Predictions" || label === "Meme Rush") classes.push("parsed-large-pill");
  if (label === "Fund" || label === "Select token" || label === "Депозит" || label === "Deposit") classes.push("parsed-green-action");
  if (/^(Сеть|24h|Hot tokens|Top Gainers|RWA|Meme|DeFi|Popular|New|Crypto|Stocks|Все поставщики)$/.test(label)) classes.push("parsed-filter");
  if (/^(BTC|ETH|SOL|LINK|CAKE|ASTER|XAUt|PAXG|HYPE|CL|ZEC)$/.test(label)) classes.push("parsed-symbol");
  if (/^\$|^\+|^-/.test(label)) classes.push("parsed-value");
  return classes.join(" ");
}

const pageTitles = {
  markets: "Markets",
  swap: "Своп",
  discover: "Подробнее",
  history: "История транзакций",
  perps: "Бесср.",
};

function renderParsedPages() {
  const pages = window.PARSED_PAGES || {};
  Object.entries(pages).forEach(([page, items]) => {
    const host = pageElementFor(page);
    if (!host) return;
    host.querySelectorAll(".parsed-node").forEach((node) => node.remove());
    items.forEach((item) => {
      if (page === "nft") return;
      if (item.rid === "action_bar_root" || item.rid === "content") return;
      const node = document.createElement(item.clickable ? "button" : "div");
      node.className = parsedClass(item, page);
      node.style.left = `calc(${item.x} * var(--px))`;
      node.style.top = `calc(${item.y} * var(--px))`;
      node.style.width = `calc(${item.w} * var(--px))`;
      node.style.height = `calc(${item.h} * var(--px))`;
      const baseFont = Math.max(14, Math.min(48, Math.floor(item.h * 0.58)));
      node.style.setProperty("--parsed-fs", `calc(${baseFont} * var(--px))`);
      node.dataset.rid = item.rid || "";
      node.dataset.label = parsedLabel(item);
      node.dataset.page = page;
      const label = parsedLabel(item);
      if (label && item.role !== "shape") {
        const span = document.createElement("span");
        span.textContent = label;
        node.appendChild(span);
      }
      host.appendChild(node);
    });
  });
}

renderParsedPages();

function resizeScreen() {
  const px = Math.min(window.innerWidth / BASE_WIDTH, window.innerHeight / BASE_HEIGHT);
  document.documentElement.style.setProperty("--px", `${px}px`);
}

function updateChrome() {
  const y = scrollPane ? scrollPane.scrollTop / Number.parseFloat(getComputedStyle(document.documentElement).getPropertyValue("--px")) : 0;
  screen.classList.toggle("is-collapsed", y > 420);
}

window.addEventListener("resize", () => {
  resizeScreen();
  updateChrome();
});

resizeScreen();

function setMainView(view, resetScroll = false) {
  const normalizedView = ["home", "perps", "markets", "swap", "discover", "history", "manage"].includes(view) ? view : "home";
  previousView = currentView === "dynamic" ? previousView : currentView;
  currentView = normalizedView;

  PAGE_CLASSES.forEach((className) => screen.classList.remove(className));
  screen.classList.remove("is-sheet");
  if (normalizedView !== "home") {
    screen.classList.add(`is-${normalizedView}`);
  }

  screen.classList.toggle("is-page", normalizedView !== "home");
  screen.classList.toggle("is-full-page", FULL_PAGE_CLASSES.includes(`is-${normalizedView}`));

  navHome?.classList.toggle("selected", normalizedView === "home");
  navMarkets?.classList.toggle("selected", normalizedView === "markets");
  navPerps?.classList.toggle("selected", normalizedView === "perps");
  navDiscover?.classList.toggle("selected", normalizedView === "discover");

  if (resetScroll && scrollPane) {
    scrollPane.scrollTop = 0;
  }

  updateChrome();
}

function openDynamicPage(type, fromView = currentView) {
  previousView = fromView && fromView !== "dynamic" ? fromView : "home";
  currentView = "dynamic";
  PAGE_CLASSES.forEach((className) => screen.classList.remove(className));
  screen.classList.remove("is-sheet");
  screen.classList.add("is-dynamic", "is-page", "is-full-page");

  const page = dynamicPages[type] || dynamicPages.info;
  dynamicTitle.textContent = page.title;
  dynamicContent.innerHTML = page.content;
  updateChrome();
}

function closeDynamicPage() {
  setMainView(previousView || "home", false);
}

function openSheet(type) {
  const nextSheet = sheets[type] || sheets.info;
  sheetTitle.textContent = nextSheet.title;
  sheetContent.innerHTML = nextSheet.content;
  screen.classList.add("is-sheet");
}

function closeSheet() {
  screen.classList.remove("is-sheet");
}

function showToast(message) {
  if (!toast) return;
  toast.textContent = message;
  toast.classList.add("is-visible");
  window.clearTimeout(toastTimer);
  toastTimer = window.setTimeout(() => toast.classList.remove("is-visible"), 1550);
}

const tokenRows = `
  <div class="dyn-row"><span class="dyn-icon">₿</span><span>Bitcoin<small>BTC · Bitcoin</small></span></div>
  <div class="dyn-row"><span class="dyn-icon">Ξ</span><span>Ethereum<small>ETH · Ethereum</small></span></div>
  <div class="dyn-row"><span class="dyn-icon">◎</span><span>Solana<small>SOL · Solana</small></span></div>
  <div class="dyn-row"><span class="dyn-icon">◆</span><span>BNB<small>BNB Smart Chain</small></span></div>
`;

const dynamicPages = {
  search: {
    title: "Поиск",
    content: `<div class="dyn-search">Поиск</div><div class="dyn-list">${tokenRows}</div>`,
  },
  scan: {
    title: "Сканировать",
    content: `<div class="qr-frame"></div><div class="dyn-card"><strong>Наведите камеру на QR-код</strong><p>В демо-режиме сканер не обращается к камере и не читает реальные данные.</p></div>`,
  },
  send: {
    title: "Отправить",
    content: `<div class="dyn-search">Поиск токена</div><div class="dyn-list">${tokenRows}</div>`,
  },
  receive: {
    title: "Получить",
    content: `<div class="dyn-search">Поиск токена</div><div class="dyn-list">${tokenRows}</div>`,
  },
  buy: {
    title: "Купить",
    content: `<div class="dyn-card"><strong>Выберите криптовалюту</strong><p>Покупка открывается как локальный экран без платежей и внешних провайдеров.</p></div><div class="dyn-list">${tokenRows}</div>`,
  },
  deposit: {
    title: "Пополнить",
    content: `<div class="dyn-card"><strong>Пополнение</strong><p>Выберите актив, чтобы увидеть экран пополнения в стиле приложения.</p></div><div class="dyn-list">${tokenRows}</div>`,
  },
  settings: {
    title: "Настройки",
    content: `
      <div class="dyn-list">
        <div class="dyn-row"><span class="dyn-icon">●</span><span>Кошельки<small>Основной кошелек 1</small></span></div>
        <div class="dyn-row"><span class="dyn-icon">☾</span><span>Тема<small>Системная</small></span></div>
        <div class="dyn-row"><span class="dyn-icon">₽</span><span>Валюта<small>USD</small></span></div>
        <div class="dyn-row"><span class="dyn-icon">?</span><span>Помощь<small>Центр поддержки</small></span></div>
      </div>`,
  },
  swapSettings: {
    title: "Настройки свопа",
    content: `
      <div class="dyn-list">
        <div class="dyn-row"><span class="dyn-icon">%</span><span>Макс. проскальзывание<small>Авто</small></span></div>
        <div class="dyn-row"><span class="dyn-icon">≋</span><span>Провайдер<small>Лучший маршрут</small></span></div>
      </div>`,
  },
  selectToken: {
    title: "Выберите токен",
    content: `<div class="dyn-search">Поиск токена</div><div class="dyn-list">${tokenRows}</div>`,
  },
  perpsHistory: {
    title: "История",
    content: `<div class="dyn-card"><strong>Сделок пока нет</strong><p>История бессрочных фьючерсов появится здесь.</p></div>`,
  },
  perpsSettings: {
    title: "Настройки",
    content: `<div class="dyn-list"><div class="dyn-row"><span class="dyn-icon">⚙</span><span>Настройки Perps<small>Плечо, уведомления и провайдеры</small></span></div></div>`,
  },
  predictions: {
    title: "Predictions",
    content: `<div class="dyn-card"><strong>Predictions</strong><p>Раздел недоступен в текущем регионе или не смог загрузиться.</p></div>`,
  },
  meme: {
    title: "Meme Rush",
    content: `<div class="dyn-card"><strong>Meme Rush</strong><p>Раздел временно недоступен.</p></div>`,
  },
  dapp: {
    title: "dApp",
    content: `<div class="dyn-search">Найти или ввести URL-адрес dApp</div><div class="dyn-card"><strong>Quick links</strong><p>Trust Wallet website, Центр поддержки, What is DeFi?</p></div>`,
  },
  info: {
    title: "Trust",
    content: `<div class="dyn-card"><strong>Раздел</strong><p>Экран добавлен как локальное состояние интерфейса.</p></div>`,
  },
};

const sheets = {
  wallet: {
    title: "Кошельки",
    content: `
      <div class="sheet-row is-on"><span class="sheet-badge">1</span><span>Основной кошелек 1<small>Multi-Coin Wallet</small></span></div>
      <div class="sheet-row"><span class="sheet-badge">+</span><span>Добавить кошелек<small>Создать или импортировать</small></span></div>`,
  },
  copied: {
    title: "Адрес",
    content: `<div class="sheet-row"><span class="sheet-badge">✓</span><span>Адрес скопирован<small>0x0000...0000</small></span></div>`,
  },
  manageTokens: {
    title: "Управлять криптовалютами",
    content: `
      <div class="sheet-row is-on"><span class="sheet-badge">₿</span><span>BTC<small>Bitcoin</small></span><span class="sheet-switch"></span></div>
      <div class="sheet-row is-on"><span class="sheet-badge">Ξ</span><span>ETH<small>Ethereum</small></span><span class="sheet-switch"></span></div>
      <div class="sheet-row is-on"><span class="sheet-badge">◎</span><span>SOL<small>Solana</small></span><span class="sheet-switch"></span></div>`,
  },
  info: {
    title: "Trust",
    content: `<div class="sheet-row"><span class="sheet-badge">i</span><span>Демо-состояние интерфейса</span></div>`,
  },
};

function setAssetTab(tab, resetScroll = false) {
  const nextTab = ["crypto", "favorites", "nft"].includes(tab) ? tab : "crypto";

  setMainView("home", false);
  screen.classList.toggle("is-favorites", nextTab === "favorites");
  screen.classList.toggle("is-nft", nextTab === "nft");
  assetTabs.forEach((button) => {
    button.classList.toggle("active", button.dataset.tab === nextTab);
  });

  if (resetScroll && scrollPane) {
    scrollPane.scrollTop = 0;
  }

  updateChrome();
}

assetTabs.forEach((button) => {
  button.addEventListener("click", () => {
    setAssetTab(button.dataset.tab, true);
  });
});

navHome?.addEventListener("click", () => {
  setMainView("home", true);
});

navMarkets?.addEventListener("click", () => {
  setMainView("markets", true);
});

navSwap?.addEventListener("click", () => {
  setMainView("swap", true);
});

navPerps?.addEventListener("click", () => {
  setMainView("perps", true);
});

navDiscover?.addEventListener("click", () => {
  setMainView("discover", true);
});

historyTab?.addEventListener("click", () => {
  setMainView("history", true);
});

manageTab?.addEventListener("click", () => {
  setMainView("manage", true);
});

document.querySelectorAll(".page-back-hit").forEach((button) => {
  button.addEventListener("click", () => {
    setMainView("home", false);
  });
});

dynamicBack?.addEventListener("click", closeDynamicPage);
sheetBackdrop?.addEventListener("click", closeSheet);
sheetClose?.addEventListener("click", closeSheet);

document.querySelectorAll(".qa-swap").forEach((button) => {
  button.addEventListener("click", () => {
    setMainView("swap", true);
  });
});

document.querySelector(".settings-dot")?.addEventListener("click", () => openDynamicPage("settings"));
document.querySelector(".search-pill")?.addEventListener("click", () => openDynamicPage("search"));
document.querySelector(".collapsed-search")?.addEventListener("click", () => openDynamicPage("search"));
document.querySelector(".scan-button")?.addEventListener("click", () => openDynamicPage("scan"));
document.querySelector(".wallet-chip")?.addEventListener("click", () => openSheet("wallet"));
document.querySelector(".copy-button")?.addEventListener("click", () => {
  openSheet("copied");
  showToast("Скопировано");
});
document.querySelector(".qa-send")?.addEventListener("click", () => openDynamicPage("send"));
document.querySelector(".qa-receive")?.addEventListener("click", () => openDynamicPage("receive"));
document.querySelector(".qa-buy")?.addEventListener("click", () => openDynamicPage("buy"));
document.querySelector(".empty-card .primary")?.addEventListener("click", () => openDynamicPage("deposit"));
document.querySelector(".empty-card .secondary")?.addEventListener("click", () => openDynamicPage("receive"));
document.querySelector(".favorite-empty button")?.addEventListener("click", () => openSheet("manageTokens"));
document.querySelector(".markets-search-hit")?.addEventListener("click", () => openDynamicPage("search", "markets"));
document.querySelector(".markets-predictions-hit")?.addEventListener("click", () => openDynamicPage("predictions", "markets"));
document.querySelector(".markets-meme-hit")?.addEventListener("click", () => openDynamicPage("meme", "markets"));
document.querySelector(".swap-settings-hit")?.addEventListener("click", () => openDynamicPage("swapSettings", "swap"));
document.querySelector(".swap-fund-hit")?.addEventListener("click", () => openDynamicPage("deposit", "swap"));
document.querySelector(".swap-token-hit")?.addEventListener("click", () => openDynamicPage("selectToken", "swap"));
document.querySelector(".discover-search-hit")?.addEventListener("click", () => openDynamicPage("dapp", "discover"));
document.querySelector(".discover-earn-hit")?.addEventListener("click", () => openDynamicPage("deposit", "discover"));
document.querySelector(".manage-crypto-hit")?.addEventListener("click", () => openSheet("manageTokens"));
document.querySelector(".perps-history-hit")?.addEventListener("click", () => openDynamicPage("perpsHistory", "perps"));
document.querySelector(".perps-settings-hit")?.addEventListener("click", () => openDynamicPage("perpsSettings", "perps"));
document.querySelector(".perps-deposit-hit")?.addEventListener("click", () => openDynamicPage("deposit", "perps"));
document.querySelector(".perps-search-hit")?.addEventListener("click", () => openDynamicPage("search", "perps"));
document.querySelectorAll(".perp-card").forEach((button) => {
  button.addEventListener("click", () => setMainView("perps", true));
});

document.querySelectorAll(".manage-low-balance-hit, .manage-nft-hit, .manage-predictions-hit, .manage-perps-hit").forEach((button) => {
  button.addEventListener("click", () => showToast("Настройка изменена"));
});

document.addEventListener("click", (event) => {
  const parsed = event.target.closest(".parsed-node");
  if (parsed) {
    const rid = parsed.dataset.rid;
    const label = parsed.dataset.label;
    if (rid && navTargets[rid]) {
      setMainView(navTargets[rid], true);
      return;
    }
    if (label === "Обмен") {
      setMainView("swap", true);
      return;
    }
    if (rid === "swap-back-button" || label === "swap-back-button" || label === "Закрыть лист") {
      setMainView("home", false);
      return;
    }
    if (rid === "bottomSheetCloseButton") {
      setMainView("home", false);
      return;
    }
    if (rid === "swap-settings-pill") openDynamicPage("swapSettings", "swap");
    if (rid === "swap-from-fund-button" || label === "Fund" || label === "Депозит" || label === "Deposit") openDynamicPage("deposit", parsed.dataset.page);
    if (rid === "swap-to-token-selector" || label === "Select token") openDynamicPage("selectToken", "swap");
    if (label === "Predictions") openDynamicPage("predictions", "markets");
    if (label === "Meme Rush") openDynamicPage("meme", "markets");
    if (rid === "bottomSheetManageCryptoButton") openSheet("manageTokens");
    if (/Checkbox|LayoutType/.test(rid)) showToast("Настройка изменена");
  }

  const switchRow = event.target.closest(".sheet-row");
  if (switchRow?.querySelector(".sheet-switch")) {
    switchRow.classList.toggle("is-on");
  }

  if (event.target.closest(".dyn-row")) {
    showToast("Открыто");
  }
});

document.querySelectorAll("button").forEach((button) => {
  button.addEventListener("click", () => {
    button.classList.remove("pressed");
    void button.offsetWidth;
    button.classList.add("pressed");
  });
});

if (scrollPane) {
  scrollPane.addEventListener("scroll", updateChrome, { passive: true });

  const params = new URLSearchParams(window.location.search);
  const requestedPage = params.get("page");
  setAssetTab(params.get("tab") || "crypto", false);
  setMainView(requestedPage || "home", false);

  const requestedAction = params.get("action");
  const requestedSheet = params.get("sheet");
  if (requestedAction) {
    openDynamicPage(requestedAction, requestedPage || "home");
  }
  if (requestedSheet) {
    openSheet(requestedSheet);
  }

  const requestedScroll = Number.parseFloat(params.get("scroll") || "0");
  if (requestedScroll > 0) {
    const px = Number.parseFloat(getComputedStyle(document.documentElement).getPropertyValue("--px"));
    scrollPane.scrollTop = requestedScroll * px;
    updateChrome();
    requestAnimationFrame(() => {
      scrollPane.scrollTop = requestedScroll * px;
      updateChrome();
    });
  } else {
    updateChrome();
  }
}

if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.getRegistrations().then((registrations) => {
      registrations.forEach((registration) => registration.unregister());
    });
  });
}

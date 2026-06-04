function handleTap(x, y) {
  if (state.sheet) return handleSheetTap(x, y);
  if (state.view === "splash") {
    state.view = "onboarding";
    draw();
    return;
  }
  if (state.view === "onboarding") {
    state.view = "login";
    draw();
    return;
  }
  if (state.view === "login") {
    const contentH = dp(340 + 16 + 48 + 8 + 48);
    const top = (BASE_H - contentH) / 2;
    if (hit(x, y, dp(16), top + dp(356), BASE_W - dp(16), top + dp(404))) state.view = "home";
    else if (hit(x, y, dp(16), top + dp(412), BASE_W - dp(16), top + dp(460))) state.view = "home";
    else state.onboardingPage = ((state.onboardingPage || 0) + 1) % ONBOARDING_PAGES.length;
    draw();
    return;
  }
  if (state.view === "history") {
    if (hit(x, y, 0, 0, 140, 240)) {
      state.view = "home";
    } else if (hit(x, y, 44, 520, 1036, 760) && state.txs.length) {
      state.selectedTx = state.txs[0];
      state.sheet = "txDetail";
    }
    draw();
    return;
  }
  if (state.view === "manage") {
    if (hit(x, y, 0, 0, 150, 250) || hit(x, y, 88, 1660, 992, 1772)) state.view = "home";
    else if (hit(x, y, 78, 422, 342, 672)) state.manageLayout = 0;
    else if (hit(x, y, 408, 422, 672, 672)) state.manageLayout = 1;
    else if (hit(x, y, 738, 422, 1002, 672)) state.manageLayout = 2;
    else if (hit(x, y, 760, 850, 992, 980)) state.hideSmallAssets = !state.hideSmallAssets;
    else if (hit(x, y, 760, 1025, 992, 1155)) state.hideNft = !state.hideNft;
    else if (hit(x, y, 760, 1200, 992, 1330)) state.hidePredictions = !state.hidePredictions;
    else if (hit(x, y, 760, 1375, 992, 1505)) state.hidePerps = !state.hidePerps;
    draw();
    return;
  }
  if (state.view === "swap") {
    if (hit(x, y, 0, 0, 150, 250)) state.view = "home";
    else if (hit(x, y, 930, 80, 1065, 250)) state.sheet = "swapDisabled";
    else if (hit(x, y, 496, 568, 584, 656)) state.swapReversed = !state.swapReversed;
    else if (hit(x, y, 646, 616, 1036, 804) || hit(x, y, 646, 296, 1036, 590)) state.sheet = "swapToken";
    else if (hit(x, y, 44, BASE_H - 328, 1036, BASE_H - 196)) state.sheet = "swapDisabled";
    draw();
    return;
  }
  if (state.view === "perps") {
    if (handleBottomNavTap(x, y)) {
      draw();
      return;
    }
    if (hit(x, y, 0, 0, 150, 250)) state.sheet = "perpsHistory";
    else if (hit(x, y, 930, 80, 1065, 250)) state.sheet = "perpsSettings";
    else if (hit(x, y, 300, 485, 540, 557)) state.sheet = "perpsDeposit";
    else if (hit(x, y, 44, 1120, 1036, 1770)) {
      state.selectedMarket = "BTC";
      state.previousView = "perps";
      state.view = "marketDetail";
    }
    draw();
    return;
  }
  if (state.view === "predictions") {
    if (handleBottomNavTap(x, y)) {
      draw();
      return;
    }
    if (hit(x, y, 0, 0, 150, 250)) state.view = "home";
    else if (hit(x, y, 930, 80, 1065, 250)) state.sheet = "search";
    else if (hit(x, y, 44, 820, 1036, 1710)) state.sheet = "predictionAction";
    draw();
    return;
  }
  if (state.view === "marketDetail") {
    if (hit(x, y, 0, 0, 150, 250)) state.view = state.previousView || "popular";
    else if (hit(x, y, 930, 80, 1065, 250)) state.sheet = "search";
    else if (hit(x, y, 44, 1125, 350, 1245)) state.sheet = "buy";
    else if (hit(x, y, 387, 1125, 693, 1245)) state.view = "swap";
    else if (hit(x, y, 730, 1125, 1036, 1245)) {
      setReceiveAsset(state.selectedMarket, state.selectedMarket === "BTC" ? "Bitcoin" : state.selectedMarket === "ETH" ? "Ethereum" : state.selectedMarket === "SOL" ? "Solana" : "BNB Smart Chain", "0xa61e0...74B0C99");
      state.sheet = "receiveQr";
    }
    draw();
    return;
  }
  if (handleBottomNavTap(x, y)) {}
  else if (state.view === "home") {
    const cy = y + state.scrollY;
    if (hit(x, cy, 20, 120, 170, 320)) state.sheet = "settings";
    else if (hit(x, cy, 160, 125, 915, 320)) state.sheet = "search";
    else if (hit(x, cy, 915, 120, 1060, 320)) state.sheet = "scan";
    else if (hit(x, cy, 430, 345, 650, 470)) state.sheet = "wallet";
    else if (hit(x, cy, 650, 345, 760, 470)) state.sheet = "copied";
    else if (hit(x, cy, 90, 780, 318, 1010)) state.sheet = "sendList";
    else if (hit(x, cy, 325, 780, 533, 1010)) state.sheet = "receiveList";
    else if (hit(x, cy, 540, 780, 748, 1010)) state.view = "swap";
    else if (hit(x, cy, 755, 780, 963, 1010)) state.sheet = "buy";
    else if (hit(x, cy, 44, 1340, 380, 1465)) state.assetTab = "crypto";
    else if (hit(x, cy, 400, 1340, 650, 1465)) state.assetTab = "favorites";
    else if (hit(x, cy, 670, 1340, 790, 1465)) state.assetTab = "nft";
    else if (hit(x, cy, 790, 1320, 900, 1475)) state.view = "history";
    else if (hit(x, cy, 930, 1320, 1040, 1475)) state.view = "manage";
    else if (state.assetTab === "favorites" && hit(x, cy, 215, 1805, 865, 1955)) state.view = "manage";
    else if (state.assetTab === "nft" && hit(x, cy, 190, 1880, 890, 2045)) state.view = "manage";
    else if (hit(x, cy, 760, 1360, 900, 1460)) state.view = "history";
  } else if (state.view === "popular") {
    const py = y + state.scrollY;
    if (hit(x, py, 930, 80, 1065, 250)) state.sheet = "search";
    else if (hit(x, py, 44, 250, 524, 400)) state.sheet = "swapDisabled";
    else if (hit(x, py, 556, 250, 1036, 400)) state.sheet = "swapDisabled";
    else if (hit(x, py, 44, 550, 374, 835)) openMarketDetail("USDT");
    else if (hit(x, py, 392, 550, 722, 835)) openMarketDetail("BTC");
    else if (hit(x, py, 740, 550, 1070, 835)) openMarketDetail("ETH");
    else if (hit(x, py, 44, 852, 254, 944) || hit(x, py, 300, 852, 830, 944) || hit(x, py, 865, 852, 1036, 944)) state.sheet = "search";
    else if (hit(x, py, 44, 1105, 1036, 1275)) openMarketDetail("BNB");
    else if (hit(x, py, 44, 1285, 1036, 1455)) openMarketDetail("XRP");
    else if (hit(x, py, 44, 1465, 1036, 1635)) openMarketDetail("USDC");
    else if (hit(x, py, 44, 1645, 1036, 1815)) openMarketDetail("SOL");
  } else if (state.view === "rewards") {
    if (hit(x, y, 44, 1200, 274, 1292)) state.rewardsTab = "active";
    else if (hit(x, y, 300, 1200, 530, 1292)) state.rewardsTab = "past";
    else if (state.rewardsTab === "active") {
      const compact = BASE_H < 2180;
      const cardY = compact ? 1320 : 1340;
      const cardH = compact ? 390 : 520;
      if (hit(x, y, 44, cardY, 1036, cardY + cardH)) state.sheet = "rewardDetail";
    }
    else if (state.rewardsTab === "past") {
      const compact = BASE_H < 2180;
      const firstY = compact ? 1320 : 1340;
      const secondY = compact ? 1518 : 1705;
      const cardH = compact ? 166 : 315;
      if (
        hit(x, y, 44, firstY, 524, firstY + cardH) ||
        hit(x, y, 556, firstY, 1036, firstY + cardH) ||
        hit(x, y, 44, secondY, 524, secondY + cardH) ||
        hit(x, y, 556, secondY, 1036, secondY + cardH)
      ) state.sheet = "rewardDetail";
    }
  } else if (state.view === "more") {
    if (hit(x, y, 44, 256, 1036, 355)) state.sheet = "search";
    else if (hit(x, y, 44, 537, 529, 818)) setView("rewards");
    else if (hit(x, y, 44, 995, 1036, 1081)) state.sheet = "support";
  }
  draw();
}

function openMarketDetail(symbol) {
  state.selectedMarket = symbol;
  state.previousView = state.view;
  state.view = "marketDetail";
  state.scrollY = 0;
}

function setView(view) {
  if (state.view !== view) state.scrollY = 0;
  state.view = view;
}

function handleBottomNavTap(x, y) {
  if (state.view === "home" || state.view === "popular" || state.view === "more") {
    if (hit(x, y, 50, 2092, 246, BASE_H)) {
      setView("home");
      return true;
    }
    if (hit(x, y, 246, 2092, 442, BASE_H)) {
      setView("popular");
      return true;
    }
    if (hit(x, y, 463, 2020, 617, BASE_H)) {
      state.sheet = "trade";
      return true;
    }
    if (hit(x, y, 638, 2092, 834, BASE_H)) {
      setView("perps");
      return true;
    }
    if (hit(x, y, 834, 2092, 1030, BASE_H)) {
      setView("more");
      return true;
    }
    return false;
  }
  if (state.view === "perps") {
    if (hit(x, y, 5, 2090, 203, BASE_H)) {
      setView("home");
      return true;
    }
    if (hit(x, y, 200, 2090, 401, BASE_H)) {
      setView("popular");
      return true;
    }
    if (hit(x, y, 462, 2015, 618, BASE_H)) {
      state.sheet = "trade";
      return true;
    }
    if (hit(x, y, 591, 2090, 792, BASE_H)) {
      setView("perps");
      return true;
    }
    if (hit(x, y, 786, 2090, 987, BASE_H)) {
      setView("more");
      return true;
    }
    return false;
  }
  const navTop = NAV_TOP;
  if (hit(x, y, 24, navTop, 268, BASE_H)) {
    setView("home");
    return true;
  }
  if (hit(x, y, 268, navTop, 470, BASE_H)) {
    setView("popular");
    return true;
  }
  if (hit(x, y, 470, navTop - 40, 650, BASE_H)) {
    state.sheet = "trade";
    return true;
  }
  if (hit(x, y, 650, navTop, 835, BASE_H)) {
    setView("perps");
    return true;
  }
  if (hit(x, y, 835, navTop, 1056, BASE_H)) {
    setView("more");
    return true;
  }
  return false;
}

function handleSheetTap(x, y) {
  if (state.sheet === "trade") {
    const sheet = tradeSheetMetrics();
    if (hit(x, y, 463, 2020, 617, BASE_H)) {
      state.sheet = null;
    } else if (hit(x, y, 44, sheet.swapY - 82, 1036, sheet.swapY + 105)) {
      state.sheet = null;
      state.view = "swap";
    } else if (hit(x, y, 44, sheet.perpsY - 82, 1036, sheet.perpsY + 128)) {
      state.sheet = null;
      setView("perps");
    } else if (hit(x, y, 44, sheet.predictionsY - 82, 1036, sheet.predictionsY + 128)) {
      state.sheet = null;
      setView("predictions");
    } else if (y < sheet.top) state.sheet = null;
    draw();
    return;
  }
  if (hit(x, y, 0, 150, 205, 390) || hit(x, y, 900, 150, 1080, 390)) {
    if (state.sheet === "network") state.sheet = "sendForm";
    else if (state.sheet === "receiveQr" && hit(x, y, 35, 210, 145, 320)) state.sheet = "receiveList";
    else state.sheet = null;
    draw();
    return;
  }
  if (state.sheet === "sendList") {
    if (hit(x, y, 44, 640, 1036, 805)) {
      setSendAsset("USDT");
      state.sheet = "sendForm";
    } else if (hit(x, y, 44, 815, 1036, 980)) {
      setSendAsset("TRX");
      state.sheet = "sendForm";
    } else if (hit(x, y, 44, 990, 1036, 1160)) {
      setSendAsset("USDT_TON");
      state.sheet = "sendForm";
    }
  } else if (state.sheet === "receiveList") {
    const layout = receiveListLayout();
    const option = receiveListOptions().find((row, index) => {
      const rowY = layout.startY + index * layout.step;
      return hit(x, y, 44, rowY - layout.rowHitTop, 1036, rowY + layout.rowHitBottom);
    });
    if (option) {
      setReceiveAsset(option[0], option[1], option[2]);
      state.sheet = "receiveQr";
    }
  } else if (state.sheet === "buy") {
    const actionY = sheetActionY();
    if (hit(x, y, 44, actionY, 1036, actionY + 145)) state.sheet = null;
  } else if (state.sheet === "swapToken") {
    if (hit(x, y, 44, 620, 1036, 780)) {
      state.swapToken = "BTC";
      state.sheet = null;
    } else if (hit(x, y, 44, 795, 1036, 955)) {
      state.swapToken = "ETH";
      state.sheet = null;
    } else if (hit(x, y, 44, 970, 1036, 1130)) {
      state.swapToken = "SOL";
      state.sheet = null;
    } else if (hit(x, y, 44, 1145, 1036, 1305)) {
      state.swapToken = "BNB";
      state.sheet = null;
    }
  } else if (state.sheet === "swapDisabled") {
    const top = bottomSheetTop(1450, 950);
    const actionY = Math.min(top + 560, BASE_H - 260);
    if (hit(x, y, 88, actionY, 992, actionY + 132) || y < top) state.sheet = null;
  } else if (state.sheet === "wallet") {
    const actionY = sheetActionY(2050);
    if (y < 1140 || hit(x, y, 88, actionY, 992, actionY + 126)) state.sheet = null;
  } else if (state.sheet === "copied") {
    const top = bottomSheetTop(1575, 825);
    const actionY = Math.min(top + 495, BASE_H - 260);
    if (y < top || hit(x, y, 88, actionY, 992, actionY + 126)) state.sheet = null;
  } else if (state.sheet === "settings" || state.sheet === "search" || state.sheet === "scan" || state.sheet === "support" || state.sheet === "about" || state.sheet === "perpsDeposit" || state.sheet === "perpsSettings" || state.sheet === "perpsHistory" || state.sheet === "predictionAction") {
    if (hit(x, y, 35, 210, 145, 320)) state.sheet = null;
    else if ((state.sheet === "support" || state.sheet === "about" || state.sheet === "perpsDeposit" || state.sheet === "perpsSettings" || state.sheet === "perpsHistory" || state.sheet === "predictionAction") && hit(x, y, 88, 1710, 992, 1836)) state.sheet = null;
  } else if (state.sheet === "rewardDetail") {
    if (hit(x, y, 35, 210, 145, 320) || hit(x, y, 88, 1710, 992, 1836)) state.sheet = null;
  } else if (state.sheet === "sendForm") {
    if (hit(x, y, 44, 455, 1036, 613)) openEditor("address");
    else if (hit(x, y, 44, 765, 604, 857)) state.sheet = "network";
    else if (hit(x, y, 44, 980, 1036, 1130)) openEditor("amount");
    else {
      const actionY = sheetActionY() - 20;
      if (hit(x, y, 44, actionY, 1036, actionY + 145)) state.sheet = "confirm";
    }
  } else if (state.sheet === "network") {
    const option = Math.floor((y - 488) / 145);
    const nets = ["BNB Smart Chain", "Ethereum", "Tron", "TON"];
    if (nets[option]) {
      state.sendNetwork = nets[option];
      state.sheet = "sendForm";
    }
  } else if (state.sheet === "confirm") {
    const actionY = sheetActionY();
    if (hit(x, y, 44, actionY, 1036, actionY + 145)) {
      createSendTx();
      state.sheet = "processing";
    }
  } else if (state.sheet === "processing") {
    const actionY = sheetActionY(2010);
    if (hit(x, y, 88, actionY, 992, actionY + 132)) {
      state.selectedTx = state.txs[0];
      state.view = "history";
      state.sheet = "txDetail";
    }
  } else if (state.sheet === "txDetail" && y < 760) {
    state.sheet = null;
  }
  draw();
}

function createSendTx() {
  const value = Math.min(state.sendAmount, sendBalance());
  if (state.sendAsset === "TRX") state.balances.trx = Math.max(0, state.balances.trx - value);
  else if (state.sendAsset === "USDT_TON") state.balances.usdtTon = Math.max(0, state.balances.usdtTon - value);
  else state.balances.usdtTron = Math.max(0, state.balances.usdtTron - value);
  const fee = feeAmount(state.sendNetwork, value);
  const feeUsd = feeFiat(state.sendNetwork, value);
  const tx = {
    sent: true,
    title: "Отправлено",
    rawAddress: state.sendAddress,
    address: `В: ${ellipsizeAddress(state.sendAddress)}`,
    amount: `-${amount(value, 6)} ${sendAssetDisplay()}`,
    fiat: `≈ $${money(value * sendAssetPrice())}`,
    feeAmount: `${amount(fee, 8)} ${feeSymbol(state.sendNetwork)}`,
    feeFiat: `≈ $${money(feeUsd)}`,
    network: state.sendNetwork,
  };
  state.txs.unshift(tx);
  state.selectedTx = tx;
}

function openEditor(mode) {
  editorMode = mode;
  editorLabel.textContent = mode === "address" ? "Адрес получателя" : `Сумма ${sendAssetDisplay()}`;
  editorInput.value = mode === "address" ? state.sendAddress : String(state.sendAmount).replace(".", ",");
  editorInput.inputMode = mode === "address" ? "text" : "decimal";
  editor.hidden = false;
  window.setTimeout(() => {
    editorInput.focus();
    editorInput.select();
  }, 20);
}

function applyEditor() {
  const value = editorInput.value.trim();
  if (editorMode === "address" && value) state.sendAddress = value;
  if (editorMode === "amount") {
    const n = Number(value.replace(",", "."));
    if (Number.isFinite(n) && n > 0) state.sendAmount = Math.min(n, sendBalance());
  }
  editor.hidden = true;
  draw();
}

function hit(x, y, l, t, r, b) {
  return x >= l && x <= r && y >= t && y <= b;
}

function clamp(v, min, max) {
  return Math.max(min, Math.min(max, v));
}


function draw() {
  ctx.save();
  ctx.setTransform(1, 0, 0, 1, 0, 0);
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  ctx.restore();

  ctx.save();
  ctx.translate(ox, oy);
  ctx.scale(scale, scale);
  ctx.globalAlpha = 1;
  ctx.globalCompositeOperation = "source-over";
  ctx.fillStyle = BG;
  ctx.fillRect(0, 0, BASE_W, BASE_H);
  if (state.view === "splash") drawSplash();
  else if (state.view === "onboarding") drawOnboarding();
  else if (state.view === "login") drawLogin();
  else {
    drawStatus();
    if (state.view === "popular") drawPopular();
    else if (state.view === "rewards") drawRewards();
    else if (state.view === "more") drawMore();
    else if (state.view === "marketDetail") drawMarketDetail();
    else if (state.view === "history") drawHistory();
    else if (state.view === "manage") drawManagePage();
    else if (state.view === "swap") drawSwapPage();
    else if (state.view === "perps") drawPerpsPage();
    else if (state.view === "predictions") drawPredictionsPage();
    else {
      ctx.save();
      ctx.translate(0, -state.scrollY);
      drawHomeContent();
      ctx.restore();
      drawScrollIndicator();
      drawHomeBottomNav();
    }
  }
  if (state.sheet) {
    dim();
    drawSheet();
  }
  ctx.restore();

}

function paintHomeDirect() {
  if (state.view !== "home" || state.sheet) return;
  ctx.save();
  ctx.translate(ox, oy);
  ctx.scale(scale, scale);
  ctx.save();
  ctx.translate(0, -state.scrollY);
  drawHomeContent();
  ctx.restore();
  drawScrollIndicator();
  drawHomeBottomNav();
  ctx.restore();
}

function drawStatus() {
  img("assets/status-bar.png", 0, 0, 1080, 102);
}


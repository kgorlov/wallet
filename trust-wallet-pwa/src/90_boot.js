canvas.addEventListener("pointerdown", (evt) => {
  const p = toBase(evt);
  dragging = true;
  moved = false;
  lastY = p.y;
});

canvas.addEventListener("pointermove", (evt) => {
  if (!dragging || state.sheet || !["home", "popular"].includes(state.view)) return;
  const p = toBase(evt);
  const dy = lastY - p.y;
  if (Math.abs(dy) > 2) moved = true;
  lastY = p.y;
  const maxScroll = state.view === "popular" ? 540 : 1240;
  state.scrollY = clamp(state.scrollY + dy, 0, maxScroll);
  scrollIndicatorUntil = Date.now() + 650;
  draw();
});

canvas.addEventListener("pointerup", (evt) => {
  const p = toBase(evt);
  if (!moved) handleTap(p.x, p.y);
  dragging = false;
});

editorCancel.addEventListener("click", () => {
  editor.hidden = true;
});
editorApply.addEventListener("click", applyEditor);
editorInput.addEventListener("keydown", (evt) => {
  if (evt.key === "Enter") applyEditor();
});

window.__trustWalletDebug = {
  state,
  draw,
  handleTap,
  tradeSheetMetrics,
  runtime: () => ({ scale, ox, oy, BASE_H, NAV_TOP }),
};

window.addEventListener("resize", resize);
window.addEventListener("orientationchange", resize);
window.visualViewport?.addEventListener("resize", resize);

resize();
document.fonts?.ready.then(draw).catch(() => {});
loadAssets().then(draw);
if ("serviceWorker" in navigator) {
  navigator.serviceWorker.getRegistrations()
    .then((registrations) => Promise.all(registrations.map((registration) => registration.unregister())))
    .catch(() => {});
}

if ("caches" in window) {
  caches.keys()
    .then((keys) => Promise.all(keys.map((key) => caches.delete(key))))
    .catch(() => {});
}


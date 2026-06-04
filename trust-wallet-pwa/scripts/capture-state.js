const { chromium } = require("playwright");

(async () => {
  const url = process.env.CAPTURE_URL || "http://127.0.0.1:8080";
  const output = process.env.CAPTURE_OUTPUT || "capture-state.png";
  const viewport = (process.env.CAPTURE_VIEWPORT || "590x1280").split("x").map(Number);
  const statePatch = process.env.CAPTURE_STATE ? JSON.parse(process.env.CAPTURE_STATE) : {};

  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage({
    viewport: { width: viewport[0], height: viewport[1] },
    deviceScaleFactor: 1,
    isMobile: true,
    hasTouch: true,
  });

  await page.goto(url, { waitUntil: "networkidle" });
  await page.waitForTimeout(800);
  await page.evaluate((patch) => {
    Object.assign(window.__trustWalletDebug.state, patch);
    window.__trustWalletDebug.draw();
  }, statePatch);
  await page.waitForTimeout(300);
  await page.screenshot({ path: output });
  await browser.close();
})();

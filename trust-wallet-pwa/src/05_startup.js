const ANDROID = {
  white: "#ffffff",
  primary: "#2e91db",
  primaryDark: "#1E76CE",
  title: "#89000000",
  item: "#dd000000",
  body: "#2e91db",
  dp: () => BASE_W / 393,
};

const ONBOARDING_PAGES = [
  {
    title: "Private & Secure",
    message: "Private keys never leave your device.",
    image: "assets/native-ui/onboarding_lock.png",
  },
  {
    title: "ERC20 Compatible",
    message: "Support for ERC20 tokens by default.",
    image: "assets/native-ui/onboarding_erc20.png",
  },
  {
    title: "Fully Transparent",
    message: "Code is open sourced (GPL-3.0 license) and fully audited.",
    image: "assets/native-ui/onboarding_open_source.png",
  },
  {
    title: "Ultra reliable",
    message: "The fastest Ethereum wallet experience on mobile.",
    image: "assets/native-ui/onboarding_rocket.png",
  },
];

function dp(value) {
  return value * ANDROID.dp();
}

function androidFont(size, weight = 400) {
  return `${weight} ${size}px Roboto, Arial, sans-serif`;
}

function androidText(value, x, y, size, color, align = "left", weight = 400) {
  ctx.fillStyle = color;
  ctx.font = androidFont(size, weight);
  ctx.textAlign = align;
  ctx.textBaseline = "alphabetic";
  ctx.fillText(value, x, y);
}

function drawLightStatus() {
  androidText("11:34", dp(24), dp(22), dp(14), "#222222", "left", 500);
  androidText("LTE", BASE_W - dp(62), dp(22), dp(12), "#222222", "left", 400);
  strokeRect(BASE_W - dp(32), dp(10), dp(18), dp(9), "#222222", dp(2), dp(0.8));
  rect(BASE_W - dp(13), dp(13), dp(1.5), dp(4), "#222222", dp(0.6));
  rect(BASE_W - dp(30.5), dp(11.5), dp(13), dp(6), "#222222", dp(1));
}

function drawSplash() {
  rect(0, 0, BASE_W, BASE_H, ANDROID.white, 0);
  drawLightStatus();
  const size = dp(128);
  img("assets/native-ui/ic_splash.png", (BASE_W - size) / 2, (BASE_H - size) / 2, size, size);
}

function drawOnboarding() {
  rect(0, 0, BASE_W, BASE_H, ANDROID.white, 0);
  drawLightStatus();
  const y = (BASE_H - dp(340)) / 2;
  drawOnboardingPage(state.onboardingPage || 0, y);
}

function drawLogin() {
  rect(0, 0, BASE_W, BASE_H, ANDROID.white, 0);
  drawLightStatus();
  const contentH = dp(340 + 16 + 48 + 8 + 48);
  const y = (BASE_H - contentH) / 2;
  drawOnboardingPage(state.onboardingPage || 0, y);
  drawPageIndicator(y + dp(340) - dp(22));
  drawAndroidButton("Create new wallet", dp(16), y + dp(340 + 16), BASE_W - dp(32), dp(48), ANDROID.primary, ANDROID.white);
  drawAndroidButton("Already have a wallet?", dp(16), y + dp(340 + 16 + 48 + 8), BASE_W - dp(32), dp(48), "#f5f5f5", ANDROID.primaryDark);
}

function drawOnboardingPage(pageIndex, topY) {
  const page = ONBOARDING_PAGES[Math.max(0, Math.min(ONBOARDING_PAGES.length - 1, pageIndex))];
  const titleY = topY + dp(45);
  const imageSize = dp(200);
  const imageY = titleY + dp(16);
  const msgY = imageY + imageSize + dp(47);
  androidText(page.title, BASE_W / 2, titleY, dp(28), ANDROID.primary, "center", 400);
  img(page.image, (BASE_W - imageSize) / 2, imageY, imageSize, imageSize);
  drawCenteredWrappedAndroidText(page.message, BASE_W / 2, msgY, BASE_W - dp(32), dp(16), ANDROID.primary, dp(22));
}

function drawCenteredWrappedAndroidText(value, centerX, baselineY, maxW, size, color, lineH) {
  ctx.font = androidFont(size, 400);
  const words = value.split(" ");
  const lines = [];
  let lineText = "";
  for (const word of words) {
    const next = lineText ? `${lineText} ${word}` : word;
    if (ctx.measureText(next).width > maxW && lineText) {
      lines.push(lineText);
      lineText = word;
    } else {
      lineText = next;
    }
  }
  if (lineText) lines.push(lineText);
  const offset = (lines.length - 1) * lineH / 2;
  lines.forEach((lineValue, index) => {
    androidText(lineValue, centerX, baselineY + index * lineH - offset, size, color, "center", 400);
  });
}

function drawPageIndicator(y) {
  const gap = dp(12);
  const r = dp(4);
  const start = BASE_W / 2 - gap * 1.5;
  for (let i = 0; i < 4; i += 1) {
    ctx.globalAlpha = i === (state.onboardingPage || 0) ? 1 : 0.35;
    ctx.fillStyle = ANDROID.primaryDark;
    ctx.beginPath();
    ctx.arc(start + i * gap, y, r, 0, Math.PI * 2);
    ctx.fill();
    ctx.globalAlpha = 1;
  }
}

function drawAndroidButton(label, x, y, w, h, fill, color) {
  rect(x, y, w, h, fill, dp(2));
  androidText(label, x + w / 2, y + h / 2 + dp(5), dp(14), color, "center", 600);
}

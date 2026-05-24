package com.wallet.crypto.trustvisual;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams attrs = window.getAttributes();
            attrs.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(attrs);
        }
        hideSystemBars();
        setContentView(new TrustCanvasView(this));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }

    private void hideSystemBars() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    static class TrustCanvasView extends View {
        static final int BASE_W = 1080;
        static final int BASE_H = 2400;
        static final int BG = Color.rgb(27, 27, 28);
        static final int PANEL = Color.rgb(39, 40, 44);
        static final int GREEN = Color.rgb(39, 214, 139);
        static final int RED = Color.rgb(255, 86, 98);
        static final int TEXT = Color.rgb(244, 244, 247);
        static final int MUTED = Color.rgb(170, 170, 178);
        static final String COINGECKO_API_KEY = "CG-8ByqwzyKrAZ8SKgezJSwqPGC";
        static final String MARKET_URL = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=100&page=1&sparkline=true&price_change_percentage=24h&locale=en";

        static final int SHEET_NONE = 0;
        static final int SHEET_MARKET_CATEGORY = 1;
        static final int SHEET_MARKET_NETWORK = 2;
        static final int SHEET_MARKET_SORT = 3;
        static final int SHEET_MARKET_RANGE = 4;
        static final int SHEET_SWAP_TOKEN = 5;
        static final int SHEET_SWAP_SETTINGS = 6;
        static final int SHEET_SAFE_ACTION = 7;
        static final int SHEET_PERPS_PROVIDER = 8;
        static final int SHEET_PERPS_SORT = 9;
        static final int SHEET_SEND = 10;
        static final int SHEET_RECEIVE = 11;
        static final int SHEET_BUY = 12;
        static final int SHEET_FUND = 13;
        static final int SHEET_DEPOSIT = 14;

        static final int MODAL_NONE = 0;
        static final int MODAL_SEARCH = 1;
        static final int MODAL_TOKEN_DETAIL = 2;
        static final int MODAL_DISCOVER_DETAIL = 3;

        final Activity activity;
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        final Map<String, Bitmap> bitmaps = new HashMap<String, Bitmap>();
        final Map<String, MarketCoin> marketCoins = new HashMap<String, MarketCoin>();
        final ArrayList<MarketCoin> marketList = new ArrayList<MarketCoin>();
        final Handler handler = new Handler(Looper.getMainLooper());
        float scale = 1f;
        float offsetX = 0f;
        float offsetY = 0f;
        int page = 0; // home, markets, swap, perps, discover
        int assetTab = 0; // crypto, favorites, nft
        int sheet = SHEET_NONE;
        int modal = MODAL_NONE;
        int marketFilter = 0;
        int networkFilter = 0;
        int sortMode = 0;
        int timeRange = 0;
        int manageLayout = 2;
        boolean hideSmallAssets = true;
        boolean hideNft = false;
        boolean hidePredictions = false;
        boolean hidePerps = false;
        boolean swapReversed = false;
        boolean history = false;
        boolean manage = false;
        float scrollY = 0f;
        float downX = 0f;
        float downY = 0f;
        float lastY = 0f;
        boolean dragging = false;
        boolean moved = false;
        String safeActionTitle = "";
        String selectedDiscoverTitle = "";
        MarketCoin selectedCoin = null;
        MarketCoin selectedSwapToken = null;
        boolean updating = false;
        long lastUpdateMs = 0L;
        final Runnable updater = new Runnable() {
            @Override
            public void run() {
                fetchMarketData();
                handler.postDelayed(this, 30000L);
            }
        };

        TrustCanvasView(Activity activity) {
            super(activity);
            this.activity = activity;
            setFocusable(true);
            setBackgroundColor(BG);
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            handler.removeCallbacks(updater);
            updater.run();
        }

        @Override
        protected void onDetachedFromWindow() {
            handler.removeCallbacks(updater);
            super.onDetachedFromWindow();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            scale = Math.min(getWidth() / (float) BASE_W, getHeight() / (float) BASE_H);
            offsetX = (getWidth() - BASE_W * scale) * 0.5f;
            offsetY = (getHeight() - BASE_H * scale) * 0.5f;

            canvas.save();
            canvas.translate(offsetX, offsetY);
            canvas.scale(scale, scale);
            rect(canvas, 0, 0, BASE_W, BASE_H, BG, 0);

            if (history) {
                drawHistory(canvas);
            } else if (manage) {
                drawManage(canvas);
            } else if (page == 1) {
                drawFixedChromePage(canvas, "assets/ui/markets-page-content-rich.png", "assets/ui/bottom-nav-markets.png");
                drawMarketsFilters(canvas);
                drawLiveMarkets(canvas);
                rect(canvas, 0, 1910, 1080, 360, BG, 0);
                drawBmp(canvas, "assets/ui/bottom-nav-markets.png", 0, 2020, 1080, 250);
                drawSystemNav(canvas);
            } else if (page == 2) {
                drawStandalonePage(canvas, "assets/ui/swap-page-content.png");
                drawSwapState(canvas);
            } else if (page == 3) {
                drawFixedChromePage(canvas, "assets/ui/perps-page-content.png", "assets/ui/bottom-nav-perps.png");
                drawPerpsFilters(canvas);
                drawLivePerps(canvas);
                rect(canvas, 0, 1780, 1080, 490, BG, 0);
                drawBmp(canvas, "assets/ui/bottom-nav-perps.png", 0, 2020, 1080, 250);
                drawSystemNav(canvas);
            } else if (page == 4) {
                drawChromePage(canvas, "assets/ui/discover-page-content.png", "assets/ui/bottom-nav-discover.png");
            } else {
                drawHome(canvas);
            }
            drawModalOrSheet(canvas);
            canvas.restore();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = (event.getX() - offsetX) / scale;
            float y = (event.getY() - offsetY) / scale;

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downX = x;
                downY = y;
                lastY = y;
                dragging = isScrollableSurface(y);
                moved = false;
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (dragging) {
                    float dy = lastY - y;
                    if (Math.abs(y - downY) > 14 || Math.abs(x - downX) > 14) moved = true;
                    scrollY = clamp(scrollY + dy, 0, maxScroll());
                    lastY = y;
                    invalidate();
                }
                return true;
            }

            if (event.getAction() != MotionEvent.ACTION_UP) return true;

            if (moved) {
                dragging = false;
                moved = false;
                return true;
            }
            dragging = false;

            if (modal != MODAL_NONE) return handleModalTouch(x, y);
            if (sheet != SHEET_NONE) return handleSheetTouch(x, y);

            if (history || manage) {
                if (manage) {
                    if (hit(x, y, 292, 955, 424, 1087)) return setManageLayout(0);
                    if (hit(x, y, 474, 955, 606, 1087)) return setManageLayout(1);
                    if (hit(x, y, 656, 955, 788, 1087)) return setManageLayout(2);
                    if (hit(x, y, 700, 1460, 1030, 1630)) { hideSmallAssets = !hideSmallAssets; invalidate(); return true; }
                    if (hit(x, y, 700, 1630, 1030, 1810)) { hideNft = !hideNft; invalidate(); return true; }
                    if (hit(x, y, 700, 1810, 1030, 1990)) { hidePredictions = !hidePredictions; invalidate(); return true; }
                    if (hit(x, y, 700, 1990, 1030, 2165)) { hidePerps = !hidePerps; invalidate(); return true; }
                }
                if (y < 260 || (manage && x > 905 && y > 410 && y < 610)) {
                    history = false;
                    manage = false;
                    invalidate();
                }
                return true;
            }

            if (hit(x, y, 44, 2020, 230, 2270)) return openPage(0);
            if (hit(x, y, 230, 2020, 416, 2270)) return openPage(1);
            if (hit(x, y, 416, 1985, 664, 2270)) return openPage(2);
            if (hit(x, y, 664, 2020, 850, 2270)) return openPage(3);
            if (hit(x, y, 850, 2020, 1036, 2270)) return openPage(4);

            if (page == 0) {
                float cy = y + scrollY;
                y = cy;
                if (hit(x, y, 11, 102, 143, 234)) return openSafeSheet("Настройки");
                if (hit(x, y, 143, 102, 948, 234)) return openSearch();
                if (hit(x, y, 948, 102, 1080, 234)) return openSafeSheet("Сканер");
                if (hit(x, y, 318, 271, 740, 403)) return openSafeSheet("Основной кошелек 1");
                if (hit(x, y, 740, 271, 872, 403)) return openSafeSheet("Адрес скопирован");
                if (hit(x, y, 144, 473, 342, 687)) return openSheet(SHEET_SEND);
                if (hit(x, y, 342, 473, 540, 687)) return openSheet(SHEET_RECEIVE);
                if (hit(x, cy, 44, 731, 453, 857)) return setAssetTab(0);
                if (hit(x, cy, 453, 731, 653, 857)) return setAssetTab(1);
                if (hit(x, cy, 653, 731, 783, 857)) return setAssetTab(2);
                if (hit(x, cy, 783, 731, 893, 857)) {
                    history = true;
                    invalidate();
                    return true;
                }
                if (hit(x, cy, 893, 731, 1036, 857)) {
                    manage = true;
                    invalidate();
                    return true;
                }
                if (hit(x, cy, 540, 473, 738, 687)) return openPage(2);
                if (hit(x, y, 738, 473, 936, 687)) return openSheet(SHEET_BUY);
                if (hit(x, cy, 44, 1850, 496, 2020)) return openPage(3);
            }

            if (page == 1) {
                if (hit(x, y, 970, 110, 1040, 230)) return openSearch();
                if (hit(x, y, 44, 256, 529, 410)) return openDiscover("Predictions");
                if (hit(x, y, 551, 256, 1036, 410)) return openDiscover("Meme Rush");
                if (hit(x, y, 44, 850, 150, 980)) return setMarketFilter(1);
                if (hit(x, y, 160, 850, 350, 980)) return setMarketFilter(0);
                if (hit(x, y, 370, 850, 580, 980)) return setMarketFilter(2);
                if (hit(x, y, 600, 850, 735, 980)) return setMarketFilter(3);
                if (hit(x, y, 755, 850, 900, 980)) return setMarketFilter(4);
                if (hit(x, y, 920, 850, 1070, 980)) return setMarketFilter(5);
                if (hit(x, y, 44, 990, 255, 1100)) return openSheet(SHEET_MARKET_NETWORK);
                if (hit(x, y, 300, 990, 840, 1100)) return openSheet(SHEET_MARKET_SORT);
                if (hit(x, y, 865, 990, 1036, 1100)) return openSheet(SHEET_MARKET_RANGE);
                if (hit(x, y, 44, 1135, 1036, 1910)) return openCoinListAt(y + scrollY, 1230, 3, 182);
            }

            if (page == 2) {
                if (hit(x, y, 44, 110, 150, 240)) return openPage(0);
                if (hit(x, y, 930, 110, 1036, 240)) return openSheet(SHEET_SWAP_SETTINGS);
                if (hit(x, y, 646, 592, 992, 710)) return openSheet(SHEET_SWAP_TOKEN);
                if (hit(x, y, 496, 530, 584, 625)) { swapReversed = !swapReversed; invalidate(); return true; }
                if (hit(x, y, 705, 278, 992, 416)) return openSheet(SHEET_FUND);
                if (hit(x, y, 44, 2072, 1036, 2204)) return openSafeSheet(selectedSwapToken == null ? "Выберите токен" : "Недостаточно средств");
            }

            if (page == 3) {
                if (hit(x, y, 44, 102, 154, 234)) { history = true; invalidate(); return true; }
                if (hit(x, y, 926, 102, 1036, 234)) return openSafeSheet("Настройки бессрочных");
                if (hit(x, y, 748, 628, 1010, 760)) return openSheet(SHEET_DEPOSIT);
                if (hit(x, y, 44, 890, 1036, 1022)) return openSearch();
                if (hit(x, y, 44, 1050, 514, 1140)) return openSheet(SHEET_PERPS_PROVIDER);
                if (hit(x, y, 610, 1050, 1036, 1140)) return openSheet(SHEET_PERPS_SORT);
                if (hit(x, y, 44, 925, 144, 1025)) return setMarketFilter(1);
                if (hit(x, y, 160, 925, 370, 1025)) return setMarketFilter(0);
                if (hit(x, y, 390, 925, 560, 1025)) return setMarketFilter(2);
                if (hit(x, y, 580, 925, 790, 1025)) return setMarketFilter(3);
                if (hit(x, y, 810, 925, 1036, 1025)) return setMarketFilter(4);
                if (hit(x, y, 44, 1180, 1036, 1780)) return openCoinListAt(y + scrollY, 1300, 0, 180);
            }

            if (page == 4) {
                y += scrollY;
                if (hit(x, y, 72, 240, 1036, 372)) return openSearch();
                if (hit(x, y, 44, 537, 529, 818)) return openDiscover("Стейкинг");
                if (hit(x, y, 0, 840, 1080, 973)) return openDiscover("Trust Wallet website");
                if (hit(x, y, 0, 973, 1080, 1106)) return openDiscover("Центр поддержки");
                if (hit(x, y, 0, 1106, 1080, 1505)) return openDiscover("Справка dApp");
            }
            return true;
        }

        boolean isScrollableSurface(float y) {
            if (modal != MODAL_NONE || sheet != SHEET_NONE || history || manage) return false;
            if (y >= 1985) return false;
            return page == 0 || page == 1 || page == 3 || page == 4;
        }

        float maxScroll() {
            if (page == 0) return assetTab == 0 ? 760f : assetTab == 1 ? 340f : 220f;
            if (page == 1) return Math.max(0f, Math.max(0, visibleCoins().size() - 3) * 182f - 680f);
            if (page == 3) return Math.max(0f, visibleCoins().size() * 180f - 600f);
            if (page == 4) return 420f;
            return 0f;
        }

        float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(max, value));
        }

        boolean openPage(int nextPage) {
            page = nextPage;
            scrollY = 0f;
            history = false;
            manage = false;
            modal = MODAL_NONE;
            sheet = SHEET_NONE;
            invalidate();
            return true;
        }

        boolean setAssetTab(int nextTab) {
            assetTab = nextTab;
            scrollY = 0f;
            invalidate();
            return true;
        }

        boolean openSheet(int nextSheet) {
            sheet = nextSheet;
            invalidate();
            return true;
        }

        boolean openSafeSheet(String title) {
            safeActionTitle = title;
            sheet = SHEET_SAFE_ACTION;
            invalidate();
            return true;
        }

        boolean openSearch() {
            modal = MODAL_SEARCH;
            sheet = SHEET_NONE;
            invalidate();
            return true;
        }

        boolean setMarketFilter(int filter) {
            marketFilter = filter;
            scrollY = 0f;
            invalidate();
            return true;
        }

        boolean setManageLayout(int layout) {
            manageLayout = layout;
            invalidate();
            return true;
        }

        boolean openDiscover(String title) {
            selectedDiscoverTitle = title;
            modal = MODAL_DISCOVER_DETAIL;
            invalidate();
            return true;
        }

        boolean openCoin(MarketCoin coin) {
            if (coin == null) return true;
            selectedCoin = coin;
            modal = MODAL_TOKEN_DETAIL;
            sheet = SHEET_NONE;
            invalidate();
            return true;
        }

        boolean openCoinAt(float y) {
            List<MarketCoin> coins = visibleCoins();
            int index;
            if (y < 1000) index = (int) ((y - 553) / 180);
            else index = (int) ((y - 1188) / 182);
            if (index < 0) index = 0;
            if (index >= coins.size()) index = coins.size() - 1;
            if (index >= 0 && index < coins.size()) return openCoin(coins.get(index));
            return true;
        }

        boolean openCoinListAt(float contentY, float firstRowY, int startIndex, float rowHeight) {
            List<MarketCoin> coins = visibleCoins();
            int index = startIndex + (int) ((contentY - firstRowY) / rowHeight);
            if (index >= 0 && index < coins.size()) return openCoin(coins.get(index));
            return true;
        }

        boolean handleModalTouch(float x, float y) {
            if (y < 250 || hit(x, y, 44, 110, 170, 240)) {
                modal = MODAL_NONE;
                invalidate();
                return true;
            }
            if (modal == MODAL_SEARCH) {
                List<MarketCoin> coins = visibleCoins();
                int index = (int) ((y - 520) / 150);
                if (index >= 0 && index < Math.min(8, coins.size())) return openCoin(coins.get(index));
            } else if (modal == MODAL_TOKEN_DETAIL) {
                if (hit(x, y, 88, 1190, 200, 1260)) timeRange = 0;
                else if (hit(x, y, 230, 1190, 342, 1260)) timeRange = 1;
                else if (hit(x, y, 360, 1190, 472, 1260)) timeRange = 2;
                invalidate();
                return true;
            } else if (modal == MODAL_DISCOVER_DETAIL) {
                if (hit(x, y, 88, 830, 992, 950)) {
                    modal = MODAL_NONE;
                    invalidate();
                    return true;
                }
            }
            return true;
        }

        boolean handleSheetTouch(float x, float y) {
            if (y < 1260 || hit(x, y, 930, 1280, 1036, 1400)) {
                sheet = SHEET_NONE;
                invalidate();
                return true;
            }
            int option = (int) ((y - 1510) / 132);
            if (sheet == SHEET_MARKET_CATEGORY) {
                marketFilter = Math.max(0, Math.min(5, option));
                scrollY = 0f;
            } else if (sheet == SHEET_MARKET_NETWORK || sheet == SHEET_PERPS_PROVIDER) {
                networkFilter = Math.max(0, Math.min(3, option));
                scrollY = 0f;
            } else if (sheet == SHEET_MARKET_SORT || sheet == SHEET_PERPS_SORT) {
                sortMode = Math.max(0, Math.min(2, option));
                scrollY = 0f;
            } else if (sheet == SHEET_MARKET_RANGE) {
                timeRange = Math.max(0, Math.min(2, option));
                scrollY = 0f;
            }
            else if (sheet == SHEET_SWAP_TOKEN) {
                List<MarketCoin> coins = visibleCoins();
                int tokenOption = (int) ((y - 1642) / 132);
                if (tokenOption >= 0 && tokenOption < Math.min(5, coins.size())) selectedSwapToken = coins.get(tokenOption);
            }
            sheet = SHEET_NONE;
            invalidate();
            return true;
        }

        boolean hit(float x, float y, float left, float top, float right, float bottom) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }

        Bitmap bmp(String name) {
            Bitmap cached = bitmaps.get(name);
            if (cached != null) return cached;
            try {
                InputStream input = activity.getAssets().open(name);
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                input.close();
                bitmaps.put(name, bitmap);
                return bitmap;
            } catch (Exception ignored) {
                return null;
            }
        }

        void drawBmp(Canvas canvas, String name, float x, float y, float width, float height) {
            Bitmap bitmap = bmp(name);
            if (bitmap == null) return;
            canvas.drawBitmap(bitmap, null, new RectF(x, y, x + width, y + height), paint);
        }

        void rect(Canvas canvas, float x, float y, float width, float height, int color, float radius) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            canvas.drawRoundRect(new RectF(x, y, x + width, y + height), radius, radius, paint);
        }

        void drawStatus(Canvas canvas) {
            drawBmp(canvas, "assets/status-bar.png", 0, 0, 1080, 102);
        }

        void drawSystemNav(Canvas canvas) {
        }

        void fetchMarketData() {
            if (updating) return;
            updating = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Map<String, MarketCoin> fetched = parseMarkets(readUrl(MARKET_URL));
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                marketCoins.clear();
                                marketCoins.putAll(fetched);
                                marketList.clear();
                                marketList.addAll(fetched.values());
                                lastUpdateMs = System.currentTimeMillis();
                                updating = false;
                                invalidate();
                            }
                        });
                    } catch (Exception ignored) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                updating = false;
                                invalidate();
                            }
                        });
                    }
                }
            }, "market-data").start();
        }

        String readUrl(String urlString) throws Exception {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(12000);
            connection.setReadTimeout(12000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "TrustVisual/1.0");
            connection.setRequestProperty("x-cg-demo-api-key", COINGECKO_API_KEY);
            InputStream input = connection.getInputStream();
            try {
                byte[] buffer = new byte[4096];
                StringBuilder out = new StringBuilder();
                int read;
                while ((read = input.read(buffer)) != -1) {
                    out.append(new String(buffer, 0, read, "UTF-8"));
                }
                return out.toString();
            } finally {
                input.close();
                connection.disconnect();
            }
        }

        Map<String, MarketCoin> parseMarkets(String json) throws Exception {
            Map<String, MarketCoin> result = new HashMap<String, MarketCoin>();
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                MarketCoin coin = new MarketCoin();
                coin.id = item.optString("id");
                coin.symbol = item.optString("symbol").toUpperCase(Locale.US);
                coin.name = item.optString("name");
                coin.price = item.optDouble("current_price", 0d);
                coin.change24h = item.optDouble("price_change_percentage_24h", 0d);
                coin.volume = item.optDouble("total_volume", 0d);
                coin.marketCap = item.optDouble("market_cap", 0d);
                JSONObject spark = item.optJSONObject("sparkline_in_7d");
                if (spark != null) {
                    JSONArray prices = spark.optJSONArray("price");
                    if (prices != null) {
                        for (int p = Math.max(0, prices.length() - 42); p < prices.length(); p++) {
                            coin.sparkline.add(prices.optDouble(p, coin.price));
                        }
                    }
                }
                result.put(coin.id, coin);
            }
            return result;
        }

        void drawLiveMarkets(Canvas canvas) {
            canvas.save();
            canvas.clipRect(0, 1135, 1080, 1910);
            rect(canvas, 0, 1135, 1080, 775, BG, 0);
            List<MarketCoin> coins = visibleCoins();
            float y = 1230 - scrollY;
            for (int i = 3; i < Math.min(coins.size(), 18); i++) {
                liveMarketRow(canvas, coins.get(i), y);
                y += 182;
            }
            canvas.restore();
        }

        void drawMarketsFilters(Canvas canvas) {
            rect(canvas, 0, 825, 1080, 310, BG, 0);
            filterPill(canvas, 44, 850, 100, 100, "★", marketFilter == 1);
            filterPill(canvas, 160, 850, 200, 100, "Hot tokens", marketFilter == 0);
            filterPill(canvas, 380, 850, 220, 100, "Top Gainers", marketFilter == 2);
            filterPill(canvas, 620, 850, 135, 100, "RWA", marketFilter == 3);
            filterPill(canvas, 775, 850, 145, 100, "Meme", marketFilter == 4);
            filterPill(canvas, 940, 850, 140, 100, "DeFi", marketFilter == 5);
            filterPill(canvas, 44, 990, 210, 90, networkFilter == 0 ? "Сеть" : networkLabel(), false);
            filterPill(canvas, 300, 990, 540, 90, sortLabel(), false);
            filterPill(canvas, 865, 990, 171, 90, rangeLabel(), false);
        }

        void drawPerpsFilters(Canvas canvas) {
            rect(canvas, 0, 900, 1080, 280, BG, 0);
            filterPill(canvas, 44, 925, 100, 100, "☆", marketFilter == 1);
            filterPill(canvas, 160, 925, 210, 100, "Popular", marketFilter == 0);
            filterPill(canvas, 390, 925, 170, 100, "New", marketFilter == 2);
            filterPill(canvas, 580, 925, 210, 100, "Crypto", marketFilter == 3);
            filterPill(canvas, 810, 925, 226, 100, "Stocks", marketFilter == 4);
            filterPill(canvas, 44, 1050, 470, 90, networkFilter == 0 ? "Все поставщики" : providerLabel());
            filterPill(canvas, 610, 1050, 426, 90, perpsSortLabel());
        }

        void filterPill(Canvas canvas, float x, float y, float w, float h, String label) {
            filterPill(canvas, x, y, w, h, label, false);
        }

        void filterPill(Canvas canvas, float x, float y, float w, float h, String label, boolean selected) {
            rect(canvas, x, y, w, h, selected ? Color.rgb(96, 101, 98) : Color.rgb(43, 43, 47), h * 0.5f);
            text(canvas, label, x + w * 0.5f, y + h * 0.62f, label.length() > 12 ? 34 : 38, selected ? TEXT : Color.rgb(214, 214, 220), Paint.Align.CENTER, true);
        }

        String networkLabel() {
            if (networkFilter == 1) return "Ethereum";
            if (networkFilter == 2) return "BNB";
            if (networkFilter == 3) return "Solana";
            return "Сеть";
        }

        String providerLabel() {
            if (networkFilter == 1) return "Binance";
            if (networkFilter == 2) return "Hyperliquid";
            if (networkFilter == 3) return "dYdX";
            return "Все поставщики";
        }

        String sortLabel() {
            if (sortMode == 1) return "Top Gainers ↓";
            if (sortMode == 2) return "Объем (24 ч.) ↓";
            return "Рыночная капитализация ↓";
        }

        String perpsSortLabel() {
            if (sortMode == 1) return "Рост цены ↓";
            if (sortMode == 2) return "Открытый интерес ↓";
            return "Объем (24 ч.) ↓";
        }

        String rangeLabel() {
            if (timeRange == 1) return "7d";
            if (timeRange == 2) return "30d";
            return "24h";
        }

        void drawLivePerps(Canvas canvas) {
            canvas.save();
            canvas.clipRect(0, 1180, 1080, 1780);
            rect(canvas, 0, 1180, 1080, 600, BG, 0);
            List<MarketCoin> coins = visibleCoins();
            float y = 1300 - scrollY;
            for (int i = 0; i < Math.min(12, coins.size()); i++) {
                livePerpsRow(canvas, coins.get(i), y);
                y += 180;
            }
            canvas.restore();
        }

        void liveMarketCard(Canvas canvas, MarketCoin coin, float x, float y) {
            if (coin == null) return;
            rect(canvas, x, y, 330, 380, PANEL, 42);
            text(canvas, shortName(coin), x + 50, y + 78, 34, MUTED, Paint.Align.LEFT, true);
            rect(canvas, x + 230, y + 38, 58, 58, Color.rgb(12, 13, 15), 29);
            text(canvas, coin.symbol.length() > 0 ? coin.symbol.substring(0, 1) : "?", x + 259, y + 80, 30, TEXT, Paint.Align.CENTER, true);
            text(canvas, formatPrice(coin.price), x + 50, y + 160, 50, TEXT, Paint.Align.LEFT, true);
            text(canvas, formatPct(coin.change24h), x + 50, y + 225, 38, coin.change24h < 0 ? RED : GREEN, Paint.Align.LEFT, true);
            sparkline(canvas, coin, x + 56, y + 270, 245, 76);
        }

        void liveMarketRow(Canvas canvas, MarketCoin coin, float y) {
            if (coin == null) return;
            rect(canvas, 44, y - 112, 992, 152, BG, 0);
            rect(canvas, 44, y - 84, 88, 88, Color.rgb(55, 56, 58), 44);
            text(canvas, coin.symbol.length() > 0 ? coin.symbol.substring(0, 1) : "?", 88, y - 27, 42, TEXT, Paint.Align.CENTER, true);
            text(canvas, coin.symbol, 160, y - 22, 46, TEXT, Paint.Align.LEFT, true);
            text(canvas, "$" + compact(coin.marketCap) + " MCap · $" + compact(coin.volume) + " Vol", 160, y + 32, 31, MUTED, Paint.Align.LEFT, true);
            text(canvas, formatPrice(coin.price), 1035, y - 6, 47, TEXT, Paint.Align.RIGHT, true);
            text(canvas, formatPct(coin.change24h), 1035, y + 52, 34, coin.change24h < 0 ? RED : GREEN, Paint.Align.RIGHT, true);
            sparkline(canvas, coin, 760, y + 1, 150, 58);
        }

        void livePerpsRow(Canvas canvas, MarketCoin coin, float y) {
            if (coin == null) return;
            rect(canvas, 132, y - 112, 430, 230, BG, 0);
            text(canvas, coin.symbol, 160, y - 25, 46, TEXT, Paint.Align.LEFT, true);
            text(canvas, perpsSubtitle(coin), 160, y + 30, 31, MUTED, Paint.Align.LEFT, true);
            rect(canvas, 535, y - 88, 502, 168, BG, 0);
            text(canvas, formatPrice(coin.price), 1035, y - 10, 47, TEXT, Paint.Align.RIGHT, true);
            text(canvas, formatPct(coin.change24h), 1035, y + 50, 34, coin.change24h < 0 ? RED : GREEN, Paint.Align.RIGHT, true);
            sparkline(canvas, coin, 560, y - 26, 150, 58);
        }

        String perpsSubtitle(MarketCoin coin) {
            if (coin.name == null || coin.name.equalsIgnoreCase(coin.symbol)) {
                return "$" + compact(coin.volume) + " Vol - 20x";
            }
            return coin.name;
        }

        void sparkline(Canvas canvas, MarketCoin coin, float x, float y, float width, float height) {
            if (coin.sparkline.size() < 2) return;
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (double value : coin.sparkline) {
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
            if (max <= min) max = min + 1d;
            Path path = new Path();
            for (int i = 0; i < coin.sparkline.size(); i++) {
                float px = x + width * i / (float) (coin.sparkline.size() - 1);
                float py = y + height - (float) ((coin.sparkline.get(i) - min) / (max - min)) * height;
                if (i == 0) path.moveTo(px, py);
                else path.lineTo(px, py);
            }
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6);
            paint.setColor(coin.change24h < 0 ? RED : GREEN);
            canvas.drawPath(path, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        void text(Canvas canvas, String text, float x, float y, float size, int color, Paint.Align align, boolean bold) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            paint.setTextSize(size);
            paint.setTextAlign(align);
            paint.setFakeBoldText(bold);
            canvas.drawText(text, x, y, paint);
            paint.setFakeBoldText(false);
        }

        String formatPrice(double value) {
            if (value >= 1000d) return "$" + String.format(Locale.US, "%,.0f", value);
            if (value >= 100d) return "$" + String.format(Locale.US, "%,.2f", value);
            if (value >= 1d) return "$" + String.format(Locale.US, "%,.2f", value);
            return "$" + String.format(Locale.US, "%.4f", value);
        }

        String formatPct(double value) {
            return String.format(Locale.US, "%+.2f%%", value);
        }

        String shortName(MarketCoin coin) {
            if (coin.name == null || coin.name.length() == 0) return coin.symbol;
            return coin.name.length() > 12 ? coin.name.substring(0, 11) + "..." : coin.name;
        }

        String compact(double value) {
            if (value >= 1000000000d) return String.format(Locale.US, "%.2fB", value / 1000000000d);
            if (value >= 1000000d) return String.format(Locale.US, "%.2fM", value / 1000000d);
            if (value >= 1000d) return String.format(Locale.US, "%.1fK", value / 1000d);
            return String.format(Locale.US, "%.0f", value);
        }

        List<MarketCoin> visibleCoins() {
            ArrayList<MarketCoin> coins = new ArrayList<MarketCoin>();
            if (marketList.size() == 0) {
                coins.addAll(marketCoins.values());
            } else {
                coins.addAll(marketList);
            }
            if (coins.size() == 0) coins.addAll(fallbackCoins());
            for (int i = coins.size() - 1; i >= 0; i--) {
                MarketCoin c = coins.get(i);
                if (page == 3) {
                    if (marketFilter == 2 && c.change24h < 0) coins.remove(i);
                } else {
                    if (marketFilter == 2 && c.change24h < 0) coins.remove(i);
                    else if (marketFilter == 3 && !containsAny(c, "bitcoin", "ethereum", "chainlink", "render", "ondo")) coins.remove(i);
                    else if (marketFilter == 4 && !containsAny(c, "pepe", "doge", "shib", "bonk", "floki")) coins.remove(i);
                    else if (marketFilter == 5 && !containsAny(c, "uniswap", "aave", "maker", "curve", "chainlink")) coins.remove(i);
                }
            }
            if (sortMode == 1) {
                Collections.sort(coins, new Comparator<MarketCoin>() {
                    @Override public int compare(MarketCoin a, MarketCoin b) { return Double.compare(b.change24h, a.change24h); }
                });
            } else if (sortMode == 2) {
                Collections.sort(coins, new Comparator<MarketCoin>() {
                    @Override public int compare(MarketCoin a, MarketCoin b) { return Double.compare(b.volume, a.volume); }
                });
            } else {
                Collections.sort(coins, new Comparator<MarketCoin>() {
                    @Override public int compare(MarketCoin a, MarketCoin b) { return Double.compare(b.marketCap, a.marketCap); }
                });
            }
            if (coins.size() == 0) coins.addAll(marketList);
            return coins;
        }

        List<MarketCoin> fallbackCoins() {
            ArrayList<MarketCoin> coins = new ArrayList<MarketCoin>();
            coins.add(fallbackCoin("bitcoin", "BTC", "Bitcoin", 76890d, 2.9d, 2430000000d, 1520000000000d));
            coins.add(fallbackCoin("ethereum", "ETH", "Ethereum", 2118d, 4.4d, 1110000000d, 255000000000d));
            coins.add(fallbackCoin("tether", "USDT", "Tether", 0.9988d, -0.01d, 705000000d, 141000000000d));
            coins.add(fallbackCoin("binancecoin", "BNB", "BNB", 659d, 3.1d, 655000000d, 88900000000d));
            coins.add(fallbackCoin("ripple", "XRP", "XRP", 1.36d, 3.6d, 1380000000d, 84200000000d));
            coins.add(fallbackCoin("usd-coin", "USDC", "USDC", 0.9998d, 0.01d, 9140000000d, 76400000000d));
            coins.add(fallbackCoin("solana", "SOL", "Solana", 86.2d, 4.9d, 2630000000d, 49900000000d));
            coins.add(fallbackCoin("tron", "TRX", "TRON", 0.363d, 1.0d, 422000000d, 34500000000d));
            coins.add(fallbackCoin("dogecoin", "DOGE", "Dogecoin", 0.103d, 3.5d, 729000000d, 15900000000d));
            coins.add(fallbackCoin("hyperliquid", "HYPE", "Hyperliquid", 63.0d, 14.2d, 1150000000d, 15100000000d));
            coins.add(fallbackCoin("zcash", "ZEC", "Zcash", 637d, 8.3d, 699000000d, 10600000000d));
            return coins;
        }

        MarketCoin fallbackCoin(String id, String symbol, String name, double price, double change, double volume, double marketCap) {
            MarketCoin coin = new MarketCoin();
            coin.id = id;
            coin.symbol = symbol;
            coin.name = name;
            coin.price = price;
            coin.change24h = change;
            coin.volume = volume;
            coin.marketCap = marketCap;
            double[] points = change < 0 ? new double[]{1, .98, .99, .96, .95, .93, .94, .91} : new double[]{.91, .92, .9, .94, .96, .95, .99, 1};
            for (double point : points) coin.sparkline.add(price * point);
            return coin;
        }

        boolean containsAny(MarketCoin coin, String a, String b, String c, String d, String e) {
            String source = (coin.id + " " + coin.name + " " + coin.symbol).toLowerCase(Locale.US);
            return source.contains(a) || source.contains(b) || source.contains(c) || source.contains(d) || source.contains(e);
        }

        void drawFilterState(Canvas canvas) {
        }

        void drawChromePage(Canvas canvas, String content, String bottomNav) {
            drawStatus(canvas);
            canvas.save();
            canvas.clipRect(0, 102, 1080, 2020);
            drawBmp(canvas, content, 0, 102 - scrollY, 1080, 1918);
            canvas.restore();
            rect(canvas, 0, 1910, 1080, 360, BG, 0);
            drawBmp(canvas, bottomNav, 0, 2020, 1080, 250);
            drawSystemNav(canvas);
        }

        void drawFixedChromePage(Canvas canvas, String content, String bottomNav) {
            drawStatus(canvas);
            drawBmp(canvas, content, 0, 102, 1080, 1918);
            rect(canvas, 0, 1910, 1080, 360, BG, 0);
            drawBmp(canvas, bottomNav, 0, 2020, 1080, 250);
            drawSystemNav(canvas);
        }

        void drawBottomNav(Canvas canvas, int activePage) {
            rect(canvas, 38, 2028, 1004, 232, Color.rgb(20, 21, 23), 86);
            float[] centers = new float[]{146, 330, 540, 750, 934};
            String[] icons = new String[]{"⌂", "↗", "⇄", "%", "◉"};
            String[] labels = new String[]{"Главная", "Рынки", "Обмен", "Бесср.", "Подробнее"};
            for (int i = 0; i < centers.length; i++) {
                boolean active = activePage == i;
                if (i == 2) {
                    rect(canvas, centers[i] - 78, 1972, 156, 156, GREEN, 78);
                    text(canvas, icons[i], centers[i], 2072, 64, Color.rgb(3, 28, 14), Paint.Align.CENTER, true);
                    text(canvas, labels[i], centers[i], 2196, 28, active ? GREEN : MUTED, Paint.Align.CENTER, true);
                    continue;
                }
                if (active) rect(canvas, centers[i] - 92, 2052, 184, 138, Color.rgb(31, 70, 46), 72);
                text(canvas, icons[i], centers[i], 2115, i == 3 ? 50 : 56, active ? GREEN : MUTED, Paint.Align.CENTER, true);
                text(canvas, labels[i], centers[i], 2188, i == 4 ? 27 : 28, active ? GREEN : MUTED, Paint.Align.CENTER, true);
            }
        }

        void drawStandalonePage(Canvas canvas, String content) {
            drawStatus(canvas);
            drawBmp(canvas, content, 0, 102, 1080, 2166);
            drawSystemNav(canvas);
        }

        void drawModalOrSheet(Canvas canvas) {
            if (modal == MODAL_SEARCH) drawSearch(canvas);
            else if (modal == MODAL_TOKEN_DETAIL) drawTokenDetail(canvas);
            else if (modal == MODAL_DISCOVER_DETAIL) drawDiscoverDetail(canvas);
            if (sheet != SHEET_NONE) drawSheet(canvas);
        }

        void dim(Canvas canvas) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(185, 0, 0, 0));
            canvas.drawRect(0, 0, BASE_W, BASE_H, paint);
        }

        void drawSearch(Canvas canvas) {
            rect(canvas, 0, 0, BASE_W, BASE_H, BG, 0);
            drawStatus(canvas);
            text(canvas, "<", 78, 185, 72, MUTED, Paint.Align.CENTER, false);
            rect(canvas, 150, 116, 880, 100, Color.rgb(35, 36, 39), 50);
            text(canvas, "Поиск токена или dApp", 205, 180, 40, MUTED, Paint.Align.LEFT, true);
            text(canvas, "Популярное", 44, 330, 46, TEXT, Paint.Align.LEFT, true);
            List<MarketCoin> coins = visibleCoins();
            float y = 520;
            for (int i = 0; i < Math.min(8, coins.size()); i++) {
                drawCoinRow(canvas, coins.get(i), y, true);
                y += 150;
            }
            drawSystemNav(canvas);
        }

        void drawTokenDetail(Canvas canvas) {
            MarketCoin coin = selectedCoin;
            if (coin == null) return;
            rect(canvas, 0, 0, BASE_W, BASE_H, BG, 0);
            drawStatus(canvas);
            text(canvas, "<", 78, 185, 72, MUTED, Paint.Align.CENTER, false);
            text(canvas, coin.name, 540, 185, 56, TEXT, Paint.Align.CENTER, true);
            String subtitle = coin.name != null && coin.name.equalsIgnoreCase(coin.symbol) ? "" : coin.symbol;
            if (subtitle.length() > 0) text(canvas, subtitle, 540, 245, 36, MUTED, Paint.Align.CENTER, true);
            text(canvas, formatPrice(coin.price), 540, 420, 86, TEXT, Paint.Align.CENTER, true);
            text(canvas, formatPct(coin.change24h), 540, 500, 44, coin.change24h < 0 ? RED : GREEN, Paint.Align.CENTER, true);
            rect(canvas, 44, 610, 992, 520, PANEL, 42);
            sparkline(canvas, coin, 92, 690, 896, 330);
            chip(canvas, 88, 1190, "24h", timeRange == 0);
            chip(canvas, 230, 1190, "7d", timeRange == 1);
            chip(canvas, 360, 1190, "30d", timeRange == 2);
            rect(canvas, 44, 1340, 992, 360, Color.rgb(34, 35, 38), 34);
            text(canvas, "Рыночная капитализация", 88, 1430, 38, MUTED, Paint.Align.LEFT, true);
            text(canvas, "$" + compact(coin.marketCap), 992, 1430, 42, TEXT, Paint.Align.RIGHT, true);
            text(canvas, "Объем (24 ч.)", 88, 1545, 38, MUTED, Paint.Align.LEFT, true);
            text(canvas, "$" + compact(coin.volume), 992, 1545, 42, TEXT, Paint.Align.RIGHT, true);
            text(canvas, "Цена", 88, 1660, 38, MUTED, Paint.Align.LEFT, true);
            text(canvas, formatPrice(coin.price), 992, 1660, 42, TEXT, Paint.Align.RIGHT, true);
            drawSystemNav(canvas);
        }

        void drawDiscoverDetail(Canvas canvas) {
            rect(canvas, 0, 0, BASE_W, BASE_H, BG, 0);
            drawStatus(canvas);
            text(canvas, "<", 78, 185, 72, MUTED, Paint.Align.CENTER, false);
            text(canvas, selectedDiscoverTitle, 540, 185, 52, TEXT, Paint.Align.CENTER, true);
            rect(canvas, 44, 340, 992, 430, PANEL, 42);
            text(canvas, "Раздел открыт", 540, 500, 54, TEXT, Paint.Align.CENTER, true);
            text(canvas, "Без внешних переходов,", 540, 590, 34, MUTED, Paint.Align.CENTER, true);
            text(canvas, "подписей и реальных операций.", 540, 645, 34, MUTED, Paint.Align.CENTER, true);
            rect(canvas, 88, 830, 904, 120, GREEN, 62);
            text(canvas, "Готово", 540, 906, 42, Color.rgb(4, 19, 11), Paint.Align.CENTER, true);
            drawSystemNav(canvas);
        }

        void drawSheet(Canvas canvas) {
            dim(canvas);
            rect(canvas, 0, 1260, 1080, 1008, Color.rgb(24, 24, 25), 48);
            text(canvas, "×", 980, 1370, 64, MUTED, Paint.Align.CENTER, false);
            String title = sheet == SHEET_MARKET_NETWORK ? "Сеть" :
                    sheet == SHEET_PERPS_PROVIDER ? "Поставщик" :
                    sheet == SHEET_MARKET_SORT ? "Сортировка" :
                    sheet == SHEET_PERPS_SORT ? "Сортировка" :
                    sheet == SHEET_MARKET_RANGE ? "Период" :
                    sheet == SHEET_SWAP_TOKEN ? "Выберите токен" :
                    sheet == SHEET_SWAP_SETTINGS ? "Настройки свопа" :
                    sheet == SHEET_SEND ? "Отправить" :
                    sheet == SHEET_RECEIVE ? "Получить" :
                    sheet == SHEET_BUY ? "Купить" :
                    sheet == SHEET_FUND ? "Пополнить" :
                    sheet == SHEET_DEPOSIT ? "Депозит" :
                    sheet == SHEET_SAFE_ACTION ? safeActionTitle : "Фильтр";
            text(canvas, title, 88, 1385, 52, TEXT, Paint.Align.LEFT, true);
            if (sheet == SHEET_SEND || sheet == SHEET_RECEIVE || sheet == SHEET_BUY || sheet == SHEET_FUND || sheet == SHEET_DEPOSIT) {
                drawActionFlowSheet(canvas, sheet);
                return;
            }
            if (sheet == SHEET_SAFE_ACTION) {
                text(canvas, "Операция недоступна в UI-прототипе.", 88, 1530, 40, MUTED, Paint.Align.LEFT, true);
                text(canvas, "Реальные переводы, QR, seed и подписи отключены.", 88, 1600, 34, MUTED, Paint.Align.LEFT, true);
                rect(canvas, 88, 1800, 904, 120, Color.rgb(43, 92, 60), 62);
                text(canvas, "Понятно", 540, 1876, 42, GREEN, Paint.Align.CENTER, true);
                return;
            }
            if (sheet == SHEET_SWAP_SETTINGS) {
                option(canvas, 1510, "Проскальзывание", "Авто");
                option(canvas, 1642, "Маршрут", "Лучший");
                option(canvas, 1774, "MEV-защита", "Вкл");
                return;
            }
            if (sheet == SHEET_SWAP_TOKEN) {
                rect(canvas, 88, 1450, 904, 104, Color.rgb(35, 36, 39), 52);
                text(canvas, "Поиск токена или сети", 150, 1516, 38, MUTED, Paint.Align.LEFT, true);
                List<MarketCoin> coins = visibleCoins();
                for (int i = 0; i < Math.min(5, coins.size()); i++) tokenOption(canvas, 1642 + i * 132, coins.get(i));
                return;
            }
            String[] labels = sheet == SHEET_MARKET_NETWORK ? new String[]{"Все сети", "Ethereum", "BNB Smart Chain", "Solana"} :
                    sheet == SHEET_PERPS_PROVIDER ? new String[]{"Все поставщики", "Binance", "Hyperliquid", "dYdX"} :
                    sheet == SHEET_MARKET_SORT ? new String[]{"Рыночная капитализация", "Top Gainers", "Объем (24 ч.)"} :
                    sheet == SHEET_PERPS_SORT ? new String[]{"Объем (24 ч.)", "Рост цены", "Открытый интерес"} :
                    sheet == SHEET_MARKET_RANGE ? new String[]{"24h", "7d", "30d"} :
                    new String[]{"Hot tokens", "Избранное", "Top Gainers", "RWA", "Meme", "DeFi"};
            for (int i = 0; i < labels.length; i++) option(canvas, 1510 + i * 132, labels[i], selectedMark(sheet, i) ? "✓" : "");
        }

        boolean selectedMark(int currentSheet, int index) {
            if (currentSheet == SHEET_MARKET_SORT || currentSheet == SHEET_PERPS_SORT) return sortMode == index;
            if (currentSheet == SHEET_MARKET_RANGE) return timeRange == index;
            if (currentSheet == SHEET_MARKET_CATEGORY) return marketFilter == index;
            if (currentSheet == SHEET_MARKET_NETWORK || currentSheet == SHEET_PERPS_PROVIDER) return networkFilter == index;
            return index == 0;
        }

        void drawActionFlowSheet(Canvas canvas, int currentSheet) {
            if (currentSheet == SHEET_SEND || currentSheet == SHEET_RECEIVE) {
                text(canvas, currentSheet == SHEET_SEND ? "Выберите актив" : "Выберите актив для получения", 88, 1495, 38, MUTED, Paint.Align.LEFT, true);
                List<MarketCoin> coins = visibleCoins();
                for (int i = 0; i < Math.min(4, coins.size()); i++) tokenOption(canvas, 1600 + i * 132, coins.get(i));
                return;
            }
            if (currentSheet == SHEET_BUY) {
                option(canvas, 1510, "MoonPay", "USD");
                option(canvas, 1642, "Mercuryo", "Card");
                option(canvas, 1774, "Ramp Network", "Best rate");
                option(canvas, 1906, "Transak", "Bank card");
                return;
            }
            if (currentSheet == SHEET_FUND) {
                option(canvas, 1510, "Пополнить с кошелька", "Crypto");
                option(canvas, 1642, "Купить с карты", "Fiat");
                option(canvas, 1774, "Получить на адрес", "QR");
                return;
            }
            option(canvas, 1510, "USDT Perps", "Доступно");
            option(canvas, 1642, "USDC Perps", "Доступно");
            option(canvas, 1774, "Перевести с кошелька", "0.00");
        }

        void option(Canvas canvas, float y, String left, String right) {
            text(canvas, left, 88, y, 42, TEXT, Paint.Align.LEFT, true);
            if (right.length() > 0) text(canvas, right, 992, y, 38, GREEN, Paint.Align.RIGHT, true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.rgb(52, 53, 56));
            canvas.drawLine(88, y + 45, 992, y + 45, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        void tokenOption(Canvas canvas, float y, MarketCoin coin) {
            rect(canvas, 88, y - 46, 76, 76, Color.rgb(55, 56, 58), 38);
            text(canvas, coin.symbol.length() > 0 ? coin.symbol.substring(0, 1) : "?", 126, y + 4, 34, TEXT, Paint.Align.CENTER, true);
            text(canvas, tokenSheetLabel(coin), 190, y - 10, 40, TEXT, Paint.Align.LEFT, true);
            text(canvas, formatPrice(coin.price), 992, y - 10, 36, MUTED, Paint.Align.RIGHT, true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.rgb(52, 53, 56));
            canvas.drawLine(190, y + 45, 992, y + 45, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        String tokenSheetLabel(MarketCoin coin) {
            if (coin == null) return "";
            if (coin.name == null || coin.name.length() == 0 || coin.name.equalsIgnoreCase(coin.symbol)) return coin.symbol;
            return coin.symbol + "  " + coin.name;
        }

        void chip(Canvas canvas, float x, float y, String label, boolean selected) {
            rect(canvas, x, y, 112, 70, selected ? Color.rgb(92, 96, 94) : PANEL, 35);
            text(canvas, label, x + 56, y + 47, 32, TEXT, Paint.Align.CENTER, true);
        }

        void drawCoinRow(Canvas canvas, MarketCoin coin, float y, boolean large) {
            rect(canvas, 44, y - 70, 88, 88, Color.rgb(55, 56, 58), 44);
            text(canvas, coin.symbol.length() > 0 ? coin.symbol.substring(0, 1) : "?", 88, y - 12, 42, TEXT, Paint.Align.CENTER, true);
            text(canvas, coin.symbol, 160, y - 25, large ? 48 : 42, TEXT, Paint.Align.LEFT, true);
            text(canvas, coin.name, 160, y + 28, 32, MUTED, Paint.Align.LEFT, true);
            text(canvas, formatPrice(coin.price), 1035, y - 25, 42, TEXT, Paint.Align.RIGHT, true);
            text(canvas, formatPct(coin.change24h), 1035, y + 28, 32, coin.change24h < 0 ? RED : GREEN, Paint.Align.RIGHT, true);
        }

        void drawSwapState(Canvas canvas) {
            if (swapReversed) {
                text(canvas, "↓", 540, 595, 58, GREEN, Paint.Align.CENTER, true);
            }
            if (selectedSwapToken != null) {
                rect(canvas, 646, 592, 346, 99, Color.rgb(42, 88, 58), 52);
                text(canvas, selectedSwapToken.symbol, 819, 654, 42, GREEN, Paint.Align.CENTER, true);
                text(canvas, formatPrice(selectedSwapToken.price), 88, 770, 32, MUTED, Paint.Align.LEFT, true);
            }
        }

        void drawHomeTop(Canvas canvas) {
            drawBmp(canvas, "assets/ui/top-settings.png", 11, 102, 132, 132);
            drawBmp(canvas, "assets/ui/top-search-pill.png", 143, 102, 805, 132);
            drawBmp(canvas, "assets/ui/top-scan.png", 948, 102, 132, 132);
            drawBmp(canvas, "assets/ui/wallet-chip.png", 318, 271, 422, 132);
            drawBmp(canvas, "assets/ui/copy-button.png", 740, 271, 132, 132);
            drawBmp(canvas, "assets/ui/qa-send.png", 144, 473, 198, 214);
            drawBmp(canvas, "assets/ui/qa-receive.png", 342, 473, 198, 214);
            drawBmp(canvas, "assets/ui/qa-swap.png", 540, 473, 198, 214);
            drawBmp(canvas, "assets/ui/qa-buy.png", 738, 473, 198, 214);
        }

        void drawHome(Canvas canvas) {
            drawStatus(canvas);
            canvas.save();
            canvas.clipRect(0, 102, 1080, 2020);
            canvas.translate(0, -scrollY);
            drawHomeTop(canvas);
            if (assetTab == 1) {
                drawBmp(canvas, "assets/ui/asset-tabs-favorites.png", 0, 731, 1080, 126);
                drawBmp(canvas, "assets/ui/favorite-empty-panel.png", 0, 857, 1080, 894);
                drawBmp(canvas, "assets/ui/favorites-perps-top.png", 0, 1751, 1080, 269);
                drawBmp(canvas, "assets/ui/home-earn-history.png", 0, 2020, 1080, 1092);
            } else if (assetTab == 2) {
                drawBmp(canvas, "assets/ui/asset-tabs-nft.png", 0, 731, 1080, 126);
                drawBmp(canvas, "assets/ui/nft-home-body.png", 0, 857, 1080, 1163);
            } else {
                drawBmp(canvas, "assets/ui/asset-tabs-crypto.png", 0, 731, 1080, 126);
                drawBmp(canvas, "assets/ui/home-empty-card.png", 44, 891, 992, 908);
                drawBmp(canvas, "assets/ui/home-perps-top.png", 0, 1852, 1080, 168);
                drawBmp(canvas, "assets/ui/home-earn-history.png", 0, 2020, 1080, 1092);
            }
            canvas.restore();
            rect(canvas, 0, 1910, 1080, 360, BG, 0);
            drawBmp(canvas, assetTab == 1 ? "assets/ui/bottom-nav-favorites.png" : "assets/ui/bottom-nav-home.png", 0, 2020, 1080, 250);
            drawSystemNav(canvas);
        }

        void drawHistory(Canvas canvas) {
            drawStandalonePage(canvas, "assets/ui/transaction-history-page-content.png");
        }

        void drawManage(Canvas canvas) {
            drawStandalonePage(canvas, "assets/ui/asset-manage-page-content.png");
            drawManageState(canvas);
        }

        void drawManageState(Canvas canvas) {
            circleSelect(canvas, 358, 1021, manageLayout == 0);
            circleSelect(canvas, 540, 1021, manageLayout == 1);
            circleSelect(canvas, 722, 1021, manageLayout == 2);
            toggle(canvas, 805, 1535, hideSmallAssets);
            toggle(canvas, 805, 1710, hideNft);
            toggle(canvas, 805, 1885, hidePredictions);
            toggle(canvas, 805, 2060, hidePerps);
        }

        void circleSelect(Canvas canvas, float cx, float cy, boolean selected) {
            if (!selected) return;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(7);
            paint.setColor(GREEN);
            canvas.drawCircle(cx, cy, 59, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        void toggle(Canvas canvas, float x, float y, boolean on) {
            rect(canvas, x - 35, y - 20, 205, 128, Color.rgb(24, 24, 25), 0);
            rect(canvas, x, y, 143, 78, on ? GREEN : Color.rgb(116, 117, 122), 39);
            rect(canvas, on ? x + 73 : x + 12, y + 11, 56, 56, Color.WHITE, 28);
        }

        static class MarketCoin {
            String id;
            String symbol;
            String name;
            double price;
            double change24h;
            double volume;
            double marketCap;
            final ArrayList<Double> sparkline = new ArrayList<Double>();
        }
    }
}

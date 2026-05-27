package com.wallet.crypto.trustvisual;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
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
        static final int NAV_TOP = 2110;
        static final int NAV_BOTTOM = 2360;
        static final int MARKETS_LIST_TOP = 1185;
        static final int MARKETS_LIST_BOTTOM = 2030;
        static final int MARKETS_FIRST_ROW_Y = 1288;
        static final int MARKETS_ROW_HEIGHT = 182;
        static final int PERPS_LIST_TOP = 1035;
        static final int PERPS_LIST_BOTTOM = 2030;
        static final int PERPS_FIRST_ROW_Y = 1138;
        static final int PERPS_ROW_HEIGHT = 180;
        static final int BG = Color.rgb(27, 27, 28);
        static final int PANEL = Color.rgb(39, 40, 44);
        static final int GREEN = Color.rgb(39, 214, 139);
        static final int RED = Color.rgb(255, 86, 98);
        static final int TEXT = Color.rgb(244, 244, 247);
        static final int MUTED = Color.rgb(170, 170, 178);
        static final String COINGECKO_API_KEY = System.getProperty("trustvisual.cg.key", "");
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
        static final int SHEET_PERPS_SETTINGS = 15;
        static final int SHEET_SWAP_DISABLED = 16;
        static final int SHEET_WALLET_SELECTOR = 17;
        static final int SHEET_COPY_ADDRESS = 18;
        static final int SHEET_SCAN = 19;
        static final int SHEET_TRADE_MENU = 20;

        static final int MODAL_NONE = 0;
        static final int MODAL_SEARCH = 1;
        static final int MODAL_TOKEN_DETAIL = 2;
        static final int MODAL_DISCOVER_DETAIL = 3;
        static final int MODAL_PERPS_DETAIL = 4;

        final Activity activity;
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        final Map<String, Bitmap> bitmaps = new HashMap<String, Bitmap>();
        final Map<String, MarketCoin> marketCoins = new HashMap<String, MarketCoin>();
        final ArrayList<MarketCoin> marketList = new ArrayList<MarketCoin>();
        final Handler handler = new Handler(Looper.getMainLooper());
        float scale = 1f;
        float offsetX = 0f;
        float offsetY = 0f;
        int page = 0; // home, popular, swap, rewards, discover, perps
        int assetTab = 0; // crypto, favorites, nft
        int sheet = SHEET_NONE;
        int modal = MODAL_NONE;
        int marketFilter = 0;
        int networkFilter = 0;
        int sortMode = 0;
        int timeRange = 0;
        int manageLayout = 2;
        int slippageMode = 0;
        int routeMode = 0;
        int rewardsTab = 0;
        boolean hideSmallAssets = true;
        boolean hideNft = false;
        boolean hidePredictions = false;
        boolean hidePerps = false;
        boolean swapReversed = false;
        boolean mevProtection = true;
        boolean history = false;
        boolean perpsHistory = false;
        boolean manage = false;
        int searchContextPage = 0;
        float scrollY = 0f;
        float sheetScrollY = 0f;
        float modalScrollY = 0f;
        float downX = 0f;
        float downY = 0f;
        float lastY = 0f;
        boolean dragging = false;
        boolean draggingSheet = false;
        boolean draggingModal = false;
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

            if (perpsHistory) {
                drawPerpsHistory(canvas);
            } else if (history) {
                drawHistory(canvas);
            } else if (manage) {
                drawManage(canvas);
            } else if (page == 1) {
                drawPopularTop(canvas);
                drawLiveMarkets(canvas);
                drawMarketsFilters(canvas);
                drawModernBottomNav(canvas, 1);
            } else if (page == 2) {
                drawSwap(canvas);
            } else if (page == 3) {
                drawRewards(canvas);
            } else if (page == 5) {
                drawPerps(canvas);
            } else if (page == 4) {
                drawDiscover(canvas);
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
                draggingModal = isScrollableModal(y);
                draggingSheet = !draggingModal && isScrollableSheet(y);
                dragging = !draggingModal && !draggingSheet && isScrollableSurface(y);
                moved = false;
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (dragging || draggingSheet || draggingModal) {
                    float dy = lastY - y;
                    if (Math.abs(y - downY) > 14 || Math.abs(x - downX) > 14) moved = true;
                    if (draggingSheet) sheetScrollY = clamp(sheetScrollY + dy, 0, maxSheetScroll());
                    else if (draggingModal) modalScrollY = clamp(modalScrollY + dy, 0, maxModalScroll());
                    else scrollY = clamp(scrollY + dy, 0, maxScroll());
                    lastY = y;
                    invalidate();
                }
                return true;
            }

            if (event.getAction() != MotionEvent.ACTION_UP) return true;

            if (moved) {
                dragging = false;
                draggingSheet = false;
                draggingModal = false;
                moved = false;
                return true;
            }
            dragging = false;
            draggingSheet = false;
            draggingModal = false;

            if (modal != MODAL_NONE) return handleModalTouch(x, y);
            if (sheet != SHEET_NONE) return handleSheetTouch(x, y);

            if (history || perpsHistory || manage) {
                if (manage) {
                    if (hit(x, y, 78, 422, 342, 672)) return setManageLayout(0);
                    if (hit(x, y, 408, 422, 672, 672)) return setManageLayout(1);
                    if (hit(x, y, 738, 422, 1002, 672)) return setManageLayout(2);
                    if (hit(x, y, 700, 820, 1030, 1010)) { hideSmallAssets = !hideSmallAssets; invalidate(); return true; }
                    if (hit(x, y, 700, 995, 1030, 1185)) { hideNft = !hideNft; invalidate(); return true; }
                    if (hit(x, y, 700, 1170, 1030, 1360)) { hidePredictions = !hidePredictions; invalidate(); return true; }
                    if (hit(x, y, 700, 1345, 1030, 1535)) { hidePerps = !hidePerps; invalidate(); return true; }
                    if (hit(x, y, 88, 1660, 992, 1772)) {
                        manage = false;
                        invalidate();
                        return true;
                    }
                }
                if (y < 260 || (manage && x > 905 && y > 410 && y < 610)) {
                    perpsHistory = false;
                    history = false;
                    manage = false;
                    invalidate();
                }
                return true;
            }

            if (hit(x, y, 44, NAV_TOP, 230, NAV_BOTTOM)) return openPage(0);
            if (hit(x, y, 230, NAV_TOP, 416, NAV_BOTTOM)) return openPage(1);
            if (hit(x, y, 416, NAV_TOP - 35, 664, NAV_BOTTOM)) return openSheet(SHEET_TRADE_MENU);
            if (hit(x, y, 664, NAV_TOP, 850, NAV_BOTTOM)) return openPage(3);
            if (hit(x, y, 850, NAV_TOP, 1036, NAV_BOTTOM)) return openPage(4);

            if (page == 0) {
                float cy = y + scrollY;
                y = cy;
                if (hit(x, y, 11, 102, 143, 234)) return openSafeSheet("Настройки");
                if (hit(x, y, 143, 102, 948, 234)) return openSearch();
                if (hit(x, y, 948, 102, 1080, 234)) return openSheet(SHEET_SCAN);
                if (hit(x, y, 318, 285, 760, 420)) return openSheet(SHEET_WALLET_SELECTOR);
                if (hit(x, y, 760, 285, 900, 420)) return openSheet(SHEET_COPY_ADDRESS);
                if (hit(x, y, 80, 690, 260, 900)) return openSheet(SHEET_SEND);
                if (hit(x, y, 280, 690, 460, 900)) return openSheet(SHEET_RECEIVE);
                if (hit(x, y, 500, 690, 660, 900)) return openPage(2);
                if (hit(x, y, 735, 690, 915, 900)) return openSheet(SHEET_BUY);
                if (hit(x, y, 44, 1160, 355, 1315)) return setAssetTab(0);
                if (hit(x, y, 355, 1160, 650, 1315)) return setAssetTab(1);
                if (hit(x, y, 650, 1160, 790, 1315)) return setAssetTab(2);
                if (hit(x, y, 790, 1160, 910, 1315)) {
                    history = true;
                    invalidate();
                    return true;
                }
                if (hit(x, y, 910, 1160, 1036, 1315)) {
                    manage = true;
                    invalidate();
                    return true;
                }
                if (assetTab == 1 && hit(x, y, 215, 1770, 865, 1866)) {
                    manage = true;
                    invalidate();
                    return true;
                }
                if (assetTab == 2 && hit(x, y, 190, 1850, 890, 1952)) {
                    manage = true;
                    invalidate();
                    return true;
                }
                if (hit(x, y, 44, 1690, 1036, 1860)) return openPage(5);
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
                if (hit(x, y, 44, MARKETS_LIST_TOP, 1036, MARKETS_LIST_BOTTOM)) return openCoinListAt(y + scrollY, MARKETS_FIRST_ROW_Y, 3, MARKETS_ROW_HEIGHT);
            }

            if (page == 2) {
                if (hit(x, y, 44, 110, 150, 240)) return openPage(0);
                if (hit(x, y, 930, 110, 1036, 240)) return openSheet(SHEET_SWAP_SETTINGS);
                if (hit(x, y, 646, 592, 992, 710)) return openSheet(SHEET_SWAP_TOKEN);
                if (hit(x, y, 496, 530, 584, 625)) { swapReversed = !swapReversed; invalidate(); return true; }
                if (hit(x, y, 705, 278, 992, 416)) return openSheet(SHEET_FUND);
                if (hit(x, y, 44, 2072, 1036, 2204)) return openSheet(SHEET_SWAP_DISABLED);
            }

            if (page == 3) {
                float cy = y + scrollY;
                if (hit(x, cy, 44, 1160, 300, 1285)) { rewardsTab = 0; invalidate(); return true; }
                if (hit(x, cy, 315, 1160, 620, 1285)) { rewardsTab = 1; invalidate(); return true; }
                if (hit(x, cy, 44, 1885, 1036, 2040)) return openDiscover("Trust Alpha");
            }

            if (page == 5) {
                if (hit(x, y, 44, 102, 154, 234)) { perpsHistory = true; invalidate(); return true; }
                if (hit(x, y, 926, 102, 1036, 234)) return openSheet(SHEET_PERPS_SETTINGS);
                if (hit(x, y, 88, 650, 992, 762)) return openSheet(SHEET_DEPOSIT);
                if (hit(x, y, 44, 790, 1036, 922)) return openSearch();
                if (hit(x, y, 44, 950, 514, 1040)) return openSheet(SHEET_PERPS_PROVIDER);
                if (hit(x, y, 610, 950, 1036, 1040)) return openSheet(SHEET_PERPS_SORT);
                if (hit(x, y, 44, 825, 144, 925)) return setMarketFilter(1);
                if (hit(x, y, 160, 825, 370, 925)) return setMarketFilter(0);
                if (hit(x, y, 390, 825, 560, 925)) return setMarketFilter(2);
                if (hit(x, y, 580, 825, 790, 925)) return setMarketFilter(3);
                if (hit(x, y, 810, 825, 1036, 925)) return setMarketFilter(4);
                if (hit(x, y, 44, PERPS_LIST_TOP, 1036, PERPS_LIST_BOTTOM)) return openCoinListAt(y + scrollY, PERPS_FIRST_ROW_Y, 0, PERPS_ROW_HEIGHT);
            }

            if (page == 4) {
                y += scrollY;
                if (hit(x, y, 44, 248, 1036, 372)) return openSearch();
                if (hit(x, y, 44, 520, 529, 800)) return openDiscover("Стейкинг");
                if (hit(x, y, 44, 865, 1036, 995)) return openDiscover("Trust Wallet website");
                if (hit(x, y, 44, 995, 1036, 1125)) return openDiscover("Центр поддержки");
                if (hit(x, y, 44, 1125, 1036, 1255)) return openDiscover("Use dApp securely");
                if (hit(x, y, 44, 1255, 1036, 1385)) return openDiscover("What is DeFi?");
                if (hit(x, y, 44, 1385, 1036, 1515)) return openDiscover("What is Token Approval?");
                if (hit(x, y, 44, 1515, 1036, 1645)) return openDiscover("What is a seed phrase?");
                if (hit(x, y, 44, 1645, 1036, 1775)) return openDiscover("Network fees explained");
            }
            return true;
        }

        boolean isScrollableSurface(float y) {
            if (modal != MODAL_NONE || sheet != SHEET_NONE || history || perpsHistory || manage) return false;
            if (y >= NAV_TOP - 35) return false;
            return page == 0 || page == 1 || page == 3 || page == 4 || page == 5;
        }

        boolean isScrollableSheet(float y) {
            return sheet == SHEET_SWAP_TOKEN && y >= 1550 && y < NAV_BOTTOM;
        }

        boolean isScrollableModal(float y) {
            return modal == MODAL_SEARCH && y >= 390 && y < NAV_BOTTOM;
        }

        float maxScroll() {
            if (page == 0) return assetTab == 0 ? 1630f : assetTab == 1 ? 1070f : 620f;
            if (page == 1) return Math.max(0f, Math.max(0, visibleCoins().size() - 3) * MARKETS_ROW_HEIGHT - (MARKETS_LIST_BOTTOM - MARKETS_FIRST_ROW_Y));
            if (page == 3) return 520f;
            if (page == 5) return Math.max(0f, visibleCoins().size() * PERPS_ROW_HEIGHT - (PERPS_LIST_BOTTOM - PERPS_FIRST_ROW_Y));
            if (page == 4) return 220f;
            return 0f;
        }

        float maxSheetScroll() {
            if (sheet == SHEET_SWAP_TOKEN) return Math.max(0f, visibleCoins().size() * 118f - 560f);
            return 0f;
        }

        float maxModalScroll() {
            if (modal == MODAL_SEARCH && searchContextPage != 4) return Math.max(0f, visibleCoins().size() * 150f - 1540f);
            return 0f;
        }

        float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(max, value));
        }

        boolean openPage(int nextPage) {
            page = nextPage;
            scrollY = 0f;
            history = false;
            perpsHistory = false;
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
            sheetScrollY = 0f;
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
            searchContextPage = page;
            modal = MODAL_SEARCH;
            modalScrollY = 0f;
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
            modal = page == 5 ? MODAL_PERPS_DETAIL : MODAL_TOKEN_DETAIL;
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
                if (searchContextPage == 4) {
                    int dappIndex = (int) ((y - 500) / 135);
                    if (dappIndex == 0) return openDiscover("Trust Wallet website");
                    if (dappIndex == 1) return openDiscover("Центр поддержки");
                    if (dappIndex == 2) return openDiscover("Staking");
                    if (dappIndex == 3) return openDiscover("What is Token Approval?");
                    if (dappIndex == 4) return openDiscover("Network fees explained");
                    return true;
                }
                List<MarketCoin> coins = visibleCoins();
                int index = (int) ((y + modalScrollY - 560) / 150);
                if (index >= 0 && index < coins.size()) return openCoin(coins.get(index));
            } else if (modal == MODAL_TOKEN_DETAIL || modal == MODAL_PERPS_DETAIL) {
                if (hit(x, y, 88, 1190, 200, 1260)) timeRange = 0;
                else if (hit(x, y, 230, 1190, 342, 1260)) timeRange = 1;
                else if (hit(x, y, 360, 1190, 472, 1260)) timeRange = 2;
                invalidate();
                return true;
            } else if (modal == MODAL_DISCOVER_DETAIL) {
                if (hit(x, y, 88, 930, 992, 1046) || hit(x, y, 88, 1085, 992, 1201)) {
                    modal = MODAL_NONE;
                    invalidate();
                    return true;
                }
            }
            return true;
        }

        boolean handleSheetTouch(float x, float y) {
            if (sheet == SHEET_TRADE_MENU) {
                if (y < 1428 || hit(x, y, 930, 1450, 1036, 1560)) {
                    sheet = SHEET_NONE;
                    invalidate();
                    return true;
                }
                if (hit(x, y, 44, 1550, 1036, 1710)) return openPage(2);
                if (hit(x, y, 44, 1710, 1036, 1888)) return openPage(5);
                if (hit(x, y, 44, 1888, 1036, 2058)) {
                    sheet = SHEET_NONE;
                    return openDiscover("Прогнозы");
                }
                return true;
            }
            if (y < 1260 || hit(x, y, 930, 1280, 1036, 1400)) {
                sheet = SHEET_NONE;
                invalidate();
                return true;
            }
            if (sheet == SHEET_TRADE_MENU) {
                if (hit(x, y, 44, 1460, 1036, 1605)) return openPage(2);
                if (hit(x, y, 44, 1635, 1036, 1805)) return openPage(5);
                if (hit(x, y, 44, 1830, 1036, 1995)) {
                    sheet = SHEET_NONE;
                    return openDiscover("Прогнозы");
                }
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
            } else if (sheet == SHEET_SWAP_SETTINGS) {
                if (option == 0) slippageMode = (slippageMode + 1) % 3;
                else if (option == 1) routeMode = (routeMode + 1) % 2;
                else if (option == 2) mevProtection = !mevProtection;
                invalidate();
                return true;
            } else if (sheet == SHEET_WALLET_SELECTOR || sheet == SHEET_COPY_ADDRESS || sheet == SHEET_SCAN) {
                sheet = SHEET_NONE;
                invalidate();
                return true;
            } else if (sheet == SHEET_SWAP_TOKEN) {
                List<MarketCoin> coins = visibleCoins();
                int tokenOption = (int) ((y + sheetScrollY - 1642) / 118);
                if (tokenOption >= 0 && tokenOption < coins.size()) selectedSwapToken = coins.get(tokenOption);
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
            rect(canvas, 0, 0, 1080, 102, BG, 0);
            rect(canvas, 58, 28, 160, 56, Color.rgb(45, 210, 98), 28);
            text(canvas, "21:57", 138, 67, 34, Color.WHITE, Paint.Align.CENTER, true);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawRect(860, 62, 868, 78, paint);
            canvas.drawRect(874, 54, 882, 78, paint);
            canvas.drawRect(888, 44, 896, 78, paint);
            canvas.drawRect(902, 34, 910, 78, paint);
            text(canvas, "LTE", 950, 70, 28, Color.WHITE, Paint.Align.CENTER, true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.WHITE);
            canvas.drawRoundRect(new RectF(990, 34, 1048, 70), 8, 8, paint);
            canvas.drawRect(1048, 45, 1055, 59, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(255, 216, 36));
            canvas.drawRoundRect(new RectF(996, 40, 1034, 64), 5, 5, paint);
        }

        void drawSystemNav(Canvas canvas) {
            rect(canvas, 345, 2320, 390, 12, Color.WHITE, 6);
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
            if (COINGECKO_API_KEY.length() > 0) {
                connection.setRequestProperty("x-cg-demo-api-key", COINGECKO_API_KEY);
            }
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
            canvas.clipRect(0, MARKETS_LIST_TOP, 1080, MARKETS_LIST_BOTTOM);
            rect(canvas, 0, 1135, 1080, 895, BG, 0);
            List<MarketCoin> coins = visibleCoins();
            float y = MARKETS_FIRST_ROW_Y - scrollY;
            for (int i = 3; i < Math.min(coins.size(), 18); i++) {
                if (y - 84 >= MARKETS_LIST_TOP && y + 52 <= MARKETS_LIST_BOTTOM) liveMarketRow(canvas, coins.get(i), y);
                y += MARKETS_ROW_HEIGHT;
            }
            canvas.restore();
        }

        void drawMarketsFilters(Canvas canvas) {
            rect(canvas, 0, 825, 1080, MARKETS_LIST_TOP - 825, BG, 0);
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
            rect(canvas, 0, 790, 1080, PERPS_LIST_TOP - 790, BG, 0);
            filterPill(canvas, 44, 825, 100, 100, "☆", marketFilter == 1);
            filterPill(canvas, 160, 825, 210, 100, "Popular", marketFilter == 0);
            filterPill(canvas, 390, 825, 170, 100, "New", marketFilter == 2);
            filterPill(canvas, 580, 825, 210, 100, "Crypto", marketFilter == 3);
            filterPill(canvas, 810, 825, 226, 100, "Stocks", marketFilter == 4);
            filterPill(canvas, 44, 950, 470, 90, networkFilter == 0 ? "Все поставщики" : providerLabel());
            filterPill(canvas, 610, 950, 426, 90, perpsSortLabel());
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
            canvas.clipRect(0, PERPS_LIST_TOP, 1080, PERPS_LIST_BOTTOM);
            rect(canvas, 0, PERPS_LIST_TOP, 1080, PERPS_LIST_BOTTOM - PERPS_LIST_TOP, BG, 0);
            List<MarketCoin> coins = visibleCoins();
            float y = PERPS_FIRST_ROW_Y - scrollY;
            for (int i = 0; i < Math.min(12, coins.size()); i++) {
                if (y - 84 >= PERPS_LIST_TOP && y + 52 <= PERPS_LIST_BOTTOM) livePerpsRow(canvas, coins.get(i), y);
                y += PERPS_ROW_HEIGHT;
            }
            canvas.restore();
        }

        void liveMarketCard(Canvas canvas, MarketCoin coin, float x, float y) {
            if (coin == null) return;
            rect(canvas, x, y, 330, 380, PANEL, 42);
            text(canvas, shortName(coin), x + 50, y + 78, 34, MUTED, Paint.Align.LEFT, true);
            drawTokenIcon(canvas, coin, x + 230, y + 38, 58);
            text(canvas, formatPrice(coin.price), x + 50, y + 160, 50, TEXT, Paint.Align.LEFT, true);
            text(canvas, formatPct(coin.change24h), x + 50, y + 225, 38, coin.change24h < 0 ? RED : GREEN, Paint.Align.LEFT, true);
            sparkline(canvas, coin, x + 56, y + 270, 245, 76);
        }

        void liveMarketRow(Canvas canvas, MarketCoin coin, float y) {
            if (coin == null) return;
            rect(canvas, 44, y - 112, 992, 152, BG, 0);
            drawTokenIcon(canvas, coin, 44, y - 84, 88);
            text(canvas, coin.symbol, 160, y - 22, 46, TEXT, Paint.Align.LEFT, true);
            text(canvas, "$" + compact(coin.marketCap) + " MCap · $" + compact(coin.volume) + " Vol", 160, y + 32, 31, MUTED, Paint.Align.LEFT, true);
            text(canvas, formatPrice(coin.price), 1035, y - 6, 47, TEXT, Paint.Align.RIGHT, true);
            text(canvas, formatPct(coin.change24h), 1035, y + 52, 34, coin.change24h < 0 ? RED : GREEN, Paint.Align.RIGHT, true);
            sparkline(canvas, coin, 760, y + 1, 150, 58);
        }

        void livePerpsRow(Canvas canvas, MarketCoin coin, float y) {
            if (coin == null) return;
            rect(canvas, 44, y - 112, 992, 152, BG, 0);
            drawTokenIcon(canvas, coin, 44, y - 84, 88);
            rect(canvas, 132, y - 112, 430, 230, BG, 0);
            text(canvas, coin.symbol, 160, y - 25, 46, TEXT, Paint.Align.LEFT, true);
            text(canvas, perpsSubtitle(coin), 160, y + 30, 31, MUTED, Paint.Align.LEFT, true);
            rect(canvas, 535, y - 88, 502, 168, BG, 0);
            text(canvas, formatPrice(coin.price), 1035, y - 10, 47, TEXT, Paint.Align.RIGHT, true);
            text(canvas, formatPct(coin.change24h), 1035, y + 50, 34, coin.change24h < 0 ? RED : GREEN, Paint.Align.RIGHT, true);
            sparkline(canvas, coin, 560, y - 26, 150, 58);
        }

        String perpsSubtitle(MarketCoin coin) {
            return "$" + compact(coin.volume) + " Vol / 20x";
        }

        void drawTokenIcon(Canvas canvas, MarketCoin coin, float x, float y, float size) {
            String asset = tokenIconAsset(coin);
            if (asset.length() > 0) {
                drawBmp(canvas, asset, x, y, size, size);
                return;
            }
            if (drawFallbackTokenIcon(canvas, coin, x, y, size)) return;
            rect(canvas, x, y, size, size, Color.rgb(55, 56, 58), size * 0.5f);
            text(canvas, coin.symbol.length() > 0 ? coin.symbol.substring(0, 1) : "?", x + size * 0.5f, y + size * 0.64f, size * 0.48f, TEXT, Paint.Align.CENTER, true);
        }

        boolean drawFallbackTokenIcon(Canvas canvas, MarketCoin coin, float x, float y, float size) {
            if (coin == null || coin.symbol == null) return false;
            String s = coin.symbol.toUpperCase(Locale.US);
            float cx = x + size * 0.5f;
            float cy = y + size * 0.5f;
            if ("USDT".equals(s)) {
                rect(canvas, x, y, size, size, Color.rgb(38, 161, 123), size * 0.5f);
                text(canvas, "T", cx, y + size * 0.67f, size * 0.58f, Color.WHITE, Paint.Align.CENTER, true);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(size * 0.06f);
                paint.setColor(Color.WHITE);
                canvas.drawLine(x + size * 0.25f, y + size * 0.35f, x + size * 0.75f, y + size * 0.35f, paint);
                paint.setStyle(Paint.Style.FILL);
                return true;
            }
            if ("USDC".equals(s)) {
                rect(canvas, x, y, size, size, Color.rgb(39, 117, 202), size * 0.5f);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(size * 0.06f);
                paint.setColor(Color.WHITE);
                canvas.drawCircle(cx, cy, size * 0.32f, paint);
                paint.setStyle(Paint.Style.FILL);
                text(canvas, "$", cx, y + size * 0.68f, size * 0.54f, Color.WHITE, Paint.Align.CENTER, true);
                return true;
            }
            if ("XRP".equals(s)) {
                rect(canvas, x, y, size, size, Color.rgb(45, 47, 51), size * 0.5f);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(size * 0.08f);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setColor(Color.WHITE);
                canvas.drawLine(x + size * 0.30f, y + size * 0.32f, x + size * 0.46f, y + size * 0.48f, paint);
                canvas.drawLine(x + size * 0.70f, y + size * 0.32f, x + size * 0.54f, y + size * 0.48f, paint);
                canvas.drawLine(x + size * 0.30f, y + size * 0.68f, x + size * 0.46f, y + size * 0.52f, paint);
                canvas.drawLine(x + size * 0.70f, y + size * 0.68f, x + size * 0.54f, y + size * 0.52f, paint);
                paint.setStrokeCap(Paint.Cap.BUTT);
                paint.setStyle(Paint.Style.FILL);
                return true;
            }
            if ("SOL".equals(s)) {
                rect(canvas, x, y, size, size, Color.rgb(24, 24, 30), size * 0.5f);
                drawSolBar(canvas, x, y, size, 0.30f, Color.rgb(20, 241, 149));
                drawSolBar(canvas, x, y, size, 0.47f, Color.rgb(140, 82, 255));
                drawSolBar(canvas, x, y, size, 0.64f, Color.rgb(20, 241, 149));
                return true;
            }
            if ("DOGE".equals(s)) {
                rect(canvas, x, y, size, size, Color.rgb(198, 146, 37), size * 0.5f);
                text(canvas, "Ð", cx, y + size * 0.66f, size * 0.62f, Color.WHITE, Paint.Align.CENTER, true);
                return true;
            }
            if ("TRX".equals(s)) {
                rect(canvas, x, y, size, size, Color.rgb(214, 33, 56), size * 0.5f);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(size * 0.055f);
                paint.setColor(Color.WHITE);
                Path p = new Path();
                p.moveTo(cx, y + size * 0.24f);
                p.lineTo(x + size * 0.75f, y + size * 0.72f);
                p.lineTo(x + size * 0.25f, y + size * 0.56f);
                p.close();
                canvas.drawPath(p, paint);
                paint.setStyle(Paint.Style.FILL);
                return true;
            }
            if ("LINK".equals(s)) {
                rect(canvas, x, y, size, size, Color.rgb(45, 91, 255), size * 0.5f);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(size * 0.08f);
                paint.setColor(Color.WHITE);
                Path p = new Path();
                for (int i = 0; i < 6; i++) {
                    double a = -Math.PI / 2 + i * Math.PI / 3;
                    float px = cx + (float) Math.cos(a) * size * 0.27f;
                    float py = cy + (float) Math.sin(a) * size * 0.27f;
                    if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
                }
                p.close();
                canvas.drawPath(p, paint);
                paint.setStyle(Paint.Style.FILL);
                return true;
            }
            if ("UNI".equals(s) || "AAVE".equals(s) || "PEPE".equals(s) || "HYPE".equals(s)) {
                int color = "UNI".equals(s) ? Color.rgb(255, 50, 150) : "AAVE".equals(s) ? Color.rgb(176, 82, 255) : "PEPE".equals(s) ? Color.rgb(79, 176, 91) : Color.rgb(40, 210, 190);
                rect(canvas, x, y, size, size, color, size * 0.5f);
                text(canvas, s.substring(0, 1), cx, y + size * 0.66f, size * 0.56f, Color.WHITE, Paint.Align.CENTER, true);
                return true;
            }
            return false;
        }

        void drawSolBar(Canvas canvas, float x, float y, float size, float cyRatio, int color) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            Path p = new Path();
            float h = size * 0.10f;
            float yy = y + size * cyRatio;
            p.moveTo(x + size * 0.30f, yy - h);
            p.lineTo(x + size * 0.73f, yy - h);
            p.lineTo(x + size * 0.63f, yy + h);
            p.lineTo(x + size * 0.20f, yy + h);
            p.close();
            canvas.drawPath(p, paint);
        }

        String tokenIconAsset(MarketCoin coin) {
            if (coin == null || coin.symbol == null) return "";
            if ("BTC".equalsIgnoreCase(coin.symbol)) return "assets/btc.webp";
            if ("ETH".equalsIgnoreCase(coin.symbol)) return "assets/eth.webp";
            if ("BNB".equalsIgnoreCase(coin.symbol)) return "assets/bnb.webp";
            return "";
        }

        void sparkline(Canvas canvas, MarketCoin coin, float x, float y, float width, float height) {
            if (coin.sparkline.size() < 2) return;
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            int start = sparkStartIndex(coin);
            for (int i = start; i < coin.sparkline.size(); i++) {
                double value = coin.sparkline.get(i);
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
            if (max <= min) max = min + 1d;
            Path path = new Path();
            Path fill = new Path();
            int count = coin.sparkline.size() - start;
            for (int i = start; i < coin.sparkline.size(); i++) {
                float px = x + width * (i - start) / (float) Math.max(1, count - 1);
                float py = y + height - (float) ((coin.sparkline.get(i) - min) / (max - min)) * height;
                if (i == start) {
                    path.moveTo(px, py);
                    fill.moveTo(px, y + height);
                    fill.lineTo(px, py);
                } else {
                    path.lineTo(px, py);
                    fill.lineTo(px, py);
                }
            }
            fill.lineTo(x + width, y + height);
            fill.close();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(coin.change24h < 0 ? Color.argb(48, 255, 86, 98) : Color.argb(48, 39, 214, 139));
            canvas.drawPath(fill, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6);
            paint.setColor(coin.change24h < 0 ? RED : GREEN);
            canvas.drawPath(path, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        int sparkStartIndex(MarketCoin coin) {
            int size = coin.sparkline.size();
            if (timeRange == 0) return Math.max(0, size - Math.max(4, size / 3));
            if (timeRange == 1) return Math.max(0, size - Math.max(6, (size * 2) / 3));
            return 0;
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
            if (page == 5 && marketFilter == 4) return stockPerpsCoins();
            ArrayList<MarketCoin> coins = new ArrayList<MarketCoin>();
            if (marketList.size() == 0) {
                coins.addAll(marketCoins.values());
            } else {
                coins.addAll(marketList);
            }
            if (coins.size() == 0) coins.addAll(fallbackCoins());
            for (int i = coins.size() - 1; i >= 0; i--) {
                MarketCoin c = coins.get(i);
                if (page == 5) {
                    if (marketFilter == 1 && !containsAny(c, "bitcoin", "ethereum", "hyperliquid", "solana", "binancecoin")) coins.remove(i);
                    if (marketFilter == 2 && c.change24h < 0) coins.remove(i);
                } else {
                    if (marketFilter == 1 && !containsAny(c, "bitcoin", "ethereum", "binancecoin", "solana", "dogecoin")) coins.remove(i);
                    else if (marketFilter == 2 && c.change24h < 0) coins.remove(i);
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
            coins.add(fallbackCoin("chainlink", "LINK", "Chainlink", 13.9d, 2.1d, 498000000d, 9400000000d));
            coins.add(fallbackCoin("uniswap", "UNI", "Uniswap", 6.8d, 1.8d, 185000000d, 4100000000d));
            coins.add(fallbackCoin("aave", "AAVE", "Aave", 184.3d, 5.4d, 252000000d, 2780000000d));
            coins.add(fallbackCoin("ondo", "ONDO", "Ondo", 0.81d, -1.3d, 147000000d, 2600000000d));
            coins.add(fallbackCoin("pepe", "PEPE", "Pepe", 0.0000068d, 7.2d, 316000000d, 2850000000d));
            coins.add(fallbackCoin("hyperliquid", "HYPE", "Hyperliquid", 63.0d, 14.2d, 1150000000d, 15100000000d));
            coins.add(fallbackCoin("zcash", "ZEC", "Zcash", 637d, 8.3d, 699000000d, 10600000000d));
            return coins;
        }

        List<MarketCoin> stockPerpsCoins() {
            ArrayList<MarketCoin> coins = new ArrayList<MarketCoin>();
            coins.add(fallbackCoin("apple", "AAPL", "Apple", 195.72d, 1.1d, 940000000d, 2940000000000d));
            coins.add(fallbackCoin("tesla", "TSLA", "Tesla", 182.34d, -2.4d, 1230000000d, 580000000000d));
            coins.add(fallbackCoin("nvidia", "NVDA", "NVIDIA", 924.15d, 3.8d, 2110000000d, 2310000000000d));
            coins.add(fallbackCoin("microstrategy", "MSTR", "MicroStrategy", 1612.5d, 4.6d, 620000000d, 29400000000d));
            coins.add(fallbackCoin("coinbase", "COIN", "Coinbase", 238.6d, 2.2d, 840000000d, 58200000000d));
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
            canvas.clipRect(0, 102, 1080, NAV_TOP);
            drawBmp(canvas, content, 0, 102 - scrollY, 1080, 1918);
            canvas.restore();
            drawModernBottomNav(canvas, page);
        }

        void drawFixedChromePage(Canvas canvas, String content, String bottomNav) {
            drawStatus(canvas);
            drawBmp(canvas, content, 0, 102, 1080, 1918);
        }

        void drawPopularHeader(Canvas canvas) {
            rect(canvas, 270, 118, 540, 94, BG, 0);
            text(canvas, "Популярные", 540, 185, 52, TEXT, Paint.Align.CENTER, true);
        }

        void drawPopularTop(Canvas canvas) {
            drawStatus(canvas);
            text(canvas, "Популярные", 540, 185, 52, TEXT, Paint.Align.CENTER, true);
            drawSearchGlyph(canvas, 1000, 174, MUTED, 1.05f);

            rect(canvas, 44, 250, 480, 150, PANEL, 42);
            drawSmallFeatureIcon(canvas, 150, 325, 0);
            text(canvas, "Прогнозы", 255, 340, 44, TEXT, Paint.Align.LEFT, true);
            rect(canvas, 556, 250, 480, 150, PANEL, 42);
            drawSmallFeatureIcon(canvas, 662, 325, 1);
            text(canvas, "Meme Rush", 760, 340, 44, TEXT, Paint.Align.LEFT, true);

            text(canvas, "Самые торгуемые (24 ч.)", 44, 505, 45, TEXT, Paint.Align.LEFT, true);
            List<MarketCoin> coins = visibleCoins();
            if (coins.size() > 0) popularMarketCard(canvas, coins.get(0), 44, 550);
            if (coins.size() > 1) popularMarketCard(canvas, coins.get(1), 392, 550);
            if (coins.size() > 2) popularMarketCard(canvas, coins.get(2), 740, 550);
        }

        void popularMarketCard(Canvas canvas, MarketCoin coin, float x, float y) {
            if (coin == null) return;
            rect(canvas, x, y, 330, 285, PANEL, 36);
            text(canvas, shortName(coin), x + 50, y + 76, 32, MUTED, Paint.Align.LEFT, true);
            drawTokenIcon(canvas, coin, x + 230, y + 38, 58);
            text(canvas, formatPrice(coin.price), x + 50, y + 155, 48, TEXT, Paint.Align.LEFT, true);
            text(canvas, formatPct(coin.change24h), x + 50, y + 215, 38, coin.change24h < 0 ? RED : GREEN, Paint.Align.LEFT, true);
            sparkline(canvas, coin, x + 185, y + 220, 105, 50);
        }

        void drawSearchGlyph(Canvas canvas, float cx, float cy, int color, float scale) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6 * scale);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(color);
            canvas.drawCircle(cx - 10 * scale, cy - 10 * scale, 28 * scale, paint);
            canvas.drawLine(cx + 12 * scale, cy + 12 * scale, cx + 42 * scale, cy + 42 * scale, paint);
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStyle(Paint.Style.FILL);
        }

        void drawSmallFeatureIcon(Canvas canvas, float cx, float cy, int icon) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(TEXT);
            if (icon == 0) {
                canvas.drawRoundRect(new RectF(cx - 35, cy - 28, cx + 35, cy + 22), 6, 6, paint);
                canvas.drawLine(cx - 20, cy + 34, cx + 20, cy + 34, paint);
                canvas.drawLine(cx, cy + 22, cx, cy + 34, paint);
                canvas.drawLine(cx - 20, cy - 2, cx - 5, cy + 12, paint);
                canvas.drawLine(cx - 5, cy + 12, cx + 24, cy - 15, paint);
            } else {
                Path rocket = new Path();
                rocket.moveTo(cx - 25, cy + 28);
                rocket.lineTo(cx + 18, cy - 35);
                rocket.lineTo(cx + 38, cy - 15);
                rocket.lineTo(cx - 8, cy + 32);
                rocket.close();
                canvas.drawPath(rocket, paint);
                canvas.drawCircle(cx + 12, cy - 12, 8, paint);
                canvas.drawLine(cx - 20, cy + 10, cx - 42, cy + 20, paint);
                canvas.drawLine(cx + 2, cy + 32, cx - 8, cy + 52, paint);
            }
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStrokeJoin(Paint.Join.MITER);
            paint.setStyle(Paint.Style.FILL);
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

        void drawModernBottomNav(Canvas canvas, int activePage) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(232, 24, 25, 27));
            canvas.drawRoundRect(new RectF(8, 2108, 1072, 2288), 92, 92, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.rgb(58, 59, 62));
            canvas.drawRoundRect(new RectF(8, 2108, 1072, 2288), 92, 92, paint);
            paint.setStyle(Paint.Style.FILL);

            navItem(canvas, 132, 0, "Главная", activePage == 0);
            navItem(canvas, 330, 1, "Популярные", activePage == 1);
            navItem(canvas, 735, 3, "Награды", activePage == 3);
            navItem(canvas, 934, 4, "Подробнее", activePage == 4);

            rect(canvas, 470, 2058, 140, 140, GREEN, 70);
            drawNavIcon(canvas, 540, 2128, 2, Color.rgb(4, 24, 12), 1.1f);
            text(canvas, "Торговать", 540, 2268, 30, TEXT, Paint.Align.CENTER, true);
            rect(canvas, 345, 2320, 390, 12, Color.WHITE, 6);
        }

        void navItem(Canvas canvas, float cx, int icon, String label, boolean active) {
            if (active) rect(canvas, cx - 96, 2124, 192, 142, Color.rgb(28, 75, 46), 76);
            drawNavIcon(canvas, cx, 2165, icon, active ? GREEN : MUTED, 1f);
            text(canvas, label, cx, 2254, label.length() > 8 ? 26 : 30, active ? GREEN : MUTED, Paint.Align.CENTER, true);
        }

        void drawNavIcon(Canvas canvas, float cx, float cy, int icon, int color, float scale) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5f * scale);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(color);

            float s = 46f * scale;
            if (icon == 0) {
                Path roof = new Path();
                roof.moveTo(cx - s * 0.55f, cy);
                roof.lineTo(cx, cy - s * 0.48f);
                roof.lineTo(cx + s * 0.55f, cy);
                canvas.drawPath(roof, paint);
                canvas.drawLine(cx - s * 0.38f, cy, cx - s * 0.38f, cy + s * 0.48f, paint);
                canvas.drawLine(cx + s * 0.38f, cy, cx + s * 0.38f, cy + s * 0.48f, paint);
                canvas.drawLine(cx - s * 0.38f, cy + s * 0.48f, cx + s * 0.38f, cy + s * 0.48f, paint);
            } else if (icon == 1) {
                canvas.drawLine(cx - s * 0.45f, cy + s * 0.38f, cx - s * 0.12f, cy + s * 0.05f, paint);
                canvas.drawLine(cx - s * 0.12f, cy + s * 0.05f, cx + s * 0.08f, cy + s * 0.2f, paint);
                canvas.drawLine(cx + s * 0.08f, cy + s * 0.2f, cx + s * 0.48f, cy - s * 0.36f, paint);
                canvas.drawLine(cx + s * 0.22f, cy - s * 0.36f, cx + s * 0.48f, cy - s * 0.36f, paint);
                canvas.drawLine(cx + s * 0.48f, cy - s * 0.36f, cx + s * 0.48f, cy - s * 0.1f, paint);
            } else if (icon == 2) {
                canvas.drawLine(cx - s * 0.42f, cy - s * 0.2f, cx + s * 0.32f, cy - s * 0.2f, paint);
                canvas.drawLine(cx + s * 0.12f, cy - s * 0.42f, cx + s * 0.34f, cy - s * 0.2f, paint);
                canvas.drawLine(cx + s * 0.12f, cy + s * 0.02f, cx + s * 0.34f, cy - s * 0.2f, paint);
                canvas.drawLine(cx + s * 0.42f, cy + s * 0.22f, cx - s * 0.32f, cy + s * 0.22f, paint);
                canvas.drawLine(cx - s * 0.12f, cy, cx - s * 0.34f, cy + s * 0.22f, paint);
                canvas.drawLine(cx - s * 0.12f, cy + s * 0.44f, cx - s * 0.34f, cy + s * 0.22f, paint);
            } else if (icon == 3) {
                canvas.drawRoundRect(new RectF(cx - s * 0.44f, cy - s * 0.08f, cx + s * 0.44f, cy + s * 0.48f), 6f * scale, 6f * scale, paint);
                canvas.drawLine(cx, cy - s * 0.08f, cx, cy + s * 0.48f, paint);
                canvas.drawLine(cx - s * 0.52f, cy - s * 0.08f, cx + s * 0.52f, cy - s * 0.08f, paint);
                canvas.drawRoundRect(new RectF(cx - s * 0.52f, cy - s * 0.3f, cx + s * 0.52f, cy - s * 0.08f), 6f * scale, 6f * scale, paint);
                canvas.drawArc(new RectF(cx - s * 0.34f, cy - s * 0.55f, cx, cy - s * 0.08f), -20, 220, false, paint);
                canvas.drawArc(new RectF(cx, cy - s * 0.55f, cx + s * 0.34f, cy - s * 0.08f), -200, 220, false, paint);
            } else {
                canvas.drawCircle(cx, cy + s * 0.08f, s * 0.42f, paint);
                Path needle = new Path();
                needle.moveTo(cx + s * 0.18f, cy - s * 0.18f);
                needle.lineTo(cx + s * 0.02f, cy + s * 0.2f);
                needle.lineTo(cx - s * 0.18f, cy + s * 0.34f);
                needle.close();
                paint.setStyle(Paint.Style.FILL);
                canvas.drawPath(needle, paint);
            }
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStrokeJoin(Paint.Join.MITER);
            paint.setStyle(Paint.Style.FILL);
        }

        void drawModalOrSheet(Canvas canvas) {
            if (modal == MODAL_SEARCH) drawSearch(canvas);
            else if (modal == MODAL_TOKEN_DETAIL) drawTokenDetail(canvas);
            else if (modal == MODAL_PERPS_DETAIL) drawPerpsDetail(canvas);
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
            rect(canvas, 150, 116, 740, 100, Color.rgb(35, 36, 39), 50);
            drawSearchGlyph(canvas, 205, 166, MUTED, 0.5f);
            text(canvas, searchPlaceholder(), 255, 180, 36, MUTED, Paint.Align.LEFT, true);
            text(canvas, "Отмена", 930, 180, 34, GREEN, Paint.Align.CENTER, true);
            if (searchContextPage != 4) {
                filterPill(canvas, 44, 290, 160, 72, "Токены", true);
                filterPill(canvas, 220, 290, 135, 72, "dApp", false);
                filterPill(canvas, 370, 290, 170, 72, "NFT", false);
            }
            text(canvas, searchContextPage == 4 ? "Популярные dApp" : "Популярное", 44, searchContextPage == 4 ? 330 : 430, 46, TEXT, Paint.Align.LEFT, true);
            if (searchContextPage == 4) {
                dappOption(canvas, 520, 0, "Trust Wallet website", "dApp");
                dappOption(canvas, 655, 1, "Support Center", "Help");
                dappOption(canvas, 790, 2, "Staking", "Earn");
                dappOption(canvas, 925, 2, "Token Approval", "Guide");
                dappOption(canvas, 1060, 2, "Network fees", "Guide");
                drawSystemNav(canvas);
                return;
            }
            List<MarketCoin> coins = visibleCoins();
            canvas.save();
            canvas.clipRect(0, 470, 1080, NAV_BOTTOM);
            float y = 560 - modalScrollY;
            for (int i = 0; i < coins.size(); i++) {
                if (y > 470 && y < NAV_BOTTOM + 120) drawCoinRow(canvas, coins.get(i), y, true);
                y += 150;
            }
            canvas.restore();
            drawSystemNav(canvas);
        }

        void dappOption(Canvas canvas, float y, int icon, String title, String subtitle) {
            drawDiscoverIcon(canvas, 84, y - 14, icon);
            text(canvas, title, 145, y, 40, TEXT, Paint.Align.LEFT, true);
            text(canvas, subtitle, 992, y, 34, MUTED, Paint.Align.RIGHT, true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.rgb(52, 53, 56));
            canvas.drawLine(145, y + 45, 992, y + 45, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        String searchPlaceholder() {
            if (searchContextPage == 1) return "Поиск токена";
            if (searchContextPage == 5) return "Поиск рынка";
            if (searchContextPage == 4) return "Введите dApp или URL";
            return "Поиск токена или dApp";
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
            drawChartRangeLabels(canvas, coin, 92, 1055, 896);
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

        void drawPerpsDetail(Canvas canvas) {
            MarketCoin coin = selectedCoin;
            if (coin == null) return;
            rect(canvas, 0, 0, BASE_W, BASE_H, BG, 0);
            drawStatus(canvas);
            text(canvas, "<", 78, 185, 72, MUTED, Paint.Align.CENTER, false);
            text(canvas, coin.symbol + " Perps", 540, 185, 56, TEXT, Paint.Align.CENTER, true);
            text(canvas, providerLabel(), 540, 245, 36, MUTED, Paint.Align.CENTER, true);
            text(canvas, formatPrice(coin.price), 540, 420, 86, TEXT, Paint.Align.CENTER, true);
            text(canvas, formatPct(coin.change24h), 540, 500, 44, coin.change24h < 0 ? RED : GREEN, Paint.Align.CENTER, true);
            rect(canvas, 44, 610, 992, 520, PANEL, 42);
            sparkline(canvas, coin, 92, 690, 896, 330);
            drawChartRangeLabels(canvas, coin, 92, 1055, 896);
            chip(canvas, 88, 1190, "24h", timeRange == 0);
            chip(canvas, 230, 1190, "7d", timeRange == 1);
            chip(canvas, 360, 1190, "30d", timeRange == 2);
            rect(canvas, 44, 1340, 992, 460, Color.rgb(34, 35, 38), 34);
            text(canvas, "Объем (24 ч.)", 88, 1430, 38, MUTED, Paint.Align.LEFT, true);
            text(canvas, "$" + compact(coin.volume), 992, 1430, 42, TEXT, Paint.Align.RIGHT, true);
            text(canvas, "Плечо", 88, 1545, 38, MUTED, Paint.Align.LEFT, true);
            text(canvas, "20x", 992, 1545, 42, TEXT, Paint.Align.RIGHT, true);
            text(canvas, "Открытый интерес", 88, 1660, 38, MUTED, Paint.Align.LEFT, true);
            text(canvas, "$" + compact(coin.marketCap * 0.018d), 992, 1660, 42, TEXT, Paint.Align.RIGHT, true);
            rect(canvas, 88, 1920, 416, 112, Color.rgb(43, 92, 60), 58);
            text(canvas, "Лонг", 296, 1992, 42, GREEN, Paint.Align.CENTER, true);
            rect(canvas, 576, 1920, 416, 112, Color.rgb(94, 43, 52), 58);
            text(canvas, "Шорт", 784, 1992, 42, RED, Paint.Align.CENTER, true);
            drawSystemNav(canvas);
        }

        void drawChartRangeLabels(Canvas canvas, MarketCoin coin, float x, float y, float width) {
            if (coin == null || coin.sparkline.size() < 2) return;
            int start = sparkStartIndex(coin);
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (int i = start; i < coin.sparkline.size(); i++) {
                double value = coin.sparkline.get(i);
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
            String range = timeRange == 0 ? "24h" : timeRange == 1 ? "7d" : "30d";
            text(canvas, range, x, y, 30, MUTED, Paint.Align.LEFT, true);
            text(canvas, "Min " + formatPrice(min), x + width * 0.5f, y, 30, MUTED, Paint.Align.CENTER, true);
            text(canvas, "Max " + formatPrice(max), x + width, y, 30, MUTED, Paint.Align.RIGHT, true);
        }

        void drawDiscoverDetail(Canvas canvas) {
            rect(canvas, 0, 0, BASE_W, BASE_H, BG, 0);
            drawStatus(canvas);
            text(canvas, "‹", 78, 185, 78, MUTED, Paint.Align.CENTER, false);
            text(canvas, selectedDiscoverTitle, 540, 185, 46, TEXT, Paint.Align.CENTER, true);

            rect(canvas, 44, 320, 992, 520, PANEL, 42);
            rect(canvas, 88, 380, 128, 128, Color.rgb(24, 76, 44), 64);
            if (selectedDiscoverTitle.toLowerCase(Locale.US).contains("support") || selectedDiscoverTitle.contains("поддерж")) {
                drawDiscoverIcon(canvas, 152, 444, 1);
            } else if (selectedDiscoverTitle.toLowerCase(Locale.US).contains("wallet")) {
                drawDiscoverIcon(canvas, 152, 444, 0);
            } else {
                drawLeafIcon(canvas, 152, 444, GREEN);
            }
            text(canvas, discoverCategory(), 250, 410, 34, MUTED, Paint.Align.LEFT, true);
            text(canvas, selectedDiscoverTitle, 250, 475, 48, TEXT, Paint.Align.LEFT, true);
            text(canvas, discoverDescriptionLine1(), 88, 610, 36, MUTED, Paint.Align.LEFT, true);
            text(canvas, discoverDescriptionLine2(), 88, 660, 36, MUTED, Paint.Align.LEFT, true);
            text(canvas, "В прототипе внешние переходы и подписи отключены.", 88, 735, 32, MUTED, Paint.Align.LEFT, true);

            rect(canvas, 88, 930, 904, 116, GREEN, 58);
            text(canvas, discoverCta(), 540, 1004, 40, Color.rgb(4, 24, 12), Paint.Align.CENTER, true);
            rect(canvas, 88, 1085, 904, 116, Color.rgb(35, 36, 39), 58);
            text(canvas, "Закрыть", 540, 1159, 40, TEXT, Paint.Align.CENTER, true);
            drawSystemNav(canvas);
        }

        String discoverCategory() {
            String lower = selectedDiscoverTitle.toLowerCase(Locale.US);
            if (lower.contains("staking") || selectedDiscoverTitle.contains("Стей")) return "Заработок";
            if (lower.contains("support") || selectedDiscoverTitle.contains("поддерж")) return "Помощь";
            if (lower.contains("wallet")) return "Сайт";
            return "Обучение";
        }

        String discoverCta() {
            String lower = selectedDiscoverTitle.toLowerCase(Locale.US);
            if (lower.contains("staking") || selectedDiscoverTitle.contains("Стей")) return "Открыть стейкинг";
            if (lower.contains("support") || selectedDiscoverTitle.contains("поддерж")) return "Открыть поддержку";
            if (lower.contains("wallet")) return "Открыть сайт";
            return "Читать";
        }

        String discoverDescriptionLine1() {
            String lower = selectedDiscoverTitle.toLowerCase(Locale.US);
            if (lower.contains("staking") || selectedDiscoverTitle.contains("Стей")) return "Просматривайте возможности заработка";
            if (lower.contains("support") || selectedDiscoverTitle.contains("поддерж")) return "Найдите ответы по кошельку и dApp.";
            if (lower.contains("wallet")) return "Официальный ресурс Trust Wallet.";
            return "Короткая справка по безопасной работе";
        }

        String discoverDescriptionLine2() {
            String lower = selectedDiscoverTitle.toLowerCase(Locale.US);
            if (lower.contains("staking") || selectedDiscoverTitle.contains("Стей")) return "и выбирайте подходящие сети.";
            if (lower.contains("support") || selectedDiscoverTitle.contains("поддерж")) return "Все действия остаются внутри mock UI.";
            if (lower.contains("wallet")) return "Переход наружу заменен mock-экраном.";
            return "с криптоактивами и разрешениями.";
        }

        void drawSheet(Canvas canvas) {
            dim(canvas);
            if (sheet == SHEET_TRADE_MENU) {
                rect(canvas, 44, 1428, 992, 670, Color.rgb(43, 43, 46), 52);
                text(canvas, "×", 980, 1522, 58, MUTED, Paint.Align.CENTER, false);
                text(canvas, "Торговать", 125, 1525, 46, TEXT, Paint.Align.LEFT, true);
                drawTradeMenuSheet(canvas);
                return;
            }
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
                    sheet == SHEET_PERPS_SETTINGS ? "Настройки бессрочных" :
                    sheet == SHEET_SWAP_DISABLED ? "Своп недоступен" :
                    sheet == SHEET_WALLET_SELECTOR ? "Кошельки" :
                    sheet == SHEET_COPY_ADDRESS ? "Адрес скопирован" :
                    sheet == SHEET_SCAN ? "Сканер" :
                    sheet == SHEET_TRADE_MENU ? "Торговать" :
                    sheet == SHEET_SAFE_ACTION ? safeActionTitle : "Фильтр";
            text(canvas, title, 88, 1385, 52, TEXT, Paint.Align.LEFT, true);
            if (sheet == SHEET_TRADE_MENU) {
                drawTradeMenuSheet(canvas);
                return;
            }
            if (sheet == SHEET_WALLET_SELECTOR) {
                drawWalletSelectorSheet(canvas);
                return;
            }
            if (sheet == SHEET_COPY_ADDRESS) {
                drawCopyAddressSheet(canvas);
                return;
            }
            if (sheet == SHEET_SCAN) {
                drawScannerSheet(canvas);
                return;
            }
            if (sheet == SHEET_SEND || sheet == SHEET_RECEIVE || sheet == SHEET_BUY || sheet == SHEET_FUND || sheet == SHEET_DEPOSIT) {
                drawActionFlowSheet(canvas, sheet);
                return;
            }
            if (sheet == SHEET_PERPS_SETTINGS) {
                option(canvas, 1510, "Поставщик по умолчанию", providerLabel());
                option(canvas, 1642, "Подтверждение ордера", "Вкл");
                option(canvas, 1774, "Риск-режим", "Консервативный");
                option(canvas, 1906, "Уведомления PnL", "Вкл");
                return;
            }
            if (sheet == SHEET_SWAP_DISABLED) {
                text(canvas, selectedSwapToken == null ? "Выберите токен, чтобы продолжить." : "Недостаточно средств для свопа.", 88, 1530, 40, MUTED, Paint.Align.LEFT, true);
                text(canvas, "Реальные подписи и транзакции в прототипе отключены.", 88, 1600, 34, MUTED, Paint.Align.LEFT, true);
                rect(canvas, 88, 1800, 904, 120, Color.rgb(43, 92, 60), 62);
                text(canvas, "Понятно", 540, 1876, 42, GREEN, Paint.Align.CENTER, true);
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
                option(canvas, 1510, "Проскальзывание", slippageLabel());
                option(canvas, 1642, "Маршрут", routeMode == 0 ? "Лучший" : "Дешевле");
                option(canvas, 1774, "MEV-защита", mevProtection ? "Вкл" : "Выкл");
                return;
            }
            if (sheet == SHEET_SWAP_TOKEN) {
                rect(canvas, 88, 1450, 904, 104, Color.rgb(35, 36, 39), 52);
                text(canvas, "Поиск токена или сети", 150, 1516, 38, MUTED, Paint.Align.LEFT, true);
                List<MarketCoin> coins = visibleCoins();
                canvas.save();
                canvas.clipRect(0, 1570, 1080, 2235);
                for (int i = 0; i < coins.size(); i++) {
                    float y = 1642 + i * 118 - sheetScrollY;
                    if (y > 1570 && y < 2260) tokenOption(canvas, y, coins.get(i));
                }
                canvas.restore();
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

        String slippageLabel() {
            if (slippageMode == 1) return "0.5%";
            if (slippageMode == 2) return "1.0%";
            return "Авто";
        }

        void drawWalletSelectorSheet(Canvas canvas) {
            rect(canvas, 88, 1480, 904, 132, Color.rgb(35, 36, 39), 34);
            text(canvas, "Основной кошелек 1", 132, 1540, 42, TEXT, Paint.Align.LEFT, true);
            text(canvas, "0x4a...91b2", 132, 1588, 30, MUTED, Paint.Align.LEFT, true);
            text(canvas, "✓", 950, 1560, 46, GREEN, Paint.Align.RIGHT, true);
            rect(canvas, 88, 1640, 904, 132, Color.rgb(29, 30, 33), 34);
            text(canvas, "Watch Wallet", 132, 1700, 42, TEXT, Paint.Align.LEFT, true);
            text(canvas, "Только просмотр", 132, 1748, 30, MUTED, Paint.Align.LEFT, true);
            rect(canvas, 88, 1848, 904, 112, Color.rgb(43, 92, 60), 56);
            text(canvas, "Добавить кошелек", 540, 1920, 40, GREEN, Paint.Align.CENTER, true);
        }

        void drawCopyAddressSheet(Canvas canvas) {
            rect(canvas, 88, 1490, 904, 126, Color.rgb(35, 36, 39), 34);
            text(canvas, "0x4a8f...91b2", 132, 1568, 42, TEXT, Paint.Align.LEFT, true);
            text(canvas, "✓", 950, 1568, 46, GREEN, Paint.Align.RIGHT, true);
            text(canvas, "Адрес основного кошелька скопирован.", 88, 1705, 36, MUTED, Paint.Align.LEFT, true);
            rect(canvas, 88, 1848, 904, 112, Color.rgb(43, 92, 60), 56);
            text(canvas, "Готово", 540, 1920, 40, GREEN, Paint.Align.CENTER, true);
        }

        void drawScannerSheet(Canvas canvas) {
            rect(canvas, 88, 1480, 904, 300, Color.rgb(35, 36, 39), 34);
            rect(canvas, 390, 1532, 300, 190, Color.rgb(24, 24, 25), 24);
            text(canvas, "□", 540, 1658, 110, MUTED, Paint.Align.CENTER, false);
            text(canvas, "Камера отключена в UI-прототипе.", 540, 1848, 36, MUTED, Paint.Align.CENTER, true);
            rect(canvas, 88, 1950, 904, 112, Color.rgb(43, 92, 60), 56);
            text(canvas, "Ввести адрес", 540, 2022, 40, GREEN, Paint.Align.CENTER, true);
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

        void drawTradeMenuSheet(Canvas canvas) {
            tradeMenuOption(canvas, 1620, 0, "Обмен",
                    "Обменивайте любую", "криптовалюту мгновенно");
            tradeMenuOption(canvas, 1788, 1, "Бессрочные фьючерсы",
                    "Торгуйте на рынке вне зависимости от", "его направления");
            tradeMenuOption(canvas, 1960, 2, "Прогнозы",
                    "Торгуйте на событиях реального мира", "");
        }

        void tradeMenuOption(Canvas canvas, float y, int icon, String title, String line1, String line2) {
            drawTradeIcon(canvas, 122, y + 10, icon);
            text(canvas, title, 200, y - 18, 46, TEXT, Paint.Align.LEFT, true);
            text(canvas, line1, 200, y + 55, 34, MUTED, Paint.Align.LEFT, false);
            if (line2 != null && line2.length() > 0) text(canvas, line2, 200, y + 105, 34, MUTED, Paint.Align.LEFT, false);
        }

        void drawTradeIcon(Canvas canvas, float cx, float cy, int icon) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(GREEN);
            if (icon == 0) {
                canvas.drawArc(new RectF(cx - 28, cy - 28, cx + 28, cy + 28), -30, 245, false, paint);
                canvas.drawLine(cx + 26, cy - 12, cx + 42, cy - 18, paint);
                canvas.drawLine(cx + 26, cy - 12, cx + 28, cy - 30, paint);
                canvas.drawArc(new RectF(cx - 28, cy - 28, cx + 28, cy + 28), 150, 245, false, paint);
                canvas.drawLine(cx - 26, cy + 12, cx - 42, cy + 18, paint);
                canvas.drawLine(cx - 26, cy + 12, cx - 28, cy + 30, paint);
            } else if (icon == 1) {
                canvas.drawLine(cx, cy - 36, cx, cy + 32, paint);
                canvas.drawLine(cx - 32, cy, cx + 32, cy, paint);
                canvas.drawCircle(cx - 32, cy, 8, paint);
                canvas.drawCircle(cx + 32, cy, 8, paint);
                canvas.drawCircle(cx, cy - 36, 8, paint);
                canvas.drawCircle(cx, cy + 32, 8, paint);
            } else {
                canvas.drawRoundRect(new RectF(cx - 34, cy - 28, cx + 34, cy + 24), 6, 6, paint);
                canvas.drawLine(cx - 20, cy + 36, cx + 20, cy + 36, paint);
                canvas.drawLine(cx, cy + 24, cx, cy + 36, paint);
                canvas.drawLine(cx - 18, cy - 2, cx - 4, cy + 12, paint);
                canvas.drawLine(cx - 4, cy + 12, cx + 22, cy - 15, paint);
            }
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStrokeJoin(Paint.Join.MITER);
            paint.setStyle(Paint.Style.FILL);
        }

        void tokenOption(Canvas canvas, float y, MarketCoin coin) {
            drawTokenIcon(canvas, coin, 88, y - 46, 76);
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
            drawTokenIcon(canvas, coin, 44, y - 70, 88);
            text(canvas, coin.symbol, 160, y - 25, large ? 48 : 42, TEXT, Paint.Align.LEFT, true);
            text(canvas, coin.name, 160, y + 28, 32, MUTED, Paint.Align.LEFT, true);
            text(canvas, formatPrice(coin.price), 1035, y - 25, 42, TEXT, Paint.Align.RIGHT, true);
            text(canvas, formatPct(coin.change24h), 1035, y + 28, 32, coin.change24h < 0 ? RED : GREEN, Paint.Align.RIGHT, true);
        }

        void drawSwap(Canvas canvas) {
            rect(canvas, 0, 0, BASE_W, BASE_H, BG, 0);
            drawStatus(canvas);
            text(canvas, "‹", 78, 190, 78, MUTED, Paint.Align.CENTER, false);
            text(canvas, "Своп", 540, 185, 56, TEXT, Paint.Align.CENTER, true);
            drawSlidersGlyph(canvas, 990, 168, MUTED, 0.72f);

            rect(canvas, 44, 260, 992, 280, PANEL, 34);
            text(canvas, "0", 92, 390, 86, TEXT, Paint.Align.LEFT, true);
            rect(canvas, 705, 318, 287, 116, Color.rgb(22, 23, 25), 58);
            rect(canvas, 730, 338, 76, 76, Color.rgb(61, 62, 66), 38);
            text(canvas, "+", 768, 392, 54, TEXT, Paint.Align.CENTER, false);
            text(canvas, "Fund", 835, 392, 42, TEXT, Paint.Align.LEFT, true);

            rect(canvas, 44, 560, 992, 170, PANEL, 34);
            text(canvas, "0", 92, 665, 70, MUTED, Paint.Align.LEFT, true);
            rect(canvas, 646, 592, 346, 99, Color.rgb(42, 88, 58), 52);
            text(canvas, selectedSwapToken == null ? "Select token" : selectedSwapToken.symbol, 819, 654, 42, GREEN, Paint.Align.CENTER, true);

            rect(canvas, 496, 520, 88, 88, Color.rgb(22, 23, 25), 44);
            text(canvas, "↓", 540, 578, 48, MUTED, Paint.Align.CENTER, true);

            rect(canvas, 44, 2072, 992, 132, Color.rgb(34, 103, 61), 66);
            rect(canvas, 44, 2072, 250, 132, Color.rgb(66, 190, 112), 66);
            text(canvas, "→", 145, 2154, 54, Color.rgb(140, 230, 170), Paint.Align.CENTER, false);
            text(canvas, "Сдвиньте вправо для свопа", 575, 2154, 38, Color.rgb(145, 180, 155), Paint.Align.CENTER, true);

            if (swapReversed) {
                text(canvas, "↓", 540, 578, 58, GREEN, Paint.Align.CENTER, true);
            }
            if (selectedSwapToken != null) {
                text(canvas, formatPrice(selectedSwapToken.price), 88, 770, 32, MUTED, Paint.Align.LEFT, true);
            }
            drawSystemNav(canvas);
        }

        void drawPerps(Canvas canvas) {
            rect(canvas, 0, 0, BASE_W, BASE_H, BG, 0);
            drawStatus(canvas);
            drawHistoryGlyph(canvas, 78, 168, TEXT, 0.95f);
            text(canvas, "Бесср.", 540, 185, 54, TEXT, Paint.Align.CENTER, true);
            drawSettingsGlyph(canvas, 992, 168, TEXT, 0.95f);

            rect(canvas, 44, 250, 992, 520, PANEL, 42);
            drawInfinityRibbon(canvas, 540, 395, 1.15f);
            text(canvas, "Торговля бесср. фьючерсами", 540, 555, 45, TEXT, Paint.Align.CENTER, true);
            rect(canvas, 88, 650, 904, 112, Color.rgb(64, 246, 132), 56);
            text(canvas, "Депозит", 540, 722, 46, Color.rgb(8, 38, 20), Paint.Align.CENTER, true);

            drawPerpsFilters(canvas);
            drawLivePerps(canvas);
            drawModernBottomNav(canvas, -1);
        }

        void drawInfinityRibbon(Canvas canvas, float cx, float cy, float scale) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(18 * scale);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(Color.rgb(25, 145, 255));
            canvas.drawArc(new RectF(cx - 210 * scale, cy - 82 * scale, cx, cy + 82 * scale), -35, 250, false, paint);
            canvas.drawArc(new RectF(cx, cy - 82 * scale, cx + 210 * scale, cy + 82 * scale), 145, 250, false, paint);
            paint.setStrokeWidth(7 * scale);
            paint.setColor(Color.rgb(185, 110, 255));
            canvas.drawArc(new RectF(cx - 210 * scale, cy - 82 * scale, cx, cy + 82 * scale), -35, 250, false, paint);
            canvas.drawArc(new RectF(cx, cy - 82 * scale, cx + 210 * scale, cy + 82 * scale), 145, 250, false, paint);
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStyle(Paint.Style.FILL);
        }

        void drawSlidersGlyph(Canvas canvas, float cx, float cy, int color, float scale) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6 * scale);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(color);
            canvas.drawLine(cx - 42 * scale, cy - 26 * scale, cx + 42 * scale, cy - 26 * scale, paint);
            canvas.drawLine(cx - 42 * scale, cy, cx + 42 * scale, cy, paint);
            canvas.drawLine(cx - 42 * scale, cy + 26 * scale, cx + 42 * scale, cy + 26 * scale, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx + 20 * scale, cy - 26 * scale, 9 * scale, paint);
            canvas.drawCircle(cx - 8 * scale, cy, 9 * scale, paint);
            canvas.drawCircle(cx + 30 * scale, cy + 26 * scale, 9 * scale, paint);
            paint.setStrokeCap(Paint.Cap.BUTT);
        }

        void drawHomeTop(Canvas canvas) {
            drawSettingsGlyph(canvas, 70, 168, MUTED, 0.58f);
            rect(canvas, 135, 120, 760, 112, Color.rgb(43, 43, 46), 56);
            drawSearchGlyph(canvas, 205, 172, MUTED, 0.58f);
            text(canvas, "Поиск", 260, 190, 40, Color.rgb(110, 110, 116), Paint.Align.LEFT, true);
            drawScanGlyph(canvas, 1008, 168, MUTED, 0.62f);

            rect(canvas, 350, 315, 380, 76, Color.rgb(55, 55, 58), 38);
            text(canvas, "Основной кошелек 1  ›", 540, 365, 36, TEXT, Paint.Align.CENTER, true);
            drawCopyGlyph(canvas, 815, 350, MUTED, 0.82f);

            text(canvas, "0,00 $", 540, 520, 78, TEXT, Paint.Align.CENTER, true);
            text(canvas, "0,00 $ (0.00%)", 540, 590, 38, MUTED, Paint.Align.CENTER, true);

            homeAction(canvas, 170, 720, 0, "Отправить", false);
            homeAction(canvas, 370, 720, 1, "Получить", false);
            homeAction(canvas, 570, 720, 2, "Обмен", false);
            homeAction(canvas, 820, 720, 3, "Покупка", true);

            rect(canvas, 44, 940, 992, 170, PANEL, 30);
            rect(canvas, 44, 940, 62, 62, Color.rgb(255, 215, 38), 0);
            drawInfinityGlyph(canvas, 145, 1035, GREEN, 1f);
            text(canvas, "Hyperliquid live with 200+", 295, 1010, 32, TEXT, Paint.Align.LEFT, true);
            text(canvas, "markets, 0% markup on fees", 295, 1055, 32, TEXT, Paint.Align.LEFT, true);
            text(canvas, "Explore now  →", 295, 1100, 30, GREEN, Paint.Align.LEFT, true);
        }

        void drawHome(Canvas canvas) {
            drawStatus(canvas);
            canvas.save();
            canvas.clipRect(0, 102, 1080, NAV_TOP);
            canvas.translate(0, -scrollY);
            drawHomeTop(canvas);
            if (assetTab == 1) {
                drawHomeAssetTabs(canvas);
                drawHomeEmptyAssets(canvas, true);
                drawHomeBelowAssets(canvas, 1935);
            } else if (assetTab == 2) {
                drawHomeAssetTabs(canvas);
                drawHomeNftEmpty(canvas);
            } else {
                drawHomeAssetTabs(canvas);
                drawHomeAssets(canvas);
                drawHomeBelowAssets(canvas, 1900);
            }
            canvas.restore();
            drawModernBottomNav(canvas, 0);
        }

        void drawDiscover(Canvas canvas) {
            drawStatus(canvas);
            canvas.save();
            canvas.clipRect(0, 102, 1080, NAV_TOP);
            canvas.translate(0, -scrollY);

            text(canvas, "Подробнее", 540, 185, 52, TEXT, Paint.Align.CENTER, true);
            rect(canvas, 44, 248, 992, 110, Color.rgb(35, 36, 39), 55);
            drawSearchGlyph(canvas, 98, 303, MUTED, 0.55f);
            text(canvas, "Найти или ввести URL-адрес dApp", 155, 318, 38, MUTED, Paint.Align.LEFT, true);

            text(canvas, "Quick links", 44, 485, 42, TEXT, Paint.Align.LEFT, true);
            rect(canvas, 44, 540, 485, 280, Color.rgb(33, 34, 36), 34);
            text(canvas, "Заработок", 88, 615, 34, MUTED, Paint.Align.LEFT, true);
            text(canvas, "Стейкинг", 88, 700, 56, TEXT, Paint.Align.LEFT, true);
            text(canvas, "Deposit now", 88, 770, 34, GREEN, Paint.Align.LEFT, true);
            rect(canvas, 375, 625, 106, 106, Color.rgb(24, 76, 44), 53);
            drawLeafIcon(canvas, 428, 676, GREEN);
            quickLinkCard(canvas, 560, 540, 0, "Website", "Trust Wallet");
            quickLinkCard(canvas, 560, 690, 1, "Support", "Help Center");

            discoverRow(canvas, 900, 0, "Trust Wallet website");
            discoverRow(canvas, 1030, 1, "Центр поддержки");
            discoverRow(canvas, 1160, 2, "Use dApp securely for your apps");
            discoverRow(canvas, 1290, 2, "What is DeFi?");
            discoverRow(canvas, 1420, 2, "What is Token Approval?");
            discoverRow(canvas, 1550, 2, "What is a seed phrase?");
            discoverRow(canvas, 1680, 2, "Network fees explained");

            canvas.restore();
            drawModernBottomNav(canvas, 4);
        }

        void discoverRow(Canvas canvas, float y, int icon, String label) {
            if (y < 420 || y > NAV_TOP + scrollY) return;
            drawDiscoverIcon(canvas, 72, y, icon);
            text(canvas, label, 145, y + 14, 40, TEXT, Paint.Align.LEFT, true);
        }

        void quickLinkCard(Canvas canvas, float x, float y, int icon, String title, String subtitle) {
            rect(canvas, x, y, 476, 128, Color.rgb(33, 34, 36), 30);
            drawDiscoverIcon(canvas, x + 58, y + 65, icon);
            text(canvas, title, x + 112, y + 58, 34, TEXT, Paint.Align.LEFT, true);
            text(canvas, subtitle, x + 112, y + 96, 27, MUTED, Paint.Align.LEFT, true);
        }

        void drawDiscoverIcon(Canvas canvas, float cx, float cy, int icon) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(MUTED);
            if (icon == 0) {
                canvas.drawCircle(cx, cy, 22, paint);
                canvas.drawLine(cx - 22, cy, cx + 22, cy, paint);
                canvas.drawArc(new RectF(cx - 12, cy - 22, cx + 12, cy + 22), -90, 360, false, paint);
            } else if (icon == 1) {
                canvas.drawArc(new RectF(cx - 24, cy - 24, cx + 24, cy + 24), 190, 160, false, paint);
                canvas.drawLine(cx - 28, cy + 2, cx - 28, cy + 25, paint);
                canvas.drawLine(cx + 28, cy + 2, cx + 28, cy + 25, paint);
                canvas.drawCircle(cx - 28, cy + 32, 4, paint);
                canvas.drawCircle(cx + 28, cy + 32, 4, paint);
            } else {
                canvas.drawCircle(cx, cy, 23, paint);
                text(canvas, "?", cx, cy + 13, 38, MUTED, Paint.Align.CENTER, true);
            }
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStrokeJoin(Paint.Join.MITER);
            paint.setStyle(Paint.Style.FILL);
        }

        void drawLeafIcon(Canvas canvas, float cx, float cy, int color) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(color);
            Path p = new Path();
            p.moveTo(cx, cy + 32);
            p.lineTo(cx, cy - 26);
            canvas.drawPath(p, paint);
            canvas.drawOval(new RectF(cx - 34, cy - 30, cx, cy + 4), paint);
            canvas.drawOval(new RectF(cx, cy - 20, cx + 34, cy + 14), paint);
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStrokeJoin(Paint.Join.MITER);
            paint.setStyle(Paint.Style.FILL);
        }

        void homeAction(Canvas canvas, float cx, float y, int icon, String label, boolean primary) {
            rect(canvas, cx - 60, y, 120, 120, primary ? GREEN : Color.rgb(43, 43, 46), 28);
            drawHomeActionIcon(canvas, cx, y + 62, icon, primary ? Color.rgb(4, 24, 12) : TEXT);
            text(canvas, label, cx, y + 170, 36, TEXT, Paint.Align.CENTER, true);
        }

        void drawHomeActionIcon(Canvas canvas, float cx, float cy, int icon, int color) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(color);
            if (icon == 0) {
                canvas.drawLine(cx - 24, cy + 24, cx + 26, cy - 26, paint);
                canvas.drawLine(cx - 2, cy - 28, cx + 28, cy - 28, paint);
                canvas.drawLine(cx + 28, cy - 28, cx + 28, cy + 2, paint);
            } else if (icon == 1) {
                canvas.drawLine(cx, cy - 30, cx, cy + 28, paint);
                canvas.drawLine(cx - 22, cy + 6, cx, cy + 28, paint);
                canvas.drawLine(cx + 22, cy + 6, cx, cy + 28, paint);
            } else if (icon == 2) {
                drawNavIcon(canvas, cx, cy, 2, color, 0.82f);
            } else {
                canvas.drawLine(cx - 30, cy, cx + 30, cy, paint);
                canvas.drawLine(cx, cy - 30, cx, cy + 30, paint);
            }
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStrokeJoin(Paint.Join.MITER);
            paint.setStyle(Paint.Style.FILL);
        }

        void drawSettingsGlyph(Canvas canvas, float cx, float cy, int color, float scale) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6 * scale);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(color);
            canvas.drawCircle(cx, cy, 14 * scale, paint);
            canvas.drawCircle(cx, cy, 27 * scale, paint);
            for (int i = 0; i < 8; i++) {
                double a = Math.PI * i / 4d;
                float x1 = cx + (float) Math.cos(a) * 30 * scale;
                float y1 = cy + (float) Math.sin(a) * 30 * scale;
                float x2 = cx + (float) Math.cos(a) * 39 * scale;
                float y2 = cy + (float) Math.sin(a) * 39 * scale;
                canvas.drawLine(x1, y1, x2, y2, paint);
            }
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStyle(Paint.Style.FILL);
        }

        void drawScanGlyph(Canvas canvas, float cx, float cy, int color, float scale) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5 * scale);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(color);
            float s = 44 * scale;
            canvas.drawLine(cx - s, cy - s, cx - s * 0.42f, cy - s, paint);
            canvas.drawLine(cx - s, cy - s, cx - s, cy - s * 0.42f, paint);
            canvas.drawLine(cx + s, cy - s, cx + s * 0.42f, cy - s, paint);
            canvas.drawLine(cx + s, cy - s, cx + s, cy - s * 0.42f, paint);
            canvas.drawLine(cx - s, cy + s, cx - s * 0.42f, cy + s, paint);
            canvas.drawLine(cx - s, cy + s, cx - s, cy + s * 0.42f, paint);
            canvas.drawLine(cx + s, cy + s, cx + s * 0.42f, cy + s, paint);
            canvas.drawLine(cx + s, cy + s, cx + s, cy + s * 0.42f, paint);
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStyle(Paint.Style.FILL);
        }

        void drawCopyGlyph(Canvas canvas, float cx, float cy, int color, float scale) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4 * scale);
            paint.setColor(color);
            canvas.drawRoundRect(new RectF(cx - 22 * scale, cy - 18 * scale, cx + 24 * scale, cy + 28 * scale), 4 * scale, 4 * scale, paint);
            canvas.drawRoundRect(new RectF(cx - 8 * scale, cy - 32 * scale, cx + 38 * scale, cy + 14 * scale), 4 * scale, 4 * scale, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        void drawInfinityGlyph(Canvas canvas, float cx, float cy, int color, float scale) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8 * scale);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(color);
            canvas.drawArc(new RectF(cx - 60 * scale, cy - 28 * scale, cx, cy + 28 * scale), -45, 270, false, paint);
            canvas.drawArc(new RectF(cx, cy - 28 * scale, cx + 60 * scale, cy + 28 * scale), 135, 270, false, paint);
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStyle(Paint.Style.FILL);
        }

        void drawHomeAssetTabs(Canvas canvas) {
            int cryptoColor = assetTab == 0 ? TEXT : MUTED;
            int favColor = assetTab == 1 ? TEXT : MUTED;
            int nftColor = assetTab == 2 ? TEXT : MUTED;
            text(canvas, "Криптовалюта", 44, 1225, 43, cryptoColor, Paint.Align.LEFT, true);
            text(canvas, "Избранное", 390, 1225, 40, favColor, Paint.Align.LEFT, true);
            text(canvas, "NFT", 690, 1225, 40, nftColor, Paint.Align.LEFT, true);
            drawHistoryGlyph(canvas, 835, 1204, MUTED, 0.72f);
            drawManageGlyph(canvas, 975, 1204, MUTED, 0.72f);
            if (assetTab == 0) rect(canvas, 44, 1258, 270, 8, GREEN, 4);
            else if (assetTab == 1) rect(canvas, 390, 1258, 210, 8, GREEN, 4);
            else rect(canvas, 690, 1258, 70, 8, GREEN, 4);
        }

        void drawHomeEmptyAssets(Canvas canvas, boolean favorites) {
            rect(canvas, 44, 1345, 992, 360, PANEL, 36);
            if (favorites) drawFavoriteEmptyIcon(canvas, 540, 1465);
            else drawWalletEmptyIcon(canvas, 540, 1465);
            text(canvas, favorites ? "Нет избранных токенов" : "Активов пока нет", 540, 1585, 44, TEXT, Paint.Align.CENTER, true);
            text(canvas, favorites ? "Добавьте токены в избранное, чтобы быстро" : "Добавьте токены или получите криптовалюту,",
                    540, 1642, 31, MUTED, Paint.Align.CENTER, true);
            text(canvas, favorites ? "следить за их балансом и ценой." : "чтобы они появились в списке.",
                    540, 1685, 31, MUTED, Paint.Align.CENTER, true);
            rect(canvas, 215, 1770, 650, 96, Color.rgb(35, 36, 39), 48);
            text(canvas, favorites ? "Управление токенами" : "Получить", 540, 1832, 36, TEXT, Paint.Align.CENTER, true);
        }

        void drawHomeNftEmpty(Canvas canvas) {
            rect(canvas, 44, 1345, 992, 560, PANEL, 36);
            drawNftEmptyIcon(canvas, 540, 1510);
            text(canvas, "NFT не найдены", 540, 1665, 46, TEXT, Paint.Align.CENTER, true);
            text(canvas, "Коллекции появятся здесь после получения", 540, 1725, 32, MUTED, Paint.Align.CENTER, true);
            text(canvas, "или подключения поддерживаемой сети.", 540, 1770, 32, MUTED, Paint.Align.CENTER, true);
            rect(canvas, 190, 1850, 700, 102, Color.rgb(35, 36, 39), 51);
            text(canvas, "Управление NFT", 540, 1915, 38, TEXT, Paint.Align.CENTER, true);
        }

        void drawFavoriteEmptyIcon(Canvas canvas, float cx, float cy) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(MUTED);
            Path p = new Path();
            for (int i = 0; i < 10; i++) {
                double a = -Math.PI / 2 + i * Math.PI / 5;
                float r = (i % 2 == 0) ? 62 : 28;
                float px = cx + (float) Math.cos(a) * r;
                float py = cy + (float) Math.sin(a) * r;
                if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
            }
            p.close();
            canvas.drawPath(p, paint);
            paint.setStrokeJoin(Paint.Join.MITER);
            paint.setStyle(Paint.Style.FILL);
        }

        void drawWalletEmptyIcon(Canvas canvas, float cx, float cy) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8);
            paint.setColor(MUTED);
            canvas.drawRoundRect(new RectF(cx - 82, cy - 50, cx + 82, cy + 58), 22, 22, paint);
            canvas.drawCircle(cx + 48, cy + 5, 10, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        void drawNftEmptyIcon(Canvas canvas, float cx, float cy) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8);
            paint.setColor(MUTED);
            canvas.drawRoundRect(new RectF(cx - 78, cy - 78, cx + 78, cy + 78), 28, 28, paint);
            canvas.drawLine(cx - 35, cy - 20, cx + 35, cy - 20, paint);
            canvas.drawLine(cx - 35, cy + 20, cx + 35, cy + 20, paint);
            canvas.drawLine(cx - 35, cy - 20, cx - 35, cy + 20, paint);
            canvas.drawLine(cx + 35, cy - 20, cx + 35, cy + 20, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        void drawHistoryGlyph(Canvas canvas, float cx, float cy, int color, float scale) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6 * scale);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(color);
            canvas.drawCircle(cx, cy, 34 * scale, paint);
            canvas.drawLine(cx, cy - 18 * scale, cx, cy + 4 * scale, paint);
            canvas.drawLine(cx, cy + 4 * scale, cx + 18 * scale, cy + 15 * scale, paint);
            canvas.drawArc(new RectF(cx - 46 * scale, cy - 46 * scale, cx + 46 * scale, cy + 46 * scale), 135, 90, false, paint);
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStyle(Paint.Style.FILL);
        }

        void drawManageGlyph(Canvas canvas, float cx, float cy, int color, float scale) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            float s = 13 * scale;
            for (int r = 0; r < 2; r++) {
                for (int col = 0; col < 2; col++) {
                    canvas.drawRoundRect(new RectF(cx - 29 * scale + col * 42 * scale, cy - 26 * scale + r * 42 * scale,
                            cx - 29 * scale + col * 42 * scale + 26 * scale, cy - 26 * scale + r * 42 * scale + 26 * scale), s * 0.35f, s * 0.35f, paint);
                }
            }
        }

        void drawHomeAssets(Canvas canvas) {
            List<MarketCoin> coins = visibleCoins();
            int count = Math.min(3, coins.size());
            for (int i = 0; i < count; i++) {
                drawHomeAssetRow(canvas, coins.get(i), 1345 + i * 132);
            }
        }

        void drawHomeAssetRow(Canvas canvas, MarketCoin coin, float y) {
            if (coin == null) return;
            drawTokenIcon(canvas, coin, 44, y - 70, 88);
            text(canvas, coin.symbol, 140, y - 22, 44, TEXT, Paint.Align.LEFT, true);
            text(canvas, coin.name, 255, y - 22, 25, MUTED, Paint.Align.LEFT, true);
            text(canvas, formatPrice(coin.price), 140, y + 30, 32, MUTED, Paint.Align.LEFT, true);
            text(canvas, formatPct(coin.change24h), 300, y + 30, 30, coin.change24h < 0 ? RED : GREEN, Paint.Align.LEFT, true);
            text(canvas, "0", 1035, y - 20, 44, TEXT, Paint.Align.RIGHT, true);
            text(canvas, "0,00 $", 1035, y + 32, 32, MUTED, Paint.Align.RIGHT, true);
        }

        void drawHomeBelowAssets(Canvas canvas, float startY) {
            text(canvas, "Бессрочные фьючерсы  ›", 44, startY, 42, TEXT, Paint.Align.LEFT, true);
            homePerpsCard(canvas, 44, startY + 250, "BTC", "$12K Vol");
            homePerpsCard(canvas, 440, startY + 250, "ETH", "$193K Vol");

            text(canvas, "Заработок  ›", 44, startY + 640, 42, TEXT, Paint.Align.LEFT, true);
            earnCard(canvas, 44, startY + 695, "★", "Заработайте до", "30.82% APY", "on Stargaze");
            earnCard(canvas, 440, startY + 695, "✳", "Заработайте до", "25.43% APY", "on Juno");

            text(canvas, "Эксклюзивно для вас", 44, startY + 1070, 42, TEXT, Paint.Align.LEFT, true);
            rect(canvas, 44, startY + 1125, 992, 360, PANEL, 34);
            glow(canvas, 130, startY + 1205, 70);
            text(canvas, "Meet Tru", 205, startY + 1210, 40, TEXT, Paint.Align.LEFT, true);
            text(canvas, "Новые интеллектуальные", 205, startY + 1265, 32, MUTED, Paint.Align.LEFT, true);
            text(canvas, "возможности кошелька", 205, startY + 1310, 32, MUTED, Paint.Align.LEFT, true);
            strokeRound(canvas, 88, startY + 1380, 904, 92, GREEN, 46);
            text(canvas, "Спросите ИИ", 540, startY + 1439, 36, GREEN, Paint.Align.CENTER, true);
        }

        void homePerpsCard(Canvas canvas, float x, float y, String symbol, String vol) {
            rect(canvas, x, y, 360, 255, PANEL, 28);
            MarketCoin coin = null;
            for (MarketCoin c : visibleCoins()) if (symbol.equals(c.symbol)) coin = c;
            if (coin != null) drawTokenIcon(canvas, coin, x + 44, y + 42, 76);
            text(canvas, symbol, x + 135, y + 92, 38, TEXT, Paint.Align.LEFT, true);
            text(canvas, "Торгуйте " + symbol + " с", x + 44, y + 150, 27, MUTED, Paint.Align.LEFT, true);
            text(canvas, "кредитным плечом", x + 44, y + 188, 27, MUTED, Paint.Align.LEFT, true);
            text(canvas, "до 200x", x + 44, y + 226, 27, MUTED, Paint.Align.LEFT, true);
            text(canvas, vol, x + 44, y + 260, 28, MUTED, Paint.Align.LEFT, true);
        }

        void earnCard(Canvas canvas, float x, float y, String icon, String line1, String line2, String line3) {
            rect(canvas, x, y, 360, 290, PANEL, 28);
            text(canvas, icon, x + 70, y + 78, 58, GREEN, Paint.Align.CENTER, true);
            text(canvas, line1, x + 44, y + 175, 34, TEXT, Paint.Align.LEFT, true);
            text(canvas, line2, x + 44, y + 222, 34, TEXT, Paint.Align.LEFT, true);
            text(canvas, line3, x + 44, y + 266, 30, MUTED, Paint.Align.LEFT, true);
        }

        void glow(Canvas canvas, float cx, float cy, float r) {
            paint.setShader(new RadialGradient(cx, cy, r, new int[]{Color.rgb(245, 215, 255), Color.rgb(115, 185, 255), Color.TRANSPARENT}, null, Shader.TileMode.CLAMP));
            canvas.drawCircle(cx, cy, r, paint);
            paint.setShader(null);
        }

        void drawRewards(Canvas canvas) {
            drawStatus(canvas);
            canvas.save();
            canvas.clipRect(0, 102, 1080, NAV_TOP);
            canvas.translate(0, -scrollY);
            text(canvas, "Награды", 540, 205, 50, TEXT, Paint.Align.CENTER, true);
            drawRewardsHero(canvas);

            rect(canvas, 44, 660, 472, 220, Color.rgb(17, 18, 19), 24);
            strokeRound(canvas, 44, 660, 472, 220, Color.rgb(49, 50, 52), 24);
            text(canvas, "Уровень", 112, 730, 34, MUTED, Paint.Align.LEFT, true);
            text(canvas, "100 XP", 112, 805, 46, TEXT, Paint.Align.LEFT, true);
            text(canvas, "до Bronze", 112, 855, 40, TEXT, Paint.Align.LEFT, true);

            rect(canvas, 564, 660, 472, 220, Color.rgb(17, 18, 19), 24);
            strokeRound(canvas, 564, 660, 472, 220, Color.rgb(49, 50, 52), 24);
            text(canvas, "Баланс XP", 632, 755, 34, MUTED, Paint.Align.LEFT, true);
            text(canvas, "0 XP", 632, 830, 48, TEXT, Paint.Align.LEFT, true);

            text(canvas, "Получите XP", 44, 1000, 46, TEXT, Paint.Align.LEFT, true);
            rewardsTab(canvas, 44, 1078, "Активно", rewardsTab == 0);
            rewardsTab(canvas, 330, 1078, "Прошлые", rewardsTab == 1);
            if (rewardsTab == 0) drawRewardsActive(canvas);
            else drawRewardsPast(canvas);

            text(canvas, "Trust Alpha", 44, 1880, 48, TEXT, Paint.Align.LEFT, true);
            text(canvas, "›", 1000, 1884, 70, MUTED, Paint.Align.RIGHT, false);
            rewardsTab(canvas, 44, 1955, "Активно", true);
            rewardsTab(canvas, 330, 1955, "Прошлые", false);
            canvas.restore();
            drawModernBottomNav(canvas, 3);
        }

        void drawRewardsHero(Canvas canvas) {
            Path back = new Path();
            back.moveTo(410, 410);
            back.lineTo(655, 350);
            back.lineTo(735, 525);
            back.lineTo(475, 585);
            back.close();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.rgb(35, 195, 165));
            canvas.drawPath(back, paint);

            Path wallet = new Path();
            wallet.moveTo(380, 510);
            wallet.lineTo(520, 455);
            wallet.lineTo(695, 500);
            wallet.lineTo(668, 650);
            wallet.lineTo(438, 650);
            wallet.close();
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new android.graphics.LinearGradient(380, 455, 695, 650,
                    Color.rgb(82, 248, 154), Color.rgb(232, 240, 72), Shader.TileMode.CLAMP));
            canvas.drawPath(wallet, paint);
            paint.setShader(null);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            paint.setColor(Color.rgb(40, 95, 255));
            canvas.drawPath(wallet, paint);
            canvas.drawCircle(610, 565, 26, paint);
            canvas.drawCircle(695, 430, 32, paint);
            canvas.drawCircle(555, 385, 38, paint);
            canvas.drawCircle(650, 500, 22, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(22, 116, 248));
            canvas.drawCircle(555, 385, 44, paint);
            paint.setColor(Color.rgb(60, 210, 255));
            canvas.drawCircle(650, 500, 28, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6);
            paint.setColor(Color.rgb(155, 120, 255));
            canvas.drawArc(new RectF(682, 385, 742, 475), 75, 260, false, paint);
            canvas.drawArc(new RectF(635, 455, 735, 545), 210, 230, false, paint);
            paint.setStyle(Paint.Style.FILL);

            rect(canvas, 490, 540, 90, 65, Color.rgb(15, 80, 62), 12);
            text(canvas, "XP", 535, 582, 24, GREEN, Paint.Align.CENTER, true);
            text(canvas, "✦", 420, 610, 32, Color.rgb(20, 55, 130), Paint.Align.CENTER, true);
        }

        void drawRewardsActive(Canvas canvas) {
            rect(canvas, 44, 1210, 992, 520, PANEL, 36);
            rect(canvas, 92, 1270, 185, 150, Color.rgb(45, 74, 86), 24);
            text(canvas, "▱", 184, 1372, 86, GREEN, Paint.Align.CENTER, true);
            text(canvas, "Новые кампании скоро", 350, 1305, 34, MUTED, Paint.Align.LEFT, true);
            text(canvas, "Подпишитесь на нас в", 350, 1370, 44, TEXT, Paint.Align.LEFT, true);
            text(canvas, "социальных сетях и", 350, 1424, 44, TEXT, Paint.Align.LEFT, true);
            text(canvas, "следите за новостями!", 350, 1478, 44, TEXT, Paint.Align.LEFT, true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.rgb(66, 67, 69));
            canvas.drawLine(88, 1575, 992, 1575, paint);
            paint.setStyle(Paint.Style.FILL);
            rect(canvas, 88, 1625, 100, 100, Color.BLACK, 26);
            text(canvas, "X", 138, 1690, 44, TEXT, Paint.Align.CENTER, true);
            text(canvas, "@TrustWallet", 220, 1692, 42, TEXT, Paint.Align.LEFT, true);
            rect(canvas, 890, 1625, 98, 98, Color.rgb(36, 91, 61), 49);
            text(canvas, "→", 939, 1690, 50, GREEN, Paint.Align.CENTER, true);
        }

        void drawRewardsPast(Canvas canvas) {
            rewardCard(canvas, 44, 1210, Color.rgb(0, 100, 72), "Tunz", "3GB", "Free 3GB Global eSIM", "1000XP");
            rewardCard(canvas, 532, 1210, Color.rgb(238, 45, 120), "U", "$50", "$50 hotel coupon", "800XP");
            rewardCard(canvas, 1020, 1210, Color.rgb(38, 150, 200), "4", "weeks", "Travel reward", "400XP");
        }

        void rewardCard(Canvas canvas, float x, float y, int color, String brand, String value, String title, String xp) {
            rect(canvas, x, y, 460, 300, color, 32);
            text(canvas, brand, x + 230, y + 120, 58, TEXT, Paint.Align.CENTER, true);
            text(canvas, value, x + 230, y + 210, 56, TEXT, Paint.Align.CENTER, true);
            text(canvas, title, x, y + 380, 36, TEXT, Paint.Align.LEFT, true);
            text(canvas, xp, x, y + 485, 44, TEXT, Paint.Align.LEFT, true);
            rect(canvas, x, y + 525, 460, 92, Color.rgb(25, 78, 49), 46);
            text(canvas, "Посмотреть", x + 230, y + 584, 36, GREEN, Paint.Align.CENTER, true);
        }

        void rewardsTab(Canvas canvas, float x, float y, String label, boolean active) {
            if (active) rect(canvas, x, y, label.length() > 7 ? 250 : 230, 92, Color.rgb(43, 44, 47), 46);
            text(canvas, label, x + 115, y + 60, 38, active ? TEXT : MUTED, Paint.Align.CENTER, true);
        }

        void strokeRound(Canvas canvas, float x, float y, float w, float h, int color, float radius) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(color);
            canvas.drawRoundRect(new RectF(x, y, x + w, y + h), radius, radius, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        void drawHistory(Canvas canvas) {
            rect(canvas, 0, 0, BASE_W, BASE_H, BG, 0);
            drawStatus(canvas);
            text(canvas, "<", 78, 185, 72, MUTED, Paint.Align.CENTER, false);
            text(canvas, "История", 540, 185, 52, TEXT, Paint.Align.CENTER, true);

            rect(canvas, 44, 300, 992, 112, Color.rgb(35, 36, 39), 56);
            drawSearchGlyph(canvas, 104, 356, MUTED, 0.5f);
            text(canvas, "Поиск", 164, 372, 38, MUTED, Paint.Align.LEFT, true);

            chip(canvas, 44, 475, "Все", true);
            rect(canvas, 180, 475, 190, 70, PANEL, 35);
            text(canvas, "Отправлено", 275, 522, 30, MUTED, Paint.Align.CENTER, true);
            rect(canvas, 390, 475, 190, 70, PANEL, 35);
            text(canvas, "Получено", 485, 522, 30, MUTED, Paint.Align.CENTER, true);
            rect(canvas, 600, 475, 180, 70, PANEL, 35);
            text(canvas, "Обмен", 690, 522, 30, MUTED, Paint.Align.CENTER, true);

            rect(canvas, 44, 695, 992, 520, PANEL, 42);
            drawHistoryEmptyIcon(canvas, 540, 850);
            text(canvas, "Транзакций пока нет", 540, 1010, 48, TEXT, Paint.Align.CENTER, true);
            text(canvas, "После отправки, получения или обмена", 540, 1072, 34, MUTED, Paint.Align.CENTER, true);
            text(canvas, "операции появятся здесь.", 540, 1120, 34, MUTED, Paint.Align.CENTER, true);

            rect(canvas, 88, 1320, 904, 116, Color.rgb(43, 92, 60), 58);
            text(canvas, "Купить криптовалюту", 540, 1394, 40, GREEN, Paint.Align.CENTER, true);
            drawSystemNav(canvas);
        }

        void drawPerpsHistory(Canvas canvas) {
            rect(canvas, 0, 0, BASE_W, BASE_H, BG, 0);
            drawStatus(canvas);
            text(canvas, "<", 78, 185, 72, MUTED, Paint.Align.CENTER, false);
            text(canvas, "История бессрочных", 540, 185, 52, TEXT, Paint.Align.CENTER, true);
            rect(canvas, 44, 420, 992, 470, PANEL, 42);
            text(canvas, "Открытых позиций нет", 540, 570, 52, TEXT, Paint.Align.CENTER, true);
            text(canvas, "Здесь появятся депозиты, сделки и PnL", 540, 650, 34, MUTED, Paint.Align.CENTER, true);
            rect(canvas, 88, 1000, 904, 116, Color.rgb(43, 92, 60), 58);
            text(canvas, "Перейти к рынкам", 540, 1074, 42, GREEN, Paint.Align.CENTER, true);
            drawSystemNav(canvas);
        }

        void drawManage(Canvas canvas) {
            rect(canvas, 0, 0, BASE_W, BASE_H, BG, 0);
            drawStatus(canvas);
            text(canvas, "<", 78, 185, 72, MUTED, Paint.Align.CENTER, false);
            text(canvas, "Управление", 540, 185, 52, TEXT, Paint.Align.CENTER, true);

            text(canvas, "Вид главной", 44, 350, 42, TEXT, Paint.Align.LEFT, true);
            drawLayoutChoice(canvas, 210, 550, 0, "Список", manageLayout == 0);
            drawLayoutChoice(canvas, 540, 550, 1, "Баланс", manageLayout == 1);
            drawLayoutChoice(canvas, 870, 550, 2, "Компактно", manageLayout == 2);

            rect(canvas, 44, 820, 992, 760, PANEL, 34);
            manageRow(canvas, 910, "Скрыть малые балансы", "Не показывать активы с нулевым балансом", hideSmallAssets);
            manageRow(canvas, 1085, "NFT", "Показывать коллекции на главной", !hideNft);
            manageRow(canvas, 1260, "Прогнозы", "Карточки событий и прогнозов", !hidePredictions);
            manageRow(canvas, 1435, "Бессрочные фьючерсы", "Показывать блок рынков с плечом", !hidePerps);

            rect(canvas, 88, 1660, 904, 112, Color.rgb(35, 36, 39), 56);
            text(canvas, "Готово", 540, 1732, 40, TEXT, Paint.Align.CENTER, true);
            drawSystemNav(canvas);
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

        void drawLayoutChoice(Canvas canvas, float cx, float cy, int layout, String label, boolean selected) {
            rect(canvas, cx - 132, cy - 128, 264, 250, selected ? Color.rgb(35, 62, 45) : PANEL, 34);
            strokeRound(canvas, cx - 132, cy - 128, 264, 250, selected ? GREEN : Color.rgb(54, 55, 58), 34);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(selected ? GREEN : MUTED);
            if (layout == 0) {
                for (int i = 0; i < 3; i++) {
                    float rowY = cy - 58 + i * 42;
                    canvas.drawCircle(cx - 70, rowY, 12, paint);
                    canvas.drawLine(cx - 38, rowY, cx + 72, rowY, paint);
                }
            } else if (layout == 1) {
                canvas.drawRoundRect(new RectF(cx - 80, cy - 70, cx + 80, cy + 10), 18, 18, paint);
                canvas.drawLine(cx - 60, cy + 48, cx + 60, cy + 48, paint);
            } else {
                canvas.drawRoundRect(new RectF(cx - 82, cy - 72, cx - 8, cy + 2), 16, 16, paint);
                canvas.drawRoundRect(new RectF(cx + 8, cy - 72, cx + 82, cy + 2), 16, 16, paint);
                canvas.drawRoundRect(new RectF(cx - 82, cy + 18, cx + 82, cy + 68), 16, 16, paint);
            }
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStyle(Paint.Style.FILL);
            text(canvas, label, cx, cy + 158, 32, selected ? GREEN : MUTED, Paint.Align.CENTER, true);
        }

        void manageRow(Canvas canvas, float y, String title, String subtitle, boolean on) {
            text(canvas, title, 88, y, 38, TEXT, Paint.Align.LEFT, true);
            text(canvas, subtitle, 88, y + 48, 28, MUTED, Paint.Align.LEFT, true);
            toggle(canvas, 805, y - 40, on);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.rgb(52, 53, 56));
            canvas.drawLine(88, y + 98, 992, y + 98, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        void drawHistoryEmptyIcon(Canvas canvas, float cx, float cy) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(9);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(MUTED);
            canvas.drawCircle(cx, cy, 78, paint);
            canvas.drawLine(cx, cy - 42, cx, cy + 8, paint);
            canvas.drawLine(cx, cy + 8, cx + 42, cy + 36, paint);
            canvas.drawArc(new RectF(cx - 108, cy - 108, cx + 108, cy + 108), 135, 235, false, paint);
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStrokeJoin(Paint.Join.MITER);
            paint.setStyle(Paint.Style.FILL);
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


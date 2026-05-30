# Canonical Home Reference

Date: 2026-05-29

This file is the single source of truth for the Home top screen. It is based on the user-provided funded Trust Wallet screenshot in the chat on 2026-05-29. Older local captures such as `live_ref2_home.png`, `current_target/05_home_top.jpg`, and `audit_20260527_exact_targets/home_top.png` are historical only and must not be used as the Home top target.

Canvas target: `1080x2400`.

Key target layout:

- Search pill: `x=180 y=150 w=716 h=108`.
- Settings icon center: `x=105 y=198`; red dot near `x=132 y=170`.
- Scan icon center: `x=973 y=200`.
- Wallet chip: `x=450 y=365 w=178 h=82`; copy icon center near `x=690 y=405`.
- Balance text: centered at `x=540`, large, baseline near `650`.
- Red delta: centered at `x=540`, baseline near `720`.
- Quick actions: centers `215, 430, 645, 860`, button top near `790`, button size about `140`.
- Hyperliquid card: `x=44 y=1066 w=992 h=270`.
- Asset tabs text baseline near `1392`; underline top near `1450`.
- Asset rows: first/second/third baselines near `1562`, `1722`, `1882`.
- Perps title baseline near `2110`; Perps card tops must remain visible above bottom nav, around `2165`.
- Bottom nav: top near `2178`.
- Typeface target: SF Pro Display for the large balance/title numbers and SF Pro Text for the rest. Use real packaged weights when available; fallback fonts are only a temporary approximation. Do not use `Paint.setFakeBoldText` as a substitute for real font weights.

Do not replace the Home top with a screenshot asset in the APK. The runtime implementation must remain native Canvas using real token icons.

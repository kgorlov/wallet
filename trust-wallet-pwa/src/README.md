# Trust Wallet Web Source Layout

Runtime stays as a single generated `dist/app.bundle.js` for PWA, iOS Safari, and Android WebView reliability.
`app.js` is only the small browser module entrypoint.
Edit source files in this directory, then run:

```powershell
.\trust-wallet-pwa\scripts\build-web.ps1
```

File roles:

- `00_app_state.js`: constants, DOM handles, asset registry, app state, market data.
- `10_core_runtime.js`: asset loading, viewport scaling, canvas primitives, wallet math helpers.
- `20_shell.js`: root `draw()` router, status bar, direct home repaint.
- `30_home.js`: Home screen and home-only cards/rows.
- `40_markets_rewards_more.js`: Popular, market detail, Rewards, More, History, Manage.
- `50_sheets.js`: modal sheets for send/receive/buy/settings/search/confirm/history details.
- `60_trade.js`: Trade sheet, Swap, Perps, Predictions.
- `70_components.js`: bottom nav and reusable drawing components/icons.
- `80_interaction.js`: tap routing, sheet routing, editor actions.
- `90_boot.js`: pointer listeners, resize hooks, asset boot, service worker/cache reset.

Keep screen-specific drawing and hitboxes in the same domain file so visual changes and tap behavior move together.

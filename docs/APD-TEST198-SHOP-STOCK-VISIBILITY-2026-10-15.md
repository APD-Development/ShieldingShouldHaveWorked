# TEST198 — shop-stock visibility boundary

Date: 2026-10-15
Branch: `codex/apd-phone-fixes-2026-10-09`
Parent: `c03905674d306d3d95f8c90d04a3a5e522d5349d`
Compiled application source remains: `364fd4724b79308fb6a13478a211bcd7599b2f7b`

## Reproduction

The queued shop-stock candidate already has a canonical recorder path. At each ordinary item poll, `ItemTracker.scanFloor` considers only a heap that the player has seen, is not a faint hidden outline, is currently inside `heroFOV`, and exposes its top item. A `FOR_SALE` heap is classified as `ItemEvents.SEEN_SHOP`; chests and unseen or out-of-view heaps remain excluded.

The concrete omission was retained coverage. Existing tests verified ordinary floor items and hidden heaps, but did not bind shop classification to the public/internal information boundary.

## Change

Added `ItemLifecycleTest.visibleShopStockIsClassifiedWithoutLeakingHiddenTruth` using a real unidentified +4 cursed `RingOfWealth` in a visible `FOR_SALE` heap.

The regression requires:

- exactly one public `ITEM_SEEN` with `SEEN_SHOP`, floor and cell;
- only the displayed unidentified ring name/category, with unknown level and curse;
- no “Wealth” class-name leak in public state;
- the true class, +4 level and curse only in the separate INTERNAL `ITEM_REGISTERED` event.

No production source, telemetry format, gameplay, RNG, save, scoring, rules, application ID or signing behavior changed.

## Verification request

Run the complete 1,611-test core suite through the existing core-only workflow. Do not build an APK; reuse the already verified TEST197 application artifact. Hosted result is pending.

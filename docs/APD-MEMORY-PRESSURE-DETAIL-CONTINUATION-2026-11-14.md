# APD memory-pressure detail continuation, November 14, 2026

Recorded Eastern: Saturday, November 14, 2026 at 10:07:53 PM EST

## Scope and ownership

Source baseline: `c8b40031d3802f5c7dcf014aa15f49468df56fd4` on `codex/apd-phone-fixes-2026-10-09`, observed immediately before creating isolated work branch `chatgpt/apd-memory-pressure-trace-20261114-2200`. This is a narrow follow-up to merged PR #147's bounded local heap diagnostics for the owner's C467 OutOfMemoryError. The OOM allocation stack in CharSprite.update does **not** establish the retention source; this change makes future pressure callbacks more useful, without claiming a crash fix.

## Evidenced gap and source changes

The existing bounded memory history persisted `phase=android.trim_memory` but omitted the Android trim **level**; the separately logged `android.trim_memory level=N` recent event could be evicted by high-volume input. The existing `onLowMemory` callback did not record any memory-history sample, so its evidence was likewise lost after input flood. Keep the phase and all old memory sample numeric fields intact. Add an optional, integer-only `trim_level=N` suffix to the Android trim sample, capture the low-memory callback in bounded memory history, and add one actual filesystem regression proving both survive 500 intervening touch events. No gameplay, score, saves, archive, signing, variant, or original APK changes.

## Execution receipts and limits

- Start development head: `c8b40031d3802f5c7dcf014aa15f49468df56fd4`.
- Source code and regression authored on isolated branch through `242e7be77e04c021a91458462e6af9edeafee6f5`.
- This document is written **before** new exact-source CI and Android compilation. No test result is claimed here.
- The earlier C471/P471 signed-pair workflow [43667638390](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43667638390) passed on the separate previous source `02e104dde6f34dc4c0eeb3a713610651058c2d1d`. It does not test this follow-up.
- There is no new signed, natively accepted or published APK in this work package. The C471/P471 candidate and its pending native acceptance remain untouched.
- Next safe step: require a full core suite and desktop/Android Java compilation for the exact source; inspect failure artifacts if needed; only integrate through a current-head-checked non-force fast-forward after success. Keep all other claims and releases intact.

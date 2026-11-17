# APD vault stealth-patrol waypoint index safety, November 17, 2026

## Isolated work claim, 09:10 EST

Morning/afternoon coding worker. Started from verified authoritative `codex/apd-phone-fixes-2026-10-09` at `4bda78ba7de02a350be9d51d61386f61b1517851`, descendant of the complete C466R7+C485 integration `c6fa033167789ef9f2fc6f31bcff39c824dfb4db`. Read `AGENTS.md`, `docs/APD-486-R7-INTEGRATION-VERIFIED-2026-11-16.md`, the previous Mob retarget continuation, current open PRs, live branch and source/CI before claiming this scope.

**Claimed, distinct scope:** `Mob.setupStealthGameplayWanderPositions`, loading `wander_positions`/`wander_pos_idx` in `Mob.restoreFromBundle`, and narrowly focused headless vault-patrol waypoint regressions. This does not modify R7 mechanics, patrol routes on valid data, probabilities, numeric balance, scoring v6, history format, edition flags, signing, or APK artifacts. The long-standing open Extended-report PRs #229/#230, older playback/native and APK worker claims remain untouched.

## Source-evidenced failure

Vault stealth patrols persist both a waypoint array and `wander_pos_idx`. On restore, the index is assigned without checking it against the restored array's actual length. The wandering state then directly indexes `wanderPositions[wanderPosIdx]`. Missing/empty waypoint arrays or a negative/too-large index from damaged or old saved state can therefore crash the next stealth patrol waypoint lookup. Generated patrol positions from the current room classes use valid nonempty lists; their behavior must not change.

## Planned narrow repair and release boundary

Normalize empty/null waypoint lists to the existing stationary-patrol fallback, and reset only out-of-range indices to the first available waypoint, both at setup and restore. Retain all valid indices/routes byte-for-byte in behavior. Add headless tests for valid waypoints, negative/high indices, empty input, and a save/restore index injected outside the restored waypoint bounds. Verify whole-core JUnit and desktop/Android Java compilation by isolated CI before any non-force expected-head merge; record exact artifacts/results. Source-only, never claim native, signed APK or release acceptance.

## Completion

Pending actual source edit, regression results, CI and guarded integration.
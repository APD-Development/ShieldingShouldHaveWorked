# APD Rat King Quick Search shadow-cast reuse, November 16 2026 (EST)

## Work claim and safe baseline
- Evening source-only APD coding began approximately 11:02 PM Eastern, November 16 2026. No separate Codex task, APK build, release, schedule changes or changes to any other game.
- Read current `AGENTS.md`, `docs/APD-486-R7-INTEGRATION-VERIFIED-2026-11-16.md`, latest Rat King / FullPackSwap handoffs, open pull requests, primary head and recent CI. Protected verified two-parent R7 + C485 source `c6fa033167789ef9f2fc6f31bcff39c824dfb4db` from PR #228.
- Live primary `codex/apd-phone-fixes-2026-10-09` at start: `5c4d2a30bb75ccd1e6e7e8905fab9f9d06432693`. Existing merged #233, #234 and #236 fixes retained; #229/#230 report exports, #226/#223/#220/#217/#214 earlier APK candidates, #211/#212 native work, independent release and playback claims excluded. No overlapping active claim found for search-sight reuse.
- Isolated branch `chatgpt/apd-rat-quicksearch-sight-reuse-20261116-2300`; [PR #238](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/238).

## Source change and independent regression
- Existing `RatKingQuickSearch.searchFrom` allocated a new floor-sized line-of-sight array every time it priced a candidate search location. Warden also cloned and rewrote the entire LOS blocker mask every candidate. A pure Dijkstra quote may visit many locations while terrain, Warden subclass, and LOS blockers stay unchanged.
- Production commit `9d8456fface74cd2f2da20e57b02d242adb3cc02` uses one private, quote-scoped `SightScratch`. The sight array is lazily allocated once then explicitly zeroed before *every* subsequent shadow cast, retaining the original clean-slate semantics. The Warden grass-blocking clone is computed only on the first actual sight cast. Non-Warden retains the original LOS blocking array. If Foresight or magic sense ends evaluation before a shadow cast, buffers are not allocated. No persistent/global cache, no player-visible rule or timing change.
- Regression commit `17d214492cbd95aaea22d3fe4e52ccd65f0b218d` adds `reusedQuickSearchSightExactlyMatchesFreshCastsIncludingWardenGrass`. An independent fresh-array, fresh-Warden-clone oracle compares complete sight masks against scratch reuse for multiple origin locations, alternating broad/narrow radii, walls, a narrow gap, HIGH_GRASS, FURROWED_GRASS, and both Warden and non-Warden modes over repeated passes.
- Workflow commit `78d8b39c8fa58ca3e232436088b9451a72abe9b4` runs the whole core test suite, desktop classes, Android debug/release/instrumentation Java compilation, exact baseline ancestry and both the new visibility regression and prior Swiftness hostile-proximity regression. **No APK assembly or signing**.

## Actual verification and merge
- Exact source CI: [run 44014581016](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/44014581016) completed successfully at about **11:07:32 PM EST November 16, 2026**, testing source head `78d8b39c8fa58ca3e232436088b9451a72abe9b4`.
- Raw job log confirms **3,546 core tests across 540 suites, zero failures/errors/skips**; full desktop and Android debug/release/instrumentation Java compilation succeeded. Both focused regressions ran. JUnit artifact `14298790498`, `apd-rat-quicksearch-sight-junit` (416,004 bytes at inspection) was retained by GitHub Actions, unexpired.
- Rechecked primary exact unchanged head `5c4d2a30bb75ccd1e6e7e8905fab9f9d06432693`, PR head and mergeable state after CI. Promoted draft to ready, merged by GitHub with **expected PR head SHA** `78d8b39c8fa58ca3e232436088b9451a72abe9b4`, no force push. GitHub independently reported and primary live ref confirmed merge commit `5d5a01cea06f31df55a5150a3bb92173cc0f5cf7` at **11:08:17 PM EST November 16**.
- This handoff is documentation only; it does not change the CI-verified game source.

## Limits and next safe work
- This is verified *source* allocation reduction and exact oracle compatibility, not measured device FPS, battery, memory, input-lag, OOM, install-over/save, phone or native Android acceptance. It does not prove a C467 OOM fix.
- No signed APK, new release, or build-number increase. Previously published C488Y/P488U and C489H/P489H bytes remain unchanged. Dedicated APK worker owns future build/signing/native acceptance and release.
- Preserve all R7 weapons/shields/sprites, FullPackSwap, ThrowAim, Greater Metamorphosis, adaptive search, playback, scoring v6, logs/history/saves, C485+ changes, original edition flags and signer.
- Future independent coding must reread live primary, owner approval, PR/CI/other worker claims, avoid repeating completed #236 and #238 Rat King optimizations, and isolate an evidenced defect with new regression. No automation schedules or other projects were touched.

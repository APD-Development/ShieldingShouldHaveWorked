# APD vault stealth west-facing angle seam: verified source continuation (November 17, 2026 EST)

## Boundary, branch and ownership
Morning APD coding execution began around 2:59 AM Eastern, November 17, 2026. Starting authoritative development head was `eafc8ac650c97b16566161159e62dd228b100378` on `codex/apd-phone-fixes-2026-10-09`, already containing the complete C466R7+C485 integration `c6fa033167789ef9f2fc6f31bcff39c824dfb4db` and recent merged Mob changes #240-#242. Read `AGENTS.md`, `docs/APD-486-R7-INTEGRATION-VERIFIED-2026-11-16.md`, prior Mob handoff, Mob/Actor/PointF source, GitHub PR claims, code search and CI. Open PRs had no overlapping stealth-angle work. Work stayed entirely within the user's repository; no external source resources were consulted.

Isolated branch: `chatgpt/apd-stealth-angle-wrap-20261117-0300`. Draft PR [#243](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/243).
- Production fix commit: `96293c07fc2810d28d547ca6f462d20a20be35cb`
- Regression commit: `b5bb1c9be5317cd2fbba2b84030c747b838b52d3`
- Initial CI workflow commit: `5362d5ce620918e589c9ee33e75a717d7feeb12f`
- Exact source-tested workflow head: `4facf6fc4596ba2661818b38577507a95647ef35`

## Evidenced existing AI bug and bounded repair
`Mob.StealthGameplayWandering.detectionChance` compares the direction of patrol movement with the direction to an enemy. `PointF.angle` returns an atan2 value between -180 and +180 degrees, but two sight-cone checks used raw `Math.abs(enemyDir - movementDir)`. For a patrol walking west, an enemy at northwest (-153 degrees) was incorrectly classified as behind (raw gap ~333 degrees), while a symmetric enemy southwest (+153 degrees) correctly fell inside the 75-degree forward cone. The same seam corrupted the wider 150-degree detection cone.

The fix computes the shortest circular separation once: `angleDiff = Math.min(Math.abs(enemyDir - movementDir), 360f - Math.abs(enemyDir - movementDir))` (implemented with an intermediate so only one Math.abs call). The original 37.5 and 75 degree half-cone thresholds, 6-cell forward limit, stealth and detection probabilities are unchanged. Only directions near the signed-angle boundary now receive the intended existing probabilities. No new AI system, gameplay balance, RNG call, assets, save/history/scoring, edition, application ID, signers or release artifacts changed.

Three headless regressions in `MobStealthAngleSeamTest` cover front vision symmetry across ±180, wider cone symmetry, and unchanged low detection behind the patroller.

## Actual CI result
[GitHub Actions run 44043521723](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/44043521723) on source-tested commit `4facf6fc...` completed successfully around 3:06 AM EST November 17. Job `verify-source` (133903542500) checked exact mandatory C486 and starting Mob ancestry, then ran the full Gradle command:

`./gradlew :core:test :desktop:classes :android:compileDebugJavaWithJavac :android:compileReleaseJavaWithJavac :android:compileDebugAndroidTestJavaWithJavac --no-daemon`

The job log reported `BUILD SUCCESSFUL in 2m 32s` and `APD_STEALTH_ANGLE_SEAM_JUNIT {'tests': 3557, 'failures': 0, 'errors': 0, 'skipped': 0, 'suites': 544}`. The CI script checked that all three new regression method names appear in JUnit results and that the shortest-angle predicate replaces raw degree comparison. CI test XML artifact: `14310646874`, `apd-stealth-angle-seam-junit` (GitHub reports 418,451 bytes). This is verified source and Java compilation, NOT Android native instrumentation execution.

## Integration and next release boundary
This continuation was documented after CI and does not change the source or tested regression. Before merging, reread the authoritative development head, verify only this documentation differs from the exact CI-tested source, and use expected-head non-force integration. Do not overwrite newer worker changes, steal active claims, or merge an incompatible tree. Preserve all R7 mechanics and C485 history/scoring, recent Mob fixes, and independently active report ZIP, trap-room, native and 4 PM APK release work.

**No APK was assembled, signed, installed, qualified or published in this execution.** Dedicated 4 PM APK task owns Developer/Alpha packaging, original-signer/native/visual/install-over verification and release. This fixes an evidenced directional-detection bug, not a demonstrated phone FPS or battery gain.

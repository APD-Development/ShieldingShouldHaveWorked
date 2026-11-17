# APD Mob nearest attackable target and hero-tie fix: verified source continuation (November 17, 2026 EST)

## Owner-authorized scope and lineage

This source-only coding package began with the authoritative APD development head `b4c345fb3f4760ba8513567616a61f6452619e83` on `codex/apd-phone-fixes-2026-10-09`, following verified C466R7+C485 integration `c6fa033167789ef9f2fc6f31bcff39c824dfb4db` and merged AI fixes #240-#243. The worker read `AGENTS.md`, `docs/APD-486-R7-INTEGRATION-VERIFIED-2026-11-16.md`, the nearest-target and stealth handoffs, Mob/Actor sources, the actual existing nearest-target regression, current branch state and open PR claims before writing. No other project's repositories, schedules, external upstream sources, native-release claims, signing or APK artifacts were touched. No separate Codex task was launched.

- Isolated branch: `chatgpt/apd-attackable-target-order-20261117-0400`
- Review: [PR #244](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/244)
- Production change: `a78a7e98dedd720dc5adf731a75dd6fb72809921`
- Regression tests: `09c6bb51f8832bfa22f37d13baa9cbbdb163d85d`
- Exact tested source/workflow head: `55f94f834a253f9b4044f49a927572ac8c6bd408`

## Evidenced existing target-choice defect

In `Mob.chooseClosestEnemy`, a later candidate that was attackable would replace an already selected attackable target regardless of path distance, because the final conditional was `(canAttack(curr) && canAttack(closest))` without a distance requirement. An attackable mob at path-distance 7 could therefore replace an attackable mob at path-distance 2. The same branch also let a later equidistant attackable mob replace an attackable hero, contradicting the method's documented hero tie preference.

The narrow fix permits this final replacement only at *equal* path distance and refuses to displace an already selected hero with a non-hero during an equal-distance attackable tie. It retains all pre-existing priorities: attackable over unattackable regardless of distance; nearer candidates when priorities permit; hero preferred on equal paths; and the pre-existing last-candidate tie behavior among equally distant attackable non-heroes. No new aggression behavior, balance numerical values, RNG sequence, FOV/pathing rules, stats, saves, history/scoring, R7 mechanics, item/drop rules, application ID, edition or signer changed.

Three ordered regressions were added to `MobNearestTargetDistanceTest`: a farther attackable target cannot replace a nearer attackable target; an equidistant attackable hero cannot be displaced by a later mob; and equal-distance non-hero attackable ties retain the pre-existing ordering. The previous nearest-target and attackability tests are untouched.

## Exact CI actually completed

[GitHub Actions run 44051814764](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/44051814764), source SHA `55f94f834a253f9b4044f49a927572ac8c6bd408`, completed `success` on November 17, 2026. Job `verify-source` (133931225506) independently passed mandatory combined integration and existing AI ancestry checks, then ran:

`./gradlew :core:test :desktop:classes :android:compileDebugJavaWithJavac :android:compileReleaseJavaWithJavac :android:compileDebugAndroidTestJavaWithJavac --no-daemon`

The job log reports `BUILD SUCCESSFUL in 1m 32s`. Parsed full JUnit XML count: **3,560 tests across 544 suites; 0 failures, errors or skips**, with all three new test methods verified present. Developer Java compilation, Android debug/release and Android instrumentation Java *compilation* succeeded. Full test XML artifact: `14314555958` (`apd-mob-attackable-order-junit`, 417,791 bytes). Actual native Android instrumentation execution was not performed.

## Integration and next worker

At completion of CI and before this documentation update, a fresh GitHub read still placed authoritative branch `codex/apd-phone-fixes-2026-10-09` at `b4c345fb3f4760ba8513567616a61f6452619e83`. This documentation commit is not a change to the CI-tested gameplay source. Only the two scoped production/test files and the new scoped workflow differ in the verified source tree.

For integration, recheck authoritative head and open work claims, then perform expected-head non-force integration without replacing concurrent work. Confirm merged source tree matches the tested source, except for this documentation. If another worker moved the head, do not overwrite; reconcile first. Preserve R7/C485 verified integration and all independently active report ZIP, trap-room search, personal playback, native and release packages. The dedicated 4 PM APK task owns freezing the next Developer/Alpha source, exact original signer, native/visual/install-over/save acceptance and release publication.

**No APK was assembled, signed, installed, qualified, or published in this execution.** This is a verified source fix, not a measured FPS/battery improvement.

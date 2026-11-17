# APD unreachable-target replacement sight correction: source-verified continuation (November 17, 2026 EST)

## Scope and baseline

At approximately 4:59 AM EST, this APD coding execution re-read `AGENTS.md`, `docs/APD-486-R7-INTEGRATION-VERIFIED-2026-11-16.md`, current Mob source/regressions, the latest PR #244 handoff, live primary branch and open PR claims. The authoritative primary was `codex/apd-phone-fixes-2026-10-09` at `7da7eee1f3867bd0f9677a5df964c589a4a1aeee`. This includes the confirmed combined C466R7+C485 integration `c6fa033167789ef9f2fc6f31bcff39c824dfb4db` and AI fixes #240-#244.

The only edited production path is `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Mob.java`. Work was isolated on `chatgpt/apd-unreachable-retarget-fov-20261117-0500`; review is [PR #245](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/245). Existing report ZIP / action history work in draft PRs #229/#230, trap-room work, original-signer/native-release candidates, and other package claims were left untouched. No separate Codex task, upstream research, other game changes, or schedule changes occurred.

## Evidenced bug and bounded fix

`Mob.Hunting.handleUnreachableTarget()` replaces `enemy` with `chooseEnemy()` when the original target cannot be approached. The subsequent recursive `act(enemyInFOV, justAlerted)` erroneously passed the **original target's** visibility state to the replacement. Consequently a newly visible replacement could be acted on as if unseen, leaving obsolete chase coordinates, or an unseen replacement could inherit the original target's visibility.

The one-location production fix recomputes the replacement's visibility from `enemy.isAlive() && fieldOfView[enemy.pos] && enemy.invisible <= 0`, exactly matching the live `Mob.act()` visibility rule, and passes the updated boolean into the existing guarded retry. It does not alter the choice of replacement, movement/pathfinding, attack priorities, numerical FOV or combat rules, damage, RNG, turn costs, scoring v6, save/history, R7 graphics/FullPackSwap/ThrowAim/Greater Metamorphosis/search/playback, edition IDs or signing.

A new headless `MobUnreachableRetargetVisibilityTest` records the inner Hunting retry's actual visibility parameter in four conditions: newly visible target after an unseen old target; unseen target after a seen old target; invisible target; dead target. The tests assert correct selected enemy and retry invocation as well.

## Exact GitHub evidence

- Initial isolated production commit: `f78f579c2a63f4c4b7fe568c3149c32fba36ab1d`.
- Four-test addition: `44ee54921ea8511f06e70605ada54d2ff276d689`.
- Scoped workflow/tested GitHub head: `5a3f163a6881b936a1f016f2facc2cf7953977de`.
- [Actions run 44061026734](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/44061026734), job `133962635768`: **completed success** November 17, 2026 at approximately 5:04 AM EST. The complete `./gradlew :core:test :desktop:classes :android:compileDebugJavaWithJavac :android:compileReleaseJavaWithJavac :android:compileDebugAndroidTestJavaWithJavac --no-daemon` invocation reported `BUILD SUCCESSFUL in 1m 38s`. Parsed JUnit XML: **3,564 tests across 545 suites; zero failures, errors or skips**. All four new method names were explicitly required in the passing workflow check.
- Retained full-JUnit XML artifact `14318007371` (`apd-unreachable-retarget-fov-junit`, 418,351 bytes).
- Source-only validation. Android debug/release/instrumentation Java *compilation* passed, but native Android instrumentation **execution**, actual install, visual review, save continuity and signed APK publication were not part of this coding package.

## Integration and next safe work

At about 5:04 AM EST the dedicated retarget-visibility CI had completed green. Re-read the authoritative head immediately before merging #245; do not force-push or rewrite another worker's head. This documentation is a non-production continuation commit after the exact-source test and does not alter tested game source. If the main branch moved, reconcile rather than overwrite. Verify the merged production/test/workflow files match the tested branch; record a final merge receipt when known.

Keep independent report ZIP, trap-room, personal playback, original-signer native and dedicated 4 PM APK publication scopes intact. The next safe source-development worker can inspect other approved bug/performance targets without duplicating this correction. **This source execution did not build, sign, native-test, qualify, release or publish a new Developer/Alpha APK.**

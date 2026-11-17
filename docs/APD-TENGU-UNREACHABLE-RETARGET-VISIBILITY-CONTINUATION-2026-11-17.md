# APD Tengu unreachable-target visibility retry: continuation, November 17, 2026 EST

## Verified scope and ownership

At approximately 06:01 EST, the morning APD coding worker verified authoritative `codex/apd-phone-fixes-2026-10-09` at `7e7102851a5148e299f842a69456d82b75d925db`, a descendant of complete C466R7+C485 integration `c6fa033167789ef9f2fc6f31bcff39c824dfb4db` and merged enemy AI PR #245. Read root `AGENTS.md`, `docs/APD-486-R7-INTEGRATION-VERIFIED-2026-11-16.md`, older continuation/claims, open PRs, `Mob.java`, `Tengu.java` and `MobUnreachableRetargetVisibilityTest.java` from the authenticated private repository. Existing Extended report PR #229/#230, native APK acceptance claims, personal playback, and other independent work packages are excluded. Do not duplicate their work.

Isolated branch `chatgpt/apd-tengu-retarget-visibility-20261117-0600` started from the exact authoritative SHA. Review [PR #246](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/246). No default-main writes, overlapping production edits, changed automation, or separate Codex tasks.

## Root cause and exact change

Merged #245 fixed stale sight flags in the general `Mob.Hunting.handleUnreachableTarget`, but `Tengu.Hunting` overrides that method. Tengu switches `enemy` from the unreachable old target to `chooseEnemy()` yet passed the old `enemyInFOV` value to recursive `act`. A visible replacement could fail to be attacked, and an unseen, invisible, or dead replacement could inherit sight of the previous target. This is a separate and directly observed source defect, not an invented gameplay mechanic.

The Tengu-only production edit rechecks `enemy.isAlive() && fieldOfView[enemy.pos] && enemy.invisible <= 0` after replacement and passes the updated value into the existing retry. It leaves the boss's attack/ability priority, targets, RNG, cooldowns, turn spending, animations and all numerical rules untouched. Four new headless `TenguUnreachableRetargetVisibilityTest` regressions exercise the actual Tengu hunting state, including visible, unseen, invisible and dead replacement targets. The test double suppresses audiovisual and ability side effects and records actual attacks.

- Initial source commit: `65736ac257ed6bb08aa5b99229ba9609427db724`.
- Four-test commit: `e0192223db55ea94a262dd25548aec6a5f5d1fe6`.
- Exact-source workflow commit: `4cc17462c16e01de5ae3f7566c55384d4bfb3c92`.
- Initial [GitHub Actions CI run](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/44070567569) is **running, not yet verified**, at the time of this initial continuation record.
- Scoped CI requires combined source ancestry, four named new JUnit tests, at least 3,568 tests across full suite with zero failures/errors/skips, and desktop plus Android debug/release/instrumentation Java compilation.

## Integration gate

Check Actions run #44070567569 to a terminal state, inspect failure logs and fix/test as needed; do not claim green until actually observed. A source-only pass is not native Android instrumented execution or APK release acceptance. On exact-source CI success, recheck live shared development branch head and claims, ensure PR's tested sources are identical, then integrate by non-force expected-head. Preserve full R7/C485, score v6, saves, histories, original signer, edition identity, prior APK bytes, and all other workers' release/build claims. Do not build or publish an unqualified APK from this source-only package. Record final merge evidence separately.

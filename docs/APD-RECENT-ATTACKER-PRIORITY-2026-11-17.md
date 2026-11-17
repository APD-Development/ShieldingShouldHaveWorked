# APD recent-attacker target ordering: November 17, 2026

## Actual morning/afternoon work claim and scope

This isolated coding session began about **11:03 AM EST, November 17, 2026**, from authoritative branch `codex/apd-phone-fixes-2026-10-09` at exact head `4037667093294d356b46206c50cdcf0e19642535`. Read `AGENTS.md`, `docs/APD-486-R7-INTEGRATION-VERIFIED-2026-11-16.md`, recent Mob AI handoff/PR #252 and current open work claims/PRs before editing. The mandatory complete C466R7+C485 ancestor is `c6fa033167789ef9f2fc6f31bcff39c824dfb4db`; the CI workflow explicitly verifies both ancestors.

Branch `chatgpt/apd-recent-attacker-priority-20261117-1103`, [PR #254](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/254). This claim covers only `Mob.Hunting.handleRecentAttackers()` selection among multiple already-eligible recent attackers, five JVM regressions and its dedicated workflow. It does not claim any Extended report PR #229/#230, personal playback, native C480 verifier #211/#212, or signing/APK work.

## Evinced existing AI bug and bounded behavior correction

The old loop replaced `enemy` immediately whenever a recent attacker was attackable **or** closer than the *currently stored* `enemy`. Because the stored enemy changed during iteration, a later close but unattackable attacker could overwrite an earlier attackable choice, and a later *farther* attackable attacker could overwrite a nearer attackable choice. The result depended on the hit-list iteration order rather than the intended attackability / proximity priority.

The revised loop keeps the original current target fixed as the eligibility comparison, then chooses the nearest eligible **attackable** recent attacker if one exists, otherwise the nearest eligible nonattackable attacker, retaining the first seen on equal-distance ties. One recorded attacker retains the existing eligibility and outcome. All original active / registered actor, alignment, field-of-view, invisibility and charm guards remain unchanged; the recent-attacker list still clears after assessment. `canAttack` and distance are evaluated once per otherwise-eligible candidate. No attack formula, attack range, damage, detection, turns, RNG, gameplay numeric constants, new items or mechanics, existing target sources or save/scoring format changed.

The new `MobRecentAttackerPriorityTest` has five actual headless cases: an attackable candidate beats a later closer nonattackable candidate; nearest attackable wins in either order; nearest eligible nonattackable wins; an unattackable target farther than the original enemy is refused; and tied candidates retain first-in-order selection. Existing earlier recent-attacker roster tests are unchanged.

## Source commits and exact verification

- Production change: `452b0fd397669cf6001b363aca47135d4f53ff80`.
- New regression fixture: `611da81ccaab10fc5654a846510289acd83089d9`.
- Dedicated source CI workflow/exact tested head: `fde598e4fa1c48b7b152f722a045af90c3d601e4`.
- [Exact GitHub Actions full-source CI run 44122110714](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/44122110714) finished successfully on the tested head. Core test suite: **3,585 tests / 549 suites, zero failures/errors/skips**, including all five regressions. Gradle logged `BUILD SUCCESSFUL in 2m 34s`. Desktop Java classes plus Android debug, release and instrumentation Java compilation all completed successfully; the JUnit count was independently reported by the post-build workflow assertion at **11:10:32 AM EST**. Build-and-test task started at **11:07:57 AM EST**.
- JUnit artifact `14345322855`, `apd-recent-attacker-priority-junit`, 422,105 bytes, GitHub SHA-256 `e76ba37a90e732f62b3284fb73c78b1027ab2c6fa5337ee147fb2eecc5879fd7`.
- All six existing intersecting Mob CI runs on the *same exact tested head* also completed **success**: #44122115580 unreachable visibility; #44122110480 attackable-target ordering; #44122110627 retarget guard; #44122110424 sleeping hostile detection; #44122110745 vault waypoint restore; #44122110990 stealth angle boundary.
- Exact branch diff before this documentation: only `Mob.java`, `MobRecentAttackerPriorityTest.java`, and `.github/workflows/apd-recent-attacker-priority-ci-20261117.yml`; three commits, ahead 3, behind 0 from the authoritative head. This documentation is not part of the tested game or JUnit production bytes.

## Integration and release boundary

Before integrating, re-read live authoritative head, open PRs and tested branch, verify the production/test blob identities against CI-tested `fde598e4`, then use only a non-force **expected-authoritative-head** two-parent integration. Record actual merged commit and source parity in the PR receipt rather than assuming the head remained unchanged. If CI, source parity or the expected-head lease changes, stop integration and preserve this isolated branch.

No APK was assembled, original-signed, installed, natively exercised, visually accepted or published by this worker. This fix does not prove a measurable speed/FPS gain on the RedMagic. The dedicated 4 PM APK worker owns the separate Developer/Alpha release gate, original signer and install-over/save checks. All complete R7 items, search, playback, scoring v6, C485+ history/save/diagnostic work, editions and signing are inherited and unchanged. Next session should inspect fresh live claims and select a distinct evidenced source defect, not repeat this priority repair.

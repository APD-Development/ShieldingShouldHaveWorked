# APD mob nearest-target distance: verified source integration (November 17, 2026 EST)

## Scope and exact ancestry

The midnight–3 PM recurring coding worker conducted one APD-only source coding execution starting around **1:00 AM EST, November 17, 2026**. It reviewed the authoritative branch, `AGENTS.md`, the verified full C466R7+C485 integration handoff, merged roster-membership PR #240, relevant source, recent open PR claims, and CI before any write. No separate Codex task, other game, research automation, schedule change, APK signing or release work was performed.

- Authoritative baseline at start: `fa49a12f1cf675195c650452aa508da5ed959734` (merged roster membership PR #240).
- Required complete R7+C485 integration ancestor: `c6fa033167789ef9f2fc6f31bcff39c824dfb4db`.
- Isolated implementation branch: `chatgpt/apd-mob-nearest-target-20261117-0100`, [PR #241](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/241).
- Production source commit: `1e04d0cbb357431e1894c49f95335e76ee620670`.
- Regression test commit: `8841e1fb298271f8d9ad61983d16354295dffffa`.
- CI workflow commit and exact tested source head: `a112e857db744b069bb9237b46180af8f644eb98`, tree `085d143aa3ebe037a1a2fe1dada3236500c9736b`.
- Guarded two-parent merge commit: `56b28b65f0f3e6adc2e95ed7850dd3cc9901ae2c`. First parent is original authoritative head `fa49a12f...`; second parent is exact tested feature head `a112e857...`. Merged tree is byte-identical to the CI-tested feature tree. Non-force primary ref update explicitly checked the expected original head and succeeded. Readback confirmed primary ref and [PR #241](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/241) closed/merged at the exact merge commit.

## Evidenced AI logic bug and code change

In `Mob.chooseEnemy`, the existing nearest-target loop stored the first candidate's `closestDist`, then changed `closest` without updating that distance on a later selection. If candidate distances arrived as **10, 3, 7**, the AI chose the distance-3 target, but then could incorrectly replace it with the farther distance-7 target by comparing against the stale distance 10.

The existing selection loop is extracted into `Mob.chooseClosestEnemy(Iterable<Char>)`, called after the same `PathFinder.buildDistanceMap`. Both branches that replace the closest candidate now update `closestDist` together with `closest`. Existing attackable-target precedence, hero tie behavior, afterimage substitution, candidate source, visibility, pathfinder and RNG calls remain unchanged. There is no authorized new AI mechanic, balancing change, altered enemy stats, score, saves, history, edition, app ID or signer.

`MobNearestTargetDistanceTest` adds three deterministic ordered-candidate regressions for the 10→3→7 nearest choice, hero tie preference and attackable-target precedence. The workflow checks combined-baseline ancestry and a complete core/Android Java/desktop compilation gate.

## Verification actually performed

Exact-source [GitHub Actions run 44028387671](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/44028387671) started at **1:03:23 AM EST** and completed **1:06:16 AM EST**, November 17, 2026, with terminal `success` on `a112e857db744b069bb9237b46180af8f644eb98`. Its verified job log reports `BUILD SUCCESSFUL in 2m 9s` and exact JUnit counts: **3,551 core tests / 542 suites, zero failures, errors or skips**. All three new nearest-target tests were required by the successful workflow's JUnit gate. Desktop classes and Android debug, release and androidTest Java compilation passed. Artifacts include JUnit XML `14304297259` (`apd-mob-nearest-target-junit`, 417,004 bytes). This worker verified the workflow metadata and exact counts in the job log; it did not independently download and re-sum all JUnit XML.

A separate GitHub compare confirmed exactly these scoped differences from the baseline: `Mob.java`, `MobNearestTargetDistanceTest.java`, and the task-specific CI workflow. After non-force integration, GitHub authenticated readback confirmed the expected two parents, identical tested tree, primary branch head and merged PR. The original integrated R7 feature set, rational scoring v6, complete history and signing boundaries remain inherited.

## APK and follow-up boundary

This execution **did not assemble, sign, publish or install an APK**, nor run Android instrumentation on an emulator or the owner's RedMagic phone. Source compilation is not native/phone acceptance or a measured runtime improvement. The dedicated 4 PM APK worker owns original signing, frozen-source/build coordination, native/visual/install-over/save-history verification and the qualified Developer/Alpha delivery. Existing independent bug-report ZIP, trap-room, search, native-release and other work claims were not edited or duplicated.

Next safe coding worker should recheck current authoritative head and open claims before starting a *different evidenced* approved bug or performance package. Do not duplicate this nearest-target fix, weaken its regression, change the owner's numerical mechanics or silently merge independent report/native work. Release worker must treat this only as source ready for inclusion after its separate APK gates.

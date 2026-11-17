# APD Tengu unreachable-target recursion guard: verified source continuation

## Source identity and coordination (November 17, 2026, Eastern)

This morning coding session began at approximately 06:56 EST from the live authoritative `codex/apd-phone-fixes-2026-10-09` head `b2639c382d709b041b1625d437864d6d4714e8ac`. Before source writes, it read root `AGENTS.md`, verified C466R7+C485 integration in `docs/APD-486-R7-INTEGRATION-VERIFIED-2026-11-16.md`, existing work ledger/handoffs, the previous Tengu/AI continuation, current open PR claims and source. Verified combined baseline ancestor: `c6fa033167789ef9f2fc6f31bcff39c824dfb4db`; prior Tengu target-visibility merge: `9fa34d192cc4608bed3f418dca0f927a97122e31`. Did not touch the Extended report branches, personal playback, native acceptance, other game work, automations or main.

Isolated claim branch: `chatgpt/apd-tengu-retarget-recursion-20261117-0700`.
Review: https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/247
Production edit: `1cb8f677ad244c06d580c28bdbc5b903e3a2f9b5`.
Regression edit: `5e1b68d8809073027e7f38459663093aa089717d`.
Exact CI-tested source/workflow head: `f623a75d32158d30fe5ea8c92ca1a63f1f6ce282`.

## Evidenced defect and narrow repair

`Tengu.Hunting.handleUnreachableTarget()` overrides `Mob.Hunting.handleUnreachableTarget()`. Tengu had been setting the inherited `recursing` flag during an internal retry, but did not check that flag before choosing another target. When successive `chooseEnemy()` calls return alternating unreachable/unattackable characters, each nested call can choose a different one and retry again without a bound. A headless regression supplies exactly that alternating condition and would fail with a bounded error on the unguarded implementation.

The fix uses `if (!recursing)` around Tengu's existing first target choice, then resets `recursing` in `finally`. Its existing replacement-target field-of-view/alive/invisibility check remains unchanged. One retry is still permitted; if that target is also unreachable and unattackable the boss follows its preexisting ability-or-spend-turn path. No boss damage, attack priorities, cooldowns, abilities, RNG, turn costs, stage rules, scoring, saves, history, playback, R7 equipment or signing identities were changed.

The new `alternatingUnreachableTargetsOnlyRetryOnce` regression in the existing `TenguUnreachableRetargetVisibilityTest` covers this narrow case and retains the existing four replacement-visibility regressions.

## Exact-source CI evidence, all completed successfully

Three runs on exact source/workflow head `f623a75d32158d30fe5ea8c92ca1a63f1f6ce282` completed successfully:

- [Dedicated branch push run 44078991897](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/44078991897), job `134024356558`, `BUILD SUCCESSFUL in 2m 27s`.
- [Dedicated PR run 44079015303](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/44079015303), job `134024440460`, `BUILD SUCCESSFUL in 2m 38s`.
- [Retained earlier Tengu visibility PR regression run 44079015295](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/44079015295), job `134024440561`, `BUILD SUCCESSFUL in 2m 32s`.

Each exact-source run reports **3,569 core tests across 546 suites; zero failures, errors or skips**, including all five Tengu visibility/recursion tests. The required `:core:test`, `:desktop:classes`, Android debug/release Java compilation and Android instrumentation Java compilation all passed. The dedicated CI checks both required baseline ancestors before compiling. These are actual CI logs, not an assertion that source compilation implies on-device execution.

## Integration and release boundary

This documentation-only continuation was committed after the successfully tested source. Confirm any documentation-only diff separately. Recheck authoritative head, open claims, source/test/workflow contents and CI at integration time. Merge only using a non-force expected-head update with the authoritative live head as first parent; do not overwrite newer coding or release work. Record the resulting merge SHA separately if the guarded integration succeeds.

No Developer or Alpha APK was built, signed, installed, qualified, or published by this worker. Native Tengu encounter, RedMagic install-over, save-history acceptance and a verified APK remain the dedicated 4 PM release worker's responsibility. This source-only stability fix is not a measured phone performance improvement.

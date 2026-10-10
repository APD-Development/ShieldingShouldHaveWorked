# Work completed ledger (what is already DONE: do not redo it)

## 2026-10-10 22:38 UTC — simulator visual-wait acceleration candidate

- Re-read branch head `94cf567594eb7f4fddf2bfe81b33d5323b94f600`, `docs/HANDOFF.md`, current workflows, and the active 100-seed jobs.
- Confirmed three of five old-source emulator shards completed and two remained in progress; APK compilation was already reused, so per-action visual waiting is the remaining dominant design cost.
- Added a simulator-test-only 100× libGDX visual time scale, reapplied after scene resets and recorded in each JSON evidence row.
- No production source, policy, actor timing, combat RNG, generation, player data, signing, application ID, release, `main` or Claude branch changed.
- Verification is pending. Do not claim a speedup until the exact-source five-seed workflow passes and its per-episode timings are inspected. The previous 100-seed run stays separate.


## 2026-10-10 21:45:37–2026-10-10 22:00:07 UTC — exact-source baseline/package validation

- Confirmed source `455f22b5f14c1ea9886d033b81d0a1270ea5cd20` run [38088088384](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38088088384): 161 suites / 993 tests, zero failures/errors/skips; five requested emulator episodes completed with nonempty traces.
- Downloaded artifact `11683248078` and verified the exact seeds, profile, terminal outcomes, trace/command evidence, instrumentation pass, manifest source and unseeded-combat disclosure.
- Confirmed package run [38089262932](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38089262932). Downloaded artifact `11683303102`, rechecked both APK hashes, and reran its 11 evidence-accounting regressions successfully.
- Gated batch run [38088289249](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38088289249) now reuses the verified APK pair across five disposable shards for the remaining 95 seeds. No batch result yet.
- This observed interval covered repository/workflow inspection, artifact downloads and validation, polling, concurrent-head reconciliation and this ledger update. Time outside it remains unknown.
- No production source, player data, signing, application ID, release, `main` or Claude branch changed.

## 2026-10-10 21:10:43–2026-10-10 21:53:58 UTC — verified AVD recovery and five normal-play episodes

- Diagnosed completed runner logs, preserved failed attempts, and made two lease-protected workflow commits: [c44985da](https://github.com/APD-Development/ShieldingShouldHaveWorked/commit/c44985da3c4cd5b9bc90def1f50375efa4082234) (supported disk-size input, proven insufficient) and [6b7a5a29](https://github.com/APD-Development/ShieldingShouldHaveWorked/commit/6b7a5a29e6e1ac93d880b27792fe6a4d97bc3d9d) (post-staging Gradle cleanup plus APK existence/checksum guards).
- Exact-source run [38087941429](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38087941429) succeeded from 21:31:16 through 21:49:33 UTC. Cleanup left 13 GB free; API 34 booted in 45.124 seconds.
- Independently downloaded artifact 11683030835 and counted 161 suites / 993 tests, zero failures, errors or skips. ZIP SHA-256 matched GitHub: `104386790101f418e4c29edf03e2482009c6a6b1429bd751077e42e07b2833ed`.
- Independently downloaded artifact 11682982670 and validated five exact requested seeds, five completed rows, nonempty traces, and `OK (1 test)` in 747.279 seconds. ZIP SHA-256 matched GitHub: `d1f2aeea7d1430a8311182e90961bbf7220a5ce63a269a86acb3c56c895478b4`.
- Retained reusable simulator artifact 11683801846: `sha256:ce28f153ae761f9f9dbfb72f5df20f437aacc2134b710addfe522fffd19bf54c`.
- All five episodes died on floors 1–3; one consumed held healing before any normal Fly drop, none observed normal Fly healing. No balance/counterfactual/100-run/whole-dungeon/phone claim.
- Observed execution through ledger preparation: 43m 15s. Verified GitHub run time: 18m 17s. Remaining observed time covered inspection, commits, concurrent-head reconciliation, polling, downloads and evidence validation. Activity/downtime outside this execution is unknown.
- Only `.github/workflows/codex-simulator-check.yml` changed in the functional fix. Production source, player data, signing, application ID, releases, `main` and Claude's branch were untouched.


## 2026-10-10 21:10:43 UTC — simulator AVD disk provisioning correction

- Re-read approved branch head `b95d808747861d80567f7e8af00e49c96b784200`, `docs/HANDOFF.md`, probe evidence, and current workflow runs before writing.
- Inspected failed run [38085711482](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38085711482). Its simulator tests and APK compilation passed, but the emulator launched zero episodes because the requested 7,372.80 MB userdata partition exceeded the runner's 7,275.79 MB free space.
- Verified the pinned action's own `action.yml` exposes a supported `disk-size` input.
- Committed [c44985da](https://github.com/APD-Development/ShieldingShouldHaveWorked/commit/c44985da3c4cd5b9bc90def1f50375efa4082234), changing only `.github/workflows/codex-simulator-check.yml` to request `4096M`.
- Used parent `b95d808747861d80567f7e8af00e49c96b784200` and an expected-head ref lease; no force update. Read back the new branch head and workflow.
- Run [38086795339](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38086795339) started for the exact correction commit; full core/emulator result is pending. Concurrent commit `da573548b6e18c66da2d227a83b524be76c85ac6` built on the correction and retained it.
- A first documentation ref update was rejected after the concurrent head advance; the branch was reread and this entry was rebuilt on the real new head instead of overwriting it.
- No production/gameplay source or player data changed by this correction. No local build/test was possible in the execution workspace. Unknown time between scheduled executions remains unknown.


Built from `git log` (218 APD commits, 2026-10-05 to 2026-10-08) and the docs. Verification levels: **device** = Android emulator tests (API 26 + 34) passed in GitHub Actions at that time; **core** = JVM unit tests pass (`tools/run-core-tests.sh`, 848 tests at HEAD); **phone** = owner tried it on the RedMagic 11 Pro. Nothing after build 80 has been verified by device tests except where stated. Builds 70-80 were never fully phone-tested by the owner (only B69 was, before the interim report below).

Before changing any item below, read its doc and its tests; they encode decisions the owner already made.

## 1. Foundation (done, on main up to apd-build-80)
- APD build setup: app name/package (`.indev` suffix), committed debug keystore (docs/SIGNING.md, same signing key for upgrades), minSdk 26 (Android 8.0+). CI: `build.yml` (main push: tests + APK + release), `android-tests.yml` (device tests). Core + device: done.
- **APD-0001**: starvation damage consumes shielding before HP. core.
- Sad Ghost shows real upgrade level of offered gear (+ transparency reveal). core.
- Exotic chance setting 0-80 (floor for exotic potion/scroll generation), recorded per run. core.
- Cleric deliberate black screen + attempt counter + 10-try badge (still the NORMAL-build behaviour when Cleric is banned). core.
- APD menus: APD Settings screen/tab, offline News/Changes/About screens, new title screen (logo, glow, flames, stone buttons, 2x3 grid), UIMSD title button + annotation arrow. Title screen layout is hash-guarded by `UimsdWiringTest`: do not edit TitleScene without updating that test.

## 2. Statistics / telemetry stack "UIMSD" (done, device-verified through build 80; docs APD-STATS-*.md, APD-UIMSD-UNIVERSAL.md)
- Phase 0 headless level-generation harness + golden seeded-level baseline (`LevelGenerationHarness`, `GoldenLevelHashTest`).
- Phase 1 storage library (event log codec, string table, telemetry versioning, worker-thread writer). Phase 2 run lifecycle recorder (telemetry 1.1).
- Phase 3 item identity/lineage (1.2). Phase 4 combat/health (1.3). Phase 5 generation provenance (1.4). Phase 6 legacy importer + derived facts (1.5). Phase 9 exact healing (1.6). Difficulty telemetry (1.7). Universal fact/series events (1.8). Hero state per turn, landmarks, keys, secrets, wells, pages (1.9). NPC quest steps (1.10).
- Phase 7 query engine, generic UIMSD scene, datasets (Runs, Items, Enemies, Deaths, Floors, event datasets, Bosses), scope/rulesets, disclosure marks (? unknown, >= at least, ~ derived), aggregate-first views, Home page, Guide, interactive tour with sample data, question/sentence builder ("Ask"), reports (Markdown/text/HTML/CSV, compare two groups), saved views, column ordering, Run type/Modded columns, wording lint.
- Phase 8 archive export/import (ZIP, all-or-nothing import, storage meter, Android file picker); emulator migration test.
- Floor replay / seed floors / recorded-vs-canonical comparison (docs/APD-FLOOR-REPLAY.md, APD-FUTURE-FLOORS-FEASIBILITY.md).
- INDEV-only synthetic demo data kept apart from real history.
- Back button handling APD-wide (root cause: the visible exit button swallowed Back), device-tested (`AndroidBackDeviceTest`).
- APD Settings dead-controls fix (scroll pane created before controls), device-tested (`ApdSettingsClickDeviceTest`).
- `PlayRunDeviceTest`: real run recorded and sealed.

## 3. Difficulty 1-5 (done; docs/APD-DIFFICULTY.md)
Framework + balance pass 1 (enemy HP, enemy damage, ordinary item count; level 3 = original balance, revision-tracked per run), ruleset identity includes difficulty, UIMSD columns/filters. **Picker moved to hero select** (build 70/71, `DifficultyBar`, `RunSetupDifficultyDeviceTest`); the APD Settings slider is removed. Do not rebalance without the owner.

## 4. Backend, provider-neutral libraries only (done; docs/APD-BACKEND.md)
Ids/envelopes/trust root + signature verification, developer grants, run submission format + policy, local submission queue, preview/withdraw, privacy summary/report, Gateway + HTTPS gateway (off by default, never follows redirects), session store, evidence requests flow, account export/delete request wire forms, ballots/votes/reports wire forms, groups/roles/permissions (no Clan behaviour), sync registry, **signed content catalogue + keeper** (anti-rollback, offline cache), **preference sync replica** (tombstones, conflict rule), reference-server end-to-end test, UIMSD Options > Privacy screens. NOT built: any real server, sign-in, hosting, account screen beyond requests, moderation tooling, secure key storage. These need owner decisions (docs/APD-BACKEND.md "DECISIONS FOR THE OWNER").

## 5. Demo Unlock (done; docs/APD-DEMO-UNLOCK-CHECKLIST.md; device test run 108 green at 933d472)
UIMSD gate (locked until 10 genuine runs), exact warning pop-up, 10-run demo route, permanent flags, demo data isolation + deletion, tour lock, one-time tenth-run arrow (Clean "UNLIMITED DOPAMINE" / Adult "DRUGS" by the shared Adult-humor setting, default Clean). Switch: `UimsdUnlock.enforced()` -> `ApdFeatures.demoUnlock()` (on only in test builds). Not phone-tested by the owner end to end.

## 6. Cleric (done unless noted)
- **Un-ban, ruleset option B** (done, core-tested): Cleric selectable only when `ApdFeatures.clericPlayable()` (test builds); runs played as Cleric record `cleric=playable` (own ruleset id); every other run keeps its original id; old history untouched. No balance change (audit in docs/APD-CLERIC-RULESET-DECISION.md and the Cleric balance audit commit). Phone-verified by owner: Cleric can be selected and starts (interim report).
- **Cleric Grudge/Lore (humor engine, 348 reviewed lines, settings page, popups, counters, stories)**: fully built and core-tested, then POSTPONED to public release by owner. Disabled everywhere by `ApdFeatures.GRUDGE_LORE_IN_THIS_RELEASE=false`. Code in `apd/humor/`, `windows/GrudgeHooks.java`, `windows/WndClericGrudge.java`, assets `core/src/main/assets/apd/humor/`. Docs: APD-VENDETTA*.md, docs/cleric-review/. Do NOT delete or rewrite it; do not re-enable without the owner. Review rules the owner set for those lines are in docs/APD-VENDETTA.md. Owner-requested changes to the page already done (kept for later): only real "heard" counters, one Adult control (APD Settings), 24h+ popup gap pinned by test.
- Future Lore plan (50 entries at release + 50/year x 20 years, shared unlock conditions): NOT designed/built.

## 7. Fixes after the owner's interim phone report (2026-10-08; docs/APD-FIX-LOG-2026-10-08.md)
Root cause of "no exit / Main Menu greyed / no difficulty picker / guidebook repeats": SPD's first-run tutorial stayed on for a used install. Fix `apd/ApdIntro.java` (+ `FirstFloorExitTest`, `ApdIntroTest`). Also verified: with the tutorial over and no guide pages found, floors 1 and 2 are walkable from the start in 12/12 seeds (headless). Test APK `apd-test-3` built from this code. **Not device-verified.**

## 8. Release machinery (done)
`.github/workflows/private-test-apk.yml` (manual; on main and the branch): optional publish as GitHub PRE-RELEASE `apd-test-N` with the APK attached; `from_run` publishes an earlier run without rebuilding. Test-build overlay `-PapdTestBuild` in android/build.gradle. Releases so far: apd-build-6..80 (main), apd-test-2 (from run 1, commit 04448a87d), apd-test-3 (commit 46229e1d7).

## NOT done (candidates for the next agent; ask the owner before starting)
1. UIMSD dialogs (e.g. Arrange the columns) close when the app is backgrounded (scene is rebuilt; only zero-arg-constructor windows are restored). Needs per-dialog restore.
2. UIMSD default scope counts only finished runs under "current rules"; Cleric/abandoned runs appear under All (mixed). Decide wording/empty state.
3. Floor-1 runs generated in the old tutorial keep their hidden door (new runs are fine).
4. Hero-select pips + win star: design in docs/APD-HERO-MARKERS.md, awaiting "approve markers".
5. Owner phone checklist steps 23-41 still to be reported; checklist PDF predates the Grudge postponement (markdown is current).
6. Backend decisions (provider, sign-in, key custody, retention, moderation), contacting upstream author, plain-English UIMSD summary polish, shareable/PDF reports polish: see docs/HANDOFF.md "Open questions (owner only)".
7. The UIMSD guide/tour and some wording were polished but never fully phone-reviewed.

## 2026-10-10T23:48:33.681Z - shared normal-policy wait boundary implemented (verification pending)

- Execution `APD-20261010T234547Z-SHARED-WAIT`; observed start `2026-10-10T23:45:47Z`; implementation record `2026-10-10T23:48:33.681Z`. Time outside this observed interval is unknown.
- Parent observed before the change: `a0ab5303089232afdce61ecec17cf88dca5617a0`. Added test-only `WarriorTurnCommand`, `WarriorTurnDriver`, `NormalWarriorTurnPolicy` and three permanent selector cases.
- Android rooted and bounded recovery waits retain their previous command labels, action ordering and exact constraints, but now cross the same adapter-neutral command boundary used by the JVM headless driver.
- The existing headless actor test still runs the real `Hero.rest(false)` and real actor scheduler. This slice establishes only the first synchronous `WAIT` family; movement, combat, items, transitions, result extraction, paired parity and complete-episode throughput remain unresolved.
- Verification status at commit preparation: source inspection and deterministic replacement guards completed; exact-source GitHub core/Android checks pending. Do not label this slice tested until those jobs pass.
- Scope: test and documentation code only. No production gameplay source, save/statistics format, signing identity, application ID, release, public/backend state, `main` or Claude branch changed. No paid service, key, runner or subscription change was used.

## 2026-10-10 23:46:36 UTC — UIMSD reading-position bug correction; native verification pending

Resumed the owner's approved bugs/Community/regression loop from actual head a63d2bbbca6e2e34fbd6f426be75c97583737889, preserving the newer headless-simulator foundation. Confirmed source defect: WndUimsdText and WndUimsdTopic restored their contents/callbacks but rebuilt ScrollPane with its default zero offset, returning a reader to the top after scene recreation.

Both windows now snapshot scroll coordinates before the old pane is destroyed and apply them through the existing ScrollPane.scrollTo clamp after creating the replacement layout. No history, queues, saved-view files, query policy, RNG, gameplay or signing/application ID changes. Added actual native regression for both windows across two scene resets, stack order/no duplicates, unchanged query/no automatic Show me action, destruction of old panes, and a larger viewport that must clamp stale positions to zero.

Local evidence: both changed production classes compile with Java11 against existing dependencies; all three changed Java files parse; 17 existing geometry/wrapping cases pass (0.092 seconds). Android/emulator assertions and complete latest-source acceptance remain pending. A fresh verification request is included; do not claim this is in delivered TEST31. TEST31 remains the last signed/private delivered APK.

## 2026-10-10T23:41:16Z - headless foundation exact-source verification passed

- Execution `APD-20261010T231111Z-HEADLESS-FOUNDATION` observed end: `2026-10-10T23:41:16Z`. This is an observed wall-clock interval, not continuous active-time accounting; activity outside it is unknown.
- Final tested source: `a9b55e59152c8603e822dc1e2bcbf6ec4afebeda`; workflow [38095765371](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38095765371); command `tools/run-core-tests.sh`; complete core job succeeded. Trace preflights succeeded: 6 recovery + 6 instrumentation + 11 summary cases.
- Both new `HeadlessActorLoopProbeTest` cases passed. Actual stdout: `APD_HEADLESS_GENERATION floors=100 mobs=844 elapsedMillis=174`.
- Actor proof: real entered floor, real `Hero.rest(false)`, real `Actor.process()`, intervening actors, actor time +1 or more, hero selected for next turn, clean bounded thread shutdown. Test-only noosa/input/render boundaries are explicit; no production bypass exists.
- Core artifact: `11685134071`, `sha256:b70f1bcaf27e6ab110b1b10c89d128a5b98d89ade369d71cbc0e3472f09d2293`.
- Commit history: `c1cd69f09c60fc3a563c822edf046d7947cec128` initial seam; `2a180833adb189e30920966da837ea81f3e32507` noosa shell; `cb46d670e36fd4df53b19de4672964d6782ff796` render boundary; `a9b55e59152c8603e822dc1e2bcbf6ec4afebeda` explicit initial input boundary and passing source. Each built on the observed real head with an expected-head lease; no force push.
- Retained failures: workflows 38095194630 (`Game.instance` absent), 38095385820 (`AttackIndicator.instance` absent), 38095569572 (initial rendered input-ready boundary absent). Each blocker was changed before rerun.
- Android repair evidence remains workflow 38094049032: five native episodes completed, 2,446 actions / 609,546 ms = 4.013 actions/s. No trace issued `SEARCH`; seed `1000048` remains directly unverified.
- Scope: floor generation is measured; complete headless episodes, policy parity and a 10,000x speedup are not claimed. The automatically started Android adapter job for 38095765371 was still running at this record and is not required for this test/docs-only change.
- No production application source, saves/statistics, signing identity, application ID, release, public/backend state, `main` or Claude branch changed. No paid service, new runner, key or subscription change was used.

## 2026-10-10 23:39 UTC - initial input-ready boundary made explicit

- Workflow [38095569572](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38095569572), source `cb46d670e36fd4df53b19de4672964d6782ff796`, ran 1,006 core cases and failed one new assertion before the actor thread started. Native was skipped.
- Generation evidence: 100 real floors, 844 mobs, 245 ms.
- Cause: `Dungeon.switchLevel` leaves `hero.ready=false` until rendered `GameScene` performs its first input-ready callback. A headless harness has no such scene.
- Correction: explicitly establish the initial public `hero.ready` input boundary in test code, issue the real `Hero.rest(false)`, run the real scheduler through intervening actors, and stop at the next selected hero turn. No production source or result is fabricated.
- Status: exact-commit rerun pending.

## 2026-10-10 23:35 UTC - second actor-probe boundary retained and isolated

- Workflow [38095385820](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38095385820), exact source `2a180833adb189e30920966da837ea81f3e32507`, ran 1,006 core cases and failed one new case; native adapter was skipped.
- Generation remained green: 100 real first floors, 844 mobs, 182 ms.
- The noosa-shell correction worked. The scheduler processed through the wait and reached the next `Hero.ready()`, then failed because render-only `AttackIndicator.instance` is null in headless mode.
- Correction: mark only the hero test sprite moving after the wait, run the real actor clock through intervening actors, and assert when the scheduler selects the hero at time +1 before calling render-only UI. The test uses reflection only to observe the private current actor and does not mutate production source. Thread cleanup releases both waits.
- Status: implemented, exact-commit rerun pending; no complete-episode or UI claim.

## 2026-10-10T23:32:06Z - exact headless probe failure retained and corrected

- Workflow [38095194630](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38095194630), exact source `c1cd69f09c60fc3a563c822edf046d7947cec128`, ran 1,006 core cases and failed one new case. Native adapter job was correctly skipped.
- Passed evidence: `oneHundredRealFirstFloorsNeedNoAndroidRuntime` generated 100 real Warrior floor-1 levels containing 844 mobs in 187 ms on the hosted two-core runner.
- Failed evidence: `realActorClockProcessesAWaitWithoutRendering` stopped before input with `Game.switchingScene()` reading null `Game.instance`. This was a headless harness boundary, not a gameplay result.
- Correction: add a minimal test-only `Game` shell with `requestedReset=false`, use it only around the actor loop, and restore the prior global game/platform state in `finally`. Production source remains unchanged.
- Verification status: correction implemented, exact-commit rerun pending. Do not call the actor-loop seam tested yet.

## 2026-10-10T23:28:50Z - emulator-free actor-clock foundation implemented (verification pending)

- Execution identifier: `APD-20261010T231111Z-HEADLESS-FOUNDATION`; observed start `2026-10-10T23:11:11Z`, record end `2026-10-10T23:28:50Z`. Activity outside this observed interval is unknown.
- Re-read the actual approved branch, current handoff/ledger and simulator implementation. Parent/head before this commit: `8b9ff21959867b9916ba087b1382379046333f50`; no force update or synthetic history.
- Inspected completed exact-source workflow [38094049032](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38094049032), source `ac496e4a118735dfca853de1b9ced82f12f856a6`. Completed actions: fetched jobs/artifacts, downloaded and checksum-verified artifact `11685237933` as `sha256:82a7da2bc5ee9ef9b68ade8488659fc5a0d53e1319afb9bb7e62fc94bedd3d80`, parsed result rows and instrumentation output.
- Actual result: workflow/core/native succeeded; five of five native episodes completed with nonempty traces, 2,446 actions / 609,546 ms = 4.013 actions/second, death depths 1-3. None used `SEARCH`; seed `1000048` was not in this set, so the former exact stall is not claimed replayed.
- Implemented a test-only JVM seam using the real `Dungeon`, generation and `Actor.process()` clock: 100 first-floor generations plus one entered-floor wait cycle with invisible test sprites. Added a scope/verification design note.
- Changed files: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/apd/simulation/HeadlessActorLoopProbeTest.java`, `docs/APD-HEADLESS-SIMULATOR-2026-10-10.md`, `docs/HANDOFF.md`, `docs/WORK-COMPLETED-LEDGER.md`.
- Capability/limitation: a real scratch execution workspace and GitHub read/write connection were available; no repository checkout or JDK compiler was available locally. The new source was inspected but not compiled or run here. Exact-commit GitHub verification is pending and the code must not be described as tested until it passes.
- No production application source, saves/statistics, signing identity, application ID, release, public upload, backend state, `main` or Claude branch was changed. No paid service, API key, runner or subscription change was used.
- Unresolved: make normal-play policy shareable between Android and JVM adapters; implement synchronous bounded action families; add source-pinned parity seeds; directly replay seed `1000048`; only then benchmark complete episodes.

## 2026-10-10 — TEST31 privately delivered and signing verified

Private publication succeeded at **2026-10-10 23:05:40 UTC / 7:05:40 PM America/New_York**; delivery workflow [38093772627](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38093772627) completed successfully. Release/delivery commit **bc9c83427654c06e4eb3f145997cbb65119845f5**; APK source **25b4a95a802be730f365f82770235edb6bfc68b1**, exact verified build run **38092709292**. No rebuild was performed for publication; application/build input trees matched the tested source.

- [TEST31 APK](https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/download/apd-codex-test-31/ADHD-Pixel-Dungeon-CODEX-TEST-31.apk) — 38,242,482 bytes; SHA-256 **22561e0294ddd5322655b11b31562ea3b0893119fa69cd260356ef77037f0ae1**.
- [Current PDF](https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/download/apd-codex-test-31/APD-Bug-Fixes-2026-10-10-TEST31.pdf) and [verification evidence](https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/download/apd-codex-test-31/TEST31-Verification.json) are uploaded on the same private prerelease. Previous release assets remain intact.
- Actual successful checks: **1,004 core cases + six API34 emulator cases**, zero failures/errors/skips; debug assembly; 11 delivery-verifier fixture tests; exact-source matching, APK checksum, cryptographic signing and direct TEST26 compatibility comparison. Certificate SHA-256 **067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e**; application ID **com.charltonchris29.adhdpixeldungeon.indev**; version **4.0.8-apd-INDEV-bC31-TEST**, non-downgrade version code.
- Implemented changes: UIMSD local-queue/status-window restoration, corruption-safe saved-view edits/strict UTF-8, literal/long-token raw preview wrapping, Community page-specific availability. Retained 240-case floor-connectivity geometry test and narrow/scene-recreation native regressions. See report for files, causes, risks, commits and failed/superseded attempts.
- Physical-phone testing and actual upgrade/save-data acceptance remain **unverified**; install over the existing APD app, never uninstall or clear data. Keep TEST26 as rollback. Process-death/scroll-offset restoration and an explicit corrupt-view repair interface remain unresolved; broader approved gameplay improvements and simulator generalization are not claimed complete.

Execution evidence starts at 22:19:37 UTC and this final evidence record was prepared at 2026-10-10 23:10:00 UTC. These are observed wall-clock timestamps, not a metered active-work total; coding/research/build waiting/idle breakdown is unavailable and CI waits overlapped other work. Build run 38092709292 was 22:47:09–22:58:38 UTC; delivery ran 23:05:00–23:05:43 UTC. The earlier noon deadline was missed; this is an evening delivery. No unattended overnight execution is demonstrated by this work. No main/Claude overwrite, player data reset, public release, paid API dependency or unfinished server activation occurred.

## 2026-10-10 23:09 UTC - TEST31 delivery and simulator evidence reconciliation

- Execution record: `APD-20261010T230906Z-simsearch`. Observed branch head before this change: `bc9c83427654c06e4eb3f145997cbb65119845f5`. Read GitHub branch/commit trees, workflow runs/jobs/artifacts, private TEST31 release metadata, the simulator adapter, `Hero.search`, `KnownMapExplorer`, handoff and ledger.
- Confirmed private TEST31 delivery [38093772627](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38093772627) succeeded. It reused, rather than rebuilt, verified APK artifact `11685125574`. Published APK SHA-256: `22561e0294ddd5322655b11b31562ea3b0893119fa69cd260356ef77037f0ae1`; signer certificate matches TEST26; application ID is unchanged.
- Reconciled batch [38088289249](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38088289249): 100 attempts, 99 completed distinct seeds, unresolved seed `1000048`, 87 deaths, 15 runs consumed held Healing before normal Fly Healing, and 12 observed normal Fly Healing. The incomplete batch is retained and not labeled a completed 100-run cohort.
- Root cause for seed `1000048`: the test adapter threw when the hero's known traversable component contained only the current cell. Added a test-only fallback using the real `Dungeon.hero.search(true)` action. It spends normal time/hunger and does not inspect hidden terrain directly.
- Measured the 100x visual-time candidate using actual artifacts: baseline 2,979 actions in 934,885 ms, candidate 3,008 actions in 617,821 ms. Throughput improved about 1.53x. One candidate seed hit the 1,000-action limit, so the candidate is not reusable evidence yet and does not satisfy the requested large-scale speed target.
- Production application source, saves/statistics, signing identity, application ID, `main`, Claude work, balance, mechanics, backend state and public distribution are unchanged. Exact-source simulator verification for the search fallback is pending. Time outside this observed execution remains unknown.

## 2026-10-10 — Verified UIMSD/Community bug cycle (TEST31 publication pending)

- Real source 25b4a95a802be730f365f82770235edb6bfc68b1; run 38092709292: 1004 core + 6 API34 cases, zero failures/errors/skips; debug assembly succeeded, actual XML independently parsed.
- Fixed and tested: lost status/queue dialogs; saved-view corruption rewrite; raw long-ID/markup preview problems. Community page-specific availability polished; native narrow/recreation navigation checks retained.
- New geometry regression: 240 regular-floor checks across all six classes and two seeds, including BVS-BND-PVG. New Java syntax checker: three fixtures pass; 13 changed Java sources parse; full compilation remains required.
- Publication/signing still pending at this commit. Preserve TEST26 until separate verified existing-artifact delivery succeeds; report docs/APD-TEST31-REPORT-2026-10-10.md. Phone/upgrade/full-run acceptance is not claimed.

# Work completed ledger (what is already DONE: do not redo it)

## 2026-10-10 22:47 UTC — native test literal correction

- Exact-source simulator run [38092405397](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38092405397) passed trace/accounting checks and the complete core suite.
- Android test compilation then failed before emulator provisioning: `UimsdRestoreDeviceTest` used invalid Java character literal `'\\\\0'`.
- Corrected it to the single null character `'\\0'`; no production behavior changed.
- Re-triggered complete application and simulator verification. Native UIMSD and simulator-speed results remain pending.


## 2026-10-10 22:43 UTC — raw-wrap contract failure correction

- Inspected failed exact-source run [38092210983](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38092210983): 1,003 core tests ran; only `ContractTests.libraryUsesNoRandomnessNoGameClassesAndNoApisMissingOnAndroid5` failed.
- Root cause: new `LongWordWrap` imported forbidden `java.util.function.ToDoubleFunction`.
- Replaced it with a project-local functional width interface; wrapping behavior and callers remain unchanged.
- The failed run built no APK and ran no Android check. Fresh exact-source verification is pending.


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

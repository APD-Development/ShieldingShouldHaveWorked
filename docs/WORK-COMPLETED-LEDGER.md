## 2026-10-11 00:52 UTC — Delivery evidence reconciled without overwriting concurrent work

Preserved independent documentation commit 0f2583fd6ec22f3fb6bd2d3f20cf1d88cad0e908, which already records successful TEST36 delivery. Root's overlapping update lost its expected-head lease; unpushed commit be4cde4a73cb1bb405cab5d6276021689f04f895 was not forced. Added machine-readable docs/APD-TEST36-DELIVERY-2026-10-10.json on the real fresh parent. The resumed cycle was approximately 68 minutes to actual publication; active/idle categories remain unmetered, explicit recorded waits total 60 seconds. Source CI took 11m9s and publication workflow 49s while other work proceeded; do not count those intervals as developer-active time.

Actual APK release asset size 38,242,482 bytes and digest match the completed gate's cd86c3b2cae76a09fae407b1b666daaa4b87c8f16ed3d651877fbcd8f42f099d. PDF asset SHA256 is 5f670e752eca109568fdadc27f3a74f503c68e77676eb7deb8281707db9a0f53. Exact private artifact links/source/signing/testing limits are in the JSON and release. Existing TEST31 rollback remains. Publication is a checkpoint, not authority to stop the approved backlog. Continuous unattended execution is not demonstrated by these build jobs or the changed enabled hourly prompt.

## 2026-10-11 00:50 UTC / October 10 evening New York — TEST36 privately delivered from verified artifacts

Private delivery workflow [38099729187](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38099729187) succeeded from 00:49:29 through 00:50:18 UTC. It reused the exact TEST36 artifact from source `3d3656e2c355fe70c5d50a6034eeb07d9700598d` and verification run [38099022839](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38099022839); no APK rebuild occurred. The gate rechecked 1,019 core and 9 API34 results with zero failures/errors/skips, matching application/build input trees, the package checksum, signatures, application ID and upgrade version against TEST31.

Private prerelease [apd-codex-test-36](https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/tag/apd-codex-test-36) was published at 00:50:16 UTC without replacing prior assets. APK SHA-256 is `cd86c3b2cae76a09fae407b1b666daaa4b87c8f16ed3d651877fbcd8f42f099d`; application ID is `com.charltonchris29.adhdpixeldungeon.indev`; certificate SHA-256 `067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e` matches TEST31. The release also retains the checksum file, verification JSON and TEST36 PDF. TEST31 remains rollback. This is private emulator/build evidence, not physical-phone save-upgrade or full-playthrough acceptance. Main, Claude and player data were not changed.

## 2026-10-11 00:49 UTC / October 10 evening New York — TEST36 exact-source checks completed; private delivery requested

Source 3d3656e2c355fe70c5d50a6034eeb07d9700598d passed workflow 38099022839 from 00:36:49 to 00:47:58 UTC. Actual downloaded XML contains 1,019 core cases and 9 API34 cases, zero failures/errors/skips; debug assembly succeeded. Android evidence artifact 11687395355 SHA256 a6ae402e44f8f6c622ec7fc022a16f9a0a354fc88faadd5d9f8bd3fbb0e0cca8 was downloaded and checked, including all three new restoration methods and actual Community summaries. Core artifact 11686243303 and APK artifact 11686608032 are pinned to the same source/run.

Requesting private delivery through the existing separate workflow, reusing these exact artifacts without rebuilding. It must compare the actual TEST31 APK checksum, signature and application ID, ensure source/build trees match, generate the current PDF with verification appendix, then create a new private prerelease without overwriting previous assets. Publication/signing confirmation is pending until that job succeeds. TEST31 remains the delivered rollback. docs/APD-TEST36-REPORT-2026-10-10.md details causes, retained tests, all local setup failures, preserved concurrent commits, limitations and honest unmetered timing. Main, Claude, player data, signing and gameplay are unchanged. A rendered draft PDF was text-checked locally.

The recurring task prompt was corrected, but no completed invocation of the new cycle has yet been causally verified. Existing Community commits are actual work, not evidence of continuous between-run execution. Two explicit 30-second CI waits were recorded; other active/idle categories were not independently metered. A low-priority empty-text-filter codec suspicion was source-reviewed: the actual UI rejects empty input and excludes blank picker values, so no reachable player regression is claimed or speculatively changed. Tour worker lifecycle remains an unreproduced suspicion. Continue independent authorized inspection/drafts during publication and resume application work after this delivery checkpoint.

## 2026-10-11 00:37 UTC / October 10 evening New York — Final candidate cutline and actual workflow repair

TEST35 workflow 38098266395 completed successfully for source c54d08f38ba765b0c40aab2176263f2e90a2348c (00:23:34-00:34:43 UTC). Downloaded XML proves 1,017 core and 9 API34 cases, zero failures/errors/skips. Its actual new Wayward cases passed; assembly succeeded. It does not include later historical-sort or Community-summary changes and was not published.

Preserved independent Community commit bb35d7997fa41fc7f1fef6124cf800c70ebf6d22. Root compiled its two production classes/test, ran all six Community tests successfully (0.026 s), and parsed all four changed Java files. The other approved execution already requested TEST36 run 38099022839, source 3d3656e2c355fe70c5d50a6034eeb07d9700598d; accepting that identical application cutline rather than creating a duplicate run. It performs one exact-source complete core/debug/API34 check that includes that work plus historical-sort compatibility. No verification job publishes; the separate delivery workflow must verify exact XML/source/artifacts, directly compare TEST31 signing/application ID/checksum and produce the current report before private publication.

Delivery coordination: this request is the prepared application cutline. During verification/publication, continue independent inspections, tests and isolated drafts; avoid advancing shared application inputs until the short private-delivery checkpoint is recorded, because the existing publication gate deliberately rejects a delivery commit whose application tree differs from the tested source. Do not cancel useful running verification or overwrite another developer's work. This is not a pause in authorized development or permission to stop after delivery.

The actual existing hosted APD Hourly Development task 6aca774b2e2081918d6025f01eb471c5 was inspected and its prompt updated successfully at 00:30:41.755314 UTC to the owner's bug/Community/test cycle, preserving its enabled hourly schedule, existing-subscription/$0 restrictions and separate lunch tasks. docs/APD-WORKFLOW-STATUS-2026-10-10.json records the exact observed metadata. Its previous last_run_time was 00:07:19 UTC and next_run_time was null; an execution of the updated instructions has not yet been causally verified. Enabled recurring tasks are not continuous between-run execution. No new automation, paid API/runner or unrelated schedule change was made.

One remaining source-reviewed suspicion is an old-scene callback if a tour-data worker finishes after Android recreates UIMSD; it has not been reproduced, is not called a confirmed bug and is not speculatively patched. Normal/full-quest/phone upgrades and process-death acceptance remain pending. No player data, gameplay, RNG, identity, main or Claude changes.

GitHub returned an UNKNOWN GraphQL error on the expected-head branch update for root's prepared request. A fresh read showed another approved developer's 3d3656e2c355fe70c5d50a6034eeb07d9700598d request had advanced the branch and already triggered TEST36. Both actual top-level application/build trees match; the lost-lease root request 28ac97439b93592cf3e04d975aa9976eef1f7604 was not pushed or forced. Reconciled only documentation on the fresh real parent, preserving all other work and the already-running verification.

## 2026-10-11T00:36:43.703Z - TEST35 evidence accepted; latest combined verification requested

- Workflow [38098266395](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38098266395), exact source `c54d08f38ba765b0c40aab2176263f2e90a2348c`, observed 2026-10-11 00:23:34-00:34:43 UTC, completed successfully.
- Downloaded core artifact `11686352327`, `sha256:be8ae52e23666febcbbd9da037467d08699eac8c12c0f6ade9536ca5cb4d34d4`: 167 XML suites, 1,017 tests, zero failures/errors/skips.
- Downloaded Android artifact `11686779020`, `sha256:b56db41443760bee4c1f26c96ceca3b0335f7ff424028b6520bc01f468491e22`: 9 API34 cases, zero failures/errors/skips. Debug assembly succeeded; delivery job was skipped as designed.
- Later current-source changes not covered by TEST35: historical localized aggregate-sort read compatibility and Community page-specific menu summaries/native subtitle assertions.
- Updated the existing build request to verify the latest combined source once. This uses the existing approved workflow and quota, requests no publication, and does not rebuild the already verified TEST35 source merely for delivery.
- No player data, gameplay/RNG, save/statistics format, signing identity, application ID, release, `main` or Claude branch changed. Time outside the observed tool/workflow intervals is unknown.

## 2026-10-11T00:35:05.753Z - Community menu summaries implemented (verification pending)

- Execution `APD-20261011T003219Z-COMMUNITY-SUMMARIES`; observed implementation record `2026-10-11T00:35:05.753Z`; parent observed before the change `e9b9e1971fed601291d19d336c202c2447395938`. Time outside this bounded execution is unknown.
- Changed `CommunityPreview.Page` to carry a unique one-line summary for each of the eight existing offline destinations; `CommunityScene` renders that summary in the landing row.
- Retained JVM assertions require unique summaries longer than 25 characters and include them in the no-internal-date/no-clan/no-creator-secret checks. The existing API34 all-page navigation case now asserts each actual row detail.
- Verification status: deterministic source replacement guards completed. Exact-source Java/core/native verification is pending. The useful TEST35 workflow remains undisturbed and predates this source.
- No online service, account, queue, fake community data, gameplay, RNG, save/statistics format, signing identity, application ID, release, `main` or Claude branch changed.

## 2026-10-11 00:28 UTC / October 10 evening New York — Historical saved-sort compatibility defect reproduced

A further regression reproduced an older Turkish/Azeri saved aggregate sort (mın:score) reopening in insertion order instead of ascending minimum order. Stabilizing newly generated aggregate IDs does not repair previously encoded locale-specific IDs. QueryCodec now resolves only known historical MIN/MEDIAN/COUNT_DISTINCT aliases that exactly correspond to aggregates in this query. Literal group keys and unknown/future sort IDs stay unchanged; the original saved-view file is never rewritten on read. Canonical in-memory sort IDs also allow normal header/selection matching.

Two permanent cases cover six old-sort combinations (three functions, ascending/descending), unchanged file bytes and preservation of unknown/literal group-key identifiers. The meaningful baseline fails 1/7 on wrong group order; corrected locale/saved-view/query/combined-scope/boss-finished set passes 42/42 (0.217 s). Changed codec compiles and both Java files parse. One attempted test invocation named a nonexistent QueryCodecTest and executed no substantive tests; it was corrected to the existing retained suites and is not counted as passing coverage.

Exact-source build acceptance remains pending. TEST35 verification is allowed to finish and is not canceled; this ordinary checkpoint contains no build request. No APK is claimed to include this correction yet. Signed TEST31 stays rollback. No gameplay, seeded RNG, history/save format, identity, main or Claude branch changes.

## 2026-10-11 00:23 UTC / October 10 evening New York — UIMSD follow-ups accepted; final known-curse display build requested

TEST34 workflow 38097519064 succeeded for exact source 715219d1fcebe2acc6fb0ee014bbdb68108e925e (00:10:21 through 00:21:46 UTC). Actual downloaded XML proves 1,015 core tests and 9 API34 cases, zero failures/errors/skips; debug assembly passed. This includes the seven warnings, grouped-sort ID fix and both actual tour-choice restorations without started tours/sample generation. It is not physical-phone verification. This candidate was not delivered; the confirmed Wayward display fix was developed while its tests ran and needs its own exact-source verification.

Requesting one final combined core/debug/API34 check including commit 22b4285f0e32d7853d8d293e5d2fea65289cdfa0. Additional cached-class quest/save-isolation regressions passed 9/9 (0.433 s); private-delivery safety fixtures passed 11/11. Report draft rendered to a valid four-page PDF and its extracted text was checked, but it is not a delivered report. Signatures, application ID, checksum and current exact-source tests must pass the separate private-delivery gate. TEST31 remains preserved rollback. No source is rebuilt merely for publication.

The one explicit 30-second wait (approximately 00:22:19-00:22:49 UTC) awaited CI completion and is recorded as tool/build waiting, not coding. Precise whole-session active/idle totals remain unmetered. No continuous overnight/unattended claim is made. No player data, gameplay mechanics, seeded RNG, signing identity, main or Claude branch changed.

## 2026-10-11 00:17 UTC / October 10 evening New York — Confirmed weapon-accuracy display defect

While the combined UIMSD check ran, a new targeted regression reproduced an active known Wayward curse showing 100.0% base weapon accuracy while the unchanged combat path used 20.0%. Weapon.accuracyDescription now applies the existing 1/5 modifier only when the curse is known and its actual buff is active. Unknown curses are not exposed; other weapons are not penalized, and inspecting the screen does not spend HP/items/time, consume the effect or advance RNG. Melee/ranged descriptions, both distances, Point Blank 0-2, understrength/normal strength and hidden upgrades are covered.

Two new permanent tests were added and the existing ranged test covers cursed/ordinary paths. Baseline with proper native/resources setup failed 1/8 cases on the wrong displayed percentage. Corrected 8/8 pass; combined accuracy/healing/drop safeguards pass 17/17 (2.418 s). Weapon compiles under Java11; two changed files parse. Two earlier local invocations lacked the native library jar, and one broader check lacked apd.game.src; these setup failures were corrected and are not counted as game bugs/passing assertions.

Production combat, enchantment triggers, targeting, damage, turns, RNG, player history/save formats and item identification mechanics are unchanged. Exact combined core/debug/native acceptance is pending. No APK is claimed delivered with this patch yet. TEST34 is allowed to finish without cancellation; one subsequent exact-source check will include this confirmed display correction before final private delivery. Existing signed TEST31 remains rollback.

## 2026-10-11 00:12 UTC / October 10 evening New York — Combined UIMSD verification and follow-up request

Exact TEST33 source b816bcf020562b153a149aabc1c52f424f9ded85 passed workflow 38096836924 (23:58:32 to 00:08:20 UTC). Downloaded XML proves 1,013 core tests and 8 API34 tests, zero failures/errors/skips. Six status paths and two-scroll-window restoration now have native acceptance; this source does not include the later grouped-ID/tour-warning/tour-choice changes. APK assembly passed, but TEST33 was not signing-compared or delivered.

Requesting one complete core/debug/API34 run for those later production corrections. Local UimsdScene compilation and Java syntax pass. Additional non-network queue/archive/history/unlock regression set passed 54/54 in 0.975 seconds. An initial broader local invocation reported two loopback HttpServer setup denials plus teardown errors (sandbox sockets unavailable) and one missing apd.game.src test property; these are not presented as game defects or concealed. Corrected property resolves the gate case. Network fixtures remain covered by the complete GitHub core suite; no backend was enabled to work around the sandbox.

No candidate is published from verification. Preserve existing TEST31 until exact-source XML, signature/application ID, checksum and current report pass separate private publication. Later changes remain native-pending; no physical-device testing was performed. This is productive active-turn work, not proof that this chat keeps executing after its turn ends.

## 2026-10-11 00:09 UTC / October 10 evening New York — UIMSD tour choices preserve Android recreation

Both scene-level tour choice windows still used anonymous WndOptions without a semantic reset restorer. PixelScene cannot reconstruct those constructors, so Android scene recreation silently discarded the first-tour offer and the own-run/sample-data chooser. They now reuse responsive WndUimsdChoices and pure display factories. Restorers and selection callbacks bind to the current scene; restoration never calls startTour/beginTour, reoffers a dismissed choice, generates sample data or changes the existing ten-run gate.

A permanent native case now exercises each actual choice across two resets, labels, query/root/guide preference preservation, absence of a started/preparing tour or sample root, Later/Back dismissal and a subsequent reset with no reopened choice. Production UimsdScene compiles and both changed Java files parse locally. Native acceptance is pending; no claim of phone verification. The ongoing TEST33 check is allowed to finish before requesting combined verification, avoiding cancellation of useful evidence. Latest privately delivered signing-compatible rollback remains TEST31. Existing saved history and gameplay are untouched.

## 2026-10-11T00:04:46Z - shared wait-command boundary exact-source verification passed

- Execution `APD-20261010T234547Z-SHARED-WAIT`; verification run [38096301603](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38096301603), exact source `ccf347179ce382f3ffdd1df4d81b59dcea549d2c`, ran 2026-10-10 23:48:42 through 2026-10-11 00:04:46 UTC. This is an observed workflow interval, not continuous active-work accounting.
- Core artifact `11686013206`, `sha256:d799cd0819b7ce41294494d350a6283f03859151064ae37c23879f348890a787`: 165 XML suites / 1,009 tests / zero failures, errors or skips. `NormalWarriorTurnPolicyTest` passed 3/3; `HeadlessActorLoopProbeTest` passed 2/2.
- Android app and test APK compilation succeeded. Native artifact `11686825922`, `sha256:a7a08e2298660c17e23a07e0030ccc8e5b32ee162e573a091847e76ce382a2cd`: instrumentation `OK (1 test)`, 499.755 seconds, five complete fixed-seed rows with nonempty commands and traces.
- Native outcomes: seeds 1000007/1000035/1000042/1000060 died on depths 2/1/4/3; 1000089 completed the scenario on depth 3. Total 2,881 trace actions in 492,176 recorded episode milliseconds. The shared recovery `WAIT` boundary executed 157 `REST` commands. Rooted wait did not occur in this sample and is only core-selector verified.
- Evidence boundary: first synchronous wait family verified in JVM and Android. Movement, combat, item use, transitions, complete JVM episodes, Android/JVM paired parity and complete-episode speed remain unresolved. Do not compare this timing causally with earlier samples because terminal paths and combat RNG differ.
- No production gameplay source, save/statistics format, signing identity, application ID, release, public/backend state, `main` or Claude branch changed. No paid service, API key, new runner or subscription change was used.

## 2026-10-11 00:03 UTC / October 10 evening New York — Grouped-sort locale bug and tour warning correction

Follow-up tests exposed another logical locale dependency: Aggregate.id used default-locale lowercase. MIN and COUNT_DISTINCT identifiers changed under Turkish/Azeri settings, breaking stored grouped-sort identifiers and hash-map keys. Two additional permanent red tests reproduce the changed ID and an incorrectly ordered saved grouped-minimum view. Aggregate.id now uses Locale.ROOT; all five locale cases and the expanded combined 42-case query/saved-view/hidden-safety set pass (4.080 seconds). The 50,000-row/2,000-type stress fixture passed again (112 ms grouping). No stored query bytes or recorded data is migrated or rewritten.

The scene-level locked-tour notice was the seventh remaining non-restorable UIMSD message path. It now reuses WndUimsdText, preserving the ten-genuine-run gate and exact notice. Native warning regression now also opens the actual locked-tour notice, restores it and closes it with Back without unlocking/starting a tour.

Changed production classes compile, and all four changed Java files parse. Exact combined native/core/build acceptance remains pending. TEST33's earlier source is still running; this ordinary commit intentionally does not cancel it. The latest delivered signed APK remains TEST31. No gameplay, RNG, save/history, creator restrictions or signing/application ID change.

## 2026-10-10 23:58 UTC — Bug cycle continues: scroll native passed; expanded-content regression retained

Exact-source TEST32 verification [38096206257](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38096206257), source a0ab5303089232afdce61ecec17cf88dca5617a0, completed all core/debug/API34 jobs successfully. It includes the actual new preview/guide reading-position case across two scene resets and larger-viewport clamping. Private auto-publication stayed disabled; this APK has not been signing-compared or delivered. It does not include the later locale or warning-message fixes.

Retained ExpandedContentStatsTest: 50,000 synthetic rows, 2,000 distinct content names, 25 records/type; checks every group's independent count/sum, all 40 pages without lost/duplicate types, 50-row geometry and past-end clamping. Local pass: grouping 106 ms in the first run / 72 ms in the combined run; full targeted 40-case query/saved-view/scope/hidden-safety suite passes in 3.461 seconds. This tests UIMSD statistics processing only, not installed mod performance, phone FPS or gameplay generation. No stress-test failure or new gameplay bug found.

Requesting one combined latest-source core/debug/API34 run containing scroll persistence, locale-independent filtering, six remaining restorable warning paths and the retained 2,000-type regression. No verification stage publishes; signing/source/rollback compatibility and a current report are still required before a private APK delivery. TEST31 remains the delivered rollback. No gameplay/balance/save/schema/signing/app-ID/main/Claude change.

## 2026-10-10 23:55 UTC — Remaining UIMSD warning/status restoration gaps closed; native pending

Continued audit identified six remaining WndMessage(String) callsites with the same confirmed missing-restorer defect: single/range invalid numeric filters, grouped column selection/arrangement, empty-history Ask, and report completion/failure callbacks. Reused WndUimsdText in literal/raw mode for every callsite, preserving original wording, action behavior and query state. No file/history/resource/network action runs while restoring the already generated message.

Added one permanent API34 case exercising all six actual call paths, real scene resets, exact payloads, one window, unchanged query and Back. Report callbacks stay on the render thread. All five changed sources parse; all four changed production classes compile locally against existing dependencies. New native case and combined exact-source core/debug verification remain pending. The existing scroll-only TEST32 is still running and has not been canceled or claimed passed.

## 2026-10-10 23:52:22 UTC — Locale-sensitive UIMSD filtering reproduced and corrected

New permanent QueryLocaleRegressionTest reproduced two failures against the current query engine: under Turkish device locale, CONTAINS "ring" omitted recorded "RING OF MIGHT", including after reopening a saved view. Root cause is default-locale lowercasing rather than a stable query comparison. QueryEngine now uses Locale.ROOT for both operands of CONTAINS; no recorded text, filters, sorting, columns, gameplay or history is rewritten.

Evidence: two red failures / three new cases before the fix; all three pass after (0.057 seconds), including literal separators/Unicode/case disk persistence. Expanded local query/saved-view/combined-scope/hidden-safety batch passes 39 cases (2.571 seconds), including 24,024 hidden-information comparisons without differences. Those loops are not 24,024 independent test cases. Changed Java11 sources compile/parse. Exact latest-source core/Android acceptance is still pending; this is not included in delivered TEST31 or the separate scroll-only TEST32 candidate.

TEST32 workflow 38096206257 (a0ab5303089232afdce61ecec17cf88dca5617a0) passed core/debug jobs; its new scroll-restoration emulator case is still running. This ordinary code commit intentionally does not cancel that native check or request a redundant build. Run the combined source once the current native result is understood; preserve the previously verified APK until acceptance and signing pass.

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


## TEST141 electricity provenance candidate — 2026-10-14T02:41:22.245Z
TEST140 exact source26809c581a9dc16b7a3f29dfeb8f47a81cf09c02/run38499807644 remains the latest complete verified application source:1491core+26API34,34valid PNG/debug/history,0failure/error/skip. This candidate builds from documentation head2c32ce75cd018df393e8af5f402b733d5b88e62b without rebuilding TEST140.

Electricity now carries optional player/enemy/boss/environment ownership through additive seeds, strongest-power water spread, clearing and save/load. Damage is scaled once while preserving the original Electricity source, paralysis, turns, density, spread and RNG; legacy fields remain unowned and malformed metadata rejects without rewriting the save. Shocking Brew, Elemental Blast and Elemental Strike mark their player-created fields. Tengu ShockerBlob and DM-300 PylonEnergy use Boss Damage rather than Enemy Damage. Ordinary Shocking/Storm traps remain environmental and unscaled.

Eight permanent tests cover all slider percentages, mixed/environmental fields, water spread, save/legacy/malformed metadata, clearing, neutral RNG/topology, the actual Shocking Brew and both boss fields, plus the three player caller boundaries. Local execution is unavailable in this connector-only workspace; compilation and expected1499core/26API34/34PNG/debug/history are pending exact-source CI. Three Damage controls remain disabled. Fire, planted effects, remaining specialized/chained explosions, abilities and derived damage still require audit before enablement. Original scoring, saves/raw history, inventory, balance, signing/application ID, main/Claude, online services and releases are unchanged.


## TEST142 nested Tengu fixture correction — 2026-10-14T02:42:15.284Z
Pre-acceptance source review found the new regression referenced ShockerBlob as Tengu.ShockerBlob, while production declares it as Tengu.ShockerAbility.ShockerBlob. Corrected only that retained test type reference; electricity production behavior and all assertions are unchanged. TEST141 run38502360650 remains unaccepted. Exact-source1499core/26API34/34PNG/debug/history verification is pending; TEST140 remains the last complete verified source and TEST123 the last owner-delivered/signature-compared APK.


## TEST142 failure and TEST143 headless fixture correction — 2026-10-14T02:48:51.514Z
TEST141 run38502360650 failed at test compilation on the incorrect nested Tengu type; no APK/Android ran. TEST142 source4f525136ae83fcb88e20b681cb95bfa18c07ae5f/run38502433855 compiled production/tests and ran1499core:1493passed,6failed, with all1491 previous tests green. Five new Electricity tests hit a fixture-only null Level.heaps; Shocking Brew hit the synthetic5x5 edge. Core artifact11866442824/digest bc613850a0d786f831c9a5014d279afb6e345d4db6597591eed920873ba37a89 retained exact XML. APK/API34 jobs were correctly skipped.

TEST143 initializes the headless heap registry and moves unchanged electrical assertions to a safe central cell in a15x15 map. Production behavior/assertions are unchanged. Exact-source1499core/26API34/34PNG/debug/history remain pending. TEST140 source26809c581a9dc16b7a3f29dfeb8f47a81cf09c02/run38499807644 remains last fully verified; TEST123 remains last owner-delivered/signature-compared. Three Damage controls remain disabled; fire/plants/specialized explosions/abilities/derived audit remains next. No phone/release/scoring/save/inventory/signing/application ID/main/Claude/online-service change.


## Verified TEST143 electricity provenance — 2026-10-14T03:02:57.585Z
Exact source f9d873f382de2533c52e6f895e403b94fc7e6d5c/tree4d7868272a4fc8172266edb5c2674cb8b89888ce, run38503023806 (2026-10-14T02:49:14Z–03:00:39Z). Downloaded XML proved1499core+26API34 passed with0failure/error/skip. Debug build/history passed. All34 PNG signatures/dimensions validated at320x640; contact sheet visually reviewed without malformed/cropped/blank required dialog evidence. Artifacts: APK11866573869 sha256:124785110c1d2e27559d89f7241dffd8b7dc3cca03997adfb8268384ce185cf1; core11866648646 sha256:f53d05596863c593cc1db6edfa8b9ef926fd2295d023eaad5742f15e5cd23381; Android11867085848 sha256:030e0d4cdd0c9d7723917a782e8a4048646e019c8f3b52a2c9208b4ee4122bab. Implemented and verified: additive Electricity attribution across water spread/save/clear; player Shocking Brew/Elemental Blast/Elemental Strike; boss Tengu ShockerBlob/DM300 PylonEnergy; environmental traps unscaled; one-event scaling with original source/RNG/turns/mitigation preserved. TEST141 compile failure and TEST142 six fixture-only failures are retained in the preceding ledger entry. No tests weakened. Candidate is staged, not owner-delivered/signature-compared/physical-phone tested; TEST123 remains delivered fallback. Three Damage controls remain disabled. Next: fire/plants/specialized/chained effects, abilities/derived damage, then class/boss/combined acceptance. Original scoring and protected save/statistics/signing/application identity remain unchanged.


### 2026-10-14T09:42:03Z — TEST145 mixed fire provenance candidate (pending verification)
- Reconciled concurrent TEST144 head `73657ac4736b99583a7cf605c6ce35c3a19a2e9f`; confirmed all touched blobs unchanged, then committed `134473c562cb4b642ff2c1e7e64f7497de34ed9c` with expected-head fast-forward lease.
- Implemented mixed Fire cell ownership, spread/decay/clear/save retention, per-tick Burning mixture, and player attribution for Liquid Flame/Fireblast; added five FireDamageProvenanceTest methods.
- No local execution workspace. Required 1510 core + debug + 26 API34 + 34 PNG + history is pending; three Damage sliders remain disabled and TEST143 remains latest verified.
- Remaining fire callers/effects are listed in docs/APD-TEST145-FIRE-SOURCE-2026-10-14.md; no completion claim.


### 2026-10-14T09:56:41Z — TEST145 CI fixture failure; TEST146 exact-source request
- Run `38544427903` exact source `7a8f08eb82559e605bb48f279a22d608b1e29a52`: 1510 core executed, 1506 passed, four new methods stopped on null headless `Level.plants`; source-wiring passed. No APK/history/API34 ran.
- Evidence: core artifact `11884268254`, SHA-256 `eda4ce3cf5eba91c2b77cb88c9228a036e3c4c9e956e8edee3223dba8f4ae699`.
- Preserved concurrent `5cc7d6da51cf9207ab94bd3c172846cf0c138d7a`, which applies the identical fixture-only initialization and records 24 targeted + 617 broader local passes. Production source/assertions unchanged; TEST146 full verification pending.


### 2026-10-14T10:13:52Z — TEST146 fire/chained provenance verified
- Source `08d10dedbc862dad0d27de35d98f719bec8e7bf2`; run `38546323675`; verify/build and API34 jobs succeeded; private delivery skipped.
- Downloaded evidence: 1516 core passed (0 failure/error/skip, 289 suites), artifact `11884134739`, ZIP SHA-256 `8e971f61889cd4ae06b9cf547a6c74c4bdc3234ad3902694c56d709c97aa7144`; 26 API34 passed (0 failure/error/skip), Android artifact `11885825108`, ZIP SHA-256 `71668af986cf8213a75763d741a7c5546f31c58a9a7775a6c42d0deecdac4bb1`.
- Debug APK artifact `11885330025`, ZIP SHA-256 `ee208db2fd4c95c367ad0560e3ba2e6e165aa1f79e0618440e9f133bfc8a71fb`; contained APK SHA-256 `e5b319edbd50ac6c4684cb039e2bb2bd4f25e51f1dafe3a9157c52e0ed5f3ffc` and its checksum verified.
- Compiled history passed against immediate previous TEST144; 34/34 screenshots decoded at 320x640 and contact-sheet review found no obviously corrupt capture. Emulator evidence is not a physical-phone claim.
- Implemented/verified: mixed Fire provenance, Liquid Flame/Fireblast player ownership, causal chained-bomb ownership, retained five fire plus six chained-bomb regressions. TEST145's four fixture-only failures remain documented.
- Not delivered: no private-delivery job, owner install, signer comparison, tester distribution, or public release. TEST123 remains delivered fallback. Three Damage sliders remain disabled pending remaining offensive-source and acceptance audits; no score/save/signing/app-ID changes.


### 2026-10-14T10:34:33Z — TEST149 Dragon's Breath source-only candidate
- Read complete current PotionOfDragonsBreath, Burning, Fire, FireImbue, Firebloom and Plant paths on head `b80b9ec4e6dbc8aeedf53dd914f50b18640b35e2`.
- Implemented one captured activation origin for direct Burning and both Dragon's Breath fire-field branches; added explicit-origin Burning API with null staying environmental.
- Added four retained methods: all20 percentages/all3 roles, legacy/null exclusion, neutral damage/next-RNG identity, and production caller wiring.
- No local execution workspace; not compiled/tested/verified. TEST148 `38549433962` remained active and was not replaced. No slider enablement, scoring/save/signing/app-ID/release change.


### 2026-10-14T10:45:49Z — TEST148 verified; TEST149 requested
- TEST148 source `b80b9ec4e6dbc8aeedf53dd914f50b18640b35e2`, run `38549433962`: 1526 core + 26 API34 passed with 0 failure/error/skip; debug and compiled history passed; 34 valid 320x640 PNGs visually reviewed.
- Artifacts: core `11885719618`/`627dafb34f33937bc80f2b7e4344fea5f240d9d93a74ad294dc2e7d84e8432ef`; Android `11886478533`/`db2c213f75e49b5755382e31dce7015c05eeadd210a2bff71a16e38fc69174ec`; APK `11886556253`/`c777f86d8eef71954bf6d9c54f98e0d302b720c2a758b1dac1c67bd52d0676f9`; contained APK `2b7dc53c96f9be31cbe250ed87681d919232dd9f31007972deaa5c91628e5e1a` checksum passed.
- Private delivery skipped; no owner install/signature comparison/physical-phone claim. TEST123 remains delivered fallback.
- TEST149 candidate `b66a31ada9c13beba67a3c8cc430668a5b8c9975` requires 1530 core/debug/history/26 API34/34 PNG; pending, with three Damage sliders disabled.


### 2026-10-14T11:00:37Z — TEST149 Dragon's Breath verified
- Source `af3fea7b8870be3e4f2e045e0bc69edfa1b4c0de`, run `38551940075`: 1530 core + 26 API34 passed, 0 failure/error/skip; debug and compiled immediate-previous-APK history passed.
- Artifacts: core `11886584059`/`517209dd85f751abac72382a50ba6a850cb3c43e2825aa36a93e07e9f71a5e33`; Android `11887522747`/`eb21e6bc5e54c3d7399643646417e1b1bff64b7756d4f0f6e4b868c0448ab82f`; APK `11887411460`/`77b4619f557e5447a116ff99e27ea19b7156795993c561e3c51e38caf6bd12c2`; contained APK `5a786a5c15fa9562f2fc4f6165d11f91cc85eaf9a2b7d4afa7a83ff0639feee7` checksum passed.
- 34/34 screenshots decoded at 320x640 and contact-sheet review found no obviously corrupt capture. Emulator evidence is not physical-phone evidence; private delivery skipped and TEST123 remains delivered fallback.
- Dragon's Breath provenance and four retained regressions are implemented and verified. Three Damage sliders remain disabled pending Inferno/Burning transfer/plants/vault/other offense and class/boss/combined acceptance.


## 2026-10-14T11:41:22Z — TEST150 Inferno/fire-transfer source candidate

- Reconciled owner documentation commit `116fc601549e83e02742b8541d3767f559ee2d85`; committed application/test/report candidate `57b3b5706f7478f742164d11c5a5ea74de94ed36` with an expected-head lease.
- Changed `CellDamageOrigins.java`, `TimedDamageOrigins.java`, `Fire.java`, `Inferno.java`, `Burning.java`, `InfernalBrew.java`; added `InfernoDamageProvenanceTest.java` and `APD-TEST150-INFERNO-FIRE-TRANSFER-2026-10-14.md`.
- Implemented immutable mixed creator retention for Inferno diffusion/save/direct and neighbouring ignition, Infernal Brew thrower capture, and Burning-to-terrain Fire transfer. Existing delayed Censer context now reaches Inferno. Original density/diffusion/turns/source objects/mitigation/RNG remain unchanged; missing historical metadata stays UNATTRIBUTED.
- Static source inspection and exact replacement guards completed. No real local Gradle repository exists in this execution workspace, so no local Java result is claimed. TEST150 exact-source 1536-core/debug/history/26-API34/34-PNG verification is pending.
- Three Damage controls remain disabled; Firebloom, vault/environment boundaries, other offensive sources and class/boss/combination acceptance remain. TEST149 stays the verified source and TEST123 stays the last owner-delivered APK.

- TEST150 run `38558479124` failed before compilation at `2026-10-14T11:43:11Z`: history validation identified the omitted reviewed path `docs/APD-TEST149-DRAGONS-BREATH-2026-10-14.md`. 0 core and 0 API34 tests ran; no APK/screenshots were produced. TEST151 adds only the missing manifest path and requests verification of the unchanged application candidate.



### 2026-10-14T12:10:50.744Z — TEST151/TEST152 Inferno provenance verified
- Re-read the actual approved branch head, current build request, handoff, completed ledger and source guide; preserved concurrent commit `299fbfb80c8030fb77b1e0c7c0402df8b76bfa08`.
- TEST150 run `38558479124` remains a history-gate failure before compilation: 0 core/API34 tests and no APK.
- TEST151 source `fe6527fe009234cdcf81edd60eebaba1ea5b0b7a`, run `38558697915` (`2026-10-14T11:44:25Z`–`11:55:00Z`), passed 1536 core + 26 API34, debug/history and 34 valid 320×640 captures. Core/APK/Android artifacts: `11891361617`/`11891381527`/`11891777754`; contained APK SHA-256 `19da0e373010fbe2fe94acc4d9992644834ed89e225a5505455198c1cd4d29e1`.
- TEST152 source `299fbfb80c8030fb77b1e0c7c0402df8b76bfa08`, run `38560042952` (`2026-10-14T11:55:40Z`–`12:06:54Z`), passed 1541 core + 26 API34 with 0 failure/error/skip, debug and immediate-previous TEST151 compiled history.
- Downloaded artifact ZIPs and used `sha256sum`, `unzip`, XML parsing, ImageMagick `identify` and `montage`. Android artifact `11892225995` ZIP SHA-256 `51fdde2b06edf22980edb2a1c6e8525796c320637db3ddee8baafd50642b1a93`; core `11891208926`/`751a48541c6b61c1f753428ead18988f94c53ac20aeba65066238d2161eaa1f5`; APK `11891463637`/`13712ba1a5570328809c12664eb268472e9996f06dbbde2fcaa6752b8ca24c68`; contained APK SHA-256 `4b8155d59d97859821205452b0136b273fed86ab35f3a52173198280a92f77a1` and checksum passed.
- Validated 34/34 Android PNGs at 320×640 and visually reviewed the contact sheet: expected readable menus/dialogs/gameplay, no corrupt capture. This is emulator evidence only.
- Verified implementation includes TEST150 Inferno/fire-transfer provenance plus TEST152 prevalidation, enclosing-context isolation and actual Brew/Censer source safety. Five new permanent safety methods plus six retained Inferno methods are included; tests were not weakened.
- Private delivery skipped; TEST123 remains the last owner-delivered/signature-compared APK. Three Damage controls remain disabled. Remaining work: plants, vault/boss, spells/abilities/derived offense, delayed state changes and all-class/boss/combination acceptance. No score/save-format/signing/app-ID/release change.


### 2026-10-14T12:37:39.766Z — TEST153 offensive plant provenance candidate
- Reconciled latest real head `566d99427b6216d60392c424040b56e45620bf10`; read current handoff, build request, source guide, completed ledger, complete Plant/all standard plant classes and all known creation callers.
- Reproduced by source inspection: plants had no creator field, so player-created Firebloom/Sorrowmoss/Rotberry entered verified Fire/Poison/Toxic Gas as UNATTRIBUTED.
- Implemented additive immutable origin storage with missing-key legacy fallback to UNATTRIBUTED. Manual seed planting, Wand of Regrowth, cursed Random Plant, Regrowth Bomb, Sandals of Nature and Overgrowth capture actual initiating origins. Natural plants remain unowned.
- Added six retained PlantDamageProvenanceTest methods covering all20 percentages/all3 roles, natural exclusion, save/legacy, neutral RNG/field identity and source wiring.
- No local Gradle checkout exists; no local compilation/test result is claimed. Full TEST153 1547core/debug/history/26API34/34PNG is pending.
- Three Damage controls remain disabled. No score/save requirement/signing/app-ID/release change.


- TEST153 run `38565297532` failed the history-input gate at `2026-10-14T12:38:44Z`: omitted reviewed TEST151/TEST152 evidence documents. 0 core/API34 tests ran; no APK/screenshots. TEST154 adds only those manifest paths and retries unchanged application source.


- TEST154 run `38565512376`: history passed; 1547 core compiled/executed, 1545 passed and two new fixture methods failed because natural Rotberry/Firebloom remained at cell0 while target/baseline used cell312. Core artifact `11895296228`, ZIP SHA-256 `e3b8acbbc953e31599fb6641a1bf81943e58aca340fbf1649b4f195ee7beaf5f`. No APK/API34/screenshots.
- TEST155 sets those fixture positions to312 only; production source/assertions unchanged.


### 2026-10-14T12:57:45.506Z — TEST155 offensive plant provenance verified
- TEST155 source `3a5c2b233075e78ca4f99298f5423e8bd8f45035`, run `38566036821` (`2026-10-14T12:43:45Z`–`12:56:16Z`) passed 1547 core + 26 API34 with 0 failure/error/skip; debug assembly and compiled immediate-previous TEST152 history passed.
- Downloaded artifacts; `sha256sum` verified core ZIP `53f0f31c4ef63042fe755c11cc2e8eb40f039b4b44044d4fd9e7a083faccefb0`, Android ZIP `185a53912d9e8f35af0edc211a3789f270e0e49ff46f943ef1c9a8e29db1d1d1`, APK ZIP `f7531c6d4c3c33ce0d8084952d9b0cc0a205bc400b84a679d55de3ed28c11b36` and contained APK `870a1e9a79882eddd37d3b9fa1e47c3f8e22333bafc1cf861340504350d8d063`.
- Parsed downloaded XML:1547 core and26 Android, all passing. ImageMagick `identify` validated34/34 PNGs at320×640; `montage` contact-sheet review found expected readable menus/dialogs/gameplay and no corrupt capture.
- Verified offensive plant origin persistence/callers and exact-once Fire/Poison/ToxicGas boundaries; natural and missing historical metadata remain unowned.
- Private delivery skipped; no owner install/signature comparison/physical-phone claim. TEST123 remains owner-delivered fallback; three Damage sliders remain disabled.
- Preliminary read-only vault scan identified special direct-damage callers in VaultBossElemental/VaultSentry and environmental VaultFlameTraps/VaultLaser. No vault source changed in this invocation.



### 2026-10-14T13:35:40.277Z — TEST156 Vault boss provenance candidate
- Re-read latest head `3900a1dbfa92927ca204758f06ccf0855723bead`, handoff, build request, source guide and completed ledger; no newer developer advance observed before preparation.
- Read full VaultBossElemental, VaultSentry, VaultFlameTraps and VaultLaser caller/state/save paths. Kept the three quest machines environmental; wired seven boss-owned attack boundaries without changing rolls, source objects, status duration, geometry, turns, RNG or quest scoring.
- Added four permanent VaultBossDamageProvenanceTest methods covering20 percentages, boss replacement, neutral source/RNG identity, complete boss caller guards and environmental guards.
- No local execution workspace; candidate is uncompiled pending1551core/debug/history/26API34/34PNG CI. Three Damage sliders remain disabled; no delivery/release/phone claim.



### 2026-10-14T13:50:03Z — TEST156 Vault boss provenance verified
- Exact source `2af7a808c53f2325c56d88124b8eb71de1e2b526`, run `38573246711` (`13:36:49Z`–`13:49:13Z`) passed1551 core +26 API34, debug and compiled immediate-previous TEST155 history, 0 failure/error/skip.
- Downloaded/checked core `11898621235`/`74794fab9c0f0038c72bda6bbbda68a6d9af45fe7c842ea68630a92b73553e24`; Android `11899122092`/`cc34aa6e9e69a0de5ca98920e5510fe9595c830c85d1571f950f7f97792b915a`; APK ZIP `11899205759`/`1caa8befa972770a9169e2eb3cdb95eed15cec37687b37173138e720912cd61d`; contained APK `ee5307c5738313d6b82f46fab53c9e6dda07713d427f0eb6d1d01f389004d817`, checksum passed.
- 34/34 PNGs decoded at320×640; contact-sheet review found expected readable UI/gameplay and no corrupt capture. Emulator only, no physical-phone claim.
- Private delivery skipped; TEST123 remains owner-delivered fallback. Three Damage sliders remain disabled; next harmful-buff/derived and remaining ability/spell/class/boss acceptance work.


## 2026-10-14T14:39:49.724Z — TEST157 player ability derived-damage candidate
- Based on latest real head `badbcae625a124a416f02bc9a948ae3f1fa439f1`; changed Elemental Blast, Elemental Strike and Blast Wave delayed collision ownership without altering neutral behavior.
- Added four retained `PlayerAbilityDamageProvenanceTest` methods. No local Gradle workspace is available; 1555 core/debug/history/26 API34/34 PNG verification is pending.
- Harmful-buff reaction attribution is explicitly deferred because attach callbacks run before creator metadata is assigned. Three Damage sliders remain disabled.
- TEST156 remains the latest verified application source; TEST123 remains the last owner-delivered/signature-compared APK.


## TEST157 CI result and TEST158 fixture correction — 2026-10-14T14:43:42.114Z
- TEST157 source `514ccd2e9090af9881f6cb30b84c156179506e19`, run `38582589811`: history passed; production/tests compiled; 1555 core tests executed, 1554 passed and one new source guard failed because it counted four `throwCharAttributed(` occurrences (two wand call sites, the legacy delegate, and the method declaration) while expecting three.
- Core artifact `11903652610`, ZIP SHA-256 `569e9c6728d9dfdd424bcd30ca35cbb1a1874773960cc417ca9959e156ba3ef9`. Debug APK/history and API34 jobs were skipped; no APK/screenshots were produced.
- TEST158 changes only that expected source count from 3 to 4. Production code and behavioral assertions are unchanged. Exact-source 1555-core/debug/history/26-API34/34-PNG verification remains pending; TEST156 remains the verified source.


## TEST158 full verification outcome — 2026-10-14T15:00:05.913Z
- Exact source `d3ed403a6d6a86f5704ea6ee9c4cc4ac5c9779ee`, run `38583123803` (2026-10-14T14:43:52Z–14:56:54Z) completed successfully.
- Downloaded XML proves 1555 core and 26 API34 tests passed with 0 failures, errors, or skips. Debug assembly and compiled history against the immediate previous TEST156 APK passed.
- Artifacts: core `11903878110`, ZIP SHA-256 `12ab5a2bc392d84fb06a2632676e6cf4a42f39f5dcc00890b113dab715ed549d`; Android `11905076416`, ZIP SHA-256 `3ad192c6228a3996e383de542f48da5e985f72d03e74d99155ab28a438829b38`; APK `11904023089`, ZIP SHA-256 `2f6aff7658490cd472b1835f4c3208c3dc1ea6f28eaa1f13d6eaf7bfd064290d`.
- Contained `ADHD-Pixel-Dungeon-CODEX-TEST-158.apk` SHA-256 `92542d7ca06672e0a72ea51494480ec3ba6f976eb5aa690e6bd1b2c78cf3233b`; the bundled checksum matched.
- All 34 screenshots decoded at 320×640. Contact-sheet review found expected readable menus, dialogs, history/score states, tiles and gameplay frames with no corrupt capture. This is emulator evidence, not physical-phone evidence.
- Verified implementation: Elemental Blast direct damage/Fireblast burning/delayed collision; Elemental Strike six secondary direct sources/two delayed collisions/conjured explosion; Blast Wave immutable collision ownership with legacy callers remaining unowned. Original source objects, mitigation, rolls, RNG, geometry, turns and neutral 100% behavior are retained.
- Private delivery was skipped. TEST123 remains the last owner-delivered/signature-compared APK. Three Damage controls remain disabled pending the deferred harmful-buff reaction, remaining spell/ability/derived sources, and six-class/boss/combination acceptance.


## 2026-10-14T15:35:09.392Z — TEST159 remaining armor-ability candidate
- Based on latest real head `9268f8daf939ae75439078370c63d90634eaa6c5`; implemented Heroic Leap Body Slam/Impact Wave, both Shockwave paths, and enemy-only Warp Beacon telefrag provenance.
- Preserved fixed manual armor ordering and the Warp Beacon hero self-cost exclusion; classified the remaining armor abilities as already delegated or inherited.
- Added five retained `RemainingArmorAbilityDamageProvenanceTest` methods. No local Gradle workspace; 1560core/debug/history/26API34/34PNG pending.
- Three Damage sliders remain disabled. TEST158 remains the latest verified source; TEST123 remains the owner-delivered fallback.


## TEST159 full verification outcome — 2026-10-14T15:51:12.335Z
- Exact source `8b5882b21d1c957846312296e471724c5a7207a0`, run `38590690277` (2026-10-14T15:35:36Z–15:49:14Z) completed successfully.
- Downloaded XML proves 1560 core and 26 API34 tests passed with 0 failures, errors or skips. Debug assembly and compiled history against the immediate previous TEST158 APK passed.
- Artifacts: core `11906709551`, ZIP SHA-256 `08c40465144105e00c1f0ebe545b4c09688fc5d34c717dbb3072fd661bb2d8da`; Android `11908206800`, ZIP SHA-256 `56a4ded6e107d2e0153737ed52ce36392125737fafb3ba5e65b1611909f2315c`; APK `11906599749`, ZIP SHA-256 `2476f022c8992f2943d9ccb638f25b660006c1b570ee38b8978b7405c462e227`.
- Contained `ADHD-Pixel-Dungeon-CODEX-TEST-159.apk` SHA-256 `dc5cb56b015a7d9cf98a5f3d246979e2c1f2b0dd562291c489797057ffd16fc7`; bundled checksum matched.
- All 34 screenshots decoded at 320×640. Contact-sheet review found expected readable menus, dialogs, history/score states, stairs, tiles and gameplay frames with no corrupt capture. Emulator evidence only.
- Verified Heroic Leap Body Slam/Impact Wave, both Shockwave damage branches and enemy-only Warp Beacon telefrag. Manual armor remains after scaling, hero telefrag self-cost stays unscaled, and Wild Magic/Spectral Blades/Hawk/Shadow Clone retain their verified delegated or inherited paths.
- Private delivery was skipped. TEST123 remains the owner-delivered/signature-compared fallback. Three Damage sliders remain disabled pending the deferred harmful-buff boundary, other non-armor offensive sources, and six-class/boss/combination acceptance.



## 2026-10-14T16:36:20.526Z — TEST160 offensive scroll damage candidate
- Reconciled latest real head `62b3027ffdfa46dd9fd67c557e7249257d6e180d`; read current handoff/build request/source guide/ledger and all 99 artifact, potion, scroll and spell sources.
- Implemented Player Damage for Scroll of Retribution and Scroll of Psionic Blast enemy damage only; Psionic Blast hero self-cost stays unscaled.
- Added four retained methods covering all20 percentages, exact-once/neutral RNG/source identity, complete caller wiring and self-cost exclusion.
- No local Gradle workspace is available; 1564core/debug/immediate-previous TEST159 history/26API34/34PNG remains pending.
- Three Damage sliders remain disabled. TEST159 remains latest verified source; TEST123 remains owner-delivered fallback.


## TEST160 full verification outcome — 2026-10-14T16:51:35.140Z
- Exact source `9a071ff3312a6ae311884429c8dca4d6d0510bb9`, run `38599525580` (2026-10-14T16:36:32Z–16:49:08Z) completed successfully.
- Downloaded XML proves 1564 core and 26 API34 tests passed with 0 failures, errors or skips. Debug assembly and compiled history against the immediate previous TEST159 APK passed.
- Artifacts: core `11912327744`, ZIP SHA-256 `28da9888190c468a488bcdb61674e3de2b80d46e2745b79479d2795430b127c3`; Android `11913009126`, ZIP SHA-256 `66da7ed913422c7bc00c7cbe404340ecf1b3c84f4385c45f29a3dcba0d2db8cd`; APK `11912287722`, ZIP SHA-256 `b81b6b7540ad5d4f205f16b0354cd7ab3e0122c7ff03a0a94ff2747da4bea020`.
- Contained `ADHD-Pixel-Dungeon-CODEX-TEST-160.apk` SHA-256 `0be385ddb52c4a0c1c38b55f5327af01d8e3489816e709d41c566101d4b39282`; bundled checksum matched.
- All 34 screenshots decoded at 320×640. Contact-sheet review found expected readable changes/history, rules/score, UIMSD, stairs, tiles and gameplay states with no corrupt capture. Emulator evidence only.
- Verified Scroll of Retribution and Scroll of Psionic Blast enemy damage use Player Damage exactly once while keeping original formulas/sources and leaving Psionic Blast hero self-damage unscaled.
- Current branch head advanced afterward with an independent Corruption save-integrity fix, so TEST160 does not verify that later application change. Its next exact-source checkpoint remains pending.
- Private delivery was skipped. TEST123 remains the owner-delivered/signature-compared fallback. Three Damage sliders remain disabled pending remaining item-derived/harmful-buff boundaries and six-class/boss/combination acceptance.


## 2026-10-14T16:52:55.080Z — TEST161 artifact-derived damage candidate
- Reconciled current head `27835d898d1a09a462c0cc49f8cfc3915d945497`, preserving the independent Corruption float-reload fix and its four tests.
- Implemented Player Damage for Guiding Light artifact procs and Cape of Thorns outgoing retaliation; retained fixed incoming deflection and non-damaging Skeleton Key movement.
- Added four retained methods covering all20 percentages, neutral RNG/source identity, source/order wiring and movement exclusion. Expected current total:1572core.
- Exact-source debug/immediate-previous TEST160 history/26API34/34PNG pending. Three Damage controls remain disabled; TEST123 remains owner-delivered fallback.


## TEST161 full verification outcome — 2026-10-14T17:13:21.216Z
- Exact source `9088ca30acde28282454598b1e649a3a70156b56`, run `38601864296` (2026-10-14T16:53:05Z–17:11:45Z) completed successfully.
- Downloaded XML proves 1572 core and 26 API34 tests passed with 0 failures, errors or skips. Debug assembly and compiled history against the immediate previous TEST160 APK passed.
- Artifacts: core `11913464893`, ZIP SHA-256 `5448af610d936195b20fa40d209e275c3893ed1c4bcfb04b0fb9c9ca677b9486`; Android `11915615198`, ZIP SHA-256 `e205d77fddff25053e9d9a8bf52f5212cc1d9087574c06aeef5d1f5cd0390276`; APK `11913524552`, ZIP SHA-256 `06b3eced60b35a2d5193c1656dca6fd6214ee4a719c1f354501cfe3aba59f24b`.
- Contained `ADHD-Pixel-Dungeon-CODEX-TEST-161.apk` SHA-256 `b01c72f6643af083a7fbf0d0bbb71dc8bde82152ed588d31e1cae527fb604b0b`; bundled checksum matched.
- All 34 screenshots decoded at 320×640. Contact-sheet review found expected readable changes/history, rules/score, UIMSD, stairs, tiles and gameplay states with no corrupt capture. Emulator evidence only.
- Verified the Corruption fractional save/reload correction plus Guiding Light artifact damage and Cape of Thorns outgoing retaliation. Cape incoming deflection remains based on the original roll; Skeleton Key movement remains non-damaging.
- Private delivery was skipped. TEST123 remains the owner-delivered/signature-compared fallback. Three Damage sliders remain disabled pending harmful-buff-safe attribution and six-class/boss/combination acceptance.


## 2026-10-14T17:53:07.187Z — TEST162 harmful-buff attachment candidate
- Reconciled actual head `7fd4e51d4ce6137d9c1baf84f39ac3dceb604fa4` after verified TEST161; reread build request, source guide, handoff and ledger.
- Audited all current production Java sources for Burning attribution and Chill/Frost attachment entry points. Confirmed the vault elemental reacts inside `add(Buff)`, before prior caller-side provenance setters ran.
- Implemented transient pre-attach immutable origin metadata in `Buff`, preconfigured single/mixed Burning causes, and exactly-once vault reaction scaling while preserving the original buff source.
- Added persistent per-cell Freezing provenance with save/reload and legacy-unknown behavior; attributed Frost potion, frost bomb, Elemental Strike and direct wand/imbue/enchantment/dart callers. Environmental traps/rooms remain unscaled.
- Added five retained regressions for player, boss, mixed saved field, legacy environment and neutral RNG/source behavior; expected total 1577 core.
- No real local Gradle workspace is available, so exact-source core/debug/API34/history/visual verification is pending. Three Damage controls remain disabled. TEST161 remains latest verified source; TEST123 remains owner-delivered fallback.


## TEST162 pre-build failure — 2026-10-14T17:56:56.152Z
- Exact source `50e61eb7d517d70566d8a06878a8709d1fd32e65`, run `38610738155` (2026-10-14T17:55:12Z–17:56:02Z) failed in history validation before compilation.
- Demonstrated cause: the comparison from compiled TEST161 source also contains its later verification record `docs/APD-TEST161-ARTIFACT-DERIVED-DAMAGE-2026-10-14.md`; that reviewed path was absent from the TEST162 manifest.
- Core, debug and API34 checks did not run; no APK or screenshots were produced. Application source was not tested and remains candidate-only.
- The manifest now includes that exact already-reviewed evidence path. TEST163 requests the same application source with corrected complete history. TEST161 remains latest verified; three Damage controls remain disabled.


## TEST163 compile failure — 2026-10-14T18:05:22.000Z
- Exact source `620562d4ce3e809aa90d2636ffc16ef3b0d0232d`, run `38611095532` (2026-10-14T17:57:44Z–18:04:15Z) passed complete-history validation, then failed `:core:compileJava`.
- Both errors were in `Buff.appendAttributed`: Java rejected private `apdPrepareAttach` calls through generic receiver type `T extends Buff`.
- The correction casts the receiver to the declaring `Buff` type before the same two private calls. No behavior, state, RNG, source or save-format logic changed.
- Tests, debug assembly and API34 did not run; no APK or screenshots were produced. TEST161 remains latest verified; three Damage controls remain disabled.


## TEST164 verification — 2026-10-14T18:23:48Z
- Exact application source `166c8a287b599fcb0f741a446f039fc33f656201`; GitHub Actions run `38612344678` ran 2026-10-14T18:06:32Z–18:18:07Z and concluded success.
- Core artifact `11917672775` (ZIP SHA-256 `a92f05678a40a517822e8cf3aee8a9e168dffb90078e97f31478b51526cf58b6`) contains 1,577 tests, 0 failures, 0 errors and 0 skipped.
- Debug APK assembled and complete reviewed history matched the immediate previous compiled TEST161 APK. APK artifact `11917912521` (ZIP SHA-256 `b1acddb6d3c46e1e3a1ba5e0b3b9595564402dffed2175359297f2e268ad4f12`) contains `ADHD-Pixel-Dungeon-CODEX-TEST-164.apk`, SHA-256 `5b6f46bebbb54f189142f4e0b1a14dbf7d204bf2a06f5fbae030efac3bee2919`.
- Android artifact `11918874056` (ZIP SHA-256 `99b8ade78132ca383f332901acc71c9180174120cc4ab641105f075f48b8492b`) contains 26 API34 emulator tests, 0 failures, 0 errors and 0 skipped. All 34 PNGs decoded as 320×640; the contact-sheet inspection found no blank, corrupt or visibly clipped capture.
- Verified behavior: harmful Burning/Chill/Frost attachment carries the actual single or mixed creator into immediate reactions and saved Freezing fields; environmental and legacy origin remain unscaled; neutral values preserve source, amount and RNG. No save-format change was introduced.
- Private delivery remained skipped. This is emulator/CI evidence only, not a physical-phone claim. TEST123 remains the last owner-delivered/signature-compared APK. Three Damage controls remain disabled pending six-class, boss-phase and rule-combination acceptance.


## 2026-10-14T18:45:47Z — TEST165 boss-phase damage acceptance candidate
- Reconciled actual head `7a4ff110e04f0005d5afe06bad9a67486d13c81d`; reread the current handoff, build request, completed ledger and source guide. Did not rebuild TEST164 unchanged.
- Audited Goo, Tengu, DM-300, Dwarf King and Yog-Dzewa direct damage sites. Confirmed omitted boss boundaries in Tengu bombs/fire, DM-300 falling rocks, Dwarf King summon collisions and Yog death rays; Tengu shocker used a hard-coded boss origin without saved creator provenance.
- Implemented exactly-once creator scaling with original source/RNG/turn behavior, pre-armor Tengu bomb order, saved mixed Tengu fields and legacy-unknown behavior. Dwarf King self-damage remains unscaled; Goo needed no source change.
- Added four retained tests covering all six hero classes, five bosses, Tengu delayed-field save/legacy behavior and source-contract/mitigation order. Expected core total 1,581.
- Exact-source core/debug/immediate-previous TEST164 history/26 API34/34 PNG verification is pending. Three Damage controls remain disabled; no delivery or phone claim.


## TEST165 core failure / TEST166 correction — 2026-10-14T18:51:04Z
- Exact source `314b678c1aa7c85d4a84f6d5a02ad1929f43ecab`, run `38618183149` (2026-10-14T18:47:45Z–18:49:47Z) passed complete-history validation and compiled all production/test source, then ran 1,581 core tests with 2 failures.
- The new headless Tengu fire regression entered the real visual particle path without a renderer and failed at `CellEmitter.get`; it now exercises the same persisted per-cell origin at the pre-attach Burning entry point, avoiding any weakened assertion.
- Existing acceptance correctly exposed a compatibility regression: a newly created Tengu shocker field seeded without transient context was treated as environmental (16 damage) instead of Tengu-owned (32 at 200% Boss Damage). New Tengu-only fields now default to Boss ownership, while restored historical fields lacking metadata remain explicitly unknown and unscaled.
- Debug, APK, API34 and screenshots did not run; no artifact was produced. TEST164 remains latest verified. TEST166 requests the corrected 1,581-test exact source; three Damage controls remain disabled.


## TEST166 verification — 2026-10-14T19:06:38Z
- Exact source `e252e92e6533ac0a3fa581048475bf55fdd5e3da`; GitHub Actions run `38618850435` ran 2026-10-14T18:52:25Z–19:05:24Z and concluded success.
- Core artifact `11922055137` (ZIP SHA-256 `d6eddd28a21303235b861d504622183fadce7dfd537bfd227d3c7a1eea2c9a0a`) contains 1,581 tests, 0 failures, 0 errors and 0 skipped. The four new boss-phase and eight existing electricity tests passed.
- Debug APK assembly and complete reviewed history against immediate previous compiled TEST164 passed. APK artifact `11921765201` (ZIP SHA-256 `77a377ace0806711bb98726850858029c33f9760c86e52c1b6bf303864b86698`) contains `ADHD-Pixel-Dungeon-CODEX-TEST-166.apk`, SHA-256 `984170746be694a64bc462febcf072292321760ffba3f4e596828f3adec1f614`; bundled checksum matched.
- Android artifact `11922116695` (ZIP SHA-256 `ba39a535b6528081553e1b357b52fe5257ae84a972b0aea041e3e77667ff04d3`) records 26 API34 emulator tests, 0 failed and 0 ignored. All 34 screenshots decoded at 320×640; contact-sheet review found expected readable history/rules/score/UIMSD/stairs/tiles/gameplay states with no blank, corrupt or visibly clipped capture.
- Verified behavior: all six classes and five main bosses select the intended damage origin; Tengu bomb/fire/shocker, DM-300 rocks, Dwarf King summon collisions and Yog rays use exactly one creator boundary. Newly created Tengu fields preserve boss ownership while metadata-less historical fields remain unknown/unscaled. Dwarf King self-damage remains unchanged.
- This is CI/emulator evidence only. Private delivery was skipped; TEST123 remains last owner-delivered/signature-compared. Three Damage controls remain disabled pending high-HP/defense, save/load and cross-rule acceptance.


## 2026-10-14T19:44:18Z — TEST167 all-18-controls acceptance candidate
- Reconciled head `4c309fd7e82ad29ada0b89428f3d046e6252a6f0` and did not rebuild verified TEST166 unchanged.
- Added distinct combination acceptance for all six classes at high enemy HP/defense and low Player Damage/upgrade count; actual enemy-versus-boss replacement damage with high Player Defense and fractional healing; and real all-18-value save/load with damaged high Player HP, difficulty/balance, nine challenges, class and seed.
- Existing all-class generation acceptance still covers spawn/hunger/food/HP/scroll interactions, so it was retained rather than duplicated.
- Marked the three Damage controls available for next-run drafts only. Active-run settings remain locked; original scores, experimental warning and pending-scoring notice are unchanged.
- Expected total: 1,585 core plus debug/immediate-previous TEST166 history/26 API34/34 PNG. Candidate-only until exact-source verification passes; TEST123 remains owner-delivered fallback.


## 2026-10-14T19:52:07Z — TEST167 failure diagnosed; TEST168 bounded correction
- TEST167 source `57b29f9ec716aabb20237a963fe8d32e4bbfb930`, run `38626757231`, completed 1,585 core tests with 2 failures; debug/APK/history compilation follow-up/API34/screenshots were skipped.
- Diagnosed the new headless real-attack fixture entering `Hero.interrupt()` with a null visual selector. Disabled only that fixture's normal damage-interrupt visual behavior; real attack, exactly-once scaling, mitigation, source and healing assertions remain.
- Diagnosed invalid manifest sprite `V061_CHANGE`; replaced it with existing native `V064_CHALLENGES`. No history guard or gameplay assertion was weakened.
- TEST168 requests the corrected 1,585-test exact source and full unchanged acceptance matrix. TEST166 remains latest verified; TEST123 remains owner-delivered fallback.


## 2026-10-14T19:57:08Z — TEST168 failure diagnosed; TEST169 fixture correction
- TEST168 source `3a70f85f706dcf56553af296c1a44f98635a5af7`, run `38627408866`, ran 1,585 core tests with 1 failure. The corrected history manifest passed; no APK/API34/screenshots were produced.
- The real enemy hit completed attack and mitigation but normal serious-damage feedback in `Hero.damage()` invoked the absent headless scene selector. Overrode only `FixedHero.interrupt()` as a no-op; application code and all combat/healing assertions remain unchanged.
- TEST169 requests the corrected 1,585-test exact source and full acceptance matrix. TEST166 remains latest verified; TEST123 remains owner-delivered fallback.


## 2026-10-14T20:12:02Z — TEST169 verified all 18 controls
- Exact source `a160dddb4f39fe2e3eb62a601e13bea71730f064`, run `38628060548` (2026-10-14T19:57:58Z–20:10:25Z), concluded success.
- Downloaded core artifact `11925865590` / ZIP SHA-256 `50b6173b27f99c9e7afb21d5d68ae5000e321f0b3e6931a143b6c72050340dca`: 1,585 tests, 0 failures/errors/skips.
- Downloaded APK artifact `11925995486` / ZIP SHA-256 `5981cc7ec25c2771c4f617a0ead99d489bd9eb03b0577d5e81155030f29ccb7e`: APK SHA-256 `ff0d7f10bcef18c7c247bfaee30e7f913ee7ecc699433103fca7a222c3090559`; bundled checksum matched. Debug/history comparison against TEST166 passed.
- Downloaded Android artifact `11924908702` / ZIP SHA-256 `f49c3775af89bda528d61f870c864975b77ce704a4803c40a3607613d839c196`: 26 API34 tests, 0 failed/ignored; 34/34 PNGs decoded 320×640 and passed contact-sheet/focused visual review.
- Verified all 18 controls are available for next-run drafts while current-run controls remain locked. Original scores/scoring identity unchanged; provisional ranges are not finalized.
- Private delivery skipped; no physical-phone claim. TEST123 remains owner-delivered fallback. Do not rebuild unchanged TEST169.


## 2026-10-14T20:38:38Z — DerivedStore equal-length source replacement fix prepared
- Read the current DerivedStore implementation and retained cache tests at branch head `9de2da09e0238dd526b2f298db715b6ceb1456b0`; confirmed cache source fingerprints contained only source kind, filename and byte length.
- Implemented content identity using byte length plus SHA-256 for each raw run/legacy log. Unreadable sources receive a deliberately non-reusable marker, so repeated read failure cannot make an incomplete derived cache look current.
- Bumped derivation version 7→8, causing one automatic derived-cache rebuild. Raw logs, game saves, application ID, signing and scoring are unchanged.
- Added one retained regression that replaces a valid raw log with a different valid log of exactly the same length and requires `REBUILT_STALE`, the replacement score and subsequent cache reuse.
- Local execution workspace was absent; no local test or build claim. TEST170 exact-source CI requests 1,586 core tests, debug assembly, immediate-previous TEST169 history, 26 API34 checks and 34 screenshots. Candidate remains pending until actual evidence passes.


## 2026-10-14T20:53:32Z — TEST170 verified DerivedStore raw-source identity
- Exact source `8da173025d4ab64fea123ee03f9afdbd4efc02f1`; run `38634072761` completed successfully from 2026-10-14T20:40:35Z through 2026-10-14T20:51:50Z.
- Downloaded core artifact `11926858030` / ZIP SHA-256 `d0ea132583f074fe7661cecb11fa0c9c2aa6255d02e15be7b87297d6c660799e`: 1,586 tests, 0 failures/errors/skips. The new equal-length valid-log replacement method passed.
- Downloaded APK artifact `11926349387` / ZIP SHA-256 `b1168659dedee4441b448591275ead4cd0f3beb83c46bfb0d96d98b158c81ddf`: APK SHA-256 `e9117959406a5893069e6f90f626386f335daa2f9b7f3c91c3ef3dd3ecb2e43c`; bundled checksum matched. Debug assembly and immediate-previous TEST169 compiled-history validation passed.
- Downloaded Android artifact `11927293898` / ZIP SHA-256 `774f8999ff0efe1d3491036f1a26d2a3fe20cb062a9dd9f60fde45f726da2b6c`: 26 API34 tests, 0 failures/errors/skips; 34/34 PNGs decoded at 320×640 and passed contact-sheet review.
- Verified stale derived statistics are not reused after a valid same-name/same-length raw-log replacement. Version8 performs one derived-cache rebuild; no raw logs, saves, scores, signing identity or application ID changed.
- Private delivery skipped; no physical-phone claim. TEST123 remains the last owner-delivered/signature-compared APK. Do not rebuild unchanged TEST170.


## 2026-10-14T20:58:00Z — RunIndex cross-instance temp-file race correction prepared
- Re-read current `RunIndex.save()` and its retained tests at branch head `7b6601811a8f9c1889179f5f6d8e16eaef2c0ba7`. Confirmed the instance monitor does not coordinate separate RunIndex objects even though all target the same fixed `index.apdi.tmp` path.
- Serialized only the on-disk atomic replacement section across instances; index encoding, CRC, rebuild behavior and file format remain unchanged.
- Added a deterministic two-instance regression using latches: the first writer holds the temp file open, the second must remain outside the shared replacement section, then both complete and the second complete index loads without rebuild.
- No local execution workspace was available; no local test/build claim. TEST171 exact-source CI requests 1,587 core tests, debug assembly, immediate-previous TEST170 history, 26 API34 tests and 34 screenshots. Candidate remains pending.

## 2026-10-14T21:10:48Z — TEST171 verified RunIndex cross-instance save serialization
- Exact source `169d2bec53968f191286e3db6cb77efe7661ef06`; GitHub Actions run `38636484010` ran from 2026-10-14T20:58:00Z through 2026-10-14T21:08:41Z and concluded success.
- Core artifact `11929355257` / ZIP SHA-256 `985ea8ff5e84b41f56671f82ee4c497f400b8eb5aeb3aab37bf3706c0f60f37`: 1,587 tests, 0 failures/errors/skips. The controlled two-instance save regression passed.
- APK artifact `11927959848` / ZIP SHA-256 `3e7e5319995a789c71d738816634861f901af72a0d3165e436195ed865d7594f`: `ADHD-Pixel-Dungeon-CODEX-TEST-171.apk` SHA-256 `e5038b2055ca71f7cf0b432d05f1d4e70dddbc8b38325ffd28e1934288c23c22`; bundled checksum matched. Debug assembly and immediate-previous TEST170 compiled-history validation passed.
- Android artifact `11929576415` / ZIP SHA-256 `6547a27802d04e527b019cc4fef6ff9b7fa4ace66e1ea724d71d06638f4f5eff`: 26 API34 emulator tests, 0 failures/errors/skips. All 34 PNGs decoded at 320×640; contact-sheet review found the expected readable history, rules, score, UIMSD, stairs, tiles and gameplay states with no blank, corrupt or visibly clipped capture.
- Verified separate RunIndex instances cannot concurrently enter the shared temp-file replacement section; both saves finish and the final second index loads without rebuild. Index format/CRC/order/rebuild behavior, raw logs, player saves, gameplay, scoring, application identity and signing are unchanged.
- Private delivery was skipped; this is CI/emulator evidence only. TEST123 remains the last owner-delivered/signature-compared APK.
- Concurrent branch head `c684d5070fc26bca6cc2d34b414ea18ecbefbb49` adds independent Crush splash scaling after the exact TEST171 source. This evidence does not verify that newer application source; it was preserved and requires separate exact-source checks.

## 2026-10-14T21:11:59Z — TEST172 Crush splash candidate reconciled
- Re-read the approved branch after TEST171 and preserved concurrent commit `c684d5070fc26bca6cc2d34b414ea18ecbefbb49` rather than overwriting it.
- Warrior Crush splash now applies the actual hero's Player Damage multiplier once to its offensive splash roll before the existing halving, target DR and Vulnerable steps.
- Three retained methods cover all permitted Player Damage percentages, selection of Player rather than Enemy/Boss Damage, pre-armor results, neutral rounding and next-RNG identity.
- TEST171 evidence was committed independently at `a187509e81d0100128884b487283e190172abaa8`; it verifies source `169d2bec53968f191286e3db6cb77efe7661ef06`, not this newer application source.
- TEST172 exact-source verification requests 1,590 core tests, debug assembly, complete history against immediate previous compiled TEST171, 26 API34 emulator tests and 34 screenshots. Candidate remains pending; no save, scoring, delivery or phone claim.

## 2026-10-14T21:26:00Z — TEST172 verified Crush splash Player Damage scaling
- Exact source `12795cd9cd3049864e1f33c90b9551e9bda67c8e`; GitHub Actions run `38638453521` ran from 2026-10-14T21:12:37Z through 2026-10-14T21:25:00Z and concluded success.
- Core artifact `11928792925` / ZIP SHA-256 `d0a82f9c7f2e3abd30f23fb99385c15c436c9ad20fe98f270c6e48b6e97b8851`: 1,590 tests, 0 failures/errors/skips. All three Crush regressions passed in 0.019 seconds.
- APK artifact `11929357443` / ZIP SHA-256 `1636835f4aa85a5ea95565fe7ec658688a67bbf9b0483d2bc921df73ef458ac8`: `ADHD-Pixel-Dungeon-CODEX-TEST-172.apk` SHA-256 `5a9739829b398ab9fbc4c1ae83c78e06dbddd23806a854406d58442e5375f153`; bundled checksum matched. Debug assembly and immediate-previous TEST171 compiled-history validation passed.
- Android artifact `11929688691` / ZIP SHA-256 `ff348102d2c2705af15e303d98e498c5d3a12483763df0ed09d9e1415a72ab46`: 26 API34 emulator tests, 0 failures/errors/skips. All 34 PNGs decoded at 320×640; contact-sheet review found expected readable TEST172 history, rules, score, UIMSD, stairs, tiles and gameplay states with no blank, corrupt or visibly clipped capture.
- Verified Warrior Crush splash selects Player Damage rather than Enemy/Boss Damage and applies it exactly once before the original halving, DR and Vulnerable order. Neutral values retain original rounding and next RNG.
- Private delivery was skipped; this is CI/emulator evidence only. No player-save, scoring, application identity, signing or physical-phone claim. TEST123 remains the last owner-delivered/signature-compared APK. Do not rebuild unchanged TEST172.

## 2026-10-14T21:36:33Z — TEST173 Fireblast staff explosion candidate
- Re-read the verified TEST172 tree and audited current direct-damage sites rather than rebuilding it unchanged.
- Confirmed normal Fireblast wand casts and fire fields were attributed, but the distinct Mages' Staff on-hit explosion still called direct target damage.
- Routed that final raw staff-explosion roll through the actual attacker exactly once while preserving the original `Hero.heroDamageIntRange` roll, power multiplier, Wand of Fireblast source, targets, direct-damage mitigation and RNG order.
- Added three retained tests for all permitted factors and player/enemy/boss creators, contradictory settings, one source-preserving hit, neutral amount/next RNG and exact production wiring.
- TEST173 exact-source verification requests 1,593 core tests, debug assembly, complete history against immediate previous compiled TEST172, 26 API34 emulator tests and 34 screenshots. Candidate remains pending; no save, scoring, delivery or phone claim.

## 2026-10-14T21:51:03Z — TEST173 verified; TEST174 direct-wand candidate
- TEST173 exact source `e101e85167ee802cef3a0db31cb8497b83a23328`; run `38641672668` ran 2026-10-14T21:37:27Z–21:50:04Z and concluded success.
- Core artifact `11931431183` / ZIP SHA-256 `7090fcda3c677e0373b08a421634229e727e252110073eb3ab4510aa3eaf402b`: 1,593 tests, 0 failures/errors/skips. APK artifact `11931396427` / ZIP SHA-256 `897d768650d788653c5df751e15c26ec0ddd3b0098ec1b32de249ddfe43c3bfc`: APK SHA-256 `2f9379b9a38a28996d32bc745af94ba72df6ba0ce63e789bd348dd7d59c95d30`; bundled checksum matched.
- Android artifact `11931062722` / ZIP SHA-256 `f22708aa7c1febcaa022859c56c45e4409619a5554f8e5d3118947d7151d6271`: 26 API34 tests, 0 failures/errors/skips; 34/34 PNGs decoded 320×640 and passed contact-sheet review. Private delivery skipped; no phone claim.
- Adjacent source audit confirmed seven separate direct-damage calls in Blast Wave, Living Earth and Prismatic Light. Their delayed collisions, guardian attacks and non-damaging staff effects were already distinct and remain unchanged.
- TEST174 routes those seven rolls through one shared actual-creator boundary while retaining original rolls, proc chances, target filters, wand sources, direct mitigation, visuals, turns and RNG.
- Added three retained tests for all permitted factors and player/enemy/boss creators, contradictory settings, single source-preserving hits, neutral amount/next RNG and exact production wiring.
- TEST174 exact-source verification requests 1,596 core tests, debug assembly, complete history against immediate previous compiled TEST173, 26 API34 tests and 34 screenshots. Candidate remains pending; no save/scoring/delivery/phone claim.

## 2026-10-14T21:54:00Z — TEST174 first run failed; test-fixture correction
- Source `6cec278930f087ed6c18b5d04c136a046ff22669`, run `38643522614` (21:52:10Z–21:53:28Z) passed history validation, then failed `:core:compileTestJava`: retained `ProbeWand` lacked Wand's required abstract `onHit` override.
- Zero tests ran; no APK/API34/screenshots/artifact. A duplicate same-source run was already active and cannot verify this known failing source.
- Correction adds only a no-op fixture override; production changes and assertions are unchanged. Corrected TEST174 full verification remains pending; TEST173 remains latest verified and TEST123 remains owner-delivered fallback.

## 2026-10-14T22:06:30Z — TEST174 verified remaining direct-wand damage provenance
- Corrected exact source `5913850f400dbfe9fd0b547abbae71c20cd9992c`, run `38643884380` (21:55:06Z–22:05:52Z), concluded success.
- Core artifact `11931736651` / ZIP SHA-256 `ab58ce2a031c47f2bb5b3f745a0eedcf7a9399542063b3965191d9e158c1bf27`: 1,596 tests, 0 failures/errors/skips. APK artifact `11931766535` / ZIP SHA-256 `4801b69528ddb80f57603ea4b1dd191bfa9e2f0b80d3acfcbf143316ed16416d`: APK SHA-256 `6cbce087ebdacfbb4a621d1689f72b86b2cad71274efe6f22a8a4c98b78feb29`; checksum/debug/history passed.
- Android artifact `11931807905` / ZIP SHA-256 `8431b9c85cc88a39957ce5b7aeefb035dbf3f58d29f8370f0792d6b895c45e9f`: 26 API34 tests passed; 34/34 PNGs decoded 320×640 and passed contact-sheet review.
- Verified seven Blast Wave/Living Earth/Prismatic Light damage calls use the actual creator factor exactly once; neutral amount/source/RNG identity passed. Duplicate old-source run `38643546560` only repeated the fixture compile failure and is not verification.
- Private delivery skipped; no physical-phone claim. TEST123 remains owner-delivered fallback. Do not rebuild unchanged TEST174; continue a distinct approved audit task.

## 2026-10-14T22:08:00Z — TEST177 weapon spell-bonus candidate
- After verified TEST174, audited weapon enchantments and hero abilities. Crystal/Shocking-derived damage already inherits the once-scaled post-mitigation base hit and remains unchanged.
- Confirmed Holy Weapon's flat bonus and Smite's independent roll bypassed the custom boundary. Routed only those two bonuses through the actual attacker once, retaining original rolls, multiplier, sources and trigger order.
- Added three retained tests for every factor/creator role, contradictory settings, single source-preserving damage, neutral RNG identity and exact production wiring. Expected total 1,599 core.
- Exact-source core/debug/immediate-previous TEST176 history/26 API34/34 PNG verification is pending. No delivery or phone claim; TEST123 remains owner-delivered fallback.

## 2026-10-14T22:24:00Z — TEST177 verified weapon-triggered spell bonuses
- Exact source `7b190b54c1aaed329e6e185c592ee686c02385ea`, run `38645784290` (22:10:39Z–22:23:35Z), concluded success.
- Core artifact `11932816785` / ZIP SHA-256 `63fd2f7af4ad6b689b4e58a0149004d8372b9c516746a73f6217ffba3277086e`: 1,599 tests, 0 failures/errors/skips. APK artifact `11932966600` / ZIP SHA-256 `f7e358e1b52e5f0892fe53f98e0cbb747fce540b32b258a62966cbccf0ecd0a9`: APK SHA-256 `ea2099d787daeacca90d2b19d7e44263c571ed8517bc3d49229a530054a23e65`; checksum/debug/history passed.
- Android artifact `11932673053` / ZIP SHA-256 `6b7c38505ee99ba17bbc3c3ca5861a051d1d1ce6db1b1d6fe44ac313b2a45d7b`: 26 API34 tests passed; 34/34 PNGs decoded 320×640 and passed contact-sheet/focused review.
- Verified Holy Weapon and Smite independent bonuses use actual attacker scaling exactly once. Crystal/Shocking derived damage remains singly scaled through the base hit.
- Private delivery skipped; no physical-phone claim. TEST123 remains owner-delivered fallback. Do not rebuild unchanged TEST177.


## 2026-10-14T22:37:24Z — TEST178 tipped-dart bonus candidate
- Re-read verified TEST177 and audited a distinct offensive item/projectile category rather than rebuilding it unchanged.
- Confirmed Shocking Dart's electrical burst and Holy Dart's anti-undead/demonic bonus are independent post-hit rolls that bypassed creator scaling.
- Routed only those two bonuses through the actual attacker once, preserving their original ranges, sources, gates, visuals, mitigation and RNG.
- Added three retained tests for every permitted factor and creator role, contradictory settings, one source-preserving hit, neutral next-RNG identity and exact production wiring. Expected total: 1,602 core.
- Exact-source core/debug/immediate-previous TEST177 history/26 API34/34 PNG verification is pending. No delivery or physical-phone claim; TEST123 remains owner-delivered fallback.


## 2026-10-14T22:51:05Z — TEST178 verified tipped-dart bonus attribution
- Exact source `a6d757fcaecc69fb0bdb38a6dd207c3aea6ccf22`, run `38649035059` (22:38:37Z–22:51:05Z), concluded success.
- Core artifact `11933781595` / ZIP SHA-256 `d9ccd829d674db97d0926421ee71e3780170a1a2e33c11414883185f2d5866e3`: 1,602 tests, 0 failures/errors/skips; all three new methods passed.
- APK artifact `11933846357` / ZIP SHA-256 `5cbfffea672bdfb047e1ed4a3e222380ff022f731476a8f6acf566f5f1d5657d`: APK SHA-256 `082e2ad55ce382fe49b348d152145da1103cdee557a80c1ddb2a582aa5029e74`; checksum/debug/immediate-previous TEST177 history passed.
- Android artifact `11934865932` / ZIP SHA-256 `7a5b2ef852f5c10ba87870a661b4c4dc0076437e8b5512f2b77ee1edb5aed238`: 26 API34 tests passed; 34/34 PNGs decoded at 320×640 and passed contact-sheet/focused history review.
- Verified Shocking Dart electrical burst and Holy Dart anti-undead/demonic bonus use the actual attacker factor exactly once; neutral amount/source/next-RNG identity passed.
- Private delivery skipped; no physical-phone claim. TEST123 remains owner-delivered fallback. Do not rebuild unchanged TEST178; continue a distinct approved backlog task.


## 2026-10-14T23:39:51Z — TEST179 UIMSD Column order recreation candidate
- Re-read the documented Arrange-columns lifecycle issue after verified TEST178.
- Current production windows already provide exact restorers, but API34 coverage only recreated Columns selection and never the distinct Column order or Move column steps.
- Strengthened the retained device test to recreate both steps exactly once, invoke Make first through the restored callback, verify the selected second column becomes first, then restore the original query.
- No production query/window, save, history, scoring, identity or signing behavior changed. TEST179 requests 1,602 core/debug/immediate-previous TEST178 history/26 strengthened API34 methods/34 PNG. Candidate remains pending; no delivery or physical-phone claim.


## 2026-10-14T23:53:14Z — TEST179 evidence retained; TEST180 fixture correction
- Exact source `f6043c7d16cd52e57276113fb038af046ff4ec48`, run `38655567956` ran 2026-10-14T23:40:55Z–23:52:01Z.
- Passed: complete 1,602 core tests, debug APK assembly, immediate-previous TEST178 compiled-history validation and artifact packaging.
- Failed: API34 completed 26 methods with 25 pass / 1 fail at `column-order fixture has at least two columns`. Runs legitimately stores an empty explicit list for default columns; the new test misread it as no effective columns.
- Android evidence artifact `11937227267` / ZIP SHA-256 `5e80aad0730b6507fe0cfba1862fae175636d70362eeac2c8f25f5508718a3e8`; core artifact `11937375321`; APK artifact `11936159775`. TEST179 is not verified or delivered.
- TEST180 changes only the retained device fixture to resolve effective defaults before the two-step recreation test and to restore the original explicit/default semantics afterward. Production behavior is unchanged.
- TEST180 requests 1,602 core/debug/immediate-previous compiled TEST179 history/26 API34/34 PNG. Pending exact-source verification; no delivery or phone claim.

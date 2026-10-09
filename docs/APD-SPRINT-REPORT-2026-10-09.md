# APD development report — October 9, 2026

## Current outcome and scope

Codex development is continuing toward the owner's November 1, 2026 alpha target. Today the owner requests a bug-cause/risk PDF by 11:55 a.m. and a playable private APK by noon, New York time. This is an evidence checkpoint, not a claim that eight active hours were delivered or that all remaining bugs are fixed.

Delivered October 9 at **9:25:40 a.m. EDT**, ahead of both requested deadlines: [private CODEX TEST 5 release](https://github.com/charltonchris29/ShieldingShouldHaveWorked/releases/tag/apd-codex-test-5), with a directly attached APK and bug-causes/risks PDF. Source **70d0e1fa9e14f611b085b01f27072ffbfe8ad504**. Complete core: **894 tests across 143 suites, zero failures/errors/skips**. Android debug assembly and **all 3 targeted API 34 checks passed**, zero failures/errors/skips. No physical-phone test is claimed.

APK: ADHD-Pixel-Dungeon-CODEX-TEST-5.apk (38,225,942 bytes). SHA-256: 771c4a41222e75ba2b5ab77b57e06aec589e8eef986111e8b1d389f5a9af69f2. [Download APK](https://github.com/charltonchris29/ShieldingShouldHaveWorked/releases/download/apd-codex-test-5/ADHD-Pixel-Dungeon-CODEX-TEST-5.apk). [Download PDF](https://github.com/charltonchris29/ShieldingShouldHaveWorked/releases/download/apd-codex-test-5/APD-Bug-Fixes-2026-10-09.pdf). The local transfer tool cannot download the approximately 35 MB artifact ZIP because its limit is 32 MiB; no local APK path is claimed. The exact usable APK location is the private release asset. Local regenerated evidence-bearing PDF: /workspace/artifacts/APD-Bug-Fixes-2026-10-09.pdf (7 pages, required text checked; checkpoint first/last pages visually inspected).

The first hosted verification/debug build SUCCEEDED: [run 37928599930](https://github.com/charltonchris29/ShieldingShouldHaveWorked/actions/runs/37928599930), source ec7cc1c7aea25f61d9b4515c6b1b6bca26bbe4cd. Actual XML: **887 tests across 142 suites, zero failures/errors/skips**. Android debug assembly succeeded. Artifact apd-codex-private-test-apk contains ADHD-Pixel-Dungeon-CODEX-TEST-1.apk plus its SHA-256 file. This APK predates acd1878 and 2764791; the new delivery build must include them and be independently verified. No physical-phone test has occurred.

Repository source baseline: Claude HEAD 714badbd313c66c41ec4d9fb50429b353a815840. Branch: codex/apd-phone-fixes-2026-10-09. The owner's installed b73 test APK was not inspected; the repository's PRIVATE TEST 3 pointer 46229e1d7 is not proof of its source. Main and Claude's branch have not been overwritten or merged into.

The owner explicitly approved all sprint development actions and asked to stop routine approval prompts. GitHub's per-app permission setting was changed to Allow all actions and verified, as requested. This supersedes older build-freeze/ask-every-time instructions for the current authorized private sprint. It does not authorize unrelated product changes. No public distribution, normal-release publication, backend enablement, signing-key change or main merge is part of this work. Private prerelease delivery is requested by the APK assignment.

## Reviewable commits on GitHub

1. 65ddac6f0cc04dd600168ed5b8283aa91951314d — wrap/scroll run-detail actions.
2. 8ff3bed4aa01209d3ed88b1c5dc4df758f5023dc — guard queue root/IDs, add preservation sequence.
3. a7e595bfce89ad6ad6a8db46e83977152b0ef8ce — persist per-view column layouts.
4. 60ac762adf83a7eabd2465a4872b805364b017f5 — explain protected redacted archive differences.
5. a14df7d5fbecdc47570fcdc3e4865532c73a4380 — fail-closed age eligibility and title fallback.
6. 1fc1cc13b815aaffbd819bceb58bd0b236d2cf69 — testable progression; reject synthetic roots.
7. 897bd9f7f1d0a7101f4225388eb68f4580c90e84 — enforce finished-only Bosses scopes.
8. df4e7f5f67059d5d5871dd98afe3beb3435f1bed — retry feature loading after early startup; preferences persistence tests.
9. e33e94548c7744779ccb6dfa48433376d1d6c671 — responsive confirmations/demo choices; narrow-window margins and 22-unit action minimum.
10. 8716b7d7426314a41e485b6b5f3f9197a2077f60 — reject impossible dates/non-finite or overflowing filter numbers.
11. c9cf1f4c50bd220513a49f64e5ce1678a741d0f3 — fresh-process queue test, real combined query fixture, isolated test preferences.
12. ec7cc1c7aea25f61d9b4515c6b1b6bca26bbe4cd — owner-approved private verification/debug build workflow.
13. acd1878a0e700c1077185df054e04d98ac5ec7c3 — exact loopback HTTP host/valid-address checks.
14. 2764791defd4859c1b1de30cfb5ce53de2daa116 — parameterized UIMSD menu restoration plus Android regression test.
15. a4478f415e357411556445b0d705aaa1d235cc43 — measured-report workflow and API 34 verification/delivery gate.
16. a938d313c7139bc3a5a16c521b2611876d1bb577 — safe saved-view replacement; directory/failure preservation tests.
17. 211cfeae70c990307f3227a3758c1e3ac7ec48a2 — restore typed values/pending selections/Ask/report/Guide windows; correct Android test import.
18. 47bc7a4007437e1f9115c164e46a8bbe70261cd8 — C3 verification request and checkpoint evidence.
19. cd5aadef2623b7abf848bc0703e806393d115b0e — preserve newer queue replacements; await the Settings-close lifecycle in instrumentation.
20. 70d0e1fa9e14f611b085b01f27072ffbfe8ad504 — check both action labels, skip non-action slots and cancel superseded branch checks.

Remote commits preserve real ancestry and untouched binary assets. This workspace's local git history is a synthetic text snapshot, not a clone; **DO NOT PUSH LOCAL GIT HISTORY**. Commits were published through the authorized Git-data API using the real base tree, real parent and expected-head lease. A fresh clone of the remote branch is the reproducible source checkout.

## Package 1 — mobile layout

Cause: WndUimsdRow used fixed-height single-line buttons outside the scroll area. Actions now wrap at font 9, size after wrapping and scroll with details. Minimum action height is 22. WindowGeometry now respects narrow available width. WndUimsdChoices uses the existing scroll/list click handling; explanatory text is inside its pane. UIMSD confirmations, report confirmations and WndDemoUnlock use it. Generic game WndOptions is not globally changed.

Geometry, row/choice wiring and font/height checks pass. Android rendering regression covers a 100-unit viewport, including responsive APD input button labels. No physical-phone visual or touch acceptance is claimed. Risks: more scrolling, changed action-row styling, generic non-UIMSD WndOptions may still need a separate audit.

## Package 2 — local queue

Null roots cannot inspect a working-directory queue, and IDs use shared validation. Deterministic enqueue deduplicates; tests check add/inspect/remove/repeat operations, unchanged duplicate bytes, unchanged original log/statistics sentinel and absence of a submitted ledger. The separate-process test inspects/removes persisted data in a fresh JVM. These operations do not call the gateway. Full CI also executes the HTTP gateway tests against a local test server; that is test-only transmission, not APD uploading history.

First CI SubmissionQueueTest: 12 tests pass. A later thirteenth method reproduces the same-ID replacement race and covers nine accepted/rejected/retry combinations; it passes locally after matching the exact queued version. Android restart/tap checks remain for the phone. Removal never deletes the underlying log in tested cases. No server/community functionality was enabled.

## Package 3 — saved views and statistics

Column preferences previously lived only in the current query and disappeared on navigation. column_layouts.txt stores only per-view columns/order; named saved queries remain separate. Explicit reset persists defaults and save failures show a notice. A controlled directory-destination test exposed the destructive delete-first store fallback: SavedViews now rejects directories and preserves the old destination when replacement fails. The pure-library Android compatibility check caught java.nio.file in an initial implementation; that approach was replaced with the supported existing File API, and the check is unchanged.

Tests cover navigation/recreated models, combined filtering/sorting/column arrangement, real Run type/Modded identifiers, ascending/descending sorting, stable equal values, unknowns last, zero matches and saved-view create/open/delete reload. The existing query performance check compares 24,174 deterministic queries without disagreement.

Bosses summaries and individual boss queries now carry a finished-only boundary. Scope mutations and codec round trips cannot enable unfinished rows for that view; unrelated scopes retain their existing behavior. Physical display/interaction and real Android force-stop restoration remain separate acceptance checks.

## Package 4 — archive import/export

A controlled unfinished-run fixture proves that hidden-information redaction changes exported bytes. The original-protection behavior is intentional. Report.redactedDifferentExisting identifies protected differing redacted copies and explains why differences can be expected; it does not claim redaction is the only cause.

Three repeated self-imports each produce 0 imported, 5 identical skipped and 1 protected differing copy, with original raw logs/imported records and secrets preserved. First CI ArchiveRoundTripTest: 15 pass; hostile-input coverage also passes. The owner's exact 5-identical/3-different archive was not supplied and its individual conflicts remain unproven.

## Package 5 — age/content

No verified age integration existed. AgeEligibility introduces explicit APD policy states. SPDSettings requires eligibility plus preference; true writes are blocked when unverified, while an existing preference is preserved but ineffective. Current provider is UNAVAILABLE, so Adult remains off. APDSettingsPane explains that limitation.

The early drugs arrow came from TitleScene's hardcoded fallback. It is removed; an absent progression prompt hides the annotation/strip. A permitted tenth-run prompt uses the eligibility-aware humor getter, and retirement remains permanent.

First CI AgeEligibilityTest: 5 pass; real preferences XML tests pass. Actual Play API/Console access, provider integration and Play-installed-device checks remain unfinished. Supported beta SDK 0.0.4 and its two-stage API were researched from official Android documentation; see APD-AGE-VERIFICATION-STATUS.md. Do not mistake SHARED for verified adulthood or null bounds for an adult signal.

## Package 6 — progression

Extracted the existing policy into UnlockProgression for deterministic disk fixtures. Synthetic catalogs and nonpositive caps count zero. Tests cover ninth/tenth real ends, unfinished/imported/demo exclusions, demo-versus-tour separation, prompt retirement, persistent flags, remembered unlocks with missing history and normal builds not enforcing private gates.

ApdFeatures' loaded flag was set before checking Gdx.files. Controlled reproduction failed before the fix; loading now retries after file service readiness, while missing/unreadable assets keep normal features off. Preferences tests reopen actual XML and verify unlock/tour/prompt/demo flags.

Intentional shortcut remains: I LIKE CONFUSION permanently opens UIMSD before ten genuine runs; it does not unlock the tour. Demo deletion does not relock UIMSD or unlock the tour. Existing players' flags are not reset.

Unresolved product ambiguity: recorded abandonment has a RUN_END, so it currently counts as finished. Older docs call it unfinished. A characterization test establishes the current behavior; the owner must settle that rule before a behavior-changing fix. The app showing one scoped row does not reveal all stored flags or all-rules history.

## Package 7 — regression, quests and build

Initial source-compilation checkpoint: 1,543 production Java files and 157 core test Java files compiled against actual upstream dependencies. This was JDK 21 with source/target 11, not Android API verification. First CI provides the real Gradle/native/assets/Android SDK build and full suite described above.

Local normal-JVM subset: latest queue checkpoint 528 pass in 84.138 seconds; earlier 527 pass in 17.225 seconds after the leaf/storage fixes; 521 pass in 19.798 seconds before address/restoration changes; 524 pass in 20.676 seconds afterward. These are subsets, not additional distinct totals on top of CI's 887. Earlier supplemental 53 headless checks used an external no-native loader; QuestRecording was blocked there and is not counted as a standard pass. Full CI subsequently passes actual QuestRecordingTest (1) and GhostTransparencyTest (2) with normal dependencies. Source paths in Sad Ghost/Wandmaker/Blacksmith/Imp were reviewed without gameplay modifications. All four quests have not been manually played on Android.

Additional confirmed fixes: impossible dates and NaN/infinity/oversized decimal filters now reject invalid input; address validation rejects remote hosts disguised as loopback. Both had failing-before/passing-after tests. The external test harness's preferences path was also corrected to use its real temporary directory; no production player settings were touched.

Menu restoration now supports semantic reopeners instead of failing no-arg construction for parameterized UIMSD windows. Default no-arg windows retain the fallback. Restoration now also covers typed APD inputs (including their text and callback), pending filter and split selections, Ask menus, report dialogs, Guide topics and the demo warning. Some less common confirmations remain unsupported. Restored scroll/text cursor position may reset; Android process-death restoration of pending dialogs and unsaved filter/sort persistence are not claimed. UimsdRestoreDeviceTest exercises Options/Columns, nested filters/Back, Settings fallback and real narrow font layout; The second hosted run, 37931869191, passed complete core tests and APK assembly but the instrumentation sources failed compilation: ApdSettingsClickDeviceTest lacked assertFalse imports after the age fix. No Android test ran and private delivery was correctly skipped. The import is corrected in 211cfeae; a new full verification is required. No assertion was removed.

The workflow only runs on this branch when the workflow/build-request file changes, avoiding paid runs on normal code/doc commits. Main/Claude are not triggers. The delivery verification adds one API 34 emulator check for UIMSD restoration and existing PlayRunDeviceTest save/continue/death/win behavior. The private prerelease will identify source/hash and attach the APK directly. Publication is conditional on its required checks passing.

## Files changed / review map

All production paths are under core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/:
- windows/WndUimsdRow.java, WndUimsdList.java, WndUimsdChoices.java, WndDemoUnlock.java, WndUimsdOptions.java, WndUimsdReport.java, WndUimsdColumns.java, WndUimsdFilter.java, WndUimsdSort.java, WndUimsdStatus.java, WndUimsdGuide.java, WndUimsdAsk.java, WndUimsdText.java, WndUimsdTopic.java, WndUimsdTextInput.java and WndTextInput.java (default generic behavior preserved; APD width hooks only).
- apd/query/WindowGeometry.java, UimsdModel.java, Scope.java, QueryCodec.java, Datasets.java, StatusText.java, CellFormatter.java, SavedViews.java.
- apd/backendbridge/SubmissionQueue.java; apd/backend/Gateways.java; apd/archive/ArchiveImporter.java.
- apd/age/AgeEligibility.java; apd/demo/UnlockProgression.java; apd/query/UimsdUnlock.java; apd/ApdFeatures.java; apd/TitleGeometry.java; SPDSettings.java; scenes/TitleScene.java, PixelScene.java, UimsdScene.java; ui/Window.java.

Tests under core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/:
- apd/query/WindowGeometryTest, UimsdModelTest, SavedViewsTest, BossesFinishedScopeTest, CellFormatterTest; apd/backendbridge/SubmissionQueueTest; apd/archive/ArchiveRoundTripTest; apd/age/AgeEligibilityTest; apd/demo/UnlockProgressionTest; apd/ApdFeaturesStartupTest; apd/HeadlessSettingsStorageTest; apd/HeadlessTestSupport.
- apd/windows/UimsdSettingsPersistenceTest, UimsdChoicesTest; existing apd/UiEntryTest, UimsdWiringTest and windows/UimsdGateTest updated only for the authorized title/content behavior.
- android/src/androidTest/.../ApdSettingsClickDeviceTest updated for fail-closed humor; UimsdRestoreDeviceTest added.

Workflow: .github/workflows/codex-sprint-check.yml. Build request: docs/APD-CODEX-BUILD-REQUEST.txt. Reports: APD-BUG-FIX-REPORT-2026-10-09.md/PDF, this report, bug log, test evidence, age status, HANDOFF.md, and tools/apd_bug_report_pdf.py.

## Next phone acceptance and risks

See the numbered physical-phone checklist in APD-BUG-FIX-REPORT-2026-10-09.md. Install over the existing app with unchanged signing key/app ID. Do not uninstall or clear data. Most important: long action text/touch/drag, queue sequence after restart, column/saved-view persistence, Bosses boundary, protected archive reimport, Clean/title progression, demo/tour separation, app-switch menus and a real save/continue/end.

No speculative balance changes, monetization, hidden achievement, Eternal Grudge or release-restriction changes were made. Grudge/Lore remains postponed/off. Unknown bugs may remain. API 34 emulator results do not certify a RedMagic phone or all Android versions.

## Time and interruption accounting

Session started October 8 at 8:33:26 p.m. EDT (October 9 00:33:26 UTC). Work is still continuing; there is no final end time in this checkpoint. The initial 10:23:46 UTC checkpoint was 9 h 50 m 20 s elapsed, not eight hours of active work. A premature closeout interpretation was corrected and development continued.

Recorded permission wait: 1,105.6 seconds (18 m 25.6 s), aborted. A later network request waited about 20 seconds without a recorded approval result. The GitHub permission change had a 31-second initial yield; total follow-up wait was not precisely measured.

Intervals with no verified productive record:
- 00:55:59–03:00:08 UTC: 2 h 4 m 9 s.
- file timestamp 03:05:51–04:51:03 UTC: approximately 1 h 45 m 12 s; publication/workspace interruption.
- 04:51:07–local commit 10:16:50 UTC: approximately 5 h 25 m 43 s; delayed execution, exact cause unavailable.
- file timestamp 11:00:35–clock 11:55:40 UTC: approximately 55 m 5 s; no verified work in this gap, exact turn/app/platform cause unavailable.

Do not call these intervals active development or invent a wait-versus-idle split. The first three total roughly 9 h 15 m. The 10:29:50–11:00:35 UTC continuation has visible editing/testing/commits, about 30 m 45 s elapsed; category timings were not stopwatched. Further active work is visible from 11:55:40 UTC onward, with interruptions from user messages. Earlier 15–30 minutes active was an estimate, not a measurement. Exact productive, coding, research, idle and tool-wait totals cannot be established.

Hosted first build: job 12:13:16–12:16:31 UTC (8:13:16–8:16:31 a.m. EDT), 3 m 15 s elapsed. Test step 1 m 46 s; Android assembly step 50 s. Independent inspection continued while it ran. Local broad checks actually took around 20 seconds, not the multi-hour gaps. Updated delivery-build/emulator durations and session end must be recorded from execution evidence when available.

## Further queue integrity and verification — C3 checkpoint

A controlled fake-gateway reproduction confirmed the previously suspected same-ID replacement race. Flush matched by ID and deleted a newer replacement after the old item was accepted; retry outcomes could also overwrite the new item's schedule. Matching the complete immutable queued version preserves replacements. Nine replacement/outcome combinations are covered without network traffic. Local subset: 528 checks pass (84.138 s on the extended run); this is not an Android check or 528 additional distinct tests beyond CI.

C3 run 37933866530: complete core XML reports 893 tests, zero failures/errors/skips. Debug APK assembly succeeded. API 34 ran three tests: the existing two play/save/continue/end checks passed, and the UIMSD restoration check failed. Root cause is the test fixture: WndSettings.hide schedules a scene reset; the fixture opened Options immediately, before that reset executed. The fixture now awaits that actual lifecycle event before opening Options. Assertions and production behavior are unchanged. Delivery was skipped because device verification failed. A fresh run is required for the corrected fixture and queue fix.

Native fixture audit before delivery also identified a null action-label slot for explanatory notes; the test now ignores non-actions and explicitly asserts that both long action labels were checked. No boundary assertion was removed. Workflow concurrency now cancels superseded checks on this branch, conserving Actions minutes while keeping the final APK and evidence tied to the same source commit.

## Hosted execution timing evidence (UTC; subtract four hours for New York EDT)

| Run | Core/build job | API 34 job | Result |
|---|---|---|---|
| C1 37928599930 | 12:13:16–12:16:31 (3m15s) | Not scheduled | Core/APK pass |
| C2 37931869191 | 12:43:05–12:46:07 (3m02s) | 12:46:09–12:49:40 (3m31s) | Instrumentation compile blocked by missing import |
| C3 37933866530 | 13:00:53–13:03:36 (2m43s) | 13:03:38–13:09:57 (6m19s) | Core/APK pass; 2 device passes, 1 fixture failure |
| C4 37935449028 | 13:14:37–13:16:44 (2m07s) | No device execution | Superseded/cancelled to conserve minutes |
| C5 37935683756 | 13:16:51–13:19:38 (2m47s) | 13:19:41–13:25:20 (5m39s) | 894 core + 3 API 34 pass; private delivery 13:25:23–13:25:45 (22s) |

These are actual hosted job durations, not measurements of developer idle time, billing charges or active coding time. Work on queue regression and report verification overlapped hosted execution. C2 core-test step took 1m42s and APK step 48s; C3 core-test step took 1m33s and APK step 38s. C4 was cancelled during the core-test step, so it is not a complete-suite pass.

## Final delivery verification and remaining work

Actual C5 XML was downloaded and independently counted: 894 core tests, 143 suites, no failures/errors/skips; SubmissionQueueTest has 13 passing methods. Actual Android XML has three passing methods: UIMSD restoration/narrow labels (4.734s), play/save/continue/end history (67.528s), and win/seal history (66.328s). The UIMSD check covers the Settings fallback, Options/Columns, nested filter Back, Ask, typed text/callback, pending enum selection and actual narrow action/input label bounds. It does not simulate physical touch or Android process death. Both earlier test-fixture issues are resolved in this verification; no assertion was removed. No newly confirmed production regression remains from these changes, but untested device behavior remains a risk.

The delivered APK uses the existing committed apd-debug.jks configuration and existing .indev application ID. Neither file/configuration was changed. The installed owner's APK was not available for certificate comparison, and the actual upgrade of phone saves was not performed; preserve the installation/data if Android rejects an update.

Outstanding items needing input or physical testing: genuine Play age-provider access/integration; whether recorded abandonment should count toward the ten-run rule; the original eight-file archive; remaining less common confirmation/leaf and process-death restoration; RedMagic visual/touch/navigation/restart checks; all four complete NPC quest playthroughs. Adult humor stays safely off until an eligible age source is integrated. No balance, Cleric restrictions, community/backend, monetization or postponed achievement/Grudge work was enabled.

Delivery-accounting cutoff: October 9 **9:27:41 a.m. EDT / 13:27:41 UTC**. Start-to-cutoff elapsed: **12h 54m 15s**. This is the APK/PDF delivery checkpoint, not eight measured active hours or proof of alpha readiness. The four unverified execution gaps total approximately **10h10m09s**; the permission wait and gaps must not be added together as independent downtime because overlap cannot be excluded. Visible recent development windows are roughly 30m45s (10:29:50–11:00:35 UTC) and 1h32m01s (11:55:40–13:27:41 UTC), with user/tool interruptions inside them; these are window durations, not a stopwatch total of active coding. Earlier active work was estimated at 15–30 minutes, not measured. Exact active time, coding/debugging/research category totals, idle time and GitHub/dependency wait totals cannot be recovered. Short explicit verification polling used four 55-second delays (about 3m40s total), with some report work overlapping yielded waits; this is not zero downtime. Hosted build durations above are measured separately and overlap development. Eight uninterrupted active hours were not delivered or claimed.

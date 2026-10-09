# APD development report — October 9, 2026

## Current outcome and scope

Codex development is continuing toward the owner's November 1, 2026 alpha target. Today the owner requests a bug-cause/risk PDF by 11:55 a.m. and a playable private APK by noon, New York time. This is an evidence checkpoint, not a claim that eight active hours were delivered or that all remaining bugs are fixed.

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

Remote commits preserve real ancestry and untouched binary assets. This workspace's local git history is a synthetic text snapshot, not a clone; **DO NOT PUSH LOCAL GIT HISTORY**. Commits were published through the authorized Git-data API using the real base tree, real parent and expected-head lease. A fresh clone of the remote branch is the reproducible source checkout.

## Package 1 — mobile layout

Cause: WndUimsdRow used fixed-height single-line buttons outside the scroll area. Actions now wrap at font 9, size after wrapping and scroll with details. Minimum action height is 22. WindowGeometry now respects narrow available width. WndUimsdChoices uses the existing scroll/list click handling; explanatory text is inside its pane. UIMSD confirmations, report confirmations and WndDemoUnlock use it. Generic game WndOptions is not globally changed.

Geometry, row/choice wiring and font/height checks pass. Android rendering regression covers a 100-unit viewport. No physical-phone visual or touch acceptance is claimed. Risks: more scrolling, changed action-row styling, generic non-UIMSD WndOptions may still need a separate audit.

## Package 2 — local queue

Null roots cannot inspect a working-directory queue, and IDs use shared validation. Deterministic enqueue deduplicates; tests check add/inspect/remove/repeat operations, unchanged duplicate bytes, unchanged original log/statistics sentinel and absence of a submitted ledger. The separate-process test inspects/removes persisted data in a fresh JVM. These operations do not call the gateway. Full CI also executes the HTTP gateway tests against a local test server; that is test-only transmission, not APD uploading history.

First CI SubmissionQueueTest: 12 tests pass. Android restart/tap checks remain for the phone. Removal never deletes the underlying log in tested cases. No server/community functionality was enabled.

## Package 3 — saved views and statistics

Column preferences previously lived only in the current query and disappeared on navigation. column_layouts.txt stores only per-view columns/order; named saved queries remain separate. Explicit reset persists defaults and save failures show a notice.

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

Source-compilation checkpoint: 1,543 production Java files and 157 core test Java files compiled against actual upstream dependencies. This was JDK 21 with source/target 11, not Android API verification. First CI provides the real Gradle/native/assets/Android SDK build and full suite described above.

Local normal-JVM subset: 521 pass in 19.798 seconds before address/restoration changes; 524 pass in 20.676 seconds afterward. These are subsets, not additional distinct totals on top of CI's 887. Earlier supplemental 53 headless checks used an external no-native loader; QuestRecording was blocked there and is not counted as a standard pass. Full CI subsequently passes actual QuestRecordingTest (1) and GhostTransparencyTest (2) with normal dependencies. Source paths in Sad Ghost/Wandmaker/Blacksmith/Imp were reviewed without gameplay modifications. All four quests have not been manually played on Android.

Additional confirmed fixes: impossible dates and NaN/infinity/oversized decimal filters now reject invalid input; address validation rejects remote hosts disguised as loopback. Both had failing-before/passing-after tests. The external test harness's preferences path was also corrected to use its real temporary directory; no production player settings were touched.

Menu restoration now supports semantic reopeners instead of failing no-arg construction for parameterized UIMSD windows. Default no-arg windows retain the fallback. Root/menu coverage is substantial but leaf inputs, pending multi-selects and Ask remain incomplete. Restored scroll position may reset. UimsdRestoreDeviceTest exercises Options/Columns, nested filters/Back, Settings fallback and real narrow font layout; new hosted Android results are pending at this checkpoint.

The workflow only runs on this branch when the workflow/build-request file changes, avoiding paid runs on normal code/doc commits. Main/Claude are not triggers. The delivery verification adds one API 34 emulator check for UIMSD restoration and existing PlayRunDeviceTest save/continue/death/win behavior. The private prerelease will identify source/hash and attach the APK directly. Publication is conditional on its required checks passing.

## Files changed / review map

All production paths are under core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/:
- windows/WndUimsdRow.java, WndUimsdList.java, WndUimsdChoices.java, WndDemoUnlock.java, WndUimsdOptions.java, WndUimsdReport.java, WndUimsdColumns.java, WndUimsdFilter.java, WndUimsdSort.java, WndUimsdStatus.java, WndUimsdGuide.java.
- apd/query/WindowGeometry.java, UimsdModel.java, Scope.java, QueryCodec.java, Datasets.java, StatusText.java, CellFormatter.java.
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

# APD bug fixes and risks — October 9, 2026

## What this report means

Target: a private, playable test APK by noon today, New York time, and this PDF by 11:55 a.m. New York time. November 1, 2026 is the playable alpha test deadline, not a feature-complete public launch; public release targets January–February 2027, subject to stability/testing. These are delivery targets, not evidence that every part of the game works.

The first Codex APK built successfully at 8:16 a.m. New York time. Its complete core test suite passed: 887 tests, no failures, errors or skipped tests. Later changes also need verification in a newer build. The final PDF's build-verification page identifies its exact source commit and available test evidence.

Automated checks are different from testing your RedMagic phone. No physical-phone test has been performed by Codex. No saved game or statistics from your phone were accessed, deleted or reset. The installed b73 test APK was not inspected, so its exact source cannot be proved from its name alone.

## 1. Run-detail buttons overflow the dialog

Problem: “Queue for submission (preview, sends nothing)” and other actions could extend outside their dialog.

Cause: each action was a single-line button with a fixed height. The actions sat below the scrolling details, so long labels and many buttons had nowhere to go.

Fix: labels wrap at the available width, grow vertically and scroll with the details. Their font remains readable. Action rows have padding and a minimum height of 22 game units. Extremely narrow windows now respect the space available on the screen rather than forcing a 90-unit minimum.

Possible effects: dialogs can be taller and require more scrolling. Action rows now use UIMSD's row appearance. A confirmation's explanatory text can scroll too. Touch targets remain large; dragging should not activate an action. Check every long action on your phone in portrait and landscape.

Verification: layout arithmetic and source-wiring tests pass. A new Android rendering check exercises long labels in a narrow window; its actual result is listed on the final verification page. This does not certify every screen size or the RedMagic's touch behavior.

Locations: WndUimsdRow, WndUimsdList, WndUimsdChoices, WndDemoUnlock, WindowGeometry, WndUimsdOptions, WndUimsdReport.

## 2. Local submission queue safety and persistence

Problem: adding, inspecting and removing a queued run needed stronger verification. A missing history folder could accidentally read a queue relative to the current directory.

Cause: constructing a file with a null parent can resolve it relative to the working directory. Queue IDs also lacked the common run-ID validation.

Fix: a missing history root returns an empty queue; invalid run IDs are rejected. Added tests for add, duplicate add, inspect, remove and duplicate remove. A separate fresh Java process inspects and removes the saved entry to verify persistence beyond the original process.

Possible effects: previously invalid IDs cannot be queued. A missing-root query cannot report a stray queue. Valid run histories and normal queue behavior are preserved. Removing a queue entry only removes the local submission copy.

Verification: queue tests preserve the original log bytes and a statistics sentinel, leave no submitted ledger, keep exactly one entry after duplicate adds and reload the outbox from disk. Local queue operations do not call a network gateway. Android process restart still needs a phone check.

Locations: SubmissionQueue and SubmissionQueueTest.

## 3. Column arrangement disappeared

Problem: selected columns and their order were lost when changing views, returning to UIMSD or rebuilding its model.

Cause: navigation replaced the current query with the view's defaults. Column choices were held only in memory.

Fix: selected columns and their order are stored separately for each view in column_layouts.txt. Navigation and model reload restore them. Reset explicitly restores and saves the default layout. Failed saves show a notice.

Possible effects: older layout choices now persist instead of resetting. This introduces a small new preferences file under the history folder; it does not change run logs. Resetting a view intentionally resets its column choices. A named saved query still stores the complete query separately.

Verification: navigation, recreated-model reload, reset persistence and named saved-view create/open/delete tests pass. Deterministic sample data verifies filtering, both sort directions, equal scores, unknown values, no matches and the real Run type/Modded columns together. Actual phone navigation and force-stop/reopen remain to be checked.

Locations: UimsdModel, SavedViewsTest and UimsdModelTest.

## 4. Bosses could include unfinished runs

Problem: the Bosses view defaulted to finished runs but generic scope controls could turn unfinished runs back on.

Cause: its default was an ordinary editable scope rather than an enforced finished-only scope.

Fix: Bosses summary and individual boss views have a finished-only boundary. Changing other filters or rules cannot remove it. The unsuitable unfinished-runs toggle becomes an explanation. Saved queries preserve the boundary; old query formats still decode.

Possible effects: current-run boss encounters will not appear in Bosses until that run ends. Other views keep their existing unfinished-run options. Saved Bosses queries no longer bypass the finished-only rule.

Verification: tests cover summary and individual scopes, query serialization, model changes and unchanged behavior of unrelated views. Check the resulting explanation and table on the phone.

Locations: Scope, QueryCodec, Datasets, UimsdModel, StatusText and WndUimsdOptions.

## 5. Reimporting an export reported different logs

Problem: your export/import reported five identical files and three differing files, with no clear explanation.

Cause established in controlled tests: an unfinished run's export deliberately removes hidden information. That exported copy differs from the complete original stored locally. A run progressing after export could also produce differences. Your exact archive was not available, so the particular three differences are not proven to have that cause.

Fix: retain the existing protection against overwriting history and add a specific explanation when differing archived copies were redacted. Repeated self-import tests verify that the original records remain intact.

Possible effects: the result includes a clearer explanation and count. Protected differences can still be reported; that does not mean a lost record or failed export. Unfinished-run hidden information remains protected.

Verification: a fixture is reimported three times, producing zero imported, five identical skipped and one differing protected copy each time; original raw records and their local secrets are unchanged. Archive round-trip and hostile-input tests pass. Your original eight-file archive still needs its own check.

Locations: ArchiveImporter and ArchiveRoundTripTest.

## 6. Adult humor was only a checkbox

Problem: a stored Adult preference was treated as permission to show adult humor, without verified age eligibility.

Cause: no implemented age-verification provider existed; the getter returned the checkbox value directly.

Fix: separate age eligibility from humor preference. Eligible, ineligible, unknown and unavailable states have explicit policy tests. Only eligible plus an Adult preference permits adult humor. The current provider is unavailable, so this build uses Clean. A previously saved preference is retained but cannot take effect while eligibility is unavailable. The settings explanation says why.

Possible effects: Adult humor cannot currently be enabled. This is intentional safety behavior, not completed age verification. A previous Adult preference is not proof of adulthood. The drugs title prompt will not appear in this build while no eligible age source is integrated.

Verification: policy and real preferences-file reload tests pass. There is no Google Play age integration in APD yet. Supported APIs were researched; access/setup and real integration remain necessary. Missing information never implies adulthood.

Locations: AgeEligibility, SPDSettings, APDSettingsPane and age/preferences tests.

## 7. The drugs title arrow appeared too early

Problem: “HEY, GO HERE FOR DRUGS” could appear before the intended tenth-run prompt or after that prompt had retired.

Cause: TitleScene used that text as a fallback whenever the progression gate was disabled or no active prompt supplied text.

Fix: remove the inappropriate fallback. When no progression prompt is active, hide the annotation and its reserved strip. Locked private-test progression retains its locked text. The tenth-run prompt selects Clean/Adult through the eligibility policy; after opening UIMSD it retires permanently.

Possible effects: the old fallback arrow disappears instead of returning after retirement. The title layout reclaims its reserved space. While age eligibility is unavailable, the tenth-run prompt uses “UNLIMITED DOPAMINE.” Check the title's spacing on the phone.

Verification: title geometry, progression policy and source-wiring tests pass. Unrelated frozen title layout remains protected by the existing source test.

Locations: TitleScene, TitleGeometry, UimsdGate and title tests.

## 8. Demo data could be miscounted as genuine history

Problem: the genuine-run counter did not explicitly reject a marked synthetic history root if a caller supplied one.

Cause: normal callers used real history, but the counting method trusted that choice of root. A controlled demo-root fixture reproduced the missing guard.

Fix: a synthetic catalog never contributes genuine runs. The existing progression logic was separated into a testable policy without resetting stored unlock flags.

Possible effects: demo records cannot satisfy the real-run requirement, including when pointed at a demo folder by mistake. Existing earned unlocks remain earned.

Verification: tests simulate nine real finished runs plus an unfinished run, the tenth finished run, prompt retirement, repeated end notifications, marked demo roots, imported records and restart-persisted flags.

Important intentional rule: the documented “I LIKE CONFUSION” demo shortcut opens UIMSD permanently before ten real runs. It does not unlock the guided tour. Deleting demo data does not revoke that shortcut or unlock the tour. Therefore UIMSD being open with one visible real run is not by itself proof of a bug.

Unresolved rule: recorded abandonment currently emits an end event and counts as an ended genuine run. Older documentation calls abandonment unfinished. A characterization test establishes current behavior; no counting policy or old player progression was silently changed. This needs an explicit product decision before alteration.

Locations: UnlockProgression, UimsdUnlock and progression/preferences tests.

## 9. Private feature flags could become permanently off during startup

Problem: asking for feature flags before the file service was ready could make a private test build behave like a normal build for the rest of that process.

Cause: ApdFeatures marked its cache loaded before checking whether Gdx.files existed.

Fix: leave the cache unloaded until files are available, allowing the later call to read the asset. A genuinely missing or unreadable asset still keeps private features off.

Possible effects: a private test build now reliably loads its intended flags after startup. Normal builds still keep test-only features off. This does not enable new community features or change the Cleric's release policy.

Verification: the early-call reproduction failed before the fix and passes after it. Missing-asset and unreadable-asset cases remain off.

Location: ApdFeatures and ApdFeaturesStartupTest.

## 10. Invalid filter values were accepted

Problem: impossible dates and non-finite numbers could silently turn into valid but incorrect filter values.

Cause: date conversion normalized dates such as February 31; numeric rounding turned NaN into zero and saturated infinities or oversized numbers.

Fix: reject a date unless converting it back gives the exact supplied date. Reject non-finite and out-of-range scaled numbers before rounding. Valid leap dates and finite numbers retain existing behavior.

Possible effects: invalid values now trigger the existing invalid-value explanation instead of producing misleading results. A previously saved numeric query remains its recorded numeric value; no history is edited.

Verification: both reproductions failed before the fix and pass after it, with valid-value regression checks.

Location: CellFormatter and CellFormatterTest.

## 11. Unsafe server addresses passed the local HTTP rule

Problem: an outside address such as http://localhost.example.org was accepted as local.

Cause: validation checked whether the text started with “http://localhost” or “http://127.0.0.1,” rather than checking the actual host.

Fix: parse the address and require an exact loopback host for HTTP. HTTPS still requires a real host. Reject embedded credentials, invalid ports and malformed addresses; preserve supported HTTPS paths.

Possible effects: previously accepted unsafe or malformed addresses are rejected and select the offline gateway. This does not turn sending on, configure a server or initiate a connection. Correct localhost test addresses and HTTPS addresses remain valid.

Verification: two controlled assertions failed before the fix; all three new test methods pass afterward. No network transmission was needed to test validation.

Location: Gateways and GatewaysTest.

## 12. UIMSD menus vanished when the scene rebuilt

Problem: switching apps or rebuilding the scene could close parameterized UIMSD dialogs while keeping the table.

Cause: the scene only knew each window's class and tried a public no-argument constructor. These dialogs need a model, row or other arguments, so restoration failed and the exception was ignored.

Fix: allow a window to supply a semantic reopening action. Added it for Options, Privacy, saved views, rule selection, sample-size selection, Columns, column order/move, filter steps and pending multi-selects, sorting steps, Ask menus, Notes, Guide/topics, report dialogs, the demo warning, demo-deletion confirmation, saved-view Open/Delete actions, grouping menus and row details. APD text inputs preserve their typed text and callback; their width follows the viewport. The Guide dispatches to the new live scene. Existing no-argument windows retain their previous fallback.

Possible effects: supported dialogs reopen instead of disappearing. Their scroll position can return to the top. Some less common confirmation/leaf dialogs still need their own reopening actions. Unsaved query filters/sorting are not newly persisted across a complete process restart; named saved views preserve those combinations. Restoring an obsolete row against a changed model is a remaining edge; normal scene recreation retains that model. Android process death is different from an in-process scene rebuild and is not claimed covered.

Verification: production sources compile and 524 local checks pass after this change. A new Android test checks Options and Columns restoration, nested filter Back behavior, Ask, typed text/callbacks, pending multi-select application, the existing Settings fallback, actual narrow labels/input buttons, and additional grouping/saved-view/demo-deletion restoration checks. Its final result must be checked on the build-verification page. Physical app-switch behavior remains to be tested.

Locations: PixelScene, Window, WndUimsdList and the named UIMSD windows; UimsdRestoreDeviceTest.

## 13. Failed saved-view replacement could alter the destination

Problem: saving views to an invalid destination could delete an empty directory and replace it with a file. The older fallback also deleted an existing view file before a replacement that could fail.

Cause: when the first rename failed, the store deleted the destination and tried again. A controlled directory fixture reproduced the unwanted deletion/replacement.

Fix: reject a destination directory. Use one sibling-file rename and keep the previous destination if replacement is refused. Remove the destructive delete-first fallback. The statistics library's older-Android API restriction is preserved.

Possible effects: a filesystem that refuses replacement now reports a save failure instead of deleting the old file and trying again. A temporary file may remain after failure. Normal Android/Linux replacements remain supported. This changes view/layout preferences storage, not run logs or player statistics.

Verification: the directory test fails before the fix and passes afterward. A failed temporary write preserves the old saved-view bytes. Existing saved-view create/replace/delete and model layout tests pass. The exact previously suspected second-rename failure was not independently forced; that destructive path is removed.

Location: SavedViews and SavedViewsTest.

## 14. An old send result could remove newly queued data

Problem: an older queued item and its newer replacement could share an ID. Completing the old send could delete the new item or apply the old retry delay to it.

Cause: after releasing the queue lock to call the gateway, the merge matched only the ID. It did not check which version had actually been tried. A controlled test replaced the item inside an in-memory gateway and reproduced the loss.

Fix: apply a result only when ID, creation time, schema, version and payload all match the attempted item. A newer replacement keeps its own payload and retry state. No storage format changes or server activation are involved.

Possible effects: a replacement remains queued for its own later attempt even if the previous item was accepted or rejected. This does not cancel a request already in progress. The disabled backend stays disabled; this was a latent data-integrity problem rather than evidence of a phone transmission.

Verification: nine controlled combinations cover accepted, rejected and retry outcomes against changed payloads, requeued identical payloads with newer timestamps, and changed versions. The new test failed before the fix. The local regression subset passes 528 checks. Complete newer-build results belong on the verification page.

Location: SubmissionQueue and SubmissionQueueTest.

## Other verification and remaining limits

Sad Ghost reward visibility and quest-recording tests passed in the complete core suite. Source paths for Sad Ghost, Wandmaker, Blacksmith and Imp were reviewed without gameplay or balance changes. This is not proof of playing all four quests to completion on Android. Blacksmith's explicitly unfinished Fungi path was not enabled.

No backend, clan/community service, monetization, hidden achievement, Eternal Grudge or Cleric Grudge/Lore feature was added or enabled. Save format, signing key and application ID are preserved. Compilation and deterministic tests cannot discover every gameplay bug.

No claim is made that the original three differing archive files were fully diagnosed, that age verification is integrated, that every UIMSD leaf dialog restores, or that eight uninterrupted active development hours occurred. Detailed evidence and interruption accounting are in APD-SPRINT-REPORT-2026-10-09.md.

## What to test next on your phone

Install the newer private APK over the existing app; do not uninstall or clear storage. If Android rejects the update, preserve the old installation and saves while the signing/version mismatch is investigated.

1. Long run-detail actions: portrait and landscape, scroll to the bottom, then tap each action. Dragging must not activate one.
2. Queue a Warrior run, inspect it, try adding it again, remove it, restart the app and verify queue/history/statistics.
3. Move and hide columns; change views; reopen UIMSD; force-stop/reopen. Save, open and delete a combined filtered/sorted view. Include equal scores and zero matches.
4. Verify Bosses excludes unfinished runs and explains why.
5. Export and reimport unfinished/completed history repeatedly; confirm no originals are lost and read the new explanation.
6. Check Clean humor, locked/tenth-run/retired title text and absence of the premature drugs fallback.
7. Check the documented demo shortcut and locked tour, including after demo deletion/restart. Preserve existing unlocks.
8. Switch apps with Options, Columns and a filter menu open. Check their restored controls and Back navigation; report unsupported leaf dialogs separately.
9. Start, save, continue and finish a real run, then check its statistics. Later test each NPC quest on the physical phone.

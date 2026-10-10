## 2026-10-10 — New confirmed issues, fixed in verified TEST31 source

| ID | Description/location | Evidence/root cause | Severity | Status | Phone check |
| --- | --- | --- | --- | --- | --- |
| 20261010-status | UIMSD status/queue dialogs disappear on scene recreation; WndUimsdOptions/Row | WndMessage(String) had no semantic/no-arg restorer; new exact-source native restoration/geometry assertions pass | Medium UI | Fixed; 1004 core/6 API34 verified | Switch apps, return, Back |
| 20261010-views | Saving/renaming/deleting a partially readable saved-view file discards skipped entries; SavedViews | Red tests reproduce byte loss; invalid Base64 UTF-8 silently replaced characters. Nine damaged edit cases preserve exact bytes after guard/strict decoder | High preference-data integrity | Fixed; future-query preservation tested | Normal view lifecycle; do not corrupt real files |
| 20261010-preview | Raw IDs/JSON can overflow and underscores/stars disappear; UimsdText/LongWordWrap | Vanilla wraps between words and parses highlighting. New raw mode/measured token breaks pass 7 fixtures and actual narrow-font native assertions | Medium UI/data presentation | Fixed; queue payload storage unchanged | Read/scroll a real queued preview |
| 20261010-delivery | Missing commit marker can auto-publish a stale report without dedicated signing comparison; sprint workflow | Existing publication branch used old October 9 docs. Verification now cannot publish; separate current-report/signing/source gate required | High delivery reliability | Automatic path disabled; TEST31 signed delivery pending | Install update after successful publication |

Remaining confirmed limitation: skipped saved-view records are protected and readable entries remain accessible, but edits pause until explicit repair; no automatic destructive recovery. Process death and preserving scroll offsets remain outside the scene-recreation fix. No new normal-floor connectivity defect found in 240 cases. Physical-device acceptance remains pending.

## Repository transfer recovered

Canonical private repository is now APD-Development/ShieldingShouldHaveWorked. Read access verified, Codex branch09c0f36113a75f51fb228b83ad03b02eff9ba565 matches the previously pushed UIMSD audit; Claude/main unchanged. The last write attempt against the old owner stalled385.6seconds and was user-aborted; local fixes were preserved in snapshot commit e9affc2, never pushed as Git ancestry. Restore the BackendClient damaged-queue refusal and its regression test through a lease-protected Git-data commit on the existing remote ancestry.

Current local source has557passing non-native tests (17.886s) and30targeted UIMSD checks (10.101s). Repository access is restored; GitHub Actions quota restoration is not verified, so do not trigger more cloud jobs or paid usage. New UIMSD fixes remain unbuilt/unverified on Android; latest delivered verified APK is TEST20. Continue independent UIMSD safety investigation, preserving saves, RNG and creator restrictions. Earlier repository-access failures below are historical.

## Cloud quota interruption — local UIMSD work continues

Owner reports GitHub Actions minutes exhausted. TEST21 core/debug and Android jobs report success, but delivery job114016454824 failed before any step, with runner_id0; its logs return404 BlobNotFound. Billing/reset details cannot be verified through this connector. A job-retry tool call stalled524.6seconds and was user-aborted; latest observed run37987339578 remains attempt1/failed. Do not assume retry ran or TEST21 was published. TEST20 remains latest verified delivered release. No paid usage, billing/settings changes or security bypass authorized by this workaround. Do not trigger/rerun Actions or change build-request/PDF-trigger files while this quota interruption remains unresolved.

Continue normal code, local tests and ordinary lease-protected GitHub commits. Codex work-branch ordinary commits do not trigger Actions (workflows inspected). APK assembly/emulator acceptance is unavailable locally because this workspace lacks the Android SDK and desktop libgdx native library. Never claim new UIMSD patches are in TEST20 or have passed unavailable native acceptance.

Final local UIMSD audit acceptance:557checks pass17.886seconds for the latest source, plus final targeted30checks pass10.101seconds. Includes192 combined scope/filter/sort/column saved-view disk-reload configurations,24,024 hidden-information comparisons, corruption preservation, accurate privacy counts, acknowledged-send bookkeeping, mixed-row-count units and excluded-history explanation. BackendClient now refuses a damaged queue before starting a background sender/network work, reports the reason, and skips mailbox fetch if damage is found after a send. The synchronous refusal has its own regression check. No server/community functionality enabled, no history reset or gameplay balance/save-schema/RNG change. Full current-source core/native/build still pending; this is not proof of zero bugs or physical-phone readiness.

GitHub application audit commit09c0f36113a75f51fb228b83ad03b02eff9ba565 plus the subsequent BackendClient/refusal-test checkpoint preserve the work for either developer. Next: restore an authorized build/test environment when available, run the full suite and targeted Android against exact latest source, then deliver a new APK/PDF; keep TEST20 rollback. Meanwhile continue independent UIMSD/gameplay data-safety tasks and record real test limits.

## Pending UIMSD reliability audit — C26–C29

Owner prioritizes UIMSD and forbids game-breaking crashes/freezes/save loss/gameplay interference. This audit does not promise zero bugs. C26: mixed-rules exclusion counters count table rows, but status text labeled them runs (five items from one run became five runs); use actual table units and singular/plural, without changing history/count policy. Two reproduction tests failed before and pass after.

C27: OutboxStore intentionally skips/counts damaged lines, but queue mutations ignored that result and rewrote the file, permanently removing unreadable entries. Reject updates and sending from a partial read; preserve original queue bytes. All mutating loads and pre/post-send loads use the guard. Three corruption cases failed before/pass after: add, remove and initial send. A fourth regression reproduces corruption during a fake gateway acknowledgement: retain the damaged file, report the real acknowledgement and record it rather than pretending nothing was sent. No real server enabled or contacted by these tests; offline operations remain local. A retained acknowledged entry may need an idempotent retry; no automatic deletion/repair is introduced.

C28: PrivacySummary treated unreadable queues as empty and all envelopes as finished-run summaries (even settings/evidence). It now warns when count is incomplete/unknown and describes queued items accurately, including explicit-consent full-log evidence. Three new controlled cases failed before/pass after; normal count wording expectation updated. It never claims a transmission from queueing/inspection. C29: Home now explicitly explains that unknown/changed-rule runs and repeated imported copies are excluded, with table Options to include them; no unlock/count policy changed or history removed.

Audit coverage: 65 targeted engine/saved-view/column/queue/archive/privacy-safety checks passed before the queue hardening. New independent combined-query oracle passes192 scope/filter/sort/column arrangements after actual saved-view disk reloads, checking expected row identity/order, equal-value stability and displayed column order. Hidden-information safety compares24,024 query outcomes across histories whose hidden facts differ; no observable differences. Queue/status/combined/safety batch28checks pass2.199s. Existing+new local555checks pass17.399s before final Home clarification; targeted Home/status four checks pass0.150s afterward. Complete core/native/build acceptance still pending for these changes. CI will also exercise recorder failure containment, recording-on/off generation/combat/item safety and unchanged golden baselines. These comparisons are test loops, not thousands of separately reported unit tests.

Remaining: physical RedMagic app-switch/touch/layout/performance checks, Android process-death restoration of unsupported dialog types, real older-APK upgrades/exact owner saves, full normal-run quest/boss walkthroughs, and the owner's exact archive. No live age provider/server exists; unknown age remains fail-closed, Grudge stays creator-disabled. No player data, seeded generation, save format, Cleric restrictions or gameplay balance changed. Latest delivered TEST20 until newer verification succeeds; Blacksmith-only TEST21 candidate is verifying separately and does not contain this audit patch.

## Pending next build — C25 Blacksmith save isolation

Continued quest/save-safety review after TEST20. Confirmed two failures before fix: a spawned legacy Blacksmith save missing free_pickaxe retained the previous save's free-pickaxe entitlement; missing reward enchantment/glyph fields retained the previous run's modifiers, including when unenchanted reward lists existed. Restore now defaults only those optional fields before reading this save. Avoid calling Blacksmith.reset during spawned restore because it creates/identifies a pickaxe; no new item allocation, recorder hook, RNG draw, reward generation, balance or schema change is introduced.

Three new persistence checks pass: absent legacy fields clear previous entitlement/modifiers, unenchanted saved rewards stay unenchanted, explicit free-pickaxe and enchanted rewards still restore. Combined ten Imp/Ghost/Blacksmith/ghost-transparency checks pass locally0.058s; existing537 non-native checks pass21.842s. Full CI core/build/native verification is requested, not yet passed for C25. Wandmaker restore already resets missing/unspawned quest saves; no confirmed new Wandmaker regression found in this review, but its full phone quest remains unverified.

Latest delivered APK remains TEST20 until next build passes. Phone testing and real older-APK upgrade remain pending. Continue to preserve creator restrictions, player data and other developers' commits.

## VERIFIED LATEST DELIVERY — CODEX TEST20

Application source e4ad30fd84362589db3ba1180a785ee35344472e; GitHub run37976561794. Private release apd-codex-test-20 published October9,2026 3:06:20PM EDT (19:06:20UTC). APK ADHD-Pixel-Dungeon-CODEX-TEST-20.apk,38,225,942bytes; SHA256 0a7d05dc4fb1e87bf502849914c94ae13c4ffeaeaacc8ca865d8c3122a5808eb. TEST17 and TEST10 remain available. Install over existing APD; do not uninstall/clear data. Signing identity/application ID/save schema unchanged.

Changes since TEST17: C23 clears Imp quest completion/shop/legacy flags/reward options between saves and new runs; C24 clears absent Ghost reward enchantment/glyph fields on restore. Both are reproduced cross-save state bugs, not speculative balance changes. Three Imp tests failed before/pass after; Ghost missing-enchantment case failed before/pass after, legitimate enchanted legacy data still restores. Gear/options icon now uses existing APD title-arrow pink0xFF2EC8 per owner correction; existing seed/challenge color indicators preserved. No gold theme shipped. The fresh-install gear report was TEST10, whose intro guard hid the options button; TEST17 already removed that guard, and TEST20 adds direct Android menu verification.

Measured acceptance: downloaded XML confirms complete core913tests/146suites/0failures/0errors/0skips, including Imp persistence3, Ghost persistence2, Ghost transparency2 and actual QuestRecording1. Debug build succeeded. API34 targeted4checks/0failures/errors/skips: UIMSD restoration/narrow layouts9.199s; play/save/continue/controlled death69.965s; controlled win66.641s; intro-enabled character difficulty plus actual gear tap/populated readable panel-contained menu70.720s. Total native testcase time216.525s. Local existing non-native537pass22.779s and seven targeted checks pass; local QuestRecording was blocked by absent libgdx64.so but passed on CI. No confirmed regression in these tested paths. This does not prove full quest walkthroughs, every boss/floor, real old-APK upgrade or RedMagic behavior; deaths/wins are controlled triggers.

Relevant commits: d4d52d3ec72b3f4655a10c665be21596a03d7b22 Imp isolation/tests;6eb27e0a03367797439181315bf06b7aa06d427b Ghost isolation/gear test;e4ad30fd84362589db3ba1180a785ee35344472e final APD-pink application source. Earlier verification-only TEST18 core/build succeeded but native was canceled for final-source changes; gold TEST19 candidate superseded before delivery by owner pink correction. Neither superseded candidate was published. Preserve GitHub ancestry; local snapshot history is synthetic and must never be pushed.

Timing: final CI core/build18:55:07–18:58:12UTC (3m05s); Android18:58:14–19:05:54UTC (7m40s); publication19:06:20UTC. Eleven explicit45-second polling waits scheduled approximately8m15s of waiting, overlapping CI, not active coding; this is an estimate from tool records. Full continuation start and precise active/coding/research allocation were not instrumented, so no fabricated continuous-eight-hour or zero-downtime claim.

Phone checks next: install TEST20 over TEST10/17; select Warrior/Mage and see/tap pink gear; verify options populated; verify first-floor doors, toolbar/Search/Wait/items, descend, save/title/continue. Check actual quest completion/rewards and switching saves. Community preview, unsupported dialog/process-death restoration, exact owner archive reproduction and actual device/upgrade/full-run coverage remain outstanding; age provider unavailable/fail-closed, Grudge still creator-disabled, no community backend enabled. Prior pending notes below are historical and superseded by this measured entry.

## Next candidate — quest isolation and APD-themed class-selection options

Since TEST17: Imp quest state is cleared on reset/load so another save cannot grant completed quest/shop access or leave reward options/legacy flags behind. Ghost reward restoration now clears absent enchantment/glyph fields rather than inheriting another save's rewards. Neither change alters the save schema, reward generation, seeded RNG, statistics or existing saved records. Risks to check: restore both old and new quest saves, retain their own completion/rewards, and finish each quest on a phone.

Evidence: Imp three regression tests failed before the fix and pass after; Ghost missing-enchantment test failed before and passes after, and legitimate enchantment restoration passes. Combined seven local Imp/Ghost checks pass. Existing 537 non-native local regression tests also pass after both fixes (22.779s). QuestRecordingTest cannot execute locally because libgdx64.so is missing; CI will run its native-backed coverage. No new physical-device test claimed.

Owner confirms the fresh-install gear/options report is from TEST10, whose tutorial hid the options button; TEST17 already removes that restriction. Owner dislikes gray and rejected the proposed gold; the ordinary gear icon now matches the existing APD title-arrow pink (0xFF2EC8), retaining green custom-seed and orange challenge indicators. HeroSelectScene compiles locally. Added actual Android gear tap coverage: button visible/active, panel opens, options have text and fit within panel. This new native check is pending; do not mark the phone report verified. No starting-equipment information was removed or changed.

Latest delivered APK remains TEST17 until this candidate completes full core, debug assembly and targeted API34 tests. Community preview and physical full-run/quest/upgrade checks remain outstanding. Previous release and all player data retained.

## Pending verification — Ghost rewards and reported gear menu (C24)

Confirmed save isolation bug: a spawned Ghost quest saved without enchant/glyph fields retained another loaded run's enchantments. Reproduced by one failing-before test; reset before restore fixes it. Both new Ghost persistence tests pass, including legitimate enchanted-save restoration; combined Imp/Ghost checks 7/7 pass locally. No reward generation, seeded RNG or save schema change.

Owner reports class-selection gear/menu information missing on a fresh install. Screenshot shows an options menu; exact APD build and whether options or starting-equipment information is missing are not yet established. Current code has no fresh-intro guard hiding gear options (removed in TEST17), and WndHeroInfo populates equipment icons/descriptions independently of intro. Added Android coverage tapping the gear button and checking populated readable panel-contained options; not yet run. Do not claim this phone report reproduced or fixed.

## Pending verification — Imp quest save isolation (C23)

Confirmed with three failing-before regression tests: restoring a save without a spawned Imp quest could retain another save's completed quest, earned shop access, legacy quest mode and reward options. Imp.Quest.reset now clears legacy mode, alternative and reward options; restore resets before reading the selected save. Existing legacy completion still restores correctly. No save format, scoring, reward generation or player history changes.

After fix: three new persistence tests and two Ghost transparency tests pass locally (5/5). QuestRecordingTest cannot execute locally because libgdx64.so is absent; GitHub full core/Android verification is requested. Physical quest walkthroughs and old-APK upgrades remain unverified. TEST17 is still the latest delivered APK; this change is not in it. Verification-only CI must not replace that release.

## Current outcome: emergency TEST17

C22 CRITICAL first-start/old-save exit/HUD/search/Main Menu/difficulty blockers: fixed and verified in appsourcea317b5d5 / run37972251204. Six generation/migration/intro-progression cases pass; full908core and four targeted API34 checks pass. Historical S03/returning-only assumptions superseded; first-run gameplay is priority one. C21/C18/C20 also verified in this exact candidate. No confirmed new regression in tested paths. Physical RedMagic, exact owner save/old APK upgrade, all-quests/full-game victory remain unverified. Full failure history follows; do not relabel older pending candidates as successful deliveries.

# APD bug log — October 9, 2026

Branch: codex/apd-phone-fixes-2026-10-09. Physical-phone testing is not claimed. Latest C8: 896 core tests and 3 targeted API 34 checks pass, zero failures/errors/skips. The owner-facing causes, fixes and side effects are in APD-BUG-FIX-REPORT-2026-10-09.md.

| ID | Description/location | Evidence / cause | Severity | Status | Phone check |
|---|---|---|---|---|---|
| C01 | Run-detail overflow, WndUimsdRow | Phone report; fixed-height single-line actions below scroll pane | High usability | Wrapped scrolling actions, 65ddac6/e33e945; layout tests pass | Required: real wrap/touch/drag/landscape |
| C02 | Columns lost, UimsdModel | Navigation resets in-memory query; no disk layout | Medium | Per-view persistence a7e595b; reload/reset tests pass | Required: navigation/restart |
| C03 | Null-root queue/invalid IDs, SubmissionQueue | Relative File(null,...) path and unvalidated key | Low | Guarded 8ff3bed; full sequence/fresh-process tests pass | Restart smoke check |
| C04 | Unexplained protected import differences, ArchiveImporter | Redaction changes unfinished exported bytes; fixture reproduced | Low usability | Diagnostic 60ac762; originals preserved across 3 imports | Owner's specific archive still unverified |
| C05 | Premature drugs arrow, TitleScene | Hardcoded fallback outside active tenth-run prompt | Medium content | Removed a14df7d; title geometry/wiring pass | Locked/tenth/retired title |
| C06 | Adult preference without eligibility, SPDSettings | Checkbox treated as permission; no age provider | High content | Fail-closed a14df7d; provider UNAVAILABLE, Adult blocked | Clean explanation; provider incomplete |
| C07 | Synthetic root counted genuine, UnlockProgression | Counter trusted supplied root; marked-root fixture proves it | Medium progression | Reject synthetic 1fc1cc1; tenth/demo policy tests pass | Demo vs tour/restart |
| C08 | Feature cache stuck off early, ApdFeatures | loaded set before Gdx.files; failing reproduction | Medium startup | Retry df4e7f5; normal/missing/unreadable off tests pass | Private startup/restart |
| C09 | Bosses unfinished scope, Scope/Datasets | Generic toggle bypasses default exclusion | Medium statistics | Finished-only boundary 897bd9f; codec/model tests pass | Finished-only note/results |
| C10 | Invalid dates/non-finite numbers, CellFormatter | Calendar normalization/Math.round saturation; tests fail before | Medium filtering | Reject invalid 8716b7d; valid-value tests pass | Invalid-value message |
| C11 | Remote HTTP looks local, Gateways | startsWith accepts localhost.example.org; tests fail before | High latent privacy | Exact parsed host acd1878; three tests pass | No server enabled; UI smoke optional |
| C12 | Menus vanish, PixelScene/UIMSD | No-arg reconstruction ignores parameterized windows | Medium usability | Semantic reopeners 2764791; extended typed/selection/Ask/narrow checks pass on API 34 | Required: app switching; some confirmations/process death unresolved |
| C13 | Test preferences outside temp folder, HeadlessTestSupport | External FileHandle prefixes user.home; failing temp-path test | Low test isolation | Explicit temporary FileHandle c9cf1f4; real XML tests pass | No production player data affected |
| S01 | Abandoned-run requirement ambiguity, RowBuilders/ApdRecorder | RUN_END sets finished; old docs say unfinished; characterization test counts 1 | Medium progression | Confirmed current behavior; design disagreement, no policy change | Owner decision before changing count |
| S02 | Generic non-UIMSD WndOptions heights | Source observation, no failing non-UIMSD reproduction | Low/medium | Suspected; UIMSD callers migrated, generic unchanged | Long non-UIMSD labels |
| S03 | Floor-1 hidden door from prior tutorial | Prior phone report; generation predates prior fix | Medium gameplay | Existing generated map persists; no reveal-all/balance change | Existing vs newly generated map |
| C15 | Queue flush removes/retries a newer replacement, SubmissionQueue | Controlled in-memory gateway replaces same ID while old payload is tried; before fix OK loses replacement | Medium latent data integrity | Merge exact ID/time/schema/version/payload only; 9 replacement/outcome cases pass, backend stays disabled | Future sending concurrency; no server enabled |
| C14 | Saved-view destination deleted/replaced, SavedViews | Invalid-directory fixture fails before; delete-first fallback unsafe | Medium preferences integrity | a938d31 rejects directories and preserves old target on failed rename; tests pass | Normal save/replace/column persistence |
| S06 | Restored row/model changes and unsupported leaf dialogs | Reopen captures semantic model/row; some confirmations/process death need more state | Medium usability | Input/selection/Ask restoration passes C5 API 34; no process-death guarantee | Leaf input, selection, app switch |

C3 verification: 893 core tests passed; API 34 ran 3 checks, 2 passed, 1 failed. The UIMSD test closed Settings (which schedules its own scene reset) and opened Options before that pending reset executed. That destroyed the new menu. The fixture now waits for the legitimate Settings reset before continuing; no production behavior or assertion was weakened. Fresh verification remains required.

Final C5 source 70d0e1fa: all 894 core checks and 3 API 34 checks passed; private CODEX TEST 5 APK/PDF published. Earlier compile/fixture failures are preserved above as historical evidence. Phone checks and unresolved policy/provider items remain open.

C12 follow-up: three previously unsupported leaf menus (Group by, saved-view actions, demo-deletion confirmation) now have explicit semantic reopeners. Expanded native checks must verify state/callbacks and that restoration/Back never confirms deletion. Follow-up result pending; TEST 5 remains verified.

| C16 | First-launch views/columns fail to persist, SavedViews/UimsdModel | Missing history parent; two controlled before-fix failures (FileNotFoundException and absent column file) | Medium preferences | Parent creation fix; targeted 29 checks plus C8 complete 896 checks / 3 API 34 checks pass | First-launch empty history, save/view/reopen |

| S07 / C17 | Ask split picker shows three checks while storing two | Oldest field removed from chosen but not its checkbox entry; list repainted only tapped row | Medium UI | Confirmed by failing API34 regression in C9; repaired source in 01c95ec4, final Android verification pending | Select three split fields; oldest unchecks, latest two remain and apply |

Latest C8 verifies first-launch storage and all added leaf restorers. C6 fixture failure is corrected and retained as historical evidence. S07 was subsequently confirmed as C17 by the diagnostic native regression; physical-phone acceptance and age/policy/process-death work remain open.


C17 (formerly S07), medium UI, confirmed by API 34 run 37946296408: selecting three Ask split fields displays three checks while answers use only the latest two. Fix synchronizes entries and refreshes switch visuals after callbacks. Thirty focused local tests pass; final native verification pending at 11:50 EDT, phone testing required.


S08: Columns picker may show the final column unchecked even though UimsdModel.toggleColumn deliberately preserves at least one column. Source evidence: WndUimsdColumns callback updates the model without reconciling Entry.checked after the model refuses removal. No controlled rendered-widget reproduction; not fixed in lunch candidate. Statistics/history remain protected by model guard. Phone check: leave one column, tap to remove it and compare checkbox/table. Low UI severity, pending reproduction.


C17 final: fixed and verified in TEST10 / run37961639763; API34 all3pass. Existing core896pass. Physical check pending. S08 remains source-only suspicion, unchanged.


## Post-TEST10 development — Columns state and deterministic archive fixture

C18 (formerly S08): confirmed last-column checkbox mismatch. Production callback test reproduces the last entry becoming unchecked while the model/table correctly retain one column. Two new tests: refused last removal, and ordinary removal/reselection across heading/note entries. Before repair: 2 tests, 1 failure. After repair: 22 focused checks pass (0.625s). WndUimsdColumns reconciles checked entries with final model columns; existing TEST10 list rendering redraws final checks. Added native actual-checkmark assertion and explanatory at-least-one-column wording. No change to the existing never-empty rule or player records.

C19 (test fixture, not a confirmed game bug): first broader run had 532 tests / 1 failure in ArchiveRoundTripTest.exportWhileARunIsBeingRecordedIsConsistent, requiring more than one event before the asynchronous writer had flushed its initial events. Isolated old suite passed15 (1.513s), confirming timing sensitivity. Fixture now asserts an initial writer flush before concurrent appends and uses AtomicBoolean for cross-thread stop visibility. No sleeps, weakened assertions or production archive changes. Targeted15 pass (0.549s); corrected broader532 pass (21.696s).

Android checks for these new changes are pending. TEST10 remains the installed/published lunch-test build; C18 is not included in it. Verify new native assertion before next delivered APK. New CI request is verification-only: privately stage APK and evidence without publishing an extra release. Daily lunch delivery schedules remain enabled. Physical RedMagic testing is still pending.


## C20 — Export must not claim success for an incomplete manifest

Controlled reproduction: a finished temporary log changes after manifest planning but before the second read (output-stream hook, no timing sleeps). Old exporter silently skipped the changed log, finished a ZIP still listing that missing entry, and reported success. Regression16 tests /1failure before. The importer safely rejects such an archive; no history loss was found.

Minimal production repair: detect missing/changed second-read content and return failed export with an explicit incomplete-archive/retry explanation instead of success. Existing originals, unfinished-run redaction and immutable redacted snapshots are unchanged; no extra whole-history memory buffering. The test mutates only its temporary fixture. Targeted16pass0.476s and broader533pass18.605s after repair.

Potential effect: exports in this exceptional change-during-read condition now fail instead of claiming partial success. A partial ZIP can remain at the chosen output location; do not use it as a backup, export again. Completed/unfinished ordinary round trips, archive imports, no-overwrite and hidden-information regressions pass. Physical-phone verification and the next full Android build for this new archive source remain pending.

C18 Android verification is running from source65403163ebecbf27f36817f6882bf91cbf9bfa61 / run37966964739. That staged candidate does not include this later C20 repair. TEST10 remains the owner's current playable phone-test APK; do not mislabel staged or local code as delivered.

C22: CRITICAL gameplay, confirmed phone screenshot + source/generation characterization. First-run intro still hides entrance/controls, suppresses search, disables Main Menu and difficulty. Earlier returning-only fix was insufficient. Implemented non-blocking tutorial and narrowly scoped saved-entrance repair; 537 local checks pass. Full core/native/Android checks pending, owner RedMagic check required. Supersedes S03 severity/assumption; not limited to old saves.
C21: confirmed title wrapping defect from missing HERE in locked message; 9 geometry tests pass and existing frozen-source remainder still checked. Actual font narrow-screen regression pending CI.

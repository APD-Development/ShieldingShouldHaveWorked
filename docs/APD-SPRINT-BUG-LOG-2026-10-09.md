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

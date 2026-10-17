## TEST271 corrected private speed Android test requested — 2026-10-17
Previous [TEST270 full verification run 39062224139](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/39062224139) failed honestly: `verify-and-build` produced a compiled private APK after passing complete 1779 core and previous TEST269 history, but `android-smoke-check` failed at `ApdSettingsClickDeviceTest.java:269` with duplicate local `choices`; the supplemental display matrix also failed instrument-test compilation. The APK was **not** accepted, staged or distributed. Corrected only the native fixture's inner local name to `speedChoices`. Application gameplay and extreme visual speed code stay byte-identical to compiled TEST270. Intentional complete corrected TEST271 gate requested using immediately previous compiled TEST270 source/run, 1779 core, 38 native API34, 57 PNGs, debug and strict history. All original failed jobs/evidence are retained; status **PENDING**. Prior staged TEST268/public owner TEST262 remain fallbacks. See `docs/APD-PRIVATE-PLAYBACK-SPEEDS-2026-10-17.md`.

## TEST270 private-only extreme visual playback full candidate requested — 2026-10-17
Exact application source `38c78be5966aeaa2fb4b1c57ff1bed7cf0a775f0` adds developer/test-build-only 4/8/16/32/64/100/1000x speed to original 0.5/1/2x. Existing `ApdFeatures.testBuild()` gates menu, persistence and visual multiplier, with production display limited to the original three. Scrollable 10-choice developer picker and extended existing native settings test exercise 1000x and reversion to 2x. Core-only [39061905957](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/39061905957) **passed** (expected 1779 core); complete debug/API34/history/screenshot verification for intentionally changed TEST270 is now requested, **not yet proven**. Prior actually compiled TEST269 source/run `7917a88234be99bbb18bf74618678bf8f851cf86` / `39053518382` is pinned, complete path ledger reviewed. Staged TEST268 and published TEST262 remain fallbacks. No publication/distribution or physical phone testing. `docs/APD-PRIVATE-PLAYBACK-SPEEDS-2026-10-17.md`.

## Developer/test-only playback speeds to 1000x candidate — 2026-10-17
Owner-approved experiment extends visual playback in `-PapdTestBuild` only: adds 4, 8, 16, 32, 64, 100 and 1000x to original 0.5, 1 and 2x. Gated centrally by `ApdFeatures.testBuild()` in selection, preference and visual group; public build retains three-choice Options UI and ignores developer-only preference values. Developer build gets a scrollable 10-option picker; does not touch combat, actor clock, scoring, RNG, saves or HUD timing. `PlaybackSpeedTest` adds private/public, factor and timing regressions; existing API34 settings touchscreen test now selects 1000x and restores 2x. Core-only verification **PENDING**. No APK/distribution/public release request until core passes. Refer to `docs/APD-PRIVATE-PLAYBACK-SPEEDS-2026-10-17.md`.

## Button-origin scroll candidate cleared complete core; full gate requested — 2026-10-17T17:23:00Z
Core-only GitHub Actions [39053210660](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/39053210660) completed successfully against candidate `f6f94c98c5e99c239b5ec7dab7029c1bfffe7cf0`, including the new `ScrollClickSuppressionTest`. Core artifact `12108228249`, ZIP digest `sha256:767a3650256bb0cea8892535a9d0d81b75f62c19b14b23238afa7e09f0702bb0`. Intentional TEST269 full candidate requested: 1776 core, 38 API34 with checkbox-origin swipe then normal tap, debug, full TEST268 compiled history and 57 PNGs. **APK, native and supplemental gates PENDING**. No publication or distribution. Keep independently staged TEST268 and published owner TEST262 unchanged. See `docs/APD-BUTTON-ORIGIN-SCROLL-2026-10-17.md`.

## Scroll gestures beginning on buttons and links, pending candidate (2026-10-17)
The owner reports that swiping a page does nothing when the touch begins on a button. Confirmed source mechanism: child controls register after the ScrollPane and consume DOWN, so the scroll listener never captures the drag. Bounded opt-in `ScrollPane.allowDragFromButtons()` reorders the pane listener ahead of child controls but passes the initial touch through. Once movement crosses the pre-existing threshold, it flags that pointer as a scroll, causing `PointerArea` to release both buttons and links without firing a click; a stationary tap still works. Fast UP-only swipes are also handled. Enabled on APD Settings, About, and Cleric Grudge; other ScrollPanes (including UIMSD row pickers and Custom Rules direction-locked sliders) are deliberately unchanged. Retained `ScrollClickSuppressionTest` for swipe/tap/cancel identity and extended the existing API-34 Adult humor touch test to swipe from a checkbox without toggling before tapping it normally. **No test evidence yet; no APK or distribution**. Source candidate should go through core-only gate first (1776 methods), then full existing 38-method native/history/debug/visual verification on intentionally new APK if core succeeds. TEST268 staged and TEST262 published remain fallbacks. Detailed notes: `docs/APD-BUTTON-ORIGIN-SCROLL-2026-10-17.md`.

## TEST268 S22+ Hide UI exact existing APK independently staged — 2026-10-17T16:47:58Z
Independent [staging run 39050915606](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/39050915606) at receipt wrapper `b92e0fcca6afa3de69817248c2f673160ebcbf45` succeeded, reusing **unchanged** compiled TEST268 APK SHA256 `589c6a0b5c2d411698efca6ac353050fbf6506e28b9e94e9ec48e8cd6ea2b1f0` from full successful [CI run 39049580620](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/39049580620). Staged private preview artifact `12107901330` (44,498,167 bytes, ZIP SHA256 `f89a4f4fbeee3f0edcadd837bfd9576c6f53c97d8d1a9cd5dfa88de3f4ef78a3`) and evidence artifact `12107936058` (8,394,072 bytes, ZIP SHA256 `0e5e26f3fa3444450c2998d4971961ac728ce82073f8f6f01f7397a1ee207ec5`) are privately staged. Passed exact source/config/evidence, 1,775 core/38 API34 methods, 57 screenshots, private Cleric marker, signature, app ID `com.charltonchris29.adhdpixeldungeon.indev`, version `4.0.8-apd-INDEV-bC268-TEST`, and signing certificate `067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e`; TEST262 version code permits install-over. This is CI evidence, **not physical S22+ or owner-save install-over proof**. Same-APK display retries accepted all five profiles while initial small/landscape failures remain explicitly preserved. TEST267 remains staged fallback; TEST262 remains latest actually published owner APK. No public release or tester distribution; no application rebuild. APK executable build request remains byte-for-byte untouched by this receipt.

## TEST268 S22+ title-touch full CI and same-APK display accepted — 2026-10-17T16:44:00Z
Verified full source `abbd936515873384a6d299f037ec925cc5185f51` in workflow [39049580620](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/39049580620): 1,775 core tests and 38 API34 methods all passed with zero failure/error/skip, debug assembly and complete immediate-previous compiled TEST267 history passed. Exact raw 38,593,360-byte APK `ADHD-Pixel-Dungeon-CODEX-TEST-268.apk` SHA256 `589c6a0b5c2d411698efca6ac353050fbf6506e28b9e94e9ec48e8cd6ea2b1f0` from artifact `12106553888` (ZIP SHA256 `80fdc44ea3d9b24d5bfe432c9bdaef90a0eee9525e7e4276a61282c9d050dc8c`). Independently extracted bytes/hash. 57/57 required PNGs decoded as 320x640, contact sheets visually reviewed with no blanks/corruption/obvious clipping. Android artifact `12107995237` (ZIP sha256 `deb5879ff7845b9bbba4a883112430c62c2948c7cacd1301d50d0735be708b2a`); core artifact `12106957231` (ZIP sha256 `7ff590804516f117259c55b8c209f85a813f0ff455db06109272b3bcfaf0d3e8`).

Supplemental [39049580636](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/39049580636): initially three profiles passed; **small and short-landscape failed when Changes did not open, before the new Hide UI tap**. Original failures and evidence `12107252635`, `12107631394` retained. One failed-jobs-only retry on the *exact same APK* passed both; all five display profiles now have accepted same-APK evidence. Native title tests include center tap, expanded outside-icon tap, fade out and restore. Current staging initial `39049580644` failed closed because the prior TEST267 preparation profile legitimately rejected changed app source; no delivery occurred. Preparation profile is now pinned to exact TEST268 evidence for **independent same-APK staging**, not rebuilding, automatic publication or tester distribution. Published owner TEST262, staged TEST267 preserved. No physical S22+ result or owner-save install-over claimed. See `docs/APD-S22PLUS-TITLE-HIDE-TOUCH-2026-10-17.md` and `docs/APD-TEST268-S22PLUS-TITLE-VERIFICATION-2026-10-17.json`.

## S22+ Hide UI touch candidate full acceptance requested — 2026-10-17T16:27:57Z
Corrected full core-only workflow [39049282660](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/39049282660) concluded **success** after the first retained 1/1775 title-golden failure in [39048921810](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/39048921810). New geometry and strict-normalized unchanged goldens are retained. This commit intentionally requests a new source candidate with 1775 core, 38 API34, 57 required PNG, debug build and full immediate-previous compiled TEST267 history. Request compares every reviewed path including the build-request and history-ledger changes. **The APK and native results are PENDING.** Current published TEST262 and verified staged TEST267 remain fallbacks. No tester distribution or physical S22+ claim. See `docs/APD-S22PLUS-TITLE-HIDE-TOUCH-2026-10-17.md`.

## Initial core failure and retained strict-golden correction — 2026-10-17T16:23:36Z
First core-only workflow [39048921810](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/39048921810) on source `df80dd5792ed2e31795f6a7b273efc67024def32` executed all 1,775 tests; 1 failed: `UimsdWiringTest.theTitleScreenMatchesApprovedResponsiveLayoutAndKeepsItsUimsdTarget` because its historic title-source digest caught the intentional Hide UI touch enlargement. This is an exact-source snapshot mismatch, not a gameplay or hit-target test failure. Preserve the failing evidence (artifact `12106966279`) and the original golden SHA values. The guard is corrected to require the exact approved new footer snippet/priority/constants and remove *only those exact snippets* before checking the old SHA values; no unrelated source or guard is exempt. Native Android test use of `PointerArea.x/y` was separately corrected at `250f5625562d345c001d1b65a7e087cce08570c4`. Full core retry and all APK/Android checks remain **PENDING**; no APK build or publication requested.

## Samsung S22+ Hide UI title-footer touch candidate — 2026-10-17T16:17:41Z
Ozzie reported TEST254 footer chevron non-response. The chevron intentionally hides the UI, and the owner showed it working on a different phone. Current branch adds a larger 32×20 virtual-pixel tap target to the original 16×16 visual icon, keeps hit area clear of other buttons and retains Hide UI/reset semantics. New core geometry regression and expanded native pointer regression are **pending** their required CI evidence. Source is a candidate only, not verified, built or delivered. Original short-landscape native failure remains unresolved until measured. Published TEST262 and staged TEST267 are unchanged; no change to executable APK build request. Evidence plan: `docs/APD-S22PLUS-TITLE-HIDE-TOUCH-2026-10-17.md`.

## TEST267 exact same-APK private staging — 2026-10-17T08:14:00Z
- Run `39016526869` at wrapper `87ce1ac4b10f27a2ca07db0529debd1139e38d0c` succeeded without rebuilding application source.
- Reused raw APK SHA-256 `f5fc7189d981709cf46e740d1966814c64b5ed29a7e32bca921739ce4e04009f`.
- Staged preview `12094921683`: 44,588,415 bytes, digest `sha256:58a3e9e7e14ec1493038787b68a3674edf3db2b28aa28d5fbfb8367a95c399a6`.
- Staged evidence `12095106256`: 8,485,020 bytes, digest `sha256:df3021f1e8cd3fae7268e15ed5ac90b8421b0c20056b37784d4f69c29560048b`.
- Verified signing certificate, application ID, TEST267 version, install-over version compatibility, Cleric-lore feature marker and all 57 required screenshots against the TEST262 line.
- No publication, tester distribution, phone test, owner-save install-over, APK rebuild or build-request edit.

## TEST267 UIMSD legacy-date validation — 2026-10-17T08:12:00Z
- Reviewed current `RowBuilders.java` completely and found one production defect: impossible Gregorian legacy dates were leniently normalized into false start timestamps.
- Reproduction: test source `c48ed80c538695356dbda05b8faab51aca6d456c`; run `39014149854` executed 1,774 methods and failed exactly the new regression. Artifact `12093313933`, 832,953 bytes, digest `sha256:ad6ac3f02ff85ecdb0b630004f1ba6b44433c85e264dd60f4e844e2e1c6881c9`.
- Fix: application commit `d76178a015b68344adfbeab2edfd701201c752a7`; corrected core run `39014400159` passed 1,774 methods. Artifact `12094346515`, 830,170 bytes, digest `sha256:d3f775a284752db59b2118cabbd23cbe7686e24bc41d67f268fd3ac17a2d5dcc`.
- Full gate: source `c761afc25902954060daf5fffbd87e728b8b1887`, run `39014635516`; 1,774 core / 38 API34 / debug / complete TEST266 history / 57 decoded and manually reviewed PNGs all passed.
- Exact APK SHA-256 `f5fc7189d981709cf46e740d1966814c64b5ed29a7e32bca921739ce4e04009f`. Private staging requested without rebuilding; no publication or distribution.
- Supplemental display run `39014635513`: 4/5 profiles accepted across three retained attempts; short-landscape Hide UI remained failed at line 83. Required visual gate remained green.
- Preserved raw history, saves, all 18 rules, active 357-value v2.6R scoring, original scores, signing/application identity, gameplay, turns, RNG and completed Cleric work.

## 2026-10-17T07:12:00Z — TEST266 independently staged without rebuild
- Stage run `39012540034` at wrapper `d5e3a87c0cb7ef128818973bac7fe890fc92c7de` succeeded and reused exact TEST266 APK SHA-256 `7ba0bf6d2d93fb2e4c7b27cf6caf718e3c74e960abfa0274d434c9869089674c`; no application rebuild.
- Staged preview artifact `12093094377`: 44,528,550 bytes, digest `sha256:629a784f55e062513fca5cf9b2d4f82d36e738f87942ee686661c754f905827a`. Evidence artifact `12093755910`: 8,426,017 bytes, digest `sha256:1e3bfd3d660b0b36e0c040faa7d79311417a2f51a6121877cd55e695b3042336`.
- Signer/app ID/version compatibility matched TEST262: certificate `067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e`, app ID `com.charltonchris29.adhdpixeldungeon.indev`, version `4.0.8-apd-INDEV-bC266-TEST`; version code permits install-over.
- Cleric-lore marker and 57 required screenshots verified. CI/emulator evidence only; no physical-phone, owner-save, publication or distribution claim.
- TEST266 verified/staged. TEST264 and published TEST262 remain fallbacks. Build request unchanged.

## 2026-10-17T07:08:00Z — TEST266 UIMSD hidden-question consistency verified
- Fixed the confirmed Hidden-details transition mismatch in application commit `09ba71aab8fd2cb426679add9c9d0ed2e4d72fb7`: disabling hidden details removes only internal measure/split/condition selections and the answer path displays and executes one sanitized question.
- Full source `e678d38c4c34bc755ed0cd501f7f44c88bc3c465` / run `39011090870` passed 1,773 core methods in 350 suites, 38 API-34 methods in 14 suites, debug assembly and complete immediate-previous TEST264 history; zero failures/errors/skips. Both new `QuestionsTest` methods passed.
- All 57 required 320×640 captures decoded and were reviewed. APK: 38,592,446 bytes, SHA-256 `7ba0bf6d2d93fb2e4c7b27cf6caf718e3c74e960abfa0274d434c9869089674c`.
- Retained TEST265 run `39010922142` as a pre-compile history failure with no APK. Retained supplemental display `39011090883` as 4 passed/1 failed (M53 title-condition timeout).
- Stage `39011090988` refused the stale TEST264 profile on both attempts and produced no artifact/publication. Corrected the preparation profile to exact TEST266 evidence without editing the executable build request or rebuilding.
- TEST266 verified, staging pending. TEST264 remains staged fallback; TEST262 remains latest published. No phone/install-over/publication/distribution claim. Review ledger: 103 exact current production paths; broader audit remains incomplete.

## 2026-10-17T02:41:24Z — UIMSD dataset schema contract verified
- Completed a current-blob read of `Datasets.java` (`2dfad3e175044300e5cfcf9cea1326a5161a1274`) and exact catalog/navigation callers; no production defect confirmed.
- Added test-only `DatasetSchemaContractTest` at source `49c58cf175e6742f5499d9e1b243847eac4aad27`: two regressions cover dataset/view/link referential integrity and all 18 frozen custom-setting columns.
- Core-only run `38995668774` / request `5eb573d98fb154b13e6f7d35a4cd95aac740882d` passed 1,765 methods in 349 suites, 0 failures/errors/skips. Artifact `12086749353`, 827,712 bytes, digest `sha256:df6772b7fa738cb5c7bada7f799ce360ac2aee661ab179bc392387c701049ea8`.
- No production/app/APK/build-request/publication change. TEST263 stays staged; TEST262 stays latest published. Continue the next unreconciled current UIMSD component; full audit remains open.

## 2026-10-17T01:41:04Z — UIMSD optional-cache contention candidate closed
- Added test-only `CacheConcurrencyTest` at `759d8ef3cc55e2504fc902d71f6242876f3dc0f9`: 96 contested full-store operations across `DerivedStore` and `DatasetStore`, validating every result and final cache reuse.
- Retained the older deterministic low-level `RowCacheConcurrentWriterTest`; the new coverage exercises real raw-log parsing and complete independent store instances.
- Core-only run `38991720872` / head `49d21a1bf83cb33a4ab4cd7bb1573eae874ea9c7` passed 1,763 tests in 348 suites, zero failures/errors/skips. New methods: 2/2 passed in 0.093s.
- Artifact `12085507234`, 826,001 bytes, digest `sha256:3213dcc47b5cfea050807bc3e4b4cebd5ec16ad7fe67a542494011099e391d3d`.
- Production application and APK build request unchanged; no APK build/publication/phone claim. TEST263 staged and TEST262 published statuses are unchanged.
- Next safe step: reconcile later UIMSD per-file review records by current blob SHA, then inspect the first remaining changed component plus its integration callers without repeating completed cache/archive/reader/settings work.

## 2026-10-17T01:18:00Z — TEST263 APD Settings native-touch coverage completed
- Added real API-34 pointer coverage for the five APD Settings controls omitted by the misleading older “every control” test: Healing to Shielding, Enhanced Tile Visibility, Automatic Stair Navigation, Healing confirmation and Custom Dungeon Rules.
- Added a 19-field reflection ownership manifest binding every interactive `APDSettingsPane` field to a named native acceptance test; future unowned controls fail acceptance.
- Final test source `d1d3e359fcec05f064714c30784cc6d189192902` reused exact TEST263 application source/run/APK `e3b5cef026f8a9d6827d0eebb5528069c646b00d` / `38980948671` / `3e71c975ddc7f85f22e1f42454aa9e1a420164f8c9ed6c7e226f11e40a08c20d`; no application rebuild or build-request edit.
- Run `38988388102` native evidence: 2/2 passed, 0 failure/error/skip, `OK (2 tests)`, 8.582 seconds. Artifact `12085125334`, digest `sha256:49a6decb59c41e9169b094e0154903cfffaef76c871e0adc809d0dc823055d34`.
- Retained wrapper outcome: overall run cancelled at its 25-minute ceiling because qemu did not exit after successful tests; not labeled a green workflow and not an APD failure.
- Retained earlier harness failures `38987653291` and `38988017361` with artifacts `12082884529` and `12084300699`; neither is relabeled successful.
- Emulator only. No phone/install-over/publication/distribution claim. TEST263 remains staged; TEST262 remains published.

## 2026-10-16T23:48:13Z — TEST263 fully accepted and staged without rebuild
- Primary source `e3b5cef026f8a9d6827d0eebb5528069c646b00d` / run `38980948671`: 1,761 core, 38 API-34, 57 reviewed required PNGs, debug and immediate-previous TEST262 history all passed.
- APK: 38,591,540 bytes, SHA-256 `3e71c975ddc7f85f22e1f42454aa9e1a420164f8c9ed6c7e226f11e40a08c20d`; package/versionCode/certificate remain install-line compatible with TEST262.
- Test-only wrapper `5c9365c866c0c55aef75eb26608f10ead948b4cc` corrected stale 37-method staging metadata. Run `38982859447` succeeded and emitted staged artifact `12082335643` plus evidence `12081234943`; no application rebuild.
- Supplemental display `38980948747` attempt2 passed the two originally failed square/short-landscape jobs. Original failures remain retained; all five profiles have accepted same-APK evidence. Retry artifacts `12081653912` and `12081483056` were decoded and reviewed.
- Verified/staged is not published. TEST262 remains latest published owner APK. No phone/owner-save/public/automatic-distribution claim.

## 2026-10-16T23:42:01Z — TEST263 Grudge touch primary gate verified; retained supplemental failures
- Source `e3b5cef026f8a9d6827d0eebb5528069c646b00d` / run `38980948671`: 1,761 core and 38 API-34 methods passed with zero failure/error/skip; debug/history succeeded. All 57 required 320×640 images decoded and were contact-sheet reviewed.
- APK `ADHD-Pixel-Dungeon-CODEX-TEST-263.apk`: 38,591,540 bytes, SHA-256 `3e71c975ddc7f85f22e1f42454aa9e1a420164f8c9ed6c7e226f11e40a08c20d`; artifact `12081246935` digest `sha256:b58e6d0baa990111b509f6038c7721a31a3a6c98242c5ff9b3c10c3b79111dcb`. Android artifact `12081835229` digest `sha256:9a0f158907ca8f64ef43a3dfd8921397eb5f394b58d2cb8c28eaab179290f78f`; core artifact `12080698247` digest `sha256:d9702b578e25bacf88923846c1c2616c60bc3a1bd208b09c3e9f3b30ffc9c768`.
- Actual emulator touches verify Grudge entry, frequency, startup-location and safe reset-confirmation access; test restores preferences and never confirms reset.
- Supplemental display `38980948747` retained 3 passes and 2 failures (square TitleScene timeout; short-landscape overlap assertion); failed-only same-APK retry requested. Stage `38980948665` retained failure from stale 37-method preparation count. Corrected profile pins the already-built source and expects 38; no rebuild or publication claim.
- TEST262 remains latest published owner fallback. No phone/owner-save install-over, public release or automatic tester distribution.

## Grudge settings ignore taps — TEST263 source candidate, verification pending (2026-10-16 UTC)
The owner provided an actual screenshot of The Grudge above APD Settings and reported that nothing on the screen responds to taps, not a complaint about window dismissal. Source review found WndClericGrudge's scroll-area controller created after its RedButton/CheckBox hit areas, reproducing the same listener-order defect previously documented/fixed in WndAPDSettings. Candidate constructs ScrollPane before adding the controls without changing Grudge options, preference values, reset, history or other gameplay. New actual Android pointer regression opens the Grudge through settings, toggles frequency and startup-location and enters the reset-confirmation safely; always restores preferences without confirming destructive action. Full1761core/38API34/57PNG/debug/compiled-TEST262 history and additional displays are PENDING. TEST262 remains the only latest verified published owner APK. See docs/APD-GRUDGE-TOUCH-2026-10-16.md. No emulator pass, phone pass or delivery is claimed.

## 2026-10-16T21:58:10Z — TEST258 exact-display reconciliation; UIMSD Markdown defect bounded
- Reconciled actual receipts: published TEST258 source f2b19b2534ad9e701823012e1c84b31bebd9c225/run38968356419; corrected same-APK native run38969701536; exact1080×2408 run38971503819; publication run38972277806. Accepted evidence is1,752 core methods,37 distinct Android methods across43 executions and79 reviewed images. APK SHA-25637f38f0c6532c8a3b1357dd58747ae6a723b616992376ad6b0f6fcfcb76febc5.
- Retained exact-display failure38970664423 was Android's immersive education covering APD; test-only source1a4cede28c4906f59444c4701f773c3f72244f3c pre-confirmed only that system prompt and reused the exact signed APK. Initial publication38972186268 stopped before release creation on the deliberately retained37/1 before-state; corrected publication requires both that record and37/0 acceptance.
- Separate ReportRenderer review identified Markdown escaping order for backslash-plus-pipe values. The bounded display-only fix and regression are documented in docs/APD-UIMSD-MARKDOWN-ESCAPE-AUDIT-2026-10-16.md. No production correction or passing test is claimed; do not attribute it to TEST258.
- docs/APD-CODEX-BUILD-REQUEST.txt was not edited. No redundant build was triggered. Physical-phone/owner-save evidence and remaining backlog stay open.
## 2026-10-16T21:07:59Z — TEST255 verified repeated-label UIMSD comparison correction
- Reproduced the comparator defect in supported core code: two distinct rows both labeled `Average` should show group B values20 and200; the first instead borrowed200. Baseline commit `df6d2a4f0e898f52c0e1227ed19c7c71b23b2c63` / run38964285358 retained1failure/0errors in artifact12073194262 (ZIP SHA-256 `137c7a8b59b13049b65e0bee815c57069668bb0bb1f69dcb67d95479d7ea9669`). The diagnostic wrapper also failed its literal-message check and remains failed.
- Source `a11b32f8067765e6229830c929d07d262de34800` pairs same-schema non-section rows by definition order instead of treating display labels as unique map keys. Missing counterparts remain unknown. One permanent core regression and the history entry are retained.
- Full run38964629976 succeeded 2026-10-16T20:46:15Z–21:01:40Z:1,737 core and36 API34, zero failures/errors/skips; debug assembly and complete immediate-previous TEST254 history passed. All49 320×640 captures were manually reviewed; no blank/corrupt or obvious clipped-regression frame.
- Artifacts: core12074118076 digest `sha256:c3edd523764e186eac6b6dbd5a30d3f6914f5eb2b9bf1efaaea356148972147b`; APK12073883782 digest `sha256:8d19fa3cd704e5ab6500ee8b4e11fca7dd0d22d83fbdeb306212179c6f0ed21b`; Android12073874781 digest `sha256:38a7f49e8a8facd2b4ba07ab1de85b5e3b7ba30578568fe8526b38cb11fbd1ba`. APK `ADHD-Pixel-Dungeon-CODEX-TEST-255.apk`,38,611,585 bytes, SHA-256 `71f0f1d77a0952f9fca121ba5e7ac31bf3488900023109dfe9bcea5812da32d5`.
- Staging run38964630161 failed before packaging/publication in its initial attempt and failed-job retry: stale preparation artifacts produced unequal application trees and `apd_existing_build_evidence.py` line67 rejected `Publication commit changes application/build inputs`. Packaging/publication skipped; do not rerun unchanged.
- TEST255 is verified only as a private workflow artifact. It was not published or distributed; TEST254 remains current published owner APK. No physical-phone, owner-save or full human boss/class playthrough claim. Raw history/saves,18 sliders, active357-value v2.6R scoring, original scores, gameplay, RNG and turns unchanged.

## 2026-10-15T10:47:30Z — TEST204 pre-gate movement boundary correction
- Review before CI found that an entry hook could count a same-cell self-trample or an immediately blocked route as a movement command.
- Moved recording to the first successful `getCloser` step. Failed/same-cell actions emit nothing; a multi-step route still emits exactly once through its transient action-local guard.
- Expected count and complete TEST204 gate are unchanged. Candidate pending; no build, delivery or physical-phone claim.

## 2026-10-15T10:40:47Z — TEST204 movement-command telemetry candidate
- Reproduced the movement omission at the real action boundary: the shipped `move.step.travelling` field is true for every ordinary physical step and therefore cannot distinguish an adjacent manual step from point-to-point travel. Its historical values remain unchanged; its description is corrected.
- Additive telemetry 1.15 introduces public `move.command`, emitted once when a `HeroAction.Move` begins and tagged `step`, `travel` or `stairs`, with destination cell and floor width. The transient action-local guard prevents one multi-step route from producing duplicate commands.
- Exact-source review found no general auto-explore mechanic in this game version, so no fictional mode or hook was added. Movement, path selection, interruptions, saves, RNG, turns, scoring and permanent statistics are unchanged.
- Added two retained core regressions plus wiring, manifest, telemetry-version and immutable-history updates. TEST204 requests 1,619 core tests, debug assembly, complete immediate-previous compiled TEST202/Codex TEST 198 history, 27 API34 methods and 35 PNGs.
- Candidate pending. TEST202 remains the verified compiled fallback; TEST123 remains owner-delivered/signature-compared. No delivery or physical-phone claim.

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


## 2026-10-15T00:08:33Z — TEST180 verified UIMSD parameterized-dialog recreation coverage
- Exact source `acf8592e2db3c30684e09a05bd22c097c0d5f077`; run `38656873159` ran 2026-10-14T23:54:11Z–2026-10-15T00:06:25Z and concluded success.
- Core artifact `11937556865` / ZIP SHA-256 `3e33f34ed08a3905d67d97652a25fa627dff485aa08134686af4afad8fa1fa2b`: 1,602 tests, 0 failures/errors. APK artifact `11937113364` / ZIP SHA-256 `2445194a97a19a118ff453f214a849dd24488fcddd529e5e0d0743a05383dde7`: APK SHA-256 `279f6691874d32a892fc2743afe5946b3bdf371f315372b7541ffec646ab8332`; checksum/debug/immediate-previous compiled TEST179 history passed.
- Android artifact `11937448955` / ZIP SHA-256 `a9638818a3f7afacbeaa05596fc1fb3b8875e3c06d1320220e2da1f64dff46b9`: 26 API34 methods, 0 failures/errors/skips; the strengthened Column order/Move column method passed in 8.33 seconds.
- 34 PNGs decoded at 320×640 and passed contact-sheet/focused history review.
- Column order and Move column now have retained exact-once recreation coverage and the restored Make first callback is exercised. Production behavior was unchanged.
- Delivery was skipped; emulator evidence is not a physical-phone claim. TEST123 remains owner-delivered fallback. Do not rebuild unchanged TEST180.


## 2026-10-15T00:12:37Z — TEST181 UIMSD scope-wording candidate
- Re-read the verified TEST180 head and reproduced the documented empty-Home ambiguity in production source: the Rules control changes rule sets, while unfinished runs are a separate Tables > Options switch.
- Added a pure Home empty-state message that names finished runs under other rules separately from runs still going, and routed the native Home through it. Options now explicitly says Rules does not change finished status; the Home scope note names runs still going among exclusions.
- Added one retained regression method covering an empty history, a finished abandoned run under different rules, singular wording, the unfinished-run route, and all-rules inclusion. Expected total: 1,603 core tests.
- Exact-source core/debug/immediate-previous TEST180 history/26 API34/34 PNG verification is pending. This is wording/query-scope UI only: no raw history, save, statistics, scoring, gameplay, application ID or signing behavior changed. No delivery or physical-phone claim; TEST123 remains owner-delivered fallback.

## 2026-10-15T00:15:34Z — TEST181 history guard failure; TEST182 correction pending
- TEST181 source `ba146f3c0ec2e145fa57295af2c398248f497d2a`, run `38658816290`, stopped before compilation at the reviewed-history guard. It reported one omitted path: `docs/APD-TEST179-UIMSD-COLUMN-RESTORE-2026-10-14.md`, added by the intervening TEST180 evidence commit.
- No core, debug or Android tests ran; TEST181 is not verified and produced no APK. The UIMSD application/test source is unchanged for TEST182.
- TEST182 adds the omitted existing documentation path to the complete comparison manifest and requests the full 1,603-core/debug/immediate-previous TEST180 history/26 API34/34 PNG gate. No delivery or physical-phone claim.

## 2026-10-15T00:30:48Z — TEST182 verified UIMSD rule/status scope wording
- Exact source `eed8bb85dfb32cffded60a08614cf3223362fb50`, run `38659050102` (2026-10-15T00:16:09Z–00:29:24Z), concluded success.
- Core artifact `11938107765` / ZIP SHA-256 `df2c8d34889cb9f387b97cfabeaebc962445613d191e73a1edffa3cb9c44567f`: 1,603 tests, 0 failures/errors/skips. The new `emptyHomeSeparatesRulesFromRunStatus` method passed in 0.139 seconds.
- APK artifact `11938012933` / ZIP SHA-256 `7bb738e8efa77e6b462e720fee3c0a41d6d91fc7b6b482cb7236e6f1cbdec6d1`: APK SHA-256 `ed13da85121f7cd13feeb751050912beba9e09f47f4b6c6867670cfc7f90e7df`; debug and immediate-previous TEST180 history passed. The compiled Changes ledger contains TEST182, then TEST180 and TEST179; TEST181 correctly has no compiled record because it stopped before compilation.
- Android artifact `11937863231` / ZIP SHA-256 `aabb3a0ac3246d7573dfbc9d1eef90893653ddd5c6c67c9820ed3244fc2a58ce`: 26 API34 tests passed. All 34 screenshots decoded at 320×640; contact-sheet and focused UIMSD Home/APD Changes review found no blank, corrupt or visibly clipped image.
- Verified behavior: Home and Options distinguish finished runs under other rules from runs still going; an abandoned run remains a finished result. No raw-history/save/statistics/scoring/gameplay/identity/signing behavior changed.
- Private delivery skipped; emulator evidence is not a physical-phone claim. TEST123 remains last owner-delivered/signature-compared. Do not rebuild unchanged TEST182.

## 2026-10-15T00:42:18.901Z — TEST183 native empty-Home regression candidate
- Re-read verified TEST182 and found its API34 suite opened the real first-run Home but did not wait for the empty model, assert the rendered rule/status wording or retain a screenshot before switching to demo data.
- Strengthened the existing native method to require an empty fresh-emulator Home, inspect the actual rendered text for “No finished runs yet” and the dedicated Tables > Options route, reject the obsolete Rules/run-status conflation, and retain `uimsd-empty-home.png`.
- Production gameplay, UIMSD query/model, raw history, saves, statistics, scoring, application identity and signing are unchanged.
- TEST183 requests 1,603 core tests, debug assembly, immediate-previous TEST182 history, 26 API34 methods and 35 valid screenshots. Candidate only until hosted evidence passes; no delivery or physical-phone claim. TEST123 remains owner-delivered fallback.


## 2026-10-15T00:54:48.241Z — TEST183 API34 fixture failure; TEST184 correction pending
- TEST183 source `68bea4b4d57aef872cab684e13f134b91f2d258c`, run `38661559728` (00:42:30Z–00:53:13Z), passed 1,603 core tests, debug assembly and immediate-previous TEST182 history. APK SHA-256 `05140836f2274dd4d560476369282a746eeb3518e4c39347f7cccf979f8965e8`.
- API34 completed 26 methods with 25 pass / 1 fail: the strengthened method assumed the shared emulator still had no finished history, but earlier device methods intentionally created finished-run fixtures. The empty-Home assertion failed before its screenshot; TEST183 is not verified or delivered.
- TEST184 changes only the device fixture: render a controlled in-memory empty Home through the real scene, assert/capture it, then restore the original cache without deleting or rewriting history.
- Exact-source TEST184 requests 1,603 core/debug/immediate-previous compiled TEST183 history/26 API34/35 PNG. Production gameplay/UIMSD model/save/statistics/scoring/identity/signing remain unchanged; no delivery or physical-phone claim.


## 2026-10-15T01:08:28.843Z — TEST184 native checks passed; TEST185 visual-retention correction
- TEST184 source `e883710fa759a41a1ffa7754f42c021f0f8121ec`, run `38662728056` (00:55:01Z–01:06:10Z), passed 1,603 core tests, debug assembly, immediate-previous compiled TEST183 history and all 26 API34 methods. APK SHA-256 `a6b121104b14d64dfa2576d10efa3772383dd500d55e4ddf880c9cd0ba950203`.
- The controlled empty-Home assertion passed and logcat records creation of `uimsd-empty-home.png`, but the evidence script's explicit list retained only the prior 34 images. The requested new screenshot could not be reviewed, so TEST184 is not the completed 35-PNG checkpoint.
- TEST185 adds that one filename to the runner's pull and PNG validator lists. Exact-source verification requests 1,603 core/debug/immediate-previous compiled TEST184 history/26 API34/35 PNG.
- Production gameplay/UIMSD model/save/statistics/scoring/identity/signing remain unchanged. No delivery or physical-phone claim; TEST182 remains the last fully verified fallback and TEST123 the last owner-delivered APK.


## 2026-10-15T01:12:37.816Z — TEST185 push produced no run; TEST186 request
- Branch commit `80506e4deb7be7a7ed1fcbbd4ee37fac17972329` contains the one-line-per-list evidence-script correction and an updated `docs/APD-CODEX-BUILD-REQUEST.txt`, but GitHub created no workflow run or check suite for that exact source during this invocation.
- No TEST185 build, core tests, emulator tests, screenshots or APK exist; do not infer completion from the branch commit.
- TEST186 re-records the exact request while keeping immediate-previous compiled TEST184 as the comparison base: 1,603 core/debug/TEST184 history/26 API34/35 PNG.
- Production source remains unchanged. No delivery or physical-phone claim; TEST182 remains the last fully verified fallback and TEST123 the last owner-delivered APK.


## 2026-10-15T01:29:34.915Z — TEST185 verified after reconciled request
- The initial TEST185 request commit `80506e4deb7be7a7ed1fcbbd4ee37fac17972329` produced no Actions run or check suite. Reconciled request source `40d31d81de4c2fcc1ae154e9ec0d1c8f6a28d984` then compiled under the workflow's actual `Codex TEST 185` label; there is no separate TEST186 APK.
- Hosted run [38664406486](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38664406486) ran from 2026-10-15T01:12:48Z through 2026-10-15T01:25:32Z and passed 1,603 core tests, debug assembly, immediate-previous compiled TEST184 history, and all 26 API34 methods with zero failures/errors/skips.
- Downloaded artifacts: core `11940232030`, APK `11940212006`, Android evidence `11941165163`. APK SHA-256 is `cc3c211a67d179f703e0d468ef54efdb5d62b1eec99f44cb54ececed2a519c1e`.
- The Android artifact contains all 35 expected 320x640 PNGs. The complete contact sheet was reviewed; the native empty-History Home is readable and correctly separates finished runs from the Tables > Options route for runs still going, and APD Changes shows the immediate TEST184/TEST185 sequence without clipping or corruption.
- TEST183's shared-emulator fixture failure and TEST184's omitted screenshot-pull entry remain recorded as failed/incomplete checkpoints. TEST185 is the latest fully verified source checkpoint. No application production code, gameplay, raw history, save, permanent statistics, scoring, application ID or signing identity changed in this evidence batch.
- This is hosted build/emulator evidence, not physical-phone or owner-save verification. No APK was distributed or published; TEST123 remains the last owner-delivered/signature-compared APK. Do not rebuild unchanged TEST185. Continue the next distinct approved post-slider UIMSD reliability/backlog task.


## 2026-10-15T01:40:02.195Z — TEST186 UIMSD preparing-tour Back correction pending
- Re-read verified TEST185 and reproduced a navigation no-op in source: `onBackPressed()` returned while `tutorialPreparing`, so Android Back and the visible exit control could not cancel sample generation/loading.
- Changed that branch to end the preparing tour, return to Home, and advance `tourGeneration`; the stale worker therefore cannot publish after cancellation. Existing serialized sample cleanup and real-history guards remain intact.
- Strengthened `AndroidBackDeviceTest` to start a real sample preparation, cancel it in the preparing state, require immediate main-model/Home restoration and require the worker generation to be invalidated before the existing normal-tour Back flow.
- TEST186 exact-source verification requests 1,603 core tests, debug assembly, immediate-previous compiled TEST185 history, all 26 API34 methods and 35 screenshots. Candidate is pending until hosted evidence passes.
- No gameplay, scoring, raw history, save, permanent statistics, identity or signing behavior changed. No delivery or physical-phone claim; TEST185 remains the verified fallback and TEST123 the owner-delivered fallback.


## 2026-10-15T01:42:48.021Z — TEST186 history-guard failure; TEST187 correction pending
- TEST186 source `c1e09d72b48cdd8e534f93818cb763ade84407c9`, run [38666897410](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38666897410) (01:40:13Z–01:41:01Z), stopped before compilation because the reviewed-history manifest omitted `docs/APD-TEST183-UIMSD-EMPTY-HOME-NATIVE-2026-10-15.md` from the intervening TEST185 evidence commit.
- No core tests, APK, Android tests or screenshots were produced; TEST186 is not verified. The exact failure is retained in `docs/APD-TEST186-UIMSD-TOUR-CANCEL-2026-10-15.md`.
- TEST187 adds only the omitted existing evidence path. UIMSD application and Android regression source remain unchanged; verify 1,603 core/debug/immediate-previous compiled TEST185 history/26 API34/35 PNG.
- No gameplay/save/statistics/scoring/identity/signing/delivery/physical-phone change. TEST185 remains the verified fallback and TEST123 the owner-delivered fallback.


## 2026-10-15T01:57:45.673Z — TEST187 passed configured gate but omitted changed Back class; TEST188 pending
- TEST187 source `44cf2b195229f8909021ed9998f65aba52acb7ce`, run [38667136514](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38667136514) (01:42:55Z–01:55:00Z), passed 1,603 core tests, debug assembly, immediate-previous compiled TEST185 history, 26 configured API34 methods and 35 validated 320x640 screenshots. APK SHA-256 `e75bb0598f051ea818964010447168a18779190149713cf6e40db16cf05f80ed`.
- Downloaded XML audit showed `AndroidBackDeviceTest` was not in the runner's explicit class list. The new regression compiled but did not execute; do not call TEST187 verification of the Back fix.
- TEST188 adds the retained class to the API34 command, increasing the required count to 27 while keeping application/test source unchanged. Compare against the immediate previous compiled TEST187 APK and retain the existing 35 screenshots.
- No delivery or physical-phone claim. TEST185 remains the last fully verified fallback; TEST123 remains the owner-delivered/signature-compared fallback.

## 2026-10-15T02:13:47.780Z — TEST188 ran the retained Back class; fixture corrected for TEST189
- TEST188 source `e251bb7a760db8274babdeb3e5356425853670d1`, run [38668463084](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38668463084) (01:57:54Z–02:10:57Z), passed 1,603 core tests, debug assembly and immediate-previous compiled TEST187 history. API34 executed all 27 configured methods: 26 passed and 1 failed.
- Exact failure: `AndroidBackDeviceTest.backWalksBackwardsOneStepPerPress` timed out at line 138 waiting for UIMSD, before any preparation-cancellation assertion. The test called public `UimsdScene.open`; the private test build intentionally enables `demoUnlock`, so fresh settings correctly routed to the unlock gate.
- The Android artifact `11942149631` retained one XML with 27 cases and all 35 validated 320x640 screenshots. APK artifact `11942373362` was built, but the failed API34 job means TEST188 is not accepted and is not delivered.
- TEST189 changes only the device fixture to call `openNow` after establishing TitleScene, isolating the scene-navigation contract without disabling or bypassing the production gate. Re-run 1,603 core/debug/immediate-previous compiled TEST188 history/27 API34/35 PNG.
- Production UIMSD, gameplay, saves, raw/permanent statistics, scoring, application ID, signing and player data are unchanged. No physical-phone or delivery claim; TEST185 remains the fully verified fallback and TEST123 the owner-delivered fallback.

## 2026-10-15T02:29:53.163Z — TEST189 verified Back cancellation during UIMSD tour preparation
- Source `ea1062079c6a43aadaf80dbf0197f3fcff4769cc`, run [38669986563](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38669986563), ran 02:15:04Z–02:28:04Z and completed successfully.
- Downloaded evidence: 309 core XML reports with 1,603 tests, and one API34 XML with 27 tests; both have zero failures, errors and skips. Debug assembly and the immediate-previous compiled TEST188 history comparison passed.
- The API34 XML explicitly includes `AndroidBackDeviceTest.backWalksBackwardsOneStepPerPress` at 28.019 seconds. Its log records cancellation during `tutorialPreparing`, worker-generation invalidation, the retained completed-tour flow, and final return to Title.
- All 35 expected screenshots are present at 320x640 and the full contact sheet was visually reviewed clean. APD Changes retains TEST189, TEST188, TEST187 and TEST185 in order.
- Artifacts: core `11944026123` / ZIP `187283af105130e4a9d5f5a6fca77cd5fe4afdebb4ff8f17e47f01a2c8223cf9`; APK `11944046061` / ZIP `9e6891dc870fae042fe5b4b93af40e2515f35c7654617994a87a0362a97d8bd0`; Android `11943957689` / ZIP `16c9ab986a64416a4d0d0e7c522a9ce84a5c7fa097373b38863d606146139692`.
- APK checksum file passed; APK SHA-256 is `f4f45c841f359530a81818364ae0d79d4989078477103555e26c5d747e05b4c6`.
- TEST186's precompile history omission, TEST187's omitted Back class and TEST188's pre-tour fixture failure remain recorded and are not relabeled as passes. TEST189 is the verified hosted/emulator checkpoint.
- No APK was delivered and no physical phone was used. TEST123 remains the last owner-delivered/signature-compared APK. No application build is pending; do not rebuild unchanged TEST189.


## 2026-10-15T02:40:14.718Z — TEST190 SavedViews cross-instance store candidate
- Confirmed the fixed `saved_views.txt.tmp` path was shared across independent store objects without a shared lock.
- Added a narrow shared atomic-write lock plus one deterministic retained concurrency regression. Verification pending: 1,604 core/debug/TEST189 history/27 API34/35 PNG.
- No raw-history, save-format, gameplay, score, signing, application-ID, delivery or physical-phone change.


## 2026-10-15T02:58:31.520Z — TEST190 hosted execution retained
- Source `97c4848581e2f7fcd979d3965e257bdae59c5a93` / run `38672320647` passed complete core/debug/immediate-previous TEST189 history and 27/27 API34; runner listed 35/35 PNGs at 320×640.
- Artifact IDs/digests: core `11944263722`/`7b8a3adf…6504d`; APK `11944698529`/`09e02464…5f93`; Android `11945087238`/`5152ec87…7431`.
- Private payload extraction/contact-sheet review was unavailable, so TEST190 is not yet called the fully reviewed checkpoint. Reuse artifacts; no unchanged rebuild. TEST189 remains fully reviewed and TEST123 owner-delivered.


## 2026-10-15T03:41:39Z — TEST191 archive wrapper lifetime candidate
- Confirmed exporter/importer left owned ZIP wrappers unclosed, deferring inflater/deflater cleanup to garbage collection although callers separately closed document-provider streams.
- Added non-closing adapters, closed ZIP wrappers on every path, and retained two regressions. Caller streams remain open; archive format and additive history semantics are unchanged.
- Verification pending: 1,606 core/debug/immediate-previous compiled TEST190 history/27 API34/35 PNG. No delivery or physical-phone claim.


## 2026-10-15T04:00:07Z — TEST191 archive wrapper correction verified
- Source `7e6cae8900c97eaa920bbe8baebb657ec5e539be` / run `38677900668` passed 1,606 core tests (310 XML) and 27 API34 tests, zero failures/errors/skips, plus debug assembly and immediate-previous compiled TEST190 history.
- Both retained stream-ownership tests passed. All 35 expected screenshots are 320×640 and the full contact sheet reviewed clean. APK SHA-256: `b88b4ddc71f12abaac8d216d3c553968577b254b9f55f3ecff2522ef0046329b`.
- The correction deterministically releases ZIP wrappers while preserving caller-owned picker streams. No archive format, history, save, score, gameplay, identity or signing change. No delivery/physical-phone claim; TEST123 remains owner-delivered.


## 2026-10-15T04:38:01Z — TEST192 streaming run-log reader candidate
- Confirmed ordinary `RunLogReader.read(File)` duplicated the complete file in memory before parsing. A controlled sparse-tail fixture isolates the resource exposure without claiming an observed player failure.
- Normal file reads now parse the bounded header and one checksum-validated frame at a time; explicit resynchronization continues using the existing random-access byte-array parser.
- Added two retained tests for valid-log parser parity and a 1 GiB sparse corrupt tail that must stop after the first bad byte while preserving file-length/damaged-byte accounting.
- Full 1,608-core/debug/immediate-previous TEST191 history/27 API34/35 PNG verification is pending. No format, history, save, score, gameplay, identity, signing, delivery or physical-phone change.


## 2026-10-15T04:57:30Z — TEST192 streaming run-log reader verified
- Source `222189f971dc55597f561eb87aadcd4e34b47a2e` / run `38682901119` passed 1,608 core tests (311 XML) and 27 API34 tests, zero failures/errors/skips, plus debug assembly and immediate-previous compiled TEST191 history.
- Both retained streaming-reader regressions passed. All 35 expected screenshots are 320×640 and the full contact sheet reviewed clean. APK SHA-256: `129626b1bd0a65453c207c9fa5d06075a048b2e4d63e30ee034d0e1b15ca846f`.
- Ordinary file reads now avoid a whole-file raw-byte copy while retaining bounded frame validation and existing explicit-resync behavior. No format, history, save, score, gameplay, identity or signing change. No delivery/physical-phone claim; TEST123 remains owner-delivered.


## 2026-10-15T05:44:52Z — TEST193 fixture failure retained; TEST194 pending

- Re-read the old tutorial floor-1 hidden-door backlog entry and current source. The production repair already exists in `RegularLevel.restoreFromBundle()` and is limited to saved entrance-room boundary secret doors; the entry was stale, not a current source omission.
- Added a retained boundary regression and a core-only CI gate so unchanged application source is not rebuilt.
- TEST193 source `34eb3d9e25177d4bff8a754c2387cabe8ef84a10`, run `38688177613`: 1,609 executed, 1,608 passed, 1 fixture failure. The synthetic level omitted `traps`, and `Level.set` threw before the repair assertion. Artifact `11950004576`, ZIP SHA-256 `184531a6e5c7f03a2f55e650cb3728df29a134dfbb3a482bc08ba98d9466fe85`. No APK.
- TEST194 initializes the fixture's normal trap/blob collections; production application source and assertions are unchanged. Complete 1,609-core verification pending.


## 2026-10-15T05:49:32Z — TEST194 fixture geometry failure retained; TEST195 pending

- TEST194 source `0fa7763a36e4ebc4994629910cab6d285d3d0af5`, run `38688555523`: 1,609 executed, 1,608 passed, 1 fixture failure. The synthetic entrance was one cell from the map border, violating the interior margin used by generated levels; `updateOpenSpace` failed before repair assertions.
- Artifact `11951230755`, ZIP SHA-256 `4228cd0eabadd0eb9391b83c9b3458e00a844dd9db3372f559c919f04e0f93e6`. No APK.
- TEST195 changes only test geometry to the normal interior margin. Full 1,609-core verification pending; production application source remains TEST192.


## 2026-10-15T05:54:02Z — TEST195 verified; stale legacy-door backlog item closed

- Exact source `f7049868cd7648b29c7606b1ade3c9c8e8de0fdd`, core-only run `38688955099` (05:50:19–05:53:03 UTC): 1,609 tests / 312 XML suites / 0 failures / 0 errors / 0 skips.
- The retained method verifies the existing saved-floor-1 repair exposes only an entrance-boundary hidden door, rebuilds passable/secret flags, and leaves an unrelated secret door and ordinary entrance wall unchanged.
- Artifact `11951151594`, downloaded ZIP SHA-256 `20a37fe25701527b8be522b3168f3beae4622c4ce27b413dcd24adc47ccef35d`.
- TEST193 run `38688177613` and TEST194 run `38688555523` remain recorded as fixture-only failures; neither implicated production and neither built an APK.
- Corrected the stale source-guide instruction that advised abandoning/searching. Physical owner-save acceptance remains unverified; never delete or abandon a save for this repair.
- No application source changed and no APK was rebuilt. TEST192 remains latest compiled application evidence; TEST123 remains owner-delivered/signature-compared.
- Next: items 4–7 in the old ordered list are owner approval/phone/decision blocked. Continue the first approved unblocked canonical recording candidate (levers) after current-source reproduction.


## 2026-10-15T06:35:47Z — TEST196 NPC interaction telemetry candidate

- Verified the queued lever candidate does not correspond to a mechanic in current source; did not invent an event or gameplay path.
- Added additive public telemetry 1.12 fact `npc.interact` after the central hero interaction accepts an NPC interaction. It records only the displayed NPC name and floor; quest-state telemetry is unchanged.
- Added a retained central-path regression covering accepted exactly-once recording and rejected-interaction omission.
- Full 1,610-core/debug/immediate-previous TEST192 history/27 API34/35 PNG gate pending. No delivery or physical-phone claim.


## 2026-10-15T06:40:00Z — TEST196 failed three new integration contracts; TEST197 pending

- Source `37da2dcd7ef3512402c2d8622be73e075d2278ba`, run `38693568938`: 1,610 core executed, 1,607 passed, three failures; no APK.
- Retained failures: Hero's sixth recorder hook was not added to `PlayWiringTest`; telemetry 1.12 lacked its permanent records-history milestone; the new test entered `Hero.ready()` and hit the absent headless `AttackIndicator` before interaction.
- TEST197 updates the two contracts and isolates the exact call-plus-record unit behind the real central path for headless execution. Assertions and recorder guards are not weakened.
- Failed core artifact `11952904481`, ZIP SHA-256 `7a11eab5b211a9190a02fc28920f2bd1de1ca718d94cab7f65944539aa270f34`. Full gate pending.


## 2026-10-15T06:55:59Z — TEST197 canonical NPC interaction telemetry verified

- Source `364fd4724b79308fb6a13478a211bcd7599b2f7b` / run `38694008236` passed 1,610 core tests (313 XML), debug assembly, complete immediate-previous TEST192 APK history, 27 API34 methods and 35 screenshots, with zero failure/error/skip.
- All screenshots are 320×640; full contact sheet reviewed clean. Inner APK SHA-256 `8fc77119f6f16193a4fb42ca539b53bbb8d75e26c23c1112a6708b51a2615beb`.
- Artifact IDs/ZIP hashes: core `11953586184` / `3f2fdb0c…8cb24`; APK `11953825841` / `312b0e82…53377`; Android `11954147276` / `8f18ea2b…ce1df`.
- The workflow named the compiled APK Codex TEST 194; source/evidence remains TEST197. TEST196's three contract failures remain recorded and were not relabelled.
- Additive public telemetry 1.12 NPC interaction recording is verified. No gameplay/save/permanent-statistics/scoring/custom-rule/identity/signing change; no physical phone or distribution. TEST123 remains owner-delivered.


## 2026-10-15T07:35:03Z — TEST198 shop-stock visibility boundary candidate
- Confirmed existing canonical recording: a currently visible, seen, non-hidden `FOR_SALE` heap is tagged `SEEN_SHOP`; generation alone is not called “seen.”
- Added a retained real-item regression for an unidentified +4 cursed ring. Public history must contain only its displayed unidentified state; true class/level/curse remain INTERNAL.
- Request 1,611 complete core tests through the core-only gate. No APK rebuild because production application source is unchanged from verified TEST197.
- No gameplay, RNG, save, telemetry format, scoring, custom-rule, application-ID, signing, delivery or physical-phone change.


## 2026-10-15T07:41:19Z — TEST198 shop-stock visibility boundary gate passed
- Source `bed2aa8513e1ab7ac5b307f8a04d9f0fda9a2d33` / run `38700157769` completed the core-only job successfully; Gradle reported `BUILD SUCCESSFUL`.
- The retained regression requires visible shop stock to be tagged `SEEN_SHOP` exactly once without leaking unidentified ring class, level or curse into PUBLIC history.
- Core artifact `11955827287`, size 745,760 bytes, GitHub digest `2a0d77bf965610aefb181a6d12355697f39b8a86a449e6cbcc6e7818f5e3d2a1`.
- Requested suite count 1,611 derives from verified TEST197's 1,610 plus one new method; independent XML recount is pending because artifact host downloads returned repeated HTTP 502.
- No application source or APK changed. TEST197 remains compiled verification; TEST123 remains owner-delivered/signature-compared.


## 2026-10-15T08:36:50Z — TEST199 ally-summon recording candidate
- Reproduced a concrete omission after TEST198: explicit offensive creator ownership already survived summon creation and save/load, but run history emitted no summon fact.
- Telemetry 1.13 adds public `ally.summon` at that existing creation boundary. It records only the displayed summoned name, displayed actual creator name and current floor.
- A summon is recorded only when ownership first becomes PLAYER and its actual creator is the hero or another creature currently visible to the hero. Repeated inheritance, enemy, boss, unattributed, off-screen-creator and restore-time paths emit no public summon event.
- Added two retained regressions for exact-once direct/chained player summons, actual creator identity, exclusions and restoration suppression. TEST199 requests 1,613 core tests, debug assembly, complete history from immediate-previous compiled TEST197/Codex TEST 194, 27 API34 methods and 35 PNGs.
- No damage amount, turn, RNG, actor save metadata, scoring, custom-rule, application ID or signing behavior changed. Candidate pending; no delivery or physical-phone claim. TEST197 remains the verified compiled fallback and TEST123 the owner-delivered fallback.


## 2026-10-15T08:48:52Z — TEST199 fixture assertion failure retained; TEST200 pending
- Source `5a937f93e2858bbc85990db2d96642c2ef4764f1`, run `38707847596`: 1,613 core executed, 1,612 passed, one fixture failure; no APK and no API34 run.
- Failure: `TelemetryVersionTest.parsesAndPrints` supplied `1.12` while accidentally expecting `1.13`. Production telemetry 1.13 self-test and ally-summon regressions passed.
- History preservation/validation passed. Failed artifact `11958859521`, 752,523 bytes, GitHub SHA-256 `8dcf551329e74d6758ae50ee6cb0f302f7892bdfb15c003e9389f55c169dc8f4`.
- TEST200 changes only the erroneous expected value and requests the unchanged full 1,613-core/debug/TEST197-history/27-API34/35-PNG gate.


## 2026-10-15T09:05:04Z — TEST200 ally-summon telemetry verified
- Source `094f6fc168c2c46057648542d68da4537739ec9f`, run `38708477712` (08:49:54Z–09:03:04Z): 1,613 core / 314 XML suites and 27 API34 methods, zero failures/errors/skips; debug assembly and immediate-previous compiled TEST197/Codex TEST 194 history passed.
- Both retained ally-summon tests and all telemetry-version tests passed. The internal Codex TEST 196 APK SHA-256 is `5fe49a5e2167dd688c35fcbc04b31e18951873fa42caae2ea645f42039e4c646`.
- Artifacts: core `11959253024` / `1c5e5084b05df08fbdef8bff4fee72d85926b90b3d5c4e5a1998761293fe7b17`; APK `11959178061` / `e93ab8c59a32331207cacce595e0535cea31972a9dc7fd817567f61dcd14f09c`; Android `11959678617` / `dfdc05d475811335ec68b606d5ae1d0e5eeaa54be3e98bdc88aef93530d49ca6`.
- All 35 screenshots are valid 320×640 PNGs; full contact sheet reviewed clean. Private delivery skipped; no physical-phone claim. TEST123 remains owner-delivered/signature-compared. Do not rebuild unchanged TEST200.


## 2026-10-15T09:14:47Z — TEST201 visible alchemy details candidate
- Reproduced a concrete gap: the successful-brew path retained INTERNAL ingredient→product lineage but had no PUBLIC/queryable craft fact.
- Added additive telemetry 1.14 `alchemy.craft` at the successful result boundary. Public data is limited to displayed ingredient/result names, their quantities, floor, total energy, main-pool energy and toolkit energy.
- Failed brews and restore-time reconstruction emit nothing; true unidentified classes remain INTERNAL. Existing lineage, recipes, inventory mutation, energy consumption, RNG and save behavior are unchanged.
- Added two core regressions and updated the wiring guard, permanent fact manifest, telemetry version test and immutable records history. Full 1,615-core/debug/immediate-previous compiled TEST200/Codex TEST 196 history/27-API34/35-PNG gate pending.
- No scoring, custom-rule, application-ID, signing, distribution or physical-phone claim. TEST200 remains the verified compiled fallback; TEST123 remains owner-delivered/signature-compared.


## 2026-10-15T09:21:06Z — TEST201 failed records-history contract; TEST202 pending
- Source `776910414b7574b42cffa0d61e9bb7d4f1e83ca7`, run `38711834653`: 1,615 core executed, 1,614 passed, one contract failure; no APK and no API34 run.
- Failure: the telemetry 1.14 milestone was inserted before the required `APD records history, format 2` first line. The alchemy production code and both new behavioral/privacy regressions compiled and passed.
- History preservation/validation passed. Failed artifact `11960867915`, 752,666 bytes, SHA-256 `e040a5f4330624fd74a0739cf914eb553d34957dcda5d8464acb39f9e6f8befe`.
- TEST202 moves only the milestone below the immutable preamble and requests the unchanged full 1,615-core/debug/TEST200-history/27-API34/35-PNG gate.


## 2026-10-15T09:36:13Z — TEST202 visible alchemy telemetry verified
- Source `186f5892429ef564ec4a9d59c9885a62fb6f1c1f` / run `38712411656` passed 1,615 core tests in 315 XML suites and 27 API34 methods with zero failures/errors/skips; debug assembly and complete immediate-previous compiled TEST200/Codex TEST 196 history passed.
- Both alchemy recorder regressions and all wiring/manifest/version/history contracts passed. Internal Codex TEST 198 APK SHA-256 is `6dd8a5e16e13670e6dfb6abd12783dd7225ef8ed936b12b57f1192b51618936d`.
- Artifacts: core `11960394313` / `639308a8a783b67fc69319092ce802272be2d1ba8a0b8e207cd07796f6e5d1f0`; APK `11960578879` / `f1498ed29f368055d4910a63ba67d6dff77321bdbb6de67387384083ff1d3170`; Android `11962595029` / `ebe2d31192960ccc23d2f2363da1932a0d5255070cf8173556314a2d2e782b5b`.
- All 35 screenshots are valid 320×640 PNGs; full contact sheet reviewed clean. Private delivery skipped; no physical-phone claim. TEST123 remains owner-delivered/signature-compared.
- Telemetry 1.14 alchemy craft details are verified. Do not rebuild unchanged TEST202.


## 2026-10-15T09:46:19Z — TEST203 level-feeling coverage gate pending
- Exact-source review found that all seven non-neutral level feelings are already public and queryable through telemetry 1.9 `journal.landmark`: the same `GameScene` branches that show each feeling add its matching `Notes.Landmark.*_FLOOR`, and `Notes.add` records only the first insertion.
- Added two retained tests covering all seven exact subject/depth/action facts, duplicate suppression, and the complete display→landmark wiring map. No new fact kind or telemetry bump was warranted.
- TEST203 is core-only: 1,617 tests expected. Production application source and verified TEST202/Codex TEST 198 APK evidence are unchanged; APK rebuild is forbidden. Candidate pending, with no delivery or physical-phone claim.


## 2026-10-15T09:53:26Z — TEST203 level-feeling coverage verified
- Commit `b72e122fbb2e7de02bc03ea42b607197f6105f8a` / run `38715813806` passed 1,617 core tests in 315 XML suites, zero failures/errors/skips. Both exact-once feeling-landmark and display-wiring regressions passed.
- Core artifact `11962917529`, 750,461 bytes, SHA-256 `054cf9b8b999940a9e9628725a5d3a91f983206dc62606a406a7344b32954265`.
- No production application source changed and no APK was built. Verified TEST202/Codex TEST 198 remains the compiled baseline; TEST123 remains owner-delivered/signature-compared.
- The level-feeling candidate is closed as already covered by `journal.landmark`; next is the separate auto-explore-versus-travel omission after reproduction.

## 2026-10-15T11:39:34Z — TEST204 corrected movement-command telemetry verified
- Exact corrected source `9549d3638de133cb09864e1c325f15b325c550b2` / run [38722796011](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38722796011) (10:47:51Z–11:11:39Z) passed 1,619 core tests in 317 XML suites and 27 API34 methods with zero failures/errors/skips; debug assembly and complete immediate-previous compiled TEST202/Codex TEST 198 history also passed.
- Internal Codex TEST 200 APK SHA-256 is `c43197a32b7adb25be11024324fd7e749214d0a32675857660cf6613b471bd5c`. Artifacts: core `11966324986` / GitHub digest `8ad428cde6079454debc7613603594d39220d479d6d1aa2520b7e8c3dd099b13`; APK `11966934665` / `3d4c47bccfcb52e74cd8e6b724b24799c137919a835aa7942515445ea26bd5a4`; Android `11968491460` / `7c809e33c16f230be33e151c0b9af8050effb0c5c782b7efad792155e68c9816`.
- All 35 screenshots are valid 320×640 PNGs and the full contact sheet was reviewed clean. The earlier successful run `38722338971` covered pre-correction source `fe79acbc87d84438a8428d81a43849643844baea`; it is superseded by the exact corrected-source gate above and is not the retained TEST204 candidate.
- Public telemetry 1.15 now records successful step, travel and stair movement commands once per action after the first successful physical step; blocked, failed and same-cell actions emit nothing. Existing `move.step` values remain unchanged and current source still has no general auto-explore mechanic.
- No private distribution or physical-phone claim was made for TEST204. The separately delivered APK remains Codex TEST198 from exact compiled TEST202 source. Do not rebuild unchanged TEST204; continue the next reproduced canonical omission or an independent test-only gate.

## 2026-10-15T11:44:13Z — expanded Shocking derived-damage regression verified
- Commit `63dbbf9d5bf28d567c036322994d67184a38511c` / core-only run [38729005866](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38729005866) (11:40:28Z–11:42:56Z) passed 1,620 core tests in 318 XML suites with zero failures, errors or skips.
- The retained real-`Shocking` method covers all 20 settings for each actual creator role (player, enemy and boss; 60 cases). The already-scaled primary attack remains unchanged, one nearby splash retains `Shocking` as its source, and derived damage uses the matching player/enemy/boss slider exactly once.
- Core artifact `11968778024`, 754,862 bytes, GitHub digest `412fb0f9ad04b7f11b7d7a74c1f839e6fd2580ac8b5a0424ed75e373a0bec953`; independent XML recount confirmed the totals above.
- This was test-only: no production source or APK changed. Do not rebuild unchanged TEST204; retain exact corrected TEST204/Codex TEST 200 as the latest verified compiled candidate. Codex TEST198 remains the separately delivered APK; no physical-phone claim.

## 2026-10-15T12:36:31Z — TEST205 visible boss-phase telemetry candidate
- Reproduced the next canonical omission: Goo, Tengu, DM-300, Dwarf King and Yog expose explicit visible phase boundaries, but no public/queryable fact recorded those transitions.
- Telemetry 1.16 adds `boss.phase` with only the displayed boss name, boss-specific phase number, current floor and visible state label. Hooks run after the real transition assignment; restore-time reconstruction and implicit initial encounter state emit nothing.
- Goo records enraged; Tengu arena; DM-300 each supercharged and returned normal/final state; Dwarf King shielded and final; Yog fist phases and final. No AI target, cooldown, hidden summon choice, HP threshold, RNG result or undiscovered state is recorded.
- Added two retained recorder/privacy regressions plus exact source-wiring counts for all five bosses, the permanent fact manifest, telemetry-version contract and immutable records history.
- TEST205 requests 1,622 core tests, debug assembly, complete immediate-previous compiled TEST204/Codex TEST 200 history, 27 API34 methods and 35 valid 320×640 PNGs. Candidate pending; TEST204 remains the verified compiled fallback and Codex TEST198 the separately delivered APK. No physical-phone claim.


## 2026-10-15T12:53:55Z — TEST205 visible boss-phase telemetry verified
- Exact source `a5071e47270ef3953a85eecc50f285d617f96fb3` / run [38736265505](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38736265505) (12:39:04Z–12:51:17Z) passed 1,622 core tests in 319 XML suites and 27 API34 methods in 12 XML suites, with zero failures, errors or skips. Debug assembly and the complete immediate-previous compiled TEST204/Codex TEST 200 history also passed.
- Internal Codex TEST 201 APK SHA-256 is `42075e16015fe677f1baf20bf946d60dddf2018143007950cdbf90ed63b4f692`. Artifacts: core `11974261597` / GitHub digest `d608738cfaff2c5adde2cdb8234114035fe73b169a83063068633a504af50655`; APK `11974785994` / `b1173b1e81674f17d0b03a2705e62c1bcc0fe7a39885d17ede3f3a4c730aa474`; Android `11974253206` / `ea3dd7b3cdb58ddd6b4f7879f9dc4fbf2e3441dbb49c4307509db2a5545584cd`.
- All 35 screenshots decode as 320×640 PNGs and the full contact sheet was manually reviewed clean. This is API34 emulator evidence only; no physical-phone claim.
- Public telemetry 1.16 now records the visible phase boundaries for Goo, Tengu, DM-300, Dwarf King and Yog after the real transition. Initial encounter state and restore-time reconstruction emit nothing; no hidden thresholds, cooldowns, targets, summon choices or RNG results are exposed.
- Private delivery remained skipped. Do not rebuild unchanged TEST205. Codex TEST198 remains the separately delivered APK and TEST123 remains the last owner-delivered/signature-compared APK. Continue the next independently reproduced canonical telemetry omission or approved acceptance item.


## 2026-10-15T13:42:07Z — source-bound UIMSD row cache verified
- Exact workflow head `cef68f0f80b1dd0c3e1660e53e1c0a0dad2e9e7e` / run [38742449533](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38742449533) (13:24:39Z–13:38:06Z) passed 1,626 core tests in 320 XML suites and 27 API34 methods in 12 XML suites, with zero failures, errors or skips. Debug assembly and complete immediate-previous compiled TEST205/Codex TEST 201 history also passed.
- Internal Codex TEST 202 APK SHA-256 is `cab516793cedf03ca90b04e6de778def9cb8fd1525d8e86bf98859cf47b7b519`. Artifacts: core `11975569462` / GitHub digest `debd3057f8d03abd61683314f517d5d7e77cdbf648e24fe20e1041f0695bc1d3`; APK `11976650506` / `37675d2513f8e1cc88aa4f95fb06c2b519130cc79e5b26aee431860c370898ba`; Android `11978246008` / `e29b5ad73656ca31b0c0a2050193df4dfc269fff692a0d3a47d9d715fba4141a`.
- All 35 screenshots decode as 320×640 PNGs and the complete contact sheet was manually reviewed clean. Private delivery remained skipped; this is API34 emulator evidence only, not a physical-phone result.
- Verified behavior: row caches are bound to raw-source SHA-256, equal-name/length/timestamp replacement exposes the replacement facts, unchanged raw data remains reusable, legacy format-2 caches rebuild once, and malformed/unbound/mismatched identities are rejected. Raw history, saves, scoring and gameplay are unchanged.
- Do not rebuild this unchanged row-cache source. Newer Cleric-lore commits at the live branch head were preserved and are outside run 38742449533; this evidence must not be used to claim they were tested. Codex TEST198 remains separately delivered and TEST123 remains owner-delivered/signature-compared.


## 2026-10-15 — conservative aggregate-bound correction verified as native TEST206
- Exact compiled source `f33591d364598f75b07d62394f5a47bf6ba6432e` / workflow [38754451109](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38754451109), started 2026-10-15T14:46:51Z, completed successfully. Independent artifact recount found 1,636 core tests in 324 suites and 28 API34 tests in 12 suites, with zero failures, errors or skips; the emulator XML timestamp is 2026-10-15T14:58:54Z.
- Debug assembly and the complete immediate-previous compiled TEST205 history passed. Internal Codex TEST206 APK SHA-256: `566e29174c17c19f9a19f7429410d872a097c09c2aa4a1e4835548617a6c809c`.
- Artifacts: core `11982772591` / GitHub digest `2f7c5cd63801552ef932d5c476bb88a43feb443d3a663e336c5645ba2d2d8006`; APK `11982687643` / `158cc8779378cd1d7268416697706fb2f29fed35a9d5078548d4a3c8962fa598`; Android `11983063448` / `56329f954587e00a1abfee8a54b635688ebd60dc2262e2afc0b643797ecbc13a`. The independently downloaded Android ZIP matched its GitHub digest.
- All 38 screenshots decode as 320×640 PNGs; the complete contact sheet was manually reviewed clean. This is API34 emulator evidence, not physical-phone or owner-save evidence.
- Verified behavior: exact mean/median values retain nearest rounding; lower-bound mean/median values floor at the existing precision so a known minimum 2.46 cannot display as >=2.5. Fixed-point mean/median/custom-question totals and rate numerators display in player units. Raw statistics, histories, caches, saves, scores, slider limits, gameplay and RNG are unchanged.
- Private delivery was skipped. TEST206 is a source-verified candidate only; owner-delivered TEST204 remains the playable fallback. No rebuild or publication is required for these documentation-only records.


## 2026-10-15 — mixed-scale RATIO presentation candidate
- Reproduced raw 250 hundredths / raw 1000 thousandths displaying as 25% (250/1000) although the player-facing values are 2.50 / 1.0 = 250%.
- Added a presentation-only CellFormatter correction and one retained regression covering both scale directions, same-scale behavior, compact text and unchanged raw AggValue.
- QueryEngine mathematics, aggregate ordering, rows, raw history, caches, saves, scores, sliders, turns, RNG and gameplay remain unchanged.
- Full 1,637-core/debug/immediate-previous TEST206-history/28-API34/38-PNG verification is pending. No release or physical-phone claim; delivered TEST204 remains fallback.


## TEST207 CI gate completed — 2026-10-15T15:53:04Z — mixed-scale RATIO correction CI-green, artifact review pending
- Exact source `f0b0cf7708a8d6af63cdb69392b22ab9e3962717` / workflow [38762160592](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38762160592) completed successfully. Complete core, debug assembly, and immediate-previous compiled TEST206 history steps passed. API34 actually finished 28/28 methods with no reported failure or skip; all 38 logged PNGs are 320×640.
- Artifacts: core `11986616803` / 767,862 bytes / GitHub digest `99465f5c3a257239586e58e13545d0f732645209bab642d492bb73aeda94bcb3`; APK `11986731648` / 36,059,570 bytes / `9951c5d863d26788675334b57ee6c6c1b2ea9ec7fa795b6f04efe1aeb2ed2c26`; Android `11988085278` / 3,225,355 bytes / `2628f834219cef1fe9bd891025edb62d52d8fa540bde7645f03d220a79af4024`.
- A local compiler harness passed three corrected mixed/same-scale cases and unchanged raw values; the exact previous TEST206 formatter reproduced the failure: expected `250% (2.50/1.0)`, got `25% (250/1000)`.
- Repeated artifact-byte downloads failed with HTTP 502. Therefore the expected 1,637 core count was not independently recounted from XML, the APK payload SHA-256 was not extracted, and the full screenshot sheet was not manually reviewed in this execution. Do not upgrade this to fully artifact-reviewed or published status. TEST204 remains owner-delivered fallback; TEST208 is separate newer source. No physical-phone claim.


## 2026-10-15T16:38:34Z — damaged-history provenance candidate
- Corrected run-fact and item/enemy/event-row count provenance so surviving prefixes from TRUNCATED, CORRUPT and SEAL_MISMATCH logs are lower bounds, not exact counts.
- Preserved exact provenance for clean UNSEALED logs and valid SEALED logs with unrelated trailing bytes.
- Advanced disposable derived-cache identities only: RowBuilders 12→13 and DerivedStore 8→9. No raw history, save, score, rules, slider, gameplay or RNG change.
- Added six retained methods covering both builder families and both cache-version contracts. The full TEST209 gate is pending; required result is 1,645 core/debug/complete TEST208 history/28 API34/38 valid PNGs. No result, publication or phone claim is made in advance.


## 2026-10-15T16:42:29Z — pre-gate history manifest correction
- Replaced the shortened nonexistent mixed-scale report path with the exact repository path `docs/APD-MIXED-SCALE-RATIO-DISPLAY-2026-10-15.md`.
- A workflow pinned to `f08f5908c53e40c058a767eb4e526731cdce252b`, if created, is not acceptable as the complete-history gate. The corrected head requires a fresh full result; no application source changed.


## 2026-10-15T16:43:57Z — exact predecessor-tree history reconciled
- Direct comparison from compiled TEST208 source found and added both lunch workflow files and the lunch publication report to the next native history.
- The history manifest now names all 13 changed paths between TEST208 and this candidate. Earlier pinned candidate runs are not acceptable as complete-history evidence; no application source changed.


## Authoritative commit-time correction
- Replaced three preparation-time labels with GitHub commit timestamps: `f08f5908` 2026-10-15T16:38:34Z, `6f511a22` 2026-10-15T16:42:29Z, and `fcc100ba` 2026-10-15T16:43:57Z.
- No application source changed. Only a full gate pinned to this final descendant can satisfy the requirement that the corrected handoff and ledger be embedded in reviewed native history.


## 2026-10-15T17:42:00Z — TEST215 catalogue keeper correction pending
- Reproduced that a stale keeper instance can persist an older correctly signed catalogue after a newer instance has already stored a higher sequence, which can undo revocations. Separate instances also target one temporary filename without a shared replacement lock.
- Candidate serializes the compare-and-replace section, re-verifies persisted state inside it, and rejects stale/equal sequences. Two retained regressions cover the original replay and deterministic concurrent replacement.
- Full 1,652-core/debug/TEST214-history/28-API34/38-PNG gate is pending. No online service was enabled and no player history, save, score, slider, gameplay, RNG, signing or application ID changed.


## 2026-10-15T17:53:26Z — TEST215 catalogue keeper correction verified
- Source `0507127f47426405a72ca83d0eccf04e1ce04752` / run [38779254822](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38779254822) passed 1,652 core tests in 326 suites, debug assembly and complete TEST214 history; 28 API34 methods and 38 valid 320x640 PNGs also passed with zero failures/errors/skips.
- Both retained cross-instance catalogue regressions passed. APK payload SHA-256 `0d0dced41e6b541f07b65bb6feb71fa17775d22327890c94cbff6deb77730bed`; no publication, signing comparison or physical-phone claim.
- This closes the reproduced stale-replay/shared-temp defect without online activation or changes to gameplay, player history, saves, scoring, custom rules or RNG. TEST214 remains the signing-compatible staged alpha candidate; TEST206 remains published owner fallback.

## 2026-10-15T18:40:09Z — TEST216 candidate: stale UIMSD load handoff
- Reproduced by deterministic monitor ordering that an older `UimsdModel.load` worker could overwrite a newer explicit catalogue and its READY view state.
- Added a generation guard that retires stale success/failure completions and a retained regression in `UimsdModelTest`.
- Candidate full gate requested: 1,653 core tests, debug APK, complete immediate-previous TEST215 history, 28 API34 methods and 38 valid screenshots. Pending until actual workflow evidence passes; no publication/delivery/phone claim.

## 2026-10-15T18:58:49Z — TEST216 verified: stale UIMSD load handoff
- Exact source `ae923b21414a03364bbb4f8701f15f54f1b9f62b` / run [38788151697](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38788151697) passed 1,653 core tests in 326 suites, debug/history against immediate previous TEST215, 28 API34 methods and 38 valid 320×640 PNGs; zero failures/errors/skips.
- Artifacts: core `11998935748` / `80dc81246f4a71379bacd15ae21effe558672a3746e2471719d61f639ebd3b12`; APK `11999155639` / `550c89d3d64a4d1b3d4a6b55af61fd44f2023d5e0eca9c294638f10a5e369cef`; Android `11998882355` / `be3bb92e47995792ffcc9dfcf3808fc7889ef96cacc05d48a0fd99691489f4c1`.
- APK payload `4a50572c1b94232fb44b71f9e97680db43ac4d5ee1fdc03e324e52c98e54194c`. Delivery skipped; no signature/owner-phone claim. The generation guard is verified without raw-history, cache-format, save, score, custom-rule, gameplay, turn or RNG changes.

## 2026-10-15T19:32:37Z — TEST217 candidate: stale seed-replay handoff
- Reproduced with deterministic latches that an old `startSeedFloors` worker could replace newer catalogue/view state and send stale notifications.
- Added an in-memory replay generation, invalidation on newer catalogue/load/provider ownership, an awaitable worker handoff and one retained regression.
- Requested one full gate: 1,654 core tests, debug APK, complete immediate-previous TEST216 history, 28 API34 methods and 38 valid screenshots. Pending; no publication/delivery/phone claim.

## 2026-10-15T19:48:25Z — TEST217 verified — 2026-10-15T19:48:25Z
- Exact source `d2c28cb076d9918632d313e5a4f9655d36c9433b` / run [38795254303](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38795254303) passed 1,654 core tests in 326 suites, debug assembly and complete immediate-previous TEST216 history; 28 API34 methods and 38 valid 320×640 PNGs passed with zero failures/errors/skips, and the complete contact sheet was reviewed readable.
- Core artifact `12000959379` / `9f1c30afee684d7cbf0192ee40e75845e086f8c5ccf621a119831f42033d459f`; APK/history `12001119270` / `0322280cdbe6e0f9e995c8940eee308353f149b889df1433d86b264b4d40fc56`; Android `12002566209` / `c15d8a4b3f4c0639b534fc2cb5ddaaac6be0a7de65f21202fbaa796d03e1663b`.
- APK payload SHA-256 `4db6d38ad6ff0a6a5e375c43fadfa990b52172b2b14aac4bb2dafce9d9e6a6e9`. Private delivery skipped; no publication, signing comparison, owner-device install or physical-phone claim. TEST214 remains signing-compatible staged and TEST206 published fallback.
- Older seed-replay workers can no longer replace newer READY catalogue/view state or emit stale failure/success notifications. Replay algorithms, histories, saves, scores, all 18 rules, gameplay, turns and RNG remain unchanged.

## 2026-10-15T20:39:32Z — TEST218 all-control acceptance and plain-text export candidate — 2026-10-15T20:39:32Z
- Dedicated API34 [run 38802091366](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38802091366) reused exact verified TEST217 source `d2c28cb076d9918632d313e5a4f9655d36c9433b` / APK `4db6d38ad6ff0a6a5e375c43fadfa990b52172b2b14aac4bb2dafce9d9e6a6e9` and passed one native method: all 18 controls accepted minimum and maximum touchscreen input, saved, reopened at the exact values, and left the active run and earned score unchanged.
- Acceptance artifact `12005075844` / SHA-256 `a89053e414966efdd622f0b2f8edffca7b8eec72b2237b063c47656f6d9bd228`; instrumentation reported `OK (1 test)`, zero failures/errors/skips. This is emulator evidence, not physical-phone evidence.
- Subsequent source labels the first column in plain-text table exports and adds two retained core acceptance methods covering all 18 recorded values through every numeric operator, combined filters, run details, comparisons, Markdown/text/HTML/CSV exports, historical unknowns, unchanged earned scores and unavailable unapproved multipliers.
- Because the renderer changed application source after the reused-APK acceptance run, TEST218 requests a fresh 1,656-core/debug/complete immediate-previous TEST217 history/29-API34/38-PNG gate. Candidate pending; do not publish or claim signing/phone acceptance. TEST214 remains signing-compatible staged and TEST206 published fallback.

## 2026-10-15T20:57:52Z — TEST218 verified — 2026-10-15T20:57:52Z
- Exact source `2210d59e1924fdf5ff56ecfb41c25ee0b2c707d0` / [run 38804215957](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38804215957) passed 1,656 core tests in 327 suites, debug assembly and complete immediate-previous TEST217 history; 29 API34 methods and 38 valid 320×640 PNGs passed with zero failures/errors/skips, and the complete contact sheet was reviewed readable.
- Core artifact `12005990913` / `510449c48e56a8adbedf45d9d24c7a9b0a2df947e4216024accdf18d3615b509`; APK/history `12006690105` / `ac73a551ffebcc2d4abaecfa35f18fed0e63d9b16841f61f161ef786af2f28be`; Android `12006182839` / `978a840e8949558462e2ed1713fe5a2bbb07b3666433adeea258d93adc2ef48e`.
- APK payload SHA-256 `184f35fc0371d07d860365f903825d37a0d759d0192e36bb2627cf8d98d04898`. Private delivery skipped; no publication, signing comparison, owner-device install or physical-phone claim. TEST214 remains signing-compatible staged and TEST206 published fallback.
- Every one of the 18 controls now has native min/max touch-save-reopen coverage and recorded-field query/detail/comparison/export coverage. Plain-text first-column labels are verified. Slider values, historical unknowns, earned scores, gameplay, saves, turns and RNG remain unchanged.
- Source after `2210d59e`, including the separate healing-conversion and authored-language work present at evidence-recording head `6e8e41ee3bba9be20c8211f39083c0b523ed3f41`, is not covered by TEST218 and must keep its own evidence boundary.

## TEST222 actually delivered — October 15, 2026, 5:37:51 p.m. EDT
Private release https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/tag/apd-codex-test-222 was actually published2026-10-15T21:37:51Z by successful publication-only run38811277370. Direct APK https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/download/apd-codex-test-222/ADHD-Pixel-Dungeon-CODEX-TEST-222.apk ;38,562,563bytes; actual GitHub asset digest and independently staged checksum both c4fe922f32c7b07f94666ae01591a04384fa5c801afcb4f869aca9a5c0e6aea5.
Compiledsource182155b7e2906357fa3006ae3af70a7e472bb0bb/run38809007695 passed1668unique core and31unique API34 checks withzero failures/errors/skips; debugassembly and exactpreceding produced219history passed. Independently downloaded/parsing core12008096258 SHA9064d43f8f55bc90d1bec2e60e8b09dd966f9f11902ec658949dd356cd17c7f4 and Android12007713589 SHA257da9ade47fbdbf5dfbbf03201ac3190fd5e3419eeb2715945d9eed4af3fe9e confirmedcounts and39valid320x640PNG; fullsheet and Healing-only settings screenshot reviewed readable.
Staging38809125187 successfully checked APK/signature/applicationID, reports, sourcearchive and screenshots, but its first publication attempt targeting the older compiledcommit received403. Publication-only retry reused staged artifact12007743612 without rebuilding and directly rechecked signature/package/hash againstpublished206. Certificate067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e; applicationcom.charltonchris29.adhdpixeldungeon.indev; version4.0.8-apd-INDEV-bC222-TEST/code920. Install-over compatible; neveruninstall/cleardata. No physicalphone/owner-save acceptanceclaim.
Release tag points to real deliverywrapper69b869ab3f1d92631f3fe75d8767c6cce081cc55, NOT compiledsource. TEST222-Delivery.json explicitly records the later authored-language source difference notinAPK; attachedAPD-Verified-Source.zip SHA9cb6c6e7bd5c69647c9ec5f7b9ff423f27c03d564dafc3a4ce75266b08cb558f pins actualcompiledcode. No synthetic Git history or main overwrite. Receipt asset SHA70e6377568f805f81abc0f22405b65a352fa22c5f56a87b2e0ec44f1cf8d1d35. PDF APD-Bug-Fixes-2026-10-15-TEST222.pdf SHA03f2349258f47d8706f6d2c8bdd56e025c6bb93c353b7b7db97150f17bc5d6d6. All oldreleases/assets including206retained.
Includes Healing-to-Shielding-only conversion floor, clearlabel, unchangedothertrinketconversions/RNG, nonzero correctedrulescope, cleanAbout and first25authoredalternatives. Does NOT include the later64-alternative localized expansion; not the tester-distribution candidate.
TEST223 sourcecf7a46191d7758f73d4901e6781eb44664f4a8d4/run38810321509 contains the localized expansion and one newguard; require1669core/31API34/39PNG/full222history before verification. Local9language+7unchangedisolationmethods pass. No gameplay,18rules,save/rawformat,stats,scoringorRNG changes.
API36/Android16 targeted acceptance run38811530072 reuses exactowner222APK and compiles onlyinstrumentation: four existing methods for all18nativecontrols,Healinglabel,AboutOFF/ON/OFF andplay/save/continue/end. Pending; these are repeatmethods onanotherOS, notfournewuniqueAndroidtests. PhysicalRedMagic remainsuntested. Continue launchverification; broadUIMSDaudit parked. No automatic10-testerdistribution.


## TEST223 verified localized clean-language follow-up — 2026-10-15T21:45:24Z
- Exact compiled source `cf7a46191d7758f73d4901e6781eb44664f4a8d4` / [run 38810321509](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38810321509) completed successfully: independently recounted 1,669 core tests in 329 suites, debug assembly and complete immediate-previous TEST222 history; 31 API34 methods and 39 valid 320×640 PNGs passed with zero failures/errors/skips. The complete contact sheet was reviewed readable.
- Artifacts: core `12007738438` / SHA-256 `22c1012716c7811bcd6fffbef139da6af70754f3443d600fcdba96be9f86b1f9`; APK/history `12007868173` / `bccbedb25d5a18487cf6e3f17c55f95958af32a06214e79fbfc2514d5faee610`; Android `12009021953` / `1c87d8a35c618534dc401995591e1a26d00f60649be36dad3f4be69802b0604a`.
- APK payload SHA-256 `757aed989f65c7f79d2afddb3486c18cde3bd5f548dc8bce64bce1b50d5e7155` (38,579,630 bytes). Private delivery was skipped; TEST222 remains the actual owner-published APK. No signing comparison, physical-phone claim or tester distribution for TEST223.
- This verifies the 64 reviewed localized clean alternatives and retained resource guard while preserving adult originals, player input, all 50 Cleric lore entries, gameplay, saves, raw history, earned scores, RNG and all 18 Custom Dungeon Rules. Later Android16-only acceptance workflows/tests at branch heads after this compiled source remain separate and cannot be credited to TEST223.


## Player-facing release notes candidate — October 15, 2026
Prepared a separate APD Alpha Preview catalogue with14 concise native sprite popups, default APD branding and existing SPD two-column/gold presentation. Versions > Build records preserves every original technical entry and ID; technical headings are neutral, with raw labels only inside explicit debug records. No public release/date/version is invented; no gameplay/save/score/rules/signing change. Added seven core and one Android methods while retaining both prior archive methods. Local18Python tests pass; source sprite validation14/14. Full1676core/debug/exactprevious producedTEST223history/32API34/40PNG gate pending. Java compilation and screenshots are not yet verified. No publication/tester distribution. Preserve newer Android16 workflow/input-fixture commits; the older local disk-cleanup commit was not applied. See docs/APD-PLAYER-RELEASE-NOTES-2026-10-15.md.

## 2026-10-15T23:31:37Z — Android 16 acceptance completed on the exact TEST222 APK

- Definitive [API 36 run 38823387859](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38823387859), test/workflow source `6263a361b1128a23a335cef837cf64278598064e`, reused exact compiled application source `182155b7e2906357fa3006ae3af70a7e472bb0bb` / TEST222 run `38809007695` / APK SHA-256 `c4fe922f32c7b07f94666ae01591a04384fa5c801afcb4f869aca9a5c0e6aea5`. The app APK was not rebuilt.
- The generated result records API level 36, 4 tests / 4 passed / 0 failures / 0 errors / 0 skipped. Isolated emulator processes passed: all 18 Custom Dungeon Rules controls at both minimum and maximum through Android `MotionEvent` dispatch, save/reopen, unchanged active-run rules and scores; exact Healing-to-Shielding setting scope; Adult Humor/About OFF/ON/OFF behavior; and a real run played, saved, continued and ended with history recorded.
- Artifact `12014545035`, 356,781 bytes, ZIP digest `sha256:0c2115b09fb453637c4bf0995a7a20701ec8bcc1b13ae4ad1f7148d1edd84a1b`. Downloaded evidence contains `all-controls-result.json`, exact application identity, four zero exit files and four `OK (1 test)` logs.
- Separate [cold-restart run 38819508203](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38819508203) passed 2/2 API 36 methods around a real `am force-stop`: Adult Humor OFF persisted and About remained clean before and after restart. Artifact `12012676845`, 471,682 bytes, digest `sha256:8954f79964568b6a8759095dcb5a2a2f24298e2c2e1525498f7972fb182dfe16`.
- Actual failed attempts retained: `38811530072`, `38812366597`, `38817907750` and `38817907562` ran zero tests because the Google APIs API36 AVD required 7,372.80 MB with only about 7,038.7 MB available. Run `38819508147` passed Healing/About but Android 16 rejected `Instrumentation.sendPointerSync`; the following play/save check was contaminated. Run `38820283602` ran zero tests because the action runner split a multiline shell function. Run `38820926816` passed Healing/About but did not route global input to the first slider and exposed a fresh-process `Document` initialization defect in the test fixture. Run `38821605202` passed Healing/About but still missed the activity display and the play/save method stopped at a nonportable pixel-render wait. Run `38822288426` stopped at test compilation because public `MotionEvent` lacks `setDisplayId`. Run `38822652603` actually passed all four methods; its workflow conclusion was a false negative because over-escaped `printf` wrote literal `0\\n` to each exit file. No failed attempt is counted as successful acceptance.
- Workflow/test-only commit chain: `24d0ef1dd470e1a5d7674bff723fbd444c692e94`, `511694dfcee90932edb924e1123496ed96570f89`, `22a127f61d0510a1aecd03a440b95d84da4c26b1`, `e47cef5ba27bb0cd3cecff575505caf41627b8ea`, `fd82d4ac672fc5b6efe5e4bcd789fafa078dffd5`, `f491891c96e9aaa15831596b2a52ce7faca191f0`, `77ff53e711bb283d099f6f00fba8b76fe87dee61`, `2f369274fe43792de15fc05a7ca6c835e32fba9b`, `fd20de670942f7f5ec9cf4f3c8665e05f80658a2`, `1226030b5a6ed130024e339920b3bcd94c811363`, `e922789cb92fcdc1c2b9d7250f471767bf055787`, then validator correction `6263a361b1128a23a335cef837cf64278598064e`. These change workflows/instrumentation only, not the TEST222 application payload.
- Scope: emulator/CI evidence only, not a physical-phone claim. No APK publication, tester distribution, player-data deletion, scoring change, save/raw-history migration, application-ID/signing change, or modification to the 18 production controls occurred. TEST222 remains the owner-published fallback. Newer application commit `3e886234e69ab128a861cfbc17bca549ba3a3456` is preserved but is outside this TEST222 evidence and requires its own normal exact-source gate; do not attribute it to TEST222 or rebuild TEST222 merely for this acceptance. Next safe step is to reconcile that newer source with the current build request and verify it once, while physical-device acceptance remains unresolved. Unmeasured durations remain unknown.



## Android16 cold-restart image evidence correction — October 15, 2026
Independently downloaded and hash-checked run38819508203 artifact12012676845 (SHA8954f79964568b6a8759095dcb5a2a2f24298e2c2e1525498f7972fb182dfe16): both forced-process-restart phases passed on actualSDK36, but manual image inspection found Android's first-use fullscreen tip covering the upper About content. Preserve the two mechanical passes; do not call those captures unobstructed visual evidence. The cold-restart workflow now sets only the disposable emulator's immersive confirmation preference before launch and reruns the same verified TEST222 APK, unchanged fixture, force-stop and assertions. No app rebuild, save/history change or physical-phone claim. TEST224 source3e886234 is separately compiling/testing its own changelog presentation; core1676/1676 independently recounted, Android32/40PNG pending.


## 2026-10-15T23:43:46Z — TEST224 gate passed and private alpha package staged

- Exact application source `3e886234e69ab128a861cfbc17bca549ba3a3456` completed [run 38823705331](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38823705331) successfully at 2026-10-15T23:42:33Z. Independent staging [run 38825223981](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38825223981) downloaded the exact three source-run artifacts and parsed their XML: 1,676 core methods in 330 suites and 32 API34 emulator methods, with zero failures, errors or skips. Debug assembly and complete immediate-previous produced TEST223 history passed.
- Source-run artifacts are core `12014675265` / ZIP SHA-256 `320bdba5953021b76b37125218ecde675f5e2f459d39c5b1e74a650aafe069a8`; APK/history `12014445635` / `b7c02921c3af7d8d0336171135f016fee638c2663d9cf4d71e4adeadbb869af1`; Android `12014511869` / `c94bd35a5086ab036e49ad2262904daaaaa131cba5bf8d405baaca235b84bc51`.
- The API34 log finished all 32 methods and the validator enumerated exactly 40 PNGs at 320×640. Staging copied all 40 and wrote their checksums. This execution could not independently download the artifact through the connector because its returned object-store URL repeatedly returned HTTP 502, so it did not manually inspect the complete TEST224 sheet; do not invent that review. Retry artifact retrieval before calling the full sheet manually reviewed.
- APK payload `ADHD-Pixel-Dungeon-CODEX-TEST-224.apk` is 4.0.8-apd-INDEV-bC224-TEST, SHA-256 `8d88ef02cb82af29cb2bb0aefc1a847726d7e95310768be59263ee78c8cb2579`, application ID `com.charltonchris29.adhdpixeldungeon.indev`. Its V2 certificate SHA-256 `067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e` matches TEST222; the version code permits install-over. The APK explicitly contains the private `testBuild`, `grudge` and `clericPlayable` flags.
- Staging produced `staged-alpha-preview224` artifact `12014956420` / ZIP SHA-256 `d65581ad8b0d52b50a9f7ed6aaf1173eae27a1faec2e7f5eb8cfbf0d0299ad57` and evidence artifact `12014816550` / `3b2db93839eb63a56cae54c9ae485e72ee169270a016bdf149e52d945495cceb`, including the exact APK, verified-source archive, 40 screenshots, checksums, inventory and PDF. Staging rebuilt neither the application nor APK.
- Separate corrected API36 cold-restart [run 38824287616](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38824287616) reused the exact TEST222 APK and passed 2/2 methods around a real force-stop; artifact `12014606742`, 497,047 bytes, ZIP SHA-256 `bdfe10d25bd297d90e3487dbca9bb48c6896899f71687a50ea7a4dcf0e0b16e7`. The prior first-use fullscreen-tip obstruction was removed on the disposable emulator; the repository's acceptance record documents independent hash and unobstructed-image review. This remains emulator evidence only.
- No release or tester distribution occurred. TEST222 remains the owner-published fallback; TEST224 is staged only. No player data was cleared, no scoring/rules/gameplay/save/RNG/signing/application-ID behavior changed in this evidence/staging batch. Next safe step: retry and manually review the TEST224 40-image sheet, then perform owner-approved physical-device/install-over/save-preservation acceptance. Do not rebuild unchanged source. Unmeasured durations remain unknown.


## 2026-10-15T23:47:25Z — documentation descendant stopped by history guard

Documentation commit `1d7a8f14a055f84b2cd3f78761433378b436f985` automatically started run [38825603109](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38825603109). The complete-history guard correctly stopped before core tests, APK assembly or API34 because the post-TEST224 staging files `.github/workflows/codex-alpha-preview-stage.yml` and `docs/APD-EARLY-ALPHA-ACCEPTANCE-2026-10-16.md` are not yet described in the next build manifest. This is not an application failure and produced no TEST225 APK. Preserve those two paths for the next meaningful source build; do not rerun unchanged TEST224 merely to compile documentation.


## TEST224 independent artifact and visual review completed — October 15, 2026
This execution successfully downloaded Android artifact12014511869 and confirmed its ZIP SHA-256 c94bd35a5086ab036e49ad2262904daaaaa131cba5bf8d405baaca235b84bc51. Independent XML recount found32 unique API34 methods, zero failures/errors/skips. All40 PNGs decoded at320x640; the entire contact sheet was reviewed, then the default APD Alpha Preview and original SPD reference were inspected at full resolution. The default shows only concise categorized icons and APD branding; the optional technical archive retains every original entry. This closes the earlier download/manual-review limitation without rebuilding the APK.
The separately downloaded staged evidence12014816550 matched ZIP SHA3b2db93839eb63a56cae54c9ae485e72ee169270a016bdf149e52d945495cceb. Its verification JSON confirms exact source3e886234e69ab128a861cfbc17bca549ba3a3456,1676core/32API34, APK SHA8d88ef02cb82af29cb2bb0aefc1a847726d7e95310768be59263ee78c8cb2579, certificate067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e and unchanged .indev applicationID. PDF APD-Alpha-Preview-224.pdf is15794bytes/SHAa581e856de00ab0ad3e53825d2b261dec4d9facea4ec78c00c166519393b536e, six readable pages with actual source/test counts. Verified-sourceZIP is4428608bytes/SHAfd783c723a65f4a1dc477427f455a3a196cf0fd2be41ce526eabde0940d1095e. TEST224 remains staged,not owner-released or tester-distributed;222 remains published fallback.
Added one supplemental native Android navigation fixture and a read-only reused-224-APK workflow. It exercises actual MotionEvents through the activity, Versions/release/technical/range selection, system Back, About return and default player view restoration. Its result is pending; do not add a pass to224's32-method gate before evidence exists. Only the instrumentation APK is compiled; no game source or application APK rebuild/publication occurs. Physical-phone/save-preservation acceptance and broader backlog remain open.
Prepared the next ledger against the actual immediately preceding produced APK224; failed documentation run38825603109 produced no225APK and is not a valid predecessor. All post224 staging/evidence/test/workflow paths are retained in that ledger without triggering a redundant application build.


## Owner preview delivered; two export reliability corrections pending current-source checks
Private owner APK224 was actually published2026-10-15T23:58:43Z (7:58:43p.m. EDT): https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/tag/apd-alpha-preview-224 . DirectAPK ADHD-Pixel-Dungeon-Alpha-Preview-224.apk is38581943bytes; GitHub asset digest matches8d88ef02cb82af29cb2bb0aefc1a847726d7e95310768be59263ee78c8cb2579. Publication-only run38826726834 used real wrapperafdea8d799434b16324ca96db9a83aafa1d55180 with application inputs identical to compiledsource3e886234e69ab128a861cfbc17bca549ba3a3456; no rebuild. All old releases/assets retained; no public release or automatic tester distribution. Supplemental navrun38826006010 artifact12014653235 SHA84af07b0f5c05ab5ca43df982ffca27f9a6eef1343d7ba7a0fbc89a9f7e12b08 independently confirmed one native touch/systemBack/version-picker pass and two readable screenshots. Counts:1676core/32mainAPI34 plusone distinct supplementalAPI34method againstthissameAPK,not a duplicate32+33 claim. Physicalphone acceptance remainsunverified.
Recovered the previously local same-millisecond archive segment/redacted-incomplete-history corrections after verifying their original production blobs still exist. Three small production edits and seven permanent core methods are pending exact224-before/current-after core acceptance; no newAPK or verificationclaim yet. Raw histories/saves, gameplay,RNG,all18rules,original scores,signing/appID remain untouched; existingGAPencoding only, no storage migration. Preserve published224 as fallback. Newpreviewnotes describe these fixes only once actually compiled in a laterAPK. See docs/APD-ARCHIVE-RELIABILITY-2026-10-15.md. The native history test is now part of future full gates with a dynamic build label: planned1683core/33API34/42PNG,subjecttoactualresults. BroadUIMSDaudit remains incomplete.


## TEST226 exact-source verification and private staging complete; owner publication pending
Application sourcea6e2f5e9aafb370ced629db01be300f6fab3f31e/run38827685570 succeeded,00:07:42–00:23:04UTC October16 (=October15 8:07:42–8:23:04p.m. EDT). Independently downloaded/recounted1683unique core methods/332suites and33API34 methods,0failures/errors/skips. The79targeted archive methods overlap the full suite; do not add them. Debug assembly and complete immediateprevious actuallyproducedTEST224 history passed. All42PNG decoded320x640 and contact sheet reviewed; default APDpreview, reopened navigation and originalSPD reference also viewed full resolution. API34 emulatorevidence only, no phone/owner-save acceptance.
Private stage38827870744 at724181c874c83d285037dc68174cb9335e987336 independently checked signing/package/version against published224 and reused exactAPK, no rebuild. APKsha2569a93691ca9417b4f3dd63c3724ba4702d5df10cca747f3db03dd67841fee3bd6; appID.indev unchanged; cert067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e; version4.0.8-apd-INDEV-bC226-TEST/code920. Staged6page PDF17902bytes/SHA4ee08d9cfc1b36dd40f9144e74ce51df399a9db1c7ba808595ba7e450eddb127 independently extracted and firstpage reviewed. SourceZIP SHAce9211f89f0805c0b22d0b2e116228873482f357f129b10948dceaf65eea41a3. Fullstageartifact12016172105/SHA0bc02df5e011eb9c84790bd64c3df4b808953b688b20840fa7c9a69ebd610214; evidence12016526624/SHA1b0e5f653b6121cbd83ced45af711bbf2871e26ff5bad516a682f15ff89de093. Machine receipt docs/APD-TEST226-VERIFICATION-2026-10-15.json contains exact artifacts.
Small production scope confirmed: ArchiveExporter6added/2removed, LogRedactor8added, EventTypes2added plus2previewnotes. Original run-history files, saves, gameplay,all18rules,RNG and scores unchanged. No formatmigration, hiddencontent, paid/publicrelease or automatic testerdistribution. Publication-only owner workflow is prepared/pending, not yet delivered. Preserve published224fallback until actual release exists. Broad UIMSD audit and unconfirmed sentry/audio/owner-run armor investigations remain incomplete. Unmeasured human/active/idle time remains unknown; CI walltime15m22s is not developer-active time.


## TEST226 actually delivered privately — 2026-10-15 8:26:30 p.m. EDT
Release https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/tag/apd-alpha-preview-226 was confirmed through actual GitHub release metadata at2026-10-16T00:26:30Z. Publication-only run38829450668 succeeded at real wrapperb71ee501f810ab2a9fce40b75d9e418a3d546d7c; application tree matches testedsourcea6e2f5e9aafb370ced629db01be300f6fab3f31e. No APK rebuild or public/tester distribution. APK38583318bytes/GitHub SHA2569a93691ca9417b4f3dd63c3724ba4702d5df10cca747f3db03dd67841fee3bd6, PDF17902bytes/SHA4ee08d9cfc1b36dd40f9144e74ce51df399a9db1c7ba808595ba7e450eddb127. Direct download filenames ADHD-Pixel-Dungeon-Alpha-Preview-226.apk and APD-Alpha-Preview-226.pdf; original internal226 trace preserved in receipt. SourceZIP4435905bytes/SHAce9211f89f0805c0b22d0b2e116228873482f357f129b10948dceaf65eea41a3. All old assets including224 remain intact. Install over, never uninstall/clear data. No physical-phone/owner-save acceptance, complete UIMSD audit or unconfirmed sentry/audio fix claimed. This actual delivery supersedes earlier pending publication statuses. Only two reproduced archive fixes were added; not a claim that the full backlog is complete.


## 2026-10-16T00:48:52Z — TEST227 Cleric collection-ledger preservation candidate
- Audited the current delivered TEST226 source and reproduced two local persistence failures without touching owner data: after a partially readable ledger skipped a damaged row, the next automatic save rewrote the file without the unknown bytes; and the replacement fallback could delete an empty directory configured at the ledger path and replace it with a file.
- Added two retained regressions. Against unchanged TEST226 production they failed 2/7 exactly at byte preservation and directory preservation. With the bounded correction all 7 LedgerTest methods pass locally (0 failures/errors/skips).
- The correction keeps intact rows available in memory but marks a partial read incomplete, refuses subsequent automatic disk writes, serializes the shared temporary path, refuses directory destinations, and replaces regular files without deleting the original first. Only the existing explicit player reset clears the incomplete state and may write a clean ledger.
- Candidate requests the complete next gate: 1,685 core methods, debug assembly, complete immediate-previous produced TEST226 history, 33 API34 emulator methods and 42 valid screenshots. Candidate is pending until actual CI evidence exists; TEST226 remains the privately delivered fallback. No release, tester distribution, physical-phone claim, save/run-history/score/RNG/gameplay/custom-rule change, lore removal, opt-out change or storage migration.
- Exact starting parent: `2da8a7e57351686efb9de5f32fdf9f64d4ebf573`. Local source SHA-256: `58dd196227cbee63a6443404a4a5fb8e4236b98275a6d867b316b627f80ccbdd`; local test SHA-256: `5e648f7bf4fb81a67a95d3d56b5396f6eb02e814ac032c834b9ef312f4bc096c`. Unmeasured durations remain unknown.


## 2026-10-16T01:07:23Z — TEST227 full gate verified; no release
- Exact source `9e553d9378dbfeca4c588910162872278ac20e2d` completed [run 38831758939](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38831758939) successfully from 2026-10-16T00:50:19Z through 2026-10-16T01:02:35Z.
- Independent core artifact recount: 1,685 methods across 332 suites, 0 failures/errors/skips. Core artifact `12017432491`, ZIP SHA-256 `d4f88bd72d43fdad33ea834a2b9a058c3f280cbad87bdebf5a3520ef64c8ccda`.
- Debug assembly and complete immediate-previous actually produced TEST226 history verification passed. APK artifact `12017189415`, ZIP SHA-256 `dd81484f26da65a2ab3013e02d30c9061bfc3948981f53b6b55eec03de24a014`; APK is 38,583,917 bytes with SHA-256 `5f57b8e3d791ba4bdcbd5ebebb4ce97cf5a85ee7b31343abe87c7552a505e26e`.
- Independent Android artifact recount: 33 API34 emulator methods, 0 failures/errors/skips. Exactly 42 PNGs decoded at 320×640 and the complete contact sheet was reviewed without a crash, blank-window or obvious clipping regression. Android artifact `12017334241`, ZIP SHA-256 `7f0f5106c4564877f96741a72942f982dce88e79e2e408476459013cede62169`.
- APK package strings retain `com.charltonchris29.adhdpixeldungeon.indev`. The APK v2 certificate SHA-256 is `067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e`, exactly matching delivered TEST226.
- Scope remains the two reproduced private Cleric collection-ledger failures and their fail-closed correction. No publication, tester distribution or physical-phone result occurred. TEST226 remains the latest privately delivered fallback; TEST227 is a verified CI candidate only. Broad UIMSD audit, owner-device install-over/save acceptance, audio and unconfirmed owner-run armor/sentry investigations remain open. Unmeasured durations remain unknown; workflow wall time is not developer-active time.


## 2026-10-16T01:18:25Z — Saved-column compatibility audit (TEST228 pending)

Reproduced two current-source blank-table failures in UIMSD saved column layouts: hidden-only columns reopened after hidden information was turned off, and retired-only IDs reopened after a schema change. Added two permanent UimsdModelTest methods; unchanged production failed 2/24 at the intended assertions. The bounded correction validates layouts against the active dataset/visibility, keeps usable order, and falls back to public view defaults when none remain without rewriting preference or history files. Corrected 24/24 focused and 294/294 broader query/UIMSD methods passed locally, including 28,254 randomized comparisons. Candidate requires the exact 1,687-core/debug/TEST227-history/33-API34/42-PNG full gate. Pending only; no APK, release, distribution or phone claim. TEST226 delivered and TEST227 verified fallbacks remain unchanged.


## Typed-payload table provenance correction — combined local tests passed, CI pending
Exact226source reproduction found3methods/2expected failures/1healthycontrol. RunFactsBuilder already marked missing typed events incomplete, but RowBuilders ignored that provenance for item/enemy totals. The bounded fix retains the warning and increments only row-cache builder14→15; original recorded values, run.partial semantics, rawfiles/saves/fact-cache formats remainunchanged. Four permanent cases cover clean counts, malformed attacks/equipment and actual valid version14cache rebuild with unchanged rawbytes. Existing cache and quest-boss version assertions remain enforced.
Local14targeted and661broader methods passed. After preserving both concurrent Ledger and UimsdModel changes,131classes/670combined methods passed; counts overlap and are not additive. See docs/APD-QUERY-PAYLOAD-PROVENANCE-2026-10-15.md for local setup failures, corrected resources and exact limitations. The first aa968c067151bb6df699aa85a02ca7d5e495908f candidate was not published: its GitHub branch-update request errored, and newer commits were preserved rather than overwritten.
Current preceding actuallyproducedAPK is228/source dba5e7478418c0657b1fff7f1c117f2eb6b1c3c4/run38834508461/artifact12017564794, despite stillpendingAndroid.227completed its verification. The new workflow is core-only, requiring unchanged226original failures plus the complete current1691-method suite. No new APKassembly/publication is requested by this commit. Published226remains ownerfallback; this queryfix is NOT in226,227or228. Android/build/history/signing and physical-phone acceptance remain open; full UIMSD audit incomplete.


## 2026-10-16T01:40:03Z — TEST228 full verification and provenance core acceptance

TEST228 source `dba5e7478418c0657b1fff7f1c117f2eb6b1c3c4` / run `38834508461` passed and was independently checked: 1,687 core/332 suites, debug and complete TEST227 history, 33 API34, 42 valid 320×640 PNGs, all zero failure/error/skip. Core/APK/Android artifact ZIP hashes are `0cc4611fdfa0ffe05f791c054e626056ff686b64c965feadd79c45b92b962c76`, `28ba68a91e6a6655a862ab231c6b2eb0471ebd673f7ea53f346e1902adb36bc6`, and `4c5f11eb59dabadc7014d2d7380877844fb8dea9f9ac1e3b3ac8a44ba503103c`. APK SHA `43349772362ec5b1a708294130e6f9b271b2fbf216b5527a5554d57dc16d3352`, 38,584,468 bytes; unchanged .indev identity and v2 certificate. Complete sheet/key views reviewed. No delivery/phone claim; TEST226 remains owner fallback.

Newer typed-payload source `f0b0691362f9ef9f4e753cbc12318a857a570238` stayed separate. Core-only run `38835333283` artifact `12019320632` / `968eb92312eea1cfa8a1317288bfae45f0c887d02f634b18219ca6ea5f79713e` confirms exact226 reproduction 2 intended failures/1 healthy pass and current 1,691/1,691 core in333 suites. It built no APK and has no Android/visual/signing evidence; require the normal TEST229 full gate before calling that application source verified.


## 2026-10-16T01:51:26.031Z — malformed hidden combat details cannot erase public combat (candidate)
Found that RowBuilders decoded optional hidden combat details inside the same failure boundary as readable public ATTACK/DAMAGE/KILL events. A malformed detail therefore dropped valid public data from enemy and per-event tables. The bounded candidate isolates detail decoding in both builders, retains public counts/amounts/events, leaves hidden class/source unknown, invalidates only disposable row caches (builder 15→16), and adds retained coverage for attack/damage/kill plus heal and raw-byte preservation. Acceptance is pending the exact parent reproduction of two failures, 1,693-core gate, and the full TEST229 build/API34/visual/history gate. No gameplay, saves, raw history, scores, rules, RNG, lore, signing, application ID or publication changed.


## TEST231 privately delivered — October 15, 2026
Publication-only wrapper `60e5339bccd5127eee8841e5cbbf9633f7bf8dbd` / run [38840006695](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38840006695) succeeded at 2026-10-16T02:21:57Z (October 15, 10:21:57 p.m. EDT). Release [apd-alpha-preview-231](https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/tag/apd-alpha-preview-231) reuses the exact staged APK from application source `5134ef6438f9562a37b3f8db727d5d004fa93527`; it did not rebuild the application. APK 38,585,399 bytes / SHA-256 `20f1d87ab58afd21cd37ae4027e6ebb79c01fcf802d0869801e6ffa03943bff9`; application ID `.indev`, version `4.0.8-apd-INDEV-bC231-TEST`, and certificate `067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e` match the verified staging evidence. No automatic tester distribution or physical-phone acceptance occurred; TEST231 is now the owner-delivered fallback.


## 2026-10-16T02:43:01Z — malformed lifecycle recovery candidate
Audited recorder resume bookkeeping after the TEST231 delivery. One malformed canonical `RUN_START` or `RULESET_CHANGE` payload stopped the shared restore loop, so later readable lifecycle state could be ignored even though the raw event remained intact. The bounded correction isolates decoding per event in resume and segment-header recovery. One retained method supplies a malformed start followed by a readable ruleset change. Exact TEST231 application source must fail that method; corrected current source must pass 1,694 core methods. Full TEST232 gate remains required: debug assembly, complete immediate-previous TEST231 history, 33 API34 methods and 42 screenshots. No raw-history/save/score/gameplay/rule/turn/RNG/lore/signing/application-ID change, release, tester distribution or phone claim.

## 2026-10-16T02:57:22Z — TEST232 full gate verified; evidence-only record
- Exact candidate source `96772420c7ccdcbc294e8eae33d68b27d71abfaa` completed the full [run 38841967217](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38841967217) successfully from 2026-10-16T02:44:25Z through 2026-10-16T02:57:22Z.
- Dedicated [run 38841967223](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38841967223) reproduced exactly one intended failure on exact TEST231 application source `5134ef6438f9562a37b3f8db727d5d004fa93527`: `malformedLifecyclePayloadCannotHideLaterRulesetState` expected `new-rules` but recovered an empty ruleset. The corrected source passed the complete 1,694-method core suite across 334 suites with 0 failures/errors/skips.
- Debug assembly and complete immediate-previous produced TEST231 history passed. Independent artifact recounts confirmed 33 API34 emulator methods with 0 failures/errors/skips and exactly 42 PNGs, all 320×640. The complete contact sheet plus full-resolution UIMSD home and sort-key views were reviewed without a crash, blank window or obvious clipping regression.
- Produced APK: `ADHD-Pixel-Dungeon-CODEX-TEST-232.apk`, 38,585,886 bytes, SHA-256 `e6e303276713e6521f84c8b3dc9e393d03c09f3edc7841fe4c9c018d6ff7045f`. Reviewed UI identifies `4.0.8-apd-INDEV-bC232-TEST`. Current APK certificate and application ID were not independently rechecked in this environment; no claim is made beyond unchanged Android configuration and the verified TEST231 identity.
- Artifact IDs / downloaded ZIP SHA-256: targeted `12021736431` / `92fff2edf68bd38f1dc952d7f5efaec78825337a2e5dd92aba07fa1506b6480c`; core `12021811183` / `f74cb48befc2c3b8b1c3960a7bbcc339a37364161d93deb97a379504aac5e74c`; APK/history `12021910160` / `f39a41ec02674b6a30c7d9ce5cac990c34da5175c8b5352a3f33be10f36b5432`; Android/visual `12022035973` / `684530423b222458acba31d1f93252ccc9aa01feb0fd324add0218ba5a39f80e`.
- Scope remains only malformed lifecycle recovery. Raw histories, saves, scores, gameplay, all 18 rules, turns, RNG, lore/opt-out, signing configuration and application-ID configuration were not changed.
- TEST232 is a fully CI-verified candidate only: no private publication, tester distribution, physical-phone result or owner-device install-over/save acceptance occurred. TEST231 remains the latest privately delivered fallback. Broad UIMSD audit, audio, and unconfirmed armor/sentry investigations remain open.
- The next meaningful build manifest must include this evidence entry and `docs/APD-TEST232-VERIFICATION-2026-10-15.json`; do not rebuild unchanged TEST232 merely to record documentation.

## 2026-10-16T03:38:42Z — active-play idle-gap correction candidate
- Audited `PlayTracker` against its stated active-time contract and found that every app-background or long-pause gap over 30 seconds was credited as exactly 30 seconds of active play because the accumulator used `min(gap, 30 seconds)`.
- Added retained method `aLongIdleGapAddsNoActivePlayTime`: it moves the monotonic baseline 31 seconds back, records the next hero turn and requires the active total to remain zero. Exact verified TEST232 source `96772420c7ccdcbc294e8eae33d68b27d71abfaa` must fail with 30,000 milliseconds.
- The bounded correction counts positive gaps through 30 seconds normally and excludes longer gaps. It does not change game time, turns, saves, existing raw history, scores, gameplay, RNG, any of the 18 rules, lore, signing or application-ID configuration.
- Dedicated before/after evidence and the full TEST233 gate are pending: expected corrected count is 1,695 core methods, then debug assembly, complete immediate-previous produced TEST232 history, 33 API34 emulator methods and 42 valid screenshots.
- No APK verification, publication, tester distribution or physical-phone result is claimed by this candidate entry. TEST231 remains the latest privately delivered fallback and verified TEST232 remains the last playable CI candidate until the new gate actually passes.

## 2026-10-16T03:59:24Z — TEST233 active-time correction fully verified
- Exact source `c6bed1f12f017d3f2fa92adf2c29d793f5e004ab` completed [full run 38847041245](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38847041245) successfully from 2026-10-16T03:44:46Z through 2026-10-16T03:59:24Z.
- Dedicated [run 38847041242](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38847041242) reproduced exactly one intended failure on exact TEST232 source `96772420c7ccdcbc294e8eae33d68b27d71abfaa`: `aLongIdleGapAddsNoActivePlayTime` expected 0 but recorded 30,000 milliseconds. Corrected source passed the complete 1,695-method core suite across 334 suites with 0 failures/errors/skips.
- Debug assembly and complete immediate-previous produced TEST232 history passed. Independent artifact recounts confirmed 33 API34 emulator methods with 0 failures/errors/skips and exactly 42 PNGs, all 320×640. The complete contact sheet plus full-resolution UIMSD home and Custom Dungeon Rules touch views were reviewed without a crash, blank window or obvious clipping regression.
- Produced APK: `ADHD-Pixel-Dungeon-CODEX-TEST-233.apk`, 38,586,337 bytes, SHA-256 `f3db3e79262ae45de213a34681df270f0b9ef662bb53eef1d84c2dc8e5f477af`. Reviewed UI identifies `4.0.8-apd-INDEV-bC233-TEST`.
- Artifact IDs / downloaded ZIP SHA-256: targeted `12024096254` / `267e93ae3d6d015f825871cc664c6cecb68c9d1f68ac4ae1aa82a01932568c1d`; core `12023821414` / `a9937a457cbc83b1ccdbf588eb0ac09cef52a18576125152ef5defc04480486b`; APK/history `12023681745` / `5c3b319a0efec8d5be9145cf80de2c93de5b908da7c9de97fb1e1c6043606626`; Android/visual `12024197452` / `ebe142a6f183383b6ce2b832c9a7b6a91b7e8c5b67e6dbb8e411fe40a9b7f317`.
- Current APK certificate and application ID were not independently rechecked in this environment; no claim is made beyond unchanged Android configuration and TEST231's verified delivered identity.
- Scope remains only active-play accounting after long idle gaps. Already-recorded history is not rewritten. Saves, scores, gameplay, all 18 rules, turns, RNG, lore/opt-out, signing configuration and application-ID configuration were not changed.
- TEST233 is a fully CI-verified candidate only: no publication, tester distribution, physical-phone result or owner-device install-over/save acceptance occurred. TEST231 remains the latest privately delivered fallback.
- The next meaningful build manifest must include this evidence entry and `docs/APD-TEST233-VERIFICATION-2026-10-15.json`; do not rebuild unchanged TEST233 merely to record documentation. Broad UIMSD audit, audio and unconfirmed armor/sentry investigations remain open.

## 2026-10-16T04:42:39.352Z — malformed run-end privacy candidate
- Audited the post-TEST233 history boundary and reproduced two failures: a checksum-valid but empty typed `RUN_END` marked a run finished in UIMSD and allowed an unredacted current-run export.
- The bounded correction requires a readable `LifecycleEvents.decodeRunEnd` before finished-run visibility or raw hidden-data export is authorized. The malformed event remains untouched in raw history.
- Added two retained regressions and exact TEST233 before/after workflow. Exact TEST233 must fail both; corrected source requires 1,697 core methods, then debug assembly, complete immediate-previous produced TEST233 history, 33 API34 methods and 42 valid screenshots.
- Candidate pending. No APK verification, publication, tester distribution, physical-phone result or owner install-over/save acceptance is claimed. TEST231 remains the privately delivered fallback and TEST233 remains the newest fully verified CI candidate until the new gates pass.


## 2026-10-16T04:58:39Z — TEST234 malformed run-end privacy fully verified
- Exact source `7f957d75614c48bbb7f98999bfecacfcc2aaede1` completed [full run 38852153894](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38852153894) successfully from 2026-10-16T04:44:19Z through 2026-10-16T04:58:39Z.
- Dedicated [run 38852153982](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38852153982) reproduced exactly two intended failures on exact TEST233 source `c6bed1f12f017d3f2fa92adf2c29d793f5e004ab`: `malformedRunEndCannotExportHiddenCurrentRunData` and `malformedRunEndCannotMarkRunFinished`. Corrected source passed the complete 1,697-method core suite across 335 suites with 0 failures/errors/skips.
- Debug assembly and complete immediate-previous produced TEST233 history passed. Independent artifact recounts confirmed 33 API34 emulator methods with 0 failures/errors/skips and exactly 42 PNGs, all 320×640. The complete contact sheet plus full-resolution UIMSD home, UIMSD text-value editor and Custom Dungeon Rules touch views were reviewed without a crash, blank window, malformed image or obvious clipping regression.
- Produced APK: `ADHD-Pixel-Dungeon-CODEX-TEST-234.apk`, 38,586,764 bytes, SHA-256 `aea1221cb6143c4e3b2b0cf88701c72f9a3fa3a8de3ee8eac5879e20ece8e202`. Manifest version `4.0.8-apd-INDEV-bC234-TEST` and application ID `com.charltonchris29.adhdpixeldungeon.indev` were read from the compiled manifest. Extracted v2 certificate SHA-256 `067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e` and application ID match the immediate previous TEST233 APK.
- Artifact IDs / independently downloaded ZIP SHA-256: targeted `12025063149` / `0e08088a382c97466c90a100135877467a1f0df92a986f2432d0d13ad589b8c6`; core `12025693310` / `9d75588a7dd84bce37be09ee0dff908e080531f748263850e4c432babc018777`; APK/history `12025638490` / `3aad301f4b15f7be3940f02033c95aaec21c4716229017e0c07ef2a1eb9a83b0`; Android/visual `12026191555` / `d18e84ba4ee21a8991cbee91620c3a513f7f426f88739cac4d4441d48d43208d`.
- Scope remains only malformed typed run-end handling: a run becomes finished, or eligible for unredacted export, only after its typed end payload decodes. The malformed frame and every existing raw history byte remain untouched. Saves, scores, gameplay, all 18 rules, turns, RNG, lore/opt-out, signing configuration and application-ID configuration were not changed.
- TEST234 is a fully CI-verified candidate only: no publication, tester distribution, physical-phone result or owner-device install-over/save acceptance occurred. TEST231 remains the latest privately delivered fallback.
- The next meaningful build manifest must include this evidence entry and `docs/APD-TEST234-VERIFICATION-2026-10-16.json`; do not rebuild unchanged TEST234 merely to record documentation. Broad UIMSD audit, audio and unconfirmed armor/sentry investigations remain open.


## 2026-10-16T05:39:30Z — malformed run-start fact recovery candidate
- Broad UIMSD derivation audit found that an unreadable first typed `RUN_START` set the first-start guard before decode, hiding a later readable start and its recorded run ID, class, seed, difficulty and exact 18-rule snapshot from permanent derived facts.
- The bounded correction sets the guard only after successful decode. An already readable first start still cannot be relabelled by later segments; malformed raw history is untouched and count provenance remains incomplete.
- Added retained method `malformedFirstStartDoesNotHideLaterReadableStart` and a dedicated exact-TEST234 reproduction workflow. Expected results are exactly one failure on TEST234, then 1,698 corrected core methods.
- Full TEST235 gate remains required: debug assembly, complete immediate-previous produced TEST234 history (source `7f957d75614c48bbb7f98999bfecacfcc2aaede1`, run `38852153894`), 33 API34 methods and 42 valid screenshots. Candidate is pending; no APK, publication, tester distribution, physical-phone result or owner install claim is made. TEST231 remains the delivered fallback and TEST234 the newest verified CI candidate.


## 2026-10-16T06:09:00Z — TEST235 malformed run-start fact recovery fully verified
- Exact source `b4a589a4d712de29f7bc651a4cbbac002ceafd68` completed [full run 38857030793](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38857030793) successfully after the Android-only second attempt.
- Dedicated [run 38857030886](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38857030886) reproduced exactly one intended failure on unchanged TEST234 source `7f957d75614c48bbb7f98999bfecacfcc2aaede1`: `malformedFirstStartDoesNotHideLaterReadableStart`. The corrected source passed all 1,698 core methods across 335 suites with 0 failures/errors/skips.
- Debug assembly and complete immediate-previous produced TEST234 history passed. APK `ADHD-Pixel-Dungeon-CODEX-TEST-235.apk` is 38,587,219 bytes with SHA-256 `6b2de5d8906d01114b01521ea2eeef6951d7b50c73f9076be05a7b9693782e27`; compiled version `4.0.8-apd-INDEV-bC235-TEST`, application ID `com.charltonchris29.adhdpixeldungeon.indev`, and extracted v2 certificate SHA-256 `067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e` match the TEST234 identity.
- API34 attempt 1 passed all 33 methods but its `uimsd-text-values.png` capture was mostly black/white and was rejected during manual review. Android-only attempt 2 passed all 33 methods and produced 42 valid 320×640 PNGs. The complete rerun sheet plus full-resolution text-value, UIMSD home/sort and Custom Dungeon Rules views were reviewed clean. The rejected artifact remains recorded; it was not silently counted as acceptable.
- Artifact IDs / independently downloaded ZIP SHA-256: targeted `12026809322` / `d40e3f9adcf8f637e63385e841b6a7b5c28da213d762a14405ff8ed24ed9cd5c`; core `12027178810` / `d376e94811ec9770b0dd7a693144ab1036725f3b0fbe98055858b24b6d2daba0`; APK/history `12026619692` / `9d37065d9323267c370d91b222c4a9a64e4aad4f99b2b03c288f5da11c6e63cc`; rejected Android attempt `12028605331` / `c5a6b3fef36b92f229314009a2908a76536266982e9f1686ee0cf362ab81358a`; accepted Android rerun `12029155187` / `35a1136f958fca2e2e023eb3287f4908ae98032d53397756e2ae7e8601bc2a58`.
- Scope remains only malformed typed start recovery in derived facts. Raw histories, saves, original scores, gameplay, all 18 rules, turns, RNG, all 50 lore entries/private grudge/opt-out, signing configuration and application-ID configuration were not changed.
- TEST235 is a CI-verified candidate only. No publication, tester distribution, physical-phone result or owner-device install-over/save acceptance occurred. TEST231 remains the latest privately delivered fallback. Broad UIMSD audit, audio and unconfirmed armor/sentry investigations remain open.
- The next meaningful build manifest must include this evidence entry and `docs/APD-TEST235-VERIFICATION-2026-10-16.json`; do not rebuild unchanged TEST235 solely to record documentation.


## 2026-10-16T06:43:14Z — archive run-start identity recovery candidate
- Audited archive transfer after TEST235 and reproduced three failures: an unreadable first typed `RUN_START` hid a later readable logical run ID from export, caused a correctly labelled import to be rejected, and allowed a header-only manifest label that contradicted the later readable start.
- The bounded correction makes exporter/importer claim the first start only after typed decoding succeeds. Raw log bytes remain untouched; the first readable start still resists later relabelling.
- Three retained `ArchiveRoundTripTest` methods and an exact-TEST235 before/after workflow require all three old failures and 1,701 corrected core methods.
- Full TEST236 gate remains required: debug assembly, complete immediate-previous produced TEST235 history (source `b4a589a4d712de29f7bc651a4cbbac002ceafd68`, run `38857030793`), 33 API34 methods and 42 valid screenshots. Candidate pending; no APK verification, publication, tester distribution, physical-phone result or owner install claim is made. TEST231 remains the delivered fallback and TEST235 the newest verified CI candidate.


## 2026-10-16T07:01:05Z — TEST236 archive run-start identity recovery fully verified
- Exact source `1ea12db8d365ca135aa7af799a1ea7405dc0241f` completed full run `38863040610` successfully from 2026-10-16T06:45:41Z through 2026-10-16T07:01:05Z.
- Dedicated run `38863040526` reproduced exactly three intended failures on unchanged TEST235 application source `b4a589a4d712de29f7bc651a4cbbac002ceafd68`: `malformedFirstStartDoesNotHideReadableRunFromExportManifest`, `importUsesFirstReadableStartAfterMalformedStart`, and `importRejectsManifestThatIgnoresLaterReadableStart`. Corrected source passed all 1,701 core methods across 335 suites with 0 failures/errors/skips.
- Debug assembly and complete immediate-previous produced TEST235 history passed. Independent artifact recounts confirmed 33 API34 emulator methods with 0 failures/errors/skips and exactly 42 PNGs, all 320×640. The complete contact sheet plus full-resolution UIMSD text-value, UIMSD home and Custom Dungeon Rules views were reviewed without a crash, blank/malformed capture or obvious clipping regression.
- Produced APK: `ADHD-Pixel-Dungeon-CODEX-TEST-236.apk`, 38,587,690 bytes, SHA-256 `8e92c62427a853f47d0dfb34739c792d716908ba9f3a56751f7d868e42f60c81`. Compiled version `4.0.8-apd-INDEV-bC236-TEST`, application ID `com.charltonchris29.adhdpixeldungeon.indev`, and extracted v2 certificate SHA-256 `067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e` match TEST235 identity.
- Artifact IDs / independently downloaded ZIP SHA-256: targeted `12030332267` / `064e0e56b02cba376b0e39991d101ac39c8481d314b85a83128e175fb70ae581`; core `12030351505` / `62ef189dd4a2203dc4a11a74882e0b34c26b7321cf29e7561aefc9a1a1a3b6b6`; APK/history `12030161792` / `6328fab6e306ddd5fcc2ace52565868766755d6ca44d920f420761f3b0576a60`; Android/visual `12030626988` / `14a464d6005b89b3392187caf2c26a88ce47e251cec445d2dbbb67324aa0d2a2`.
- Scope remains only archive identity recovery after malformed typed starts. Raw histories, saves, original scores, gameplay, all 18 rules, turns, RNG, all 50 lore entries/private grudge/opt-out, signing configuration and application-ID configuration were not changed.
- TEST236 is a fully CI-verified candidate only. No publication, tester distribution, physical-phone result or owner-device install-over/save acceptance occurred. TEST231 remains the latest privately delivered fallback.
- The next meaningful build manifest must include this evidence entry and `docs/APD-TEST236-VERIFICATION-2026-10-16.json`; do not rebuild unchanged TEST236 solely to record documentation.


## 2026-10-16T07:32:21Z — archive folder-kind integrity candidate
- Broad archive/UIMSD audit found that `runs/` entries could declare kind `legacy` and bypass logical run-ID validation; `legacy/` entries declaring kind `run` were also accepted.
- The bounded correction validates the folder/kind pair before any payload is staged. A mismatch rejects the whole archive without writing history. Valid archives, repeated-path compatibility and raw logs remain unchanged.
- Two retained hostile-archive methods and an exact-TEST236 before/after workflow require both old bypasses and 1,703 corrected core methods.
- Full TEST237 gate remains required: debug assembly, complete immediate-previous produced TEST236 history (source `1ea12db8d365ca135aa7af799a1ea7405dc0241f`, run `38863040610`), 33 API34 methods and 42 valid screenshots. Candidate pending; no APK verification, publication, tester distribution, physical-phone result or owner install claim is made. TEST231 remains the delivered fallback and TEST236 the newest verified CI candidate.


## 2026-10-16T07:49:23Z — TEST237 archive folder-kind integrity fully verified
- Exact source `8f9b6bf9e7a8f56b6b46c3b713f3c49f47a3f17f` completed full run `38868120835` successfully from 2026-10-16T07:34:52Z through 2026-10-16T07:49:23Z.
- Dedicated run `38868120948` reproduced exactly two intended failures on unchanged TEST236 source `1ea12db8d365ca135aa7af799a1ea7405dc0241f`: `aLegacyPathCannotMasqueradeAsARun` and `aRunPathCannotMasqueradeAsLegacyToBypassRunIdentity`. Corrected source passed all 1,703 core methods across 335 suites with 0 failures/errors/skips.
- Debug assembly and complete immediate-previous produced TEST236 history passed. Independent artifact recounts confirmed 33 API34 emulator methods with 0 failures/errors/skips and exactly 42 PNGs, all 320×640. The complete contact sheet plus full-resolution UIMSD text-value, UIMSD home and Custom Dungeon Rules views were reviewed without a crash, blank/malformed capture or obvious clipping regression; logcat contained no fatal app exception.
- Produced APK: `ADHD-Pixel-Dungeon-CODEX-TEST-237.apk`, 38,588,149 bytes, SHA-256 `aa2e69b10a8ca88a2ba11dab947fed582cf94395902513d4971163f4e07921ea`. Compiled version `4.0.8-apd-INDEV-bC237-TEST`, application ID `com.charltonchris29.adhdpixeldungeon.indev`, and extracted v2 certificate SHA-256 `067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e` match TEST236 identity.
- Artifact IDs / independently downloaded ZIP SHA-256: targeted `12032087748` / `ed9c5af1426d3d0cbfd515ef44a3c29e41014bcc2c4d5d09076d077483522271`; core `12031844153` / `42d28f2054ce1ddff933c4c379dc31e3ba969f1236e86252bd000e2dde772b14`; APK/history `12031864048` / `19c820b6133ed6255172c40282ad4611cc85daf48bdd60104d30dd06641bc7f1`; Android/visual `12032443592` / `334d28dd285456a0cd7e9670bb1db59682ba68ceaa7447a205c25cdb5e638f24`.
- Scope remains only archive folder-kind integrity. Raw histories, saves, original scores, gameplay, all 18 rules, turns, RNG, all 50 lore entries/private grudge/opt-out, signing configuration and application-ID configuration were not changed.
- TEST237 is a fully CI-verified candidate only. No publication, tester distribution, physical-phone result or owner-device install-over/save acceptance occurred. TEST231 remains the latest privately delivered fallback.
- The next meaningful build manifest must include this evidence entry and `docs/APD-TEST237-VERIFICATION-2026-10-16.json`; do not rebuild unchanged TEST237 solely to record documentation.


## 2026-10-16T08:35:41Z — archive empty run-identity recovery candidate
- Continued the UIMSD/archive audit after TEST237 and reproduced an identity gap: a decoded but empty first `RUN_START` hid a later valid logical run ID during export and import.
- The bounded correction claims typed run identity only after the existing valid-run-ID check succeeds. It leaves raw log bytes, event order, finished-state handling and valid archives unchanged.
- Two retained `ArchiveRoundTripTest` methods and an exact-TEST237 before/after workflow require both old failures and 1,705 corrected core methods.
- Full TEST238 gate remains required: debug assembly, complete immediate-previous produced TEST237 history (source `8f9b6bf9e7a8f56b6b46c3b713f3c49f47a3f17f`, run `38868120835`), 33 API34 methods and 42 valid screenshots. Candidate pending; no APK verification, publication, tester distribution, physical-phone result or owner install claim is made. TEST231 remains the delivered fallback and TEST237 the newest verified CI candidate.


## 2026-10-16T08:51:20Z — TEST238 verified archive empty run-identity recovery
- Exact source `d39de932c69c23bcb013a8ee86df930718ad39c8` passed full run [38874923900](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38874923900): 1,705 core methods in 335 suites, debug assembly, complete immediate-previous produced TEST237 history, 33 API34 emulator methods and 42 valid 320×640 screenshots, with zero failures/errors/skips.
- Dedicated run [38874924003](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38874924003) reproduced exactly the two intended empty-run-ID failures on exact TEST237 source `8f9b6bf9e7a8f56b6b46c3b713f3c49f47a3f17f`, then passed the corrected 1,705-method suite.
- APK: 38,588,580 bytes; SHA-256 `e0d657026c7889806b669e9032ed0b6b7992b081756baefb48820d36ef1c9025`; `.indev` app ID and v2 certificate `067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e` match TEST237.
- Independent artifact recount, app-crash scan and complete 42-image contact-sheet review passed. Raw history, saves, scores, gameplay, all 18 rules, turns, RNG, 50 lore entries/private grudge/opt-out, signing and application ID remain preserved.
- No publication, tester distribution, physical-phone result or owner install-over/save acceptance occurred. TEST231 remains the latest privately delivered fallback. Continue the broad UIMSD reliability audit; do not rebuild unchanged TEST238 solely for evidence documentation.


## 2026-10-16T09:36:49Z — hidden aggregate cleanup candidate
- Continued the UIMSD reliability audit after TEST238 and confirmed that disabling hidden information left grouped aggregates dependent on hidden denominator, condition or distinct-by columns. Query execution then omitted those aggregates and reported problems even though the UI had supposedly cleaned the query.
- The bounded correction removes an aggregate when any resolved aggregate reference is internal. Public aggregates and unknown forward-compatible references retain their prior behavior; history, saved views, cache formats and scoring are unchanged.
- One retained `UimsdModelTest` method covers all three missed dependency positions plus a public aggregate that must remain. Exact TEST238 source must fail it; corrected source requires 1,706 core methods.
- Full TEST239 gate remains required: debug assembly, complete immediate-previous produced TEST238 history (source `d39de932c69c23bcb013a8ee86df930718ad39c8`, run `38874923900`), 33 API34 methods and 42 valid screenshots. Candidate pending; no APK verification, publication, tester distribution, physical-phone result or owner install claim is made. TEST231 remains delivered fallback and TEST238 the newest verified CI candidate.

## 2026-10-16T09:52:42Z — TEST239 hidden aggregate cleanup fully verified
- Exact source `6182b042b1d93b64341f41a516a49ef3d12c93ee` completed [full run 38881765134](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38881765134) successfully from 2026-10-16T09:37:49Z through 2026-10-16T09:52:42Z.
- Dedicated [run 38881765302](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38881765302) reproduced exactly one intended failure on exact TEST238 application source `d39de932c69c23bcb013a8ee86df930718ad39c8`: `disablingHiddenInformationRemovesEveryHiddenAggregateDependency`. Corrected source passed all 1,706 core methods across 335 suites with 0 failures, errors or skips.
- Debug assembly and complete immediate-previous produced TEST238 history passed. The TEST239 APK contains 285 history TSV records and its new record has both required categories: verified TEST238 evidence and the hidden-aggregate correction.
- Independent artifact recounts confirmed 33 API34 emulator methods with 0 failures, errors or skips and exactly 42 PNGs, all 320×640. The complete 42-image contact sheet was reviewed without a blank, malformed or visibly clipped capture; the app log contained no fatal app-exception, ANR or native-abort signature.
- Produced APK: `ADHD-Pixel-Dungeon-CODEX-TEST-239.apk`, 38,589,027 bytes, SHA-256 `a9d634a1b9c791e971ccf94e1c864f3864144d6e908a68e2f515ecbaf50c30bf`. Compiled version `4.0.8-apd-INDEV-bC239-TEST`, application ID `com.charltonchris29.adhdpixeldungeon.indev`, and extracted v2 certificate SHA-256 `067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e` match TEST238 identity.
- Artifact IDs / independently downloaded ZIP SHA-256: targeted `12037697502` / `482d8a7f5f60b158426b6b9929ac011766004314f6d2109b33bdafc2ef41b08b`; core `12037837605` / `3aef277fc6c50b65eba3aad3bf9a06c667994dc730d59b6baaa49b79e7a1d48c`; APK/history `12037962338` / `18ec1f0391a2085edbf8db967945e5d434933df512962156c5160e4035c9fda1`; Android/visual `12038079510` / `cbe9be1aa815eac9533f19c4feadc4176a29459594d8b492088d2d43d14ed8ef`.
- Scope remains only cleanup of hidden aggregate dependencies when hidden information is disabled. Raw histories, saved views, derived caches, saves, original scores, gameplay, all 18 rules, turns, RNG, all 50 Cleric lore entries/private grudge/opt-out, signing configuration and application-ID configuration were not changed.
- TEST239 is a fully CI-verified candidate only. No publication, tester distribution, physical-phone result or owner-device install-over/save acceptance occurred. TEST231 remains the latest privately delivered fallback.
- Do not rebuild unchanged TEST239 solely to record evidence. Continue the broad UIMSD reliability audit; audio and unconfirmed armor/sentry acceptance remain open.


## 2026-10-16T10:44:50Z — distinct-key uncertainty candidate
- Continued the UIMSD aggregate audit after TEST239 and confirmed that per-distinct aggregates kept missing or lower-bound keys as separate row identities but displayed the result as exact.
- The bounded correction preserves those rows and the existing numeric result while marking the aggregate uncertain whenever a distinct key is hidden, unknown or lower-bound. The detail view now uses the existing “some entries could not be judged” disclosure; exact known keys are unchanged.
- Two retained `AggregateExtensionsTest` methods require the unchanged TEST239 source to fail both cases and the corrected source to pass all 1,707 core methods. No test or guard was weakened.
- Full TEST240 gate remains required: debug assembly, complete immediate-previous produced TEST239 history (source `6182b042b1d93b64341f41a516a49ef3d12c93ee`, run `38881765134`), 33 API34 emulator methods and 42 valid screenshots.
- Scope is derived query display/provenance only. Raw histories, saved views, caches, saves, original scores, gameplay, all 18 rules, turns, RNG, all 50 Cleric lore entries/private grudge/opt-out, signing and application ID remain unchanged. Candidate pending; no APK verification, publication, tester distribution, physical-phone result or owner install claim is made. TEST231 remains the delivered fallback and TEST239 the newest verified CI candidate.


## 2026-10-16T11:03:00Z — TEST240 distinct-key uncertainty fully verified
- Exact source `7f18ca8bcbabc07f159b6e3acfc9689c1ae6f28b` completed [full run 38889483083](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38889483083) successfully from 2026-10-16T10:46:55Z through 2026-10-16T11:00:18Z.
- Dedicated [run 38889483125](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38889483125) reproduced exactly two intended failures on unchanged TEST239 application source `6182b042b1d93b64341f41a516a49ef3d12c93ee`: `rowsWithNoValueToShareAreNotMergedByTheDistinctRule` and `lowerBoundDistinctKeysAreNotTreatedAsExactIdentities`. Corrected source passed all 1,707 unique core methods across 335 suites with 0 failures, errors or skips.
- Debug assembly and complete immediate-previous produced TEST239 history passed. Independent comparison found all 285 TEST239 history TSV records unchanged; TEST240 adds only `codex-test-240.tsv` plus the generated index update, for 286 records. The new record contains both reviewed categories.
- Independent artifact recount confirmed 33 API34 emulator methods with 0 failures, errors or skips and exactly 42 PNGs, all 320×640. The complete 42-image contact sheet and full-resolution UIMSD text-entry view were reviewed without a blank, malformed or visibly clipped capture; logcat contained no fatal app-exception, ANR, native-abort or fatal-signal signature.
- Produced APK: `ADHD-Pixel-Dungeon-CODEX-TEST-240.apk`, 38,589,474 bytes, SHA-256 `46666aaad0a2a21a859ba57156eb8789ba704b93df67775f5448797091c28f3a`. Compiled version `4.0.8-apd-INDEV-bC240-TEST`, application ID `com.charltonchris29.adhdpixeldungeon.indev`, and extracted v2 certificate SHA-256 `067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e` match TEST239 identity.
- Artifact IDs / independently downloaded ZIP SHA-256: targeted `12040539281` / `a7008bdd5e4966eb464d3be4bdb5065b7ebedc294bbebd874c8944970c73a57c`; core `12041590231` / `29cec783f0c0be0c443cdc1f217b3523277b25455bfe51b2261d6f0ff9143002`; APK/history `12040799585` / `5f551fe1fb8f1f0453d5f0a9c2a1181839acc35a79ecb809accc036b072da7e5`; Android/visual `12042111015` / `d95ce848a1b8e72fb221acdcc7a41493063eca1b1d7a9400e8b95a74283152cb`.
- Scope remains only uncertainty disclosure for per-distinct aggregates with hidden, unknown or lower-bound identities. Their rows and prior numeric results are preserved. Raw histories, saved views, caches, saves, original scores, gameplay, all 18 rules, turns, RNG, all 50 Cleric lore entries/private grudge/opt-out, signing and application ID were not changed.
- TEST240 is a fully CI-verified candidate only. No publication, tester distribution, physical-phone result or owner-device install-over/save acceptance occurred. TEST231 remains the latest privately delivered fallback.
- Do not rebuild unchanged TEST240 solely to record evidence. Continue the broad UIMSD reliability audit; audio and unconfirmed armor/sentry acceptance remain open.


## TEST241 circular sentry correction verified and staged — October16 morning
Exactsourceb5d562ca37fff9ac628af3f4ade731e9fa7d83f1/fullrun38894980574 succeeded11:37:18–11:50:47UTC. Independently parsed1716unique core methods and33API34methods,zero failures/errors/skips; debugassembly and completeimmediate-previous TEST240historypassed. All42PNG decode320x640; completecontactsheet and fullresolutiontechnicalhistoryreview passed, and app loghadno fatalexception/ANR/nativeabort signature. Dedicated38894980632 artifact12042934563 ZIP05e4a4841cdbb3b2d7e4dae71152f7dd6a42276b8271801787cb3741ccd7c5a0 independently reproduced4assertionfailures+1healthycontrol in unchanged37a193 and passed1716after. Do notaddtargeted/localcounts tofullsuite.
Genericstage38895319271 at4e00c48ff3e2c9b73a44927278d600e3a4e5960c reusedexact241APK,no rebuild. APK SHA99ca269dbebcd19266dbf4e27aca798252ed3d3b021f97b730915cbbea52b7f4; appIDcom.charltonchris29.adhdpixeldungeon.indev/version4.0.8-apd-INDEV-bC241-TEST/code920; actualsignaturecertificate067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e matches published231. SourceZIP7ee00690114432093be13ceed6230242fbb1b64ced2d4bf2223cc894d45037b7 independently containsactualroominterval andsave-loadrepair. SevenpagePDF20382bytes/SHA56a13e2a4e823d639180843e3ab547ef1f9d11fbcf877f0edacdcaf20dc1b87a extracted andfirstpage reviewed. Fullstage12043559007 ZIP41fcc894448ec71617378bbef5c5ed82b9d1fbe1fdfd4ca0500d69c68c9d9924; evidence12043828065 ZIP55176b3aeb734d39ffa413fbd32f5837b56700078840db77068b78c3c1a5c6e6. Filenames staged-alpha-preview241 and staged-alpha-preview241-evidence. Useexistingartifacts fordelivery; neverrebuildjusttosendreport. Machine receipt docs/APD-TEST241-VERIFICATION-2026-10-16.json.
New128-seed independent generationcomparison preserves map,pattern,nextRNG digest6078e48df512d854ff5cc8d5778686e6547ad2c4315cc665667c0097311214da; original128interval1/corrected128interval2. No unrelatedupstreamhazard/pattern/lootbalancingchanges. RealprovidedcompletedDUELISThistory seed383073447724 spansTEST10→79, containscircleVaultatdepth19branch1,and higher-tierarmor generated/seen/acquired records. Thisdoesnotestablishitisthespecificmissing-armorrun; do notdeclarethatreportresolved orchange lootprobabilities. Rawhash008cd75a5a2d4f20b6c59a573c1e7a9349e6a1678c046da03016f9b3beb8484f unchanged; read-onlyevidence docs/APD-REAL-RUN-ARMOR-EVIDENCE-2026-10-16.json.
Privatepublication stillPENDING; keep published231fallback, physicalphone/owner-saveacceptance andbroaderauditunfinished. Do notpublishearly. ActualtaskAPIupdates succeeded for PDF11:55 and APKnoonAmerica/New_York (APKwasstill11:59afterearlierinterruptedupdate). Both nowexplicitly requirefallbacklinkdelivery regardlessunfinishedwork andnotwaitingforanotherownermessage. Thescheduleconfiguration isverified, not proof offutureon-timeexecution orrepairedpriorinvocationfailure. No main/Claudeoverwrite,paidservice,publicrelease,automatictesterdistribution,rawhistory/inventorydeletion,scoring/slider/lorechange.


## TEST242 lower-bound IN-filter uncertainty candidate — 2026-10-16T12:35:52.018Z
- Confirmed controlled UIMSD query defect: numeric IN returned an exact match for a lower-bound minimum and exact non-matches for higher candidates that the true value could equal.
- Corrected only QueryEngine's numeric IN branch: any candidate >= the bound is uncertain; all candidates below the bound are impossible. Added lowerBoundInFilterOnlyRejectsImpossibleCandidates covering minimum, higher and impossible sets.
- Pending evidence: exact TEST241 before-case and full1717core/debug/history/33API34/42PNG. No application artifact, publication, tester distribution or physical-phone result is claimed yet; TEST241 remains the verified candidate and TEST231 the delivered fallback.


TEST242 attempt1 evidence: run38901726106 stopped before tests because the candidate ledger omitted seven reviewed TEST241 preparation/evidence paths since the immediate previous APK source. Run38901726126 stopped at the pinned-source staging guard because application inputs changed. Both failures are legitimate fail-closed checks; no APK/staging/publication occurred. Follow-up records all compared paths without weakening guards.


## TEST242 audit candidate rejected — 2026-10-16T12:43:28.551Z
- Dedicated38901726173: the copied method failed once on unchanged TEST241 as expected, then the corrected complete suite ran1717 methods with1 failure in the same method. This showed the test premise, not production behavior, was wrong: numeric IN is not an allowed NUM operator, so the engine leaves an invalid filter unapplied.
- Reverted QueryEngine, QueryEngineTest and the dedicated workflow. Attempt1 full38901726106 stopped before tests on incomplete history paths; stage38901726126 stopped at its changed-tree guard. Attempt2 runs38902077854/38902077124 were already started and are not candidate evidence regardless of outcome. No eligible APK/publication/phone claim.
- TEST241 remains the verified application source. This rejection preserves honest failure evidence and avoids changing behavior for an unreachable query.


Rejected TEST242 terminal evidence (2026-10-16T12:45:33.417Z):38902077854 passed strict history/tool validation then failed core; APK/debug/history-compile/Android/delivery skipped.38902077124 failed its application-tree guard; staging/report uploads skipped. No APK produced or staged. Current app source equals TEST241.


## 2026-10-16T13:36:47Z — Early-alpha roster acceptance gate prepared
- Added `EarlyAlphaRosterDeviceTest` and a fail-closed workflow pinned to TEST241 source/run/artifact/checksum.
- Authorized scope is test-only: six classes start/save/continue with 18 locked values, difficulty 5, nine challenges and usable controls; relevant boss/rule headless suites rerun.
- No application-source change, APK rebuild candidate, publication, tester distribution or physical-device claim. Remote execution pending.

2026-10-16T14:13:02Z SIX-CLASS FALLBACK ACCEPTANCE VERIFIED: run38913239490 reused exact TEST241 APK/source and passed10 targeted headless methods plus1 API34 method covering all6 shipped classes start/save/continue, usable controls, exact18 locked rules, difficulty5,9 challenges and saved HP after preference changes. Artifact12050989389 ZIP5d20c2586ff4ad3889315ae6750dd80d7803508dbe9e7b846e9e641687b0c44d; clean fatal/ANR/native/instrumentation crash scan. No application rebuild/publication/physical-device claim.

2026-10-16T14:18:24Z TEST247 SUPERSEDED: source835bf2ba6d25898e2612900d90258b7b1dcf9a6d/run38913947244 passed1728core/debug/history and produced APK artifact12050814748,38,607,556bytes,SHAa0c4e72e9b7e401e5b382e0fc5572d69ae35df299428c0c18a86bee3420e9fa9; Android cancelled at emulator boot after newer malformed-score hardening, so0 Android/PNG evidence and no delivery. Retirement run38914520685 preserved TEST247 as the actual immediate predecessor. Next gate requires1729core/34API34/44PNG against TEST247; verified241/published231 remain fallbacks.

2026-10-16T14:36:09Z TEST249 VERIFIED AND PRIVATELY STAGED: exact source37a8f6d16cd497e13ed581e887ed9d4858fe97aa/run38914965700 passed1729core/34API34/44validPNG, debug and compiled-history against actual TEST247. APK artifact12051339058,38,608,287bytes,SHA dcda79a5c6c83b1ac88e5294b3b134c8e1483109929b3a8da5365e7eadc8536e; core artifact12051119818; Android artifact12052683420; clean crash scan and visual review of all44 screenshots. Private stage38914965883 passed signer/appID, checksum, rollback, lore and screenshot checks; staged artifact12052344817/evidence12052693358. No release/distribution/physical-device claim. Next safe step: owner-controlled physical install-over/save acceptance, or continue approved gameplay backlog without rebuilding unchanged source for evidence-only docs.

2026-10-16T14:50:21Z TEST249 SIX-CLASS SCORING/SAVE ACCEPTANCE VERIFIED: test-only commit10335c1f6892e418ef481f1317c7c48889e1c232/run38918464322 reused exact source37a8f6d16cd497e13ed581e887ed9d4858fe97aa/build38914965700/APKartifact12051339058/SHAdcda79a5c6c83b1ac88e5294b3b134c8e1483109929b3a8da5365e7eadc8536e without rebuilding. Headless acceptance and API34 instrumentation passed0failures/errors/skips: all6classes start/save/continue retained exact18locked rules,difficulty5,9challenges,savedHP and frozen score recordv2/EXPERIMENTAL/calculation-version/exact combined multiplier after neutral next-run preference changes. Artifact12053802684,132973bytes,digest3739b2f409ffd58114404ddd37703c1bb1e678bf7665706b79e052d6d3e1f41b. Emulator only; no physical-phone claim.

2026-10-16T14:50:21Z TEST249 OWNER DRAFT VERIFIED AFTER BOUNDED WORKFLOW FAILURE: run38918379865 created draft release415070992/tag apd-alpha-preview-249 with6 exact assets, then failed only because GitHub's tag endpoint returns404 for unpublished drafts. Workflow-only commit0b3aa77595ff01dce8f5fbc911ab74101b9e2d81 changed verification to an idempotent unique-draft list lookup. Corrected run38918857771 passed all source/run,signer,appID,checksum,rollback and asset assertions; receipt artifact12053167843,1970bytes,digest2a350951baa997a1e6d155ac7259e3052453be60a6286be183b026d7e880e4cb. Draft=true,prerelease=true,published_at=null,public=false,tester-distribution=false,application-rebuilt=false. TEST231 remains latest privately published fallback. Next safe acceptance is owner-controlled physical install-over/save continuity plus real-phone playthrough/audio/armor-report review.

2026-10-16T20:45:26Z TEST255 UIMSD GROUP-COMPARISON CANDIDATE: diagnostic commit df6d2a4f0e898f52c0e1227ed19c7c71b23b2c63/run38964285358 reproduced one retained failure (expected group-B first Average20, received later Average200),0errors; artifact12073194262/ZIPsha137c7a8b59b13049b65e0bee815c57069668bb0bb1f69dcb67d95479d7ea9669. The wrapper also failed its literal message check; failure evidence is preserved. Candidate pairs non-section detail lines by shared definition order and retains the test. No histories/saves/scoring/gameplay changes. Full1737core/debug/TEST254-history/API34 visual gate pending; published254 remains fallback; no phone/distribution claim.

## 2026-10-16T22:44:54Z — TEST261 private owner prerelease published

- Exact application producer: `7368f13940690e599ca05c47a1cf29cca6beed09`, run38975391839. Core1756/0fail/error/skip, debug and complete TEST259 history passed. Raw APK38,589,942bytes/SHA-256 `71eb6e057d4cdbaa194c8b37e9d243079e7e0849afc6eb164fd66c9187844833`; APK artifact12078787669 ZIP digest `sha256:136c832862c6068234191a4fbd71217fb59e1f25b0cd280fea1de9068d8052e9`.
- Producer workflow remains failure: Android test compilation failed before instrumentation on a fixture checked exception. Test-only source `655128758111d740994d86bdbfe1f8e819e3605a` preserved identical application inputs; run38975988571 passed37/37 required methods and57/57 required320×640 captures against the unchanged signed APK. Full artifact12079970767 digest `sha256:b567ba3fe99b06cda42652fd6bd22f2b493dd357aedf0d625769ac90b9280072`.
- Four extra display profiles passed in that run. First320×480 attempt failed1method because a photographed Quickstep launcher ANR blocked touch; failed artifact12079470445 retained. Exact same APK passed clean small-emulator retry run38976735864/source`8a44f5d2923e4390164817962944b3b9421e785f`,1method/4captures; artifact12079641116 digest `sha256:79585d96161df00f85fe07ea27857dffaef6cb0d18ad690f272fbf72892898cd`.
- Accepted total:37 distinct Android methods,42 executions,77 required images. Publication verifier recorded decode/manual review. No physical-phone claim.
- Private prerelease published 2026-10-16T22:44:46Z by run38977482912 at https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/tag/apd-alpha-preview-261 . Six assets verified after download; APK/PDF digests `sha256:71eb6e057d4cdbaa194c8b37e9d243079e7e0849afc6eb164fd66c9187844833` / `sha256:75639a3e68bd19141ea635fbcb3d09721327e55fe9b593a4ce2979388d0c73d2`. App ID/versionCode/certificate match delivered TEST258; install-over required.
- Preserved actual failures: TEST260 core1754/1756 and no APK; producer native compile failure; first small-display Quickstep ANR. No public release, automatic tester distribution, phone/owner-save claim or backlog-complete claim.


## Verified and delivered TEST262 — 2026-10-16

Actual application sourcee4ee7e6d213a997163ecc2520da0eea309a1a819/run38977979688: complete1761core and37required API34 methods pass,0failure/error/skip. Five new permanent regressions reproduce the old five announcements then verify silent already-playable classes and retained original unlock policy. All six classes remain available. Original signed APK reused for recovery38979689121/sourceefa5443b9f59d24a173d0699829a887f34a28713: three existing methods pass, including actual gameplay unlock validation, recovering five images omitted by the primary collector. All57required captures reviewed. Total accepted Android executions44,37unique identities, plusone failed supplemental square-display execution retained separately.

Original staging38977979687 remains failed at52-versus57 images; source collector corrected for future checks. Supplemental matrix38977979689:4pass/1fail; captured/logged Quickstep launcher ANR blocked square-display input. Small-display Options image shows title rather than dialog despite passed functional checks; not claimed as visual settings proof. Published PDF/evidence retain these limits. No physical-phone or owner-save install-over verification. Only production changes from261:18 added Badges.java lines and curated Bug Fixes note; no class availability, progression policy, saves, stats, scores or gameplay mechanics changed.

Private publication38980234369/sourcef8650a356fda9201ca66b780cbd7d4272cd56f02 completed: published23:14:03Z, APK/PDF actual downloads rehashed23:14:07Z, links sent between23:14:51Z–23:15:12Z. APK https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/download/apd-alpha-preview-262/ADHD-Pixel-Dungeon-Alpha-Preview-262.apk PDF https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/download/apd-alpha-preview-262/APD-Alpha-Preview-262.pdf SHA256 5ad5beb717673a51a10a646c28b887ed54a4f78f8d25ee351834a39a29ecf8d2. AppID com.charltonchris29.adhdpixeldungeon.indev/versionCode920/cert067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e match retained261. Install over; no uninstall/data clear. Receipt docs/APD-TEST262-OWNER-DELIVERY-2026-10-16.json. No public/paid/automatic tester distribution. Latest verified published owner fallback is262; preserve261/258 too. Reuse262 if no source change; no redundant new build for this evidence update. Broader backlog remains open.

Separate inherited-audio diagnostic evidence reconciled: optional fake-endpoint Android method passed on unchanged259, proving only deliberately blocked worker queue behavior; no audible reproduction or production audio correction. Keep reported RedMagic/Prison/Tengu issue open and not region-exclusive. See audio investigation document.

## 2026-10-17T04:10:11Z — TEST264 aggregate provenance verified and privately staged
- Confirmed and corrected a bounded UIMSD display-provenance defect: imported-ranking aggregate flags were computed but ignored by `CellFormatter`. Legacy-only aggregates now use the existing legacy state; mixed provenance retains lower-bound/inferred priority and explicitly notes imported ranking data. Numeric aggregation, raw history and query math are unchanged.
- Application `4d4048f80ab150aca3bc1ce690de48b5532922a0`; full source `9a712453443573f2027150cb204cb6be304da57c`; run `39000124500`: 1,768 core/350 suites and 38 API-34/14 suites, zero failures/errors/skips, debug and strict TEST263 history passed. Reviewed 57 required 320×640 captures plus 25 captures across five supplemental display profiles.
- APK artifact `12087924665`; file 38,591,975 bytes; raw SHA-256 `e6b65a4a7d48f7a8e9b9d181fff7745f6211b7659012ff29e9c0c0980dc496bb`.
- Independent stage `39001343302` / wrapper `8b1878235df7b538da5c9c970bbc6350dae1e641` reused the exact APK. Preview artifact `12089525962` digest `sha256:035c6802b21f696caa01adebf16233b5bd50f8d7656217046cbec942149c0337`; evidence `12089540854` digest `sha256:7890babf3307da28a2eae17a124e33ead61a1c74654fd8c02adf8051c8dd922a`. Signer/app ID/version/install-over line match TEST262. Automatic stale-profile stage `39000124638` remains failed with no artifact.
- TEST264 is staged, not published or distributed; TEST262 remains the latest published owner APK. No phone/save install-over claim. UIMSD exact-review reconciliation is 98 paths and remains incomplete.

## 2026-10-17T04:18:24Z — UIMSD self-test privacy contract verified
- Reviewed current `UimsdSelfTest.java` plus DatasetStore/QueryEngine/CellFormatter/RunLogWriter interactions; no production defect confirmed.
- Test-only source `5a69400536f44ea04efe35edfb5398aefc0e4917` adds an independent finished-versus-open internal-identity regression. Run `39001854306` / request `d9238c2cb1e8a9345deee4c3b39b2963318066d5` passed 1,769 tests in 350 suites with zero failures/errors/skips; the new method passed in 0.005 seconds.
- Artifact `12089406326`, 829,331 bytes, digest/ZIP SHA `sha256:8beb240b624e6b48e1578edab42fccbcf3cf28aac855960d5cf1b167dbb3d1d8`.
- No application source, executable APK request, APK, history/save/scoring/gameplay/signing/app-ID/publication state changed. TEST264 remains staged; TEST262 remains published. UIMSD exact review is 99 paths and remains incomplete.

## 2026-10-17T04:46:02Z — UIMSD Home finished-run scope contract verified
- Reviewed current `UimsdHome.java` (`71fd5b3b626ccc7cf388445c34ad7d57677a86b7`) and its Catalog/DatasetStore/QueryEngine presentation paths; no production defect confirmed.
- Test-only source `914586aa83175b54528b2bbf59a2db71a4663bd7` adds `homeScopeExcludesUnfinishedRunsAndCountsOtherFinishedRulesExactly`: current/all-rules counts, other-rules disclosure, exact visible finished total and unfinished-class exclusion.
- Core-only run `39003588552` / request `01a32c305da5f97747c5d73c32762e80dd27f8c6` passed 1,770 tests in 350 suites, 0 failures/errors/skips. New method passed in 0.008 seconds.
- Artifact `12089109846`, 826,513 bytes, digest/ZIP SHA `sha256:98c9e31f6bc7a3ea9810bfc952026548983877dfac6266c26310c53e7cea710b`.
- No application source, executable APK request, APK, history/save/scoring/gameplay/signing/app-ID/publication state changed. TEST264 remains staged; TEST262 remains published. UIMSD exact review is 100 paths and remains incomplete.

## 2026-10-17T05:36:53Z — UIMSD query serialization contract verified
- Reviewed current `QueryCodec.java` (`3a643d13367ca9ef51cc109667fc7bf7b52ee368`) with Query/Filter/Aggregate/SavedViews consumers; no production defect confirmed.
- Test-only source `c2014077526dfa87b6062ab1762548d74d632290` adds `queryCodecPreservesAllEscapedFieldsAndEmptyTextCardinality`, covering every encoded delimiter, empty-text cardinality, aggregate metadata/extreme values and stable repeated encoding.
- Core-only run `39006675168` / request `27de932775034c51ba7ea5c33063056204142d77` passed 1,771 tests in 350 suites, 0 failures/errors/skips. `QueryEngineTest` passed 21/21 methods.
- Artifact `12091417221`, 828,440 bytes, digest/ZIP SHA `sha256:4c94568a467fcfe8198024a5372ab6a0eb4e0269d7ffaa6067f23f3a412e6bc3`.
- No application source, executable APK request, APK, history/save/scoring/gameplay/signing/app-ID/publication state changed. TEST264 remains staged; TEST262 remains published. UIMSD exact review is 101 paths and remains incomplete.

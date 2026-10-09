# APD bug log — October 9, 2026

Branch: codex/apd-phone-fixes-2026-10-09. Physical-phone testing is not claimed. Final C5: 894 core tests and 3 targeted API 34 checks pass, zero failures/errors/skips. The owner-facing causes, fixes and side effects are in APD-BUG-FIX-REPORT-2026-10-09.md.

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

# APD plain-text fact metadata boundary, November 15, 2026

Initial checkpoint: Sunday, November 15, 2026, approximately 1:05 AM EST.

- Authoritative source base: `b433929e961b1c26eb4d5ed4f137e21defc43b37` on `codex/apd-phone-fixes-2026-10-09`, verified from live GitHub. Do not treat `main` as the continuation. Isolated work branch: `chatgpt/apd-text-fact-metadata-20261115-0100`.
- Existing report Markdown metadata, CSV comment, and sparse table fixes are already merged. The open PR inventory showed no active report-rendering claims at this checkpoint. Preserve all concurrent playback, heap/C467 investigation, native test and release work.
- Evidenced defect: plain-text report Fact section headings, labels, and parenthetical detail directly append embedded CR/LF into a format whose Fact contract calls for one row per fact. That permits a label/detail to appear as a separate fact or section. This is an export-presentation integrity issue, not a gameplay, save, history, seed or scoring issue.
- Narrow change: normalize CRLF/CR/LF to single spaces at plain-text Fact metadata rendering only. Retain actual data values verbatim, report title/headings/about/notes behavior, all other export formats and original in-memory Report.
- Two JUnit regressions assert fact-row containment and normal/legacy multiline-value compatibility. Exact source workflow requests full core JUnit and desktop/Android debug/release/instrumentation Java compilation. Until its terminal result is inspected, tests and production compilation are **not yet verified**.
- No APK built, signed, device-tested or published as part of this package. Signing inputs/native acceptance and install-over/save gates must be independently verified for any release. Retain C471/P471 candidates and prior releases unchanged.
- Next: inspect actual CI and JUnit artifact, correct genuine failures without weakening tests, recheck current live branch and conflicting work, then integrate only the exact green tree through guarded non-force expected-head update. Record final Eastern time, test/build/publish evidence and next safe work here.

# APD CSV report comment-line boundary continuation, November 13, 2026 (Eastern)

## Isolated work claim, approximately 04:03 EST

Original regular ChatGPT APD development conversation. Verified authoritative branch `codex/apd-phone-fixes-2026-10-09` at `426a90c8cf7c9e041b2e2e3188557b50b879a1b9`, following merged PR #101/#103 and earlier archives and report hardening. Isolated work branch: `chatgpt/apd-csv-comment-boundary-20261113-0403`. No main changes, APK rebuilds, signing changes, gameplay, balance, scores, saved data, or concurrent package changes.

Evidence from current `ReportRenderer.csv(Report)`: table/facts/no-table comment headings append mutable report/table title strings directly into unquoted `# ...\r\n` records; the final comment appends an app-version-derived footer raw. A title with CR/LF can thereby inject a second physical CSV row, including a formula-looking row, even though ordinary CSV *cells* already have formula and CR/LF protection. This is a source-code finding, not a claim of observed user data or exploit.

Bounded work: keep all CSV data cells, report values and normal titles exactly unchanged; flatten only CR/LF in CSV **comment fields** into spaces so metadata cannot create an unquoted extra row. Add focused regression tests for report title, table title, optional version footer, no-table and unchanged ordinary output. Run exact-source core, desktop and Android Java gates before any integration. Do not touch independently active playback, hero selection, release/native acceptance, GPL, seed or wall-search work.

No implementation, passing tests, APK, original-signer verification or publication claimed at claim time. Use current remote expected-head non-force integration only after source verification. Record execution start/end and actual results below.

# APD report short-row export continuation, November 13, 2026 (Eastern)

## Isolated work claim, 06:14 EST

Authoritative source branch: `codex/apd-phone-fixes-2026-10-09`; verified starting head `bf6e5e58dd2f24abede89a67d055e0686f211b19`. Isolated branch `chatgpt/apd-report-short-rows-20261113-0615`. Keep all gameplay, balance, scoring, saves, history, canonical provenance, original signing and qualified APKs untouched.

Distinct source-observed defect: `Report.Table.rows` is a mutable list of cell arrays with no requirement that a row contain as many cells as the header list. `ReportRenderer` currently renders only `row.length` values in Markdown, text, HTML, CSV. When a row has fewer values than column headers, CSV records have the wrong number of fields and Markdown/HTML columns are incomplete. Report missing positions as **blank cells, not unknown measured values**, in all four output formats, preserve extra data cells and original report objects unchanged. Do not repeat already merged null-cell, coverage, CSV provenance or Markdown escaping work; this is an omitted-position boundary, not a null in an existing position.

Scope: `ReportRenderer.java`, a new narrowly scoped regression test, an isolated exact-source CI workflow, this continuation receipt. No gameplay or format schema changes. Verify all core JUnit and Android/desktop Java compile before proposing integration; no native APK acceptance or publication is assumed. Recheck active claims, PRs and authoritative live head before any expected-head non-force merge. APD464 and open playback/door/UI claims are independent.

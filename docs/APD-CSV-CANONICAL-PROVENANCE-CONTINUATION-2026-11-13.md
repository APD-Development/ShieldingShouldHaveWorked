# APD CSV canonical provenance continuity, 2026-11-13 Eastern

## Isolated work claim, 11/13/2026, 05:29:31 EST

Original work context: existing APD regular ChatGPT development thread. Repository APD-Development/ShieldingShouldHaveWorked. Started from freshly verified authoritative `codex/apd-phone-fixes-2026-10-09` at `c20266a24b772831153fd5a0bc7bda50aa0dc08a`. Isolated branch `chatgpt/apd-csv-canonical-provenance-20261113-0530`.

Source-evidenced bug: `docs/APD-REPORTS.md` requires a prominent canonical provenance notice in every export format. `ReportRenderer.markdown`, `text` and `html` emit `CANONICAL_NOTICE`; `csv` only uses a block-local provenance label and a terse legend. Existing `ReportTest.seedFloorsReportKeepsRecordedAndCanonicalApartEverywhere` accidentally substitutes Markdown when CSV is nonempty, thereby failing to test CSV's missing notice. On sharing canonical seeded-floor data as a standalone spreadsheet, the explanation that seed replays are *not recorded* and *not what the run would have met* is absent. Preserve all data, statuses, rows, formulas, numerical rules and existing export output for recorded-only reports.

Scope: prepend a physical single-line `# ` CSV comment with the existing `CANONICAL_NOTICE` *only* when `r.hasCanonical()`; place it after synthetic notice if present. Reuse `csvComment` to keep physical CSV lines intact. Correct the mistaken CSV test slot in existing `ReportTest` and add small targeted tests for canonical, mixed, recorded-only and synthetic ordering. No other renderer, source data, game mechanic, balance, save, scoring, signing, edition or APK changes.

Coordination: checked open PRs and work claims before this branch. PR #111's Markdown coverage guard has now merged independently, and all other open claims concern visual speed, hero UI, search, GPL or challenge. This task owns only CSV canonical provenance. Never update an active sibling branch or bypass original signer/native Android/release gates.

Verification: exact-source, source-only CI must run full core JUnit and desktop/Android Java compile; inspect complete terminal result, required regression test cases, JUnit XML, receipt and live authoritative head before guarded non-force integration. Do not mistake source CI for APK or device acceptance. At claim time no application code had yet been modified, no tests executed and no APK built or released.

## Execution result

Pending. Fill only with subsequently verified actions and results, including start/end SHA, commits, CI, release boundary and next safe owner.

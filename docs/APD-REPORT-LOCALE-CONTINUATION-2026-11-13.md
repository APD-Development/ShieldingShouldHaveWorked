# APD plain-text report locale stability, November 13, 2026 (Eastern)

## Isolated independent work claim, 07:31 EST

- Work context: originating regular ChatGPT APD conversation; no separate Codex task. Verified authoritative `codex/apd-phone-fixes-2026-10-09` at `fca14d4ece0928b56b174fb69819290994b5eaf6` immediately before claiming, UTC receipt 2026-11-13T12:31:08.477Z. Isolated branch `chatgpt/apd-report-locale-20261113-0730` starts at precisely that head. Do not advance the authoritative branch without exact-source terminal verification, fresh claims/head reconciliation and expected-head non-force update.
- Read root `AGENTS.md`, `docs/WORK-COMPLETED-LEDGER.md`, `docs/KNOWN-ISSUES-AND-SOURCE-GUIDE.md`, `docs/HANDOFF.md`, current `docs/APD-REPORT-FILENAME-CONTINUATION-2026-11-13.md`, report format guidance, `Report.java`, `ReportRenderer.java`, and relevant report tests. Live open PRs #118, #48, #47, #38, #33, #22, #20, #6, #3, #2 own playback, native UI, Search, license/challenge work. The newly merged #125/#126 own long export filenames. Search for report-locale PRs found none.
- Narrow source-observed defect: `ReportRenderer.text` uses default-locale `String.toUpperCase()` for report titles and level-1 headings. Thus an unchanged report containing the letter i can export different plain-text bytes in Turkish versus English runtimes, contradicting the renderer's documented deterministic output contract. This is a source finding; no owner-device reproduction claimed.
- Approved-scope correction: change only those presentation-only uppercase calls to `toUpperCase(Locale.ROOT)`, with an explicit `java.util.Locale` import. Preserve report data, ordinary English output, Markdown/HTML/CSV, all game/save/history/scoring/balance, signer, application IDs and edition boundaries. Add a locale-sensitive regression with full default-locale restoration and ordinary-output checks.
- Gate: new isolated exact-source workflow will run all core JUnit tests, desktop and Android debug/release/instrumentation Java compilation, verify zero failures/errors/skips and the new regression. Initial state: not run; no APK or native acceptance. This branch is separate from the APD465 native/original-signer gate and previously published APD464, neither of which is changed.

## Execution result

Pending actual source edit, test, exact-source CI, and fresh integration verification. Do not claim success before results are observed.

# APD Markdown raw-HTML presentation boundary, November 13, 2026

## Active isolated work claim (08:03 EST)

- Source starting point: authoritative `codex/apd-phone-fixes-2026-10-09` at `d66fc14d24ba76dda8c50cc8649369a6cca6b31c`, independently verified on GitHub at 08:00 EST. Isolated branch: `chatgpt/apd-markdown-raw-html-20261113-0800`.
- Work package: `ReportRenderer.markdown` currently passes literal raw HTML beginning with `<` through its inline `md` helper; multiline `Report.Note` additionally passes this HTML through `quotedNoteLines`. Because these fields can originate from report text, a Markdown viewer allowing raw HTML could interpret this content as active markup rather than ordinary report data. This claim is **only** for presentation-time neutralization of raw HTML openers (with tests), not a broader Markdown reformatting or report-data mutation.
- Edit ownership: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/apd/report/ReportRenderer.java`, new `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/apd/report/ReportMarkdownRawHtmlBoundaryTest.java`, a dedicated exact-source CI workflow, and this handoff. Do not overlap prior Markdown CR, metadata, Facts, coverage, note-line, CSV, null-cell or locale fixes. Do not touch gameplay/seed/score/history, APK contents, signing, editions or concurrent release packages.
- Checked pre-work: AGENTS.md, completed ledger, current repo branch/commit, 10 open PRs and recent Actions runs. No open PR currently claims this raw-HTML presentation boundary; open personal playback/native/release work is separate.
- Tests and merge: pending; do not claim this claim is complete until full JUnit, desktop and Android Java source checks run successfully. Preserve exact-source and Android native/release gates, original signer, save compatibility, APD464 and failed APD465/APD466 attempts. Re-read live shared head before any non-force expected-head integration.

## Result

Pending source, tests and CI.

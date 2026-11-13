# APD Markdown raw-HTML presentation boundary, November 13, 2026

## Active isolated work claim (08:03 EST)

- Source starting point: authoritative `codex/apd-phone-fixes-2026-10-09` at `d66fc14d24ba76dda8c50cc8649369a6cca6b31c`, independently verified on GitHub at 08:00 EST. Isolated branch: `chatgpt/apd-markdown-raw-html-20261113-0800`.
- Work package: `ReportRenderer.markdown` currently passes literal raw HTML beginning with `<` through its inline `md` helper; multiline `Report.Note` additionally passes this HTML through `quotedNoteLines`. Because these fields can originate from report text, a Markdown viewer allowing raw HTML could interpret this content as active markup rather than ordinary report data. This claim is **only** for presentation-time neutralization of raw HTML openers (with tests), not a broader Markdown reformatting or report-data mutation.
- Edit ownership: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/apd/report/ReportRenderer.java`, new `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/apd/report/ReportMarkdownRawHtmlBoundaryTest.java`, a dedicated exact-source CI workflow, and this handoff. Do not overlap prior Markdown CR, metadata, Facts, coverage, note-line, CSV, null-cell or locale fixes. Do not touch gameplay/seed/score/history, APK contents, signing, editions or concurrent release packages.
- Checked pre-work: AGENTS.md, completed ledger, current repo branch/commit, 10 open PRs and recent Actions runs. No open PR currently claims this raw-HTML presentation boundary; open personal playback/native/release work is separate.
- Tests and merge: pending; do not claim this claim is complete until full JUnit, desktop and Android Java source checks run successfully. Preserve exact-source and Android native/release gates, original signer, save compatibility, APD464 and failed APD465/APD466 attempts. Re-read live shared head before any non-force expected-head integration.

## Source and regression changes (08:04 to 08:08 EST)

- Production commit `b45bb30335ec4643bada066b805edc5c21c73be5`: `ReportRenderer.md` encodes literal `<` as `&lt;` only at Markdown rendering time, and `quotedNoteLines` does the same without flattening note lines. This limits raw-HTML interpretation in Markdown viewers; all non-Markdown formats and stored report fields remain untouched. Markdown escaping of pipes/backslashes, newlines, provenance, numerical marks, and note blockquotes is retained.
- Regression commit `d880c4e29230b3186b340dac6b7f79a5ab119040` adds two tests covering raw HTML in report title, about, heading, facts, table, coverage, footer and multiline notes; confirms original model data, plain text, HTML, CSV and ordinary Markdown data remain consistent.
- Exact-source workflow source commit `b93944950a2225ab06406b3bcb8c4549af197947`. [GitHub Actions 43430568572](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43430568572) finished **success** at 08:07:37 EST on November 13. Job `131902722783` logged `BUILD SUCCESSFUL in 2m 4s` and `EXACT_APD_MARKDOWN_RAW_HTML_JUNIT {'tests': 3440, 'failures': 0, 'errors': 0, 'skipped': 0} suites 514`; desktop and Android debug/release/instrumentation Java compilation passed. Both new test names were required by CI. The JUnit artifact `14039442495`, `apd-markdown-raw-html-junit`, is 561,495 bytes with SHA-256 `abf7a8293f964da2309ace04a1ce3d560944d9338308bd3b1361a7f9873dd964`.
- Local full Gradle, emulator, physical RedMagic, original-signer, install-over and APK publication acceptance were **not** run. This is source verification only; APD464 remains the older published executable and APD465/APD466 native/release attempts belong to separate work. No existing task schedules were modified.

## Integration

Pending guarded integration. Before touching the shared ref, recheck the live authoritative head, isolated head and PR changes. Never force-push or bypass the original signing/native/publishing gates.


# APD Markdown orphan-coverage boundary, November 13, 2026 (Eastern)

## Independent source work claim, approximately 05:13 EST

Continue the owner's existing APD repository and regular ChatGPT development, not a new game or separately owned implementation. Before editing, read AGENTS.md, current development-branch head, current open PRs/work claims, the final Markdown coverage receipt, recent source CI and releases. The authoritative branch `codex/apd-phone-fixes-2026-10-09` was verified at `c519643dece90e80b51d12dbe7b132a9e4ab122e`. This isolated work branch was created directly from that commit. No live open PR owns malformed report-table coverage-list resilience. Draft playback, HeroSelect, release/native, GPL and challenge work are independent.

**Source-evidenced failure:** `Report.Table.headers` and `Report.Table.coverage` are independently mutable public lists. HTML already bounds coverage lookup by headers, but `ReportRenderer.markdown` loops over `t.coverage.size()` and unconditionally indexes `t.headers.get(i)`. A report with one valid header and an extra orphan coverage entry therefore throws `IndexOutOfBoundsException`, preventing export. This is a source-deduced input-shape defect, not a claimed observed incident; legitimate generated reports are expected to have parallel lists.

**Approved bounded reliability scope:** Make Markdown summarize coverage for header-backed columns only. Preserve ordinary table text, valid coverage strings, empty coverage handling, HTML, CSV, plain text, source report values, gameplay, balance, saves, history, scores, signing, app IDs, editions, release gates and all other work packages. Add focused test cases for an extra coverage item and legitimate/absent coverage. Verify exact-source full core tests and Android/desktop Java compilation on an isolated CI workflow. Integrate only after terminal passing evidence and a fresh expected-head non-force check. Never publish an APK from a source-only test.

**Initial execution state:** branch created. Production change, tests and source CI still pending; no APK assembled or published by this execution. This record must be completed with exact commit IDs, tested source, verified outcomes, blockers and next safe work before ending.

## Isolated implementation and exact-source verification requested (approximately 05:18 EST)

- Claimed branch from authoritative `c519643dece90e80b51d12dbe7b132a9e4ab122e` at isolated claim commit `a94a8f1ffe5ce53bbd3ea13ba28501496b18c3a6`.
- Production commit `96e25c4e21a4412c50e090c427db07cbd758a4e1` bounds Markdown coverage summary iteration by both the header and coverage list sizes, without modifying stored report data or any other renderer.
- Regression commit `ee9ac3552d95a9173328270ed1ac5dbcf7371369` adds three focused JUnit methods for one extra orphan coverage field, zero header columns with orphan metadata, and unchanged parallel/empty coverage.
- Exact-source workflow commit `7024d18cef1a8c0097fa0f2005e15be37a193809` adds a dedicated complete core/JUnit and desktop/Android Java CI. [Actions run 43408075003](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43408075003) was **in progress** at this checkpoint; no terminal conclusion or test totals may be claimed from this line.
- [Draft PR #111](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/111) remains unmerged pending exact-source CI. Comparison to the frozen starting SHA shows four intended paths and four commits, zero behind. Concurrent [draft PR #110](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/110) edits a different Markdown note-emission statement in the same `ReportRenderer.java` file; these changes are logically independent but require actual head reconciliation if PR #110 integrates first.
- No APK was assembled or signed; native device tests, screenshots, install-over/save acceptance and publisher gates have not occurred. APD464 remains published with unmodified bytes and excludes this correction.

Next: verify terminal exact-source CI job, all three JUnit names, core suite totals and compilation; inspect final branch heads and PR #110 for race changes; do not merge until expected-head safe integration can preserve both source changes. Keep all other work claims and releases intact.

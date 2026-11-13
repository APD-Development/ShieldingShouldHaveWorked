# APD nullable report table-cell rendering continuation, November 13, 2026 (Eastern)

## Isolated work claim, 06:00 EST

Starting authoritative `codex/apd-phone-fixes-2026-10-09` GitHub head: `55b7168bdc66190ae395abe3e91a6770e5e050a0`. This claim belongs to the existing APD regular ChatGPT conversation and is isolated on `chatgpt/apd-report-null-cells-20261113-0600`.

Evidenced source defect: `ReportModelTest.statesUsedIncludesFactsAndTablesButIgnoresSectionsNullsAndInvalidStates` deliberately constructs a `Report.Table` row containing a null `Report.Cell`, and `Report.statesUsed()` safely ignores that slot. All four `ReportRenderer` output paths currently dereference table cells without a null guard, so attempting to export such a model throws instead of producing a report. Handle these sparse slots as blank display values, without relabeling them UNKNOWN or changing the underlying report, counts, canonical warnings, normal nonnull output, statistics, history, game balance or save data. Add focused four-format regressions.

This claim is distinct from already integrated CSV canonical disclosure, Markdown note escaping, orphan coverage and other report fixes, and from all open playback, hero-select UI, door search, licensing and challenge claims. No APK or signed/native acceptance is claimed. At this checkpoint no source code or tests have yet changed. Do not integrate until exact-source core/desktop/Android Java verification, rechecked work claims and a guarded expected-head non-force branch update. Preserve APD464 and all prior releases. Do not alter existing recurring APD automation tasks.

## Exact-source verification, 06:05 EST

- Initial source work claim commit: `39335a48ee062f6a210b5f88bfc2650e7bd973f7`.
- Production null-cell renderer correction: `cc7766bf1f227d6dd52b93b1c28553500fbccab4`, affecting only `ReportRenderer`, rendering null table cells as blanks in Markdown, plain text, HTML and CSV. Other input cells remain unchanged.
- Focused three-test class: `6e45d6e31f28521571d20e119d01f078adf569c8`. Tests demonstrate four-format null handling, canonical provenance without invented unknown values, retention of real lower-bound and small-sample marks, and no mutation of source data.
- Exact-source CI workflow commit (tested source): `09904c50a99cc512a0f1a646588f6dcb9d6da3f7`. [Run 43413818105](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43413818105), job `131846603145`, **completed success at 06:05:32 EST**. Raw decoded logs show `BUILD SUCCESSFUL in 2m 17s` and `EXACT_APD_SPARSE_REPORT_JUNIT {'tests': 3427, 'failures': 0, 'errors': 0, 'skipped': 0} suites 508`. Full core JUnit, desktop classes, Android debug/release and debug-instrumentation Java compilation all passed.
- JUnit evidence artifact `14030074080`, `apd-report-null-cells-junit`, 558,480 bytes, SHA256 `cff3b5253725b6134c5552f38b045d9f46aaa5483dc61cbf55481cf07c81b9f5`, is retained.
- Exact isolated comparison against starting authoritative head: four commits ahead, zero behind; changes scoped to source renderer, the new test, CI workflow and this continuation file. [PR #116](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/116) is isolated. No APK assembly, original signing, native/device acceptance, install-over/save verification or publication performed. APD464 remains unchanged.

**Integration pending at this checkpoint.** Any next documentation change is non-executable; exact source CI pertains to `09904c50`. Re-read authoritative and PR heads, check work claims, ensure the source tree is unchanged, and perform a guarded, expected-head, non-force integration only if compatible.

## Final source integration

Pending.


# APD nullable report table-cell rendering continuation, November 13, 2026 (Eastern)

## Isolated work claim, 06:00 EST

Starting authoritative `codex/apd-phone-fixes-2026-10-09` GitHub head: `55b7168bdc66190ae395abe3e91a6770e5e050a0`. This claim belongs to the existing APD regular ChatGPT conversation and is isolated on `chatgpt/apd-report-null-cells-20261113-0600`.

Evidenced source defect: `ReportModelTest.statesUsedIncludesFactsAndTablesButIgnoresSectionsNullsAndInvalidStates` deliberately constructs a `Report.Table` row containing a null `Report.Cell`, and `Report.statesUsed()` safely ignores that slot. All four `ReportRenderer` output paths currently dereference table cells without a null guard, so attempting to export such a model throws instead of producing a report. Handle these sparse slots as blank display values, without relabeling them UNKNOWN or changing the underlying report, counts, canonical warnings, normal nonnull output, statistics, history, game balance or save data. Add focused four-format regressions.

This claim is distinct from already integrated CSV canonical disclosure, Markdown note escaping, orphan coverage and other report fixes, and from all open playback, hero-select UI, door search, licensing and challenge claims. No APK or signed/native acceptance is claimed. At this checkpoint no source code or tests have yet changed. Do not integrate until exact-source core/desktop/Android Java verification, rechecked work claims and a guarded expected-head non-force branch update. Preserve APD464 and all prior releases. Do not alter existing recurring APD automation tasks.

## Verification and integration receipt

Pending.

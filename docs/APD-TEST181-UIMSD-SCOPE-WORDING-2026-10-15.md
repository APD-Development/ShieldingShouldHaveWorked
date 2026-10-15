# TEST181 UIMSD scope-wording candidate — 2026-10-15

## Scope and cause
At live parent `1f52c04d9383bfd6d7a711315e6bcc848c85f74d`, the empty Home said to tap Rules for “other runs” but did not explain that Rules selects rule sets only. A run still in progress is controlled separately by Tables > Options > Include runs still going. Source inspection also confirmed an abandoned run emits RUN_END and is therefore finished; it must not be described as unfinished.

## Implemented
- `UimsdHome.emptyMessage()` provides testable singular/plural empty-state wording and keeps different rules separate from run status.
- `UimsdScene` renders that message instead of duplicating weaker inline text.
- `WndUimsdOptions` states that Rules does not change whether a run is finished and points unfinished runs to the dedicated switch.
- The Home scope note includes runs still going among the normally excluded categories.
- `UimsdHomeTourTest.emptyHomeSeparatesRulesFromRunStatus` covers empty history, one finished abandoned run under different rules, singular wording, unfinished-run navigation and all-rules inclusion.

## Verification state
Implemented against the actual latest parent; expected complete core count is 1,603. GitHub core/debug/immediate-previous TEST180 history/API34/34-screenshot verification is pending. No result is claimed before the workflow completes.

## Boundaries
No raw history, save, statistics calculation, scoring, gameplay, application ID, signing identity or player data changed. No APK was delivered and no physical-phone result is claimed.

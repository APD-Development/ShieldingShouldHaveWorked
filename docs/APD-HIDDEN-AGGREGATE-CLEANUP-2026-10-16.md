# APD hidden aggregate cleanup — 2026-10-16T09:36:49Z

## Confirmed defect
Turning UIMSD hidden information off removed hidden filters, sorts, displayed columns, group keys and aggregates whose primary value column was hidden, but it retained aggregates whose denominator, condition or distinct-by column was hidden. The query therefore still depended on hidden data while the engine silently omitted those aggregates from the displayed result.

## Bounded correction
Hidden-information shutdown now checks all four aggregate column references: value, ratio denominator, conditional column and distinct-by column. An aggregate is removed when any resolved reference is internal. Public aggregates and unknown forward-compatible references keep their existing behavior. Recorded history, derived cache formats, saved-view files and scoring are unchanged.

## Retained acceptance
`UimsdModelTest.disablingHiddenInformationRemovesEveryHiddenAggregateDependency` covers a public aggregate plus ratio-denominator, condition and distinct-by dependencies. Exact TEST238 application source must fail the method; corrected source must pass all 1,706 core methods.

Expected TEST239 gate: one intended TEST238 failure, then 1,706 corrected core methods, debug assembly, complete immediate-previous TEST238 APK history, 33 API34 emulator methods and 42 valid screenshots. No publication, tester distribution, physical-phone result or owner install-over/save claim is authorized by this record.

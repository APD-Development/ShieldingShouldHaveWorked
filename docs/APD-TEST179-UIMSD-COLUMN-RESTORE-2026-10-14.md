# TEST179 — UIMSD column-dialog recreation regression

Started: 2026-10-14T23:39:51Z

## Reviewed issue
The source guide still identified Arrange columns as disappearing when Android rebuilds UIMSD. Current production code now assigns exact restorers to both the Column order list and its Move column child, and PixelScene captures those restorers during scene reset. The retained API34 suite covered Columns selection but did not directly recreate either step of this two-window workflow.

## Bounded regression
The existing `menusSurviveSceneRecreationAndNarrowActionsStayInsideTheirWindow` device test now:
1. opens Column order with a multi-column query;
2. rebuilds the UIMSD scene and requires exactly one restored Column order window;
3. selects the second column, rebuilds again, and requires exactly one restored Move column window;
4. invokes Make first through the restored callback, requires return to Column order, and verifies the chosen column actually became first;
5. restores the original query columns before continuing the existing test.

Production query/window code, player history, saves, scoring, application identity and signing are unchanged.

## Verification request
TEST179 requests the unchanged 1,602 core tests, debug assembly, compiled history against immediate previous TEST178, all 26 API34 methods with the strengthened recreation assertions, and 34 valid screenshots. Candidate remains pending until exact-source evidence passes.

Private delivery is not requested. TEST123 remains the last owner-delivered/signature-compared APK.


## TEST179 exact-source result — fixture failure
Exact source `f6043c7d16cd52e57276113fb038af046ff4ec48`, run `38655567956` (2026-10-14T23:40:55Z–23:52:01Z), concluded failure after the complete device suite. The unchanged 1,602 core tests, debug assembly, immediate-previous TEST178 history validation and private APK packaging passed. API34 ran all 26 methods: 25 passed and this new method failed at `column-order fixture has at least two columns`.

The production Runs query validly represents its default columns with an empty explicit list. The new fixture saved that empty list, restored it after its single-column check, then incorrectly treated it as a literal zero-column layout. This is a test setup defect, not evidence of a production lifecycle failure. Android artifact `11937227267` (ZIP SHA-256 `5e80aad0730b6507fe0cfba1862fae175636d70362eeac2c8f25f5508718a3e8`) retains the exact failure; core artifact `11937375321`; APK artifact `11936159775`. TEST179 is not verified and was not delivered.

## TEST180 fixture correction request
Resolve the effective test layout from dataset defaults when the original query's explicit column list is empty. Exercise the two recreation steps against that effective multi-column list, then restore the original explicit list (including empty/default semantics). Production code remains unchanged.

TEST180 requests 1,602 core tests, debug assembly, compiled history against the immediate previous compiled TEST179 APK, all 26 API34 methods, and 34 screenshots. Candidate remains pending until exact-source evidence passes. TEST123 remains the last owner-delivered/signature-compared APK.

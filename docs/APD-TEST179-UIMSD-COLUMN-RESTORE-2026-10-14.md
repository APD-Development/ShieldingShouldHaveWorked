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

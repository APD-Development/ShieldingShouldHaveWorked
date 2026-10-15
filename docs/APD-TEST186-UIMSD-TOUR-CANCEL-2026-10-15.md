# TEST186 — cancel Back during UIMSD tour preparation

Started: 2026-10-15T01:40:02.195Z

## Reproduced failure
At verified TEST185 evidence head `6f368e1e27cf99ca631c8aa343429c2bba6c86ca`, `UimsdScene.onBackPressed()` returned immediately whenever `tutorialPreparing` was true. Starting the sample-data tour sets that flag before a background worker generates and loads the sample, so both Android Back and the visible exit control did nothing during that interval. The player could not cancel the tour or leave the preparation screen until the worker finished or timed out.

## Bounded correction
- Treat preparation as the first stage of the tour: Back calls the existing `endTutorial()`, returns to Home, dirties the scene, and consumes one navigation step.
- `endTutorial()` advances `tourGeneration`; the already-running worker therefore fails its generation guard and cannot publish a stale sample model after cancellation.
- Preserve the serialized sample cleanup path and its real-history marker guard. No history, save, statistics or sample deletion rules changed.
- Extend the retained API34 Android Back method to begin a real sample tour, cancel it while `tutorialPreparing` is true, require immediate Home/main-model restoration, and prove the worker generation was invalidated before starting the existing completed-tour Back checks.

## Verification request
TEST186 requests the unchanged 1,603 core tests, debug assembly, immediate-previous compiled TEST185 history comparison, all 26 API34 methods with the strengthened Back regression, and the existing 35 screenshots.

This candidate is implemented but not verified until exact-source hosted evidence passes. No gameplay, scoring, raw history, player save, permanent statistics, application ID or signing identity changed. No delivery or physical-phone result is claimed; TEST123 remains the last owner-delivered/signature-compared APK.


## TEST186 hosted result and TEST187 correction
TEST186 source `c1e09d72b48cdd8e534f93818cb763ade84407c9`, run [38666897410](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38666897410) (2026-10-15T01:40:13Z–01:41:01Z), stopped in the reviewed-history guard before compilation. The exact reported omission was `docs/APD-TEST183-UIMSD-EMPTY-HOME-NATIVE-2026-10-15.md`, changed by the TEST185 evidence commit after compiled source `40d31d81de4c2fcc1ae154e9ec0d1c8f6a28d984`. No core, debug, APK or API34 tests ran; TEST186 is not verified and produced no APK.

TEST187 adds that existing evidence path to the complete comparison manifest. UIMSD production and Android regression source are unchanged. The corrected request remains 1,603 core/debug/immediate-previous compiled TEST185 history/26 API34/35 PNG, with no delivery or physical-phone claim.

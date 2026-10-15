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


## TEST187 hosted result and TEST188 runner correction
TEST187 source `44cf2b195229f8909021ed9998f65aba52acb7ce`, run [38667136514](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38667136514) (2026-10-15T01:42:55Z–01:55:00Z), passed 1,603 core tests, debug assembly, immediate-previous compiled TEST185 history, the configured 26 API34 methods, and 35 validated 320x640 screenshots. APK artifact `11942296471` has SHA-256 `e75bb0598f051ea818964010447168a18779190149713cf6e40db16cf05f80ed`; core artifact `11942106695`; Android artifact `11942507561`.

Artifact audit found the explicit API34 class list did not contain `AndroidBackDeviceTest`. The changed regression compiled but did not execute, so TEST187 is not accepted as verification of preparing-tour cancellation despite its successful configured suite and clean 35-image review.

TEST188 adds that existing class to `tools/run-codex-android-checks.sh`. Application and Android test source are unchanged. Require 1,603 core/debug/immediate-previous compiled TEST187 history, all 27 API34 methods including `backWalksBackwardsOneStepPerPress`, and 35 screenshots. No delivery or physical-phone claim.

## TEST188 hosted result and TEST189 fixture correction
TEST188 source `e251bb7a760db8274babdeb3e5356425853670d1`, run [38668463084](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38668463084) (2026-10-15T01:57:54Z–02:10:57Z), passed 1,603 core tests, debug assembly and immediate-previous compiled TEST187 history. API34 executed all 27 configured methods: 26 passed and `AndroidBackDeviceTest.backWalksBackwardsOneStepPerPress` failed before reaching the tour at `AndroidBackDeviceTest.java:138`, timing out while waiting for UIMSD. The Android artifact is `11942149631`; all 35 expected 320x640 screenshots were retained and validated. APK artifact `11942373362` exists, but TEST188 is not an accepted candidate because its API34 job failed.

The exact fixture cause is the regression's use of public `UimsdScene.open(...)`. The private test APK intentionally enables `demoUnlock`, so fresh test settings correctly show the unlock gate instead of entering UIMSD. TEST189 uses `openNow(...)` only after the method has already established a real Title scene; the production entry point and unlock gate are unchanged. Re-run 1,603 core tests, debug assembly, immediate-previous compiled TEST188 history, all 27 API34 methods including the preparing-tour cancellation assertions, and 35 screenshots. No delivery or physical-phone claim.

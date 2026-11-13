# APD hero-select difficulty native fixture recovery, November 13, 2026 (Eastern)

## Isolated claim (08:50 EST)

This work follows draft PR #20's owner-reported difficulty picker fade fix and draft PR #33's native regression. This is a test-only recovery, not a second gameplay fix, new difficulty rule, APK candidate or release.

The exact inherited native fixture head is `61d3be2e309954ffcf25b6ceb154f022e49ed4c0` on `chatgpt/apd-hero-fade-native-regression-20261112-1445`. Earlier exact run [43295613260](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43295613260) compiled the entire existing 3,300-core-test suite without failures/skips, compiled disposable Android debug/instrumentation APKs and booted API34. Its **single native test failed before it could reach the UI**: `HeroSelectDifficultyFadeDeviceTest.java:108` called `SPDSettings.apdDifficulty()` while `Gdx.app` was null, before `ActivityScenario.launch`. The same fixture's original `finally` restored settings after the scenario closed. These are test lifecycle mistakes, not evidence that the production fade works or fails.

This isolated branch moves the original preference/class snapshots **after** actual launcher/game initialization and restores preferences/class **inside** the live ActivityScenario, on its render thread. It leaves all prior UI assertions, the five button/label opacity and touch-state checks, four real screenshot captures and unchanged run difficulty assertion intact. It changes no production source, balance, save formats, signing, application ID or accepted release. It uses a separate branch-specific nonpublishing workflow with the same complete core, Android assembly and API34 actual-device test gates.

## Pending exact verification

Inspect the new workflow's terminal conclusion, the complete raw test result, PNG screenshot transfer and actual pictures, and failure receipts before integration. Do not claim this test proves Android acceptance until it passes. Keep PR #20/#33 isolated until verified; do not overlap APD465/APD466 publishing, and never replace existing signed APKs or recurring APD task settings.

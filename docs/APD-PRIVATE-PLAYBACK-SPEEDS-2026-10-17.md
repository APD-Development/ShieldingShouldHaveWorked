# Developer-build-only extreme visual playback speeds
Date: 2026-10-17
Candidate: pending core-only CI and full candidate acceptance

The owner authorized extending the existing 0.5x, 1x and 2x playback speed control to extreme speeds **only in developer builds**. This is a visual experiment. It must not accelerate actor turns, change loot, RNG, scores, gameplay state, timestamps or global input/HUD timing.

Source implementation: `PlaybackGroup` now centralizes playback labels and factors: 0.5x, 1x, 2x, 4x, 8x, 16x, 32x, 64x, 100x and 1000x. Its maximum index is gated by `ApdFeatures.testBuild()`, which the Android build enables only with `-PapdTestBuild` through `apd/features.txt`. Without that flag `choiceCount()` returns 3, indices above 2 are refused, `SPDSettings.playbackSpeed()` treats stale dev-only selections as 1x, and `PlaybackGroup.speed()` falls back to 1x. Production retains the original three-choice `WndOptions`; developer/test builds show a scrollable `WndUimsdList` with an explicit experimental warning so all 10 options fit smaller phones. The active scene's existing update hook reads the validated preference, so no actor clock or global timescale change was made.

Core tests extend `PlaybackSpeedTest`: private-only visibility and stale release preference, all 10 stored choices and selected factors, 4x–1000x visual speed with actor/global/HUD clocks unchanged. Existing nested group, exception restoring clock and tween tests retained. Native Android `ApdSettingsClickDeviceTest.adultHumorControlWorksWithoutAnAgeProviderAndReopensWithSavedChoice` now tests the 10-row developer speed chooser with taps at 0.5x, 1x, 2x, 4x, 1000x and back to 2x, while retaining the original three-choice production test.

The experimental 1000x option may compress many animations into a single frame. Do not advertise its visual quality or phone stability without device evidence. No APK built or tester distribution occurred in this initial code candidate. Previously compiled TEST269 source/run and verified staged TEST268 fallbacks are preserved; latest published owner APK is TEST262.

#!/usr/bin/env bash
# Full existing checks plus actual Changes screenshots; preserve a failing test exit.
set -u
apd_build_number="$1"
apd_test_status=0
timeout --signal=TERM --kill-after=30 840 ./gradlew :android:connectedDebugAndroidTest -PapdTestBuild "-PapdBuild=C${apd_build_number}" "-Pandroid.testInstrumentationRunnerArguments.class=com.shatteredpixel.shatteredpixeldungeon.android.UimsdRestoreDeviceTest,com.shatteredpixel.shatteredpixeldungeon.android.PlayRunDeviceTest,com.shatteredpixel.shatteredpixeldungeon.android.RunSetupDifficultyDeviceTest,com.shatteredpixel.shatteredpixeldungeon.android.ApdSettingsClickDeviceTest#adultHumorControlWorksWithoutAnAgeProviderAndReopensWithSavedChoice,com.shatteredpixel.shatteredpixeldungeon.android.ApdSettingsClickDeviceTest#communityMenuOpensEveryPreviewAndReturnsWithoutStackingWindows,com.shatteredpixel.shatteredpixeldungeon.android.AboutDeviceTest,com.shatteredpixel.shatteredpixeldungeon.android.UimsdScreensDeviceTest,com.shatteredpixel.shatteredpixeldungeon.android.UimsdResourcesDeviceTest,com.shatteredpixel.shatteredpixeldungeon.android.ChangesVisualDeviceTest" || apd_test_status=$?
mkdir -p codex-visual-evidence
for apd_visual_name in apd-changes.png spd-changes-reference.png apd-build-22.png apd-build-37.png apd-codex-test-67.png tiles-original-2.png tiles-enhanced-2.png tiles-original-4.png tiles-enhanced-4.png stairs-shortcut.png stairs-highlight.png uimsd-home.png uimsd-demo-info.png uimsd-text-values.png uimsd-sort-keys.png chalice-preview.png blacksmith-rewards.png blacksmith-reward-detail.png healing-confirmation.png healing-threshold-settings.png metamorph-five-choices.png metamorph-choice-detail.png; do
    if ! adb exec-out cat "/sdcard/Pictures/APD-Test-Visuals/${apd_visual_name}" > "codex-visual-evidence/${apd_visual_name}"; then
        rm -f "codex-visual-evidence/${apd_visual_name}"
        if [ "$apd_test_status" -eq 0 ]; then apd_test_status=1; fi
    fi
done
if ! python3 tools/validate_android_visuals.py codex-visual-evidence apd-changes.png spd-changes-reference.png apd-build-22.png apd-build-37.png apd-codex-test-67.png tiles-original-2.png tiles-enhanced-2.png tiles-original-4.png tiles-enhanced-4.png stairs-shortcut.png stairs-highlight.png uimsd-home.png uimsd-demo-info.png uimsd-text-values.png uimsd-sort-keys.png chalice-preview.png blacksmith-rewards.png blacksmith-reward-detail.png healing-confirmation.png healing-threshold-settings.png metamorph-five-choices.png metamorph-choice-detail.png; then apd_test_status=1; fi
exit "$apd_test_status"

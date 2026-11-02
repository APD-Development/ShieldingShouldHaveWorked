#!/usr/bin/env bash
set -euo pipefail
adb shell am instrument -w -r -e class com.shatteredpixel.shatteredpixeldungeon.android.QolItemFeedbackDeviceTest com.charltonchris29.adhdpixeldungeon.indev.test/androidx.test.runner.AndroidJUnitRunner | tee marker-instrumentation.txt
rg 'OK \(1 test\)' marker-instrumentation.txt
mkdir -p codex-visual-evidence
for name in qol-dew-settings.png qol-force-weapon.png qol-sharpshooting-weapon.png qol-searched-wall.png qol-offline-license.png; do
  adb exec-out cat "/sdcard/Pictures/APD-Test-Visuals/$name" > "codex-visual-evidence/$name"
done
python3 tools/validate_android_visuals.py codex-visual-evidence qol-dew-settings.png qol-force-weapon.png qol-sharpshooting-weapon.png qol-searched-wall.png qol-offline-license.png

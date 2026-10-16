#!/usr/bin/env bash
set -euo pipefail
adb shell wm size "$1"
adb shell wm density "$2"
mkdir -p title-evidence
adb shell wm size > title-evidence/display.txt
adb shell wm density >> title-evidence/display.txt
adb logcat -c
adb logcat -v time > title-logcat.txt &
adb install -r application/*.apk
adb install -r android/build/outputs/apk/androidTest/debug/*-androidTest.apk
adb shell am instrument -w -r -e class com.shatteredpixel.shatteredpixeldungeon.android.TitleMenuLayoutDeviceTest com.charltonchris29.adhdpixeldungeon.indev.test/androidx.test.runner.AndroidJUnitRunner > title-instrumentation.txt 2>&1
cat title-instrumentation.txt
if ! grep -q 'OK (1 test)' title-instrumentation.txt; then exit 1; fi
for name in main-title-layout.png main-title-hidden.png main-title-restored.png options-confirmations.png; do
  adb exec-out cat "/sdcard/Pictures/APD-Test-Visuals/$name" > "title-evidence/$name"
done
python3 tools/validate_android_visuals.py title-evidence main-title-layout.png main-title-hidden.png main-title-restored.png options-confirmations.png

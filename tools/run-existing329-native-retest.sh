#!/usr/bin/env bash
# ADHD Pixel Dungeon
# Copyright (C) 2026 Chris Charlton
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>

set -euo pipefail
adb logcat -c
adb logcat -v time > exact329-logcat.txt &
adb install -r exact329.apk
adb install -r android/build/outputs/apk/androidTest/debug/android-debug-androidTest.apk
# Preserve the same earlier Home font/layout case as the full suite before measuring.
adb shell am instrument -w -r -e class 'com.shatteredpixel.shatteredpixeldungeon.android.AndroidBackDeviceTest#homeTextKeepsItsWordsAndFitsOversizedTokens,com.shatteredpixel.shatteredpixeldungeon.android.AndroidBackDeviceTest#warmUimsdTapDrawsTheComplete72RunHomeTwiceWithin250ms,com.shatteredpixel.shatteredpixeldungeon.android.DiagnosticReportDeviceTest#actualReportButtonSavesDeviceEvidenceAndMenusSurvivePauseResume' com.charltonchris29.adhdpixeldungeon.indev.test/androidx.test.runner.AndroidJUnitRunner | tee title-instrumentation.txt
mkdir -p title-evidence
for name in diagnostic-report-button.png diagnostic-report-saved.png; do
    adb exec-out cat "/sdcard/Pictures/APD-Test-Visuals/$name" > "title-evidence/$name"
done
APD_EXPECTED_METHODS=3 APD_EXPECTED_IMAGES='diagnostic-report-button.png diagnostic-report-saved.png' python3 tools/record-native-confirmation-results.py

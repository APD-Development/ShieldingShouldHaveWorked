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

# Test the EXACT published-to-Actions TEST314 application artifact, not a new build.
set -euo pipefail
adb logcat -c
adb logcat -v time > exact-logcat.txt &
adb shell wm size 360x560
adb shell wm density 160
adb shell settings put secure immersive_mode_confirmations confirmed
adb shell settings put global hide_error_dialogs 1
adb install -r exact.apk
TESTAPK="$(find android/build/outputs/apk/androidTest -name '*.apk' -type f | head -1)"
test -n "$TESTAPK"
adb install -r "$TESTAPK"
adb shell am instrument -w -r -e class 'com.shatteredpixel.shatteredpixeldungeon.android.RunSetupDifficultyDeviceTest#clericStartsWithoutPrivateFeatureFlagInsteadOfShowingBlackScreen,com.shatteredpixel.shatteredpixeldungeon.android.TitleMenuLayoutDeviceTest#titleControlsStaySeparateAndNativeChangesAndHideUiRemainUsable' com.charltonchris29.adhdpixeldungeon.indev.test/androidx.test.runner.AndroidJUnitRunner | tee native-result.txt
grep -q '^OK (2 tests)' native-result.txt
mkdir -p native-screens
adb exec-out cat /sdcard/Pictures/APD-Test-Visuals/cleric-unflagged-start.png > native-screens/cleric-unflagged-start.png
python3 - <<'PY'
b=open('native-screens/cleric-unflagged-start.png','rb').read()
assert b.startswith(bytes.fromhex('89504e470d0a1a0a')) and len(b)>4096,len(b)
print('Validated real Cleric dungeon PNG:',len(b),'bytes')
PY

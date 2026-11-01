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
apd_aapt="$(find "$ANDROID_HOME/build-tools" -type f -name aapt | sort -V | tail -1)"
apd_instrumentation=android/build/outputs/apk/androidTest/debug/android-debug-androidTest.apk
apd_package="$("$apd_aapt" dump badging death-order-evidence/after.apk | sed -n "s/^package: name='\([^']*\)'.*/\1/p")"
apd_test_package="$("$apd_aapt" dump badging "$apd_instrumentation" | sed -n "s/^package: name='\([^']*\)'.*/\1/p")"
test -n "$apd_package"
test -n "$apd_test_package"
for apd_variant in before after; do
    adb install -r -d "death-order-evidence/${apd_variant}.apk"
    adb install -r -d "$apd_instrumentation"
    adb shell pm clear "$apd_package"
    adb logcat -c
    adb shell am instrument -w -r -e class 'com.shatteredpixel.shatteredpixeldungeon.android.PlayRunDeviceTest#aRunCanBePlayedSavedContinuedAndEndedWithItsHistoryRecorded' "${apd_test_package}/androidx.test.runner.AndroidJUnitRunner" > "death-order-evidence/${apd_variant}-instrumentation.txt"
    adb logcat -d > "death-order-evidence/${apd_variant}-logcat.txt"
done
python3 - <<'PY'
import json,pathlib
p=pathlib.Path('death-order-evidence')
before=(p/'before-instrumentation.txt').read_text();after=(p/'after-instrumentation.txt').read_text()
assert 'death cleanup awaits the final ranking' in before and 'FAILURES!!!' in before,before[-4000:]
assert 'OK (1 test)' in after and 'FAILURES!!!' not in after,after[-4000:]
(p/'result.json').write_text(json.dumps({'before_test287_expected_failure':'death cleanup awaits the final ranking','after_corrected_apk_passed':1,'production_rebuilt':False,'physical_phone_verified':False},indent=2))
PY

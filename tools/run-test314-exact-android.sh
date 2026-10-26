#!/usr/bin/env bash
# Test the EXACT published-to-Actions TEST314 application artifact, not a new build.
set -euo pipefail
adb logcat -c
adb logcat -v time > exact-logcat.txt &
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

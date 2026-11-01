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
mkdir -p api36-isolated
cp acceptance-source.json api36-isolated/
adb shell wm size 1216x2688
adb shell wm density 442
adb shell wm size > api36-isolated/display.txt
adb shell wm density >> api36-isolated/display.txt
adb shell settings put global hide_error_dialogs 1
adb shell settings put secure immersive_mode_confirmations confirmed
adb logcat -c
adb logcat -v time > api36-isolated/logcat.txt &
adb install -r application/*.apk
adb install -r android/build/outputs/apk/androidTest/debug/*-androidTest.apk
for apd_method in 'TitleMenuLayoutDeviceTest#titleControlsStaySeparateAndNativeChangesAndHideUiRemainUsable' 'ScaleChangeDeviceTest#nativeSevenToSixScaleChangePreservesSettingsAndCoversViewport' 'ScaleChangeDeviceTest#nativeTapQueuedDuringTitleBuildIsDiscardedButFreshTapWorks'; do
  adb shell am force-stop com.charltonchris29.adhdpixeldungeon.indev
  adb shell am instrument -w -r -e class "com.shatteredpixel.shatteredpixeldungeon.android.$apd_method" com.charltonchris29.adhdpixeldungeon.indev.test/androidx.test.runner.AndroidJUnitRunner > "api36-isolated/$apd_method.txt" 2>&1
  cat "api36-isolated/$apd_method.txt"
done
python3 - <<'PY'
import pathlib,re,xml.etree.ElementTree as E
root=E.Element('testsuite',name='ExactTEST331API36Isolated',tests='3',failures='0',errors='0',skipped='0')
files=list(pathlib.Path('api36-isolated').glob('*#*.txt'));assert len(files)==3
for p in files:
 s=p.read_text();assert 'OK (1 test)' in s and 'FAILURES!!!' not in s,s
 assert re.findall(r'INSTRUMENTATION_STATUS_CODE: (-?\d+)',s)==['1','0'],s
 cls,method=p.stem.split('#');assert method in s
 E.SubElement(root,'testcase',classname='com.shatteredpixel.shatteredpixeldungeon.android.'+cls,name=method)
E.ElementTree(root).write('api36-isolated/TEST-api36-isolated.xml',encoding='utf-8',xml_declaration=True)
PY
for apd_image in main-title-layout.png main-title-hidden.png main-title-restored.png options-confirmations.png scale-phone-seven.png scale-phone-six.png; do
  adb exec-out cat "/sdcard/Pictures/APD-Test-Visuals/$apd_image" > "api36-isolated/$apd_image"
done
python3 tools/validate_android_visuals.py api36-isolated main-title-layout.png main-title-hidden.png main-title-restored.png options-confirmations.png scale-phone-seven.png scale-phone-six.png

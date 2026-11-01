#!/bin/bash
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

# Runs the instrumented tests with logcat captured to files that are uploaded even when the run hangs.
# The gradle step is bounded (14 min, the normal run is about 5) so a hang FAILS the job and leaves its evidence, instead of
# silently using up the job's 45 minutes with no logs.
adb logcat -c
adb logcat -v time > logcat.txt &
LOGCAT_PID=$!
# the migration test's own step markers (and its watchdog's thread dump) and the timing numbers go straight into this step's log, live
adb logcat -v time APDMIG:V APDPERF:V APDUI:V '*:S' &
MARKS_PID=$!
timeout --signal=TERM --kill-after=30 840 ./gradlew :android:connectedDebugAndroidTest
STATUS=$?
if [ "$STATUS" = "124" ]; then echo "DEVICE TESTS DID NOT FINISH IN 14 MINUTES (see logcat artifact)"; fi
sleep 2
adb logcat -d -v time > logcat-final.txt || true
echo "=== last lines of the APDMIG markers ==="
grep -a "APDMIG\|APDPERF\|APDUI" logcat-final.txt | tail -60
echo "=== crashes / ANR / test-process errors ==="
grep -a -E "FATAL EXCEPTION|ANR in|AndroidRuntime|am_proc_died|Process .* has died|INSTRUMENTATION_" logcat-final.txt | tail -40
kill "$LOGCAT_PID" "$MARKS_PID" 2>/dev/null || true
exit "$STATUS"

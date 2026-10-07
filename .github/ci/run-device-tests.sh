#!/bin/bash
# Runs the instrumented tests with logcat captured to files that are uploaded even when the run hangs.
# The gradle step is bounded (14 min, the normal run is about 5) so a hang FAILS the job and leaves its evidence, instead of
# silently using up the job's 45 minutes with no logs.
adb logcat -c
adb logcat -v time > logcat.txt &
LOGCAT_PID=$!
timeout --signal=TERM --kill-after=30 840 ./gradlew :android:connectedDebugAndroidTest
STATUS=$?
if [ "$STATUS" = "124" ]; then echo "DEVICE TESTS DID NOT FINISH IN 14 MINUTES (see logcat artifact)"; fi
adb logcat -d -v time > logcat-final.txt || true
kill "$LOGCAT_PID" 2>/dev/null || true
exit "$STATUS"

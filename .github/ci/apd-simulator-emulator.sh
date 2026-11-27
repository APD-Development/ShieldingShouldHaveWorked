#!/bin/bash
# Installs the APD Simulator on the running emulator, lets it play, and prints the results into the log.
set -x
PKG=com.charltonchris29.adhdpixeldungeon.simulator
APK=simulator/android/build/outputs/apk/debug/android-debug.apk
adb install -r "$APK"
adb logcat -c
adb shell pm grant $PKG android.permission.POST_NOTIFICATIONS || true
adb shell am start -n $PKG/com.shatteredpixel.shatteredpixeldungeon.android.SimulatorActivity --ez apdsim.autostart true --ei apdsim.speed 3
mkdir -p shots
TOTAL=$(( ${MINUTES:-6} * 60 ))
for t in 20 60 120 $TOTAL; do
  [ $t -gt $TOTAL ] && continue
  sleep $(( t - ${LAST:-0} )); LAST=$t
  adb exec-out screencap -p > shots/at-$t.png
done
set +x
echo "===== SIMULATOR OUTPUT (logcat) ====="
adb logcat -d | grep -E "APDSIM|AndroidRuntime|FATAL|SHPD|GAME" | grep -v APDSIM_TURN | tail -300
echo "===== RUNS ====="
adb shell run-as $PKG cat files/simruns.txt || true
echo "===== REPORTS ====="
for f in $(adb shell run-as $PKG ls files/simreports 2>/dev/null); do echo "--- $f"; adb shell run-as $PKG cat files/simreports/$f | head -60; done
echo "===== SCREENSHOTS (base64 PNG) ====="
for f in shots/*.png; do echo "SHOT $f"; base64 -w 0 "$f"; echo; done
exit 0

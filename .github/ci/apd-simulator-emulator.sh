#!/bin/bash
# Installs the APD Simulator on the running emulator, lets it play, and prints the results into the log.
set -x
PKG=com.charltonchris29.adhdpixeldungeon.simulator.visual
APK=simulator/android/build/outputs/apk/visual/debug/android-visual-debug.apk
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
echo "===== HEADLESS WITH THE SCREEN OFF ====="
# The Headless app must keep playing with the screen off: count finished runs before, during and after.
HPKG=com.charltonchris29.adhdpixeldungeon.simulator.headless
adb shell am force-stop $PKG
adb install -r simulator/android/build/outputs/apk/headless/debug/android-headless-debug.apk
adb logcat -c
adb shell pm grant $HPKG android.permission.POST_NOTIFICATIONS || true
adb shell am start -n $HPKG/com.shatteredpixel.shatteredpixeldungeon.android.SimulatorActivity --ez apdsim.autostart true --ei apdsim.speed 3
runs(){ adb shell run-as $HPKG cat files/simruns.txt 2>/dev/null | grep -c "deepest" ; }
sleep 60
echo "HEADLESS runs after 60 s on screen: $(runs)"
adb exec-out screencap -p > shots/headless-on.png
adb shell input keyevent 26
sleep 5
echo "HEADLESS screen state: $(adb shell dumpsys power | grep -E 'mWakefulness=|Display Power: state=' | tr '\n' ' ')"
echo "HEADLESS runs at screen off: $(runs)"
sleep 120
echo "HEADLESS runs after 120 s with the screen off: $(runs)"
adb shell dumpsys activity services $HPKG | grep -E "ServiceRecord|isForeground|foregroundServiceType" | head -5
adb shell input keyevent 224
adb shell input keyevent 82
adb shell am start -n $HPKG/com.shatteredpixel.shatteredpixeldungeon.android.SimulatorActivity
sleep 40
echo "HEADLESS runs 40 s after coming back: $(runs)"
adb exec-out screencap -p > shots/headless-back.png
adb logcat -d | grep -E "APDSIM_BACKGROUND|AndroidRuntime|FATAL" | tail -60
echo "===== HEADLESS REPORTS ====="
for f in $(adb shell run-as $HPKG ls files/simreports 2>/dev/null); do echo "--- $f"; adb shell run-as $HPKG cat files/simreports/$f | head -40; done
echo "===== SCREENSHOTS (base64 PNG) ====="
for f in shots/*.png; do echo "SHOT $f"; base64 -w 0 "$f"; echo; done
exit 0

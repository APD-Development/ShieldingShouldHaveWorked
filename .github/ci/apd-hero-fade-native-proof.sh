#!/usr/bin/env bash
# Execute only the existing owner's hero-select fade regression on a disposable API34 emulator.
# The emulator action invokes each inline script line using /bin/sh; keep Bash syntax in this file.
set -euo pipefail
mkdir -p native-fade/screenshots
trap 'adb logcat -d -v time > native-fade/logcat.txt 2>&1 || true' EXIT

mapfile -t app < <(find android/build/outputs/apk/debug -maxdepth 1 -name '*.apk' -type f)
mapfile -t instrumentation < <(find android/build/outputs/apk/androidTest/debug -maxdepth 1 -name '*.apk' -type f)
test "${#app[@]}" -eq 1
test "${#instrumentation[@]}" -eq 1

adb install -r "${app[0]}"
adb install -r "${instrumentation[0]}"
adb logcat -c
timeout 1200 adb shell am instrument -w -r -e class \
  com.shatteredpixel.shatteredpixeldungeon.android.HeroSelectDifficultyFadeDeviceTest \
  com.charltonchris29.adhdpixeldungeon.indev.test/androidx.test.runner.AndroidJUnitRunner \
  | tee native-fade/instrumentation.txt
grep -q 'OK (1 test)' native-fade/instrumentation.txt
if grep -q 'FAILURES!!!' native-fade/instrumentation.txt; then
  echo 'Android instrumentation reported a failure'
  exit 1
fi

adb pull /sdcard/Pictures/APD-Hero-Fade native-fade/screenshots
python3 - <<'PY'
from pathlib import Path
root=Path('native-fade/screenshots/APD-Hero-Fade')
for name in ('fully-visible.png','half-faded.png','fully-faded.png','restored.png'):
    f=root/name
    assert f.is_file() and f.stat().st_size>1000,(name,'missing screenshot evidence')
print('Native assertion passed, four real PNGs transferred; visual review is still separate')
PY

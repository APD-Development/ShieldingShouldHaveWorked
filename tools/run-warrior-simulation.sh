#!/usr/bin/env bash
# Must run while the disposable emulator is still alive.
set -uo pipefail
adb logcat -c
adb logcat -v time > simulator-logcat.txt &
apd_logcat_pid=$!
trap 'kill "$apd_logcat_pid" 2>/dev/null || true' EXIT
./gradlew :android:connectedDebugAndroidTest -PapdTestBuild -PapdBuild=SIM \
  -Pandroid.testInstrumentationRunnerArguments.class=com.shatteredpixel.shatteredpixeldungeon.android.WarriorSimulationDeviceTest \
  -Pandroid.testInstrumentationRunnerArguments.apdSimulation=true \
  -Pandroid.testInstrumentationRunnerArguments.simulationCount="${APD_SIMULATION_COUNT:-1}" \
  -Pandroid.testInstrumentationRunnerArguments.simulationSeed="${APD_SIMULATION_SEED:-1000000}" \
  -Pandroid.testInstrumentationRunnerArguments.simulationActions="${APD_SIMULATION_ACTIONS:-1000}"
apd_test_status=$?
adb exec-out run-as com.charltonchris29.adhdpixeldungeon.indev cat files/simulation-results.jsonl > simulation-results.jsonl
apd_trace_status=$?
if [ "$apd_test_status" -ne 0 ]; then exit "$apd_test_status"; fi
if [ "$apd_trace_status" -ne 0 ] || [ ! -s simulation-results.jsonl ]; then
  echo 'Simulator result evidence was not extracted.' >&2
  exit 1
fi

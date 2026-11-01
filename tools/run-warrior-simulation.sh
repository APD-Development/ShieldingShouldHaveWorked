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

# Must run while the disposable emulator is still alive.
set -uo pipefail

apd_count="${APD_SIMULATION_COUNT:-1}"
apd_seed="${APD_SIMULATION_SEED:-1000000}"
apd_logcat_pid=""
cleanup() {
  if [ -n "$apd_logcat_pid" ]; then
    kill "$apd_logcat_pid" 2>/dev/null || true
    wait "$apd_logcat_pid" 2>/dev/null || true
  fi
}
trap cleanup EXIT

adb logcat -c
adb logcat -v time > simulator-logcat.txt &
apd_logcat_pid=$!
apd_seed_arguments=()
if [ -n "${APD_SIMULATION_SEEDS:-}" ]; then
  apd_seed_arguments=(-Pandroid.testInstrumentationRunnerArguments.simulationSeeds="$APD_SIMULATION_SEEDS")
fi

./gradlew :android:connectedDebugAndroidTest -PapdTestBuild -PapdBuild=SIM \
  -Pandroid.testInstrumentationRunnerArguments.class=com.shatteredpixel.shatteredpixeldungeon.android.WarriorSimulationDeviceTest \
  -Pandroid.testInstrumentationRunnerArguments.apdSimulation=true \
  -Pandroid.testInstrumentationRunnerArguments.simulationCount="$apd_count" \
  -Pandroid.testInstrumentationRunnerArguments.simulationSeed="$apd_seed" \
  -Pandroid.testInstrumentationRunnerArguments.simulationActions="${APD_SIMULATION_ACTIONS:-1000}" \
  "${apd_seed_arguments[@]}"
apd_test_status=$?

cleanup
apd_logcat_pid=""
python3 - "$apd_count" "$apd_seed" <<'PY'
import base64
import json
import os
import pathlib
import re
import sys

expected_count = int(sys.argv[1])
first_seed = int(sys.argv[2])
explicit = os.environ.get('APD_SIMULATION_SEEDS', '')
expected_seeds = [int(s.strip()) for s in explicit.split(',')] if explicit else list(range(first_seed, first_seed + expected_count))
if len(expected_seeds) != expected_count or len(set(expected_seeds)) != expected_count:
    raise SystemExit('Invalid requested seed plan.')
pattern = re.compile(r"APD_SIM_CHUNK:(\d+):(\d+):(\d+):([A-Za-z0-9+/=]+)")
pieces = {}
totals = {}
for line in pathlib.Path("simulator-logcat.txt").read_text(encoding="utf-8", errors="replace").splitlines():
    match = pattern.search(line)
    if not match:
        continue
    episode, part, total = map(int, match.group(1, 2, 3))
    key = (episode, part)
    if key in pieces:
        raise SystemExit(f"duplicate simulator trace chunk {episode}:{part}")
    pieces[key] = match.group(4)
    if episode in totals and totals[episode] != total:
        raise SystemExit(f"inconsistent simulator chunk total for episode {episode}")
    totals[episode] = total

rows = []
for episode in range(expected_count):
    total = totals.get(episode)
    if total is None:
        raise SystemExit(f"missing simulator trace for episode {episode}")
    expected_parts = set(range(total))
    actual_parts = {part for item, part in pieces if item == episode}
    if actual_parts != expected_parts:
        raise SystemExit(f"incomplete simulator trace chunks for episode {episode}")
    encoded = "".join(pieces[(episode, part)] for part in range(total))
    try:
        row = json.loads(base64.b64decode(encoded, validate=True).decode("utf-8"))
    except Exception as failure:
        raise SystemExit(f"invalid simulator JSON for episode {episode}: {failure}")
    required = {
        "seed", "completed", "observedHealingBeforeNormalFlyHealing",
        "normalFlyHealingObserved", "terminalDepth", "terminalHp",
        "stopReason", "startedEpochMillis", "finishedEpochMillis",
        "elapsedMillis", "trace"
    }
    missing = sorted(required.difference(row))
    if missing:
        raise SystemExit(f"simulator row {episode} lacks {missing}")
    if row["seed"] != expected_seeds[episode]:
        raise SystemExit(f"simulator row {episode} has the wrong seed")
    if not isinstance(row["completed"], bool) or not isinstance(row["trace"], list) or not row["trace"]:
        raise SystemExit(f"simulator row {episode} is not complete evidence")
    rows.append(row)

if len(totals) != expected_count:
    raise SystemExit("unexpected extra simulator traces were captured")
pathlib.Path('simulation-attempts.jsonl').write_text(''.join(json.dumps(row, separators=(',', ':')) + '\n' for row in rows))
if any(row['completed'] is not True for row in rows):
    raise SystemExit('Incomplete episodes preserved as attempts, not successful simulation evidence.')
with pathlib.Path("simulation-results.jsonl").open("w", encoding="utf-8") as output:
    for row in rows:
        output.write(json.dumps(row, separators=(",", ":")) + "\n")
PY
apd_parser_status=$?
if [ "$apd_test_status" -ne 0 ]; then exit "$apd_test_status"; fi
exit "$apd_parser_status"

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

# Runs the core unit tests WITHOUT the Android module (which needs Android SDK/AGP).
# Builds a throwaway Gradle project that includes only SPD-classes, core and services.
# Usage: tools/run-core-tests.sh [extra gradle args, e.g. --tests '*FirstFloorExitTest*']
set -euo pipefail
REPO="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"
cat > "$TMP/build.gradle" <<'G'
allprojects {
  ext { appJavaCompatibility = JavaVersion.VERSION_11
        gdxVersion='1.14.0'
        gdxControllersVersion='2.2.4' }
  repositories { mavenCentral() }
}
G
cat > "$TMP/settings.gradle" <<G
rootProject.name='apd-core-tests'
include ':SPD-classes',':core',':services'
project(':SPD-classes').projectDir=file('$REPO/SPD-classes')
project(':core').projectDir=file('$REPO/core')
project(':services').projectDir=file('$REPO/services')
G
"$REPO/gradlew" -p "$TMP" :core:test "$@"
echo "Results: $REPO/core/build/test-results/test (XML), $REPO/core/build/reports/tests/test/index.html"

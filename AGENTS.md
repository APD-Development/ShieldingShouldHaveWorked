# AGENTS.md: ADHD Pixel Dungeon (APD), start here

APD is a private fork of Shattered Pixel Dungeon 4.0.x (upstream licence/attribution must stay). It is a solo project: all in-game developer voice is first-person singular (I/me/my). The owner is the sole decision maker. Repo: github.com/APD-Development/ShieldingShouldHaveWorked (private).

## Branches
- Current work branch: `codex/apd-phone-fixes-2026-10-09`. Resumed on November 10 from `6919e4b86b033f7d8ceec9d4c18f3f4d69cb2f18`; follow current receipts and `docs/APD-CURRENT-STATE-2026-11-10.json`. Historical Claude branches and `main` are not the current work checkout.
- Preserve `main`; never merge unfinished work there to obtain an APK. All current source verification, preparation and private delivery use the active work branch.

## Read first (in this order)
0. `docs/WORK-COMPLETED-LEDGER.md`: everything already DONE (do not redo) vs NOT done. Check it before starting any task.
1. `docs/KNOWN-ISSUES-AND-SOURCE-GUIDE.md`: current alpha configuration, known bugs, incomplete features, where things are.
2. `docs/HANDOFF.md`: owner decisions, release process, history. (Written for Claude; ignore its mentions of an "hourly resume trigger", Claude tools and sandboxes. The decisions in it still hold.)
3. `docs/APD-FIX-LOG-2026-10-08.md`: each recent fix with its root cause.
4. `docs/APD-BACKEND.md`, `docs/APD-UIMSD-UNIVERSAL.md`, `docs/APD-VENDETTA.md` (postponed Cleric Grudge), `docs/APD-PHONE-CHECKLIST-B69.md` (+ .pdf), `docs/APD-HERO-MARKERS.md`.
5. `docs/APD-HANDOFF.md`: long engineering log (telemetry, UIMSD, difficulty, back handling).

## Standing rules from the owner (do not break)
- Do NOT trigger GitHub Actions, publish a release, push to main, or spend money without the owner's explicit approval for that specific action.
- Owner update, November 10, 2026: resume the approved APD work and "Push builds to me every new build you complete". This authorizes the build, verification and private owner delivery Actions for each completed build in this resumed work, superseding the expired November 9 delivery window and per-build confirmation requirement. Preserve the existing signing identity, all previous builds and evidence, and send the version/download links. This does not authorize main pushes, new gameplay decisions, paid services, or a new recurring schedule.
- No gameplay/balance changes beyond what the owner approved. Cleric balance is NOT to be changed. Cleric is playable (own ruleset `cleric=playable`; all other ruleset ids and run history must stay unchanged).
- Never weaken or skip tests to get green. Never force-push, never change signing keys, no secrets in the APK.
- No fabricated statistics, achievements or history anywhere in the game.
- Owner correction, October 15, 2026: Cleric lore must be enabled in private test builds, with exactly 50 existing entries. Private feature assets explicitly name `grudge`; normal release remains gated by `GRUDGE_LORE_IN_THIS_RELEASE`. Preserve collection history, opt-out, all entries and Cleric gameplay. Do not disable lore based on the superseded postponement note.
- Backend: provider-neutral libraries only. No provider, sign-in or hosting chosen; do not commit to one.
- Test features (Demo Unlock, Cleric playable) are ON only in the private test build (`-PapdTestBuild`); normal builds have them off.
- The owner tests on a RedMagic 11 Pro and reports bugs by checklist step. Untested changes caused many bugs before: prefer small, test-backed changes and say clearly what is NOT device-verified.

## Build and test
- Unit tests (no Android SDK needed): `tools/run-core-tests.sh` (about 1-3 minutes; 3,164 tests across 457 suites at the November 10 resumed-source check, all passing). Single test: `tools/run-core-tests.sh --tests '*ApdIntroTest*'`.
- The Android module (APK) needs the Android SDK/AGP; build it with `./gradlew :android:assembleDebug -PapdTestBuild -PapdBuild=<n>` on a machine with the SDK, or via the manual GitHub workflow below.
- Level generation can be tested headlessly: see `core/src/test/.../apd/LevelGenerationHarness.java` and `FirstFloorExitTest`.

## Releasing a verified build during the resumed work
The November 10 instruction above authorizes delivery of every completed verified build. Follow the exact current build request and preparation workflow, preserve the immediately previous compiled APK history, and require the complete core results, required compilation and all targeted native cases on the exact assembled pair. Review the affected native screenshots before publication. `docs/APD-REVIEWED-PAIR-DELIVERY.json` binds the original artifact IDs/digests, source commit, APK hashes, test counts and manual image hashes; its workflow rechecks them and downloads the pair/source back before publication. The delivery-only workflow never rebuilds the applications.

Latest verified release at this update: [APD430](https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/tag/apd-alpha-preview-430), published November 10 after recovering the original November 9 artifacts. APD431 is held for a confirmed singular charge/points wording defect; its original native verification is running at [42859611090](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/42859611090); APD432 is the corrective candidate. Consult the current-state record and delivery receipt for later outcomes. The same original signer and `.indev` application ID are required. Never ask the owner to uninstall or clear player data. Send each completed build's version and download links in the owner conversation.

## Code map
- Game: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/`; APD additions under `apd/` (recorder, stats, query = UIMSD statistics database, backend, backendbridge, humor, difficulty, demo, replay). UI windows in `windows/`, scenes in `scenes/`.
- Feature switches: `apd/ApdFeatures.java` (asset `apd/features.txt` exists only in test builds). Tutorial fix: `apd/ApdIntro.java`.
- Tests: `core/src/test/` and Android device tests in `android/src/androidTest/` (run by the "Android device tests" workflow; also costs Actions minutes).

## Open bugs (short; full list in KNOWN-ISSUES doc)
UIMSD dialogs close on app switch; UIMSD default scope wording; floor-1 runs generated during the old tutorial keep a hidden door; phone checklist steps 23-41 untested; hero-select pips + win star designed, not built.


# AGENTS.md: ADHD Pixel Dungeon (APD), start here

APD is a private fork of Shattered Pixel Dungeon 4.0.x (upstream licence/attribution must stay). It is a solo project: all in-game developer voice is first-person singular (I/me/my). The owner is the sole decision maker. Repo: github.com/APD-Development/ShieldingShouldHaveWorked (private).

## Latest continuation checkpoint, November 11

Read `docs/APD-SESSION-CONTINUATION-2026-11-11.md` and `docs/APD-CURRENT-STATE-2026-11-11.json` before the older build-status paragraphs. This continuation resumed from `31e66d6c`; the completed APD439, APD440 and APD441 checkpoint remains below.

APD442 and APD443 are verified and delivered. APD442 retained 3,197 core tests across 464 suites and all twelve native cases. APD443 was produced from [a2960688](https://github.com/APD-Development/ShieldingShouldHaveWorked/commit/a2960688e02468817914f006ff4c563b844c80c5) in [run 43051129325](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43051129325): 3,210 tests across 465 suites, all required compilation, both JSON ABI checks, exact-source scan and all twelve native cases passed. All 2,866 PNGs and transfer manifests were verified; manual review covered 33 originals per variant. Both APD443 Ooze captures visibly show the hero and Caustic Ooze feedback. Exact saved-file readback preceded delivery. The [accepted receipt commit](https://github.com/APD-Development/ShieldingShouldHaveWorked/commit/69801dff65bc10b6fb45720c88f3d0cd5f7dd281) records its delivery, manual and independent acceptance. This supersedes the older pending APD443 state and older-build Ooze limits without changing those historical records.

The later [source 43c078b7](https://github.com/APD-Development/ShieldingShouldHaveWorked/commit/43c078b75b73626625369ed21d1cbf2f013e6f31) passed 3,214 tests across 466 suites and core, desktop, Android debug, Android release and instrumentation compilation in [source CI run 43056607040](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43056607040). It includes the approved ordinary Empowered Strike and Arcane Vision wording, real-handler regressions and instrumentation-only Ooze capture stabilization. These later changes are excluded from APD443.

APD444 was requested at [producing source 2b8787a2](https://github.com/APD-Development/ShieldingShouldHaveWorked/commit/2b8787a2fb41fc6ecacbb77617bd5f1763d902b0). [Run 43057716359](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43057716359), attempt 1, was in progress at `2026-11-11T16:26:30.714Z`; its predecessor/history and full core/compile/ABI steps had passed, and the single-pair assembly/freeze step was running. No APD444 APK is verified or delivered at this checkpoint. Preserve the single frozen pair once created, then finish source, native, affected-image, signature/history and saved-download checks before immediate delivery. The full ordinary-description inventory remains 445 source units, 442 complete and three partial. All standing instructions below remain unchanged.

## Branches

- Current work branch: `codex/apd-phone-fixes-2026-10-09`. Resumed on November 11 from `293b5a0d5fed957c1d953b301fadf576a766611b`; follow current receipts, `docs/APD-CURRENT-STATE-2026-11-11.json` and `docs/APD-SESSION-2026-11-11.md`. Historical Claude branches and `main` are not the current work checkout.
- APD441's exact producing source is `748f9c990284b61f74b39c5111646bcb1d395183`. Later evidence commits do not change the source inside that frozen pair. `76866517a5e6ecd043d7ac3f36fe383d2cf9aaf3` records APD440 delivery and the APD441 source-scan request. The session handoff lists the source and verification commits since the resumed baseline; later evidence commits retain the frozen producing source.
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
- Owner update, November 10, 2026: resume the approved APD work and "Push builds to me every new build you complete". This authorizes the build, verification and private owner delivery Actions for each completed build in this resumed work, superseding the expired November 9 delivery window and per-build confirmation requirement. Preserve the existing signing identity, all previous builds and evidence, and send the version/download links. This does not authorize main pushes, new gameplay decisions, paid services, or a new recurring development schedule.
- Owner continuation, November 11, 2026: keep actively developing approved work, push verified source regularly, build when the required environment is available, and deliver each completed verified APK immediately with a direct working download. Preserve failures and continue independent work when a build gate blocks. Do not claim background development after the session ends without a real active mechanism.
- No gameplay/balance changes beyond what the owner approved. Cleric balance is NOT to be changed. Cleric is playable (own ruleset `cleric=playable`; all other ruleset ids and run history must stay unchanged).
- Do not search or use Evan Debenham's external resources. Work from the owner's repository and retained evidence.
- Never weaken or skip tests to get green. Never force-push, never change signing keys, no secrets in the APK.
- No fabricated statistics, achievements or history anywhere in the game.
- Owner correction, October 15, 2026: Cleric lore must be enabled in private test builds, with exactly 50 existing entries. Private feature assets explicitly name `grudge`; normal release remains gated by `GRUDGE_LORE_IN_THIS_RELEASE`. Preserve collection history, opt-out, all entries and Cleric gameplay. Do not disable lore based on the superseded postponement note.
- Backend: provider-neutral libraries only. No provider, sign-in or hosting chosen; do not commit to one.
- Test features (Demo Unlock, Cleric playable) are ON only in the private test build (`-PapdTestBuild`); normal builds have them off.
- The owner tests on a RedMagic 11 Pro and reports bugs by checklist step. Untested changes caused many bugs before: prefer small, test-backed changes and say clearly what is NOT device-verified.

## Build and test

- Unit tests (no Android SDK needed): `tools/run-core-tests.sh` (about 1-3 minutes; 3,190 tests across 463 suites at the November 11 source gate, all passing). Single test: `tools/run-core-tests.sh --tests '*ApdIntroTest*'`.
- APD441 run `43026044936` fully passed at exact source `748f9c99`: 3,190 core tests / 463 suites with zero failures/errors/skips, core/desktop/Android debug/release/instrumentation compilation, two JSON ABI checks covering 4,134 production classes and 38 references, and all twelve native cases on the one frozen pair. The earlier `dd9cfb27` source-only run `43021746221` built no APK; the later fresh local 519-test check also was not native acceptance.
- The Android module (APK) needs the Android SDK/AGP; build it with `./gradlew :android:assembleDebug -PapdTestBuild -PapdBuild=<n>` on a machine with the SDK, or via the manual GitHub workflow below.
- Level generation can be tested headlessly: see `core/src/test/.../apd/LevelGenerationHarness.java` and `FirstFloorExitTest`.

## Releasing a verified build during the resumed work

The November 10 instruction above authorizes delivery of every completed verified build. Follow the exact current build request and preparation workflow, preserve the immediately previous compiled APK history, and require the complete core results, required compilation and all targeted native cases on the exact assembled pair. Review the affected native screenshots before publication. `docs/APD-REVIEWED-PAIR-DELIVERY.json` binds the original artifact IDs/digests, source commit, APK hashes, test counts and manual image hashes; its workflow rechecks them and downloads the pair/source back before publication. The delivery-only workflow never rebuilds the applications.

November 11 acceptance: the original APD439 and APD440 pairs and matching source were delivered privately after exact saved-file readback, with receipts in `docs/APD-439-DELIVERY-2026-11-11.json` and `docs/APD-440-DELIVERY-2026-11-11.json`. Their application and instrumentation APKs were not rebuilt. APD440's successful corrective run `43024704967` preserves failed recovery `43021022953` and its successful cases. Earlier APD441 request `43021889179` stopped before compilation or assembly; keep that failure distinct from the completed producing run.

APD441 was also delivered privately in chat after exact saved-file readbacks for both APKs and matching source completed at `2026-11-11T13:45:56.849545+00:00`; see `docs/APD-441-DELIVERY-2026-11-11.json`. Acceptance includes exactly two newly built application APKs, all twelve native cases, complete remote image inventories and all 1,414 PNG hashes/decoding per variant. Manual review covered 19 Developer and 25 Alpha original images, including all required Excess Charge, Counter Ability, Trap, wall and inventory frames; root separately re-viewed eight affected originals per variant. This is not full-size review of every PNG. Source scan `43028854285` at `76866517` passed against all 4,943 source-manifest files using unchanged rules, with 72 reviewed source candidates and 23 retained redacted historical findings. Its source ZIP SHA-256 is `41b4ca10a168a49c3afcf4afc4f66c2facfff53b22668444c47ab9f5c303d1d0`. Frozen build artifact `13860480452` and complete owner artifact `13861068719` are separate; the latter has SHA-256 `b16193bb202a2dbe17af4d808667576a0c11506abc0b91393669fadb7dd1ba4a`. Never rebuild the frozen APD441 pair to repair evidence or delivery.

The same original signer, `.indev` application ID and versionCode 920 are preserved. Never ask the owner to uninstall or clear player data. Send each completed verified build's version and working download links immediately after saved-file readback. Direct owner delivery and GitHub release publication are separate actions; these November 11 workflows did not automatically publish releases. Physical-phone installation, install-over compatibility, acoustic/audio acceptance and targeted native DM300/pylon gameplay remain unverified. APD441's corrected Trap On/restarted/Off controls are visibly framed; the Developer Ooze image alone does not clearly establish the runtime effect, which is supported by passing native assertions.

The latest rechecked GitHub-published release is APD437, tag `apd-alpha-preview-437`, source `4da1ef4cf0e116c7a5187236b5c61e53600e241b`, published at `2026-11-11T01:00:23Z`. The APD439 release request in run `42938508243` failed with HTTP 403, `Resource not accessible by integration`, at `POST /releases`. No credential-free correction has been proven. Direct saved-file delivery of APD439, APD440 and APD441 succeeded; do not call those new GitHub releases.

Retain older delivery records, including [APD432](https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/tag/apd-alpha-preview-432) and `docs/APD-432-DELIVERY-RECEIPT.json`. APD431 remains rejected for three wording defects and preserved in an unpublished draft with `docs/APD-431-ARCHIVE-RECEIPT.json`; never publish it. The existing read-only notification watch is recorded in `docs/APD-BUILD-NOTIFICATION-WATCH.json`; this session created or resumed no recurring development schedule. Completed build/scan runs do not imply ongoing work after the session ends.

## Code map

- Game: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/`; APD additions under `apd/` (recorder, stats, query = UIMSD statistics database, backend, backendbridge, humor, difficulty, demo, replay). UI windows in `windows/`, scenes in `scenes/`.
- Feature switches: `apd/ApdFeatures.java` (asset `apd/features.txt` exists only in test builds). Tutorial fix: `apd/ApdIntro.java`.
- Tests: `core/src/test/` and Android device tests in `android/src/androidTest/` (run by the "Android device tests" workflow; also costs Actions minutes).

## Open bugs (short; full list in KNOWN-ISSUES doc)

Ordinary talent-description audit remains partial; Cursed Wand wording and existing mechanic-specific panels still need owner decisions. Physical-phone checklist steps 23-41 and Prison/Tengu speaker latency remain unverified. Hero-select pips and win star are designed but await approval. Earlier UIMSD restoration/scope and legacy floor-1 repair entries have retained source/native evidence; consult the latest state before reopening them.

Completed November 11 scope: ordinary Excess Charge factors/effective imbued-wand level/rounding/stronger-Barrier wording; Counter Ability rank +2 typo `0.77` to `0.75`; four restored actual-handler tests; six new real targeting call-site tests; verification transfer/PNG/source/ABI/capture fixes. No gameplay formulas, balance, saves or statistics changed. A bounded read-only check found Feigned Retreat and Expose Weakness's 2/4/6/8-turn normal descriptions already match the real handler and existing four-rank tests. Do not repeat completed investigations or treat this narrow result as completion of the whole talent audit.


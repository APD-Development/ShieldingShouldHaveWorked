# APD hero-select difficulty native fixture recovery, November 13, 2026 (Eastern)

## Isolated claim (08:50 EST)

This work follows draft PR #20's owner-reported difficulty picker fade fix and draft PR #33's native regression. This is a test-only recovery, not a second gameplay fix, new difficulty rule, APK candidate or release.

The exact inherited native fixture head is `61d3be2e309954ffcf25b6ceb154f022e49ed4c0` on `chatgpt/apd-hero-fade-native-regression-20261112-1445`. Earlier exact run [43295613260](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43295613260) compiled the entire existing 3,300-core-test suite without failures/skips, compiled disposable Android debug/instrumentation APKs and booted API34. Its **single native test failed before it could reach the UI**: `HeroSelectDifficultyFadeDeviceTest.java:108` called `SPDSettings.apdDifficulty()` while `Gdx.app` was null, before `ActivityScenario.launch`. The same fixture's original `finally` restored settings after the scenario closed. These are test lifecycle mistakes, not evidence that the production fade works or fails.

This isolated branch moves the original preference/class snapshots **after** actual launcher/game initialization and restores preferences/class **inside** the live ActivityScenario, on its render thread. It leaves all prior UI assertions, the five button/label opacity and touch-state checks, four real screenshot captures and unchanged run difficulty assertion intact. It changes no production source, balance, save formats, signing, application ID or accepted release. It uses a separate branch-specific nonpublishing workflow with the same complete core, Android assembly and API34 actual-device test gates.

## Pending exact verification

Inspect the new workflow's terminal conclusion, the complete raw test result, PNG screenshot transfer and actual pictures, and failure receipts before integration. Do not claim this test proves Android acceptance until it passes. Keep PR #20/#33 isolated until verified; do not overlap APD465/APD466 publishing, and never replace existing signed APKs or recurring APD task settings.


## Verified CI and Android native result, 09:03 EST (same source and test code)

- GitHub Actions [run 43438377502](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43438377502), job `131929287855`, ran on exact source/test/workflow commit `b09c7b8b19edade9bb460f85c62600915d73a739`, starting 08:58:34 EST and ending **09:02:54 EST**, `completed/success`. The complete core XML was inspected from the retained artifact: **3,300 tests in 472 suites, zero failures, zero errors, zero skips**. The job also passed `:android:assembleDebug` and `:android:assembleDebugAndroidTest` for disposable debug test APKs, which do not qualify as owner release APKs.
- The real API34 Android emulator passed **one** `HeroSelectDifficultyFadeDeviceTest.difficultyLabelAndAllFiveSegmentsFollowFadeAndRestore` instrumented case, terminal `OK (1 test)`. Test directly asserts DifficultyBar composite enabled state, label and all five segments' opacity and input state at visible, half-fade, fully hidden and restored; the next-run difficulty remains unchanged.
- Downloaded the exact retained GitHub [evidence artifact `14044177249`](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43438377502/artifacts/14044177249), size **1,352,460 bytes**, ZIP SHA-256 `fc62a5d6699f25070486a71f02a0577bf7a1fd3c937b6c387de538c3cdbc2cc2`. Extracted all four original device PNGs and visually inspected a side-by-side contact sheet: difficulty text and all five buttons are fully visible initially, partially faded midway, absent when fully faded, and fully present again when restored. The screenshots depict Warrior selected on the character screen, and no layout collision or stuck visible difficulty element was observed in this narrow test. The first and restored PNGs are exactly byte-identical. All four are 320x640 PNGs:
  - fully-visible: SHA-256 `614b682168e6b706b05aea702b66263e7ff54cb4600e5a20626e14be3bfd9244`
  - half-faded: SHA-256 `12b02da454ca3edda123cd60ea50fc772c443d2fcc1dbc9568ae331982282930`
  - fully-faded: SHA-256 `673ca6a9dafb62c2fa8d9cca7b77a23d03d120fb95e0ceee9ef6132a58e18258`
  - restored: SHA-256 `614b682168e6b706b05aea702b66263e7ff54cb4600e5a20626e14be3bfd9244`
- The isolated claim is draft [PR #135](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/135) with target `chatgpt/apd-hero-fade-native-regression-20261112-1445` (original draft PR #33). Source owner-reported fade code remains independently unmerged on draft [PR #20](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/20). No prior failed native run was relabelled successful, and no previous evidence or build was replaced.

## Boundary and next safe work

This **verifies the source-branch UI fade on the disposable API34 emulator in four observed display states**, not the final production/APD signer APK, a physical RedMagic install-over, arbitrary screen densities/rotation or all input gestures. No original-signed Developer/Alpha release APK was assembled or published here. No save/history/scoring, core mechanics, numeric balance, app IDs or versionCode were edited. Preserve frozen APD464, APD465 and APD466 branches and release blockers. This exact execution ends at the documentation-only continuation commit; the test source/CI remains frozen at `b09c7b8b19edade9bb460f85c62600915d73a739`.

Before any integration, reread the current remote heads, open work claims and PR states. The safe next action is a guarded integration of this tested fixture into its parent draft PR #33, then separately coordinate the already-approved production fade PR #20 against the much newer authoritative development head and obtain full qualified build/native/signing/save release gates before owner delivery. Do not force an integration or change any recurring APD task.

# APD462 movement-only playback and signed APK delivery continuation
Date: 2026-11-12, America/New_York.

## Owner direction
Only character movement should follow the selected playback speed (including 1000x). Combat/attack animations, alerts, symbols, both wells, toxic gas, particles, environment and every other animation must remain at normal 1x. Owner also requested that character-selection difficulty bar fade with the rest of the menu and requested a **new download-ready APK** containing approved fixes, not another source-only report.

## Verified base and concurrency
Started isolated branch `chatgpt/apd-movement-only-playback-20261112` from established `codex/apd-phone-fixes-2026-10-09` commit `6dcb0ad0d797ee2519621f44922637dd817554ac`, verified through the connected GitHub repository. Rechecked AGENTS.md, live open PRs and workflows. Existing PR #19/#23 are merged and their well and alert 1x source changes retained. Other pending PR #20 and its native verification work are separate. APD461 not published; APD460 is last verified release. No upstream external material was consulted. Do not overwrite the other contributors' branches, known-good APKs or APD461.

## Actual source changes on this branch
- `a109e6c8cf2d3a3bd544b1f8ba237ab54af74e0a`: add `PlaybackGroup.movementDuration`, retaining all approved playback speed choices.
- `abb4f0c9144215d77cf25f49e89da317f6fee2c2`: change only character movement's `PosTweener` duration to use that factor, without changing actor/global clocks or the animation of attacks.
- `fbc20138cc2afaf21a6a795fe5cfecc1895a38bd`: replace 15 remaining accelerated GameScene visual groups with normal 1x groups. Together with the prior merged normal 1x groups, all dungeon non-movement visual layers use unscaled frame time.
- `65e3ffc7077d44efe0aaddc8f0048e7a568eecc6` and `8ecde42c3c62b8bc9bc34ed0d3d9d326992d5d49`: include owner's pending difficulty bar fade correction in isolated APK candidate, without altering existing selection or fade duration.
- `198ac5e78f78852eb2b451145d7f094b398dda75`: add new ten-speed normal-effects and accelerated-movement core regression.
- `131f3b8e4d365548885fddab4fce4a62b3380d8b`: add strict six-native-case screenshot receipt verifier derived from this repository's own prior procedure.
- `e26663f5cb8f13c74c312f318c881e79236fbf87`: start new isolated APD462 workflow with source suite, original-signer Developer and Alpha-Testers APK build, exact package/signature/version identity checks, six native Android test cases on frozen APK bytes with screenshot transfer checks, publication only on success, and GitHub readback SHA-256 verification.

## Evidence and caution
GitHub Actions run [43296176816](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43296176816) started on the exact frozen application/workflow commit `e26663f5cb8f13c74c312f318c881e79236fbf87`. At first inspection the source job was running. Its status/result must be checked live; it has NOT been reported as passed or as a published APK. The user wants a direct download only after the entire original-signer/native/publisher gate passes. No prior releases will be overwritten. Preserve original app identity, versionCode 920, saves, scoring and edition feature differences. The physical RedMagic phone remains untested.

## Next safe step
Check Actions run 43296176816 including exact failing step and logs. If a gate fails, fix only that gate or source defect on isolated branch, then trigger a fresh exact pipeline run with updated frozen source, without presenting unaccepted APKs as qualified. If all gates succeed, read back release `apd-462`, verify asset names and direct GitHub download URLs, and deliver links in originating regular APD conversation immediately. Coordinate any concurrent project changes before merging the branch into the authoritative dev head; do not force-write.


## Verified qualified APD462 publication (November 12, 2026, Eastern)
Source and candidate workflow [43296176816](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43296176816) passed the source suite, desktop/Android Java compilation, original-signer candidate build, package identity, versionCode 920 and variant checks on frozen application source `e26663f5cb8f13c74c312f318c881e79236fbf87`. That combined workflow's first native attempt failed screenshot device-transfer gates, **not** source/compile/signature gates; preserve that failed evidence.

Recovery [43297884679](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43297884679) reused the *same* signed APK bytes and verified five complete native Android cases, transferring screenshot bytes with validation against device SHA-256. Developer SeedHistory's emulator disconnected during screenshot transfer; do not claim that attempt passed. The six-case publisher correctly stayed blocked.

One-case recovery and publisher [43298764431](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43298764431) successfully completed Developer SeedHistory from a fresh emulator, retained the five earlier valid receipts, required six exact frozen-APK accepted native cases, and published `apd-462`. The final publisher step read back all published Developer/Alpha APK and matching source bytes and checked every SHA-256 against the original candidate receipt. GitHub workflow conclusion **success** at 2026-11-12T20:20:28Z (3:20:28 PM Eastern).

Verified APK download URLs:
- https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/download/apd-462/APD-Developer-462.apk (39,211,116 bytes)
- https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/download/apd-462/APD-Alpha-Testers-462.apk (39,211,092 bytes)
- https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/download/apd-462/APD-Source-462.zip (111,285,540 bytes)
Published release: https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/tag/apd-462.

The exact verified source is `e26663f5cb8f13c74c312f318c881e79236fbf87`; subsequent documentation and recovery-workflow commits are source-neutral and did not rebuild the frozen APKs. No APK460 or older release was overwritten, and the blocked APD461 candidate remained untouched. Original signer/package/edition differences and save compatibility retained. Physical RedMagic installation and subjective 1000x animation acceptance remain owner phone checks, **not** verified by emulator screenshots alone.

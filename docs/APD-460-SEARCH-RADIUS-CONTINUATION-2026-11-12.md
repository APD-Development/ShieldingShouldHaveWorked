# APD460 isolated contextual Search scan continuation, November 12, 2026

## Status at this checkpoint
APD459 remains the latest confirmed published Developer/Alpha pair until the APD460 release workflow actually completes and its assets are read back. This session started from the established development head `9a653930b135538958a8592fe829f7e39161aeeb` after verifying AGENTS.md, the APD459 handoff, active branch and release history. All work is isolated on `codex/apd-search-radius-scan-20261112` to avoid colliding with staggered coding sessions. Do not rebuild or overwrite APD459.

## Source implementation
- `d8b752d081b9164580b43eaf916f01e3c22624ed`: centralize the existing Search footprint radius in `WallSearchSession.searchRadius(hero,foresight)`, retaining Warrior/other radius 1, Rogue radius 2, +1 only if Wide Search is acquired, and Foresight radius 8.
- `d108ef0cc84b9a447f305652d06528bfda33261c`: constrain `SearchControlContext.hasMeaningfulWall` scan to that exact footprint radius, rather than scanning a radius-3 square for ordinary heroes. The original `covers`, doorway geometry, known terrain, candidate and checkmark guards remain in place.
- `7f75f820096d64336c06752a85bcb6b741227781`: add `contextualScanBoundsMatchActualClassTalentAndForesightFootprints` regression for every class, acquired Wide Search rank 1/2 with rounded corners, Metamorphosis, out-of-range walls and actual Foresight.
- Frozen source/CI commit: `7c6a10e2db7c82f38297f5d5e38a62ceba27c962`. No scoring, saves, RNG, talent values, action costs, geometry, signer or game balance changes.

## Verified source
Full GitHub Actions source CI [43237301847](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43237301847) succeeded. Actual raw log reports 3,266 core tests across 470 suites, zero failures/errors/skips; desktop classes and Android debug, release and instrumentation Java compilation all passed. The exact new regression was required by the CI gate.

## Frozen-APK pipeline, pending terminal verification
- Signed original-signer candidate workflow [43237859054](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43237859054), trigger commit `3f37bff65330ea63cc05355ee514cd5b5700aa0f`. Must consume source run 43237301847 successfully and build one Developer/Alpha pair from exactly `7c6a10e2`, with the original signer/application/versionCode.
- Android native workflow [43238039604](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43238039604), source `6ee677aabaa9b38a5be00f7dbd7e21d6ae1dbf72`. Must reuse original candidate APK bytes for six emulator cases, three each variant, with full screenshot transfer and hashes. Helper `tools/apd460_native_postcheck.py` binds exact candidate source, no app rebuild.
- Release publisher [43238146009](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43238146009), source `2bb2cc6350eaa57f3e72b7b1f33bb131e5cdace1`. Must require exact source/build/native run success, six original Android case artifacts, then publish `apd-460` only if not already present and read back all three published file hashes.

This is not an APK delivery receipt. Inspect completed workflow outcomes, exact signer/package and native evidence before claiming a new playable APK. If any fails, preserve the failure and correct only the demonstrated blocker. Once successful, retain all previous release identities and integrate to the established branch without overwriting other contributors. Physical-phone install-over and audio checks remain unverified. Unresolved personal seed-reuse scoring numerics and Cleric design choices belong to the owner.

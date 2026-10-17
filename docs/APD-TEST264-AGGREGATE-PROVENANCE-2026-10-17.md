# TEST264 imported aggregate provenance correction — 2026-10-17

## Result

TEST264 is a verified private workflow candidate at exact workflow source `9a712453443573f2027150cb204cb6be304da57c`, containing application correction `4d4048f80ab150aca3bc1ce690de48b5532922a0`. Full run [39000124500](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/39000124500) succeeded from 2026-10-17T03:47:28Z through 04:03:13Z.

The correction makes grouped UIMSD statistics that include imported ranking records expose imported provenance. A legacy-only aggregate now uses the existing legacy cell state. Mixed lower-bound, inferred and imported values retain the established warning precedence while the detail line explicitly says imported ranking data is included. Aggregate numeric values, raw histories and query math are unchanged.

## Verification

- Core artifact `12089275432`, digest `sha256:99d61d3b569cf20154501fad80dde799e56df02613e222f9a140b442d76ffc38`: 1,768 tests in 350 suites, 0 failures, 0 errors, 0 skipped.
- The three retained `AggValueContractTest` methods passed. They cover combined provenance, imported-only state, exact RATE/RATIO metadata and a zero-denominator unknown ratio.
- Android artifact `12088694058`, digest `sha256:9d8d16e73efafc60367cb487dd4ef5301e3de948d9a325679698ebdcda3625bc`: 38 methods in 14 suites, 0 failures, 0 errors, 0 skipped.
- All 57 required PNGs decoded as 320×640 and were reviewed as a complete contact sheet; no blank, corrupt or obvious clipped-regression frame was found.
- APK artifact `12087924665`, digest `sha256:a7a90141ad0c41ddd1aeaae6b4f1b4838c66a3814b86090b2c25e539c0b85d8e`, contains `ADHD-Pixel-Dungeon-CODEX-TEST-264.apk`, 38,591,975 bytes, raw SHA-256 `e6b65a4a7d48f7a8e9b9d181fff7745f6211b7659012ff29e9c0c0980dc496bb`.
- Supplemental display run [39000124598](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/39000124598) passed all five M53-representative, compact, small, square and short-landscape jobs. Its 25 images decoded at their declared resolutions and were reviewed without an obvious title/options clipping regression.
- Core-only precursor [38999867903](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38999867903) independently passed the same 1,768/350 core inventory; artifact `12088612594`, digest `sha256:bd2f65ea753de33b3b01e639b9466034e93e63b6c6af32e55c20ded381037a35`.

The automatic staging run `39000124638` started before the new full evidence existed and correctly failed its stale TEST263 application-tree check. It produced no staged artifact or publication and is not relabeled green. The staging profile is now pinned to the successful TEST264 run for independent signer, application-ID, version and install-over compatibility checks.

## Preserved behavior and limits

Raw history, derived caches, save formats, all 18 Custom Dungeon Rules, active 357-value v2.6R scoring, original scores, gameplay, turns, RNG, all 50 Cleric lore entries, private Grudge enablement, opt-out and collection ledger are unchanged.

This is emulator/CI evidence, not physical-phone or owner-save install-over evidence. It does not claim the broad UIMSD audit or remaining gameplay backlog complete. No automatic tester distribution or publication occurred. TEST263 remains the preceding staged fallback and TEST262 remains the latest actually published owner APK.

# TEST183 native empty-Home regression candidate — 2026-10-15

## Reproduced coverage gap
At verified evidence parent `a02b6ed01af2e3bc48b89f417392cc98bbf46e76`, `UimsdScreensDeviceTest.everyUimsdScreenAndDialogCanBeBuilt` opened the real UIMSD scene on a fresh emulator, slept for a few frames, then immediately replaced the Home with tour/demo data. TEST182's core regression proved the model string, but the API34 suite neither required the empty Home to finish loading, inspected the rendered wording nor captured that native state.

## Implemented
- Wait for the real scene's `cachedHome`.
- Require that the fresh-emulator Home is actually empty before continuing.
- Inspect the native `RenderedTextBlock` children and require “No finished runs yet” plus “Tables > Options > Include runs still going”.
- Reject the obsolete “Tap Rules to include” wording in the no-history state.
- Capture `uimsd-empty-home.png` before the tour injects its made-up history.

## Verification request
TEST183 requests the unchanged 1,603 core tests, debug assembly, immediate-previous TEST182 APK-history review, all 26 API34 methods and 35 valid screenshots. The strengthened assertion and new screenshot are pending until the exact candidate source completes the hosted gate.

## Boundaries
Only the retained Android device test and build/evidence documentation changed. Production gameplay, UIMSD query/model behavior, raw history, saves, permanent statistics, scoring, application ID and signing identity are unchanged. No APK delivery or physical-phone result is claimed.


## TEST183 hosted result
Source `68bea4b4d57aef872cab684e13f134b91f2d258c`, run `38661559728` (00:42:30Z–00:53:13Z) passed 1,603 core tests, debug assembly and immediate-previous TEST182 history, but API34 finished 25/26 methods with one failure in the new assertion: earlier device methods had already created finished-run fixtures, so this method's emulator was not an empty-history state. The assertion failed before `uimsd-empty-home.png` could be captured. TEST183 is not verified or delivered.

TEST184 corrects only the retained fixture. It injects an in-memory empty Home into the real scene/cache, renders and inspects it, then restores the original model/cache without deleting or rewriting device history. Production source and the wording assertions are unchanged. Full 1,603-core/debug/immediate-previous compiled TEST183 history/26 API34/35 PNG verification is pending.

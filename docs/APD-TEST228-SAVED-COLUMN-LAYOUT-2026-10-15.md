# TEST228 saved-column layout compatibility candidate

Timestamp: 2026-10-16T01:18:25Z (2026-10-15 9:18:25 p.m. EDT)

## Reproduced defects

Current verified TEST227 production accepted a saved column layout without checking it against the current dataset and hidden-information setting. Two retained regressions reproduced blank UIMSD tables:

- a view saved with only an internal column while hidden information was enabled reopened with zero headers after hidden information was disabled;
- a layout containing only a column removed by a newer build reopened with zero headers.

Against unchanged production, the two new methods failed exactly at the expected four-versus-zero header assertions; the other 22 UimsdModelTest methods passed.

## Correction

UimsdModel now filters a saved layout through the current dataset before applying it, retains valid columns in their saved order, rejects duplicate/retired IDs, and excludes internal columns when hidden information is off. If no usable saved column remains, the view uses its current public defaults. The saved-layout file is not rewritten by this compatibility fallback.

## Local verification

- corrected UimsdModelTest: 24/24 passed;
- broader dependency-light query/UIMSD suite: 294/294 passed, including 28,254 randomized safety comparisons;
- source SHA-256: `80d39f90675e54722cc8eb5a0b6e7d55ebe1094fc7cdef1185475d16ab308925`;
- test SHA-256: `0b52a9d91164e8e58f255431d6c63ff6614619c1458b8146a8017329417df95e`.

These local counts are subsets of the requested complete gate, not additive to it.

## Required full gate

Verify the exact candidate with 1,687 core methods, debug assembly, complete immediate-previous produced TEST227 history, 33 API34 emulator methods and 42 valid screenshots. Until that evidence succeeds, this is a pending source candidate only. TEST226 remains the latest privately delivered APK and TEST227 remains the verified CI fallback.

No release, tester distribution, physical-phone claim, raw-history/save/cache-format/scoring/custom-rule/gameplay/turn/RNG/signing/application-ID change, hidden-lore disclosure or production storage migration is included. Owner-device install-over/save acceptance, audio and unconfirmed armor/sentry investigation remain open. Unmeasured durations remain unknown.

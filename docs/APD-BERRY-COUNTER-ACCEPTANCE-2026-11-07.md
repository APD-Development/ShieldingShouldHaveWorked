# Berry saved-counter acceptance

Test-only continuation after the verified seasonal-food package. Application source and Berry behavior are unchanged.

## Actual regression

The new real-path test invokes Berry.satisfy twice on an isolated level with the real Generator default seed table:

- the first meal creates a SeedCounter at exactly1 and does not drop an item;
- the counter is revive-persistent and its compressed Bundle round-trip preserves count1;
- repeated inspection preserves the hero, counter and world and does not choose or drop a seed;
- the second meal drops exactly one real Plant.Seed and detaches the counter.

The test uses a quiet item sprite only to suppress rendering. It does not replace Berry, Generator, CounterBuff, Hunger or item-drop behavior.

## Verification state

Candidate pending exact source-only platform gate. Expected inventory:2931 tests/448 suites, zero failures/errors/skips, plus Android debug/release/instrumentation Java and desktop compilation. No APK, release, physical-phone or rendered-native claim.

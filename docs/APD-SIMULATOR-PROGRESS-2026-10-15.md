# APD Simulator Progress Report

**Date:** 2026-10-15  
**Branch:** `research/apd-simulator`  
**Last published simulator checkpoint:** `5a5fe7d15` (`Add deterministic headless Goo simulator checkpoint`)

No earlier simulator progress PDF appears in this branch's Git history. The prior simulator status was recorded in `docs/APD-SIMULATOR-PROGRESS.md` on 2026-10-14.

## Progress Since the Previous Status

- The headless test harness now accepts a `HeroClass` and includes class and seed in its transcript.
- The deterministic Goo replay test now attempts seed `638194` for every class and compares two runs per class.
- This exposed a class-specific failure: Warrior completes the replay and defeats Goo, but Mage does not defeat Goo with the current shared policy.
- Because the test stops at Mage's failed win assertion, this run provides no new Goo-win verification for Rogue, Huntress, Duelist, or Cleric.

## Tests and Results

- `./gradlew :core:test --tests com.shatteredpixel.shatteredpixeldungeon.apd.recorder.HeadlessSimulationHarnessTest` — **failed**. The Warrior case passed its replay and victory assertions; the Mage case failed the Goo-victory assertion for seed `638194`.
- A full `:core:test` run has not been performed against these uncommitted harness edits.
- The previous published checkpoint reported a passing Warrior Goo replay and passing `:core:test`; those results predate the class-parameterized test edit.

## Current Capability

The pushed checkpoint runs a deterministic, test-only Warrior scenario through generated levels 1–5 and defeats Goo using real game combat code. It also exercises movement, a food pickup, and a rat encounter. The scenario directly enters each depth and provisions test resources, so it is not yet a complete legal run through ordinary exploration and progression.

The newer local test edit can initialize each hero class and attempt deterministic Goo replays, but the shared strategy does not yet win as Mage. Tengu and all six-class Goo coverage remain unimplemented or unverified.

## Unfinished Work

- Fix Mage's Goo strategy and establish reproducible Goo victories for all six classes while preserving the verified Warrior result.
- Implement Tengu mechanics and class-aware strategies, with deterministic regression tests.
- Replace direct floor entry and injected potions/upgrades with legal exploration, resource acquisition, and progression.
- Add a reusable observation/action interface, multi-seed reliability checks, remaining boss and feature coverage, and recorder-backed end-to-end verification.
- Keep the simulator current as APD evolves; future features cannot be implemented before their requirements exist.

## Estimated Time to Done

Planning estimate only, assuming one experienced contributor can spend about 30–40 hours per week on the simulator, the game rules remain stable, and the test infrastructure does not require major repair:

- **Six classes through Goo and Tengu with reproducible tests:** approximately **4–8 weeks**. The failed Mage run confirms that class-specific strategy work remains before Tengu and the full class matrix can be considered verified.
- **Robust coverage of currently shipped APD gameplay features:** approximately **2–4 months**, including legal exploration and progression, remaining bosses and mechanics, reliability testing, and recorder/statistics verification.
- **All future APD features:** no finite completion date is meaningful while future feature requirements are unknown; this will be ongoing maintenance as the game changes.

These ranges should be recalibrated after a working Mage policy and initial Tengu prototype establish actual implementation and test throughput.

## Next Milestones

1. Keep the verified Warrior seed passing and develop a class-aware Mage policy.
2. Run the remaining class strategies independently, fix failures, and establish per-class Goo regressions.
3. Implement and test Tengu while retaining all Goo regressions.
4. Replace test-driven floor jumps and resource injection with legal play.
5. Expand toward current-feature coverage, multi-seed reliability, and recorder-backed run verification.
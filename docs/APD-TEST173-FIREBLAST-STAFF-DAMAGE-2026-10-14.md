# TEST173 — Mages' Staff Fireblast explosion provenance

Started: 2026-10-14T21:36:33Z

## Confirmed omission
The normal Fireblast wand cast and its fire fields already carried the wand user's origin. The Mages' Staff on-hit explosion was a separate offensive roll and still called `Char.damage` directly for each nearby enemy, so non-neutral Player Damage did not apply to that staff-only secondary event.

## Bounded correction
The original `Hero.heroDamageIntRange` roll and power multiplier remain in the same location and order. Its final raw integer now passes through the existing offensive hook with the actual staff attacker, original Wand of Fireblast source object and original target.

The hook runs once before the existing direct-damage call. It adds no RNG, armor roll, turn, source substitution, field change or save-format state. Neutral 100% is identity.

## Retained regressions
Three tests require:
- every permitted 25%–500% factor for player, ordinary-enemy and boss attackers, with contradictory other slider values;
- exactly one damage call with the original source;
- neutral amount and next RNG identity;
- the production staff path to use one attributed boundary and remove the old direct call.

## Verification request
TEST173 requests 1,593 complete core tests, debug assembly, compiled history against the immediate previous TEST172 APK, 26 API34 emulator checks and 34 valid screenshots. Candidate remains unverified until exact-source evidence passes.

No player-save, run-history, scoring, application identity, signing, delivery or physical-phone claim is made. TEST123 remains the last owner-delivered/signature-compared APK.

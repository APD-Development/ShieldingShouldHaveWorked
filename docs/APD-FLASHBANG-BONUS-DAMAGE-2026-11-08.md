# Flashbang electricity bonus damage provenance — 2026-11-08

## Confirmed defect

The base Flashbang explosion delegates to `Bomb.explode`, which already scales its original roll once with the immutable fuse/throw creator before the existing armor roll. The separate 25% electricity roll then called `Char.damage` directly. At non-neutral Player, Enemy or Boss Damage settings that attributable secondary roll therefore stayed at 100%.

## Bounded correction

The already-generated electricity amount now passes through `Bomb.apdScaleExplosionDamage` immediately before the original `Electricity` damage call. The captured bomb creator, original `Electricity` source object, damage roll, stun, targets, geometry, timing and death handling are unchanged. The helper returns the original integer directly at 100%, so neutral behavior and RNG order are preserved. Unowned/environmental bombs retain the unattributed 100% origin. Saved bombs retain their actual stored creator.

## Regression scope

`FlashBangBombDamageProvenanceTest` exercises Player, Enemy and Boss creators at every 25% step from 25% through 500%; the base pre-armor hit and electricity hit are asserted separately. It also checks neutral 100%, unattributed/environmental behavior, stored Boss creator restore, original Bomb/Electricity source objects and the next RNG value. Three new tests bring the intended full inventory to 2,975 tests / 454 suites.

This is a source-only candidate pending the exact-source gate. No APK, save schema, score, Custom Rules setting, signing identity, application ID, build request or publication changes.

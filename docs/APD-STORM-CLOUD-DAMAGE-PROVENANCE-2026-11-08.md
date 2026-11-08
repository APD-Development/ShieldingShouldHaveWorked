# Storm Cloud damage provenance — 2026-11-08

## Confirmed defect

`StormCloud` damages fiery characters for the same raw amount as Toxic Gas while converting its occupied cells to water and clearing Fire. Potion throws already carry their immutable thrower through `OffensiveEffectContext`; delayed Chaotic Censer gas already stores its actual creator. The blob did not retain that context and passed its raw damage directly to `Char.damage`, so attributable Player, Enemy or Boss cloud damage remained at 100%.

## Bounded correction

The blob now retains cell-by-cell offensive origin proportions using the existing `CellDamageOrigins` metadata used by Toxic Gas, Electricity and Fire. Only the already-generated fiery-target damage amount is scaled, exactly once. The original `StormCloud` object remains the damage source. Diffusion density, volume, cell order, water conversion, Fire clearing, immunity/property checks, timing and RNG are unchanged. Neutral 100% returns the original integer directly. Unowned/environmental clouds remain unattributed. Existing saves without metadata remain unattributed; current saves retain exact mixed ownership without inventing modern defaults.

## Regression scope

`StormCloudDamageProvenanceTest` covers Player, Enemy and Boss creators at every 25% step from 25% through 500%; mixed ownership; unowned fields; save/reload and legacy saves; neutral topology, water, volume and next RNG; the delayed saved Chaotic Censer creator; and the current Potion/Censer context boundaries. Six tests bring the intended full inventory to 2,992 tests / 456 suites.

This is a post-APD378 source candidate pending the exact-source gate. No APK has been assembled or phone-tested. APD378 remains the current actually published playable candidate and does not contain this correction. No save schema replacement, score, slider, signing identity, application ID, build request or publication changed.

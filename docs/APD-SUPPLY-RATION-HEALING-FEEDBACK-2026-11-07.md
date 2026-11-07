# Supply Ration scaled-healing feedback

Prepared: 2026-11-07T15:04Z  
Parent: `e5845504982a0cb3f9f8cf985d77cce29556c6cc`  
Status: pending remote source verification; no APK

## Confirmed defect

`SupplyRation.satisfy` already scales its base 5 HP offer exactly once through `CustomHealingHooks.amount`, then caps at maximum HP. Its floating healing text nevertheless always displayed `5`. Healing 0%, 25%, 200%, and capped non-neutral heals therefore reported a value different from the actual HP gained.

## Bounded correction

Only the displayed value now uses the existing pure `CustomHealingHooks.display(hero, before, 5)` helper. This matches the established APD policy: non-neutral settings display the actual HP delta, while neutral 100% preserves the original upstream text even when capped. The base request, fractional-credit accumulation, final cap, combat telemetry, nutrition, Cloak recharge, action time, talents, RNG, turns and saves are unchanged.

## Regression

The real protected `satisfy` path is invoked at Healing 0%, 25%, 100% and 200%. The test checks actual HP, floating text, a capped 200% heal reporting 1, and neutral capped behavior retaining the original 5. Required acceptance is the exact 2,887-test/446-suite inventory with zero failures/errors/skips plus Android debug/release/instrumentation Java and desktop compilation. No APK assembly or phone claim.

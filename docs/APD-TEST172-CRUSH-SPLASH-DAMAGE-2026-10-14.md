# TEST172 — Crush splash Player Damage scaling

Started: 2026-10-14T21:11:59Z

## Reconciled source
The approved branch advanced after TEST171 to `c684d5070fc26bca6cc2d34b414ea18ecbefbb49`. That commit was preserved. TEST171 evidence applies only to its exact source and does not verify this newer application change.

## Bounded correction
Warrior Crush splash now routes the hero-owned offensive splash roll through the existing Player Damage hook exactly once. Scaling occurs before the original halving, target armor roll and Vulnerable adjustment. Enemy Damage and Boss Damage do not replace Player Damage for this hero-created effect.

At neutral 100%, the calculation retains the original `Math.round(damageRoll * 0.25f * comboCount)` result and consumes no new RNG. The original combo count, target selection, DR, Vulnerable effect, turns and source behavior remain.

## Retained regressions
Three tests require:
- 200% and 50% Player Damage results before the original fixed-armor order;
- original rounding for raw rolls 0–99 and combo counts 1–24 at neutral, plus identical next RNG;
- every permitted 25%–500% Player Damage value, while contradictory Enemy/Boss values cannot affect the hero-owned result.

## Verification request
TEST172 requests 1,590 complete core tests, debug assembly, compiled history against the immediate previous TEST171 APK, 26 API34 emulator checks and 34 valid screenshots. This source is candidate-only until that exact evidence passes.

No player-save, run-history, scoring, application identity, signing, delivery or physical-phone claim is made. TEST123 remains the last owner-delivered/signature-compared APK.

# TEST177 — Weapon-triggered spell bonus provenance

Started: 2026-10-14T22:08:00Z

## Confirmed omissions
Current-source audit after verified TEST174 found two independent offensive bonuses in `Weapon.proc` that bypassed the custom damage boundary: Holy Weapon's flat post-enchantment bonus and Smite's independently rolled spell bonus. Both are produced by the actual hero attacker and are separate from the already-scaled base weapon hit.

Crystal and Shocking enchantment damage deliberately remain unchanged: each derives its amount from the already once-scaled, post-mitigation `damage` argument, so applying another custom boundary would double-scale it.

## Bounded correction
A shared helper applies the actual creator's Player, Enemy or Boss Damage factor once to those independent bonuses and preserves each original roll, proc multiplier, source object, trigger order and direct-damage behavior. Neutral 100% is identity. No attack roll, base weapon damage, armor, score, save format, class mechanic or RNG order changes.

## Retained regressions
Three methods require all permitted 25%–500% factors and creator roles with contradictory settings, exactly one source-preserving hit, neutral amount/next-RNG identity, and exact wiring of both production calls with the old direct calls absent.

## Verification request
TEST177 requests 1,599 complete core tests, debug assembly, compiled history against immediate previous compiled TEST176, 26 API34 emulator tests and 34 valid screenshots. Candidate remains unverified until exact-source evidence passes.

Private delivery is not requested. TEST123 remains the last owner-delivered/signature-compared APK.

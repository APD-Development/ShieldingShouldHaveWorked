# TEST178 — Tipped-dart bonus damage provenance

Started: 2026-10-14T22:37:24Z

## Confirmed omissions
The post-TEST177 current-source audit found two independent tipped-dart bonus rolls that bypassed the custom offensive boundary: Shocking Dart's electrical burst and Holy Dart's anti-undead/demonic bonus. Both occur after the base missile hit and are therefore not represented by the already-scaled base attack damage.

## Bounded correction
A shared tipped-dart helper applies the actual attacker's Player, Enemy or Boss Damage factor once to these two independent rolls. Original random ranges, Shocking Dart's Electricity source, Holy Dart's dart source, charged-shot/alliance gates, visuals, direct mitigation and RNG order remain unchanged. At neutral 100%, amount, source and next RNG are identical.

## Retained regressions
Three methods require all permitted 25%–500% factors for player, enemy and boss creators under contradictory settings; exactly one source-preserving damage call; neutral amount and next-RNG identity; and exact production wiring with the two old direct calls absent.

## Verification request
TEST178 requests 1,602 complete core tests, debug assembly, compiled history against immediate previous TEST177, 26 API34 emulator tests and 34 valid screenshots. Candidate is not verified until exact-source evidence passes.

Private delivery is not requested. TEST123 remains the last owner-delivered/signature-compared APK.

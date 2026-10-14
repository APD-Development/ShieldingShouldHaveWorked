# TEST174 — Remaining direct-damage wand provenance

Started: 2026-10-14T21:51:03Z

## Confirmed omissions
Current-source audit after TEST173 found seven direct-damage calls that did not use the custom offensive boundary:
- Blast Wave's two normal-zap targets and its Mages' Staff paralysis detonation;
- Living Earth's two normal-zap damage branches;
- Prismatic Light's ordinary and demonic/undead target branches.

These rolls are distinct from already-attributed Blast Wave collision damage, Living Earth guardian attacks and Prismatic's non-damaging staff cripple.

## Bounded correction
A shared direct-wand helper applies the actual creator's Player, Enemy or Boss Damage factor once, then calls the original target with the original wand source. Each caller retains its existing damage roll, proc chance, target filtering, direct-damage mitigation, visual effects, turns and RNG order.

Neutral 100% is identity. No field, guardian, collision, buff, save-format or score behavior changes.

## Retained regressions
Three tests require:
- all permitted 25%–500% values for player, ordinary-enemy and boss creators with contradictory other settings;
- exactly one damage call with the original source;
- neutral amount and next RNG identity;
- all seven production calls to use the shared boundary and the old direct calls to be absent.

## Verification request
TEST174 requests 1,596 complete core tests, debug assembly, compiled history against the immediate previous TEST173 APK, 26 API34 emulator checks and 34 valid screenshots. Candidate remains unverified until exact-source evidence passes.

No player-save, run-history, scoring, application identity, signing, delivery or physical-phone claim is made. TEST123 remains the last owner-delivered/signature-compared APK.

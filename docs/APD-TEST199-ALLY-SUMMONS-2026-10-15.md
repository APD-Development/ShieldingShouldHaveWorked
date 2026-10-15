# TEST199 ally-summon recording candidate — 2026-10-15

## Reproduction
Current source already preserves explicit offensive creator ownership for summoned actors, including save/load and later damage attribution. It did not emit a run-history observation when a player-owned ally was created. Inferring ownership from allied alignment would be wrong because enemy and boss summons, transformed creatures and legacy actors can also be allied or unattributed.

## Correction
Telemetry 1.13 adds the public fact `ally.summon` at `Char.apdInheritOffensiveOrigin`, the existing explicit creation-time ownership boundary. The hook fires only on the first transition to PLAYER ownership and only when the actual creator is the hero or visible to the hero. It stores displayed ally and creator names plus the current floor. Enemy, boss, unattributed, repeated ownership, off-screen-creator and restore-time paths do not emit a public fact.

The hook reads existing state only, consumes no RNG and is wrapped by the recorder's failure isolation. Actor save metadata and offensive attribution are unchanged.

## Retained tests and required verification
`AllySummonRecordingTest` contains two methods covering direct hero creation, a visible player-owned ally creating another ally, exact-once repeated inheritance, actual displayed creator identity, enemy/boss/unattributed exclusion, off-screen creator secrecy and restore suppression. The fact-kind manifest, telemetry version contract, records-history milestone and source-hook count advance together.

Required hosted gate: 1,613 complete core tests, debug assembly, complete reviewed history since immediate-previous compiled TEST197/Codex TEST 194, 27 API34 emulator methods and 35 valid screenshots. Candidate is pending until those checks pass.

No gameplay, damage amount, source, turn, RNG, save field, permanent score, custom-rule, application ID or signing behavior changed. No APK distribution or physical-phone claim. TEST197 remains the verified compiled fallback; TEST123 remains the last owner-delivered/signature-compared APK.

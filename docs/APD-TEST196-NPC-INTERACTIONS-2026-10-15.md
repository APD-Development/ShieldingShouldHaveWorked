# TEST196 — canonical NPC interaction recording candidate

Timestamp: 2026-10-15T06:35:47Z

## Reproduction

The first queued candidate, levers, is not a mechanic in the current 4.0 source: there is no lever class, terrain constant, English message key, or interaction path. No invented event was added.

The next approved candidate is a concrete omission. The central hero interaction path called `Char.interact`, but the recorder only emitted `npc.quest` when one of four quest state machines advanced. Repeated conversations, shopkeeper interaction, and other accepted NPC interactions therefore left no canonical observation.

## Candidate

- Add public fact kind `npc.interact` at telemetry 1.12.
- Record exactly once after an NPC accepts the central hero interaction.
- Store only the NPC name already shown to the player and the current floor.
- Keep `npc.quest` unchanged and separate.
- A rejected interaction produces no event.
- Recorder calls remain guarded and exception-isolated; gameplay result, turns and RNG are unchanged.

## Verification request

Pending hosted verification: 1,610 core tests, debug assembly, immediate-previous compiled TEST192 APK-history comparison, 27 API34 methods and 35 validated screenshots. This is application source, so no TEST192 artifact reuse claim applies after the candidate commit.

No save, permanent statistics, scoring, custom-rule, gameplay, identity, signing, delivery or physical-phone change. TEST192 remains the verified fallback until the complete gate passes; TEST123 remains owner-delivered/signature-compared.

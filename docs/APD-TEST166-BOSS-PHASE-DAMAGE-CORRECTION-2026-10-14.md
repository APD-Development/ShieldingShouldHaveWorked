# TEST166 boss-phase damage correction candidate — 2026-10-14

## Demonstrated failure
- TEST165 source `314b678c1aa7c85d4a84f6d5a02ad1929f43ecab`, run `38618183149` (2026-10-14T18:47:45Z–18:49:47Z), compiled and executed 1,581 core tests with 2 failures.
- The new fire-field regression invoked visual particle emission in the headless suite. Existing electricity acceptance also proved a newly created context-free Tengu shocker field had become unscaled.

## Correction
- A newly created Tengu fire or shocker field with no transient creator context defaults to Boss ownership, matching the only gameplay creator and prior Tengu behavior.
- Restored historical Tengu fields lacking provenance metadata are marked legacy-unknown and remain unscaled. Explicit creator context still overrides the default and supports mixed fields.
- The fire persistence regression now retrieves the restored per-cell provenance and calls the production pre-attach Burning entry point directly. It retains the mixed 300% reaction/source assertion without entering renderer-only particles.
- No gameplay RNG, turns, mitigation, source object or density changed. Other TEST165 production boundaries are unchanged.

## Verification state
- Expected complete core total remains 1,581. Core, debug assembly, immediate-previous TEST164 compiled history, 26 API34 checks and 34 screenshots are pending.
- TEST164 remains latest verified and TEST123 remains last owner-delivered/signature-compared. Three Damage controls remain disabled; no delivery or physical-phone claim.

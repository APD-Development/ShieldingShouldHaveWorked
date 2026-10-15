# TEST205 — visible boss-phase telemetry

Date: 2026-10-15
Branch: `codex/apd-phone-fixes-2026-10-09`
Previous verified compiled source: TEST204 `9549d3638de133cb09864e1c325f15b325c550b2`, run `38722796011`, internal Codex TEST 200.

## Reproduced omission

The five main bosses already have explicit gameplay transitions with visible effects, dialogue, invulnerability or arena changes, but UIMSD had no public fact for them.

## Boundary

Telemetry 1.16 adds `boss.phase` only after a real transition:
- Goo: enraged.
- Tengu: arena.
- DM-300: each supercharge and each return to normal/final combat.
- Dwarf King: shielded and final.
- Yog-Dzewa: each fist phase and final.

The public payload contains only the boss name displayed at that moment, the boss-local phase number, current floor and public state label. Initial encounter state is implicit. Restore paths do not call the hook. No HP threshold, cooldown, target, hidden summon choice, random result or undiscovered state is exposed.

## Required gate

- 1,622 complete core tests, zero failures/errors/skips.
- Debug assembly.
- Complete immediate-previous compiled TEST204/Codex TEST 200 history.
- 27 API34 methods.
- 35 valid 320×640 screenshots and contact-sheet review.
- No private delivery or physical-phone claim.

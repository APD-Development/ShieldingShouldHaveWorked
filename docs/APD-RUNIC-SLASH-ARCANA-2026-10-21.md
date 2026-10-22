# APD Runic Slash / Projecting and Arcana description repair
Reviewed 2026-10-22T02:10:49.784Z; application source checkpoint `a081b8791570ee3cfb3bc4c019e845e6f7c49ae7`. Owner report 2026-10-21.

## Confirmed root cause
`Weapon.Enchantment.genericProcChanceMultiplier` originally consumed and detached `RunicSlashTracker` (and `ElementalStrike.DirectedPowerTracker`) even when `Weapon.reachFactor` called it merely to check a target. This mutated an actual pending attack while trying to inspect reach. `RunicBlade` applied the boost before its `hero.canAttack` check. The targeting range itself was already finite through `KindOfWeapon.canReach`; at +6 the temporary enchantment boost is 3+0.5*6 = 6 (600 percent added power). Without Arcana or other modifiers, Projecting increases reach from one to eight tiles, and a ninth tile is beyond range. Do not classify legitimate eight-tile attacks as an infinite-range exploit.

## Implementation
- `Weapon.Enchantment.genericProcChanceMultiplierReadOnly` shares its power arithmetic with the actual consuming proc method but leaves one-use buffs attached during a range check. Actual combat procs still consume normally. The projecting reach calculation is bounded by the maximum possible shortest path on the finite current floor, preventing overflow without reducing a physically reachable attack.
- Runic Blade uses one `runicSlashEnchantBoost()` formula both for temporary attack boost and its description.
- Arcana Projecting previews distinguish melee added/total reach, ranged wall-penetration radius, separately added Runic Slash power and actual other active power modifiers. ASCII "to" replaces the invalid glyph. Identified Arcana and equipment details are required; unknown identities/upgrades remain suppressed.
- `ArcanaInfoDisplay` provides source-specific text for the 48 built-in enchantments/curses/glyphs and calculated strength for exact effects such as Blocking, Chilling, Shocking, Venomous, Eldritch, Elastic, Blooming, Potential, Entanglement, Repulsion, Affection and Thorns. Target-dependent and randomized outcomes are explicitly qualified, not assigned invented damage/turns.
- No gameplay damage, scoring, spawn/hunger, save format, raw UIMSD history, RNG or signing changes.

## Verification
Core run https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/39709404209: 2,046 tests /397 suites /0 failed /0 errors /0 skipped; preserved all six shared UIMSD window regressions. Earlier run39707812284: 2,044/397 with only the new English message-count fixture failure. Run39708731383: 2,045/397 passed after exact reviewed count correction. Run39709184603 did not execute JUnit due to a nonexistent test-only cleanup method, subsequently corrected at core-only run39709404209.

No Android APK has yet been built from this exact source at time of this report. Latest previously produced private developer/player pair is TEST305; TEST304 is the all-display verified fallback. API34, direct native Runic interaction, multi-device layouts, installation over real owner's save and physical RedMagic gameplay remain to be validated. Do not publish or distribute.

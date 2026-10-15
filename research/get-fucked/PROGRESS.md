# Get Fucked: retained-state experiments

This is feasibility research on `codex/get-fucked-feasibility-tests`. Production main remains `8228fbcb030b3b0a2a787b6e3ea1cb8593e3e13d`. No scoring or gameplay changes are included.

The full-run verdict remains unresolved. The work below establishes consecutive early sections, not Amulet victory or ascension.

## Evidence chain

1. `provenance/floor3-prefix.java.txt` and the compressed prefix log record commands from the actual level-1 Cleric through entering floor 3. The native checkpoint contains that earned hero, all three generated floors, native game globals, and the captured RNG stack. No replacement character or refreshed inventory is used.
2. The current `GetFuckedRouteTest` can restore that checkpoint and checks retained HP, maximum HP, hunger, inventory, equipment, XP, strength, talents, whole and partial Tome charge, monster IDs/positions/HP/timing, elapsed time, gold and energy before continuing.
3. The continuation consumes the second ration, waits 60 actual turns, acquires and spends floor 3's upgrade scroll on the existing spears, defeats two bridge gnolls and two spawned enemies, finds secret doors using actual search actions, acquires the levitation potion and keys, retrieves and equips the +2 quarterstaff, and escapes the chasm vault before levitation expires.
4. It then pays six waiting turns for a Tome recharge, kills the bottom snake and rat, uses the earned iron key to enter the alchemy room, collects five energy and the second strength potion, drinks it to reach 12 strength, and converts the owned healing potion into one shielding potion using the production alchemy inventory, recipe, energy-payment and crafting methods. Four energy is spent. The potion remains unconsumed.

The captured floor-3 checkpoint begins at total time 632, level 2 / 4 XP, 11 strength, hunger 332, 1/1 HP, no shields, one +2 spear, one ration, one pasty, an unused healing potion, +0 mail carried, +0 cloth worn, and a +1 Tome with one whole charge and 0.367043048 partial charge.

## Replaying the continuation

Use a separate checkout of this research branch. The test-only Gradle options forward `route.*` properties and optionally pin HotSpot object hashing:

```bash
./gradlew :core:test --tests '*GetFuckedRouteTest' \
  -Droute.resume="$PWD/core/src/test/resources/apd/get_fucked_seed266_floor3" \
  -Droute.stableHash=true -Droute.noSnapshots=true
```

The pinned setting is `-XX:+UnlockExperimentalVMOptions -XX:hashCode=2`. It stabilizes object hashing and thus the existing scheduler's unspecified HashSet ordering among actors with equal time and priority. It does not replace the scheduler, damage rolls, AI, resources, or challenge rules. Two identical section traces were observed under this setting. This is controlled desktop-engine evidence; reproducibility on the target Android runtime remains unvalidated. The earlier earned prefix was recorded under the default JVM hash setting, then resumed through the normal native loading path.

The source at checkpoint capture is preserved separately because later experiment edits can change allocation and tie ordering. A seed number alone is not a complete action-replay fixture.

## Concrete failures and corrections

- At 11 strength, unupgraded mail has a three-point strength deficit. Its armor roll loses six points, eliminating its armor-roll protection while slowing movement and reducing evasion. The continuation uses cloth and the legally acquired quarterstaff instead.
- The bomb can kill one or two rats depending on actual AI/tie ordering. The prefix contains explicit investigated variants and retains the actual spent bomb and charges. It is not a guarantee across all executions of seed 266.
- Guiding Light makes the next physical hit reliable when strength requirements are met. The illumination is consumed by that hit; it does not guarantee every subsequent spear hit. A second spear can miss.
- Seeing an enemy does not establish a clear projectile path. Terrain and nearer characters intercept shots. A missed terrain shot remains spent in the prefix.
- Moving onto a heap while enemies are visible may leave the item uncollected. The scroll and potion acquisitions now explicitly verify inventory; key counts are logged separately.
- The apparently passive spear-wielding gnoll is an enemy variant, not a companion. It has reach two and can become hostile after damage or a negative buff. The tested sequence leaves it passive.
- A waiting experiment on floor 2 allowed a spawned gnoll to approach from behind and failed. Recharge time is paid and spawns remain active.
- The potion approach initially stopped at a secret door. The corrected sequence pays both search time and the engine's additional search hunger cost.
- The native alchemy population helper expects ingredient templates. Passing the owned object itself does not select it because `getAllSimilar` excludes that identical object. The corrected adapter uses a non-inventory template, then lets production code detach the actual owned potion. No template is collected as an item.

## Still to investigate

The current character remains in floor 3's alchemy room. A spawned 50-HP swarm is wandering near its exit. Escaping or defeating it, dealing with the fast crab near the exit, obtaining further earned talent points, floor 4, Goo, the remaining bosses, Horn acquisition/upgrades, and the entire ascent have not been validated in this continuous chain. The new shielding potion is only ten barrier at 1 maximum HP and decays normally; it cannot be credited to several later encounters.

The old isolated tests and audit are retained. Their supplied combat fixtures are not treated as earned states in this route. The exported engine came from CI commit `0d09a9ae11a46088ad99f77b4a30eb88e37ae7ac`. A local combined regression attempt exited without a JUnit completion count, so it is not recorded as 41 newly passing tests.

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

The continuation now reaches floor 4. It earns level 3 from an actual spawned snake and spends the new point on Shield of Light. The crafted shielding potion is consumed against the pursuing swarm, including every split child. The barrier drops from ten to three through that encounter, then to one during a paid 28-turn recharge. The crab near the exit is lured around projectile-blocking decorations, shot twice (the second shot misses), then killed in melee. Waiting in the doorway rather than stepping into the room avoids waking the original swarm during this fight. The owned blink stone is then consumed to reach the stairs. The original swarm remains alive and is saved on floor 3.

Two executions produce identical 362 command-state observations from the earned floor-3 checkpoint through floor-4 entry. Native saves differ only in corresponding generated missile-set identifiers, not the observed actions or resources. The exact first output save is preserved as `get_fucked_seed266_floor4`.

At floor-4 entry: 1/1 HP, zero shield, level 3 / 8 XP, 12 strength, +2 quarterstaff equipped, +0 cloth equipped, +0 mail carried, +2 Tome with zero whole charge and 0.295187175 partial charge, one pasty, waterskin, velvet pouch and the original cudgel. Neither missiles, healing/shielding potions nor blink stone remains in the inventory. The ration at floor 3 cell 604 and spears at cells 585 and 652 remain uncollected. Hunger is 369, exactly 961 elapsed turns + 8 extra search hunger - 600 from two consumed rations.

Floor 4, Goo, the remaining bosses, actual Horn acquisition/upgrades, alternative class routes and the entire ascent remain to be validated. This remains a partial legal route, not proof of victory.

The old isolated tests and audit are retained. Their supplied combat fixtures are not treated as earned states in this route. The exported engine came from CI commit `0d09a9ae11a46088ad99f77b4a30eb88e37ae7ac`. A local combined regression attempt exited without a JUnit completion count, so it is not recorded as 41 newly passing tests.


## Additional measured decay results

Three new isolated production-Barrier tests pass: ten barrier lasts 61 ticks without damage; 75 barrier lasts 135 ticks; adding 25 every 43 ticks stays positive in the tested no-damage recurrence and settles at a six-to-31 cycle. These supplied increments are mathematical decay fixtures, not owned spell charges, and are never inserted into the played route. Combined with the existing 17 Get Fucked rule tests and two ShieldHunger tests, the local completed run reports `OK (22 tests)`.

The dungeon seed is 266. The initial runtime RNG is separately controlled at seed 266 for the command experiment and is captured in native continuations; a dungeon seed alone does not ordinarily fix every combat result. The sequence does not reseed between encounters.

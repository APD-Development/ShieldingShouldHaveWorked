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

## Floor-4 continuation

The original 200-turn entrance recharge fails: a spawned slime reaches the unprotected hero and kills it. A bounded 120-turn wait after eating the earned pasty allows an active route. The meal clamps hunger 369 to zero, wasting 81 of its 450 food value; the three eating turns then accrue hunger normally.

The replay from the actual floor-4 save defeats crab 81 using Shield of Light, Guiding Light and one quarterstaff attack. The remaining charge pays Shield of Light against spawned slime 87; four melee actions, including a miss, kill it. After twenty paid waiting turns, the northern gnoll is killed in a single actual melee hit. The original swarm follows the hero into the exit room.

The successful variant obtains the liquid-flame potion at 474, casts Shield of Light, moves to 473, and throws the potion at 436. The offset explosion burns the swarm but excludes the hero. It opens the chest at 472, casts Holy Ward with the newly recharged charge, acquires the three shuriken, attacks the parent once, uses APD's first shuriken throw at zero time to kill the parent, and kills the split child in melee. Level 4 and its first Searing Light point are earned. No monsters or resources are supplied. A replay after recovering the workspace reproduced the result at total time 1158, hunger 197, 1/1 HP, no barrier, Tome zero whole charge and 0.091669507 partial charge.

The shuriken reveals the Sacrificial curse. Its production proc has a 0.1 outer chance and, at 1/1 HP, a 0.125 inner chance of applying at least one bleeding. That is a potential lethal 1.25% proc per applicable hit before modifiers, not an unavoidable death. The observed successful throw does not apply bleeding.

This success has a resource conflict: the floor's food, upgrade scroll and toxic-gas potion are behind barricade 746. The liquid flame used in the swarm fight was the natural means of opening that barricade. Those supplies are NOT credited to the successful continuation. Nearby traps are AlarmTrap 519 and ChillingTrap 996, not a substitute fire source. Preserving the flame while fighting the unburned swarm failed in tested action orders. A replacement route remains under investigation.

The preserved native north-room save and compressed replay log are in provenance. Early floor-4 failed trials before a workspace reset were not committed; their findings above are based on recorded outputs. The successful north-room section was replayed after restoration and saved again. Neither boss victory nor ascension has been established.

CI run 38815420848 on commit 633ffac4 completed successfully, covering the baseline test invocation and the pinned native floor-3-to-floor-4 replay.

## Earned floor-5 entry and initial Goo experiment

A changed spell order kills the unburned floor-4 swarm while preserving liquid flame: Holy Ward at hunger 190, Guiding Light after opening the chest, an actual melee hit, a zero-time shuriken throw that misses, movement to reach the split child, then two attacks on each remaining swarm (including a miss). The hero reaches level 4 at hunger 202, with the flame potion still owned. The parent drops one healing potion in this execution. This variant does not inherit the burned variant's damage or loot.

The extra slime still makes the food-room approach fail in tested sequences. A longer floor-3 exit wait also fails: spawned crab 74 reaches the waiting hero and causes actual death. A 90-turn initial floor-3 wait instead changes where the existing spawned gnoll 53 intercepts the route. These are failed preparation strategies, not universal impossibility.

The next successful continuation collects the healing potion and missed shuriken at 473, steps to the actual exit 513, and descends normally before the second slime can attack. The original floor-4 food, scroll, barricade and Ghost quest remain unacquired. Actual floor-5 entry is total time 1167, hunger 206, level 4 / 1 XP, 12 strength, 1/1 HP, no barrier, Tome +2 with zero whole charge and 0.316669553 partial charge, quarterstaff +2, cloth +0, mail carried, three +1 shuriken, one liquid-flame potion, one healing potion, Rage scroll, waterskin, pouch and cudgel. Gold is zero and energy one. The complete native save including all five floors is retained under core test resources.

From that earned floor-5 state, 100 actual waiting turns and a paid search of secret door 603 reach Goo's doorway at 430 with three whole Tome charges. Goo notices the hero and locks the floor. Hunger remains 323 during the subsequent actual attack and movement sequence. Guiding Light and the first zero-time shuriken reduce Goo to 82 HP; the retreat lets it heal on water, reaching 85 HP at the observed endpoint. No boss victory is claimed.

The rendering adapter now supplies GooSprite for Goo and completes pumpAttack through the same onAttackComplete callback used by the real animation. Omitting that callback would suppress charged attacks and invalidate a later boss test.

Source checks: Hunger.act returns while the floor is locked, preserving the original challenge's lethal-starvation rule rather than weakening it. LockedFloor starts with 50 turns of passive regeneration; Goo damage replenishes it and Goo's water healing reduces it, so a locked floor does not provide indefinite passive recharge. Ooze can deal its first one damage even on water, since its first act has not yet set acted=true. Merely standing in water is not proof of immunity to Goo's on-hit debuff. Missiles that stick in a living enemy are unavailable for reuse until actually retrieved; their nominal durability does not supply additional inventory ammunition.

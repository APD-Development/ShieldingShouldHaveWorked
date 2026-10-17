# Seed 266: continuous food, equipment and survival witness through time 6686

Original Get Fucked rules only: maximum HP remains one, and positive starvation damage is lethal through shielding. No production changes, resets, restored intermediate saves, extra challenge restrictions, scoring or autonomous player. All actions use the existing engine and the retained manual UI adapters. The unchanged test source SHA-256 is `29027ad0e357e4ec56fca77ea7af23fbfdb28292f2bfa319877b3ef8f91e9174`.

This establishes a longer consecutive resource witness, not a full-run victory. Earlier evidence defeating Goo and Tengu and separately reaching DM-300's first pylon remains preserved. DM-300 completion, Dwarf King, Yog, Amulet acquisition and the entire ascent remain unproved.

## Consecutive state ledger

Every row belongs to the same fresh level-one replay, `chasm-corridor-complete.log`. Tome and Hourglass figures include retained fractional charge; no resources are restored at a row boundary. HP/HT is 1/1 throughout.

| Time | Actual state | Hunger | Shield | Level / XP | Tome | Hourglass |
|---:|---|---:|---:|---|---:|---:|
| 5983 | Floor 14 / 745, after actual ration consumption and escape | 132 | 7 | 11 / 32 | 0.168640 | 0.240590 |
| 5984 | Actual adjacent fall to unsealed floor-15 entrance area | 133 | 6 | 11 / 32 | see native save | see native save |
| 6224 | 240 paid waiting turns completed there | 373 | 0 | 11 / 32 | 6.382925 | 3.298210 |
| 6240 | Floor 14 / 1094 after Ward and six frozen moves | 389 | 10 | 11 / 32 | 4.771485 | 0.488687 |
| 6253 | Key spent on the locked lab; energy crystal physically collected | 402 | 6 | 11 / 32 | 4.102254 | 0.662020 |
| 6259 | Owned Starflower fruit cooked and eaten; earned Sunray 2 | 1 | 4 | 12 / 32 | 5.251645 | 0.742020 |
| 6344 | Lab wraith and spinner actually killed; spear recovered | 86 | 8 | 12 / 41 | 1.322282 | 0.846122 |
| 6529 | Second real fall and 180 paid recharge turns completed | 271 | 0 | 12 / 41 | 6.048213 | 3.172789 |
| 6564 | Guarded upgrade acquired; mastered Greatsword +1 equipped after gas escape | 306 | 7 | 12 / 41 | 2.904460 | 0.601326 |
| 6569 | Shaman 529 killed with the actual sword | 311 | 6 | 12 / 49 | 1.040385 | 0.667992 |
| 6650 | Floor-13 brute 513 died after its rage expired; no incoming hit | 396 | 0 | 12 / 57 | 1.206894 | 0.724915 |
| 6679 | Magical fire extinguished; pasty acquired and eaten | 1 | 0 | 12 / 57 | 2.989966 | 1.107479 |
| 6686 | Food-room upgrade spent on Mail +4; actual key collected | 8 | 13 | 12 / 57 | 2.172827 | 1.197223 |

At 6686 the hero retains the mastered Greatsword +1, Mail +4 with its existing glyph, Tome +6, Hourglass +0, quarterstaff +2, cloth +1, spear +3, two boomerang pieces, Blast Wave (one charge plus 0.135385), Regrowth (two charges), healing potion, mind-vision potion, stylus, Sorrowmoss and Earthroot seeds, three Goo Blobs and waterskin. Gold 778, alchemical energy zero, strength 15. Raw fruit, Starflower seed, the floor-14 strength potion, both actually spent upgrade scrolls, the floor-13 frost potion and the pasty are consumed. Keys are represented by native Notes/records, not the abbreviated inventory list.

## What resolved the previously blocked sections

The real chasm falls dealt zero damage at HT=1 and still applied Cripple. The entrance area of the unsealed boss floor contains its four neutral pylons and does not yet contain DM-300. The hero paid hunger and world time for each recharge. Existing floor-14 actors remained in their original off-floor state; this is the engine's normal level transition, not a reconstructed room or reset encounter. The later boss still has to be fought.

The 240-turn recharge did not itself solve the shaman approach. Six actual frozen moves from 864 through 911, 957, 956, 1002 and 1048 to 1094 bypassed the immediate obstruction. Sunray on the fresh shaman 587 then allowed five paid moves into the western corridor. The original iron key was collected at 444 and spent to unlock the lab. Five collected crystal energy plus the one previously retained energy paid two for cooking and four for Mastery. The owned Starflower fruit provided food and one actual experience level, retaining 32 XP. The actual next talent point upgraded Sunray to two.

The strength potion was used exactly once, as Mastery's ingredient; strength stayed 15. The mastered unupgraded sword still required 16 strength. It was not credited with qualified accuracy. The later actual floor-14 scroll upgraded it to +1 and made its requirement 15, after which it was equipped. Regrowth rooted the guarding DM on eligible ground and grew cover, while Hourglass paid for the four crossing moves. The DM remained alive. Its gas inflicted three damage on each of the recorded ticks during upgrading and escape; those ticks consumed actual shielding. The hero left via 651/698/745 before equipping the sword. No artificial gas immunity or shield refill was added.

A wraith interrupted the lab recharge and was killed by a real wand zap. Spinner 525 then arrived with 45 HP. A frozen Sunray, Light, a second Sunray, the actual spear throw, another Sunray and the finishing Blast Wave killed it for nine XP without an incoming hit or poison. The spear was physically recovered. On the later shaman 529, Sunray plus instant Shield of Light enabled the approach; two sword hits dealt 15 and 16 after armor. Its one actual physical hit dealt zero after defenses. That kill awarded eight XP.

The floor-13 wandering brute was not removed from the dungeon. At zero shielding, a frozen step and Sunray were followed by a real knockback, movement, a second Sunray and spear hit. Damage broke paralysis early: it was already moving again with two HP at 6643. A second Blast Wave triggered its normal zero-HP rage and displaced it. Six actual retreat moves outlasted that rage, awarding eight XP. The dropped spear at 1014 was picked up on the return route. This demonstrates that the lethal rage can be avoided in this particular continuous encounter; it is not an armor-tanking claim.

The frost potion at 619 was physically collected rather than assuming access to the farther chest at 771. Throwing it from 1516 removed the actual EternalFire barrier. The hero then traversed the cleared tiles, acquired the pasty at 1664 and ate it before starvation. The food-room scroll was collected and spent on the existing mail, and the key was collected afterward. The unused stylus was not applied, so there is no favorable glyph grant or reroll in this section.

## Failures retained alongside the successful route

* `chasm-return-complete.log`: 150-turn recharge. Red shaman 526 already had Sunray's permanent used marker; another cast dealt damage but did not blind it again. A bolt consumed eleven shield. After a side blast displaced the hero, a later fourteen-damage bolt killed the hero on the retreat through 676. This is a failed sequence, not universal impossibility.
* `chasm-deep-reserve-complete.log`: 240-turn recharge. Entering 911 without a protected crossing exposed the hero to a fourteen-damage bolt at only ten shield; actual death. The NPC positions differed from the shorter camp and were not treated as interchangeable.
* `chasm-flank-complete.log`: the six frozen crossing moves succeeded. Moving to 1093 and then waiting left a firing line; an eleven-damage bolt killed the hero at nine shield. The successful corridor branch adds the real fresh-enemy Sunray and continues movement instead.
* Trial `1792217327868134392` is an adapter-input failure, not a character death. The prefix assembly accidentally appended an already-recorded prefix a second time; a spell assertion rejected that impossible command at 5983. The trial is retained, and the successful run's canonical command list contains the prefix only once.

## Validation and limits

The deeper-camp fresh replay matched the previously retained ration/escape state at 5983 across all fifteen native JSON files, permitting only the already documented global missile-set-ID bijection. Its active gameplay RNG also matched. The inactive unseeded fallback RNG is not claimed to match.

The complete fresh-origin corridor run ended healthy at 6686 with JUnit `OK (1 test)`, elapsed 1871.332 seconds including manual investigation time. An independent fresh-origin replay of all 426 fixed command records matched at 6686 across all sixteen native JSON files, under only the documented global missile-set-ID bijection, and matched the active gameplay RNG. The recorded verification is `independent-6686-verification.json`. Its paused-prefix log is preserved separately; it is not falsely presented as a second completed JUnit run. The actual independent character then remains available for continued original-engine actions.

Eight more XP are required for level 13 and its first tier-three point. Lay on Hands has not been granted to this character. Food, artifact charges, upgrades and foes for later sections must continue from the retained endpoint. The Horn is still unacquired. Full bosses, Amulet victory and ascension remain separate unvalidated work; the earlier DM-300 first-phase witness is retained as an earlier branch, not spliced into this one.

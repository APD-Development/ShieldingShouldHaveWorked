# Ring of Wealth TEST285 investigation

Status: source audit complete; deterministic core/simulation acceptance pending. No production loot changes.

TEST285 source0652d715cea86fb0242f4470cf870b3761672d0f. RingOfWealth.java is byte-for-byte identical to official current upstream SPD; RingOfWealth, Generator, Mob and Goo are unchanged in current TEST300 source d41631b3e5b43bf832036426a9e7c7a0694d07ed.

Separate channels: Swarm/Bat/Necromancer ordinary potions are Healing only, subject to diminishing run-wide caps5/7/6 and level eligibility. APD identifies only their normal Healing/Shielding drops. RoW multiplies ordinary drop chance by1.2^(effective positive level+1); it does not change a source's item table. An independent counter gives bonus rewards per roughly10 weighted eligible tries, with normal/miniboss/boss weights1/5/15. Upgrade level changes reward tiers and equipment quality, not this counter's cadence. Goo has no ordinary Healing guarantee; its die method separately drops WornKey and GooBlob. A Healing drop on Goo with RoW is consistent with a bonus, not a guaranteed Healing drop.

Default random potion weights combine both15-card decks: Strength0, Healing6/30, MindVision4/30, Frost/Flame/Toxic3/30 each, Haste/Invisibility/Levitation/ParalyticGas/Purity2/30 each, Experience1/30. Four independent category-draw Healing results therefore have probability(1/5)^4=1/625,0.16%. This is not a probability for four unspecified enemy drops. Conditional on normal potion drops from Healing-only enemies, all four being Healing is expected. Actual eligible-kill probabilities also depend on caps, split generation, ring level and source mix. All RoW regular-potion rewards include an additional Experience channel; their Healing fraction differs by reward tier/ring level. Quantity stacks and events must be counted separately.

Eight new deterministic tests run actual game generation, full Mob.rollToDropLoot, all5 regions and6 ring levels, boss/miniboss weighting, ordinary enemy tables/caps/identification, all18 custom rules at both limits, five difficulty presets, and tracker/ring/generator persistence. Expected/observed frequency tables and seeded streak counts remain pending.

No APD save format, loot probabilities, guaranteed drops or RNG-consuming production code changed. Do not add balancing changes based only on four observed potions. Detailed final source/evidence report will follow the completed run.

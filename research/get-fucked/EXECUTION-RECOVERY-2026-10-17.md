# Execution recovery and continuation evidence

Original rules remain fixed maximum HP 1 and lethal starvation through shielding. No scoring or production gameplay change.

The persistent checkpoint at totalTime 6190 is a completed fresh-level-one route: actual Frost771 acquisition, fire clearance, Pasty consumption, actual SOU/key ownership. Two independent full fresh runs matched all 16 native JSON files and ACTIVE gameplay RNG. The archive and comparison evidence are retained at commit 948b10116d416d96e60b7cbf04815524ef3c7853.

An ensuing live continuation was observed alive through 6359: floor13/1244, HP/HT1/1, barrier0, hunger175, level12XP30, STR15, Mastered Greatsword1/Mail4Repulsion, Tome6 charge0+0.7344220877, Hourglass0+0.80649734, Blast0+0.7664963, Regrowth0+0.35247937, Healing1, MindVision consumed, spear unowned on1537, two worn boomerangs, gold700/energy2. Spider50341HP is Hunting1279; Brute65140HP Wandering546; DM20050480HP Wandering1500; Bat50630HP Sleeping1617; Spider50850HP Sleeping343; Blacksmith5011052. No food, Horn, Ankh or Lay on Hands is credited.

The local execution environment was replaced and its live session and uncommitted native files became unavailable. The next submitted moves (1208,1172,1137) have no retained successful result and MUST NOT be counted. This is an execution interruption, not a demonstrated hero death.

The attached after-6212 journal reconstructs the exact manually submitted command sequence from the retained conversation/tool trace. It is appended to frost-food-provenance's manual-thirteen-armor-xp-complete.journal, whose final Blast1266 originally exposed the discarded-loot sprite null pointer. The published test adapter now binds the original discarded-loot presentation only; all item falling, XP, RNG, damage and scheduler work remains original. A full fresh replay of the combined 478-command journal is underway, with no native restore. Its 6190 and 6211 native checkpoints must match the retained archive before extending the run. The later reconstructed continuation needs a completed fresh replay and then an independent full-native/RNG repeat before being promoted to the same evidence grade.

Observed mechanical findings requiring preservation:
- Pit deaths halve XP (odd XP rounds randomly). The 6212 brute gave4, not8.
- Normal poison/rage death of Brute502 gave8 after legal Sorrowmoss production and kiting; hero never stepped on the poison plant.
- Base Regrowth2 charges spends ONE charge: ceil(30% curCharges), giving Roots4 and maxrange4. Older all-charges/Roots8 assumptions are wrong.
- Sunray blind is once per living enemy; a timely second cast can paralyze, but damage can break Paralysis early.
- Wandering.noticeEnemy spends NO time, so detection can lead to an immediate attack in the same world tick. No assumed detection grace is valid.
- Hero.handle on a nonadjacent target may perform multiple moves. Every actual world/hunger cost must come from the engine, not command counts.
- One-way ballistics occur. Red645 cast from1422 toHero1462 although Hero's shot stopped1461. Its later 11 bolt was absorbed by12 shield after a real sideblast1463 pushed Hero1462->1460.
- The last Regrowth1350 legally rooted Spider503 through a cone outer ray despite the blocked central line. The Red encounter finished with actual sword rolls (hit14, miss, miss, hit27), counters miss/0, and ordinary final shield decay to0.
- Throwing the actually owned, unidentified MindVision on Stormvine1387 used original Potion.onThrow pressCell and shatter to clear it without a dropped heap. It granted no MindVision buff. This paid resource permitted the north exit; Healing remains held.
- The spear recovered1236 was thrown again and is unowned at1537 (durability7.3824 when last inspected); do not duplicate it.
- The last observed mob movement and recharge reserve are continuous. DM300, later bosses, Amulet and ascension are still unproved.

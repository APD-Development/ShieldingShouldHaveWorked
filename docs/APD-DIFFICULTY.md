# APD difficulty system (framework)

Levels 1-5. **3 = the game as it is** (the baseline; it never changes). 1-2 easier, 4-5 harder. **No balance values are approved yet:** every level is a
neutral placeholder, so every level currently plays exactly like 3. The framework is ready for the owner to choose the actual modifiers.

## Architecture (one place to edit)
- `apd.difficulty.Difficulty`: the level range, `Modifier` (ENEMY_HP, ENEMY_DAMAGE, RESOURCES, HEALING, FOOD, ENEMY_SPAWN, BOSS, CHAMPION, TRAPS), the
  modifier table (rows = levels, all 1.0 now) and the balance revision per level (0 = placeholder). Level 3 returns 1.0 whatever the table holds, and
  scaling is exact (no float rounding) when a modifier is neutral. Gameplay asks for a Modifier, never for the level.
- `apd.difficulty.DifficultyHooks`: the game's seams, each called from ONE place: `Level.createMob` (enemy HP; champion/boss rules join here) and
  `Char.attack` (damage of hostile creatures only). A new difficulty-sensitive system = add a Modifier, add one hook call, set the table.
  Resources/healing/food/spawns/boss/champion/traps are named modifiers with no seam yet: they are wired when their values are approved.
- Neutral at 3 and at every other level today: pinned by tests (level generation hashes, scaling exactness, seams are identity); a test marked
  REPLACE-WHEN-APPROVED asserts the placeholder state.

## Identity and persistence
- Setting: `SPDSettings.apdDifficulty()` (1-5, default 3) in APD Settings: "Difficulty (new runs)". It only affects runs started afterwards.
- Run: `Dungeon.difficulty` is set from the setting when a run starts and is saved with the run (`apd_difficulty`). Changing the setting never changes
  a run that exists. A save from before difficulty existed loads as `difficultyKnown = false`: it plays as 3 but is never labelled with a difficulty.
- Ruleset: difficulty 3 adds nothing to the rule text, so its id is exactly the id those rules had before (older runs and new difficulty-3 runs share a
  ruleset: same balance). Any other level adds `difficulty=N;balance=R` to the text, so every level (and every balance revision) is its own ruleset and the
  default scope never mixes them. `Ruleset.current()` is the run in progress, `Ruleset.forNewRun()` is what the settings would give.
- Telemetry 1.7: RUN_START gets two appended fields (difficulty, balance revision); 0 = not recorded. Older logs end before them and read as not recorded.
  Archive redaction copies them (not hidden information).

## UIMSD
- Runs column `Difficulty` (1 - Easiest ... 5 - Hardest; `?` for runs recorded before 1.7, never 3) and `Balance revision`; both filterable/sortable/groupable.
- View "Difficulty": runs grouped by difficulty and balance revision across all rulesets; the unknown group is runs from before difficulty existed.
- Floor replay uses the run's difficulty (a replay of a run recorded without one uses 3).

## Honesty about the placeholder
The selector says that levels other than 3 currently play like 3 but are recorded as chosen. Their runs have balance revision 0 and their own ruleset, so
they can never be confused with runs played under approved balance.

## Non-P2W (permanent)
Difficulty is a free gameplay setting. Nothing may ever be sold that selects, unlocks or alters it or its results.

## Needed from the owner
The actual modifier values per level (and which systems to bring under control first). Then: fill the table, add seams, set balance revisions to 1, and
replace the placeholder test.

# Floor replay (canonical seed floors)

Phase A of the future-floor feature (study: APD-FUTURE-FLOORS-FEASIBILITY.md). Decisions: canonical, always labelled; unreached floors
of FINISHED runs may be interrogated; replay only when no live game; recorded telemetry is authoritative for reached floors.

## What it returns
`FloorReplay.replay(seed, challenges, heroClass, maxDepth)` -> `List<ReplayFloor>` for floors 1..N (max 26, main branch only).
Floor N = what the seed gives after floors 1..N-1 were generated in order under the **reference context** `ref1-classkit`:
- reference hero: the run's class with its starting kit (the kit decides shop bags), no talent points, no bones, no play-time loot;
- reference journal: Adventurer's Guide + Alchemy Guide pages found/read, no lore page found; tutorial off;
- the run's challenges; APD exotic chance as set by the generator.
It is never "what this run would have met". Floors the run reached come from the run's own telemetry (events 291-294).

## Proof of fidelity
`FloorReplayTest.replayMatchesTheGamesOwnGenerationFloorForFloor`: 6 seeds x all 6 classes x 26 floors, SHA-256 of each full saved
level equals the game's own generation (harness context REFERENCE). Also repeatable, prefix-stable, content checks.

## Isolation
- Refuses while a game scene (GameScene/InterlevelScene) is on screen or while another replay runs. Every recording hook is switched off
  while it runs (`ApdRecorder.suspendHooks/resumeHooks`), so an open run log (a run saved and left) can never receive replay events.
  `Dungeon.init`/`loadGame` call `FloorReplay.awaitIdle()` so a game start never overlaps a replay.
- Reference journal/tutorial are answered only to the replay thread (`ReplayContext`): `Document.isPageFound/isPageRead`,
  `SPDSettings.intro`. Nothing is written; `Catalog.setSeen` is a no-op on the replay thread; the kit is built while the hero counts as
  not alive so no badge/discovery is recorded (`neverRecordsDiscoveriesOrBadgesForThePlayer`).
- Puts back: Dungeon fields, the three item identity tables, limited drops, item generator, statistics, quests (reset first: restore
  only sets keys it finds), Imp reward list, notes. `leavesTheDungeonStateAsFound`.
- `ReplayStaticAuditTest` diffs every static in levels/items/actors/journal/... before/after: only three scratch statics may differ
  (SecretRoom.regionSecretsThisRun, ArmoryRoom.prizeCats, SpecialRoom.floorSpecials), each rebuilt before use.
- Bug found by the tests: quest restore left the replay's Ghost enchant/glyph behind (fixed by resetting before restore).

## Limits
Branch levels (mines 11-14, vaults 16-19) are not generated. Bones, daily seeds' calendar state, hero talents and play-time loot are not
modelled. An upstream change to level generation changes results: bump `ENGINE_VERSION` / the golden test guards it.

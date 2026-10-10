# Warrior early-healing experiment — development evidence

This simulator is test infrastructure, not a balance recommendation or automatic balancing system. It drives the actual Android GameScene, actor scheduler, combat, movement, item pickup and healing actions. It never generates a replacement probability model or counts generated floors as played runs.

## Scope and policy

Warrior, Standard difficulty (3), no challenges, existing starting equipment. The current reckless policy attacks adjacent visible enemies, pursues visible enemies, explores discovered traversable tiles, picks up ordinary heaps and eats when starving. It drinks a held Potion of Healing when current HP is at or below the audited selected adjacent enemy's raw melee damage maximum. It does not retreat, optimize surprise attacks, spend talents or equip found upgrades. Optional/unknown encounters, unhandled dialogs, blocked actions and timeouts are recorded as incomplete.

An episode ends at death, first recorded normal Fly Healing drop, or floor 5. These are early-game episodes, not complete dungeon victories. Consuming healing in a threatening situation is an observation; it does not prove that the same action without healing would have caused death. Results do not establish the owner's playstyle, a population rate or a reason to change Warrior's starting identification.

Only actual `ITEM_GENERATED` evidence with container `C_MOB_LOOT`, owner `actors.mobs.Swarm` and true class `items.potions.PotionOfHealing` qualifies as normal Fly Healing. Bonus loot and Shielding are not mislabeled as Healing. The log is flushed/read only when the normal Swarm drop counter changes. No damage-roll/RNG call is made to predict damage; gameplay still consumes its normal RNG.

The batch uses explicitly recorded seed ranges, preserves per-action state traces, terminal HP/depth, completion/failure reason, normal Fly Healing observation, wall-clock start/end and monotonic elapsed time. Two consecutive adapter failures stop a shard instead of repeating a broken setup indefinitely. Missing, duplicate, unexpected or incomplete seed records cannot become a successful 100-run report.

## Isolation

Execution requires explicit simulation opt-in and a disposable Android emulator. The adapter refuses physical hardware, existing saves, existing rankings or existing run logs. Tests affect only that disposable emulator's state. No owner saves, signing identity, application ID, Claude branch or main are changed. No simulated run log is imported into the owner's UIMSD history. Simulator APKs are test infrastructure and are not claimed to be lunch-test deliveries.

## Actual implementation and checks

- `61d3ba9b86d82b55ee40359ca25aff749718b718`: actual Android adapter and known-map navigation. Twelve local navigation/policy tests passed (0.068 seconds). Run `38079881160`: Android debug/test compilation and one connected check passed (1 test, zero failures/errors/skips, XML duration 28.41 seconds). The overall job failed afterward: result extraction ran after emulator shutdown, leaving an empty JSON file. No per-seed result or percentage is claimed from this incomplete evidence.
- `4bd6b536e2cf485eb252e33dc9f3a525d4195c0a`: corrected recorder class-name handling and added existing-data/emulator guards. Fifteen local cases passed (0.062 seconds). Complete CI core XML: 962 tests, zero failures/errors/skips, artifact `11679842395`, run `38080008651`. Android setup failed downloading the default API34 image (`Error on ZipFile unknown archive`); simulator execution did not begin.
- `5a78b74a2374dd493b561a3f89f0500307af6a73`: actual serialized-recording regression, improved failure/terminal-state diagnostics, supported google_apis image recovery. Sixteen targeted local tests passed (0.391 seconds). Its uncompleted workflow was superseded by the extraction-order correction.
- `3402d7cabf5f5c08ffbd2d65a6b9713351cbb911`: extracts traces before shutting the emulator down; preserves test failure status and rejects missing evidence. Shell syntax passed. Run `38080454423` is pending at this report checkpoint; no successful corrected episode or completed 100-seed batch is claimed yet.

## Efficiency work

Large-batch preparation compiles one application/test APK pair, preserves their checksums/source manifest, and reuses them across five disjoint 20-seed emulator shards. Each shard installs that pair without Gradle rebuilding. This batch workflow is being prepared; syntax validation alone is not execution evidence. The first usable compiled pair must be preserved: earlier checks did not retain these binaries, so those discarded outputs cannot be reused.

Current limitations: only audited normal Sewers enemy bounds, no full counterfactual replay, optional quests/unknown enemies may interrupt an episode, fixed basic strategy rather than a complete configurable gameplay bot, action limits, no physical-phone acceptance. No Warrior identification change is implemented.

## Timing and delivery distinction

Repository commits, workflow timestamps, XML durations and per-episode clocks are evidence. Exact accumulated active coding time, time typing versus thinking and historical idle periods cannot be reconstructed from commits; no invented overnight time accounting is supplied.

TEST25 remains the latest delivered APK: source `42aa5ff0a332eab3afe12f036c9beeeb1bfb5e71`. It contains the Community privacy correction and has 948 core plus six API34 checks and verified signing/checksum evidence. New normal enemy-drop identification source `63fe8a693794b3421cebeb9dc62550ba97ba185b` passed 955 core tests but is not in TEST25. Simulator checks are not a substitute for the required playable APK regression and signing/delivery checks.

## Concurrent scheduled execution observed

Scheduled development advanced the branch to d5957b5cb370e3f2bcfc97aefa4cad4510a98d26 while this active turn was preparing a correction for the same evidence-loss problem. Its test-only Base64 log-chunk emission and strict reconstruction script are preserved. This is confirmed unattended code/evidence-plumbing work, not proof of continuous overnight coding or a tested new Android result. An unpublished alternative commit eb8c9cbd0d33fd9d2445c16ba861c1b848344628 was not applied; no synthetic/local history was pushed. The latest workflow retains APK binaries for subsequent direct-instrumentation batch reuse, and requests a new verified single episode with the preserved scheduled fix.

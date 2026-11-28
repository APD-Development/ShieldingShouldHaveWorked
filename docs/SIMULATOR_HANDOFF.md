# APD Simulator handoff: read this first, update it at the end of every run

Last updated: 2026-11-27 (end of the first long session). Every scheduled run is a FRESH session: this file is the memory.

## Chris's rules (binding)
- Repo APD-Development/ShieldingShouldHaveWorked. Work ONLY on branch `feature/apd-simulator` (made from `release/play-closed-test`).
  Never push to `release/play-closed-test` or `main`. Never upload to Play. Merge only when Chris says so. No force-push.
- Never commit keystores, passwords or keys. No em dashes anywhere (text, code comments, commits).
- The simulator is a STANDALONE app ("APD Simulator", app ID `com.charltonchris29.adhdpixeldungeon.simulator`) on the phone and a
  PC program. Never published (no Play, no GitHub Releases). It goes to two people only: Chris and National Pie (Pie has the
  Developer build too, so the read-only link must work for him, including building a bot from his own history).
- APD itself (Developer, Alpha, Play builds) contains NO simulator code. Only exception, Developer build only: the read-only
  history link (`android/src/apdlink`, debug + `-PapdTestBuild`).
- The simulator never touches real runs, saves, settings, UIMSD, badges or the live Community server.
- NO AI of any kind in the simulator: no LLM, no machine learning, no neural nets. The bot is hand-written rules only.
  "Play like Chris" is plain counting and averaging. Expert intelligence should be basically perfect play (rules, full game knowledge).
- Full play on random seeds (not generation sweeps) because it is used for balancing. Seeds never repeat in the simulator's history.
- Simulators at once: as many as the phone can handle (one process each; background ones without graphics). Not built yet.
- Ask Chris before ANY mechanic, balance or scope change. Fix real game bugs right away, with a test, and list them.
- Scope: Chris said "Keep them all" and "Proceed with everything": every item in `docs/APD-SIMULATOR-COMPLETE-LIST.pdf`
  (text of the earlier list: `docs/APD-SIMULATOR-SPEC.md`) is approved. [YOUR CALL] items still need his answer first.
- Pull `release/play-closed-test` into `feature/apd-simulator` at least once a day (`git fetch origin release/play-closed-test && git merge --no-edit FETCH_HEAD`).
- Send Chris builds as files (phone APK, Developer APK with link, PC zip). He passes Pie's copies on.
- Chris wants small chats: do the work, update this file, give a short summary. Say exactly what was verified vs assumed.
- Chris: "Do not stop ever." Each run should make real progress, commit and push.

## Intelligence levels and win-rate targets (Chris, Nov 27 night; replaces every earlier version)
Five levels, hand-written rules only: Clueless 0%, Average 0.5%, Smart 3%, Pro 7.5% (new, between Smart and Expert),
Expert 15%. These are full-win rates (beat the whole dungeon) on default difficulty with no challenges. They are targets to
tune towards and to measure with the benchmark and headless runs, by level and class. (Earlier the same evening Chris gave
75/50/25/12.5/2; the list above is his later correction and is the one that counts. "Pro" is the working name of the new
level: Chris has not confirmed the name.) Current state: 0 wins at every level; `PRO` exists in `SimProfile.Intelligence`
and currently plays like Smart (`smart()` is true for Smart, Pro and Expert). Chris: "I want the bots to be able to beat the
game, not just beat Goo." Goo HAS been beaten (every run that reached floor 6+); no bot has beaten Tengu (floor 10) yet.

## Cross-launch replay: measured, and the owner's bar (Nov 27 late)
Chris: "Anything close to 75% is good enough." So NO game code edit for stable ordering (the `Random` switch is off the
table unless the rate drops well below that). Measured at commit 93e15f1e: the 30 fixed benchmark seeds played in two
separate launches (PC, Normal/Average), compared by end digest and turn: 29 of 30 identical (97%). The one split was a
Rogue run (floor 7 in one launch, died to Goo on floor 5 in the other). Caveat: no run went past floor 9; deeper runs make
more address-ordered picks, so re-measure once bots get deep. Same run also scores the bot rules that were unscored:
avg deepest 4.50 and 4.43 (was 4.27), 15 of 30 reach floor 5+, max 9, top deaths Goo 5-6, starvation 4, wraith 4.

## Bot progress, Nov 27 late session (30 fixed seeds, Normal/Average)
Average deepest floor by step: 4.50 (start of session) -> 4.40 worn key fix (bots that killed Goo could not unlock the
LOCKED_EXIT and starved; `unlock()` now uses the WornKey) plus ooze wash anywhere, Great Crab ignored, remains only from
level 5 -> 4.87 Goo standing rule (`gooSpot`: water under the hero, dry floor around; Goo deaths 14 -> 7) -> 5.03 SimNav
no longer paths over tombs/chests/remains (a step onto one is a tap that opens it; that was the floor 2 wraith deaths)
-> 5.60 gear rules: everyone but Cautious tries found weapons and armor on unidentified; upgrade scrolls shared between
armor and a tier 2+ weapon; Smart and better hold scrolls on floors 1-3 while in tier 1 armor. Max floor 10, Goo deaths 4,
20 of 30 reach floor 5. Run summary lines in simruns.txt now end with "gear at floor 5" and "gear at end" (`gearSummary`).
Before the gear rules 18 of 20 bots met Goo in cloth armor with the starting weapon; after, 9 of 20 have tier 2 armor and
10 of 20 a tier 2 weapon. Trace mode (`-Dapd.sim.trace=1`) prints an ASCII map (APDSIM_MAP) when the bot has nothing to do.
Still open on gear: Average spends scrolls on cloth; items above the hero's strength are never worn; no shop buying plan;
arcane stylus, enchant scrolls and the Ghost reward are not used on purpose. New top killers: DM-100 4, Goo 4.

## Work is split from here (owner, Nov 28): one session on the apps and tools, the next on the bots, and so on
The owner asked how long until complete and said the last stretch made no progress. Honest position given to him: apps
and tools about 4 to 6 more sessions; bots that win at the target rates unknown (weeks, not certain). He chose to
alternate. **Apps and tools session 1 (this one):**
- **Headless is about 25 times faster for benchmarks.** Profiling: a 29 s run spent 5 s in game frames; the rest was the
  fade, loading and death screens, timed by the real clock. In headless those run on a 40x clock
  (`SimGame.headlessScreenSpeed`), except in InterlevelScene's STATIC phase (it has a 10 s levelgen watchdog on the same
  clock). Each run prints `APDSIM_PERF` (game frames, screen refreshes, ms in game frames). A shorter bot idle time was
  tried and dropped: it changed every result and was not faster.
- **Several at once (PC)**: `-Dapd.sim.workers=N` (`SimWorkers`): N copies in `worker-N` folders, merged `simruns.txt` and
  `simlog.csv`, printed summary. 60 fixed seeds on 3 copies: 139 s (was about an hour with the old scripts). Use this for
  all bot measurement now; 600 runs is about 25 minutes, so small effects can finally be seen.
- **Replay between launches** is now about 3 in 4 seeds at current depths (was 29 of 30 when runs were shorter), the same
  known cause (address-ordered picks). The owner's bar was "close to 75%"; it will fall as bots go deeper. Changing the
  window size also changes results, so the window size stays fixed.
- `JudgeLeavesGameAloneTest`: judging 38 kinds of monster on 5 depths changes no loot table and draws no game randomness.
Still to build on the apps side: headless as an Android foreground service, several at once on the phone, the Visual
grid of runs, the labs and audits in the complete list, on-screen check of the Boss test button and the summary.

## Measured on 60 seeds (Nov 28, later): what helped, what did not
From here every change is measured on 60 fixed seeds (`SIMRUNS=20` per simulator); 30 seeds flattered (5.9 to 6.1 on
30, 5.6 on 60). **Average baseline 5.60 average deepest floor**: 39 of 60 reach floor 5, 30 get past Goo, 4 reach
floor 10, 1 gets past Tengu. Smart is level with Average.
- Holding early upgrade scrolls (now for every level but Clueless): no change (5.57). Only 16 of 40 bots have found
  armor better than cloth by floor 5, so gear on arrival is limited by drops, not by how scrolls are spent.
- Always throwing held missiles at a boss: no change on 70 Goo fights (49 then 46).
- **Corner trick and doorway ambush: worse.** 5.60, 5.57, 5.55 without; 5.45, 5.40, 5.32, 5.18 with. Now OFF unless
  `-Dapd.sim.corner=true` (`SimRuntime.cornerTrick`). They do get used and do fix the odd snake death; the cost is
  somewhere deeper and has not been traced.
- Zapping what dodges swings and keeping the last staff charge for it: removes the floor 1 snake deaths for Mages; too
  small to show on the average.
**The cursed wand's fake crash**: a rare cursed wand effect (`CursedWand.AbortRetryFail`) shows a fake error and then
really calls `Game.finish()`. That shut the whole simulator down silently, always on the same seeds (seed base 2000),
and cut several sets short (the "Smart runs did not finish" note above was this, not slowness). `SimGame.finish()` now
ends the run instead. This is the original game's joke, not a bug; nothing in APD was changed.
Deaths over 120 Average runs by floor: 1: 8 (snakes), 2: 16, 3-4: 17, 5: 19 (Goo 16), 6-7: 23 (skeletons, guards),
8-9: 30 (DM-100, guards, necromancer's skeletons), 10: 5, 11-12: 2. By class, average deepest: Warrior 6.7,
Huntress 6.6, Duelist 5.4, Mage 5.2, Cleric 4.8, Rogue 4.6.

## Bot progress, Nov 28 (after the fight judge section below was written)
Full runs, 30 fixed seeds, Normal aggression: **Average 6.07 average deepest floor, max 12, 21 of 30 reach floor 5**;
**Smart (judge and plan on) 5.90, max 12, 21 of 30** (one earlier Smart run reached floor 15 and died to DM-300).
Session start was 4.50 for Average. Measured with the build before the sentry and fire fixes; a rerun is the next step.
**Tengu: first kills.** 12 floor 10 snapshots; about 5 of 12 won (Huntress 3 of 5, Mage 1 of 2, Warrior 0-1 of 3).
Rules: close in (the game gives him accuracy 20 at range, 10 adjacent); ranged heroes shoot when he is more than two steps
away; all active traps on the prison boss floor in its first phase count as known (the game shows them for a moment);
`markBossDanger` marks bomb area (only when its count is 1 or less), flames and every cell beside a shocker.
**Bot bugs fixed** (each cost runs): the fight plan fled DOWN the stairs (hurt level 2 heroes on floor 3; flee is now
only back up); old corridor rule swapped places with looting forever (starved on floor 1); red sentries were treated as
monsters to walk up to; statues were attacked when walked past; resting on a sealed boss floor never ended (no health
comes back there); the first health reading counted as a drop and blocked resting; rest before a boss now runs to 90%
and is allowed while hungry (not starving); on fire with no water the bot stands on bare floor instead of walking
through grass and re-lighting.
**Known limits**: food is tight by design (about one food item and about 300 turns per floor); starving happens when a
floor runs long, usually a secret door hiding the way down. Top killers now: Goo, prison guards, gnoll brutes, DM-100.
No DM-300 snapshot pool yet (one bot has reached floor 15). Pro and Expert still play like Smart.

## Fight judge, fight plan, boss tests and the history check (Nov 27-28)
Owner said "Stop stopping": keep working without waiting between steps. Replies to the owner are in caveman style again
(his request); code, commits and docs stay normal English.

**Fight judge** (`FightJudge.java`): arithmetic on the game's own numbers, no learning. Hit chance by the game's rule
(`hitChance`), monster damage and armor sampled 200 times from a throwaway copy on a throwaway random stream (a live
`damageRoll` has side effects, for example Goo's pump), hero accuracy from level, weapon and ring, surprise swings always
land. Gives hit chances, damage each way, turns to kill, health the fight should cost. Used for: when to heal, which
enemy to hit first (Smart+), ranged openers in dangerous fights, how long Smart+ rest. `-Dapd.sim.nojudge=true` plays by
the older fixed rules so the judge can be measured.

**Fight plan** (`FightPlan.java`, Smart and better; from National Pie's design): step 0 read the arena (how open, nearest
corridor or doorway, water, traps, stairs), step 1 read each enemy (speed, damage, health, ranged, turns to melee), step 2
corridor reachable before fast enemies arrive, step 3 compare cost here, in the corridor and health left, step 4 pick an
item (thrown gas/fire, mirror image/lullaby/terror, teleport). No retreat is planned past an enemy faster than the hero
(`reachableInTime`). Average bots only get the judge's healing rule; the owner has not said whether Average should plan.

**Boss tests from checkpoints**: snapshots are saved on every new deepest floor (40 kept per class on boss floors, with
a readable `checkpoint.txt`). `SimProfile.bossTest` + `checkpointFloor` (home screen "Boss test" button; PC
`-Dapd.sim.checkpoint=5 -Dapd.sim.bosstest=true`, optional `-Dapd.sim.checkpointName=`) starts every run from a snapshot
saved on that exact floor, taken in turn (`Checkpoints.inTurn`) so two series face the same fights, and ends when the
floor is cleared or the hero dies. Marked TEST in the run line and left out of the summary's win rates. Only Goo can be
tested so far: 35 floor 5 snapshots, 3 for floor 10, none deeper. The Boss test button and summary change are not yet
checked on screen.

**Goo, 210 fights from the same 35 snapshots (Average)**: 57% judge off = 58% judge on (the judge does not matter against
one boss in an open room; Smart with the planner 56%) -> 66% after fixing rule order (the ooze rule ran before healing
and pump dodging and aimed at Goo's own cell) -> 69% pump dodge only when the moves left really end more than 2 cells
away (Goo pumps for two turns, the pump is wasted if the hero is out of reach when it swings) -> 68% fighting from water
(ooze deaths 13 to 3, deaths to hits 52 to 65). By class about: Mage 85%, Huntress 75%, Duelist 60%, Warrior 60%,
Rogue 57%, Cleric 40-60%. Ranged classes win; the rest is gear and level on arrival.

**Other bot bugs fixed on the way**: stairs are now the end of a path only (exploring walked onto the stairs up and left
the floor); no resting while health is dropping or with the Great Crab near (the bot ignores the crab and was resting
while it attacked); no stair escape on a sealed boss floor.

**History check**: the owner uploaded his history export (118 detailed runs, 953,301 events, 1 win; mostly Duelist; 11
rulesets, 30 runs on custom rules). It is NEVER committed; it lives outside the repo.
`JudgeHistoryCheckTest` (`-Papd.history.dir=<unpacked export>`) compares the judge with every recorded weapon swing
(83,253): monsters hit the hero 46.2% real against 45.3% judge; monster damage before armor 8.97 real against 8.33 from a
calm copy (so the judge scales by that ratio, `DAMAGE_SEEN_IN_PLAY`); monster armor 1.99 against 1.96; the hero hits
80.5% real against 76.2% from level alone (snakes 53% against 24%: surprise attacks, now counted for the first swing).
Open with the owner: which of his runs count as real attempts, and whether the history should also set rule numbers or
build a "Chris bot".

## How it is built
- `simulator/core`: bot, checks, reports, screens (package `...apd.simulator`). `simulator/android`: the app. `simulator/desktop`: PC program.
- `SimGame` (subclass of the game) drives everything; `SimRuntime` is the per-frame driver; `SimBot` decides; `SimNav` paths;
  `TurnStepper` runs the game's turns on the render thread; `CountingRandom` is the run's seeded generator (visual code gets a
  separate one); `StableTurnOrder`, `RunContext`, `FrameRunnables`, `SeedActor` support exact replay; `FloorTruth`/`FloorAudit`
  check floors against the true map; `SaveLoadCheck`, `Invariants`, `TextCheck`, `RecorderCheck`, `NetworkGuard` are detectors;
  `Checkpoints`, `Digest`, `SimProfile`, `SimHomeScene`, `SimHud`, `WndSim*` are features and UI.
- Build needs the Android SDK (`sdk.dir` in `local.properties`, not committed). Install with sdkmanager: `platforms;android-36`, `build-tools;36.0.0`.
  - Phone app: `./gradlew :simulator:android:assembleDebug -PapdBuild=S2` (signed with the same alpha key as the Developer build)
  - Developer build with the link: `./gradlew :android:assembleDebug -PapdTestBuild -PapdBuild=C490D-SIMLINK`
  - PC zip: `./gradlew :simulator:desktop:distZip`; tests: `./gradlew :simulator:core:test` (`-Papd.audit.seeds=400` for a big floor audit)
- Test the bot on a PC or in the cloud: `xvfb-run -a -s "-screen 0 540x960x24" ./gradlew :simulator:desktop:run -Papd.sim.autostart=1
  -Papd.sim.runs=6 -Papd.sim.speed=3 -Papd.sim.dir=<empty dir>`. Output lines `APDSIM_RUN`, `APDSIM_BUG`. Options:
  `-Papd.sim.replay=CLASS:SEEDCODE:BOTSEED`, `-Papd.sim.trace=1` (every action with monster fingerprint, frame and random-draw
  counts), `-Papd.sim.verify=N` (determinism replay every N runs), `-Papd.sim.checkpoint=N`, `-Papd.sim.aggression/intelligence=`.
- Android check: workflow `apd-simulator-check.yml` runs on every push that touches `simulator/**`: builds, runs the tests, installs
  on an emulator, plays 6 minutes, prints runs, reports and base64 screenshots into the job log.

## Done and verified
- Standalone app builds; S1 build (commit fd0cb616) delivered to Chris Nov 27 and passed the emulator check (17 runs, no crashes).
  NOT verified on a real phone: notifications, Share, the history link between the two apps, screen-off.
- APD's game code equals `release/play-closed-test` except: history link (dev build only), Play bundle check in
  `apd-play-release.yml` (fails if simulator/link code is in the bundle), and the staff fix below.
- Exact replay: same class + seed + bot seed replays identically, across launches and inside one session. Verified on 8
  run-and-replay pairs (Warrior, Rogue, Duelist, floors 1 to 7) plus three launches of one seed. Automatic determinism check
  (every Nth run, default 10) files "Replay did not match".
- Floor audit: 400 dungeons / 10,400 floors, 0 findings. Origin test (starving at 1 HP with Potion of Shielding survives) passes for all classes.
- Detectors working: crashes/exceptions, stuck (checked against the true map; bot-dumb cases do not notify), broken text incl.
  clean-mode swear words, impossible states, recorder cross-check, slow turns/frames, network guard, save/load every 300 turns.
- Features working: profiles (class, aggression, intelligence, difficulty, seeds, randomized APD Settings, stop conditions),
  speed 1x/2x/4x/max, pause, skip, Drive (take over), Chauffeur, checkpoints, Try my death, reports with Share/Copy/Replay/Delete,
  severity, summary, daily digest, launch readiness.

## Game bugs found
1. FIXED on this branch (not committed test run after the fix: see "In flight"): Mage's staff taking a new wand (imbue or Scroll of
   Transmutation) left the old wand's charging effect on the hero until the next save and load. No gameplay effect. Fix in
   `MagesStaff.imbueWand`; test `StaffChargerTest` (fails without the fix, passes with it). Found by save/load check, MAGE seed
   GAI-RVB-UDM, bot seed -5367025815424091256.
2. OPEN QUESTION for Chris (asked, not answered): the engine orders simultaneous actors by memory address (`Actor.all` is a
   HashSet), so the same seed and choices can play differently between launches. Options given: (1) keep insertion order
   [recommended], (2) order by actor id, (3) simulator only [current state], (4) leave it. Do NOT change the real game until he answers.

## In flight when the session ended
- Uncommitted work was committed with this file: rest budget per floor (bot rests only below a health line, never hungry, limited
  turns), class rotation ignores determinism replays, save/load snapshot names the item a charger belongs to, the staff fix + test.
- DONE: replay of MAGE:GAI-RVB-UDM after the staff fix (clean, same digest). Still to check: the
  emulator check for the exact-replay commits (check the latest `apd-simulator-check.yml` run; reflection on libGDX's `runnables`
  field and `Actor` fields must work on Android).

## Known broken or weak
- The bot is weak: dies on floors 1 to 7. Top deaths: starvation, wraiths, swarms of flies, piranhas, gas/fire, statues, Goo.
  Expert currently plays like Smart. No quests solved, no alchemy, shops barely used, no boss tactics, subclass/armor ability
  choices are button presses.
- The simulator clears "bones" (remains of dead heroes) every run for exact replay, so remains are never tested.
- Guidebook pages are skipped by the bot (their placement is unseeded on purpose).
- Run summaries/UIMSD tag: aggression/intelligence recorded via fact kind `sim.run`; not yet checked inside the UIMSD screens.
- `docs/APD-SIMULATOR-GUIDE.md` needs the new features added (Drive, determinism check, checkpoints are partly there).

## Work claims
- The live session is working again (Chris, Nov 27 evening: "work here") on BOT QUALITY: `SimBot.java`, `SimNav.java` and the
  fixed-seed benchmark. Scheduled runs: do NOT edit those two files until this claim is removed. Take item 1 (emulator check
  result, build S2 and send it), then item 3 (screen-off foreground service + no-graphics mode), then item 4.
  Always `git pull --rebase origin feature/apd-simulator` before pushing.

## Next (in this order unless Chris says otherwise)
1. Confirm the two "not yet re-run" items above; build S2 (phone APK, Developer APK with link, PC zip) and send to Chris.
2. Bot quality, hand-written rules: hunger management, fight selection by real damage numbers, wraith/flies/statue handling,
   doors as choke points, ranged classes kiting, class tools, quests (ghost, wandmaker, blacksmith, imp), shops, alchemy,
   subclass/armor ability choices, bosses. Then the Expert level (exact formulas, boss patterns, safe item testing).
3. Screen-off running: foreground service + no-graphics mode; then several simulators at once (one process each).
4. Remaining [TOP] items: Send-run button in the dev build, seed fairness proofs, hands mode (real taps), "numbers match what
   happens" audits, update test, UIMSD at scale, replay Chris's recorded runs, Chris bot (counting only), network audit report,
   cloud twin [YOUR CALL], scenario files, stuck classification refinements.
5. Everything else in the complete list: PC grid (4 runs), labs, what-if lab, Community rehearsals (test server only, [YOUR CALL]),
   coverage, bug handling tools, reports and outputs, fun extras.

## Run log (newest first; one line per run: what was done, what was verified, commit)
- 2026-11-27 live session, night: Chris's new requirement: TWO SEPARATE APPS, installable side by side, same code, same
  version, every build makes both: "APD Sim Headless" (`...simulator.headless`: no-graphics mode, benchmark button, export)
  and "APD Sim Visual" (`...simulator.visual`: watchable runs, speed, personalities, later a grid of runs). Tapping a seed in
  Headless opens it in Visual. Same split on PC. BUILT: Android product flavors `headless`/`visual` in `simulator/android`
  (the app knows which it is from its package name, `SimRuntime.app`), hand-over by an explicit intent (`SimRuntime.handOver`),
  Benchmark button (10 fixed seeds, seedbase 777, shows average deepest floor and runs per hour), PC start scripts
  `bin/APD-Sim-Headless` and `bin/APD-Sim-Visual` in one zip (`APD-Sim-<version>.zip`), Headless starts Visual with the run.
  VERIFIED: both APKs build with version 4.0.9-apd-SIM-bS2 and the right names and IDs; the PC Headless program plays the
  benchmark seeds with the same digests as before. NOT VERIFIED: the hand-over on a phone or PC (needs two installed apps and
  a screen), the Benchmark button. NOT BUILT YET (Chris asked for them): Headless running in the background as a foreground
  service (needs a true no-graphics game loop: the game stops stepping when Android stops drawing), the threads setting
  (several runs at once), the Visual app's grid of runs. The old single app ID `...simulator` (build S1) is retired: uninstall it.
- 2026-11-27 live session, evening: (1) Chris's new requirement: TWO MODES. Mode 1 headless (no drawing, fastest, every
  death/bug/stat logged to an exportable file), Mode 2 visual (watchable, personalities). Same engine and bot code, so a seed
  gives the same result in both; a button replays any headless seed in visual mode. BUILT: Mode toggle on the home screen,
  `SimGame.draw` skips 19 of 20 frames in headless, `simlog.csv` (one line per run, `RunLog`), "Export log" (zip of log, run
  list, reports), "Run log" window where tapping a run replays it in visual mode, desktop `-Dapd.sim.headless=1`.
  VERIFIED: 2 of 3 seeds identical in both modes; the third differed for a reason that is NOT the mode (next point). Headless
  is only slightly faster under the cloud's software graphics (54 s vs 59 s for 3 runs); on a phone, and for screen-off, true
  no-graphics still needs work. (2) NEW GAME FINDING, waiting for Chris: `Random.chances(HashMap)` and
  `Random.element(HashSet)` pick from address-ordered collections (example: Wand of Corruption's debuff table, keyed by
  class), so the same random number picks a different entry after an app restart. Same family as the turn-order finding.
  Inside one app session replays are exact; across restarts they can split when such a pick happens. A switch in
  `Random` (inert in the real game, turned on by the simulator) was drafted but NOT applied: it edits game code, so it needs
  Chris's yes. (3) Bot benchmark on 30 fixed seeds: baseline 3.30 average deepest floor; piranha pool rule 4.00; Goo, boss
  rest and hunger rules 4.27 (14 of 30 reach floor 5+, max 9). Potion rules (no low-health gamble, test in water) not scored.
  PC program version fix (it refused its own saves). The hourly scheduled run is DISABLED (Chris: "Only work here").
- 2026-11-27 live session, last entry: bot stands and fights when no stairs are near (running across open floor got it killed),
  relaxes its careful rules in 3 steps when it has nothing left to do (it used to wait forever), food in the pack extends the
  rest budget. Compiles; only one noisy 12-run check, NOT benchmarked. New tool: fixed-seed benchmark, desktop option
  `-Dapd.sim.seedbase=N` (seed = N*1000003 + run*7919, bot seed = N*31 + run), so bot changes are compared on the same dungeons.
  Suggested use: 3 simulators in parallel (seedbase 1000, 2000, 3000), 10 runs each, via the installed PC program
  (`./gradlew :simulator:desktop:installDist`, run `bin/APD-Simulator` from `core/src/main/assets` with `JAVA_OPTS=-Dapd.sim...`),
  then average the "deepest N" values in each `simruns.txt`. The baseline was started but not finished: run it first.
- 2026-11-27 first session, late: replayed MAGE GAI-RVB-UDM after the staff fix: same digest b3362bc0f6abc2ed, no save/load
  report (VERIFIED). Bot: tombs opened only by Aggressive+ bots at 80% health, skeletons/remains not by Cautious or hurt bots.
  12-run check: 3 runs reached floor 6 (was floor 1 to 5); top killers now piranha (4), gnoll scout (3). Emulator check for
  commit 12d72680 was still running: check it first.
- 2026-11-27 first session: everything above. Last commits: 5a76504f (determinism check), then this handoff commit.

## 600 fresh seeds baseline and the gravity freeze (Nov 28)

First measurement on seeds the bots never played (seedbase 5000, 3 at once):

- Average, 520 runs (see freeze below): average deepest floor 5.34, reach floor 5 59%, past Goo 45%, reach floor 10 4.4%, past Tengu 1.9%, dead by floor 2 14%, deepest 13, wins 0.
- Smart, 600 runs: average deepest floor 5.67, reach floor 5 64%, past Goo 47%, reach floor 10 6.3%, past Tengu 4.2%, dead by floor 2 12%, deepest 14, wins 0.

These match the usual 60 seeds, so the earlier numbers were not fitted to them.

Simulator bug found and fixed: the cursed wand's gravity effect (GravityChaosTracker) waits inside its own turn for
every sprite to stop moving. The simulator runs turns on the render thread, the only thread that can finish a sprite's
move, so the whole simulator froze forever (one worker stopped at run 120 of 200). TurnStepper now gives that effect its
turn only when nothing is moving. Proven by replaying WARRIOR:5000973199:155121: old build freezes, fixed build finishes.
The game itself is fine and was not changed. The "stopped responding" check cannot catch a freeze of the render thread
itself, so the several-at-once launcher now has a freeze guard: a copy that writes nothing for 3 minutes
(apd.sim.stallsec) is stopped, its seed goes to simfrozen.txt, and it restarts on the next run. Tested by suspending a
copy on purpose. The class rotation now follows the saved run counter so a restarted copy keeps its class order.

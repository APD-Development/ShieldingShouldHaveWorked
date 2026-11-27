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
- 2026-11-27 first session, late: replayed MAGE GAI-RVB-UDM after the staff fix: same digest b3362bc0f6abc2ed, no save/load
  report (VERIFIED). Bot: tombs opened only by Aggressive+ bots at 80% health, skeletons/remains not by Cautious or hurt bots.
  12-run check: 3 runs reached floor 6 (was floor 1 to 5); top killers now piranha (4), gnoll scout (3). Emulator check for
  commit 12d72680 was still running: check it first.
- 2026-11-27 first session: everything above. Last commits: 5a76504f (determinism check), then this handoff commit.

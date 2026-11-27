# APD Simulator: Everything It Could Do
Every idea for the simulator, grouped.

DECISION (Chris, Nov 27 2026): "Keep them all". Every item on this list is approved scope, built in batches after the first watchable version.
Other decisions from the same conversation:
- Seeds: FULL PLAY (not generation sweeps), because the simulator is used for balancing. Random seeds are uniform and never repeat a seed already in the simulator's history.
- Simulators at once: "literally as many as my phone can handle" (one process per simulator; background ones run without graphics).
- Delivery: the simulator ships inside the normal Developer build (Simulator button on the title screen), not as a separate app or package.
- Aggression and intelligence are recorded with every run in the simulator's UIMSD (fact keys sim.aggression and sim.intelligence).

## 1. Where it lives and safety
- [DECIDED] Developer build only, behind a Simulator button. Never in Play builds. Nobody else sees it.
- [DECIDED] Its own saves and its own separate UIMSD. Never touches your real runs, saves, settings or UIMSD.
- Never touches the live Community server: no sign-ins, posts, votes, uploads or reports.
- Never unlocks your real badges, Play Games achievements or rankings.
- Never counts toward your seed-reuse penalty, UIMSD 10-run gate or tour flag.
- Never changes game balance or mechanics; it only plays and observes.
- Runs in a sandbox folder; a crash in the simulator can never corrupt your real save.
- Kill switch: one button stops every simulator instantly.
- Simulator runs are stamped "SIM" everywhere so they can never be mistaken for real runs.

## 2. What it plays
- [DECIDED] All classes, full runs, until it dies or wins.
- Class choice per simulator: one class, rotate all, or random.
- Subclass choice: fixed, rotate, or chosen by its intelligence level.
- Armor ability choice: fixed, rotate, or chosen.
- Talent paths: fixed builds, random, or "best known" builds you define.
- Difficulty, challenges and custom rule sliders per simulator.
- Seeds: random, typed in, a list, a range, or the seed of one of your real runs (read-only).
- Daily seed: everyone (every simulator) plays today's seed.
- Randomized APD Settings combos per run (auto-pickup, walk-over pickup, trap continue, single-tap search, auto-eat, etc.), recorded with each run.
- Humor setting on/off (Clean / Adult text), since text differs.
- Starting conditions for testing: start on any floor with generated gear (marked as a test run, never mixed with normal runs).
- Ascension / post-Amulet play if the bot gets there.
- Future: mod heroes and scripted mods once mods exist.

## 3. Behavior settings
- [DECIDED] Aggression: Cautious / Normal / Aggressive / Reckless.
- [DECIDED] Intelligence: Clueless / Average / Smart / Expert. Hand-written rules only, no AI.
- Exploration: full clear, stairs dive, or "find the key items then dive".
- Item curiosity: never use unknown items / sometimes / always test immediately.
- Greed: how far it goes out of its way for loot, chests, vaults.
- Shop behavior: never buys / buys food and healing / buys best upgrade.
- Upgrade strategy: one weapon, one armor, wands, rings, spread.
- Alchemy: never / basic recipes / everything it knows.
- Ally use: ignores allies / uses them (Ghost, Wandmaker allies, summons).
- Quest handling: skip all / do easy ones / do all.
- Search habit: never searches / searches dead ends / searches everything.
- Retreat rules: never runs / runs to stairs / runs and comes back.
- Throwing: never throws / throws darts and stones / throws everything sensible.
- Personality presets combining all of the above (for example "Speedrunner", "Hoarder", "Coward", "Berserker", "Completionist", "New Player").
- Custom preset: save your own mix of settings with a name.
- Random personality per run, recorded with the run.

## 4. Watching
- [DECIDED] Watch on your phone at 1x / 2x / 4x / max speed, pause, skip to next run.
- Step one turn at a time while paused.
- Headless max speed: no drawing at all, fastest possible.
- Follow-cam on the hero, or free camera.
- Overlay showing what the bot is "thinking": current goal, target, why it chose this action.
- Overlay showing aggression, intelligence, seed, class, floor, turn, run number.
- HP, hunger and danger graph on screen.
- Highlight on a turn where a bug was detected.
- Picture-in-picture or a small floating window while you use other apps.
- Several simulators at once: one watchable, the rest in the background (ask Chris before building).
- Switch which simulator you are watching.
- Sound on/off for watching.

## 5. Running
- Stop conditions: forever, N runs, N minutes, stop on first bug, stop on first win, stop at a floor.
- Background running with the screen off as a foreground service.
- Pause when battery under 20% or the phone is hot; optional only while charging.
- Schedule: run overnight only, or between set hours.
- Resume after the phone restarts or the app is killed.
- Queue: line up batches with different settings to run one after another.
- Throttle: cap CPU use so the phone stays usable.

## 6. Bug hunting
- [DECIDED] Reports crashes, getting stuck, broken text, impossible states and slowness.
- [DECIDED] Each report: build number, seed, class, settings, aggression, intelligence, floor, turn, last 50 turns.
- Crash and exception capture with full stack trace.
- Freeze detection: game stops responding for N seconds.
- Stuck detection: no progress for N turns, walking in circles, oscillating between two cells.
- Unreachable exits, sealed rooms (like the floor 16 pit), keys it cannot use, doors that never open.
- Items that cannot be picked up, used or dropped when they should be.
- Broken text in everything it reads: code names ("actors.mobs.X", "com."), "NO TEXT FOUND", wrong plurals, empty strings, text running off windows, untranslated keys.
- Impossible states: HP over max, negative counts, stacked buffs that should not stack, items in two places, enemies inside walls, gold going negative.
- Recorder disagreeing with the game (UIMSD saying something different from what happened).
- Death screen ("What happened") checked against what actually killed it.
- Determinism check: same seed and same decisions must give the same result.
- Save/load stress: save and reload mid-run every N turns, compare hero, inventory and floor.
- Rotate the screen and back mid-run and compare.
- App pause/resume stress (simulate switching apps).
- UI poking: open every window it can reach, tap every button, report crashes.
- Text sweep: read every item, enemy, buff, talent and trap description it meets.
- Performance: slow turns, slow frames, long floor generation, report the run and turn.
- Memory: track memory over hours and report leaks with the run where it started.
- Battery and heat per hour of simulating (for your mid-range phone question).
- APD feature checks: auto-pickup actually picked up, stair walk stopped only when it should, trap continue behaved, auto-eat fired, walk-over pickup took every item.
- Rule checks for your own rules: Gathering never pulls from locked rooms, floor 1 never has a hidden door, defense means armor, and similar.
- Badge checks: badges unlock when their condition is met (in the simulator's own badge list).
- Score checks: final score matches the scoring rules and seed-reuse formula.
- Report "near misses": the game almost crashed (caught exception, warning logged).

## 7. Bug reports
- [DECIDED] Phone notification when it finds a bug. Reports stay on the phone.
- No duplicate notifications: same bug notifies once, then counts.
- Severity levels: crash, save loss, stuck, wrong result, text, slow.
- Bugs screen: list, filter by type, class, build, severity.
- Bug replay: tap a report to replay that run and watch the last 50 turns.
- Share button: zips the report to send to the chat that fixes bugs.
- Mark a bug as fixed in build N; if it shows up again in a later build, flag it as a regression.
- Screenshot of the screen at the moment of the bug.
- Daily digest notification: runs, new bugs, deepest floor.
- Quiet hours for notifications.

## 8. Testing builds
- Build vs build: same seeds on two builds, report what changed.
- Regression suite: a fixed list of seeds that must keep passing on every new build.
- "Golden runs": saved runs whose result must never change unless the game was meant to change.
- Pull release/play-closed-test into the simulator branch at least once a day.
- Stamp every run with the build number.

## 9. Stats and balance data (data only, never changes)
- Summary screen: runs, deepest floor, win rate, top death causes by class, aggression and intelligence.
- Which enemies kill most, at which floors, per class.
- Boss win rates and how long each boss fight takes.
- Which items were most useful (picked, used, upgraded, in winning runs).
- Which talents and subclasses won most.
- Hunger and food: how often it starved, how much food was left at death.
- Gold: earned, spent, left over.
- Turns per floor, time per floor.
- Trap deaths by trap type.
- Distribution of deepest floor reached.
- Compare personalities against each other.
- Compare the bot with your real runs (side by side, never mixed).
- Export the simulator's UIMSD the same way as yours.

## 10. Fun extras
- "Try my death": play the seed of one of your real deaths and see whether the bot survives.
- Bot vs bot: two personalities on the same seed, race to the deepest floor.
- Hall of fame: the simulator's best runs.
- Death reel: the last few turns of its funniest or dumbest deaths.
- Name each personality and keep a record per name.
- Cleric lore check: the Cleric jokes and pop-ups fire and never break (simulator copy only).

## 11. How the other chat should work
- Work only on feature/apd-simulator; merge only when Chris says so; never upload to Play.
- Reuse the old Oct 10 emulator Warrior bot where useful; this one runs on the real phone.
- Ask Chris before any mechanic, balance or scope change. Fix real bugs it finds right away.
- Send Chris a dev APK when the first watchable version works, then keep going in big batches.
- Never commit keystores, passwords or keys. No em dashes.

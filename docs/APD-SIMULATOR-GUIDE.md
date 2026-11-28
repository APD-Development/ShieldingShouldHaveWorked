# APD Simulator: how to use it

APD Simulator is two apps on the phone that install side by side, and two programs on the PC, all built from the same game and bot code with the same version number:
- **APD Sim Headless** (app ID ending `.simulator.headless`): plays as fast as it can with almost no drawing, logs every run to a file you can export. Tapping a run opens that seed in the Visual app.
- **APD Sim Visual** (app ID ending `.simulator.visual`): full graphics, speed controls, watchable runs.

They contain the current game and a bot that plays it to find bugs and collect balance data. They are never published (not on Play, not on GitHub Releases). Only Chris and National Pie get them.

## Safety
- Android keeps the simulator's saves, settings, badges and UIMSD apart from APD's. It cannot write to your real game.
- Your real history is read through the Developer build's read-only link (only apps signed with the same key can open it). Nothing can be written through it.
- Every network connection the game tries inside the simulator is blocked and logged; a connection to anything other than APD's own server is reported as a bug. It never signs in, posts, votes or uploads.
- APD itself (Developer, Alpha and Play builds) contains no simulator code. The Play build fails if any simulator or history-link code reaches the bundle.

## Phone
1. Install the Developer build (for the history link) and both simulator APKs.
2. Open APD Sim Visual (or Headless). Allow notifications (bug reports and the daily digest arrive as notifications).
3. Set the simulator up on the home screen: class (or rotate all), aggression, intelligence, difficulty, seeds (random, typed, or one of your real runs), randomized APD Settings, stop condition, Chauffeur, start from checkpoints, sound.
4. Press Start. The bar over the game: Pause, speed (1x, 2x, 4x, max), Drive (take the controls; press Bot to hand them back), Skip (next run), Home.
5. Bug reports: Home > Bug reports. Each report has Share (zips it for the chat that fixes bugs), Copy, Replay (same seed, class, bot seed and settings; plays fast until about 50 turns before the bug, then at 1x) and Delete.
6. Your runs (Try my death): your real runs, with how each ended. Tap one to use its seed for this simulator, or to play it once.
7. Clear simulator history deletes only the simulator's UIMSD, reports and run list.

## PC
Unzip APD-Sim, then run `bin/APD-Sim-Visual` or `bin/APD-Sim-Headless` (`.bat` on Windows). It needs Java 17 or newer. Its files go next to the program; it reads the APD desktop game's history read-only.

## The bot
Hand-written rules only: no AI, no machine learning, nothing trained. Two settings shape it.
- **Aggression** (Cautious, Normal, Aggressive, Reckless): when it heals, rests, backs off, takes optional fights, tries unknown items.
- **Intelligence** (Clueless, Average, Smart, Pro, Expert): how much it works out. The home screen shows each level's target share of full wins on default difficulty: 0%, 0.5%, 3%, 7.5%, 15%. These are targets to tune towards. Pro and Expert play like Smart for now, and no bot has won a run yet.

From Average up the bot uses the **fight judge**: the game's own hit rule and damage numbers, worked out for each monster in view (how often each side hits, how hard, how many turns, how much health the fight should cost). Smart and better also make a **fight plan** before every fight: read the room, read each enemy's speed and reach, look for a corridor or doorway they can get to first, compare the cost with the health left, then fight, reposition, use an item or flee up the stairs. They never plan a retreat past something faster than the hero.

Bosses have their own rules so far for Goo (fight from water, step out of the pumped hit only when the moves left really get out of reach) and Tengu (close in because his aim is half as good up close, remember the traps his cell shows, stay out of bomb, flames and shock).

## Boss tests
Every time a run reaches a new floor the simulator saves a snapshot (floor, hero, gear, items, health). **Boss test** on the home screen cycles Goo, Tengu, DM-300, Dwarf King, Yog-Dzewa: every run then starts from a snapshot saved on arriving at that boss floor, taken in turn so each snapshot is fought equally often, and ends when the floor is cleared or the hero dies. These runs are marked TEST in the run list and left out of the win rates in the summary. A boss can only be tested once some bot has reached its floor.

## Checking the fight judge against real play
`./gradlew :simulator:core:test --tests '*JudgeHistoryCheckTest*' -Papd.history.dir=<unpacked history export>` compares the judge's hit chances, monster damage and monster armor with every recorded weapon swing in a history export and prints a table. The export is only read; never put it in the repository.

## What it checks
Crashes and exceptions; stuck runs, checked against the real map (hidden doors and keys included) so "the bot was dumb" is not reported as a game bug; broken text (code names, missing text, placeholders, wrong plurals, swear words with Adult humor off) in game messages, windows, story screens, item and monster names and descriptions and the death screen; impossible states (HP over max, negative gold or energy, two characters on one cell, standing in walls, bad item counts); the recorder against the game after each finished run; slow turns and frames; unexpected network connections; save and reload every 300 turns, comparing before and after.

## Tests that run on every build
- Floor audit: generates whole dungeons without playing and checks every floor against the real map. 400 dungeons (10,400 floors): no findings.
- Origin test: a starving hero at 1 HP with a Potion of Shielding active survives, for every class.
- Text checks.

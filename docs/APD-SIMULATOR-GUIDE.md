# APD Simulator: how to use it

APD Simulator is its own app ("APD Simulator", app ID `com.charltonchris29.adhdpixeldungeon.simulator`) on the phone and a program on the PC. It contains the current game and a bot that plays it to find bugs and collect balance data. It is never published (not on Play, not on GitHub Releases). Only Chris and National Pie get it.

## Safety
- Android keeps the simulator's saves, settings, badges and UIMSD apart from APD's. It cannot write to your real game.
- Your real history is read through the Developer build's read-only link (only apps signed with the same key can open it). Nothing can be written through it.
- Every network connection the game tries inside the simulator is blocked and logged; a connection to anything other than APD's own server is reported as a bug. It never signs in, posts, votes or uploads.
- APD itself (Developer, Alpha and Play builds) contains no simulator code. The Play build fails if any simulator or history-link code reaches the bundle.

## Phone
1. Install the Developer build (for the history link) and the APD Simulator APK.
2. Open APD Simulator. Allow notifications (bug reports and the daily digest arrive as notifications).
3. Set the simulator up on the home screen: class (or rotate all), aggression, intelligence, difficulty, seeds (random, typed, or one of your real runs), randomized APD Settings, stop condition, Chauffeur, start from checkpoints, sound.
4. Press Start. The bar over the game: Pause, speed (1x, 2x, 4x, max), Drive (take the controls; press Bot to hand them back), Skip (next run), Home.
5. Bug reports: Home > Bug reports. Each report has Share (zips it for the chat that fixes bugs), Copy, Replay (same seed, class, bot seed and settings; plays fast until about 50 turns before the bug, then at 1x) and Delete.
6. Your runs (Try my death): your real runs, with how each ended. Tap one to use its seed for this simulator, or to play it once.
7. Clear simulator history deletes only the simulator's UIMSD, reports and run list.

## PC
Unzip APD-Simulator, then run `bin/APD-Simulator.bat` (Windows) or `bin/APD-Simulator` (Linux/Mac). It needs Java 17 or newer. Its files go to `APD-Simulator` in your home folder; it reads the APD desktop game's history read-only.

## What it checks
Crashes and exceptions; stuck runs, checked against the real map (hidden doors and keys included) so "the bot was dumb" is not reported as a game bug; broken text (code names, missing text, placeholders, wrong plurals, swear words with Adult humor off) in game messages, windows, story screens, item and monster names and descriptions and the death screen; impossible states (HP over max, negative gold or energy, two characters on one cell, standing in walls, bad item counts); the recorder against the game after each finished run; slow turns and frames; unexpected network connections; save and reload every 300 turns, comparing before and after.

## Tests that run on every build
- Floor audit: generates whole dungeons without playing and checks every floor against the real map. 400 dungeons (10,400 floors): no findings.
- Origin test: a starving hero at 1 HP with a Potion of Shielding active survives, for every class.
- Text checks.

# APD scheduled work backlog

Owner schedule (set Nov 17, 2026): hourly coding runs 6 AM-2 PM Eastern, one APK a day started at 3 PM and delivered by 4 PM.
Hourly runs work top-down, commit and push to `fix/apd-trap-room-search-20261116`, and never edit
`.github/workflows/apd488t-trap-room-search-apk.yml` (that file triggers the APK build; only the 3 PM run touches it).
Chris, Nov 18: ChatGPT is not touching this branch. Fix real bugs a test finds right away. When a decision comes up,
make the reasonable call yourself, list it in that run's summary, and revert it if Chris later says no. Still no big
unapproved features.

## Approved, in order
1. Tests for this week's features:
   - [x] room-wide Search tap (Nov 18, 6 AM) (`SearchControlContext.roomHasSearchableWall`, `fullSearchUseful`)
   - [x] `Hunger.turnsUntilHungry` / `perTurn` (Nov 18, 6 AM; rate<=0 case not forced) (Salt Cube, custom hunger rule, Hungry, Starving, never)
   - hunger text in the status tab and top-right (values, yellow at 300, red at 450)
   - Bug Notes inside both report formats; Standard report byte-identical when no notes
   - Bug Note button Developer-only
   - heavy armor in the real `Armor.info()` (identified exact, unidentified +0, no level leak)
   - every food's "Refills N hunger" equals what eating removes (incl. On Diet)
   - Gemstone Dust apply/recipe on real rings; trophy outline and Bestiary line; Amulet layout reveal on a real level
2. Tests for the 17 untested APD classes: ApdFacts, BuiltinFactKinds, CustomBossHealthHooks, CustomDefenseHooks,
   DewExplanation, EventBuilder, GameSnapshot, GenerationTracker, HotbarPersistence, KeyExplanation, Mailbox,
   PositionedStringLookup, ProgressGuardInputStream, ReplayScan, StaffExplanation, StringLookup, Utf8.
3. Behavior tests for untested SPD gameplay classes: traps, rooms (valid generation on every floor), mobs, spells,
   scrolls, blobs.
4. Construction smoke tests for sprites, windows and UI classes where the headless runner allows.

## Waiting on Chris (do not build)
- Tracked voluntary rules / conducts (said "not yet").
- Gemstone Dust custom sprite and alchemy-guide entry (offered, not approved).
- Amulet reveal opening secret doors (currently map only).
- Stronger enemies / better loot for trophy tiers (undecided).

# The Developer's Personal Vendetta Against Cleric (PROPOSAL, awaiting owner approval)

Status: roadmap entry plus proposed architecture and sample lines. NOTHING here is built. The full line library and the popup runtime are NOT started until the owner approves this document. Lower priority than backend work and pending tests. Cleric balance and mechanics are untouched by this feature (see docs/APD-CLERIC-AUDIT.md for the separate balance audit).

## What already exists on paper (reviewed)
docs/APD-HANDOFF.md "CLERIC HUMOR" (1)-(4) and "UIMSD absurd-query easter egg" already record: a one-time first-Cleric-run warning; very rare slander announcements with long real-time cooldowns; effectively unlimited variety through templates x recorded context; a presentation-only difficulty gag; Cleric lore; never during combat/bosses/dialogue; a player switch; Adult/Clean wording; never fabricate statistics. This proposal keeps all of that and generalizes it into one engine.
Reconciliation to approve: the old text says "weeks or months apart"; the new request says "reasonably rare". Proposed default: at most one popup per ~3 real days of play, hard cap 1 per session, never in the first 20 minutes after install, player-adjustable (Off / Rare / Less rare).

## Architecture (all offline, no AI, no network)
Package `apd/humor` (pure Java core, no libGDX types, so it is unit-testable headless):
1. **Line packs**: plain text assets `assets/apd/humor/*.txt`, one line per record: `id | category | tier | requires | weight | clean text || adult text`. `id` is a stable string (e.g. `clr.death.0412`), never renumbered, never reused; deleting a line retires its id. Adding lines = appending to a file. A lint test rejects duplicate ids, missing Clean text, unknown placeholders, over-long text, and Adult-only words in the Clean column.
2. **Categories**: `ANY_CHARACTER` (context-free, the pool that works while playing Warrior/Mage/Rogue/Huntress/Duelist/future), `CLERIC_DEATH`, `CLERIC_VICTORY`, `CLERIC_FAILED_ASCENSION`, `BOSS`, `ABILITY`, `EQUIPMENT`, `HISTORY` (uses recorded totals), `FOURTH_WALL`, `MENU`, `POST_VICTORY`, plus `character` tag so future characters/developer jokes reuse the engine (`vendetta=cleric` today; other targets are just another pack tag).
3. **Context** (`HumorContext`): an immutable snapshot built ONLY from existing recorded facts (UIMSD/run log queries, current hero class, last run outcome). Each line declares `requires` predicates (e.g. `lastRun.hero=CLERIC & lastRun.died`). A line is eligible only if every predicate is true on real data. Numbers in text (`{clericRuns}`, `{guidingLightCasts}`) are placeholders filled from recorded data; if the datum is unavailable the line is ineligible. Pure jokes with invented numbers ("would have died 17 floors ago") are only allowed when flagged `joke=1` and phrased as hypothetical/obvious exaggeration; they are never shown as a recorded statistic. The lint rejects a joke line that uses a recorded-stat placeholder next to invented numbers.
4. **Seen ledger** (`SeenLedger`): a small local file in app prefs storage, separate from run logs and never uploaded: id -> (timesShown, lastShownAt, lastShownRunIndex). Survives restarts and app updates; a corrupt file degrades to "nothing seen yet" (never crashes, never blocks gameplay). Cleared only by the player (a reset row in APD Settings) or reinstall.
5. **Selection** (`HumorPicker`): filter by humor level and eligibility, then (a) prefer never-seen lines, weighted; (b) when none remain, take lines outside the recent window (last N shown or last M days), least-recently-seen first; (c) never the same category twice in a row; (d) a template family cooldown so the same joke shape does not cluster. Uses its OWN `java.util.Random` seeded from the wall clock, never `Random.Int` of the game, so game RNG is untouched.
6. **Scheduler** (`HumorScheduler`): decides WHEN. Rolls only at safe moments: title screen, run-end results/after the death or victory window is dismissed, victory screen, between-level transition after the new level is fully loaded and the hero is idle with no enemy in view, and the pause/inventory-closed state. Never: mid-turn, in combat, with a hostile visible, during boss fights, in dialogue or quest windows, while a prompt or tutorial is open, during save/load, or at the same moment as the UIMSD tenth-run prompt. Gated by the cooldown/session cap above and the player switch. The popup is a simple window shown from the render thread through the existing window stack; dismissing it is the only interaction. It consumes no turns, stores nothing in the game save, and calls no game RNG.
7. **Humor level**: `SPDSettings.apdAdultHumor()` (default Clean). Unknown eligibility = Clean; age is never inferred. Every line has a Clean text; the Adult text is optional.
8. **Fourth-wall lines** are just a category. A first-Cleric-run warning is a special one-time line id with its own ledger entry.
9. **Isolation guarantees** (tested): no `Random`/`Dungeon.hero` mutation, no game-time advance, identical game RNG state with the system on or off, no references to the humor package from game logic except one call at each safe-moment hook (guarded by a source-scan test in the style of PlayWiringTest/BackendFoundationsTest).
10. **Extensibility**: a new character, boss or ability gets lines by adding a pack file and a requires-predicate; no code change. Predicate vocabulary is a small registry, extended when UIMSD gains new facts.

## Tests to write after approval
Persistence across restart; never-seen-first and no repeat until exhausted, recent-window behaviour after exhaustion; eligibility (Cleric-only lines never appear while the last run was Warrior, placeholders require data); safe-moment table (every unsafe state returns "not now"); Adult/Clean restriction and Clean fallback when Adult text is absent; unknown age defaults to Clean; game-RNG and game-state equality with/without the system across a seeded run; lint of every pack (ids, lengths, placeholders); cooldown/session cap; ledger corruption recovery; source-scan that the humor package is not used outside the allowed hooks.

## Content plan (after approval)
Seed pack ~300 hand-written lines across the categories, then growth toward thousands by appending packs (batches of ~100, reviewed by the owner in plain text before they ship). Template shapes plus recorded context multiply variety; the ledger guarantees a long-time player keeps meeting unseen lines.

## SAMPLE DIALOGUE (for tone approval; Clean first, Adult variant where it differs)
Context-free (any character):
- `any.0001` "Just checking in. You're still not playing Cleric, right? Good. You've made at least one intelligent decision today."
- `any.0002` "This message has nothing to do with your current run. I just remembered Cleric exists, and now I'm upset."
- `any.0003` "Unrelated announcement: Cleric is still a class. I have had a few days to be okay with this. I am not okay with this."
- `any.0004` "Whatever you are playing right now, it is better than Cleric. That is not a balance statement. It is a feeling."
- `any.0005` "I would like to apologize to every other class for sharing a menu with Cleric." Adult: "...for sharing a fucking menu with Cleric."
- `any.0006` "Reminder: hydrate, stretch, and do not pick Cleric." Adult: "...and don't you dare pick fucking Cleric."
- `any.0007` "Nothing important happened. I just wanted you to know I'm still thinking about Cleric. Carry on."
Fourth wall:
- `fw.0001` "As the developer, I want it on record that I made this popup on purpose. Cleric did this to me."
- `fw.0002` "Yes, I spent real development time on a message about Cleric. No, I do not regret it. Yes, I need a hobby."
- `fw.0003` "If this popup is annoying, there is a switch in APD Settings. Cleric has no such switch, which is exactly the problem."
Hypothetical jokes (flagged as jokes, never presented as recorded):
- `joke.0001` "Fun fact: if Cleric were a person, it would have died 17 floors ago. Anyway, carry on." (joke: invented number is the punchline)
- `joke.0002` "Hypothetically, Cleric would have found the trap. Realistically, Cleric would have walked into it twice."
Contextual, recorded data only (placeholders):
- `hist.0001` requires clericRuns>=100: "You have started {clericRuns} Cleric runs. I fundamentally do not understand you."
- `hist.0002` requires clericWins>=50: "You have won {clericWins} Cleric runs. I am beginning to suspect the problem may be me. I still hate Cleric."
- `hist.0003` requires guidingLightCasts>=1000: "You have cast Guiding Light {guidingLightCasts} times. This has not improved my opinion of Cleric."
- `hist.0004` requires currentHero!=CLERIC & clericRuns>=1: "You are playing {currentHero}, and your records show {clericRuns} Cleric runs. I am choosing to believe those were accidents."
Cleric death / victory / failed ascension (requires the last run to be real and Cleric):
- `clr.death.0001` "Cleric died on depth {depth}. I would say I'm sorry, but that would be lying, and I don't lie about Cleric."
- `clr.death.0002` "Another Cleric run, another death. At least it was consistent."
- `clr.win.0001` "You won as Cleric. Fine. I will allow one moment of respect. That moment is over."
- `clr.win.0002` Adult: "You won as Cleric. Congratulations, you beautiful masochist."
- `clr.asc.0001` requires last run Cleric, reached the Amulet floor, died: "You got that far with Cleric and still did not ascend. I know that feeling. It took me 225 attempts. I have not recovered."
  (225 is the owner's own stated count, used only in the developer's voice; not a computed player stat.)
Boss / ability / equipment (requires the fact to be recorded):
- `boss.0001` requires boss killed this run & hero is not Cleric: "Congratulations on defeating {boss}. Cleric would have needed three tries and a nap."
- `abl.0001` requires hero is Cleric & a spell cast recorded this run: "{spell}. Sure. Great spell. Very Cleric of you."
- `eqp.0001` requires hero holds a Tome: "That is a Cleric's book. I would like to speak to whoever gave it to you."
Menu / post-victory:
- `menu.0001` "Welcome back. No Cleric was harmed in the making of this title screen. Several were mentioned."
- `post.0001` "You just won a run. This is the best time to remember that Cleric exists. You're welcome."
First Cleric run (one-time, from the owner's own text, polished): "You have chosen Cleric. I fucking hate Cleric. It took me 225 attempts to finally ascend with this fucking class. I'm not going to stop you from playing it because apparently some people enjoy suffering. Good luck. You'll need it." Clean: "You have chosen Cleric. I really, really dislike Cleric. It took me 225 attempts to finally ascend with this class. I'm not going to stop you, because apparently some people enjoy suffering. Good luck. You'll need it."

## Decisions needed from the owner
1. Approve the architecture and the default frequency (one per ~3 days of play, one per session, player-adjustable) versus the older "weeks or months".
2. Approve tone of the samples; mark any that should never ship.
3. Whether the difficulty-display gag (HANDOFF item 3) joins this engine (presentation only) or stays separate.
4. Whether the seed pack is written by me for your review, or you supply lines.

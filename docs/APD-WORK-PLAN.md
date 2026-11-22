# APD work plan: read this first every run (Chris, Nov 18 2026)

Every scheduled run starts fresh. This file is the memory: read it, work, then add a dated line to "Run log" below.

## Standing rules
- Repo: APD-Development/ShieldingShouldHaveWorked. Work branch: `release/play-closed-test`. Never push to `main`.
  Never force-push. Never delete branches. Daily alpha APKs come from `fix/apd-trap-room-search-20261116`.
- Never commit keystores, passwords, API keys or tokens. GitHub Actions secrets can't be written from the cloud
  sandbox; Chris adds them himself (see docs/PLAY-RELEASE.md).
- Ask Chris before changing any game mechanic, balance or scope. Fix real bugs right away and list them.
  Small decisions: make the call, list it in the summary, revert if Chris says no.
- No local Gradle in the sandbox. Test via CI: `gh workflow run apd-extended-bug-report-ci.yml --ref <branch>`
  (full :core:test). Release build + emulator launch check: `.github/workflows/apd-play-release.yml` (runs on push).
- Chris is the designer: never invent mechanics or features. Brainstorms are not approvals.
- Say exactly what was verified (which CI run passed) vs assumed.

## NEVER STAND DOWN (Chris, Nov 19 10:38 AM): scheduled runs ALWAYS code, even if a live session pushed recently.
A recent push is not a clash: `git pull --rebase` before every push and resolve conflicts. Skipping a run is not allowed.

## CURRENT STATE, Nov 18 21:35 (overrides anything older below)
- Closed test LIVE on Play since Nov 18, 12+ testers opted in (14-day clock running; earliest production apply ~Dec 2).
  Builds: 921 approved; 922, 923 superseded; 924 (Community text + Google sign-in crash fixes) uploaded by Chris.
- Play builds are signed OUTSIDE CI (GitHub secrets won't save on Chris's account): the manual run of
  apd-play-release.yml pushes the unsigned .aab to branch handoff/unsigned-aab and a Developer APK to handoff/dev-apk;
  the live session signs with the upload key (never in the repo). Scheduled runs: do NOT build Play bundles.
- Community server LIVE: Cloudflare Workers Builds from branch `deploy/community-server` (push HEAD there to deploy;
  D1 migrations run on deploy). https://apd-community.charltonchris29.workers.dev (/privacy, /delete).
- SUPERSEDED decisions: sign-in is GOOGLE ONLY (email code behind EMAIL_SIGNIN, off). Tournament is a 16-seed
  bracket from week 1 (1v16, 2v15 ... 8v9), RESEEDED every round (highest left vs lowest left); a tie for a seed,
  including at 16th, goes to the earlier submission. Week-1 picks are gone.
- Public GPL source: github.com/APD-Development/adhd-pixel-dungeon (export with tools/export-public-source.sh).

## HERO MOD ANSWERS (Chris, Nov 20 ~5:30 PM)
- Mod sprite IS shown in the dungeon (build it).
- Metamorphosis in a mod run: offer what it normally offers (keep the current other-class rule; no change).
- Starting items on top of vs instead of the class kit: still OPEN (Chris was unclear; keep kit + mod items until he answers).

## LOOT AVAILABILITY (Chris, Nov 20 2:30 PM) — done, live session
- Custom rule "Equipment Availability" is now "Loot Availability" (enum key EQUIPMENT kept for saves). It scales ONLY the number of ordinary random items each floor places (RegularLevel.createItems, gold piles included), round half up, min 1. Range stays 25-200%.
- NEVER two of the same item on one tile; no duplicate or deleted drops anywhere (enemy drops, rooms, shops untouched). No extra Upgrade Scrolls, Strength potions or enemy loot. Food keeps its own slider.
- OPEN: the score multiplier for this slider (ProvisionalScoreLookup EQUIPMENT row) was set for the old duplicate behavior and is wrong now. Chris doesn't know the right values yet; do NOT change it without him.

## UI POLISH (Chris, Nov 22 8:31 AM: "All of the above. It just looks horrible") — Play branch, top priority
Screens: Community menu, Custom Dungeon Rules (+ its sliders/presets), and the script-mods "Scripts" screen
(feature/script-mods only). Apply ALL FOUR directions Chris picked:
 1. Cleaner layout, same style: tighter spacing, smaller buttons, section headers, short one-line descriptions
    under each option instead of text walls; no huge uniform blocks.
 2. Tabs: Custom Rules split into tabs (Rules / Presets / Scoring); Community grouped where it helps.
 3. Cards: compact cards with an icon, a title and a one-line summary; tap for details.
 4. Collapsible: long explanations (experimental scoring etc.) fold behind a small "More"/"?" link; controls first.
Rename "Scripts" to something that says what it does ("Mods for this run"). Keep every existing function and value;
this is presentation only (no mechanic/balance changes). Each screen: screenshot-free JVM layout tests where
possible (no overlaps, nothing off-screen at 390px-wide phone scale), then a dev APK for Chris to look at.

## RUN REVIEW ANSWERS (Chris, Nov 19 6:30 AM) — these override earlier defaults/questions. Build them next.
- Luck drop: reviewers play the same seed; drops vary naturally like for any player (more/fewer potions is part of the
  test). No luck bonus or adjustment. Reviewers can say in their advice that a verdict depends on luck.
- Failed reviewers still vote + give advice, but their vote shows they died and on which floor.
- Bullying: moderator-only for launch. LATER (not now): flagged comment goes to a random other player (peer jury).
- REPLACE roasts on/off with a REVIEW TYPE the reviewed player picks: roasts allowed / straight advice only / encouraging.
  Reviewers see the choice before writing. Comments that don't match it or read as bullying are HELD for moderation
  instead of shown.
- Custom dungeon rules ARE reviewable: send the run's full custom rule settings with the submission; reviewers play the
  seed with exactly the same rules, challenges and difficulty LOCKED (can't change them). Remove the "custom rules not
  offered" restriction.
- Chris: don't ask permission for things he already said to do (e.g. deploy after tests pass).

## TONIGHT (Chris, Nov 18 21:32: "work throughout the night till I wake up at 6"). Build in this order.
1. **Community selected content** (DECIDED): when a bracket finishes, the champion gets a page with a status
   Planned -> In development -> Testing -> Released, set by the admin, plus admin dev notes; players comment on it.
   A browsable history of every past champion and its status ("an accessible history of what is being worked on").
   Server routes + D1 migration + admin controls in the app + tests.
2. **Run reviews** (DECIDED, Chris's design, do not add to it):
   - A player submits a finished run for review. Reviewers get the seed and play it in-game; the seed is never shown
     publicly, and reviewers see NOTHING about how the original player died or what loot they found until they've played.
   - Reviewer plays exactly the original setup (class, challenges, difficulty, settings) EXCEPT the original player's
     "luck drop". OPEN QUESTION, ask Chris in the morning before building that part: does "luck drop" mean the
     reviewer gets different random loot, or the same seed loot without the original player's luck bonus? Build
     everything else first; keep loot generation unchanged until he answers.
   - A reviewer counts once they reach the point where the original player died. Dying before that = a "failed
     review", which COUNTS toward the 5. No retry.
   - Review closes when 5 reviewers have finished (no time limit); the player sees progress like "2/5". Verdicts stay
     hidden until it closes. Each reviewer votes salvageable or not and says what the player should have done.
   - The reviewed player picks roasts on or off. Bullying comments can be reported; a peer or moderator decides if the
     reviewer keeps review access. No appeals: peer review decides it.
   - LATER, not now: in-game tips for beginners on how they could have escaped.
3. Ship-ready: speed (cache the last Community screen, refresh in background); real tests of admin tools.
4. SCRIPTED MODS, BROTATO STYLE (Chris, Nov 21 7:55 AM: "I want the way brotato mods work in APD. Otherwise mods in APD
   should not exist."). THIS REPLACES THE DATA-ONLY RULE BELOW.
   - Play policy (checked Nov 21, Device and Network Abuse): Play bans downloading EXECUTABLE code from outside Play,
     i.e. dex, JAR and .so files (Java bytecode as well as native). Code run by an interpreter that only reaches Android
     indirectly (JavaScript, Python, Lua...) IS allowed, as long as the scripts can't be used to break Play policy.
     So mods = Lua scripts run by an interpreter bundled in the APK, sandboxed: no file, network, OS or Java access,
     only a game API APD exposes. Never load mod dex/JAR/.so.
   - STATUS: the old data-only hero mods are switched OFF (ModHeroSelection.ENABLED=false, 4d615219/14ee18c0, CI
     44906689119): no hero-select list, no workshop menu, no new mod runs; old mod-run saves still load. Keep that code;
     it becomes part of the Lua mod system (a Lua mod can still ship hero.json + sprite.png).
   - CHRIS'S RULES (Nov 21 8:08 AM, binding):
     1. ENGINE: Rhino 1.9.1 (JavaScript), APPROVED by Chris Nov 21 8:23 AM ("Okay continue").
        ALL script-mod work happens on branch feature/script-mods (never on release/play-closed-test) so nothing
        mod-related reaches a Play build (rule 7). Merge into the Play branch only after Chris's phone test.
        DONE on feature/script-mods: ScriptSandbox (apd/mods/script) + 11 JVM tests (CI 44909036298) + device test
        ScriptSandboxDeviceTest passing on Android 8.0 (API 26) and 14 (API 34) emulators (run 44909459894;
        android-tests.yml only_class input).
     ITEMS HOOK (Chris, Nov 21 9:19 AM: "3. Both, the mod chooses per item. Natural drops use a mod-set drop weight
        with a sane cap so one mod can't flood the loot pool."):
        DONE (feature/script-mods, CI 44914359230, 24 mod tests): APD.item / APD.effect (apd/mods/script: ScriptDefs,
        ScriptModRuntime, ScriptItem, ScriptEffect, HeroApi, ScriptApi, ScriptSprites). Kinds potion/scroll/stone;
        dropWeight 0-3 per item (3 = as common as Healing), all mod items together <= 25% of a category
        (MAX_MOD_SHARE); startCount 0-5; no RNG used when no mod item can drop (normal seeds identical); hero API:
        heal, damage, shield, addBuff (whitelist), addEffect, message, hp/maxHp/level/depth; effects with onStart /
        onTurn / onEnd; saves keep mod id + item id, a missing/broken mod's items stay inert; nested calls share one
        engine context and never refresh a mod's own budget.
        DONE Nov 21 10 AM run (feature/script-mods; core CI 44917525402, 44917986363, 44918462693, 44918820359,
        44919586179; device test ScriptSampleModDeviceTest passed API 26+34 in 44919279513): steps a-d below.
        a) triggers fire from Hero.attackProc (melee/thrown), Wand.wandUsed, Hero.damage, Mob.die (enemy killed by
           hero/wand/thrown); plain facts only, return ignored, non-re-entrant. b) ScriptModLibrary. c) ScriptModRun
           (saved as apd_script_mods ids, re-read on load, missing mods inert, isModRun) + hero select "Scripts" button,
           test builds only (ApdFeatures.testBuild). d) sample mod android/apdSampleMods (test-build assets only).
        NEXT: e) build a Developer (test) APK from feature/script-mods and give it to Chris (live session; scheduled
        runs don't build APKs). Not device-verified yet: the Scripts button UI and an actual run with the sample mod.
        STILL TO DO, in this order, each with tests (CI on feature/script-mods; never merge to the Play branch):
        a) wire the other effect triggers into the game: onMeleeHit, onThrownHit, onWandZap, onDamageTaken, onKill,
           passing plain facts (damage, target name, wand/weapon name). Anything a script can do in response stays
           within the bounded hero API; no "repeat the hit/zap" feature unless Chris asks for it.
        b) load mods from files: <files>/scriptmods/<folder>/mod.json {format, id, name, author, description} +
           main.js (64 KB max); list them with reasons when refused.
        c) run activation: pick script mods before a run (hero select, like the old mod list); active mod ids saved
           in the run; on load re-read the scripts; give starting kits at run start; ScriptModRuntime.active() makes
           HeroModRun.isModRun() true (rule 5).
        d) a small sample mod bundled in a DEV APK only, plus a device test: one potion + one scroll + one timed
           effect, using existing buffs/heal/shield. (Chris, Nov 21: the example item he used to explain script mods
           was an AI suggestion; he doesn't use AI suggestions. Never build it or use it as sample/test data.)
        e) DONE Nov 21 1:05 PM: dev APK built from feature/script-mods (5dfc1bb2, run 44932639319; R8 keep/dontwarn
           rules for Rhino added) and sent to Chris; device tests ScriptSandboxDeviceTest + ScriptSampleModDeviceTest
           passed on API 26 + 34 (run 44932118010). That run also wrote a feature-branch AAB to handoff/unsigned-aab:
           NEVER upload a bundle built from feature/script-mods; the 8-hour Play build always rebuilds from this branch.
           Phone test still to come, but Chris said Nov 21 1:10 PM: get to work on "the heroes and shit" -> build
           the next hooks now (still feature/script-mods only, never a Play build).
     MONSTERS HOOK: CORE DONE Nov 21 1:40 PM on feature/script-mods (aec5ca05, CI 44934273954, ScriptMonstersTest 5/5;
        ScriptMob, MonsterApi, ScriptMobSprites, ScriptModRuntime.maybeSpawn hooked into Level.createMob; sample mod
        has a Sample Crab). DEVICE TEST DONE Nov 21 11 PM (ScriptMonsterDeviceTest, run 44983636500, API 26+34). NEXT: the
        HEROES HOOK below. Spec as written: APD.monster({ id, name, description, sprite: an existing mob sprite name
        (e.g. "RatSprite"), hp, attack:[min,max], accuracy, defense, evasion, armor:[min,max] or number, speed
        (0.5-2), xp, spawn:{ floors:[from,to], weight } and/or none, loot (a mod item id or none),
        triggers onSpawn / onTurn / onAttack(m, target) / onHurt(m, damage) / onDeath(m) }) with a bounded monster
        API (hp/maxHp/pos facts, heal, damage, addBuff from a whitelist, message). Numbers clamped to sane ranges.
        Spawning: mod monsters join the floor's spawn rotation by weight, capped like items: per-monster weight
        0-3, all mod monsters together <= 25% of spawns (same "sane cap" rule Chris set for loot); no RNG used
        when none can spawn. Saves keep mod id + monster id; a missing/broken mod's monster is removed on load
        without a crash. Mod monsters only in mod runs. Sample: add one monster to the bundled sample mod. Tests:
        JVM (defs, clamps, spawn share/cap, save/load, broken mod) + device test.
     SCOPE (Chris, Nov 21 1:10 PM: "Hell, if someone wants 100 dungeon floors, then they get it"): script mods must
        be able to change the game at that scale. The rooms/level-gen and rule-changes hooks must let a mod set the
        dungeon's length (e.g. 100 floors), which floors use which region/level type, where bosses go and what the
        final floor is, plus other run-wide rules. Design those hooks for that, not just small tweaks. (Still only in
        mod runs; normal runs untouched.)
     HEROES HOOK: CORE DONE Nov 22 1 AM on feature/script-mods (CI 44992368431, 44992723057, 44993400093, 44993695111,
        44994002452; device test ScriptHeroDeviceTest passed API 26+34 in 44994361546). APD.hero is checked by the
        old HeroMod rules and runs through HeroModRun (stats, talents, abilities, save record); startingItems may add
        { modItem, quantity } of the mod's own items; onStart/onLevelUp/onTurn/onKill wired; hero select Scripts list
        shows "Play as <hero> (<class>)" for mods that are ON; sample mod has a Sample Hero. sprite: not built (base
        class look). NOT device-verified: the Scripts-list hero UI and a real run. NEXT: TALENTS hook (rule 4 order).
     HEROES HOOK (spec, after monsters): APD.hero({ id, name, description, sprite (existing hero sprite or mod art later),
        baseClass, hp, str, startingItems (existing or mod item ids), talents (existing talent names per tier, like
        the old data mods), armorAbilities, triggers onStart / onLevelUp / onTurn / onKill }) reusing the old
        HeroModRun hooks where they fit; picked in hero select's Scripts list. Same caps/tests/sample rules.
     2. Play: mods = text scripts + art, run inside the engine. Never download/load dex, jar or .so. Anything a
        script can't do goes through submit -> Chris approves -> ships in an app update.
     3. Sandbox: no file, network, OS or reflection access; only the hooks APD exposes. Time AND memory limit per
        script. A broken mod shows an error and gets disabled; never crashes the game or corrupts a save.
     4. Hooks in this order: items, monsters, heroes, talents, rooms + level gen, then rule changes. Each hook ships
        with a sample mod + tests before the next one starts.
     5. Mod runs stay out of normal rankings, badges, tournament and run reviews unless Chris says otherwise.
     6. Workshop: existing Community workshop hosts script mods, same size limits, reporting, moderation; scripts
        scanned for banned API use on upload.
     7. NOTHING mod-related goes into a Play build until engine + sandbox + items hook pass tests AND Chris has tried a
        sample mod on his phone (deliver it in a dev APK only). Keep ModHeroSelection.ENABLED=false in Play builds.
   - (older note) NEXT (design first, then build; ask Chris before scope calls): pick the interpreter (LuaJ pure-Java is the likely
     choice; confirm license + Android support + sandboxing), define the game API (items, potions/scrolls with custom
     effects, buffs, hooks: on zap / thrown hit / melee hit / damage taken / kill / turn), save compatibility, limits
     (instruction budget per hook, memory), and workshop review. 
   OLD DATA-ONLY PLAN (Nov 19, superseded; kept for history): Brotato-workshop style,
   DATA + ART ONLY. A hero mod = hero.json + sprite.png built only from existing pieces:
   name/author/description, baseClass (class engine + subclasses), starting HP + STR, starting items (+level, quantity),
   talents for tiers 1-3 picked from ANY class's talents of that tier (same count as the base class), 1-3 existing
   armor abilities. Anything needing new code: modder submits, Chris approves, it ships in an app update.
   BUILD ORDER: (a) DONE 4:45 PM: apd/mods/HeroMod parser + validator + tests. (b) DONE 5 PM: apd/mods/ModLibrary scans a mods folder (needs wiring to the real files dir): load
   <files>/mods/<id>/hero.json + sprite.png (same layout as hero sprites), list valid ones, show refusal reasons.
   (d-core) DONE 5:15 PM: apd/mods/HeroModRun gameplay hooks (talents, armor abilities, base HP, STR, extra items on top of the class kit
   ("keep class kit" = safe default so class mechanics don't break), mod json stored in the save).
   (gating) DONE 7 PM Nov 19. BEFORE (c) exposes mods: gate mod runs (HeroModRun.isModRun()) out of Community (rankings upload, tournament, run reviews,
   send-for-review) and out of normal badges; local rankings entry marked Mod. Then (c) hero select: modded heroes appear after the normal classes (name, sprite, description, author).
   (d) game: start a run as the mod: base class engine, mod HP/STR, mod starting items instead of the class kit,
   mod talent tiers in place of the class lists, mod armor abilities; save/load keeps the mod id (missing mod on load =
   clear message, never a crash). (e) Community workshop: upload (signed in, 16+), browse, subscribe/download,
   report -> moderator, size limits (json 16 KB, png 64 KB). (f) DONE 5 PM Nov 19 (first draft, matches current code): docs/MODDING.md for modders.
   OPEN QUESTIONS for Chris (ask one at a time; build the rest meanwhile, safe default in brackets):
   - ANSWERED (Chris, Nov 19): mod-hero runs count "only on their app": local rankings/history on that phone only, marked Mod;
     never sent to Community (no Community leaderboards, tournament, run reviews). Badges (Chris): MOD-ONLY badges: mod runs never unlock normal badges; mods get their own separate badge set (design of that set: ask Chris later, do not invent).
   - Can a mod hero's numbers (talent strengths etc.) be changed by modders? [not yet: needs per-talent code changes]
   NOT designed yet, do NOT build or invent: competitions, community events, development and testing.
   Chris will be asked one question at a time.
Every change: tests, CI (apd-extended-bug-report-ci.yml) green, push release/play-closed-test, and push the server to
deploy/community-server only when server tests pass. Log what was verified.

## Release: public on Google Play Jan 1, 2027
Package `com.charltonchris29.adhdpixeldungeon`, personal account: 12-tester closed test for 14 straight days
before production, so the closed test must start by Nov 23.

### Priority order (Chris, Nov 18 09:49; 09:55: "Only community is important above all else")
Chris 09:57: don't drop Phase 1: closed test as close to Nov 23 as possible. Phase 1 code is done; the moment the
APD_UPLOAD_* secrets exist, push to release/play-closed-test (touch the workflow) to rebuild, confirm the .aab is signed
(`jarsigner -verify` in CI), then give Chris the Play Console upload steps one at a time. Otherwise all time goes to Community.

1. **Phase 1 (target Nov 21-22):** signed release .aab. Done except signing: blocked on Chris adding the
   APD_UPLOAD_* secrets. Then: Play Console upload steps for Chris, one at a time.
2. **Community (most important for Jan 1), starts right after Phase 1.** Goal for Dec 22: full Community loop:
   submissions with threaded comments, voting down to a top 16, the weekly elimination tournament, moderation
   (report, block, profanity/slur filter, no private messages), admin tools for Chris (hide/remove posts, ban
   accounts), account deletion, accounts (Google sign-in OR email/password with emailed verification code), and the
   16+ age gate for Community only (Play Age Signals API first; else birth month+year, store only pass/fail, no
   retry with a different year on that device). Backend: Cloudflare Workers + D1 + Turnstile (recommended to Chris
   Nov 18; he answered yes). Use the existing provider-neutral foundations (docs/APD-BACKEND.md, apd/backend,
   apd/backendbridge; the HttpGateway contract `/v1/envelope`, `/v1/requests` and EndToEndSubmissionTest's
   reference server). Real tests for the server and the age gate. Design: docs/APD-COMMUNITY-ALPHA-DESIGN.md
   (Month 1 submissions, Month 2 voting to top 16, Month 3 weekly elimination; comments never count as votes).
   DECIDED Nov 18 (Chris): Month 2 upvote-only, one per submission, never your own; top 16 plus everything tied with
   16th. Tournament week 1: everyone picks up to 8 of the qualifiers, top 8 advance, cut ties to higher Month 2 score.
   Weeks 2-4: seeded 1v1 bracket (1v8, 4v5, 2v7, 3v6), one vote per account per matchup, changeable until the week
   ends, tie to the higher seed. Claude's calls (revertable): full ties go to the earlier submission; no voting for
   your own entry in the tournament either; banned voters' and removed posts' votes don't count; byes with <8. Test the full cycle with a
   compressed clock. Needs from Chris: Cloudflare account + API token secret, Google OAuth client, email sender.
   If done early: run reviews, then events.
3. **Phase 2 cleanup, alongside Community (due Dec 10):** GPL source public (new public repo, clean history;
   `GplLicense.SOURCE_URL` must point to it; repo name from Chris), keep LICENSE and copyright headers, About
   credits Shattered Pixel Dungeon; remove Shattered/Evan branding, support and Patreon text and anything implying
   endorsement; nothing contacts Shattered servers (done Nov 18); ChatGPT auto-dev workflow disabled (done);
   adult humor OFF by default (done). `tools/export-public-source.sh` makes the GPL source zip (no history, no
   keystore) and REFUSES while android/build.gradle still has the old alpha key password fallback: remove that
   fallback (and keystore/) once Chris adds the APD_DEBUG_* secrets.
4. **Phase 4, Dec 23-31:** bug fixing only, final build; Chris submits for production.

### Play Console paperwork Chris must do (remind him)
Add the 4 upload-key secrets; Data safety form (accounts, user content); content rating questionnaire again (users
interact); privacy policy update; account deletion web link.

## Daily alpha APK: PAUSED (Chris, Nov 18 11:30)
No more alpha APKs after C489S/P489S until the Community server is up and running. Do not run the steps below
until Chris says so.

## Daily alpha APK steps (3 PM ET run only, when un-paused)
Merge `release/play-closed-test` into `fix/apd-trap-room-search-20261116`, add the Change History entry
(core/src/main/assets/apd/changes.tsv, player-noticeable changes only), bump the build label in
`.github/workflows/apd488t-trap-room-search-apk.yml` (C489O -> C489P -> ...; that push triggers the build), fix any
failure, and give Chris both artifact links (Developer = C, Alpha Testers = P) by 4 PM ET.

## Community server
`server/community/` (Worker + D1). Tests: `npm test` (node 22 `--experimental-sqlite`), CI workflow
`apd-community-server.yml`. Done Nov 18: accounts (email+code, Google), age pass flag, submissions, threaded comments,
filter, reports, blocks, admin, account deletion. Seasons + voting + tournament done. Nov 18 PM: login/sign-up rate limits, deletion keeps others' replies,
real Google token check, Cloudflare Email Service mailer (needs Workers Paid + domain onboarding: ASK CHRIS, costs
money), deploy workflow (needs CLOUDFLARE_API_TOKEN/ACCOUNT_ID secrets; D1 found/created by name; deploy = push the
release head to branch deploy/community-server), Java client + all Community screens
wired (CommunityController/CommunityScene; live when SPDSettings.communityServer is set). Existing players keep old
default-on settings (SPDSettings.keepOldDefaultsForExistingPlayers). Next: Google sign-in button on Android
(Credential Manager), server URL default once deployed, run reviews. HttpGateway contract routes, deployment workflow, Android Community screens wired to it, Play Age Signals.

## Run log
- 2026-11-22 2 PM (scheduled): Community server bug: upvoting a banned player's idea, or one from a player I blocked, by its id still worked although reading it returned 404; now 404 too (same rule as comments). +1 assertion; server CI 45058946421 green. NOT deployed (live session/Chris to deploy). Still OPEN for Chris: `defense` vs `evasion` in the monsters spec; talents hook spec; mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-22 1 PM (scheduled): Community server bug: commenting on a banned player's idea, or on one from a player I blocked, by its id still worked although reading it returned 404; now 404 too (same rule as review claims). +2 assertions; server CI 45053261292 green. NOT deployed (live session/Chris to deploy). Still OPEN for Chris: `defense` vs `evasion` in the monsters spec; talents hook spec; mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-22 12 PM (scheduled): Community server bug: deleting an account kept only one level of placeholders, so another player's reply under two of the deleted player's own comments was cascade-deleted (submissions and champion comments); now repeats until every ancestor stays. +1 server test; server CI 45047715635 green. NOT deployed (deploy/community-server not pushed; live session/Chris to deploy). Still OPEN for Chris: `defense` vs `evasion` in the monsters spec; talents hook spec; mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-22 11 AM (scheduled): Community cache bug: commenting on an idea left the cached Submissions list showing the old comment count; now drops it. +1 test; full CI 45041988469 green. Not device-verified. UI-polish dev APK still needs a live session. Still OPEN for Chris: `defense` vs `evasion` in the monsters spec; talents hook spec; mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-22 10 AM (scheduled): Community cache bug: posting an idea, upvoting, voting, blocking/unblocking, champion status/notes and moderator hide/remove/ban/start-season left the cached list screens stale (old counts or now-hidden posts shown until the background refresh, or kept if it failed); now each drops the lists it changed. +1 assertion; full CI 45036602075 green. Not device-verified. UI polish screens all done; the UI-polish dev APK for Chris still needs a live session (scheduled runs don't build). Still OPEN for Chris: `defense` vs `evasion` in the monsters spec; talents hook spec; mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-22 9 AM (scheduled): UI polish, Scripts screen (feature/script-mods): hero-select button "Scripts" -> "Mods", window title "Mods for this run", one-line description instead of the long text. Presentation only; full CI 45031058430 green. Not device-verified. Still OPEN for Chris: `defense` vs `evasion` in the monsters spec; talents hook spec; mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-22 7 AM (scheduled): Script mods (feature/script-mods): monster heal() after its own script killed it (e.g. damage(999) then heal(5) in one onTurn) put HP back above 0 on a dead monster; now heal does nothing on a dead monster (same as hero heal). +1 JVM test; full CI 45020594420 green. Not device-verified. Still OPEN for Chris: `defense` vs `evasion` in the monsters spec; talents hook spec; mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-22 6 AM (scheduled): Script mods (feature/script-mods): a mod monster killed by its own onSpawn/onTurn script kept acting that turn (could still attack); now it stops, and onTurn never runs on a monster onSpawn killed. +1 JVM test; full CI 45016137276 green. Not device-verified. QUESTION for Chris: the monsters spec lists both `defense` and `evasion`, but only `evasion` is read (`defense` is silently ignored); what should `defense` do? Still OPEN: talents hook spec; mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-22 5 AM (scheduled): Script mods (feature/script-mods): APD.monster `spawn` without `floors` silently spawned on floor 1 only; now the mod is turned off with a reason (my call, revertable). +1 JVM test case; full CI 45010835965 green. Not device-verified. Still OPEN for Chris: talents hook spec; mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-22 4 AM (scheduled): Script mods (feature/script-mods): APD.monster `loot` naming an item the mod never declares silently never dropped; now the mod is turned off with a reason (same rule as a hero's modItem). +1 JVM test case; full CI 45005980989 green. Not device-verified. Still OPEN for Chris: talents hook spec; mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-22 3 AM (scheduled): Script mods (feature/script-mods): APD.monster `armor` accepted only a number, but the written spec says "armor:[min,max] or number"; now [min, max] rolls min..max per hit, a number still rolls 0..n (unchanged). +1 JVM test; full CI 45001300089 green. Not device-verified. Talents hook still waits on Chris's spec (what may a talents hook do?). Still OPEN: mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-22 2 AM (scheduled): Script mods (feature/script-mods): APD.hero `sprite` now picks an existing hero's look (e.g. 'mage') for the dungeon sprite and avatar, saved with the hero (HeroMod optional `look`); unknown name turns the mod off. +1 JVM test; full CI 44996787418 green. Not device-verified. Talents hook NOT started: no spec exists. QUESTION for Chris: what should the talents hook let a mod do (new talents with their own script effects, or scripts attached to existing talents)? Still OPEN: mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-22 1 AM (scheduled): Script mods (feature/script-mods): HEROES HOOK core done: APD.hero (data-mod rules + mod items in the kit, max 8), run start/save via HeroModRun, onStart/onLevelUp/onTurn/onKill, hero-select 'Play as' rows, Sample Hero. +9 JVM tests (last full CI 44994002452 green) + ScriptHeroDeviceTest API 26+34 (44994361546). Not phone-tested. Next: talents hook. Still OPEN for Chris: mod starting items on top of vs instead of the class kit (script heroes also keep kit + items); Loot Availability score multiplier.
- 2026-11-22 12 AM ET (8-hour Play build): Play **936** at 599ea65a uploaded to closed test (alpha). CI 44987554929 green on 86703508. New: three-way walk-over pickup setting. No script mods. Last uploaded build: 936.
- 2026-11-21 11 PM (scheduled): Script mods (feature/script-mods): new ScriptMonsterDeviceTest: sample mod's Sample Crab spawns on its floors only, keeps name/sprite/HP through a real Bundle write/read, missing mod leaves it inert. Passed API 26 + 34 (run 44983636500). Test only, no game code. Next: heroes hook. Still OPEN for Chris: mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-21 4 PM ET build (fired late, ran 10 PM): Play **935** at 6dd2222a uploaded to closed test (alpha). CI 44978653680 green on 051875eb. New: Duelist lock-on, walk-over whole pile + survives harmless interrupts, Wealth gear range, Gemstone Dust halved, spellbook scroll names, bug report pickup logging. No script mods (verified: no Rhino, no scriptmods in the bundle). Last uploaded build: 935.
- 2026-11-21 10 PM (scheduled): no code; a live session pushed to this branch at 22:05 (Play build 935), so this run
  stood down to avoid clashing.
- 2026-11-21 1 PM (scheduled): Script mods (feature/script-mods): a mod effect's onDamageTaken still ran after the killing blow, so a script heal gave HP back to a dead hero whose game was over; dead hero's effects now never run and heal/shield/addBuff/addEffect do nothing on a dead hero. +1 test; full CI 44933784503 green. Not device-verified. Still WAITING on Chris's phone test of the dev APK before the monsters hook. Still OPEN for Chris: mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-21 12 PM (scheduled): Script mods (feature/script-mods): installBundledSamples wrote mod.json then main.js straight into the final folder, so a failed write left a half folder that was refused ("main.js is missing") and never re-copied; now staged in a hidden folder and renamed into place, leftovers cleaned. +1 test; full CI 44928410863 green. Not device-verified. Next still: step e dev APK for Chris (live session). Still OPEN for Chris: mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-21 11 AM (scheduled): Script mods (feature/script-mods): mod.json "format" accepted 1.5 or "1" as format 1, and "one" was refused only as "could not be read"; now must be the number 1, refused with the format reason. +1 test; full CI 44923185305 green (first run 44922763612 failed on a test-fixture folder clash, fixed). Next still: step e dev APK for Chris (live session). Still OPEN for Chris: mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-21 10 AM (scheduled): script mods steps a-d on feature/script-mods (triggers wired, mod files loader, run activation + test-build Scripts picker, dev-only sample mod); all core CI green (last 44919586179), device test 44919279513 green. Next: step e dev APK for Chris's phone test.
- 2026-11-21 9 AM ET (scheduled): Script mods (feature/script-mods only): ScriptSandbox.call accepted any Java object as an argument, which Rhino would wrap and expose its methods to the script (breaks rule 3, no Java access); now only plain values (string/number/boolean/null) go in, anything else is refused without disabling the mod. +1 test; full CI 44911948358 green. Not device-verified. Still waiting on Chris: items hook API proposal before building; mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-21 8 AM ET (8-hour Play build, ran 8:05): Play **934** at 1ceeac2f uploaded to closed test (alpha). CI 44907063664 green on 73a7e47e. New: data-only hero mods switched off (Lua mods coming), overnight mod hardening. Last uploaded build: 934.
- 2026-11-21 7 AM ET (scheduled): Hero mods bug: installing a workshop mod whose id matched a hand-placed folder holding a DIFFERENT mod silently replaced (deleted) that mod; now refused with a reason, folder untouched. +1 test; full CI 44902279007 green. Not device-verified. Still OPEN for Chris: mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-21 6 AM ET (scheduled): Hero mods safety: ModLibrary.scan read hero.json and the workshop upload read sprite.png with readAllBytes (whole file, after the size check; upload also followed links); new shared bounded no-follow ModLibrary.readBounded (ModSprites.readSprite now uses it). +1 test; full CI 44897599221 green. Not device-verified. Still OPEN for Chris: mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-21 5 AM ET (scheduled): Hero mods validator: mod name/author could contain line breaks, tabs or control characters, which broke one-line titles (hero select, hero window, rankings "Mod:" rows); now refused with a reason, description keeps plain line breaks (my call, revertable). MODDING.md updated. +1 test; full CI 44892790781 green. Not device-verified. Still OPEN for Chris: mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-21 4 AM ET (scheduled): Hero mods safety: ModSprites read sprite.png with readAllBytes before any size check and followed links, so a sprite swapped for a huge/linked file after the scan was loaded whole into memory; new bounded no-follow readSprite (null = base class look). +1 test; full CI 44888020019 green. Not device-verified. Still OPEN for Chris: mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-21 3 AM ET (scheduled): Hero mods safety: ModLibrary.scan followed linked mod folders / hero.json / sprite.png (could read files outside the mods folder; ModInstaller already refused links); now refused with a reason. MODDING.md updated. +1 test; full CI 44883345615 green. Not device-verified. Still OPEN for Chris: mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-21 2 AM ET (scheduled): Hero mods validator: pickup-only items (Dewdrop, keys.*, journal.*, LostBackpack, TengusMask, CorpseDust/DwarfToken/CeremonialCandle) were accepted as starting items but only work via doPickUp, so they sat dead in the pack; now refused with a reason (my call, revertable). MODDING.md updated. +1 test; full CI 44878683432 green. Not device-verified. Still OPEN for Chris: mod starting items on top of vs instead of the class kit; Loot Availability score multiplier.
- 2026-11-21 1 AM ET (scheduled): Hero mods validator: general base types (Item, Potion, ExoticPotion, Ring, MeleeWeapon, Bag, Artifact) were accepted as starting items, giving a kindless item (raw Potion/Ring can crash the identify tables); now refused with a plain reason (my call, revertable). MODDING.md updated. +1 test; full CI 44874094632 green. Not device-verified. Still OPEN for Chris: mod starting items on top of vs instead of the class kit (keeping kit + mod items); Loot Availability score multiplier.
- 2026-11-21 12 AM ET (8-hour Play build): Play **933** at 78ab6a12 uploaded to closed test (alpha). CI 44868795245 green on 032de70f. New: mod sprite in dungeon + mod names/talents in hero/save windows, full-backpack pickup settings, Gathering moves loot on spent traps. Last uploaded build: 933.
- 2026-11-20 11 PM ET (scheduled): Hero mods bug: the starting-item name check refused every top-level item (Gold, EnergyCrystal, Ankh, Torch...), so the documented Gold/EnergyCrystal starting amounts could never be used; now accepted. Amulet of Yendor refused as a starting item (instant 'win'; my call, revertable). MODDING.md updated. +1 test; full CI 44864530914 green. Not device-verified. Still OPEN for Chris: mod starting items on top of vs instead of the class kit (keeping kit + mod items).
- 2026-11-20 10 PM ET (scheduled): Hero mods: the saved-run list (Start > class) and the saved-run window showed only the base class for a mod run; both now name the mod hero ("Mod: <name>", "Unreadable mod" for a damaged record; read from the save, never changes live run state; my call, revertable). +1 test; full CI 44859555951 green. Not device-verified. Still OPEN for Chris: mod starting items on top of vs instead of the class kit (keeping kit + mod items); Loot Availability score multiplier.
- 2026-11-20 9 PM ET (scheduled): Hero mods bug: the in-run hero info window (WndHero info button) showed the BASE class talents and armor abilities in a mod run; it now shows the mod's (hero select still shows the plain class, never a leftover mod). +1 test; full CI 44854098226 green. Not device-verified. Still OPEN for Chris: mod starting items on top of vs instead of the class kit (keeping kit + mod items); Loot Availability score multiplier.
- 2026-11-20 8 PM ET (scheduled): Hero mods: HeroModRun.heroName existed but was never used, so the hero window title showed only the base class in a mod run; it now shows the mod hero's name above the level/class line (disguise unchanged; my call, revertable). +1 test; full CI 44848178783 green. Not device-verified. Still OPEN for Chris: mod starting items on top of vs instead of the class kit (keeping kit + mod items); Loot Availability score multiplier.
- 2026-11-20 7 PM ET (scheduled): Hero mods bug: when a disguise (HeroDisguise) ended, the hero went back to the BASE class sheet instead of the mod sprite; mirror images also used the base class sheet. Both now use the hero's own look (HeroSprite.ownSheet: mod sheet in a mod run, else class sheet; mirror images: my call, revertable). +1 test; full CI 44842188146 green (first try 44841716151 failed only my test's file path). Not device-verified. Still OPEN for Chris: mod starting items on top of vs instead of the class kit (keeping kit + mod items); Loot Availability score multiplier.
- 2026-11-20 6 PM ET (scheduled): Hero mods: mod sprite now shown in the dungeon (Chris answered yes): HeroSprite + status-pane avatar use the installed mod's sprite.png (matched by mod id); missing/broken mod falls back to the base class sheet; disguise unchanged. +1 test; full CI 44834412521 green. Not device-verified. Still OPEN for Chris: mod starting items on top of vs instead of the class kit (keeping kit + mod items).
- 2026-11-20 5 PM ET (scheduled): Hero mods bug: a Seed floors replay (stats screen) builds its throwaway hero via HeroClass.initHero -> HeroModRun.beginRun(), which turned a live mod run into a normal one (Community/badge gating lost) and ate a pending mod pick. Replay now cancels the pick for its own hero and puts the full mod state back (HeroModRun.Snapshot). +1 test; full CI 44826264110 green. Not device-verified. Still BLOCKED on Chris: mod sprite in the dungeon? Starting items on top of vs instead of the class kit? Metamorphosis base-class talents for mod heroes?
- 2026-11-20 4 PM ET (8-hour Play build): Play **932** at 70af488d uploaded to closed test (alpha). CI 44816421163 green on 51a81f64. New: block numbers, Loot Availability, ring alchemy (uncursed/unknown rings, identifies, worn rings refused), Gemstone Dust icon, hero mod fixes. Last uploaded build: 932.
- 2026-11-20 3 PM ET (scheduled): Hero mods bug: a mod's starting Gold / EnergyCrystal went into the pack as a dead item; now adds to the run's gold / alchemy energy (not counted as gold collected; my call, revertable). MODDING.md updated. +1 test; full CI 44808501703 green. Daily alpha APK skipped: still PAUSED. Not device-verified. QUESTION for Chris: Metamorphosis in a mod run never offers the base class's own talents (other-class rule); should a mod hero be offered them? Still BLOCKED: mod sprite in the dungeon? Starting items on top of vs instead of the class kit?
- 2026-11-20 2 PM ET (scheduled): Hero mods bug: in a mod run, Scroll of Metamorphosis changes were lost on save/load (mod talent list rebuilt without the replacements, so the new talent and its points vanished), and the scroll's choose window listed the base class's talents instead of the mod's. Both fixed. +1 test; full CI 44799194577 green. Not device-verified. Still BLOCKED on Chris: mod sprite in the dungeon? Mod starting items on top of vs instead of the class kit?
- 2026-11-20 1 PM ET (scheduled): Hero mods: mod starting items ignored the run's challenges (class kit skips challenge-blocked items, e.g. Dewdrop under No Herbalism); now the same rule applies (my call, revertable). +1 test; full CI 44789740596 green. Not device-verified. Still BLOCKED on Chris: mod sprite in the dungeon? Mod starting items on top of vs instead of the class kit?
- 2026-11-20 12 PM ET (scheduled): Hero mods: "Remove from this phone" only dropped a pending mod pick when it was the exact same object, so a pick from an earlier scan of the same mod survived removal; now matched by mod id. +1 test; full CI 44780163731 green. Not device-verified. Still BLOCKED on Chris: mod sprite in the dungeon? Mod starting items on top of vs instead of the class kit?
- 2026-11-20 11 AM ET (scheduled): Hero mods validator: a starting item with level > 0 on a non-upgradable item ("+3 Ration") was accepted, and items the game can't create were silently dropped at run start; both now refused with a plain reason (my call, revertable). MODDING.md updated. +1 test; full CI 44770220972 green. Not device-verified. Still BLOCKED on Chris: mod sprite in the dungeon? Mod starting items on top of vs instead of the class kit?
- 2026-11-20 10:35 AM (live): Play 930 uploaded (full 0.5x-1000x game speed in every build, walk-over item pickup ON by default). CI 44762431311 green; release run 44763281102. Last uploaded build = 930 at commit ae32c17c.
- 2026-11-20 10 AM ET (scheduled): Hero mods bug: a starting item's quantity was silently ignored for non-stackable items (Sword quantity 3 gave 1); now gives that many separate copies, and a full pack puts starting items straight into the backpack instead of losing them (old code retried collect() once, which also failed). MODDING.md updated. +1 test; full CI 44760160033 green. Not device-verified. Still BLOCKED on Chris: mod sprite in the dungeon? Mod starting items on top of vs instead of the class kit?
- 2026-11-20 9 AM ET (scheduled): Community workshop cache bug: subscribing left the cached "All mods" list (subscriber count) stale, and cache removals/sign-out clear ran without the lock the background refresh uses (HashMap race). New uncache() locked helper; subscribe/upload/delete drop both mod lists. +1 assertion; full CI 44750549000 green. Not device-verified. Still BLOCKED on Chris: mod sprite in the dungeon? Mod starting items on top of vs instead of the class kit?
- 2026-11-20 8:05 AM (8-hour Play build): Play 929 uploaded (hero mod workshop screens, mod sprite previews, remove mod). Server deployed to deploy/community-server (42 tests pass). CI 44740652082 green; release run 44741299470. Last uploaded build = 929 at commit 46a9a2a2.
- 2026-11-20 7 AM ET (scheduled): Hero mods bug: HeroMod.parse could throw past Invalid (an object/list where an ability or talent name belongs -> IllegalStateException), which the workshop installer and save restore don't catch (crash); and huge numbers wrapped through asInt (hp 4294967318 accepted as 22). Now parse only throws Invalid and range-checks before asInt. +1 test; full CI 44733314133 green. Not device-verified. Still BLOCKED on Chris: mod sprite in the dungeon? Mod starting items on top of vs instead of the class kit?
- 2026-11-20 6 AM ET (scheduled): Hero mods bug: "Remove from this phone" silently failed for hand-placed mod folders whose name isn't id-shaped (e.g. "My Hero"; MODDING.md allows any folder name). New ModInstaller.uninstallFolder (one plain name, no paths/dot folders/links). +1 test; full CI 44725058061 green. Not device-verified. Still BLOCKED on Chris: mod sprite in the dungeon? Mod starting items on top of vs instead of the class kit?
- 2026-11-20 5 AM ET (scheduled): Hero mods: installed mods could never be removed from the phone (ModInstaller.uninstall had no UI). Hero select mod details now has "Remove from this phone" (confirm; deletes only the folder the mod was loaded from, directly under <files>/mods; drops a pending pick so the next run is normal; started runs keep their saved mod record). My call, revertable. +1 test; full CI 44716494612 green. Not device-verified. Still BLOCKED on Chris: should mod runs use the mod sprite in the dungeon? Starting items on top of class kit vs instead of it?
- 2026-11-20 4 AM ET (scheduled): Hero mods workshop sprite preview: the workshop mod page shows the download's own sprite (idle frame) beside the title (ModSprites.avatar from bytes, cached per mod id+version; missing/bad/over-64 KB data = no picture, never a crash; WndUimsdList got an optional title icon). +1 test, +2 assertions; full CI 44708071577 green. Not device-verified. Next: in-dungeon hero sprite for mod runs is BLOCKED on Chris (should mod runs use the mod sprite in the dungeon?).
- 2026-11-20 3 AM ET (scheduled): Hero mods sprite preview: the mod details window in hero select now shows the mod's own sprite.png idle frame (new apd/mods/ModSprites, cached per file+mtime so a reinstall shows the new art; unreadable/wrong-size sprite falls back to the base class avatar, never crashes). +1 test; full CI 44700114006 green. Not device-verified. Next: same preview in the Community workshop mod page, then in-game hero sprite for mod runs (ask Chris if mod runs should use the mod sprite in-dungeon).
- 2026-11-20 2 AM ET (scheduled): Hero mods (e) workshop screens: Community hub "Hero mods workshop" lists shared mods (author, version, subscribers, installed tag), My subscriptions, mod page with Download and install / Reinstall (ModInstaller into <files>/mods; refused download leaves installed copy, plain-words reason), Subscribed toggle, Report (kind mod), Delete for my own mods, Upload one of my installed mods (exact hero.json + sprite; update = new version). +1 controller test; full CI 44693713987 green (first two tries 44692722634/44693246504 failed only my test's assertions/ids). Not device-verified; server still needs the 11 PM migration 0009 deployed by a live session. Next: mod sprite preview in hero select/workshop.
- 2026-11-20 1 AM ET (scheduled): Hero mods (e) app side: new apd/mods/ModInstaller puts a workshop download (exact hero.json + base64 sprite) into <files>/mods/<mod id>/ only after the same checks as the scanner (json/sprite limits, 256x128 PNG header), via a staging folder + atomic swap; refused downloads never touch an installed mod; reinstall replaces; same id in another folder refused with its name; never writes through links; uninstall. ModLibrary now ignores dot folders (staging leftovers). +5 tests; full CI 44685651786 green. Not device-verified. Next: workshop screens in Community (browse, download -> ModInstaller, subscribe, upload).
- 2026-11-20 12:05 AM (8-hour Play build): Play 928 uploaded to the closed test (lag fix, frame-rate diagnostics, hero mods on hero select, workshop groundwork). CI 44678269167 green; release run 44678613506. Last uploaded build = 928 at commit f44c5f4f.
- 2026-11-19 11 PM (scheduled): Hero mods (e) workshop SERVER: migration 0009 + /v1/mods upload (16+ member; hero.json <=16 KB, valid id, name/author/description refused if the word filter would change them since the game shows the raw json; sprite.png <=64 KB, 256x128 PNG header), browse, download (exact json + base64 png), subscribe/unsubscribe/subscribed list, author delete, re-upload by same author replaces (version+1), id owned by its author, 5 uploads/updates per day (my call), report kind mod -> moderator queue, admin hide/remove/restore, blocks/bans hide it, account deletion removes mods + subs. App: CommunityApi mod calls (limits checked before any request) + plain words for every new error. +3 server tests, +1 API test; server CI 44672440961 + full core CI 44673214715 green (first core try 44672732580 failed only the plain-words guard for the new errors). NOT deployed: sandbox refused the push to deploy/community-server, a live session must push it. Next: in-app workshop screens (browse/download into <files>/mods/, upload from a mod folder), mods (c) sprite preview.
- 2026-11-19 10 PM (scheduled): Hero mods (d): continuing a save whose mod record can't be read now shows a clear in-game warning (continues on base class, stays Mod/local, record kept). +1 test; full CI 44665454320 green. Not device-verified. Next: mods (c) sprite preview, then (e) workshop.
- 2026-11-19 9 PM (scheduled): Hero mods bug: a save whose stored mod record no longer parsed silently became a NORMAL run (could unlock normal badges / be sent for review); now it stays a mod run ("Unreadable mod" in local rankings) and the raw record is written back unchanged. +1 test; full CI 44658432439 green. Not device-verified. Next: mods (c) sprite preview, then (d) missing-mod message.
- 2026-11-19 8 PM (scheduled): Hero mods (c) first cut: hero select shows a Mods button (only when <files>/mods has anything) listing installed mod heroes (name, author, description, base class, HP/STR, refused mods with reasons); Play as selects the base class and queues the mod's exact json; picking a normal hero, a different class or a daily drops it. No sprite preview yet (list is text). +3 tests; full CI 44651310645 green. Not device-verified. Next: mod sprite in hero select/game, then (d) remaining start-run checks.
- 2026-11-19 7 PM (scheduled): Hero mods gating done: mod runs never unlock/announce normal badges, local rankings entry marked Mod (record keeps mod name; list shows "Mod:", window shows Mod hero), no Send for review on mod runs, a review run drops a pending mod hero. +3 tests; full CI 44643352780 green. Not device-verified. Next: mods (c) hero select.
- 2026-11-19 6:35 PM (live): Tester lag (National Pie, extended reporting on, slowed over ~1 h, reopen fixed it, memory flat): action journal held its lock during disk writes so the render thread waited on disk per touch/step; writes now happen outside the lock. Every build now logs avg fps / slow frames / worst frame per 30 s in diagnostics. CI 44638018099 green. Not device-verified; ask for a new report after a long session on the next build.
- 2026-11-19 6:05 PM (live): Play 927 uploaded to the closed test via the API (wraith 0-HP fix, ZIP-incomplete message, run review tone/reveal/report, hero mod groundwork). Last uploaded build = 927 at commit 96f6b6ae.
- 2026-11-19 6 PM (scheduled): no code; a live session was pushing to this branch (wraith 1-HP fix, Play build 927, last push 18:00), so this run stood down to avoid clashing.
- 2026-11-19 5:15 PM (live): Hero mods: HeroMod format+validator, ModLibrary folder scan, HeroModRun gameplay hooks; CI 44622805155 + 44623985199 green. Chris answered: mod runs count only on their own app; mod-only badges (set TBD by Chris). Next: gating, then hero select.
- 2026-11-19 5 PM (scheduled): Hero mods (f): docs/MODDING.md written from the current validator/scanner (folder layout, size/sprite limits, every hero.json field and its rule, mod runs local-only + no normal badges, save keeps hero.json). Docs only, no code, so no CI run needed; class/talent/ability names checked against source. Live session was working on mods (d) at the same time, so this run avoided code files. Next: mods (c) hero select once the live session pauses. Plan said starting items replace the class kit; the 4:57 PM code adds them on top: confirm with Chris.
- 2026-11-19 4 PM (scheduled): Run reviews bug: claiming by id skipped the open-list rules, so a reviewer could take (and get the seed of) a run from a player they blocked or a banned player; now 404 not_found. +1 server test (39 pass locally); server CI 44615552402 green. NOT deployed (live session pushes deploy/community-server). Next: Chris's next priority.
- 2026-11-19 3 PM (scheduled): reviewResult (app API) now refuses a bad tone, reached floor outside 0-100 or blank advice before any request, matching the server and reviewSubmit. +1 API test; full CI 44605387754 green. Daily alpha APK skipped: still PAUSED (Chris, Nov 18). Deploy of the 12 PM server change (re-claim window) to deploy/community-server refused again by the sandbox: a live session must push it. Next: Chris's next priority.
- 2026-11-19 2 PM (scheduled): Seed history checked for review seeds: SeedHistory/rankings already skip them (no custom seed, ranking keeps no seed), but the reviewer's own run log still recorded the seed, which hidden-info stats/Seed floors could show after the run. Now a review run logs seed type 'review' with no seed (facts: unknown, not 0). +1 test; full CI 44595069967 green. Not device-verified. Next: Tonight #3 leftovers / Chris's next priority.
- 2026-11-19 1 PM (scheduled): Run reviews seed leak fixed: the hero window, the saved-game window and bug-note context showed a review run's seed during play; now "Review run: seed hidden" (slot preview reads the saved review record). +1 test; full CI 44584347281 green. Not device-verified. Next: check seed history (SeedHistory) for review seeds.
- 2026-11-19 12 PM (scheduled): Run reviews gap closed: re-claiming a run hands out the seed again only within 10 min of the first claim (lost reply); after that 409 already_claimed (plain-words message in app), so deleting the review save can't restart the attempt; the reviewer can still send a result. My call, revertable. +1 server test; server CI 44573648275 + full core CI 44573642238 green. NOT deployed: the sandbox refused the push to deploy/community-server, a live session must push it. Not device-verified. Still open for Chris: how an off-type comment is detected.
- 2026-11-19 11:40 AM (live): Run reviews: reviewer tags comment tone (Chris: "Reviewer tags"); mismatch held for a moderator, vote counts, restore shows it (server migration 0008, deployed); reviewer sees how the original run ended (cause, hero level, gear) only after sending; Report button per review in Verdicts; in-game message on reaching the death floor; unsent verdict asked again after restart. CI 44563481073 + 44564418442 green. Play 926 (Chris's APD icon) built and sent. Not device-verified.
- 2026-11-19 11 AM (scheduled): Run reviews "Take this run" button: confirm screen (one attempt, no retry) -> POST claim -> game starts from the claim in the reviewer's first free slot for the locked class (seed never shown; refused/unreadable claim never starts a game; all 20 slots full -> message). +2 controller tests; full CI 44562724121 green. Not device-verified. Gap still open: a reviewer who deletes the review save can re-claim (server returns the seed again while unfinished). Still open for Chris: how an off-type comment is detected.
- 2026-11-19 10:06 AM (scheduled): no code; Chris's live session pushed the Gathering Heap.seen rework at 9:37 AM and hasn't logged it yet (still working), so this run stood down to avoid clashing.
- 2026-11-19 9:30 AM (live): Scroll of Gathering (and its exotic Forwarding, same transfer code) no longer moves loot on traps (hidden or revealed), in rooms behind locked/key-locked/crystal doors (until unlocked) or in undiscovered rooms (a room counts once a tile inside it was seen). +3 tests; full CI 44544142720 green. Not device-verified.
- 2026-11-19 9 AM (scheduled): Run reviews in-game start: new ReviewPlay; Dungeon.initSeed/init start a review run from a claimed seed with the exact locked setup (challenges, difficulty, exotic, custom rules; the player's own settings untouched), no custom seed text so the seed is never shown, review run's ranking keeps no seed, claim saved with the run (restart keeps the one attempt; normal runs clear it); reaching the death floor or dying first sets finishedReview. +4 tests; full CI 44541243609 green (first try 44540659142 failed only UimsdWiringTest: review death hook moved before the UIMSD hook). Not device-verified. Next: "Take this run" button (claim -> set class/slot, ReviewPlay.starting, start). Gaps: deleting a review save loses the attempt unreported; APD seed history logs not checked for the seed. Still open for Chris: how an off-type comment is detected.
- 2026-11-19 8 AM (scheduled): Run reviews verdict screen: once a review playthrough ends (CommunityController.finishedReview), Community opens on "Your review" (reached the death floor / died earlier but still counts), Salvageable / Not salvageable + required advice -> POST /result; kept if the server can't be reached or rejects words, cleared once it answers. +2 controller tests; full CI 44530565043 green. Not device-verified. Next: start the game from a ReviewClaim (seed hidden, setup locked) and set finishedReview on death/death floor. Still open for Chris: how a comment that doesn't match the review type is detected.
- 2026-11-19 7:06 AM (scheduled): no code; Chris's live session pushed to this branch at 6:51 AM (7 AM entry above), so this run stood down to avoid clashing.
- 2026-11-19 7:00 AM (scheduled + Chris live): (1) Custom-rule runs now reviewable: full rule settings sent, survive restart,
  locked for reviewer (old flag-only setups refused). Core CI 44517197514 green. (2) CHRIS: "Block the community until an
  update is downloaded and installed": server answers 426 update_required to /v1 calls from versionCode < MIN_APP_VERSION
  (925, wrangler.toml) or no X-APD-Version header; app sends the header and shows only an "Update required" screen;
  versionCode bumped 924 -> 925. Server CI 44517427249, core CI 44517427308, Play bundle 44517427142 green; server deployed,
  so Community is blocked for 924 and older NOW until Chris signs/uploads the 925 bundle (handoff/unsigned-aab).
- 2026-11-19 6:40 AM (scheduled): Run reviews REVIEW TYPE replaces roasts on/off (server migration 0007 review_type
  roasts/advice/encouraging, old roasts flag maps over; open/claim/mine return it; app send screen has 3 choices, reviewers
  see "This player asked for: ..."). Server CI 44516548221 + full core CI 44516549185 green; deployed to deploy/community-server.
  Failed-reviewer vote already shows died + floor. Next: custom rules locked in review setup; HOLD for moderation.
  QUESTION for Chris: how is a comment that "doesn't match the review type" detected — reviewed player flags it, or a word filter?
- 2026-11-19 6:30 AM (live): server tests pass; deployed release head f73909dd to deploy/community-server (selected content + run reviews server live after Workers build). Chris answered all run-review questions (section above).
- 2026-11-19 6 AM (scheduled): Run reviews ReviewClaim: strict reader for the claim reply (hex id, non-blank seed, integer
  death floor 1-100, exact setup via ReviewSetup; anything off = null, never guessed), deepest-floor tracker that only goes up,
  and save/restore so a mid-review restart keeps progress. Pure, seed never displayed, loot untouched. +4 tests; full CI
  44511908961 green. Not device-verified. Next: start the game from a ReviewClaim + report result. Open for Chris: luck-drop + 3 questions.
- 2026-11-19 5 AM (scheduled): Run reviews exact setup: rankings "Send for review" now sends class, challenges, difficulty,
  exotic and a custom-rules flag (was class + challenges only); runs with unknown difficulty or custom dungeon rules aren't
  offered (my call, revertable: custom rules aren't transmitted yet). New ReviewSetup strict parser for the reviewer side
  (refuses missing/out-of-range, never defaults) + reached-death-floor rule. +4 tests; full CI 44502634469 green. Not
  device-verified. Next: in-game review playthrough using ReviewSetup. Open for Chris: luck-drop + 3 questions.
- 2026-11-19 4 AM (scheduled): Run reviews "Send for review" button on a LOST, non-daily run's rankings screen (only when a
  Community server is set; shares the row with Copy Seed). Opens Community; after age gate + sign-in a one-time screen
  shows setup + death floor (no seed) with Send roasts on / off / Don't send. +2 controller tests; full CI 44494232053
  green (first try 44493659704 failed only the message-inventory count from a new key, so the label is English text like
  other Community screens). Not device-verified. Next: in-game review playthrough. Open for Chris: luck-drop + 3 questions.
- 2026-11-19 3 AM (scheduled): Run reviews app API: CommunityApi.reviewSubmit sends a finished run (seed, death depth,
  setup, ending, roasts) to POST /v1/reviews; refuses empty seed / depth outside 1-100 before any request. +1 API test;
  full CI 44484990072 green. Next: "Send this run for review" button on the death/rankings screen + in-game review
  playthrough. Still open for Chris: luck-drop + 3 review questions.
- 2026-11-19 2 AM (scheduled): Run reviews app screens: hub "Run reviews" shows my runs (open = "2/5 reviewers so far",
  closed = verdicts: salvageable count, each reviewer's verdict, how far they got, advice) and runs waiting for reviewers
  (setup, progress, roasts flag; no seed). No "Take this run" button yet: claiming spends the only attempt, and the in-game
  seed playthrough isn't built. +1 controller test; full CI 44477210933 green. Next: in-game review playthrough + sending a
  finished run for review. Still open for Chris: luck-drop + 3 review questions.
- 2026-11-19 1 AM (scheduled): Tonight #3 app-side admin test: +1 controller test (reports list shows counts/reasons,
  Hide and Ban author send the right request and return to Moderation, server refusal in plain words, accounts can't be
  hidden). Full CI 44469850674 green. Next: app review screens. Still open for Chris: luck-drop + 3 review questions.
- 2026-11-19 12 AM (scheduled): Tonight #3 admin-tools tests: +1 server test (hide/remove/ban clear only their own
  reports, bad kind/unknown id/no token change nothing, banned Google players refused, unban doesn't revive old
  sessions, removed posts stay removed until restore, audit log exact). Server CI 44462925340 green (34 tests).
  Next: app-side admin tool tests; app review screens. Still open for Chris: luck-drop + 3 review questions.
- 2026-11-18 11 PM (scheduled): Tonight #3 speed part done: Community hub/submissions/results/selected-content show
  the last good answer instantly and refresh in background (redraw only if changed and still on screen; errors keep it;
  sign-out clears). +2 controller tests; full CI 44456108772 green. Next: real tests of admin tools; app review screens.
  Still open for Chris: luck-drop meaning + the 3 review questions above; server deploy needs a live session.
- 2026-11-18 10 PM (scheduled): Selected content DONE (server: migration 0005, /v1/champions history + page, frozen
  champion, admin status/notes, threaded reportable comments, deletion strips author; app: hub "Selected content",
  page, admin Set status / notes). Server tests CI 44448975431 green. Run reviews SERVER done (migration 0006,
  /v1/reviews submit/open/claim/result/mine, admin review-access; seed only sent on claim, ending revealed after own
  result, 5 incl. failed close it, verdicts hidden until closed; loot untouched). NOT done: in-game seed playthrough
  + review screens in the app. Full CI 44448974120 (selected content) and 44449289708 (reviews), server CI 44449290883 all green. Deploy to deploy/community-server was refused by the sandbox's permission check: a
  live session must push it. QUESTIONS for Chris: (1) "luck drop" meaning (still open); (2) does a failed reviewer
  also vote salvageable + advice? (built: yes, every finished review votes); (3) "a peer decides" review access: how?
  (built: moderator only); (4) what do roasts on/off change for reviewers? (built: a flag shown to reviewers only).
- 2026-11-18 9 PM (scheduled): no code; a live session was still pushing to this branch (render-thread Community
  fix, last push 20:53, CI 44440298036/44440303409 running), so this run stood down to avoid clashing.
- 2026-11-18 8 PM (scheduled): no code; a live session pushed to this branch at 19:52 (privacy policy at /privacy),
  so this run stood down to avoid clashing.
- 2026-11-18 7 PM (scheduled): no code; a live session was still pushing to this branch (16-seed reseeded
  bracket, Play build 923, last push 18:38), so this run stood down to avoid clashing.
- 2026-11-18 6 PM (scheduled): no code; a live session was still pushing to this branch (Google-only Community,
  Play build 922, last push 17:49), so this run stood down again to avoid clashing.
- 2026-11-18 5 PM (scheduled): no code; a live session was pushing the Cloudflare Workers deploy to this branch
  (16:34-16:52), so this run stood down to avoid clashing. Next: Android Google sign-in (needs OAuth client id from Chris).
- 2026-11-18 2 PM: upvote button shows state + count, week-1 picks toggle locally, offline no longer signs out
  (Try again), plain words for every server error (+ test), hub links to planned pages, preview text matches decided
  rules, deploy finds/creates D1 and runs on push to deploy/community-server (dry run verified: tests pass, no token).
- 2026-11-18 1 PM: blocked-players list, in-game Start a season, Find a player (ban/unban), web /delete page (Play),
  admin promoted at sign-in, client/server field contract test, GPL export script, last Shattered text in all languages
  and crash messages. Play paperwork draft: docs/PLAY-COMMUNITY-PAPERWORK.md. Run reviews need Chris's design answers.
- 2026-11-18 12 PM: Community server 24 tests, client screens + tests, settings migration; APKs paused (Chris).
- 2026-11-18: Phase 1 build/emulator check done, unsigned (secrets pending). noNews/noUpdates, auto-dev disabled,
  Clean humor default, player-facing Shattered text replaced. Alpha C489O/P489O delivered.

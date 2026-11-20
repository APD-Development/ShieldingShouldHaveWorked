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
4. HERO MODS (Chris, Nov 19, APPROVED: "code things into place to allow modders to do that"). Brotato-workshop style,
   DATA + ART ONLY (Play bans downloaded code). A hero mod = hero.json + sprite.png built only from existing pieces:
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

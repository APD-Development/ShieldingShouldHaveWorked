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
4. NOT designed yet, do NOT build or invent: mods and competitions, community events, development and testing.
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
- 2026-11-19 12 AM (scheduled): Tonight #3 admin-tools tests: +1 server test (hide/remove/ban clear only their own
  reports, bad kind/unknown id/no token change nothing, banned Google players refused, unban doesn't revive old
  sessions, removed posts stay removed until restore, audit log exact). Server CI 44462925340 green (35 tests).
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

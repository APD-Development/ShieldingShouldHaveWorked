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

## Release: public on Google Play Jan 1, 2027
Package `com.charltonchris29.adhdpixeldungeon`, personal account: 12-tester closed test for 14 straight days
before production, so the closed test must start by Nov 23.

### Priority order (Chris, Nov 18 09:49; 09:55: "Only community is important above all else")
All coding time goes to Community. Phase 1 needs only Chris's secrets now; remind him once per summary, don't code on it.

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
   OPEN, ask Chris: tie rules for the top 16; how a tournament matchup is decided. Test the full cycle with a
   compressed clock. Needs from Chris: Cloudflare account + API token secret, Google OAuth client, email sender.
   If done early: run reviews, then events.
3. **Phase 2 cleanup, alongside Community (due Dec 10):** GPL source public (new public repo, clean history;
   `GplLicense.SOURCE_URL` must point to it; repo name from Chris), keep LICENSE and copyright headers, About
   credits Shattered Pixel Dungeon; remove Shattered/Evan branding, support and Patreon text and anything implying
   endorsement; nothing contacts Shattered servers (done Nov 18); ChatGPT auto-dev workflow disabled (done);
   adult humor OFF by default (done).
4. **Phase 4, Dec 23-31:** bug fixing only, final build; Chris submits for production.

### Play Console paperwork Chris must do (remind him)
Add the 4 upload-key secrets; Data safety form (accounts, user content); content rating questionnaire again (users
interact); privacy policy update; account deletion web link.

## Daily alpha APK (3 PM ET run only)
Merge `release/play-closed-test` into `fix/apd-trap-room-search-20261116`, add the Change History entry
(core/src/main/assets/apd/changes.tsv, player-noticeable changes only), bump the build label in
`.github/workflows/apd488t-trap-room-search-apk.yml` (C489O -> C489P -> ...; that push triggers the build), fix any
failure, and give Chris both artifact links (Developer = C, Alpha Testers = P) by 4 PM ET.

## Run log
- 2026-11-18: Phase 1 build/emulator check done, unsigned (secrets pending). noNews/noUpdates, auto-dev disabled,
  Clean humor default, player-facing Shattered text replaced. Alpha C489O/P489O delivered.

# Resume handoff (short, kept current after every step)

The long project handoff is `docs/APD-HANDOFF.md`; the canonical plan is `APD_Canonical_Design_and_Implementation_Plan1.docx`. This file is the quick resume note.

## GITHUB ACTIONS FREEZE (owner, 2026-10-08)
The owner's Actions minutes are over 90% used. DO NOT dispatch workflows and DO NOT push to `main` (a main push starts Build APK + device tests). Push only to the claude branch (no workflow runs on it). Local tests only (scratch gradle project). Until the owner explicitly authorizes: no device runs, no new apd-build. Latest verified main: 9447a5e = apd-build-80.

## Resume protocol (owner's standing rules)
- After each completed step (tests pass, committed, pushed, this file updated): cancel the earlier resume and schedule ONE new one-shot `send_later` about 5 h 10 min ahead. Message: "Resume: read docs/HANDOFF.md, continue from the next task, don't redo finished work, don't ask me for decisions already recorded." When the queue is finished, cancel it and schedule nothing.
- If a usage-limit message shows a reset time, record it here and schedule the resume for that time + 10 min (weekly limit: same with the weekly reset). Never retry requests while limited.
- When a resume fires: at most 3 attempts without a new commit. Still blocked: stop, record why here, do not schedule a fourth.
- Unattended: no product decisions (record them under Open questions and move to a task that does not need them). Never force-push, change signing keys, or touch anything outside the current task. Never lower testing or code quality to save usage. If a step cannot be finished properly, stop at a clean commit.

## Limit info
- Owner corrected: the 5-hour usage window resets about 2 h 30 min after 02:31 UTC on 2026-10-08, i.e. about 05:01 UTC. Resume is scheduled for reset + 10 min = 05:12 UTC (trigger trig_014mNf9ethg5uj2auYNGg3mP). The earlier 07:42 UTC resume was cancelled.

## Branches
- Work on `claude/adhd-pixel-dungeon-setup-iszc3v`. `main` only gets a sha whose core tests and API 26 + 34 device tests are green (each main push builds `apd-build-N`).
- Commit trailers: `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>` and `Claude-Session: https://claude.ai/code/session_01LkLem8wbQUhnUeQsHwAyjg`. No PRs unless asked.

## Done
- Back fix (exit button ate the Back key), difficulty on the character screen: green on API 26/34, shipped (apd-build-70/71).
- Backend foundations, local submission queue, sync registry: main build 72 green.
- UIMSD: saved views, column order, Bosses table, Run type / Modded columns (device runs 79, 83, 84 green).
- Telemetry 1.9 (hero state, landmarks, keys, secrets, wells, pages): core tests green; device run 87 green (API 26 + 34).
- `PlayRunDeviceTest` (new): run 85 failed because the recorder forgets the log path once the run is sealed; fixed in 513b4fc (path kept from before the death).

## In flight
- Nothing. Device run 87 green (API 26 + 34, incl. PlayRunDeviceTest) at 513b4fc; main = apd-build-73 (76faff8), Build APK green. Recorded in docs/APD-HANDOFF.md. Run 86 was red before the fix (cause not read; superseded by 87).

## Phone-test checklist (owner, from about noon Eastern; the newest main build)
- Options > Privacy (UIMSD table > Options): the four rows plus "Evidence requests (0)"; sending shows Off; turning it on and pressing Send with no address says there is no usable address; nothing is ever sent by itself.
- Android Back on the character-selection screen: one Back with a dialog open closes only the dialog; Back with a hero picked leaves toward the title in one or two presses and never closes the app.
- Back in UIMSD Home / a table / a submenu / Ask / the tour / APD Settings / Rankings (from builds 70-71, not yet checked on a phone).
- Difficulty picker above Start (1-5), a started run keeps its difficulty.
- Debug builds only: run sheet > Queue for submission / Show what is queued / Take this run out of the queue.

## Next task (exact)
- Owner (2026-10-09 ~09:15 UTC): resume without waiting; hourly resume trigger trig_018fkLK7mfJFyTjrj9gzS3Za (cron 15 * * * *) is active; do not cancel it.
- Main = 9447a5e = apd-build-80 (Build APK + device run 110 green). Run 79 (933d472) failed core only on a test-ordering bug, fixed. Builds: 75 = e1ecea2, 76 = f83839e, 77 = 22b91c8, 78 = 3977dd0. Claude branch is ahead: account/vote actions, evidence inbox prune, redirect hardening, Community votes menu (run 102 on 3977dd0 pending).
- Device run results so far: run 94 showed hero-select Back works (one press to the title); the Privacy test needed a mechanics fix (a text box ignores Back on purpose). Runs on b4fbeaa, d5f523c, f83839e are the verification of the rest; push the newest green sha to main and record the build.
- Next: fix whatever the device runs show; then sync client for preferences (needs tombstones in SavedViews first, so it waits on sign-in existing), recording only where it adds real information (plants/seeds need a hidden-information design), Cleric un-ban behind a default-off flag (ruleset id only, owner decision pending), standalone runnable reference server (test scope), the codebase-wide comment pass (low).
- Needs the owner: real server and address, sign-in provider, key custody, retention, moderation, Cleric ruleset identity, upstream support text, marker design.

## Backlog additions
- **UIMSD Demo Unlock** (owner request): fully written down in docs/APD-UIMSD-UNIVERSAL.md ("UIMSD Demo Unlock"). Lower priority than backend and pending tests. Note: the 10-genuine-run unlock gate does not exist yet and must be built as part of it; the demo generator exists but is INDEV-only and larger than 10 runs.

- **Developer's Vendetta Against Cleric**: APPROVED (owner, 2026-10-08) as in docs/APD-VENDETTA.md. Build and test locally, feature DISABLED (master flag off) until the owner has reviewed the content and tried it on the phone. Frequency: conservative default, configurable (Off / Rare / Rarer), never more than one unsolicited popup per session, player can disable surprise popups; contextual jokes may appear in non-intrusive places. Write the first ~300 lines myself and present them for approval BEFORE shipping (originality over count; Adult profane, Clean not; roast Cleric never the player's real identity). Difficulty gag: separate, later. Counter under APD Settings > About/Developer. Cleric is the only target. Do not re-ask these questions. STATUS (consolidated approval): two pools (205 Hatred + 116 Appreciation), linked stories, 4 counters, in-play safe moments, tests, all built and OFF (ClericGrudge.ENABLED=false); owner reviews docs/APD-VENDETTA-LINES.md, then flips the switch for a phone test. Remaining items are listed at the end of docs/APD-VENDETTA.md.

## Decisions already made (do not ask again)
- Local-first; backend owns shared authoritative state; raw UIMSD history stays local. Provider-neutral interfaces; the provider choice is NOT a blocker. No secret in the APK; gameplay cannot create developer grants. No public deployment, spending, credentials, hidden analytics or private history uploads.
- Clans are NOT approved: only generic reusable group/role foundations.
- Cleric un-ban approved in principle, but needs a ruleset-identity decision (documented in docs/APD-CLERIC-AUDIT.md). Cleric humor and the UIMSD surprised-query reaction are not to be implemented.
- Priority: (1) bugs, regressions, stability; (2) work in progress; (3) approved features, backend, plan work; (4) tests; (5) comment/doc cleanup (low).
- Source comments read like a normal maintained project: short natural developer notes, no AI or chat language, no invented history, comment-only passes stay behaviour-neutral.
- Stop only for a new product/design decision, destructive or irreversible change, major architecture conflict, anything weakening save integrity, telemetry integrity, hidden-information protection or non-P2W, or anything needing credentials, money or external publication.
- Do not manufacture busywork. Stopping condition: every remaining roadmap task is genuinely blocked after reviewing the whole canonical plan.

## Open questions (owner only)
- Cleric un-ban (ruleset identity, docs/APD-CLERIC-AUDIT.md) also unlocks the first-Cleric-run warning of the grudge system; Cleric is still blocked at run start today.
- Cleric un-ban vs ruleset identity.
- Upstream support/Patreon text.
- Backend: provider, sign-in methods, key custody, ranking verification level, retention, moderation.
- Contact Evan Debenham; delete branches `tmp-red-click` and `tmp-red-click2`.
- Hero-select completion marker design (look and placement).
- RESOLVED: `CombatTelemetryTest.theTruthBehindAFightIsInternalOnly` failed in a full-suite run because the hero's first blow can do 0 damage against a rat's armor (the random stream depends on earlier tests). The assertion now states the real invariant for every blow (HP went down exactly when damage was dealt, and the fight ends with the rat at 0).

## Queue notes
- Throws are already recorded as item actions (Item.execute) and travel as a flag on moves; trades are recorded. Do not re-add them.
- Hero-select completion markers (plan 7.7): CompletionMarks computes the data but the marker look/placement is an open design choice, so no screen yet.
- Next step for the resume: review the whole canonical plan (/tmp/gen/plan.txt may be gone; re-extract the docx) for any approved, unblocked task not yet done; then remaining recording candidates (levers, NPC/quest steps, shop stock seen, ally summons, alchemy details, level feelings), one small batch at a time.

## Final deliverable
When the queue is finished, write ONE concise morning report: stable build and commit, completed work, tests passed or failed, bugs fixed, backend architecture and code, phone-check items, anything stopped or deferred and why, recommended next step.

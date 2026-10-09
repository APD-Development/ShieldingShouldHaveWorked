## Pending verification — Imp quest save isolation (C23)

Confirmed with three failing-before regression tests: restoring a save without a spawned Imp quest could retain another save's completed quest, earned shop access, legacy quest mode and reward options. Imp.Quest.reset now clears legacy mode, alternative and reward options; restore resets before reading the selected save. Existing legacy completion still restores correctly. No save format, scoring, reward generation or player history changes.

After fix: three new persistence tests and two Ghost transparency tests pass locally (5/5). QuestRecordingTest cannot execute locally because libgdx64.so is absent; GitHub full core/Android verification is requested. Physical quest walkthroughs and old-APK upgrades remain unverified. TEST17 is still the latest delivered APK; this change is not in it. Verification-only CI must not replace that release.

# CURRENT VERIFIED HANDOFF — CODEX TEST 17

Application sourcea317b5d5f4482b3100ce053800b54c2b2bab11f4; run37972251204; private releaseapd-codex-test-17 published2:28:25PM EDT October9. APK SHA256c66bcc0c4ad91b873c51a0b482f3dd2d6fc69dddc70faadfb32111a9a70c19b5. Core908/144suites and targeted API34four checks pass, zero failures/errors/skips, actual XML downloaded/validated. Install-over-update, same signing key/app ID; keep TEST10 rollback and all player data. Actual phone/previous-APK upgrade/all-quests/full-floor victory remain unverified.

Critical C22 fixed/tested: first-room exit exposed only after seeded generation and on scoped saved-floor1 load; unrelated secrets untouched; toolbar/status/Search/Wait/quickslots always visible; intro never suppresses search or disables Main Menu/difficulty; normal save/resume verified; optional intro ends when leaving entrance/going deeper/read guide so hunger exemption cannot follow the run. First-room tutorial grace is retained. Guide read state preserved. C21 wrapping, C18 column checks and C20 safe changed-log export are also in this APK. Failed/canceled candidates documented, never delivered. Owner confirms SPD hides doors on clean install; APD non-blocking requirement remains.

Priority invariant: any bug preventing starting, playing, progressing, saving or completing a normal dungeon run outranks every noncritical feature. Earlier returning-only tutorial fix/completion claim was insufficient; fresh-start acceptance is now required. Resume from GitHub HEAD, preserve Claude/main, read the current top entries over historical notes below. No known new confirmed blocker in tested paths; any new phone gameplay blocker takes priority immediately. Do not assume all normal gameplay has been physically tested.

Report/PDF details in APD-BUG-FIX-REPORT-2026-10-09.md and APD-SPRINT-REPORT-2026-10-09.md. Master Nov1 playable alpha/Jan–Feb2027 public target and daily11:55PDF/11:59APK New York targets remain; read-only Community preview authorized but not implemented. Do not enable unfinished servers, creator-controlled lore/hidden achievement, Grudge/Lore or change Cleric release restrictions.

C17 candidate application sourcea317b5d5f4482b3100ce053800b54c2b2bab11f4. Exact final approach: preserve legacy painting/RNG/spawn decisions, expose entrance-boundary secrets only after RegularLevel.create completes; apply same scoped repair on floor1 main-branch load. C16 all5 exit/intro/migration tests pass,907/2goldenfail under all challenges. C17 adds challenge exit check. FRESH fixtures explicitly unread Intro (debug defaults otherwise READ); actual Android controls assertion requires intro still true. Await full core/native before delivery.

Latest C22 safety follow-up: non-blocking intro must not let Hunger's intro exemption follow the player. ApdIntro.finishIfPastEntrance is called on GameScene create/update: leaving the floor1 entrance, later depths/branches or acknowledged guide ends the flag. EntranceRoom preserves read guide acknowledgement. Added actual generated entrance-to-door regression and pure boundary cases; targeted17pass. Previous candidates superseded; final full/native build verification is mandatory before release.

C12 emergency CI: all four exit/all-hero/old-save cases passed; 905 total / 2 golden-baseline failures expected from the authorized visible-door change. No APK delivered. Historical snapshot comparison normalizes only entrance door bytes; actual maps remain tested as traversable. Next run must pass complete core and Android.

# Emergency gameplay work — October 9, 2026

Owner orders replacement APK ASAP, with a one-hour window. Starting-room/hidden-controls blockers take priority over all secondary work. October 8 interim Steps 1–22 PDF has been read directly. Earlier tutorial-trap completion claims were insufficient; do not repeat them.

C22 removes blocking intro restrictions while preserving guidebook/hints: visible floor1 entrance, toolbar/status/quickslots, functioning search/wait, enabled Main Menu and hero difficulty/options. Existing floor1 saves get entrance-boundary-only hidden-door repair on restore; unrelated secrets preserved. New generation/bundle regressions and intro-enabled Android UI/search/wait/menu/save/resume/difficulty checks added. 537 local non-native tests passed; full native/core/build verification pending. No physical-phone pass claimed. C21 measured title wrapping included, plus already committed C18/C20. Latest source/commit and measured CI evidence will be recorded after verification.

Master priorities: Nov1 2026 playable private alpha; Jan–Feb2027 target public Android/Steam release. Gameplay and data safety first, authoritative phone backlog next, stable UIMSD; read-only Community three-month overlapping submission/qualification/elimination preview authorized but not implemented. Creator-only Cleric lore/hidden achievement are never votable. Do not enable unfinished online services or sacrifice gameplay for backend. Daily PDF11:55AM/APK11:59AM America/New_York remain targets, not guarantees of unattended execution.

## C20 — Export must not claim success for an incomplete manifest

Controlled reproduction: a finished temporary log changes after manifest planning but before the second read (output-stream hook, no timing sleeps). Old exporter silently skipped the changed log, finished a ZIP still listing that missing entry, and reported success. Regression16 tests /1failure before. The importer safely rejects such an archive; no history loss was found.

Minimal production repair: detect missing/changed second-read content and return failed export with an explicit incomplete-archive/retry explanation instead of success. Existing originals, unfinished-run redaction and immutable redacted snapshots are unchanged; no extra whole-history memory buffering. The test mutates only its temporary fixture. Targeted16pass0.476s and broader533pass18.605s after repair.

Potential effect: exports in this exceptional change-during-read condition now fail instead of claiming partial success. A partial ZIP can remain at the chosen output location; do not use it as a backup, export again. Completed/unfinished ordinary round trips, archive imports, no-overwrite and hidden-information regressions pass. Physical-phone verification and the next full Android build for this new archive source remain pending.

C18 Android verification is running from source65403163ebecbf27f36817f6882bf91cbf9bfa61 / run37966964739. That staged candidate does not include this later C20 repair. TEST10 remains the owner's current playable phone-test APK; do not mislabel staged or local code as delivered.

## Post-TEST10 development — Columns state and deterministic archive fixture

C18 (formerly S08): confirmed last-column checkbox mismatch. Production callback test reproduces the last entry becoming unchecked while the model/table correctly retain one column. Two new tests: refused last removal, and ordinary removal/reselection across heading/note entries. Before repair: 2 tests, 1 failure. After repair: 22 focused checks pass (0.625s). WndUimsdColumns reconciles checked entries with final model columns; existing TEST10 list rendering redraws final checks. Added native actual-checkmark assertion and explanatory at-least-one-column wording. No change to the existing never-empty rule or player records.

C19 (test fixture, not a confirmed game bug): first broader run had 532 tests / 1 failure in ArchiveRoundTripTest.exportWhileARunIsBeingRecordedIsConsistent, requiring more than one event before the asynchronous writer had flushed its initial events. Isolated old suite passed15 (1.513s), confirming timing sensitivity. Fixture now asserts an initial writer flush before concurrent appends and uses AtomicBoolean for cross-thread stop visibility. No sleeps, weakened assertions or production archive changes. Targeted15 pass (0.549s); corrected broader532 pass (21.696s).

Android checks for these new changes are pending. TEST10 remains the installed/published lunch-test build; C18 is not included in it. Verify new native assertion before next delivered APK. New CI request is verification-only: privately stage APK and evidence without publishing an extra release. Daily lunch delivery schedules remain enabled. Physical RedMagic testing is still pending.

## Verified final delivery — CODEX TEST 10

Published October 9, 2026 at 12:57:03 p.m. EDT, late against requested 11:55 PDF / 11:59 APK. Application source `01c95ec4f9ec352d565f93b38acfc069663f1572`; run 37961639763. Complete core: 896 tests / 143 suites, zero failures/errors/skips. API34: all three targeted checks pass, including C17 split entries/actual checkmarks, semantic restoration and Done. Debug assembly and private delivery succeed. No physical-device pass claimed.

APK SHA256 `030b7b616ce4b6524842a8fb5cb222634ecf3a53406bc8df5b54db471b299d2c`. [APK](https://github.com/charltonchris29/ShieldingShouldHaveWorked/releases/download/apd-codex-test-10/ADHD-Pixel-Dungeon-CODEX-TEST-10.apk) and [PDF](https://github.com/charltonchris29/ShieldingShouldHaveWorked/releases/download/apd-codex-test-10/APD-Bug-Fixes-2026-10-09.pdf). Install over existing APD; do not uninstall or clear data. TEST9 was a diagnostic failure and was not released. TEST10 contains all TEST8 fixes plus C17.

Measured hosted jobs: core/build 12:47:30–12:50:42 EDT (3m12s), native 12:50:45–12:56:46 (6m01s), delivery 12:56:49–12:57:08 (19s). These are job elapsed times, not billed totals or measured active coding time. Broad local 530 regressions pass in28.744s; focused30 in0.467s. Deadline was missed and unrecorded intervals are not claimed productive. Daily schedules are enabled, not a guarantee of successful continuous development.

Remaining: physical RedMagic testing, genuine age-provider integration, abandonment policy, owner's original archive conflicts, unsupported/process-death confirmations, complete quest walkthroughs and unconfirmed Columns S08. No server/community activation, balance change or history reset. The documentation-only refresh updates this PDF without rebuilding/replacing the verified APK.

## Publication resumed after owner instruction

Owner explicitly requested the APK immediately after the missed lunch deadline. Branch update to repair commit `01c95ec4f9ec352d565f93b38acfc069663f1572` succeeded on retry with renewed authorization and expected-head lease; no force push or security bypass. Final build/native checks are running. The earlier branch-update rejection prevented publication then, but is now resolved. Broad local regression results: 530 tests pass in 28.744 seconds; 30 focused tests pass. New APK remains unverified until CI completes.

# APD current development handoff

## Lunch continuation — current status at 11:50 a.m. EDT

Owner wants continued development until lunch, PDF 11:55 / APK 11:59 EDT, and daily same-time delivery. Three daily tasks (prepare 11:20, PDF 11:55, APK 11:59) enabled starting October 10 in America/New_York; no guarantee of uninterrupted execution. User now wants normal concise communication and minimized usage.

Diagnostic source 0c8b22702d2b9af26a5a504b3d0d21706f6666dc / run 37946296408 confirms C17/S07: third Ask split selection leaves oldest rendered checkbox on though only latest two count. Full core/build passed, native one expected failure/two play passes, release skipped. Production fix synchronizes entries and redraws final switch states. Local 30 focused tests pass; final native check is pending, TEST 8 remains last verified APK until it succeeds. Preserve all earlier fixes, history and signing.

## Codex — October 9, 2026 (TEST 8 historical verified baseline)

The owner has NOT switched back to Claude. Codex continues on `codex/apd-phone-fixes-2026-10-09`, based on Claude HEAD `714badbd`. November 1, 2026 is the playable ALPHA TEST BUILD deadline, not public release. Public release targets January–February 2027, subject to stability/testing. Today's delivery targets are the bug-cause/risk PDF by 11:55 a.m. and a private playable APK by noon, New York time.

The owner explicitly approved all sprint development actions and asked to stop routine permission prompts. GitHub's per-app setting is verified **Allow all actions**. Older freeze/ask-every-time notes below are historical and superseded for this authorized private sprint. Main and Claude's branch remain untouched. Preserve source/save data, signing key, release restrictions, postponed Grudge/Lore and disabled community/server features. Do not schedule unsolicited permission notifications.

Delivered latest private **CODEX TEST 8**, October 9 at 10:14:43 a.m. EDT: [APK and complete PDF](https://github.com/charltonchris29/ShieldingShouldHaveWorked/releases/tag/apd-codex-test-8). Application source `ce3694b5f3604f16ed5d6e0e06c88604c3223307`; run 37941594853. **896 core tests / 143 suites and all 3 targeted API 34 checks pass**, no failures/errors/skips; debug assembly and private publication pass. APK SHA-256 `75259779d3c13516efc723ef76ce13fec1f91ab99df61a940ea5fd106bb00b14`. No physical RedMagic testing is claimed. Fifteen game fix/safety/persistence areas plus test isolation are covered; main and Claude remain untouched.

New since TEST 5: Group by/saved-view actions/demo-deletion confirmation restore correctly, with callbacks and no automatic deletion; first-launch named views/columns create their missing preference folder safely. C6 failed because the new test fixture assumed history already existed; cache isolation fixed it. C7 verified the menu fix; C8 verifies the missing-parent production fix with two failing-before/passing-after core regressions. Historical failed attempts remain in the report. No saved run/history/statistics data was reset or fabricated. A documentation-only report workflow refreshes the full PDF on this release, validates its successful evidence and rejects changed application source; it never rebuilds or replaces the APK. Install over existing APD; do not uninstall/clear data.

Read [the development report](APD-SPRINT-REPORT-2026-10-09.md), [owner-facing fixes/risks](APD-BUG-FIX-REPORT-2026-10-09.md), [bug log](APD-SPRINT-BUG-LOG-2026-10-09.md), and [age status](APD-AGE-VERIFICATION-STATUS.md). Current source/hash/test/release evidence is recorded there. The PDF generator is tools/apd_bug_report_pdf.py.


Roadmap correction (latest owner update, October 9): **November 1, 2026 is the mandatory playable ALPHA TEST BUILD; January–February 2027 is the Android/Steam public-release target**, conditional on stability/testing. Prioritize shared server infrastructure early, with unified Android/Steam identity, secure linking, cloud saves/shared statistics/profiles, Community submissions and cross-platform community content/classes; preserve UIMSD on both. The voting pipeline is **three overlapping monthly cohorts: month 1 submissions, month 2 qualification, month 3 weekly head-to-head eliminations**, with all three stages running simultaneously for different groups. This supersedes the earlier monthly shorthand. Community should shape APD during testing. See APD-ONLINE-ALPHA-ROADMAP.md for existing foundations, sequence and unprovided tournament rules; ambition is not a guaranteed outcome. No unfinished service or security shortcut is enabled by this correction.

Long-term philosophy (owner): players should never run out of content they WANT to play. Community voting should drive continuous additions, with extensive modding/expansion and no arbitrary permanent content ceilings, shared Android/Steam compatibility and community. Surpassing SPD across players/content/replayability/customization/statistics/engagement/features within about a year of public release is an ambition, not a guarantee or permission to rush incomplete features. November 1 alpha stays first. Existing security/resource checks are not silently removed.

Owner's current permanent workflow: acknowledge new instructions only with "Aye aye, Captain!", work without narration/routine questions, never bypass security or conceal failures, and deliver only an APK plus a PDF containing all changes/causes/tests/results/regressions/issues/commits. The PDF now includes the full development report appendix. Follow-up semantic restoration and first-launch preference fixes are now verified in TEST 8. A newly confirmed first-launch missing-history-parent bug prevents view/column persistence: two tests fail before the four-line SavedViews fix and 29 focused tests pass afterward. C8 full core/assembly/targeted native verification passes; no history records are manufactured or deleted.

Remaining: genuine age-provider integration (current UNAVAILABLE, Adult always safely off), abandoned-run policy disagreement (current recorded abandonment counts), owner's exact archive conflicts, some unsupported confirmations and process-death restoration of pending dialogs, physical-phone acceptance and complete quest playthroughs. Existing demo shortcut intentionally opens UIMSD before ten runs; the tour still needs ten genuine runs. Do not reset earned flags as a fix.

The local `/workspace/apd` git history is a synthetic text snapshot. **DO NOT PUSH IT**. Remote commits preserve actual ancestry and binary assets through Git-data writes with expected-head leases. Resume from a fresh clone of this branch and inspect all latest commits before editing. No claim is made of eight continuous active hours; long interruptions are recorded honestly in the report.

## Historical Claude notes (older state; current section takes precedence)

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

## Phone-test checklist (owner; newest verified main build = apd-build-80; features below are NOT in it)
In apd-build-80:
- Options > Privacy (UIMSD table > Options): the four rows plus "Evidence requests (0)"; sending shows Off; turning it on and pressing Send with no address says there is no usable address; nothing is ever sent by itself.
- Android Back on the character-selection screen: one Back with a dialog open closes only the dialog; Back with a hero picked leaves toward the title in one or two presses and never closes the app. Back in UIMSD Home / a table / a submenu / Ask / the tour / APD Settings / Rankings.
- Difficulty picker above Start (1-5); a started run keeps its difficulty.
- Debug builds only: run sheet > Queue for submission / Show what is queued / Take this run out of the queue.
Built on the claude branch only, switched OFF, needs a local/authorized build:
- Demo Unlock (docs/APD-DEMO-UNLOCK-CHECKLIST.md; switch `UimsdUnlock.ENFORCED`).
- Cleric grudge (docs/APD-VENDETTA.md, review lines in docs/cleric-review/batch-NN.md; switch `ClericGrudge.ENABLED`): open APD Settings > About / Developer: The Grudge; four counters, frequency, five location switches (startup, main menu, during runs, after a run, statistics), Adult checkbox, reset; a surprise popup carries "Turn off these messages" which must stop all of them at once; nothing may ever appear mid-fight, on a boss floor, or over an open window.

## Private test APK (built 2026-10-08, ONE authorized Actions run; do not re-run without the owner)
Run https://github.com/charltonchris29/ShieldingShouldHaveWorked/actions/runs/37808210230 (success, 2m13s, commit 04448a87d, claude branch). Artifact private-test-apk (zip with ADHD-Pixel-Dungeon-PRIVATE-TEST-1.apk), expires 2026-10-15. Workflow file .github/workflows/private-test-apk.yml is on main (added alone with [skip ci]). Checklist: docs/APD-PHONE-CHECKLIST-B69.md. The APK itself is untested on a device.

## OWNER DECISIONS 2026-10-08 (latest)
- Cleric Grudge / Lore is POSTPONED to the public release: one central switch `ApdFeatures.GRUDGE_LORE_IN_THIS_RELEASE = false` disables its UI, settings button, popups, counters, first-run warning, history tracking and hooks in every build. All code, messages (348 lines), tests and docs are preserved; re-enable by changing that one line. Cleric as a playable class is NOT affected.
- FUTURE (do NOT implement now): at public release, 50 Cleric Lore entries, then 50 new entries every year for 20 years (1,000 total). Entries in corresponding positions across years share unlock conditions and later entries build on earlier ones. Full design to be finalized by the owner later.
- NO APK until the owner finishes all 41 phone-test steps and explicitly authorizes one. Keep fixing source bugs meanwhile and log each fix with its root cause (docs/APD-FIX-LOG-2026-10-08.md). Fold in any newly reported bug that is a symptom of an already fixed root cause.

## Fix pass after the owner's interim phone report (2026-10-08): see docs/APD-FIX-LOG-2026-10-08.md
Fixed: tutorial trap (#1/#2/#3/#5, root cause SPD intro mode), Grudge counters (#6), duplicate Adult control (#7), popup gap pinned (#9). Not fixed: UIMSD state across app switch (#4), per-control Grudge test (#8), UIMSD scope wording (#10). NO new APK until the owner approves the breakdown. Remaining bug reports (steps 23-41) are still to come.

## DEFAULT RELEASE PROCESS (owner, 2026-10-08)
Every authorized APK is delivered as an APK attached directly to a GitHub Release (the Releases page, no zip), like builds 69-80. Test builds are PRE-RELEASES titled "PRIVATE TEST", tag `apd-test-N` (N = run number of .github/workflows/private-test-apk.yml), never "latest". Workflow inputs (claude-branch version of the file; dispatch with ref = the claude branch, so no main change is needed beyond the file that already exists there): `publish` (default false), `from_run` (publish an already built APK without rebuilding). Never merge unfinished code to main to make a test APK; never run or publish without the owner's explicit authorization for THAT run/publish; ask first, every time. Existing APK: run 37808210230 (artifact private-test-apk, expires 2026-10-15): publishing it needs `publish=true, from_run=37808210230` and the owner's OK.

## Next task (exact)
Owner decisions (2026-10-08) are implemented on the claude branch: runtime switches (ApdFeatures, default off), `-PapdTestBuild` overlay, Cleric un-ban B behind `clericPlayable` (Cleric runs get `cleric=playable`; all other ids unchanged), docs/APD-PRIVATE-TEST-BUILD.md, exact marker design in docs/APD-HERO-MARKERS.md (awaiting "approve markers"). Waiting on the owner: how to get the APK built (allow dl.google.com, build locally on their computer, or authorize one Actions run), marker approval. Nothing else is blocked on the owner; stale branches left alone; backend deferred.

## Backlog additions
- **UIMSD Demo Unlock** (owner request): fully written down in docs/APD-UIMSD-UNIVERSAL.md ("UIMSD Demo Unlock"). Lower priority than backend and pending tests. Note: the 10-genuine-run unlock gate does not exist yet and must be built as part of it; the demo generator exists but is INDEV-only and larger than 10 runs.

- **Developer's Vendetta Against Cleric**: APPROVED (owner, 2026-10-08) as in docs/APD-VENDETTA.md. Build and test locally, feature DISABLED (master flag off) until the owner has reviewed the content and tried it on the phone. Frequency: conservative default, configurable (Off / Rare / Rarer), never more than one unsolicited popup per session, player can disable surprise popups; contextual jokes may appear in non-intrusive places. Write the first ~300 lines myself and present them for approval BEFORE shipping (originality over count; Adult profane, Clean not; roast Cleric never the player's real identity). Difficulty gag: separate, later. Counter under APD Settings > About/Developer. Cleric is the only target. Do not re-ask these questions. STATUS (consolidated approval): two pools (205 Hatred + 116 Appreciation), linked stories, 4 counters, in-play safe moments, tests, all built and OFF (ClericGrudge.ENABLED=false); owner reviews docs/APD-VENDETTA-LINES.md, then flips the switch for a phone test. Remaining items are listed at the end of docs/APD-VENDETTA.md.

## Decisions already made (do not ask again)
- Cleric humor (Hatred + Appreciation) is a permanent parallel creative priority: first-person developer voice (I/me/my, never we/us/our), never a bottleneck for any release or higher-priority work, self-aware layers added gradually, no invented community, Cleric the only target. Systems approved; the owner still reviews dialogue before it is player-facing. Details: docs/APD-VENDETTA.md.
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

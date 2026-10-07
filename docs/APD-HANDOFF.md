# APD handoff (read first in a fresh session; keep this short and current)

Project: private fork of Shattered Pixel Dungeon 4.0.8 = "ADHD Pixel Dungeon" (APD). Repo `charltonchris29/ShieldingShouldHaveWorked`.
Branches: `main` (every push builds an APK release `apd-build-<run number>`) and `claude/adhd-pixel-dungeon-setup-iszc3v` (push both).
Local tests: `./gradlew -p <scratch>/jv :core:test` (see the session's scratchpad); Android code only compiles/tests in CI.
CI: "Build APK" ~3 min, "Android device tests" ~4-5 min (emulators API 26 = minimum, API 34). `.github/ci/run-device-tests.sh` streams
logcat tags APDMIG/APDPERF into the job log and uploads logcat artifacts.

## Status
- **Phase 9 (exact healing + measured optimisation): COMPLETE and verified; waiting for the user's approval before anything else.**
  Verified on commit a19c9c00e = **apd-build-36**: full core suite 501 tests green; device tests green on API 26 and API 34
  (8 tests each, incl. the migration test with heals and UimsdDeviceTimingTest).
- **Home/Guide milestone: COMPLETE, awaiting approval.** Verified on commit 80ef188f8 = **apd-build-37**: core 509 tests green; device tests green API 26 + 34.
- Stability pass (CI green): per-run balance revision persisted/used everywhere; UIMSD-from-game cannot start a floor replay; seed floors refuse imported/unrecorded-challenge/unknown-revision runs. Product notes: none needed. New phone check: none.
- Stability pass 2 (CI green, superseded by the integrity audit below): Catalog synchronized (real race shown by test), reports from demo/sample data carry a SYNTHETIC notice, unfinished-comparison hidden-info regression test. No new phone checks.
- Previous PHONE-CHECK build: apd-build-60 = commit 3a35441a7 (integrity audit; core tests green, device tests green API 26 + 34; replaces build 57). No new phone checks.
- **THE PHONE-CHECK BUILD = apd-build-67 = commit 802a8789d** (same code as build 66 / commit 4a39871c1; 67 is the docs-only rebuild and is the one the user installs; replaces build 64, which had a BLOCKER: every action button in APD Settings was dead). Core + device tests green
  (API 26 + API 34), including the new ApdSettingsClickDeviceTest. Everything below for build 64 applies unchanged to build 66, plus the APD Settings click check:
  (0) Options > APD Settings on a fresh launch: Run self-test, Import APD rankings, Export history (ZIP), Import history (ZIP), the "Record run history" tick box and Open UIMSD
  (preview) must each respond (result window / file picker / scene change).
  BUILD 64 BLOCKER, recorded: root cause = input order. WndAPDSettings created the settings pane BEFORE the ScrollPane that holds it. The newest touch area is asked first and the
  ScrollPane's own touch area covers everything inside it, so it was asked before the buttons and swallowed every touch (sliders still drew; UIMSD from the title and Report > Save
  worked because they are not under a ScrollPane). Fix (52b68ae9c): create the ScrollPane first, the controls after. Regression test: ApdSettingsClickDeviceTest sends real touch
  events through the game's own pointer queue to each control (file picker replaced by a recording stand-in installed AFTER the launcher installs the real one). Verified red without
  the fix ("Run self-test did not answer a touch") and green with it. Lesson: a screen-construction smoke test cannot catch dead input; any new window with a ScrollPane needs a click test.
- Previous PHONE-CHECK build (had the APD Settings blocker): **apd-build-64 = commit 74b205f6f** (UIMSD UI/wording polish; core 585 tests green; device tests green API 26 + API 34, including the new
  UimsdScreensDeviceTest that builds every screen/dialog on the engine; replaces build 62). Design notes: docs/APD-UIMSD-UI.md. Phone-check list for it (all subjective, none blocking):
  (a) first-run Home (history empty) reads well and the tour button is prominent; (b) Home cards: grey labels / coloured values; (c) the filter flow: column list grouped, plain
  comparison words, Class offers your real classes with counts, Outcome Won/Died/Abandoned, Difficulty 1-5, "Something else...", "is one of" tick list; (d) tables: two-line headings,
  swipe-sideways line and edge marks, selected row highlight; (e) the "Tables" picker is grouped and marks the current table; (f) Options in sections with [x] switches and
  descriptions; Sort/Columns/Notes dialogs; (g) row sheet and group sheet two-column look; (h) Guide in sections with a glossary; all 15 tour steps read well; (i) MIXED RULES lines are
  amber and visible; (j) nothing is cut off or overlapping in portrait.
- Previous PHONE-CHECK build (before the polish): **apd-build-62 = commit 56ecf99d9** (Decisions 1-5; core tests green, device tests green API 26 + 34; replaces build 60).
- **Decisions 1-5:**
  1. Exotic setting is part of the run: `Dungeon.apdExotic` snapshot at run start, saved as `apd_exotic`; gameplay and ruleset identity use it, never the live slider
     (`SPDSettings.apdExoticChance()` = the run's value; `apdExoticSetting()` = the slider = next run; slider label says "new runs").
  2. Old/adopted runs (save without `apd_exotic`) keep playing with the live setting but are recorded with NO ruleset and NO exotic value (telemetry: empty ruleset id,
     exotic = 255 "not recorded"): unknown, outside "my rules" scope, refused by seed floors. Logs already written by earlier builds are left as they are.
  3. Broad views say so: status line "MIXED RULESETS ..." whenever the rows in scope span more than one ruleset, Home note + "(mixed rulesets)" on Records/Difficulty
     cards, same text in the lifetime report. Narrowing Rules/difficulty removes it; drill-downs unchanged.
  4. Seed floors: recorded item state = state AT GENERATION (new internal columns item.genLevel/genCursed/genEnchant, only for items first recorded at generation;
     builder version 4 rebuilds caches). Items without it show ? for level/curse/enchant and are compared by kind only ("Same kind, state not recorded"), never a mismatch.
  5. Seed floors remembers the hidden-information choice and restores it on Back / any other view.
  NEW PHONE-CHECK ITEMS: (a) APD Settings exotic slider reads "(new runs)"; (b) a game saved by build 60 or older, continued, shows "exotic not recorded (older run)" in UIMSD
  rules text and its new log is unknown-ruleset; (c) Home with all rules on shows the MIXED RULESETS note; (d) Seed floors comparison table has the new "Same kind, state
  not recorded" column; (e) hidden information is back to your own setting after leaving Seed floors.
- Integrity audit (invariants 1-12; 3 read-only audits, then verified): FIXED (tests added; leak and ruleset-gap reproduced before the fix): hidden-group
  drill-down copied an unfinished run's true value into a visible filter; a ruleset change in the last level / between saves was not recorded until now
  (checked at save and run end too); Challenges view merged difficulties (now also groups by difficulty + balance); seed floors refuse MIXED-rules runs and
  runs with unrecorded exotic setting; canonical seed floor numbers are "~" when grouped; seed comparison table is MIXED provenance in reports; stale
  seed Back places dropped when another run's seed floors open; UIMSD rules label used the wrong challenges from a running game.
  ENFORCED with tests: raw logs only appended (LogRecovery tail-repair keeps the cut bytes), cache rebuild identical, demo isolation, imports never replace,
  finished/unfinished masking in tables/filters/sorts/aggregates/reports/exports, Catalog sync.
  PRODUCT DECISIONS (all five decided and implemented, see "Decisions 1-5" below): exotic setting per run, unknown for old runs, mixed-ruleset labelling,
  as-generated item state in seed floors, hidden-information choice restored after seed floors.
  Known low risks left: unreadable run log is dropped from datasets without a notice; row-cache validity = log length + manual BUILDER_VERSION/DERIVATION_VERSION
  bumps (nothing couples them to code edits); legacy ranking records without gameID can import twice; stale seed-floor worker has no epoch check; tour worker
  writes non-volatile statics; unknown balance revision >=2 plays as revision 1 in live play (replay refuses it).
- Previous PHONE-CHECK build: **apd-build-57 = commit 3212b882a** (stability pass; core 567 tests, device tests green API 26 + 34). Previous: apd-build-53 = commit 9ff719889 (Difficulty balance pass 1): core 560 tests green; device tests green API 26 + 34. Previous: apd-build-51 (difficulty framework), 49 (reports), 8dbbadf69 (floor replay).
- Frozen: Phases 0-7 (Phase 7 UIMSD approved/frozen at build 28). Phase 8 (export/import ZIP, storage meter, Android file picker):
  automated verification complete; NOT permanently frozen until the user's physical picker check.
- Telemetry version now 1.6. UIMSD = one major feature; these phases are milestones inside it.

## Review later (decided: NOT to change now)
- **Rankings navigation.** Upstream's title screen had a Rankings button (classic top-scores list, `RankingsScene`). APD's title menu (Play, UIMSD, Badges, Journal, Options, Quit,
  APD Settings, News, Changes, About) has none. `RankingsScene` is still reachable only from inside a game (`WndGame`, the end screens `AmuletScene`/`SurfaceScene`). UIMSD already
  imports APD's rankings file as "imported ranking records". Decide intentionally: keep it on the title, fold it into UIMSD, keep it only where it is now, or retire it.
  Upstream's title-screen Support button, `NewsScene` (replaced by APD's own news text) and the in-title `ChangesScene` link are likewise unwired but still compiled.
- **Upstream sync.** APD is forked from upstream tag v4.0.0 (2bb34a4e9, the last upstream commit in the history). Upstream has v4.0.1 and v4.0.2 which are NOT merged. Do a
  deliberate upstream-sync review later (read their changes, merge or port selectively); do not merge as part of other work.
- **APK size (investigated, no action).** APD ships a DEBUG APK (CI `assembleDebug`): no R8 or resource shrinking, all four CPU architectures, ~38.0 MB; the number Android shows
  (38.04 MB) is essentially the APK file. SPD from Play is a release build (R8 + resource shrinking + one architecture) plus compiled-code files, so the two "App size" numbers
  are not comparable. Versus upstream v4.0.0: zero files deleted or renamed, no build setting strips content, APD adds ~1.4 MB of class bytes (~0.8 MB dex) and ~0.3 MB of
  images. User data (2.3 MB vs 274 KB) is APD's run history and caches. Size optimisation is not a priority; do not remove unused upstream code or resources for size.

## Pending manual checks (user)
- Phase 8: physical phone check of the Android system file-picker screens (export save, import open). Everything else is automated.

## Unresolved issues
- None known. Build APK run 31 once failed only in "Publish release" (sub-second, transient; no release apd-build-31 exists).

## Approved requirements that affect current/near work
- Minimum Android = 8.0 (API 26). Do not reintroduce API 21-25 support. Harmless dead compat code (listed in docs/APD-MIN-ANDROID.md)
  is NOT to be cleaned up now.
- Do NOT implement archive-and-remove (deletes raw history): needs the user's separate decision.
- **Adult-humor / content preference (implement before public release, not before Phase 9 ends; do not derail current work):**
  one-time question "are you 18 or older?"; store ONLY a local content preference (no birthday, no age). 18+: title text stays
  "HEY, GO HERE FOR DRUGS"; under 18 / clean: "HEY, GO HERE FOR UNLIMITED DOPAMINE". Both still open UIMSD exactly as now. UIMSD itself
  is never age-restricted. Add an "Adult Humor" setting in APD Settings to change it later. Build it as ONE shared preference that any
  future adult joke reads (no scattered age checks).
- Future UIMSD work (tracked, not started): plain-English Summary/Home view; interactive tutorial/guide (datasets, drill-downs, filters,
  sorting, meaning of ? >= ~ coverage); final language/UX polish pass; shareable/PDF reports; post-run whole-seed/future-floor
  interrogation (needs a proper technical feasibility study first).
- **UIMSD CORE PHILOSOPHY (REQUIREMENT from the user, applies to THE ENTIRE GAME; NOT a "Character UIMSD" feature; survey/design first, NO coding until the user approves; design in docs/APD-UIMSD-UNIVERSAL.md):**
  If APD can reasonably observe something, UIMSD records it. If it can be counted, totalled, averaged, sorted, filtered, grouped, compared or cross-referenced, the player can do that.
  Applies to everything: characters and their unique mechanics (EVERY character, incl. future APD characters), items, equipment actually worn, upgrades, attacks, damage, misses,
  healing, shielding, enemies, floors, movement, spells, abilities, talents, subclasses, consumables, hunger, status effects, shops, gold, identification, bosses, deaths, victories,
  challenges, difficulty, turns, time, generation, and anything else recordable WITHOUT harming gameplay/performance/storage/privacy or revealing still-hidden information.
  The examples are NOT a requirements list and capabilities must NOT be limited by the questions we think of during development: "average Guiding Light casts per floor" must emerge
  from recorded facts (spell casts + character + floor + run) being cross-referenceable, not from anyone predicting that question. Character statistics are one part of the one
  universal data model, never a separate feature or database. Long-term goal: any reasonable question about the player's APD history that APD had the information to answer, UIMSD can answer.
- Owner decision left open by Phase 9: natural regeneration is the biggest log cost (32 B/heal exact vs 18 B inferred, ~70 KiB per
  20k-turn run). Possible later trade-off: one summary event per stretch of regeneration (loses per-tick history). Not done.

## Decisions that must not be accidentally reversed
- **Non-P2W (permanent design constraint): money may never provide gameplay, progression, statistical, competitive or UIMSD advantages.**
  Applies to every feature, including reports/export, UIMSD and any future store, supporter perk or donation flow.
- Raw per-run logs are the source of truth; every cache/index/fact is derived and rebuildable. Never weaken that.
- Hidden information: INTERNAL events/columns are never shown for unfinished runs; exports carry unfinished runs only as redacted
  copies (INTERNAL events dropped, seed removed). New event types default to INTERNAL; PUBLIC ones must be audited in LogRedactor.
  Exact healing source lives only in INTERNAL HEAL_DETAIL (event 299); the public HEAL stays player-visible information.
- Import is all-or-nothing, never overwrites, never partially modifies history. The ZIP importer's guards stay on every Android
  version: `readChunk` (a 0-byte read = damaged archive) and `ProgressGuardInputStream` (a source that stops answering fails the
  import). Found because Android 5's ZipInputStream spun forever on a cut entry and held the importer lock.
- Demo data (INDEV only) lives in its own marked folder and never enters real history/exports/imports.
- Telemetry changes are additive minors; old logs keep their meaning (older data shows unknown/inferred, never zero).
- Only the hero's HP is recorded for healing; every HP-raising line in the game is either hooked or an explained exemption
  (`ExactHealingWiringTest` fails on a new one).
- Title button label/target: the UIMSD button opens UimsdScene (TitleButtonWiringTest).

## Next approved work
Home/Guide milestone implemented (see docs/APD-UIMSD-HOME-GUIDE.md): UimsdHome, UimsdGuide, UimsdTutorial, tour sample data,
UimsdScene Home + tour overlay. Awaiting user approval of the milestone report. Wording is placeholder (final polish pass pending).
Still pending (user, non-blocking): Phase 8 picker check, Phase 9 healing-column phone check, plus a phone look at Home and the tour.
Not started: adult-humor preference, language polish, shareable/PDF reports, whole-seed interrogation, archive-and-remove.
Measured in Phase 9 (for reference): desktop 50,000-row scan 50 ms; emulator demo history first build 183 ms (API 26) / 630 ms (API 34),
cache reload 22 / 61 ms, slowest view 11 / 54 ms; exact healing 32 B per heal (inferred was 18 B), ~70 KiB per 20k-turn run.
No optimisation was warranted beyond slimming HEAL_DETAIL (19 -> 14 B).

## Future-floor feasibility (study done, not implemented)
docs/APD-FUTURE-FLOORS-FEASIBILITY.md: floors are not f(seed,depth); exact only as in-order replay 1..N under a fixed reference context
(harness proves it). Needs replay-only init + statics snapshot/restore; finished runs only. Awaiting user decisions listed there.

## Phone checks (user workflow)
Do NOT ask the user to install/test each build; intermediate builds need no install. Keep automated core/device/CI testing as strong.
Request a physical test only when (1) blocked without it, (2) a major milestone needs final physical approval, or (3) several pending
checks can be bundled into one designated PHONE-CHECK build. Accumulate non-blocking checks here; keep working meanwhile.
Pending, bundled for the next PHONE-CHECK build (none blocking):
- Phase 8 system file picker (export/import)
- Phase 9 healing columns in UIMSD
- Home layout
- Guide/tour overlay and interaction
- Seed floors screen (Runs row -> Seed floors (canonical)): labels, ~ marks, notice line, comparison table
- APD Settings: Difficulty slider (1-5) and its explanatory text fit and work
- Reports: Home Report button, Options -> Share a report, run/group window report buttons, the share sheet and saving each format to a file

## Floor replay (user approved all 3 decisions; Phase A -> B -> C autonomous; A green at 4e43ce250, B+C implemented and verified at 8dbbadf69 (milestone complete, awaiting approval), see docs/APD-FLOOR-REPLAY.md; physical check of the Seed floors screen joins the PHONE-CHECK bundle)
Decisions: canonical seed floors always labelled (never "the exact counterfactual"); unreached floors of FINISHED runs may be
interrogated; replay only when no live game is loaded; for reached floors recorded telemetry is authoritative (canonical only fills
floors that never existed). Phase A (FloorReplay + isolation + device test) -> docs/APD-FLOOR-REPLAY.md. Phase B: "Seed floors" dataset;
Phase C: recorded-vs-canonical comparison. Keep provenance (RECORDED vs CANONICAL) explicit everywhere.

## ETA rule (permanent)
Before starting any task expected to produce a build, output exactly one short line: "ETA: ~X-Y min" (based on real work/tests/CI), then begin.
If the rule is learned mid-work, give one estimate for the remaining time. Update only if a material problem changes it; no routine
progress reports or repeated ETA checks. When the build finishes, report actual elapsed time if known.

## UIMSD reports/export (milestone COMPLETE at 0994586d1, awaiting approval; see docs/APD-REPORTS.md)
Format: Markdown / plain text / HTML (print to PDF) / CSV from one `Report` model; share as text or save via the system picker; no native PDF
(investigated). Reports: lifetime, view, run, group, run comparison, seed floors (canonical clearly separated). Marks + legend + canonical
notice in every format. Physical check joins the PHONE-CHECK bundle: Report buttons/windows, share sheet, saving a file.

## Difficulty system (balance pass 1 IMPLEMENTED and verified at 9ff719889; see docs/APD-DIFFICULTY.md)
Levels 1-5, 3 = baseline (never changes, revision 0). Revision 1 (levels 1,2,4,5): enemy HP, enemy damage, ordinary-item count at 70/70/140, 85/85/120,
120/120/85, 150/150/70 %. Healing, food, spawns, bosses, champions, traps stay neutral (do NOT add effects without approval). Setting = new runs only; run keeps its
difficulty (saved); legacy runs unknown (?) and play as 3. Telemetry 1.7; UIMSD columns + "Difficulty" view; FloorReplay uses the run's difficulty.
Physical check joins PHONE-CHECK: difficulty slider text; feel of levels 1 and 5 in a real run (enemy toughness, loot amount). Non-P2W applies.

## UIMSD sweep (safe work done; see below for what is NOT done)
Done: Home difficulty card; difficulty named in the rules list/notes/reports; class names as words; Guide topics for reports, seed floors/canonical data and
difficulty; tour reports step; run and group comparisons in reports; wording lint test (UimsdWordingTest); docs refreshed. Reviewed for TODOs: none open.
NOT done on purpose (each needs the owner's decision, none is a TODO in the code): bosses in UIMSD (boss flag is not recorded: telemetry change); saved
queries UI; richer multi-column grouping UI; recorded per-floor creatures in seed floors (needs a new row source); final language/style pass (the text is
functional and lint-clean, not final); archive-and-remove; adult-humor preference (not UIMSD); exact hooks for other inferred signals.
**PHONE-CHECK build** = the CI build of the commit named in "Latest known-good" below; it carries the whole bundled checklist (Phase 8 file picker, Phase 9
healing columns, Home layout (incl. Report and Difficulty card), Guide/tour overlay (15 steps), Seed floors screen, Reports (buttons, share sheet, saving each
format, comparisons), APD Settings difficulty slider and text, feel of difficulty levels 1 and 5). Do not install intermediate builds.

## Working rules the user set (usage efficiency)
Minimal narration; batch related work; do not poll CI repeatedly (estimate the finish, check once around then, do independent approved
work meanwhile); continue autonomously inside approved scope and stop only for genuine product/design decisions, destructive actions,
architectural changes, conflicts with requirements or the end of a phase; testing standards stay exactly as strong (full core suite,
API 26 + API 34 device tests, no weakened assertions or hidden-by-timeout fixes); keep this file updated.

Communication: ultra-terse by default. No narration of routine actions, obvious commands/edits/tests/CI states, or repeats of known
info; no running commentary unless attention is needed. Routine updates 1-3 short sentences ("Implemented. 509 tests green. CI running.").
Tell the user once when waiting on CI, check at an appropriate time, don't re-narrate. Continue autonomously in approved scope; don't ask
needless confirmation. Full detail only for: decisions needed, bugs/risks/unexpected behavior, milestone/phase reports, details needed to
resume safely. Never shorten code, tests, reasoning, verification or docs to save usage. Keep this file concise and authoritative.

## Splitting work with ChatGPT (user's rule, to save Claude usage)
ChatGPT has no access to this session or repo; every handoff must be self-contained. Offload only when it clearly saves effort:
architecture/design review, specs and requirements, test-plan and regression-test design (before implementing), edge-case analysis,
review of a diff or of Claude's completion report, documentation drafting, bug analysis from logs/code the user pastes.
Keep for Claude: editing the repo, repo-wide searches for implementation, building, running tests, fixing compile/test failures,
git, CI/releases, implementation that needs the surrounding source. At the start of a substantial task, say in one or two lines whether
a meaningful part can be offloaded; if so give ONE copy/paste package (task, context, which files/diffs to attach). No pointless
handoffs. Splitting never reduces tests, verification or quality.

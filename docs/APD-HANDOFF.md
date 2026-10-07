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
- Latest known-good build: **apd-build-37** (commit 80ef188f8). Later commits are docs only.
- Frozen: Phases 0-7 (Phase 7 UIMSD approved/frozen at build 28). Phase 8 (export/import ZIP, storage meter, Android file picker):
  automated verification complete; NOT permanently frozen until the user's physical picker check.
- Telemetry version now 1.6. UIMSD = one major feature; these phases are milestones inside it.

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
- Owner decision left open by Phase 9: natural regeneration is the biggest log cost (32 B/heal exact vs 18 B inferred, ~70 KiB per
  20k-turn run). Possible later trade-off: one summary event per stretch of regeneration (loses per-tick history). Not done.

## Decisions that must not be accidentally reversed
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

## Floor replay (user approved all 3 decisions; Phase A -> B -> C autonomous)
Decisions: canonical seed floors always labelled (never "the exact counterfactual"); unreached floors of FINISHED runs may be
interrogated; replay only when no live game is loaded; for reached floors recorded telemetry is authoritative (canonical only fills
floors that never existed). Phase A (FloorReplay + isolation + device test) -> docs/APD-FLOOR-REPLAY.md. Phase B: "Seed floors" dataset;
Phase C: recorded-vs-canonical comparison. Keep provenance (RECORDED vs CANONICAL) explicit everywhere.

## ETA rule (permanent)
Before starting any task expected to produce a build, output exactly one short line: "ETA: ~X-Y min" (based on real work/tests/CI), then begin.
If the rule is learned mid-work, give one estimate for the remaining time. Update only if a material problem changes it; no routine
progress reports or repeated ETA checks. When the build finishes, report actual elapsed time if known.

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

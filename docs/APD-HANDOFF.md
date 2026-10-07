# APD handoff (read first in a fresh session; keep this short and current)

Project: private fork of Shattered Pixel Dungeon 4.0.8 = "ADHD Pixel Dungeon" (APD). Repo `charltonchris29/ShieldingShouldHaveWorked`.
Branches: `main` (every push builds an APK release `apd-build-<run number>`) and `claude/adhd-pixel-dungeon-setup-iszc3v` (push both).
Local tests: `./gradlew -p <scratch>/jv :core:test` (see the session's scratchpad); Android code only compiles/tests in CI.
CI: "Build APK" ~3 min, "Android device tests" ~4-5 min (emulators API 26 = minimum, API 34). `.github/ci/run-device-tests.sh` streams
logcat tags APDMIG/APDPERF into the job log and uploads logcat artifacts.

## Status
- **Current phase: 9 (exact healing + measured optimisation), implementation done, commit a19c9c00e pushed, CI result pending.**
  After CI is green: write the Phase 9 completion report to the user and STOP (no new phase without approval).
- Latest known-good build: **apd-build-35** (commit 468abc8b9, Phase 8 + API 26 minimum, all green).
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
1. Confirm CI green for a19c9c00e (Build APK + device tests API 26 and 34; read APDPERF numbers from the job log), then report Phase 9
   to the user and stop.
2. Then only what the user approves. Candidates they have named: the adult-humor preference (above), the future UIMSD items (above),
   Phase 8 picker sign-off.

## Working rules the user set (usage efficiency)
Minimal narration; batch related work; do not poll CI repeatedly (estimate the finish, check once around then, do independent approved
work meanwhile); continue autonomously inside approved scope and stop only for genuine product/design decisions, destructive actions,
architectural changes, conflicts with requirements or the end of a phase; testing standards stay exactly as strong (full core suite,
API 26 + API 34 device tests, no weakened assertions or hidden-by-timeout fixes); keep this file updated.

> Current emergency release TEST17 (appsourcea317b5d5, CI37972251204): full908core + four targeted API34 checks pass. First-run controls/visible entrance and saved-floor1 entrance-only repair verified; exact owner phone/save upgrade still needs testing. Never advise deleting/abandoning a save for this door repair. The older notes below are historical; current details are at the top of HANDOFF.md. Gameplay blockers always precede noncritical features.

> Emergency October9 correction: TEST10 still blocked fresh first-run gameplay. Source545faa7f implements non-blocking tutorial controls/visible entrance and saved-floor1 entrance-only repair; four actual generation/bundle tests pass, but its first full run failed two historical golden snapshots. Follow-up986c6c50 preserves the historical comparison while requiring real visible exits; replacement verification is underway. Do not advise abandoning a saved run to fix the door. Install-over-update and physical RedMagic verification remain required. The older notes below are historical, not current completion claims.

> October 9 current status: Codex work is on codex/apd-phone-fixes-2026-10-09. First full CI passes 887 tests and Android assembly; later menu/address fixes have separate verification. Semantic UIMSD restoration is partial; some leaf/input dialogs remain. Adult is blocked because no age provider exists. Recorded abandonment currently counts as ended; item 2's unfinished claim below conflicts with source. Review HANDOFF.md and APD-SPRINT-BUG-LOG-2026-10-09.md before using this older snapshot.

# ADHD Pixel Dungeon (APD) alpha: known issues and source guide

Repository: github.com/charltonchris29/ShieldingShouldHaveWorked (private). Work branch: `claude/adhd-pixel-dungeon-setup-iszc3v`. `main` only holds verified builds plus the one manual workflow file. Upstream: Shattered Pixel Dungeon 4.0.x (licence and attribution preserved).

## Alpha configuration (what the test APK contains)
- Cleric is playable (un-ban, option B). Cleric runs record their own ruleset (`cleric=playable`); every other run keeps its original ruleset id, so old history is untouched.
- Demo Unlock is ON in the test APK only (UIMSD locked until 10 genuine runs; demo option).
- Cleric Grudge / Lore is OFF in every build behind one switch: `ApdFeatures.GRUDGE_LORE_IN_THIS_RELEASE = false` (core/.../apd/ApdFeatures.java). All its code, 348 lines (core/src/main/assets/apd/humor/), tests and docs are kept.
- Switches live in `ApdFeatures`; a test build (`-PapdTestBuild`, see android/build.gradle) writes the asset `apd/features.txt`. Normal builds have no such file, so every test feature is off.
- Tutorial fix: `ApdIntro` ends SPD's first-run tutorial for a used install (it caused the closed floor-1 start, greyed Main Menu and hidden difficulty picker).

## Known status / remaining bugs / open items
1. UIMSD parameterized-dialog restoration has retained API34 coverage: verified TEST180 recreates Column order and Move column exactly once. TEST188 executed the retained Android Back class, but its fixture entered through the private build's intentionally enabled demo-unlock gate and timed out before the tour assertions. TEST189 enters the already-authorized scene directly; the Back-during-preparation correction, production gate and application behavior are unchanged, and exact-source verification is pending. Physical-phone lifecycle acceptance remains unclaimed.
2. UIMSD default Home scope counts finished runs under the rules a new run would use. Abandoned runs are finished records; Cleric and other different-rule runs require Rules: all, while runs still going use the separate Tables > Options switch. TEST185 run 38664406486 verified the controlled native empty-History rendering and retained/reviewed all 35 screenshots after TEST183's fixture-order failure and TEST184's omitted evidence-list entry. Physical-phone wording acceptance remains unclaimed.
3. A floor-1 run created while the tutorial was on keeps its hidden entrance door (abandon it and start a new run, or search).
4. Hero-select markers (pips + win star): design written (docs/APD-HERO-MARKERS.md), NOT built, awaiting approval.
5. Phone-test steps 23-41 of docs/APD-PHONE-CHECKLIST-B69.md were not finished/reported; unknown bugs may remain. Nothing in this build has been tested on a device by the developer assistant.
6. The Android module cannot be built in the Claude sandbox (blocked Google host); APKs are built by the manual GitHub workflow `.github/workflows/private-test-apk.yml`.

## Incomplete / future features
- Cleric Grudge/Lore (postponed to public release). Future plan, NOT designed or built: 50 Cleric Lore entries at release, then 50 per year for 20 years (1,000), shared unlock conditions across years, later entries build on earlier ones.
- Backend: provider-neutral libraries only (queue, gateway, trust, signed content catalogue, preference-sync replica); no server, sign-in or hosting chosen. Decisions in docs/APD-BACKEND.md.
- Hero markers, plain-English UIMSD summary, tutorial polish: see docs/HANDOFF.md.

## Where things are
- docs/HANDOFF.md: resume protocol, decisions, release process. docs/APD-FIX-LOG-2026-10-08.md: each fix with root cause. docs/APD-PHONE-CHECKLIST-B69.md(+pdf): phone checklist (Grudge section postponed).
- Game code: core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ ; APD additions under `apd/` (recorder, stats, query/UIMSD, backend, humor, difficulty). Tests: core/src/test (848 tests; run with Gradle `:core:test`).
- Release process: Actions > "Private test APK" > Run workflow on the claude branch with `publish` on = GitHub pre-release with the APK attached (tag apd-test-N). Never push unfinished code to main for a test APK.

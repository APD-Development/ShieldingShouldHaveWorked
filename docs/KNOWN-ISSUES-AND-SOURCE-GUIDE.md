# ADHD Pixel Dungeon (APD) alpha: known issues and source guide

Repository: github.com/charltonchris29/ShieldingShouldHaveWorked (private). Work branch: `claude/adhd-pixel-dungeon-setup-iszc3v`. `main` only holds verified builds plus the one manual workflow file. Upstream: Shattered Pixel Dungeon 4.0.x (licence and attribution preserved).

## Alpha configuration (what the test APK contains)
- Cleric is playable (un-ban, option B). Cleric runs record their own ruleset (`cleric=playable`); every other run keeps its original ruleset id, so old history is untouched.
- Demo Unlock is ON in the test APK only (UIMSD locked until 10 genuine runs; demo option).
- Cleric Grudge / Lore is OFF in every build behind one switch: `ApdFeatures.GRUDGE_LORE_IN_THIS_RELEASE = false` (core/.../apd/ApdFeatures.java). All its code, 348 lines (core/src/main/assets/apd/humor/), tests and docs are kept.
- Switches live in `ApdFeatures`; a test build (`-PapdTestBuild`, see android/build.gradle) writes the asset `apd/features.txt`. Normal builds have no such file, so every test feature is off.
- Tutorial fix: `ApdIntro` ends SPD's first-run tutorial for a used install (it caused the closed floor-1 start, greyed Main Menu and hidden difficulty picker).

## Known remaining bugs / open items
1. UIMSD dialogs (e.g. Arrange the columns) close when you switch Android apps and come back; the table stays. Needs per-dialog restore.
2. UIMSD default scope counts only FINISHED runs under the rules a new run would use; abandoned runs and Cleric runs (own ruleset) only appear under "All (mixed)". By design, but may confuse; wording/empty state could be clearer.
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

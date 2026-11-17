# APD character-selection difficulty fade handoff, 2026-11-12 Eastern

## Owner report and cause
On November 12 at approximately 1:17 PM Eastern, owner shared Discord #bug-reports screenshot confirming the character portrait selection menu fades away but the recently added run difficulty selector remains opaque. The established HeroSelectScene.updateFade() updates hero buttons, Start, options and info controls, but omitted DifficultyBar (its separately constructed label and five number buttons). Owner explicitly asked to fix this. Do not change the fade duration, selected difficulty, or run-setup mechanics.

## Baseline and concurrency
Inspected established development branch `codex/apd-phone-fixes-2026-10-09`, starting SHA `46d16489bf0069d8981fea6f285f13942bc1f24f`. Reviewed code for HeroSelectScene, DifficultyBar, DifficultyBarTest and existing Android RunSetupDifficultyDeviceTest. Separate PR #19 changes visual animation speed groups; PR #6 has APD461 blockers. Do not overwrite or rebuild either. Isolated branch `chatgpt/apd-hero-difficulty-fade-20261112`, draft PR #20, not merged.

## Actual changes
- `0b77d43529a64f3f271acfa37ab75a1ab6fa3c77`: DifficultyBar.fade(float) applies same alpha to composite label and all five segment buttons; enables touch targets when alpha > 0 and disables them at zero.
- `c98d6e137cdc5bf9af1019f3a8ef9fbeba720b52`: HeroSelectScene.updateFade() now includes difficultyBar.active gating and difficultyBar.fade(alpha), preserving portrait/landscape UI behavior.
- `8113a4a6346026c6f0d987ab01c762773ca2dd3d`: isolated CI gate checks exact fade/visibility/touch wiring and runs complete core tests and desktop/Android debug/release/instrumentation Java compilation.

## Verification
GitHub Actions run [43278538431](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43278538431) was IN PROGRESS when observed; do not claim it passed until inspecting final result and logs. No local tests or emulator/phone visual fade acceptance have been run this execution. No APK built or published. No edits to save schema, gameplay, scoring, difficulty numerical rules, signing, application ID or known-good releases.

## Next safe action
Check CI run 43278538431 for exact pass/failure. If failure, fix only the evidenced regression on this isolated branch. If passing, inspect the live established development head for concurrent changes, then integrate without force update and verify merged result. Native Android UI fade verification remains useful before a gated APK release. Preserve existing APD460 and APD461 candidate/artifacts. Do not assume draft PR #20 equals a shipped APK.

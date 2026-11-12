# APD well activation visual timing follow-up (November 12, 2026, Eastern)

## Owner instruction and initial verified state
Owner reiterated that enemy-alert symbols, both wells and gas animations must run at normal 1x even when selected visual playback is extreme. Established development branch `codex/apd-phone-fixes-2026-10-09` was read at commit `8d5856f8037a72684ecc6becd0c7e523a8fcbb84`. PR #19 had already merged the 1x status, enemy-alert and blob groups after its successful full suite. Do not duplicate PR #19. This pass inspected `AGENTS.md`, the normal-overlay handoff, `GameScene`, `PlaybackGroup`, `WellWater`, `WaterOfHealth`, `WaterOfAwareness`, `ToxicGas`, `CellEmitter` and `CharSprite`, and verified APD461 was not published. No external upstream search.

## Newly evidenced missing path
`WaterOfHealth.affectHero/affectItem` still used `CharSprite.emitter()` or `CellEmitter.get()`, both routing through accelerated `GameScene.emitters`. This caused transient effects from drinking/using that well, filling a waterskin, blessing an ankh and removing a curse to use 1000x when playback was set to 1000x. The persistent Well of Health / Well of Awareness particle ambience and the Well of Awareness's Identification animations already used normal-speed `gases` via `BlobEmitter.parent`.

## Isolated changes
Branch: `chatgpt/apd-well-activation-normal-speed-20261112`; draft PR [#22](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/22), based on development head above.
- `2ce9be7ce239099b68b3782e8ab84114410e4c9c`: new `GameScene.wellEmitter()` allocates/recycles a particle Emitter in existing normal-speed `gases` group.
- `457f62305ae5569e398b40cec771238ef49d5aab`: `CellEmitter.well(cell)` retains normal cell placement and uses that well emitter.
- `28c5884385ced5cd76f5c1372a7c4ca2ebfbcd74`: Well of Health uses normal emitter for the hero healing burst with unchanged sprite emitter placement, and its four cell emission sites use `CellEmitter.well`; particle types, emission interval and counts unchanged.
- `a056a1dca7e58e929d7d4e46d6ec8d9babd35fb8`: isolated CI workflow checks routing and runs full core suite plus desktop and Android debug/release/instrumentation Java compiles.

## Verification status
Isolated GitHub Actions [run 43281202873](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43281202873) was **in progress** when checked. No CI success is claimed without final confirmation. No native Android emulator/phone visual testing or APK build performed in this execution. The draft PR is not merged. No known-good release/signing identity touched; APD460 remains independently checked and APD461 remains separately blocked.

## Next safe action
Read the final CI outcome for run 43281202873 and investigate any failures before merge. Inspect live development head for overlapping work. If exact CI is green, integrate source-only PR using no-force integration with a reverified base; preserve APD461 and owner-approved balance. The next **qualified** signed APK requires established frozen candidate, native Android acceptance, original-signer and publication gates. Actual RedMagic animations remain unverified until such a build is installed and observed.


## First CI failure and correction (November 12, Eastern)
Initial run [43281202873](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43281202873) failed at `:core:compileJava` because `WaterOfHealth` was missing the explicit `GameScene` import used by its new well-only healing emitter. Static routing checks passed but no core tests ran. Do not count it as a passing build.
- `fc33b68b6e7afa47ca09d6642f339482217bef40` adds the exact missing import.
- `48ea0434f4d8406bcd541ad42aa9c83d6982f6a5` strengthens the isolated workflow's import assertion and triggers full retry [43281486580](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43281486580).
Retry status was queued when observed; await its real outcome before merge/release. No APK was built.

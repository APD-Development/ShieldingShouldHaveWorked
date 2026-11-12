# Personal APK playback preferences, November 12 2026 (Eastern)

## Authority and boundaries
Owner supplied 58 individual category preferences in the originating APD conversation and explicitly confirmed option A: selected game speed accelerates only presentation/playback, never actor turns, attack costs, buff/debuff turns, hunger, combat, scoring or balance. Later requested that these preferences be implemented for **the owner's personal APK**, NOT as an approved community/Alpha feature or as approval to implement the proposed Discord poll. Public/Alpha game behavior must remain unchanged.

Base is exact frozen APD463 published producing commit `6d901f2886d6060056d9aaddf63aa1f81536c079`; authoritative shared development branch was independently checked at `bf9b86c697d15237a868c4958473eda1043cae42`. All edits are isolated on `chatgpt/apd-personal-playback-preferences-20261112`, no shared branch writes and no alteration of APD463 release. Personal edition currently uses `ApdFeatures.testBuild()`, while Alpha uses `extendedPlayback`. Preserve that existing boundary, signing, appID, versionCode 920 and all history/save/scoring.

## Owner's agreed playback matrix
Hero: walking/running, melee/ranged, wand casting, item/drink, search, damage/flash = selected speed. Hero idle, death/resurrection, jumping/falling/teleporting = 1x.
Enemies: all eight categories including movement, idle, attacks, damage, death, spawn, boss abilities = selected speed.
Combat: weapon swings, projectiles, wand bolts, explosions, hit/blood particles, barriers/healing and numeric feedback = selected speed; ongoing burning/freeze/etc status visuals = 1x.
Environment: vegetation and plant-room, water/ripples, wells, gas, fire/ambient particles, traps and terrain = 1x; door opening/closing = selected speed.
UI: alert/question symbols and floating word-status = 1x. Health bars, pickups, search markers, inventory/hotbar, menus/transitions, camera/shake and UI notifications = selected speed.
Gameplay: selected 'game speed' on turn categories means faster presentation/execution only, NEVER reduced turns or changed mechanics. Game sounds remain original 1x cadence.

## Implemented only as partial source work, not a completed 58-category deliverable
- `ca58ec78`: adds Developer-only `PlaybackGroup.personalSpeedGroup()`; uses it for heap sprites/pickups, health indicators, checked/targeted cell markers. Hero idle/death/fly states use normal 1x via an inherited per-state frame timer hook; all other hero and enemy sprite-frame timing retains APD463 selected speed.
- `1183bd0d`: corrected feature-flag test fixture.
- `7b33a157`: adds Developer-only spell sprite playback and numeric feedback subgroup, with unchanged Alpha group behavior.
- `dd20dcb2`: routes strictly numeric floating damage/healing feedback to the Developer's accelerated numeric layer, while ordinary text remains 1x.
- `d1be3829` and `52f3c2a9`: isolated source/Java/JUnit verification workflow.

## Missing items, MUST NOT falsely claim finished
Unrouted owner-selected effects remain: differentiated combat versus environmental emitters/projectiles/explosions; item pickup toolbar animation (heap sprite layer alone does not establish this); weapon swing and spell particles beyond SpellSprite; door animations; hero resurrection/teleport sequence; inventory/hotbar/menu transitions; camera/shake; UI notifications; special boss/hit visuals beyond character frames. Some groups contain mixed effects and MUST NOT be accelerated indiscriminately. Need explicit effect ownership and tests. Adaptive wall-search user's device report remains unresolved and separate. No new APK should be published until all requested personal playback behaviors are genuinely implemented, tested and native-accepted, or the owner explicitly requests an intermediate build.

## Verification and release
Source CI triggered on this branch; read live Actions results before reporting passes. No local tests, signed APK, native acceptance or release in this work package. Existing published APD463 remains intact, and previously closed APD462 does not change. The next qualified personal APK must be a separately numbered Developer build using original private signer plus preserved history and native screenshots. Do not publish the Alpha/community variant with personal-only preferences.

## Verified source checkpoint, November 12 2026, 6:08 PM Eastern
GitHub source verification run [43325770885](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43325770885) completed successfully on exact gameplay/test/workflow commit `52f3c2a9aae68517ec9d1ab5b92403062b592a07`. Log shows `BUILD SUCCESSFUL in 2m 54s` and **3,324 core tests across 476 suites, zero failures/errors/skips**, successful desktop classes and Android debug/release/instrumentation Java compilation. Earlier run [43325469250](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43325469250) also passed but predates spell/numeric effect changes. Documentation-only commits after exact tested source do not change gameplay. No native visual/device acceptance, original-signer Developer APK build, or new release. **Draft PR #48 remains partial and must not be presented as the full 58-choice fix.** Existing APD463 APKs remain unchanged. Explicit remaining work is listed above. Do not infer live background coding from an in-chat trigger.

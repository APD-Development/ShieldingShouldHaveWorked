# APD personal playback continuation, November 13, 2026 (Eastern)

## Owner authority
Implement the owner's previously supplied category-by-category speed matrix in the personal Developer APK. Only playback and animation rate changes; **no actor-turn costs, enemy AI, hunger, score multipliers, buff duration in turns, save data or combat balance changes**. Do not add a community customization setting from the unapproved Discord poll. Earlier APD464 is verified published but incomplete; never overwrite its tag/APKs. Do not distribute a source-only or partially qualified candidate as finished.

## Verified starting state and isolation
Source base: APD464 original producing branch head `8acb8aa5421da6801023c94d2b55dfd77dd5681f`.
Independent branch: `chatgpt/apd-personal-playback-completion-20261113`. Authoritative shared development branch was `513a6d27bbc2a92347a431b08f62b8c31027fe18` on the initial November 13 live check; do not write to it. Original published APD464 release targets the above source. Open APD PR #48 and historic PRs #38/#47 must not be overwritten or merged without head verification. Only owner repository and material were used.

## Personal-only playback source added
- `985f2c80`: scale actual UI pickup flight and journal-notification flash using try/finally visual delta, without changing input/timing callbacks.
- `fe5b7387`: separate explicit wand/weapon/bomb/boss impact particle emitters from ambient plants, wells, gas and traps; add positive-speed smoke test.
- `3152e14d`: isolate the test's two inactive fixture emitters before updating normal and personal groups.
- `2f291898`: camera pan/shake scaling only on GameScene's personal visual camera, reset on scene destruction; no alteration of global elapsed or actor turns.
- `82c7b196`: missile projectile tweens, item rotation, magic bolt animation and title/game scene fades scale in Developer only; Alpha remains unchanged.
- `f0f7b0d0`: initial broad effects-group acceleration was rolled back at `914b6135` after inspecting persistent TorchHalo, shield and other status visuals sharing this group. They are not safe to accelerate collectively.
- `6f2e21f8`: damage flashes and explicitly combat blood/hit splashes use personal speed; water/ambient splashes unchanged.
- `becfa77f`: inventory/hotbar item-icon animation and health/talent warning visuals scale without accelerating UI input.
- `a9b368d7`: explicit bomb/wand/boss-laser impact bursts use selected speed; alarm/trap/ambient bursts remain 1x.
- `dd7df0d3`: lightning combat effect lifespan scales, audio cadence unaffected.
- `1b979c59`: preserve legacy partial headless GameScene fixtures by falling back to their original emitter group if newly introduced combat group is absent, without weakening tests; `32a002ed` adds a regression.
- `d8afc95b`: shield halo and healing-effect emitter go to personal-only combat visual groups; ongoing illumination/chilled/burning status layers remain at 1x.
- `f7465099` and `b15250b4`: numerically formatted combat labels accelerate; countdowns such as `5...`, words and status notices remain 1x, regression-covered.
- Added real Android `PlaybackProfileDeviceTest` that starts an isolated legitimate floor, checks Developer vs Alpha visual group partition and 16x camera factor, captures a screenshot and protects original history; the proof does not claim every visual-duration category is perceptually accepted on a physical RedMagic.

## Source and verification history
Initial full source CI [43411497626](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43411497626) passed 3,327 core tests / 476 suites, no failures, and Android + desktop Java compilation on exact early source.
Subsequent source CI [43412681115](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43412681115) failed three existing `FlashBangBombDamageProvenanceTest` cases due a headless fixture missing the new optional combat visual group. Its failure is real, not a mechanics regression; the original visual emitter fallback was restored and separately tested.
Run [43413286190](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43413286190) began before the fallback change and retained the same three failures. The corrected source is tested through a subsequent isolated workflow, with its result to be checked live. Earlier passing tests do not qualify newer source.

## Scope / acceptance
Direct character movement, idle/action/death frame choices, impact effects, projectiles, inventory/pickup/camera/fade UI, and original ambient 1x routing have been modified in source. Doors are currently immediate terrain tile changes without an independent timed door animation to accelerate. Game-turn processing, resting mechanics, buff turns, hunger and scores stay unchanged per option A. Existing physical-phone playback at 16x and the owner's separate adaptive wall-search complaint require owner acceptance; an APK cannot be described as fully phone-verified from an emulator screenshot.

No APK was built or published by this source-edit package. The next Developer personal successor must pass final exact core/Android compilation, native original-signer APK acceptance and hash-readback release gates. Do not create or deliver a release before those gates succeed. Never overwrite APD463/464 or publish personal-only preferences in Alpha/community.

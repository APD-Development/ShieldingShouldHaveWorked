# APD character-sprite animation playback continuation, November 12, 2026 (Eastern)

## Owner decision
At 4:56 PM Eastern the owner clarified that BOTH player and enemy character animations must scale with the selected playback multiplier, at every existing 0.5x–1000x option. All other visuals including plant rooms, ambient vegetation, magic wells, toxic gas, exclamation-mark alerts, generic particles, environmental layers and UI must remain normal 1x. The owner's separate adaptive wall-search issue remains tracked independently; do not conflate it with this animation change. The owner's recording demonstrated APD462 at 16x appearing unexpectedly slow; an exact device-side frame-timing root cause has not been established.

## State and scope
Latest authoritative primary branch observed at 4:58 PM Eastern: `codex/apd-phone-fixes-2026-10-09` = `bf9b86c697d15237a868c4958473eda1043cae42`. The released APD462 gameplay source is `e26663f5cb8f13c74c312f318c881e79236fbf87`. Its unmerged, independent source integration PR #38 currently has head `1a7836ab1d59ce4438bc91621bfe2dc194be4fac`, which leaves the entire GameScene mob layer at 1x and accelerates walking duration alone. This work is an isolated child of that exact head, `chatgpt/apd-character-animation-speed-20261112`, review PR #47 targeting the PR #38 branch. No shared primary branch, releases, signed APKs or PR #38 head were modified.

## Code changes
- `23bd045c`: `PlaybackGroup.characterAnimationElapsed()` returns selected-speed-scaled frame time without changing the scene group.
- `c9100d3e`: `CharSprite.updateAnimation` advances only character frames at selected speed and restores `Game.elapsed` in a finally block. All normal 1x scene groups and the existing movement-tween duration remain as before.
- `f3e48164`: removes the older `HeroSprite` idle-only cap, so hero sprites receive the same inherited speed behavior as enemy sprites.
- `f0f25dcd` + `b7ff274e`: tests selected-speed frame timing across all ten settings, keep the surrounding group at 1x, and verify the actual `CharSprite` frame timer without graphics assets.
- `1141ce4d`: isolated source-verification GitHub Actions workflow, including full core regression suite, desktop/Android Java compilation, and exact JUnit assertions.

## Verification
At 4:59 PM Eastern, Actions run https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43315524786 was observed **in progress**, with **no conclusion yet**. No local Gradle or Android-device tests were run, no new signed APK was created, and no APK was published/delivered in this execution. Do NOT claim the fix is test verified or physically accepted based solely on the pushed source. Read the workflow's complete result, failed-step logs if any, and original JUnit XML before advancing the draft PR.

## Next safe action
1. Check run 43315524786. If any test or Java compilation fails, fix only demonstrated issues on this isolated child branch, then re-run exact source tests without weakening acceptance.
2. Preserve PR #38's original frozen APD462 published assets. Reconcile the advancing authoritative primary head before any independent integration; PR #47's child base must not silently be treated as merged into primary.
3. A successor original-signer Android APK requires the established native visual, history, app-ID/signature/save-integrity and publication gates, plus new 16x owner-reported behavior checks before claiming the device issue fixed.
4. Leave recurring APD task schedules untouched.

# TEST153 offensive plant provenance

Status: application candidate; exact-source verification pending.

## Confirmed gap and fix

Player-planted Firebloom, Sorrowmoss and Rotberry previously lost their planter before delayed Fire, Poison or Toxic Gas damage. Plants now retain one immutable PLAYER/ENEMY/BOSS/UNATTRIBUTED origin through save/load. Historical plant saves without the additive key remain UNATTRIBUTED. Natural level plants and dropped interlevel seeds remain environmental.

Manual seed throws/plant actions, Wand of Regrowth, cursed-wand Random Plant, Regrowth Bomb, Sandals of Nature and Overgrowth capture the actual initiating actor/effect. Regrowth Bomb reuses its saved fuse origin. Wand of Regrowth consumes a per-zap snapshot rather than consulting stale global state. Firebloom, Sorrowmoss and Rotberry pass the retained origin into the already verified Fire, Poison and Toxic Gas boundaries, so raw damage is scaled exactly once and original damage-source objects remain unchanged.

## Preserved behavior

No plant selection, position, terrain, activation, Warden benefit, Lotus trigger, duration, gas/fire density, RNG call, mitigation, turn, drop, challenge, save-format requirement or score calculation changed. Icecap and the remaining plants were read; they do not directly apply HP damage at activation and therefore need no damage-slider boundary here.

## Retained regressions

Six permanent methods cover all 20 percentages for all three owned roles across Firebloom/Fire, Sorrowmoss/Poison and Rotberry/Toxic Gas; natural environmental exclusion; plant save/legacy compatibility; neutral density/volume/next-RNG identity; and complete known caller wiring. No existing assertions were weakened.

## Verification boundary

Prepared at `2026-10-14T12:37:39.766Z` from real head `566d99427b6216d60392c424040b56e45620bf10`. This execution workspace has no full Gradle checkout, so no local Java result is claimed. TEST153 must run 1547 core tests, debug assembly, compiled immediate-previous-APK history against TEST152, 26 API34 tests and 34 screenshot checks. Three Damage controls remain disabled pending vault/boss, remaining spell/ability/derived sources and six-class/boss/combined acceptance. TEST152 remains the verified fallback; TEST123 remains the last owner-delivered/signature-compared APK.


### TEST153 history-gate outcome

Run `38565297532` stopped before compilation at `2026-10-14T12:38:44Z`. The manifest omitted the already reviewed TEST151/TEST152 evidence paths `docs/APD-TEST150-INFERNO-FIRE-TRANSFER-2026-10-14.md` and `docs/APD-INFERNO-SOURCE-SAFETY-PREPARED-2026-10-14.md` from the TEST152-to-current comparison. Zero core/API34 tests ran and no APK/screenshots were produced. TEST154 corrects only those manifest entries; application source remains the unverified TEST153 candidate.


### TEST154 fixture result; TEST155 correction

Run `38565512376` passed history validation, compiled the complete source and executed all 1547 core tests: 1545 passed and two new fixture methods failed. The natural Rotberry/Firebloom comparison instances retained Java's default `pos=0` while their target and attributed baseline used `CELL=312`; one therefore produced no target hit and the other correctly produced fire at a different cell. TEST155 sets both natural fixtures to the tested cell. Production source and assertions are unchanged. TEST154 produced no APK/API34/screenshots.

# TEST167 all Custom Dungeon Rules acceptance candidate — 2026-10-14

## Baseline
- Started from verified TEST166 source `e252e92e6533ac0a3fa581048475bf55fdd5e3da`, run `38618850435`, and reconciled actual branch head `4c309fd7e82ad29ada0b89428f3d046e6252a6f0`.
- TEST166 passed 1,581 core, 26 API34, debug/history and 34 screenshots. It completed the remaining offensive-source and five-main-boss audit.

## Implemented
- Added retained high-HP/high-defense/low-upgrade acceptance across all six classes using their actual starting attacks. The checks require Player Damage before the scaled armor roll and Enemy HP exactly once.
- Added an actual enemy-versus-boss attack interaction at extreme settings. Enemy Damage and Boss Damage remain replacements, Player Defense applies once afterward, and 25% restorative healing retains fractional recovery.
- Added real game save/load acceptance for all six classes with every one of the 18 settings non-neutral, damaged 500% player HP, healing fraction, difficulty 5/balance revision, nine challenges, class and seed identity.
- Existing retained tests continue to cover high spawn/high enemy HP/fast hunger/low food/low player HP/one scroll across all six classes and each main boss's phase-specific behavior.
- Marked all 18 next-run controls implemented. Active-run controls remain locked, the experimental warning and pending-scoring notice remain, and no scoring coefficient or earned score changed.

## Verification state
- Expected core total is 1,585: three new interaction/save tests plus one presentation guard.
- Exact-source core, debug APK, immediate-previous TEST166 compiled history, 26 API34 tests and 34 screenshots are pending.
- Until those checks pass, TEST166 remains latest verified and the three Damage controls remain a candidate change only. No delivery, release or physical-phone claim.

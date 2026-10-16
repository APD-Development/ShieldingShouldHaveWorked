# Already-available character unlock announcements

## Confirmed cause
The alpha uses the inherited INDEV class-availability override: all six HeroClass values return available, but five unlock validators only check the old global badge. Reproduced all five announcements with the unmodified current Badges blob dbb0874a3de4a2b5d260907c02db203b2929af47. Warrior is always available and has no unlock badge. Mage actually unlocks after using a Scroll of Upgrade, not identifying an Identify scroll; Rogue requires ten surprise attacks, Huntress ten thrown attacks, Duelist eligible tier-two-or-higher equipment without a strength penalty, Cleric fully cleansing cursed equipment.

## Small correction
The common badge presentation path maps only the five character-unlock badges to their actual HeroClass availability. If playable already, record the earned badge normally without the log notification or badge popup. Keep every validator, threshold, badge persistence, seeded-run policy, ordinary achievements and HeroClass availability unchanged. When a class genuinely remains locked under the original non-INDEV policy, its existing announcement still runs. No player data reset, migration, mechanics, scoring or balance changes.

## Local evidence and pending verification
Five permanent host regressions pass: all-six fresh alpha availability; both identified/unidentified Duelist equipment and restored badge bundles; mapping/retained original locked policy and unrelated badges; seeded-run non-awards; actual announcements for genuinely locked classes. The original fresh-player test fails with all five unwanted log notifications; corrected tests pass 5/5. Test state is isolated and restored. The existing native real-run method now repeats actual validators with fresh/restored badge bundles, asserts all six remain available and no announcement is dispatched, and restores all run/badge/log state.

Full required checks for the next uniquely numbered APK are pending: 1,761 core, 37 required native Android methods, 57 screenshots, debug compilation, compiled immediate-predecessor history, signing/checksum and private delivery. No new APK or physical-phone success is claimed yet. Published verified TEST261 remains fallback and contains the separately verified donation-promotion correction. The new fix is not in TEST261.

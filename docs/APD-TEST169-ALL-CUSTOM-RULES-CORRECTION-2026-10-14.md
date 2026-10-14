# TEST169 headless acceptance correction — 2026-10-14

## Failed evidence retained
- TEST168 source `3a70f85f706dcf56553af296c1a44f98635a5af7`, run `38627408866`, ran 1,585 core tests with 1 failure. The complete-history manifest guard passed; no APK/API34/screenshots were produced.
- The real 75-damage enemy hit correctly passed attack, exactly-once scaling and 500% Player Defense, then the normal serious-damage feedback called `Hero.interrupt()`; that UI helper requires a live `GameScene.cellSelector`, which a core fixture intentionally lacks.
- TEST169 makes only the headless `FixedHero.interrupt()` a no-op. It does not bypass `Hero.damage()`, attack hooks, defense, source tracking, healing or assertions.

## TEST169 request
- Verify all 1,585 core tests, debug assembly, complete reviewed history against immediate previous compiled TEST166, 26 API34 emulator tests and 34 screenshots.
- All 18 controls remain candidate-only until these checks pass. Active-run settings remain locked; original scores/scoring identity remain unchanged.
- No delivery, public release or physical-phone claim.

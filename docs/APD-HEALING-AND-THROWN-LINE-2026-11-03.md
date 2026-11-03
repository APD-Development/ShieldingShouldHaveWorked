# Healing Off shortcut and clear thrown line (2026-11-03 UTC)

Owner reports TEST350 Off(one tap) does not work and requests throwing at visible enemies with clear line of fire, without bending around corners.

## Confirmed Off cause and correction

The setting callback already saves -1. However, WndCategoryHotbar.open always opens an item chooser; its direct-drink check existed only after selecting an item from that chooser. Therefore Off did not provide a one-tap quickslot even when the slot had exactly one known healing stack. Move this unambiguous shortcut before menu construction. Keep ambiguous, unknown, non-healing and enabled-threshold selection behavior; no change to potion quantities, healing, turn costs, RNG, storage or preference defaults.

Native TEST350 reproduction41564748055 failed exactly: expected quantity1 after actual Off button + actual healing quickslot tap, observed2 with chooser present. The same case checks threshold setting, Save, warning Cancel (no HP/item/turn change), confirmed normal use, and actual Off preference -1 first. Earlier41564119872 passed the real threshold/confirmation flow;41563626330 failed in test setup by reading preferences before Android startup, corrected without application change. These are separate results, not passed Off acceptance.

## Confirmed thrown-line case and correction

Original unenchanted ThrowingKnife failed with hero(2,2), enemy(6,5), wall(4,4): enemy was visible through actual ShadowCaster; forward Bresenham collision99, target166; reverse ray reached hero66 without collision. The same geometric straight ray can round onto different neighboring grid cells in opposite directions. For ordinary thrown weapons only, retain the original forward collision unless a currently visible enemy has a fully clear reverse projectile ray. This never computes a route around a wall or ignores an intervening creature. Unseen targets retain normal rules. Original Spirit Bow and Projecting enchantment behavior is preserved; no changes to range caps, damage, accuracy, durability, turn costs or combat RNG.

Six real-physics unit tests cover the originally failing tie case, long straight/diagonal visible shots, blocked corner, intervening creature, unseen enemy and original Projecting bypass. The initial fixture needed blobs/traps initialization; fixture errors were corrected before recording the actual throwing assertion failure. The clear-ray assertion failed before the production correction, then all six passed.

## Verification boundary

Local complete core suite:2311 tests,0 failures/errors. Three new Off policy cases protect the single-stack shortcut, ambiguity/non-healing items, and unknown/enabled-threshold exclusions. Native healing test uses actual taps and restores all original hotbar/preferences. Full required Android debug/release/desktop compilation, full prior69 API34 cases plus native Off case, six display profiles and paired tester checks must complete before publication. No phone acceptance claim. Source/build artifacts are not yet prepared or delivered for this correction; retain350 fallback.

Owner cap:3 Developer plus3 Alpha-Testers APKs per New York day. Prepare one351 pair, not another series of application builds. Preserve all350 release assets/signing/application ID and matching source process.

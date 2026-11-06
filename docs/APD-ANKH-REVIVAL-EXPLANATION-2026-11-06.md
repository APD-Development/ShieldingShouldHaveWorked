# Blessed Ankh revival explanation — source candidate

Prepared: 2026-11-06T20:00Z
Parent: c982143c3ff1c5bbeb0c1651bb02ffea426ae7f0
Status: pending remote source verification

The live blessed-Ankh path previously assigned `HT / 4` directly inside `Hero.die`, while the detailed Ankh panel independently repeated that arithmetic. This candidate exposes the same integer-division expression as `Ankh.blessedReviveHP(maximumHP)` and uses it at the original live assignment point and in the read-only panel. No operation order, side effect, source attribution, turn, RNG draw, cure, invulnerability, item consumption, save data or balance value changes.

The panel now states the approved rule explicitly: an Ankh revival is a revival event, not ordinary HP recovery, so the Custom Dungeon Rules Healing percentage does not multiply it. A new regression covers Healing 0%, 25%, 100% and 200% across maximum HP 20, 21, 99, 100, 101 and 1000, checks exact integer truncation and confirms inspection does not execute the revival.

Required evidence: exact 2,859-test/446-suite core inventory with zero failures/errors/skips plus Android debug/release/instrumentation Java and desktop compilation. No APK assembly or native/phone claim. TEST369 remains the current delivered release and does not contain this later source candidate.

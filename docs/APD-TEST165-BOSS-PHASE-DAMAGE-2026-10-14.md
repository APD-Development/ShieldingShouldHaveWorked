# TEST165 boss-phase damage acceptance candidate — 2026-10-14

## Scope
- Started from verified TEST164 application source `166c8a287b599fcb0f741a446f039fc33f656201`, run `38612344678`, with actual branch head `7a4ff110e04f0005d5afe06bad9a67486d13c81d`.
- Audited every direct `.damage(...)` site in Goo, Tengu, DM-300, Dwarf King and Yog-Dzewa before changing source.
- Goo already delegates offensive rolls through the normal attack pipeline and needed no source change.

## Implemented
- Tengu bomb damage now applies the captured boss factor before its existing armor roll; the original `Bomb.class` source, roll, score effects and timing remain unchanged.
- Tengu fire and shocker fields now retain per-cell single or mixed creator provenance through decay and save/reload. Burning attachment and electric damage use that provenance; historical fields without metadata remain unknown and unscaled.
- DM-300 delayed falling rocks, Dwarf King summon-site collision damage and Yog-Dzewa death rays now apply their actual boss creator exactly once while retaining their original source objects and RNG calls.
- Dwarf King self-damage from `KingDamager` remains unscaled. No trap, starvation, environmental or Chalice behavior changed.
- Added retained acceptance covering all six current hero classes, Goo/Tengu/DM-300/Dwarf King/Yog classification, Tengu field save/legacy behavior, direct source boundaries, mitigation order and preserved self-damage.

## Verification state
- Expected complete core total: 1,581 tests (four new tests). Exact-source core, debug APK, immediate-previous TEST164 compiled history, 26 API34 checks and 34 screenshots are pending GitHub verification.
- Three Damage controls remain disabled pending successful evidence and the remaining high-HP/defense, save and cross-rule acceptance work.
- Private candidate only: no delivery, release or physical-phone claim. TEST164 remains the latest verified application source and TEST123 remains the owner-delivered/signature-compared fallback.

# TEST162 harmful-buff attachment provenance candidate

Prepared 2026-10-14T17:53:07.187Z from actual branch head `7fd4e51d4ce6137d9c1baf84f39ac3dceb604fa4`, after verified TEST161.

The deferred pre-attach boundary is implemented without changing buff source objects, mitigation, turn costs or RNG. Newly created offensive Burning, Chill and Frost effects now carry immutable creator metadata while `Char.add(Buff)` is executing. Vault boss elemental form reactions scale that one immediate damage event exactly once, then discard transient attachment metadata. Burning fields prepare their saved single or mixed timed cause before attachment. Freezing fields now retain per-cell mixed ownership through decay and save/reload; old or environmental fields remain unattributed.

Updated known player/enemy/boss callers include fire/frost wands, imbues, armor and duelist abilities, cursed-wand effects, elemental and Yog effects, frost bombs, Frost potions, weapon enchantments and darts. Trap and room hazards remain environmental. No actor reference is serialized or retained. No historical missing origin is assigned a modern default.

Five retained regression methods cover player Burning, boss Chill, mixed saved Freezing fields, legacy environmental Freezing and neutral amount/source/RNG identity. Existing source-contract tests were updated to the pre-attach entry points. Static source audit found zero remaining production `Buff.affect(... Burning).reigniteAttributed...` attachment-after-attribution patterns among the inspected current sources.

No local Gradle repository workspace exists in this execution environment. Required exact-source verification is therefore pending: 1577 core tests, debug assembly, compiled history against immediate previous TEST161, 26 API34 checks and 34 valid screenshots. Three Damage controls remain disabled. This is a private candidate only; no delivery, release or physical-phone claim.

# TEST140 toxic-gas source package

Nine production files and12 permanent tests. Optional per-cell source contributions retain player/enemy/boss/environmental ownership through toxic-gas diffusion and saves. Mixed damage is applied once using actual density contributions; boss contributions use Boss Damage instead of Enemy Damage. Original density, diffusion, volume, turns, RNG, source object, immunity, mitigation and death logic remain unchanged. Legacy metadata stays unknown; invalid metadata is rejected without rewriting saved data.

Actual creators are supplied by DM200, delayed DM300, RotHeart, Rotting Fist, Stench, cursed-wand gas and delayed Censer. DM300 captures before scheduling. Censer persists its role, including corrosive clouds, without changing the strongest-corrosion-source rule. Potion throw attribution already exists. Environmental rooms and traps remain unowned.

Causal diffusion is prepared before damage and published afterward: a regression verifies gas seeded during damage does not acquire output computed before its creation. Maps are lazy, double-buffered, released on expiry, and use a reusable per-sweep accumulator. No production UIMSD compression changes.

12 targeted checks passed0.330s. They cover all source percentages, one-event overlap,8 neutral diffusion ticks and RNG identity, save/load, legacy absence, clearing, invalid metadata, callback ordering, and actual emitting callers. The headless DM300 fixture overrides only its absent UI interrupt hook; production interruption is preserved. Final broad regression result is recorded below. Full repository/Android verification is pending; expected1491core/26API34/34PNG/debug/history.

TEST139 source375adeb094460949f71537547037bb062e200f5e/run38495415452 passed1479core and26API34,0failure/error/skip,34valid PNG. Before/after score scrolling visually compared. APK artifact11863736166; core11863656438; Android11864386218. No physical-phone, new signer/checksum comparison or release claim.

Remaining: fire/electric and other persistent effects, untracked planted effects, specialized/chained bombs, abilities and derived damage, combined class/boss acceptance. Three Damage controls remain disabled. No new score formula, ranges, raw-history edits, inventory changes, signing/application ID changes, main overwrite, online activation or background Codex claim.

Final local broad regression:633checks passed in19.295s after the final causal-ordering change, with0failures. Current-source repository CI still required.

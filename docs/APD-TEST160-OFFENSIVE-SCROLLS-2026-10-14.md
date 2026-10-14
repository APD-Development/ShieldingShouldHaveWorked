# TEST160 offensive scroll damage candidate

- Execution start: 2026-10-14T16:32:59Z
- Candidate prepared: 2026-10-14T16:36:20.526Z
- Base head: `62b3027ffdfa46dd9fd67c557e7249257d6e180d`
- Read all 99 current artifact, potion, scroll and spell Java sources and classified direct damage, harmful effects, delayed fields and environmental boundaries.
- Confirmed Scroll of Retribution and Scroll of Psionic Blast were player-owned direct enemy damage paths still calling `mob.damage` directly.
- Candidate routes only their enemy damage through the existing Player Damage boundary. Original damage formulas, source objects, target collection, blindness/weakness, turn flow and RNG remain unchanged.
- Psionic Blast's hero self-damage remains deliberately unscaled. Chalice self-damage, starvation and unowned traps/environment remain excluded.
- Four retained tests cover all 20 authorized percentages, exact-once scaling, neutral next-RNG/source identity, both caller guards and the self-cost exclusion.
- No score, save format, inventory, signing, application ID, release, delivery or slider-enablement change. This connector-only workspace has no local Gradle checkout.
- Exact-source 1564 core/debug/immediate-previous TEST159 history/26 API34/34 PNG verification is pending. Three Damage controls remain disabled; harmful-buff pre-attach work and all-class/boss combination acceptance remain separate.

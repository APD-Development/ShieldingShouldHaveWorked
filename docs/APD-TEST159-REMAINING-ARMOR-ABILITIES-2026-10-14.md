# TEST159 remaining armor-ability damage candidate

- Execution start: 2026-10-14T15:32:44Z
- Candidate prepared: 2026-10-14T15:35:09.392Z
- Base head: `9268f8daf939ae75439078370c63d90634eaa6c5`
- Heroic Leap Body Slam and Shockwave now apply Player Damage before their existing manual armor roll, preserving armor as a fixed mitigation step. Their original source objects and attack-proc ordering remain unchanged.
- Heroic Leap Impact Wave captures the player before delayed Blast Wave collision damage.
- Warp Beacon applies Player Damage to the enemy telefrag only. Its matching hero HP/shield self-cost remains deliberately unscaled.
- Read-only classification confirmed Wild Magic delegates through verified wand paths, Spectral Blades through `Hero.attack`, and Spirit Hawk/Shadow Clone through saved inherited player ownership.
- Five retained tests cover all 20 percentages, fixed pre-mitigation armor, neutral next-RNG identity, exact caller ordering/completeness, self-cost exclusion, and delegate/inherited-owner guards.
- No score, save-format, inventory, signing, application-ID, release, or slider-enablement change. This connector-only workspace has no local Gradle checkout.
- Exact-source 1560 core/debug/immediate-previous TEST158 history/26 API34/34 PNG verification is pending. Three Damage controls remain disabled; the pre-attach harmful-buff reaction remains separately deferred.

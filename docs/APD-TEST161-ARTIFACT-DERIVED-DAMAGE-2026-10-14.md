# TEST161 artifact-derived damage and Corruption save-integrity candidate

- Execution checkpoint: 2026-10-14T16:52:55.080Z
- Base head: `27835d898d1a09a462c0cc49f8cfc3915d945497`
- Reconciled and preserved the independent Corruption fractional-save correction already on the branch. TEST160 does not cover that later source change.
- Guiding Light damage from artifact procs now uses the actual hero as Player Damage origin while retaining `GuidingLight.INSTANCE` as the original source.
- Cape of Thorns still samples one deflection roll and reduces incoming damage by that original amount; only its outgoing retaliation is scaled, with the cape buff retained as source.
- Skeleton Key's two Blast Wave paths were read completely and remain non-damaging movement because collision damage is false.
- Added four retained artifact-derived tests for all 20 percentages, neutral next-RNG/source identity, exact caller/ordering guards and Skeleton Key exclusion. Together with four current Corruption save tests, expected core total is 1572.
- No score, save-format key, inventory, signing, application ID, release, delivery or slider-enablement change. Exact-source core/debug/immediate-previous TEST160 history/26 API34/34 PNG verification is pending.

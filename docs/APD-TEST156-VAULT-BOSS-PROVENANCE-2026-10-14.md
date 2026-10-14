# TEST156 Vault boss damage provenance

Status: application candidate; exact-source verification pending.

## Scope read and classification

Read the complete current VaultBossElemental, VaultSentry, VaultFlameTraps and VaultLaser implementations, including their delayed buffs and save/restore paths. Vault Sentry scans, laser beams and flame machinery are quest hazards with hazard-freebie/quest-score behavior; they remain unattributed environmental damage. Vault Boss Elemental is a BOSS actor, so its retaliation, fireball field and direct Burning, moving firewall, frost cone, lightning bolt and delayed lightning chase use Boss Damage instead of Enemy Damage.

## Implementation

Four direct-damage sites now enter CustomOffensiveHooks exactly once while retaining the original Shocking, Frost or Electricity source object. Fireball uses attributed Fire plus attributed Burning, and FireWall uses its saved Buff target (the actual boss) for delayed Burning. FrostCone and LightningChase likewise use their saved Buff target, so ordinary actor/buff serialization retains the creator without a new save field. Damage rolls, mitigation, paralysis/frost/burning duration, attack geometry, quest scoring, turns and RNG calls are unchanged. Neutral 100% follows the existing identity path.

VaultSentry, VaultFlameTraps and VaultLaser were intentionally not changed. The elemental's incoming harmful-buff reaction at VaultBossElemental.damage/add is not silently reclassified in this batch; its player-derived ownership still needs the wider derived-damage audit.

## Retained regressions

VaultBossDamageProvenanceTest adds four methods: concrete boss replacement across all 20 percentages while Player/Enemy differ; neutral source/amount/next-RNG identity; every known Vault boss caller boundary; and explicit preservation of the three environmental machinery paths.

No local Gradle checkout exists in this connector-only execution workspace. Compilation, 1551 core tests, debug assembly, immediate-previous TEST155 APK history, 26 API34 emulator tests and 34 screenshots are pending exact-source CI. Three Damage sliders remain disabled. No private delivery, signer comparison, physical-phone claim, scoring change, application-ID change or save-format change.

# TEST150 Inferno and fire-transfer provenance

Status: candidate implementation; exact-source CI pending.

## Scope

- Inferno now retains immutable PLAYER/ENEMY/BOSS/UNATTRIBUTED mixtures through seed, diffusion, save/restore, direct Burning, and neighboring Fire creation.
- Infernal Brew captures its actual thrower once and uses that origin for every seeded Inferno cell.
- Chaotic Censer's existing delayed saved origin is consumed by Inferno through the existing lexical effect context.
- Attributed Burning transfers the ownership mixture for its current damage tick when it ignites flammable terrain.
- Legacy Inferno saves without the additive metadata restore as UNATTRIBUTED; no attacker object is retained.

## Preserved behavior

The change does not alter field density, diffusion, terrain destruction, duration, turn ordering, damage source objects, mitigation, or RNG calls. At neutral 100% it is intended to be behaviorally identical. Unowned environmental Inferno and fire remain unscaled.

## Retained regression coverage

`InfernoDamageProvenanceTest` covers all 20 percentages for PLAYER/ENEMY/BOSS direct Inferno contact, exact-once scaling, source identity, Inferno save/reload and legacy fallback, neighboring Fire transfer, Burning-to-terrain transfer for all three roles, neutral density/volume/next-RNG identity, and production wiring for Infernal Brew and delayed Chaotic Censer.

## Verification record

- First independently captured clock during this bounded invocation: `2026-10-14T11:39:59Z`; earlier active duration is unknown.
- Source was read from real branch head `116fc601549e83e02742b8541d3767f559ee2d85`.
- Candidate application commit: `57b3b5706f7478f742164d11c5a5ea74de94ed36` (expected-head lease from `116fc601549e83e02742b8541d3767f559ee2d85`).
- Local source inspection and exact replacement guards completed. A real local Java/Gradle repository is not present in this execution workspace, so no local Java test result is claimed.
- Full core, debug APK, compiled immediate-previous history, API34 emulator, and screenshot evidence remain pending until the exact application source is requested through the approved existing workflow.

Three Damage controls remain disabled. Firebloom, vault/environment boundaries, remaining offensive sources, and complete class/boss/combination acceptance are still unresolved.


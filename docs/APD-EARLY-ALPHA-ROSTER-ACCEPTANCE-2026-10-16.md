# APD early-alpha six-class Android acceptance — October 16, 2026

Status: candidate test gate; remote evidence pending.

## Scope

This instrumentation-only gate reuses the exact verified TEST241 APK from source `b5d562ca37fff9ac628af3f4ade731e9fa7d83f1`, run `38894980574`, artifact `12043946029`, SHA-256 `99ca269dbebcd19266dbf4e27aca798252ed3d3b021f97b730915cbbea52b7f4`.

On API 34 it must start, save and continue one disposable run for each shipped class: Warrior, Mage, Rogue, Huntress, Duelist and Cleric. Each run uses the same non-neutral 18-value rules snapshot, difficulty 5 and all nine challenges. The check requires usable gameplay controls before and after reload and proves that neutral next-run preferences cannot overwrite the locked saved-run identity.

The gate also reruns the existing headless combination, five-main-boss phase/provenance and boss-health acceptance classes.

## Boundaries

This is not a new application build, candidate APK, release or tester distribution. The workflow installs the existing verified TEST241 APK and compiles only the current instrumentation package. It is emulator evidence, not physical-phone evidence. It does not claim full playthrough completion, boss kills, audio acceptance, or resolution of the unconfirmed armor report.

TEST241 remains the verified application source. TEST231 remains the latest privately published fallback until a later publication is independently confirmed.

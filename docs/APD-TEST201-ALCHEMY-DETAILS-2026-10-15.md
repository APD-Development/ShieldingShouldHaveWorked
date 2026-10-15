# TEST201 — visible alchemy craft details

Timestamp: 2026-10-15T09:14:47Z

Status: candidate pending hosted verification.

## Reproduced gap
The existing successful-brew path already sends ingredient-to-product lineage to the INTERNAL item recorder, but no PUBLIC/queryable observation identifies the craft the player just completed, its visible inputs/output, output quantity, floor, or energy split.

## Bounded change
- Add public telemetry 1.14 fact `alchemy.craft` only after `Recipe.brew` succeeds.
- Use the existing pre-brew ingredient duplicates so quantities and displayed names survive recipe mutation.
- Record displayed ingredient/result names, result quantity, floor, total energy, main-pool energy, and Alchemist's Toolkit energy.
- Keep true unidentified classes in existing INTERNAL item history. Failed brews and restore-time reconstruction record nothing.
- Preserve the existing `ApdItems.onCrafted` lineage call unchanged.

## Retained verification
Two new core methods cover the successful public payload/privacy boundary and failed/restore-time omission. `PlayWiringTest`, the fact manifest, telemetry version contract and permanent records history pin the integration.

Requested gate: 1,615 core tests, debug APK assembly, immediate-previous compiled TEST200/Codex TEST 196 history, 27 API34 methods and 35 valid 320×640 screenshots.

No recipe, inventory, energy, RNG, gameplay, save, score, custom-rule, application-ID, signing, delivery or physical-phone behavior is changed.

## TEST201 hosted result — failed contract
Source `776910414b7574b42cffa0d61e9bb7d4f1e83ca7`, run `38711834653`, executed all 1,615 core tests: 1,614 passed and `ContractTests.recordsHistoryDescribesTheCurrentTelemetryVersion` failed. The new milestone was placed before the required `APD records history, format 2` header.

History preservation/validation passed. APK assembly and API34 were skipped; no TEST201 APK exists. Failed core artifact `11960867915`, 752,666 bytes, SHA-256 `e040a5f4330624fd74a0739cf914eb553d34957dcda5d8464acb39f9e6f8befe`.

TEST202 moves only that milestone below the immutable format preamble; production source and all assertions remain unchanged.

# UIMSD hero-damage → healing correlated window

Date: 2026-10-23. Branch: `codex/apd-phone-fixes-2026-10-09`.
Core-verified source: `783d1d0c6caa07a2bb43abe9428d77a97b058d04`.

## Implemented scope

The Research lab and exact hero-damage row actions can open **Healing after damage**. The player chooses 1, 5, 10, 20 or 50 turns. The analysis uses the same case-sensitive run and the shared `RunTurnWindow` definition: strictly after the selected damage turn through damage turn + N inclusive.

It totals only exact recorded HP-healing rows and exposes the exact anchor and contributing rows through the existing evidence/export/navigation system. Same-turn healing is excluded because the recorder does not establish ordering within a clock tick. Damage-table filters select the anchor and are not silently translated into Healing filters.

The value is visibly an observed lower bound. Missing coverage is unknown, not zero. Zero exact observations does not assert that no recovery happened. Healing is not shielding or max-HP growth, and the result does not claim that selected damage caused subsequent healing.

No event, save, raw-history, query format, score, RNG, combat, healing or turn behavior changed.

## Regression evidence

Initial run [40031186659](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/40031186659) compiled production, then stopped before JUnit because the new negative-path test used nonexistent fixture constant `KIND_MOB`. Raw failure evidence is retained. The test-only correction uses existing `KIND_ENEMY`.

Corrected run [40031443456](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/40031443456) passed:

- 2,064 methods / 400 suites
- 0 failures / 0 errors / 0 skips
- all five `DamageRecoveryWindowTest` methods
- all six shared `RunTurnWindowTest` methods
- all three Back-latency tracing regressions
- all 2,059 predecessor methods

Focused cases cover exact exclusive/inclusive boundaries and case-sensitive run identity; binary writer/close/reload/query with byte-identical raw history; query-codec and evidence export; non-hero, uncertain and overflowing anchors; missing coverage versus lower-bound zero; and uncertain healing exclusion.

Core artifact: `12534544189`, 974,351 bytes, ZIP SHA-256 `15ebb824bef2e4c4c120fc676ba08222a8719c4ae31802222d79823a58360b15`.

## Remaining acceptance

Native/API34 menu, row-action, repeated-window, evidence/Back and screenshot acceptance remains pending. No APK was requested or produced. TEST310 remains the latest fully emulator-verified private developer/player pair; TEST262 remains the latest actually owner-published APK. Physical-phone acceptance is unclaimed.


## Native candidate acceptance request (2026-10-24)

Added an API34 pointer test using a real persisted binary run log. It enters Research lab, chooses Healing after damage, selects the recorded rat hit, verifies the 10-turn lower-bound total of 7, restores the window after scene recreation, captures `uimsd-damage-recovery-window.png`, opens the exact two healing contributors, confirms QueryCodec/evidence navigation and byte-identical raw history, then reopens the analysis through the damage-row action and verifies the 5-turn total of 4.

The complete native inventory is now 54 methods and 80 required PNGs. Acceptance remains pending until the intentional matched private candidate finishes full core/debug/API34/visual/history/signing gates. The test does not substitute for physical-phone Back-latency measurement.

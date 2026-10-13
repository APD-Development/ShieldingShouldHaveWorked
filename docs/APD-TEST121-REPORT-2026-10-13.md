# TEST121 preparation — October13,2026

Baseline actual produced APK: TEST120, source d017bd3009b40bd015779e68f43d19f898bc412b, verification run38396459334. Current parent includes creator-approved lore curation26bae9b49aa7190cce24cadc7cce71cc9c6752c2. This package stages an APK only; it does not distribute to alpha testers.

## Implemented

Healing Effectiveness follows the locked run setting for player HP recovery: potions/delayed recovery, natural regeneration, sleep, dew, meal talents and foods, class recovery, enchantments, Chalice regeneration, sungrass, aquatic elixir, wells and escape/full-heal effects. Each recovery source is wrapped once before its final HP cap; non-player recovery, shielding, starting HP, maximum-HP increases and Ankh revival remain original. Neutral100%returns original recovery without additional RNG. Small quarter-step recovery fractions accumulate in an optional Hero save field; overhealing is never banked. Invalid fractions are rejected without rewriting the input save. Source-specific resource budgets and hunger costs stay intact.

Confirmed interaction:0%recovery could otherwise leave restorative sleep unable to reach full health and wake. The zero-healing path ends restorative paralysis safely. Regression covers both new attachment and an existing sleeping buff. No combat timing, Chalice prick damage, earned-score formula or slider boundaries changed.

Fourteen gameplay effects now have source implementations. Player Damage, Enemy Damage, Boss Damage and Equipment Availability remain disabled/incomplete. Attribution groundwork is not complete gameplay. Mandatory permanent UIMSD recording/filter/export/scoring interface acceptance remains pending; broad audit remains paused per owner. No claim of alpha completion.

Cleric content cleanup in parent:348to50entries,298retired,49counted+1meta. Eight complete story pairs; no unavailable fact dependencies in retained entries. IDs/wording of retained entries unchanged, old ledger records preserved. Lore feature remains off. Full IDs in private curation document; no secret mechanics exposed.

Android test correction: TEST120's third narrow-scrolled image actually showed bottom sliders, not the scoring paragraph. Corrected target measures the paragraph itself. Previous wide warning image is valid; previous third capture is not proof of paragraph visibility. New image review is required.

## Verification before commit

Final local healing/combat/Chalice/telemetry/source checks:56passed,0failed,2.303seconds. Seven new healing methods include all six classes' actual healing/save paths at0/25/100/200%,all nine settings/fraction accumulation,neutral RNG,dew/HP caps,shield isolation,sleep safety and resurrection. Lore package:82passed,0failed,2.007seconds,three new methods. These are separate targeted suites, not a complete game test run. A local JUnit invocation initially named Chalice's wrong package; corrected command passed without changing tests. Java compilation succeeded with existing unchecked aquatic-effect warnings.

Full required core suite(expected1370unique),22API34instrumented checks,25PNG and debug assembly are pending CI. Expected totals are not claimed passed. No physical RedMagic/Android16 playthrough, all-class full runs or exhaustive five-boss combined-modifier playthrough completed. No new signing/application-ID/checksum publication receipt yet. Existing data and signing files untouched. No APK delivery claimed.

## Timing and risks

This resumed work began after the owner asked to continue on October13morning New York. Local final suite elapsed2.303seconds; previous lore suite2.007seconds. No reliable total active/idle metering exists. CI timing will be recorded from actual run timestamps. Healing rounds down whole HP while retaining fractional remainder; zero recovery removes HP healing benefit but retains other effects. Existing saves without the optional fraction load zero; no historical run settings or scores are rewritten. Unknown active historical rules remain unknown in statistics.

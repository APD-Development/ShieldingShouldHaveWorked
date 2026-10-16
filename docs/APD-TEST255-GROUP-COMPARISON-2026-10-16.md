# TEST255 UIMSD repeated-label group comparison correction — 2026-10-16

## Controlled reproduction

The current report comparator converted group B's detail lines to a map keyed by the player-facing label. Dataset definitions allow the same label in different sections, so the later line replaced the earlier one. A supported two-group fixture with two separate statistics both labeled `Average` expected group B values `20` and `200`; the first row instead displayed `200`.

Diagnostic commit `df6d2a4f0e898f52c0e1227ed19c7c71b23b2c63` ran the one-method regression in workflow [38964285358](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/38964285358). JUnit recorded one test, one failure and zero errors: expected `20`, received `200`. Artifact `12073194262` (ZIP SHA-256 `137c7a8b59b13049b65e0bee815c57069668bb0bb1f69dcb67d95479d7ea9669`) preserves the XML and HTML report. The diagnostic workflow itself concluded failure because its post-check expected JUnit's compact diff text rather than the actual bracketed diff; this does not relabel the retained application assertion as passed.

## Bounded correction

`ReportBuilder.compareGroups` now pairs the two detail sheets by their shared dataset-definition order. Section headings remain omitted from the comparison table, repeated display labels remain supported, and a missing counterpart still displays unknown. No raw history, query result, cache, save, gameplay, RNG, turn, slider or score calculation changes.

The retained regression remains in the normal core suite. The full candidate gate must compare the immediate previous compiled TEST254 source `832c1a5679a8cd216e8c57638f1bc0fe2442fdfe` / run `38949555542`, verify all reviewed history paths, build debug, and rerun the existing API 34 visual/play acceptance. Until that completes, this is a pending source candidate; privately published TEST254 remains the playable fallback. No physical-phone, owner-save or automatic tester-distribution claim.

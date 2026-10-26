# UIMSD comparable-condition progress foundation — 2026-10-25T22:11:36Z

## Result

Exact source `a771a64087d1c926c2057d66a1f10946577b9f0e` passed the complete core suite in [run 40238803735](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/40238803735), job `121305970033`: 2,077 methods in 403 suites, zero failures, errors or skips.

Retained artifact: `12615950934`, 977,681 bytes, digest `sha256:bbbf22c54c3840d8d8e70276d76a1f9fb46a29e4ece7c6b152332e2097c197c1`.

## Accepted core behavior

`ComparableProgress` accepts two explicit, ungrouped Runs queries and a metric. A comparison is available only when:

- exact metric contributors have distinct run identities across cohorts;
- every contributor has an exact positive recorded start time;
- the earlier cohort ends before the recent cohort begins;
- every contributor has the same known, unmixed rules identity;
- APD version, class, difficulty, balance revision, challenges and exact Custom Rules identity are recorded exactly and match.

The numeric direction is recent minus earlier. The interpretation explicitly says the result is observational and is not proof of improvement or causation. Unknown conditions, missing dates and incomplete metrics never become current defaults or zero. PopulationStudy retains the existing minimum-sample, overlap, independence, version/rules and uncertainty gates.

## Retained regressions

1. Exact comparable cohorts produce recent-minus-earlier with exact ranges and contributor counts.
2. Shared runs and reversed/interleaved dates fail closed.
3. Changed difficulty and unknown exact Custom Rules identity fail closed.
4. Missing dates and incomplete metrics remain unavailable rather than being inferred.

## Boundaries

This is a read-only core/model foundation. It adds no UI, export, telemetry, gameplay, save/history migration, raw-history mutation, scoring change, Custom Rules change or RNG use. No APK was built. TEST313 remains the newest emulator-verified developer/player APK pair in GitHub Actions artifacts; TEST262 remains the newest APK uploaded to GitHub as a release. Physical-device behavior is unmeasured.


## Evidence/export checkpoint — 2026-10-26T14:07:14Z

Exact source `7c2d46edae9bc884bac1bd1309259249be2335ba` passed the complete core suite in [run 40336065670](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/40336065670), job `121617849976`: 2,080 methods in 403 suites, zero failures, errors or skips. Retained artifact `12655929494`, 979,736 bytes, digest `sha256:21acd6482b14a33c419b5828f2a876e43d953efe6a6dfd333859dfa6ef4512d7`.

The verified model now produces one read-only `ResearchDocument` shared with the existing report exporters. It contains recent-minus-earlier, exact UTC date ranges, exact matching rules/version/class/difficulty/balance/challenges/Custom Rules, sample and exclusion counts, uncertainty, both exact query encodings, separate contributor evidence, and all matching cohort rows. Unknown or uncertain metric rows remain visible in cohort-definition evidence but are not contributors and never become zero. Failed comparisons contain the exact rejection and no contributor evidence.

Three new tests retain the exported disclaimer and run identities, reject unavailable conditions without evidence, and distinguish all matching rows from exact contributors. No native screen or persistence entry was added in this checkpoint. No APK was built; TEST313 remains the newest emulator-verified pair in GitHub Actions artifacts and TEST262 remains the newest APK uploaded to GitHub as a release.

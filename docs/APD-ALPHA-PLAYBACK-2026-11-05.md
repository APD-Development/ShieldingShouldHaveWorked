# Alpha Testers visual playback speeds — November 5, 2026

## Request and root cause

The owner approved the screenshot's full Game speed selector for Alpha Testers. Previously both choice validation and the settings window gated the seven extra speeds behind the Developer testBuild flag, so Alpha Testers had only0.5x,1x,2x.

## Changes

The Alpha variant now packages only an extendedPlayback feature flag and exposes the same saved scrollable0.5x,1x,2x,4x,8x,16x,32x,64x,100x,1000x selector as Developer. No Developer switches are enabled in Alpha Testers. Ordinary non-alpha builds keep the original three choices. Default1x and existing saved choices remain unchanged. This controls visual playback, not turn costs, combat speed rules or game-clock/input timing. Existing extreme-speed hero-idle/audio safeguards remain intact.

The candidate also preserves the newer source-verified UIMSD raw-content identity correction at131e6551a: same-length history replacements with restored timestamps invalidate stale warm catalogs. This work was already on the approved remote branch and must not be overwritten. All door eligibility and prior owner features remain included.

## Potential regressions and limits

Extreme playback can make movement/effects visually abrupt, especially1000x; this is the approved existing Developer behavior. Alpha packaging is verified to contain only the playback flag, with all shared assets matching Developer. Saving/loading speed preferences and ordinary-build gating are covered. Physical-phone performance and install-over acceptance remain pending.

## Verification

Targeted playback tests pass, including an added Alpha-only flag regression exercising every factor, file-backed preference reload, unchanged global frame delta, and no Developer unlocks. Full core and required Android/debug/release/desktop checks plus full native/display/actual-tester checks must pass on the exact candidate before publication. No candidate is called verified based solely on this preparation report. Retain TEST364 and its assets as last verified fallback.

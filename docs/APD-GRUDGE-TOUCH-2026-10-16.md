# Grudge settings touch regression — 2026-10-16 (candidate, not yet verified)
Owner screenshot shows Grudge open over APD Settings; frequency, location checkboxes and history confirmation did not respond to taps. This report does not assert a failure of Back or window dismissal.

## Supported cause
WndClericGrudge registered all its interactive controls before constructing ScrollPane. ScrollPane creates a full-area PointerController, which then receives the first touch and swallows clicks; the earlier settings pane WndAPDSettings explicitly fixed the same order. No evidence indicates a preference-storage, lore selection or UI-rendering failure.

## Bounded correction
Construct the ScrollPane directly after the empty content Component, BEFORE any RedButton/CheckBox is created. Fill and size content exactly as before, then resize/window/place the already-constructed pane in the original order. No changes to Grudge frequency rules, location defaults, humor text, message counters, reset semantics, application ID, saves, history, scoring, RNG or combat.

## Regression and safe test operation
ApdSettingsClickDeviceTest.grudgeControlsRespondToActualTouches opens APD Settings, actually taps the Grudge entry, taps and checks a persisted frequency change, taps and checks the Game startup checkbox, and taps the history-reset control only far enough to show the confirmation; it never accepts the reset. Both changed preferences are restored in a finally block. Core Grudge preferences and prior full-suite tests remain required. Full Android38, core1761, debug/history, existing57 required images and supplemental displays are PENDING until real execution.

Attribution: first-alpha owner screenshot, no physical owner-device reproduction by an automated test; source-level root cause established from the input listener creation order. Current published fallback TEST262. No TEST263 APK is verified or published by this document.

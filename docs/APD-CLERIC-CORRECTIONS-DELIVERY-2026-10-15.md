# Cleric corrections — private APK delivery, October 15, 2026

## Owner-reported problems
Cleric lore did not appear when starting Cleric. Unassigned quick-cast positions 2–4 appeared on screen but did not open the spell picker after increasing the button count; assigning through a long-press in the Tome did work.

## Corrections
The private feature asset now explicitly enables grudge, and the feature gate permits that owner-approved private configuration. Exactly 50 existing lore entries and their permanent IDs remain intact. The first-run message is triggered by Start with Cleric selected, ignores surprise chance/grace, respects opt-out, and is recorded only after display. No collection history was cleared. The owner-only collection PDF is attached to the retained TEST198 private release.

Hidden Cleric tags set themselves inactive; the scene formerly restored only visibility when increasing the count. Inactive components are not updated or given input, so positions 2–4 remained drawn but untappable. The scene now restores active and visible together. The Android regression was corrected to stop manually updating inactive tags, which had masked the failure. It now runs the actual one-to-four transition and injects pointer taps on each empty position, checks picker opening and assignment, and retains normal casting/cost/cancellation/save checks. Required evidence includes the empty-slot picker and four-slot HUD screenshots.

Separate source-bound UIMSD row caches now reject equal-size/name/timestamp raw-log replacements rather than returning stale facts. This only rebuilds disposable caches; raw histories, archives and saves are untouched. The combined candidate also gives simultaneous row/fact cache writers their own temporary files, preventing overlapping writers from deleting one another's completed optional cache. No production raw-history storage format changed.

The builds since delivered TEST198 also retain visible boss-phase telemetry, successful physical-movement command telemetry and their privacy/restore-time exclusions, documented under their exact individual compiled builds in the offline history. They do not change boss behavior, movement turns or scoring.

## Verification and limits
Exact candidate source: 245b21b7064a03b916e68f1992f94ca017f22fc0; verification run38747671347; native TEST204. The attached machine-generated verification is authoritative for completed full core, API34, signing, application ID, source and checksum checks. Publication must refuse unless those checks succeed. This report was prepared during verification and does not substitute expected counts for actual results.

Prior TEST203/sourcea9b9d0b231fda11962598ccd2f6f46027dd8d02e/run38745042577 actually passed1629core+28API34, zero failures/errors/skips, debug/history and36screenshots. Its lore popup image was reviewed readable, but it predates the slot-touch fix and is not the requested corrected delivery. TEST204 is expected to run1631core+28API34 and38screenshots; the delivery process requires actual successful evidence rather than assuming this expectation passed.

Local retained evidence:68focused lore tests pass; all50 unique IDs counted and their eligibility/rendering checked. The new activation regressions fail against the original disabled gate and pass after correction. Cache correction:15focused tests pass;618broader checks pass after recompiling an unchanged current fixture whose older compiled version had inlined an obsolete derivation version. Local broad source guards target baseline121; full current-source CI is required separately.

No physical-phone or owner-save test is claimed. API34 emulator input is automated evidence, not a RedMagic/Pixel acceptance run. No APK install, uninstall or data clear was performed on the owner's device. Install over existing APD; do not uninstall or clear data.

## Risks and remaining work
Watch for stale hidden HUD state when changing spell count, both tag sides, small screens and returning from menus. Existing hidden slot assignments are preserved when lowering the count. Collection rarity remains unchanged (default Rarer and optional Rare); Frequency Off remains Off. Custom-rule earned scoring remains deferred by owner instruction. The full211-component UIMSD audit and the remaining approved backlog are not complete. Original scores, slider ranges, Cleric abilities and balance, signing key and appID are preserved.

## Timing
The owner requested lore investigation, then clarified reenabling and reported the separate empty-slot touch failure during the same active session. Lore TEST203 was fully verified before the touch correction. TEST204 is one subsequent meaningful correction batch, not an unchanged rebuild. Delivery reuses that verified application artifact and performs no Gradle rebuild. Exact workflow start/finish timestamps are retained in GitHub and attached verification; no claim of work during inactive sessions is made.

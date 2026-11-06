# Door-adjacent wall-end markers — November 6 UTC / November 5 New York

## Owner report and reproduced cause

TEST365 screenshots18711–18713 show a corner/end below a known door carrying a checkmark, which disappears after the area on the other side becomes known. The eligibility policy allowed existing doors/barricades to serve as structural side frames of a hypothetical doorway. With unknown backing, the corner was provisionally admitted; when backing became known wall, eligibility rejected it. Search history was not erased. This is incorrect initial eligibility, rather than evidence of lost save data.

A new focused regression reproduces the exact wall-end/known-door/unknown-backing pattern against the original365 source and fails. Corrected code must keep that end and the existing doorway unmarked both before and after exploring the other side.

## Correction

Only ordinary/secret-disguised/decorated stone can be a known structural side frame. Known closed/open/locked/crystal/hero-locked doors and barricades are openings, not structural frames for another doorway. Rendering additionally refuses to host a raised-wall projection on a known doorway tile. Undiscovered neighbors remain unknown: no hidden room connections, secret-door identity or unobserved terrain is inspected.

Save bits and ordinary search behavior remain unchanged. Do not freeze a legitimately invalidated marker or invent historical search memory. Valid stone front faces, southern lips and side faces remain covered; generated-floor verification must preserve actual secret-door candidates. Two older unit fixtures incorrectly asserted that an open doorway was a valid solid side frame; their assertions now enforce the approved wall-end exclusion.

## Verification

Original focused regression failed as expected before the correction. New tests cover legacy search memory, before/after exploration, all four orientations and all six visible doorway states. Full2505 core tests442 suites and Android debug/release/instrumentation Java plus desktop compilation are required; verification status is recorded below only after actual completion. Existing78-floor generation audit remains intact. No RNG, generation, discovery probability, turn costs, player inventory/statistics, signing or appID changes. No physical-phone acceptance claim.

## APK boundary

TEST365 remains the last verified playable private pair and does not contain this source correction. At00:16UTC November6 it was still19:16November5 in New York; the three prior pairs363(withheld),364,365 use the conservative daily assembly allowance. No fourth pair or application build request is created. Preserve all release assets; include this correction in the next permitted deliberate pair. No APK is called fixed/verified from these source checks.

## Optional Search control preference

Owner clarified: make the flip selectable in APD Settings, not mandatory. Add Single-tap Search under Gameplay, off by default to retain existing installs' controls. On: tap performs the normal paid search immediately, including when cancelling an inspect/target selector; hold selects a tile/enemy/item to inspect without searching or spending turns. Off: original tap-to-inspect/second-tap-to-search and hold-to-search remain. Long-press release is consumed so it cannot also fire a search, and busy/dead heroes cannot queue one. Preferences persist through actual file reload tests. Existing native play acceptance now exercises tap cost and hold/release no-turn semantics with the option explicitly enabled, and the settings touch inventory owns the new checkbox. Native execution requires the next permitted APK; compilation alone is not called device verification.

## Disappearing-mark boundary

The shown corner's transition is reproduced and corrected without deleting historical bits. Additional regression keeps legitimate stone FACE and OVERHANG markers identical through leaving sight, restoring saved search memory and returning to sight. No separate loss of valid saved marks has been reproduced from the supplied evidence; do not claim every possible disappearance is fixed. The next APK must verify actual render-layer/camera/lifecycle behavior as well as the new input semantics.

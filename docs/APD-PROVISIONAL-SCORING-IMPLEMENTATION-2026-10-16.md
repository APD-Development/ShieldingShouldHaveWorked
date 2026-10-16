# Provisional custom scoring and live previews — October 16, 2026

Requested by the owner with the supplied complete 357-position v2.6R lookup. This introduces active provisional scoring for newly created runs; it is not a calibrated competitive formula.

## Changes

- One engine-independent lookup stores all supplied decimal digits for all 357 positions and 18 sliders. Draft previews, frozen run scoring, UIMSD validation and score display share this authority.
- Every slider shows its individual multiplier; a pinned combined multiplier displays the ordinary product of all 18 draft values. Horizontal dragging updates immediately, before release. Vertical gestures retain scrolling without changing settings; cancelled horizontal gestures restore the starting draft value.
- New-run scoring authority is frozen and persisted with the exact multiplier and calculation version. Original SPD score components, challenge and victory/ascension calculations stay intact. The adjusted long score is rounded once to the nearest whole point and shown/sorted as the earned score; original scores remain separately recorded.
- Old runs without scoring metadata or with original v1 authority retain original earned scores. Editing preferences never reprices an active or historical run. UIMSD permanently retains original and adjusted scores and exact lookup version, available through its existing fields, filters and exports.
- The score breakdown places Custom Rules Multiplier immediately below Challenge Multiplier. Wide values wrap to a separate line rather than overflowing.
- Current scoring notices and player release notes describe active provisional experimental values. The lifetime slider warning remains. The records-history milestone documents this transition without rewriting earlier milestones.
- Disposable statistics and query caches change derivation versions and rebuild from unchanged raw records. No save format replacement, raw-history deletion, gameplay slider recalibration, or challenge multiplier changes.

## Exact table and rounding

Table: apd-experimental-v2.6R-isolated-product-v1; model: phase6-experimental-resource-v2.6R. BigDecimal retains decimal inputs and uses exact ordinary multiplication; displayed values round to four decimal places, with compact engineering notation for extremes. Only the final integer earned score is rounded, once, HALF_UP. Long score range is tested at the hardest supported endpoints; no int saturation or arbitrary multiplier cap is introduced.

The owner's prose example conflicts with the supplied authoritative table: Enemy HP 125% is 1.0897011360446645, while 150% is 1.1727900665646018. The supplied table is preserved, including 50% = 0.8256281968950766 and neutral = exactly one.

## Verification status at source submission

Local Java production and test compilation succeeded. 55 relevant JUnit methods passed, including all 357 exact positions, 18 neutral displays, mixed/endpoints, ordinary product, original challenges/victory inputs, RNG preservation, frozen saves, real recorder end scores, rankings ordering, all-six-class reload, permanent statistics, queries, exports and archive round trips.

An exploratory manual all-class invocation lacked the repository's Gradle working directory, fixtures, source-path properties and isolated setup; it reported 417 failures in 1724 methods and is not valid full-suite evidence. It also caught a cache-version assertion requiring an update after the intentional cache bump; that assertion is corrected and passes in the targeted run. Its log is retained locally. The authoritative complete core suite and actual Android assembly/instrumentation remain PENDING in the exact-source workflow at this checkpoint. No new APK is called verified or delivered here.

Required Android evidence: 34 instrumented methods and 44 valid screenshots, including 357 rendered draft positions, a real touchscreen drag checked before release, narrow scrolling, existing gestures, score windows, original menus, save/continue and gameplay checks. Emulator evidence does not establish physical RedMagic 11 Pro / Android 16 behavior.

## Risks and limits

The numerical rewards are explicitly experimental and not competitively calibrated. Existing runs keep their original scoring authority and do not acquire these rewards retrospectively. Original integer SPD scores and legacy ranking serialization remain intact; adjusted scores use a separate long-valued result. Future lookup changes must introduce a new calculation version and preserve old authorities. Physical-device verification remains pending.

Last verified available APK before this candidate: TEST241 from b5d562ca37fff9ac628af3f4ade731e9fa7d83f1, full run 38894980574; 1716 core, 33 API34 methods, 42 screenshots. Rejected TEST242/243 attempts produced no APK. Preserve TEST241 and the published TEST231 fallback if this candidate fails.

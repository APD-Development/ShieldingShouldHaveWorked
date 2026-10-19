# National Pie outcome and UIMSD viewport fixes, October 19, 2026

## Root causes

Terminal deaths from damage sources that submit Dungeon.fail after Hero.die first run Hero.reallyDie, which deletes the save. The generic deletion path called ApdRecorder.onSlotDeleted and sealed the recorder as Abandoned. The later Dungeon.fail could submit a death ranking but could not reopen the sealed recording. Earlier tests called Dungeon.fail directly and missed the cleanup-before-fail sequence. Hero now uses an explicit death-cleanup path that removes exactly the same save files without invoking the manual-abandonment recorder hook. Dungeon.fail retains ownership of the final score, outcome and exact ranking link. Doom sources which already finish before cleanup remain idempotent. Manual deletion still records Abandoned.

UIMSD model changes rebuild the entire scene table. UimsdTable also explicitly scrolled to (0,0) when rebuilding content. The scene now transfers transient viewport state to the replacement table; the widget retains it on its own relayouts. TableViewport anchors horizontal position to the visible column ID, so removing earlier columns does not lose the statistic being inspected. A newly changed sort is minimally revealed if necessary, and starts at the top result. Filters/settings preserve vertical offset, clamped to the available rows. Sort markers precede long column labels to remain visible. This shares the model-triggered rebuild with the Columns touch bug, whose existing modal guard remains in effect: no underlying table controls are recreated while any modal stays open.

## Outcomes and compatibility

Died=0, Won=1 and Abandoned=2 retain their existing serialized values. Ascensions remain successful runs counted in win rates, with a new optional Ascended fact which can be selected, filtered, grouped and exported. The optional RUN_END tail records this only for new runs; imported rankings copy the existing explicit Statistics.ascended value when present. Older run ends without that field remain unknown. Derived cache versions advance to rebuild projections without changing authoritative logs.

No destructive historical migration is performed. The faulty recorder sealed an actual Abandoned outcome and often lacked its later ranking link. HERO_DEATH alone cannot establish permanent death because blessed/unblessed Ankhs can revive the hero. HP or approximate matching by score, seed, class or time is insufficient to replace an explicitly stored historical outcome. Old records retain their original result unless a later separately reviewed repair has definitive identity-linked evidence. No existing save, raw history, original score, filter or saved view is rewritten by this fix.

## Regression coverage

RunOutcomeRegressionTest covers cleanup-before-fail, the already-finished Doom order, explicit abandonment even after a death event, save/load, normal victories, ascensions, final scores, ranking links, run facts and row caches. RunOutcomePersistenceTest covers old event compatibility with unknown ascension; byte-for-byte history preservation; all outcomes and ascensions across archive export/import and cold/warm derived caches; outcome filters; the Deaths table final-death flag; CSV and Markdown run reports. TableViewportTest covers both directions, horizontal anchoring, vertical policy, settings refresh, filtering to short/empty data, column changes, offscreen selected columns and visible indicators. Android UimsdColumnsModalDeviceTest retains repeated real-touch column selection and adds touch-driven Sort/highest/reverse behavior, actual sorted values, viewport retention, settings and filter refresh.

Core and Android gates must be read from exact-source workflow results before claiming success. Physical-device verification remains unclaimed. Both developer/player APKs must retain the existing package and signing identity and all embedded historical build records. No public release or tester distribution is part of this request.

## Export issue found by regression

The new export assertion exposed that ReportRenderer.csv skipped Report.Facts blocks entirely. A run report therefore omitted Outcome, Ascended and other run facts from CSV even though the table/model and Markdown held them. CSV now emits these as escaped Section/Field/Value/Detail rows alongside all pre-existing tables. ReportCsvCompletenessTest covers the result fields, preserved table rows and formula/comma escaping. Failed TEST288 and TEST289 gates produced no APK; the immediate previous compiled history remains TEST287.

## Multi-key sorting completion

Viewport focus now tracks the sort key actually changed, including secondary and tertiary keys; it does not automatically scroll back to an unchanged primary key. Sort signatures are ordered by sort rank rather than column display order, so moving columns does not spuriously reset vertical scrolling. Two additional core regressions and the actual Android menu test cover these cases. The next uniquely numbered pair preserves the produced TEST290 history.

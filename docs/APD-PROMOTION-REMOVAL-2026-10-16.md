# Remove inherited donation promotions — October 16, 2026

## Confirmed cause
WornKey.doPickUp checks the inherited supportNagged preference. When false, it saves and schedules WndSupportPrompt before calling the normal Key pickup. That window combines an SPD developer greeting with SupporterScene's Patreon appeal and an external Patreon button. This explains a promotion after Goo's key independently of boss or reward-generation logic.

A second active route is WndVictoryCongrats:its final paragraph asks for support and its button opens SupporterScene. That scene contains the other Patreon URL. A source/reference search found no other active direct donation endpoints. Old title-page Patreon strings were also present but not used by APD's current TitleScene.

## Bounded correction
WornKey now inherits the existing Key.doPickUp unchanged. Journal registration, quantity, normal pickup turn cost and Skeleton Key interactions remain in that shared path. No boss, loot, key, save format or preference value is modified. The promotional extra pre-pickup save is removed with the promotional hook; ordinary game saving is unchanged.

The victory dialog retains its normal content and thank-you text, removes the support appeal/button, and uses the existing Close button at full width. Legacy WndSupportPrompt and SupporterScene class names remain compatible:the old window contains only neutral upstream attribution and the old scene routes to the existing About screen. Neither contains a donation link. No new promotion or payment feature is added.

Removed276 obsolete promotional localization keys from46 source-verified resource files:23 scene bundles and23 window bundles. Each local original matched the real approved branch's Git blob before editing. Removal is limited to SupporterScene, old title Patreon keys, the old prompt keys and victory support keys. Legitimate About credits, copyright headers, GPL notices, artists, music/sound credits and all their links are unchanged. Historical SPD change notes mentioning old donation features remain historical facts rather than active donation appeals; no donation link is retained there.

## Regression and verification
Two new permanent core methods cover actual WornKey pickup with either old support preference, journal key count, one normal pickup action/cost, unchanged old preference, runtime-source donation URLs/localization appeals, and required attribution/license text. Both pass locally. The first local attempts exposed only a missing native-library classpath and uninitialized test Notes; the isolated fixture was corrected and rerun. Do not call those setup failures passing tests or production regressions.

The existing full Android gameplay method now picks up WornKey with both preference values and verifies no support window appears. It opens the victory dialog, checks its sole Close action, captures boss-key and victory screens, then returns to gameplay. Expected complete next gate:1756 core methods,37 Android methods,57 required screenshots, debug assembly and exact previous TEST259 APK history. Candidate pending until actual CI and visual evidence pass. TEST258 remains the verified owner-delivered fallback.

## Timing and limits
This urgent tester report was handled while TEST259's prior Markdown fix was already built and undergoing Android verification. The next unique application build must compare against actual TEST259 source8d9381e5116678f51fb2f2d70b23a681102a85bd/run38973264847, not relabel the Markdown fix as new. Android evidence is emulator-only; the tester's phone and existing owner save are not attached. No public release or automatic tester distribution.

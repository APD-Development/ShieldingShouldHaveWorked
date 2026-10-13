# TEST123 equipment verification repair and slider touch correction — October13,2026

## Actual root causes
TEST122 source6054e401f901a981eaa21ed7af2e1310a9127889 failed1379core with3failures and produced noAPK. Canonical normal-drop identification/provenance calls were renamed inside equipment batching, breaking two existing wiring guards. Commit93a1c37c131c7d7dd33e15f01b13b6e1c60d1529 restores the canonical calls without weakening either assertion;15targeted checks passed1.204s.
Neutral diagnostic38429722829 matched all24original/candidate floor/shop records andnextRNG values against exactTEST121source. Full-suite diagnostic38430708030 then passed1378of1379 and captured the sole remaining difference: all12floors gained a Guidebook because earlier tests left the introductory guide page undiscovered. Removing only that extra item makes every original record identical; allshoprecords/RNGmatch. The permanent fixture sets a reference introductory page state and restores its original state, retaining unchanged expected hashes. A new regression verifies discovered andundiscovered priorstates produce the same neutral snapshots and are restored.

## Reported slider touch bug
SPD OptionSlider immediately moves its thumb on pointerdown and treats every drag as horizontal; it captures the pointer before the surrounding ScrollPane can handle it.
Custom rules now explicitly opt into native-pane gesture arbitration. Nothing changes selected value on pointerdown. A small zoom-scaled threshold classifies the dominant axis, locks that direction for the touch and forwards vertical motion to the existing ScrollPane. Horizontal gestures retain native slider positioning; taps commit as before. Short swipes are classified at release. Vertical gestures and cancellation restore the original thumb/value. Other SPD sliders keep the opt-out behavior.
No slider boundaries, steps, gameplay factors, scoring, UIMSD recording or save formats change in this touch fix. Next-run drafts remain unsaved until Save; active-run rules remain immutable.

## Tests
Four new classifier methods cover zoom1/2/4/6, direction locking after diagonal changes, short gestures, touch jitter/taps, equal diagonals and rapid sequences. New neutral journal-isolation method exercises both discovery states. Nine combined generation/gesture local tests passed1.871s; production classes compiled.
New Android test injects MotionEvent events with SOURCE_TOUCHSCREEN through Android instrumentation, covering vertical/diagonal/short/long/rapid swipes, horizontal/diagonal swipes, taps and cancellation, asserting slider values, scrolling, unsaved preferences, active rules andscore. This is emulator touch-input verification when executed, not a physical-phone claim.
Required full1384unique core,24API34cases,27validPNG anddebug assembly are pending. Screenshot custom-rules-touch-gestures.png must be retrieved/reviewed. Do notclaim touchscreen fix verified untilactualAndroidresultpasses.

## Scope and risks
Equipment generation changes remain those of the failed122candidate; additional sealed-chest/ordinary-drop/quest-protection regressions remain. Earlier sourcefixes are retained, not rewritten. Main, Claude, playerhistory, saves, signer andapplicationID are preserved.
Touch thresholds, direction locking, off-widget drags, release/cancellation, enabled/disabled controls and small-screen rendering require actual Android checks. Device-specific touch behavior remains physical-testpending.
Fifteen gameplay effects now have sourceimplementation; three offensive-damage effects andmandatoryUIMSD/scoring-interface acceptance remain incomplete. No unapproved scoring coefficients, online features, GetFuckedmechanics or loreactivation.

## Build attribution and fallback
Previous actualAPK isTEST121source010c44c877ef03dca7fe5ac2987cde309471d089/run38410865529; failed122hasnoAPK. Newhistory compares the next producedAPK against121, preserving all meaningful changes includingtests/reliabilitytooling. Existing private121APK remains playablefallback untilfullnewchecks/signingpass.
Local timestamps/CI elapsed are not continuous active-work accounting. No backgroundCodexworker orcontinuousunattendeddevelopment is claimed.

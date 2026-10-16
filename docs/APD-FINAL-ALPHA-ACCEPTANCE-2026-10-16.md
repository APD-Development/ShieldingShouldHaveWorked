# Early-alpha owner acceptance — October 16, 2026

## Implemented since the delivered TEST250
The optional Experimental scoring popup explains my long-term ideas for interactions among all 18 rules, classes, subclasses, crown choices and progression. It distinguishes those ideas from today's unchanged provisional lookup multiplication and does not reward or penalize random loot. It is separate from the slider controls and uses the existing scrollable Pixel Dungeon window.

A real daily run exposed a calendar inconsistency: original score 133, earned score 266, displayed calendar score 133. Daily history now reads the frozen earned value and retains compatible optional long-score metadata. Existing integer scores remain intact. The latest retained older frozen daily can be recovered, but older days whose adjusted values were never stored cannot be invented. Large earned values wrap beneath dates when needed.

## Current narrow-toolbar correction — TEST253 candidate
At the minimum 135-pixel logical portrait width, the original three quickslot frames require 57 pixels, the normal action buttons 64, and the page switch 19 additional pixels: 140 total. The centering code shifts that overflowing row into Inventory. A local correction moves only the page switch above the quickslots when its row cannot fit, preserves all original control sizes, and keeps that placement when the toolbar is flipped. Wider layouts are unchanged.

An expanded native fixture covers 72 combinations: six widths, three original layouts, both toolbar sides and both pages. It also captures the actual toolbar and taps the pager using Android touchscreen events. The daily-calendar fixture now explicitly waits for a newly created TitleScene before adding its popup; fresh-process diagnostics distinguish lifecycle interference from an application Back defect.

The changed Toolbar compiles locally. Seventeen targeted methods pass after providing the standard source-root test property; an initial manual invocation omitted that property and failed one source-inspection fixture before it could inspect source. No test assertion was weakened. The next exact-source gate requires 1,736 core methods, 36 Android 14 methods, 49 reviewed captures, debug assembly and complete history against the actual preceding TEST252. All candidate checks remain pending at this source checkpoint; do not publish before success and Android 16 re-verification.

## Completed TEST252 verification before supplemental Android 16 checks
TEST252 source fa39ad824347a67624a93efbb4a3fb358c7477fe passed 1,734 unique core methods and 36 Android 14 instrumentation methods with zero failures, errors or skips. Debug assembly and exact preceding-APK history checks passed. All 48 primary screenshots were reviewed. CI ran 12:56:19–1:13:20 p.m. EDT, 17 minutes 1 second; that is workflow elapsed time, not developer-active time.

The exact APK SHA-256 is 333086ba911b7614f8562df21a8b50e1d8f18cb362b647bfa7032273d64a7f51. Application ID com.charltonchris29.adhdpixeldungeon.indev, version code 920, signing certificate 067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e match the published TEST250 install identity. TEST252 has not been published while additional acceptance is unresolved.

## Supplemental class and boss evidence
The same signed TEST252 APK passed a real Android 14 new-run/save/continue scenario for all six classes using locked non-neutral rules, Difficulty 5 and all nine challenges. Six continued-game screenshots were reviewed. This is one parameterized instrumentation method, not six additional methods.

Seventeen targeted headless methods passed: fifteen repeat methods from the primary suite and two new permanent boss phase/save methods. The new methods cover 1,200 Tengu and 1,200 DM-300 configurations across six class identities, five difficulty levels, twenty Boss HP settings and both stronger-boss states. They exercise actual boss damage/phase/save logic with visual callbacks isolated. They do not establish full human-played boss victories or class-specific strategies. The complete suite will contain 1,736 methods once these two methods are included in a later full run; do not call the TEST252 primary run 1,736 tests.

## Unsuccessful checks retained
The first Android 16 attempt ran no app tests: the emulator needed 7,372.80 MB for userdata and the runner had 7,036.54 MB available. Removing unused toolchains from the disposable runner allowed the next emulator to boot without modifying the application.

That retry ran seven methods: five passed, the daily-calendar test reached its correct value/layout/screenshots then lost the render thread after Back closed the activity, and the real-run test reported category-hotbar overlap with Inventory. The candidate was not called Android 16 verified or published. A subsequent diagnostic attempt failed before any app tests because the emulator action split a Python heredoc into independent shell commands. It was corrected to execute a script file. Fresh-process diagnostics remain pending at this report checkpoint.

## Remaining acceptance and limitations
Investigate and resolve the two Android 16 failures, recheck actual screenshots, complete any required full gate after production changes, and publish the verified private owner APK with its exact source, signature and checksum evidence. The published TEST250 APK remains the fallback and its assets are preserved.

No physical phone is connected to this execution environment. Owner-phone install-over, the owner's active-save continuity and physical touchscreen acceptance remain unverified. Emulator testing does not satisfy those checks. Broad human-played class/boss combinations, the entire UIMSD audit, audio-delay reproduction and the original armor-availability playthrough remain open; they are not claimed complete.

All 357 multipliers, ordinary multiplication, original SPD score calculation, nine challenge factors and 18 slider mechanics remain unchanged by the explanation and daily-calendar fixes. Future dynamic/class-specific scoring is not implemented. No public release, paid service, unfinished online activation, automatic tester distribution, uninstall or player-data clearing is performed.

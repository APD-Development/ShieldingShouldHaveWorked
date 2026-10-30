# APD TEST342 UIMSD Tables Restoration Verification Report

**Recorded:** 2026-10-30T19:36:11Z  
**Approved branch:** `codex/apd-phone-fixes-2026-10-09`  
**Application change:** `c1f04a0b8edb9aba51008ae66cda7e3b65d614b7`  
**Candidate request/history:** `ff3dae8c6610bcfe033c6c3e7928d08d0b215e91`  
**Disposition:** compiled candidate rejected/pending; not published or distributed

## Reproduced failure and bounded correction

At real head `6eeb5ca52fd3810913822077b26af2eef0763941`, `UimsdScene.pickView` created the parameterized Tables `WndUimsdList` without `restoreWith`. `WndUimsdList` has no zero-argument constructor, so the existing `PixelScene.saveWindows` fallback could not reconstruct it and silently discarded the open chooser during seamless scene recreation.

The production correction adds the same active-scene-checked exact restorer already used by the Conditions and tour menus. It does not alter the catalog, selected view, query, history, scores, saves, all18 Custom Dungeon Rules,357 scoring values/formula, combat, RNG, content, signer, application ID or versionCode. A core source guard protects the exact restoration wiring. The existing Android menu-restoration method now opens Tables reflectively, requires a non-null restorer, rebuilds the scene twice, requires one restored chooser each time and executes the restored first-item callback.

## Evidence

| Gate | Actual result |
|---|---|
| Core-only | Run41112805919/job124259238377:2230 methods,425 suites,0 failures/errors/skips; all6 RunTurnWindow methods |
| Candidate producer | Run41113901529, TEST342; verify-and-build job124263027668 success |
| History | Immediate compiled predecessor TEST341 run40976774216/source`acb69c653033ee0d8df68264395ca27e08eb566d`; reviewed-path and compiled-history verification passed |
| Developer artifact |13003779687; workflow ZIP digest `cc7615e6154503c2373883a563b1699959c9808ccabcff6160cfdf0c0dc50765` |
| Matching player | Run41113901520/job124263028125 success; artifact13005766501 digest `9e384ca3a5a4513179fa52684ebc08c97fbd1c2539d0638153680d1bd30c008a` |
| API34 | Job124265010700:66/66 passed,0 skipped/failed;92 listed320x640 captures |
| API34 artifact |13004844055; digest `d247674ff7020f0e9f02d00ff796417a2c758ea2a3c9251f15373c161c33ecc6` |
| Display profiles | Compact,small,square,short-landscape and M53 passed on attempt2 with the exact TEST342 APK |
| API36 |3 tests run;1 failure in `titleControlsStaySeparateAndNativeChangesAndHideUiRemainUsable` |
| API36 failure | `TopScoresDeviceTest.render` line27 timed out waiting60seconds for render-thread completion; artifact13006246410 digest `a01745bb7546873396b7989371306ce7d196f6b461060a7ca486cea77aba02f4` |

The first display attempt began before the producer artifact existed and failed exact-producer lookup; attempt2 is the measured result. The API36 failure was retained and was not repeatedly rerun until lucky. No physical-phone testing occurred.

Three other automatically triggered legacy/staging paths failed independently of the Tables correction:

- Stage run41113901554 failed its application-tree equality check because its resolved publication inputs were from an older application tree.
- Hotbar run41113901528 and death-order run41113901564 failed before native execution because `git diff` could not resolve object `eda307d567bd7eacd1b304042747b65d0ea4ed72`.
- Their failures do not supply acceptance evidence and were not hidden.

## Current APK status

The latest actually published private pair remains TEST341, release428527455/tag `apd-alpha-preview-341`, published2026-10-30T00:24:54Z:

- Developer SHA256: `ad6281b3e41fe414187ca40824157a5bbcf102d7d86e569dbdb8439e7f47d694`
- Alpha-Testers SHA256: `639ce15ddb4b02d037cd29f995c9f38514e03cf577370cf38488f4602bfd65bd`

TEST330 remains a published fallback. TEST342 is a private workflow artifact only and must not be named as current, verified, delivered or playable while API36 is red.

## Next safe step

Reuse the exact TEST342 APK and retained API36 artifact for a bounded render-thread-timeout diagnosis without rebuilding application source. Do not weaken the60-second assertion, erase the failed attempt or publish TEST342. If that diagnosis is deferred, preserve TEST341 and continue independent read-only UIMSD audit work. Physical-phone verification remains separate and absent.

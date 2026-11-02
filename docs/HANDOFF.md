## 2026-11-02T20:17:38Z — Grass dew, searched-wall memory and affected-item ring readouts source-verified

- Owner authorized all three QoL changes. Grass dew collection defaults ON, visible-enemy override OFF; movement and normal pickup consume separate turns (two at ordinary movement speed). Blocked/full/missing-vial dew remains ordinary floor loot. Native Options -> UI -> Dew pickup settings provides independent persistent controls.
- Manual Search marks visible searched wall cells only. Sparse optional per-floor memory survives compressed Level save/load/revisit; legacy floors begin unmarked. Batched pixel checks stay below fog/blockers; Examine confirms manual search. Search radius/chances/turn/hunger costs are unchanged.
- New ring contribution blocks appear only in affected-item descriptions, as explicitly chosen by the owner. Show Sharpshooting damage before/after (including own missile/bow scaling and augments), other applicable weapon/armor/recharge effects and known combined rings. Preserve existing Arcana details. Wealth does not change existing item stats, so no fake damage/value bonus is added. Hide hidden ring/equipment properties; previews do not consume RNG/buffs or rewrite Spirit Form effect items. Ring formulas are unchanged shared pure mappings.
- Source251e3c416 preserved/merged incoming test-only e7fce780f into exact approved head5362acb17a4dc34cc36c48e450564a44c04822b4. Run41516969763/job125522885517 passed2279 core/429 suites/0 failures-errors-skips plus Android debug/release Java and desktop compilation. All47 new cases passed. Artifact13176191303/SHA5f807ab8e70b3a2bf8cff2a55534dfb9965d9745ca7ac6a382cde09d79b81d37 was independently downloaded/XML-parsed.
- Initial full run's only failure was the exact reviewed-English-string inventory:22 new clean keys require5098 instead of5076. All Adult Humor OFF/ON scans and original golden references retained. No new APK, numbered build, public/private release, signing/app-ID change, save/statistic deletion or numeric scoring-table change. Native/phone acceptance of these features is NOT RUN; unchanged TEST345 evidence must not be relabelled as new-feature evidence.
- Implementation/evidence: docs/APD-QOL-2026-11-02.md and docs/APD-QOL-VERIFICATION-2026-11-02.json. Separate GPL PR3 remains separate/unmerged; existing published TEST341 does not contain these features. Continue owner work without rebuilding unchanged artifacts or exceeding three APKs per New York day.

## 2026-11-02T15:37:43Z — TEST345 exact APK accepted on complete emulator gates

- Accepted application is the unchanged TEST345 developer APK from producer run41344574181/application source `207ed6819ed2a36cd4f6ccd02eaca5d7b9aa1457`, SHA256 `a6c5653d5eaf182c65a5352146ce8660cdfd0f71ad70796049a87dc35b3e5b46`.
- Exact-APK [run41470277645](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/41470277645), attempt2/job125363526637 passed the complete API34 list: `OK (67 tests)`, 0 failed/ignored. Privacy restoration passed. Warm 72-run UIMSD Home passed at 204 ms and 116 ms against the unchanged 250 ms limit.
- All 92 retained visual PNGs decoded and validated at 320×640. Artifact13155976745 is 7,377,011 bytes; digest `dae59e8d8176cf388b4609b375f97073eb2c20f84581a4a97838b63569527151`.
- All six producer display profiles and matching player checks were already green. Player APK SHA256 is `a910500b72f77906827cdf699da69b7f1151ebdb52f85b941bc6adc8cd6b7bda`.
- Retained failures remain documented: missing-KVM diagnostic run41347351842 had no APD timing result; focused exact-APK run41469223189 was test-green (`OK (1 test)`, 210/101 ms) but wrapper-failed; attempt1 of run41470277645 stopped before emulator creation on a corrupt SDK system image.
- **Disposition:** TEST345 is accepted as the latest private emulator-verified candidate. It is not published, distributed or physical-device verified. Latest actual private publication remains TEST341 release428527455; TEST330 remains fallback.
- No application source or APK changed. Build-request blob `3fe0ce4437fb3129a69588b2f943b1cc368c691d` remains unchanged.
- Full evidence: [APD-TEST345-FINAL-ACCEPTANCE-2026-11-02.md](./APD-TEST345-FINAL-ACCEPTANCE-2026-11-02.md).
- **Next safe step:** under separate publication authorization, reuse the already-built TEST345 pair and create/re-verify an actual receipt; otherwise continue the next documented UIMSD backlog slice. Do not rebuild TEST345 merely to record acceptance.

## 2026-11-01T20:26:24Z — TEST345 Privacy restoration accepted narrowly; candidate rejected overall

- Branch evidence head before this verification-only handoff: `4dab228a8cf681f64111a605adcf0cf269677f19`.
- Application correction `c501320ccdcbeb15659f7de228771180779cfcae` restores the five parameterized Privacy subflows. Source guard is green (run41319425592/job124886277582: 425 suites, 2232 tests, 0 failures/errors/skips).
- TEST345 producer [run41344574181](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/41344574181) passed reviewed history, complete core, debug assembly and compiled-history verification. Exact developer APK SHA256 is `a6c5653d5eaf182c65a5352146ce8660cdfd0f71ad70796049a87dc35b3e5b46`; it remains a private rejected artifact.
- Full API34 executed 67 methods: 65 passed, 2 failed. The Privacy failure was only a test-label mismatch (`Run restore-fixture-run` versus the raw ID) and was corrected in test-only commit `586a71620bc459ded9dd19668826070290c22390`. The other failure measured the existing warm 72-run Home opening at 276 ms against a 250 ms limit.
- Exact-APK no-rebuild [run41346533622](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/41346533622)/job124967431276 proved the corrected Privacy case on API34: `OK (1 test)` in 3.465 s; application inputs unchanged; artifact13099059654 digest `6a97f7644a50ced03dc3a1859c6ed50ba2bfa52521d0d70b8a3131f266e31deb`.
- All six fresh display profiles passed, including short-landscape and RedMagic representative API36. Matching player run41344574193 passed; player APK SHA256 `a910500b72f77906827cdf699da69b7f1151ebdb52f85b941bc6adc8cd6b7bda`.
- **Disposition:** Privacy restoration is narrowly accepted on the exact APK; TEST345 remains rejected overall because the complete API34 suite is not green. No publication, distribution or physical-phone claim.
- Latest actual private publication remains TEST341 release428527455 (Developer `ad6281b3…`, Alpha-Testers `639ce15d…`); TEST330 remains fallback.
- Build request blob `3fe0ce4437fb3129a69588b2f943b1cc368c691d` is unchanged by verification-only work.
- Full evidence: [APD-TEST345-UIMSD-PRIVACY-VERIFICATION-2026-11-01.md](./APD-TEST345-UIMSD-PRIVACY-VERIFICATION-2026-11-01.md).
- **Next safe step:** inspect the retained 276 ms warm-Home timing miss as a bounded performance package using existing logs/artifact; no unchanged rerun and no new APK request until a real correction is ready.


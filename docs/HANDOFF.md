

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


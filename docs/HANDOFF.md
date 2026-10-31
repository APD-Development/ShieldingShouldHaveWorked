# APD HANDOFF

**Recorded:** 2026-10-31T14:30:48Z  
**Approved branch:** `codex/apd-phone-fixes-2026-10-09`  
**Acceptance evidence head:** `b8e336a427846d35535521e3c2a44dbdd004e843`  
**Application source under review:** `c1f04a0b8edb9aba51008ae66cda7e3b65d614b7` (TEST342)  
**Disposition:** TEST342 compiled but rejected; not published or distributed

## Current published APK

The latest actual private publication remains TEST341, release428527455/tag `apd-alpha-preview-341`, published2026-10-30T00:24:54Z:

- Developer SHA256 `ad6281b3e41fe414187ca40824157a5bbcf102d7d86e569dbdb8439e7f47d694`
- Alpha-Testers SHA256 `639ce15ddb4b02d037cd29f995c9f38514e03cf577370cf38488f4602bfd65bd`
- Receipt SHA256 `e74d079b9b4df2d1d9778d2e15c4eebf72ee121449a38b525c1ceaf36aeab9a1`

TEST330 remains a published fallback. No physical-phone verification is claimed.

## TEST342 retained evidence

- Core: run41112805919,2230 methods/425 suites/0 failures/errors/skips.
- API34 main:66/66 passed with92 listed captures.
- Exact compiled developer/player artifacts remain private workflow evidence only.
- Original API36 failure was not a live render-thread-freeze proof: Activity destruction preceded the cleanup render timeout.
- Lifecycle-exact fixture `a423f98d5532c9b008a34f5828afce618aca2302`: five API34 display profiles passed; API36 failed before Settings at the first Changes-page system Back.
- Guarded readiness fixture `b8e336a427846d35535521e3c2a44dbdd004e843`, run41209586264: broadcast queues reached idle and Android accepted the accessibility Back request, but no `APD-BACK` route reached the app; title restoration timed out. The two following API36 scale tests passed.
- Four API34 jobs passed. M53 instrumentation passed1/1, then its emulator went offline during capture and the non-PNG validator failed closed.
- API36 retained artifact13045274020 digest `29dd440b91c264819868c5b8de8e082c33b1a5f64ca94470c7eaa9d67dfe2a97`; M53 artifact13046166155 digest `ff948e1d6ad653b542c8ed317f76e0a18341fd818f2c64e88b3dd76d3b330082`.

## Preserved state

All18 Custom Dungeon Rules remain implemented/enabled. The357-value v2.6R scoring authority, original scores, saves, permanent UIMSD history, Cleric corrections, UIMSD fixes, circular-vault timing, signer, application ID and versionCode are unchanged. No player data was cleared and no app was uninstalled.

`docs/APD-CODEX-BUILD-REQUEST.txt` remains byte-identical at blob `3db90d8ac9cd2e17acaba08330694c516856b0ee`; verification-only work did not request another build.

## Next safe step

Do not rerun TEST342 unchanged or publish it. Preserve the retained artifacts and TEST341. The remaining acceptance blocker is API36 Google-image delivery of an accepted accessibility global Back to the already registered priority-0 app callback. Continue only with a bounded platform-delivery diagnosis that still exercises the real Android callback path, or independent read-only UIMSD audit work. Keep emulator and physical evidence separate.

# APD archive duplicate-total-line validation, November 12, 2026

## Independent work claim, 11/12/2026 around 21:04 EST

Continuation in the existing regular ChatGPT APD conversation. Authoritative development branch `codex/apd-phone-fixes-2026-10-09` was independently verified at `a11eff710512d4754b23d1d2a2e17b4c8eb2df92`, not `main`. Isolated work branch: `chatgpt/apd-archive-count-duplicate-20261112-2104`. PR #59 (negative event counts) and PR #60 (terminal handoff) were already merged, and there was no PR/issue claim for repeated `count` records at this checkpoint. Open playback/native APK, wall search and other work packages remain untouched.

## Source-evidenced defect and narrow scope

The format-1 `ArchiveManifest.encode` writer produces exactly one `count\t<files>\t<bytes>` line. The `parse` switch currently overwrites `countFiles` and `countBytes` for every recognized `count` line, accepting a checksum-valid manifest with contradictory duplicated totals when only the last matches. This permits inconsistent archive metadata to be silently discarded. Reject a second known `count` record, including two identical totals, while continuing to ignore future *unknown* metadata lines and retaining established first-wins duplicate *file path* compatibility. Existing current/legacy valid manifests, history, saves, scoring, mechanics, application IDs, signer, APK assets and releases must not change.

## Execution status at claim

No source edit, regression, CI, original-signer APK, emulator native acceptance or publication has been performed for this package yet. Local private Git clone access is unavailable because DNS to github.com fails; use scoped GitHub connector changes and a branch-scoped Actions workflow for real Java/JUnit verification, not static assertions alone.

## Safe continuation

Add the parser guard and focused checksum-valid manifest regressions including a real ZIP import non-mutation test, run full core and Android/desktop Java compilation in isolated exact-source CI. Preserve failed receipts if any. Recheck live authoritative and PR heads before a guarded non-force integration; do not merge or publish without verified source and release gates. Document actual Eastern timestamps, commits, JUnit totals, CI result and native/APK boundaries.

## Implementation and verification checkpoint, 11/12/2026 around 21:06 EST

- Parser guard commit `0519349cfae409065664415d2b1874dd55d6da6f`: a second recognized `count` totals record now raises `IllegalArgumentException("the manifest has repeated totals")` rather than overriding the prior totals. Existing first-wins duplicate *file paths* are unmodified.
- Four regression tests in `ArchiveManifestDuplicateCountTest.java`, commit `730588b3e91fcedbd5ec2b0d20d0d1f947af16f8`: contradictory checksum-valid totals, identical duplicate totals, valid single-count round trip, and real ZIP rejection without altering an existing history log.
- Dedicated source-only CI workflow commit `3d039b20bf5b72656ee421eee39696c82e77cd12`. [Actions run 43349653095](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43349653095), exact source/test/workflow SHA `3d039b20bf5b72656ee421eee39696c82e77cd12`, was **in progress** when checked. Static scope preflight passed; full Gradle JUnit + desktop/Android Java checks had not yet finished. Do not interpret pending tests as passed.
- [Draft PR #61](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/61) targets only `codex/apd-phone-fixes-2026-10-09`. Fresh compare showed exactly four changed paths: this handoff, the parser, test class and isolated workflow. No changes to gameplay, scoring, saves, original log formats, old releases, personal animation or native APK work. No APK was assembled, signed, native accepted or published.

**Next:** inspect terminal CI, decoded JUnit totals, and all required Java targets. If passing, recheck live head, open claims and PR diff before a non-force expected-head merge; otherwise preserve the failure and fix the specific test. Record the final branch and publication boundaries.

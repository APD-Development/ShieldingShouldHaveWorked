# APD mob unreachable-retarget recursion guard: continuation (November 17, 2026)

## Work claim and source baseline

Claim created Tuesday, November 17, 2026 at 08:01 EST. Isolated branch `chatgpt/apd-mob-retarget-guard-reset-20261117-0800` starts from authoritative `codex/apd-phone-fixes-2026-10-09` head `50151463acabd8b5468af34fed7b3d67affe4508`. The complete R7+C485 baseline is ancestor `c6fa033167789ef9f2fc6f31bcff39c824dfb4db`. This worker first reviewed `AGENTS.md`, `docs/APD-486-R7-INTEGRATION-VERIFIED-2026-11-16.md`, open PRs, current source and preceding Tengu AI handoff.

**Scope claimed:** `Mob.Hunting.handleUnreachableTarget` exception-safe recursion guard reset, its existing JVM regression class, isolated source-only CI and this continuation. Tengu already received a corresponding `try/finally` safety reset in PR #247, so that existing package is not touched. Existing open PRs #229/#230 concern Extended diagnostics; other open PRs concern build/native/personal playback/older drafts. None should be merged or modified by this worker.

## Evidenced failure and proposed narrowly scoped repair

`Mob.Hunting.handleUnreachableTarget` currently sets `recursing = true`, invokes the recursive `act(...)`, then resets `recursing = false` only on normal return. An exception from an overridden `act` leaves the same persistent hunting state with `recursing = true`, silently disabling future one-step retries on that state if the exception is caught or recovered by a caller. Tengu's already-merged corresponding handler uses a `finally` reset. Change only this guard reset, preserving existing movement, target visibility, targeting, damage, ability, RNG and turn logic. Add a headless regression that first raises a controlled exception from a hunting retry and then confirms a second retry on the **same state** is permitted.

## Verification, integration and release

Source edits, regression, CI and guarded integration completed; final merged readback is recorded below. Any failure and the next safe work will be recorded here. This branch does not build, sign or publish APKs; qualified release, native acceptance, install-over and original-signer gates remain with the dedicated APK worker.

## Verified code, tests, CI and PR checkpoint (08:06 EST)

- Work claim: `133a179da7f21e4071d0f141b8eb3fc18b3672ae`; source fix: `cfa04683c42c8e2afc554a2e290cac74260e8f50`; regression: `ad84412a90c5fccf4733d7af3b94084c42b56ab6`; workflow and exact tested source head: `ef3f98c0b8001974a4da4b703ba1e8a7e2909e21`.
- Draft review [PR #248](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/248) targets the established authoritative work branch. Pre-CI compare against starting authoritative SHA showed exactly four modified/new paths: Mob.java (+5/-3), one existing JVM regression (+40), a dedicated source-only CI workflow, and this handoff. No R7/C485 gameplay/balance, saves, history, scoring, playback, Android build, edition or signer files changed.
- [Dedicated GitHub Actions run 44088760500](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/44088760500) completed **success**, on exact production/test/workflow source `ef3f98c0`. Job `134058070536` reported `BUILD SUCCESSFUL in 2m 18s`. Its actual parsed JUnit XML count was **3,570 core tests in 546 suites, zero failures/errors/skips**. Required `:core:test`, `:desktop:classes` and Android debug/release/instrumentation Java compilation succeeded. The named guard-reset regression and all four older replacement-visibility methods were present in JUnit.
- JUnit XML artifact `14331086102`, `apd-mob-retarget-guard-reset-junit`, 419,827 bytes, GitHub digest `sha256:aedf41879ff430909926f4e6443702b604cf37b110a8e361e8aace29838b3345`.
- Three additional retained regression workflows also **completed success** on this same source: [attackable target ordering 44088760335](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/44088760335), [unreachable-target visibility 44088760524](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/44088760524) and [vault stealth angle-seam 44088760700](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/44088760700).

**Integration gate:** documentation-only evidence was written after the successful test source; before merge, recheck both live heads, confirm no overlapping work and merge with GitHub's non-force PR merge requiring the exact tested+documentation head SHA. Record the merge and fresh readback separately. If base advances or conflict occurs, do not force. Source tests do not establish actual Android device gameplay, install-over, signing or release qualification.

## Final guarded merge and fresh readback (08:07 EST)

After all four source CI jobs reported terminal success, the authoritative live base was freshly read as the unchanged `50151463acabd8b5468af34fed7b3d67affe4508`. The source/test/workflow were identical between the exact CI-tested head `ef3f98c0b8001974a4da4b703ba1e8a7e2909e21` and documentation-only successor `b8f1f49c9f0097a6bdffba33e1d56c953aa8169f` (compare showed only this markdown changed). GitHub verified PR #248 was mergeable, non-draft and at exact expected head `b8f1f49c` before accepting a normal non-force merge with `expected_head_sha`.

**Merged [PR #248](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/248)** into `codex/apd-phone-fixes-2026-10-09` at `cf1a98c25a42c134621d5c66ae577e81f3d78a11`. Fresh readback returned exactly that authoritative SHA; GitHub reported PR merged/closed, and comparison against the PR head found **zero changed files** (only the expected merge metadata commit). Independently fetched merged production and test blob SHAs were `ad66a0fbdf8265b4897bc542b2e7f7f91e2cd038` and `a2f0a204e3ac53ce0c1dd8c93b968e9a518f69cd`, equal to the CI-tested source and test blobs. The Mob `try/finally` guard and new five-method fixture are present.

**Next safe worker:** start from the *live* current authoritative head, re-read current work claims and PRs before choosing a distinct evidenced bug. Leave Extended reporting #229/#230, personal playback, existing native and APK builds to their owners. This is a small source reliability fix, not a measured performance improvement, native test, new signed APK or acceptance of installation/save history. Keep R7+C485 and all approved scoring v6, assets and edition/signature constraints intact.

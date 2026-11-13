# APD storage meter symlink-boundary continuation, Friday November 13, 2026 (Eastern)

## Isolated claim, approximately 12:19 AM EST

This is an owner-directed continuation in the originating regular ChatGPT APD development conversation. The authoritative branch `codex/apd-phone-fixes-2026-10-09` was re-read at `5e3d531de81cc42ca220dbc987c1ec27fe8e8f6c`; the isolated branch `chatgpt/apd-storage-meter-symlink-20261113-0019` was created from that exact commit. Read AGENTS.md, the completed-work ledger, source guide, current history/recovery handoffs, recent PR #80/#81, active open pull requests and source CI; none of the open PRs claims StorageMeter. Preserve the independently active playback/native/release/GPL/search packages and all signed APD464 and earlier releases.

### Evidenced issue and bounded plan

`StorageMeter.measure` reads `runs/`, `legacy/`, `derived/`, and `index.apdi` through `File` APIs without checking canonical direct-child identity. `size(derived)` recursively follows directory symlinks, so a stale cache link to an ancestor can recurse indefinitely and a link to outside files can count unrelated bytes. Run, legacy and cache file links can also inflate read-only storage totals and low-space warnings. This conflicts with the direct-child path policy already established for the history index and importer. This is an observed source-code defect, **not** a claimed physical-phone incident.

Implement a narrow canonical direct-child check in StorageMeter only, compatible with a legitimately symlinked history storage root. Skip external aliases in runs/legacy/derived/index and avoid circular symlink traversal. Never delete or modify anything while measuring. Add real-filesystem regressions for external/circular links and a symlinked root; retain existing ordinary storage totals/warning behavior. Full core JUnit, desktop and Android debug/release/instrumentation Java compilation are required on exact committed source before integration. No changes to gameplay, scoring, history/save serialization, signing, application IDs, edition boundaries, native/release work, previous builds or recurring automation schedules.

At claim time: no production implementation, test run, APK, signer check, native acceptance or release/publication has occurred. Follow up below with actual commits, evidence, blockers and safe continuation. Do not merge without re-reading live target/head and concurrent claims; use expected-head non-force integration only.

## Implementation and verification in progress, approximately 12:24 AM EST

- Isolated branch starting/claim commit: `49a12e2e21ff55316f7c0121d9acd03e2f13a350`, based on authoritative `5e3d531de81cc42ca220dbc987c1ec27fe8e8f6c`.
- Production-only `StorageMeter.java` repair: `133130fd0bc19175cfab0971724516136301a4e5`. Canonical direct-child validation now protects runs, legacy, derived recursion, and derived index bytes; an intentionally symlinked history root remains valid.
- Three JUnit real-filesystem regressions added at `5b0691840a2792d2d1b78e9edb01291e4aca57bd`. Small fixture-cleanup correction `f31f6b1b8a7ca8c8e668575f85092489e0087530` unlinks the deliberate cache symlink cycle in `finally` before JUnit TemporaryFolder cleanup, without changing production code or assertions.
- Dedicated source-only workflow `.github/workflows/apd-storage-meter-symlink-ci-20261113.yml` committed at `bf8178ed0bcd2783e0cf275b99890d3e2c5860ef`, builds no APK. Draft [PR #82](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/82) targets the authoritative branch only.
- First push gate [run 43371474312](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43371474312) started against `bf8178ed0bcd2783e0cf275b99890d3e2c5860ef`; this is before the test-fixture cleanup correction. A new exact-source gate [run 43371722229](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43371722229) was queued against `f31f6b1b8a7ca8c8e668575f85092489e0087530`. **Neither is claimed passed in this checkpoint**. Production logic is identical in the two runs; only test fixture cleanup differs.
- Independently checked existing [APD464](https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/tag/apd-464) release remains published at 2026-11-13T00:07:51Z, with original Developer APK and source. No new app APK, signer/identity native acceptance or publication has been attempted in this source-only package.

Next: wait for the exact latest source gate's terminal results, preserve any earlier failure, recheck the authoritative base/head, open PR claims and source paths, and only then integrate if every gate passes and there is no conflicting newer work. Never infer signed Android or device acceptance from Java compilation.

# APD storage meter symlink-boundary continuation, Friday November 13, 2026 (Eastern)

## Isolated claim, approximately 12:19 AM EST

This is an owner-directed continuation in the originating regular ChatGPT APD development conversation. The authoritative branch `codex/apd-phone-fixes-2026-10-09` was re-read at `5e3d531de81cc42ca220dbc987c1ec27fe8e8f6c`; the isolated branch `chatgpt/apd-storage-meter-symlink-20261113-0019` was created from that exact commit. Read AGENTS.md, the completed-work ledger, source guide, current history/recovery handoffs, recent PR #80/#81, active open pull requests and source CI; none of the open PRs claims StorageMeter. Preserve the independently active playback/native/release/GPL/search packages and all signed APD464 and earlier releases.

### Evidenced issue and bounded plan

`StorageMeter.measure` reads `runs/`, `legacy/`, `derived/`, and `index.apdi` through `File` APIs without checking canonical direct-child identity. `size(derived)` recursively follows directory symlinks, so a stale cache link to an ancestor can recurse indefinitely and a link to outside files can count unrelated bytes. Run, legacy and cache file links can also inflate read-only storage totals and low-space warnings. This conflicts with the direct-child path policy already established for the history index and importer. This is an observed source-code defect, **not** a claimed physical-phone incident.

Implement a narrow canonical direct-child check in StorageMeter only, compatible with a legitimately symlinked history storage root. Skip external aliases in runs/legacy/derived/index and avoid circular symlink traversal. Never delete or modify anything while measuring. Add real-filesystem regressions for external/circular links and a symlinked root; retain existing ordinary storage totals/warning behavior. Full core JUnit, desktop and Android debug/release/instrumentation Java compilation are required on exact committed source before integration. No changes to gameplay, scoring, history/save serialization, signing, application IDs, edition boundaries, native/release work, previous builds or recurring automation schedules.

At claim time: no production implementation, test run, APK, signer check, native acceptance or release/publication has occurred. Follow up below with actual commits, evidence, blockers and safe continuation. Do not merge without re-reading live target/head and concurrent claims; use expected-head non-force integration only.

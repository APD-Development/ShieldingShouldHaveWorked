# APD derived-facts source-log boundary continuation, 2026-11-13

## Isolated claim and verified starting context

Regular ChatGPT APD continuation only. Eastern starting time: 2026-11-13 approximately 00:31 EST. Authoritative branch: `codex/apd-phone-fixes-2026-10-09`; independently re-read starting head `3451afa6a47e1662da06402144c56885d1ff86fa`. Isolated feature branch: `chatgpt/apd-derivedstore-log-boundary-20261113-0031`. No edits to main or other work branches, PRs, existing releases, or automation.

Reviewed repository AGENTS.md, completed-work ledger, current recovery/index handoffs, PR inventory and the separate storage-meter PR #82. This claim is **read-only DerivedStore source discovery for derived facts**, not StorageMeter, RunIndex, StatsStore recovery, archive import, game playback, native testing, or APK publication. Already-completed source fixes and currently claimed Android/release tasks remain intact. No existing open PR claimed DerivedStore.logs at claim time.

### Evidenced problem and bounded correction

`DerivedStore.logs(File dir)` currently uses `f.isFile()` and suffix only. Java follows per-file symlinks, so an external `.apdlog` or a local alias of another file is fingerprinted and included in derived APD/legacy facts. `RunIndex` and `StatsStore.recoverAll` already exclude these aliases by canonical direct-child path ownership. Align only DerivedStore's **read-only source enumeration** with the established canonical direct-child rule, allowing an intentionally symlinked storage root. Preserve legitimate raw history, facts, cache schema, gameplay, scoring, saves, signing identity, application IDs and all releases. No deletion or rewrite of source logs.

Planned three real-filesystem regressions: direct APD log external/local symlink exclusion including derived cache; same boundary for imported legacy history; and compatibility with a symlinked history root. Run the full core JUnit suite and desktop/Android debug/release/instrumentation Java compilation via an isolated source-only workflow. No Java CI success implies native Android acceptance, signed APK or release.

**At claim:** no production code changed, no tests executed, no APK assembled/signed/tested/published. Existing APD464 unchanged. Completion to be appended with exact commits, raw test receipt and further blockers.

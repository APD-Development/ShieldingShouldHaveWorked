# APD run-index filename boundary, November 12, 2026 (Eastern)

## Isolated claim, Thursday November 12, 2026, approximately 11:00 PM EST

Regular ChatGPT APD continuation, not a separate Codex task. Starting authoritative branch `codex/apd-phone-fixes-2026-10-09` at `9fa38c96de89c9074d8801134c65bf0955bc986d`, after checking AGENTS.md, archive/history handoffs, recent merged PRs #70-#73, active draft playback PRs, open claims, existing source CI workflows and gated release evidence. This work is isolated on `chatgpt/apd-index-invalid-log-filename-20261112-2300`; do not overlap or merge into separate playback/native/release branches. Previous release APD464 and all earlier builds remain unchanged.

### Verified source gap

`RunIndex.rebuild()` scans direct `.apdlog` files but does not require a valid APD run-ID stem. Invalid names can be displayed as fabricated unreadable entries, or their valid copied log headers can masquerade as actual runs. `RunIndex.readFrom()` likewise previously trusted a checksum-valid old cache entry naming an invalid log. Strict `StatsStore.isValidRunId` already defines the valid on-disk identity and is used by `RunIndex.refresh`.

### Bounded correction

Require valid direct `runs/<id>.apdlog` filenames during rebuild and when decoding cached entries. A bad cached name causes the derived index to rebuild from immutable original logs. Preserve genuine damaged log files with valid names as visible UNREADABLE records, preserve symlink-root support and existing link boundaries. Do not change game mechanics, score, RNG, seeds, archive formats, history bytes, save compatibility, signing, application IDs, editions or prior builds.

Four regression tests cover invalid names and raw bytes, a valid-header alias under an invalid filename, a checksum-valid outdated derived cache, and legitimate damaged-log visibility. The isolated workflow runs full core JUnit, desktop Java and Android debug/release/instrumentation Java compilation. Any pending or failed CI remains distinct from a verified pass.

### At creation

- Baseline: `9fa38c96de89c9074d8801134c65bf0955bc986d`
- CI: pending until GitHub Actions finishes; no results claimed yet
- APK: no newly assembled/signed/native-accepted/published APK claimed
- Next: inspect exact CI results, only integrate after terminal success and fresh non-force expected-head check, then record exact receipts and safe work

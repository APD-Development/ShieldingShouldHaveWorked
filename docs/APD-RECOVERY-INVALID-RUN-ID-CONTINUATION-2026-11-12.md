# APD invalid-run-filename destructive recovery boundary (November 12, 2026, Eastern)

## Isolated claim, 11:28 PM EST

Originating regular ChatGPT APD development, not a separate Codex task. Inspected the authoritative `codex/apd-phone-fixes-2026-10-09` at `7ee78e1af25a4e181f92c48755ad416fc5052dfe`, AGENTS.md, current run-index identity/filename and destructive recovery handoffs, latest merged PRs #74–#77, open PRs and work claims, current source CI, and existing APK/native release constraints. This isolated branch is `chatgpt/apd-recovery-invalid-run-id-20261112-2328`. No active PR claims the exact recovery-of-invalid-filenames issue. Do not interfere with separately active native/release, playback, GPL, wall-search or other work packages.

### Evidenced defect and bounded correction

`RunIndex.rebuild()` now deliberately skips files with invalid APD run-ID stems, but `StatsStore.recoverAll()` still selects any canonical direct regular file ending `.apdlog`, even if its stem violates `StatsStore.isValidRunId`. For a parseable damaged log copied under a foreign or invalid filename, `LogRecovery.repair()` backs up and truncates it even though it is not recognized as an APD run by the derived index. Keep the existing valid-ID, canonical-direct-child and symlinked-root rules aligned; refuse destructive repair of invalid-name files. Preserve true valid-ID damaged log recovery, its tail backup, all original history, score/save format, signing, app IDs, editions, releases and gameplay. This is a source-evidenced risk, not a claimed user-phone incident.

### Verification claim at start

No files have been modified other than this claim. No tests, APK assembly, native Android tests, original-signer checks or publication have been performed. Plan: add exact production guard, real-filesystem regression tests, and scoped full-core/desktop/Android Java source CI; review terminal evidence before any integration; use fresh live expected-head non-force update and durable result here. Existing APD464 is an independently published prior build and must not be relabeled or overwritten. No automations may be changed.

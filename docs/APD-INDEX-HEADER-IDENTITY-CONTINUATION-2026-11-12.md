# APD run-index filename/header identity boundary, November 12, 2026

## Scope and isolated claim

Originating regular ChatGPT APD execution; no separate Codex task. Live authoritative branch `codex/apd-phone-fixes-2026-10-09` was verified at `4d7791020359341d3cb906fa8c429e3c2e94ca7f` immediately before creation of isolated branch `chatgpt/apd-index-header-identity-20261112`. Read AGENTS.md, the completed-work ledger, source guide, previous index filename handoff and PR #74/#75, open PRs, recent Actions results and release claims. This identity mismatch is distinct from the completed invalid-filename check. No active open PR claims this exact subcase. Playback, native, release, wall-search and existing claims remain untouched.

### Evidenced defect

The derived RunIndex currently verifies the log filename but can still accept a checksum-valid cache row whose `runId` points to a different log than `logFile`. Likewise `rebuild` and `refresh` may take a valid but mismatched run ID from a copied log's header, attributing one physical log to a different history identity. This changes only a derived summary. Original logs remain authoritative and must never be modified by this repair.

### Narrow planned correction

Reject checksum-valid derived cache entries where log filename stem and entry run ID differ; rebuild them from original logs. Treat a direct log whose readable header identifies a different run as UNREADABLE under its real on-disk filename instead of attributing it to the header's different identity. Preserve valid log handling, damaged-log visibility, symlinked history root support, original history bytes, score/save compatibility, gameplay, signer, app identity and prior releases.

Three real-filesystem JUnit regressions cover rebuild, refresh, and checksum-valid old-cache mismatch. Execute full core JUnit, desktop classes, and Android debug/release/instrumentation Java compilation on the exact proposed source. No APK or native device acceptance may be inferred from source compilation. Integrate only after terminal success and fresh remote head / concurrent PR check; do not force-update or overwrite conflicting work.

At claim: no tests passed, no game APK created/signed, no native acceptance or publication occurred. Existing APD464 and earlier builds remain untouched.

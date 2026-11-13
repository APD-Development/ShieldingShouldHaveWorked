# APD archive terminal-end privacy continuation, November 13, 2026 (Eastern)

## Isolated work claim, approximately 01:00 EST

Originating regular ChatGPT APD project, no Codex task. Live authoritative branch `codex/apd-phone-fixes-2026-10-09` was independently read at `feafe348033a0760545abedb863d40b10a61e093`. Independent source-only branch: `chatgpt/apd-archive-terminal-end-20261113-0100`. Inspected `AGENTS.md`, relevant completed-work handoffs, open PRs, CI/release inventory and archived export tests. No open PR claims this exact archive completion-state boundary. Do not overlap release/native/playback/search/GPL work or modify any task schedule.

### Evidenced behavior and approved boundary

`ArchiveExporter.collect` currently sets `LogInfo.ended` true after **any** decodable `RUN_END` event in the source log, although its own contract requires the newest log to end with the run's end. This can permit raw (unredacted) export even when the log is unsealed or later events follow the end marker. The safe archival default is to redact any run not positively proven complete. The planned bounded change trusts a terminal decodable `RUN_END` only in a clean, sealed log, while preserving byte-for-byte raw export of valid completed runs. Add real filesystem/ZIP tests for unsealed end, sealed log with later event, and ordinary sealed final end. Existing history bytes, export format, save/scoring/gameplay and editions must be unchanged.

This is not a claimed phone observation, and no functionality is redesigned. No app APK, signer/native acceptance or release is attempted by this independent source-only claim. Run exact-source full core JUnit and desktop/Android debug/release/instrumentation Java compile before considering integration. Maintain non-force expected-head updates, do not overwrite newer authoritative commits. At claim time no code has been modified or tested. Completion notes must record actual results and outstanding gates.

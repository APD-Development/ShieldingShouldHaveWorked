# APD archive terminal-end privacy continuation, November 13, 2026 (Eastern)

## Isolated work claim, approximately 01:00 EST

Originating regular ChatGPT APD project, no Codex task. Live authoritative branch `codex/apd-phone-fixes-2026-10-09` was independently read at `feafe348033a0760545abedb863d40b10a61e093`. Independent source-only branch: `chatgpt/apd-archive-terminal-end-20261113-0100`. Inspected `AGENTS.md`, relevant completed-work handoffs, open PRs, CI/release inventory and archived export tests. No open PR claims this exact archive completion-state boundary. Do not overlap release/native/playback/search/GPL work or modify any task schedule.

### Evidenced behavior and approved boundary

`ArchiveExporter.collect` currently sets `LogInfo.ended` true after **any** decodable `RUN_END` event in the source log, although its own contract requires the newest log to end with the run's end. This can permit raw (unredacted) export even when the log is unsealed or later events follow the end marker. The safe archival default is to redact any run not positively proven complete. The planned bounded change trusts a terminal decodable `RUN_END` only in a clean, sealed log, while preserving byte-for-byte raw export of valid completed runs. Add real filesystem/ZIP tests for unsealed end, sealed log with later event, and ordinary sealed final end. Existing history bytes, export format, save/scoring/gameplay and editions must be unchanged.

This is not a claimed phone observation, and no functionality is redesigned. No app APK, signer/native acceptance or release is attempted by this independent source-only claim. Run exact-source full core JUnit and desktop/Android debug/release/instrumentation Java compile before considering integration. Maintain non-force expected-head updates, do not overwrite newer authoritative commits. At claim time no code has been modified or tested. Completion notes must record actual results and outstanding gates.

## Implementation and source verification pending, November 13 approximately 01:02 EST

- Claim commit `c24186b9e1f9a8192301fe1247021ef47992648b`.
- Bounded production change in `ArchiveExporter.java`: `ebf1366899351e7a51e24c68d79b98cc031fa2a2`. Uses only an already parsed clean, sealed log whose terminal event is a successfully decoded `RUN_END` as proof for raw export; all other existing readable source logs remain eligible for the existing redacted export. No source logs are rewritten.
- Three real-file and actual-ZIP regression tests: `ffe7c5f3751691bda2498c7a82632163f89fb7c8`, covering unsealed-but-end-marked logs, sealed logs with later hidden events and legitimate clean finished logs.
- Dedicated source-only CI workflow: `c4d01b66601bb1d17395d2761e8c49d715b5c317`, [run 43375919912](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43375919912) triggered from that exact commit. At this checkpoint full JUnit/Java compilation is in progress, **not yet passed**. [Draft PR #86](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/86) is isolated and targets the authoritative development branch. Do not merge until terminal exact-source gate success and conflict/readback checks.
- No Android APK build, original signing proof, emulator/physical-phone acceptance, install-over-save or release publication occurred in this work package. APD464 remains preserved independently. No automation schedule changed.

Next safe action: read terminal exact-source CI, inspect any failures and repair only evidenced test/production issues, then recheck live authoritative head, latest PR claims, and merge guards. Record final integration and APK blockers separately.

## Compatibility correction and superseded initial source gate, approximately 01:06 EST

During review of the actual `RunRecorder.end` implementation, found that APD appends a legitimate `RANKING_LINK` immediately after `RUN_END` and before `close(true)` when a matching ranking record exists. The initial strict-last-event guard (`ebf13668`) would have redacted ordinary finished ranked runs, violating lossless finished-history export. It has **not** been integrated. Its original [Actions run 43375919912](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43375919912) completed source success but covers the superseded logic; **not acceptance for the corrected source**.

Corrected production commit `5354713c2fd4edf46dba8b0241927d014e821687` allows exactly one *decodable* normal `RANKING_LINK` trailer after terminal `RUN_END` in a clean sealed log. Other trailing events, unsealed logs and malformed end/trailers remain redacted, preserving the run's original bytes and raw history. Fourth real-writer/ZIP regression committed as `97b9a558352e86e92acfe3061c2fdffa38fafe0f`, requiring a completed ranked run remain unredacted and byte-identical. Updated exact-source CI workflow commit `621b5f2d4cc30671342dbf65f4c6b2bebd1f1c13` asserts all four cases, previous export privacy and segment ordering, at least 3,385 total core tests and desktop/Android Java compilation. [Actions run 43376337921](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43376337921) was pending at this checkpoint. Earlier intermediate cancelled/pending runs are not substitute verification.

No APK/native/original signer/physical-phone acceptance, save-install-over or gated publication. PR #86 remains draft; no shared branch or release update. Re-read exact terminal CI and live authoritative SHA before any integration; record actual results and blockers.

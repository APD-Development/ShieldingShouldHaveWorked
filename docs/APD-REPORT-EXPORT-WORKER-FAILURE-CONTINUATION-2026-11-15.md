# APD UIMSD report worker launch failure, November 15, 2026 (Eastern)

## Independent work claim, 07:01 EST

- Owner repository: `APD-Development/ShieldingShouldHaveWorked`; authoritative development branch `codex/apd-phone-fixes-2026-10-09`; **fresh verified start head** `c4bdb14a4e2a1b3bb3cd3c197e1ff2fa54baf261`. Isolated branch `chatgpt/apd-report-worker-failure-20261115-0700`. No shared-branch write or build claimed at this point.
- Inspected AGENTS.md, work-completed ledger, known issues and current handoff, open PRs, latest diagnostic worker-start repair #165/#166, source CI, C471/P471/native/signing boundary, the UIMSD `ReportExport.save` and existing `ReportTest`. Open report-related PR claims do not overlap this method.
- **Distinct evidenced bug:** `ReportExport.save` starts a new Java thread after the file picker transfers its output stream. If thread construction/start throws, the stream is not closed. The outer error handler may also report failure while another accepted stream owns an eventual success result. An OutOfMemoryError inside the worker is not converted into a terminal report failure.
- Bounded proposed correction: close stream and report one failure when the report-save thread cannot launch; preserve successful asynchronous stream ownership and report bytes; guard against provider exceptions after handing off an accepted stream; ensure worker OOME returns failure. Add targeted core JVM regressions, run complete core suite and desktop/Android Java compile before qualified integration. **This does not fix the cause of C467**.
- Exclusions: seed reuse scoring, gameplay/balance, saves/history, rulesets, personal/community edition, original signing identity, Android file picker behavior, C471/P471 candidates and prior releases. Do not overlap personal playback, hero fade or existing native/release claims.
- **Status:** work claim only; no source change, CI success, native acceptance, signed APK or release is claimed. Source work must remain in an isolated branch until exact-source verification and a guarded, expected-head, non-force integration.

## Verified execution and handoff

Pending. Record exact source/test/CI/merge commits, failures, build gates, Eastern end time and next safe work after verification.

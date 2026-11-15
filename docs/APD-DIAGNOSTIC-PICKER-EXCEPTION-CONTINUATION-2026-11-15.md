# APD diagnostic export picker failure: independent continuation (2026-11-15)

## Work claim, Sunday November 15, 2026, approximately 03:00 EST
- Authoritative source branch: `codex/apd-phone-fixes-2026-10-09`; exact live start SHA `857eea6da21b343588658f975a934f5f00ceebaf`, independently read from GitHub. `main` is not used.
- Isolated claim branch: `chatgpt/apd-diagnostic-picker-exception-20261115-0300`. AGENTS.md, the November 15 UIMSD title prewarm handoff, completed ledger, open PRs, source, source tests, previous CI and release receipts were inspected before edits.
- Current parallel playback/native C471/P471, hero-select fade, seed-scoring and C467 memory-analysis work is outside this claim. No live open PR claims diagnostic file-picker failure handling, and no APK or release is claimed by this branch.
- Evidenced reliability defect: `DiagnosticReport.export` invokes `ApdFiles.Provider.chooseExport` without a catch. If a platform file picker throws a `RuntimeException` before delivering an `ExportTarget` callback, export propagates an uncaught exception instead of notifying the player. Its failure callbacks and worker IO catches do not cover that synchronous picker failure. `ReportExport.save` already handles analogous provider failure; this work is limited to diagnostic export.
- Planned scope: report an actual picker-open failure once; retain success, cancellation, IO failure and stream closure; add headless regression for a throwing provider (and double-callback guard if needed). Preserve diagnostic data, memory traces, saves, gameplay, statistics, signing, application IDs, and all published/candidate builds.
- **This is a work claim only.** No source correction, tests, APK assembly, native Android acceptance or publication is established at this checkpoint. Do not duplicate it in a parallel execution.

## Verification and integration
- Pending: complete core JUnit + desktop/Android Java compilation on isolated exact source, retained CI logs and JUnit artifact, expected-head non-force guarded integration, final timestamped handoff.
- APK gate: original signer/native Android and install-over/save-integrity acceptance are not provided by a source-only workflow; do not publish an unqualified candidate.

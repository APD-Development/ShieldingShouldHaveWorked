# APD archive manifest event-count validation, November 12, 2026

## Work claim (created 11/12/2026, 20:36:08 America/New_York)

Original regular ChatGPT APD development conversation. Live authoritative development branch `codex/apd-phone-fixes-2026-10-09` independently verified at `abac56c5df1a8564d41e0c38dfe832e78c349cb3`, not `main`. Isolated work branch `chatgpt/apd-archive-event-count-20261112-2035`. Existing archive flag-validation PR #57 and terminal handoff PR #58 are already merged; concurrent draft personal playback/animation and native APK packages are untouched. Searches of current PRs/issues found no event-count work claim. Do not duplicate this work package.

Source finding: format-1 archive manifests serialize a nonnegative count of log events; `ArchiveManifest.parse` currently accepts checksum-valid negative `events` values, including `-1`, with no rejection. This can allow malformed metadata in an imported history archive. Scoped correction: reject negative event counts, with checksum-valid malformed cases and a real ZIP no-history-mutation regression, while preserving valid zero/positive counts, all original log/history files, gameplay/balance, scoring, app IDs, signing and release boundaries. No new design or gameplay mechanic is introduced.

## Execution state at claim

Implementation, tests, CI and merge not yet complete. GitHub connector access is available, but local private Git clone is unavailable in this execution environment (network resolution of github.com failed), so this work needs an isolated branch-scoped GitHub Actions source gate for actual Gradle/JUnit/Android Java compilation. Do not call a workflow trigger a passed test. No new APK assembled, original private signer verified, native/device accepted or published.

## Safe next steps

Implement scoped parser guard and three regressions; commit on the isolated branch; run complete source tests and Android/desktop Java compilation in dedicated CI. Review terminal CI and current remote refs before considering guarded integration. Preserve APD464 and previous accepted releases; do not build or publish without original-signer/native gates.

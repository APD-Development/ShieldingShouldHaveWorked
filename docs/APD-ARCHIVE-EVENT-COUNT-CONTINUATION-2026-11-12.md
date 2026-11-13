# APD archive manifest event-count validation, November 12, 2026

## Work claim (created 11/12/2026, 20:36:08 America/New_York)

Original regular ChatGPT APD development conversation. Live authoritative development branch `codex/apd-phone-fixes-2026-10-09` independently verified at `abac56c5df1a8564d41e0c38dfe832e78c349cb3`, not `main`. Isolated work branch `chatgpt/apd-archive-event-count-20261112-2035`. Existing archive flag-validation PR #57 and terminal handoff PR #58 are already merged; concurrent draft personal playback/animation and native APK packages are untouched. Searches of current PRs/issues found no event-count work claim. Do not duplicate this work package.

Source finding: format-1 archive manifests serialize a nonnegative count of log events; `ArchiveManifest.parse` currently accepts checksum-valid negative `events` values, including `-1`, with no rejection. This can allow malformed metadata in an imported history archive. Scoped correction: reject negative event counts, with checksum-valid malformed cases and a real ZIP no-history-mutation regression, while preserving valid zero/positive counts, all original log/history files, gameplay/balance, scoring, app IDs, signing and release boundaries. No new design or gameplay mechanic is introduced.

## Execution state at claim

Implementation, tests, CI and merge not yet complete. GitHub connector access is available, but local private Git clone is unavailable in this execution environment (network resolution of github.com failed), so this work needs an isolated branch-scoped GitHub Actions source gate for actual Gradle/JUnit/Android Java compilation. Do not call a workflow trigger a passed test. No new APK assembled, original private signer verified, native/device accepted or published.

## Safe next steps

Implement scoped parser guard and three regressions; commit on the isolated branch; run complete source tests and Android/desktop Java compilation in dedicated CI. Review terminal CI and current remote refs before considering guarded integration. Preserve APD464 and previous accepted releases; do not build or publish without original-signer/native gates.

## Implementation and verification checkpoint (11/12/2026, 20:38:39 America/New_York)

- Production fix: `e0e9356eac835bb9b42fb910107297c6e4956aab`, `ArchiveManifest.parse` now rejects negative per-file event metadata after parsing the long and before importing it. Existing zero/positive and current round trips unchanged.
- New JUnit tests: `a19389c3383241160446fe891f3f5a13a80b1f2f`, three regression methods in `ArchiveManifestEventCountTest`: checksum-valid negative counts including `Long.MIN_VALUE`, nonnegative boundary counts including `Long.MAX_VALUE`, and a real ZIP negative-count rejection preserving an existing history file byte for byte and leaving no staging folder.
- Dedicated full-core/desktop/Android Java gate: workflow commit `486a2396de9981dae1c45b9c4de04c8db014c942`. [GitHub Actions run 43346261344](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43346261344), exact workflow/source/test SHA above, was **in progress** at the last observation. Static preflight passed; full Gradle suite plus required Java compilation still running; JUnit gate and artifact upload not yet complete. **Do not claim tests passed or code integrated until actual terminal evidence.**
- [Draft PR #59](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/59) targets the authoritative branch and remains unmerged. The authoritative live head was independently rechecked at the unchanged start `abac56c5df1a8564d41e0c38dfe832e78c349cb3` after implementation.
- No APK compiled, signed, run on emulator/RedMagic or published; separate original private signer and native/screenshot/manual/publisher gates are prerequisites. No released APK contains this fix.

**Next:** inspect terminal Actions run and decoded JUnit/Gradle results; if green, recheck active work and both branch heads, then non-force expected-head integration. Commit the verified terminal receipt, distinguishing source CI from native acceptance. If the run fails, preserve the failure and correct the actual cause on this isolated branch without altering approved game rules.

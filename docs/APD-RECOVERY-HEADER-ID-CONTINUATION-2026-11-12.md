# APD destructive recovery log-header identity boundary, November 12, 2026 (Eastern)

## Isolated work claim, approximately 11:58 PM EST

Origination: regular ChatGPT APD development conversation. Verified live authoritative branch `codex/apd-phone-fixes-2026-10-09` at `19c16369964a6b982ab5dcd828fa37988022c728`, then created isolated branch `chatgpt/apd-recovery-header-id-20261112-2358` from that exact commit. Read `AGENTS.md`, completed-work ledger, latest recovery invalid-name handoff, run-index header identity handoff, recent integrated PRs #76–#79, open PRs, source CI and existing APD464 release. No open PR claims destructive recovery of a valid-named damaged log whose parsed header identifies another run. Separately active playback, native/release, GPL and search packages remain untouched.

### Evidenced distinct defect

`RunIndex.describeDirectLog` already treats a copied log whose header run ID differs from its valid filename as UNREADABLE under the filename. `StatsStore.recoverAll` checks valid file names and direct-child paths but calls `LogRecovery.repair` without checking that parsed header identity; a corrupt tail on such a copied log is backed up and truncated despite that file being rejected as the represented run by the derived index. This is a repository-evidenced consistency/safety defect, not a reported player-phone incident.

### Approved scope and gates

Apply a narrowly scoped identity check only on the `StatsStore.recoverAll` recovery path before any tail backup or truncation. Preserve all accepted matched-name damaged-log repairs and standalone `LogRecovery.repair(File)` behavior. Add actual-filesystem regressions proving copied mismatched log bytes and tail-sidecar absence, genuine matched-name repair and derived index treatment. Run full core JUnit and desktop/Android debug/release/instrumentation Java compilation on exact committed source through isolated CI. Recheck work claims, PR and live head before any expected-head non-force integration. No new mechanics, balance, saves, scoring, signing, app ID, edition or archived release edits. Existing APD464 release predates this work.

**At this claim:** no production code or tests changed, no CI run, APK, original-signer check, native Android acceptance or publication completed. No recurring task schedule changed.

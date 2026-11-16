# APD writer append reopen, identity-only scan, November 16, 2026

## Independent claim (10:23 EST)
- Authoritative source: `codex/apd-phone-fixes-2026-10-09` at `b6d8c8dcfc4c28c5c6a8a09e08fdb626b2f30258` as independently read from GitHub at 10:23 EST. This work is isolated on `chatgpt/apd-writer-reopen-identity-20261116-1023`.
- Inspected `AGENTS.md`, `docs/WORK-COMPLETED-LEDGER.md`, `docs/KNOWN-ISSUES-AND-SOURCE-GUIDE.md`, the handoff, existing RunLogReader/RunLogWriter/ReadOptions code, continuation #224 and open PRs. Existing C484/P484 build-only draft #223 and C480/P480 native-acceptance draft #211, plus all playback/UI/release branches, remain outside this work package.
- **Evidenced allocation concern:** `RunLogWriter.openForAppend` uses two ordinary `RunLogReader.read(file)` passes (before and after possible repair), retaining *every* historical `ApdEvent` payload in two `ReadResult.events` lists even though append only consumes header, seal, status, string table, event count, and last clock. `ReadOptions.onlyRunStartEvents(true)` is already implemented and tested to preserve those counters, string definitions, checksum and seal validation while avoiding retention of unrelated events. Reuse it at precisely the two writer reopen reads.
- Add a real unsealed-log regression exceeding the clock-frame interval, with repeated/new string identifiers and appended events, checking exact file prefix preservation, event order/count/clock/string recovery, and seal status. Preserve existing damaged-tail, mismatched-version and sealed-log tests.
- Do not change event formats, active gameplay, RNG, balance, scoring, saves/history, signing, application IDs, versionCode 920, edition boundaries, existing candidate APKs or publication. CI validation is required before integration. No APK or native device acceptance is claimed.
- No changes to the authoritative branch before successful checks. Re-read live head and coordinate a non-force expected-head integration or leave an isolated PR if checks cannot be completed.

## Verification and handoff
Pending. Record only CI, test, merge and build outcomes actually observed. No claim that memory use is quantitatively measured or that C467's OutOfMemoryError is fixed.

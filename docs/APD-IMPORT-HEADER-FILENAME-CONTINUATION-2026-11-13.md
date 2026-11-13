# APD archive import header/filename identity continuation, November 13, 2026 (Eastern)

## Isolated claim, approximately 01:18 EST

Originating regular ChatGPT APD conversation; no Codex task or automation changes. Live authoritative `codex/apd-phone-fixes-2026-10-09` verified at `541b3ab82757e26eaf99379f35e211ee3977ac1a`; independent branch `chatgpt/apd-import-header-filename-20261113-0118` starts from that exact commit. Reviewed root AGENTS.md, completed-work/known-issues handoffs, recent archive/import/index and release receipts, current open PRs and CI. No open PR claims this precise import identity boundary. Respect open visual playback, native/release, GPL and other work packages.

### Evidenced source defect, bounded approved repair

`ArchiveImporter.stage` checks the typed RUN_START logical run ID against the manifest's `runId`, but does not confirm that the raw log header's file ID equals the manifest's `runs/<log-id>.apdlog` filename. A fully checksummed archive can therefore import `runs/a.apdlog` containing an embedded header identifying `b` and a perfectly matching typed logical run start. `RunIndex.describeDirectLog` later rejects this mismatch as UNREADABLE. Fix only the per-file header-to-filename identity for `kind=run`, preserving the intentionally distinct shared typed run ID across legitimate segments and existing legacy imports. Refuse the entire import rather than depositing unreadable/misattributed data; never rewrite or delete previous history.

Add real-writer/archive regression for matching typed logical run but mismatching header filename, atomicity with a neighboring valid log and existing local history, and a genuine two-segment logical run where per-segment header IDs correctly match their filenames. Preserve archive format and manifest schema, saves/scoring/seed history, app IDs, signing, editions, gameplay balance, old releases and other claims. Verify exact committed source with the full core JUnit suite and desktop/Android debug/release/instrumentation Java compile. Native/phone/original signer acceptance and gated APK publication remain separate and must not be inferred from source CI.

At claim: no production change or tests yet; no APK assembled or published. Continue with exact code/test/CI commits, failures, ET test results, live expected-head guarded integration only if every gate passes. Do not modify task schedules.

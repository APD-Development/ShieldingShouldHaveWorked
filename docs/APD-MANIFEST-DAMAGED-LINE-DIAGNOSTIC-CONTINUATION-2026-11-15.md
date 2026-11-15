# APD history-manifest malformed-line diagnostic bound, November 15, 2026

## Scoped work claim (pending verification)

- Start: Sunday, November 15, 2026, approximately 5:00 PM Eastern; claim recorded at 11/15/2026, 17:03:20 Eastern. Independently verified authoritative development branch `codex/apd-phone-fixes-2026-10-09` at `26d5234dd14f73fd20dcecf3c1c97d952292a943`, not main.
- Isolated branch: `chatgpt/apd-manifest-diagnostic-limit-20261115-1700`. This branch owns only the history manifest parser's **error diagnostic length** and accompanying regression/CI evidence. It does not own export/archive acceptance, APD native testing, signing, gameplay, seed scoring, or any existing release candidate.
- Reviewed `AGENTS.md`, completion ledger and known issues, current `ArchiveManifest.parse` and importer, latest archive continuations, all currently open PR titles/scopes, previous C471/P471 APK source and exact-source CI receipts. None of the open PRs claims this malformed-line diagnostic path.
- Source-evidenced reliability gap: malformed signed manifest fields trigger `ArchiveManifest.parse`'s catch for `IndexOutOfBoundsException | NumberFormatException`, which appends **the entire unchecked line** to the thrown exception. `ArchiveManifest.MAX_BYTES` permits up to 16 MiB. `ArchiveImporter` copies that exception message into its user-visible refusal `Report.problem`, increasing temporary allocations and the report's size for a bad archive.
- Narrow proposed remedy: retain the exact original diagnostic for ordinary short malformed lines; cap only the embedded raw malformed-line excerpt for long lines and mark truncation. Do **not** change manifest byte/checksum/format/count semantics, accepted data, ZIP validation, history, saves, scores, app identity or signing.
- Proposed regression: a valid-checksum malformed short line retains its exact message, a much longer malformed line yields a bounded message, and a real ZIP using the long malformed manifest is rejected without changing existing history. A complete JVM suite and desktop/Android debug/release/instrumentation Java compilation are required for integration.
- This is a claim, **not yet a verified fix**. No new tests, APK, original-signer, native Android acceptance or publication are claimed at this point. Before primary integration, require exact-source successful CI, latest remote primary head and PR inspection, expected-head non-force update and independent readback. Preserve all old signed C471/P471 and prior releases. Do not duplicate prior completed missing/invalid/ignored/collision diagnostics or negative container validation.

## Verification and continuation

Pending. Record actual commits, CI terminal results, test/artifact evidence, merge result, time and blocked APK gates here after completion.

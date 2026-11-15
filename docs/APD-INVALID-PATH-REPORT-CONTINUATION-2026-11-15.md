# APD history ZIP invalid-path diagnostic bound, November 15, 2026

## Scoped independent claim (source not yet verified)

- Started Sunday, November 15, 2026, approximately 6:00 PM EST. Read-only remote verification found authoritative `codex/apd-phone-fixes-2026-10-09` at `dcb0a8bbb05f523783e7d0d1b5a5afa07f0decf6`, not `main`.
- Isolated branch: `chatgpt/apd-invalid-path-report-bound-20261115-1800`, derived from that exact head. Scope: only the **early refusal message for invalid file paths** in `ArchiveImporter.importZip`, plus focused regression tests and exact-source CI. No ownership of native acceptance, signing, release publishing, playback, other import diagnostics, game mechanics, or APK rebuilding.
- Read `AGENTS.md`, `docs/WORK-COMPLETED-LEDGER.md`, `docs/KNOWN-ISSUES-AND-SOURCE-GUIDE.md`, recent archive continuation and C472/P472 build receipts, open PR and branch lists, plus current `ArchiveManifest` and `ArchiveImporter` source. Open archive claims were not identified. Previous malformed-manifest-line report bound is complete and distinct.
- Evidenced gap: `ArchiveImporter` accepts an archive's correctly checksummed and syntactically complete manifest for preliminary parsing, then refuses disallowed file paths with `report.problem = "the archive lists a file it is not allowed to write: " + e.path`. The parser limits the manifest to 16 MiB, but an invalid `file` path can be hundreds of thousands of characters long and is copied whole into a player-visible error, even though import is correctly refused. Earlier malformed-line bounds do not cover a syntactically valid `file` record.
- Planned narrow correction: retain the **exact original message for short invalid paths**, but bound only the user-controlled long invalid path excerpt in this early refusal with a marked truncation. No change to which files are accepted, ZIP processing, checksums, history, file writes, scoring, balance, save schema or application identities.
- Regression plan: short invalid path keeps identical diagnostic, a valid-checksum manifest containing a very long invalid path yields bounded error, and a real ZIP with that manifest is refused without touching existing saved history or leaving staging files. Preserve separate archive and first-wins regressions.
- Integration gate: execute full core suite, desktop/Android debug, release and instrumentation Java compilation in an exact-source GitHub Actions run; verify terminal results and artifacts before merging. Recheck current primary head and use a guarded expected-head, non-force integration. Do not declare success before CI and independent GitHub readback.
- APK gate: previously signed C472/P472 candidates remain intact. Do not publish an unaccepted APK without native acceptance, signer/save-integrity proof and the established gated release procedure.

## Result and continuation

Pending actual source/test commits and verification. Record terminal successes/failures, exact commits, integration, Android APK status and next safe work here.

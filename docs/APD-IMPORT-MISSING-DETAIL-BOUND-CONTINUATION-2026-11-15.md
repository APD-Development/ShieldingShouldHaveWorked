# APD missing manifest-listed ZIP members: independent continuation (November 15, 2026 EST)

## Work claim at approximately 11:03 EST

- Existing repository only: `APD-Development/ShieldingShouldHaveWorked`; authoritative starting branch `codex/apd-phone-fixes-2026-10-09` at `04aeca52c667735a1b5e5d3f1752bd0a92fd1c53`. Isolated source branch `chatgpt/apd-import-missing-details-bound-20261115-1100`. No main, release, personal playback or other PR changes.
- Inspected AGENTS.md, completed-work ledger, known issues, archived PR #173/#174 continuation, current open PR scope and previous exact-source CI. This missing-manifest-member diagnostic issue is distinct from the completed bound on *unlisted/repeated* ZIP members.
- Source evidence: `ArchiveImporter.importZip` increments `problems` and appends a `report.details` line for *every* manifest-listed path absent from a ZIP. Format 1 permits up to 500,000 listed files within a 16 MiB manifest, so an otherwise correctly checksummed but incomplete ZIP can allocate a large in-memory diagnostic list even though it must reject the entire import.
- Planned bounded correction: retain exact error count, first 100 missing path details, then one omission message. Preserve all manifest validation, staging, checksum checks, ordinary short diagnostics, partial-import rejection and existing history. Tests use real ZIP streams and preserve an original existing log; no mechanics, saves or scoring changes.
- Source-only full-core plus desktop and Android Java compile PR CI. Native acceptance, original private signing, install-over/save continuity and qualified release gate remain separate. No APK or passed test is claimed by this work claim.

## Verification and integration

Pending actual results. Record exact source hashes, test outputs and CI status, and compare live authoritative head before any guarded non-force integration. An unqualified source patch must not be packaged as C471/P471 or published.

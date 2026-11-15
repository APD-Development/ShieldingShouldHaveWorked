# APD history ZIP ignored-entry diagnostic bound: independent continuation (November 15, 2026 EST)

## Claim, approximately 10:03 EST

- Authoritative starting branch: `codex/apd-phone-fixes-2026-10-09` at `98ce0aa5c2004a308e2a7def8075c6fe55279e1a`; isolated source branch: `chatgpt/apd-import-extra-report-bound-20261115-1000`. Do not edit main, release candidates, private playback/native branches or shared work packages.
- Inspected AGENTS.md, the completed-work ledger, current handoff, recent PRs #169–#172 and the open PR claims. No open PR claims this independent importer-report scope.
- Source evidence: `ArchiveImporter.importZip` adds an unconstrained `Report.details` string for **each** extra or repeated ZIP entry, despite only using the first listed entry for verified import. An archive with many ignored entries can produce unnecessarily large in-memory diagnostic output, regardless of whether any player history is imported. This is a bounded reliability finding from source, **not** an established cause of C467's OutOfMemoryError.
- Scope: cap individually displayed ignored-entry diagnostics and add one explicit truncation note; still scan/drain every unexpected ZIP entry with the existing size bound, preserve first-wins, valid archive import, history, counts, rejection criteria, save/scoring and all application identities.
- Implement two JVM regressions for ordinary unexpected-entry visibility and a large unexpected-entry set that successfully imports its one valid log with bounded diagnostics. Execute complete core and desktop/Android Java source tests in branch-scoped CI. Only after exact-source verification, check remote head, then integrate without force/overlap.
- No APK is claimed. Original signing, native Android acceptance, install-over/history continuity and publication gates remain independent. Keep C471/P471 candidate evidence intact.

## Execution evidence (fill only after results are verified)

- Pending: source/test commits, actual CI results, guarded integration, APK blocker and final branch head.

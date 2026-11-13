# APD archive container metadata singleton, November 12, 2026 (Eastern)

## Isolated work claim (9:30 PM EST)

This execution in the existing regular ChatGPT APD project verified the live authoritative `codex/apd-phone-fixes-2026-10-09` branch at `7b2af9d86f61370589034c10c9bb7bf08eb3a19d`, not main. It read AGENTS.md, the owner-approved archive contract, current parser/import/export source, the completed repeated-header/duplicate-count handoffs, open PRs, source CI and release inventory. Isolated branch: `chatgpt/apd-archive-container-singleton-20261112-2130`. No open PR or issue claimed this container-record repair. Other ongoing playback, wall-search and original-signed APK/native/release work remain independently owned and untouched.

Source-evidenced defect: `ArchiveManifest.encode` writes exactly one `container` metadata record for the raw log container format; `ArchiveManifest.parse` silently accepts and overwrites repeated `container` values even when the manifest checksum is correct. A duplicated contradictory record makes the archive's authoritative metadata ambiguous. Correct only this integrity boundary: reject a second recognized `container` line, including identical duplicates, while preserving a single valid container value, unknown future records, the existing first-wins duplicated file-path compatibility, saves, original history, scoring, gameplay, format, signing and edition identity.

At claim time: no production edits, tests, CI, native acceptance, APK build or publication. The repository's original private signing inputs/native qualification gates are not available through a source-only test workflow. Follow isolated branch source/JUnit+desktop/Android Java CI, inspect terminal receipts, then use fresh target/PR heads and expected-head non-force merge only if verified safe. No scheduled tasks were changed.

## Implementation and verification checkpoint (9:32 PM EST)

- Work claim: `5f21f55c6d72f184dae0e5591340636854490e13`.
- Source fix: `84823474e49a48e1cecc016b01270bda88509c49`. Only `ArchiveManifest.parse` changed: it now remembers the first recognized `container` record and refuses a second. No format, game, save or scoring change.
- Four new JUnit regression tests: `11b26626cb7c8d2002556731487894d4b9561aa2` in `ArchiveManifestContainerSingletonTest`. They cover contradicting and identical checksum-valid duplicate container records, valid/unknown future metadata compatibility, and real ZIP import refusal preserving existing log bytes and absent staging files.
- Dedicated source-only workflow: `d59286112515b1a8bad0fc2f39dfb4c978ce6e90`, [Actions run 43352817119](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43352817119), exact source SHA, attempt 1, job `131647704107`. Static scoped preflight passed; complete core JUnit and desktop/Android Java compilation were still **in progress** at checkpoint, with no terminal test result yet.
- [Draft PR #65](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/65) targets the authoritative development branch and contains precisely four files: parser, one new test class, isolated workflow and this handoff. The published APD464 and held candidates have not been touched or rebuilt. No APK, original signing, emulator/native or physical-phone acceptance, or publication has occurred in this package.

Next: inspect terminal CI and JUnit acceptance, recheck branch and all work claims, then perform a guarded expected-PR-head non-force merge only if the exact source passes. Do not report this pending check as successful or confuse source Java compilation with native/APK gates.

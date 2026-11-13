# APD post-seal archive privacy continuation, November 13, 2026 (Eastern)

## Isolated work claim, Friday November 13, 2026, 1:32 AM EST

Originating regular ChatGPT APD project. Live authoritative development branch `codex/apd-phone-fixes-2026-10-09` independently verified at `a0fb61d7369ec1581a18a74b5a73517960c077ac` after integrated PR #88. Isolated branch `chatgpt/apd-archive-postseal-tail-20261113-0132` starts at that exact commit. Read `AGENTS.md`, current terminal archive privacy handoff (PR #86/#87), recent import header receipt (PR #88), open PRs, CI runs, releases, `ArchiveExporter`, `RunLogReader`, `LogRecovery`, format docs and real writer/export tests. At claim, no open PR claims this exact post-SEAL trailing-byte export boundary. Do not overlap active playback, GPL, native/release or other APD source packages. No task schedule changes.

### Source-evidenced defect and bounded intent

`RunLogReader.parse` stops decoding on the SEAL frame but preserves `validBytes` and `fileLength`; after a valid seal, appended bytes are not interpreted yet the reader can still report `ReadStatus.SEALED` and `status.isClean()`. Recovery explicitly recognizes and archives such trailing bytes. The newly merged archive completion guard uses `r.sealed && r.status.isClean()` to allow raw export after valid terminal `RUN_END` (possibly followed by a legitimate `RANKING_LINK`). Consequently extra bytes appended after SEAL do not prevent a raw, hidden-facts-containing export, even though the physical file does not end cleanly at that SEAL.

Scope: only tighten `ArchiveExporter.collect` to require `r.damagedBytes() == 0` before it treats this latest log as unambiguously complete. Leave original logs, format, parsing/recovery semantics, valid ranked-run trailing event, archive import first-wins compatibility, gameplay, balance, saves, scoring, signing, app IDs and editions unchanged. Add real-writer/real-ZIP regressions for post-SEAL junk and valid decodable post-SEAL frames; assert redaction, hidden seed/item privacy, byte-identical original history, and unchanged legitimate clean ranked export.

Run full `:core:test`, desktop classes and Android debug/release/instrumentation Java compilation on exact source in isolated GitHub Actions. Source compilation is not original-signed APK construction or native Android acceptance. Do not merge to shared authoritative branch before terminal successful exact-source CI and renewed live remote head/claim checks. All previously published APD464 and blocked release candidates remain intact.

### Initial execution state

At claim: no production code changed, tests run, APK assembled/signed, native checks or release performed. Subsequent sections must record actual commits/CI results and final gates honestly.

## Production/test/CI checkpoint, Friday November 13, 2026, about 1:34 AM EST

- Source guard commit `ceb56bd063d5fe8046f41af03c95f69524047ba3` tightens only `ArchiveExporter.collect` to require `r.damagedBytes() == 0` alongside the already established seal/clean/end checks. No log reader or recovery change.
- Two tests in `ArchiveTerminalEndPrivacyTest` commit `7a87149febad91a94828e6d881d6d958e36bfdf6`: sealed log plus post-seal junk and sealed log plus valid checksummed post-seal frame, both use actual writer, filesystem, ZIP and assert privacy and immutable original bytes. Previous clean finished and ranked trailer tests remain.
- Exact-source dedicated full-test/desktop/Android Java compile workflow committed at `8cb23884ae3ab37ea82fbb8373fdf1fcb6c481b7`; [run 43379508117](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43379508117) was **in progress** at this checkpoint. It is not a passed-test claim.
- [Draft PR #89](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/89) opened against authoritative branch at `a0fb61d7369ec1581a18a74b5a73517960c077ac`; exact comparison shows 4 isolated files, four commits ahead and zero behind at this checkpoint.
- [APD464](https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/tag/apd-464) is an independently published pre-existing release. No new APK was built, signed, natively tested or published. Its original signer/install-over/save identity and native publication gate have not been exercised here.
- No scheduled task prompts or schedules changed. Retain earlier native/release/playback claims, blocked builds and owners' game design decisions.

Next: read terminal exact-source workflow status and actual JUnit results; correct only evidenced failures. If passed, refresh live authoritative head and open claims and integrate using a guarded, non-force operation. Never use this source-only CI as Android native or original-signer acceptance.

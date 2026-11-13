# APD post-seal archive privacy continuation, November 13, 2026 (Eastern)

## Isolated work claim, Friday November 13, 2026, 1:32 AM EST

Originating regular ChatGPT APD project. Live authoritative development branch `codex/apd-phone-fixes-2026-10-09` independently verified at `a0fb61d7369ec1581a18a74b5a73517960c077ac` after integrated PR #88. Isolated branch `chatgpt/apd-archive-postseal-tail-20261113-0132` starts at that exact commit. Read `AGENTS.md`, current terminal archive privacy handoff (PR #86/#87), recent import header receipt (PR #88), open PRs, CI runs, releases, `ArchiveExporter`, `RunLogReader`, `LogRecovery`, format docs and real writer/export tests. At claim, no open PR claims this exact post-SEAL trailing-byte export boundary. Do not overlap active playback, GPL, native/release or other APD source packages. No task schedule changes.

### Source-evidenced defect and bounded intent

`RunLogReader.parse` stops decoding on the SEAL frame but preserves `validBytes` and `fileLength`; after a valid seal, appended bytes are not interpreted yet the reader can still report `ReadStatus.SEALED` and `status.isClean()`. Recovery explicitly recognizes and archives such trailing bytes. The newly merged archive completion guard uses `r.sealed && r.status.isClean()` to allow raw export after valid terminal `RUN_END` (possibly followed by a legitimate `RANKING_LINK`). Consequently extra bytes appended after SEAL do not prevent a raw, hidden-facts-containing export, even though the physical file does not end cleanly at that SEAL.

Scope: only tighten `ArchiveExporter.collect` to require `r.damagedBytes() == 0` before it treats this latest log as unambiguously complete. Leave original logs, format, parsing/recovery semantics, valid ranked-run trailing event, archive import first-wins compatibility, gameplay, balance, saves, scoring, signing, app IDs and editions unchanged. Add real-writer/real-ZIP regressions for post-SEAL junk and valid decodable post-SEAL frames; assert redaction, hidden seed/item privacy, byte-identical original history, and unchanged legitimate clean ranked export.

Run full `:core:test`, desktop classes and Android debug/release/instrumentation Java compilation on exact source in isolated GitHub Actions. Source compilation is not original-signed APK construction or native Android acceptance. Do not merge to shared authoritative branch before terminal successful exact-source CI and renewed live remote head/claim checks. All previously published APD464 and blocked release candidates remain intact.

### Initial execution state

At claim: no production code changed, tests run, APK assembled/signed, native checks or release performed. Subsequent sections must record actual commits/CI results and final gates honestly.

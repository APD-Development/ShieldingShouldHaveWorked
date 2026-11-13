# APD archive-export external symlink boundary, November 12, 2026 (Eastern)

## Actual execution and coordination

Started about 7:00 PM EST, November 12, 2026. Queried the connected GitHub repository, not main or any upstream source. The authoritative `codex/apd-phone-fixes-2026-10-09` branch head was **`bf9b86c697d15237a868c4958473eda1043cae42`** when this independent work began; `main` was `8228fbcb030b3b0a2a787b6e3ea1cb8593e3e13d`. Read root AGENTS.md, the completed-work ledger, known issues, current-state handoff, archive import/recovery symlink receipts and the live open PR list. APD463 is published from a separate frozen pipeline. Independent APD464 personal playback and sprite-animation claims are owned by other work; this source-only package does not modify their APK sources, work branches, signing, native evidence, release procedures or assets. Existing archive *import* and destructive *recovery* symlink guards were already completed and are not repeated.

## Identified existing reliability/privacy gap

`ArchiveExporter.collect` used `File.listFiles` and `File.isFile`, both of which follow symbolic links. A linked `runs/` or `legacy/` folder, or a plain-ID `*.apdlog` symlink inside either, could cause export to read files outside the requested canonical history folder. Import and destructive recovery already had independent canonical-path protections; exporter did not. This is a source-level exposure path, not evidence that an owner's own history was leaked.

## Isolated code, tests and claims

- Branch: `chatgpt/apd-export-symlink-boundary-20261112-1905`, forked from exact authoritative `bf9b86c...`, no shared branch writes.
- Production commit **`9420235648a5902c5621a0bf3713835629c932a1`** adds canonical direct-child checks before traversing `runs/`/`legacy/`, before reading each eligible log, and again when the archive payload is measured and written. A malformed alias fails the export with an explanation rather than producing a misleading partial backup. Legitimate symlink access to the supplied root itself remains supported. No format, redaction, save, mechanics, scoring, balance, application ID, signature or asset changes.
- Test commit **`4692c2436da5092f827a941b1ebdc8378840a880`** adds `ArchiveExporterSymlinkTest`: linked external run/legacy files, linked external run/legacy directories, and normal export through a canonical root alias. Tests insist a refused export has written zero bytes, external and genuine history remain unchanged, and a normal export still works.
- CI workflow commit **`6f28fabf027ad4d58429fbe5fdf4e2699b1a9fe7`** adds a dedicated source gate (full `:core:test`, desktop classes, Android debug/release/instrumentation Java compilation, JUnit XML totals, existing archive duplicate first-wins and output-stream ownership regressions). The workflow has GitHub Actions read-only contents permission and builds no APK.
- [Draft PR #49](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/49) targets the authoritative development branch, **not main**. It must not be merged without actual CI verification and a fresh live-head check.

## Verification at initial checkpoint

[GitHub Actions run 43334098035](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43334098035), on exact CI commit `6f28fabf...`, began at 7:04 PM EST and was still running at this checkpoint. Only the workflow source check was independently observed passing. Full JUnit and Android Java compilation are **pending**; no count or successful final conclusion is claimed. No local tests ran because this container cannot resolve github.com for cloning, and a local Android SDK/signer workflow was not used. No game or instrumentation APK was assembled, no original-signer acceptance was performed, no native emulator or physical-phone test was run, and no release was published by this execution.

## Next safe work

Read the actual final CI conclusion, targeted JUnit cases, full totals and required Java compiles. If CI fails, repair the exact error on this isolated branch, keeping failure evidence. If it passes, verify PR diff and live base/head, update this receipt with results, and perform guarded non-force integration only if no newer work conflicts. Leave the separate APD463 and APD464 native/signer/publication packages untouched; this isolated change does not independently qualify a new APK. Preserve every old signed APK and original history.

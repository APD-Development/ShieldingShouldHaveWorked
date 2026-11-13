# APD archive manifest boolean-flag validation, November 12, 2026

## Isolated work claim

Started 11/12/2026, 20:16:23 EST (local America/New_York; UTC 2026-11-13T01:16:23.025Z) in the originating regular ChatGPT APD conversation. The authoritative development branch `codex/apd-phone-fixes-2026-10-09` was independently re-read at `d8075a0007b350e256466106d3209facdd2bee2b`; no assumption that `main` is current. Isolated branch: `chatgpt/apd-archive-bool-flags-20261112-2015`. This work does not touch gameplay/balance, user saves/history bytes, scoring, native acceptance, signing, application identity, release assets, APK numbers, or the concurrently claimed playback/Android and prior archive format/size/path tasks. Previous parser format-floor work in PR #51 and fallback ZIP selection PR #55 are already merged; this is a different manifest field-validation gap.

## Source evidence and approved scope

The v1 manifest documents serialized `sealed 0/1` and `redacted 0/1` file flags, and its writer emits only 0 or 1. The parser previously interpreted any other token as `false`. A checksum-valid malformed archive could silently convert invalid flags to false, changing import reporting about redacted logs. Require the existing two defined tokens before parsing; preserve valid v1 archives, legacy first-wins repeated-path behavior, unknown metadata lines, raw log data, and all importer failure boundaries.

## Work status at claim

Implementation, isolated source CI, and merge have not yet been performed. No Android APK is built, signed, native-tested or published in this work package. Do not mistake claims or a workflow trigger for verified execution.

## Next safe steps

Implement strict validation and regression tests for both flags, including an import rejection without history mutation. Run the complete core suite and compile desktop/Android debug/release/instrumentation Java through an isolated branch-scoped CI gate. Inspect terminal run, job logs and JUnit results, then recheck live PR and base SHA before any expected-head non-force integration. Record accurate outcomes and blockers before exit.

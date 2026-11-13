# APD archive scalar metadata singleton validation, November 12, 2026 (Eastern)

## Isolated work claim (Thursday, November 12, 2026 at 9:42:45 PM EST)

Owner-directed regular ChatGPT APD execution. Live authoritative branch `codex/apd-phone-fixes-2026-10-09` was verified at `c22d240a9b597bb693f5bac6b6b827774e0f06e0`; `main` is not the development baseline. AGENTS.md, recent archive singleton handoffs, the current archive parser and tests, active PRs, branch commits, CI and release records were inspected. Independent branch: `chatgpt/apd-archive-scalar-singletons-20261112-2142`. No open pull request claims scalar `created`, `app`, or `telemetry` manifest duplicate validation. Preserve unrelated active playback, native Android, wall search, original signing and release work without shared-branch writes.

Source-evidenced defect: `ArchiveManifest.encode()` emits exactly one of each recognized `created`, `app`, and `telemetry` metadata records, but `ArchiveManifest.parse()` accepts repeated checksum-valid records and silently replaces the prior values. This allows ambiguous archive provenance/timestamps. Scope is strictly to reject a second recognized line for each of these three keys, whether identical or contradictory. Do not require fields that historical manifests may omit, alter the v1 file format, unknown future metadata, duplicate log-path first-wins compatibility, saves, gameplay, history, scoring, signer, app IDs, or edition boundaries. Provide real checksum-valid unit and non-mutating ZIP import regressions.

At claim: no production change, no tests executed, no APK assembly, no native Android/phone acceptance, no original signer and no publication. Create a dedicated source-only CI gate for complete `:core:test`, desktop and Android Java compiles. Merge only after exact-source terminal success and fresh live authoritative and PR checks with expected-head non-force merge. Previously published APD464 remains unchanged; release records checked independently. No recurring tasks changed.

## Continuation
Pending source/test/workflow commits and exact verification results. This initial claim is not evidence of completion.

# APD run-index filename/header identity boundary, November 12, 2026

## Scope and isolated claim

Originating regular ChatGPT APD execution; no separate Codex task. Live authoritative branch `codex/apd-phone-fixes-2026-10-09` was verified at `4d7791020359341d3cb906fa8c429e3c2e94ca7f` immediately before creation of isolated branch `chatgpt/apd-index-header-identity-20261112`. Read AGENTS.md, the completed-work ledger, source guide, previous index filename handoff and PR #74/#75, open PRs, recent Actions results and release claims. This identity mismatch is distinct from the completed invalid-filename check. No active open PR claims this exact subcase. Playback, native, release, wall-search and existing claims remain untouched.

### Evidenced defect

The derived RunIndex currently verifies the log filename but can still accept a checksum-valid cache row whose `runId` points to a different log than `logFile`. Likewise `rebuild` and `refresh` may take a valid but mismatched run ID from a copied log's header, attributing one physical log to a different history identity. This changes only a derived summary. Original logs remain authoritative and must never be modified by this repair.

### Narrow planned correction

Reject checksum-valid derived cache entries where log filename stem and entry run ID differ; rebuild them from original logs. Treat a direct log whose readable header identifies a different run as UNREADABLE under its real on-disk filename instead of attributing it to the header's different identity. Preserve valid log handling, damaged-log visibility, symlinked history root support, original history bytes, score/save compatibility, gameplay, signer, app identity and prior releases.

Three real-filesystem JUnit regressions cover rebuild, refresh, and checksum-valid old-cache mismatch. Execute full core JUnit, desktop classes, and Android debug/release/instrumentation Java compilation on the exact proposed source. No APK or native device acceptance may be inferred from source compilation. Integrate only after terminal success and fresh remote head / concurrent PR check; do not force-update or overwrite conflicting work.

At claim: no tests passed, no game APK created/signed, no native acceptance or publication occurred. Existing APD464 and earlier builds remain untouched.

## Terminal exact-source CI, Thursday November 12, 2026, approximately 11:21 PM EST

Production correction `ad217a2c67277f7b866fb8e9dbb6678a0202ae3e`, real-filesystem regression commit `96332faa5a1017af210b1690658373f57481dd83`, and source gate commit `12efeabc845309e598f4b85727c695e9ae2f02bc`. Draft [PR #76](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/76) contains exactly four changed paths: `RunIndex.java`, the three-test regression class, dedicated workflow and this handoff. [Exact Actions run 43364634799](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43364634799), attempt 1, concluded **success** on tested commit `12efeabc845309e598f4b85727c695e9ae2f02bc`. Job `131686039492` raw log records `BUILD SUCCESSFUL in 2m 37s`; terminal XML gate passed **3,371 core tests / 491 suites, zero failures, zero errors, zero skips**. Required `:core:test`, `:desktop:classes`, Android debug/release and debug AndroidTest Java compilation all passed. The CI gate explicitly verified all three new identity regressions and six established history/index tests. JUnit artifact `14009722900` uploaded (546,501 bytes). This is **source and Java compilation only**, not assembled/signed APK or native/physical Android acceptance.

The remote authoritative branch and PR were re-read after CI: target still at `4d7791020359341d3cb906fa8c429e3c2e94ca7f`, PR clean/mergeable, no claimed overlapping work identified. This post-CI documentation commit is not the CI-tested source commit. Integrate with a fresh expected-head non-force update, verify independent readback and write a final integration receipt. Do not alter gameplay, historical evidence, native/release packages or task automation.

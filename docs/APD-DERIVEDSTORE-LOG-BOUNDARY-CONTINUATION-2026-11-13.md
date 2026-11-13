# APD derived-facts source-log boundary continuation, 2026-11-13

## Isolated claim and verified starting context

Regular ChatGPT APD continuation only. Eastern starting time: 2026-11-13 approximately 00:31 EST. Authoritative branch: `codex/apd-phone-fixes-2026-10-09`; independently re-read starting head `3451afa6a47e1662da06402144c56885d1ff86fa`. Isolated feature branch: `chatgpt/apd-derivedstore-log-boundary-20261113-0031`. No edits to main or other work branches, PRs, existing releases, or automation.

Reviewed repository AGENTS.md, completed-work ledger, current recovery/index handoffs, PR inventory and the separate storage-meter PR #82. This claim is **read-only DerivedStore source discovery for derived facts**, not StorageMeter, RunIndex, StatsStore recovery, archive import, game playback, native testing, or APK publication. Already-completed source fixes and currently claimed Android/release tasks remain intact. No existing open PR claimed DerivedStore.logs at claim time.

### Evidenced problem and bounded correction

`DerivedStore.logs(File dir)` currently uses `f.isFile()` and suffix only. Java follows per-file symlinks, so an external `.apdlog` or a local alias of another file is fingerprinted and included in derived APD/legacy facts. `RunIndex` and `StatsStore.recoverAll` already exclude these aliases by canonical direct-child path ownership. Align only DerivedStore's **read-only source enumeration** with the established canonical direct-child rule, allowing an intentionally symlinked storage root. Preserve legitimate raw history, facts, cache schema, gameplay, scoring, saves, signing identity, application IDs and all releases. No deletion or rewrite of source logs.

Planned three real-filesystem regressions: direct APD log external/local symlink exclusion including derived cache; same boundary for imported legacy history; and compatibility with a symlinked history root. Run the full core JUnit suite and desktop/Android debug/release/instrumentation Java compilation via an isolated source-only workflow. No Java CI success implies native Android acceptance, signed APK or release.

**At claim:** no production code changed, no tests executed, no APK assembled/signed/tested/published. Existing APD464 unchanged. Completion to be appended with exact commits, raw test receipt and further blockers.

## Exact source correction and terminal CI, Friday November 13, 2026, approximately 00:35 EST

Source-only production commit `f7bb9b0bd876f16f64b48ccdd120196e3488ba64` adds a canonical direct-child ownership guard in `DerivedStore.logs()` for both `runs/` and `legacy/`, so external per-file symlinks and in-directory aliases do not become derived rows or source fingerprints. The intentionally linked history root remains supported. Three real-filesystem regression cases were committed at `f495cb89d93d717d09f41d96beed57bcf4ee0905`. Exact source CI workflow commit `164c7c97ee7f68ece08efb38de5fa8d6a97628ce` is the tested commit and opens [PR #84](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/84).

[GitHub Actions run 43372758416](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43372758416), attempt 1, exact head `164c7c97ee7f68ece08efb38de5fa8d6a97628ce`, reached **terminal success** at 2026-11-13 05:35:27 UTC (00:35:27 EST). The job log says `BUILD SUCCESSFUL in 1m 46s`. Full JUnit XML gate counted **3,381 tests in 494 suites, zero failures, zero errors, zero skips**, including all three new regressions and the selected previous history/index/cache safeguards. The same CI job passed `:core:test`, `:desktop:classes`, `:android:compileDebugJavaWithJavac`, `:android:compileReleaseJavaWithJavac`, and `:android:compileDebugAndroidTestJavaWithJavac`. JUnit artifact `14013770805`, `apd-derivedstore-boundary-junit`, size 545,362 bytes, was uploaded; raw log reports artifact ZIP SHA-256 `28e41680f5a4e8078287ac8e6f9224256cfac056cf070dc055bb4022bc8d8eb7`.

The final documentation-only commit is **not** a second CI run and does not modify executable source. Before integration, re-read live target head, PR #84 and other active claims. Integrate only via expected-head non-force update after confirming target has not moved, and preserve all already merged fixes. If target moved, reconcile rather than reusing the feature tree as a replacement for newer target work.

**Build and publication boundary:** no APK assembled, signed, natively tested, installed on the owner's RedMagic, or published by this work package. Local execution lacks Android SDK and original private signing inputs; production game APK qualification requires independently owned exact-APK signer, native, save-install-over, manual screenshot/source and gated publication evidence. Existing APD464 release remains intact and does not contain this correction. No recurring task was changed.

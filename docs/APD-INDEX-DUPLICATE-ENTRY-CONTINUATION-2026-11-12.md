# APD derived run-index duplicate identity continuation, November 12, 2026 (EST)

## Work ownership and baseline
The 4:07 PM EST execution read the private owner's repository, root AGENTS.md, active ledger and handoffs, merged PR #39 index-tail guard and PR #40 TabFile integrity receipt, open pull requests, CI and separately published APD462. Authoritative development base was 1774c76f21b55ced1827957e9b620c9ef1b62413 on codex/apd-phone-fixes-2026-10-09, not main. This bounded package is isolated on chatgpt/apd-index-duplicate-entry-20261112-1608; it does not touch the concurrent APD462 integration, Android native, APK/signing, wall-search, or prior run-index-tail packages. No upstream material used.

## Source-level defect and bounded fix
The existing RunIndex.readFrom accepts a checksum-valid index with two entries sharing an identical run ID. Its map insertion silently lets the later entry overwrite the earlier one, potentially presenting a falsified or zero-event summary despite preserved original run logs. This is a source-level derived-data integrity vulnerability, **not** a confirmed device incident. The change rejects the duplicate as malformed derived data and lets the existing RunIndex.load fallback rebuild from immutable original logs. It does not rewrite run logs or introduce new schema, gameplay, scoring, balance, RNG, package, signing or edition changes.

A new real-filesystem JUnit regression creates a valid sealed run with one event, substitutes a valid-CRC two-entry index with a second zero-event summary for the same run ID, and proves loader rebuild, original log byte preservation, one correct event, and clean subsequent index reload. Existing empty-index, unsigned-count and exact-length regressions remain.

## Verification and continuation
This initial handoff is a source change proposal, not a claim of passing CI. An isolated workflow will require the new regression, existing compatibility tests, the complete core suite with zero failures/errors/skips, desktop and Android debug/release/instrumentation Java compilation. Inspect terminal job results and actual JUnit XML before merging. GitHub Actions compilation is not a signed or native APK acceptance. Do not publish a new game binary without original-signer, complete native/visual and existing guarded release gates. APD462 and all accepted/blocked old binaries remain untouched.

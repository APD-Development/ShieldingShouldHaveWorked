# APD manifest size-overflow refusal continuation (November 12, 2026)

## Isolated source changes, verification pending

Started from the live established development branch `codex/apd-phone-fixes-2026-10-09` at `da79c8b1b195a7f253521e8e19c0a212369dc5b8` (the verified PR #17 archive-stream refusal merge). Work is isolated on `chatgpt/apd-manifest-size-overflow-20261112` and does not touch the shared branch, APD461 draft PR #6, or any signed APK/release. Workflow launch on 2026-11-12 18:04:16 UTC / 13:04:16 America/New_York (EST).

Observed source defect: `ArchiveManifest.parse` summed untrusted nonnegative `long` file sizes without checking signed overflow. A syntactically valid, correctly checksummed manifest could encode two entries sized `Long.MAX_VALUE` and `1`, producing a wrapped negative total and passing the manifest's count comparison. This is a source-level proven malformed-input condition, not a claimed device incident.

Implementation: commit `862372ef6bd0f94c0beda4e1fc03205c18b4e27d` rejects an overflowed manifest total before import staging. Regression commit `86ebe790c5a04a8b9280635d52b2ebc559a6a5d7` covers checksum-valid overflow refusal and acceptance of the exact maximum representable non-overflowing total. Regression commit `2300ad8af6f36806955bf3699763488d98d1a530` ensures `ArchiveImporter` rejects the malformed manifest with `imported=0` and no changed history. Workflow commit `3e90e1a8eaba63099de64316a8b30074d5fc0add` launches the full APD core suite plus desktop/Android Java compilation, with explicit assertions for both regressions and clean JUnit reports.

Current verification status: **PENDING**. Exact Actions run [43275984687](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43275984687) began against head `3e90e1a8eaba63099de64316a8b30074d5fc0add`. Do not call tests passed, claim a new Android APK, merge, or change published release status without fresh terminal evidence. The most recently checked published original-signer APK pair is APD460. APD461 remains separately gated and unaccepted until its own native acceptance and publisher verify.

No change to valid archive format, save compatibility, stats/scoring, game balance, signer, application IDs, editions, or known-good releases. For continuation, inspect live development branch/PRs and this CI run; fix any real failures on the isolated branch, reverify exact source, and integrate only with the expected PR head SHA and no conflicting shared-branch modifications. Leave an exact outcome and next safe work here.

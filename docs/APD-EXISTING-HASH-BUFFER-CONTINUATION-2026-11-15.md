# APD repeated existing-history hash comparisons, November 15, 2026 Eastern

## Isolated source work claim, approximately 9 PM EST
- Authoritative starting branch: `codex/apd-phone-fixes-2026-10-09` at verified commit `98accb9ecb5b6d63080ab71812577c270b383b58`. Isolated work branch: `chatgpt/apd-existing-hash-buffer-20261115-2100`.
- Reviewed `AGENTS.md`, completed PR #195/#196 and its CI/handoff, open PR claims, workflow receipts, and the current import implementation and real-archive fixtures. No open PR claims this local hash-buffer scope.
- Observed a separate allocation source after completed ZIP-entry buffer reuse: `sameContent` creates a fresh 16 KiB array per already-existing filename, and fallback `copy` also allocates one. Large repetitive imports cause needless cumulative allocation.
- This candidate shares one 16 KiB local buffer within `commit` across file-hash comparisons and fallback copies; never shares between imports. It does not alter archive acceptance, original log bytes, skip/collision counts, ZIP processing, scoring, gameplay, signing, app identities or formats.
- Added a real ZIP regression with 96 identical local logs, 12 different local logs and one new log, asserting exact preservation, counts, imported bytes and temporary-folder cleanup. The existing large collision and mixed ZIP acceptance tests remain required.
- A dedicated exact-source GitHub Actions workflow checks all core tests and desktop/Android debug, release and instrumentation Java compilation. CI results, integration and build status must be recorded separately after the run is observed, not assumed.
- No original-signed APK, native Android acceptance, install-over/save test or publication is claimed here; preserve prior signed C473/P473 and all known-good releases. Native/release claims are separate.

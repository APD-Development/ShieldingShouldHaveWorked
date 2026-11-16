# APD archive existing-log size short-circuit, November 15, 2026 Eastern

## Scoped isolated source claim, approximately 10 PM EST

- Confirmed authoritative starting head `f4f573387d87007da518e79056de144ef9dc9ff1` on `codex/apd-phone-fixes-2026-10-09`, not main. Isolated branch: `chatgpt/apd-existing-size-fastpath-20261115-2200`.
- Read AGENTS.md, work-completed ledger and current handoffs, all open PR claims, prior PR #197/#198 and exact CI/C474-P474 receipts, and ArchiveImporter plus its real-ZIP regression. No open claim owns the differing-file-length hash fast path.
- Evidenced separate I/O cost: even when an existing history file's length differs from the manifest's verified archived file size, `sameContent` previously opened and SHA-256-hashed the entire local file. That cannot produce an identical file. Reject size mismatches before opening the file, while retaining full SHA-256 verification for same-sized files and leaving every local original unchanged.
- Regression uses a 3 MiB different-sized existing log, a same-sized different log, an exact duplicate, and a new log in one real ZIP. Assert exact import/skip/collision counts, local byte preservation and staging cleanup. The existing 3,514 tests and archive compatibility semantics remain required.
- This is a strictly behavior-preserving import performance improvement, not an OOM root-cause claim. It does not touch ZIP validation, manifest semantics, gameplay, scoring, saves, history schema, signing, application IDs, edition flags, native/release gates or previous C474/P474 candidates.
- CI, integration and build status are **unverified at this initial claim checkpoint**. Require exact-source full JUnit and desktop/Android Java compilation before any authoritative integration. GitHub Actions JVM compilation is not native Android acceptance. Preserve independent playback, native and release packages.

## Continuation to complete after verification

- Record actual source commit, tree, CI run and JUnit artifact, results and timestamps; check the live primary head again before any expected-head non-force update. If native Android, signer/save or gated release is not verified, preserve candidates and mark the exact blocker.

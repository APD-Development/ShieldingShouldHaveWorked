# APD shared ZIP entry scratch-buffer reuse, November 15, 2026 (Eastern)

## Scoped claim (around 8 PM EST)
- Started from verified authoritative `codex/apd-phone-fixes-2026-10-09` head `f9a62d71ca70619ad4ad5c5e3218c895281b5a4e`, **not main**. Scoped isolated branch `chatgpt/apd-shared-zip-entry-buffer-20261115-2000`.
- Inspected `AGENTS.md`, the completed-work ledger, known issues, live open PRs, recent source PR #193/#194 and their exact CI/build receipts, and the current importer and archive-test fixtures.
- Separate from completed PR #193 (which reuses one buffer *only among ignored ZIP members*), the manifest-listed `stage()` routine still allocates a fresh 16 KiB buffer for every valid/invalid listed member. Reuse a single 16 KiB read buffer across staged and ignored entries, with no change to ZIP reading order, checksum/size validation, first-wins matching, archive acceptance, history, scoring, saves, application identities or signed releases.
- Two new real-ZIP regressions alternate 48,321-byte ignored entries with three genuine logs, prove exact-byte successful imports, and prove a corrupted final log leaves pre-existing history untouched and commits none of the earlier staged files.
- This first checkpoint **only claims source change plus submitted verification**. The complete exact-source `:core:test`, desktop Java classes and Android debug/release/instrumentation Java compilation must pass before any authoritative integration. Do not mistake CI compilation for native acceptance.
- No APK assembly, original-signing, native-device acceptance, install-over or gated publication is claimed; C473/P473 are existing unaccepted candidates whose source excludes this proposed optimization. Preserve their frozen artifacts and all prior releases.
- Final CI IDs, exact test counts, branch integration and blockers will be added only after independent verification.

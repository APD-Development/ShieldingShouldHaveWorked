# APD diagnostic export worker-start failure, 2026-11-15 (Eastern)

## Independent work claim, 06:08 EST

- Repo: APD-Development/ShieldingShouldHaveWorked; authoritative development branch codex/apd-phone-fixes-2026-10-09; starting verified head `9aa9a162d92f0f3d9c1846538e8e1ca0f885bd1b`; isolated branch `chatgpt/apd-diagnostic-worker-start-20261115-0600`.
- Read AGENTS.md, latest November 15 handoff, open PRs and current DiagnosticReport/SupportDiagnosticsTest. Earlier merged #159 handles file-picker provider exceptions; this scope is distinct: when diagnostic picker hands over a valid stream but the background export Thread cannot start (notably during C467-like low-memory conditions), the callback currently has no try/catch, does not close the stream, and never gives the player a definite failure notice.
- Bounded source/test-only correction: detect failure to launch diagnostic export worker, release the accepted stream, report failure once. Preserve successful async export, existing reports, diagnostic content, game logic, scoring, saves, signing and application IDs.
- Do not infer or claim a fix for the underlying C467 OutOfMemoryError. Native acceptance, original-signer and new APK release remain independent blocked work. No parallel open PR claims this method. Full core suite and Android/desktop compilation required before guarded integration.
- This is a work claim, not test/build/release evidence. Start head 9aa9a162d92f0f3d9c1846538e8e1ca0f885bd1b; test results, final commits and blockers must be appended once verified.

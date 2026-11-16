# APD history identity-only frame-buffer reuse, November 16, 2026

## Isolated source claim (approximately 1:03 AM EST)

- Verified authoritative `codex/apd-phone-fixes-2026-10-09` starting SHA: `45b1c75351a550bc625c1ca2718e706eb01b927b`; isolated branch `chatgpt/apd-identity-frame-reuse-20261116-0100`.
- Inspected AGENTS.md, completed-work ledger, known issues, handoffs, open PR claims, C477/P477 receipts and previous PR #203. No open competing `RunLogReader` modification in examined PRs.
- New distinct allocation source: identity-only streaming still made a new exact-length full-frame byte array for each equal-sized gameplay event after PR #203 removed the separate tail copy. Use the previous buffer only when the next frame is exactly the same length. Ordinary readers still receive fresh storage; retained RUN_START payloads are copied before reuse; string values are decoded independently.
- Regression `identityOnlyStreamingReusesEqualLengthFramesAndKeepsDifferentLengthsCorrect` writes 768 real events of changing bytes and two alternating sizes in runs of eight, checking ordinary streaming/buffered parity and identity-only status, clocks, counts, seal and byte boundaries.
- This is the initial claim, not verification or publication. Preserve all history/save/scoring/gameplay/edition/signing IDs and C477/P477/older builds and their release gates.
- Next: run new exact-source full core tests and desktop/Android Java compilation; recheck current primary head and CI outcome before any guarded integration. No native acceptance, install-over validation, signed build or release is claimed here.

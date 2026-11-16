# APD streaming checksum allocation reduction, November 16, 2026

## Initial isolated work claim (approximately 05:26 EST)

- Verified current authoritative branch `codex/apd-phone-fixes-2026-10-09` head: `2e0fe9e3b22340dbbd3a6f80f8c087cf21663562`; isolated work branch `chatgpt/apd-stream-crc-reuse-20261116-0526`. Never use main as development head.
- Inspected AGENTS.md, completed ledger, known-issues and handoff records, exact latest C479/P479 continuation, live branch, open PR claims, CI workflow and the current `RunLogReader.java` and `RunLogStreamingReadTest.java` source. Open PRs are separate historical playback/hero fade/other work. No open PR in that list claims this streaming CRC/checksum allocation package.
- Evidence: streaming history reading still constructs a fresh `CRC32` object and four-byte checksum array for each framed log entry, including thousands of events processed only for import identity validation. Scope: reuse one per-reader CRC32 instance reset for each frame, and parse four checksum bytes directly while maintaining the exact CRC32 comparison, truncated-checksum refusal, existing result statuses and offsets. Preserve all ordinary/resync semantics.
- Add a real-frame regression for hundreds of differently sized and differently populated frames, corrupted checksum after many valid frames, and incomplete 1–4 byte checksums, asserting parity with the existing buffered reader. Require full core, desktop and Android Java compile CI on the isolated exact tree. Do not merge before terminal success and live-head/claim recheck.
- Preserve C479/P479 and older signed candidates/releases, native and install-over gates; no APK, signing or release claimed. No gameplay, balance, scores, saves, history schema, version codes, app IDs or edition changes. This is not evidence of the cause of C467 OutOfMemoryError.
- Completion and verification receipts to be appended only after observed results.

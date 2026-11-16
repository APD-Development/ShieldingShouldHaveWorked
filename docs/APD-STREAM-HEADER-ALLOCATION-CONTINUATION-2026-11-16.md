# APD streamed history header read allocation, November 16, 2026

## Independent work claim, approximately 07:13 EST

- Read owner repository instructions (AGENTS.md), completed ledger, known issues, relevant historical handoffs and newest frame-result completion. Checked live GitHub head, recent merged PRs, current open PRs, and separate frozen C480/P480 native claim (#211/#212). Do not touch active native claims, the frozen C481/P481 candidates or pending personal-playback/UI branches.
- Verified authoritative branch `codex/apd-phone-fixes-2026-10-09` at `fe72aec5072ca894ee2ba7e0614eed1d3bf12e7c`; `main` is not authoritative. Independent source branch `chatgpt/apd-stream-header-efficient-20261116-0700`.
- Evidence: `RunLogReader.readHeader(InputStream)` preallocates `MAX_HEADER_BODY + 11` (4,107 bytes) for every streamed log even when typical headers are much smaller, and then produces copied prefix/return arrays. It also reads the entire body via one-byte reads. A bounded exact-length header buffer and chunked read avoids the fixed 4 KiB allocation and repeated one-byte calls without changing header format, accepted header size, CRC, malformed/truncated/error reporting or resync/buffered behavior.
- Add explicit parity regressions against the byte-array parser for small and near-limit valid headers, truncated prefix/body/checksum, and unsupported newer-container handling. Verify full core test suite and required Java compilation on the isolated exact source before any integration; preserve source failures honestly.
- No gameplay mechanics, balance, scoring, RNG, save/log format, history semantics, signer, app identity, edition boundary, prior candidates, or release/publication gates may change. Do not call a source-only CI result native acceptance or a C467 OOM root-cause determination.
- Start commit: `fe72aec5072ca894ee2ba7e0614eed1d3bf12e7c`. Initial state: no changes tested, APK assembled, or release published in this run.

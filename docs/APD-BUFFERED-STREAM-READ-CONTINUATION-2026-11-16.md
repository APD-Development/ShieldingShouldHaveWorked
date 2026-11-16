# APD buffered history-stream read, November 16, 2026

## Isolated claim, 09:38 EST

- Verified authoritative development branch `codex/apd-phone-fixes-2026-10-09` at `050a69e6178da974cfff15634f7ec42b9511a59a` (independently read back on November 16, 09:38 EST). Working only on isolated `chatgpt/apd-buffered-log-stream-20261116-0935`. `main` is not development authority.
- Inspected AGENTS.md, completed ledger, known-issues guide, handoffs, newest stream-reader test/code, open PRs and release/build receipts. C483/P483 original-signed build 43873192490 is terminal success but still lacks native/visual/install-over gates; held C480/P480 native verifier PR #211 remains independently owned. Existing unrelated playback, UI and release PRs are left intact.
- Evidence: non-recovery `RunLogReader.readStreaming(File,...)` reads each frame's sync, type, varint length and four CRC bytes with repeated single-byte calls on a raw `FileInputStream`. With thousands of small frames these produce avoidable native I/O calls. Preserve all byte offsets/CRC/seal/status/identity semantics by wrapping only this normal forward-only stream in an 8 KiB `BufferedInputStream`. Recovery's separately owned byte-array semantics remain untouched.
- Add a real-file boundary regression spanning multiple 8192-byte refills, with unknown frames, ordinary events and seal; compare both valid and truncated/corrupted streams byte-for-byte against the unchanged buffered parser, including import identity mode. Require complete 3,528+ core JUnit tests and desktop, Android debug/release/instrumentation Java compilation on isolated exact-source CI before integrating.
- No gameplay design, balance, scoring, RNG, saves/history formats, signing, application ID, versionCode 920, edition flags, previous signed APKs, publication gates, active native work claims or automation schedules will be changed. Do not infer C467 OOM root cause or native Android acceptance from this source-only change.

## Results and handoff

Pending actual implementation, CI results, merge and any separate build gates.
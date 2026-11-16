# APD bounded unknown-frame reading continuation, November 16, 2026

## Isolated source-work claim

- Started Nov 16, 2026, 8:03:28 AM EST (2026-11-16T13:03:28.707Z) from verified authoritative `codex/apd-phone-fixes-2026-10-09` head `473e5ca0d177077d942f7954d12aae7083b9e46d`; independent branch `chatgpt/apd-unknown-frame-bounded-20261116-0800`. `main` is not the development authority.
- Read AGENTS.md, work-completed ledger, known issues, current handoff and recent C479–C482 streaming/history continuation; inspected active open PRs and current CI/build receipts. C480/P480 native verifier PR #211 is independently held with failing runs. C482/P482 original-signed candidate artifacts from build 43866117653 are **not native-qualified or published**. Do not modify any existing native/build work claim, signed candidate or publication workflow.
- Evidenced remaining allocation: `RunLogReader.readFrame(InputStream,...)` creates a payload array up to the allowed 16 MiB even for unknown frame types (neither string, event, clock nor seal), despite `applyFrame` only counting these frames. The byte-array resync parser also copies payloads of such unknown frames before immediately discarding them. Intended bounded optimization: preserve header/length, CRC and truncation validation and byte offsets, but process unknown streaming payloads with a per-reader small scratch buffer, and avoid unnecessary copies for unknown buffered frames. Keep all known-frame/event behavior identical.
- Add regression coverage for large valid unknown frames, damaged CRC, truncated payload and neighboring real events; assert streamed/buffered status, skipped counts, event identity/bytes, sealed counts and `validBytes` parity. Run the full core suite and desktop/Android Java compile checks on an isolated exact-source GitHub Actions workflow; **no tests yet run** at claim time.
- Do not change game mechanics, balance, scoring, RNG, history/save formats, application ID/versionCode, original signer, edition flags, prior candidate/release bytes or schedules. This is a memory-risk reduction, not proof of the cause of C467 OutOfMemoryError. No APK assembled, native accepted, or published by this claim.

## Continuation receipts

Pending actual implementation, verified CI results, guarded merge, and build gate decisions.

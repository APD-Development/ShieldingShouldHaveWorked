# APD diagnostic event line integrity continuation, November 14, 2026

Recorded Eastern: Saturday, November 14, 2026, 11:01 PM EST (initial source preparation).

## Baseline and scope

The authoritative development head was `aed60ff89c3a604953b5b87c09483154d1310655` on `codex/apd-phone-fixes-2026-10-09`. Separate open playback/native PRs were preserved. The previous memory pressure correction in PR #148 and handoff #149 was merged; do not duplicate it. The C471/P471 pair from earlier run 43667638390 is on different source and remains frozen.

The existing `SupportDiagnostics.event` only removed LF from detail, while leaving CR and both newline types in the event kind. Thus a throwable message or malformed diagnostic detail could create extra physical lines in a C467 diagnostic record, misleading interpretation of chronology. This is an evidence integrity correction, **not** the OOM root-cause fix.

## Narrow source correction

Normalize CR and LF to spaces in event kind and detail after retaining existing length limits. Ordinary events, memory history, saved games, gameplay, balance, identities and prior builds are unchanged. A real-filesystem regression requires one physical event and one exception error line after messages containing both line breaks. All changes are isolated on `chatgpt/apd-diagnostic-line-integrity-20261114-2300`. A new branch-scoped workflow requires full core tests, desktop compilation and Android debug, release and instrumentation Java compilation.

## Verification and publication boundary

This initial handoff records intended work only; exact new-source CI has not yet run. Do not call it a passing test or a new APK. Integrate only after exact-source tests pass and a live expected-head non-force update. Preserve the previous signed candidates and all native/signing/install-over/publication gates.

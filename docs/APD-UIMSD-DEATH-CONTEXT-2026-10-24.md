# UIMSD recorded death-context first slice — 2026-10-24T22:07:18Z

## Scope
This bounded core-only slice wraps the existing Deaths dataset and public RunTurnBrowser observations. It does not add or rewrite telemetry, raw history, saves, scoring, gameplay, turns or RNG.

## Implemented contract
- An adjustable 1–1000 turn window ends exactly on the recorded death turn and clamps at turn 1.
- Only public observations already recorded for the exact case-sensitive run are listed.
- The recorded death is explicit, including its cause when present.
- Revived, final, non-final and unknown post-death states remain distinct. Contradictory fields fail closed to unknown.
- Missing death turns produce no reconstructed timeline.
- Output states that observations are incomplete lower-bound evidence and do not prove survival, causation or absence of other events.

## Regression gate
Five new DeathContextTest methods require exact boundaries/chronology, exclusion of other runs, revival-versus-final wording, missing older data, bounded inputs, start clamping and qualified non-final output. Full-suite target: 2069 methods /401 suites /0 failures /0 errors /0 skips.

## Core verification
Exact source `dea4a490158be46e24a2bd116c2f158de55de391` passed [run40137272801](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/40137272801): 2,069 methods /401 suites /0 failures /0 errors /0 skips. Dedicated retained XML confirms all five DeathContextTest methods. Artifact `12578106262` is 976,925 bytes with digest `sha256:59b48feefb055637ec0207ebcbd58601920f98deb7f31cae94157c8b63633f03`.

## Acceptance boundary
The core model is accepted. It has no native UIMSD surface, API34/display verification or APK request. TEST312 remains the latest emulator-verified private pair; TEST262 remains the latest actual owner publication. Physical-device Back latency remains unmeasured. The executable APK request stayed byte-identical at `42af2cfe9a72655b5f1e7a2dbdf4777219b809e0`.

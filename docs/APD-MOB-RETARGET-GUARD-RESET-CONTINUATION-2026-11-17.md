# APD mob unreachable-retarget recursion guard: continuation (November 17, 2026)

## Work claim and source baseline

Claim created Tuesday, November 17, 2026 at 08:01 EST. Isolated branch `chatgpt/apd-mob-retarget-guard-reset-20261117-0800` starts from authoritative `codex/apd-phone-fixes-2026-10-09` head `50151463acabd8b5468af34fed7b3d67affe4508`. The complete R7+C485 baseline is ancestor `c6fa033167789ef9f2fc6f31bcff39c824dfb4db`. This worker first reviewed `AGENTS.md`, `docs/APD-486-R7-INTEGRATION-VERIFIED-2026-11-16.md`, open PRs, current source and preceding Tengu AI handoff.

**Scope claimed:** `Mob.Hunting.handleUnreachableTarget` exception-safe recursion guard reset, its existing JVM regression class, isolated source-only CI and this continuation. Tengu already received a corresponding `try/finally` safety reset in PR #247, so that existing package is not touched. Existing open PRs #229/#230 concern Extended diagnostics; other open PRs concern build/native/personal playback/older drafts. None should be merged or modified by this worker.

## Evidenced failure and proposed narrowly scoped repair

`Mob.Hunting.handleUnreachableTarget` currently sets `recursing = true`, invokes the recursive `act(...)`, then resets `recursing = false` only on normal return. An exception from an overridden `act` leaves the same persistent hunting state with `recursing = true`, silently disabling future one-step retries on that state if the exception is caught or recovered by a caller. Tengu's already-merged corresponding handler uses a `finally` reset. Change only this guard reset, preserving existing movement, target visibility, targeting, damage, ability, RNG and turn logic. Add a headless regression that first raises a controlled exception from a hunting retry and then confirms a second retry on the **same state** is permitted.

## Verification, integration and release

Pending actual source edit, regression, CI and expected-head guarded integration. Any failure and the next safe work will be recorded here. This branch does not build, sign or publish APKs; qualified release, native acceptance, install-over and original-signer gates remain with the dedicated APK worker.

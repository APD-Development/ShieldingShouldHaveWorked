# TEST170 — Derived cache raw-source identity correction

Started: 2026-10-14T20:38:38Z

## Confirmed defect
`DerivedStore.fingerprints()` recorded only source kind, filename and byte length. A valid raw log replaced by different valid content of the same length retained the same cache identity, allowing stale derived statistics to be returned.

## Bounded correction
- Fingerprint every run and legacy source with its actual bytes: length plus SHA-256.
- Use the bytes actually read for the recorded length.
- Make an unreadable source fingerprint deliberately non-reusable so a failed read cannot establish a trusted cache.
- Bump derivation version from 7 to 8 for one automatic rebuild.
- Do not rewrite or migrate raw logs, player saves, scores, signing identity or application ID.

## Retained regression
`aSameLengthRawReplacementInvalidatesTheCache` creates and caches one valid run log, replaces it with a different valid equal-length log under the same name, and requires stale-cache rebuild, replacement facts, then normal cache reuse.

## Verification state
No real execution workspace was available in this invocation, so no local command/test result is claimed. Exact-source TEST170 requests 1,586 core tests, debug assembly, complete history against immediate previous compiled TEST169, 26 API34 emulator checks and 34 screenshots. The candidate is unverified until that run completes. TEST169 remains the verified fallback; TEST123 remains the owner-delivered/signature-compared fallback.

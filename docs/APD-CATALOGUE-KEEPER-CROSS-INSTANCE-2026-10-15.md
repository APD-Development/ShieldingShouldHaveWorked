# Catalogue keeper cross-instance correction — October 15, 2026

## Reproduced cause
`CatalogueKeeper.offer` was synchronized only on one keeper instance. Two keepers targeting the same file used the same `.tmp` name and compared a candidate only with their own in-memory `held` value. A stale keeper created before another instance accepted sequence 2 therefore accepted and persisted signed sequence 1, undoing the newer catalogue and any revocations it carried.

## Bounded correction
The compare-and-replace section is serialized across keeper instances. Inside that section each offer re-reads and verifies the persisted catalogue, adopts it when newer than local state, and rejects equal or older sequences before writing. Atomic replacement, signature/purpose/time checks, expired-cache revocation protection and caller-visible results are retained.

This is local provider-neutral storage reliability only. It does not activate a backend, network access, accounts, content downloads or online play. It changes no raw run history, save, score, custom rule, gameplay, RNG, signing key or application ID.

## Retained regressions and gate
Two new `ContentCatalogueTest` methods cover the original stale replay and deterministic cross-instance replacement serialization. The original source accepts the stale sequence-1 replay after sequence 2; corrected source must retain sequence 2. A second keeper cannot enter the shared replacement section while the first is paused there, and the final file reloads as sequence 2 without a leftover temp file.

TEST215 requests 1,652 core tests, debug assembly, complete immediate-previous TEST214 history, 28 API34 methods and 38 valid 320x640 screenshots. Candidate pending until actual workflow evidence completes. TEST214 remains signing-compatible staged evidence; published TEST206 remains the owner-delivered fallback. No physical-phone claim.

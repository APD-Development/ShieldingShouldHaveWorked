# UIMSD opening crash: November 6, 2026

The owner’s physical-phone build365 diagnostic confirms Java heap exhaustion while reopening UIMSD. The heap limit was268435456 bytes (256MiB); the crash snapshot used267270832 bytes. The first failed allocation was32 bytes,4.323 seconds after the statistics-open event. The render/input iterator is the allocation failure site; the report does not establish that input processing allocated the retained history. System RAM readings after restarting do not rule out the earlier per-process Java heap exhaustion.

## Reproduced defects and corrections

- Foreground opening could leave a pending title preload alongside a new history load. Foreground claiming now atomically takes ready state, retires pending preparation, and validates exact source freshness.
- Previous viewer catalogs and query/back results could remain strongly referenced. Exiting, opening a replacement and reloading now release derived tables while preserving preferences and raw recorded files.
- A cache-read `Throwable` handler could swallow memory exhaustion and attempt raw parsing. Fatal memory errors now propagate to the model’s failure state rather than starting a fallback parse; normal invalid/corrupt caches retain their existing rebuild behavior.
- Optional preparation errors could reach the uncaught-error handler. Background memory/runtime failures now become bounded failure diagnostics without deleting recorded history.
- A cancelled decoder can briefly retain its current working batch. Catalog allocation and Home preparation are serialized on background workers until that batch unwinds. Recorder flush/freshness remains outside that guard. An initially broader guard failed the existing stale-open regression; narrowing the guard restored the unchanged assertions.

## Actual verification and limits

The source through8ab4a7915 passed2835 tests in446 suites, zero failures/errors/skips. A separate JVM explicitly limited to256MiB passed92 targeted tests in12 suites. The initial memory ownership/error-handler baselines and raw XML archives are listed in `APD-UIMSD-OOM-2026-11-06.json` and the shared handoff. The source-only Android/desktop compilation and final candidate’s native checks are recorded separately as their actual receipts arrive.

Candidate368 is being built from7ef0800df (the full SHA is in the candidate request). Developer producer42262060915, paired Alpha42262060908 and six-profile display42262060874. This is today’s third and final pair; later test-only corrections must reuse these APKs. No new release or physical-phone success is claimed here. The previous delivered365 has this confirmed phone crash and must not be described as containing the fix.

The diagnostic has no heap dump or allocation trace, so the exact share of memory consumed by each retained structure remains unproven. The regression fixes address demonstrated ownership and failure-handling defects; they are not a guarantee that every possible history fits a256MiB heap. No save, statistic, source history, signing key or application identity was reset or changed.

# TEST171 — RunIndex cross-instance save serialization

Started: 2026-10-14T20:58:00Z

## Confirmed defect
`RunIndex.save()` used an instance monitor, but separate RunIndex objects targeting the same root all opened `index.apdi.tmp`. Concurrent saves could truncate one another's temp stream and race rename/delete fallback operations. The index is derived and rebuildable, but the race could cause avoidable rebuilds or save failures.

## Bounded correction
- Serialize only the small file-writing and atomic-replacement section across RunIndex instances.
- Preserve index format 2, entry ordering, CRC, load/rebuild behavior and the existing temp filename.
- Do not change raw run logs, player saves, application identity, signing, gameplay or scoring.

## Retained regression
The controlled two-instance test pauses the first writer after opening the temp file, starts a second instance, verifies it cannot open the shared temp concurrently, releases the first writer, and requires both saves plus a complete final second index that loads without rebuild.

## Verification state
No real local execution workspace was available. Exact-source TEST171 requests 1,587 core tests, debug assembly, complete history against immediate previous compiled TEST170, 26 API34 emulator tests and 34 screenshots. Candidate remains unverified until actual evidence passes.

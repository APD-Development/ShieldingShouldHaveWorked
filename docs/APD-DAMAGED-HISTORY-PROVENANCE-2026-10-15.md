# Damaged-history count provenance correction — 2026-10-15

## Scope

A damaged telemetry log can retain a valid event prefix. Before this correction, RunFactsBuilder and RowBuilders treated counts derived from that surviving prefix as exact whenever the log had no explicit GAP event and did not start partial. TRUNCATED, CORRUPT, and SEAL_MISMATCH histories could therefore expose lower bounds as exact values.

## Correction

- Count provenance is incomplete whenever ReadStatus is not clean.
- Clean UNSEALED histories remain exact.
- Valid SEALED histories remain exact even when unrelated bytes trail the seal; the reader intentionally keeps status SEALED and reports the extra damaged bytes.
- RunFactsBuilder derivation version advances 8 to 9.
- RowBuilders cache version advances 12 to 13.
- Raw logs, saves, score values, rules, gameplay, RNG, slider values, and cache formats are unchanged. Only disposable derived caches are invalidated and rebuilt.

## Retained regression coverage

Six methods cover clean unsealed history, truncated tail, corrupt tail, seal-count mismatch, valid seal with trailing junk, and both cache-version contracts. Each damaged case asserts both the run-level ITEMS_ACQUIRED fact and the item-row item.acquired cell use incomplete provenance while preserving the surviving count.

The handoff records the earlier four-method reproduction as three failures before the fix. This integrated six-method suite and the full 1,645-core/debug/API34/history/visual gate are pending on candidate source creation; no result is claimed in advance.

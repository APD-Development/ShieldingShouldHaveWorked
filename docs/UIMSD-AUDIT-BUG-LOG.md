# UIMSD audit — confirmed controlled regressions

Full audit incomplete. These are controlled tests, not claims of observed phone failures.

1. ArchiveImporter ignored ZIP entries bypass existing512MiB limit. Medium resource exhaustion. Exact-source baseline accepts513MiB controlled entry; bounded drain rejects. Five tests cover boundaries/progress/history preservation. Archive-only CI run38143393376 passed, not delivered.
2. RunIndex negative decoded unsigned count accepts empty derived index with valid CRC and hides existing logs. Low display reliability. One-line count guard forces rebuilding; source logs untouched. Baseline failed; regression and empty-index compatibility tests pass.
3. RunLogReader unsigned length overflow accepted as empty valid unknown frame. Low malformed-input integrity. Nonnegative size guard rejects; valid future frames remain supported. Baseline failed, fixed tests pass.
4. RunLogReader resync huge sparse string ID exhausts32MiB heap on tiny valid-checksum input. Medium reader-API robustness. Bound impossible recovery IDs by file length before allocating holes. Exact-source OutOfMemoryError reproduced; fixed small-heap test passed. No production resync caller found; not claimed as an observed phone crash.
5. RunLogWriter create exists/open race truncates competing log. High integrity if race occurs. Controlled baseline changes existing5-byte evidence into28-byte header. Atomic file claim refuses race without altering evidence. Regression passes. No observed player data loss claimed.
6. RunLogWriter close timeout falsely reports completion while worker blocked; repeated close also incorrectly true. Medium API correctness. Require completion latch/repeated worker completion; asynchronous writer still finishes after release. Controlled baseline failed; fixed test preserves accepted event/seal.

Tests:12 new permanent tests. Combined130 local archive/reader/index/writer tests passed, zero failures. Full accumulated-source CI requested; physical-phone checks pending. None of these fixes is in delivered TEST45.

Unresolved: manifest linear find yields quadratic import lookup; cross-instance derived-index temp-file races; huge-file whole-memory reader cost; archive compression wrapper lifetime. These remain investigation items, not claimed normal-run defects. Existing functionality, save formats, gameplay and player statistics preserved. SPD inherited-code investigation remains read-only.

Update 2026-10-14T20:58:00Z: the earlier manifest linear lookup item had already been resolved by the retained first-match map and TEST48-era coverage. TEST171 now addresses the distinct cross-instance `RunIndex` fixed-temp save race with a controlled retained regression; verification is pending. Huge-file whole-memory reader cost and archive compression-wrapper lifetime remain investigation items, not claimed normal-run defects.

Update 2026-10-15T02:40:14.718Z: a distinct current-source review confirmed that separate `SavedViews` objects shared one fixed `saved_views.txt.tmp` without cross-instance serialization. TEST190 serializes only that atomic replacement section and adds a controlled two-writer regression. Exact-source full verification is pending; this is not an observed phone data-loss claim. The huge-file reader cost and archive compression-wrapper lifetime remain investigation items.

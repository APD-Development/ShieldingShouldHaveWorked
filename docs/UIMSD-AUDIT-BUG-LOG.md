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

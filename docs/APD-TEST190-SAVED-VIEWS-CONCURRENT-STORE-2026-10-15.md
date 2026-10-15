# APD TEST190 — SavedViews cross-instance store serialization

Candidate prepared: 2026-10-15T02:40:14.718Z
Branch: `codex/apd-phone-fixes-2026-10-09`
Immediate previous compiled APK: TEST189, source `ea1062079c6a43aadaf80dbf0197f3fcff4769cc`, run `38669986563`

## Reproduced source defect

`SavedViews.store` used the fixed sibling path `saved_views.txt.tmp`, but its write and rename were not serialized across separate `SavedViews` objects. Two UI/store instances targeting the same history root could therefore open and truncate the same temporary file concurrently, then race their rename. Instance synchronization would not protect this shared on-disk resource.

## Bounded correction

A private static lock now covers only the small directory-create, temp-write and rename replacement section. A package-private test hook pauses the first writer after opening the temp file. The retained two-thread regression proves a second independent instance cannot open the shared temp path until the first finishes, then requires both calls to complete, the final second view to reload cleanly, and no temp file to remain.

The partial-load refusal remains outside mutation and unchanged. Saved-view format, query encoding, raw run logs, player saves, permanent statistics, gameplay, scoring, application ID and signing are unchanged.

## Requested verification

TEST190 must run the complete 1,604-core suite, debug assembly, immediate-previous compiled TEST189 history comparison, all 27 API34 methods and all 35 expected 320×640 screenshots. This candidate is not accepted, delivered or physical-phone verified until exact-source evidence passes. TEST189 remains the verified hosted/emulator fallback and TEST123 remains the last owner-delivered/signature-compared APK.

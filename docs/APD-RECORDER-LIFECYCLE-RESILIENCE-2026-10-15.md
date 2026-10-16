# Recorder lifecycle payload resilience — October 15, 2026

## Reproduced boundary

`RunRecorder` restored logical-run, segment and ruleset bookkeeping from the canonical raw event stream inside one shared exception boundary. A malformed `RUN_START` or `RULESET_CHANGE` payload therefore stopped the scan and hid every later readable lifecycle event from the resumed recorder.

The raw history remained present, but later saves or run-end bookkeeping could be based on stale in-memory ruleset state.

## Bounded correction

- Decode each relevant lifecycle payload in its own failure boundary.
- Continue to later canonical events after one unreadable payload.
- Apply the same isolation while finding an earlier segment header.
- Preserve the original raw bytes, timeline, saves, scores, gameplay, rules, turns and RNG.

One retained core method supplies a malformed `RUN_START` followed by a readable `RULESET_CHANGE`. It must fail on exact TEST231 application source `5134ef6438f9562a37b3f8db727d5d004fa93527` and pass after the correction.

## Acceptance boundary

Pending evidence must show the one intended TEST231 failure, 1,694 corrected core methods, debug assembly, complete immediate-previous TEST231 history, 33 API34 emulator methods and 42 valid screenshots. No automatic release, tester distribution or physical-phone claim is authorized by this batch.

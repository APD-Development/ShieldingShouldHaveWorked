# Active-play time idle-gap correction — October 15, 2026

## Reproduced boundary

`PlayTracker` says app-background and long-pause gaps are not play time, but its accumulator used `min(gap, 30 seconds)`. Every gap longer than 30 seconds therefore added exactly 30 seconds to `time.active` when the next hero turn began.

The retained regression moves the private monotonic baseline 31 seconds into the past, records the next hero turn and requires the active-time total to remain zero. Exact verified TEST232 source `96772420c7ccdcbc294e8eae33d68b27d71abfaa` must fail that method with 30,000 milliseconds.

## Bounded correction

- Count positive gaps up to and including 30 seconds.
- Count gaps over 30 seconds as idle and add nothing.
- Preserve the monotonic clock, running-total format and existing short-gap behavior.
- Do not change game time, turns, saves, raw history, scoring, gameplay, RNG, any Custom Dungeon Rule, or any already-recorded event.

## Acceptance boundary

The dedicated workflow must reproduce exactly one intended failure on TEST232 and pass all 1,695 current core methods after correction. The normal TEST233 gate must then pass debug assembly, complete immediate-previous produced TEST232 history, 33 API34 emulator methods and 42 valid screenshots. No automatic publication, tester distribution or physical-phone claim is authorized by this batch.

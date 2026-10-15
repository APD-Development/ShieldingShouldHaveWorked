# UIMSD stale background-load guard — October 15, 2026

## Confirmed failure

`UimsdModel.load()` built a catalogue on a daemon thread and installed it unconditionally when that thread finished. If newer state was installed before the older worker reached the synchronized handoff, the stale worker replaced the newer catalogue, view, query, page and back stack.

The retained regression holds the model monitor, starts the old disk load, installs a distinct newer catalogue while the worker is forced to wait at the handoff, then releases the monitor. Before the correction the old worker deterministically wins last and the assertion fails. The test does not depend on timing or sleeps.

## Bounded correction

- Each load receives a monotonic in-memory generation.
- A newer load or explicit `setCatalog` retires earlier workers.
- A retired worker installs neither a successful catalogue nor a failure and sends no stale listener notification.
- The existing one-thread screen model, raw histories, derived cache formats, saves, scores, all 18 Custom Dungeon Rules, gameplay, turns and RNG are unchanged.

## Verification state

Candidate TEST216 requests the full 1,653-core/debug/history/API34/visual gate against immediate previous compiled TEST215. Until that run passes, this correction is pending and TEST215 remains the latest fully verified source; staged TEST214 and published TEST206 remain unchanged.

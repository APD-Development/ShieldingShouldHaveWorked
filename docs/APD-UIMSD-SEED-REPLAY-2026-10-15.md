# UIMSD stale seed-replay handoff guard — October 15, 2026

## Confirmed failure

`startSeedFloors` captured a catalogue and ran the expensive seed rebuild on a worker, but its completion installed datasets and navigated unconditionally. If a newer catalogue became READY first, the old replay could still replace its view/query/back-stack state and emit stale listener notifications.

The retained regression uses a blocking `SeedFloors.Provider`: it starts the old replay, installs a distinct newer catalogue while the provider is blocked, releases the replay, then joins its worker. Before the correction the model opens the stale seed view and notifies again. No sleep or probabilistic ordering is used.

## Bounded correction

- Each replay receives an in-memory generation.
- A newer replay, catalogue load, explicit catalogue installation or provider replacement retires older replay workers.
- Retired workers install neither data nor failure notices and do not navigate or notify.
- Seed generation, canonical/recorded provenance, raw history, derived formats, saves, scores, all 18 rules, gameplay, turns and RNG are unchanged.

## Verification state

Candidate TEST217 requests the full 1,654-core/debug/history/API34/visual gate against immediate previous compiled TEST216. Until actual evidence passes, TEST216 remains the latest verified source; TEST214 remains staged and TEST206 published.

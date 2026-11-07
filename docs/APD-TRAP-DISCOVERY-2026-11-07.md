# Continue moving after trap discovery

## Implementation status

Implemented and locally tested; Android runtime/build verification is requested and remains pending until its actual evidence is checked. Owner APK delivery hold stays active. One internal, clearly labelled debug application is authorized for the requested Android checks; it must not be published, uploaded as an application artifact, or sent to the owner. Its signing key/application ID stay unchanged. Other approved work remains in progress.

## Actual APD behavior traced

- Hero.handle stores destination commands in curAction (Move STEP/TRAVEL, LvlTransition, pickup/chest/interaction actions). Char.path holds the cached sequence of cells. Held directional input is stored by CellSelector.
- Hero.getCloser performs a legal movement step, pays the original movement delay and then calls passive search(false). Foresight also searches on initial attachment and on standing observation. Intentional searches remain separate.
- Hero.search previously combined visible secret traps and doors into smthFound, showed the notice/sound, then unconditionally called interrupt. Hero.interrupt clears curAction, stores an ordinary unfinished destination in lastAction, resets held input and invalidates trap consent. Thus discovering a nearby trap could stop travel without stepping on it.
- Level.discover changes SECRET_TRAP to TRAP and updates its terrain flags, then reveals the trap. Hidden traps are passable; discovered traps are avoided by normal walking. getCloser rechecks the next cached cell and uses Dungeon.findPath to recalculate a normal legal route. It retains the existing detour-length rejection. Adjacent explicitly selected hazard destinations, fog-trap protection, flight/Amok and trap confirmation retain their existing special rules.
- Actual stepping/occupancy is independent: Level.pressCell interrupts the hero on the triggered cell and calls Trap.trigger. Time-freeze/bubble delayed presses retain their existing path. No trigger, trap effect or hazard rule is patched.

## New option

APD Settings → Gameplay → **Continue when traps are revealed**, Off by default. Its help explains On and Off, normal hazard avoidance and legitimate interruptions. The boolean is stored through the existing flushed preferences system under apd_continue_trap_discovery. It applies immediately to current runs and is not a Custom Dungeon Rule or save-file field.

Search tracks visible non-trap discoveries separately. With On, passive trap-only discovery during movement omits just its own interrupt call. The original destination, queue and held arrows remain. Explicit Search, waiting, doors/other secret discoveries and other interrupt callers keep their behavior. A scoped transient movement-search flag also covers actual walking toward items/chests, and is restored in finally. No cancelled action is restored. Two protected visual hooks retain the original pre-reveal tile/post-reveal particle ordering, enabling headless tests to suppress only renderer effects while exercising actual map discovery. Android checks use the original visuals.

## Local verification

Full local suite: **2,909 tests / 447 suites**, zero failures/errors/skips; `/workspace/apd-trap-discovery-final-full.log`, raw XML `/workspace/apd-trap-discovery-final-local-junit.zip`.22 new cases cover both preference states, actual movement/discovery without stepping, cache/input retention and normal turn cost, trap on route with a safe alternative, trap blocking the only route, real obstacles, actual visible/hidden trap triggering and damage, confirmation, simultaneous door discovery, manual Search/waiting, item/chest/transition travel, enemy/damage/manual cancellation, strict stair safety, fog targets, exploration/step commands, flight, preference-file reload, current-run toggling, Hero save/reload and unchanged detection RNG across40 seeds.

The initial test compile used a nonexistent confirmation cancel convenience method; corrected to the existing choose(false). The first runtime suite then exposed one invalid boundary-writing test fixture; corrected to construct the bulk padded map before building flags. Assertions and production terrain rules were preserved. Logs `/workspace/apd-trap-discovery-targeted.log`, `...-targeted-2.log` and the successful `...-targeted-3.log` are retained. Three tooling tests verify rejection of incomplete, failed/skipped, duplicate and wrong-case instrumentation evidence.

## Android plan and boundaries

The dedicated private workflow compiles the complete core/desktop/Android source, builds a single INTERNAL-TRAP-20261107 debug application plus its test harness, verifies package/certificate/checksum, and runs three API34 native cases: actual On tap, an actual force-stop/new-process saved-On check with real Off tap, and real gameplay discovery/rerouting followed by actual Ooze trap activation. Only receipts, raw results, logs and screenshots are retained; no application APK is published or delivered. Results remain pending until all stages actually pass. These are disposable emulator fixtures, not natural generated-room/seed claims or physical-phone testing.

Intentional limitations: this option does not force movement through a blocked route or change strict stair/wall-search hazard checks. Normal adjacent explicit trap destinations and the separate confirmation preference keep their established behavior. The whole pre-existing mechanics/translation audit and physical-phone acceptance remain open.

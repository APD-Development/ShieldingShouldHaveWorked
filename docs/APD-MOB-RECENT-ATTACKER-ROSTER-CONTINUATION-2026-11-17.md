# APD recent-attacker roster membership: source verification, November 17, 2026 (EST)

## Scope and source identity

This APD-only morning coding execution began around 2:00 AM EST, November 17, 2026. It read the existing complete C466R7+C485 integration handoff, AGENTS.md, primary GitHub branch head, Mob/Actor code, the prior roster-membership and nearest-target continuations, open PR/work claims and source CI before modifying anything.

- Authoritative initial primary head: `d1abeaa7331a121e6bcceef5ba87b7a0802efdc7`.
- Mandatory full C466R7+C485 ancestor: `c6fa033167789ef9f2fc6f31bcff39c824dfb4db`.
- Isolated branch: `chatgpt/apd-mob-recent-attacker-roster-20261117-0200`; [PR #242](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/242).
- Production source commit: `9dde2080e0dea58a718241296e28da6a1df56002`.
- Regression test commit: `856d0927e2a3621b368de133376eac5ea66f43c1`.
- CI workflow/exact source-tested head: `b56e20c1e55442e23430afb60b1537eb5ad47847`.

## Evidenced allocation and bounded fix

The already merged roster-membership work introduced `Actor.containsChar(Char)` to avoid cloning every registered character when checking one target. A separate call remained in `Mob.Hunting.handleRecentAttackers`: it invoked `Actor.chars().contains(ch)` for each recorded recent attacker, allocating a full defensive roster copy for a single membership question.

This package changes only that call to `Actor.containsChar(ch)`, preserving the existing null, living/active, registered, hostile-alignment, visibility, invisibility, charm, attackability and distance/retarget conditions. The existing synchronized membership implementation maintains the previous roster semantics without copying the collection. No new AI behavior, gameplay mechanic, RNG call, scoring, save/history, edition flag, original signer, app ID or asset change is introduced. The R7 weapon/shield, FullPackSwap, ThrowAim, Greater Metamorphosis, adaptive search and playback integration is inherited unchanged.

Three headless tests verify that a registered visible attacker can be selected, a removed but still living attacker cannot be selected, and a registered attacker not in sight cannot be selected. The isolated workflow also checks for the direct lookup in the exact source method, validates both required ancestry commits and runs all core/desktop/Android Java compilation tasks.

## CI evidence

[Exact GitHub Actions run 44035814309](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/44035814309) executed on `b56e20c1...`, completed **successfully** at approximately 2:06 AM EST on November 17, 2026. Its job log reported `BUILD SUCCESSFUL in 1m 35s` and exactly **3,554 core JUnit tests across 543 suites, zero failures, errors or skips**, including all three new tests. Desktop classes plus Android debug, release and Android instrumentation **Java compilation** succeeded. JUnit XML artifact: `14307496926`, `apd-mob-recent-attacker-roster-junit` (417,068 bytes as reported by GitHub). These are source tests and Java compilation, not executed Android native device tests.

The verified feature diff from the starting primary contained only one production-line substitution, a new JUnit fixture and its own isolated CI workflow. This continuation file was added after the successful CI, and contains no source or test modification.

## Coordination and next boundary

Check the live primary and PR heads again before merging. Integrate only with an expected-primary-head non-force two-parent merge (or equivalent guarded GitHub merge), preserving the successful source tree and all unrelated concurrent work. If primary advanced, do not overwrite it; re-evaluate and rerun CI if required. Do not modify independent report ZIP/action journal PRs #229/#230, trap-room work, other active branches, research tasks or other games.

**No APK was assembled, signed, installed, qualified or published by this worker.** The dedicated 4 PM APD APK worker owns original-signer/build/Android-native/visual/install-over-save verification and Developer/Alpha release delivery. No phone FPS improvement or other measured performance gain is claimed from this source-only allocation removal.

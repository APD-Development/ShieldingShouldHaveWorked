# APD sleeping-mob hostile detection order, November 17, 2026

## Morning/afternoon isolated work claim (10:01 EST)

Started from fresh authoritative `codex/apd-phone-fixes-2026-10-09` at `ba1f1c319897efd183b9aa33fafbf63907a3bd9e`, retaining complete verified C466R7+C485 merge `c6fa033167789ef9f2fc6f31bcff39c824dfb4db`. Read `AGENTS.md`, `docs/APD-486-R7-INTEGRATION-VERIFIED-2026-11-16.md`, completed ledger, the final vault waypoint handoff, live branch and open PRs before work. This claim is limited to `Mob.Sleeping`'s selection among already visible eligible hostiles and a new headless regression fixture. It does not claim the extended-report, personal playback, native release or APK work, and does not touch their branches.

## Source-evidenced bug and bounded correction

The existing Sleeping state comment explicitly says it can wake from the **least stealthy hostile** in view. Detection chances are greater for easier-to-notice opponents. However the old loop initialized `highestChance` to positive infinity and replaced it whenever `bestChance < highestChance`, consistently choosing the **lowest** finite detection chance. With otherwise eligible hostiles at 0.8 and 0.2 detection probabilities, the sleeping enemy used 0.2, irrespective of which target was actually easiest to notice.

The fix selects the **highest finite** existing `detectionChance` among the same eligible hostiles. It preserves FOV, invisibility, alignment, existing Silent Steps/flying exclusions, the existing positive-infinity exclusion sentinel, and first-seen tie behavior. A zero-probability eligible hostile still yields a candidate and therefore the existing random detection roll; the number of rolls when all eligible chances are zero is not reduced. No new probabilities, numeric constants, turns, combat, loot, abilities, scoring, save formats, R7 playback, edition flags, signing settings or release semantics are introduced.

The pure candidate-selection step was extracted into `Sleeping.mostDetectableHostile(Iterable<Char>)` so headless tests can directly verify its real production branch. Four tests prove encounter-order independence when chances differ; tie preservation; valid zero-chance eligibility; and exclusion of unseen, invisible, aligned, neutral and infinity-suppressed candidates. The caller retains its original single `Random.Float()` roll and recomputation of `detectionChance` for the chosen candidate.

## Actual source commits and verification gate

Isolated branch: `chatgpt/apd-sleeping-hostile-detection-20261117-1001`. Source commit `9cfd7e229fa6ef3493a9f58d0795592caa7070bf`, regression commit `bfb50ac04e2b4561232afc402a0f0f66b43bdb51`, focused full-suite CI workflow commit `f16e1e007971a68e27194a2ca9e4ea1022eeb1ae`. Dedicated `apd-sleeping-detection-ci-20261117.yml` must pass the full core suite with at least 3,580 tests / zero failures, errors or skips plus desktop and Android debug, release and instrumentation Java compilation on an isolated PR. Existing overlapping Mob regression checks must remain green.

No JUnit/Android CI, merged code, native-device verification or signed APK is claimed until separately observed. Before any merge, re-read authoritative base head, open PRs and workflow results, validate tested production/test bytes, and use a GitHub expected-head merge (never force). If any gate fails, leave the branch/PR isolated with the actual failure recorded. The dedicated APK worker retains release and packaging responsibility.

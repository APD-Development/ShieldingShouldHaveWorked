# TEST157 player ability and delayed derived-damage candidate

- Execution start: 2026-10-14T14:30:52Z
- Candidate prepared: 2026-10-14T14:39:49.724Z
- Base head: `badbcae625a124a416f02bc9a948ae3f1fa439f1`
- Scope: Elemental Blast, Elemental Strike, and Blast Wave collision damage only.
- Elemental Blast now routes both direct damage branches through Player Damage, attributes Fireblast burning, and captures the player before delayed knockback collision.
- Elemental Strike now routes six secondary direct-damage branches through Player Damage, captures both delayed knockbacks, and gives the conjured explosion the activating player origin.
- Wand of Blast Wave captures its user for delayed collision damage. The original public `throwChar` entry point remains environmental/unattributed for callers that do not provide a creator.
- Original rolls, sources, mitigation, status durations, geometry, turns, and neutral 100% RNG are retained. No scoring, save-format, inventory, signing, application-ID, release, or slider-enablement change.
- Four retained regressions cover all 20 percentages, neutral source/RNG identity, complete Elemental Blast/Strike caller guards, delayed immutable capture, and legacy environmental exclusion.
- The Vault elemental harmful-buff reaction remains deferred: `Buff.attachTo` invokes the target callback before current Burning attribution is assigned, so correcting mixed creator ownership safely requires a separate pre-attach provenance design.
- This connector-only execution has no local Gradle workspace. Compilation, 1555 core tests, debug build/history, 26 API34 checks, and 34 screenshots are pending exact-source CI. Three Damage sliders remain disabled.


## TEST157 CI result and TEST158 fixture correction — 2026-10-14T14:43:42.114Z
- TEST157 source `514ccd2e9090af9881f6cb30b84c156179506e19`, run `38582589811`: history passed; production/tests compiled; 1555 core tests executed, 1554 passed and one new source guard failed because it counted four `throwCharAttributed(` occurrences (two wand call sites, the legacy delegate, and the method declaration) while expecting three.
- Core artifact `11903652610`, ZIP SHA-256 `569e9c6728d9dfdd424bcd30ca35cbb1a1874773960cc417ca9959e156ba3ef9`. Debug APK/history and API34 jobs were skipped; no APK/screenshots were produced.
- TEST158 changes only that expected source count from 3 to 4. Production code and behavioral assertions are unchanged. Exact-source 1555-core/debug/history/26-API34/34-PNG verification remains pending; TEST156 remains the verified source.

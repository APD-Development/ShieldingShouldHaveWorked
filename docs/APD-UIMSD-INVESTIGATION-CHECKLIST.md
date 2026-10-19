# UIMSD connected investigation implementation checklist
Updated 2026-10-19 after TEST301 navigation milestone verification. Owner's five-capability request is the acceptance scope. Supporting tools do not complete a whole capability.

## Audited baseline
Accepted supporting-tool baseline: TEST294 /84b465af7e12fce897d318fcf385529a50ad48e2,1,948 core +48 API34 methods/64 captures. First connected equipment-window milestone is now accepted in TEST300 /d41631b3e5b43bf832036426a9e7c7a0694d07ed:1,959 build-time core methods (later1,967 after independent loot regressions),50 unique Android cases verified across the49-pass full run and corrected one-case exact-APK gesture rerun,73 full-suite captures. Exact cold-restart/player Settings and five display profiles also passed. No broader capability is complete on backend/tests/documentation alone.
Existing: binary offline recorder with rollback-aware canonical history, public/internal disclosure, typed provenance, queryable events/equipment/facts, all 18 Custom Rules, grouping, row links, saved views, exports, viewport restoration and accurate death/abandonment outcomes.
Partial: topic/simple-question search and fixed multilingual aliases; basic RunTurnBrowser public event text; research A/B query pins and descriptive/inferential comparisons.
Missing: general sequence/window queries, extensible interpreted terminology, comprehensive contributing-record navigation, complete death context/resource snapshots, named persisted experiments, longitudinal comparable-condition analysis.
Verified TEST301 navigation milestone: per-place Back checkpoints include horizontal/vertical position alongside query/page, including same-dataset evidence and scene recreation. Source5c11b1fc346efd8f398ab2cf2872e03b15b29dc0 passed1,973 core and51 API34 methods/76 captures; strengthened exact-APK replay39335009747 passed all original and frozen-pane assertions. This closes the tracked cross-table Back position gap. Do not claim all evidence-navigation requirements are complete. Preserve Columns deferred-rebuild fix, sorting viewport fix, raw logs, saved views and existing outcomes.

## Acceptance checklist
| Capability | Status | Acceptance criteria |
| --- | --- | --- |
| 1 Search and questions | Partial | Offline conjunctions across events/equipment/class/subclass/floor/version/difficulty/challenges/all 18 controls; explicit sequence and time-window boundaries; extensible translated aliases; visible exact interpretation; unsupported parts fail closed; persist and reopen exact question. |
| 2 Evidence navigation | Partial | Each calculation opens exact contributors, source run/floor/fight/turn; includes numerator/denominator provenance and unknowns; Back restores filters/sorts/scroll; stale/disclosure changes reject old results; exports retain evidence identities. |
| 3 Timeline and death investigation | Partial foundation | Canonical chronological events; health/shield/source/status/equipment/action/resource observations where recorded; adjustable final-turn window; revival versus final death explicit; gaps/unknowns visible; no invented older events or guaranteed-survival claims. |
| 4 Personal experiments | Partial foundation | Named saved question plus A/B definitions and metric; automatically reevaluate qualifying future runs after reload; sample/exclusion/condition differences and uncertainty; exact contributor navigation; separately versioned, nondestructive persistence. |
| 5 Personal progress | Pending | Time-ordered cohorts with recorded dates; comparable conditions; version/rule/balance strata explicit; uncertainty and missing observations; drill-down/export; never confuse mixed settings with improvement. |

## First end-to-end milestone: equipment-following damage window
- [x] Select a recorded equipment occurrence, retain current equipment filters/scope.
- [x] Choose N=1/5/10/20/50 turns; show interpretation: same run, strictly after equip turn through start+N inclusive, hero-target damage including shield absorption.
- [x] Read existing real recording through DatasetStore and QueryEngine; no gameplay/RNG changes.
- [x] Show observed sum with exact contributors; missing/partial recording never becomes an asserted zero or a full-history total.
- [x] Inspect anchor equipment and each matching damage record; exact query round-trip and Back navigation.
- [x] Native Android menu (emulator), repeated selections, lifecycle/disclosure safety and screenshot evidence.
- [x] Writer -> save/close -> reload -> query -> result regression, boundary/overflow/other-run/unknown/gap tests.
- [x] Measure execution cost and APK/history growth; record test environment and limits.
- [x] In-game Changes, paired uniquely numbered developer/player APKs; verify player cheats gate.
This milestone is a useful subset of capabilities 1 and 2, not completion of the five.

## Evidence-navigation position milestone (verified in TEST301)
- [x] Store transient viewport checkpoints per unique navigation place, including late checkpoints after asynchronous navigation.
- [x] Route ordinary links, groups, research evidence and seed replay through the shared navigation stack.
- [x] Restore exact parent query/sort/page, anchor column and clamped vertical offset; new links start at their own origin.
- [x] Checkpoint before scene destruction so rotation can restore the surviving model.
- [x] Inspect full core and actual Android pointer/scene-recreation acceptance results:1,973 core /51 API34,0 failures/errors/skips; strengthened1-case exact-APK repeat and3 stable captures also passed.
- [x] Verify paired TEST301 APKs, all328 previous history files, player feature gates and in-game Changes; exact cold restart/death gates and all5 display profiles passed.
See [TEST301 report](APD-UIMSD-NAVIGATION-2026-10-19.md) for immutable source, checksums, retained original display failures and exact evidence. No raw/save/query schema changes or historical migration. This closes a navigation gap within capability 2, not the entire capability.

## Later connected milestones
- [ ] Extensible terminology + question persistence and general correlated windows.
- [ ] Timeline/death evidence and necessary forward-only recorded snapshots.
- [ ] Named experiment registry using shared QueryCodec/PopulationStudy and future-run reevaluation.
- [ ] Comparable-condition progress using the same experiment/evidence model.
- [ ] Benchmark comparison from primary project sources before any specific uniqueness claim.
- [ ] Storage/latency profiling on representative histories and Android, including recording overhead.

## Release acceptance
No user save/history migration or deletion. No inferred outcomes rewritten. All prior tests retained. Report completed, partial and pending separately with immutable source/build/evidence links. Android emulator evidence must not be called physical-device verification.

## Benchmark scope checked against primary project sources
These are design benchmarks, not evidence of uniqueness. No claim that UIMSD is the first or only system is supported.
| Benchmark | Confirmed reference quality | UIMSD implication |
| --- | --- | --- |
| [Cogmind Dataminer source](https://github.com/leiavoia/cogmind-dataminer) and [Cogmind scoresheets](https://www.gridsagegames.com/cogmind/scores/) | Scoresheet visualization and local-file analysis; client-side operation | Offline analysis and deep run records already exist elsewhere; preserve public recorded evidence and version boundaries. |
| [Sequell query syntax](https://github.com/crawl/sequell/blob/master/docs/listgame.md) | Filtered/grouped queries and explicit field syntax | Extend the real query system, show interpretation, reject unsupported conditions. |
| [Spire Codex source/API](https://github.com/ptrlrd/spire-codex/blob/main/README.md) | Filtered runs, version metadata, decisions and sample/uncertainty metrics | Inspect decisions and condition differences; associations are not causal survival guarantees. |
| [Dota Plus Death Summary](https://www.dota2.com/plus/?l=english) | Incoming damage/disabling-effect chronology and source breakdown | Death investigation needs chronological source/context observations; a basic turn text browser is insufficient. |
| [Factorio statistics GUI design](https://www.factorio.com/blog/post/fff-337) | Readable graphs, highlighting and shared search | Connected phone navigation and clear labels matter alongside calculation depth. |
Checked 2026-10-19. This is a scoped comparison of documented capabilities, not an exhaustive competitive survey.

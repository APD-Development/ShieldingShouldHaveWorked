# UIMSD connected investigation implementation checklist
Updated 2026-10-19. Owner's five-capability request is the acceptance scope. Supporting tools do not complete a whole capability.

## Audited baseline
Working source: 84b465af7e12fce897d318fcf385529a50ad48e2 (TEST294 candidate); preceding accepted APK: TEST292 / e9f028a35285d919258088cd5360d954ceb429f4.
TEST294 core: 1,948 tests passed; Android acceptance pending. No feature is accepted on backend/tests/documentation alone.
Existing: binary offline recorder with rollback-aware canonical history, public/internal disclosure, typed provenance, queryable events/equipment/facts, all 18 Custom Rules, grouping, row links, saved views, exports, viewport restoration and accurate death/abandonment outcomes.
Partial: topic/simple-question search and fixed multilingual aliases; basic RunTurnBrowser public event text; research A/B query pins and descriptive/inferential comparisons.
Missing: general sequence/window queries, extensible interpreted terminology, comprehensive contributing-record navigation, complete death context/resource snapshots, named persisted experiments, longitudinal comparable-condition analysis.
Preserve Columns deferred-rebuild fix, sorting viewport fix, raw logs, saved views and existing outcomes.

## Acceptance checklist
| Capability | Status | Acceptance criteria |
| --- | --- | --- |
| 1 Search and questions | Partial | Offline conjunctions across events/equipment/class/subclass/floor/version/difficulty/challenges/all 18 controls; explicit sequence and time-window boundaries; extensible translated aliases; visible exact interpretation; unsupported parts fail closed; persist and reopen exact question. |
| 2 Evidence navigation | Partial | Each calculation opens exact contributors, source run/floor/fight/turn; includes numerator/denominator provenance and unknowns; Back restores filters/sorts/scroll; stale/disclosure changes reject old results; exports retain evidence identities. |
| 3 Timeline and death investigation | Partial foundation | Canonical chronological events; health/shield/source/status/equipment/action/resource observations where recorded; adjustable final-turn window; revival versus final death explicit; gaps/unknowns visible; no invented older events or guaranteed-survival claims. |
| 4 Personal experiments | Partial foundation | Named saved question plus A/B definitions and metric; automatically reevaluate qualifying future runs after reload; sample/exclusion/condition differences and uncertainty; exact contributor navigation; separately versioned, nondestructive persistence. |
| 5 Personal progress | Pending | Time-ordered cohorts with recorded dates; comparable conditions; version/rule/balance strata explicit; uncertainty and missing observations; drill-down/export; never confuse mixed settings with improvement. |

## First end-to-end milestone: equipment-following damage window
- [ ] Select a recorded equipment occurrence, retain current equipment filters/scope.
- [ ] Choose N=1/5/10/20/50 turns; show interpretation: same run, strictly after equip turn through start+N inclusive, hero-target damage including shield absorption.
- [ ] Read existing real recording through DatasetStore and QueryEngine; no gameplay/RNG changes.
- [ ] Show observed sum with exact contributors; missing/partial recording never becomes an asserted zero or a full-history total.
- [ ] Inspect anchor equipment and each matching damage record; exact query round-trip and Back navigation.
- [ ] Native phone menu, repeated selections, lifecycle/disclosure safety and screenshot evidence.
- [ ] Writer -> save/close -> reload -> query -> result regression, boundary/overflow/other-run/unknown/gap tests.
- [ ] Measure execution cost and APK/history growth; record test environment and limits.
- [ ] In-game Changes, paired uniquely numbered developer/player APKs; verify player cheats gate.
This milestone is a useful subset of capabilities 1 and 2, not completion of the five.

## Later connected milestones
- [ ] Extensible terminology + question persistence and general correlated windows.
- [ ] Timeline/death evidence and necessary forward-only recorded snapshots.
- [ ] Named experiment registry using shared QueryCodec/PopulationStudy and future-run reevaluation.
- [ ] Comparable-condition progress using the same experiment/evidence model.
- [ ] Benchmark comparison from primary project sources before any specific uniqueness claim.
- [ ] Storage/latency profiling on representative histories and Android, including recording overhead.

## Release acceptance
No user save/history migration or deletion. No inferred outcomes rewritten. All prior tests retained. Report completed, partial and pending separately with immutable source/build/evidence links. Android emulator evidence must not be called physical-device verification.

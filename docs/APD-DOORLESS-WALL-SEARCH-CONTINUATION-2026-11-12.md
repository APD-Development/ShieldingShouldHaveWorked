# APD doorless-floor wall-search chooser continuation, November 12, 2026

## Scope and starting verification

Owner screenshot and report on main-dungeon floor 15: Wall Search remained available while standing inside a room. This work addresses the *saved-wall-search chooser option*, not the independently allowed ordinary manual Search. The existing `DoorSearchEligibility.supportsDoors(Level)` rules identify main-dungeon floors 1, 10, 15, 20, 25 and 26 as having no generated hidden doors. `WallSearchSession.resume` already declines execution on unsupported floors, but `SearchControlContext.choices` still showed `Resume wall search` solely because any saved wall-search memory existed. Do not infer from this scoped UI fix that the user's complete phone behavior is accepted.

Execution started November 12, 2026, 4:44 PM EST. Read current `AGENTS.md`, live open PRs, `SearchControlContext`, `WallSearchSession`, `DoorSearchEligibility`, the actual `WndWallSearchResume` dispatcher, the full `SearchControlContextTest`, and earlier chooser safety handoff. Initial authoritative `codex/apd-phone-fixes-2026-10-09` SHA was `d63435d3ffc10f005e0a9593c7e6032d0485aa9c`, not `main`. Independently verified publication of existing [APD462](https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/tag/apd-462) at 2026-11-12 20:20:21 UTC with separate Developer/Alpha APKs and matching source archive. This work does not modify those binaries or APD461/462 native, publication or signing claims.

## Isolated code and regression

Isolated branch: `chatgpt/apd-wall-search-boss-floor-20261112-1644`, forked from exact authoritative SHA above. Production commit `c72a3370e94f0f9609a8ea930a37c42e556327a7` alters only `SearchControlContext.choices`: an existing saved session is insufficient to advertise Wall Search on a floor with no supported hidden-door generation. Preserve ordinary Search, saved-position Show/Forget and Resume Movement. The chooser still allocates just one caller-owned result array. It changes no search turn, hunger, search radius, detection, actual secret generation, markers, saves, history, score, signing, package or assets.

Test commit `dae4e5517429f7aff4e769edea6287066b120864` adds `doorlessFloorsOmitWallResumeButKeepNormalSearchAndSavedPosition` to the existing `SearchControlContextTest`. It checks all six doorless main-dungeon depths, both ordinary and movement-first presentation, immutable saved memory and no action/turn, then asserts eligible floor 3 still exposes the previously available explicit wall resume.

Source CI workflow commit `a26d5ed1ef82db78553caa41ff942b11dbe3b983` adds a branch-isolated push gate for static scope assertions, complete `:core:test`, desktop compilation and Android debug/release/instrumentation Java compilation. [Run 43313468715](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43313468715) started at 4:46 PM EST and was still *in progress* at initial documentation. No passing tests or built APK are claimed at this checkpoint. No local tests ran: the local container cannot resolve github.com to clone this private repository.

[Draft PR #46](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/46) targets authoritative development and is **not merged** at this checkpoint. Do not merge until exact passing CI, regression coverage and live heads are confirmed, and no competing claim overlaps this change. No native acceptance or signed build was started. Existing APD462 remains the last verified published pair.

## Safe continuation

First recheck exact CI and JUnit totals, the current isolated and authoritative heads, active claims and PR status. If CI fails, inspect logs and fix the actual issue without weakening tests. If it passes, review code diff and perform expected-head, non-force integration only when safe; preserve independent release work. Record final merge SHA and source-only boundary here, or accurately leave this PR pending. A source-only test/compile pass is **not** a new native-qualified or deliverable APK; the signed publication gates remain mandatory.

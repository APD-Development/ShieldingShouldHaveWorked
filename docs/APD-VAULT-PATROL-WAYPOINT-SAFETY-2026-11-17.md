# APD vault stealth-patrol waypoint index safety, November 17, 2026

## Isolated work claim, 09:10 EST

Morning/afternoon coding worker. Started from verified authoritative `codex/apd-phone-fixes-2026-10-09` at `4bda78ba7de02a350be9d51d61386f61b1517851`, descendant of the complete C466R7+C485 integration `c6fa033167789ef9f2fc6f31bcff39c824dfb4db`. Read `AGENTS.md`, `docs/APD-486-R7-INTEGRATION-VERIFIED-2026-11-16.md`, the previous Mob retarget continuation, current open PRs, live branch and source/CI before claiming this scope.

**Claimed, distinct scope:** `Mob.setupStealthGameplayWanderPositions`, loading `wander_positions`/`wander_pos_idx` in `Mob.restoreFromBundle`, and narrowly focused headless vault-patrol waypoint regressions. This does not modify R7 mechanics, patrol routes on valid data, probabilities, numeric balance, scoring v6, history format, edition flags, signing, or APK artifacts. The long-standing open Extended-report PRs #229/#230, older playback/native and APK worker claims remain untouched.

## Source-evidenced failure

Vault stealth patrols persist both a waypoint array and `wander_pos_idx`. On restore, the index is assigned without checking it against the restored array's actual length. The wandering state then directly indexes `wanderPositions[wanderPosIdx]`. Missing/empty waypoint arrays or a negative/too-large index from damaged or old saved state can therefore crash the next stealth patrol waypoint lookup. Generated patrol positions from the current room classes use valid nonempty lists; their behavior must not change.

## Planned narrow repair and release boundary

Normalize empty/null waypoint lists to the existing stationary-patrol fallback, and reset only out-of-range indices to the first available waypoint, both at setup and restore. Retain all valid indices/routes byte-for-byte in behavior. Add headless tests for valid waypoints, negative/high indices, empty input, and a save/restore index injected outside the restored waypoint bounds. Verify whole-core JUnit and desktop/Android Java compilation by isolated CI before any non-force expected-head merge; record exact artifacts/results. Source-only, never claim native, signed APK or release acceptance.

## Source, test and CI evidence, 09:16 EST

- Work-claim commit `80216bd5e4fd44b3521e806eb7067e4311c1b74d`, source repair `be782e9750ab4ca7044a2b86a0dfae3a16e6aeb0`, six test regressions `abfa2ab6f0ea09b66c5a147941da704e86d5a0b5`, tested branch SHA with source, tests and workflow `c2eeb0d38c9a2e82927ffa44db5e2ece9a2256be`.
- Isolated draft [PR #250](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/250) has exactly four changed paths: `Mob.java` (+10/-4), one new headless JUnit fixture (+86), one source/Android compilation CI workflow, and this handoff. No approved R7/C485, playback, damage, scoring, save serialization format, APK or signing change.
- The [GitHub Actions verification run 44100630398](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/44100630398) completed **success** on PR test-merge SHA `ea3ef1e2f56098c47e483d595aa87da5c3ec0e63` joining the unchanged authoritative base `4bda78ba7de02a350be9d51d61386f61b1517851` and source branch `c2eeb0d38c9a2e82927ffa44db5e2ece9a2256be`. JUnit **3,576 core tests in 547 suites; zero failures/errors/skips**; all six newly named patrol tests were checked in XML. Desktop classes plus Android debug, release and instrumentation Java compilation succeeded. The Gradle log reports `BUILD SUCCESSFUL in 2m 19s`.
- JUnit XML evidence artifact `14335099429`, `apd-vault-patrol-waypoint-junit`, 420,531 bytes, GitHub digest `sha256:ef787bfce37d9a867e110711c58a986d49d8d4e65dacccc2cd22ec0f1e086a45`.
- Pre-merge branch/base compare: four scoped paths, base unchanged at check. Other retained Mob regressions were independently triggered; no other work claim was modified.

## Integration and continuation boundary

Only after all relevant CI is terminal success and a fresh live-base/head/overlap check may PR #250 be marked ready and merged with GitHub's exact expected PR head and no force. This post-test documentation update must be compared against the tested branch to ensure that no production, test or workflow bytes changed. Record final integration as a separate merge receipt when known.

No APK was assembled or published by this worker. Source compilation and JVM JUnit are not native-device execution, actual install-over/save migration, physical-phone acceptance, original signing or qualified release publication. Leave those gates with the dedicated APK worker.
## Final guarded merge receipt, 09:18 EST

- All four pre-existing intersecting regression jobs were terminal **success**: Mob guard 44100630189, attackable-target ordering 44100630224, unreachable-target visibility 44100630184, and vault stealth angle seam 44100630342. Exact newly added full-suite/job 44100630398 was already success, 3,576 JUnit tests / 547 suites clean.
- Before merge, fresh authoritative live base was still `4bda78ba7de02a350be9d51d61386f61b1517851`. PR #250 was non-draft, mergeable and at exact expected head `d865c0ff41306297251ddef413f7c9cfbb379f93`. Comparison from CI-tested source branch `c2eeb0d3` to PR final head showed exactly **one markdown-only edit**; no production/test/workflow changes.
- GitHub accepted the **normal non-force merge with expected_head_sha** of [PR #250](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/250), creating authoritative production merge `64068999ebafb7df893f129c8c1969f9e164aac6`. A fresh readback confirmed the authoritative development branch at exactly this SHA, PR closed/merged, and compare from PR head to merge showed **zero differing files**.
- Independently fetched merged and CI-tested blobs match exactly: `Mob.java` SHA `161c0238c932db8daf6de9db9919f85b02f8678e`, `MobVaultPatrolWaypointSafetyTest.java` SHA `30417367ab6f41a5334e7d955c6642bd2f2baba7`. This is source/JVM/Android-Java compile acceptance, **not** an assembled/signed APK or native-device verification.
- Next worker must start from fresh live authoritative head, read current claims before choosing a distinct bug. Preserve R7+C485, all v6 scoring/history/saves, editions/signing and original APKs. Leave pending Extended report, personal playback, native install-over and APK release gates to their dedicated owners. This docs-only receipt branch changes no production, test, CI, gameplay, release, schedule or other project.

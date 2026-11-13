# APD derived run-history index direct-log boundary, November 12, 2026 (EST)

## Execution and coordination

This actual regular ChatGPT execution began around **7:35 PM EST** on November 12, 2026, using the owner's connected GitHub repository only. Inspected the live authoritative development ref, current AGENTS.md and history/recovery/archive handoffs, active PRs, recent commits, CI/source receipts, APK release metadata and parallel claims. The authoritative starting ref was **`3d1a0b34b1542044b6f6639c31a2173be31bcdff`**, not main. The existing personal animation/playback PRs #38, #47, #48, wall-search PR #6 and their separate native/release ownership remain out of scope. Published APD463/APD464 and all held APK candidates remain untouched.

Work package: isolated branch **`chatgpt/apd-run-index-direct-log-guard-20261112`**, [draft PR #53](https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/53), targeting the established development branch. No conflicting shared branch writes or new app-number claims.

## Evidenced code gap and bounded fix

The previously merged `StatsStore.recoverAll` symlink guard stops destructive recovery from following linked individual log entries, but `RunIndex.refresh` and `RunIndex.rebuild` still used `File.isFile()`, which follows those same links. An external `*.apdlog` file linked into `runs/`, or an alias of another local log, could therefore appear as its own derived history entry. This is a source-demonstrated boundary flaw, not an allegation that owner history was actually exposed or changed.

- Production commit **`2ef14043e0c77aa7b16f09a8a42e7c614bf79c04`** adds the same canonical direct-child and exact canonical-name rule to derived index explicit refresh and directory rebuild. Linked individual logs are refused or ignored; genuine damaged direct-child logs still produce UNREADABLE summaries. Configured symlinked storage roots are allowed. No log bytes, index encoding, gameplay, scoring, RNG, save layout, signer, app ID or edition identity change.
- Test commit **`8b51ac10ab1c68f99cc6347f4f09eb8733bee050`** adds `RunIndexLinkBoundaryTest` with two real-filesystem cases covering external same-named symlink targets, local aliases, normal refresh, corrupt direct-child visibility, missing-index rebuild and valid symlinked storage roots. External source bytes and links must remain unchanged.
- CI workflow commit **`d4fad453ac2216a5ede4716daffc3fb71106dc01`** adds an isolated no-APK full-core and desktop/Android Java source gate, requiring at least 3,328 tests with no failures/errors/skips and existing recovery/refresh compatibility cases.

## Verification status at 7:38 PM EST

GitHub Actions [run 43338572640](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43338572640) started at 7:37:51 PM EST on exact source/test/workflow commit `d4fad453ac2216a5ede4716daffc3fb71106dc01` and was **in progress** at this timestamp. Do not claim that tests passed before a terminal run and JUnit evidence are read. No local Gradle execution, Android native emulator/device acceptance, original-signed APK assembly, or gated game release has occurred in this work.

## Next safe action

Read the terminal source CI result and exact logs, fix evidenced failures if necessary without relaxing tests, read fresh primary and PR heads and conflict state, and use only a guarded non-force integration after verification. Record actual integration, verification and blockers in this continuation. Never overwrite independently claimed APD native/releases or confuse source CI with a qualified new APK. This execution does not imply continuous development between invocations.

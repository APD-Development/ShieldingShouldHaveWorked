# APD465 frozen Developer APK final acceptance and blocked publication
November 13, 2026, America/New_York (EST)

## Owner and exact source
Owner requires the approved personal playback matrix, without changing gameplay mechanics or Alpha/community behavior. The personal implementation and all source tests were frozen at commit `c2fb3af855afa358d76d687bcf180ee031281f55` on `chatgpt/apd-personal-playback-completion-20261113`. This recovery branch was forked from that exact source and adds only a recovery workflow and this documentation. The authoritative shared development branch, old releases, saves, scores, IDs, and signing materials were not changed.

## Source, build and native acceptance
Original frozen pipeline: [run 43414835573](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43414835573). Source job 131850006290 passed **3,330 core tests in 477 suites; zero failures, errors or skips**, and Android debug/release/instrumentation plus desktop Java compilation, finishing 2026-11-13 11:12:37 UTC (6:12 AM EST). Candidate job 131851296048 passed: **original signing certificate**, package `com.charltonchris29.adhdpixeldungeon.indev`, unchanged `versionCode=920`, matched frozen Developer, Alpha Testers and instrumentation APKs. Frozen candidate GitHub artifact `14031247763`, https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43414835573/artifacts/14031247763 .

Seven of eight exact native cases passed on the original Developer/Alpha frozen APKs, including both new personal playback profile tests. The one original Alpha WallGeometry case failed due emulator/ADB device disconnect during native screenshot transfer, not a reported assertion failure. Original publish job correctly skipped instead of accepting incomplete evidence.

Recovery workflow [run 43416465219](https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43416465219), head `a8f0af1a5f0c0871777e6063e968f6632b84e9ed`, reinstalled **exact original frozen APKs** and re-ran only Alpha-Testers/WallGeometryDeviceTest. Its native job 131855507808 passed. Recovery publisher job 131856551288 independently downloaded the candidate and all original/recovered native receipts and printed `Exact frozen source, original signed APKs and eight complete native visual test receipts verified`, including exact source/build, screenshot hashes and each APK/source archive SHA-256. No APK was rebuilt, and no native acceptance was bypassed.

Original candidate receipt:
- Developer `APD-Developer-465.apk`: 39,211,120 bytes, SHA-256 `bb6fa53913579a03f0353af1bd7138dbe31047fc9361caffff9aace8aa6e5e52`
- Alpha (validation only): SHA-256 `f555a75c73ff701ed58f0b00e8f1da56d0b16cc2ef60c6661cc7e9e55f219d0c`
- Source ZIP: SHA-256 `b705b14bba35ed67daee160a907bd65618f3f8da6e38b40d218aa69c3ff58b9e`
- Original signing certificate SHA-256 required by successful candidate gate: `067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e`.

## Publication blocker, not an APK/native blocker
At 2026-11-13 11:24:33 UTC (**6:24:33 AM EST**), after **all eight native receipts were verified**, `gh release create apd-465` failed with **HTTP 403 `Resource not accessible by integration`** on `POST /repos/APD-Development/ShieldingShouldHaveWorked/releases`, despite the workflow requesting `permissions: {contents: write, actions: read}`. The GitHub Release did **not** publish. `apd-465` was verified absent via the GitHub releases tag API (HTTP 404). Do not report a published GitHub release or success for the recovery workflow. This is a GitHub token/permissions publication blocker; do not bypass the original signing/native/save checks, overwrite old releases, or rebuild a validated binary solely for a publication repair.

## Direct owner delivery and limitations
The original frozen GitHub Actions artifact was downloaded through the connected GitHub tool into the conversation, the Developer APK was extracted verbatim, and its complete bytes and SHA-256 were verified against the original receipt. The owner was given a direct conversation download of the exact **APD-Developer-465.apk**; this is **not** a GitHub Release. GitHub artifact link above remains available while retention permits. No Alpha edition was distributed. Native suite proves the scoped structural routing and fixtures, not a comprehensive physical RedMagic subjective check of every one of the owner's UI/animation categories; that remains an owner-device acceptance item.

## Next safe action
Recover GitHub Release creation permissions for workflows or use an authorized GitHub release-publishing identity, then publish the **same frozen, hash-verified Developer APK and source** after checking `apd-465` remains unused and repeating release asset readback SHA-256. Avoid another candidate build or native test unless evidence expires, fails reconciliation, or code changes. No recurring APD task schedule was changed and no independent background work is claimed.

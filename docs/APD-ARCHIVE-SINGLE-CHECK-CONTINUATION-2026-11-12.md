# APD archive single-check record guard, November 12, 2026 (Eastern)

## Isolated work claim (Thursday, November 12, 2026, 10:35 PM EST)

Owner-directed continuation in the originating regular ChatGPT project thread. Verified authoritative branch `codex/apd-phone-fixes-2026-10-09` at `e70424ad41d1ccc36991497e703a2334be877214` after inspecting AGENTS.md, current archive handoffs, recent commits, open PRs, CI/workflows, previous build/release receipts and the parser/test source. `main` is not the baseline. This work is isolated on `chatgpt/apd-archive-single-check-20261112-2235`; no conflicting open manifest/checksum work package was identified. Independently active playback, native, release, wall-search and GPL work remains untouched.

### Source-evidenced defect and exact scope

The v1 writer produces one terminal `check\t<sha256>` line. `ArchiveManifest.parse` currently locates the **last** `check\t` and accepts earlier `check` records in the checksum-protected body as unknown forward records. A malicious or malformed archive can append a new correct final checksum to an already checksummed manifest, causing the parser to accept two authoritative checksum records. Refuse any additional recognized `check` record inside the body even when the final checksum is valid. Preserve the single-check original format, unknown extension records, old history, first-wins duplicate path semantics, legitimate Unicode, scoring, saves, signer, app IDs, editions and all prior qualified builds.

Add checksum-valid manifest regression cases plus a ZIP import safety regression that proves original history bytes remain unchanged. Full :core:test, desktop classes and Android debug/release/instrumentation Java compile are required on the exact proposed source. Do not describe Java compilation as native Android acceptance.

### Execution checkpoint

At claim time: no production change, tests, APK build, signer test, native device acceptance, release or publication has occurred. APD464 is an earlier published release; this package must not overwrite it or independently claim a new APK. Use an isolated PR, inspect live heads, and merge only after exact-source verified successful CI via non-force expected-head integration. Record all actual results, blockers and next work below.

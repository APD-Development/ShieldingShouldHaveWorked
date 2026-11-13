# APD archive repeated-header guard, November 12, 2026

## Work claim (9:19 PM America/New_York)

This regular ChatGPT APD execution independently read the authoritative `codex/apd-phone-fixes-2026-10-09` at `e306c948d41f05317971cd51c9888d3257a1e5c2`, its AGENTS.md, handoffs, merged PR #61/#62, open PRs, source CI, and published APD464. Isolated branch: `chatgpt/apd-archive-repeated-header-20261112`. Search found no existing repeated-archive-header work claim. Do not duplicate this package.

Source-evidenced reliability defect: the format-1 `ArchiveManifest.encode` writer emits exactly one `APD-ARCHIVE\t1` header, but `ArchiveManifest.parse` merely sets `header = true` on each occurrence. A checksum-valid manifest with repeated headers is accepted, and a later record may overwrite the format version. Scoped correction: refuse a second recognized header while continuing to accept a normal single-header archive and ignore unknown future metadata records. Add unit regressions and a real ZIP rejection preserving existing history. Do not alter first-wins duplicate *file path* compatibility, data formats, scoring, gameplay, signing, APK identity, or prior releases.

No production edits, tests, APK assembly, native acceptance, publication, or guarded integration have occurred at the initial claim. The original private repository cannot be cloned from this runtime; use a branch-specific GitHub Actions source-only verification gate for actual JUnit and desktop/Android Java compilation. Source compilation is not Android native or originally signed APK acceptance.

Next: implement parser guard/tests in this isolated branch, verify actual CI terminal output and JUnit totals before non-force expected-head integration, preserve concurrent playback/native work, and record exact commits, Eastern timestamps, and any release blockers.
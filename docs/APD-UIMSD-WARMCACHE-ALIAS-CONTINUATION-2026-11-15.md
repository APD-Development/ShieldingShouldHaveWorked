# Optional UIMSD title prewarm: source-file alias boundary (November 15, 2026)

## Start and independent work claim
- Began Sunday, November 15, 2026 at 2:08 AM EST, on isolated `chatgpt/apd-warm-preload-alias-20261115-0200` from verified authoritative `codex/apd-phone-fixes-2026-10-09` head `017c8604b340aaff0f93bea67c69259be11604ed`. Do not assume main is current.
- Inspected AGENTS.md, latest verified handoff, current open PRs, recent CI and release receipts. Separate playback, C467 crash, C471/P471 native acceptance and signing work remain claimed elsewhere. No overlap or release rebuild.
- Source-evidenced gap: `UimsdWarmCache.stamp` fingerprints title-preload files with `File.isFile`/`FileInputStream`, which follow symlinked `runs/*.apdlog`, `legacy/*.apdlog`, column layouts and demo marker files outside the chosen history root. It also traverses symlinked `runs`/`legacy` child directories. `canPrepare` counts aliased history files. This optional background read is inappropriate and differs from the existing no-alias direct-history recovery policy. No claim of exploitation or C467 OOM root cause.
- Intended narrow scope: reject *optional preload only* when a child source file/folder is a symbolic link, without reading the aliased target. Preserve valid storage roots that themselves are symlinked, ordinary non-aliased history fingerprints, foreground UIMSD behavior, ownership handoff, games/saves/history/scoring/signing, and existing tested builds. Regression tests should exercise aliases and canonical-root compatibility.
- This initial checkpoint is **not** a tested/merged build. No unit tests, Android compilation, APK signing, native acceptance, publication or downloads are claimed yet. Preserve all earlier candidate and failed-verification receipts.

## Verification and integration pending
- Run complete core JUnit and desktop/Android Java compilation on the exact isolated branch; inspect the terminal job, JUnit totals and artifacts.
- Recheck live remote head and PR/work claims before non-force expected-head integration; reconcile newer work rather than overwriting it.
- This is source-only unless an entirely new original-signed APK independently passes established native, install-over/save-integrity and gated publication requirements.

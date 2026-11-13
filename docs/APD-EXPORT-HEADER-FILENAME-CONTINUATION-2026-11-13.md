# APD exporter filename/header identity continuation, Friday November 13, 2026 (Eastern)

## Isolated work claim, approximately 02:03 EST

Originating regular ChatGPT APD development thread. Verified authoritative branch `codex/apd-phone-fixes-2026-10-09` at `97b66dbee095fbd33cd418a8e16b3f58bccd7712`. This claim's isolated branch is `chatgpt/apd-export-header-identity-20261113-0203` at that exact base. Reviewed root AGENTS.md, completed-work ledger, known issues, preceding archive/import/privacy continuation records, actual source and real-writer tests, relevant branches, open PRs, CI and releases. Existing open playback/hero-selection/native/release/GPL branches remain exclusively owned by their separate work packages, untouched here.

**Evidenced defect:** `ArchiveExporter.collect` treats a parseable runs/<id>.apdlog as readable when `RunLogReader.parse` yields any header, even if the embedded `LogHeader.runId` is a different per-file ID. A same-directory copy renamed to `runs/alias.apdlog` can therefore enter a checksummed exported ZIP as `alias`, although the newly integrated `ArchiveImporter.stage` rejects it because the raw header identifies its original filename. Export and import disagree about a valid archive; misattributed raw history must not be packaged as a different log. This is distinct from the completed import-side check in PR #88 and from RunIndex/recovery boundaries.

**Bounded repair:** In the exporter only, consider a run log readable when its valid embedded header's individual log ID matches the filename stem. Do not apply this rule to imported legacy record files, whose historical identity handling must remain compatible. Preserve distinct per-log file IDs and shared logical typed RUN_START IDs across multi-segment runs. Skip mismatched copied run files as unreadable without mutating them; continue exporting independent valid logs, preserving manifest validity, exact source bytes and zip importability.

Add real-filesystem/real-writer/real-ZIP regressions for a mismatched local run alias excluded from the archive, a normal independently valid run retained and importable, valid distinct segment filename IDs sharing a typed logical run ID, and legacy compatibility. No gameplay mechanics, RNG, scoring, history files, archive schema, signer, app IDs, editions, releases, task schedules or other PRs change.

**Gate:** Full core JUnit and desktop plus Android debug, release and instrumentation Java compilation on the exact isolated source commit. Only merge after terminal successful CI and fresh live remote-head/PR checks with expected-head non-force integration. No native original-signed APK construction, manual screenshots, installed-over saves, physical-device acceptance, or release publication is claimed from source CI.

### Initial status

Work claim only; at this point no production edits, tests, build, publication or merge have occurred. Later sections must record verified commits, actual terminal test results and blockers. No recurring APD tasks changed.

# UIMSD export reliability candidate — October 15, 2026

## Existing work recovered, not restarted
Recovered the two corrections and seven regression methods retained locally in /workspace/cleric-current/report-review. Live ArchiveExporter blob2e7f1bd9371b0e97419eef1a35ee5c679b89f405 and LogRedactor blob83aa86992be79b9060c9714fbe0badaa555b8d4b still match the original defective source. EventTypes changes are limited to one named reason for the existing GAP event; no telemetry, save, archive container or raw-log encoding version changes.

## Equal-time run segment selection
ArchiveExporter selected the newest segment using only its creation timestamp. DatasetStore already uses timestamp and then run-start segment number. Two segments with equal milliseconds could therefore make a resumed unfinished run look finished during export, exposing unfinished-run internal fields, or unnecessarily redact finished history. The correction reads the existing segment field and uses the same tie-breaker. Two permanent tests cover both completion states, both filename orders, privacy, byte-identical completed exports, manifest/header consistency and unchanged original files.

## Incomplete redacted histories
Re-encoding a damaged surviving prefix, or skipping an unreadable public payload, could produce a syntactically clean copy whose counts appeared exact. The correction adds a public GAP marker with existing TLV/payload encoding and named reason5, amount0 meaning unknown loss, only when source status is not clean or a public event could not be decoded. It uses the last retained public clock and reveals no discarded-byte count, hidden event count or hidden-only clock. Clean UNSEALED/SEALED inputs and valid seals with unrelated trailing bytes retain exact counts and no invented gap.

Older/current RunLogReader implementations already count any EventTypes.GAP, and RunFactsBuilder treats any gap as incomplete; there is no new reader requirement, production storage migration or raw-history rewrite. This is provenance preservation in the existing privacy-redacted export copy. Original saves, raw run files, scores, difficulty rules and generation RNG remain untouched. Five permanent methods cover truncation, corruption/seal mismatch, unreadable typed payloads, clean controls and export/import/source-byte preservation.

## Verification boundary
Historical local evidence: both ordering regressions failed against the original exporter; redaction had four failures and one clean-control pass. The prior corrected647-method headless run used older dependency/source overlays, so it is not presented as current exact-source or Android evidence.
New core-only acceptance checks the exact verified TEST224 production source without modifying it (only the seven new tests are copied in), requires exactly the six expected failures and one passing clean control, then checks corrected archive/facts/rows/rule-archive integration. The test asserts numeric GAP reason5 without depending on the newly named production constant so it also compiles against the actual original source. Current results are pending; no new APK is built or published by that workflow.

The existing native release-history interaction method is selected in subsequent full Android gates and derives the technical heading from the actual APK index rather than hardcoding224. Subsequent full gates expect1683core methods,33API34 methods and42 PNGs, pending actual results. This does not count a repeated supplemental test twice.
Two concise Statistics/UIMSD preview icons describe these fixes only in a future compiled APK; the privately published224 APK is immutable and does not include them.

## Remaining limitations
The entire UIMSD audit is incomplete. This does not claim to solve all direct-query behavior for structurally valid but unreadable typed payloads; those require a separate verified investigation. No full audit count is advanced from these recovered per-blob checks. No production storage compression/format redesign, online activation, score multiplier, unrelated mechanic or Cleric change is made.


## Archive export corrections independently verified; full application gate requested
Run38827208663 at source59e99c04d582a8565195532914bec8633e3bd92b reproduced exactly six expected failures and one clean control in unchanged TEST224 application source3e886234e69ab128a861cfbc17bca549ba3a3456. Corrected source passed79 unique targeted methods,0failures/errors/skips. Independently downloaded artifact12015517394 ZIP SHA2560db5dfd7e9c1570828b61220f1bca26f74a782fa7fd184869997c13833d00c1f and recounted original/corrected XML. Original production files were not edited by the reproduction. This supersedes old local overlay evidence; targeted79 is a subset of the full suite, not an added79. No new APK yet. Request full1683core/debug/immediateprevious actually produced224history/33API34/42PNG; all20 comparison paths covered. Published224 remains fallback. Physical-phone acceptance and full UIMSD audit remain incomplete.

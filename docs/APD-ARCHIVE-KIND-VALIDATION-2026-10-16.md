# APD archive folder-kind integrity — 2026-10-16T07:32:21Z

## Confirmed defect
The importer validated archive paths and file contents but did not require a manifest entry's declared kind to match its folder. A file written under `runs/` could declare itself as `legacy`, bypassing the logical run-ID consistency check. The reverse mismatch—`legacy/` declared as a live run—was also accepted.

## Bounded correction
Before any archive payload is staged, `ArchiveImporter` now requires `runs/` entries to use kind `run` and `legacy/` entries to use kind `legacy`. Any mismatch rejects the complete archive without writing history. Valid existing archives and repeated-path compatibility are unchanged.

## Retained acceptance
Two hostile-archive methods cover both mismatched directions. Exact TEST236 application source must fail both methods. Corrected source must pass all 1,703 core methods and retain the existing all-or-nothing, checksum, path, duplicate-entry and non-overwrite contracts.

Expected candidate gate: two intended TEST236 failures, then 1,703 corrected core methods, debug assembly, complete immediate-previous TEST236 APK history, 33 API34 emulator methods and 42 valid screenshots. No publication, tester distribution, physical-phone result or owner install-over/save claim is authorized by this record.

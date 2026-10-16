# APD archive run-start identity recovery — 2026-10-16T06:43:14Z

## Confirmed defect
Archive export and import stopped considering typed `RUN_START` records after the first typed frame even when that frame's payload could not be decoded. A later readable start could therefore be omitted from the export manifest's logical run identity. Import could reject a correctly labelled archive or accept one labelled only with the physical log-header ID while ignoring the later readable logical run ID.

## Bounded correction
`ArchiveExporter.collect` and `ArchiveImporter.stage` now claim the first run start only after `LifecycleEvents.decodeRunStart` succeeds. Malformed frames remain byte-for-byte in raw history. The first readable start still wins, so later segments cannot relabel an already identified run.

## Retained acceptance
Three methods cover export identity, acceptance of an archive labelled with the first readable logical run, and rejection of a manifest that substitutes the physical header ID. Exact TEST235 application source must fail all three methods. Corrected source must pass all 1,701 core methods while preserving lossless finished-log export.

Expected candidate gate: three intended TEST235 failures, then 1,701 corrected core methods, debug assembly, complete immediate-previous TEST235 APK history, 33 API34 emulator methods and 42 valid screenshots. No publication, tester distribution, physical-phone result or owner install-over/save claim is authorized by this record.

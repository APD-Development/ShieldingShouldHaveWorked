# APD archive empty run-identity recovery — 2026-10-16T08:35:41Z

## Confirmed defect
A syntactically decodable `RUN_START` with an empty run ID was treated as the authoritative typed start. It could hide a later valid logical run identity, causing export to label the log with its physical header ID, reject a correctly labelled import, or accept an import labelled only with that header ID.

## Bounded correction
Export and import now claim a typed start identity only when its decoded run ID satisfies the existing `StatsStore.isValidRunId` contract. Empty or invalid identities are skipped while scanning for the first valid typed start. Raw bytes, event order, finished-state detection and valid existing archives are unchanged.

## Retained acceptance
Two `ArchiveRoundTripTest` methods cover export labelling and both import decisions. Exact TEST237 application source must fail both methods. Corrected source must pass all 1,705 core methods.

Expected candidate gate: two intended TEST237 failures, then 1,705 corrected core methods, debug assembly, complete immediate-previous TEST237 APK history, 33 API34 emulator methods and 42 valid screenshots. No publication, tester distribution, physical-phone result or owner install-over/save claim is authorized by this record.

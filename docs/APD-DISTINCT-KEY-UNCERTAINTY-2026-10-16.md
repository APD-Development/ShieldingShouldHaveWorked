# APD distinct-key uncertainty correction — 2026-10-16

## Confirmed defect
A per-distinct aggregate used a private loose identity for each row whose distinct key was hidden, missing or otherwise unavailable. This deliberately prevented unknown keys from being merged or dropped, but the resulting `AggValue` was not marked `UNSURE`. Lower-bound keys were also accepted as exact identities. The table could therefore present an exact-looking count or rate even though the distinct identity was inexact.

## Bounded correction
- Keep every inexact-key row and preserve the existing numeric result.
- Use an exact shared key only when the distinct value is visible, known and not lower-bound.
- Mark the aggregate uncertain for every loose identity so the existing detail formatter discloses that some entries could not be judged.
- Leave exact known distinct keys and all aggregate arithmetic unchanged.

## Retained acceptance
`rowsWithNoValueToShareAreNotMergedByTheDistinctRule` still proves two missing keys stand for separate rows, and now requires uncertainty plus the UI disclosure. `lowerBoundDistinctKeysAreNotTreatedAsExactIdentities` proves a lower-bound key is not treated as an exact shared identity. Exact TEST239 source `6182b042b1d93b64341f41a516a49ef3d12c93ee` must fail both methods; corrected source must pass all 1,707 core methods.

## Required TEST240 gate
Dedicated before/after evidence, complete core suite, debug assembly, complete immediate-previous TEST239 APK history from run `38881765134`, 33 API34 emulator methods and 42 valid screenshots. No automatic publication, tester distribution, physical-phone result or owner-device install-over/save claim is authorized.

No raw histories, saved views, cache formats, saves, scores, gameplay, Custom Dungeon Rules, turns, RNG, Cleric lore/private-grudge/opt-out behavior, signing or application ID are changed.

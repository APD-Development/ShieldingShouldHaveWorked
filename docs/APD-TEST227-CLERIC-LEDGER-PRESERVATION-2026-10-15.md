# TEST227 Cleric collection-ledger preservation candidate

Recorded: 2026-10-16T00:48:52Z (2026-10-15 8:48:52 p.m. EDT)

## Reproduction

The delivered TEST226 implementation tolerated malformed ledger rows in memory, but did not retain knowledge that loading was incomplete. A later automatic save serialized only recognized rows and permanently discarded every unknown original byte. Its rename fallback also deleted the destination before retrying, so an empty directory at the configured ledger path could be replaced by a regular file.

Two permanent JUnit methods reproduce these boundaries without owner data:

- `automaticSaveDoesNotRewriteAFileWithDamagedRows`
- `saveNeverReplacesAnExistingDirectoryAtTheLedgerPath`

Against unchanged TEST226 production: 7 methods run, 2 failures, 0 errors/skips. The failures were the expected byte-preservation and directory-preservation assertions.

## Bounded correction

- Keep every intact parsed row available in memory.
- Mark malformed rows or an interrupted read as incomplete.
- Refuse automatic persistence after an incomplete load, preserving the original file byte-for-byte.
- Serialize use of the shared sibling temporary file.
- Refuse directory destinations and replace regular files without first deleting the original.
- Preserve the existing explicit player reset as the sole action that clears incomplete state and writes a clean ledger.

Local corrected result: all 7 `LedgerTest` methods pass, with 0 failures/errors/skips. Java compiler warning about the standalone test command's source-module path is non-failing and unrelated to application behavior.

## Scope and pending evidence

No save, raw run-history, score, gameplay, turn, RNG, Custom Dungeon Rules, lore content, opt-out, application ID, signing key or storage format was changed. This is a candidate only. The required full gate is 1,685 core methods, debug APK assembly, complete immediate-previous produced TEST226 history, 33 API34 emulator methods and 42 valid screenshots. No publication, tester distribution or physical-phone result is claimed. TEST226 remains the privately delivered fallback until actual later evidence exists.

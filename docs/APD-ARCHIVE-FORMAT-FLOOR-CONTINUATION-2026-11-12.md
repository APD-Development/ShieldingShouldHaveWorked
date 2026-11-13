# APD archive manifest nonpositive-format boundary, November 12, 2026 (Eastern)

## Work claim and source
Started at approximately 7:21 PM EST on November 12, 2026. Independent source-only work branch `chatgpt/apd-archive-format-floor-20261112-1921` begins from authoritative development commit `d6a737b4e82e5740f7559a63f1a491e29f5be298`. This is not a main-branch, playback, wall-search, APK, native-acceptance, signing, or release work package. Prior manifest-size overflow and duplicate-manifest-path compatibility investigations remain complete and untouched.

The owner's ArchiveManifest.FORMAT is 1 and the parser rejects newer formats, but a checksum-valid `APD-ARCHIVE\t0` or negative format currently passes that check. The source contract supports format 1, not invented pre-version formats. An independent regression will prove rejection before the importer writes files, while ordinary current-format manifest round trips and unknown future metadata-line compatibility remain unchanged.

## Status
Work claimed, implementation and source CI not yet run. No APK built, signed, accepted or published. Do not infer device or native acceptance from a source test.

## Next steps
Add narrowly scoped parser validation and tests; run isolated source-only CI with full core tests and required desktop/Android Java compilation; inspect terminal results before any integration. Read authoritative head and active claims again before writes or merge; preserve all accepted and blocked application builds and the original historical files. Record exact source commits, tests and limitations here after verification.

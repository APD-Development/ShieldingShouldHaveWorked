# APD diagnostic file alias boundary, November 15, 2026

Recorded at Sunday, November 15, 2026, approximately 12:09 AM EST.

## Start and independent work claim
- Authoritative baseline: `codex/apd-phone-fixes-2026-10-09` at `05f3e893f72eb7457275c74965ba9839c4f30f93`. This is not `main`.
- Isolated branch: `chatgpt/apd-diagnostic-symlink-boundary-20261115-0000`. Previous diagnostics PRs #147, #148 and #150 are merged; C467's OutOfMemoryError cause remains unknown. Do not duplicate those fixes.
- EVIDENCED: `SupportDiagnostics.read` follows a same-named symbolic link for `session.txt` or `last-crash.txt`, potentially importing an unrelated file into exported support reports despite their no-history assertion. The crash writer also follows such a link, and the async atomic writer follows a `session.txt.tmp` link. These are local file-alias integrity defects, not evidence that a device has been exploited.
- Source fix commit: `6c95d89a0d02ec716e84b5d4c1d38a61e859d2fa`. Existing canonical diagnostics directory permits regular files and already-canonicalized symlinked storage roots; refuse file aliases in read, atomic staging and emergency crash paths.
- Regression commit: `080198b6c5de42799adb4b42080dba0bb8bdf001`. Three real filesystem tests cover unrelated session/crash aliases, staging temp aliases and a valid symlinked storage root. Existing C467 bounded memory and line-integrity tests stay intact.
- This is an isolated source package, NOT a tested, merged, native-accepted or published APK at this checkpoint. Keep C471/P471 frozen and all earlier release, signer, identity, save/history and scoring receipts unchanged.

## Verification and next safe steps
Verify entire core JUnit suite plus desktop and Android debug/release/instrumentation Java compilation on the exact isolated source. Inspect terminal CI, artifacts and regression names. Recheck active work claims and authoritative remote head; integrate only by guarded expected-head non-force update after green CI, without touching unrelated branches. Native Android/phone, same-signer install-over/save-integrity and existing publication gates remain mandatory before claiming a qualified APK. Preserve the C467 diagnostic root-cause investigation independently.

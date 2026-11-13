# APD fallback import ZIP symlink boundary, November 13, 2026 (Eastern)

## Independent work claim (09:36 EST)

- Verified repository: `APD-Development/ShieldingShouldHaveWorked`. The established authoritative branch is `codex/apd-phone-fixes-2026-10-09`, starting at `7c8678c1940b1e19f2521e3e5c20753f9432187a`, not `main`. Isolated branch: `chatgpt/apd-fallback-zip-symlink-20261113-0936`.
- Reviewed AGENTS.md, current Markdown continuation, completed fallback ZIP timestamp correction `docs/APD-FOLDER-IMPORT-MTIME-CONTINUATION-2026-11-12.md`, open PRs, relevant `ApdFiles.FolderProvider` source and filesystem tests. Existing report Markdown PR #138 and personal playback/native/release claims do not own this independent fallback-provider concern.
- Source-evidenced boundary: `FolderProvider.chooseImport` uses `File.isFile()` to select the newest `.zip`, which follows symlinks. An alias `imports/z-newest.zip` can therefore cause the fallback desktop picker to read an external ZIP instead of an actual file inside its import directory. This differs from the completed timestamp-ordering correction. Preserve normal timestamp/name selection and valid symlinked base storage roots; reject aliasing at the ZIP-file boundary, and avoid any Android SAF provider change.
- Scope: filesystem-safety/correctness bugfix only in the desktop/fallback import picker, regression tests, exact-source verification and handoff. Do not modify actual imported contents, run history, saved games, scoring, balance, signer, application IDs, editions or independent release claims. No APK qualifies without original signing and native gates.

## Verification and integration

- Pending source/test edits, exact CI and isolated review. Check the shared remote head and overlapping claims before any integration. Use only expected-head non-force updates, never overwrite concurrent work.

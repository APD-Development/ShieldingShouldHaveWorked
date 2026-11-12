# APD local backend-record integrity continuation, November 12, 2026 (Eastern)

## Verified starting state and isolated ownership

Started the coding execution about 3:46 PM EST in the existing regular APD conversation. Read the owner's private repository, AGENTS.md, the active handoffs and ledger, open PRs, CI, and published releases. Authoritative branch codex/apd-phone-fixes-2026-10-09 had exact HEAD 51192feb191c092b4c81a0f13aa54861b2d5ce0b when this isolated work branched. Main was not used. Previously completed report-provider/index/search/archive fixes and parallel animation, APK/native and gated release claims were not touched. No upstream resources were accessed.

Isolated branch: chatgpt/apd-tabfile-integrity-20261112-1545. Draft PR #40: https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/40 . GitHub ref writes used expected-head, non-force updates and never wrote to the shared development branch.

## Evidenced source findings and changes

The existing backendbridge/TabFile.java loads Base64-encoded fields using UTF-8 replacement decoding, allowing invalid bytes to silently become U+FFFD instead of rejecting that damaged record. Its write fallback deletes the existing destination if a sibling rename fails, before another rename that may also fail. This creates a source-level record-integrity risk for optional local session, submission and receipt records, not a verified user-device incident.

Commit 6a0d8b509182e1fca7ee330562ca722ad4637919: Strict UTF-8 field decode; skip only damaged lines; refuse directory destinations; never delete existing records after a failed rename. Four new JUnit cases check malformed UTF-8 between intact Unicode records, normal Unicode replacement, directory-target preservation, and byte preservation on failed temporary writing. Valid Base64 encoding, schema, successful record reads/writes, gameplay, balance, saves, scores, history, original signer, app IDs, and edition boundaries are unchanged.

Original run https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43304596378 failed in new test-source compilation. Raw logs identify five incompatible Arrays.asList(String[]) generic row types. This failure is preserved and is not passing CI.

Corrective commit 804082cbd5e644bc574357251438fb1aedbfa50b replaces those five fixtures with Collections.singletonList(String[]) and adjusts CI path filters so corrected source/test pushes retrigger verification. Exact corrected source run https://github.com/APD-Development/ShieldingShouldHaveWorked/actions/runs/43304707388 was in progress at this handoff checkpoint. No clean test counts or Android Java compile success is claimed before terminal evidence.

## APK/publication and next safe execution

No signed APK was built, tested natively, published or installed by this execution at the initial handoff. Separately verified release APD462 was published at 2026-11-12T20:20:21Z with Developer, Alpha-Testers and matching source assets: https://github.com/APD-Development/ShieldingShouldHaveWorked/releases/tag/apd-462 . It does not contain this local record fix, and APD461 and PR #38 remain separate.

Next safe step: inspect run 43304707388 at its exact source SHA and its raw jobs/JUnit XML, requiring at least 3,315 clean core cases including all four new methods and desktop plus three Android Java compilation targets. Correct demonstrated failures only. Before any merge, reread the live authoritative head, PR #40 head and open work claims; integrate without force/overwrite only after exact CI success. Keep this receipt updated with integration/test results and distinguish source verification from native/signed APK gates.

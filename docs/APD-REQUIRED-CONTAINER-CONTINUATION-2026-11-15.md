# APD format-1 required container record: independent continuation (November 15, 2026 EST)

## Work claim, approximately 09:00 EST

- Owner: APD-Development/ShieldingShouldHaveWorked. Start authoritative branch `codex/apd-phone-fixes-2026-10-09` head `5be41405598826a41c94a23f19c6c4a080a000ac`. Isolated proposed source branch `chatgpt/apd-missing-container-20261115-0900`. Work does not target main, releases, native Android test ownership, personal playback or active open PR #135/#118/#48/#47/#38/#33/#22/#20/#6/#3/#2.
- Reviewed AGENTS.md, completed-work ledger, known issues, latest checksum-footer handoff (PR #169/#170), current open PR claims, historic build/release gate and exact-source CI. No active open PR claims this missing-container behavior. The previous container singleton and scalar compatibility tests are separate and remain intact.
- **Evidence:** `ArchiveManifest.parse` records `containerSeen` only to refuse duplicates, but never requires a container field. A correctly checksummed format-1 manifest missing the writer-required raw log container version silently parses with Java's default zero. This can misrepresent archive provenance. The writer always includes the field, including its legitimate zero value. Historical optional created/app/telemetry fields and checksummed unknown extension records must stay valid.
- **Bounded change:** Refuse missing `container` metadata only; keep numeric zero, all current valid writer output, first-wins duplicate path handling, optional scalar omissions, unknown extensions, archive writer/checksum and existing original history. Add parser/ZIP non-mutation and compatibility regression cases.
- This initial claim alone establishes no passed test, APK, native acceptance, merge, publication or signer verification. Exact CI, fresh-head coordination and non-force guarded integration are required before merging. Android Java compilation is not native/device acceptance.

## Execution state

- This isolated branch proposes the minimal parser guard, three regressions and branch-scoped full core / desktop / Android Java CI. Record exact results only after run completes; do not infer source success from scheduling or workflow creation.
- Latest prior qualified publisher and C471/P471 release/signing/native gates remain separate. No APK is requested from an unaccepted source patch.

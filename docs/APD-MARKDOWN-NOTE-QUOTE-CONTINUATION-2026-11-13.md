# APD Markdown note-quote boundary, Friday November 13 2026 (Eastern)

## Isolated ownership claim: 05:13 EST

Origin: existing regular APD ChatGPT conversation, not a separate development task. Verified latest authoritative branch `codex/apd-phone-fixes-2026-10-09` at `c519643dece90e80b51d12dbe7b132a9e4ab122e`, newer than the verified Markdown CR and coverage repairs (PR #106–109). Created isolated branch `chatgpt/apd-markdown-note-quote-20261113-0500` from exactly that SHA. Read AGENTS.md, completed-work ledger, source guide, current handoffs, latest report source/tests, repository branches and open PR claims. Current open claims concern playback, hero UI/native, search, licensing and separate challenge mechanics; no open PR owns this note-block quotation boundary. Preserve all those work packages, APD464 and older builds.

**Source-evidenced defect, not an owner-reported phone reproduction:** `ReportRenderer.markdown` emits a `Report.Note` as one leading `> ` followed by raw note text. An embedded LF/CRLF/bare CR causes later lines to escape the blockquote; a saved note can therefore appear as independent report content rather than as part of its own warning/note. This is distinct from the already corrected Markdown table-cell and coverage-summary formatting. Scope only this note rendering path: quote every logical line, normalize all three line-ending forms, preserve intended inline Markdown, all other formats and source data. Add a focused JUnit regression for warning/info/note, line endings and unchanged normal/other-format behavior.

**Verification requirement:** Add a source-only push CI gate on this isolated branch running complete `:core:test`, `:desktop:classes`, `:android:compileDebugJavaWithJavac`, `:android:compileReleaseJavaWithJavac`, `:android:compileDebugAndroidTestJavaWithJavac` and explicit new test presence checks. Preserve any failed attempts. Do not merge merely on source push: require successful exact-source CI, recheck authoritative branch head and active claims, then integrate only with guarded expected-head non-force procedure. This claim is not code completion, test success, a new APK, signer acceptance, native test or release.

**No scope:** no gameplay, balance, history/seed scoring, save schema, app identifiers, signing, edition boundary, release candidate, active playback or search work. Original-signed APK publication and physical/native/install-over acceptance remain separately gated and not established in this package.

## Pending verification and continuation

Record exact commits, terminal CI, target integration and build/release blockers before closing. If CI or collision blocks safe integration, leave this isolated candidate and all evidence intact.

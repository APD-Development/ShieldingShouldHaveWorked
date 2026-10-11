# UIMSD audit: archive ignored-entry resource-limit bypass

Confirmed, fixed in this batch; complete audit remains pending.
Severity: medium (import resource exhaustion). Existing saves/statistics are preserved.
Location: core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/apd/archive/ArchiveImporter.java, ignored/unlisted/repeated entry branch.
Evidence: controlled513MiB ZIP entry stream accepted by exact canonical importer despite existing512MiB protection. Regression fails before fix and passes afterward without huge allocations.
Cause: getNextEntry implicitly drains skipped entry outside bounded reads. Fix uses existing bounded reads/limit before staged history commit.
Tests: five new regression tests plus existing archive suite, total68 passed locally. Full CI pending. Physical phone testing pending. Not shipped in TEST45.
Unresolved investigation: ArchiveManifest.find linear lookup per archive entry creates quadratic total lookup work; no speculative gameplay changes. StorageMeter recursive traversal and archive wrapper lifetimes remain suspicions, not confirmed normal-player bugs.

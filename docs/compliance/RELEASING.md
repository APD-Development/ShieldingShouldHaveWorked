# APD binary and corresponding-source release procedure

**Current state: NOT DONE / blocked.** See `release-blockers.json` and `REPORT.md`.
Do not call current distributions fully GPL-compliant. Do not publish source with signing material.
This policy does not change visibility, rename APD, rotate keys or rewrite history.

1. Pin the real producing commit, binary SHA256, versionCode/versionName (desktop: embedded version/platform), build flags and dependency/tool versions. Never tag a later unrelated commit as the producing source. Preserve original publication and test evidence.
2. Tag that exact commit. Use a unique tag containing platform, versionCode and versionName; record each variant's recipe when one source commit produces several variants. Existing `apd-alpha-preview-341` already points to its actual producer `acb69c653033ee0d8df68264395ca27e08eb566d`.
3. Run pinned gitleaks against the working tree and all reachable fetched branch/tag history. Supplement with signing-file and properties inspection. Keep raw/redacted scanner output private; only sanitized path/commit/rule metadata belongs in this repository. A scan is not a guarantee that every secret is found. Assess any findings before sharing source. Do not rewrite history or rotate the install key automatically.
4. Prepare a **key-free complete source archive** for that binary. Include all source, assets, licenses, build/install scripts and precise generation inputs. APD CI currently merges history from a previous APK and creates feature assets: include those exact generated inputs too. Do not make recipients depend on expired private Actions artifacts. Explain how to build using their own signing key; never provide the developer's private key. Functional corresponding source and bit-identical signed output are different claims.
5. Verify the archive against the producing commit and generated-input hashes; rebuild/compare application contents in a clean environment. Record limitations in `build-recipe.json`. Do not claim a signed-byte reproduction without actually demonstrating it. Retain the archive for as long as required.
6. Resolve dependency and asset rights using `dependencies.json` and `assets.json`, including transitive/native components and required attribution. Do not infer a font/sample license from the Java code license.
7. Supply a manifest with `source_commit`, `source_tag`, `version_code`, `version_name`, `binary`, `binary_sha256`, `source_archive`, `source_sha256`, `source_download_url`, `recipient_access_evidence`, `secret_scan_evidence`, and `build_recipe`. Run `python3 tools/apd_gpl_release_gate.py --manifest <manifest.json>` **before publication**. It currently fails closed on real unresolved blockers. It is a preflight, not a legal certificate or an archive/rebuild verifier.
8. Put the binary and matching-source links **together** in release notes, tester posts and store listings. A link to upstream SPD or to APD's changing default branch is insufficient. A private GitHub URL does not provide equivalent access to a recipient without repository membership. Provide a no-charge accessible source archive through the same distribution channel if repository access is unsuitable. Do not impose an NDA or prohibit GPL-authorized redistribution.
9. Verify both downloadable assets, hashes and recipient access before announcing delivery. Preserve existing assets. Keep signing identity/application ID and saves intact. Do not publish public releases or contact testers from this audit.

Suggested download block:

```
APD <versionName>, versionCode <code>, <platform/variant>
APK/binary: <direct download> — SHA256 <hash>
Matching source: <accessible archive> — SHA256 <hash>
Exact producing commit/tag: <commit> / <tag>
Build instructions: <recipe in source archive>
GPLv3 or later; no warranty. APD is an unofficial modified version of SPD.
```

**NOT DONE: universal enforcement.** Legacy publishing scripts/workflows have not all been migrated to this preflight. The new source-only CI checks do not publish or build APKs. Repository/organization release permission and required-check policies are **NEEDS CHRIS**; a documentation rule cannot prevent an authorized account from bypassing it. Do not claim future deliveries are automatically compliant until all active publication paths are gated and the blockers are resolved.

Keep the owner's three-APK-per-day maximum. This audit requests **no APK builds or releases**.

# APD GPLv3 compliance audit — 2026-11-01

**NOT DONE: APD is not established as fully GPL-compliant.** This PR supplies fixes and evidence, not a legal certification. Known signing/source-access, dependency and asset issues remain. No new APK or source archive was released.

**DONE means evidenced in this unmerged PR branch or a verified historical fact; it does not mean these fixes are already in APK341.** Fix implementation commit: `4440fed63`; notice-only layout-guard correction: `6854bd37a`; current publication guards: `8b4503e76`.

Audited real approved-branch head: `377c3e20b7dc6ec6c4d149c5c1b4ea7cda3bb7ed`.
Exact inherited SPD v4.0.0 baseline: `2bb34a4e91d29c8785a9363cad6ddfe5122b1d4f`.
Fresh isolated full clone; pre-existing dirty `/workspace/apd` was not modified. No history rewrite or main/Claude change.

## 1. License and notices

| Status | Item and evidence |
|---|---|
| DONE | Root `LICENSE.txt` retained byte-for-byte; SHA256 `d0495053051967ebe76fb1facd287d79d1ed800da1be75cf501a556bc39a0472`. Its complete GPLv3 terms are copied verbatim to `core/src/main/assets/legal/GPL-3.0.txt`. |
| DONE | Existing upstream copyright lines preserved. `tools/test_apd_gpl_compliance.py` compares modified source files against the pinned audited head. |
| DONE | APD headers added to comment-capable APD-modified/added source/config files. `docs/compliance/header-years.json` derives years from author dates after the real upstream baseline; all identified APD changes are in2026. Generator: `tools/apd_copyright_headers.py`; per-file coverage: `file-notices.json`. Headers apply to APD modifications, not a claim to upstream authorship. |
| NOT DONE | Literal inline headers in **every** file: adding comments inside binary assets, JSON, TSV/scoring tables and historical document formats would corrupt them or alter recorded data. Those files have external per-file notices in `file-notices.json`; their payloads are preserved. Signing material is deliberately untouched under the report-only instruction. |
| DONE | Root `NOTICE` and bundled `legal/NOTICE.txt` identify Chris Charlton, SPD/Evan and Pixel Dungeon/Oleg; date begins2026-10-05 in Git, same GPL3-or-later terms, no warranty and no affiliation/endorsement. The owner's stated project start date is not substituted for Git modification dates. |

## 2. About and offline license

| Status | Item and evidence |
|---|---|
| DONE | `scenes/AboutScene.java` identifies APD as an unofficial modified version; retains original contributor, artist, sound and translator credits. `apd/GplLicense.java` states GPLv3-or-later, no warranty, non-endorsement and actual APD source URL. |
| DONE | `windows/WndGplLicense.java` exposes every byte of the bundled license offline through bounded native scrolling pages. `GplLicenseTest` checks byte identity, complete reconstruction, page bounds and legal notices. The menu avoids constructing a single enormous text window. |
| NOT DONE | Physical-phone/offline navigation acceptance has not been performed for these new controls. Source/core/platform verification is reported separately below; do not infer it from old APK341 tests. |
| NEEDS CHRIS | In-game development-source URL remains private. It is visibly described as private, not falsely advertised as universally accessible corresponding source. Choose a recipient-accessible source route without changing repository visibility automatically. |

## 3. Distributed build and source

| Status | Item and evidence |
|---|---|
| DONE | Latest published build is341, not rejected342/344 or uncompiled343. Existing `apd-alpha-preview-341` tag resolves to exact producer `acb69c653033ee0d8df68264395ca27e08eb566d`; no tag move is needed. Release428527455 and `builds.json` record both binary hashes, app ID, versionCode920, developer `4.0.8-apd-INDEV-bC341-TEST` and tester `4.0.8-apd-INDEV-bP341` recipes. This identifies the newest distributed artifact, not which version each tester actually installed. |
| NOT DONE | A complete, key-free, accessible matching source archive is not attached to341. CI-injected feature assets and366 compiled-history TSVs must accompany the source/build recipe. A Git tag alone does not demonstrate complete corresponding source or bit-for-bit binary reproduction. |
| NEEDS CHRIS | Establish no-charge source access for every recipient, including testers without private-repository access; select an archive host/channel and resolve signing exposure before sharing source. No visibility change, public release or tester post was made. |
| DONE | `RELEASING.md` defines matching tags/version metadata, source archives, generated inputs, adjacent source/binary links, hashes and actual recipient-access checks; `apd_gpl_release_gate.py` fails closed with known blockers and missing manifests. |
| DONE | `publication-guards.json` identifies25 current binary-publication paths and30 guarded create/upload calls/actions. `publication_run` refuses writes with unresolved blockers; workflow release actions have preflight steps. Read-only checks, existing assets and PDF-only reports remain intact. Targeted regression tests cover refusal before writes. Implementation: `8b4503e76`. |
| NOT DONE | Universal enforcement and historical source-to-binary recovery: historical workflow reruns and manual API/UI writes remain possible. The patch is not merged into active branches. Required-check/release-permission enforcement is NEEDS CHRIS. |

## 4. Secrets — findings only

| Status | Item and evidence |
|---|---|
| DONE | Pinned gitleaks8.30.1 archive verified with published checksum `551f6fc83ea457d62a0d98237cbad105af8d557003051f41f3e7ca7b3f2470eb`; full reachable history scanned with `--all --full-history`, plus working-tree scan and supplemental file/config inspection. `secrets-redacted.json` records the initial10628 reachable commits and a final10630-commit scan (including the first two audit commits), candidate metadata and scope. Both history scans returned10 candidates; pre/post-fix working-tree scans each returned6. The scanner exit1 represents candidates, not a clean/zero-findings pass. Raw output stays outside the repository in `/workspace/gpl-audit-private`. |
| NEEDS CHRIS | Confirmed tracked `keystore/apd-debug.jks` and hard-coded `storePassword`/`keyPassword` in `android/build.gradle`, introduced in `56be46ab9`. Values are intentionally omitted here. No key, password or history was removed/rotated. Preserve install compatibility and assess existing exposure before choosing remediation. |
| DONE | Automated history candidates were inspected as Git/checksum assertions, test names, key-binding identifiers and a sound resource name, not confirmed API credentials. Supplemental inspection caught signing material the generic scanner missed. No `google-services.json` path was found in fetched reachable history. This is not a guarantee that no other secret exists; unreachable server objects, Actions logs/secrets and external uploads were not audited. |

## 5. Dependencies and assets

| Status | Item and evidence |
|---|---|
| DONE | `dependencies.json` enumerates43 direct external Gradle declarations, including native classifiers/test/build tooling, versions, POM URLs, declared license metadata and hashes where retrieval succeeds. Local project dependencies remain inherited GPL source modules. |
| NOT DONE | Runtime `org.json:json:20170516` explicitly declares the JSON License with “Good, not Evil”, a GPL-incompatible use restriction. It occurs in `SPD-classes/build.gradle` and core tests. A tested Public Domain replacement20240303 was rejected and rolled back: commit `62890a2be`, run41266617554/job124730633829,2234 methods with2 unchanged-golden-hash failures. The candidate compiled on Android debug/release and desktop, but compatibility was not established. No golden reference was rewritten and no library/production format change is retained. `dependencies.json` records POM/source/JAR evidence and artifact13067885574 ZIPbdfe6572b1080d68f123c482f8d6abf1807b2145c5cee931c0cd6b9a0035554f. The hash differences are not proof of actual dungeon changes or data corruption; that cause remains unconfirmed. Resolve a compatible implementation or appropriate permissions before distribution. |
| NOT DONE | A complete transitive/native license bill of materials is not established by POM metadata. FreeType/LWJGL/RoboVM native exceptions and test-only/build-tool licenses need component-level confirmation. No proprietary billing/analytics SDK was identified in direct Gradle declarations; this is not an assertion about every transitive binary. |
| DONE | `assets.json` enumerates360 graphical/audio/font assets, hashes and inherited/APD-added/APD-modified status against the exact SPD baseline. Seven APD-specific assets are individually listed: `effects/apd_flame.png`, `interfaces/apd_button.png`, `interfaces/apd_logo.png`, `interfaces/apd_logo_glow.png`, `splashes/title/apd_bg_land.png`, `splashes/title/apd_bg_port.png`, and modified `sprites/items.png`. |
| NEEDS CHRIS | Per-file license/provenance for those seven assets and exact inherited font/sound-sample mapping are not proven by a repository-wide code license. About preserves original CC-BY/CC0 sample credits. No permissions or licenses were invented. Resolve unknowns before claiming asset compliance. |

## 6. Branding and upstream hooks

| Status | Item and evidence |
|---|---|
| DONE | `build.gradle` already uses `com.charltonchris29.adhdpixeldungeon`; debug `.indev` remains unchanged. Java package names, legacy preferences and backup names are technical compatibility identifiers and remain intact. |
| DONE | Removed visible SPD logo from the About credit block while preserving factual text attribution. Corrected APD-identifying welcome/update/save/error strings and developer contact messages; retained original translator credits. `README.md` no longer presents APD as official SPD or promotes upstream store/donation links. |
| DONE | Android release, desktop and iOS select disabled non-network news/update services. Both former upstream implementations are also inert, preventing SPD feed/release requests if accidentally selected. Debug stubs no longer generate fake news/updates. See platform Gradle files and `services/news/`, `services/updates/`. No unfinished APD network service was activated. |
| DONE | Existing `SupporterScene` redirects to About; `WndSupportPrompt` is neutral attribution and boss-reward promotion was already removed before this PR. No billing SDK/purchase flow was found in direct platform dependencies. Credits and GPL notices remain. |
| NOT DONE | “No SPD logo/title art/icon anywhere” is **not** satisfied: launcher/release/desktop/iOS crown icons and packed legacy logo/changelog atlas regions remain inherited. Main APD title art is modified, with rights/provenance still unresolved. This PR does not claim these assets have been replaced or platform-tested. |
| NOT DONE | `remaining-branding.json` records161 remaining proper-name/source literal findings with factual-history versus technical-identifier classification. Historical SPD reference changelogs still contain attributed original developer text and icons; full multilingual/manual review of every user-visible string is incomplete. Ordinary lowercase “shattered” item descriptions are not automatically renamed. The factual About attribution URL is retained. |
| DONE | `PLAY-LISTING-DRAFT.md` supplies an unofficial-fork/non-endorsement/credits/source-link draft, clearly unsubmitted with an accessible-source placeholder. |
| NEEDS CHRIS | Repository visibility, any history rewrite and any game rename remain unchanged. Review “Pixel Dungeon” title/store impersonation risk, original branding assets and final listing. No author contact or store submission was performed. |

## Verification

DONE:6 local compliance regressions initially passed; the final publication-guard additions pass8 targeted regressions. All3 pure offline-license JUnit cases and all8 `UimsdWiringTest` cases pass locally. All tool Python sources and workflow YAML parse. The gate correctly reports BLOCKED for real unresolved issues, not a compliance pass.

DONE: source commit `6854bd37a7eb62316ed1126c3b877c4a1031d7bb` passed run41266451202/job124730153071: full2234 core tests, Android debug/release Java compilation and desktop classes; no APK assembly. Artifact13068035354 ZIP515e7baa12136a79e2b241ecb1cdaccce4377367368b377dca639b8fd78b1858.

Retained failures: initial72c8bfb37 runs41266136894 and41266172803 each failed1/2234 because a legal-header insertion changed a whole-source layout fingerprint. The correction permits only the exact APD notice before `package`, keeps both original expected code/layout hashes and all assertions, and does not skip tests. The Public Domain JSON candidate then failed2/2234 golden comparisons and was rejected; details above.

DONE: final application/source commit `8b4503e7662d44654da13384b4a69730b1a6b50a` passed run41266949018/job124731618647: **2234 core methods /426 suites /0 failures, errors or skips**, plus Android debug/release Java compilation and desktop classes. XML artifact13068011563 ZIPffd8a6c9143c129696b6f1f325df8749b387d7c494b7e1db43b2fe52f4446b03 was downloaded and independently parsed. CI ran7 tool regressions at that commit; a later tool-only guard-coverage test makes8 local regressions, all passing. Subsequent changes are report/inventory/tool-test only; application source is unchanged. No APK assembly, signing, publication, emulator run or physical-device test occurred in this audit. GUI license navigation therefore remains NOT DONE for device acceptance.

Evidence commits: `4440fed63` implements notices/offline license/services/identity fixes; `72c8bfb37` removes generated Python bytecode; `6854bd37a` corrects only the legal-header hash exception with original expected hashes unchanged; `62890a2be` is a retained rejected JSON candidate; `8b4503e76` restores the original dependency pin and adds guarded current publication writes. Root GPL, signing key, data tables and all prior goldens remain unchanged.

Potential regressions: new About/translation-fallback wording and license navigation; intentional disabling of inherited news/update services on all platforms. Translation identity fixes use English fallback text where a reviewed translation is unavailable. Game/save/scoring logic and numeric tables were not changed. Copyright-only edits are broad but preserve upstream lines. Existing rejected gameplay candidates remain rejected; this audit does not fix their API36 Back failures.

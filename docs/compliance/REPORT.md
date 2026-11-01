# APD GPLv3 compliance audit — 2026-11-01

**NOT DONE: APD is not established as fully GPL-compliant.** This PR supplies fixes and evidence, not a legal certification. Known signing/source-access, dependency and asset issues remain. No new APK or source archive was released.

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
| NOT DONE | Universal migration/enforcement across legacy publication paths and historical source-to-binary recovery are not complete. The new gate is not claimed to have been wired into every old publisher or store pipeline. Required-check/release-permission enforcement is NEEDS CHRIS. |

## 4. Secrets — findings only

| Status | Item and evidence |
|---|---|
| DONE | Pinned gitleaks8.30.1 archive verified with published checksum `551f6fc83ea457d62a0d98237cbad105af8d557003051f41f3e7ca7b3f2470eb`; full reachable history scanned with `--all --full-history`, plus working-tree scan and supplemental file/config inspection. `secrets-redacted.json` records10628 reachable commits, candidate metadata and scope. Raw output stays outside the repository in `/workspace/gpl-audit-private`. |
| NEEDS CHRIS | Confirmed tracked `keystore/apd-debug.jks` and hard-coded `storePassword`/`keyPassword` in `android/build.gradle`, introduced in `56be46ab9`. Values are intentionally omitted here. No key, password or history was removed/rotated. Preserve install compatibility and assess existing exposure before choosing remediation. |
| DONE | Automated history candidates were inspected as Git/checksum assertions, test names, key-binding identifiers and a sound resource name, not confirmed API credentials. Supplemental inspection caught signing material the generic scanner missed. No `google-services.json` path was found in fetched reachable history. This is not a guarantee that no other secret exists; unreachable server objects, Actions logs/secrets and external uploads were not audited. |

## 5. Dependencies and assets

| Status | Item and evidence |
|---|---|
| DONE | `dependencies.json` enumerates43 direct external Gradle declarations, including native classifiers/test/build tooling, versions, POM URLs, declared license metadata and hashes where retrieval succeeds. Local project dependencies remain inherited GPL source modules. |
| NEEDS CHRIS | Runtime `org.json:json:20170516` explicitly declares the JSON License with “Good, not Evil”, a GPL-incompatible use restriction. It occurs in `SPD-classes/build.gradle` and core tests. No silent dependency upgrade was made; choose a compatible replacement and verify historical save/statistics serialization. |
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
| NOT DONE | Historical SPD reference changelogs still contain attributed original developer text and icons; full multilingual/manual review of every user-visible string is incomplete. Ordinary lowercase “shattered” item descriptions are not automatically renamed. The factual About attribution URL is retained. |
| DONE | `PLAY-LISTING-DRAFT.md` supplies an unofficial-fork/non-endorsement/credits/source-link draft, clearly unsubmitted with an accessible-source placeholder. |
| NEEDS CHRIS | Repository visibility, any history rewrite and any game rename remain unchanged. Review “Pixel Dungeon” title/store impersonation risk, original branding assets and final listing. No author contact or store submission was performed. |

## Verification

NOT DONE: final source/compile/test evidence is pending when this initial report is committed. The source-only GPL workflow runs compliance regressions, the complete core suite and Android debug/release plus desktop compilation without assembling APKs. Its actual result will be appended; failed checks will not be called passed.

Potential regressions: new About/translation-fallback wording and license navigation; intentional disabling of inherited news/update services on all platforms. Translation identity fixes use English fallback text where a reviewed translation is unavailable. Game/save/scoring logic and numeric tables were not changed. Copyright-only edits are broad but preserve upstream lines. Existing rejected gameplay candidates remain rejected; this audit does not fix their API36 Back failures.

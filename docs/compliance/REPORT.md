# APD GPLv3 compliance audit and fixes — November 1, 2026

**Technical fixes and matching TEST341 source packages are implemented. Full distribution compliance is NOT DONE.** Administrator signing-secret setup, external-recipient source delivery, inherited license exceptions and native acceptance remain explicitly unresolved. This report is evidence of work, not a legal certification. No repository visibility, game name, main history, installed data or original signing identity was changed.

Private PR: https://github.com/APD-Development/ShieldingShouldHaveWorked/pull/3

Audited approved head: `377c3e20b7dc6ec6c4d149c5c1b4ea7cda3bb7ed`; actual SPD v4.0.0 baseline: `2bb34a4e91d29c8785a9363cad6ddfe5122b1d4f`. APD's first Git modification is `56be46ab9` (2026-10-05). The isolated audit clone preserves Claude's branch and the pre-existing dirty development checkout.

DONE below means implemented in the **unmerged PR**, or a verified historical/delivery fact. It does not mean the new code is in the unchanged distributed APK341.

## License and copyright notices

| Status | Evidence |
|---|---|
| DONE | Root `LICENSE.txt` is byte-identical to the audited original: SHA256 `d0495053051967ebe76fb1facd287d79d1ed800da1be75cf501a556bc39a0472`. Complete normative GPLv3 terms are bundled verbatim as `core/src/main/assets/legal/GPL-3.0.txt`. |
| DONE | Original Oleg/Evan copyright lines are preserved, tested against the audited head. Chris Charlton/APD notices use years derived from real Git history (2026) and are below existing notices in comment-capable source/config. Generator `tools/apd_copyright_headers.py`; `header-years.json` and `file-notices.json` give per-file evidence. Implementation begins `4440fed63`, continued in `fc2b8f191` and `8c54fb2cf`. |
| NOT DONE | Literal headers inside every binary/JSON/TSV/document payload would damage formats or falsely relabel third-party material. Such files instead have external per-file modification notices in `file-notices.json`; numeric tables, historical data and upstream authorship are preserved. No legal text is inserted into recorded player data. |
| DONE | Root `NOTICE` and bundled `legal/NOTICE.txt` identify APD/Chris, SPD/Evan and Pixel Dungeon/Oleg, GPL3-or-later, no warranty and no affiliation/endorsement. |

## About and offline notices

| Status | Evidence |
|---|---|
| DONE | `scenes/AboutScene.java`, `apd/GplLicense.java`, and `windows/WndGplLicense.java` provide credits, source URL, GPL/no-warranty statement and bounded native pages containing the full offline GPL. Original contributor, artist, translator and sound credits remain. Three `GplLicenseTest` cases verify complete reconstruction, bounds and notice content. |
| DONE | Font metadata independently declares Apache2 for both `pixel_font.ttf` and desktop `droid_sans.ttf`. `font-licenses.json` records actual embedded notices/hashes. Full Apache2 text and copyright notices are bundled in app assets and beside the desktop font. Font bytes are unchanged. |
| NOT DONE | Physical-phone/offline GUI navigation and final branding acceptance have not been run for this PR. JVM tests/platform compilation are not physical-phone evidence. |
| NEEDS CHRIS | The actual source URL is private and explicitly labelled so. People receiving APKs outside the repository must also receive the source archive without needing private repository membership. |

## Exact distributed source and reconstruction

| Status | Evidence |
|---|---|
| DONE | Latest published binaries are TEST341. Existing tag `apd-alpha-preview-341` correctly resolves to producer `acb69c653033ee0d8df68264395ca27e08eb566d`; no tag was moved. `builds.json` records versionCode920, developer `4.0.8-apd-INDEV-bC341-TEST`, tester `4.0.8-apd-INDEV-bP341`, exact recipes and APK hashes. Rejected342/344 and uncompiled343 remain unpublished. |
| DONE | Both complete key-free matching-source ZIPs and checksums are attached to existing private release428527455, preserving all existing assets. `source341-delivery.json` records real asset IDs, publication timestamps and direct links. Both archives were downloaded back and compared byte-for-byte with the locally verified archives. The existing release body now puts matching source beside each APK. |
| DONE | `tools/apd_corresponding_source.py` packages the pinned real source, restores all366 generated history TSVs and exact runtime assets from checksum-pinned APKs, supplies the developer feature-generation input and build recipe, excludes Git/key history, and sanitizes signing-only credentials/configuration with every substitution recorded. `apd_publish_source_archive.py` is restricted to these source ZIP/checksum names, verifies every entry, forbids APK/key uploads, requires reviewed scan evidence, preserves existing assets and checks the existing release is private. Commit `a3a298f0b`. |
| DONE | Clean tester-source reconstruction succeeded in run41270469863/job124742017471. **All970 application entries are identical**: DEX, resources, assets, native libraries and manifest included; zero missing/extra/different entries. Only cryptographic signatures/signing block and ZIP metadata are excluded. No signed-byte equivalence is claimed. Evidence artifact13068684211; `source341-delivery.json`. |
| NOT DONE | Developer variant was not separately rebuilt. Its source/generated input/checksums are verified; do not describe it as independently reproduced. Older historical distributions have not all been reconstructed or individually source-packaged. |
| NEEDS CHRIS | Relay the matching archives beside APKs already sent through external tester channels; actual recipient access is not established by private links. No Discord/tester message or public upload was sent by this audit. |
| DONE | `RELEASING.md` requires exact version/source tags, generated inputs, adjacent binary/source links, archive hashes and recipient access. `apd_gpl_release_gate.py` fails closed with unresolved blockers/missing exact manifests. `publication-guards.json` records25 current publication paths/30 guarded writes/actions, implemented in `8b4503e76`. Source-only historical recovery does not permit new APK publication. |
| NEEDS CHRIS | Merge/repository required-check and release-permission policy. A source patch cannot stop historical workflow reruns or manual API/UI writes. No branch protections or main branch were changed. |

## Secrets and preserved signing identity

| Status | Evidence |
|---|---|
| DONE | Pinned gitleaks8.30.1, archive verified against published SHA256 `551f6fc83ea457d62a0d98237cbad105af8d557003051f41f3e7ca7b3f2470eb`, scanned working tree and full fetched reachable history (`--all --full-history`), supplemented by filename/properties inspection. `secrets-redacted.json` records scope, counts and redacted locations. Raw scan files stay outside source packages/repository. Scanner exit1 denotes findings, not a clean scan. |
| DONE | Confirmed original committed JKS and plaintext Gradle password exposure is reported. No values are in this report. The current PR removes the key/passwords from the tree, preserves the exact key privately outside the checkout, changes Gradle to external credentials and prevents fallback key creation. Certificate remains `067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e`; it was independently checked against the preserved key. App ID remains `com.charltonchris29.adhdpixeldungeon.indev`. Commit `fc2b8f191`; `docs/SIGNING.md`. |
| DONE | `apd_prepare_signing.py` and `.github/actions/apd-signing/action.yml` restore only the established certificate from encrypted secrets. `signing-integration.json` records42 wired workflow jobs. Compile-only and explicitly unsigned reconstruction need no private key. No key rotation, uninstall, save reset or destructive migration occurred. |
| NEEDS CHRIS | **Actual blocker:** `gh secret list --repo APD-Development/ShieldingShouldHaveWorked --json name` returned **HTTP403 Resource not accessible by integration**, repository Actions-secrets endpoint. No secret values were installed/overwritten. A repository administrator must configure the existing key as `APD_SIGNING_KEYSTORE_BASE64` and `APD_SIGNING_STORE_PASSWORD` (optional key-password/alias secrets). Signed CI builds remain blocked until then. |
| NEEDS CHRIS | Historical key exposure remains in unchanged private Git history. Assess exposure; do not publish that history. No history rewrite or visibility change was performed. |
| DONE | Latest completed full-history scan found11 generic candidates; working tree6; inspected as build hashes/assertions, keyboard-binding identifiers and a sound filename, not confirmed API tokens. Supplemental inspection caught the genuine signing material missed by generic scanning. No `google-services.json` path was found in reachable history. This does not cover unreachable server objects, Actions secrets/logs, or every external upload. |
| DONE | Both source archives were separately scanned:96 candidates, all reviewed as SHA256 integrity fields or the same known non-secret identifiers/assertions. `source341-scan.json` contains redacted findings/dispositions. Supplemental checks reject key paths and ensure historical password strings are absent from every archive entry. |

## Dependencies and assets

| Status | Evidence |
|---|---|
| DONE | `dependencies.json` lists43 direct Gradle external declarations, coordinates/versions/classifiers/scopes and POM-license evidence/hashes. Test/build tools are distinguished from production libraries. No proprietary billing/analytics SDK or GPL2-only direct dependency was identified. |
| DONE | **Actual incompatible dependency fixed:** original `org.json:json:20170516` declares the “Good, not Evil” restriction. Both selected pins now use Public Domain20240303. Verified replacement `4d86c4898` passed2239 methods/427 suites with zero failures/errors/skips, Android debug/release Java and desktop compilation, and an ABI scan of3827 production classes/35 distinct JSON references with zero float-method descriptors. The GPL-incompatible old implementation is not bundled in this PR. |
| DONE | Root cause of initial replacement failures: modern parser uses BigDecimal, changing synthetic canonical spelling, and modern JSON adds float overloads absent on Android. `Bundle.java` explicitly widens float writes to the original double overload, preserving old serialized precision and runtime ABI. Test-only canonicalization normalizes numeric spelling only when exact decimal roundtrip is proven; it rejects excess precision/overflow and does not narrow integers. Five JSON compatibility cases cover real legacy JSON/gzip reads, writes, float serialization,64-bit integers and precision-loss rejection. **Original golden hashes were not rewritten.** |
| NOT DONE | Existing unchanged TEST341 binaries still contain old incompatible JSON. Source delivery does not retroactively fix their dependency. Do not describe those historical APKs as compliant or continue distributing them as a corrected build. A new tested signed build is required after the remaining release blockers are resolved. |
| DONE | `assets.json` retains the360-asset audited baseline and seven original APD differences. Its current overlay points to original replacement generator/hashes and verified font metadata. `branding-assets.json` covers all replacement platform output files. The APD Gathering glyph is isolated to x192..206,y304..317 in `sprites/items.png`, introduced by real commit `168e49778`; all other item pixels remain inherited. |
| NOT DONE | Complete per-component native/transitive license/notice mapping and exact inherited CC-BY sample versions/file mapping are not established by direct POMs or the code license alone. About's original named authors/titles and CC-BY/CC0 categories are preserved. Unknown exceptions are flagged rather than invented. Resolve these before calling every distributed asset fully cleared. No known-good audio or legitimate attribution was deleted to hide uncertainty. |

License-policy evidence: GNU's [license list](https://www.gnu.org/licenses/license-list.en.html) flags the original JSON restriction; Apache's [GPL compatibility statement](https://apache.org/licenses/GPL-compatibility.html) confirms Apache2/GPLv3 compatibility. These policy sources do not establish ownership of individual assets.

## Branding, hooks and store draft

| Status | Evidence |
|---|---|
| DONE | All Android main/debug launcher PNGs/adaptive/monochrome, desktop PNG/ICO/ICNS and iOS AppIcons/banners now use original APD artwork. `tools/build_apd_branding.py` includes elementary editable glyph geometry and deterministic generation; no external font/logo/remote artwork is used. Original APD title/backdrop/button/flame assets replace uncertain earlier branding; four stock SPD title layers are deleted and `TitleBackground.java` uses the APD backdrop. Commits `fc2b8f191` and `e007a0d63`. |
| DONE | Packed stock logos in banners/icons/change_icons are replaced only within declared branding cells. Four automated artwork tests check every output hash/dimension, iOS opaque icon requirement, Android adaptive safe area, deleted old title layers, and exact unchanged pixels outside reserved atlas rectangles. The menu frame transparent separator and unused lower swatch contract are preserved; a discovered violation was corrected without weakening its regression. |
| DONE | `remaining-branding.json` classifies proper-name Java literals; historical upstream notes now explicitly credit Evan/Oleg and are labelled reference history, not APD announcements. Translator handles and persisted base-game/ruleset identifiers remain for legitimate attribution/compatibility. Two remaining Greek daily-game title references were changed to APD. Ordinary shattered-pot/brew descriptions are not branding. Commit `8c54fb2cf`. |
| DONE | Upstream news/update implementations are inert and all platforms select disabled no-network services; debug stubs no longer fabricate news/updates. News fallback goes to actual APD repository; only factual About attribution retains ShatteredPixel.com. Existing supporter scene redirects to About, boss donation promotion is absent, translations link is neutral attribution. No unfinished APD server or purchase flow was activated. |
| DONE | `PLAY-LISTING-DRAFT.md` is an unsubmitted unofficial-fork/source/credits/non-endorsement draft. |
| NEEDS CHRIS | Review “Pixel Dungeon” title/store impersonation risk and final listing/art. No game rename, public store release, author contact, repository visibility change or Git-history rewrite was performed. |

## Verification, failures and remaining acceptance

- DONE:15 local tool regressions (8 compliance/publication,4 artwork,3 source-security recovery). Three offline-license JVM cases and five JSON compatibility cases are included in full core verification.
- DONE: `4d86c4898`, run41268811315/job124737125611:2239 tests/427 suites, zero failures/errors/skips; Android debug/release and desktop compilation; JSON ABI guard. XML artifact13068114677 was downloaded and independently parsed, ZIP SHA256 `a92d6c78644783daea6921de439fa8e2aed0b4b51ad9b93786fd54398ec5de4e`.
- DONE: `e007a0d63`, run41270517705: latest complete artwork/platform verification passed. Final attribution revision `8c54fb2cf` also **passed** run41270753736/job124742851521:2239 tests/427 suites, zero failures/errors/skips,15 tool regressions, both Android Java variants/desktop compilation, and3826 production classes/35 distinct JSON references with zero float descriptors. Artifact13068809125 was independently downloaded/XML-parsed, SHA256 `cfc940a034270c49606c6d0ef782bc67cb802d5c72b1ef66e8c1a803c33a254e`; `final-verification.json`.
- DONE: tester historical unsigned reconstruction, run41270469863:970 identical application entries. This is one verification APK, not a new playable release; count it toward the three-APK daily cap. No new developer/player release was manufactured.
- Retained failures:72c8bfb37 legal header changed a whole-source layout hash;6854bd37a permits only the exact copyright notice while preserving expected code/layout hashes. JSON attempts62890a2be and824b1e3aa failed two original golden comparisons before the numeric/float-overload cause was resolved. Branding run41270257276 failed1/2239 on the menu atlas unused opaque swatch; corrected in e007a0d63. No original golden was rewritten or failing test skipped.
- Actual failed operations: local direct source upload HTTP401; normal repository Actions source upload then succeeded. CLI artifact download redirected to a forbidden blob; the official GitHub connector retrieved the2239-test XML successfully. Actions secrets HTTP403 remains unresolved. These are different operations, not an inferred blanket permissions failure.
- NOT DONE: native/emulator and physical-phone acceptance of this PR's new About/title controls; separately rebuilt developer341; all historical old-binary source recovery; full inherited sample/native exception review. No existing TEST341 emulator evidence is relabelled as new-PR evidence.

Potential regressions are confined to new About/legal navigation, title artwork/backdrop/launcher presentation, intentional disabled inherited network hooks, English identity fallbacks, and the JSON compatibility change covered above. Scoring values,18 slider mechanics, all357 multipliers, class availability, saved runs/statistics and gameplay timing are unchanged. Signing must use the preserved certificate; missing secrets fail explicitly rather than substituting a key.

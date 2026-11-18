# APD Google Play release (Nov 18, 2026)

Package `com.charltonchris29.adhdpixeldungeon`. Public launch Jan 1, 2027. Personal account: a 12-tester closed test
must run 14 straight days before production, so the closed test must start by Nov 23.

## Decisions (Chris, Nov 18)
- **New key for Play, not the old alpha key.** Alpha builds install as `...adhdpixeldungeon.indev`, a separate app,
  so no key choice lets Play updates replace them. The old key's password is in this repo and the source has to go
  public under GPL, so it cannot protect a store app.
- Play App Signing: Google generates and keeps the app signing key (Play Console default for new apps). Our new key
  is only the **upload key**. If it is ever lost, Google can reset it; the app signing key itself can't be lost.
- Daily alpha APKs keep the old key so testers keep updating over their installs.

## Build
`.github/workflows/apd-play-release.yml` runs on every push to `release/play-closed-test` that touches the Android
build. It builds the `.aab` and a release APK, then installs that APK on API 34 and API 36 emulators and fails if the
game crashes, isn't running after 60 seconds, or shows a blank screen.

Verified on run 44318319106: versionCode 921, versionName 4.0.9-apd, minSdk 26, targetSdk 36 (Google Play has
required API 36 for new apps and updates since Aug 31, 2026), launched with no crash on both emulators.

## Secrets (repo Settings > Secrets and variables > Actions)
Never commit any of these. The keystore files live outside the repo.

| Secret | What |
|---|---|
| `APD_UPLOAD_KEYSTORE_BASE64` | new upload keystore, base64 |
| `APD_UPLOAD_STORE_PASSWORD` | its password |
| `APD_UPLOAD_KEY_ALIAS` | `apd-upload` |
| `APD_UPLOAD_KEY_PASSWORD` | same as the store password |
| `APD_DEBUG_KEYSTORE_BASE64` | old alpha keystore, base64 |
| `APD_DEBUG_STORE_PASSWORD` | old alpha keystore password |

Without the upload secrets the `.aab` is built unsigned and Play will refuse it.

## Git history
`keystore/apd-debug.jks` and its password are in this repo's history. Rewriting history means force-pushing and
breaking every commit link in the docs, so instead: publish the GPL source as a **new public repo** with one clean
starting commit and no keystore, and keep this repo private. Once the debug secrets exist, the keystore file is deleted
from this repo going forward. The old key only signs alpha `.indev` builds, so its exposure can't touch the Play app.

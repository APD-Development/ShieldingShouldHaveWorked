# APD signing key

APD debug builds are signed with `keystore/apd-debug.jks` (alias `apd`, password `apdpassword`),
configured in `android/build.gradle`. It is committed to this repo because GitHub secrets could
not be written when the project was set up, and a stable key is needed so updates install over
older builds.

**This key is public to anyone who can read this repo. Before the repo ever goes public:**

1. Generate a new keystore and keep it out of git (store it as GitHub Actions secrets).
2. Change `android/build.gradle` and the workflow to read the key from secrets.
3. Remove `keystore/apd-debug.jks` from the entire git history (e.g. `git filter-repo --path keystore --invert-paths`).
4. Treat the old key as compromised. Builds signed with the new key will not install over builds signed with this one; users must uninstall first.

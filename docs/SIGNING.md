# APD signing identity — preserve installed player data

Use the existing APD key outside the checkout. Never generate or substitute a key, change the application ID, uninstall APD, or clear data to solve signing errors.

Expected certificate SHA-256: `067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e`.

Local builds require `APD_SIGNING_KEYSTORE` (absolute external path), `APD_SIGNING_STORE_PASSWORD`, and optionally `APD_SIGNING_KEY_PASSWORD` and `APD_SIGNING_KEY_ALIAS` (default `apd`). Keep credentials outside shell history and source archives. Compile-only checks need no signing credentials. `-PapdUnsignedVerification` produces an unsigned, non-distributable reconstruction for comparison; it is not an installable owner update.

GitHub Actions use `.github/actions/apd-signing` to restore the same key into a private temporary directory and verify its certificate before building. A repository administrator must configure encrypted Actions secrets `APD_SIGNING_KEYSTORE_BASE64`, `APD_SIGNING_STORE_PASSWORD`, and optional key password/alias, using the existing key. Do not replace existing secret values without checking their identity.

This session's `gh secret list` failed with HTTP 403, Resource not accessible by integration, at the repository Actions secrets endpoint. No secrets were installed or overwritten. Signed CI builds remain blocked until an administrator configures them. The preserved local key was independently certificate-checked.

The key is removed from this PR's current tree, not from history. Historical exposure remains a security finding. Do not publish Git history containing it. Share only scanned, key-free corresponding-source archives. No history rewrite, rotation, repository visibility change or destructive migration is authorized here.

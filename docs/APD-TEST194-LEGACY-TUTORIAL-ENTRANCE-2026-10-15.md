# TEST194 — corrected legacy tutorial entrance repair regression

Prepared: 2026-10-15T05:44:52Z

TEST193 run 38688177613 executed 1,609 tests and failed only because the synthetic level omitted the normal trap collection required by `Level.set`. The corrected fixture initializes its trap and blob collections before recreating the legacy hidden entrance door. Production application source and assertions are unchanged.

Expected: 1,609 core tests, zero failures/errors/skips. The test must expose only the entrance-boundary `SECRET_DOOR`, rebuild its passable/secret flags, preserve an unrelated secret door, and preserve an ordinary entrance wall.

This remains core-only verification. No APK is rebuilt because application source is still exact verified TEST192 source `222189f971dc55597f561eb87aadcd4e34b47a2e`. TEST123 remains the last owner-delivered/signature-compared APK.

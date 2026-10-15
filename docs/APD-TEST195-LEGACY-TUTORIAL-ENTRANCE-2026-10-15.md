# TEST195 — legacy tutorial entrance repair regression

Prepared: 2026-10-15T05:49:32Z

TEST193 omitted normal level collections. TEST194 initialized them but put the synthetic room one cell from the map border, violating the generated-level margin assumed by `updateOpenSpace`. Both failures occurred in setup before the repair assertions and are retained.

TEST195 uses the same production code and assertions with a production-realistic interior room margin. It must expose only the entrance-boundary `SECRET_DOOR`, rebuild its passable/secret flags, preserve an unrelated secret door, and preserve an ordinary entrance wall.

Expected: 1,609 core tests, zero failures/errors/skips. No APK rebuild: production application source remains exact verified TEST192 source `222189f971dc55597f561eb87aadcd4e34b47a2e`. TEST123 remains the last owner-delivered/signature-compared APK.

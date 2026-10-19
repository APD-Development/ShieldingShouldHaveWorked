# Hotbar persistence and APD Settings acceptance

Status: implementation checkpoint, verification pending.

Confirmed source defect: Dungeon.loadGame restored saved quickslots and then reapplied the Waterskin startup preference, and the preference picker also rearranged the active game. The preference must affect new runs only. Serialization already stores item quickslotpos and placeholders; retain those existing formats, capacities and player preferences. Remove load-time/picker reapplication; keep HeroClass initialization.

Do not claim the exact same-preference screenshot pattern is reproduced until the cold-process baseline is examined. Test the exact Waterskin2/spikes5 configuration separately from conflicting-preference and Automatic scenarios.

Settings: six focused categories using the same shared pane, compact gray pixel-art controls, readable descriptions, explicit immediate/new-run/restart timing, responsive scroll host, category Back and scene restoration. Retain every old setting; add convenient links to existing hotbar layouts/customization. Recording diagnostics and storage are lazily read in Advanced. No defaults or preferences intentionally changed.

Regression gates: real save files and fresh models/preferences, all five layouts and custom item types, empty slots/placeholders; actual Android category/navigation/control touches, compact screen, recreation; force-stop/restart save restoration on exact APKs. Preserve all previous core/native tests and UIMSD outcomes/history/viewport/modal fixes.

Pending: full core, native cold-process before/after proof, screenshot review, exact paired APKs and signing/history comparison. No physical-device claim, publication or tester distribution.

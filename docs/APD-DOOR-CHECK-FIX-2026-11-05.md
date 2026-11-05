# Door-check eligibility correction — November 5, 2026

Owner report: TEST362 shows checkmarks on a bookshelf-lined room where those tiles cannot be hidden doors. Floor1 and Floor10 also must not suggest searching for generated hidden doors.

## Actual root causes

The old filter reused the renderer's broad wall category, which includes bookshelves, and never checked the candidate tile's own type. It treated arbitrary solid furniture as a door frame, and treated every non-stone tile as potential backing. It had no public floor/generator eligibility gate. Its old tests explicitly accepted bookshelf backing and exercised positive searched-wall screenshots on Floor1, so they failed to cover the owner's intended rule.

## Code correction

`DoorSearchEligibility` is the shared public-rule policy for marker eligibility, cell information, explicit wall-target selection and completed-check presentation. Only wall, decorated wall and their indistinguishable secret-door disguise can be candidates. Main-dungeon floors1,10,15,20,25,26 do not offer door checks. Scripted prison/caves/city/halls boss maps, final/dead-end maps, vault and mines are excluded by generator type too. Mining concealed connections are painted as diggable stone, not searchable secret-door tiles. Floor5 remains eligible, preserving Rat King searching and its separate optional Quick Search.

Observed geometry must admit a doorway orientation: structural side frames and potentially open backing on opposite sides. Visible furniture, blocked backing, pillars, corners and map borders rule the tile out. Undiscovered neighbors remain unknown; their actual terrain, room identities/connections and whether a secret actually exists are never used to prune markers. The runtime rule does not scan hidden-door locations or count remaining secrets.

New bookshelves never receive saved door marks. Existing saved marks are filtered at rendering/inspection time without erasing historical search memory. Existing unsupported-floor marks likewise stay hidden after loading/revisiting. Normal Search, trap discovery, hunger, turns, RNG, generation, inventory, statistics and signing identity are unchanged. Explicit impossible door targets are refused without spending a turn; general Search still works normally.

## Verification

Local full suite:2495 tests441 suites,0 failures/errors/skips. Eighteen added regressions cover all terrain types, all excluded floors, branch/generator rules, legacy raw marks, corners/pillars/backing/frames, fog invariance, map/RNG immutability and ordinary turn costs. A real generation observer checks78 floors across three complete seeded dungeons: excluded floors contain no secret-door tiles, and every real generated secret door remains eligible under fully observed geometry. Normal valid wall faces, southern overhangs, side faces, saved colors and guaranteed-versus-uncertain search evidence remain covered.

Native fixtures retain all existing cases. They now assert zero marks on actual Floor1 with injected old saved marks, descend through the real transition to Floor2, verify an injected bookshelf mark is absent, then run the original positive stone/overhang/color/save/resume checks. Both new negative examples are captured. Native/Android compilation and APK acceptance are pending at this report's source checkpoint; no physical-phone reproduction or acceptance is claimed.

## Delivery boundary

TEST362 remains the last actually released pair and still contains this bug. Preserve its private release/direct assets and all prior source/owner features, including the latest exact-occurrence timeline check (72-case Android inventory). No new APK or build number has been requested by this source-only check. One intentional replacement pair may follow successful source verification; no duplicate builds for documents or test-only corrections. Never call a failed or untested candidate verified.

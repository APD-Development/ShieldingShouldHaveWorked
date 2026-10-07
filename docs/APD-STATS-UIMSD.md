# APD stats: the query engine and the generic UIMSD screen (Phase 7)

UIMSD (Ultimate Infinite Mega Statistics Database) shows what the raw telemetry holds. It adds **no new telemetry** (telemetry stays at 1.5):
everything on screen is built from the raw logs, can be rebuilt, and is never a second source of truth.

## Opening it
The **UIMSD button on the title screen** is the primary entry point. "Open UIMSD (preview)" in APD Settings (title screen or pause menu) also
opens it. Only the title button's click target changed; the title layout, text, arrow and annotation are untouched and a test pins the
title files. Back returns to where UIMSD was opened from.

## Layers (all pure Java except the last two)
| layer | what | where |
|---|---|---|
| schema | `DatasetDef`, `ColumnDef` (type, format, PUBLIC/INTERNAL, merge rule, since-telemetry, words for enums), `LinkDef` | `apd/query` |
| rows | compact `Row` (long/String/provenance byte per cell; unknown is not set, never 0) | `apd/query` |
| builders | one raw log -> rows of every dataset (`RowBuilders`), per-log binary row cache (`RowCodec`, `DatasetStore`) | `apd/query` |
| assembly | merge the segments of a run (`RowMerger`), link imported ranking records to recorded runs, rulesets, runs-covered | `DatasetStore` |
| query | `Query` (immutable, text form `QueryCodec`), `QueryEngine`, `QueryResult` | `apd/query` |
| presentation | `ResultTable`, `StatusText`, `CellFormatter`, `TableGeometry`, `UimsdModel` (the whole screen minus pixels) | `apd/query` |
| screen | `UimsdScene`, `UimsdTable` (frozen first column + sticky header), `WndUimsd*` dialogs | game code |

The engine, model, table and dialogs know no particular dataset: a source-scan test fails if they mention a shipped dataset or column.
A new dataset = a `DatasetDef` + a row builder + (optionally) views, with no screen change. A saved query is a `Query` written with `QueryCodec`.

## Datasets and views
Datasets: **Runs**, **Items**, **Enemies**, **Deaths**. Views (a view is a dataset plus a starting query, it stores nothing): Runs, Items,
Weapons (melee + thrown), Armor, Rings, Trinkets, Artifacts (item category filters), Enemies, Deaths, **Characters** (runs grouped by class),
**Challenges** (runs grouped by challenges + exotic chance, which together are exactly what makes a ruleset, over all rulesets).
Bosses are not included: the boss flag is not recorded. Links move between datasets ("Items in this run" ...) from the dataset definition.

## Hidden information (the strict rule)
Every column and every row is PUBLIC or INTERNAL (explicit in the definitions).
- A PUBLIC cell is built only from PUBLIC events. An INTERNAL cell comes from hidden truth (true item class and level, generation placement,
  true enemy class, seed, how many items a run contains, how many events were rolled back ...).
- A row that exists only because of hidden events (an item or enemy the player never met) is not a public row.
- INTERNAL cells and rows are shown only when the player turned hidden information on **and** the row's run is permanently finished (its
  newest log segment ended). For a run that is not finished the engine masks them whatever the query says: they cannot be shown, filtered,
  sorted, grouped or counted, so they cannot be inferred either; hidden-only rows of an unfinished run are dropped without a trace (not even
  counted). Rows of finished runs that stay out because hidden information is off are counted.
- Row order and public numbers never depend on hidden events (rows are listed in the order the player met them; "turns equipped" ends at the
  last PUBLIC event). A differential test on rows built from real logs compares hundreds of queries between two histories that differ only in
  an unfinished run's hidden information and requires identical output.
- A run whose log was left open (the app was killed) counts as unfinished for ever; its hidden information stays hidden.

## Unknown, missing, lower bound, inferred
Unknown is never zero. Cells show `?` (unknown), `hidden`, `>=12` (only a lower bound: gaps in the log, or an older telemetry version could
not count it), `~6` (inferred), and blue for values from an imported ranking record. Filters never match an unknown value (except "is
unknown") and the status strip says how many rows that cost; a lower bound is compared as "somewhere from here upward" so only decidable
answers are given and the rest are reported as undecidable. Sorting puts unknown (and hidden) last in both directions.
Aggregates use only known values and say how many ("9 of 12 known"); a total of lower bounds is a lower bound; a rate leaves lower bounds out.

## Scope, sample size, coverage
Default scope: the current ruleset, finished runs only, no imported record that is also a recorded run. Everything left out is counted in the
status strip and can be included on purpose: other rulesets, rows with an unknown ruleset (never silently treated as the current one), runs
whose rules changed mid-run, unfinished runs, duplicate imported records. Group views have a minimum group size (default 5); smaller groups are
listed as hidden, not dropped. Headers show "known/total" per column; the status strip shows how many of the runs in scope can have rows at
all ("Rows come from 4 of 7 runs in scope; the others were recorded before this data existed").

## Segments of one run
A run recorded across several log segments (a new telemetry version starts a new log) is one row per run in every dataset. Columns combine by
their rule: sums add, "distinct" counts take the largest part, last/same take the right segment. If a segment could not record a value the total
is only a lower bound. The raw logs are untouched; merging happens when the datasets are assembled.

## Row cache (derived, rebuildable)
`derived/rows/<log>.apdr`, one per log, valid while the log has exactly the same size and the builder the same version; otherwise rebuilt from
the log. A new run or a run that went on costs one log. Deleting the folder loses nothing; deleting a raw log removes its rows.

## Dialog layout
UIMSD's dialogs (`WndUimsd*`) are sized by `WindowGeometry` from the screen they open on: width follows the screen with a margin on each side,
the list is as tall as its content up to 85% of the screen and scrolls beyond that, rows wrap their text and stack without gaps. A window
with a scroll pane is **resized before the pane is placed** (resizing moves the window's camera, and a pane works out its screen position
when it is placed; doing it the other way round left the Views list far to the right of its window in build 22). A test pins that order.

## Tests
Engine semantics, disclosure (including the differential leak test), merging, builders (each dataset), caches, model, geometry, UI wiring, a
device self-test, and performance (printed as APD-PERF lines).

## Limitations
- The screen cannot be exercised headless: its layout is covered by geometry tests, the drawing by the phone.
- Table cells are cut to the column width; the detail window shows everything. Column widths are estimated from text length.
- Paged at 50 lines; no charts, no export (Phase 8), no saved-query UI, no custom expression language, no global statistics.
- Group-by in the UI picks one column with generic aggregates; richer grouping is a `GroupBy` away but has no screen yet.
- Enemy rows pair each public combat event with the detail event written right after it; if telemetry ever interleaves more events there,
  the hidden class column of an enemy row may be missing (the public numbers do not depend on it).
- The first open after an update rebuilds the row caches (a short wait); the screen says "Reading your history...".

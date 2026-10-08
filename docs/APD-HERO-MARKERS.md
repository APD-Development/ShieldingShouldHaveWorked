# Hero-select completion markers: design proposals (nothing is built; pick one or none)

## What the data already gives (`CompletionMarks`, from genuine finished runs only)
Per class: runs, wins, and the highest difficulty (1-5) the player has won on. Nothing invented: a class never won shows no marker; wins recorded without a difficulty are counted separately and never guessed. Demo and imported-duplicate runs are excluded. Hidden information is not involved (all of this is the player's own finished result).

## Where it goes
The hero-select screen already shows one portrait per class with the class name under it. Markers sit on or under the portrait and must never cover the face, the name, or the touch area.

## Option 1: Difficulty pips (recommended)
Five tiny pips in a row under the class name, one per difficulty level. A pip is filled up to the highest difficulty won with that class, empty above it. A class never won shows five empty pips (or none, see below).
- Reads at a glance, uses exactly the data we have, and rewards a clear goal ("fill the row") without implying a points system.
- Cost: one small widget; the same pips can be reused in UIMSD's class table.

## Option 2: Win badge
A small star or laurel in a portrait corner once the class has been won at all; a number beside it for total wins if wanted ("3").
- Simplest and least cluttered; says less (no difficulty).

## Option 3: Frame tint
The portrait frame changes color by the highest difficulty won (plain, bronze, silver, gold, and so on).
- Very quiet and pretty; but color alone is hard to read and fails for color-blind players unless paired with a shape.

## Option 4: Text line
A tiny caption under the class name such as "Won ×3, best difficulty 4".
- Most precise, least pretty, widest.

## Rules for any option
- Shown only for classes with at least one genuine win (others show nothing, so the screen stays clean for a new player), except the pips which may show as empty for a class the player has run before.
- Never counts demo, unfinished, or duplicate runs. Never reveals anything about a run in progress.
- Accessible: shape or text, never color alone. Same look on every device size, no overlap with the difficulty picker above Start.
- Cleric: shown like any class once Cleric can be played (no special treatment, no joke on the marker itself).

## Recommendation
Option 1 (pips) with Option 2's star appearing beside the pips when the class has been won at all. It is the smallest design that shows both "have I won this?" and "how hard?", it needs no new data, and it stays quiet for new players. I will not build it until you choose, and it will wait behind the higher-priority work.

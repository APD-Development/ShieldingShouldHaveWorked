# Making a hero mod for APD

Status (Nov 19, 2026): the file format, validator, mods-folder scanner and gameplay hooks exist.
Hero select does not offer mods yet, and there is no Community workshop upload yet. This guide
describes what the current code accepts (`apd/mods/HeroMod.java`, `apd/mods/ModLibrary.java`).

## What a mod can and can't do
Mods are DATA and ART only. Google Play does not allow apps to run downloaded code, so a hero mod is
built entirely from pieces that already exist in APD: an existing class engine, existing talents,
existing armor abilities and existing items. If your idea needs new code (a new talent, ability,
item or rule), submit it to the developer; if approved it ships in an app update.

Mod-hero runs count only on your own phone: they appear in local history/rankings marked as Mod and
are never sent to Community (no leaderboards, tournament or run reviews). Mod runs never unlock the
normal badges.

## Folder layout
```
mods/
  my-hero/            any folder name
    hero.json         at most 16 KB, UTF-8
    sprite.png        at most 64 KB, a real PNG, exactly 256x128 (same layout as the built-in hero sprites)
```
A broken mod is listed with the reason it was refused; it never stops other mods from loading.
The folder and both files must be real files, not links (shortcuts); linked ones are refused.
Two installed mods may not share the same `id`.

## hero.json (format 1)
```json
{
  "format": 1,
  "id": "yourname.hero-name",
  "name": "Shown name",
  "author": "Your name",
  "description": "One paragraph shown on hero select.",
  "baseClass": "WARRIOR",
  "hp": 20,
  "str": 10,
  "startingItems": [ {"item": "weapon.melee.Sword", "level": 0, "quantity": 1} ],
  "talents": {
    "tier1": ["HEARTY_MEAL", "..."],
    "tier2": ["..."],
    "tier3": ["..."]
  },
  "armorAbilities": ["HeroicLeap"]
}
```

| Field | Rules |
|---|---|
| `format` | Must be `1`. |
| `id` | Required, up to 64 characters, only `a-z`, `0-9`, `.`, `_`, `-`, starting with a letter or digit. |
| `name`, `author` | Required, up to 32 characters each. |
| `description` | Optional, up to 600 characters. |
| `baseClass` | One of the game's hero classes (e.g. `WARRIOR`, `MAGE`, `ROGUE`, `HUNTRESS`, ...). Your hero runs on that class's engine and subclasses. |
| `hp` | Whole number 1-999: starting max HP. Normal per-level HP growth still applies. |
| `str` | Whole number 1-99: starting strength. |
| `startingItems` | Optional list, at most 12. `item` is the item's class path under `items` (e.g. `weapon.melee.Sword`, `potions.PotionOfHealing`); `level` 0-20 (default 0; must be 0 for items that can't be upgraded, such as potions or food); `quantity` 1-99 (default 1; a non-stackable item such as a weapon gives that many separate copies). Top-level items have no package part (`Gold`, `EnergyCrystal`, `Ankh`, `Torch`). The Amulet of Yendor (`Amulet`) is refused, and so are general item types that aren't a real item (`Item`, `potions.Potion`, `potions.exotic.ExoticPotion`, `rings.Ring`, `weapon.melee.MeleeWeapon`, `bags.Bag`, `artifacts.Artifact`): name a specific one. Items that only work when picked up in the dungeon are refused too: `Dewdrop`, `LostBackpack`, `TengusMask`, any `keys.*` or `journal.*` item, `quest.CorpseDust`, `quest.DwarfToken`, `quest.CeremonialCandle`. `Gold` and `EnergyCrystal` add that amount to the run's gold and alchemy energy. Given on top of the base class's normal starting kit. |
| `talents` | `tier1`, `tier2`, `tier3` lists of talent names (the game's internal names, upper case). Each tier may use talents from ANY class, but only talents of that same tier, no duplicates, and exactly as many as the base class has in that tier. |
| `armorAbilities` | 1 to 3 existing armor ability names (e.g. `HeroicLeap`), no duplicates. |

The numbers are sanity bounds only; balance is your call. Modders cannot change a talent's or
ability's own strength yet (that would need per-talent code changes).

## Saves
The mod's hero.json is stored inside the save, so a run keeps working if you edit or delete the mod
afterwards.

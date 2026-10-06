/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */


package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ItemEvents;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.EnergyCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Spell;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;

//Read-only facts about an Item, split into what the PLAYER can see and what is TRUE. The two are never mixed: the visible
//state only uses the game's own player-facing answers (name(), levelKnown, cursedKnown, visiblyUpgraded(), ...), so it can
//not leak an unidentified item's real nature. Nothing here changes an item or touches random numbers.
final class ItemFacts {

	private ItemFacts(){}

	//Items that are never part of a hero's possessions as objects, or are only placeholders: no identity for them.
	static boolean tracks( Item item ){
		if (item == null) return false;
		if (item.quantity() <= 0) return false; //quickslot placeholders and used-up stacks
		if (item instanceof Gold || item instanceof EnergyCrystal || item instanceof Dewdrop || item instanceof Key) return false;
		return true;
	}

	static int category( Item item ){
		if (item instanceof MissileWeapon) return ItemEvents.CAT_MISSILE;
		if (item instanceof Weapon || item instanceof MagesStaff) return ItemEvents.CAT_WEAPON;
		if (item instanceof Armor) return ItemEvents.CAT_ARMOR;
		if (item instanceof Ring) return ItemEvents.CAT_RING;
		if (item instanceof Artifact) return ItemEvents.CAT_ARTIFACT;
		if (item instanceof Wand) return ItemEvents.CAT_WAND;
		if (item instanceof Potion) return ItemEvents.CAT_POTION;
		if (item instanceof Scroll) return ItemEvents.CAT_SCROLL;
		if (item instanceof Plant.Seed) return ItemEvents.CAT_SEED;
		if (item instanceof Runestone) return ItemEvents.CAT_STONE;
		if (item instanceof Food) return ItemEvents.CAT_FOOD;
		if (item instanceof Bag) return ItemEvents.CAT_BAG;
		if (item instanceof Trinket) return ItemEvents.CAT_TRINKET;
		if (item instanceof Spell) return ItemEvents.CAT_SPELL;
		if (item instanceof Bomb) return ItemEvents.CAT_BOMB;
		return ItemEvents.CAT_OTHER;
	}

	//what the player can see right now
	static ItemEvents.VisibleState visible( Item item ){
		ItemEvents.VisibleState v = new ItemEvents.VisibleState();
		v.category = category(item);
		String name;
		try {
			name = item.name();
		} catch (Throwable t){
			name = "";
		}
		v.name = name == null ? "" : name;
		v.quantity = Math.max(0, item.quantity());
		v.levelKnown = item.levelKnown;
		v.cursedKnown = item.cursedKnown;
		v.visibleLevel = item.levelKnown ? item.visiblyUpgraded() : 0;
		v.visiblyCursed = item.cursedKnown && item.cursed;
		v.identified = item.isIdentified();
		return v;
	}

	//what is true, which may differ from what the player knows
	static final class Truth {
		String trueClass = "";
		int level;
		boolean cursed;
		String enchant = "";
		int quantity;

		boolean sameAs( Truth o ){
			return level == o.level && cursed == o.cursed && enchant.equals(o.enchant);
		}
	}

	static Truth truth( Item item ){
		Truth t = new Truth();
		t.trueClass = GameSnapshot.shortName(item.getClass());
		t.level = item.trueLevel();
		t.cursed = item.cursed;
		t.quantity = Math.max(0, item.quantity());
		if (item instanceof Weapon && ((Weapon) item).enchantment != null){
			t.enchant = ((Weapon) item).enchantment.getClass().getSimpleName();
		} else if (item instanceof Armor && ((Armor) item).glyph != null){
			t.enchant = ((Armor) item).glyph.getClass().getSimpleName();
		}
		return t;
	}
}

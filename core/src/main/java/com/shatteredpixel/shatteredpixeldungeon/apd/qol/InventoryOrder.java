/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * ADHD Pixel Dungeon
 * Copyright (C) 2026 Chris Charlton
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

package com.shatteredpixel.shatteredpixeldungeon.apd.qol;

import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.*;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;

/** Presentation order uses only publicly visible tier, upgrades and stack count. */
public final class InventoryOrder {
    private InventoryOrder(){}
    private static int tier(Item item){
        if(item instanceof MeleeWeapon)return ((MeleeWeapon)item).tier;
        if(item instanceof MissileWeapon)return ((MissileWeapon)item).tier;
        if(item instanceof Armor)return ((Armor)item).tier;
        return 0;
    }
    // An item's upgrade is public as soon as its level is known. Curse knowledge
    // is unrelated to equipment ordering and must not push visible +3 gear last.
    private static boolean gear(Item item){
        return item instanceof MeleeWeapon || item instanceof MissileWeapon
                || item instanceof Armor || item instanceof Wand
                || item instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring
                || item instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact
                || item instanceof com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
    }
    // An item's upgrade is public as soon as its level is known.
    private static boolean sortableKnowledge(Item item){
        return gear(item) ? item.levelKnown : item.isIdentified();
    }
    /** Chris, Nov 22-23: within a category: identified, then unidentified but known not cursed (blue),
     *  then unidentified with curse unknown (purple), then known-cursed (red) last.
     *  Only what the player already knows counts; a hidden curse is never read. */
    private static int knowledgeGroup(Item item){
        if (item.cursedKnown && item.cursed) return 3;
        if (sortableKnowledge(item)) return 0;
        return gear(item) && item.cursedKnown ? 1 : 2;
    }
    private static int knowledge(Item a,Item b){
        return Integer.compare(knowledgeGroup(a),knowledgeGroup(b));
    }
    private static int category(Item item){
        return Generator.Category.order(item)/100;
    }
    /** Tier always before level (Chris, Nov 22). Tier and item type are visible even when the level isn't. */
    private static int visible(Item a,Item b){
        int order=Integer.compare(tier(a),tier(b));
        if(order!=0)return order;
        if(a.levelKnown && b.levelKnown){
            order=Integer.compare(a.visiblyUpgraded(),b.visiblyUpgraded());
            if(order!=0)return order;
        }
        order=Integer.compare(Generator.Category.order(a),Generator.Category.order(b));
        if(order!=0)return order;
        order=a.name().compareTo(b.name());
        if(order!=0)return order;
        return Integer.compare(a.quantity(),b.quantity());
    }
    public static int compare(Item a,Item b){
        int group=Integer.compare(category(a),category(b));
        if(group!=0)return group;
        int order=knowledge(a,b);
        if(order!=0)return order;
        // Unidentified non-gear (potions, scrolls...) keep pickup order; gear sorts on what is visible.
        if(knowledgeGroup(a)==2 && !gear(a) && !gear(b))return 0;
        return visible(a,b);
    }
    public static int compareInBag(Bag bag,Item a,Item b){
        if(bag instanceof MagicalHolster){
            // Throwables, including bombs, before wands.
            int group=Integer.compare(a instanceof Wand?1:0,b instanceof Wand?1:0);
            if(group!=0)return group;
            if(a instanceof Wand && b instanceof Wand){
                int known=knowledge(a,b);
                if(known!=0)return known;
                return visible(a,b);
            }
            return compare(a,b);
        }
        if(bag instanceof VelvetPouch || bag instanceof ScrollHolder || bag instanceof PotionBandolier){
            // Seeds with seeds, stones with stones; never interleave by stack size.
            int group=Integer.compare(category(a),category(b));
            if(group!=0)return group;
            int known=knowledge(a,b);
            if(known!=0)return known;
            int stacks=Integer.compare(a.quantity(),b.quantity());
            if(stacks!=0)return stacks;
        }
        return compare(a,b);
    }
}

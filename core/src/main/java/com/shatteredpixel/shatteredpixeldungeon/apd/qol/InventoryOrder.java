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
    public static int compare(Item a,Item b){
        int categoryA=Generator.Category.order(a),categoryB=Generator.Category.order(b);
        int order=Integer.compare(categoryA/100,categoryB/100);
        if(order!=0)return order;
        order=knowledge(a,b);
        if(order!=0)return order;
        // Unknown items keep stable relative order. Never consult hidden level/type as a tie-breaker.
        if(!a.isIdentified())return 0;
        order=Integer.compare(categoryA,categoryB);
        if(order!=0)return order;
        order=Integer.compare(tier(a),tier(b));
        if(order!=0)return order;
        order=Integer.compare(a.visiblyUpgraded(),b.visiblyUpgraded());
        if(order!=0)return order;
        // Quantity is a tie-breaker in the backpack, never ahead of equipment tier/upgrade.
        return Integer.compare(a.quantity(),b.quantity());
    }
    private static int knowledge(Item a,Item b){
        return Boolean.compare(!a.isIdentified(),!b.isIdentified());
    }
    public static int compareInBag(Bag bag,Item a,Item b){
        if(bag instanceof MagicalHolster){
            // All thrown items, including bombs, stay before wands.
            int group=Integer.compare(a instanceof Wand?1:0,b instanceof Wand?1:0);
            if(group!=0)return group;
            if(a instanceof Wand && b instanceof Wand){
                int known=knowledge(a,b);
                if(known!=0)return known;
                if(!a.isIdentified())return 0;
                int level=Integer.compare(a.visiblyUpgraded(),b.visiblyUpgraded());
                return level!=0?level:Integer.compare(a.quantity(),b.quantity());
            }
            return compare(a,b);
        }
        if(bag instanceof VelvetPouch || bag instanceof ScrollHolder || bag instanceof PotionBandolier){
            int known=knowledge(a,b);
            if(known!=0)return known;
            int stacks=Integer.compare(a.quantity(),b.quantity());
            if(stacks!=0)return stacks;
        }
        return compare(a,b);
    }
}

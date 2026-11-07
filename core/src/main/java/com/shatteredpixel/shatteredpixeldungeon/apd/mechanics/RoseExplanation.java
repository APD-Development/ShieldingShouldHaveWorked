/*
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
package com.shatteredpixel.shatteredpixeldungeon.apd.mechanics;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;

/** Potential base ghost statistics; never locates a ghost or inspects hidden equipment. */
public final class RoseExplanation {
    private RoseExplanation() {}
    public static int maximumHealth(DriedRose rose){return rose.isIdentified()?40+10*rose.level():-1;}
    public static int unarmedMaximum(DriedRose rose){return rose.isIdentified()?rose.ghostStrength()-8:-1;}
    public static String summary(DriedRose rose){
        if(!rose.isIdentified())return "";
        return "Once the Ghost's quest is complete, an equipped, uncursed Rose with a full summoning meter can summon the ghost. At this Rose level, its base maximum health is "+maximumHealth(rose)+" HP and its equipment strength is "+rose.ghostStrength()+" points. While a ghost is present, the icon's percentage shows the ghost's health, not the summoning charge meter. Without a ghost, eligible recharge fills the summon meter; while it lives, eligible passive recharge heals it instead.";
    }
    public static String details(DriedRose rose,Hero hero){
        if(!rose.isIdentified())return "";
        return "_Ghost summon and base statistics_\n"+summary(rose)
            +" Summoning also requires no Magic Immunity and a valid unoccupied neighboring passable or avoidable tile. It takes 1 base game-time unit, empties the full "+rose.maximumCharges()+"-point summoning meter and breaks the hero's invisibility. It does not summon a second ghost while one is already associated with the Rose. Inspection does not test hidden tiles or find that ghost."
            +"\nBase maximum HP = 40 + 10 × Rose upgrade level = "+maximumHealth(rose)+" HP at the ghost's next statistics refresh. A fresh summon starts with that base health before later effects. Equipment strength = 13 + floor(Rose upgrade level / 2) = "+rose.ghostStrength()+" points. Strength gains 1 point every two Rose upgrades; it is not a damage percentage."
            +"\nWhen unarmed, the base damage roll is 1–"+unarmedMaximum(rose)+" damage points, biased toward the middle, before defenses and other applicable rules. The maximum is ghost strength − 8. With a weapon, its own damage and effects replace that unarmed roll; excess ghost strength can add a separate roll from zero to (ghost strength − weapon strength requirement). Hidden gear levels are not read to predict that amount here."
            +"\nBase attack accuracy = hero level + 9; base evasion = hero level + 4."
            +(hero==null?" Current personal scores are withheld without a loaded hero.":" At your current level these inputs are "+(hero.lvl+9)+" accuracy points and "+(hero.lvl+4)+" evasion points before equipment, surprise and status rules.")
            +" These are competing scores, not fixed hit/dodge percentages, and do not copy every modifier from the hero's own final scores. Rose upgrades do not directly enter these two base-score formulas."
            +"\nGhost equipment is limited to supported melee weapons and armor. Unique items and armor with an attached seal are rejected. Gear must be known uncursed and meet the ghost's strength; unknown upgrades must also pass the ordinary +0 strength check. Giving equipment transfers that actual item, not a free copy. Armor protection/glyphs and weapon defense/enchantments follow their own supported ghost rules; no hidden gear properties are revealed by this preview."
            +"\nRose upgrades increase base maximum HP by 10 at the next statistics refresh. The existing upgrade path can offer the living ghost 8 HP, capped by its maximum at that moment; it does not promise 10 HP of healing or a full heal. Maximum health and current HP are separate. Ordinary ghost healing and meter recharging use the separate rules below. Inspection does not summon, upgrade, heal, equip or spend a turn.";
    }
}

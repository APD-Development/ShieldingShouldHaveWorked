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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.*;

/** Plain units for utility resources; never repairs, revives, blesses, collects or inscribes. */
public final class UtilityItemExplanation {
    private UtilityItemExplanation(){}
    private static String n(float value){return ObfuscationExplanation.number(value);}
    public static String details(Item item,Hero hero){
        if(item instanceof Bag){Bag bag=(Bag)item;String text="_Container slots_\nCurrent capacity: "+bag.capacity()+" item slots. A compatible stack generally occupies one slot regardless of its quantity; nested-container and secondary-weapon rules keep their normal accounting. Capacity is not a weight limit, gold value or a guarantee that every item type belongs in this container. Inventory capacity and automatic pickup rules are unchanged.";
            if(item instanceof MagicalHolster)text+="\nThe holster multiplies the ordinary thrown expected-use input by "+n(MagicalHolster.HOLSTER_DURABILITY_FACTOR)+" before later rounding. Wand recharge uses its curve parameter "+n(MagicalHolster.HOLSTER_SCALE_FACTOR)+" in 10 + 40 × parameter^(missing charges) game-time units per baseline charge, before Energy and recharge effects. A smaller parameter shortens this interval when charges are missing; it is not a flat 15% extra whole charges or a change to charge capacity.";
            return text;}
        if(item instanceof Ankh){Ankh ankh=(Ankh)item;return "_Revival and blessing_\n"+(ankh.isBlessed()?"This Ankh is blessed. ":"This Ankh is not blessed. ")+"Blessing consumes a full 20-drop Waterskin and one ordinary setup time unit. A blessed Ankh is prioritized by the existing death handler; it revives at floor(maximum HP / 4), uses the healing-potion cure list and offers Invulnerability for "+n(Invulnerability.DURATION)+" game-time units before normal prolonging. "+(hero==null?"":"At current maximum HP, the blessed revival value is "+(hero.HT/4)+" HP. ")+"An ordinary Ankh uses the existing resurrection choice, inventory-recovery and floor-transition flow instead; its resurrection setup restores HP to current maximum and gives the normal Lost Inventory state and three-unit Invisibility offer. Neither kind increases the inventory limit or creates extra copies of lost gear. Inspection does not trigger a revival or spend dew.";}
        if(item instanceof Waterskin||item instanceof Dewdrop)return "_Dew as a resource_\nA Waterskin holds up to 20 drops. The ordinary base healing effect of used dew is round(maximum HP × 0.05 × drops). "+(hero==null?"":"At current maximum HP, one drop's base effect is "+Math.round(hero.HT*.05f)+" HP before missing-health limits, Vial and run modifiers. ")+"A full skin can instead be consumed to bless an Ankh. Shielding Dew can allocate excess through its separate maximum-HP cap; it does not automatically sell or turn dew into permanent HP. Waterskin drinking chooses drops under its existing health/shield/rounding rules and keeps the player's confirmation preference. Automatic collection preserves its configured enemy-visibility and capacity safeguards.";
        if(item instanceof Torch)return "_Torch time and sight_\nLighting consumes one Torch and "+n(Torch.TIME_TO_LIGHT)+" ordinary game-time unit. It offers Light for "+n(Light.DURATION)+" scheduled units through its existing additive duration path. Light's normal view-distance input is at least "+Light.DISTANCE+" tiles relative to the floor's base range; other sight modifiers, terrain and sensing rules still apply. Duration is not a real-world clock or proof that every wall within that range has been searched.";
        if(item instanceof ArcaneResin)return "_Resin units and wand upgrades_\nAn eligible identified wand below ordinary level +3 costs ordinary wand level + 1 resin units to apply one resin bonus. Application increments the resin bonus and current charge, then uses the wand's existing level/capacity update. The cost is units consumed, not an energy conversion cost, permanent identification of other wands or a free cast. Ordinary upgrades and imbuing retain their resin-handling rules. Converting a supported uncursed wand to resin uses 2 × (ordinary level excluding resin bonuses + 1), with the existing alternate Preservation bonus when applicable. Inspection does not read a selected unknown wand or perform a conversion.";
        if(item instanceof LiquidMetal)return "_Metal units and thrown-weapon repair_\nFor a selected eligible non-dart thrown weapon, the full-repair metal input is 5 × (tier + 1) × 1.35^(ordinary level). Each metal unit corresponds to 100 divided by that input in durability points. The partial-repair branch charges ceil(full-repair input × (0.999 − current durability / 100)), with the existing wear/extra-use checks. A short stack can take the separate spare-weapon reconstruction branch instead; this is not always a flat number of extra throws or a permanent upgrade. Recycling keeps its own default-stack and level-cap rules. Inspection does not pick a weapon, consume metal or simulate a future impact.";
        if(item instanceof Stylus)return "_Inscribing armor_\nA supported use consumes one Stylus and applies a glyph through the existing random glyph-selection rules. Common, uncommon and rare pool weights are separate from an individual glyph's trigger chance and strength. The selected armor's eligibility, current curse and hardening rules still apply; inscription is not an upgrade level or a guarantee of a chosen rare glyph. Inspection does not roll a glyph or reveal hidden armor properties.";
        if(item instanceof EnergyCrystal)return "_Alchemy energy units_\nThis pile's quantity is "+item.quantity()+" alchemy energy units. The supported pickup adds that quantity to the run's energy balance through its normal pickup action; it is not gold, weapon charge or experience. Recipes consume their stated energy separately, and inspection does not collect the pile.";
        if(item instanceof Gold)return "_Gold units_\nThis pile's quantity is "+item.quantity()+" gold. Supported pickup adds that quantity to the run's gold balance through its normal pickup action. It is not alchemy energy, an automatic shop sale or a change to prices; inspection does not collect or spend it.";
        return "";
    }
}

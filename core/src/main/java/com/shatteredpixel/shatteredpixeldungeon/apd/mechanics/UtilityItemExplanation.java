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
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.*;

/** Plain units for utility resources; never repairs, revives, blesses, collects or inscribes. */
public final class UtilityItemExplanation {
    private UtilityItemExplanation(){}
    private static String n(float value){return ObfuscationExplanation.number(value);}
    private static String glyphSelection(){
        float[] weights=com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph.typeChances;
        int[] sizes={com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph.common.length,
                com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph.uncommon.length,
                com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph.rare.length};
        String[] names={"common","uncommon","rare"};float total=0;
        for(float weight:weights)total+=weight;
        StringBuilder text=new StringBuilder("The ordinary glyph selector first chooses a rarity pool, then chooses equally among that pool's eligible glyphs. Current pool odds: ");
        for(int i=0;i<3;i++){
            if(i>0)text.append("; ");
            text.append(names[i]).append(" ").append(n(100*weights[i]/total)).append("%");
            if(sizes[i]>0)text.append(" (with no exclusions, ").append(n(100*weights[i]/total/sizes[i])).append("% per glyph)");
        }
        return text+". Inscribing excludes the selected armor's existing glyph, so eligible alternatives in that same pool share its chance. If an entire selected pool is excluded, the selector uses its existing unrestricted fallback. These are selection odds, not a glyph's chance to trigger in combat; Arcana affects the resulting glyph's own effect, not these rarity weights.";
    }
    private static String holsterDetails(MagicalHolster holster){
        StringBuilder text=new StringBuilder("\n_Normal thrown-weapon wear_\nFor weapons using the normal thrown durability rule, the holster increases the budget of uses before wear calculations by ")
                .append(n(100*(MagicalHolster.HOLSTER_DURABILITY_FACTOR-1))).append("% (× ").append(n(MagicalHolster.HOLSTER_DURABILITY_FACTOR))
                .append("). Rounding, existing wear, rings, talents and the weapon's own limits determine the actual whole throws left. Special dart types can use different wear rules; this does not multiply damage by the same percentage.\n\n_Wand charging_\nStored wands normally refill faster. The benefit depends on the number of charges currently missing. The ordinary recharge curve changes from ")
                .append(n(Wand.normalRechargeScaleFactor())).append(" to ").append(n(MagicalHolster.HOLSTER_SCALE_FACTOR))
                .append(". Base interval = ").append(n(Wand.baseRechargeDelay())).append(" + ").append(n(Wand.scalingRechargeDelay()))
                .append(" × curve^(missing charges) game-time units per baseline charge. Missing charges means maximum charges minus current charges. As these wands get fuller, their baseline charging slows. Each refill changes the missing-charge count and therefore the rate. Existing partial progress can finish the next charge sooner; this interval is not a countdown to the next charge or the time to refill the entire wand. Energy and other charging effects apply separately. Locked-floor regeneration rules can pause base gain; Recharging adds its own fractional gain when the charger runs. Magic immunity skips the scheduled recharge step. Direct charge gifts retain their own rules.");
        for(Item stored:holster.items){
            if(!(stored instanceof Wand))continue;
            Wand wand=(Wand)stored;
            if(!wand.levelKnown||!wand.cursedKnown||!wand.curChargeKnown)continue;
            int missing=Math.max(0,wand.maxCharges-wand.curCharges);
            text.append("\n").append(wand.name()).append(": ");
            if(missing==0)text.append("full or overcharged; the scheduled charger adds no further charge.");
            else text.append(missing).append(" missing charges; ordinary → holstered baseline interval ")
                    .append(n(Wand.baselineRechargeTurns(missing,Wand.normalRechargeScaleFactor()))).append(" → ")
                    .append(n(Wand.baselineRechargeTurns(missing,MagicalHolster.HOLSTER_SCALE_FACTOR)))
                    .append(" game-time units per baseline charge, before Energy and other effects.");
        }
        return text.append(" Unidentified wand calculations are withheld; inspection does not charge a wand or spend game time.").toString();
    }
    public static String details(Item item,Hero hero){
        if(item instanceof Bag){Bag bag=(Bag)item;String text="_Container slots_\nCurrent capacity: "+bag.capacity()+" item slots. A compatible stack generally occupies one slot regardless of its quantity; nested-container and secondary-weapon rules keep their normal accounting. Capacity is not a weight limit, gold value or a guarantee that every item type belongs in this container. Inventory capacity and automatic pickup rules are unchanged.";
            if(item instanceof MagicalHolster)text+=holsterDetails((MagicalHolster)item);
            return text;}
        if(item instanceof Honeypot)return "_Bee and broken pot_\nA supported shatter can create one bee at the impact cell. If that cell is occupied, it considers the four side-adjacent non-solid empty cells; when none is eligible, the pot is returned instead of guaranteeing a spawn. The bee uses the existing scaling-floor spawn rules. It is not automatically a harmless ally. A ground pot's bee normally looks for eligible targets within three tiles of the pot, counting a diagonal step as one tile. Carrying its matching broken pot can make the holder its target. Throwing into a pit follows the ordinary falling-item branch. Inspection does not choose a spawn cell, create a bee or advance randomness.";
        if(item instanceof Honeypot.ShatteredPot)return "_The bee's tracked home_\nThis is an existing bee's broken pot, not another unused summon. Picking it up updates matching hostile bees to track the holder; dropping, throwing or moving it updates their recorded pot location. A stack updates at most its quantity of matching bees. Destroying it clears those matching home records. A holder can become the bee's target; carrying the pot does not guarantee friendship or protection. Inspection does not move the pot, change a bee's target or reveal unseen bee locations.";
        if(item instanceof BrokenSeal){
            String text="_Seal protection and upgrades_\nFor normal Warrior sealed-armor protection, the capacity is 3 + 2 × armor tier + Iron Will rank shield points. Armor upgrade level is not part of that capacity formula. A separate non-Warrior Iron Will rule uses the talent's rank as its capacity. Capacity is protection available to the existing activation/cooldown system, not permanent HP or a continuously full shield. The shield's own status describes its current reserve and cooldown. One ordinary upgrade can reside on the seal; affixing an upgraded seal adds one armor level and detaching removes that transferred level. Existing eligibility, curses and glyph-transfer choices still apply.";
            if(hero!=null&&hero.heroClass==com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.WARRIOR&&hero.belongings.armor!=null)text+=" If affixed to your currently worn armor, its normal Warrior capacity would be "+(3+2*hero.belongings.armor.tier+hero.pointsInTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.IRON_WILL))+" shield points. This conditional preview does not attach or activate it.";
            else if(hero!=null&&hero.hasTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.IRON_WILL))text+=" Your alternate Iron Will capacity is "+hero.pointsInTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.IRON_WILL)+" shield points under that separate rule.";
            return text;
        }
        if(item instanceof Ankh){Ankh ankh=(Ankh)item;return "_Revival and blessing_\n"+(ankh.isBlessed()?"This Ankh is blessed. ":"This Ankh is not blessed. ")+"Blessing consumes a full 20-drop Waterskin and one ordinary setup time unit. A blessed Ankh is prioritized by the existing death handler; it revives at floor(maximum HP / 4), uses the healing-potion cure list and offers Invulnerability for "+n(Invulnerability.DURATION)+" game-time units before normal prolonging. This blessed revival follows its own quarter-HP rule. The Custom Dungeon Rules Healing percentage does not multiply it, including at 0% Healing. "+(hero==null?"":"At current maximum HP, the blessed revival value is "+Ankh.blessedReviveHP(hero.HT)+" HP. ")+"An ordinary Ankh uses the existing resurrection choice, inventory-recovery and floor-transition flow instead; its resurrection setup restores HP to current maximum and gives the normal Lost Inventory state and three-unit Invisibility offer. Neither kind increases the inventory limit or creates extra copies of lost gear. Inspection does not trigger a revival or spend dew.";}
        if(item instanceof Waterskin||item instanceof Dewdrop)return "_Dew as a resource_\nA Waterskin holds up to 20 drops. The ordinary base healing effect of used dew is round(maximum HP × 0.05 × drops). "+(hero==null?"":"At current maximum HP, one drop's base effect is "+Math.round(hero.HT*.05f)+" HP before missing-health limits, Vial and run modifiers. ")+"A full skin can instead be consumed to bless an Ankh. Shielding Dew can allocate excess through its separate maximum-HP cap; it does not automatically sell or turn dew into permanent HP. Waterskin drinking chooses drops under its existing health/shield/rounding rules and keeps the player's confirmation preference. Automatic collection preserves its configured enemy-visibility and capacity safeguards.";
        if(item instanceof Torch)return "_Torch time and sight_\nLighting consumes one Torch and "+n(Torch.TIME_TO_LIGHT)+" ordinary game-time unit. It offers Light for "+n(Light.DURATION)+" scheduled units through its existing additive duration path. Light's normal view-distance input is at least "+Light.DISTANCE+" tiles relative to the floor's base range; other sight modifiers, terrain and sensing rules still apply. Duration is not a real-world clock or proof that every wall within that range has been searched.";
        if(item instanceof ArcaneResin){
            String talent=hero==null?"Your personal Preservation bonus requires a loaded character; no rank is assumed. ":"Your current extra resin yield from Preservation is "+(hero.heroClass==com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.MAGE?0:hero.pointsInTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.WAND_PRESERVATION))+" resin units. ";
            return "_Applying resin_\nAn identified wand below permanent level +"+ArcaneResin.applicationLevelLimit()+" costs current permanent level + 1 resin units to gain one resin bonus and one current charge, followed by its normal capacity update. The level check and cost include existing resin and curse-infusion bonuses; temporary casting bonuses are separate. A normal upgrade or staff imbuing can remove resin under its existing rules.\n\n_Converting a wand into resin_\nConversion consumes the selected wand and "+ArcaneResin.Recipe.energyCost()+" alchemy energy. It requires known, uncursed status. Base yield = 2 × (permanent wand level excluding resin bonuses + 1) resin units. A non-Mage's Wand Preservation adds its trained rank in extra units; the Mage version affects staff imbuing instead. "+talent+"An unknown wand level has no exact preview yield; its true yield is resolved when the wand is consumed. Merely previewing the recipe or inspecting resin does not alter, identify, charge or consume a wand.";
        }
        if(item instanceof LiquidMetal)return "_Metal units and thrown-weapon repair_\nFor a selected eligible non-dart thrown weapon, the full-repair metal input is 5 × (tier + 1) × 1.35^(ordinary level). Each metal unit corresponds to 100 divided by that input in durability points. The partial-repair branch charges ceil(full-repair input × (0.999 − current durability / 100)), with the existing wear/extra-use checks. A short stack can take the separate spare-weapon reconstruction branch instead; this is not always a flat number of extra throws or a permanent upgrade. Recycling keeps its own default-stack and level-cap rules. Inspection does not pick a weapon, consume metal or simulate a future impact.";
        if(item instanceof Stylus)return "_Inscribing armor_\nA supported use consumes one Stylus and "+n(Stylus.inscriptionTime())+" game-time units to apply a glyph. The selected armor must have known curse status and must not be cursed or bear a curse glyph; a rejected selection does not consume the Stylus. "+glyphSelection()+" Inscription does not raise armor upgrade level. Inspection does not select armor, roll a glyph, consume time or reveal hidden armor properties.";
        if(item instanceof EnergyCrystal)return "_Alchemy energy units_\nThis pile's quantity is "+item.quantity()+" alchemy energy units. The supported pickup adds that quantity to the run's energy balance through its normal pickup action; it is not gold, weapon charge or experience. Recipes consume their stated energy separately, and inspection does not collect the pile.";
        if(item instanceof Gold)return "_Gold units_\nThis pile's quantity is "+item.quantity()+" gold. Supported pickup adds that quantity to the run's gold balance through its normal pickup action. It is not alchemy energy, an automatic shop sale or a change to prices; inspection does not collect or spend it.";
        return "";
    }
}

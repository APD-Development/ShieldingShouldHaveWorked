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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;

/** Area-effect rules, without counting hidden targets or simulating an attack. */
public final class StrikeExplanation {
    private StrikeExplanation(){}
    private static String n(float x){return ObfuscationExplanation.number(x);}
    private static String chance(float value){return n(Math.min(100,100*value))+"%";}
    public static String details(Hero hero){
        if(hero==null)return "";
        int reach=hero.pointsInTalent(Talent.ELEMENTAL_REACH),directed=hero.pointsInTalent(Talent.DIRECTED_POWER);
        float power=1+.3f*hero.pointsInTalent(Talent.STRIKING_FORCE);
        String result="\n\n_Elemental Strike attack and area_\nThe cone reaches at most 4 + Elemental Reach rank = "+(4+reach)+" tiles over 65 + 10 × rank = "+(65+10*reach)+" degrees, limited by the aimed distance and solid terrain. A valid primary target must still pass ordinary attack reach, alignment and charm checks. Its strike uses guaranteed-hit accuracy in the normal contest; special dodge rules still apply. After resolving, spend the hero's normal attack delay. Inspection does not roll damage or consume an attack benefit.\nThe cone's special effect uses Striking Force power = 1 + 0.30 × rank = "+n(power)+". This is separate from normal enchantment activation power. Directed Power offers an added 0.30 × enemy count in the cone × rank = "+n(.3f*directed)+" per counted enemy to applicable enchantment power. No hidden enemy count is computed here. The equipped melee weapon selects the cone effect; a primary hit can also run its normal weapon effects.";
        KindOfWeapon held=hero.belongings.weapon();
        if(held instanceof MeleeWeapon&&!held.cursedKnown)return result+"\nIdentify the weapon's unknown properties before its specific cone effect is shown. The same information is withheld regardless of any hidden curse or enchantment, and unknown upgrades or future rolls are not inspected.";
        Weapon.Enchantment enchantment=held instanceof MeleeWeapon?((MeleeWeapon)held).enchantment:null;
        String name=enchantment==null?"None":enchantment.getClass().getSimpleName();String effect;
        switch(name){
            case "None":effect="Each eligible non-allied character in the cone receives a separate 6–12 raw damage roll multiplied by power and rounded: current range "+Math.round(6*power)+"–"+Math.round(12*power)+" points before normal offensive/target rules.";break;
            case "Blazing":effect="Creates fire across the cone. The fire follows its own ignition, spreading, decay and damage rules; the primary hit still has the weapon's normal enchantment behavior.";break;
            case "Chilling":effect="Creates freezing effects across the cone. Water, terrain, items and creatures follow the freezing field's normal rules; it is not a fixed added melee damage amount.";break;
            case "Shocking":effect="Creates electricity across the cone. Electrical field spread, paralysis and damage follow their own rules, rather than every tile receiving the same guaranteed HP damage.";break;
            case "Kinetic":effect="If damage was stored before the strike, each affected character other than the primary target is offered round(stored damage × 0.4 × power) raw damage points. That captured amount is then cleared. With no valid primary target, any remaining conserved-damage buff is removed. Inspection does not spend the stored benefit.";break;
            case "Venomous":effect="Affected characters other than the primary target receive an added "+n(10*power)+" units of Poison duration. A newly created Poison is delayed by three game-time units. These are duration units, not that many guaranteed damage points; Poison's current strength, ticks and target defenses determine damage.";break;
            case "Blocking":effect="Before the primary hit, offers round(6 × enemy count in the cone × power) Barrier points. The current per-enemy factor is "+n(6*power)+" before final rounding; a stronger existing Barrier is kept. It does not multiply the hero's maximum HP.";break;
            case "Blooming":effect="Attempts to place round(8 × power) = "+Math.round(8*power)+" tall-grass tiles on suitable plant-free terrain without immovable occupants. Other suitable cells become short grass. Non-allied affected characters are offered "+Math.round(6*power)+" units of Roots. After the saved grass-use counter reaches40, tall grass becomes furrowed; normal uses add1, while no visible enemy and no enemy in the cone adds4. Experience reduces that counter by20 per hero-level fraction earned. Inspection does not create or advance the counter.";break;
            case "Eldritch":effect="Affected characters are offered "+n(10*power)+" scheduled units of Terror directed away from the hero, subject to normal status restrictions.";break;
            case "Elastic":effect="Offers outward knockback with strength round(5 × power) = "+Math.round(5*power)+". Terrain and target rules can reduce actual travel. A primary target already moved by its strike can be skipped.";break;
            case "Lucky":effect="Each eligible enemy has a "+chance(.125f*power)+" loot check, from 0.125 × power. Only one successful cone-loot award is allowed per enemy through this tracker. The generated item follows Lucky's normal loot rules.";break;
            case "Projecting":effect="Each affected character other than the primary target is offered round(a separate hero damage roll × 0.3 × power) raw damage. Future rolls and unknown weapon upgrades are not simulated here.";break;
            case "Unstable":effect="Each affected character other than the primary target runs Unstable's normal random proc using a separate weapon damage roll. Each chosen effect has its own conditions; there is no single fixed extra-damage amount or activation percentage.";break;
            case "Vorpal":effect="Affected characters other than the primary target are offered Bleeding with strength "+n(10*power)+". Strength is the input to Bleeding's diminishing damage rolls, not an exact total of HP lost.";break;
            case "Corrupting":effect="Eligible living monsters other than the primary target use chance = (0.05 + 0.20 × missing-HP fraction) × power, capped naturally by the random check. At current power, the full-health to zero-HP formula bounds are "+chance(.05f*power)+"–"+chance(.25f*power)+"; a living target's actual check also requires no Corruption or immunity. No target's hidden HP is read into this explanation.";break;
            case "Crystal":effect="Affected characters other than the primary target receive a 10–20 raw damage roll × power, rounded, and offer4 × power = "+n(4*power)+" repair units through Crystal's repair rules. Repair units are not direct HP healing or permanent upgrades.";break;
            case "Grim":effect="Other affected targets use chance = (0.06 + 0.24 × missing-HP fraction) × power. Current formula bounds are "+chance(.06f*power)+"–"+chance(.30f*power)+". A success offers raw damage equal to the target's current HP through normal offensive/target rules; immunity and damage rules can prevent an instant kill. No hidden target HP or actual chance is calculated here.";break;
            case "Vampiric":effect="Before the primary hit, offers round(2.5 × enemy count in the cone × power) HP of healing. The current per-enemy factor is "+n(2.5f*power)+" before final rounding; current healing rules and missing HP limit actual restoration.";break;
            case "Annoying":effect="Each affected character has a "+chance(.2f*power)+" check for six scheduled units of Amok.";break;
            case "Displacing":effect="Each affected character has a "+chance(.5f*power)+" teleport check. A successful legal teleport changes a hunting monster to wandering; destination and teleport restrictions still apply.";break;
            case "Dazzling":effect="Each affected character has a "+chance(.5f*power)+" check for six scheduled units of Blindness.";break;
            case "Explosive":effect="One "+chance(.5f*power)+" check can detonate a conjured bomb at one randomly chosen affected character. This is one cone-wide check, not one explosion roll per tile or target. Bomb damage and terrain rules still apply.";break;
            case "Friendly":effect="Each affected character has a "+chance(.5f*power)+" check for six units of Charm toward the hero.";break;
            case "Polarized":effect="Each affected character has a "+chance(.5f*power)+" check for a separate 24–36 raw damage roll. Power changes this check, not the24–36 roll's size; normal offensive/target rules still apply.";break;
            case "Pressurized":effect="Each cone cell has a "+chance(power/2)+" water-placement check, with terrain rules still applied. Affected characters are offered knockback strength round(2 × power) = "+Math.round(2*power)+"; actual travel depends on obstacles and target properties.";break;
            case "Sacrificial":effect="Offers the hero Bleeding strength "+n(10*power)+" before the strike and affected characters Bleeding strength "+n(12*power)+". These are diminishing Bleeding inputs, not fixed total HP damage.";break;
            case "Wayward":effect="Each affected character has a "+chance(.5f*power)+" check for six scheduled units of Hex.";break;
            case "Wondrous":effect="Each affected character has a "+chance(power/2)+" check for a valid cursed-wand effect. Resin rules determine whether only positive effects can be chosen. The chosen effect has its own target and outcome rules; inspection does not choose it.";break;
            default:return result+"\nThis enchantment's additional cone effect has not been calculated here; no unsupported effect or hidden property is invented.";
        }
        return result+"\n\n_Current known cone effect_\n"+effect;
    }
}

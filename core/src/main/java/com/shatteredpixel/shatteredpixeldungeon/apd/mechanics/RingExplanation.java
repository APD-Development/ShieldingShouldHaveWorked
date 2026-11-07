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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.*;

/** One identified ring's contribution, using the same pure functions as combat. */
public final class RingExplanation {
    private RingExplanation(){}
    private static String n(float value){return ObfuscationExplanation.number(value);}
    public static String details(Ring ring){
        if(!ring.isIdentified())return "Identify this ring before viewing its exact effect or upgrade-dependent calculations.";
        if(ring instanceof RingOfArcana)return ((RingOfArcana)ring).statsInfo();
        int b=ring.soloBuffedBonus(),plain=ring.soloBonus();String effect;
        if(ring instanceof RingOfAccuracy)effect="Attack accuracy score × 1.3^bonus = × "+n(RingOfAccuracy.accuracyMultiplier(b))+". This scales the attack score, not a fixed hit probability. The enemy's evasion still matters.";
        else if(ring instanceof RingOfEvasion)effect="Evasion score × 1.125^bonus = × "+n(RingOfEvasion.evasionMultiplier(b))+". This scales the defense score, not a fixed dodge probability. The enemy's accuracy still matters.";
        else if(ring instanceof RingOfElements)effect="Supported elemental damage or duration × 0.825^bonus = × "+n(RingOfElements.elementalMultiplier(b))+". A factor below one reduces the supported amount; this is not protection from every kind of damage or every debuff.";
        else if(ring instanceof RingOfEnergy)effect="Wand/artifact/heroic-armor recharge rate × 1.175^bonus = × "+n(RingOfEnergy.ringChargeMultiplier(b))+". For an otherwise unchanged recharge, time is divided by this factor. This does not add that many charges or lower a charge cost.";
        else if(ring instanceof RingOfFuror)effect="Attack rate × 1.09051^bonus = × "+n(RingOfFuror.attackSpeedMultiplier(b))+". Attack time is divided by this factor; damage per hit and movement speed are separate.";
        else if(ring instanceof RingOfHaste)effect="Movement rate × 1.15^bonus = × "+n(RingOfHaste.speedMultiplier(b))+". Ordinary movement time is divided by this factor; attacks are separate.";
        else if(ring instanceof RingOfMight)effect="Adds "+plain+" strength points. Maximum-HP factor = 1.035^bonus = × "+n(RingOfMight.HTMultiplier(b))+" before the maximum-HP calculation's integer rounding. Strength uses the ring's ordinary bonus; temporary effective-ring bonuses can affect HP separately. This does not heal the same amount as its percentage.";
        else if(ring instanceof RingOfSharpshooting)effect="Adds "+b+" effective upgrade levels to thrown-weapon damage calculations. Durability factor = 1.2^ordinary bonus = × "+n(RingOfSharpshooting.durabilityMultiplier(plain))+". This changes durability consumption, not damage by the same percentage; each thrown weapon's own rules and rounding still apply.";
        else if(ring instanceof RingOfTenacity){
            effect="Supported incoming damage factor = 0.85^(bonus × fraction of HP missing). ";
            if(Dungeon.hero!=null&&Dungeon.hero.HT>0){float missing=(Dungeon.hero.HT-Dungeon.hero.HP)/(float)Dungeon.hero.HT;effect+="At the current "+n(100*missing)+"% missing HP, this ring's factor is × "+n(RingOfTenacity.damageMultiplier(b,missing))+". ";}
            else effect+="A personal reduction requires a loaded hero with a valid maximum HP; no missing-health percentage is assumed. ";
            effect+="At full HP the factor is one. The listed maximum reduction is a limit as missing HP approaches 100%, not your reduction on every hit. Defense order and rounding still affect final HP loss.";
        }
        else if(ring instanceof RingOfWealth)effect="Qualifying ordinary loot chance × 1.20^bonus = × "+n((float)Math.pow(1.20,b))+". This multiplies an eligible source's chance; it does not add that percentage to every drop. Special bonus-loot counters and their guarantees follow separate rules, and some sources ignore this ring.";
        else if(ring instanceof RingOfForce){
            if(Dungeon.hero==null)effect="A personal unarmed damage range requires the wearer's actual strength; no example strength is assumed. Strength gives tier = max(1, (strength − 8) / 2); above tier 5 only half of the excess is kept. A nonpositive ring bonus uses damage tier 1. Minimum = max(0, round(tier + bonus)); maximum = max(0, round(5 × (tier + 1) + bonus × (tier + 1))). Armed damage instead adds "+b+" damage points. Brawler's Stance has its separate strength-dependent bonus; its personal result also requires a hero.";
            else{
            int strength=Dungeon.hero.STR();float tier=RingOfForce.unarmedTier(strength),damageTier=b<=0?1:tier;
            effect="At your current "+strength+" strength points, this ring's unarmed base range is "+RingOfForce.unarmedMinimum(strength,b)+"–"+RingOfForce.unarmedMaximum(strength,b)+" damage points before target defenses, other effects and custom damage rules. Strength first gives tier = max(1, (strength − 8) / 2); above tier 5, only half of the excess is kept. Current tier: "+n(tier)+". For a nonpositive ring bonus the damage calculation uses tier 1 instead. Minimum = max(0, round(tier + bonus)); maximum = max(0, round(5 × (tier + 1) + bonus × (tier + 1))). Here the damage tier is "+n(damageTier)+". Armed damage instead adds "+b+" damage points, not a percentage. Brawler's Stance uses the unarmed calculation and adds round(3 + strength-based tier + bonus × (4 + 2 × strength-based tier) / 8) damage points; weapon augmentation and enchantment can apply separately.";
            }
        }
        else return "";
        String state= Dungeon.hero!=null&&Dungeon.hero.buff(MagicImmune.class)!=null?"Magic immunity currently suppresses ring effects. These are identified equipment contributions, not active benefits.\n\n":"";
        return state+"_This ring's identified contribution_\n"+effect+"\n\nEffective bonus for the calculation above: "+b+". These are this ring's contributions, not a total including an unidentified second ring. Equipped rings of the same type normally combine their bonuses before their formula is applied. An ordinary uncursed ring starts with bonus = upgrade level + 1; upgrading increases that bonus. Curses and temporary effects can change which bonus applies. Values are rounded for display.";
    }
}

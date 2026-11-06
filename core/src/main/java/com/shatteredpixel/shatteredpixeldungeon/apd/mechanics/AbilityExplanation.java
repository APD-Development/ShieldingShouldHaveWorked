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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.apd.qol.RingItemEffects;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;

/** Charge units and live cost; does not activate abilities or query attack/damage handlers. */
public final class AbilityExplanation {
    private AbilityExplanation() {}
    private static String n(float value) { return ObfuscationExplanation.number(value); }
    public static String summary(ArmorAbility ability, Hero hero) {
        float cost=hero==null?ability.baseChargePoints():ability.chargeUse(hero);
        return "Armor charge is a resource measured from 0 to 100 points, not a chance of success. "
            + (hero==null?"Base cost: ":"Cost with your current talents and ability state: ") + n(cost)
            + " charge points. A command to an existing summoned ally can have a different cost from creating it.";
    }
    public static String details(ArmorAbility ability, Hero hero) {
        String result="_Armor ability charge_\nBase use costs "+n(ability.baseChargePoints())
            +" points from a 100-point charge meter. Heroic Energy multiplies that cost by 0.88, 0.77, 0.68 or 0.6 at ranks one through four; without it the factor is 1. These are alternative rank factors, not four reductions added or multiplied together. Other conditional ability discounts multiply at their own step.";
        if(hero==null)return result;
        int rank=hero.pointsInTalent(Talent.HEROIC_ENERGY);
        result+="\nCurrent Heroic Energy rank: "+rank+". Actual current ability cost: "+n(ability.chargeUse(hero))+" charge points.";
        result+="\nDouble Jump and Elimination Match apply 0.84 raised to their talent rank while their ready effects exist; Shadow Step uses the same factor while invisible. Double Mark instead uses 0.707 raised to its rank while ready. Summon commands such as directing an existing hawk or clone can cost zero; that does not make creating a replacement free.";
        Integer energy=RingItemEffects.knownBonus(hero,RingOfEnergy.class,true);
        result+="\n\n_Armor recharge_\nWhen normal regeneration is allowed, each eligible one-turn armor charger update adds 0.2 × its Energy recharge factor, capped at 100 charge points. At factor 1, an empty meter takes 500 eligible updates to fill. Time when regeneration is blocked does not count as eligible charging time.";
        if(energy==null)result+=" Identify equipped rings before the combined Energy factor is shown.";
        else result+=" Current known Energy factor: "+n(RingOfEnergy.ringChargeMultiplier(energy))+"; contribution per eligible update: "+n(.2f*RingOfEnergy.ringChargeMultiplier(energy))+" charge points before the meter cap.";
        return result+effects(ability,hero);
    }
    public static String effects(ArmorAbility ability,Hero hero){
        if(hero==null)return "";
        if(ability instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure){
            int shrug=hero.pointsInTalent(Talent.SHRUG_IT_OFF),sustained=hero.pointsInTalent(Talent.SUSTAINED_RETRIBUTION);
            return "\n\n_Enduring and retaliation_\nActivation spends three game-time units. During this action, damage entering Endure's step is multiplied by 0.5 × 0.8 raised to Shrug It Off rank; current factor: "+n(.5f*(float)Math.pow(.8f,shrug))+". This is remaining damage at that step, not a percentage of maximum HP or guaranteed final HP loss. Each qualifying hit first adds half its incoming damage to the stored retaliation amount, truncating the accumulated result to an integer. Further Shrug It Off protection does not shrink that stored contribution.\nWhen the hero next acts, convert the store: truncate(store × (1 + 0.15 × Sustained Retribution rank)), then truncate(that result × (1 + nearby enemies × 0.05 × Even the Odds rank)), then divide by 1 + Sustained Retribution rank with integer truncation. These are successive multipliers and rounding steps, not one combined rounding. Nearby means enemies within two tiles at conversion; this inspector does not count hidden enemies. Current retaliation offers "+(1+sustained)+" qualifying hits reaching the attack-damage step if the resulting bonus is positive. Each such hit adds the resulting flat damage points and spends one use; a normal miss does not reach this step. This benefit expires on its normal timer if unused; it is not a permanent weapon upgrade.";
        }
        if(ability instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Shockwave){
            int expansion=hero.pointsInTalent(Talent.EXPANDING_WAVE),force=hero.pointsInTalent(Talent.SHOCK_FORCE),striking=hero.pointsInTalent(Talent.STRIKING_WAVE);
            String range="Identify equipped rings before the current strength-based damage range is shown.";
            Integer might=RingItemEffects.knownBonus(hero,com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMight.class,false);
            if(might!=null){int strength=hero.previewStrength(might);int scaled=strength-10;range="At current known strength "+strength+": base roll "+(5+scaled)+"–"+(10+2*scaled)+" damage points; after Shock Force alone, "+Math.round((5+scaled)*(1+.2f*force))+"–"+Math.round((10+2*scaled)*(1+.2f*force))+" points.";}
            return "\n\n_Shockwave area and damage_\nReaches up to "+(5+expansion)+" tiles in a "+(60+15*expansion)+" degree cone, limited by the aimed distance and solid terrain. Casting spends one game-time unit. Each differently aligned character in the cone uses a separate strength-based damage roll: from 5 + (strength − 10) to 10 + 2 × (strength − 10), favoring middle results under ordinary damage-roll rules. Shock Force multiplies that roll by 1 + 0.2 × rank = "+n(1+.2f*force)+", then rounds. "+range+" Custom attack rules and the target's armor apply afterward; this is not guaranteed HP lost.\nStriking Wave's attack-effect borrowing check succeeds on min(10, 3 × rank) of ten outcomes: current chance "+Math.min(100,30*striking)+"%. Borrowed effects still use their own activation and target rules. A surviving target is offered five turns of Paralysis with chance Shock Force rank / 4 = "+(25*force)+"%; otherwise it is offered five turns of Cripple. Resistance, immunity and other damage/discovery rules still apply.";
        }
        if(ability instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap){
            int slam=hero.pointsInTalent(Talent.BODY_SLAM),impact=hero.pointsInTalent(Talent.IMPACT_WAVE);
            return "\n\n_Leap landing effects_\nThe leap follows a straight line until the selected tile or solid terrain; an occupied landing is moved back along that route to an unoccupied tile. Rooting prevents the leap. A completed leap costs one game-time unit, resolves the destination tile's normal effects and dispels invisibility. It does not allow travel through walls.\nBody Slam at current rank "+slam+" offers adjacent non-allied characters a damage roll from "+slam+" to "+(4*slam)+" points, plus round(the hero's armor-block roll × 0.25 × rank), only when learned. Custom attack rules and each target's armor apply afterward. The armor-block roll is not a permanent flat damage bonus; hidden armor levels are not inspected here.\nImpact Wave at current rank "+impact+" offers outward knockback with strength "+(1+impact)+" when learned; terrain and target rules can limit actual travel. It also offers five turns of Vulnerable with chance rank / 4 = "+(25*impact)+"%. A ready Double Jump discount is consumed by the next leap; otherwise a learned Double Jump readies its three-turn opportunity.";
        }
        return "";
    }
}

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
        return result;
    }
}

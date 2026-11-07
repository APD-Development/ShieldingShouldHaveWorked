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

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.utils.Holiday;

/** Describes the same current calendar form used by Pasty.satisfy; never applies it. */
public final class PastyExplanation {
    private PastyExplanation() {}
    public static String summary(Hero hero) {
        switch (Holiday.getCurrentHoliday()) {
            case LUNAR_NEW_YEAR:
                return "This form also creates one leftover meal, eaten separately for "
                    + ObfuscationExplanation.number(FoodExplanation.nutritionBeforeHorn(Hunger.HUNGRY/2f))
                    + " hunger points before a cursed Horn and the fully-fed limit.";
            case APRIL_FOOLS: case EASTER:
                return "This form recharges non-cursed artifacts using 2 recharge power each. Each artifact converts that power using its own rate and capacity; this does not mean 2 finished charges.";
            case PRIDE:
                return "This form attempts to Charm one adjacent enemy for 5 scheduled game-time units before resistance. Bosses and minibosses are excluded. It does not guarantee that a suitable target exists.";
            case SHATTEREDPD_BIRTHDAY: case PD_BIRTHDAY:
                return "This form grants experience: " + (hero==null ? "at least 2 XP, based on your full level requirement." : Math.max(2,hero.maxExp()/10)+" XP at your current level.");
            case HALLOWEEN:
                return "This form offers " + (hero==null ? "at least 3 base HP" : Math.max(3,hero.HT/20)+" base HP at your current maximum health")
                    + " of healing before healing modifiers and the missing-health limit.";
            case WINTER_HOLIDAYS:
                return "This form adds half a charge to each supported wand that is not full. Two halves can make one usable charge; charge capacity still applies.";
            case NEW_YEARS:
                return "This form offers " + (hero==null ? "at least 5 Barrier shield points" : Math.max(5,hero.HT/10)+" Barrier shield points at your current maximum health")
                    + ". It keeps a stronger existing Barrier instead of adding the two reserves.";
            default:
                return "This ordinary form has no extra holiday effect beyond eating and your normal meal benefits.";
        }
    }
    public static String details(Hero hero) {
        String text="_Current seasonal food effect_\n"+summary(hero);
        switch(Holiday.getCurrentHoliday()) {
            case LUNAR_NEW_YEAR:
                return text+" The main meal uses 300 base hunger points; the leftover uses 150. On Diet divides each by 3. The leftover goes into your inventory, or drops if it cannot be collected. Eating it later consumes its own normal meal action.";
            case APRIL_FOOLS: case EASTER:
                return text+" This is a direct artifact recharge offer, not a timed recharge buff or a separate wand-charge bonus. Recharge particles do not provide another charge effect.";
            case PRIDE:
                return text+" Among adjacent eligible enemies, your focused target has priority. Charm prevents that enemy from attacking you while active; it does not make the enemy an ally. Immunity and resistance apply. Reapplication adds the resisted duration to an existing Charm schedule and assigns you as its source. Inspection does not select or reveal a target.";
            case SHATTEREDPD_BIRTHDAY: case PD_BIRTHDAY:
                return text+" Formula: max(2, floor(full current level XP requirement / 10)). This uses the full requirement, not the XP remaining until your next level; normal experience and level-up rules still apply.";
            case HALLOWEEN:
                return text+" Formula: max(3, floor(maximum HP / 20)) base HP. The current healing rule, fractional healing carry and missing health determine the HP actually restored. Inspecting this food does not consume or reset that carry.";
            case WINTER_HOLIDAYS:
                return text+" Each attached wand charger receives 0.5 charge. Existing fractional charge is retained until it reaches a whole charge; reaching capacity clears excess fractional charge. Already-full wands gain nothing. No wand capacity or hidden upgrade is revealed here.";
            case NEW_YEARS:
                int offer=hero==null?0:Math.max(5,hero.HT/10);
                Barrier barrier=hero==null?null:hero.buff(Barrier.class);
                return text+" Formula: max(5, floor(maximum HP / 10)). New reserve = max(existing Barrier, offered shield points)."+(hero==null?"":" Your resulting Barrier reserve would be "+Math.max(offer,barrier==null?0:barrier.shielding())+" shield points before subsequent damage and decay.")+" Equal or larger offers reset partial decay; a stronger existing reserve retains its decay progress. Ordinary Barrier decay continues.";
            default: return text;
        }
    }
}

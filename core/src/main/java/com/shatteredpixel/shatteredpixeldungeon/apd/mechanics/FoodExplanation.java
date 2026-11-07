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

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Blandfruit;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.food.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

/** Nutrition and action-time inputs, without eating or previewing random meal effects. */
public final class FoodExplanation {
    private FoodExplanation() {}
    public static float baseNutrition(Food food){
        return food instanceof Pasty && com.shatteredpixel.shatteredpixeldungeon.utils.Holiday.getCurrentHoliday()==com.shatteredpixel.shatteredpixeldungeon.utils.Holiday.LUNAR_NEW_YEAR?Hunger.HUNGRY:food.energy;
    }
    public static float nutritionBeforeHorn(Food food){
        float base=baseNutrition(food);
        return nutritionBeforeHorn(base);
    }
    public static float nutritionBeforeHorn(float base){
        return Dungeon.isChallenged(Challenges.NO_FOOD)?base/3f:base;
    }
    private static String unavailable(Food food){
        if(food instanceof MysteryMeat.PlaceHolder)return "This is a crafting placeholder, not a preview of an edible meal.";
        if(food instanceof Blandfruit && ((Blandfruit)food).potionAttrib==null)return "This uncooked fruit cannot be eaten. Its cooked form has separate nutrition and potion effects.";
        return "";
    }
    public static String summary(Food food){
        String unavailable=unavailable(food);if(!unavailable.isEmpty())return unavailable;
        String text="Nutrition: _"+ObfuscationExplanation.number(nutritionBeforeHorn(food))+" hunger points_ before a cursed Horn's penalty and the fully-fed limit. Eating subtracts hunger points; it does not restore that many HP.";
        float time=food.previewEatingTime();
        if(Float.isFinite(time))text+=" Current eating action: _"+ObfuscationExplanation.number(time)+" game turns_ before effects that change all action times.";
        if(food instanceof Blandfruit)text+="\n"+BlandfruitExplanation.summary((Blandfruit)food);
        return text;
    }
    public static String details(Food food){
        String unavailable=unavailable(food);if(!unavailable.isEmpty())return unavailable;
        return "_Nutrition and meal time_\nBase nutrition: "+ObfuscationExplanation.number(baseNutrition(food))+" hunger points. On Diet divides this by 3; under the current challenge it offers "+ObfuscationExplanation.number(nutritionBeforeHorn(food))+" points before other limits. An active cursed Horn multiplies the meal's offered nutrition by 0.67, unless its curse is suppressed. A personal cursed-Horn result is not guessed from unidentified equipment.\nOrdinary satisfaction subtracts that amount from hunger, stopping at zero. Excess is not stored as extra HP or an unlimited food reserve. The hunger-rate setting changes hunger accumulation rather than multiplying this positive nutrition. Meal-related talents and special food effects follow their separate rules.\nThe current action-time preview uses this food's actual eating-time rule. Normal food uses "+ObfuscationExplanation.number(Food.TIME_TO_EAT)+" turns, shortened by qualifying meal talents; berries and Supply Rations have their own faster rule. Any zero shown comes from an existing food/talent rule. It is not a free action added by this description. The food is still consumed, and other actors and hunger advance according to the actual elapsed game time. Random food effects are not rolled during inspection."+specialEffect(food,Dungeon.hero);
    }
    private static String n(float value){return ObfuscationExplanation.number(value);}
    public static String specialEffect(Food food,Hero hero){
        if(food instanceof MysteryMeat.PlaceHolder)return "";
        String current=hero==null?"":" At current maximum health, that base offer is "+(hero.HT/4)+" HP or Barkskin strength points, respectively.";
        if(food instanceof FrozenCarpaccio)return "\n_Frozen meat effect_\nFive equally likely initial outcomes: Invisibility for "+n(Invisibility.DURATION)+" scheduled game-time units; Barkskin strength floor(maximum HP / 4), decaying one point per one-unit update; the Healing potion's supported ailment cure; healing with a base offer floor(maximum HP / 4); or no extra effect. Each initial outcome has a 20% chance. Healing modifiers, missing health, status resistance and reapplication still apply."+current;
        if(food instanceof PhantomMeat)return "\n_Phantom meat effect_\nOffers all four supported benefits: Barkskin strength floor(maximum HP / 4), decaying one point per one-unit update; Invisibility for "+n(Invisibility.DURATION)+" scheduled units; healing with a base offer floor(maximum HP / 4); and the Healing potion's supported ailment cure. Healing modifiers, missing health, resistance and reapplication still apply."+current;
        if(food instanceof MysteryMeat)return "\n_Raw meat risk_\nFive equally likely initial outcomes: reignite Burning; add Roots for "+n(Roots.DURATION*2)+" scheduled units; offer Poison with duration floor(maximum HP / 5) game-time units; add Slow for "+n(Slow.DURATION)+" scheduled units; or no extra effect. Each initial outcome has a 20% chance before immunity and resistance. Poison duration also controls its changing damage per tick; it is not guaranteed total damage, and raw meat is inappropriate for automatic eating.";
        if(food instanceof MeatPie)return "\n_Well Fed_\nResets the Well Fed reserve to "+n(Dungeon.isChallenged(Challenges.NO_FOOD)?Hunger.STARVING/3:Hunger.STARVING)+" scheduled updates. It offers 1 base HP at each supported multiple of "+WellFed.HEAL_INTERVAL+" remaining updates when health is missing. Salt Cube changes update spacing rather than the base heal per offer; healing modifiers still apply. Well Fed pauses ordinary hunger accumulation. Eating another pie resets this reserve rather than adding a second independent reserve.";
        if(food instanceof Berry)return "\n_Berry seeds_\nEvery second eaten berry creates one random seed through the normal seed table. The saved counter carries between berries and is cleared after the seed is produced. Inspection does not increment it or choose a seed.";
        if(food instanceof SupplyRation)return "\n_Supply Ration_\nOffers 5 base HP of healing, subject to healing modifiers and missing health. If a Cloak of Shadows is carried, it also offers 1 direct Cloak charge and the ordinary recharge visual feedback. Capacity and item rules still apply; this is separate from its nutrition and fast eating time.";
        if(food instanceof Blandfruit)return "\n"+BlandfruitExplanation.details((Blandfruit)food,hero);
        if(food instanceof Pasty)return "\n_Seasonal food_\nIts visible holiday form determines its extra effect under the existing calendar: Lunar New Year adds a separate leftover meal; April Fools and Easter offer 2 artifact recharge power; Pride attempts a 5-unit Charm on an adjacent eligible non-boss enemy; birthday forms offer max(2, floor(level experience requirement / 10)) experience; Halloween offers max(3, floor(maximum HP / 20)) base healing; winter offers 0.5 supported wand charge; New Year offers max(5, floor(maximum HP / 10)) Barrier shielding. Normal capacity, defense, healing and reapplication rules apply. Other dates add none of those effects. These are conditional holiday effects, not benefits every Pasty grants at once.";
        return "";
    }

}

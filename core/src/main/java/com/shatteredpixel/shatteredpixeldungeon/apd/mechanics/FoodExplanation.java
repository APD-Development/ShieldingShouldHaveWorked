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
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;

/** Nutrition and action-time inputs, without eating or previewing random meal effects. */
public final class FoodExplanation {
    private FoodExplanation() {}
    public static float nutritionBeforeHorn(Food food){
        return Dungeon.isChallenged(Challenges.NO_FOOD)?food.energy/3f:food.energy;
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
        return text;
    }
    public static String details(Food food){
        String unavailable=unavailable(food);if(!unavailable.isEmpty())return unavailable;
        return "_Nutrition and meal time_\nBase nutrition: "+ObfuscationExplanation.number(food.energy)+" hunger points. On Diet divides this by 3; under the current challenge it offers "+ObfuscationExplanation.number(nutritionBeforeHorn(food))+" points before other limits. An active cursed Horn multiplies the meal's offered nutrition by 0.67, unless its curse is suppressed. A personal cursed-Horn result is not guessed from unidentified equipment.\nOrdinary satisfaction subtracts that amount from hunger, stopping at zero. Excess is not stored as extra HP or an unlimited food reserve. The hunger-rate setting changes hunger accumulation rather than multiplying this positive nutrition. Meal-related talents and special food effects follow their separate rules.\nThe current action-time preview uses this food's actual eating-time rule. Normal food uses "+ObfuscationExplanation.number(Food.TIME_TO_EAT)+" turns, shortened by qualifying meal talents; berries and Supply Rations have their own faster rule. Any zero shown comes from an existing food/talent rule. It is not a free action added by this description. The food is still consumed, and other actors and hunger advance according to the actual elapsed game time. Random food effects are not rolled during inspection.";
    }
}

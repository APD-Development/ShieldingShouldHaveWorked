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

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Blandfruit;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.*;

/** Visible cooked-fruit effects; never eats, throws, identifies a potion or rolls an outcome. */
public final class BlandfruitExplanation {
    private BlandfruitExplanation(){}
    private static boolean recognized(Blandfruit fruit){
        return fruit.potionAttrib!=null&&Potion.SeedToPotion.types.containsValue(fruit.potionAttrib.getClass());
    }
    public static boolean releasesEffectWhenThrown(Blandfruit fruit){
        Potion p=fruit.potionAttrib;
        return p instanceof PotionOfLiquidFlame||p instanceof PotionOfToxicGas
                ||p instanceof PotionOfParalyticGas||p instanceof PotionOfFrost
                ||p instanceof PotionOfLevitation||p instanceof PotionOfPurity;
    }
    private static boolean harmfulMeal(Blandfruit fruit){
        Potion p=fruit.potionAttrib;
        return p instanceof PotionOfLiquidFlame||p instanceof PotionOfToxicGas
                ||p instanceof PotionOfParalyticGas||p instanceof PotionOfFrost;
    }
    private static String n(float value){return ObfuscationExplanation.number(value);}
    public static String summary(Blandfruit fruit){
        if(fruit.potionAttrib==null)return "Uncooked fruit cannot be eaten.";
        if(!recognized(fruit))return "Detailed effects are unavailable for this imbued fruit.";
        if(harmfulMeal(fruit))return "_Eating releases its harmful effect on your own tile._ Throwing places the effect at the landing tile instead; you and nearby creatures can still be affected.";
        if(fruit.potionAttrib instanceof PotionOfHealing){
            if(Dungeon.isChallenged(Challenges.NO_HEALING))return "_Pharmacophobia: eating offers Poison instead of healing_, after its normal supported ailment cure.";
            return "Eating offers gradual healing and cures its supported ailments. Missing health and healing modifiers affect the result.";
        }
        if(fruit.potionAttrib instanceof PotionOfStrength)return "Eating permanently adds _1 base Strength point_. It does not immediately heal HP or upgrade equipment.";
        if(fruit.potionAttrib instanceof PotionOfExperience)return "Eating grants experience toward your next level; this is separate from its nutrition.";
        if(fruit.potionAttrib instanceof PotionOfInvisibility)return "Eating offers Invisibility for _"+n(Invisibility.DURATION)+" game-time units_; supported attacks and other actions can end it early.";
        if(fruit.potionAttrib instanceof PotionOfHaste)return "Eating offers Haste for _"+n(Haste.DURATION)+" game-time units_, speeding movement rather than every action.";
        if(fruit.potionAttrib instanceof PotionOfMindVision)return "Eating offers Mind Vision for _"+n(MindVision.DURATION)+" game-time units_, sensing eligible creatures under its normal rules.";
        if(fruit.potionAttrib instanceof PotionOfLevitation)return "Eating offers Levitation for _"+n(Levitation.DURATION)+" game-time units_. Throwing instead releases Confusion Gas and leaves edible chunks on ordinary ground.";
        if(fruit.potionAttrib instanceof PotionOfPurity)return "Eating offers protection against supported environmental clouds for _"+n(BlobImmunity.DURATION)+" game-time units_. Throwing instead clears supported nearby clouds and leaves edible chunks.";
        return "Its visible cooked form determines its extra effect.";
    }
    public static String details(Blandfruit fruit,Hero hero){
        if(fruit.potionAttrib==null)return "Uncooked fruit cannot be eaten.";
        if(!recognized(fruit))return "Detailed effects are unavailable for this imbued fruit; no hidden effect is guessed.";
        String effect=PotionExplanation.effect(fruit.potionAttrib,hero);
        String text="_Cooked fruit: "+fruit.name()+"_\n"+effect;
        if(fruit.potionAttrib instanceof PotionOfHealing&&Dungeon.isChallenged(Challenges.NO_HEALING))
            text+="\n_Current challenge: Pharmacophobia._ The healing branch instead offers Poison"+(hero==null?" with a duration based on hero level.":" for "+(4+hero.lvl/2)+" game-time units, from 4 + floor(hero level / 2).")+" This duration is not guaranteed total damage; normal status and damage rules apply.";
        if(harmfulMeal(fruit))text+="\n_Eating is hazardous:_ it releases the described shattering effect at your own location after the food action. The nutrition does not make that effect safe. Immunities and exposure keep their normal rules.";
        else text+="\nEating applies the described benefit after its normal food action. The food's displayed meal time applies; no separate one-turn potion-drinking action is added.";
        if(releasesEffectWhenThrown(fruit)){
            text+="\nThrowing onto ordinary ground releases this form's shattering effect at the landing tile and leaves _1 portion of edible fruit chunks_. Chunks offer "+n(FoodExplanation.nutritionBeforeHorn(Hunger.STARVING))+" hunger points before a cursed Horn and the fully-fed limit. The potion effect and that later meal are separate; the thrown fruit does not feed you immediately. Wells and pits instead keep their ordinary item-drop/fall behavior.";
        }else text+="\nThrowing normally drops the _whole fruit_ without applying its eating effect or creating chunks. Wells and pits keep their ordinary item-drop/fall behavior.";
        return text+"\nInspection does not eat, throw, spend time or identify other potions. The publicly visible fruit type supplies this explanation.";
    }
}

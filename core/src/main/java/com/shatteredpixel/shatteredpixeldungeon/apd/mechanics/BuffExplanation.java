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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

/** Additional reviewed status rules, with no random rolls or effect-handler calls. */
public final class BuffExplanation {
    private BuffExplanation(){}
    private static String n(float x){return ObfuscationExplanation.number(x);}
    public static String details(Buff buff){
        if(buff instanceof Healing){Healing healing=(Healing)buff;return "_Healing requests and pool_\nThe next unmodified healing request is "+healing.nextRequestedHealing()+" HP. Remaining unmodified pool: "+healing.remainingHealingPool()+" HP. While its pool is positive, each tick requests clamp(round(remaining pool × rate) + flat amount, 1, remaining pool). Current rate: "+n(100*healing.healingRate())+"% of the remaining pool; flat amount: "+healing.flatHealing()+" HP, with the applicable Blood Vial per-tick limit afterward. "+(healing.healingPerTickLimit()==Integer.MAX_VALUE?"No Blood Vial limit applies.":"Current Blood Vial limit: "+healing.healingPerTickLimit()+" HP per tick.")+" Normal healing rules and missing HP determine actual HP gained. The pool is spent using the unmodified request, not a promise of that many HP restored under every rule. Reapplications keep the larger remaining pool and the stronger rate/flat components rather than simply adding every heal.";}
        if(buff instanceof Burning){Burning burning=(Burning)buff;return "_Burning damage and timing_\nRemaining duration: "+n(burning.apdRemaining())+" turns. The ordinary unmodified damage roll is 1–"+burning.apdRandomMaximum()+" damage points, biased toward the middle, before source-specific and target modifiers. The maximum uses 3 + floor(scaling depth / 4); it is not a fixed percentage of HP. Water can remove Burning, but removal does not undo a tick already resolved. Burning immunity prevents the damage, and an active Brimstone glyph can separately turn attempted ignition into Barrier shielding. Fire can also affect exposed burnable items; protected containers and other item rules matter.";}
        if(buff instanceof Corrosion){Corrosion corrosion=(Corrosion)buff;return "_Corrosion strength_\nNext unmodified tick input: "+corrosion.apdNextRaw()+" damage points. Remaining duration: "+n(corrosion.apdRemaining())+" turns. After each tick, strength rises by 1 while below floor(scaling depth / 2) + 2; afterward it rises by 0.5. The tick amount is truncated to an integer, so a half-point increase need not change every displayed tick. Source modifiers, immunity and defenses determine final HP loss. New applications keep the stronger strength and longer remaining duration; the latest source policy still applies.";}
        if(buff instanceof Ooze){Ooze ooze=(Ooze)buff;return "_Ooze damage and timing_\nThe next ordinary unmodified input under its duration/floor rule is "+ooze.apdNextRaw()+" damage points before washing-off and source/target modifiers. Remaining duration: "+n(ooze.apdRemaining())+" turns. Above scaling depth5, raw damage is 1 + floor(depth / 5) per tick; at depth5 it is one per tick; below that it is one only when the integer remaining duration is even. Water washing and a newly applied effect's first-tick ordering matter; zero on an alternating tick does not mean the effect is permanently harmless.";}
        if(buff instanceof AdrenalineSurge){AdrenalineSurge surge=(AdrenalineSurge)buff;return "_Temporary strength_\nCurrent added strength: "+surge.boost()+" points. Its next scheduled weakening is in "+n(Math.max(0,surge.visualcooldown()))+" game-time units. Each weakening removes one strength point, then schedules the next after "+n(surge.weakeningInterval())+" turns while any boost remains. These points affect strength-based mechanics; they are not that many percent more damage or health.";}
        if(buff instanceof WandEmpower){WandEmpower empower=(WandEmpower)buff;return "_Wand damage contribution_\nAdds "+empower.dmgBoost+" damage points to each of the next "+empower.left+" qualifying direct-wand damage rolls. This is added damage, not an upgrade level or a percentage. The counter is consumed by those rolls, so a multi-target/multi-roll action can differ from one simple zap; indirect damage sources do not all use this path. A new application replaces the bonus amount and keeps the larger remaining-roll count.";}
        if(buff instanceof Degrade){StringBuilder result=new StringBuilder("_Degradation curve_\nAt this stage, levels ≤ 0 are unchanged. Positive levels use round(sqrt(2 × (level − 1)) + 1). This is a rounded square-root curve, not exponential decay. It changes effective upgrade benefits rather than permanently removing upgrades. Strength requirements, normal wand-charge capacity, ordinary thrown durability and artifacts keep their separate rules. Other temporary bonuses can apply at other stages.\n");
            if(buff.target==Dungeon.hero&&buff.target instanceof Hero){Hero hero=(Hero)buff.target;for(Item item:new Item[]{hero.belongings.weapon(),hero.belongings.armor(),hero.belongings.ring(),hero.belongings.misc()})if(item!=null&&!(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact)&&item.levelKnown)result.append("\n").append(item.name()).append(": known level ").append(item.level()).append("; degradation-only result ").append(Degrade.reduceLevel(item.level())).append(" before other bonuses.");}
            return result.toString();}
        if(buff instanceof Doom)return "_Doom's damage stage_\nEligible incoming damage is multiplied by 1.67 at Doom's stage, before later source-resistance, rounding and protection steps. It does not add a flat 67 damage points, bypass immunity, or guarantee 67% more final HP loss. Immunity to Doom prevents this modifier. The effect has no ordinary duration countdown.";
        if(buff instanceof Corruption)return "_Corruption's gradual harm_\nEach normal tick adds target maximum HP / 100 to a damage accumulator. Its whole damage points are sent to the normal damage handler and its fractional remainder is retained. This is base 1% maximum HP per tick, not 1% of the HP currently left or a guaranteed fixed lifetime through every defense. Corruption also changes allegiance; the original attack and target rules still apply.";
        if(buff instanceof Berserk){Berserk rage=(Berserk)buff;return "_Rage and damage_\nCurrent rage: "+n(100*rage.rageFraction())+"%. Its damage factor is min(1.5, 1 + rage / 2), where 100% rage means rage = 1; current factor: "+n(rage.damageFactor(1))+" times normal at this step. Rage above 100% does not raise that factor past 1.5. The berserk shield formula starts from 8 + 2 × effective armor level (zero armor contribution when no armor is available) and multiplies by 1 + 2 × (missing-HP fraction)³, then by rage when rage exceeds1, with integer rounding. Active berserk shield loss is based on total current shielding, not a fixed amount: ceil(total shield × 0.025), modified by Hold Fast and fractional rounding. Shield/HP, armor, recovery and other effects still follow their own rules.";}
        return "";
    }
}

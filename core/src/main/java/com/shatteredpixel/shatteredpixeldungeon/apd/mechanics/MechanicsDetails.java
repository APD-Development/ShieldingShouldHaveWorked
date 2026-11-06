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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import java.util.Locale;

/** Public-value explanations; never asks for a damage roll, hidden enemy stat, or proc. */
public final class MechanicsDetails {
    private MechanicsDetails(){}
    public static String glossary(String description){
        String text=description.toLowerCase(Locale.ROOT);StringBuilder out=new StringBuilder();
        if(text.contains("accuracy")||text.contains("evasion"))out.append("_Accuracy and evasion_\nThese are competing scores, not hit/dodge percentages. A normal attack rolls a value from zero up to the attacker's final accuracy and another from zero up to the defender's final evasion. The attack hits when its roll is at least the defense roll.\nFor positive finite scores after applicable modifiers: if accuracy ≥ evasion, hit chance = 1 − evasion / (2 × accuracy); otherwise hit chance = accuracy / (2 × evasion). Guaranteed hits/dodges and other special rules override this normal contest. The target and current effects matter, so a weapon's accuracy percentage is not a fixed chance to hit.\n\n");
        if(text.contains("shield"))out.append("_Shielding_\nShield points are temporary damage protection, separate from maximum HP. Damage which uses shielding consumes those points before health. A shield's own description determines its duration, decay, replacement or stacking; different shield sources need not follow the same rule. This is not a percentage reduction or permanent healing.\n\n");
        if(text.contains("turn")||text.contains("speed")||text.contains("recharg"))out.append("_Time and speed_\nTurns measure game time, not seconds on your phone. An ordinary wait uses one unit of game time; actions, talents and other effects can cost different amounts. A speed or recharge-rate factor multiplies that rate, so increasing a rate shortens the time needed for the same action or charge. Movement, attack speed and recharge are separate rates. The visual Game speed setting does not change these costs.\n\n");
        if(text.contains("chance")||text.contains("trigger"))out.append("_Conditional chances_\nA chance applies only when the effect's stated trigger and other requirements are met. It is not a guarantee against immunity or an unavailable target. Repeated checks are separate opportunities; a per-hit chance is not a per-second chance. Where an effect uses power beyond a guaranteed trigger, its own rule says what becomes stronger.\n\n");
        if(text.contains("damage"))out.append("_Damage numbers_\nDamage ranges are amounts of damage, not percentages or a promise of final HP loss. Equipment, the target's defenses, resistance, shielding and the specific effect's rules can change the result. A larger minimum improves weaker rolls; a larger maximum improves the upper end. Raw weapon damage, armor absorption and damage-over-time are different quantities.\n\n");
        if(text.contains("strength"))out.append("_Strength_\nStrength is a score measured in points. An equipment requirement is a minimum, not a percentage. Being below it can impose the penalties described by that equipment; excess strength only grants benefits where the relevant mechanic explicitly uses it.\n\n");
        if(text.contains("multiplier")||text.contains("×")||text.contains("1x"))out.append("_Added amounts and multiplied amounts_\nAn added bonus increases a score by that amount. A multiplier scales the stated quantity: a factor above one increases it, below one reduces it. Do not add two percentage bonuses together unless that effect's rule says to; different effects can combine at different stages. Caps and rounding belong to the particular effect.\n\n");
        return out.toString().trim();
    }
    public static String item(Item item,String visibleDescription){
        StringBuilder result=new StringBuilder();
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring)result.append(RingExplanation.details((com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring)item)).append("\n\n");
        if(item instanceof Armor){
            Armor armor=(Armor)item;
            if(ObfuscationExplanation.applies(armor))result.append(ObfuscationExplanation.details(armor,Dungeon.hero)).append("\n\n");
            if(armor.levelKnown)result.append("_Armor's current base absorption_\n").append(armor.DRMin()).append("–").append(armor.DRMax()).append(" damage points per applicable hit before other defensive effects. Strength requirement: ").append(armor.STRReq()).append(" points. Upgrades and augmentation change these displayed values; this range is not a flat reduction percentage.\n\n");
            else result.append("Armor-level calculations stay hidden until its upgrades are identified.\n\n");
        }
        if(item instanceof Weapon)result.append("_Weapon's displayed values_\nThe damage range is the weapon's base damage before the target's defenses and other bonuses. The accuracy number is the weapon's contribution to an attack score, not a hit probability. Upgrade and ring comparisons above describe the stated contribution; they do not include every target, buff or dungeon rule. Unidentified upgrade-dependent calculations remain hidden.\n\n");
        String common=glossary(visibleDescription);if(!common.isEmpty())result.append(common);
        return result.toString().trim();
    }
    public static String buff(Buff buff,String visibleDescription){
        String extra="";
        if(buff instanceof ShieldBuff)extra="_Current shield_\n"+((ShieldBuff)buff).shielding()+" shield points from this effect.\n\n";
        if(buff instanceof Poison){Poison poison=(Poison)buff;extra+="_Next scheduled poison tick_\n"+poison.apdNextInput()+" damage points are currently scheduled to be sent to the target before its final defenses/shielding. Remaining poison duration: "+ObfuscationExplanation.number(poison.apdRemaining())+" turns. This is not a promise of that much HP loss.\n\n";}
        if(buff instanceof Bleeding){Bleeding bleeding=(Bleeding)buff;extra+="_Bleeding calculation_\nThe next unmodified tick rounds the current strength to "+bleeding.apdNextRaw()+" damage points. Source-specific rules can change the amount sent to the target. After a tick, strength is randomly reduced to between half and all of its previous value, with results biased toward the middle. It ends when the rounded strength reaches zero. Normal new applications keep the stronger randomly reduced strength; they do not simply add their totals.\n\n";}
        if(buff instanceof Barkskin)extra+="_Current Barkskin roll_\nAdds a protection roll from 0 to "+((Barkskin)buff).level()+" damage points on an applicable physical hit. This adds damage-blocking points rather than a percentage reduction.\n\n";
        if(buff instanceof ArcaneArmor)extra+="_Current Arcane Armor roll_\nAdds a protection roll from 0 to "+((ArcaneArmor)buff).level()+" damage points against qualifying magical damage, before the remaining defenses apply.\n\n";
        if(buff instanceof Barrier){
            Barrier barrier=(Barrier)buff;float factor=1;
            if(buff.target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero){
                com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero owner=(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero)buff.target;HoldFast hold=owner.buff(HoldFast.class);
                if(hold!=null&&owner.pos==hold.pos){int points=owner.pointsInTalent(Talent.HOLD_FAST);factor=points==1?.5f:points==2?.25f:points==3?0:1;}
            }
            float rate=Math.min(1f,barrier.shielding()/20f)*factor;
            extra+="_Barrier decay_\nEach normal barrier tick adds min(1, current shield / 20) × the active Hold Fast factor toward losing one shield point. With the current state, that is "+ObfuscationExplanation.number(rate)+" per tick. Progress toward the next point lost is currently "+ObfuscationExplanation.number(100*barrier.decayProgress())+"%. At one whole point of progress, one shield point is removed and that progress resets. Incoming damage can also consume shielding.\n\n";
        }
        if(buff instanceof FlavourBuff)extra+="_Current displayed duration_\n"+ObfuscationExplanation.number(Math.max(0,buff.visualcooldown()))+" game-time units. The icon can round this display; this is not real-world seconds.\n\n";
        return (extra+glossary(visibleDescription)).trim();
    }
    public static String talent(Talent talent,int points,String visibleDescription){
        return "_Talent rank_\nCurrent rank: "+points+" / "+talent.maxPoints()+". The description's rank-specific amounts are the benefits for those ranks, not amounts to add across every rank unless explicitly stated. Conditional effects apply only when their requirements are met.\n\n"+glossary(visibleDescription);
    }
}

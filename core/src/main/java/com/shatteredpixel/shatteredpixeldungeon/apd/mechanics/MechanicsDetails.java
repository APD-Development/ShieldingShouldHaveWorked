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
    private static float holdFastDecay(com.shatteredpixel.shatteredpixeldungeon.actors.Char target){
        if(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero){
            com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero owner=(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero)target;HoldFast hold=owner.buff(HoldFast.class);
            if(hold!=null&&owner.pos==hold.pos){int points=owner.pointsInTalent(Talent.HOLD_FAST);return points==1?.5f:points==2?.25f:points==3?0:1;}
        }
        return 1;
    }
    public static String glossary(String description){
        String text=description.toLowerCase(Locale.ROOT);StringBuilder out=new StringBuilder();
        if(text.contains("accuracy")||text.contains("evasion"))out.append("_Accuracy and evasion_\nThese are competing scores, not hit/dodge percentages. A normal attack rolls a value from zero up to the attacker's final accuracy and another from zero up to the defender's final evasion. The attack hits when its roll is at least the defense roll.\nFor positive finite scores after applicable modifiers, the usual continuous-roll estimate is: if accuracy ≥ evasion, hit chance = 1 − evasion / (2 × accuracy); otherwise hit chance = accuracy / (2 × evasion). Guaranteed hits/dodges and other special rules override this normal contest. The game uses finite-precision rolls and rounded scores. The target and current effects matter, so a weapon's accuracy percentage is not a fixed chance to hit.\n\n");
        if(text.contains("shield"))out.append("_Shielding_\nShield points are temporary damage protection, separate from maximum HP. Damage which uses shielding consumes those points before health. A shield's own description determines its duration, decay, replacement or stacking; different shield sources need not follow the same rule. This is not a percentage reduction or permanent healing.\n\n");
        if(text.contains("turn")||text.contains("speed")||text.contains("recharg"))out.append("_Time and speed_\nTurns measure game time, not seconds on your phone. An ordinary wait uses one unit of game time; actions, talents and other effects can cost different amounts. A speed or recharge-rate factor multiplies that rate, so increasing a rate shortens the time needed for the same action or charge. Movement, attack speed and recharge are separate rates. The visual Game speed setting does not change these costs.\n\n");
        if(text.contains("chance")||text.contains("trigger"))out.append("_Conditional chances_\nA chance applies only when the effect's stated trigger and other requirements are met. It is not a guarantee against immunity or an unavailable target. Repeated checks are separate opportunities; a per-hit chance is not a per-second chance. Where an effect uses power beyond a guaranteed trigger, its own rule says what becomes stronger.\n\n");
        if(text.contains("damage"))out.append("_Damage numbers_\nDamage ranges are amounts of damage, not percentages or a promise of final HP loss. Equipment, the target's defenses, resistance, shielding and the specific effect's rules can change the result. A larger minimum improves weaker rolls; a larger maximum improves the upper end. Raw weapon damage, armor absorption and damage-over-time are different quantities.\n\n");
        if(text.contains("strength"))out.append("_Strength_\nStrength is a score measured in points. An equipment requirement is a minimum, not a percentage. Being below it can impose the penalties described by that equipment; excess strength only grants benefits where the relevant mechanic explicitly uses it.\n\n");
        if(text.contains("multiplier")||text.contains("×")||text.contains("1x"))out.append("_Added amounts and multiplied amounts_\nAn added bonus increases a score by that amount. A multiplier scales the stated quantity: a factor above one increases it, below one reduces it. Do not add two percentage bonuses together unless that effect's rule says to; different effects can combine at different stages. Caps and rounding belong to the particular effect.\n\n");
        return out.toString().trim();
    }
    /** Owner-approved complex item effects; never the generic item/glossary dispatcher. */
    public static String itemCalculations(Item item){
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana
                ||item instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce
                ||item instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity)
            return item.isIdentified()?RingExplanation.details((com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring)item):"";
        // Owner explicitly retained existing artifact/trinket-specific panels pending review.
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact)
            return item.isIdentified()?ArtifactExplanation.details((com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact)item):"";
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket)
            return item.isIdentified()?TrinketExplanation.details((com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket)item):"";
        // Separate ward lifecycles and cumulative regrowth depletion are genuinely multi-stage.
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding
                ||item instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth)
            return item.isIdentified()?WandExplanation.details((com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand)item):"";
        if(item instanceof Armor){
            Armor armor=(Armor)item;
            return armor.levelKnown&&armor.glyph!=null&&!armor.glyph.curse()?GlyphExplanation.details(armor,Dungeon.hero):"";
        }
        if(item instanceof Weapon){
            Weapon weapon=(Weapon)item;
            if(!weapon.levelKnown||weapon.enchantment==null||weapon.enchantment.curse())return "";
            if(weapon instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart)
                return com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart.inspect(()->EnchantmentExplanation.describe(weapon,Dungeon.hero,true));
            return EnchantmentExplanation.describe(weapon,Dungeon.hero,true);
        }
        return "";
    }
    public static String item(Item item,String visibleDescription){
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart)
            return com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart.inspect(()->itemInContext(item,visibleDescription));
        return itemInContext(item,visibleDescription);
    }
    private static String itemInContext(Item item,String visibleDescription){
        StringBuilder result=new StringBuilder();
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.Food)result.append(FoodExplanation.details((com.shatteredpixel.shatteredpixeldungeon.items.food.Food)item)).append("\n\n");
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand)result.append(WandExplanation.details((com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand)item)).append("\n\n");
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact)result.append(ArtifactExplanation.details((com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact)item)).append("\n\n");
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring)result.append(RingExplanation.details((com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring)item)).append("\n\n");
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket)result.append(TrinketExplanation.details((com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket)item)).append("\n\n");
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion)result.append(PotionExplanation.details((com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion)item,Dungeon.hero)).append("\n\n");
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll)result.append(ScrollExplanation.details((com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll)item,Dungeon.hero)).append("\n\n");
        result.append(UtilityItemExplanation.details(item,Dungeon.hero)).append("\n\n");
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb)result.append(BombExplanation.details((com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb)item)).append("\n\n");
        result.append(CraftingEffectExplanation.details(item)).append("\n\n");
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed)result.append(PlantExplanation.details(((com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed)item).plantType(),Dungeon.hero)).append("\n\n");
        if(item instanceof Armor){
            Armor armor=(Armor)item;
            if(armor instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor && Dungeon.hero!=null && Dungeon.hero.armorAbility!=null)
                result.append(AbilityExplanation.details(Dungeon.hero.armorAbility,Dungeon.hero)).append("\n\n");
            result.append(GlyphExplanation.details(armor,Dungeon.hero)).append("\n\n").append(CurseExplanation.armor(armor,Dungeon.hero,true)).append("\n\n");
            result.append(ArmorExplanation.details(armor,Dungeon.hero)).append("\n\n");
        }
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff)
            result.append(((com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff)item).mechanicsDetails()).append("\n\n");
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon)result.append(WeaponAbilityExplanation.details((com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon)item,Dungeon.hero)).append("\n\n");
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon)result.append(MissileExplanation.details((com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon)item,Dungeon.hero)).append("\n\n");
        if(item instanceof Weapon)result.append(WeaponExplanation.details((Weapon)item,Dungeon.hero)).append("\n\n").append(EnchantmentExplanation.describe((Weapon)item,Dungeon.hero,true)).append("\n\n").append(CurseExplanation.weapon((Weapon)item,Dungeon.hero,true)).append("\n\n");
        String common=glossary(visibleDescription);if(!common.isEmpty())result.append(common);
        return result.toString().trim();
    }
    public static String buff(Buff buff,String visibleDescription){
        String extra=BuffExplanation.details(buff);
        if(buff instanceof Combo){
            Combo combo=(Combo)buff;int count=combo.getComboCount();
            extra+="\n\n_Combo count and remaining window_\nCurrent count: "+count+" successful combo hits; remaining timer: "+ObfuscationExplanation.number(combo.remainingComboTime())+" game-time units. Each scheduled update normally subtracts 1. With the current position and Hold Fast benefit, the next update subtracts "+ObfuscationExplanation.number(holdFastDecay(buff.target))+". Inspecting does not consume a finisher, spend the timer or end an out-of-position Hold Fast benefit.\nAn ordinary hit keeps at least a five-unit window under its own rule. A qualifying kill replaces it with the Cleave window; it does not add that window. Finisher requirements are counts, not charges or weapon upgrade levels: Clobber "+Combo.ComboMove.CLOBBER.comboReq+", Slam "+Combo.ComboMove.SLAM.comboReq+", Parry "+Combo.ComboMove.PARRY.comboReq+", Crush "+Combo.ComboMove.CRUSH.comboReq+", Fury "+Combo.ComboMove.FURY.comboReq+". Clobber and Parry each have their own already-used restriction.\nSlam adds round(a defense roll × count / 5) damage before later attack processing; it does not multiply every damage source by that amount. Crush uses count / 4 as its direct attack damage factor; at your current count that is "+ObfuscationExplanation.number(.25f*count)+" times the normal attack input. Fury can attempt up to the combo count in strikes, each using a 0.6 attack-damage factor, and can end early if the target dies, leaves reach or the hero is paralysed. These are not promises of final HP loss. Ordinary finisher hit rules, immunity, defenses and other effects still apply.";
        }

        if(buff instanceof Talent.LiquidAgilACCTracker && Dungeon.hero!=null){
            extra+="\n\n_Remaining potion accuracy uses_\n"+((Talent.LiquidAgilACCTracker)buff).uses+" qualifying attack uses remain.\n"+TalentExplanation.details(Talent.LIQUID_AGILITY,Dungeon.hero.pointsInTalent(Talent.LIQUID_AGILITY));
        }
        if(buff instanceof Talent.PreciseAssaultTracker && Dungeon.hero!=null)extra+="\n\n"+TalentExplanation.details(Talent.PRECISE_ASSAULT,Dungeon.hero.pointsInTalent(Talent.PRECISE_ASSAULT));
        if(!extra.isEmpty())extra+="\n\n";
        if(buff instanceof Haste)extra+="_Movement contribution_\nHaste multiplies movement speed by 3, so an otherwise unchanged step costs one third of the game time. It does not multiply attack or recharge rates. Other movement modifiers can still combine with it.\n\n";
        if(buff instanceof Slow)extra+="_Action-time contribution_\nSlow sets the slow/chill action-rate factor to 0.5; an otherwise unchanged action costs twice as much game time. Slow and Chill do not multiply together: Slow takes priority. A separate all-action Speed effect multiplies the rate by 2 and can offset this factor.\n\n";
        if(buff instanceof Speed)extra+="_Action-time contribution_\nSpeed multiplies the all-action rate by 2, so an otherwise unchanged action costs half the game time. It combines multiplicatively with the active Slow/Chill rate, not by adding percentage points. Movement and attack-specific bonuses can also affect the original action cost.\n\n";
        if(buff instanceof Chill){Chill chill=(Chill)buff;extra+="_Current Chill calculation_\nChill's action-rate factor = max(0.5, 1 − remaining scheduled turns × 0.1). Current factor: "+ObfuscationExplanation.number(chill.speedFactor())+"; divide an otherwise unchanged action's time by this factor. The slowdown cannot take the rate below half normal. Slow takes priority if both are present, rather than multiplying both penalties.\n\n";}
        if(buff instanceof Weakness)extra+="_Attack-damage contribution_\nQualifying attack damage is multiplied by 0.67 before the target's armor stage. Other attack modifiers apply at their own stages, then the amount is rounded before defense processing. This does not remove strength-stat points or directly raise equipment requirements. Final HP loss can change differently because armor, shielding and other effects follow.\n\n";
        if(buff instanceof Vulnerable)extra+="_Incoming attack-damage contribution_\nAfter the attack's armor subtraction, qualifying remaining attack damage is multiplied by 1.33 and truncated to an integer before later attack effects. This is not an added 33 damage points or a guarantee of 33% more final HP loss from every damage source.\n\n";
        if(buff instanceof Recharging){Recharging recharge=(Recharging)buff;extra+="_Wand charge contribution_\nFor an eligible wand charger tick, this effect adds 0.25 × min(1, remaining scheduled duration) partial charge when that remainder is positive. Current positive contribution before the wand's capacity: "+ObfuscationExplanation.number(.25f*Math.max(0,recharge.remainder()))+" charge units. This is an added amount on top of normal charging, not a multiplied recharge rate. Full wands do not gain unlimited charges.\n\n";}
        if(buff instanceof ArtifactRecharge){ArtifactRecharge recharge=(ArtifactRecharge)buff;extra+="_Artifact recharge contribution_\nRemaining recharge duration: "+ObfuscationExplanation.number(Math.max(0,recharge.left()))+" turns. On an eligible tick, each supported uncursed equipped artifact receives min(1, remaining duration) recharge power. Each artifact converts that power using its own rule; it is not necessarily one charge or a universal speed multiplier. "+(recharge.ignoreHornOfPlenty?"The Horn of Plenty is excluded by this effect.":"The Horn of Plenty is not excluded by this effect.")+"\n\n";}
        if(buff instanceof ShieldBuff)extra+="_Current shield_\n"+((ShieldBuff)buff).shielding()+" shield points from this effect.\n\n";
        if(buff instanceof Poison){Poison poison=(Poison)buff;extra+="_Next scheduled poison tick_\n"+poison.apdNextInput()+" damage points are currently scheduled to be sent to the target before its final defenses/shielding. Remaining poison duration: "+ObfuscationExplanation.number(poison.apdRemaining())+" turns. This is not a promise of that much HP loss.\n\n";}
        if(buff instanceof Bleeding){Bleeding bleeding=(Bleeding)buff;extra+="_Bleeding calculation_\nThe next unmodified tick rounds the current strength to "+bleeding.apdNextRaw()+" damage points. Source-specific rules can change the amount sent to the target. After a tick, strength is randomly reduced to between half and all of its previous value, with results biased toward the middle. It ends when the rounded strength reaches zero. Normal new applications keep the stronger randomly reduced strength; they do not simply add their totals.\n\n";}
        if(buff instanceof Barkskin)extra+="_Current Barkskin roll_\nAdds a protection roll from 0 to "+((Barkskin)buff).level()+" damage points on an applicable physical hit. This adds damage-blocking points rather than a percentage reduction.\n\n";
        if(buff instanceof ArcaneArmor)extra+="_Current Arcane Armor roll_\nAdds a protection roll from 0 to "+((ArcaneArmor)buff).level()+" damage points against qualifying magical damage, before the remaining defenses apply.\n\n";
        if(buff instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blocking.BlockBuff){
            com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blocking.BlockBuff block=(com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blocking.BlockBuff)buff;
            extra+="_Blocking shield duration_\nRemaining baseline decay units: "+ObfuscationExplanation.number(block.remainingDecay())+". A normal tick consumes one unit; active Hold Fast changes this rate. Current decay rate: "+ObfuscationExplanation.number(holdFastDecay(buff.target))+" units per tick. At zero units the shield expires; damage can consume it sooner. A new application resets its duration to five units, rather than promising five turns regardless of other effects.\n\n";
        }
        if(buff instanceof Barrier){
            Barrier barrier=(Barrier)buff;float factor=holdFastDecay(buff.target);
            float rate=Math.min(1f,barrier.shielding()/20f)*factor;
            extra+="_Barrier decay_\nEach normal barrier tick adds min(1, current shield / 20) × the active Hold Fast factor toward losing one shield point. With the current state, that is "+ObfuscationExplanation.number(rate)+" per tick. Progress toward the next point lost is currently "+ObfuscationExplanation.number(100*barrier.decayProgress())+"%. At one whole point of progress, one shield point is removed and that progress resets. Incoming damage can also consume shielding.\n\n";
        }
        if(buff instanceof FlavourBuff&&!extra.isEmpty())extra+="_Current displayed duration_\n"+ObfuscationExplanation.number(Math.max(0,buff.visualcooldown()))+" game-time units. Time until this effect's scheduled update: "+ObfuscationExplanation.number(Math.max(0,buff.cooldown()))+" game-time units. The standard timed-effect counter adds one because these updates follow character actions; individual effects can use a different display rule. Action costs can differ from one turn, and the icon can round its number. These are not real-world seconds.\n\n";
        return extra.trim();
    }
    public static String talent(Talent talent,int points,String visibleDescription){
        return talent(talent,points,visibleDescription,false);
    }
    public static String talent(Talent talent,int points,String visibleDescription,boolean alternateDescription){
        String specific=TalentExplanation.details(talent,points,alternateDescription);
        if(!specific.isEmpty())specific+="\n\n";
        if(talent==Talent.ENHANCED_RINGS)specific="_Enhanced Rings calculation_\nQualifying artifact use grants +1 effective ring level for 3 × talent rank = "+(3*points)+" turns. This is temporary, not an actual upgrade. Effects using ordinary rather than effective ring level, such as Might's strength points and Sharpshooting's durability, do not gain that extra level. Rank zero grants no effect.\n\n";
        if(talent==Talent.PROTECTIVE_SHADOWS)specific="_Protective Shadows calculation_\nWhile invisible and below this talent's shield threshold, its tracker adds 0.5 × rank toward the next shield point each tick. Threshold at this rank: "+(1+2*points)+" shield points. At a whole point of progress it grants one point and resets progress. This limits new gain from this talent, not larger shielding supplied by other effects. The tracker refreshes barrier decay while it runs. Rank zero grants no effect.\n\n";
        if(talent==Talent.SILENT_STEPS)specific="_Sleeping-enemy condition_\nThe standard sleeping-enemy check does not wake for your footsteps at distance ≥ 4 − rank tiles. This is a condition on that sleeping check, not invisibility, immunity to alarms or a rule making already-alert enemies forget you. Rank zero grants no effect.\n\n";
        if(!alternateDescription&&points>0){String spell=SpellExplanation.talentDetails(talent,com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero);if(!spell.isEmpty())specific+=spell+"\n\n";}
        if(specific.isEmpty())return "";
        return "_Talent rank_\nCurrent rank: "+points+" / "+talent.maxPoints()+". The description's rank-specific amounts are the benefits for those ranks, not amounts to add across every rank unless explicitly stated. Conditional effects apply only when their requirements are met.\n\n"+specific;
    }
}

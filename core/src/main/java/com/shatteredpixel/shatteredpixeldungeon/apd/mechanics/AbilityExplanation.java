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
        if(ability instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower){
            int growing=hero.pointsInTalent(Talent.GROWING_POWER),wrath=hero.pointsInTalent(Talent.NATURES_WRATH),momentum=hero.pointsInTalent(Talent.WILD_MOMENTUM);
            return "\n\n_Movement and bow speed_\nActivation spends one game-time unit and offers "+n(com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower.naturesPowerTracker.DURATION)+" scheduled units of Nature's Power. Its movement-speed factor is 2 + 0.25 × Growing Power rank = "+n(2+.25f*growing)+". With other conditions unchanged, a step takes the usual time divided by that factor.\nThe Spirit Bow instead adds (8 + Growing Power rank) / 24 = "+n((8+growing)/24f)+" to its attack-speed factor after its other speed effects. This is added to that factor, rather than multiplying every attack's speed by the movement factor. Bow attack delay uses the resulting rate, and normal augment and special-shot rules still apply.\nDuring an eligible ordinary bow hit, Nature's Wrath checks rank / 12 = "+n(100f*wrath/12f)+"% for one of Blindweed, Firebloom, Icecap, Sorrowmoss or Stormvine effects. A qualifying defeated target can extend the effect by Wild Momentum rank = "+momentum+" scheduled units, at most twice per activation; zero-rank extensions do not spend an opportunity. Other target and plant rules still apply.";
        }
        if(ability instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk){
            int eagle=hero.pointsInTalent(Talent.EAGLE_EYE),swift=hero.pointsInTalent(Talent.SWIFT_SPIRIT),eyes=hero.pointsInTalent(Talent.GO_FOR_THE_EYES);
            return "\n\n_Hawk movement, sight and attacks_\nSummoning spends one game-time unit and needs a free adjacent passable tile; commands to an existing hawk are separate. A new hawk has 10 maximum HP, a base accuracy score of 60, a base evasion score of 60 and a middle-favoring 5–10 damage roll before normal attack modifiers and defenses. These scores are not hit or dodge percentages.\nIts movement-speed factor is 2 + Swift Spirit rank / 2 = "+n(2+swift/2f)+". The first "+(2*swift)+" ordinary attacks which check its evasion gain guaranteed-dodge evasion; other damage sources need not check evasion. Inspection does not spend those dodges. It has 100 game-time units of lifetime, spent as it takes actions; this is not exactly 100 movement tiles.\nIts initial sight radius is clamped between six and eight tiles: current "+Math.min(8,6+eagle)+". On its own updates, sight uses 6 + Eagle Eye rank = "+(6+eagle)+" tiles. Terrain and visibility rules determine actual revealed tiles. At Go for the Eyes rank "+eyes+", a qualifying hit offers "+(eyes==0?0:eyes==1?2:5)+" turns of Blindness and "+(eyes<3?0:eyes==3?2:5)+" turns of Cripple; zero means no such offer. Immunity/resistance and other target rules still apply.";
        }
        if(ability instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpectralBlades){
            int fan=hero.pointsInTalent(Talent.FAN_OF_BLADES),projecting=hero.pointsInTalent(Talent.PROJECTING_BLADES);
            return "\n\n_Spectral attack targets and multipliers_\nThe primary target receives an ordinary hero attack at a damage factor of 1. Extra targets receive a factor of 0.5 at the attack multiplier step, with later attack effects, armor and rounding still applied. These are not guaranteed final damage amounts. After the attacks resolve, spend the hero's normal attack delay; viewing this explanation does not query or consume a ready attack benefit.\nFan of Blades can search a cone of 30 × rank = "+(30*fan)+" degrees and retains at most 1 + rank = "+(1+fan)+" targets in total. Each ray selects an eligible non-allied character; additional targets and valid paths are not guaranteed. Projecting Blades allows 2 × rank = "+(2*projecting)+" solid tiles along a ray and multiplies its attack accuracy by 1 + 0.25 × rank = "+n(1+.25f*projecting)+". This is an accuracy-score factor, not added percentage points to hit chance. Learned Spirit Blades supplies its own conditional attack-effect behavior.";
        }
        if(ability instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.SmokeBomb){
            int retreat=hero.pointsInTalent(Talent.HASTY_RETREAT),body=hero.pointsInTalent(Talent.BODY_REPLACEMENT),step=hero.pointsInTalent(Talent.SHADOW_STEP);
            return "\n\n_Smoke Bomb destination and timing_\nChoose a visible destination reachable within six legal steps through passable or avoidable terrain. A different destination must be unoccupied; rooting prevents moving this way. Normal use costs one game-time unit and offers adjacent non-allied monsters "+n(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness.DURATION/2f)+" turns of Blindness; a hunting monster is changed to wandering. Resistance and special enemy behavior still matter.\nHasty Retreat rank "+retreat+" offers Haste and Invisibility for "+n(.67f+retreat)+" scheduled game-time units only when learned; the action itself advances time, so this is not a promise of that many additional full actions. Body Replacement rank "+body+" creates one log with "+(20*body)+" HP and adds a middle-favoring armor roll of "+body+"–"+(3*body)+" points only when learned, replacing a prior log.\nIf already invisible and Shadow Step is learned, use the separate Shadow Step mode: no game-time cost, a charge factor of 0.84 raised to rank "+step+", and no new blindness, log, Haste or Invisibility from this cast. The destination still resolves its normal effects; this is not movement through walls.";
        }
        if(ability instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark){
            int durability=hero.pointsInTalent(Talent.DEATHLY_DURABILITY);
            return "\n\n_Death Mark_\nMark a visible enemy for "+n(com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark.DeathMarkTracker.DURATION)+" scheduled game-time units. Casting costs armor charge but no game time. At the marked, non-allied target's damage step, incoming damage is multiplied by 1.25 before later damage rules; this is not added 25 damage points or a promise of 25% more final HP lost. The mark can keep a zero-HP target alive until it ends.\nDeathly Durability's shield offer is round(recorded initial target HP × 0.125 × talent rank). At current rank "+durability+", that is "+n(12.5f*durability)+"% of the recorded initial HP, not the hero's maximum HP or damage dealt. Refreshing records the greater initial HP; the inspector does not reveal an enemy's stored health. A qualifying death offers a Barrier that keeps a stronger existing value instead of adding the entire offer. Fear the Reaper and target immunity rules remain conditional.";
        }
        if(ability instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.ShadowClone){
            int copy=hero.pointsInTalent(Talent.PERFECT_COPY),blade=hero.pointsInTalent(Talent.SHADOW_BLADE),cloned=hero.pointsInTalent(Talent.CLONED_ARMOR);
            return "\n\n_New clone and copied effects_\nCreating a clone spends one game-time unit and requires an adjacent unoccupied passable tile; directing an existing clone is a separate command. A newly created clone at current hero level "+hero.lvl+" has 80 + round(0.1 × Perfect Copy rank × (15 + 5 × hero level)) = "+(80+Math.round(.1f*copy*(15+5*hero.lvl)))+" maximum HP. Its base evasion score is hero level + 4 = "+(hero.lvl+4)+" and accuracy score is hero level + 9 = "+(hero.lvl+9)+"; these are opposed scores, not percentages. This does not retroactively recalculate an existing clone's creation stats.\nIts ordinary damage starts with a middle-favoring 10–20 roll. Shadow Blade adds round(0.08 × rank × truncate(the hero's rolled damage / hero attack delay)) when positive; current rank "+blade+". This uses an actual future roll and action-delay calculation, so inspection does not simulate or consume them. Weapon-proc borrowing is a separate rank / 4 check: "+(25*blade)+"% at current rank, followed by the borrowed effect's own rules.\nCloned Armor adds a positive round(hero armor-block roll × 0.12 × rank), at current rank "+cloned+". Glyph-level borrowing and armor-proc borrowing each use their own rank / 4 check: "+(25*cloned)+"%. These are separate conditional checks, not a guarantee that every copied effect triggers together. Unknown weapon/armor properties and future random rolls are not disclosed here.";
        }
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

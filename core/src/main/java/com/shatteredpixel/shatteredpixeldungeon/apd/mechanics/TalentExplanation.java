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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;

/** Read-only rank and public maximum-HP calculations; no effect or attack handlers. */
public final class TalentExplanation {
    private TalentExplanation() {}
    public static String details(Talent talent, int rank) { return details(talent, rank, false); }
    public static String details(Talent talent, int rank, boolean alternateDescription) {
        if (rank <= 0) return "";
        int hp = Dungeon.hero == null ? 0 : Dungeon.hero.HT;
        switch (talent) {
            case FOCUSED_MEAL:
                if(alternateDescription || (Dungeon.hero!=null&&Dungeon.hero.heroClass!=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.DUELIST))
                    return "_Alternate next-hit damage_\nEating offers one physical damage-roll benefit equal to round(hero level / (4 − rank)) damage points. " + (Dungeon.hero==null?"":"At your current hero level: +"+Math.round(Dungeon.hero.lvl/(4f-rank))+" points. ")
                        + "This is a flat bonus, not a percentage or a weapon upgrade. A result of zero offers no useful damage benefit. It follows the Physical Empower replacement rule: replace only when offered bonus × offered rolls is greater than the existing bonus × remaining rolls.";
                return "_Weapon ability charge from eating_\nEating offers " + ObfuscationExplanation.number((rank+1)/3f)
                    + " weapon-ability charges. Formula: (rank + 1) / 3. Fractional progress accumulates toward a whole charge; it is not rounded up after each meal. The charger stops at its current capacity and discards excess fractional progress when full. These are ability charges, not wand charges, spell charges or free attacks.";
            case ENLIGHTENING_MEAL:
                if(alternateDescription || (Dungeon.hero!=null&&Dungeon.hero.heroClass!=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.CLERIC))
                    return "_Alternate recharge effects_\nEating offers " + (1+rank)
                        + " game-time units of wand recharging and the same duration of artifact recharging. These are durations of recharge effects, not that many whole charges. Eligible items use their own charging rates and limits. Wand duration is prolonged; the artifact effect keeps a longer existing duration instead of adding another full duration. Meals supplied by the Horn do not recharge the Horn through this effect.";
                return "_Tome charge from eating_\nWith a Holy Tome in your belongings, eating offers " + ObfuscationExplanation.number((rank+1)/3f)
                    + " Tome charges, from (rank + 1) / 3. Fractional charge accumulates until it reaches one whole charge. The Tome stops at its current capacity and discards excess fractional progress when full. No Tome means no charge gained; this is not an immediate free spell or wand recharge.";
            case EMPOWERING_MEAL:
                return "_Added wand damage after eating_\nEating sets the added damage to " + (1+rank)
                    + " points per qualifying direct-wand damage roll and offers three such rolls. This adds damage points, not effective wand levels or a percentage. The direct-damage roll consumes the counter, so a multi-roll action can spend more than one use. Reapplying replaces the bonus amount and keeps the larger remaining-roll count; it does not add three more to an existing count. Target defenses and later damage rules still apply.";
            case STRENGTHENING_MEAL:
                return "_Added physical damage after eating_\nEating offers +3 damage points on each of the next " + (1+rank)
                    + " qualifying hero physical damage rolls. These are added points at the hero's damage-roll stage, not permanent weapon upgrades or a percentage of final HP lost. The counter is spent by damage rolls rather than merely by pressing Attack. Reapplication replaces the current benefit only if offered bonus × offered rolls is greater than the existing bonus × remaining rolls; it does not simply add the two pools. Later attack modifiers and defenses can change the final result.";
            case VETERANS_INTUITION:
            case ADVENTURERS_INTUITION:
            case SCHOLARS_INTUITION:
            case SURVIVALISTS_INTUITION:
            case THIEFS_INTUITION:
                String effect;
                switch(talent){
                    case VETERANS_INTUITION:
                        effect = "Melee-weapon use-identification progress is multiplied by " + ObfuscationExplanation.number(1f+.75f*rank) + "; armor progress by " + (1+rank) + ". At rank two, equipping armor also completes identification, subject to passive-identification restrictions."; break;
                    case ADVENTURERS_INTUITION:
                        effect = "Melee-weapon use-identification progress is multiplied by " + ObfuscationExplanation.number(1f+1.5f*rank) + "; armor progress by " + ObfuscationExplanation.number(1f+.75f*rank) + ". At rank two, equipping a weapon also completes identification, subject to passive-identification restrictions."; break;
                    case SCHOLARS_INTUITION:
                        effect = "Wand use-identification progress is multiplied by " + (1+2*rank) + ". At rank two, a qualifying wand use completes identification, subject to passive-identification restrictions."; break;
                    case SURVIVALISTS_INTUITION:
                        effect = "Thrown-weapon use-identification progress is multiplied by " + (1+2*rank) + ". At rank two, a qualifying thrown-weapon hit readies identification through the normal weapon-use handler."; break;
                    default:
                        effect = "Ring use-identification progress is multiplied by " + (1+rank) + ". Equipping reveals its ring type; at rank two, collecting also reveals the type and equipping completes identification, subject to passive-identification restrictions. Revealing a type alone does not reveal hidden upgrade or curse properties.";
                }
                return "_Identification progress_\n" + effect + " These factors multiply progress earned by eligible uses, not damage, drop chances, or a fixed number of turns saved. Other applicable intuition factors multiply together. Available identification progress and the item's own usage rules can limit progress from an individual action. Shard of Oblivion can mark identification ready instead of revealing the item. No hidden item values are used to produce this explanation.";
            case HOLD_FAST:
                return "_Protection while holding position_\nWaiting readies an added armor-block roll of " + rank + "–" + (2*rank)
                    + " damage points, with rolls biased toward the middle. The Warrior needs the seal's shielding effect equipped to ready it; other classes use their own talent rules. Moving ends the benefit. The Warrior's added roll is capped at six points. For another class, the cap is worn armor tier plus its effective level, or zero without armor; an unidentified armor's hidden level is not disclosed here. "
                    + "Supported combo and shield decay steps multiply their normal decay by " + ObfuscationExplanation.number(rank==1?.5f:rank==2?.25f:0f)
                    + ". This slows decay by " + (rank==1?50:rank==2?75:100) + "%, rather than adding duration to every buff or reducing damage by that percentage. It applies to the supported Combo, Barrier, Blocking shield, seal shield-expiry delay and Berserk shielding decay paths; it does not freeze all status effects.";
            case HEARTY_MEAL:
                return "_Healing from eating_\nEating while current HP divided by maximum HP is strictly below 0.334 offers " + (2+2*rank)
                    + " HP of healing before any custom healing modifier. This is a flat amount, not a percentage of maximum HP. At or above that threshold, this talent offers no healing. Actual health restored is limited by missing HP and the current run's healing rules; excess is not converted to shielding. Eating still spends the food and its normal action time.";
            case IRON_STOMACH:
                return "_Protection during the eating action_\nEating grants protection only while the hero has positive remaining action time from that meal. At this rank, incoming damage at the food-protection step is "
                    + (rank == 2 ? "set to zero" : "divided by four, leaving 25% before rounding")
                    + ". This is conditional protection during that action, not permanent armor or a shield-point reserve. Damage is rounded to whole points afterward, and later damage rules still apply. The effect does not make eating free or stop hunger from advancing.";
            case LIQUID_WILLPOWER:
                float fraction = .030f + .035f * rank;
                return "_Potion shielding_\nAn ordinary potion offers a Barrier equal to "
                    + ObfuscationExplanation.number(100 * fraction) + "% of maximum HP, rounded to the nearest shield point. "
                    + (hp > 0 ? "At your current " + hp + " maximum HP: " + Math.round(hp * fraction) + " shield points. " : "")
                    + "Formula: round(maximum HP × (0.030 + 0.035 × rank) × the consumed item's talent-effect factor). Some potion-derived items have a different factor. This replaces a weaker Barrier; it does not add the whole amount to an existing stronger Barrier. It protects health rather than healing it.";
            case SHIELD_BATTERY:
                return "_Converting wand charges_\nTarget yourself with a charged wand to consume all its charges and offer a Barrier. Each charge contributes "
                    + (rank == 2 ? "6" : "4") + "% of maximum HP; round only the final total to the nearest shield point. "
                    + "Formula: round(maximum HP × 0.04 × charges consumed" + (rank == 2 ? " × 1.5" : "") + "). "
                    + "The second rank multiplies the shield amount by 1.5, rather than adding 1.5 shield points. A stronger existing Barrier remains stronger; the amounts are not added. This action costs one turn and cannot be used while magic is disabled or the wand has no charges.";
            case INSCRIBED_POWER:
                return "_Empowered wand uses_\nAn ordinary scroll offers " + (1 + rank)
                    + " empowered wand uses. Each qualifying use gains +2 effective wand levels, rather than +2 damage points. Damage and other level-dependent effects follow that wand's own rules. "
                    + "Uses offered = truncate((1 + rank) × the consumed item's talent-effect factor). A new application keeps the larger remaining use count instead of adding the counts. It does not permanently upgrade the wand.";
            case SHIELD_OF_LIGHT:
                if(alternateDescription || (Dungeon.hero!=null&&Dungeon.hero.heroClass!=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.CLERIC))
                    return "_Alternate targeted protection_\nWhen a qualifying incoming attack comes from the currently selected enemy, the six-outcome check succeeds on 1 + rank outcomes: current chance " + ObfuscationExplanation.number(100f*(1+rank)/6f)
                        + "% for that matching attack. A success subtracts one damage point at this defense step. This is not a percentage reduction in the whole hit, and attacks from other enemies do not gain this passive check. It is the non-Cleric benefit rather than granting the Cleric's targeted spell.";
                return "_Targeted spell protection_\nThe spell offers a middle-favoring block roll from " + (1+rank) + " to " + (2*(1+rank))
                    + " damage points against its selected enemy. This is armor blocking at that step rather than Barrier points or a reduction against every attacker. The spell costs one Tome charge and takes no game time to cast; its four scheduled units normally appear as five on the counter because of the following status update.";
            case BLESS:
                if(alternateDescription || (Dungeon.hero!=null&&Dungeon.hero.heroClass!=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.CLERIC))
                    return "_Alternate allied accuracy and evasion_\nAllied characters, including the hero, multiply the normal accuracy and evasion rolls by 1.01 + 0.02 × rank = " + ObfuscationExplanation.number(1.01f+.02f*rank)
                        + ". These are factors in the opposed hit contest, not added percentage points to hit or dodge chance. Each side's bonuses and special rules still matter. This passive does not require or grant the Cleric's Bless spell, and it is distinct from the temporary Bless status's 1.25 factor.";
                return "_Bless spell amounts_\nOn the hero, offer " + (2+4*rank) + " scheduled turns of Bless and " + (5+5*rank)
                    + " Barrier points. On another eligible creature, offer " + (5+5*rank) + " turns of Bless and up to " + (5+5*rank)
                    + " HP healing, with unused healing offered as Barrier points. A stronger Barrier is kept instead of simply adding the full offer. The temporary Bless status multiplies accuracy and evasion contest scores by 1.25; it does not add 25 percentage points to either chance.";
            case STRONGMAN:
                float strengthFraction = .03f + .05f * rank;
                return "_Added strength points_\nAdds floor(base strength × " + ObfuscationExplanation.number(strengthFraction)
                    + ") strength points. The percentage uses base strength, excluding Ring of Might, Adrenaline Surge and this talent's own bonus. Those separate added bonuses are not multiplied again. "
                    + (Dungeon.hero == null ? "" : "Current base strength: " + Dungeon.hero.STR + " points; this rank adds " + (int)Math.floor(Dungeon.hero.STR * strengthFraction) + " strength points. ")
                    + "The result is rounded down to whole strength points and can stay unchanged until enough base strength is gained. Strength requirements and strength-based damage effects use their own rules; this is not a direct damage percentage.";
            case DESPERATE_POWER:
                return "_Last-charge wand level_\nWhen a charged wand is down to exactly one charge, this talent adds " + rank
                    + " effective wand levels to its cast. It does not add that many damage points or permanent upgrades. The wand's level-dependent effects determine the result. A stronger Magic Charge and Wild Magic's own cap/order can affect the resulting effective level; wand details show the current result without spending the charge.";
            case WILD_POWER:
                return "_Wild Magic's temporary level bonus_\nBefore the cap, the two equally likely bonus branches add " + ((4+rank)/2)
                    + " or " + ((5+rank)/2) + " effective wand levels. When both amounts are equal, there is no difference between the branches. "
                    + "If the incoming effective level is below " + (3+rank) + ", add the selected bonus but stop at " + (3+rank)
                    + ". A wand already at or above that level is not lowered by this cap. A stronger Magic Charge is considered afterward. These are temporary casting levels, not permanent upgrades or added damage points.";
            case POINT_BLANK:
                return "_Thrown-attack accuracy by range_\nAt adjacent range, multiply the bow or thrown weapon's accuracy contribution by "
                    + ObfuscationExplanation.number(.5f+.25f*rank) + "; without the talent it is 0.5. At non-adjacent range, the factor remains 1.5. "
                    + "Formula at adjacent range: 0.5 + 0.25 × rank. These are accuracy-score factors, not fixed hit percentages. Target evasion, other accuracy effects and special hit rules still matter.";
            case PROJECTILE_MOMENTUM:
                return "_Thrown attacks while freerunning_\nMultiply the qualifying thrown attack's accuracy score by "
                    + ObfuscationExplanation.number(1f+rank/2f) + " and its weapon damage roll by "
                    + ObfuscationExplanation.number(1f+.15f*rank) + ", rounding the damage to the nearest point. "
                    + "The damage step follows applicable excess-strength damage. These bonuses require active freerunning, not merely stored momentum. Accuracy is an opposed score, not a hit percentage, and later defenses can change final HP loss.";
            case SPEEDY_STEALTH:
                return "_Invisible momentum and movement_\nWhile invisible, with freerunning ready and not already active, each normal one-turn momentum update adds two stacks, capped at ten. Cooldown blocks this stack gain. "
                    + (rank >= 2 ? "At this rank, invisibility also pauses the active freerunning countdown. " : "This rank does not pause the freerunning countdown. ")
                    + (rank == 3 ? "Invisible movement uses a speed factor of 2 even outside freerunning, so otherwise unchanged steps take half their usual game time. It is the same factor while freerunning, not 2 × 2. " : "This rank does not separately double movement speed outside freerunning. ")
                    + "This movement benefit does not multiply attack speed or damage.";
            case EMPOWERED_STRIKE:
                return "_Ready staff strike_\nThe ready staff strike multiplies damage at its staff-effect step by "
                    + ObfuscationExplanation.number(1f+rank/6f) + " and rounds to a whole damage point. Formula: round(damage entering this step × (1 + rank / 6)). "
                    + "Its supported staff on-hit effect uses a separate power factor of " + ObfuscationExplanation.number(1f+rank/2f)
                    + ". That second factor strengthens the particular wand's staff effect; it does not multiply all weapon damage again. Chance, duration or amount can use it differently for different wands. The ready effect is consumed by the qualifying staff strike, with delayed resolution where required.";
            case SHARED_ENCHANTMENT:
                return "_Borrowing the bow's enchantment_\nOn each eligible borrowing check, chance = talent rank / 3. Current chance "
                    + ObfuscationExplanation.number(100f*rank/3f) + "%. This offers the bow's enchantment an opportunity to run; the enchantment's own activation roll and target restrictions still apply. "
                    + "At rank three, borrowing is guaranteed, but the enchantment's effect is not necessarily guaranteed. Magic immunity prevents the borrowed on-hit effect. The thrown weapon's own enchantment can also apply, and Projecting has a separate range check.";
            case PROVOKED_ANGER:
                return "_Added attack damage_\nA ready benefit adds " + (1 + 2 * rank)
                    + " damage points to the next qualifying attack at the talent damage stage, then is consumed. Formula: 1 + 2 × rank. It is an added amount, not a percentage. The ready benefit lasts five turns after damage breaks a shielding buff; later defenses, shielding and attack effects can still change final HP loss.";
            case LINGERING_MAGIC:
            case PATIENT_STRIKE:
            case SUCKER_PUNCH:
                return "_Added attack damage_\nA qualifying attack adds a uniformly rolled " + rank
                    + "–2 damage points at the talent damage stage. At rank two this is always two points, rather than another roll on top of rank one's benefit. "
                    + (talent == Talent.LINGERING_MAGIC ? "A wand or staff zap readies this benefit for five turns; the next qualifying physical attack consumes it. "
                        : talent == Talent.PATIENT_STRIKE ? "Waiting readies this benefit while you hold that position; moving ends it, and the next qualifying non-thrown attack consumes it. "
                        : "This requires a surprise attack against a monster which has not already received this talent's bonus. The bonus is recorded per monster, rather than repeating on every surprise hit. ")
                    + "These are added damage points, not a hit chance or permanent weapon upgrade. Later attack effects, shielding and defenses can change the final HP loss.";
            case FOLLOWUP_STRIKE:
                return "_Marked-enemy follow-up_\nA bow or thrown-weapon hit marks that living enemy for five turns. A qualifying non-thrown attack against the same marked enemy adds "
                    + (1 + rank) + " damage points at the talent damage stage and consumes the mark. Formula: 1 + rank. It is an added amount, not a percentage. Hitting a different enemy with a thrown weapon replaces the target.";
            case DEADLY_FOLLOWUP:
                return "_Marked-enemy damage multiplier_\nA thrown-weapon hit, excluding the Spirit Bow's arrow, marks that enemy for five turns. Qualifying non-thrown attacks against the same marked enemy multiply damage at this talent's stage by "
                    + ObfuscationExplanation.number(1f + .1f * rank)
                    + " and round to the nearest damage point. Formula: round(damage entering this step × (1 + 0.1 × rank)). "
                    + "This step follows the flat attack-talent bonuses, so it also scales those applicable bonuses. It is not added damage points or a guaranteed percentage increase in final HP loss. The mark lasts until its timer expires or a new thrown target replaces it; one melee hit does not consume it.";
            case LIQUID_AGILITY:
                return "_Potion accuracy and evasion_\nAt this rank, the temporary evasion benefit "
                    + (rank == 2 ? "guarantees a dodge in the normal accuracy-versus-evasion check" : "multiplies the evasion score by 3; it does not add a flat dodge percentage")
                    + ". It lasts through the current action's remaining time, with extra time if the consumed item's talent-effect factor exceeds one. "
                    + "The next qualifying ordinary non-thrown attack "
                    + (rank == 2 ? "uses guaranteed-hit accuracy" : "multiplies its accuracy score by 3")
                    + ", available for five turns after an ordinary potion. Ordinary potions offer one such use; other factors offer round(factor) uses only when the factor is at least 0.5. "
                    + "Weapon abilities, thrown attacks, a spinning Flail and a charged Crossbow shot do not consume this accuracy benefit. A ready Precise Assault benefit takes priority instead of multiplying these two temporary bonuses. Guaranteed evasion wins over guaranteed-hit accuracy in the normal contest; other special attack rules still apply.";
            case PRECISE_ASSAULT:
                return "_Regular-attack accuracy_\nAfter a qualifying weapon ability, the ready benefit changes the next eligible ordinary non-thrown attack's accuracy to "
                    + (rank == 3 ? "guaranteed-hit accuracy" : (rank == 2 ? "5" : "2") + " times its otherwise calculated score")
                    + ". It is not a flat hit percentage. This takes priority over a ready Liquid Agility accuracy benefit; the two temporary bonuses do not multiply together. "
                    + "For a hero other than the Duelist, the separate persistent bonus multiplies qualifying ordinary non-thrown accuracy by "
                    + ObfuscationExplanation.number(1f + .1f * rank)
                    + ". Weapon abilities, a spinning Flail and a charged Crossbow shot have separate rules. Guaranteed evasion wins over guaranteed-hit accuracy in the normal contest.";
            case LIQUID_NATURE:
                return "_Nearby roots and grass_\nFor an ordinary potion, nearby enemies are offered " + rank
                    + " turns of Roots and up to " + (2 + 2 * rank) + " nearby tiles are selected for tall grass. "
                    + "The affected area is the potion's tile and its eight neighbors. Only suitable terrain without a plant can become tall grass, so the selected count is not guaranteed grass. "
                    + "Roots duration = rank × the consumed item's talent-effect factor; selected grass tiles = truncate((2 + 2 × rank) × that factor), limited by the nine-tile area. Other eligible empty tiles in the area become short grass. No extra movement or free searching is granted.";
            default: return "";
        }
    }
}

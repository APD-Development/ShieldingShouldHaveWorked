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
    public static String details(Talent talent, int rank) {
        if (rank <= 0) return "";
        int hp = Dungeon.hero == null ? 0 : Dungeon.hero.HT;
        switch (talent) {
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

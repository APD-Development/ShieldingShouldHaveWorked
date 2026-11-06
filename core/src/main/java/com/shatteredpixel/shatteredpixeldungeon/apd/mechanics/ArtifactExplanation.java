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

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.apd.ChaliceDamagePreview;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.*;

/** Reviewed artifact explanations, never an artifact use/recharge/proc. */
public final class ArtifactExplanation {
    private ArtifactExplanation(){}
    private static String n(float value){return ObfuscationExplanation.number(value);}
    public static String summary(Artifact artifact){
        if(!artifact.isIdentified())return "";
        if(artifact.cursed)return "\n\n"+ArtifactEconomyExplanation.summary(artifact,Dungeon.hero);
        String effect="";
        if(artifact instanceof CapeOfThorns)effect="While its effect is active, each applicable incoming hit can lose anywhere from none to all of its damage, with results biased toward the middle. Only an adjacent attacker receives the amount removed. This is not a fixed damage-reduction percentage.";
        else if(artifact instanceof ChaliceOfBlood)effect="When worn and active, this Chalice shortens natural healing's delay. Its current base delay is "+n(10f-(1.33f+artifact.level()*.667f))+" turns per HP before recharge, regeneration and healing modifiers. It does not heal through starvation or bypass rules that disable regeneration. Pricking upgrades it at the cost of potentially lethal damage.";
        else if(artifact instanceof HornOfPlenty){int nourishment=(int)(Hunger.STARVING/5f);if(Dungeon.isChallenged(Challenges.NO_FOOD))nourishment/=3;effect="Each charge restores "+nourishment+" hunger points in the current challenge configuration, limited by how hungry you are. Snack spends one charge; Eat can spend several. Hunger points are nourishment, not real-world seconds. Eating still costs normal eating time.";}
        if(effect.isEmpty())effect=ArtifactEconomyExplanation.summary(artifact,Dungeon.hero);
        if(effect.isEmpty())return "";
        if(Dungeon.hero!=null&&Dungeon.hero.buff(MagicImmune.class)!=null&&(artifact instanceof ChaliceOfBlood||artifact instanceof HornOfPlenty))effect="Magic immunity currently suppresses this artifact's normal activation/recharging benefit. "+effect;
        return "\n\n"+effect;
    }
    public static String details(Artifact artifact){
        if(!artifact.isIdentified())return "Identify this artifact before viewing its exact stored amounts and upgrade calculations.";
        if(artifact.cursed)return ArtifactEconomyExplanation.details(artifact,Dungeon.hero);
        String state="";
        if(artifact.maximumCharges()>0)state=artifact.maximumCharges()==100?"Stored resource meter: "+artifact.currentCharges()+" / 100 points. A percent on the icon is this meter's fullness, not an activation chance.\n\n":"Stored charges: "+artifact.currentCharges()+" / "+artifact.maximumCharges()+". These are resource units available to the artifact, not turns of free actions.\n\n";
        int level=artifact.level();
        if(artifact instanceof CapeOfThorns)state+="_Cape activation and deflection_\nWhile inactive, an applicable damage input adds damage × (0.5 + 0.05 × artifact level) to its meter, truncating the stored integer. Current coefficient: "+n(.5f+.05f*level)+" meter points per damage point. Reaching the meter cap empties it and starts "+(10+level)+" turns of activity, including protection against that triggering hit. Remaining active countdown: "+artifact.effectCountdown()+" turns. Each active hit rolls the removed amount from 0 through its incoming damage, biased toward the middle; only adjacent attackers receive retaliation. Upgrading adds 0.05 to the charging coefficient and one turn to new activations. Other defenses and target effects still apply.";
        else if(artifact instanceof ChaliceOfBlood)state+="_Chalice healing and prick_\nUncursed base natural-healing delay = 10 − (1.33 + 0.667 × level) = "+n(10f-(1.33f+level*.667f))+" turns per HP before other modifiers. Each level reduces this base delay by 0.667 turn. HP gains remain conditional on hunger, regeneration eligibility, missing HP and healing rules. The next prick's raw range is "+ChaliceDamagePreview.baseMin(level)+"–"+ChaliceDamagePreview.baseMax(level)+" damage points: lower bound ceil(3 + 2.5 × level²), upper bound floor(7 + 3.5 × level²). Current defenses can alter final HP loss; use the existing confirmation preview before pricking. This calculation is not a survival guarantee.";
        else if(artifact instanceof HornOfPlenty)state+="_Horn food and recharge_\nA normal charge restores floor(starvation threshold / 5) hunger points; On Diet divides that amount by three. Experience-driven charge gain before charge rounding and capacity is level-progress fraction × (0.25 + 0.125 × artifact level) × 5, then multiplied by artifact recharge bonuses. Current coefficient before those bonuses: "+n((.25f+.125f*level)*5)+" charges per whole hero-level worth of qualifying progress. This is not passive charges per turn. Upgrades also affect charge capacity; the actual current capacity is shown above. Snack always uses one available charge; Eat chooses an integer number based on hunger and available charges. Hunger cannot be restored below its normal lower limit.";
        return (state+"\n\n"+ArtifactEconomyExplanation.details(artifact,Dungeon.hero)).trim();
    }
}

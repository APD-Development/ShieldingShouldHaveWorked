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

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

/** Primary ability rules only; does not select targets, roll attacks or spend energy. */
public final class MonkAbilityExplanation {
    private MonkAbilityExplanation() {}
    public static String details(MonkEnergy energy,Hero hero){
        if(hero==null)return "";
        boolean empowered=energy!=null&&energy.abilitiesEmpowered(hero);
        Integer strength=ArmorExplanation.knownStrength(hero);
        String range=strength==null?"Basic damage endpoints are withheld while contributing strength bonuses are unknown or unavailable."
                :"Known strength: "+strength+" points. Basic unarmed roll: 1–"+Math.max(1,strength-8)+" damage points. Flurry's basic per-strike range: 2–"+Math.round(1.5f*Math.max(1,strength-8))+"; Dragon Kick's current basic range: "+(empowered?9:6)+"–"+((empowered?9:6)*Math.max(1,strength-8))+" damage points.";
        return "_Flurry: separate instant strikes_\nTwo ordinary attack resolutions with a 1.5 damage-input factor each, stopping after the first if the target dies. The basic unarmed roll is 1 to max(strength−8,1); the normal Force-ring damage curve is excluded by the unarmed-ability rule. Attack damage rounds later, and other offensive effects or protections can change final HP loss. "+range+" The unarmed ability ignores ordinary armor protection, not every resistance or shield. Infinite evasion can still beat its guaranteed-accuracy offer. It spends energy but no normal attack-time step; the ready cooldown marker prevents immediate repeated use. Empowerment enables weapon enchantment use for these strikes.\n\n"
            +"_Focus: one ready defense_\nOffers a Focus buff which makes ordinary hit contests use guaranteed evasion. This can defeat guaranteed accuracy; direct damage and status applications outside that contest retain their own rules. Normal parry reporting removes the Focus buff. Setup costs 1 turn normally, or no action-time step when empowered; it still spends the ability's energy cost. It cannot normally be readied again while a Focus buff is present.\n\n"
            +"_Dash: range and a clear route_\nMaximum grid distance is 4 tiles normally or 8 when empowered; current range: "+(empowered?8:4)+" tiles. Rooting prevents it. The destination must be unoccupied and reachable by the ability's clear projectile path and terrain checks; range alone does not allow going through a wall. The move still occupies the destination under normal terrain rules. It spends energy without a normal movement-time step. Inspection does not test an unseen route or reveal hidden occupants.\n\n"
            +"_Dragon Kick: main hit and displacement_\nThe main attack uses a 6 damage-input factor normally or 9 when empowered, from the same basic unarmed roll. It skips ordinary armor under the ability's rule. Knockback attempts up to 6 tiles along the push path. The offered paralysis is min(6,push-path distance) game-time units when its active-target check permits; it is based on that path, not guaranteed actual movement. Obstacles and displacement immunity still matter. Empowerment also attempts to push adjacent enemy-aligned creatures; that extra push is not a second area-damage roll. Kick action time uses the normal unarmed attack-delay path while the ability tracker is active.\n\n"
            +"_Meditate: time, cleansing and delayed recharging_\nClears eligible negative effects, preserving allegiance-changing buffs and Lost Inventory. Spends five separate constant one-unit time steps, with normal scheduling consequences; it does not merely change a displayed counter. A delayed effect offers 8 game-time units of wand recharging and extends artifact recharging by 8, with the Horn allowed. Those durations do not equal eight whole item charges. When empowered, it also offers a healing pool of round(missing HP/5), currently "+Math.round((hero.HT-hero.HP)/5f)+" HP before current healing rules, restored over later updates. During meditation its resistance effect multiplies qualifying incoming attack damage by 0.2 at that step; this is not universal invulnerability or shielding. Inspection does not clear statuses, spend time, grant charges or advance healing.";
    }
}

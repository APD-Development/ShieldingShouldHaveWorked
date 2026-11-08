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

import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;

/** Public artifact state only: never constructs a cone or inspects any floor cell. */
public final class TalismanExplanation {
    private TalismanExplanation() {}
    public static float range(TalismanOfForesight item) {
        return item.isIdentified() ? Math.min(5+2*item.level(),(item.currentCharges()-3)/1.08f) : Float.NaN;
    }
    public static String summary(TalismanOfForesight item) {
        if(!item.isIdentified())return "";
        return "Scrying searches a cone for secrets and maps it, costing _1 turn_ and charge. It reveals creatures and item piles for _"
            +(5+2*item.level())+" turns_. The talisman's warning about nearby hidden traps is passive.";
    }
    public static String details(TalismanOfForesight item) {
        if(!item.isIdentified())return "";
        int level=item.level();
        return "_Active Scrying_\n"+summary(item)
            +" Scrying requires the equipped Talisman, at least 5 stored meter points and no Magic Immunity. Selecting your own tile does not cast."
            +"\nRange-limit formula: min(5 + 2 × upgrade steps, (stored whole meter points − 3) / 1.08) = "
            +(item.currentCharges()<5 ? "currently unavailable because the meter is below the 5-point activation requirement." : ObfuscationExplanation.number(range(item))+" tile-distance units at the current meter.")+" This is the long-range cutoff, not a guarantee that every tile within that radius is searched. Adjacent targets are extended outward; the existing short-distance exception applies before long trajectories are clipped."
            +"\nAt actual target distance d, cone angle = round(200 × 0.92^d) degrees. Farther casts reach farther but cover a narrower angle. Nominal resource cost is 3 + 1.08 × d meter points; existing whole-meter subtraction and fractional carry govern the actual stored debit. It is not a chance of finding a secret. Casting also ends invisibility."
            +"\nThe cone maps previously unmapped, discoverable tiles and reveals supported secrets inside it. Creature awareness excludes ordinary neutral creatures and your own alignment; Mimics follow their special eligibility. Creature and pile awareness duration = 5 + 2 × upgrade steps = "+(5+2*level)+" scheduled units before applicable status rules. It does not grant permanent sight through the entire floor."
            +"\nUpgrade progress: 1 point per newly mapped discoverable tile; 10 per revealed hidden trap; 100 per revealed secret door; 10 per eligible creature outside current sight; 10 per unseen item pile. Next upgrade threshold = 100 + 50 × upgrade steps = "+(100+50*level)+" progress points, subject to the maximum upgrade. A cast can perform one upgrade check; these progress points are separate from the charge meter."
            +"\nThe passive warning only checks search-eligible hidden traps in the current field of view within three tiles along each map axis. It does not certify a wall has no door. Inspection does not check any hidden tile, select targets, roll results or spend time or charges.";
    }
}

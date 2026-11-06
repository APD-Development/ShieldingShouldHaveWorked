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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WandEmpower;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.*;

/** Known wand contributions, without casting, selecting a random level or consuming a use. */
public final class WandExplanation {
    private WandExplanation() {}
    public static String details(Wand wand) {
        if (!wand.isIdentified()) return "Identify the wand before its upgrade-dependent calculations are shown.";
        int[] levels = wand.previewEffectiveLevels();
        String result = "_Current wand levels and charges_\nEffective level for the next cast: "
            + levels[0] + (levels[0] == levels[1] ? "" : " to " + levels[1])
            + ". This is a level used by the wand's own formulas, not that many damage points. "
            + "Current charges: " + wand.curCharges + " of " + wand.maxCharges + ". A charge is a casting resource, not damage. "
            + "Temporary +2 levels from Inscribed Power, one-charge Desperate Power, Wild Magic's capped random bonus and a stronger Magic Charge can change effective levels; they do not permanently upgrade the wand. "
            + "An ordinary upgrade raises the underlying level by one, consumes one existing resin bonus when present, and can remove a curse. Charge limits and each wand's effects follow their separate formulas.";
        if (levels[0] != levels[1]) result += "\nWild Magic has two equally likely bonus branches before its level cap and stronger Magic Charge apply. The range above includes both outcomes; the cast selects its outcome normally.";
        if (wand instanceof DamageWand) {
            DamageWand direct = (DamageWand)wand;
            int min = direct.min(levels[0]), max = direct.max(levels[1]);
            result += "\n\n_Base direct-damage roll_\nCurrent roll bounds: " + min + "–" + max
                + " damage points before direct-roll bonuses, the wand's situational effects and the target's defenses. "
                + "This is not a guaranteed amount of health lost. The game's damage-roll rules determine which result is selected.";
            // These built-in wands have affine level bounds; do not infer formulas for custom subclasses.
            if (wand.getClass().getPackage().equals(DamageWand.class.getPackage())
                    && (wand instanceof WandOfBlastWave || wand instanceof WandOfFireblast
                    || wand instanceof WandOfLightning || wand instanceof WandOfMagicMissile
                    || wand instanceof WandOfPrismaticLight || wand instanceof WandOfDisintegration
                    || wand instanceof WandOfTransfusion || wand instanceof WandOfLivingEarth
                    || wand instanceof WandOfFrost)) {
                result += "\nFor the current charge configuration, minimum = " + direct.min(0) + " + "
                    + (direct.min(1)-direct.min(0)) + " × effective level; maximum = " + direct.max(0)
                    + " + " + (direct.max(1)-direct.max(0)) + " × effective level. "
                    + "Multi-charge Fireblast, penetrating beams, water, target type and other situational rules can change the actual effect; those are not a universal damage multiplier.";
            }
            WandEmpower empower = Dungeon.hero == null ? null : Dungeon.hero.buff(WandEmpower.class);
            if (empower != null) result += "\nCurrent Wand Empower adds " + empower.dmgBoost
                + " damage points to a qualifying direct-damage roll, with " + empower.left
                + " such rolls remaining. With that bonus, these roll bounds are " + (min+empower.dmgBoost)
                + "–" + (max+empower.dmgBoost) + " damage points before later adjustments. Multiple rolls or targets can consume more than one use.";
        } else if (wand instanceof WandOfCorrosion) {
            result += "\n\n_Corrosive gas_\nStarting unmodified gas damage uses 2 + effective wand level: "
                + (2+levels[0]) + (levels[0]==levels[1]?"":" to "+(2+levels[1]))
                + " damage points. This is the starting tick strength, not total damage from the entire cloud. Remaining in the gas and later Corrosion ticks have their own timing and increasing-strength rules; immunity and protection can change actual HP loss.";
        }
        return result;
    }
}

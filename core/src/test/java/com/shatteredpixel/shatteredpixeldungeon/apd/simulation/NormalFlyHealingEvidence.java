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

package com.shatteredpixel.shatteredpixeldungeon.apd.simulation;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.GenerationEvents;

public final class NormalFlyHealingEvidence {
    private NormalFlyHealingEvidence() {}
    public static boolean matches(GenerationEvents.ItemGenerated drop) {
        // Recorder names retain their package below the game package; simple names are not its format.
        return drop != null && drop.container == GenerationEvents.C_MOB_LOOT
                && "actors.mobs.Swarm".equals(drop.owner)
                && "items.potions.PotionOfHealing".equals(drop.trueClass);
    }
}

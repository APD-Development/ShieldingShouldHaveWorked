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

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

// Test-only movement policy. Uses observed terrain and occupants, never hidden mob positions.
public final class SimulationNavigation {
    private SimulationNavigation() {}
    public static boolean[] walkable(Level level, Hero hero) {
        boolean[] result = new boolean[level.length()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (level.visited[i] || level.mapped[i]) && level.passable[i] && !level.pit[i];
            if (level.traps.get(i) != null && level.traps.get(i).visible && level.traps.get(i).active)
                result[i] = false;
        }
        for (Mob mob : level.mobs) if (mob.isAlive() && hero.fieldOfView[mob.pos]) result[mob.pos] = false;
        return result;
    }
}

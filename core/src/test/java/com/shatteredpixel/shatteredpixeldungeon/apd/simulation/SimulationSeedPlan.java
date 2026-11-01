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

import java.util.HashSet;
import java.util.Set;

// Explicit seed lists let recovery preserve completed episodes instead of rerunning them.
public final class SimulationSeedPlan {
    private SimulationSeedPlan() {}
    public static long[] parse(String explicit, long first, int count) {
        if (count < 1 || count > 100) throw new IllegalArgumentException("episode count must be 1–100");
        long[] seeds = new long[count];
        if (explicit == null || explicit.trim().isEmpty()) {
            for (int i = 0; i < count; i++) seeds[i] = Math.addExact(first, i);
            return seeds;
        }
        String[] values = explicit.split(",", -1);
        if (values.length != count) throw new IllegalArgumentException("explicit seed count mismatch");
        Set<Long> unique = new HashSet<>();
        for (int i = 0; i < count; i++) {
            seeds[i] = Long.parseLong(values[i].trim());
            if (!unique.add(seeds[i])) throw new IllegalArgumentException("duplicate explicit seed");
        }
        return seeds;
    }
}

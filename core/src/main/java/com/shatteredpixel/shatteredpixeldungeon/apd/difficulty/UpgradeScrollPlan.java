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

package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;

/** Immutable allocation for non-neutral rules. Neutral generation must use Dungeon.souNeeded unchanged. */
public final class UpgradeScrollPlan {
    public static final int MIN = 1, NEUTRAL = 15, MAX = 60;
    private final int count;
    public UpgradeScrollPlan(int count) {
        if (count < MIN || count > MAX) throw new IllegalArgumentException("Upgrade scroll count outside 1..60");
        this.count = count;
    }
    public int count() { return count; }
    public double percentage() { return count * 100.0 / NEUTRAL; }
    public boolean usesVanillaGeneration() { return count == NEUTRAL; }
    /** Balanced allocation: regional counts differ by at most one. No RNG draws. */
    public int regionCount(int region) {
        if (region < 0 || region >= 5) throw new IllegalArgumentException("Region outside 0..4");
        return count * (region + 1) / 5 - count * region / 5;
    }
    /** Non-boss floors in the five main regions, with no new RNG or hidden placements. */
    public int floorCount(int depth) {
        if (depth<1 || depth>24 || depth%5==0) return 0;
        int region=(depth-1)/5, index=(depth-1)%5;
        int regional=regionCount(region);
        return regional*(index+1)/4-regional*index/4;
    }
    /** Count after existing Forbidden Runes removal, whose counter spans regions. */
    public int countAfterForbiddenRunes() { return (count + 1) / 2; }
}

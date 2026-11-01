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

package com.shatteredpixel.shatteredpixeldungeon.apd;

/** Limits repeated ambient SFX to real time, never modifies game or animation speed. */
public final class RealTimeSoundCadence {
    private final float minimumSeconds;
    private float remaining;
    public RealTimeSoundCadence(float minimumSeconds) {
        if (!(minimumSeconds > 0) || !Float.isFinite(minimumSeconds)) throw new IllegalArgumentException("cadence");
        this.minimumSeconds=minimumSeconds;
    }
    public void advance(float unscaledSeconds) {
        if (!Float.isFinite(unscaledSeconds) || unscaledSeconds < 0) return;
        remaining = Math.max(0,remaining-unscaledSeconds);
    }
    public boolean accept() {
        // A fractional frame of floating-point drift must not postpone a strike.
        if (remaining > 0.001f) return false;
        remaining = minimumSeconds;
        return true;
    }
}

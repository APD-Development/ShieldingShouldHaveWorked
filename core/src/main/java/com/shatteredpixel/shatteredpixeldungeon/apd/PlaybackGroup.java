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

import com.watabou.noosa.Game;
import com.watabou.noosa.Group;

/**
 * Visual-only playback control. Private test builds can intentionally exaggerate
 * animations; actor turns, UI input timing, and the global game clock are untouched.
 */
public final class PlaybackGroup extends Group {
    private static final float[] FACTORS = {0.5f, 1f, 2f, 4f, 8f, 16f, 32f, 64f, 100f, 1000f};
    private static final String[] LABELS =
            {"0.5x", "1x", "2x", "4x", "8x", "16x", "32x", "64x", "100x", "1000x"};
    private static final int PUBLIC_CHOICES = 3;
    private static final int NORMAL = 1;

    private static float speed = 1f;
    private static int updating;

    public static int choiceCount() {
        return ApdFeatures.testBuild() ? FACTORS.length : PUBLIC_CHOICES;
    }

    public static boolean allowed(int selection) {
        return selection >= 0 && selection < choiceCount();
    }

    public static String label(int selection) {
        return LABELS[allowed(selection) ? selection : NORMAL];
    }

    public static void speed(int selection) {
        speed = FACTORS[allowed(selection) ? selection : NORMAL];
    }

    /** Unscaled wall-frame delta even when this group's visuals are accelerated.
     * Audio cadence must not be tied to selected visual playback speed. */
    public static float realElapsed() {
        return updating > 0 ? Game.elapsed / speed : Game.elapsed;
    }

    /**
     * Keep the hero's two-frame standing animation readable when experimental
     * visual playback is extremely fast. This runs only inside a PlaybackGroup;
     * movement/attacks/effects still receive the full selected speed.
     */
    public static float heroIdleElapsed() {
        return updating > 0 && speed > 2f ? Game.elapsed / speed : Game.elapsed;
    }

    @Override public synchronized void update() {
        float original = Game.elapsed;
        if (updating++ == 0) Game.elapsed = original * speed;
        try { super.update(); }
        finally { updating--; Game.elapsed = original; }
    }
}

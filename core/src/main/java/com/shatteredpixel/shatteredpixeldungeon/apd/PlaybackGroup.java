package com.shatteredpixel.shatteredpixeldungeon.apd;

import com.watabou.noosa.Game;
import com.watabou.noosa.Group;

// Scale only dungeon visual groups. HUD input timing and the actor clock stay unchanged.
public final class PlaybackGroup extends Group {
    private static float speed = 1f;
    private static int updating;
    public static void speed(int selection) {
        speed = selection == 0 ? 0.5f : selection == 2 ? 2f : 1f;
    }
    @Override public synchronized void update() {
        float original = Game.elapsed;
        if (updating++ == 0) Game.elapsed = original * speed;
        try { super.update(); }
        finally { updating--; Game.elapsed = original; }
    }
}

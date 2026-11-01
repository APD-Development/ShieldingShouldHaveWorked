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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.watabou.input.PointerEvent;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.Visual;
import org.junit.Test;
import static org.junit.Assert.*;

public class ScrollClickSuppressionTest {
    private static class VisibleHit extends Visual {
        VisibleHit(){ super(0, 0, 30, 30); }
        @Override public boolean overlapsScreenPoint(int x, int y){ return true; }
    }
    private static class ClickTarget extends PointerArea {
        int clicks;
        int releases;
        ClickTarget(){ super(new VisibleHit()); }
        @Override protected void onPointerUp(PointerEvent event){ releases++; }
        @Override protected void onClick(PointerEvent event){ clicks++; }
    }
    private static PointerEvent touch(int id) {
        return new PointerEvent(10, 12, id, PointerEvent.Type.DOWN, PointerEvent.LEFT);
    }

    @Test public void aScrollingGestureReleasesItsTargetButCannotActivateIt(){
        ClickTarget control = new ClickTarget();
        try {
            PointerEvent scrolling = touch(101);
            assertFalse(scrolling.scrolled);
            control.onSignal(scrolling);
            scrolling.scrolled = true;
            control.onSignal(scrolling.up());
            assertEquals("finger release is preserved", 1, control.releases);
            assertEquals("scroll cannot press a button or a link", 0, control.clicks);

            PointerEvent tap = touch(102);
            assertFalse("new pointers must never inherit old suppression", tap.scrolled);
            control.onSignal(tap);
            control.onSignal(tap.up());
            assertEquals("ordinary tap still activates control", 1, control.clicks);
            assertEquals(2, control.releases);

            PointerEvent cancelled = touch(103);
            control.onSignal(cancelled);
            control.onSignal(cancelled.cancel());
            assertEquals("cancel never clicks", 1, control.clicks);
        } finally {
            control.destroy();
        }
    }
}

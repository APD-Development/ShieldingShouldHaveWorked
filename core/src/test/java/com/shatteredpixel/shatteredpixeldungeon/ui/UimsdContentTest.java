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

import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import org.junit.Test;
import static org.junit.Assert.*;

public class UimsdContentTest {
    private static class Tracked extends Gizmo {
        int destroyed;
        @Override public void destroy() { destroyed++; super.destroy(); }
    }
    @Test public void replacedChildrenAreDisposedOnce() {
        UimsdContent content = new UimsdContent();
        Tracked child = new Tracked(); content.add(child);
        content.clear(); content.clear(); content.destroy();
        assertEquals(1, child.destroyed); assertNull(child.parent);
    }
    @Test public void replacementContainerRemainsReusable() {
        UimsdContent content = new UimsdContent();
        Tracked first = new Tracked(); content.add(first); content.clear();
        Tracked next = new Tracked(); content.add(next);
        assertSame(content, next.parent); assertEquals(0, next.destroyed);
        content.clear(); assertEquals(1, next.destroyed);
    }
    @Test public void nestedResourcesAreDisposed() {
        UimsdContent content = new UimsdContent();
        Group group = new Group(); Tracked child = new Tracked();
        group.add(child); content.add(group); content.clear();
        assertEquals(1, child.destroyed); assertNull(child.parent);
    }
    @Test public void childDetachingDuringDestructionDoesNotSkipSibling() {
        UimsdContent content = new UimsdContent();
        Tracked first = new Tracked() {
            @Override public void destroy() {
                if (parent != null) parent.erase(this);
                super.destroy();
            }
        };
        Tracked sibling = new Tracked();
        content.add(first); content.add(sibling); content.clear();
        assertEquals(1, first.destroyed); assertEquals(1, sibling.destroyed);
    }
    @Test public void removedChildrenAreNotDestroyedByFormerOwner() {
        UimsdContent content = new UimsdContent();
        Tracked removed = new Tracked(); Tracked retained = new Tracked();
        content.add(removed); content.add(retained); content.remove(removed);
        content.clear(); assertEquals(0, removed.destroyed);
        assertEquals(1, retained.destroyed);
    }
}

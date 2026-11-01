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

package com.shatteredpixel.shatteredpixeldungeon.apd.qol;

import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Read-only, player-visible open heap inspection. Never opens containers, identifies items or changes play. */
public final class HeapExamination {

    private HeapExamination() {}

    public static boolean hasVisiblePile(Heap heap) {
        return heap != null && heap.seen && (heap.type == Heap.Type.HEAP || heap.type == Heap.Type.FOR_SALE)
                && heap.items != null && heap.items.size() > 1;
    }

    /** A stable top-first view of precisely the live item references, without sorting or merging. */
    public static List<Item> snapshot(Heap heap) {
        if (!hasVisiblePile(heap)) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(heap.items));
    }

    /** Display only what the ordinary SPD item API tells the player, never true class/curse/level. */
    public static String visibleLabel(Item item) {
        if (item == null) throw new IllegalArgumentException("null item");
        String label = Messages.titleCase(item.name());
        return item.quantity() > 1 ? label + "  x" + item.quantity() : label;
    }

    /** Index selection only; never pops, collects or reveals anything. */
    public static Item select(List<Item> snapshot, int index) {
        if (snapshot == null || index < 0 || index >= snapshot.size()) return null;
        return snapshot.get(index);
    }
}

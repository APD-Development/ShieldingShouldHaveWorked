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

package com.shatteredpixel.shatteredpixeldungeon.apd.history;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/** Resolves existing item-sheet symbols without creating or identifying game items. */
public final class ApdSpriteRef {
    private ApdSpriteRef() {}
    public static Integer itemImage(String reference) {
        if (reference == null || !reference.startsWith("ITEM_")) return null;
        try {
            return ItemSpriteSheet.class.getField(reference.substring(5)).getInt(null);
        } catch (ReflectiveOperationException | IllegalArgumentException invalid) {
            return null;
        }
    }
}

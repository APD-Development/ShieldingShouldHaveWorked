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
package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;

/** One batched tile layer below normal fog/blockers, with no input handlers. */
public final class SearchedWallTilemap extends DungeonTilemap {
    public SearchedWallTilemap() {
        super("interfaces/apd_searched_wall.png");
        map(Dungeon.level.map, Dungeon.level.width());
    }
    @Override protected int getTileVisual(int pos, int tile, boolean flat) {
        return Dungeon.level.searchedWalls.shows(Dungeon.level, pos) ? 0 : -1;
    }
}

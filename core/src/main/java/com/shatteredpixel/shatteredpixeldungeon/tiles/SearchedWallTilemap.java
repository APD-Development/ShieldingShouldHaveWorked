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
        applyAppearance();
        map(Dungeon.level.map, Dungeon.level.width());
    }
    public void applyAppearance(){
        hardlight(com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchedWallAppearance.color(
                com.shatteredpixel.shatteredpixeldungeon.SPDSettings.searchedWallColor()).rgb);
    }
    public void refreshKnownGeometry(){
        if(data==null||data.length!=Dungeon.level.length())return;
        int width=Dungeon.level.width();
        for(int p=Dungeon.level.searchedWalls.nextMarked(0);p>=0;p=Dungeon.level.searchedWalls.nextMarked(p+1)){
            if(data[p]!=getTileVisual(p,map[p],false)
                    ||p>=width&&data[p-width]!=getTileVisual(p-width,map[p-width],false))updateMapCell(p);
        }
    }
    @Override protected int getTileVisual(int pos, int tile, boolean flat) {
        return com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchedWallMarkers.visual(Dungeon.level, pos);
    }
}

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

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet;

/** Projects saved search memory onto visible wall faces, edges and raised overhangs. */
public final class SearchedWallMarkers {
    public static final int FACE=1, OVERHANG=2, LEFT=4, RIGHT=8;
    private SearchedWallMarkers(){}
    private static boolean known(Level l,int p){return p>=0&&p<l.length()&&(l.heroFOV[p]||l.visited[p]||l.mapped[p]);}
    private static boolean open(Level l,int p){return known(l,p)&&!DungeonTileSheet.wallStitcheable(l.map[p]);}
    /** A renderer and UI must use the same public floor/terrain/geometry eligibility rule. */
    public static boolean couldBeDoor(Level l,int p){return DoorSearchEligibility.candidate(l,p);}
    private static boolean showWall(Level l,int p){return l.searchedWalls.shows(l,p)&&couldBeDoor(l,p);}
    public static int visual(Level l,int p){
        if(p<0||p>=l.length())return -1;
        int w=l.width(),mask=0;
        if(showWall(l,p)){
            if(p+w<l.length()&&open(l,p+w))mask|=FACE;
            else {
                if(p%w>0&&open(l,p-1)&&open(l,p-1-w)&&open(l,p-1+w))mask|=LEFT;
                if(p%w<w-1&&open(l,p+1)&&open(l,p+1-w)&&open(l,p+1+w))mask|=RIGHT;
            }
        }
        // SPD draws a southern wall's visible lip on the preceding floor tile.
        // Its own tile is obscured by wall blocking/fog; never bypass either layer.
        if(open(l,p)&&p+w<l.length()&&showWall(l,p+w))mask|=OVERHANG;
        return mask==0?-1:mask;
    }
}

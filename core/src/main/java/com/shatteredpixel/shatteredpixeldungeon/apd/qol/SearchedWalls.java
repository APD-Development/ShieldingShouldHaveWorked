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
import com.watabou.utils.Bundle;
import java.util.BitSet;

/** Per-floor memory of deliberately searched, visible walls; no discovery or search rolls. */
public final class SearchedWalls {
    private static final String KEY = "apd_searched_walls_v1";
    private final BitSet cells = new BitSet();
    private final BitSet doorChecks = new BitSet();
    private static final String CHECKS_KEY="apd_door_checks_v1";

    public boolean mark(Level level, int cell) {
        if (cell < 0 || cell >= level.length() || !level.heroFOV[cell]
                || !wall(level.map[cell]) || cells.get(cell)) return false;
        cells.set(cell);
        return true;
    }

    public boolean shows(Level level, int cell) {
        return cell >= 0 && cell < level.length() && cells.get(cell) && wall(level.map[cell])
                && (level.heroFOV[cell] || level.visited[cell] || level.mapped[cell]);
    }

    private static boolean wall(int tile) {
        // Secret doors look exactly like ordinary walls until the original search code discovers them.
        return tile == Terrain.WALL || tile == Terrain.WALL_DECO
                || tile == Terrain.SECRET_DOOR || tile == Terrain.BOOKSHELF;
    }

    /** Allocation-free traversal for refreshing only saved markers after observation. */
    public int nextMarked(int from){return cells.nextSetBit(Math.max(0,from));}
    public void clear() { cells.clear(); doorChecks.clear(); }
    public void invalidate(int cell){if(cell>=0){cells.clear(cell);doorChecks.clear(cell);}}
    public void recordCompletedDoorCheck(Level level,int cell,boolean guaranteed){
        if(cell<0||cell>=level.length())return;
        doorChecks.clear(cell);
        // A paid check must actually have resolved; an unresolved hidden tile is never a negative result.
        if(guaranteed&&level.heroFOV[cell]&&wall(level.map[cell])&&!level.secret[cell]
                &&level.map[cell]!=Terrain.SECRET_DOOR)doorChecks.set(cell);
    }
    public boolean confirmed(Level level,int cell){return cell>=0&&cell<level.length()&&level.heroFOV[cell]&&wall(level.map[cell])&&doorChecks.get(cell);}
    public boolean attempted(int cell){return cell>=0&&cells.get(cell);}


    public void store(Bundle bundle) {
        // An untouched floor retains its original payload and level-generation fingerprints.
        if (!cells.isEmpty()) bundle.put(KEY, cells.stream().toArray());
        if (!doorChecks.isEmpty()) bundle.put(CHECKS_KEY,doorChecks.stream().toArray());
    }

    public void restore(Bundle bundle, int length) {
        clear();
        if (bundle.contains(CHECKS_KEY))for(int cell:bundle.getIntArray(CHECKS_KEY))if(cell>=0&&cell<length)doorChecks.set(cell);
        if (!bundle.contains(KEY)) return; // Legacy saves have no fabricated search history.
        for (int cell : bundle.getIntArray(KEY)) {
            if (cell >= 0 && cell < length) cells.set(cell);
        }
    }
}

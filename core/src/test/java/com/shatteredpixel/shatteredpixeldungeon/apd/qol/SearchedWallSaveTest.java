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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.apd.LevelGenerationHarness;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.utils.Bundle;
import org.junit.*;
import java.io.*;
import static org.junit.Assert.*;

/** Actual Level save/restore, including compressed Bundle bytes; disposable headless data only. */
public class SearchedWallSaveTest {
    private Level oldLevel;
    private Hero oldHero;
    @Before public void setup(){HeadlessTestSupport.start();oldLevel=Dungeon.level;oldHero=Dungeon.hero;}
    @After public void cleanup(){Dungeon.level=oldLevel;Dungeon.hero=oldHero;}
    private Level floor()throws Exception{
        LevelGenerationHarness.generate(734811,0,HeroClass.WARRIOR,LevelGenerationHarness.Context.VETERAN,1);
        return Dungeon.level;
    }
    private int wall(Level l){for(int i=0;i<l.length();i++)if(l.map[i]==Terrain.WALL){l.heroFOV[i]=true;l.visited[i]=true;return i;}throw new AssertionError("No wall in generated fixture");}
    private Level reload(Level original,Bundle state)throws Exception{
        ByteArrayOutputStream out=new ByteArrayOutputStream();assertTrue(Bundle.write(state,out));
        Bundle fromBytes=Bundle.read(new ByteArrayInputStream(out.toByteArray()));
        Level loaded=original.getClass().getDeclaredConstructor().newInstance();Dungeon.level=loaded;loaded.restoreFromBundle(fromBytes);return loaded;
    }
    @Test public void searchedWallSurvivesRealFloorSaveReloadAndRevisit()throws Exception{
        Level original=floor();int cell=wall(original);assertTrue(original.searchedWalls.mark(original,cell));
        Bundle b=new Bundle();original.storeInBundle(b);Level loaded=reload(original,b);
        assertTrue(loaded.searchedWalls.shows(loaded,cell));assertArrayEquals(original.map,loaded.map);
        Bundle second=new Bundle();loaded.storeInBundle(second);Level revisited=reload(loaded,second);
        assertTrue(revisited.searchedWalls.shows(revisited,cell));assertEquals(original.heaps.valueList().size(),revisited.heaps.valueList().size());
    }
    @Test public void oldFloorWithoutFieldLoadsUnmarkedWithoutChangingItsMap()throws Exception{
        Level original=floor();int cell=wall(original);Bundle b=new Bundle();original.storeInBundle(b);
        assertFalse(b.contains("apd_searched_walls_v1"));Level loaded=reload(original,b);
        assertFalse(loaded.searchedWalls.shows(loaded,cell));assertArrayEquals(original.map,loaded.map);
    }
}

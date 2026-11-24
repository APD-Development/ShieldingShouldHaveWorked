package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FloorMaps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import org.junit.Test;

public class FloorMapCodesTest {

	@Test
	public void codesEachKindOfCell(){
		int[] map = { Terrain.WALL, Terrain.EMPTY, Terrain.GRASS, Terrain.DOOR, Terrain.SECRET_DOOR, Terrain.LOCKED_DOOR,
				Terrain.WATER, Terrain.CHASM, Terrain.ENTRANCE, Terrain.EXIT, Terrain.LOCKED_EXIT, Terrain.BOOKSHELF,
				Terrain.HIGH_GRASS, Terrain.WALL_DECO, Terrain.STATUE, Terrain.EMPTY_SP };
		assertEquals("#..+++~ <>>#.##.", FloorMapCodes.encode(map, 4, 4));
	}

	@Test
	public void refusesMapsThatDontFit(){
		assertNull(FloorMapCodes.encode(null, 1, 1));
		assertNull(FloorMapCodes.encode(new int[3], 2, 2));
		assertNull(FloorMapCodes.encode(new int[0], 0, 0));
		assertTrue(FloorMaps.valid("#.<>", 2, 2));
		assertFalse(FloorMaps.valid("#.<", 2, 2));
		assertFalse(FloorMaps.valid("#.<x", 2, 2));
		assertFalse(FloorMaps.valid(null, 2, 2));
	}
}

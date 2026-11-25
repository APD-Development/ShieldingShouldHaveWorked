/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FloorMaps;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

//Turns a floor's terrain into the floor.map text (see FloorMaps). Pure: reads only the array it is given.
public final class FloorMapCodes {

	private FloorMapCodes(){}

	public static char code( int terrain ){
		switch (terrain){
			case Terrain.ENTRANCE: case Terrain.ENTRANCE_SP: return FloorMaps.ENTRANCE;
			case Terrain.EXIT: case Terrain.LOCKED_EXIT: case Terrain.UNLOCKED_EXIT: return FloorMaps.EXIT;
			case Terrain.DOOR: case Terrain.OPEN_DOOR: return FloorMaps.DOOR;
			case Terrain.LOCKED_DOOR: case Terrain.HERO_LKD_DR: return FloorMaps.LOCKED_DOOR;
			case Terrain.CRYSTAL_DOOR: return FloorMaps.CRYSTAL_DOOR;
			case Terrain.SECRET_DOOR: return FloorMaps.HIDDEN_DOOR;
			case Terrain.WATER: return FloorMaps.WATER;
			case Terrain.CHASM: return FloorMaps.CHASM;
		}
		if (terrain < 0 || terrain >= Terrain.flags.length) return FloorMaps.WALL;
		int f = Terrain.flags[terrain];
		if ((f & Terrain.PIT) != 0) return FloorMaps.CHASM;
		if ((f & Terrain.SOLID) != 0 && (f & Terrain.PASSABLE) == 0) return FloorMaps.WALL;
		return FloorMaps.FLOOR;
	}

	//null when the map can't be kept (missing, empty, wrong size or too big)
	public static String encode( int[] map, int width, int height ){
		if (map == null || width <= 0 || height <= 0) return null;
		long n = (long) width * height;
		if (n > FloorMaps.MAX_CELLS || n != map.length) return null;
		StringBuilder sb = new StringBuilder((int) n);
		for (int t : map) sb.append(code(t));
		return sb.toString();
	}

}

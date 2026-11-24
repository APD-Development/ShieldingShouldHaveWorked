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

package com.shatteredpixel.shatteredpixeldungeon.apd.stats;

import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

//Turns a floor's terrain into the compact text the floor.map fact keeps (one character per cell, row by row), and back into cell kinds
//for the run replay. Pure: no game state, no random numbers.
public final class FloorMaps {

	private FloorMaps(){}

	public static final char WALL = '#', FLOOR = '.', DOOR = '+', WATER = '~', CHASM = ' ', ENTRANCE = '<', EXIT = '>';

	//a map bigger than this is not kept (no floor in the game is near it); a damaged one must not cost unbounded memory
	public static final int MAX_CELLS = 1 << 16;

	public static char code( int terrain ){
		switch (terrain){
			case Terrain.ENTRANCE: case Terrain.ENTRANCE_SP: return ENTRANCE;
			case Terrain.EXIT: case Terrain.LOCKED_EXIT: case Terrain.UNLOCKED_EXIT: return EXIT;
			case Terrain.DOOR: case Terrain.OPEN_DOOR: case Terrain.LOCKED_DOOR: case Terrain.HERO_LKD_DR:
			case Terrain.CRYSTAL_DOOR: case Terrain.SECRET_DOOR: return DOOR;
			case Terrain.WATER: return WATER;
			case Terrain.CHASM: return CHASM;
		}
		if (terrain < 0 || terrain >= Terrain.flags.length) return WALL;
		int f = Terrain.flags[terrain];
		if ((f & Terrain.PIT) != 0) return CHASM;
		if ((f & Terrain.SOLID) != 0 && (f & Terrain.PASSABLE) == 0) return WALL;
		return FLOOR;
	}

	//null when the map can't be kept (missing, empty, wrong size or too big)
	public static String encode( int[] map, int width, int height ){
		if (map == null || width <= 0 || height <= 0) return null;
		long n = (long) width * height;
		if (n > MAX_CELLS || n != map.length) return null;
		StringBuilder sb = new StringBuilder((int) n);
		for (int t : map) sb.append(code(t));
		return sb.toString();
	}

	//true when a recorded map's text fits its declared size and only uses the known codes
	public static boolean valid( String cells, long width, long height ){
		if (cells == null || width <= 0 || height <= 0 || width * height > MAX_CELLS || cells.length() != width * height) return false;
		for (int i = 0; i < cells.length(); i++){
			char c = cells.charAt(i);
			if (c != WALL && c != FLOOR && c != DOOR && c != WATER && c != CHASM && c != ENTRANCE && c != EXIT) return false;
		}
		return true;
	}
}

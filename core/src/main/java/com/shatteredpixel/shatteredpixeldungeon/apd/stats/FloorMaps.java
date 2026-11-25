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

//The compact text the floor.map fact keeps (one character per cell, row by row; written by recorder.FloorMapCodes), for the run replay. Pure: no game state, no random numbers.
public final class FloorMaps {

	private FloorMaps(){}

	public static final char WALL = '#', FLOOR = '.', DOOR = '+', WATER = '~', CHASM = ' ', ENTRANCE = '<', EXIT = '>';
	//door kinds (recorded from Nov 25): a door that needed a key, a crystal door, a hidden (secret) door. Older maps only have DOOR.
	public static final char LOCKED_DOOR = '=', CRYSTAL_DOOR = '*', HIDDEN_DOOR = '?';

	//a map bigger than this is not kept (no floor in the game is near it); a damaged one must not cost unbounded memory
	public static final int MAX_CELLS = 1 << 16;

	//true when a recorded map's text fits its declared size and only uses the known codes
	public static boolean valid( String cells, long width, long height ){
		if (cells == null || width <= 0 || height <= 0 || width * height > MAX_CELLS || cells.length() != width * height) return false;
		for (int i = 0; i < cells.length(); i++){
			char c = cells.charAt(i);
			if (c != WALL && c != FLOOR && c != DOOR && c != WATER && c != CHASM && c != ENTRANCE && c != EXIT
					&& c != LOCKED_DOOR && c != CRYSTAL_DOOR && c != HIDDEN_DOOR) return false;
		}
		return true;
	}
}

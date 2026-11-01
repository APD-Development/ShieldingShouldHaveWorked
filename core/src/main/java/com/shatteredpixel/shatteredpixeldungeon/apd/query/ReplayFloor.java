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

package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import java.util.ArrayList;
import java.util.List;

//One floor as a floor replay generated it: a CANONICAL SEED FLOOR under the reference context. Plain data, read-only once built.
//It is never the record of what a run met (that is the recorded telemetry); it is what the seed gives in an in-order replay.
public final class ReplayFloor {

	public static final class Item {
		public String trueClass = "";
		public int level;
		public boolean cursed;
		public String enchant = "";
		public int quantity;
		//where it was found: heap kind (HEAP, CHEST, LOCKED_CHEST, CRYSTAL_CHEST, TOMB, SKELETON, REMAINS, FOR_SALE),
		//MIMIC, STATUE, or QUEST_REWARD
		public String container = "";
		public int pos;
		public boolean hidden;
		public String room = "";
		//the creature holding it (mimic, statue, quest giver), else ""
		public String owner = "";
	}

	public static final class Mob {
		public String mobClass = "";
		public int pos;
		public int hp;
		public String room = "";
		public List<String> champion = new ArrayList<>();
	}

	public int depth;
	public String levelClass = "";
	public String feeling = "";
	public int width, height;
	public int trapsVisible, trapsHidden;
	public List<String> rooms = new ArrayList<>();
	public List<Item> items = new ArrayList<>();
	public List<Mob> mobs = new ArrayList<>();
	//SHA-256 of the full saved level (tests and cache validation only)
	public String fingerprint = "";
}

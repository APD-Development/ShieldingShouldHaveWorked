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

package com.shatteredpixel.shatteredpixeldungeon.apd;

import static org.junit.Assert.*;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import org.junit.After;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

// Pins the cause of "the floor-1 start room has no exit": SPD's first-run tutorial hides the entrance door on purpose. A used install must
// not be in the tutorial (ApdIntro), and a floor generated without it must have a normal, walkable way out.
public class FirstFloorExitTest {

	private final Map<Integer, Level> seen = new HashMap<>();

	@After
	public void clear(){
		LevelGenerationHarness.levelObserver = null;
	}

	//can the hero walk from the entrance to the stairs down, without searching for anything?
	private static boolean exitWalkable( Level l ){
		boolean[] seenCell = new boolean[l.length()];
		ArrayDeque<Integer> q = new ArrayDeque<>();
		q.add(l.entrance());
		seenCell[l.entrance()] = true;
		int[] steps = { -1, 1, -l.width(), l.width() };
		while (!q.isEmpty()){
			int c = q.poll();
			if (c == l.exit()) return true;
			for (int s : steps){
				int n = c + s;
				if (n < 0 || n >= l.length() || seenCell[n]) continue;
				if (n == l.exit() || (Terrain.flags[l.map[n]] & Terrain.PASSABLE) != 0){
					seenCell[n] = true;
					q.add(n);
				}
			}
		}
		return false;
	}

	private Level floorOne( long seed, LevelGenerationHarness.Context context ) throws Exception {
		seen.clear();
		LevelGenerationHarness.levelObserver = new java.util.function.BiConsumer<Integer, Level>(){
			@Override
			public void accept( Integer depth, Level level ){
				seen.put(depth, level);
			}
		};
		LevelGenerationHarness.generate(seed, 0, HeroClass.WARRIOR, context, 2);
		return seen.get(1);
	}

	@Test
	public void theTutorialStartIsClosedAndANormalStartIsOpen() throws Exception {
		int closed = 0, open = 0;
		for (long seed = 1; seed <= 12; seed++){
			Level tutorial = floorOne(seed * 7919, LevelGenerationHarness.Context.FRESH);
			Level normal = floorOne(seed * 7919, LevelGenerationHarness.Context.VETERAN);
			assertNotNull(tutorial);
			assertNotNull(normal);
			if (!exitWalkable(tutorial)) closed++;
			if (exitWalkable(normal)) open++;
		}
		assertEquals("every tutorial floor 1 hides its way out (the tester's 'no exit' rooms)", 12, closed);
		assertEquals("every normal floor 1 has a walkable way out", 12, open);
	}

	@Test
	public void aReturningPlayerWithNoGuidePagesGetsAnOpenFloorOneAndWeSeeWhatFloorTwoDoes() throws Exception {
		int open1 = 0, open2 = 0;
		for (long seed = 1; seed <= 12; seed++){
			Level one = floorOne(seed * 104729, LevelGenerationHarness.Context.RETURNING);
			if (exitWalkable(one)) open1++;
			if (exitWalkable(seen.get(2))) open2++;
		}
		System.out.println("APD-RETURNING floor1 walkable " + open1 + "/12, floor2 walkable " + open2 + "/12");
		assertEquals("floor 1 must be open once the tutorial is over", 12, open1);
	}
}

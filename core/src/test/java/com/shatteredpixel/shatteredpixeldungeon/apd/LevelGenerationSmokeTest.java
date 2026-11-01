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

package com.shatteredpixel.shatteredpixeldungeon.apd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;

import org.junit.Test;

import java.util.List;

public class LevelGenerationSmokeTest {

	@Test
	public void headlessLevelGenerationWorks() throws Exception {
		List<LevelGenerationHarness.LevelPrint> prints = LevelGenerationHarness.generate(12345L, 0, HeroClass.WARRIOR, LevelGenerationHarness.Context.FRESH, 3);
		assertEquals(3, prints.size());
		for (LevelGenerationHarness.LevelPrint p : prints){
			System.out.println("APD-SMOKE " + p.line());
			assertFalse(p.hash.isEmpty());
		}
	}
}

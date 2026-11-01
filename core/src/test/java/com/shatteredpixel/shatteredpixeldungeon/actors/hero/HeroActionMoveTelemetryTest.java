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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.apd.navigation.StairNavigation;

import org.junit.Test;

public class HeroActionMoveTelemetryTest {

	@Test
	public void movementModesAreStableAndRecordOnlyOnce(){
		HeroAction.Move step = new HeroAction.Move(1);
		HeroAction.Move travel = new HeroAction.Move(2, HeroAction.Move.Mode.TRAVEL);
		HeroAction.Move stairs = new StairNavigation.Walk(3, null);

		assertEquals("step", step.apdModeTag());
		assertEquals("travel", travel.apdModeTag());
		assertEquals("stairs", stairs.apdModeTag());
		assertTrue(step.apdStartRecording());
		assertFalse(step.apdStartRecording());
	}
}

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
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.Difficulty;
import com.shatteredpixel.shatteredpixeldungeon.ui.DifficultyBar;

import org.junit.Test;

//The run-setup difficulty picker: every level 1-5 has a name, 3 is the normal game, and the explanation says the difficulty is fixed once the run starts.
public class DifficultyBarTest {

	@Test
	public void everyLevelHasAName_andThreeIsNormal(){
		for (int level = Difficulty.MIN; level <= Difficulty.MAX; level++) assertFalse("level " + level, DifficultyBar.nameOf(level).isEmpty());
		assertEquals("Normal", DifficultyBar.nameOf(Difficulty.DEFAULT));
		assertEquals(3, Difficulty.DEFAULT);
		assertEquals("", DifficultyBar.nameOf(0));
		assertEquals("", DifficultyBar.nameOf(6));
	}

	@Test
	public void theExplanationSaysThatARunKeepsItsDifficulty(){
		String d = DifficultyBar.describe();
		assertTrue(d.contains("3 is the normal game"));
		assertTrue(d.contains("fixed when the run begins"));
		assertTrue(d.contains("never changes a run that is already going"));
	}

	@Test
	public void normalIsStillTheUnmodifiedBaseline(){
		//the move to run setup changes where the level is chosen, never what a level does
		assertEquals(0, Difficulty.balanceRevision(Difficulty.DEFAULT));
	}
}

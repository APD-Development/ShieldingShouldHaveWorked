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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Direct contracts for the immutable text/provenance value shared by tables,
 * home cards, row details and report builders.
 */
public class CellTextContractTest {

	@Test
	public void knowledgeStateIdsRemainStableAndContiguous(){
		assertEquals(0, CellText.EXACT);
		assertEquals(1, CellText.LOWER);
		assertEquals(2, CellText.INFERRED);
		assertEquals(3, CellText.LEGACY);
		assertEquals(4, CellText.UNKNOWN);
		assertEquals(5, CellText.HIDDEN);
	}

	@Test
	public void twoArgumentConstructorDefaultsToNormalSampleAndReturnsItsText(){
		CellText cell = new CellText(">=12", CellText.LOWER);

		assertEquals(">=12", cell.text);
		assertEquals(CellText.LOWER, cell.state);
		assertFalse(cell.small);
		assertEquals(cell.text, cell.toString());
	}

	@Test
	public void threeArgumentConstructorPreservesSmallSampleAndKnowledgeState(){
		CellText cell = new CellText("~6", CellText.INFERRED, true);

		assertEquals("~6", cell.text);
		assertEquals(CellText.INFERRED, cell.state);
		assertTrue(cell.small);
		assertEquals(cell.text, cell.toString());
	}
}

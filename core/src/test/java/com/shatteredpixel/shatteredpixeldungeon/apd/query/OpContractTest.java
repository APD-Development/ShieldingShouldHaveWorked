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

import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class OpContractTest {

	@Test
	public void eachColumnTypeOffersExactlyItsPublicOperatorsInPickerOrder(){
		assertArrayEquals(new Op[]{
				Op.EQ, Op.NE, Op.LT, Op.LE, Op.GT, Op.GE, Op.BETWEEN, Op.KNOWN, Op.UNKNOWN
		}, Op.ops(ColumnDef.Type.NUM));
		assertArrayEquals(new Op[]{
				Op.EQ, Op.NE, Op.CONTAINS, Op.IN, Op.KNOWN, Op.UNKNOWN
		}, Op.ops(ColumnDef.Type.TEXT));
		assertArrayEquals(new Op[]{
				Op.EQ, Op.NE, Op.IN, Op.KNOWN, Op.UNKNOWN
		}, Op.ops(ColumnDef.Type.ENUM));
	}

	@Test
	public void onlyPresenceOperatorsOmitAValueAndInternalOperatorsAreNeverOffered(){
		for (Op op : Op.values()){
			assertEquals(op.name(), op != Op.KNOWN && op != Op.UNKNOWN, op.takesValue());
		}

		EnumSet<Op> offered = EnumSet.noneOf(Op.class);
		for (ColumnDef.Type type : ColumnDef.Type.values()) offered.addAll(Arrays.asList(Op.ops(type)));
		for (Op internal : new Op[]{Op.GROUP_VALUE, Op.GROUP_BOUND, Op.GROUP_TEXT, Op.IDENTIFIER_IN}){
			assertFalse(internal.name(), offered.contains(internal));
		}
	}
}

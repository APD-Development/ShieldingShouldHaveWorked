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


package com.shatteredpixel.shatteredpixeldungeon.apd.query;

//What a filter does with its column. Which operators a column offers depends on its type (see ops()).
public enum Op {
	EQ("is"), NE("is not"), LT("<"), LE("<="), GT(">"), GE(">="), BETWEEN("between"), CONTAINS("contains"), IN("is one of"),
	KNOWN("is known"), UNKNOWN("is unknown");

	public final String word;

	Op( String word ){
		this.word = word;
	}

	//the operators that make sense for a column type, in the order a picker should list them
	public static Op[] ops( ColumnDef.Type type ){
		switch (type){
			case NUM:
				return new Op[]{EQ, NE, LT, LE, GT, GE, BETWEEN, KNOWN, UNKNOWN};
			case TEXT:
				return new Op[]{EQ, NE, CONTAINS, IN, KNOWN, UNKNOWN};
			default:
				return new Op[]{EQ, NE, IN, KNOWN, UNKNOWN};
		}
	}

	public boolean takesValue(){
		return this != KNOWN && this != UNKNOWN;
	}
}

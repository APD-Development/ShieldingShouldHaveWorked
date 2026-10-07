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

//A number computed for each group. Only known values take part; the result says how many did.
public final class Aggregate {

	public enum Fn { COUNT, COUNT_KNOWN, SUM, AVG, MIN, MAX, RATE }

	public final Fn fn;
	//the column (COUNT has none)
	public final String column;
	//RATE: the value of the column that counts as a hit (a win rate is outcome = won)
	public final long target;

	public Aggregate( Fn fn, String column, long target ){
		this.fn = fn;
		this.column = column == null ? "" : column;
		this.target = target;
	}

	public static Aggregate count(){
		return new Aggregate(Fn.COUNT, null, 0);
	}

	public static Aggregate of( Fn fn, String column ){
		return new Aggregate(fn, column, 0);
	}

	public static Aggregate rate( String column, long target ){
		return new Aggregate(Fn.RATE, column, target);
	}

	//the id a Sort can refer to
	public String id(){
		return fn == Fn.COUNT ? "count" : fn == Fn.RATE ? "rate:" + column + "=" + target : fn.name().toLowerCase() + ":" + column;
	}

	@Override
	public boolean equals( Object o ){
		if (!(o instanceof Aggregate)) return false;
		Aggregate a = (Aggregate) o;
		return fn == a.fn && column.equals(a.column) && target == a.target;
	}

	@Override
	public int hashCode(){
		return fn.hashCode() * 31 + column.hashCode() + (int) target;
	}
}

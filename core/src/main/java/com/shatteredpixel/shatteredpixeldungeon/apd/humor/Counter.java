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

package com.shatteredpixel.shatteredpixeldungeon.apd.humor;

// The two numbers on the counter screen. `total` is the count of authored complaints in the installed version; `heard` is how many
// distinct ones this player has been shown (each counted once, however often it appeared).
public final class Counter {

	public final int total;
	public final int heard;

	Counter( int total, int heard ){
		this.total = total;
		this.heard = heard;
	}

	public String totalText(){
		return "I'VE COMPLAINED ABOUT CLERIC " + total + " TIMES.";
	}

	public String heardText(){
		return "YOU'VE HEARD ME COMPLAIN " + heard + " TIMES.";
	}
}

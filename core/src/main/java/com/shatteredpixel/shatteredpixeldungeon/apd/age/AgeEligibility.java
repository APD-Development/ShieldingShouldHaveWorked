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

package com.shatteredpixel.shatteredpixeldungeon.apd.age;

/** Verified age signals and the independent player humor preference.
 * These are APD policy states, not claimed Google Play API status values.
 */
public final class AgeEligibility {
	public enum State { ELIGIBLE, INELIGIBLE, UNKNOWN, UNAVAILABLE }

	private AgeEligibility(){}

	//No age provider is integrated. Missing signals must not be treated as verified adulthood.
	public static State current(){
		return State.UNAVAILABLE;
	}

	public static boolean permitsAdult(State state){
		return state != State.INELIGIBLE;
	}

	//Verified adults default to Adult; otherwise Clean. An explicit Clean choice wins.
	public static boolean defaultAdultHumor(State state){
		return state == State.ELIGIBLE;
	}

	public static boolean adultHumor(State state, boolean preferred){
		return preferred && permitsAdult(state);
	}
}

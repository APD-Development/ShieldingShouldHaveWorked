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

package com.watabou.input;

//Android can deliver one press of Back by two routes at once (the game's own key handling and the system back-gesture callback, depending
//on the Android version and how the app was opened). Both routes go through this gate, so one press is ONE Back, never two: a second
//Back arriving within a very short moment of the first is the same press.
public final class BackGate {

	private BackGate(){}

	//a real second press takes a person far longer than this
	public static final long SAME_PRESS_NANOS = 120_000_000L;

	private static long last;
	private static boolean any;

	//true if this Back should be acted on
	public static synchronized boolean accept(){
		return accept(System.nanoTime());
	}

	public static synchronized boolean accept( long nowNanos ){
		if (any && nowNanos - last >= 0 && nowNanos - last < SAME_PRESS_NANOS) return false;
		last = nowNanos;
		any = true;
		return true;
	}

	public static synchronized void reset(){
		any = false;
		last = 0;
	}
}

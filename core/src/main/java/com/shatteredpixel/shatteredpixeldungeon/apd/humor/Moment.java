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

// The few places where a line may be shown. Each is a quiet point: no turn is running and nothing the player is deciding is waiting.
public enum Moment {

	// the title screen, built and idle
	MENU( true ),
	// a new floor has fully loaded and the hero is idle with nothing hostile in view
	LEVEL_CALM( true ),
	// a boss was just defeated and the hero is idle again with nothing hostile in view
	BOSS_CLEARED( true ),
	// after the run has permanently ended and its results are on screen
	RUN_END_DEATH( true ),
	RUN_END_VICTORY( true ),
	// the player just picked Cleric to start a run (their own choice)
	FIRST_CLERIC_RUN( false ),
	// the player opened the counter screen
	COUNTER_SCREEN( false );

	// a surprise: it counts against the popup budget
	public final boolean surprise;

	Moment( boolean surprise ){
		this.surprise = surprise;
	}
}

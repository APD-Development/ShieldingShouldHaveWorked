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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BossClassesTest {

	@Test
	public void knownBossesAreBossesAndOrdinaryMobsAreNot(){
		for (String n : new String[]{ "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo", "Tengu", "com.x.DM300", "DwarfKing", "YogDzewa", "com.x.YogFist$BurningFist", "RottingFist" }){
			assertTrue(n, BossClasses.isBoss(n));
		}
		for (String n : new String[]{ "Rat", "com.x.Gnoll", "DM201", "(several)", "", null }){
			assertFalse(String.valueOf(n), BossClasses.isBoss(n));
		}
	}
}

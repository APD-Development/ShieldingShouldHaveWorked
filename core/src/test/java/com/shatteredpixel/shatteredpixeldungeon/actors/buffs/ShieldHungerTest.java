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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import static org.junit.Assert.assertEquals;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

import org.junit.Test;

//APD-0001 regression: starvation damage must consume shielding before HP
public class ShieldHungerTest {

	private static class TestChar extends Char {
		TestChar(int hp) {
			HP = HT = hp;
		}
	}

	private static class TestShield extends ShieldBuff {
		TestShield(int amount) {
			setShield(amount);
		}
	}

	@Test
	public void starvationTickConsumesShieldBeforeHp() {
		Char target = new TestChar(1);
		TestShield shield = new TestShield(5);
		shield.attachTo(target);

		//one starvation tick of 1 damage, as dealt by Hunger.act()
		int leftover = ShieldBuff.processDamage(target, 1, new Hunger());
		target.HP -= leftover;

		assertEquals(4, shield.shielding());
		assertEquals(1, target.HP);
	}

	@Test
	public void starvationOnlyHurtsHpOnceShieldIsGone() {
		Char target = new TestChar(10);
		TestShield shield = new TestShield(1);
		shield.attachTo(target);

		int leftover = ShieldBuff.processDamage(target, 3, new Hunger());

		assertEquals(0, shield.shielding());
		assertEquals(2, leftover);
	}
}

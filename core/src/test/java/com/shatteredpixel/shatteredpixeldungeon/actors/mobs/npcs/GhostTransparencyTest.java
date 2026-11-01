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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;

import org.junit.After;
import org.junit.Test;

//APD change 2.2: the gear the Sad ghost offers shows its real upgrade level. Whether it is cursed or carries an enchantment stays hidden.
public class GhostTransparencyTest {

	@After
	public void cleanUp(){
		Ghost.Quest.weapon = null;
		Ghost.Quest.armor = null;
	}

	@Test
	public void offeredGearShowsItsRealLevelAndNothingElse(){
		Sword weapon = new Sword();
		weapon.level(2);
		LeatherArmor armor = new LeatherArmor();
		armor.level(3);
		weapon.levelKnown = false;
		armor.levelKnown = false;
		weapon.cursedKnown = false;
		armor.cursedKnown = false;
		Ghost.Quest.weapon = weapon;
		Ghost.Quest.armor = armor;

		assertEquals("unknown level reads as 0 before the reveal", 0, weapon.visiblyUpgraded());
		Ghost.Quest.revealOfferedLevels();

		assertTrue(weapon.levelKnown);
		assertTrue(armor.levelKnown);
		assertEquals(2, weapon.visiblyUpgraded());
		assertEquals(3, armor.visiblyUpgraded());
		assertFalse("curse state stays hidden", weapon.cursedKnown);
		assertFalse(armor.cursedKnown);
	}

	@Test
	public void revealingWithNoOfferIsHarmless(){
		Ghost.Quest.weapon = null;
		Ghost.Quest.armor = null;
		Ghost.Quest.revealOfferedLevels();
	}
}

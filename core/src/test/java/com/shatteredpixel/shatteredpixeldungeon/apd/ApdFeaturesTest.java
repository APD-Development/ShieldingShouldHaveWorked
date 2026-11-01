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

package com.shatteredpixel.shatteredpixeldungeon.apd;

import static org.junit.Assert.*;

import com.shatteredpixel.shatteredpixeldungeon.apd.demo.UimsdUnlock;
import com.shatteredpixel.shatteredpixeldungeon.apd.humor.ClericGrudge;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.Ruleset;

import org.junit.After;
import org.junit.Test;

public class ApdFeaturesTest {

	@After
	public void off(){
		ApdFeatures.resetForTest();
	}

	@Test
	public void everySwitchIsOffUnlessTheTestBuildFileNamesIt(){
		ApdFeatures.resetForTest();
		assertFalse(ApdFeatures.testBuild());
		assertFalse(ApdFeatures.grudge() || ClericGrudge.enabled());
		assertFalse(ApdFeatures.demoUnlock() || UimsdUnlock.enforced());
		assertFalse(ApdFeatures.clericPlayable());
	}

	@Test
	public void theFileTurnsOnOnlyWhatItNames(){
		ApdFeatures.parse("# private test\ngrudge\r\nunknownThing\n  demoUnlock  \n");
		//the Cleric Grudge / Lore is postponed: naming it in the file does nothing while the central release switch is off
		assertFalse(ApdFeatures.GRUDGE_LORE_IN_THIS_RELEASE);
		assertFalse(ApdFeatures.grudge() || ClericGrudge.enabled());
		assertTrue(ApdFeatures.demoUnlock() && UimsdUnlock.enforced());
		assertFalse(ApdFeatures.testBuild());
		assertFalse(ApdFeatures.clericPlayable());
		ApdFeatures.parse("");
		assertFalse(ApdFeatures.grudge());
	}

    @Test
    public void privateBuildExplicitlyEnablesLoreWithoutEnablingNormalRelease() {
        ApdFeatures.parse("testBuild\ngrudge\nclericPlayable\n");
        assertFalse(ApdFeatures.GRUDGE_LORE_IN_THIS_RELEASE);
        assertTrue(ApdFeatures.grudge());
        assertTrue(ClericGrudge.enabled());
        assertTrue(ApdFeatures.clericPlayable());
        ApdFeatures.parse("testBuild\nclericPlayable\n");
        assertFalse(ApdFeatures.grudge());
        ApdFeatures.parse("grudge\n");
        assertFalse(ApdFeatures.grudge());
    }

	@Test
	public void clericRunsHaveTheirOwnRulesetAndEveryoneElseKeepsTheirs(){
		Ruleset normal = Ruleset.of(30, 0, 3, 0, false);
		Ruleset cleric = Ruleset.of(30, 0, 3, 0, true);
		//the id every recorded run has today (see DifficultyTest) does not move
		assertEquals("r1-37d60350c6b1", normal.id);
		assertEquals(Ruleset.of(30, 0).id, normal.id);
		assertTrue(cleric.text.contains("cleric=playable") && !cleric.text.contains("cleric=blocked"));
		assertNotEquals(normal.id, cleric.id);
		assertEquals(cleric.id, Ruleset.of(30, 0, 3, 0, true).id);
	}
}


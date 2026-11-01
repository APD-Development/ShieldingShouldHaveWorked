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

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;
import com.watabou.utils.Bundle;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class ImpQuestPersistenceTest {

	@After
	public void cleanUp() { Imp.Quest.reset(); }

	private Bundle legacyCompletedQuest() {
		Bundle quests = new Bundle();
		Bundle imp = new Bundle();
		imp.put("spawned", true);
		imp.put("given", true);
		imp.put("completed", true);
		imp.put("alternative", true);
		quests.put("demon", imp);
		return quests;
	}

	@Test
	public void loadingSaveWithoutQuestCannotInheritCompletedQuest() {
		Imp.Quest.restoreFromBundle(legacyCompletedQuest());
		assertTrue(Imp.Quest.isCompleted());
		assertTrue(Imp.Quest.earnedShop());
		Imp.Quest.restoreFromBundle(new Bundle());
		assertFalse(Imp.Quest.isCompleted());
		assertFalse(Imp.Quest.earnedShop());
		assertFalse(Imp.Quest.given());
	}

	@Test
	public void unspawnedSaveClearsPreviousQuestAndRewards() {
		Imp.Quest.restoreFromBundle(legacyCompletedQuest());
		Imp.Quest.rewardOptions.add(new Sword());
		Bundle quests = new Bundle();
		Bundle imp = new Bundle();
		imp.put("spawned", false);
		quests.put("demon", imp);
		Imp.Quest.restoreFromBundle(quests);
		assertFalse(Imp.Quest.earnedShop());
		assertFalse(Imp.Quest.isOld());
		assertTrue(Imp.Quest.rewardOptions.isEmpty());
	}

	@Test
	public void resetClearsRewardsAndLegacyStateWithoutChangingLoadedLegacySave() {
		Imp.Quest.restoreFromBundle(legacyCompletedQuest());
		assertTrue(Imp.Quest.isOld());
		Imp.Quest.rewardOptions.add(new Sword());
		Imp.Quest.reset();
		assertFalse(Imp.Quest.isOld());
		assertTrue(Imp.Quest.rewardOptions.isEmpty());
		assertEquals(2, Imp.Quest.hazardFreebies);
		Imp.Quest.restoreFromBundle(legacyCompletedQuest());
		assertTrue(Imp.Quest.isOld());
		assertTrue(Imp.Quest.earnedShop());
	}
}

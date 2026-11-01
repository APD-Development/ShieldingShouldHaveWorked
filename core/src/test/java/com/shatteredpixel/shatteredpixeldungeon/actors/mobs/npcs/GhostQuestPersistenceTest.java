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

import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Flow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.watabou.utils.Bundle;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class GhostQuestPersistenceTest {
	@After
	public void cleanUp() { Ghost.Quest.reset(); }

	@Test
	public void unenchantedSaveCannotInheritPreviousSavesEnchantments() {
		Ghost.Quest.enchant = new Blazing();
		Ghost.Quest.glyph = new Flow();
		Bundle quests = new Bundle();
		Bundle ghost = new Bundle();
		ghost.put("spawned", true);
		quests.put("sadGhost", ghost);
		Ghost.Quest.restoreFromBundle(quests);
		assertNull(Ghost.Quest.enchant);
		assertNull(Ghost.Quest.glyph);
	}

	@Test
	public void enchantedSaveStillRestoresItsOwnEnchantments() {
		Bundle quests = new Bundle();
		Bundle ghost = new Bundle();
		ghost.put("spawned", true);
		ghost.put("enchant", new Blazing());
		ghost.put("glyph", new Flow());
		quests.put("sadGhost", ghost);
		Ghost.Quest.restoreFromBundle(quests);
		assertTrue(Ghost.Quest.enchant instanceof Blazing);
		assertTrue(Ghost.Quest.glyph instanceof Flow);
	}
}

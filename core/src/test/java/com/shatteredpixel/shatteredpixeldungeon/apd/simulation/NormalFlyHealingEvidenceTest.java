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

package com.shatteredpixel.shatteredpixeldungeon.apd.simulation;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.GenerationEvents;
import org.junit.Test;
import static org.junit.Assert.*;

public class NormalFlyHealingEvidenceTest {
    private GenerationEvents.ItemGenerated drop() {
        GenerationEvents.ItemGenerated d = new GenerationEvents.ItemGenerated();
        d.container = GenerationEvents.C_MOB_LOOT;
        d.owner = "actors.mobs.Swarm";
        d.trueClass = "items.potions.PotionOfHealing";
        return d;
    }
    @Test public void acceptsActualRecorderNamesAndRejectsSimpleNames() {
        GenerationEvents.ItemGenerated d = drop();
        assertTrue(NormalFlyHealingEvidence.matches(d));
        d.owner = "Swarm"; assertFalse(NormalFlyHealingEvidence.matches(d));
        d = drop(); d.trueClass = "PotionOfHealing"; assertFalse(NormalFlyHealingEvidence.matches(d));
    }
    @Test public void bonusLootAndOtherSourcesAreNotNormalFlyHealing() {
        GenerationEvents.ItemGenerated d = drop(); d.container = GenerationEvents.C_BONUS_DROP;
        assertFalse(NormalFlyHealingEvidence.matches(d));
        d = drop(); d.owner = "actors.mobs.Bat"; assertFalse(NormalFlyHealingEvidence.matches(d));
        assertFalse(NormalFlyHealingEvidence.matches(null));
    }
    @Test public void exoticShieldingDoesNotBecomeAHealingObservation() {
        GenerationEvents.ItemGenerated d = drop(); d.trueClass = "items.potions.exotic.PotionOfShielding";
        assertFalse(NormalFlyHealingEvidence.matches(d));
    }
}

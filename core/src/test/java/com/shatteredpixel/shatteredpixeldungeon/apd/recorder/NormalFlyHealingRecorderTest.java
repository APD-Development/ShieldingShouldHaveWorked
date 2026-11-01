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

package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm;
import com.shatteredpixel.shatteredpixeldungeon.apd.simulation.NormalFlyHealingEvidence;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.*;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class NormalFlyHealingRecorderTest {
    @After public void resetRecorder() { ApdRecorder.reset(); }
    @Test public void classifierReadsActualSerializedLootAndRejectsShieldingAndBonusLoot() throws Exception {
        RecorderTestSupport.Session session = ItemTestSupport.startRun(391);
        Swarm source = new Swarm(); source.pos = Dungeon.hero.pos;
        Item healing = new PotionOfHealing(), shielding = new PotionOfShielding(), bonus = new PotionOfHealing();
        ApdGen.onLootCreated(source, healing);
        ApdGen.onLootCreated(source, shielding);
        ApdGen.onBonusDrop(bonus, source.pos);
        ReadResult read = ItemTestSupport.finish(session);
        assertTrue(read.status.isClean());
        int found = 0;
        for (ApdEvent event : read.events) if (event.type == EventTypes.ITEM_GENERATED) {
            GenerationEvents.ItemGenerated drop = GenerationEvents.decodeItemGenerated(event, read);
            if (drop.uid.equals(healing.apdUid)) { assertTrue(NormalFlyHealingEvidence.matches(drop)); found++; }
            if (drop.uid.equals(shielding.apdUid) || drop.uid.equals(bonus.apdUid)) {
                assertFalse(NormalFlyHealingEvidence.matches(drop)); found++;
            }
        }
        assertEquals("all three real serialized events were checked", 3, found);
    }
}

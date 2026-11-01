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

package com.shatteredpixel.shatteredpixeldungeon.apd.humor;

import com.shatteredpixel.shatteredpixeldungeon.apd.ApdFeatures;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.util.Random;
import static org.junit.Assert.*;

public class ClericLoreActivationTest {
    @Rule public TemporaryFolder tmp = new TemporaryFolder();
    @After public void reset() { ApdFeatures.resetForTest(); }

    @Test public void privateBuildGateReachesShippedFirstRunAndRemembersOnlyActualDisplay() {
        ApdFeatures.parse("testBuild\ngrudge\nclericPlayable\n");
        assertTrue(ClericGrudge.enabled());
        File history = new File(tmp.getRoot(), "ledger.txt");
        GrudgePrefs prefs = HumorTestSupport.prefs(Frequency.RARER);
        ClericGrudge g = new ClericGrudge(HumorTestSupport.shipped(), new Ledger(history), new Random(7), ClericGrudge.enabled(), 100L);
        Shown first = g.firstClericRun(prefs, false, 100L);
        assertNotNull("No surprise grace/chance applies to first-run message", first);
        assertEquals("first.0001", first.id);
        assertNotNull(g.firstClericRun(prefs, false, 101L));
        g.confirm(first, 102L);
        ClericGrudge reloaded = new ClericGrudge(HumorTestSupport.shipped(), new Ledger(history), new Random(8), ClericGrudge.enabled(), 200L);
        assertNull("An update/restart must not repeat a collected first-run entry", reloaded.firstClericRun(prefs, true, 200L));
        assertNotNull(new Ledger(history).get("first.0001"));
    }

    @Test public void privateActivationPreservesSavedOptOutWithoutConsumingEntry() {
        ApdFeatures.parse("testBuild\ngrudge\nclericPlayable\n");
        GrudgePrefs prefs = HumorTestSupport.prefs(Frequency.OFF);
        ClericGrudge g = new ClericGrudge(HumorTestSupport.shipped(), new Ledger(null), new Random(7), ClericGrudge.enabled(), 100L);
        assertNull(g.firstClericRun(prefs, false, 100L));
        prefs.setFrequency(Frequency.RARE);
        assertEquals("first.0001", g.firstClericRun(prefs, false, 101L).id);
    }
}

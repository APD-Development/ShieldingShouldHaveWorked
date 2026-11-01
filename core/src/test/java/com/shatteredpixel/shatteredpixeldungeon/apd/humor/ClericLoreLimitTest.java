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

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class ClericLoreLimitTest {
    @Test public void exactlyFiftyUniqueEntriesAndWording() throws Exception {
        List<Line> lines = HumorTestSupport.shipped();
        assertEquals(50, lines.size());
        Set<String> ids = new HashSet<>(), clean = new HashSet<>(), adult = new HashSet<>();
        int parsed = 0;
        for (String pack : HumorTestSupport.SHIPPED.names())
            parsed += LinePack.parse(HumorTestSupport.SHIPPED.read(pack), pack).size();
        assertEquals(50, parsed); // No rejected or unreachable entries hidden by the loader.
        for (Line line : lines) {
            assertTrue(line.id, ids.add(line.id));
            assertTrue(line.id, clean.add(line.clean.trim().toLowerCase(Locale.ROOT)));
            assertTrue(line.id, adult.add((line.adult == null ? line.clean : line.adult).trim().toLowerCase(Locale.ROOT)));
        }
    }

    @Test public void everyEntryCanBeSelectedAndRenderedAtAnIntendedTrigger() {
        List<Line> lines = HumorTestSupport.shipped();
        for (Line line : lines) {
            Ledger ledger = new Ledger(null);
            for (Line predecessor : lines) ledger.markShown(predecessor.id, 1L);
            Context context = Context.EMPTY.with("hero", "CLERIC").with("lastHero", "CLERIC")
                .with("lastOutcome", line.category == Category.VICTORY ? "WON" : "DIED")
                .with("runVerified", 1).with("depth", 22).with("killer", "skeleton")
                .with("lastAscended", 1).with("clericAvailable", 1).with("subclass", line.id.equals("clr.death.0015") ? "PALADIN" : "PRIEST").with("boss", "Tengu").with("armorAbility", "Trinity").with("clericRuns", 225);
            boolean selected = false;
            for (Moment moment : Moment.values()) {
                Line picked = Picker.pick(Collections.singletonList(line), moment, context, ledger,
                    new Random(7), null, 100L, 100L * 86400000L);
                if (picked != null) {
                    assertEquals(line.id, picked.id);
                    assertNotNull(line.id, picked.render(context, false));
                    assertNotNull(line.id, picked.render(context, true));
                    selected = true;
                }
            }
            assertTrue(line.id + " has no accessible trigger", selected);
        }
    }

    @Test public void retiredHistoryIsNotDeletedAndNormalReleaseStaysOff() {
        Ledger ledger = new Ledger(null);
        ledger.markShown("app.0001", 1L); // Retired by curation; history must survive.
        ClericGrudge grudge = new ClericGrudge(HumorTestSupport.shipped(), ledger, new Random(7), true, 100L);
        assertEquals(49, grudge.counter().total + grudge.counter().appreciationTotal);
        assertNotNull(ledger.get("app.0001"));
        com.shatteredpixel.shatteredpixeldungeon.apd.ApdFeatures.resetForTest();
        assertFalse(ClericGrudge.enabled());
    }
}


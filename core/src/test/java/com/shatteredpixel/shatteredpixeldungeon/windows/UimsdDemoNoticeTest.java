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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.apd.demo.UimsdUnlock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class UimsdDemoNoticeTest {
    @Rule public TemporaryFolder history = new TemporaryFolder();
    private static class Flags implements UimsdUnlock.Flags {
        boolean tour;
        public boolean unlocked(){ return true; }
        public void setUnlocked(){}
        public boolean demoUsed(){ return true; }
        public void setDemoUsed(){}
        public boolean tourUnlocked(){ return tour; }
        public void setTourUnlocked(){ tour = true; }
        public boolean promptPending(){ return false; }
        public void setPromptPending(){}
        public boolean promptShown(){ return false; }
        public void setPromptShown(){}
    }
    @Test public void demoAccessDoesNotHideTheTenRealRunNotice() {
        Flags flags = new Flags();
        for (boolean adult : new boolean[]{false, true}) {
            assertEquals(UimsdGate.LOCKED_ARROW_TEXT,
                    UimsdGate.annotationFor(flags, history.getRoot(), true, adult, ""));
        }
        assertTrue(UimsdUnlock.isUnlocked(flags, history.getRoot(), true));
        assertFalse(flags.tour);
    }
    @Test public void genuineProgressionAndNormalBuildKeepTheNoticeRetired() {
        Flags flags = new Flags();
        flags.setTourUnlocked();
        assertEquals(UimsdGate.TENTH_RUN_ARROW_CLEAN, UimsdGate.annotationFor(flags, history.getRoot(), true, false, ""));
        assertEquals("", UimsdGate.annotationFor(new Flags(), history.getRoot(), false, false, ""));
    }
}

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

package com.shatteredpixel.shatteredpixeldungeon.apd.stats;

import java.io.File;
import java.nio.file.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class RunLogCreationRaceTest {
    @Rule public TemporaryFolder tmp=new TemporaryFolder();
    @Test public void aLogAppearingAfterExistenceCheckIsNeverTruncated() throws Exception {
        File actual=new File(tmp.getRoot(),"existing.apdlog");
        byte[] evidence={9,8,7,6,5};Files.write(actual.toPath(),evidence);
        // Simulate a competing writer claiming the path after the existence check.
        File checked=new File(actual.getPath()){
            @Override public boolean exists(){return false;}
        };
        RunLogWriter writer=RunLogWriter.create(checked,TelemetryVersion.CURRENT,"existing","test",1);
        boolean disabled=writer.isDisabled();
        RunLogWriter.OpenStatus status=writer.openStatus();writer.close(false);
        assertArrayEquals("competing log evidence must survive",evidence,Files.readAllBytes(actual.toPath()));
        assertTrue("creation must atomically claim the path before opening a truncating stream",disabled);
        assertEquals(RunLogWriter.OpenStatus.FAILED_EXISTS,status);
        assertArrayEquals(evidence,Files.readAllBytes(actual.toPath()));
    }
}

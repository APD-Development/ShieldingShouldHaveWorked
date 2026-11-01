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

public class RunIndexCountValidationTest {
    @Rule public TemporaryFolder tmp = new TemporaryFolder();

    @Test public void unsignedCountOutsideSignedRangeRebuildsFromLogs() throws Exception {
        File root=tmp.newFolder();
        File runs=new File(root,"runs");assertTrue(runs.mkdir());
        byte[] original={3,1,4,1,5};
        File log=new File(runs,"preserved.apdlog");Files.write(log.toPath(),original);
        ByteSink body=new ByteSink().writeBytes(RunIndex.MAGIC).writeByte(RunIndex.FORMAT);
        // A syntactically valid unsigned 64-bit value that becomes negative as a Java long.
        for(int i=0;i<9;i++)body.writeByte(255);
        body.writeByte(1);
        body.writeInt32((int)LogFormat.crc(body.rawBuffer(),0,body.size()));
        File index=new File(root,"index.apdi");Files.write(index.toPath(),body.toByteArray());
        RunIndex loaded=RunIndex.load(index,runs);
        assertTrue("malformed count must not hide existing history behind a valid CRC",loaded.wasRebuilt());
        assertEquals(1,loaded.size());
        assertNotNull(loaded.get("preserved"));
        assertArrayEquals(original,Files.readAllBytes(log.toPath()));
    }
    @Test public void legitimateEmptyIndexRemainsValidWithoutRebuilding() throws Exception {
        File root=tmp.newFolder();File runs=new File(root,"runs");assertTrue(runs.mkdir());
        File index=new File(root,"index.apdi");new RunIndex(index,runs).save();
        RunIndex loaded=RunIndex.load(index,runs);
        assertFalse(loaded.wasRebuilt());assertEquals(0,loaded.size());
    }
}

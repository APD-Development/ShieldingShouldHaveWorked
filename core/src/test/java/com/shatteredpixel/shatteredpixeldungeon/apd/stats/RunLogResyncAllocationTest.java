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

import java.io.ByteArrayOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class RunLogResyncAllocationTest {
    @Test public void enormousSparseStringIdIsRejectedDuringRecovery() throws Exception {
        ByteArrayOutputStream log=new ByteArrayOutputStream();
        log.write(LogHeader.encode(TelemetryVersion.CURRENT,1000,"preserved","test"));
        log.write(LogFormat.stringFrame(Integer.MAX_VALUE,"known text"));
        ReadResult result=RunLogReader.parse(log.toByteArray(),new ReadOptions().resync(true));
        assertEquals(ReadStatus.CORRUPT,result.status);
        assertEquals(0,result.strings.size());
    }
}

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

public class RunLogFrameLengthValidationTest {
    @Test public void unsignedFrameLengthCannotWrapIntoAnEmptyValidFrame() throws Exception {
        byte[] header=LogHeader.encode(TelemetryVersion.CURRENT,1000,"preserved","test");
        ByteSink frame=new ByteSink().writeByte(LogFormat.SYNC).writeByte(250);
        // 2^63, outside supported nonnegative Java-long sizes; its int cast is zero.
        for(int i=0;i<9;i++)frame.writeByte(128);
        frame.writeByte(1);
        frame.writeInt32((int)LogFormat.crc(frame.rawBuffer(),1,frame.size()-1));
        ByteArrayOutputStream log=new ByteArrayOutputStream();log.write(header);log.write(frame.toByteArray());
        ReadResult result=RunLogReader.parse(log.toByteArray());
        assertEquals("oversized unsigned length must not be accepted as zero",ReadStatus.CORRUPT,result.status);
        assertEquals(header.length,result.validBytes);
        assertEquals(0,result.eventCount);
    }
    @Test public void validFutureFrameRemainsReadableAndSkipped() throws Exception {
        ByteArrayOutputStream log=new ByteArrayOutputStream();
        log.write(LogHeader.encode(TelemetryVersion.CURRENT,1000,"preserved","test"));
        log.write(LogFormat.frame(250,new byte[]{1,2,3}));
        ReadResult result=RunLogReader.parse(log.toByteArray());
        assertEquals(ReadStatus.UNSEALED,result.status);
        assertEquals(log.size(),result.validBytes);
        assertEquals(1,result.skippedUnknownFrames);
    }
}

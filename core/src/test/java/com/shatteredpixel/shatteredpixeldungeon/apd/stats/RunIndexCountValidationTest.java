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

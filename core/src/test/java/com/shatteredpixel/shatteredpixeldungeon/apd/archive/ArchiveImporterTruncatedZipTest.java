package com.shatteredpixel.shatteredpixeldungeon.apd.archive;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

// Chris, Nov 19: a history ZIP imported while its export was still writing ended early
// ("java.io.EOFException: Unexpected end of ZLIB input stream"). Nothing may be imported,
// and the message must say the file is incomplete instead of showing a Java exception.
public class ArchiveImporterTruncatedZipTest {
    @Rule public TemporaryFolder tmp = new TemporaryFolder();

    @Test public void zipCutShortImportsNothingAndSaysItIsIncomplete_fullFileStillImports() throws Exception {
        File source = tmp.newFolder("source");
        TestLogs log = new TestLogs(source, "part-one", ArchiveSupport.V, 9100)
                .start("logical-run", 0, "WARRIOR", ArchiveSupport.RS, 0, 0, false)
                .enter(1);
        for (int i = 0; i < 3000; i++) log.heroAttacks("rat", "actors.mobs.Rat", true);
        log.end(LifecycleEvents.OUTCOME_DIED, "Rat", 1, 1, 5).close(true);
        byte[] original = Files.readAllBytes(log.file.toPath());
        Map<String, byte[]> files = new TreeMap<>();
        files.put("runs/part-one.apdlog", original);
        Map<String, String> runIds = new TreeMap<>();
        runIds.put("runs/part-one.apdlog", "logical-run");
        byte[] zip = ArchiveSupport.archiveOf(files, runIds);

        File cutRoot = tmp.newFolder("cut");
        byte[] cut = Arrays.copyOf(zip, zip.length * 2 / 3);
        ArchiveImporter.Report r = ArchiveImporter.importArchive(cutRoot, new ByteArrayInputStream(cut));
        assertFalse(r.ok);
        assertEquals(0, r.imported);
        assertTrue(r.problem, r.problem.contains("ends early") && r.problem.contains("incomplete"));
        assertFalse("no raw Java exception shown", r.summary().contains("java.io"));
        assertFalse(new File(cutRoot, "runs/part-one.apdlog").exists());

        File fullRoot = tmp.newFolder("full");
        ArchiveImporter.Report ok = ArchiveImporter.importArchive(fullRoot, new ByteArrayInputStream(zip));
        assertTrue(ok.summary(), ok.ok);
        assertEquals(1, ok.imported);
    }
}

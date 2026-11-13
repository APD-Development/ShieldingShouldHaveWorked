package com.shatteredpixel.shatteredpixeldungeon.apd.archive;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunLogReader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.shatteredpixel.shatteredpixeldungeon.apd.archive.ArchiveSupport.*;
import static org.junit.Assert.*;

/**
 * A RUN_END must not unlock raw hidden archive contents unless the same
 * log also has a decodable, valid typed RUN_START.
 */
public class ArchiveMissingRunStartPrivacyTest {
    @Rule public TemporaryFolder tmp = new TemporaryFolder();

    private static Map<String, byte[]> entries(byte[] archive) throws Exception {
        Map<String, byte[]> result = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
            ZipEntry entry;
            byte[] buf = new byte[4096];
            while ((entry = zip.getNextEntry()) != null) {
                ByteArrayOutputStream content = new ByteArrayOutputStream();
                int n;
                while ((n = zip.read(buf)) != -1) content.write(buf, 0, n);
                result.put(entry.getName(), content.toByteArray());
            }
        }
        return result;
    }

    private TestLogs log(File root, String fileId) {
        return new TestLogs(root, fileId, V, 1000)
                .seeds(424242L, SECRET_SEED);
    }

    private void assertProtected(File root, TestLogs log, String fileId) throws Exception {
        byte[] original = Files.readAllBytes(log.file.toPath());
        assertTrue(contains(original, SECRET_CLASS));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ArchiveExporter.Report report = ArchiveExporter.export(root, output, "test", 123);
        assertTrue(report.summary(), report.ok);
        assertEquals(1, report.runLogs);
        assertEquals(1, report.redactedRunLogs);

        Map<String, byte[]> zip = entries(output.toByteArray());
        String path = "runs/" + fileId + ".apdlog";
        ArchiveManifest manifest = ArchiveManifest.parse(zip.get(ArchiveManifest.ENTRY_NAME));
        assertNotNull(manifest.find(path));
        assertTrue("unproven completion must remain redacted", manifest.find(path).redacted);
        byte[] copy = zip.get(path);
        assertNotNull(copy);
        assertTrue(LogRedactor.isRedacted(RunLogReader.parse(copy).header.writerBuild));
        assertFalse("hidden item class must not leave the device", contains(copy, SECRET_CLASS));
        assertFalse("custom seed must not leave the device", contains(copy, SECRET_SEED));
        assertArrayEquals("the original log must never be rewritten", original, Files.readAllBytes(log.file.toPath()));

        File restored = tmp.newFolder("restore-" + fileId);
        ArchiveImporter.Report imported = ArchiveImporter.importArchive(restored,
                new ByteArrayInputStream(output.toByteArray()));
        assertTrue(imported.summary(), imported.ok);
        assertEquals(1, imported.imported);
    }

    @Test public void sealedEndWithoutTypedRunStartCannotExposeHiddenFacts() throws Exception {
        File root = tmp.newFolder("missing-start");
        TestLogs log = log(root, "orphan-end")
                .enter(1)
                .registered("hidden-ring", SECRET_CLASS, 3, true, 0)
                .end(LifecycleEvents.OUTCOME_DIED, "Rat", 1, 1, 100);
        log.close(true);
        assertProtected(root, log, "orphan-end");
    }

    @Test public void malformedRunStartCannotProveCompletion() throws Exception {
        File root = tmp.newFolder("invalid-start");
        TestLogs log = log(root, "bad-start")
                .start("invalid/typed-id", 0, "MAGE", RS, 0, 0, false)
                .enter(1)
                .registered("hidden-ring", SECRET_CLASS, 3, true, 0)
                .end(LifecycleEvents.OUTCOME_WON, "", 26, 26, 100);
        log.close(true);
        assertTrue("original log carries the sensitive custom seed", contains(Files.readAllBytes(log.file.toPath()), SECRET_SEED));
        assertProtected(root, log, "bad-start");
    }

    @Test public void validTypedRunStartRetainsLosslessCompletedExport() throws Exception {
        File root = tmp.newFolder("valid-start");
        TestLogs log = log(root, "normal-end")
                .start("logical-normal-end", 0, "MAGE", RS, 0, 0, false)
                .enter(1)
                .registered("hidden-ring", SECRET_CLASS, 3, true, 0)
                .end(LifecycleEvents.OUTCOME_WON, "", 26, 26, 100);
        log.close(true);
        byte[] original = Files.readAllBytes(log.file.toPath());

        Map<String, byte[]> zip = entries(export(root));
        String path = "runs/normal-end.apdlog";
        ArchiveManifest manifest = ArchiveManifest.parse(zip.get(ArchiveManifest.ENTRY_NAME));
        assertNotNull(manifest.find(path));
        assertFalse(manifest.find(path).redacted);
        assertArrayEquals("valid completed run remains byte-for-byte identical", original, zip.get(path));
        assertArrayEquals(original, Files.readAllBytes(log.file.toPath()));
    }
}

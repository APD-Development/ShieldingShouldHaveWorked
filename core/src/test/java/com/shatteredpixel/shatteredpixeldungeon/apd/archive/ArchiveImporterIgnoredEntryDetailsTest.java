package com.shatteredpixel.shatteredpixeldungeon.apd.archive;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

// Unlisted ZIP members are ignored for import, but they must not make the
// in-memory user-facing diagnostic list grow without bound.
public class ArchiveImporterIgnoredEntryDetailsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static byte[] archiveWithExtras(byte[] log, int extraCount) throws Exception {
        ArchiveManifest manifest = new ArchiveManifest();
        manifest.container = 1;
        ArchiveManifest.Entry listed = new ArchiveManifest.Entry();
        listed.path = "runs/a.apdlog";
        listed.kind = ArchiveManifest.KIND_RUN;
        listed.runId = "run-a";
        listed.size = log.length;
        listed.sha256 = ArchiveManifest.sha256Hex(log);
        manifest.files.add(listed);

        List<String> names = new ArrayList<>();
        List<byte[]> contents = new ArrayList<>();
        names.add(ArchiveManifest.ENTRY_NAME);
        contents.add(manifest.encode().getBytes(ArchiveManifest.utf8()));
        for (int i = 0; i < extraCount; i++) {
            names.add("unlisted/extra-" + i + ".txt");
            contents.add(new byte[]{(byte) i});
        }
        names.add(listed.path);
        contents.add(log);
        return ArchiveSupport.zip(names, contents);
    }

    @Test
    public void smallArchiveStillReportsEveryIgnoredExtraByName() throws Exception {
        byte[] log = ArchiveSupport.smallLog(temporaryFolder.newFolder("scratch-small"), "a", "run-a");
        File history = temporaryFolder.newFolder("history-small");
        ArchiveImporter.Report result = ArchiveImporter.importArchive(history,
                new ByteArrayInputStream(archiveWithExtras(log, 2)));

        assertTrue(result.summary(), result.ok);
        assertEquals(1, result.imported);
        assertEquals(2, result.details.size());
        assertEquals("not in the manifest or repeated, ignored: unlisted/extra-0.txt",
                result.details.get(0));
        assertEquals("not in the manifest or repeated, ignored: unlisted/extra-1.txt",
                result.details.get(1));
        assertArrayEquals(log, Files.readAllBytes(new File(history, "runs/a.apdlog").toPath()));
    }

    @Test
    public void manyExtraEntriesKeepValidImportAndBoundDiagnosticMemory() throws Exception {
        byte[] log = ArchiveSupport.smallLog(temporaryFolder.newFolder("scratch-many"), "a", "run-a");
        File history = temporaryFolder.newFolder("history-many");
        ArchiveImporter.Report result = ArchiveImporter.importArchive(history,
                new ByteArrayInputStream(archiveWithExtras(log, 250)));

        assertTrue(result.summary(), result.ok);
        assertEquals(1, result.imported);
        assertEquals(101, result.details.size());
        assertEquals("not in the manifest or repeated, ignored: unlisted/extra-0.txt",
                result.details.get(0));
        assertEquals("not in the manifest or repeated, ignored: unlisted/extra-99.txt",
                result.details.get(99));
        assertEquals("Additional unlisted or repeated archive entries were ignored (details omitted).",
                result.details.get(100));
        assertFalse(result.summary().contains("unlisted/extra-249.txt"));
        assertArrayEquals(log, Files.readAllBytes(new File(history, "runs/a.apdlog").toPath()));
    }
}

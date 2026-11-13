package com.shatteredpixel.shatteredpixeldungeon.apd.archive;

import com.shatteredpixel.shatteredpixeldungeon.apd.query.DatasetStore;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunLogWriter;
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

import static org.junit.Assert.*;

// The exporter must apply the same synthetic-demo provenance boundary as the importer,
// including copied logs whose filenames have been changed to ordinary run IDs.
public class ArchiveDemoProvenanceExportTest {

    @Rule public TemporaryFolder tmp = new TemporaryFolder();

    private static Map<String, byte[]> entries(byte[] archive) throws Exception {
        Map<String, byte[]> result = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
            ZipEntry entry;
            byte[] buf = new byte[4096];
            while ((entry = zip.getNextEntry()) != null) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                int n;
                while ((n = zip.read(buf)) != -1) bytes.write(buf, 0, n);
                result.put(entry.getName(), bytes.toByteArray());
            }
        }
        return result;
    }

    private static byte[] exported(File root, ArchiveExporter.Report[] result) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        result[0] = ArchiveExporter.export(root, out, "test", 123);
        assertTrue(result[0].summary(), result[0].ok);
        return out.toByteArray();
    }

    // Produce a real, clean sealed APD log with a synthetic writer-build marker,
    // under a normal-looking path and run ID.
    private static File syntheticWriter(File root, String folder, String name) throws Exception {
        File dir = new File(root, folder);
        assertTrue(dir.isDirectory() || dir.mkdirs());
        File log = new File(dir, name + ".apdlog");
        RunLogWriter w = RunLogWriter.create(log, ArchiveSupport.V, name,
                "TEST-SYNTHETIC-DEMO-FIXTURE", 1000);
        LifecycleEvents.RunStart start = new LifecycleEvents.RunStart();
        start.runId = "ordinary-" + name;
        start.segment = 0;
        start.heroClass = "WARRIOR";
        start.rulesetId = ArchiveSupport.RS;
        start.seed = 777;
        assertTrue(LifecycleEvents.emitRunStart(w, 1, false, start));
        LifecycleEvents.RunEnd end = new LifecycleEvents.RunEnd();
        end.outcome = LifecycleEvents.OUTCOME_DIED;
        end.cause = "Rat";
        end.depth = 1;
        end.deepest = 1;
        end.heroLevel = 1;
        end.score = 5;
        end.turns = 2;
        assertTrue(LifecycleEvents.emitRunEnd(w, 2, end));
        w.close(true);
        return log;
    }

    @Test public void logicalDemoRunIdUnderPlainFilenameCannotExport() throws Exception {
        File root = tmp.newFolder("logical-demo");
        TestLogs disguised = new TestLogs(root, "plain-file", ArchiveSupport.V, 1000)
                .seeds(777, ArchiveSupport.SECRET_SEED)
                .start(DatasetStore.DEMO_LOG_PREFIX + "hidden-run", 0, "WARRIOR",
                        ArchiveSupport.RS, 0, 0, false)
                .enter(1);
        disguised.registered("secret", ArchiveSupport.SECRET_CLASS, 2, false, 0)
                .end(LifecycleEvents.OUTCOME_DIED, "Rat", 1, 1, 5).close(true);
        byte[] before = Files.readAllBytes(disguised.file.toPath());
        byte[] normal = ArchiveSupport.smallLog(root, "normal-run", "normal-logical-run");

        ArchiveExporter.Report[] status = new ArchiveExporter.Report[1];
        byte[] zip = exported(root, status);
        assertEquals(1, status[0].skippedDemo);
        assertEquals(1, status[0].runLogs);
        Map<String, byte[]> archived = entries(zip);
        ArchiveManifest manifest = ArchiveManifest.parse(archived.get(ArchiveManifest.ENTRY_NAME));
        assertNull(manifest.find("runs/plain-file.apdlog"));
        assertFalse(archived.containsKey("runs/plain-file.apdlog"));
        assertFalse(ArchiveSupport.contains(zip, ArchiveSupport.SECRET_CLASS));
        assertFalse(ArchiveSupport.contains(zip, ArchiveSupport.SECRET_SEED));
        assertArrayEquals(normal, archived.get("runs/normal-run.apdlog"));

        File destination = tmp.newFolder("logical-destination");
        ArchiveImporter.Report imported = ArchiveImporter.importArchive(destination, new ByteArrayInputStream(zip));
        assertTrue(imported.summary(), imported.ok);
        assertEquals(1, imported.imported);
        assertArrayEquals(normal, Files.readAllBytes(new File(destination, "runs/normal-run.apdlog").toPath()));
        assertArrayEquals("original synthetic log must stay intact", before, Files.readAllBytes(disguised.file.toPath()));
    }

    @Test public void syntheticWriterMarkerCannotEscapeThroughRunOrLegacyNames() throws Exception {
        File root = tmp.newFolder("writer-demo");
        File run = syntheticWriter(root, "runs", "innocent-run");
        File legacy = syntheticWriter(root, "legacy", "innocent-legacy");
        byte[] runBefore = Files.readAllBytes(run.toPath());
        byte[] legacyBefore = Files.readAllBytes(legacy.toPath());
        byte[] normal = ArchiveSupport.smallLog(root, "valid-log", "real-run");

        ArchiveExporter.Report[] status = new ArchiveExporter.Report[1];
        byte[] zip = exported(root, status);
        assertEquals(2, status[0].skippedDemo);
        assertEquals(1, status[0].runLogs);
        assertEquals(0, status[0].importedFiles);
        Map<String, byte[]> archived = entries(zip);
        ArchiveManifest manifest = ArchiveManifest.parse(archived.get(ArchiveManifest.ENTRY_NAME));
        assertEquals(1, manifest.files.size());
        assertNull(manifest.find("runs/innocent-run.apdlog"));
        assertNull(manifest.find("legacy/innocent-legacy.apdlog"));
        assertArrayEquals(normal, archived.get("runs/valid-log.apdlog"));

        File destination = tmp.newFolder("writer-destination");
        ArchiveImporter.Report imported = ArchiveImporter.importArchive(destination, new ByteArrayInputStream(zip));
        assertTrue(imported.summary(), imported.ok);
        assertEquals(1, imported.imported);
        assertArrayEquals(runBefore, Files.readAllBytes(run.toPath()));
        assertArrayEquals(legacyBefore, Files.readAllBytes(legacy.toPath()));
    }

    @Test public void normalDemoFilenameStillSkippedWithoutAffectingOtherLogs() throws Exception {
        File root = tmp.newFolder("filename-demo");
        TestLogs demo = new TestLogs(root, DatasetStore.DEMO_LOG_PREFIX + "fixture",
                ArchiveSupport.V, 1000)
                .start("ordinary-logical-id", 0, "WARRIOR", ArchiveSupport.RS, 0, 0, false);
        demo.end(LifecycleEvents.OUTCOME_DIED, "Rat", 1, 1, 5).close(true);
        byte[] demoBefore = Files.readAllBytes(demo.file.toPath());
        byte[] real = ArchiveSupport.smallLog(root, "real", "non-demo-logical-id");
        ArchiveExporter.Report[] status = new ArchiveExporter.Report[1];
        byte[] zip = exported(root, status);
        assertEquals(1, status[0].skippedDemo);
        assertEquals(1, status[0].runLogs);
        Map<String, byte[]> archived = entries(zip);
        assertFalse(archived.containsKey("runs/demo-fixture.apdlog"));
        assertArrayEquals(real, archived.get("runs/real.apdlog"));
        assertArrayEquals(demoBefore, Files.readAllBytes(demo.file.toPath()));
    }
}

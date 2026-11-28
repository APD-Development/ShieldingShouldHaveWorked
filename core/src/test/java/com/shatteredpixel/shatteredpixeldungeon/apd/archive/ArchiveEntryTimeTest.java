package com.shatteredpixel.shatteredpixeldungeon.apd.archive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

// Tester report (Nov 27): exported history files showed as December 1969 in a phone file manager,
// because every entry was stamped with time 0. Entries now carry the export time.
public class ArchiveEntryTimeTest {

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Test
	public void entriesCarryTheExportTimeNotNineteenSixtyNine() throws Exception {
		File history = temp.newFolder("history");
		ArchiveSupport.history(history);
		long now = 1795896000000L; //2026-11-28 UTC
		ByteArrayOutputStream written = new ByteArrayOutputStream();
		ArchiveExporter.Report report = ArchiveExporter.export(history, written, "test", now);
		assertTrue(report.summary(), report.ok);

		int entries = 0;
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(written.toByteArray()))){
			for (ZipEntry e; (e = zip.getNextEntry()) != null; ){
				entries++;
				//DOS time keeps 2-second steps in local time; within a day is enough to rule out 1969
				assertTrue(e.getName() + " time " + e.getTime(), Math.abs(e.getTime() - now) < 86400000L);
			}
		}
		assertTrue("archive has entries", entries > 0);
	}

	@Test
	public void clockBeforeNineteenEightyIsClampedToZipEpoch(){
		assertEquals(ArchiveExporter.ZIP_EPOCH_MILLIS, ArchiveExporter.entryTime(0));
		assertEquals(ArchiveExporter.ZIP_EPOCH_MILLIS, ArchiveExporter.entryTime(-5));
		assertEquals(1795896000000L, ArchiveExporter.entryTime(1795896000000L));
	}
}

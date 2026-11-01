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

package com.shatteredpixel.shatteredpixeldungeon.apd.archive;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipInputStream;

//The archive APIs own their compression wrappers, not the streams supplied by Android's document provider.
public class ArchiveStreamOwnershipTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final class TrackingOutputStream extends ByteArrayOutputStream {
		boolean closed;
		@Override public void close() throws IOException { closed = true; super.close(); }
	}

	private static final class TrackingInputStream extends ByteArrayInputStream {
		boolean closed;
		TrackingInputStream( byte[] bytes ){ super(bytes); }
		@Override public void close() throws IOException { closed = true; super.close(); }
	}

	private static final class TrackingZipInputStream extends ZipInputStream {
		boolean closed;
		TrackingZipInputStream( byte[] bytes ){ super(new ByteArrayInputStream(bytes)); }
		@Override public void close() throws IOException { closed = true; super.close(); }
	}

	@Test
	public void exporterReleasesItsZipWithoutClosingTheCallerStream() throws Exception {
		File source = tmp.newFolder("export-source");
		ArchiveSupport.history(source);
		TrackingOutputStream out = new TrackingOutputStream();
		ArchiveExporter.Report report = ArchiveExporter.export(source, out, "test", 1);
		assertTrue(report.summary(), report.ok);
		assertFalse("the caller retains destination-stream ownership", out.closed);
		out.write(7);
	}

	@Test
	public void importerReleasesItsZipWithoutClosingTheCallerStream() throws Exception {
		File source = tmp.newFolder("import-source");
		ArchiveSupport.history(source);
		byte[] archive = ArchiveSupport.export(source);
		TrackingInputStream in = new TrackingInputStream(archive);
		ArchiveImporter.Report publicReport = ArchiveImporter.importArchive(tmp.newFolder("public-target"), in);
		assertTrue(publicReport.summary(), publicReport.ok);
		assertFalse("the caller retains source-stream ownership", in.closed);
		TrackingZipInputStream zip = new TrackingZipInputStream(archive);
		ArchiveImporter.Report directReport = ArchiveImporter.importZip(tmp.newFolder("direct-target"), zip);
		assertTrue(directReport.summary(), directReport.ok);
		assertTrue("an importer-owned ZIP wrapper is always released", zip.closed);
	}
}

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.RandomAccessFile;

public class StorageMeterTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static void size( File f, long bytes ) throws Exception {
		f.getParentFile().mkdirs();
		RandomAccessFile r = new RandomAccessFile(f, "rw");
		r.setLength(bytes);
		r.close();
	}

	@Test
	public void itCountsEvidenceAndCachesSeparatelyAndNeverTouchesThem() throws Exception {
		File root = tmp.newFolder("apd");
		size(new File(root, "runs/a.apdlog"), 1000);
		size(new File(root, "runs/b.apdlog"), 2000);
		size(new File(root, "runs/b.apdlog.tail-5.damaged"), 30);
		size(new File(root, "legacy/import-1.apdlog"), 400);
		size(new File(root, "derived/rows/a.apdr"), 111);
		size(new File(root, "derived/facts.apdd"), 222);
		size(new File(root, "index.apdi"), 5);
		StorageMeter.Report r = StorageMeter.measure(root);
		assertEquals(2, r.runLogs);
		assertEquals(3000, r.runLogBytes);
		assertEquals(1, r.importedFiles);
		assertEquals(400, r.importedBytes);
		assertEquals(30, r.damagedTailBytes);
		assertEquals(3430, r.historyBytes());
		assertEquals(338, r.derivedBytes);
		assertEquals(3768, r.totalBytes());
		assertTrue(new File(root, "runs/a.apdlog").isFile());
		assertTrue(r.summary().contains("2 runs"));
	}

	@Test
	public void aNoRootOrAnEmptyRootIsFine(){
		assertEquals(0, StorageMeter.measure(null).runLogs);
		assertEquals(0, StorageMeter.measure(new File(tmp.getRoot(), "none")).runLogs);
	}

	@Test
	public void itWarnsAsTheHistoryGrowsButNeverLimitsAnything() throws Exception {
		File small = tmp.newFolder("small");
		size(new File(small, "runs/a.apdlog"), 1000);
		assertFalse(containsLarge(StorageMeter.measure(small)));
		File large = tmp.newFolder("large");
		size(new File(large, "runs/a.apdlog"), StorageMeter.LARGE_BYTES + 1);
		StorageMeter.Report r = StorageMeter.measure(large);
		assertTrue(r.warnings.toString(), containsLarge(r));
		File huge = tmp.newFolder("huge");
		size(new File(huge, "runs/a.apdlog"), StorageMeter.VERY_LARGE_BYTES + 1);
		assertTrue(StorageMeter.measure(huge).warnings.toString().contains("very large"));
	}

	private static boolean containsLarge( StorageMeter.Report r ){
		for (String w : r.warnings) if (w.contains("large")) return true;
		return false;
	}

	@Test
	public void sizesAreInWords(){
		assertEquals("512 B", Size.text(512));
		assertEquals("1.5 KB", Size.text(1536));
		assertEquals("2.0 MB", Size.text(2L * 1024 * 1024));
		assertEquals("3.0 GB", Size.text(3L * 1024 * 1024 * 1024));
	}
}

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SelfTestTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void theDeviceSelfTestPassesOnThisMachineAndCleansUpAfterItself() throws Exception {
		SelfTest.Report r = SelfTest.run(tmp.getRoot());
		System.out.println("APD-SELFTEST\n" + r.text());
		assertTrue(r.text(), r.passed);
		assertTrue(r.lines.size() > 10);
		assertEquals("the self-test must remove its own files", 0, tmp.getRoot().list().length);
	}

	@Test
	public void aFolderThatCannotBeWrittenGivesAFailedReportNotAnException() throws Exception {
		java.io.File file = tmp.newFile("a-file");
		SelfTest.Report r = SelfTest.run(new java.io.File(file, "inside"));
		assertTrue(!r.passed);
	}
}

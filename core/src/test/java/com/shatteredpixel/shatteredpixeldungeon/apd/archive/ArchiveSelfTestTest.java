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

import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.SelfTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

//The self-test that runs on the phone also passes here.
public class ArchiveSelfTestTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void theDeviceSelfTestPasses() throws Exception {
		SelfTest.Report r = new SelfTest.Report();
		ArchiveSelfTest.run(tmp.newFolder("work"), r);
		assertTrue(r.text(), r.passed);
		assertTrue(r.text().contains("history archive"));
		assertTrue("it cleans up after itself", tmp.getRoot().listFiles()[0].listFiles().length == 0);
	}
}

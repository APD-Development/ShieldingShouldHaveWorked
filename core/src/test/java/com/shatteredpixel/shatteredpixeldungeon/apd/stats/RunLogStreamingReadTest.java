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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

public class RunLogStreamingReadTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void ordinaryFileReadMatchesTheByteArrayParser() throws Exception {
		File file = new File(tmp.getRoot(), "parity.apdlog");
		RunLogWriter writer = RunLogWriter.create(file, TelemetryVersion.CURRENT,
				"parity", "test", 1234);
		writer.event(LogTestSupport.T_PLAIN).clock(7).flags(3).string("alpha").signed(-9).emit();
		writer.event(LogTestSupport.T_PLAIN).clock(11).bytes(new byte[]{1, 2, 3}).emit();
		writer.close();

		ReadResult streamed = RunLogReader.read(file);
		ReadResult buffered = RunLogReader.parse(LogTestSupport.bytes(file));
		assertEquivalent(buffered, streamed);
	}

	@Test
	public void corruptSparseTailDoesNotRequireWholeFileMemory() throws Exception {
		File file = new File(tmp.getRoot(), "sparse.apdlog");
		byte[] header = LogHeader.encode(TelemetryVersion.CURRENT, 1000, "sparse", "test");
		FileOutputStream out = new FileOutputStream(file);
		out.write(header);
		out.write(0); //not a frame marker
		out.close();

		long sparseLength = 1L << 30;
		RandomAccessFile sparse = new RandomAccessFile(file, "rw");
		sparse.setLength(sparseLength);
		sparse.close();

		ReadResult result = RunLogReader.read(file);
		assertEquals(ReadStatus.CORRUPT, result.status);
		assertEquals(sparseLength, result.fileLength);
		assertEquals(header.length, result.validBytes);
		assertEquals(sparseLength - header.length, result.damagedBytes());
		assertEquals(0, result.events.size());
	}

	private static void assertEquivalent( ReadResult expected, ReadResult actual ){
		assertEquals(expected.status, actual.status);
		assertEquals(expected.problem, actual.problem);
		assertEquals(expected.fileLength, actual.fileLength);
		assertEquals(expected.validBytes, actual.validBytes);
		assertEquals(expected.sealed, actual.sealed);
		assertEquals(expected.eventCount, actual.eventCount);
		assertEquals(expected.lastClock, actual.lastClock);
		assertEquals(expected.strings, actual.strings);
		assertEquals(expected.events.size(), actual.events.size());
		for (int i = 0; i < expected.events.size(); i++){
			ApdEvent left = expected.events.get(i);
			ApdEvent right = actual.events.get(i);
			assertEquals(left.index, right.index);
			assertEquals(left.type, right.type);
			assertEquals(left.flags, right.flags);
			assertEquals(left.clock, right.clock);
			assertArrayEquals(left.payload, right.payload);
		}
		if (expected.header == null){
			assertNull(actual.header);
		} else {
			assertEquals(expected.header.containerFormat, actual.header.containerFormat);
			assertEquals(expected.header.telemetryVersion, actual.header.telemetryVersion);
			assertEquals(expected.header.createdAtMillis, actual.header.createdAtMillis);
			assertEquals(expected.header.runId, actual.header.runId);
			assertEquals(expected.header.writerBuild, actual.header.writerBuild);
			assertEquals(expected.header.length, actual.header.length);
		}
	}
}

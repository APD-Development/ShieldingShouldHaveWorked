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

	@Test
	public void identityOnlyReadValidatesLargeGameplayEventsWithoutCopyingTheirTails() throws Exception {
		File file = new File(tmp.getRoot(), "large-event-tails.apdlog");
		RunLogWriter writer = RunLogWriter.create(file, TelemetryVersion.CURRENT,
				"large-event-tails", "test", 1234);
		byte[] largePayload = new byte[32 * 1024];
		for (int i = 0; i < largePayload.length; i++) largePayload[i] = (byte) (i * 31);
		for (int i = 0; i < 32; i++){
			writer.event(LogTestSupport.T_PLAIN).clock(i + 1).bytes(largePayload).emit();
		}
		writer.close();

		ReadResult ordinary = RunLogReader.read(file);
		ReadResult identityStream = RunLogReader.read(file, new ReadOptions().onlyRunStartEvents(true));
		ReadResult identityBytes = RunLogReader.parse(LogTestSupport.bytes(file),
				new ReadOptions().onlyRunStartEvents(true));
		assertEquals(ReadStatus.SEALED, ordinary.status);
		assertEquals(32, ordinary.events.size());
		assertEquals(0, identityStream.events.size());
		assertEquals(0, identityBytes.events.size());
		assertEquals(32, identityStream.eventCount);
		assertEquals(ordinary.status, identityStream.status);
		assertEquals(ordinary.status, identityBytes.status);
		assertEquals(ordinary.eventCount, identityStream.eventCount);
		assertEquals(ordinary.eventCount, identityBytes.eventCount);
		assertEquals(ordinary.sealEventCount, identityStream.sealEventCount);
		assertEquals(ordinary.sealEventCount, identityBytes.sealEventCount);
		assertEquals(ordinary.lastClock, identityStream.lastClock);
		assertEquals(ordinary.lastClock, identityBytes.lastClock);
		assertEquals(ordinary.validBytes, identityStream.validBytes);
		assertEquals(ordinary.validBytes, identityBytes.validBytes);
		assertEquals(ordinary.strings, identityStream.strings);
		assertEquals(ordinary.strings, identityBytes.strings);
		assertArrayEquals(ordinary.events.get(0).payload,
				RunLogReader.parse(LogTestSupport.bytes(file)).events.get(0).payload);
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

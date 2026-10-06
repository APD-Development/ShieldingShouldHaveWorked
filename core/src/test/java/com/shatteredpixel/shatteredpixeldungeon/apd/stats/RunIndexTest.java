package com.shatteredpixel.shatteredpixeldungeon.apd.stats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class RunIndexTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	//one store with: a sealed run, an open run, a damaged run, a run from a newer telemetry version, an unreadable file
	private StatsStore populated() throws Exception {
		StatsStore store = new StatsStore(tmp.newFolder("store"));

		RunLogWriter a = store.startRun("a-sealed", "b", 100);
		for (int i = 0; i < 5; i++) a.event(LogTestSupport.T_PLAIN).clock(i).unsigned(i).emit();
		a.close();
		store.refreshIndex("a-sealed");

		RunLogWriter b = store.startRun("b-open", "b", 200);
		for (int i = 0; i < 7; i++) b.event(LogTestSupport.T_PLAIN).clock(i).unsigned(i).emit();
		b.close(false);
		store.refreshIndex("b-open");

		RunLogWriter c = store.startRun("c-damaged", "b", 300);
		for (int i = 0; i < 9; i++) c.event(LogTestSupport.T_PLAIN).clock(i).unsigned(i).emit();
		c.close(false);
		File cf = store.logFile("c-damaged");
		byte[] cb = LogTestSupport.bytes(cf);
		LogTestSupport.write(cf, Arrays.copyOf(cb, cb.length - 2));
		store.refreshIndex("c-damaged");

		RunLogWriter d = store.startRun("d-future", new TelemetryVersion(9, 2), "b", 400, new RunLogWriter.Options());
		d.event(LogTestSupport.T_PLAIN).unsigned(1).emit();
		d.close();
		store.refreshIndex("d-future");

		LogTestSupport.write(new File(store.runsDir(), "e-junk.apdlog"), new byte[]{1, 2, 3});
		return store;
	}

	@Test
	public void entriesDescribeEachRun() throws Exception {
		StatsStore store = populated();
		store.index().rebuild(); //pick up the junk file
		RunIndex idx = store.index();

		RunIndexEntry a = idx.get("a-sealed");
		assertEquals(RunIndexEntry.Status.SEALED, a.status);
		assertEquals(5, a.eventCount);
		assertEquals(4, a.lastClock);
		assertEquals(100, a.createdAtMillis);
		assertEquals(TelemetryVersion.CURRENT, a.telemetryVersion);
		assertFalse(a.newerThanReader);
		assertEquals("a-sealed.apdlog", a.logFile);

		assertEquals(RunIndexEntry.Status.OPEN, idx.get("b-open").status);
		assertEquals(7, idx.get("b-open").eventCount);

		RunIndexEntry c = idx.get("c-damaged");
		assertEquals(RunIndexEntry.Status.DAMAGED, c.status);
		assertEquals(8, c.eventCount);
		assertTrue(c.validBytes < c.fileLength);

		RunIndexEntry d = idx.get("d-future");
		assertEquals(new TelemetryVersion(9, 2), d.telemetryVersion);
		assertTrue(d.newerThanReader);

		RunIndexEntry e = idx.get("e-junk");
		assertEquals(RunIndexEntry.Status.UNREADABLE, e.status);
		assertNull(e.telemetryVersion);
		assertEquals(5, idx.size());
	}

	@Test
	public void listIsOrderedByCreationTime() throws Exception {
		StatsStore store = populated();
		List<RunIndexEntry> all = store.index().all();
		assertEquals("a-sealed", all.get(0).runId);
		assertEquals("b-open", all.get(1).runId);
		assertEquals("c-damaged", all.get(2).runId);
		assertEquals("d-future", all.get(3).runId);
	}

	@Test
	public void saveAndLoadRoundTrip() throws Exception {
		StatsStore store = populated();
		store.index().save();
		RunIndex loaded = RunIndex.load(new File(store.root(), "index.apdi"), store.runsDir());
		assertFalse(loaded.wasRebuilt());
		assertEquals(store.index().all(), loaded.all());
	}

	@Test
	public void missingIndexIsRebuiltFromTheLogsAndMatches() throws Exception {
		StatsStore store = populated();
		store.index().rebuild();
		store.index().save();
		List<RunIndexEntry> expected = store.index().all();
		File indexFile = new File(store.root(), "index.apdi");
		assertTrue(indexFile.delete());

		RunIndex rebuilt = RunIndex.load(indexFile, store.runsDir());
		assertTrue(rebuilt.wasRebuilt());
		assertEquals(expected, rebuilt.all());
		assertTrue("the rebuilt index is saved again", indexFile.isFile());
	}

	@Test
	public void damagedIndexFilesAreRebuiltNotTrusted() throws Exception {
		StatsStore store = populated();
		store.index().rebuild();
		store.index().save();
		File indexFile = new File(store.root(), "index.apdi");
		byte[] good = LogTestSupport.bytes(indexFile);
		List<RunIndexEntry> expected = store.index().all();

		byte[][] variants = new byte[][]{
				Arrays.copyOf(good, good.length / 2),                          //truncated
				Arrays.copyOf(good, 3),                                        //almost nothing
				new byte[0],                                                   //empty
				flip(good, good.length / 2),                                   //bit rot
				flip(good, 0),                                                 //bad magic
				flip(good, good.length - 1),                                   //bad checksum
				LogTestSupport.concat(good, new byte[]{1}),                    //trailing byte
		};
		for (int i = 0; i < variants.length; i++){
			LogTestSupport.write(indexFile, variants[i]);
			RunIndex idx = RunIndex.load(indexFile, store.runsDir());
			assertTrue("variant " + i, idx.wasRebuilt());
			assertEquals("variant " + i, expected, idx.all());
		}
	}

	private static byte[] flip( byte[] data, int at ){
		byte[] c = data.clone();
		c[at] ^= 0x40;
		return c;
	}

	@Test
	public void leftoverTempFileFromAnInterruptedSaveIsHarmless() throws Exception {
		StatsStore store = populated();
		store.index().save();
		LogTestSupport.write(new File(store.root(), "index.apdi.tmp"), new byte[]{9, 9, 9});
		RunIndex loaded = RunIndex.load(new File(store.root(), "index.apdi"), store.runsDir());
		assertFalse(loaded.wasRebuilt());
		assertEquals(4, loaded.size());
		loaded.save(); //and saving over it works
		assertFalse(new File(store.root(), "index.apdi.tmp").exists());
	}

	@Test
	public void refreshTracksAChangingRun() throws Exception {
		StatsStore store = new StatsStore(tmp.newFolder("s2"));
		RunLogWriter w = store.startRun("live", "b", 1);
		assertEquals(0, store.index().get("live").eventCount);
		w.event(LogTestSupport.T_PLAIN).clock(3).unsigned(1).emit();
		w.flush();
		store.refreshIndex("live");
		assertEquals(1, store.index().get("live").eventCount);
		assertEquals(RunIndexEntry.Status.OPEN, store.index().get("live").status);
		w.close();
		store.refreshIndex("live");
		assertEquals(RunIndexEntry.Status.SEALED, store.index().get("live").status);
		assertTrue(store.logFile("live").delete());
		assertNull(store.index().refresh("live"));
		assertNull(store.index().get("live"));
	}

	@Test
	public void entryDecoderIgnoresFieldsAddedByLaterVersions() throws Exception {
		RunIndexEntry e = new RunIndexEntry("x", new TelemetryVersion(1, 0), false, "x.apdlog", 5,
				RunIndexEntry.Status.OPEN, 3, 4, 5, 6);
		byte[] enc = e.encode();
		byte[] withExtra = LogTestSupport.concat(enc, new byte[]{42, 43, 44});
		assertEquals(e, RunIndexEntry.decode(withExtra));
	}

	@Test
	public void recoverAllRepairsDamagedLogsAndNeverDeletesThem() throws Exception {
		StatsStore store = populated();
		List<LogRecovery.RepairReport> reports = store.recoverAll();
		assertEquals(5, reports.size());
		int truncated = 0;
		for (LogRecovery.RepairReport r : reports){
			assertNull(r.error);
			if (r.truncated) truncated++;
		}
		assertEquals(1, truncated);
		assertEquals(RunIndexEntry.Status.OPEN, store.index().get("c-damaged").status);
		assertEquals(8, store.index().get("c-damaged").eventCount);
		assertEquals(5, store.index().size());
		assertTrue(store.logFile("c-damaged").isFile());
		assertNotNull(store.index().get("e-junk"));
	}
}

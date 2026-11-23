package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

//The memory wall (UIMSD deep dive, Nov 23): opening UIMSD on a long history built every row of every dataset, 340 bytes a row,
//so 20 long runs took 247 MB of the phone's 256 MB heap. The gate: 50 long runs (about 1.9 million recorded rows) must open,
//Home included, inside 64 MB, and the biggest single dataset (every step of all 50 runs) must stay well inside the phone's heap.
public class UimsdMemoryGateTest {

	static final long MB = 1024L * 1024L;
	static final int RUNS = 50;

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static long used(){
		Runtime rt = Runtime.getRuntime();
		long best = Long.MAX_VALUE;
		for (int i = 0; i < 4; i++){
			System.gc();
			try { Thread.sleep(40); } catch (InterruptedException ignored) { }
			best = Math.min(best, rt.totalMemory() - rt.freeMemory());
		}
		return best;
	}

	@Test
	public void fiftyLongRunsOpenInsideSixtyFourMegabytes() throws Exception {
		File root = tmp.getRoot();
		LongHistory.write(root, RUNS);
		//the first open builds the row caches; the gate is the ordinary open, from the caches
		new DatasetStore(root).load();

		long before = used();
		Catalog cat = new DatasetStore(root).load();
		UimsdHome home = UimsdHome.build(cat, "rules-a", false);
		long opened = used() - before;
		assertEquals(RUNS, home.runs);

		long logBytes = 0;
		File[] logs = new File(root, "runs").listFiles();
		if (logs != null) for (File f : logs) logBytes += f.length();
		long rows = 0;
		for (Dataset d : cat.datasets()) rows += d.rowCount();

		Dataset steps = cat.dataset(FactDatasets.PREFIX + "move.step");
		long t0 = System.nanoTime();
		int stepRows = steps.rows.size();
		long readMs = (System.nanoTime() - t0) / 1000000L;
		long withSteps = used() - before;
		ColumnarRows compact = (ColumnarRows) ((LazyRows) steps.rows).data();

		System.out.println("APD-MEMORY " + RUNS + " long runs (" + (logBytes / 1024) + " KiB of logs), " + rows + " rows: open + Home " + (opened / 1024) + " KiB; after reading all "
				+ stepRows + " step rows " + (withSteps / 1024) + " KiB (store about " + (compact.approximateBytes() / 1024)
				+ " KiB, " + (compact.approximateBytes() / Math.max(1, stepRows)) + " bytes a row), read in " + readMs + " ms");
		assertEquals(RUNS * LongHistory.STEPS, stepRows);
		assertTrue("opening UIMSD on " + RUNS + " long runs took " + (opened / MB) + " MB", opened < 64 * MB);
		assertTrue("every step of " + RUNS + " runs took " + (withSteps / MB) + " MB", withSteps < 64 * MB);
		//keep everything reachable until it was measured
		assertTrue(cat.datasets().size() > 4 && home != null);
	}
}

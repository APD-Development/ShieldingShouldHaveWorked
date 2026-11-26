package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import org.junit.Test;

/**
 * The statistics screen once ran out of memory (256 MB heap) on Chris's phone. Run with
 * -Papd.history.dir=<an unzipped history export>: the whole catalog loads, every dataset's rows are read, and every
 * run's What happened and Replay floors are built, all inside a 256 MB heap. Skips when no history is given.
 */
public class RealHistoryMemoryTest {
	@Test
	public void aRealHistoryFitsInThePhonesHeap(){
		String dir = System.getProperty("apd.history.dir");
		assumeTrue("no history given", dir != null && new File(dir).isDirectory());
		long max = Runtime.getRuntime().maxMemory();
		assertTrue("heap must be capped at 256 MB for this test, was " + (max >> 20) + " MB", max <= 270L << 20);
		for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) if (p.getType() == MemoryType.HEAP) p.resetPeakUsage();

		long start = System.nanoTime();
		Catalog cat = new DatasetStore(new File(dir)).load();
		long rows = 0;
		java.util.Set<String> runs = new java.util.LinkedHashSet<>();
		for (Dataset d : cat.datasets()){
			for (Row r : d.rows) rows++;
			for (Dataset.RunInfo ri : d.runs) if (ri.finished && !ri.duplicate) runs.add(ri.runId);
		}
		int postmortems = 0, replays = 0;
		for (String id : runs){
			if (Postmortem.of(cat, id) != null) postmortems++;
			if (RunReplay.of(cat, id) != null) replays++;
		}
		long peak = 0;
		for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) if (p.getType() == MemoryType.HEAP) peak += p.getPeakUsage().getUsed();
		System.out.println("REAL HISTORY: datasets=" + cat.datasets().size() + " rows=" + rows + " finishedRuns=" + runs.size()
				+ " postmortems=" + postmortems + " replays=" + replays + " heapPeakMB=" + (peak >> 20) + " maxMB=" + (max >> 20)
				+ " seconds=" + ((System.nanoTime() - start) / 1_000_000_000L));
	}
}

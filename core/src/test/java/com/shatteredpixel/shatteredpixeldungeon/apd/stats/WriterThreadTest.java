package com.shatteredpixel.shatteredpixeldungeon.apd.stats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class WriterThreadTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final List<RunLogWriter> opened = new ArrayList<>();

	@After
	public void closeEverything(){
		for (RunLogWriter w : opened) w.close(false);
	}

	private RunLogWriter create( String id, RunLogWriter.Options options ){
		RunLogWriter w = RunLogWriter.create(new File(tmp.getRoot(), id + ".apdlog"), TelemetryVersion.CURRENT, id, "t", 0, options);
		opened.add(w);
		return w;
	}

	private static int liveWriterThreads(){
		int n = 0;
		for (Thread t : Thread.getAllStackTraces().keySet()){
			if (t.isAlive() && "apd-stats-writer".equals(t.getName())) n++;
		}
		return n;
	}

	@Test
	public void flushMakesEverythingVisibleInTheFile() throws Exception {
		RunLogWriter w = create("flush", new RunLogWriter.Options());
		for (int i = 0; i < 1000; i++) assertTrue(w.event(LogTestSupport.T_PLAIN).clock(i).unsigned(i).emit());
		assertTrue(w.flush(10_000, true));
		ReadResult r = RunLogReader.read(w.file());
		assertEquals(1000, r.events.size());
		assertEquals(ReadStatus.UNSEALED, r.status);
		//keep going after a flush
		assertTrue(w.event(LogTestSupport.T_PLAIN).clock(1000).unsigned(1000).emit());
		assertTrue(w.flush());
		assertEquals(1001, RunLogReader.read(w.file()).events.size());
	}

	@Test
	public void closeSealsStopsTheThreadAndRefusesLaterEvents() throws Exception {
		int before = liveWriterThreads();
		RunLogWriter w = create("close", new RunLogWriter.Options());
		assertEquals(before + 1, liveWriterThreads());
		for (int i = 0; i < 50; i++) w.event(LogTestSupport.T_PLAIN).unsigned(i).emit();
		assertTrue(w.close());
		assertEquals("worker thread must be gone after close", before, liveWriterThreads());

		ReadResult r = RunLogReader.read(w.file());
		assertEquals(ReadStatus.SEALED, r.status);
		assertEquals(50, r.events.size());

		assertFalse(w.event(LogTestSupport.T_PLAIN).unsigned(1).emit());
		assertFalse(w.append(LogTestSupport.T_PLAIN, 0, 0, new byte[0]));
		assertFalse(w.flush());
		assertTrue("closing twice is harmless", w.close());
		assertEquals(50, RunLogReader.read(w.file()).events.size());
		assertFalse(w.isDisabled());
	}

	@Test
	public void closeWithoutSealLeavesAResumableLog() throws Exception {
		RunLogWriter w = create("noseal", new RunLogWriter.Options());
		w.event(LogTestSupport.T_PLAIN).unsigned(1).emit();
		assertTrue(w.close(false));
		assertEquals(ReadStatus.UNSEALED, RunLogReader.read(w.file()).status);
	}

	@Test
	public void concurrentProducersLoseNothingAndKeepPerThreadOrder() throws Exception {
		final int threads = 8, perThread = 2000;
		RunLogWriter.Options o = new RunLogWriter.Options().queueCapacity(1 << 20);
		final RunLogWriter w = create("conc", o);
		final CountDownLatch start = new CountDownLatch(1);
		final AtomicInteger accepted = new AtomicInteger();
		List<Thread> ts = new ArrayList<>();
		for (int t = 0; t < threads; t++){
			final int id = t;
			Thread th = new Thread(() -> {
				try { start.await(); } catch (InterruptedException e){ return; }
				for (int i = 0; i < perThread; i++){
					if (w.event(LogTestSupport.T_STRINGS).unsigned(id).string("thread" + id).signed(i).emit()) accepted.incrementAndGet();
				}
			});
			ts.add(th);
			th.start();
		}
		start.countDown();
		for (Thread th : ts) th.join();
		assertTrue(w.close());
		assertEquals(threads * perThread, accepted.get());

		ReadResult r = RunLogReader.read(w.file());
		assertEquals(ReadStatus.SEALED, r.status);
		assertEquals(threads * perThread, r.events.size());
		assertEquals(threads, r.strings.size());
		int[] next = new int[threads];
		for (ApdEvent e : r.events){
			PayloadReader p = e.reader(r);
			int id = (int) p.unsigned();
			assertEquals("thread" + id, p.string()); //string definitions always arrive before their first use
			assertEquals(next[id]++, (int) p.signed());
		}
		for (int n : next) assertEquals(perThread, n);
	}

	@Test
	public void aFullQueueDropsEventsCountsThemAndWritesAGap() throws Exception {
		CountDownLatch gate = new CountDownLatch(1);
		RunLogWriter.Options o = new RunLogWriter.Options().queueCapacity(24);
		o.workerGate = gate; //the writer thread is held back, so the queue fills up
		RunLogWriter w = create("full", o);

		int accepted = 0;
		for (int i = 0; i < 200; i++){
			if (w.event(LogTestSupport.T_PLAIN).clock(i).unsigned(i).emit()) accepted++;
		}
		assertTrue(accepted > 0 && accepted < 200);
		assertEquals(accepted, w.eventsAccepted());
		assertEquals(200 - accepted, w.eventsDropped());
		assertFalse("a full queue is not a failure", w.isDisabled());

		gate.countDown();
		assertTrue(w.flush(10_000, false));
		//the next accepted event is preceded by a GAP that says how many were lost
		assertTrue(w.event(LogTestSupport.T_PLAIN).clock(500).unsigned(500).emit());
		assertTrue(w.close());

		ReadResult r = RunLogReader.read(w.file());
		assertEquals(ReadStatus.SEALED, r.status);
		assertEquals(accepted + 1 /*gap*/ + 1 /*last*/, r.events.size());
		ApdEvent gap = r.events.get(accepted);
		assertEquals(EventTypes.GAP, gap.type);
		PayloadReader p = gap.reader(r);
		assertEquals(EventTypes.GAP_DROPPED_EVENTS, p.unsigned());
		assertEquals(200 - accepted, p.unsigned());
		//the events that were accepted are exactly the first ones, in order
		for (int i = 0; i < accepted; i++) assertEquals(i, r.events.get(i).reader(r).unsigned());
	}

	@Test
	public void droppedEventsAreStillReportedIfTheLogIsClosedFirst() throws Exception {
		CountDownLatch gate = new CountDownLatch(1);
		RunLogWriter.Options o = new RunLogWriter.Options().queueCapacity(16);
		o.workerGate = gate;
		RunLogWriter w = create("dropclose", o);
		int accepted = 0;
		for (int i = 0; i < 100; i++) if (w.event(LogTestSupport.T_PLAIN).unsigned(i).emit()) accepted++;
		long dropped = 100 - accepted;
		assertTrue(dropped > 0);
		gate.countDown();
		assertTrue(w.close());
		ReadResult r = RunLogReader.read(w.file());
		assertEquals(ReadStatus.SEALED, r.status);
		assertEquals(1, r.gapEvents);
		ApdEvent gap = r.events.get(r.events.size() - 1);
		PayloadReader p = gap.reader(r);
		assertEquals(EventTypes.GAP_DROPPED_EVENTS, p.unsigned());
		assertEquals(dropped, p.unsigned());
	}

	@Test
	public void stringsInADroppedEventDoNotBreakLaterEvents() throws Exception {
		CountDownLatch gate = new CountDownLatch(1);
		RunLogWriter.Options o = new RunLogWriter.Options().queueCapacity(12);
		o.workerGate = gate;
		RunLogWriter w = create("dropstr", o);
		for (int i = 0; i < 60; i++) w.event(LogTestSupport.T_STRINGS).unsigned(i).string("unique-" + i).signed(i).emit();
		gate.countDown();
		w.flush();
		for (int i = 100; i < 105; i++){
			assertTrue(w.event(LogTestSupport.T_STRINGS).unsigned(i).string("late-" + i).signed(i).emit());
			assertTrue(w.flush()); //keeps the tiny queue empty so these are not dropped by a race with the writer thread
		}
		assertTrue(w.close());
		ReadResult r = RunLogReader.read(w.file());
		assertEquals(ReadStatus.SEALED, r.status);
		//every string reference in every surviving event resolves
		for (ApdEvent e : r.events){
			if (e.type != LogTestSupport.T_STRINGS) continue;
			PayloadReader p = e.reader(r);
			p.unsigned();
			assertTrue(p.string() != null);
		}
	}

	@Test
	public void manyOpenAndCloseCyclesLeakNoThreads() throws Exception {
		int before = liveWriterThreads();
		for (int i = 0; i < 30; i++){
			RunLogWriter w = create("cycle" + i, new RunLogWriter.Options());
			w.event(LogTestSupport.T_PLAIN).unsigned(i).emit();
			assertTrue(w.close());
		}
		assertEquals(before, liveWriterThreads());
	}
}

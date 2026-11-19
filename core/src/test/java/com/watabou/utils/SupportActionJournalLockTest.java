package com.watabou.utils;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

// Tester lag report (Nov 19): recording a touch/move must never wait for the journal's disk writes.
public class SupportActionJournalLockTest {
	@Rule public TemporaryFolder tmp = new TemporaryFolder();

	@After public void off(){ SupportActionJournal.setEnabled(false); }

	@Test public void recordNeverWaitsWhileTheWriterIsBusyOnDisk_andNothingIsLost() throws Exception {
		File dir = tmp.newFolder("journal");
		SupportActionJournal.configure(dir);
		SupportActionJournal.setEnabled(true);

		Field f = SupportActionJournal.class.getDeclaredField("IO");
		f.setAccessible(true);
		final Object io = f.get(null);
		final CountDownLatch holding = new CountDownLatch(1), release = new CountDownLatch(1);
		Thread disk = new Thread(() -> {
			synchronized (io){ // simulate a slow disk write in progress
				holding.countDown();
				try { release.await(5, TimeUnit.SECONDS); } catch (InterruptedException ignored){}
			}
		});
		disk.start();
		assertTrue(holding.await(2, TimeUnit.SECONDS));

		long start = System.nanoTime();
		for (int i = 0; i < 200; i++) SupportActionJournal.record("hero.action", "type=Move step=" + i);
		long ms = (System.nanoTime() - start) / 1_000_000L;
		assertTrue("record() blocked for " + ms + " ms while the disk was busy", ms < 500);

		release.countDown();
		disk.join(5000);
		// the writer catches up afterwards: every event lands in the file
		String text = "";
		for (int tries = 0; tries < 50 && !text.contains("step=199"); tries++){
			Thread.sleep(100);
			File log = new File(dir, "action-history.txt");
			if (log.isFile()) text = new String(Files.readAllBytes(log.toPath()), StandardCharsets.UTF_8);
		}
		assertTrue("last event written", text.contains("step=199"));
		assertTrue("first event written", text.contains("step=0\n"));
	}
}

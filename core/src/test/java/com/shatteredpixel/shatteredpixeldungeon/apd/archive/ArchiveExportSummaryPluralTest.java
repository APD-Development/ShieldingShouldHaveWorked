package com.shatteredpixel.shatteredpixeldungeon.apd.archive;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

// The export summary said "1 file(s) were not readable" and "1 saved damaged tail(s)"; it now uses proper singular/plural.
public class ArchiveExportSummaryPluralTest {

	private static String summary(int unreadable, int tails){
		ArchiveExporter.Report r = new ArchiveExporter.Report();
		r.ok = true;
		r.skippedUnreadable = unreadable;
		r.skippedDamagedTails = tails;
		return r.summary();
	}

	@Test
	public void singleCountsAreSingular(){
		String s = summary(1, 1);
		assertTrue(s, s.contains("\n1 file was not readable as a log and was left out."));
		assertTrue(s, s.contains("\n1 saved damaged tail stays on this device only."));
		assertFalse(s, s.contains("(s)"));
	}

	@Test
	public void largerCountsArePlural(){
		String s = summary(3, 2);
		assertTrue(s, s.contains("\n3 files were not readable as logs and were left out."));
		assertTrue(s, s.contains("\n2 saved damaged tails stay on this device only."));
		assertFalse(s, s.contains("(s)"));
	}
}

package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RecorderStatusPluralTest {
	@Test
	public void historyLineUsesSingularForOneRun(){
		RecorderStatus s = new RecorderStatus();
		s.runLogs = 1;
		s.storageBytes = 2048;
		assertTrue(s.summary(), s.summary().contains("History: 1 run, 2 KB"));
		s.runLogs = 0;
		assertTrue(s.summary(), s.summary().contains("History: 0 runs, 2 KB"));
		s.runLogs = 3;
		assertTrue(s.summary(), s.summary().contains("History: 3 runs, 2 KB"));
	}
}

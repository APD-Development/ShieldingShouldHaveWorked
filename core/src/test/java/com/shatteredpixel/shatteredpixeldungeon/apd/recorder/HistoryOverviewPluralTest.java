package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HistoryOverviewPluralTest {
	@Test
	public void overviewCountsUseSingularForOne(){
		assertEquals("1 game", ApdRecorder.count(1, "game", "games"));
		assertEquals("0 games", ApdRecorder.count(0, "game", "games"));
		assertEquals("2 distinct runs", ApdRecorder.count(2, "distinct run", "distinct runs"));
	}
}

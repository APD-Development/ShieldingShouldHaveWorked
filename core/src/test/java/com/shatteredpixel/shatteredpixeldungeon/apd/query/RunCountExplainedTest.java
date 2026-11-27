package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

//Trial jurors 4, 7 and 12 (Nov 27): "1 of 6 runs" with nothing saying what the other 5 were
public class RunCountExplainedTest {

	@Test
	public void everyLeftOutRunIsAccountedFor(){
		QueryResult.Stats s = new QueryResult.Stats();
		s.total = 6; s.inScope = 2; s.matching = 1; s.unfinished = 3; s.otherRulesets = 1;
		String line = StatusText.leftOutSummary(s, "run", "runs");
		assertEquals("5 runs not shown: 3 still going or not finished, 1 under other rules, 1 removed by your filters.", line);
	}

	@Test
	public void nothingLeftOutSaysNothing(){
		QueryResult.Stats s = new QueryResult.Stats();
		s.total = 4; s.inScope = 4; s.matching = 4;
		assertEquals("", StatusText.leftOutSummary(s, "run", "runs"));
	}

	@Test
	public void unexplainedRemainderIsStillCounted(){
		QueryResult.Stats s = new QueryResult.Stats();
		s.total = 6; s.inScope = 1; s.matching = 1; s.unfinished = 2;
		String line = StatusText.leftOutSummary(s, "run", "runs");
		assertTrue(line, line.startsWith("5 runs not shown: 2 still going or not finished, 3 hidden for other reasons"));
	}
}

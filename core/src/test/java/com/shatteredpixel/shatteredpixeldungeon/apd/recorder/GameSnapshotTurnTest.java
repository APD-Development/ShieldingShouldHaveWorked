package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import static org.junit.Assert.assertEquals;

import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import org.junit.After;
import org.junit.Test;

//Chris, Nov 25: "Marsupial rat killed you on floor 1, turn 0", with no health or damage shown. Every event between two saves had the
//same turn, because Statistics.duration only catches up when the actor clock is re-based.
public class GameSnapshotTurnTest {
	private float before;

	private static void now( float t ) throws Exception {
		java.lang.reflect.Field f = Actor.class.getDeclaredField("now");
		f.setAccessible(true);
		f.setFloat(null, t);
	}

	@After
	public void restore() throws Exception {
		now(0);
		Statistics.duration = 0;
	}

	@Test
	public void turnCountsTheActorClockBetweenSaves() throws Exception {
		Statistics.duration = 0;
		now(37);
		assertEquals(37, GameSnapshot.turn());
		//a save re-bases the clock: the total must not change
		Statistics.duration = 30;
		now(7);
		assertEquals(37, GameSnapshot.turn());
	}
}

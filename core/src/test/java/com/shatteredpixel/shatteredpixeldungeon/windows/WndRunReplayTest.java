package com.shatteredpixel.shatteredpixeldungeon.windows;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WndRunReplayTest {

	//a floor with no recorded steps used to restart itself forever (toggle -> floor -> toggle ...)
	@Test
	public void emptyFloorNeverRestarts(){
		assertFalse(WndRunReplay.restartOnPlay(false, 0, 0));
		assertFalse(WndRunReplay.restartOnPlay(true, 0, 0));
	}

	@Test
	public void finishedFloorRestartsOnPlay(){
		assertTrue(WndRunReplay.restartOnPlay(false, 5, 5));
		assertFalse(WndRunReplay.restartOnPlay(false, 3, 5));
		assertFalse(WndRunReplay.restartOnPlay(true, 5, 5));
	}
}

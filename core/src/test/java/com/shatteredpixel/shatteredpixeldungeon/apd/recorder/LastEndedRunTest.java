package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Test;

//Chris, Nov 24: Goo killed him, and "What happened" described an older run's gnoll-scout death on floor 2.
//A run APD was not recording must never inherit the previous run's id.
public class LastEndedRunTest {

	@After
	public void tidy(){
		ApdRecorder.reset();
	}

	@Test
	public void anUnrecordedDeathDoesNotShowTheLastRun(){
		ApdRecorder.reset();
		ApdRecorder.lastEndedRunId = "older-run";
		ApdRecorder.onRunEnded(false, null);
		assertNull(ApdRecorder.lastEndedRunId());
	}

	@Test
	public void startingOrLoadingARunForgetsTheLastOne(){
		ApdRecorder.reset();
		ApdRecorder.lastEndedRunId = "older-run";
		ApdRecorder.onNewRun();
		assertNull(ApdRecorder.lastEndedRunId());
		ApdRecorder.lastEndedRunId = "older-run";
		ApdRecorder.onGameLoaded(0, null, 0);
		assertNull(ApdRecorder.lastEndedRunId());
	}

	//Pie and Chris, Nov 25: starving opened the game-over screen before the death was filed, so What happened and Replay floors
	//were missing until the app was reopened. The run still being recorded is the one the screen is about.
	@Test
	public void theGameOverScreenFindsTheRunBeforeItsEndIsFiled() throws Exception {
		ItemTestSupport.startRun(313L);
		String id = ApdRecorder.gameOverRunId();
		assertNotNull(id);
		ApdRecorder.onRunEnded(false, null);
		assertEquals(id, ApdRecorder.lastEndedRunId());
		assertEquals(id, ApdRecorder.gameOverRunId());
	}

	@Test
	public void anUnrecordedRunHasNoGameOverRun(){
		ApdRecorder.reset();
		ApdRecorder.onNewRun();
		assertNull(ApdRecorder.gameOverRunId());
	}
}

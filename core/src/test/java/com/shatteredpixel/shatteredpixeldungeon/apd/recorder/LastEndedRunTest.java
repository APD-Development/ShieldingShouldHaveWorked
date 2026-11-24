package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

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
}

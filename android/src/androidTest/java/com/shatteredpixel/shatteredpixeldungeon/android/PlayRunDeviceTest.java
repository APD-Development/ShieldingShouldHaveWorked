package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdRecorder;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.RecorderStatus;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKeys;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunFacts;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunFactsBuilder;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunLogReader;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

//A real run on a real device: start a new Warrior game the way the character screen does, take some turns, save, continue from the save,
//and die. The run's history must be recorded and complete through all of it. Nothing else in the suite plays the game, so this is the
//check that the recording hooks, the save/continue path and the run end work together on Android.
@RunWith(AndroidJUnit4.class)
public class PlayRunDeviceTest {

	private static final String TAG = "APDUI";

	private static void onRender( final Runnable r ) throws Exception {
		final CountDownLatch done = new CountDownLatch(1);
		final AtomicReference<Throwable> failure = new AtomicReference<>();
		Game.runOnRenderThread(new com.watabou.utils.Callback(){
			@Override
			public void call(){
				try {
					r.run();
				} catch (Throwable t){
					failure.set(t);
				} finally {
					done.countDown();
				}
			}
		});
		assertTrue("the render thread did not answer", done.await(60, TimeUnit.SECONDS));
		if (failure.get() != null) throw new AssertionError("failed on the render thread: " + failure.get(), failure.get());
	}

	private static void waitUntil( String what, Callable<Boolean> c, long millis ) throws Exception {
		long end = System.currentTimeMillis() + millis;
		while (System.currentTimeMillis() < end){
			if (c.call()) return;
			Thread.sleep(150);
		}
		throw new AssertionError("timed out waiting for " + what);
	}

	private static RunFacts factsOfLog( String path ) throws Exception {
		ApdRecorder.flushForReading();
		assertNotNull("the recorder has a log for the run", path);
		return RunFactsBuilder.fromLog(RunLogReader.read(new File(path)), "device-run");
	}

	@Test
	public void aRunCanBePlayedSavedContinuedAndEndedWithItsHistoryRecorded() throws Exception {
		boolean recordingWas = SPDSettings.apdRecording();
		boolean introWas = SPDSettings.intro();
		final int slot = 1;
		try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)){
			long end = System.currentTimeMillis() + 120000;
			while ((Gdx.app == null || Gdx.files == null || Game.scene() == null) && System.currentTimeMillis() < end) Thread.sleep(250);
			assertNotNull("the game did not start", Game.scene());
			long titleBy = System.currentTimeMillis() + 60000;
			while (!(Game.scene() instanceof TitleScene) && System.currentTimeMillis() < titleBy) Thread.sleep(250);
			Thread.sleep(2500);
			SPDSettings.apdRecording(true);
			SPDSettings.intro(false);

			//start: exactly what the character screen's Start button does
			onRender(new Runnable(){
				public void run(){
					GamesInProgress.curSlot = slot;
					GamesInProgress.selectedClass = HeroClass.WARRIOR;
					Dungeon.hero = null;
					Dungeon.daily = Dungeon.dailyReplay = false;
					Dungeon.initSeed();
					ActionIndicator.clearAction();
					InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
					Game.switchScene(InterlevelScene.class);
				}
			});
			waitUntil("the first floor", new Callable<Boolean>(){
				public Boolean call(){ return Game.scene() instanceof GameScene && Dungeon.hero != null && Dungeon.hero.ready; } }, 150000);
			Log.w(TAG, "run started: depth " + Dungeon.depth + ", class " + Dungeon.hero.heroClass + ", log " + ApdRecorder.status().logPath);
			assertEquals(1, Dungeon.depth);
			assertEquals(HeroClass.WARRIOR, Dungeon.hero.heroClass);
			final String runId = ApdRecorder.currentRunId();
			assertNotNull("a run id was given to the new run", runId);

			//play: wait for ten turns
			for (int i = 0; i < 10; i++){
				waitUntil("the hero to be ready", new Callable<Boolean>(){ public Boolean call(){ return Dungeon.hero.ready; } }, 30000);
				onRender(new Runnable(){ public void run(){ Dungeon.hero.rest(false); } });
				Thread.sleep(120);
			}
			waitUntil("the hero to be ready", new Callable<Boolean>(){ public Boolean call(){ return Dungeon.hero.ready; } }, 30000);

			RecorderStatus st = ApdRecorder.status();
			assertTrue("recording is on", st.enabled && !st.sessionDisabled);
			assertEquals("no recorder errors while playing: " + st.lastError, 0, st.errorCount);
			assertTrue("events were recorded", st.eventsAccepted > 0);
			//the recorder forgets its log path once the run is sealed, so keep it from here
			final String logPath = ApdRecorder.status().logPath;
			RunFacts live = factsOfLog(logPath);
			assertEquals("WARRIOR", live.textOr(FactKeys.HERO_CLASS, "?"));
			assertTrue("the difficulty of the run was recorded", live.isKnown(FactKeys.DIFFICULTY));
			assertFalse("the run is not finished yet", live.isKnown(FactKeys.OUTCOME));

			//save, then continue from the save like the Continue button does
			final int hpBefore = Dungeon.hero.HP;
			onRender(new Runnable(){
				public void run(){
					try {
						Dungeon.saveAll();
					} catch (java.io.IOException e){
						throw new RuntimeException(e);
					}
					Dungeon.hero = null;
					InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
					Game.switchScene(InterlevelScene.class);
				}
			});
			waitUntil("the game to continue", new Callable<Boolean>(){
				public Boolean call(){ return Game.scene() instanceof GameScene && Dungeon.hero != null && Dungeon.hero.ready; } }, 150000);
			assertEquals("the same run continues", runId, ApdRecorder.currentRunId());
			assertEquals(1, Dungeon.depth);
			assertEquals(hpBefore, Dungeon.hero.HP);
			assertEquals(HeroClass.WARRIOR, Dungeon.hero.heroClass);
			assertEquals("still recording after the continue", 0, ApdRecorder.status().errorCount);

			//die: the run must be sealed with its outcome
			onRender(new Runnable(){ public void run(){ Dungeon.fail(Rat.class); } });
			Thread.sleep(1500);
			RunFacts done = factsOfLog(logPath);
			assertEquals("the run ended", LifecycleEvents.OUTCOME_DIED, done.numOr(FactKeys.OUTCOME, -1));
			assertEquals("sealed", done.textOr(FactKeys.STATUS, "?"));
			Log.w(TAG, "the run was played, saved, continued and ended; " + ApdRecorder.status().eventsAccepted + " events");
		} finally {
			try {
				GamesInProgress.delete(slot);
				FileUtils.deleteDir(GamesInProgress.gameFolder(slot));
			} catch (Throwable ignored){
			}
			SPDSettings.apdRecording(recordingWas);
			SPDSettings.intro(introWas);
		}
	}
}

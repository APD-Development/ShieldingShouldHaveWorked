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

	private static Object field(Object target, String name) throws Exception {
		Class<?> type = target.getClass();
		while (type != null){
			try { java.lang.reflect.Field f = type.getDeclaredField(name); f.setAccessible(true); return f.get(target); }
			catch (NoSuchFieldException ignored){ type = type.getSuperclass(); }
		}
		throw new NoSuchFieldException(name);
	}

	private static void click(Object button, String method) throws Exception {
		Class<?> type = button.getClass();
		while (type != null){
			try { java.lang.reflect.Method m = type.getDeclaredMethod(method); m.setAccessible(true); m.invoke(button); return; }
			catch (NoSuchMethodException ignored){ type = type.getSuperclass(); }
		}
		throw new NoSuchMethodException(method);
	}

	private static void assertGameplayControls() throws Exception {
		com.watabou.noosa.ui.Component toolbar = (com.watabou.noosa.ui.Component) field(Game.scene(), "toolbar");
		assertTrue("toolbar is visible and usable before guidebook acknowledgement", toolbar.visible && toolbar.active);
		com.watabou.noosa.ui.Component status = (com.watabou.noosa.ui.Component) field(Game.scene(), "status");
		assertTrue("health/status remain visible", status.visible && status.active);
		for (String name : new String[]{"btnSearch", "btnWait", "btnInventory"}){
			com.watabou.noosa.ui.Component button = (com.watabou.noosa.ui.Component) field(toolbar, name);
			assertTrue(name + " is visible and active", button.visible && button.active);
		}
		Object[] slots = (Object[]) field(toolbar, "btnQuick");
		assertTrue("quick-use slots exist", slots.length > 0);
		int visibleSlots = 0;
		for (Object slot : slots) if (((com.watabou.noosa.ui.Component) slot).visible) visibleSlots++;
		assertTrue("the current quick-use page is visible", visibleSlots > 0);
	}

	@Test
	public void aRunCanBePlayedSavedContinuedAndEndedWithItsHistoryRecorded() throws Exception {
		boolean recordingWas = SPDSettings.apdRecording();
		boolean introWas = SPDSettings.intro();
		boolean introPageFoundWas = com.shatteredpixel.shatteredpixeldungeon.journal.Document.ADVENTURERS_GUIDE.isPageFound(com.shatteredpixel.shatteredpixeldungeon.journal.Document.GUIDE_INTRO);
		boolean introPageReadWas = com.shatteredpixel.shatteredpixeldungeon.journal.Document.ADVENTURERS_GUIDE.isPageRead(com.shatteredpixel.shatteredpixeldungeon.journal.Document.GUIDE_INTRO);
		final int slot = 1;
		try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)){
			long end = System.currentTimeMillis() + 120000;
			while ((Gdx.app == null || Gdx.files == null || Game.scene() == null) && System.currentTimeMillis() < end) Thread.sleep(250);
			assertNotNull("the game did not start", Game.scene());
			long titleBy = System.currentTimeMillis() + 60000;
			while (!(Game.scene() instanceof TitleScene) && System.currentTimeMillis() < titleBy) Thread.sleep(250);
			Thread.sleep(2500);
			SPDSettings.apdRecording(true);
			com.shatteredpixel.shatteredpixeldungeon.journal.Document.ADVENTURERS_GUIDE.deletePage(com.shatteredpixel.shatteredpixeldungeon.journal.Document.GUIDE_INTRO);
			SPDSettings.intro(true);

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
			onRender(() -> { try { assertTrue("exercise controls before acknowledging intro", SPDSettings.intro()); assertGameplayControls(); } catch (Exception e){ throw new RuntimeException(e); } });
			//Use the actual toolbar callbacks; the tutorial must not suppress search or wait.
			onRender(() -> { try { click(field(field(Game.scene(), "toolbar"), "btnSearch"), "onLongClick"); } catch (Exception e){ throw new RuntimeException(e); } });
			waitUntil("ready after searching", () -> Dungeon.hero.ready, 30000);

			final String runId = ApdRecorder.currentRunId();
			assertNotNull("a run id was given to the new run", runId);

			//play: wait for ten turns
			for (int i = 0; i < 10; i++){
				waitUntil("the hero to be ready", new Callable<Boolean>(){ public Boolean call(){ return Dungeon.hero.ready; } }, 30000);
				onRender(() -> { try { click(field(field(Game.scene(), "toolbar"), "btnWait"), "onClick"); } catch (Exception e){ throw new RuntimeException(e); } });
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
						com.shatteredpixel.shatteredpixeldungeon.windows.WndGame menu = new com.shatteredpixel.shatteredpixeldungeon.windows.WndGame();
						GameScene.show(menu);
						com.shatteredpixel.shatteredpixeldungeon.ui.RedButton main = null;
						for (Object child : (java.util.List<?>) field(menu, "members")){
							if (child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.RedButton &&
									((com.shatteredpixel.shatteredpixeldungeon.ui.RedButton) child).text().equals(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(menu, "menu"))) main = (com.shatteredpixel.shatteredpixeldungeon.ui.RedButton) child;
						}
						assertNotNull("Main Menu exists", main);
						assertTrue("Main Menu is enabled during intro", main.active);
						click(main, "onClick");
					} catch (Exception e){ throw new RuntimeException(e); }
				}
			});
			waitUntil("Main Menu saves and returns to title", () -> Game.scene() instanceof TitleScene, 30000);
			onRender(() -> {
				Dungeon.hero = null;
				InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
				Game.switchScene(InterlevelScene.class);
			});
			waitUntil("the game to continue", new Callable<Boolean>(){
				public Boolean call(){ return Game.scene() instanceof GameScene && Dungeon.hero != null && Dungeon.hero.ready; } }, 150000);
			assertEquals("the same run continues", runId, ApdRecorder.currentRunId());
			assertEquals(1, Dungeon.depth);
			assertEquals(hpBefore, Dungeon.hero.HP);
			assertEquals(HeroClass.WARRIOR, Dungeon.hero.heroClass);
			assertEquals("still recording after the continue", 0, ApdRecorder.status().errorCount);
			onRender(() -> { try { assertGameplayControls(); } catch (Exception e){ throw new RuntimeException(e); } });

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
			com.shatteredpixel.shatteredpixeldungeon.journal.Document.ADVENTURERS_GUIDE.deletePage(com.shatteredpixel.shatteredpixeldungeon.journal.Document.GUIDE_INTRO);
			if (introPageFoundWas) com.shatteredpixel.shatteredpixeldungeon.journal.Document.ADVENTURERS_GUIDE.findPage(com.shatteredpixel.shatteredpixeldungeon.journal.Document.GUIDE_INTRO);
			if (introPageReadWas) com.shatteredpixel.shatteredpixeldungeon.journal.Document.ADVENTURERS_GUIDE.readPage(com.shatteredpixel.shatteredpixeldungeon.journal.Document.GUIDE_INTRO);
		}
	}

	//the other way a run ends: winning. The win must be recorded as a win and the log sealed, like a death.
	@Test
	public void aWonRunIsRecordedAsWonAndSealed() throws Exception {
		boolean recordingWas = SPDSettings.apdRecording();
		boolean introWas = SPDSettings.intro();
		final int slot = 2;
		try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)){
			long end = System.currentTimeMillis() + 120000;
			while ((Gdx.app == null || Gdx.files == null || Game.scene() == null) && System.currentTimeMillis() < end) Thread.sleep(250);
			assertNotNull("the game did not start", Game.scene());
			long titleBy = System.currentTimeMillis() + 60000;
			while (!(Game.scene() instanceof TitleScene) && System.currentTimeMillis() < titleBy) Thread.sleep(250);
			Thread.sleep(2500);
			SPDSettings.apdRecording(true);
			SPDSettings.intro(false);
			onRender(new Runnable(){
				public void run(){
					GamesInProgress.curSlot = slot;
					GamesInProgress.selectedClass = HeroClass.MAGE;
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
			assertEquals(HeroClass.MAGE, Dungeon.hero.heroClass);
			final String logPath = ApdRecorder.status().logPath;
			assertNotNull(logPath);
			for (int i = 0; i < 5; i++){
				waitUntil("the hero to be ready", new Callable<Boolean>(){ public Boolean call(){ return Dungeon.hero.ready; } }, 30000);
				onRender(new Runnable(){ public void run(){ Dungeon.hero.rest(false); } });
				Thread.sleep(120);
			}
			waitUntil("the hero to be ready", new Callable<Boolean>(){ public Boolean call(){ return Dungeon.hero.ready; } }, 30000);
			onRender(new Runnable(){ public void run(){ Dungeon.win(com.shatteredpixel.shatteredpixeldungeon.items.Amulet.class); } });
			Thread.sleep(1500);
			RunFacts done = factsOfLog(logPath);
			assertEquals("the run was won", LifecycleEvents.OUTCOME_WON, done.numOr(FactKeys.OUTCOME, -1));
			assertEquals("sealed", done.textOr(FactKeys.STATUS, "?"));
			assertEquals("MAGE", done.textOr(FactKeys.HERO_CLASS, "?"));
			Log.w(TAG, "the run was won and recorded as won");
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

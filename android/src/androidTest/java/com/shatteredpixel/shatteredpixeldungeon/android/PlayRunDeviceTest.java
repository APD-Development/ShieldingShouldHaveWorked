package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

	// hero.ready is changed by the actor thread; toolbar.active follows on the next render update.
	// Wait for actual controls instead of racing that update. Persistent disabled controls still fail.
	private static void waitForGameplayControls() throws Exception {
		final AtomicReference<String> state = new AtomicReference<>("not sampled");
		long deadline = System.currentTimeMillis() + 30000;
		while (System.currentTimeMillis() < deadline) {
			final java.util.concurrent.atomic.AtomicBoolean usable = new java.util.concurrent.atomic.AtomicBoolean();
			onRender(() -> {
				try {
					com.watabou.noosa.ui.Component toolbar = (com.watabou.noosa.ui.Component) field(Game.scene(), "toolbar");
					boolean enabled = Dungeon.hero.ready && Dungeon.hero.isAlive() && toolbar.visible && toolbar.active;
					StringBuilder detail = new StringBuilder("heroReady=").append(Dungeon.hero.ready)
							.append(", lastEnabled=").append(field(toolbar, "lastEnabled"));
					for (String name : new String[]{"btnSearch", "btnWait", "btnInventory"}) {
						com.watabou.noosa.ui.Component button = (com.watabou.noosa.ui.Component) field(toolbar, name);
						enabled &= button.visible && button.active;
						detail.append(", ").append(name).append("=").append(button.visible).append("/").append(button.active);
					}
					state.set(detail.toString());
					usable.set(enabled);
				} catch (Exception e) { throw new RuntimeException(e); }
			});
			if (usable.get()) return;
			Thread.sleep(150);
		}
		throw new AssertionError("gameplay controls did not become usable: " + state.get());
	}

	//Exercise the actual rendered category toolbar without consuming items or changing turns.
	private static void assertCategoryHotbarControls() throws Exception {
		int oldMode = SPDSettings.apdHotbarMode();
        int[] oldCategories = new int[6];
        String[] oldItems = new String[6];
        for (int i=0; i<6; i++) { oldCategories[i] = SPDSettings.apdHotbarCategory(i); oldItems[i] = SPDSettings.apdHotbarItem(i); }
		try {
			SPDSettings.apdHotbarMode(1);
			com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar.updateLayout();
			com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton.refresh();
			assertGameplayControls();
			com.watabou.noosa.ui.Component toolbar =
					(com.watabou.noosa.ui.Component) field(Game.scene(), "toolbar");
			Object[] slots = (Object[]) field(toolbar, "btnQuick");
			int count = 0;
			for (Object raw : slots) {
				com.watabou.noosa.ui.Component button = (com.watabou.noosa.ui.Component) raw;
				if (!button.visible) continue;
				count++;
				assertTrue("hotbar touch target remains usable", button.width() >= 16 && button.height() >= 16);
				assertTrue("hotbar fits camera horizontally", button.left() >= -1 && button.right() <= button.camera().width+1);
				for (String name : new String[]{"btnSearch", "btnWait", "btnInventory"}) {
					com.watabou.noosa.ui.Component control = (com.watabou.noosa.ui.Component) field(toolbar, name);
					if (control.visible && button.camera() == control.camera()) {
						assertTrue("category hotbar must not overlap " + name,
								button.right() <= control.left()+1 || control.right() <= button.left()+1
								|| button.bottom() <= control.top()+1 || control.bottom() <= button.top()+1);
					}
				}
			}
			assertTrue("category positions visible", count > 0);
			if (count < 6) {
				com.watabou.noosa.ui.Component swap = (com.watabou.noosa.ui.Component) field(toolbar, "btnSwap");
				assertTrue("narrow screen can reach all six slots", swap.visible && swap.active);
			}
			com.shatteredpixel.shatteredpixeldungeon.items.Item[] classic =
					new com.shatteredpixel.shatteredpixeldungeon.items.Item[6];
			for (int i=0; i<6; i++) classic[i] = Dungeon.quickslot.getItem(i);
			int hp = Dungeon.hero.HP;
			com.shatteredpixel.shatteredpixeldungeon.windows.WndCategoryHotbar.open(0);
			assertEquals("opening category spends no health", hp, Dungeon.hero.HP);
			assertTrue("opening category spends no turn", Dungeon.hero.ready);
			for (int i=0; i<6; i++) org.junit.Assert.assertSame("classic assignments preserved", classic[i], Dungeon.quickslot.getItem(i));
			for (Object child : new java.util.ArrayList<>((java.util.List<?>) field(Game.scene(), "members"))) {
                if (child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList) {
                    ((com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList) child).hide();
                }
            }
            com.shatteredpixel.shatteredpixeldungeon.items.Item food = null;
            for (com.shatteredpixel.shatteredpixeldungeon.items.Item candidate : Dungeon.hero.belongings) {
                if (candidate instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.Food) { food = candidate; break; }
            }
            assertNotNull("Warrior has food for a specific favorite", food);
            int quantity = food.quantity();
            assertTrue(com.shatteredpixel.shatteredpixeldungeon.apd.hotbar.CategoryHotbar.chooseItem(0, food));
            com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar.updateLayout();
            com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton.refresh();
            Object first = field(((Object[]) field(toolbar, "btnQuick"))[0], "slot");
            org.junit.Assert.assertSame("specific favorite displays actual inventory item", food, field(field(first, "slot"), "item"));
            assertFalse("generic Item label does not cover favorite", ((com.watabou.noosa.ui.Component) field(first, "categoryLabel")).visible);
            java.lang.reflect.Method hover = first.getClass().getDeclaredMethod("hoverText");
            hover.setAccessible(true);
            assertEquals("specific favorite tooltip names its item", food.name(), hover.invoke(first));
            assertEquals("configuring favorite never consumes it", quantity, food.quantity());
            assertTrue("configuring favorite spends no turn", Dungeon.hero.ready);
            for (int i=0; i<6; i++) org.junit.Assert.assertSame("custom favorite preserves classic assignments", classic[i], Dungeon.quickslot.getItem(i));
		} finally {
            for (int i=0; i<6; i++) { SPDSettings.apdHotbarCategory(i, oldCategories[i]); SPDSettings.apdHotbarItem(i, oldItems[i]); }
			SPDSettings.apdHotbarMode(oldMode);
			com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar.updateLayout();
			com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton.refresh();
		}
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
			waitForGameplayControls();
			onRender(() -> { try { assertTrue("exercise controls before acknowledging intro", SPDSettings.intro()); assertGameplayControls(); assertCategoryHotbarControls(); } catch (Exception e){ throw new RuntimeException(e); } });
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
			waitForGameplayControls();
			onRender(() -> { try { assertGameplayControls(); assertCategoryHotbarControls(); } catch (Exception e){ throw new RuntimeException(e); } });

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

    private static com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList clericMenu() throws Exception {
        for (Object child : (java.util.List<?>) field(Game.scene(), "members")) {
            if (child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList)
                return (com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList) child;
        }
        throw new AssertionError("Cleric assignment menu did not open");
    }

    private static void assertClericStack() throws Exception {
        com.shatteredpixel.shatteredpixeldungeon.ui.ClericQuickCastTag[] tags =
                (com.shatteredpixel.shatteredpixeldungeon.ui.ClericQuickCastTag[]) field(Game.scene(), "clericSpells");
        for (com.shatteredpixel.shatteredpixeldungeon.ui.ClericQuickCastTag tag : tags) tag.update();
        GameScene.layoutTags();
        int count = SPDSettings.apdClericQuickSlots();
        com.watabou.noosa.ui.Component toolbar = (com.watabou.noosa.ui.Component) field(Game.scene(), "toolbar");
        for (int i=0; i<tags.length; i++) {
            assertEquals("chosen number of spell buttons", i<count, tags[i].visible);
            if (i>=count) continue;
            assertTrue("spell touch target", tags[i].width()>=24 && tags[i].height()>=24);
            assertTrue("spell inside horizontal screen", tags[i].left()>=0 && tags[i].right()<=GameScene.uiCamera.width);
            assertTrue("spell inside vertical screen", tags[i].top()>=0 && tags[i].bottom()<=GameScene.uiCamera.height);
            assertTrue("spell never covers hotbar", tags[i].bottom()<=toolbar.top());
            assertEquals("spells stack vertically", tags[0].left(),tags[i].left(),0.01f);
            assertEquals("spell 2/3/4 above previous spell", tags[0].top()-24*i,tags[i].top(),0.01f);
        }
    }

    @Test
    public void clericQuickCastsPersistAndKeepNormalCostsAndTargeting() throws Exception {
        int oldCount=SPDSettings.apdClericQuickSlots(), oldMode=SPDSettings.apdHotbarMode();
        boolean oldIntro=SPDSettings.intro(), oldFlip=SPDSettings.flipTags();
        String[] oldSpells=new String[4];
        for(int i=0;i<4;i++) oldSpells[i]=SPDSettings.apdClericQuickSpell(i);
        final int slot=4; // Isolated emulator scratch run only.
        try (ActivityScenario<AndroidLauncher> scenario=ActivityScenario.launch(AndroidLauncher.class)) {
            waitUntil("game initialized",()->Gdx.app!=null && Gdx.files!=null && Game.scene()!=null,120000);
            // ActivityScenario can resume the scene retained by the preceding test.
            onRender(()->Game.switchScene(TitleScene.class));
            waitUntil("title",()->Game.scene() instanceof TitleScene,60000);
            SPDSettings.intro(false); SPDSettings.flipTags(false); SPDSettings.apdHotbarMode(1);
            SPDSettings.apdClericQuickSlots(4);
            onRender(()-> {
                GamesInProgress.curSlot=slot; GamesInProgress.selectedClass=HeroClass.CLERIC;
                Dungeon.hero=null; Dungeon.daily=Dungeon.dailyReplay=false; Dungeon.initSeed();
                ActionIndicator.clearAction(); InterlevelScene.mode=InterlevelScene.Mode.DESCEND;
                Game.switchScene(InterlevelScene.class);
            });
            waitUntil("Cleric first floor",()->Game.scene() instanceof GameScene && Dungeon.hero!=null && Dungeon.hero.ready,150000);
            assertEquals(HeroClass.CLERIC,Dungeon.hero.heroClass);
            onRender(()-> { try {
                com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome tome =
                        Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome.class);
                assertNotNull(tome);
                com.shatteredpixel.shatteredpixeldungeon.apd.hotbar.ClericQuickCasts.assign(0,
                        com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.GuidingLight.INSTANCE,tome);
                com.shatteredpixel.shatteredpixeldungeon.apd.hotbar.ClericQuickCasts.assign(1,
                        com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon.INSTANCE,tome);
                com.shatteredpixel.shatteredpixeldungeon.apd.hotbar.ClericQuickCasts.assign(2,
                        com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWard.INSTANCE,tome);
                com.shatteredpixel.shatteredpixeldungeon.apd.hotbar.ClericQuickCasts.assign(3,
                        com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Sunray.INSTANCE,tome);
                float charge=((Number)field(tome,"charge")).floatValue(), time=Dungeon.hero.cooldown();
                for(int n=1;n<=4;n++) { SPDSettings.apdClericQuickSlots(n); assertClericStack(); }
                assertEquals("assignment does not spend charge",charge,((Number)field(tome,"charge")).floatValue(),0);
                assertEquals("assignment does not spend a turn",time,Dungeon.hero.cooldown(),0);

                // Real long-press on the original picker opens a position chooser without casting.
                com.shatteredpixel.shatteredpixeldungeon.windows.WndClericSpells picker =
                        new com.shatteredpixel.shatteredpixeldungeon.windows.WndClericSpells(tome,Dungeon.hero,false);
                GameScene.show(picker);
                for(Object child:(java.util.List<?>)field(picker,"members")) {
                    if(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndClericSpells.SpellButton) {
                        click(child,"onLongClick"); break;
                    }
                }
                com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList menu=clericMenu();
                assertEquals("four assignment positions",4,((java.util.List<?>)field(menu,"entries")).size());
                ((com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.Picker)field(menu,"picker")).onPick(0,false);
                menu.hide();
                assertEquals("long press does not cast",charge,((Number)field(tome,"charge")).floatValue(),0);

                Object[] tags=(Object[])field(Game.scene(),"clericSpells");
                click(tags[1],"onLongClick");
                menu=clericMenu();
                ((com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.Picker)field(menu,"picker")).onPick(0,false);
                menu.hide();
                assertNull("saved spell can be removed",com.shatteredpixel.shatteredpixeldungeon.apd.hotbar.ClericQuickCasts.saved(1,tome));
                com.shatteredpixel.shatteredpixeldungeon.apd.hotbar.ClericQuickCasts.assign(1,
                        com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon.INSTANCE,tome);

                click(tags[3],"onClick");
                assertEquals("locked saved spell spends no charge",charge,((Number)field(tome,"charge")).floatValue(),0);
                click(tags[1],"onClick");
                assertNotNull("actual Holy Weapon effect",Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon.HolyWepBuff.class));
                assertEquals("vanilla Holy Weapon charge cost",charge-2,((Number)field(tome,"charge")).floatValue(),0);
                assertEquals("vanilla Holy Weapon turn cost",time,Dungeon.hero.cooldown(),0);
                float remaining=((Number)field(tome,"charge")).floatValue();
                click(tags[0],"onClick");
                assertEquals("targeting waits for selection",remaining,((Number)field(tome,"charge")).floatValue(),0);
                assertTrue("targeted spell can be canceled",GameScene.cancel());
                assertEquals("cancel spends no charge",remaining,((Number)field(tome,"charge")).floatValue(),0);

                tome.directCharge(2);
                float beforeSwitch=((Number)field(tome,"charge")).floatValue();
                click(tags[0],"onClick");
                click(tags[2],"onClick");
                assertNotNull("switching to Holy Ward applies its normal effect",Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWard.HolyArmBuff.class));
                assertEquals("switched spell pays its own normal cost",beforeSwitch-1,((Number)field(tome,"charge")).floatValue(),0);
                assertFalse("old Guiding Light selector is canceled when switching spells",GameScene.cancel());

                // Force simultaneous tags on portrait/narrow/landscape UI viewports and both sides.
                com.watabou.noosa.Camera camera=GameScene.uiCamera;
                int width=camera.width,height=camera.height;
                com.watabou.noosa.ui.Component toolbar=(com.watabou.noosa.ui.Component)field(Game.scene(),"toolbar");
                float tx=toolbar.left(),ty=toolbar.top(),tw=toolbar.width(),th=toolbar.height();
                String[] flags={"tagAttack","tagLoot","tagAction","tagResume","tagStairs"};
                boolean[] was=new boolean[flags.length];
                try {
                    for(int i=0;i<flags.length;i++) {
                        java.lang.reflect.Field flag=GameScene.class.getDeclaredField(flags[i]); flag.setAccessible(true);
                        was[i]=flag.getBoolean(Game.scene()); flag.setBoolean(Game.scene(),true);
                    }
                    for(int[] size:new int[][]{{160,240},{320,180},{240,320}}) {
                        camera.resize(size[0],size[1]); toolbar.setRect(0,size[1]-26,size[0],26);
                        for(boolean flip:new boolean[]{false,true}) {
                            SPDSettings.flipTags(flip); assertClericStack();
                            java.util.List<com.watabou.noosa.ui.Component> controls=new java.util.ArrayList<>();
                            for(Object tag:tags) controls.add((com.watabou.noosa.ui.Component)tag);
                            for(String name:new String[]{"attack","loot","action","resume","stairs"}) controls.add((com.watabou.noosa.ui.Component)field(Game.scene(),name));
                            for(int i=0;i<controls.size();i++) {
                                com.watabou.noosa.ui.Component a=controls.get(i);
                                assertTrue("tag stays on screen",a.left()>=0 && a.right()<=camera.width && a.top()>=0 && a.bottom()<=toolbar.top());
                                for(int j=i+1;j<controls.size();j++) {
                                    com.watabou.noosa.ui.Component b=controls.get(j);
                                    assertTrue("spell/attack/loot/action/resume tags never overlap",a.right()<=b.left()+0.01f || b.right()<=a.left()+0.01f
                                            || a.bottom()<=b.top()+0.01f || b.bottom()<=a.top()+0.01f);
                                }
                            }
                        }
                    }
                } finally {
                    camera.resize(width,height); toolbar.setRect(tx,ty,tw,th); SPDSettings.flipTags(false);
                    for(int i=0;i<flags.length;i++) {
                        java.lang.reflect.Field flag=GameScene.class.getDeclaredField(flags[i]); flag.setAccessible(true);
                        flag.setBoolean(Game.scene(),was[i]);
                    }
                    GameScene.updateTags=true; GameScene.layoutTags();
                }
                Dungeon.saveAll(); Game.switchScene(TitleScene.class);
            } catch(Exception e) { throw new RuntimeException(e); } });
            waitUntil("saved title",()->Game.scene() instanceof TitleScene,60000);
            onRender(()-> { Dungeon.hero=null; InterlevelScene.mode=InterlevelScene.Mode.CONTINUE; Game.switchScene(InterlevelScene.class); });
            waitUntil("saved Cleric resumed",()->Game.scene() instanceof GameScene && Dungeon.hero!=null && Dungeon.hero.ready,150000);
            onRender(()-> { try {
                assertEquals(4,SPDSettings.apdClericQuickSlots());
                assertClericStack();
                com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome tome =
                        Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome.class);
                org.junit.Assert.assertSame(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon.INSTANCE,
                        com.shatteredpixel.shatteredpixeldungeon.apd.hotbar.ClericQuickCasts.saved(1,tome));
                org.junit.Assert.assertSame(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Sunray.INSTANCE,
                        com.shatteredpixel.shatteredpixeldungeon.apd.hotbar.ClericQuickCasts.saved(3,tome));
                assertNull("locked preference preserved without granting spell",
                        com.shatteredpixel.shatteredpixeldungeon.apd.hotbar.ClericQuickCasts.available(Dungeon.hero,tome,3));
            } catch(Exception e) { throw new RuntimeException(e); } });
        } finally {
            GamesInProgress.delete(slot); FileUtils.deleteDir(GamesInProgress.gameFolder(slot));
            SPDSettings.intro(oldIntro); SPDSettings.flipTags(oldFlip); SPDSettings.apdHotbarMode(oldMode);
            SPDSettings.apdClericQuickSlots(oldCount);
            for(int i=0;i<4;i++) SPDSettings.apdClericQuickSpell(i,oldSpells[i]);
        }
    }

    static void visibilityScreenshot(String name) throws Exception {
        android.content.Context context=androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext();
        android.graphics.Bitmap bitmap=androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        assertNotNull(bitmap);
        android.content.ContentValues values=new android.content.ContentValues();
        values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME,name);
        values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE,"image/png");
        values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH,"Pictures/APD-Test-Visuals");
        android.net.Uri uri=context.getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        assertNotNull(uri);
        try(java.io.OutputStream out=context.getContentResolver().openOutputStream(uri)){assertNotNull(out);assertTrue(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out));}
        finally{bitmap.recycle();}
    }
    private static com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions stairsMenu() throws Exception {
        for(Object member:(java.util.List<?>)field(Game.scene(),"members"))
            if(member instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions)
                return (com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions)member;
        return null;
    }
    @Test public void tileVisibilityAndStairNavigationKeepNormalTurnsFogAndControls() throws Exception {
        boolean oldEnhanced=SPDSettings.enhancedTileVisibility(),oldShortcut=SPDSettings.stairNavigationShortcut();
        boolean oldRecord=SPDSettings.apdRecording(),oldIntro=SPDSettings.intro();int oldGrid=SPDSettings.visualGrid();
        int oldSpeed=SPDSettings.playbackSpeed();
        try(ActivityScenario<AndroidLauncher> scenario=ActivityScenario.launch(AndroidLauncher.class)){
            waitUntil("game ready",()->Gdx.app!=null && Game.scene()!=null,120000);
            onRender(()->{
                SPDSettings.apdRecording(false);SPDSettings.intro(false);SPDSettings.visualGrid(0);
                SPDSettings.enhancedTileVisibility(false);
                // Exercise a fresh preference: do not force-enable the shortcut as TEST79 did.
                com.badlogic.gdx.Preferences prefs=Gdx.app.getPreferences("ShatteredPixelDungeon");
                prefs.remove("apd_stair_navigation_shortcut");prefs.flush();
                assertTrue("stair shortcut is discoverable by default", SPDSettings.stairNavigationShortcut());
                GamesInProgress.curSlot=4;GamesInProgress.selectedClass=HeroClass.WARRIOR;
                Dungeon.hero=null;Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.initSeed();
                ActionIndicator.clearAction();InterlevelScene.mode=InterlevelScene.Mode.DESCEND;Game.switchScene(InterlevelScene.class);
            });
            waitUntil("first floor ready",()->Game.scene() instanceof GameScene && Dungeon.hero!=null && Dungeon.hero.ready,150000);
            waitForGameplayControls();Thread.sleep(600);
            final long[] observed=new long[3];
            for(int mode=0;mode<3;mode++){
                final int selected=mode;
                final java.util.concurrent.atomic.AtomicLong started=new java.util.concurrent.atomic.AtomicLong(),finished=new java.util.concurrent.atomic.AtomicLong();
                final float turnBefore=com.shatteredpixel.shatteredpixeldungeon.actors.Actor.now();
                onRender(()->{try{
                    SPDSettings.playbackSpeed(selected);
                    com.watabou.noosa.Group world=(com.watabou.noosa.Group)field(Game.scene(),"mobs");
                    assertTrue("dungeon sprites use playback groups",world instanceof com.shatteredpixel.shatteredpixeldungeon.apd.PlaybackGroup);
                    started.set(System.nanoTime());
                    world.add(new com.watabou.noosa.tweeners.Tweener(new com.watabou.noosa.Gizmo(),0.5f){
                        @Override protected void updateValues(float progress){}
                        @Override protected void onComplete(){finished.set(System.nanoTime());killAndErase();}
                    });
                }catch(Exception e){throw new RuntimeException(e);}});
                waitUntil("native animation completes",()->finished.get()!=0,10000);
                observed[mode]=finished.get()-started.get();
                assertEquals("visual playback consumes no turn",turnBefore,com.shatteredpixel.shatteredpixeldungeon.actors.Actor.now(),0f);
            }
            assertTrue("half-speed animation is slower than normal",observed[0]>observed[1]);
            assertTrue("double-speed animation is faster than normal",observed[1]>observed[2]);
            Log.i(TAG,"Playback animation nanoseconds slow/normal/fast: "+java.util.Arrays.toString(observed));
            onRender(()->SPDSettings.playbackSpeed(1));
            final int[] terrain=Dungeon.level.map.clone();final boolean[] visited=Dungeon.level.visited.clone();
            final boolean[] fov=Dungeon.hero.fieldOfView.clone();final int depth=Dungeon.depth;
            for(int zoom:new int[]{2,4}){
                onRender(()->{com.watabou.noosa.Camera.main.zoom(zoom);SPDSettings.enhancedTileVisibility(false);});Thread.sleep(350);
                visibilityScreenshot("tiles-original-"+zoom+".png");
                onRender(()->SPDSettings.enhancedTileVisibility(true));Thread.sleep(350);
                visibilityScreenshot("tiles-enhanced-"+zoom+".png");
                assertTrue(SPDSettings.enhancedTileVisibility());assertEquals(0,SPDSettings.visualGrid());
                org.junit.Assert.assertArrayEquals("terrain unchanged",terrain,Dungeon.level.map);
                org.junit.Assert.assertArrayEquals("exploration unchanged",visited,Dungeon.level.visited);
                org.junit.Assert.assertArrayEquals("fog unchanged",fov,Dungeon.hero.fieldOfView);
            }
            final java.util.concurrent.atomic.AtomicReference<com.watabou.utils.Point> stairTouch=new java.util.concurrent.atomic.AtomicReference<>();
            onRender(()->{try{
                com.watabou.noosa.ui.Component button=(com.watabou.noosa.ui.Component)field(Game.scene(),"stairs");
                com.watabou.noosa.ui.Component toolbar=(com.watabou.noosa.ui.Component)field(Game.scene(),"toolbar");
                assertTrue(button.visible);assertTrue(button.bottom()<=toolbar.top()+0.01f);
                assertTrue(button.left()>=0 && button.right()<=button.camera().width);
                stairTouch.set(button.camera().cameraToScreen(button.left()+button.width()/2f,button.top()+button.height()/2f));
            }catch(Exception e){throw new RuntimeException(e);}});
            com.watabou.utils.Point at=stairTouch.get();
            com.watabou.input.PointerEvent.addPointerEvent(new com.watabou.input.PointerEvent(at.x,at.y,9,com.watabou.input.PointerEvent.Type.DOWN,com.watabou.input.PointerEvent.LEFT));
            Thread.sleep(150);
            com.watabou.input.PointerEvent.addPointerEvent(new com.watabou.input.PointerEvent(at.x,at.y,9,com.watabou.input.PointerEvent.Type.UP,com.watabou.input.PointerEvent.LEFT));
            waitUntil("stair menu responds to a real touch",()->stairsMenu()!=null,30000);
            visibilityScreenshot("stairs-shortcut.png");
            onRender(()->{try{stairsMenu().hide();}catch(Exception e){throw new RuntimeException(e);}});
            final java.util.concurrent.atomic.AtomicInteger destination=new java.util.concurrent.atomic.AtomicInteger(-1);
            onRender(()->{
                for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:new java.util.ArrayList<>(Dungeon.level.mobs))
                    com.shatteredpixel.shatteredpixeldungeon.actors.Actor.remove(mob);
                Dungeon.level.mobs.clear();Dungeon.hero.checkVisibleMobs();
                for(int offset:com.watabou.utils.PathFinder.NEIGHBOURS8){int cell=Dungeon.hero.pos+offset;
                    if(cell>=0 && cell<Dungeon.level.length() && Dungeon.hero.fieldOfView[cell] && com.shatteredpixel.shatteredpixeldungeon.apd.navigation.StairNavigation.safeStep(Dungeon.level,Dungeon.hero,cell)){destination.set(cell);break;}}
                assertTrue("a discovered safe adjacent tile exists",destination.get()>=0);
                Dungeon.level.transitions.add(0,new com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition(
                        Dungeon.level,destination.get(),com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition.Type.REGULAR_EXIT));
                assertNull(com.shatteredpixel.shatteredpixeldungeon.apd.navigation.StairNavigation.begin(Dungeon.hero,false));
                float turn=Dungeon.hero.cooldown();int origin=Dungeon.hero.pos;
                Dungeon.level.passable[destination.get()]=false;
                try{
                    java.lang.reflect.Method move=com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero.class.getDeclaredMethod("actMove",com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction.Move.class);
                    move.setAccessible(true);assertFalse((Boolean)move.invoke(Dungeon.hero,Dungeon.hero.curAction));
                }catch(Exception e){throw new RuntimeException(e);}finally{Dungeon.level.passable[destination.get()]=true;}
                assertNull("newly blocked route stops",Dungeon.hero.curAction);assertEquals(origin,Dungeon.hero.pos);assertEquals(turn,Dungeon.hero.cooldown(),0);
                assertNull(com.shatteredpixel.shatteredpixeldungeon.apd.navigation.StairNavigation.begin(Dungeon.hero,false));
                Dungeon.hero.interrupt();assertNull(Dungeon.hero.curAction);assertNull(Dungeon.hero.lastAction);
                assertNull(com.shatteredpixel.shatteredpixeldungeon.apd.navigation.StairNavigation.begin(Dungeon.hero,false));
                Dungeon.hero.damageInterrupt=false;Dungeon.hero.damage(1,new com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger());
                assertNull("damage cancels the route",Dungeon.hero.curAction);
                assertNull(com.shatteredpixel.shatteredpixeldungeon.apd.navigation.StairNavigation.begin(Dungeon.hero,false));
                Rat threat=new Rat();threat.pos=destination.get();GameScene.add(threat);Dungeon.hero.checkVisibleMobs();
                assertNull("new visible enemy cancels the route",Dungeon.hero.curAction);
                com.shatteredpixel.shatteredpixeldungeon.actors.Actor.remove(threat);Dungeon.level.mobs.remove(threat);
                if(threat.sprite!=null)threat.sprite.killAndErase();Dungeon.hero.checkVisibleMobs();
                try{
                    click(field(Game.scene(),"stairs"),"onClick");
                    com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions menu=stairsMenu();assertNotNull(menu);
                    java.lang.reflect.Method choose=menu.getClass().getDeclaredMethod("onSelect",int.class);choose.setAccessible(true);choose.invoke(menu,1);menu.hide();
                    boolean marked=false;
                    for(Object effect:(java.util.List<?>)field(field(Game.scene(),"overFogEffects"),"members"))
                        if(effect instanceof com.shatteredpixel.shatteredpixeldungeon.effects.StairHighlight
                                && ((com.shatteredpixel.shatteredpixeldungeon.effects.StairHighlight)effect).cell==destination.get())marked=true;
                    assertTrue("the selected known stair is highlighted exactly",marked);
                }catch(Exception e){throw new RuntimeException(e);}
            });
            Thread.sleep(300); // Capture a completed rendered frame while the short marker is still visible.
            visibilityScreenshot("stairs-highlight.png");
            waitUntil("walk arrives and stops",()->Dungeon.hero.ready && Dungeon.hero.pos==destination.get(),30000);
            assertNull(Dungeon.hero.curAction);assertEquals("no automatic floor transition",depth,Dungeon.depth);
            onRender(()->{try{
                assertNull(com.shatteredpixel.shatteredpixeldungeon.apd.navigation.StairNavigation.begin(Dungeon.hero,true));
                assertTrue("save during pending navigation",Dungeon.hero.curAction instanceof com.shatteredpixel.shatteredpixeldungeon.apd.navigation.StairNavigation.Walk);
                Dungeon.saveAll();Game.switchScene(TitleScene.class);
            }catch(Exception e){throw new RuntimeException(e);}});
            waitUntil("title after save",()->Game.scene() instanceof TitleScene,30000);
            onRender(()->{Dungeon.hero=null;InterlevelScene.mode=InterlevelScene.Mode.CONTINUE;Game.switchScene(InterlevelScene.class);});
            waitUntil("saved navigation position",()->Game.scene() instanceof GameScene && Dungeon.hero!=null && Dungeon.hero.ready,150000);
            assertEquals(destination.get(),Dungeon.hero.pos);assertEquals(depth,Dungeon.depth);assertNull(Dungeon.hero.curAction);
            assertTrue(SPDSettings.enhancedTileVisibility());assertTrue(SPDSettings.stairNavigationShortcut());
            final com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem[] chaliceWindow=new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem[1];
            final com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact[] oldArtifact=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact[1];
            final int[] oldHp=new int[1];
            onRender(()->{
                oldArtifact[0]=Dungeon.hero.belongings.artifact;oldHp[0]=Dungeon.hero.HP;
                com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood chalice=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood();
                for(int i=0;i<5;i++)chalice.upgrade();
                Dungeon.hero.belongings.artifact=chalice;Dungeon.hero.HP=Dungeon.hero.HT;
                String description=chalice.desc();
                assertTrue(description.contains("Base damage:"));assertTrue(description.contains("HP loss after shields:"));
                assertTrue(description.contains("WARNING: This prick could kill you!"));
                chaliceWindow[0]=new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem(chalice);
                GameScene.show(chaliceWindow[0]);
            });
            Thread.sleep(350);visibilityScreenshot("chalice-preview.png");
            onRender(()->{chaliceWindow[0].hide();Dungeon.hero.belongings.artifact=oldArtifact[0];Dungeon.hero.HP=oldHp[0];Game.switchScene(TitleScene.class);});
        }finally{
            SPDSettings.enhancedTileVisibility(oldEnhanced);SPDSettings.stairNavigationShortcut(oldShortcut);
            SPDSettings.visualGrid(oldGrid);SPDSettings.apdRecording(oldRecord);SPDSettings.intro(oldIntro);
            SPDSettings.playbackSpeed(oldSpeed);
        }
    }

}

package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.app.Instrumentation;
import android.util.Log;
import android.view.KeyEvent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Query;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Question;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Questions;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdModel;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.UimsdScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndAPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdAsk;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Gizmo;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

//Android's Back, pressed for real (the system's own Back key is injected into the app), must walk backwards through whatever is open:
//a dialog closes before the screen under it, a menu opened from a menu returns to that menu, a tour ends before UIMSD is left, and UIMSD
//returns to where it was opened from. Each press must be ONE step (never two, never none).
@RunWith(AndroidJUnit4.class)
public class AndroidBackDeviceTest {

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

	private static void waitUntil( String what, java.util.concurrent.Callable<Boolean> c, long millis ) throws Exception {
		long end = System.currentTimeMillis() + millis;
		while (System.currentTimeMillis() < end){
			if (c.call()) return;
			Thread.sleep(100);
		}
		throw new AssertionError("timed out waiting for " + what);
	}

	//the system's Back key, exactly as the Back button does it
	private static void pressBack() throws Exception {
		Instrumentation in = InstrumentationRegistry.getInstrumentation();
		in.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
		Thread.sleep(500);
	}

	@SuppressWarnings("unchecked")
	private static List<Gizmo> membersOf( Group g ) throws Exception {
		Field f = Group.class.getDeclaredField("members");
		f.setAccessible(true);
		List<Gizmo> list = (List<Gizmo>) f.get(g);
		return list == null ? new ArrayList<Gizmo>() : new ArrayList<>(list);
	}

	private static int windows() throws Exception {
		int n = 0;
		for (Gizmo g : membersOf(Game.scene())) if (g instanceof Window && ((Window) g).alive) n++;
		return n;
	}

	private static Object uimsd( String name ) throws Exception {
		Field f = UimsdScene.class.getDeclaredField(name);
		f.setAccessible(true);
		return f.get(null);
	}

	@Test
	public void ordinarySettingsBackClosesOnlyTheWindowWithoutRebuildingTheScene() throws Exception {
		try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)){
			waitUntil("the title scene", () -> Gdx.app != null && Game.instance != null
					&& Game.scene() instanceof TitleScene, 120000);
			final com.watabou.noosa.Scene sceneBefore = Game.scene();
			final int before = windows();
			onRender(() -> ShatteredPixelDungeon.scene().add(new com.shatteredpixel.shatteredpixeldungeon.windows.WndSettings()));
			waitUntil("ordinary Settings window", () -> windows() == before + 1, 30000);
			pressBack();
			waitUntil("ordinary Settings window closed", () -> windows() == before, 30000);
			assertTrue("ordinary Settings Back must leave the current scene intact", Game.scene() == sceneBefore);
		}
	}

	@Test
	public void backWalksBackwardsOneStepPerPress() throws Exception {
		try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)){
			long end = System.currentTimeMillis() + 120000;
			while ((Gdx.app == null || Gdx.files == null || Game.scene() == null) && System.currentTimeMillis() < end) Thread.sleep(250);
			assertNotNull("the game did not start", Game.scene());
			waitUntil("the title scene", new java.util.concurrent.Callable<Boolean>(){
				public Boolean call(){ return Game.scene() instanceof TitleScene; } }, 120000);
			Thread.sleep(2500);

			//1. two dialogs over the title: each Back closes ONE, the newest first, and the title stays
			final int base = windows();
			onRender(new Runnable(){
				public void run(){
					ShatteredPixelDungeon.scene().add(new WndAPDSettings());
					ShatteredPixelDungeon.scene().addToFront(new WndMessage("Back test"));
				}
			});
			Thread.sleep(500);
			assertEquals(base + 2, windows());
			pressBack();
			assertEquals("one Back closes one dialog", base + 1, windows());
			assertTrue(Game.scene() instanceof TitleScene);
			pressBack();
			assertEquals("the next Back closes the one under it", base, windows());
			assertTrue("the title screen is still there", Game.scene() instanceof TitleScene);
			Log.w(TAG, "Back closed two dialogs one at a time");

			//2. Enter UIMSD directly from the title because this test covers scene navigation, not the private-build demo unlock gate;
			//a tour on its sample history; Back ends the tour first, the next Back returns to the title
			onRender(new Runnable(){ public void run(){ UimsdScene.openNow(TitleScene.class); } });
			waitUntil("the UIMSD scene", new java.util.concurrent.Callable<Boolean>(){ public Boolean call(){ return Game.scene() instanceof UimsdScene; } }, 30000);
			Thread.sleep(2500);
			final Method begin = UimsdScene.class.getDeclaredMethod("beginTour", int.class, boolean.class);
			begin.setAccessible(true);
			final Method backDuringPreparation = UimsdScene.class.getDeclaredMethod("onBackPressed");
			backDuringPreparation.setAccessible(true);

			//Back must cancel the loading phase itself. The generation increment is the
			//permanent guard that prevents the still-running worker from publishing later.
			onRender(new Runnable(){
				public void run(){
					try {
						begin.invoke(Game.scene(), 0, false);
						assertTrue("tour entered its preparation phase", (Boolean) uimsd("tutorialPreparing"));
						long startedGeneration = (Long) uimsd("tourGeneration");
						backDuringPreparation.invoke(Game.scene());
						assertTrue("Back ends tour preparation immediately", !((Boolean) uimsd("tutorialPreparing")));
						assertNull("cancelled preparation has no tutorial", uimsd("tutorial"));
						assertEquals("cancelled preparation restores the player's model", uimsd("mainModel"), uimsd("model"));
						assertTrue("cancelled preparation returns to Home", (Boolean) uimsd("atHome"));
						assertTrue("Back invalidates the preparing worker", (Long) uimsd("tourGeneration") > startedGeneration);
					} catch (Exception e){
						throw new RuntimeException(e);
					}
				}
			});
			Thread.sleep(500);
			assertNull("invalidated worker never publishes after Back", uimsd("tutorial"));
			assertTrue("UIMSD stays open after cancelling preparation", Game.scene() instanceof UimsdScene);
			Log.w(TAG, "Back cancelled UIMSD tour preparation and invalidated its worker");
			onRender(new Runnable(){
				public void run(){
					try {
						begin.invoke(Game.scene(), 0, false);
					} catch (Exception e){
						throw new RuntimeException(e);
					}
				}
			});
			waitUntil("the tour", new java.util.concurrent.Callable<Boolean>(){
				public Boolean call() throws Exception { return uimsd("tutorial") != null && !((Boolean) uimsd("tutorialPreparing")); } }, 60000);
			Thread.sleep(1500);

			//2b. dialogs of the question builder, one over the other (built on the tour's sample history)
			final UimsdModel m = (UimsdModel) uimsd("model");
			assertNotNull(m);
			final int beforeAsk = windows();
			onRender(new Runnable(){
				public void run(){
					Catalog cat = m.catalog();
					WndUimsdAsk.Host host = new WndUimsdAsk.Host(){
						public void openAsTable( Query q, Runnable back ){}
					};
					Question q = Question.about(Questions.askable(cat).get(0).def.id);
					WndUimsdAsk.builder(cat, m.currentRuleset(), host, q);
					WndUimsdAsk.answer(cat, m.currentRuleset(), host, q);
				}
			});
			Thread.sleep(500);
			assertEquals("the answer is over the builder", beforeAsk + 2, windows());
			pressBack();
			assertEquals("Back from the answer returns to the builder", beforeAsk + 1, windows());
			pressBack();
			assertEquals("Back from the builder closes it", beforeAsk, windows());
			assertTrue(Game.scene() instanceof UimsdScene);
			Log.w(TAG, "Back walked back through the question builder");

			//a dialog the tour has open closes first (newest first); the tour itself ends on the Back after those, and the screen stays
			int presses = 0, open = windows();
			Log.w(TAG, "before ending the tour: " + open + " window(s), tutorial=" + uimsd("tutorial") + ", preparing=" + uimsd("tutorialPreparing"));
			while (uimsd("tutorial") != null && presses <= open){
				pressBack();
				presses++;
				Log.w(TAG, "after Back " + presses + ": scene=" + (Game.scene() == null ? "none" : Game.scene().getClass().getSimpleName()) + ", windows=" + windows()
						+ ", tutorial=" + (uimsd("tutorial") != null) + ", atHome=" + uimsd("atHome"));
				assertTrue("Back ends the tour first (press " + presses + ")", Game.scene() instanceof UimsdScene);
			}
			Log.w(TAG, "the tour ended after " + presses + " Back press(es); windows now " + windows());
			assertNull("the tour is over", uimsd("tutorial"));
			pressBack();
			waitUntil("the title again", new java.util.concurrent.Callable<Boolean>(){ public Boolean call(){ return Game.scene() instanceof TitleScene; } }, 15000);
			Log.w(TAG, "Back left the tour, then UIMSD, one step each");
		}
	}
}

package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Instrumentation;
import android.util.Log;
import android.view.KeyEvent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdModel;
import com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.UimsdScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

//Two things the Back and Privacy work needs on a real device: the character-selection screen answers the system Back key (a dialog first,
//then the screen goes back to the title, never out of the app), and the Privacy menu in the UIMSD options works end to end with sending OFF
//by default. The Privacy menu is driven through its own list picker, which is the code a tap on a row runs.
@RunWith(AndroidJUnit4.class)
public class PrivacyAndHeroBackDeviceTest {

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

	private static void pressBack() throws Exception {
		Instrumentation in = InstrumentationRegistry.getInstrumentation();
		in.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
		Thread.sleep(600);
	}

	@SuppressWarnings("unchecked")
	private static List<Gizmo> membersOf( Group g ) throws Exception {
		Field f = Group.class.getDeclaredField("members");
		f.setAccessible(true);
		List<Gizmo> list = (List<Gizmo>) f.get(g);
		return list == null ? new ArrayList<Gizmo>() : new ArrayList<>(list);
	}

	private static List<Window> windows() throws Exception {
		List<Window> out = new ArrayList<>();
		for (Gizmo g : membersOf(Game.scene())) if (g instanceof Window && ((Window) g).alive) out.add((Window) g);
		return out;
	}

	private static Window top() throws Exception {
		List<Window> w = windows();
		return w.isEmpty() ? null : w.get(w.size() - 1);
	}

	private static void start() throws Exception {
		long end = System.currentTimeMillis() + 120000;
		while ((Gdx.app == null || Gdx.files == null || Game.scene() == null) && System.currentTimeMillis() < end) Thread.sleep(250);
		assertNotNull("the game did not start", Game.scene());
		waitUntil("the title scene", new java.util.concurrent.Callable<Boolean>(){
			public Boolean call(){ return Game.scene() instanceof TitleScene; } }, 120000);
		Thread.sleep(2500);
	}

	//what a tap on row 'index' of the list on top does: the list's own picker runs
	private static void pickItem( final int index ) throws Exception {
		final Window w = top();
		assertTrue("a list is on top, but it is " + (w == null ? "nothing" : w.getClass().getSimpleName()), w instanceof WndUimsdList);
		Field f = WndUimsdList.class.getDeclaredField("picker");
		f.setAccessible(true);
		final WndUimsdList.Picker p = (WndUimsdList.Picker) f.get(w);
		onRender(new Runnable(){ public void run(){ p.onPick(index, false); } });
		Thread.sleep(500);
	}

	private static Object uimsdModel() throws Exception {
		Field f = UimsdScene.class.getDeclaredField("model");
		f.setAccessible(true);
		return f.get(null);
	}

	@Test
	public void characterSelectionAnswersBackOneStepAtATime() throws Exception {
		try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)){
			start();
			boolean introWas = SPDSettings.intro();
			SPDSettings.intro(false);
			try {
				onRender(new Runnable(){ public void run(){ ShatteredPixelDungeon.switchNoFade(HeroSelectScene.class); } });
				waitUntil("the character screen", new java.util.concurrent.Callable<Boolean>(){
					public Boolean call(){ return Game.scene() instanceof HeroSelectScene; } }, 30000);
				Thread.sleep(2000);

				//a dialog over the screen: Back closes the dialog and the screen stays
				final int base = windows().size();
				onRender(new Runnable(){ public void run(){ ShatteredPixelDungeon.scene().addToFront(new WndMessage("Back test")); } });
				Thread.sleep(400);
				assertEquals(base + 1, windows().size());
				pressBack();
				assertEquals("Back closed the dialog", base, windows().size());
				assertTrue("and the character screen is still there", Game.scene() instanceof HeroSelectScene);

				//with a hero picked, Back goes back (at most two presses) and ends on the title, never outside the game
				final Method pickHero = HeroSelectScene.class.getDeclaredMethod("setSelectedHero", HeroClass.class);
				pickHero.setAccessible(true);
				onRender(new Runnable(){
					public void run(){
						try {
							pickHero.invoke(Game.scene(), HeroClass.WARRIOR);
						} catch (Exception e){
							throw new RuntimeException(e);
						}
					}
				});
				Thread.sleep(800);
				int presses = 0;
				while (!(Game.scene() instanceof TitleScene) && presses < 3){
					pressBack();
					presses++;
					Thread.sleep(1500);
					Log.w(TAG, "hero select Back press " + presses + " -> " + Game.scene().getClass().getSimpleName());
				}
				assertTrue("Back from the character screen reaches the title (after " + presses + ")", Game.scene() instanceof TitleScene);
				assertTrue("in at most two presses", presses <= 2);
			} finally {
				SPDSettings.intro(introWas);
			}
		}
	}

	@Test
	public void thePrivacyMenuWorksAndSendingIsOffByDefault() throws Exception {
		boolean sendingWas = SPDSettings.apdSending();
		String serverWas = SPDSettings.apdServer();
		try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)){
			start();
			SPDSettings.apdSending(false);
			SPDSettings.apdServer("");
			onRender(new Runnable(){ public void run(){ UimsdScene.open(TitleScene.class); } });
			waitUntil("the UIMSD scene", new java.util.concurrent.Callable<Boolean>(){ public Boolean call(){ return Game.scene() instanceof UimsdScene; } }, 30000);
			Thread.sleep(2500);
			waitUntil("the model", new java.util.concurrent.Callable<Boolean>(){ public Boolean call() throws Exception { return uimsdModel() != null; } }, 60000);
			final UimsdModel m = (UimsdModel) uimsdModel();

			//the Options window itself builds, with the Privacy entry in it
			final int base = windows().size();
			onRender(new Runnable(){ public void run(){ WndUimsdOptions.show(m); } });
			Thread.sleep(500);
			assertTrue("the options window is up", windows().size() > base);
			pressBack();
			assertEquals("Back closes it", base, windows().size());

			//the Privacy menu
			final Method privacy = WndUimsdOptions.class.getDeclaredMethod("privacy", UimsdModel.class);
			privacy.setAccessible(true);
			onRender(new Runnable(){
				public void run(){
					try {
						privacy.invoke(null, m);
					} catch (Exception e){
						throw new RuntimeException(e);
					}
				}
			});
			Thread.sleep(500);
			assertFalse("sending starts OFF", SPDSettings.apdSending());

			int open = windows().size();
			pickItem(0);   //what stays on this device
			assertEquals("a message about what stays on the device opened", open + 1, windows().size());
			assertTrue(top() instanceof WndMessage);
			pressBack();
			assertTrue("Back returns to the Privacy list", top() instanceof WndUimsdList);

			pickItem(1);   //switch sending on
			assertTrue("sending is now on", SPDSettings.apdSending());
			assertTrue(top() instanceof WndMessage);
			pressBack();
			pickItem(3);   //send now: on, but no address, so nothing is sent
			assertTrue("a message says there is no usable address", top() instanceof WndMessage);
			pressBack();
			pickItem(2);   //the address box
			assertTrue("the address box opened", top() instanceof WndTextInput);
			pressBack();
			assertEquals("an empty address was not invented", "", SPDSettings.apdServer());
			pickItem(4);   //evidence requests: none waiting
			assertTrue("a message says nothing is waiting", top() instanceof WndMessage);
			pressBack();
			pickItem(1);   //back off
			assertFalse("sending is off again", SPDSettings.apdSending());
			Log.w(TAG, "Privacy menu: summary, switch on/off, send-now refusal and address box all worked");
		} finally {
			SPDSettings.apdSending(sendingWas);
			SPDSettings.apdServer(serverWas);
		}
	}
}

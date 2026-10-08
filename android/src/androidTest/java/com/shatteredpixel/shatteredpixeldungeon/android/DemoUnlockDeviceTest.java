package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.apd.demo.DemoData;
import com.shatteredpixel.shatteredpixeldungeon.apd.demo.UimsdUnlock;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdRecorder;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.UimsdScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.UimsdGate;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

//The Demo Unlock on a real device, with the gate switched on for the test: UIMSD is locked, the warning appears, "I LIKE CONFUSION" makes the
//ten demo runs and opens UIMSD, the unlock and the used-up button are permanent, and deleting the demo data changes neither. The player's own
//settings and history are put back afterwards.
@RunWith(AndroidJUnit4.class)
public class DemoUnlockDeviceTest {

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

	@SuppressWarnings("unchecked")
	private static List<Gizmo> membersOf( Group g ) throws Exception {
		Field f = Group.class.getDeclaredField("members");
		f.setAccessible(true);
		List<Gizmo> list = (List<Gizmo>) f.get(g);
		return list == null ? new ArrayList<Gizmo>() : new ArrayList<>(list);
	}

	private static Window top() throws Exception {
		Window w = null;
		for (Gizmo g : membersOf(Game.scene())) if (g instanceof Window && ((Window) g).alive) w = (Window) g;
		return w;
	}

	private static void choose( final Window w, final int index ) throws Exception {
		assertTrue("a choice window is on top", w instanceof WndOptions);
		final Method m = WndOptions.class.getDeclaredMethod("onSelect", int.class);
		m.setAccessible(true);
		onRender(new Runnable(){
			public void run(){
				try {
					m.invoke(w, index);
				} catch (Exception e){
					throw new RuntimeException(e);
				}
			}
		});
		Thread.sleep(400);
	}

	//the two permanent flags as the game keeps them
	private static UimsdUnlock.Flags settingsFlags(){
		return new UimsdUnlock.Flags(){
			public boolean unlocked(){ return SPDSettings.apdUimsdUnlocked(); }
			public void setUnlocked(){ SPDSettings.apdUimsdUnlocked(true); }
			public boolean demoUsed(){ return SPDSettings.apdDemoUnlockUsed(); }
			public void setDemoUsed(){ SPDSettings.apdDemoUnlockUsed(true); }
		};
	}

	@Test
	public void theDemoUnlockMakesTenDemoRunsOpensUimsdForGoodAndItsButtonIsUsedUp() throws Exception {
		boolean unlockedWas = SPDSettings.apdUimsdUnlocked();
		boolean usedWas = SPDSettings.apdDemoUnlockUsed();
		try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)){
			long end = System.currentTimeMillis() + 120000;
			while ((Gdx.app == null || Gdx.files == null || Game.scene() == null) && System.currentTimeMillis() < end) Thread.sleep(250);
			assertNotNull("the game did not start", Game.scene());
			long titleBy = System.currentTimeMillis() + 45000;
			while (!(Game.scene() instanceof TitleScene) && System.currentTimeMillis() < titleBy) Thread.sleep(250);
			if (!(Game.scene() instanceof TitleScene)){
				onRender(new Runnable(){ public void run(){ com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.switchNoFade(TitleScene.class); } });
				waitUntil("the title scene", new java.util.concurrent.Callable<Boolean>(){ public Boolean call(){ return Game.scene() instanceof TitleScene; } }, 60000);
			}
			Thread.sleep(2500);

			final File real = ApdRecorder.historyRoot();
			assertNotNull(real);
			final File demo = DemoData.demoRootFor(real);
			DemoData.delete(demo);
			SPDSettings.apdUimsdUnlocked(false);
			SPDSettings.apdDemoUnlockUsed(false);
			UimsdGate.forceGateOnForTest(true);

			//locked: asking for UIMSD shows the locked window with the demo route
			onRender(new Runnable(){ public void run(){ UimsdScene.open(TitleScene.class); } });
			Thread.sleep(600);
			Window locked = top();
			assertTrue("UIMSD asked before opening", locked instanceof WndOptions);
			assertFalse("and did not open", Game.scene() instanceof UimsdScene);
			choose(locked, 1);   //Unlock it now with demo runs...
			Window warning = top();
			assertTrue("the warning is up", warning instanceof WndOptions && warning != locked);
			//declining first: nothing is made and nothing is used up
			choose(warning, 1);   //I'LL PLAY 10 RUNS
			assertFalse(demo.exists());
			assertFalse(SPDSettings.apdUimsdUnlocked() || SPDSettings.apdDemoUnlockUsed());
			assertFalse(Game.scene() instanceof UimsdScene);
			Log.w(TAG, "demo unlock: declining changed nothing");

			//now accept
			onRender(new Runnable(){ public void run(){ UimsdScene.open(TitleScene.class); } });
			Thread.sleep(600);
			choose(top(), 1);
			choose(top(), 0);   //I LIKE CONFUSION
			waitUntil("UIMSD to open on the demo data", new java.util.concurrent.Callable<Boolean>(){ public Boolean call(){ return Game.scene() instanceof UimsdScene; } }, 60000);
			assertTrue("the ten demo runs are in their own marked folder", DemoData.isSyntheticRoot(demo));
			File[] logs = new File(demo, "runs").listFiles();
			int n = 0;
			for (File f : logs) if (f.getName().endsWith(".apdlog")) n++;
			assertTrue("ten demo runs plus the second part of one that was split in two (" + n + " logs)", n >= UimsdUnlock.DEMO_RUNS);
			assertTrue(SPDSettings.apdUimsdUnlocked());
			assertTrue(SPDSettings.apdDemoUnlockUsed());
			Log.w(TAG, "demo unlock: " + n + " demo logs made, UIMSD opened and the unlock is recorded");

			//delete the demo data: UIMSD stays unlocked and the button stays gone
			assertTrue(UimsdUnlock.deleteDemoData(real));
			assertFalse(demo.exists());
			assertTrue(SPDSettings.apdUimsdUnlocked());
			assertTrue(SPDSettings.apdDemoUnlockUsed());
			assertFalse("the button is not offered again", UimsdUnlock.demoButtonAvailable(settingsFlags(), real, true));
			Log.w(TAG, "demo unlock: deleting the demo data left the unlock and the used-up button as they were");
		} finally {
			UimsdGate.forceGateOnForTest(false);
			DemoData.viewDemo(false);
			SPDSettings.apdUimsdUnlocked(unlockedWas);
			SPDSettings.apdDemoUnlockUsed(usedWas);
			try {
				File real = ApdRecorder.historyRoot();
				if (real != null) DemoData.delete(DemoData.demoRootFor(real));
			} catch (Throwable ignored){
			}
		}
	}
}

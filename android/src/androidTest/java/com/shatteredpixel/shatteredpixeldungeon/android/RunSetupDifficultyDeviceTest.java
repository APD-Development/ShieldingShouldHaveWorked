package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.graphics.Point;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.DifficultyBar;
import com.shatteredpixel.shatteredpixeldungeon.windows.APDSettingsPane;
import com.watabou.input.PointerEvent;
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

//Difficulty belongs to the run being set up, not to the app's settings: the character screen shows 1 to 5 right above Start, a touch on one
//sets what the NEXT run starts with, and APD Settings no longer has the control. Real touches are sent through the game's own pointer queue.
@RunWith(AndroidJUnit4.class)
public class RunSetupDifficultyDeviceTest {

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

	private static <T> T find( Gizmo g, Class<T> type ) throws Exception {
		if (type.isInstance(g)) return type.cast(g);
		if (g instanceof Group){
			for (Gizmo m : membersOf((Group) g)){
				T t = find(m, type);
				if (t != null) return t;
			}
		}
		return null;
	}

	private static void tap( Point at ) throws Exception {
		PointerEvent.addPointerEvent(new PointerEvent(at.x, at.y, 9, PointerEvent.Type.DOWN, PointerEvent.LEFT));
		Thread.sleep(150);
		PointerEvent.addPointerEvent(new PointerEvent(at.x, at.y, 9, PointerEvent.Type.UP, PointerEvent.LEFT));
		Thread.sleep(500);
	}

	@Test
	public void difficultyIsChosenOnTheCharacterScreenAndNotInSettings() throws Exception {
		int before = SPDSettings.apdDifficulty();
		try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)){
			long end = System.currentTimeMillis() + 120000;
			while ((Gdx.app == null || Gdx.files == null || Game.scene() == null) && System.currentTimeMillis() < end) Thread.sleep(250);
			assertNotNull("the game did not start", Game.scene());
			waitUntil("the title scene", new java.util.concurrent.Callable<Boolean>(){
				public Boolean call(){ return Game.scene() instanceof TitleScene; } }, 120000);

			//APD Settings no longer has a difficulty control
			try {
				APDSettingsPane.class.getDeclaredField("optDifficulty");
				fail("APD Settings must not have a difficulty slider any more");
			} catch (NoSuchFieldException expected){
				//good
			}

			//the character screen, with a hero picked
			SPDSettings.intro(false);
			onRender(new Runnable(){ public void run(){ ShatteredPixelDungeon.switchNoFade(HeroSelectScene.class); } });
			waitUntil("the character screen", new java.util.concurrent.Callable<Boolean>(){
				public Boolean call(){ return Game.scene() instanceof HeroSelectScene; } }, 30000);
			Thread.sleep(2000);
			final Method pick = HeroSelectScene.class.getDeclaredMethod("setSelectedHero", HeroClass.class);
			pick.setAccessible(true);
			onRender(new Runnable(){
				public void run(){
					try {
						pick.invoke(Game.scene(), HeroClass.WARRIOR);
					} catch (Exception e){
						throw new RuntimeException(e);
					}
				}
			});
			Thread.sleep(1000);
			final DifficultyBar bar = find(Game.scene(), DifficultyBar.class);
			assertNotNull("the difficulty bar is on the character screen", bar);
			assertTrue("and it is visible", bar.visible);

			//touch each level for real; the setting follows, and the label says what it is
			int[] levels = { 4, 1, 5, 3 };
			for (final int level : levels){
				final AtomicReference<Point> centre = new AtomicReference<>();
				onRender(new Runnable(){
					public void run(){
						try {
							Field f = DifficultyBar.class.getDeclaredField("segments");
							f.setAccessible(true);
							Object[] segs = (Object[]) f.get(bar);
							com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton b = (com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton) segs[level - 1];
							centre.set(b.camera().cameraToScreen(b.left() + b.width() / 2f, b.top() + b.height() / 2f));
						} catch (Exception e){
							throw new RuntimeException(e);
						}
					}
				});
				tap(centre.get());
				assertEquals("a touch on " + level + " chose difficulty " + level, level, SPDSettings.apdDifficulty());
				Log.w(TAG, "difficulty " + level + " chosen on the character screen");
			}
			assertEquals("3 is the normal game", "Normal", DifficultyBar.nameOf(3));
		} finally {
			SPDSettings.apdDifficulty(before);
		}
	}
}

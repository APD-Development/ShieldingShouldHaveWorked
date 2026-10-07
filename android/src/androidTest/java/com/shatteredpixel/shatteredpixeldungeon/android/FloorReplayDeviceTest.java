package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.apd.replay.FloorReplay;
import com.shatteredpixel.shatteredpixeldungeon.apd.replay.ReplayFloor;
import com.watabou.noosa.Game;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

//Phase A (floor replay) on a real device runtime: the game engine is really running (the app is launched and sits on its title screen,
//so no game is live), and a replay of all 26 floors must work, be repeatable, and fit a generous time budget. The numbers go to logcat
//(tag APDPERF).
@RunWith(AndroidJUnit4.class)
public class FloorReplayDeviceTest {

	private static final String TAG = "APDPERF";

	private static String signature( List<ReplayFloor> floors ){
		StringBuilder sb = new StringBuilder();
		for (ReplayFloor f : floors){
			sb.append(f.depth).append(f.levelClass).append(f.width).append('x').append(f.height).append(f.feeling)
					.append(f.rooms.size()).append(f.items.size()).append(f.mobs.size());
			for (ReplayFloor.Item i : f.items) sb.append(i.trueClass).append(i.level).append(i.pos).append(i.container).append(i.quantity);
			for (ReplayFloor.Mob m : f.mobs) sb.append(m.mobClass).append(m.pos);
			sb.append('|');
		}
		return sb.toString();
	}

	@Test
	public void floorsCanBeReplayedOnTheDevice() throws Exception {
		try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)){
			long end = System.currentTimeMillis() + 120000;
			while ((Gdx.app == null || Gdx.files == null || Game.scene() == null) && System.currentTimeMillis() < end){
				Thread.sleep(250);
			}
			assertNotNull("the game did not start", Game.scene());
			com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdRecorder.reset();
			assertNull("no game should be live on the title screen", FloorReplay.refusal());

			long t0 = System.nanoTime();
			List<ReplayFloor> a = FloorReplay.replay(424242L, 0, HeroClass.WARRIOR, FloorReplay.LAST_FLOOR);
			long t1 = System.nanoTime();
			List<ReplayFloor> b = FloorReplay.replay(424242L, 0, HeroClass.WARRIOR, FloorReplay.LAST_FLOOR);
			List<ReplayFloor> other = FloorReplay.replay(424243L, 3, HeroClass.MAGE, FloorReplay.LAST_FLOOR);
			long ms = (t1 - t0) / 1000000;
			Log.w(TAG, "floor replay of 26 floors took " + ms + " ms (" + signature(a).length() + " chars of floor data)");

			assertEquals(26, a.size());
			assertEquals("a replay must be repeatable on the device", signature(a), signature(b));
			assertTrue("different seeds must differ", !signature(a).equals(signature(other)));
			assertEquals("levels.SewerLevel", a.get(0).levelClass);
			assertEquals("levels.LastLevel", a.get(25).levelClass);
			assertTrue("replay too slow: " + ms + " ms", ms < 60000);
			assertNull("the replay must leave the lock free", FloorReplay.refusal());

			//repeatability across more seeds and classes (object-identity ordering inside the game differs from run to run on a device)
			long[] seeds = { 7L, 99999L, 123456789L, 5550123L };
			HeroClass[] classes = { HeroClass.ROGUE, HeroClass.HUNTRESS, HeroClass.DUELIST, HeroClass.CLERIC };
			for (int i = 0; i < seeds.length; i++){
				String first = signature(FloorReplay.replay(seeds[i], 0, classes[i], FloorReplay.LAST_FLOOR));
				String second = signature(FloorReplay.replay(seeds[i], 0, classes[i], FloorReplay.LAST_FLOOR));
				assertEquals("replay of seed " + seeds[i] + " not repeatable", first, second);
			}
		}
	}
}

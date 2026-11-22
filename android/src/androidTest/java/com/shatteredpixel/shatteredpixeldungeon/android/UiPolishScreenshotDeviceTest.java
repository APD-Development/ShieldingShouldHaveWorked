package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.*;
import android.graphics.Bitmap;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CommunityScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndCustomDungeonRules;
import com.watabou.noosa.Game;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/** UI polish (Chris, Nov 22): renders the polished Community and Custom Rules screens and saves screenshots CI uploads for review. */
@RunWith(AndroidJUnit4.class)
public class UiPolishScreenshotDeviceTest {
	private static void render(Runnable task) throws Exception {
		CountDownLatch done = new CountDownLatch(1); AtomicReference<Throwable> failure = new AtomicReference<>();
		Game.runOnRenderThread(() -> { try { task.run(); } catch (Throwable t) { failure.set(t); } finally { done.countDown(); } });
		assertTrue(done.await(60, TimeUnit.SECONDS)); if (failure.get() != null) throw new AssertionError(failure.get());
	}
	private static void shot(String name) throws Exception {
		Thread.sleep(900);
		Bitmap b = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot(); assertNotNull(b);
		File dir = new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null), "apd-shots");
		assertTrue(dir.isDirectory() || dir.mkdirs());
		try (FileOutputStream out = new FileOutputStream(new File(dir, name))) { assertTrue(b.compress(Bitmap.CompressFormat.PNG, 100, out)); }
		finally { b.recycle(); }
	}
	private static void waitScene(Class<?> type) throws Exception {
		long until = System.currentTimeMillis() + 30000;
		while ((!type.isInstance(Game.scene()) || Game.switchingScene()) && System.currentTimeMillis() < until) Thread.sleep(200);
		assertTrue(type.getSimpleName(), type.isInstance(Game.scene()));
	}

	@Test public void polishedScreens() throws Exception {
		try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)) {
			long deadline = System.currentTimeMillis() + 120000;
			while ((Game.scene() == null || PixelScene.uiCamera == null) && System.currentTimeMillis() < deadline) Thread.sleep(200);
			Thread.sleep(1500);
			render(CommunityScene::open);
			waitScene(CommunityScene.class);
			Thread.sleep(2500);
			shot("community-hub.png");
			render(() -> ShatteredPixelDungeon.switchNoFade(TitleScene.class));
			waitScene(TitleScene.class);
			final WndCustomDungeonRules[] w = new WndCustomDungeonRules[1];
			String[] names = {"rules", "presets", "scoring"};
			for (int t = 0; t < 3; t++) {
				final int tab = t;
				render(() -> { if (w[0] == null) { w[0] = new WndCustomDungeonRules(); Game.scene().addToFront(w[0]); } w[0].showTab(tab); });
				shot("custom-rules-" + names[t] + ".png");
			}
			render(() -> { w[0].hide(); w[0] = new WndCustomDungeonRules(true); Game.scene().addToFront(w[0]); });
			shot("custom-rules-locked.png");
			render(() -> w[0].hide());
		}
	}
}

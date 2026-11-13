package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.os.Environment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.DifficultyBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.watabou.noosa.Game;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

// Native assertion and retained screenshots for the owner-reported hero-select difficulty fade.
// This checks the actual constructed label and all five buttons, not only source-text wiring.
@RunWith(AndroidJUnit4.class)
public class HeroSelectDifficultyFadeDeviceTest {

    private static void onRender(Runnable action) throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> problem = new AtomicReference<>();
        Game.runOnRenderThread(new com.watabou.utils.Callback() {
            @Override public void call() {
                try { action.run(); } catch (Throwable error) { problem.set(error); }
                finally { done.countDown(); }
            }
        });
        assertTrue("render thread did not respond", done.await(60, TimeUnit.SECONDS));
        if (problem.get() != null) throw new AssertionError("render-thread failure", problem.get());
    }

    private static void waitForHeroScene() throws Exception {
        long end = System.currentTimeMillis() + 30000;
        while (!(Game.scene() instanceof HeroSelectScene) && System.currentTimeMillis() < end) {
            Thread.sleep(100);
        }
        assertTrue("hero-select scene did not start", Game.scene() instanceof HeroSelectScene);
    }

    private static void assertInputs(DifficultyBar bar, boolean enabled, float expectedAlpha) throws Exception {
        assertTrue("selected difficulty control stays in the layout", bar.visible);
        assertEquals("difficulty composite input", enabled, bar.active);
        Field labelField = DifficultyBar.class.getDeclaredField("label");
        labelField.setAccessible(true);
        StyledButton label = (StyledButton) labelField.get(bar);
        assertNotNull("difficulty explanation label", label);
        assertEquals("difficulty explanation input", enabled, label.active);
        assertEquals("difficulty explanation opacity", expectedAlpha, label.alpha(), 0.01f);
        Field segmentsField = DifficultyBar.class.getDeclaredField("segments");
        segmentsField.setAccessible(true);
        DifficultyBar.Segment[] segments = (DifficultyBar.Segment[]) segmentsField.get(bar);
        assertEquals("all five difficulty levels", 5, segments.length);
        for (int i = 0; i < segments.length; i++) {
            assertEquals("difficulty " + (i + 1) + " input", enabled, segments[i].active);
            assertEquals("difficulty " + (i + 1) + " opacity", expectedAlpha, segments[i].alpha(), 0.01f);
        }
    }

    private static void setFade(float alpha, boolean enabled) throws Exception {
        onRender(() -> {
            try {
                HeroSelectScene scene = (HeroSelectScene) Game.scene();
                Field uiAlpha = HeroSelectScene.class.getDeclaredField("uiAlpha");
                uiAlpha.setAccessible(true);
                uiAlpha.setFloat(scene, alpha);
                Method updateFade = HeroSelectScene.class.getDeclaredMethod("updateFade");
                updateFade.setAccessible(true);
                updateFade.invoke(scene);
                Field difficultyField = HeroSelectScene.class.getDeclaredField("difficultyBar");
                difficultyField.setAccessible(true);
                assertInputs((DifficultyBar) difficultyField.get(scene), enabled,
                        Math.max(0f, Math.min(1f, alpha)));
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    private static void capture(String phase) throws Exception {
        Thread.sleep(350); // Wait for the actual rendered frame after the requested alpha.
        Bitmap screenshot = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        assertNotNull("real emulator screenshot for " + phase, screenshot);
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "APD-Hero-Fade");
        assertTrue("create the evidence directory", directory.isDirectory() || directory.mkdirs());
        try (FileOutputStream output = new FileOutputStream(new File(directory, phase + ".png"))) {
            assertTrue("save the " + phase + " screenshot", screenshot.compress(Bitmap.CompressFormat.PNG, 100, output));
        } finally { screenshot.recycle(); }
    }

    @Test
    public void difficultyLabelAndAllFiveSegmentsFollowFadeAndRestore() throws Exception {
        // Preferences need the live Gdx application; ActivityScenario must initialize it first.
        try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)) {
            long end = System.currentTimeMillis() + 120000;
            while ((Gdx.app == null || Game.scene() == null) && System.currentTimeMillis() < end) Thread.sleep(200);
            assertNotNull("actual game has launched", Gdx.app);
            assertNotNull("actual game scene has started", Game.scene());

            final int originalDifficulty = SPDSettings.apdDifficulty();
            final boolean originalIntro = SPDSettings.intro();
            final HeroClass originalClass = GamesInProgress.selectedClass;
            try {
                onRender(() -> {
                    SPDSettings.intro(false);
                    ShatteredPixelDungeon.switchNoFade(HeroSelectScene.class);
                });
                waitForHeroScene();
                onRender(() -> {
                    try {
                        Method pick = HeroSelectScene.class.getDeclaredMethod("setSelectedHero", HeroClass.class);
                        pick.setAccessible(true);
                        pick.invoke(Game.scene(), HeroClass.WARRIOR);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });
                setFade(2f, true);
                capture("fully-visible");
                setFade(0.5f, true);
                capture("half-faded");
                setFade(0f, false);
                capture("fully-faded");
                setFade(2f, true);
                capture("restored");
                assertEquals("no change to the next run's difficulty", originalDifficulty, SPDSettings.apdDifficulty());
            } finally {
                // Restore while the launcher still owns Gdx.app, not after ActivityScenario closes.
                onRender(() -> {
                    SPDSettings.apdDifficulty(originalDifficulty);
                    SPDSettings.intro(originalIntro);
                    GamesInProgress.selectedClass = originalClass;
                });
            }
        }
    }
}

/* ADHD Pixel Dungeon (C) 2026 Chris Charlton, GPL-3.0-or-later */
package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.*;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.apd.PlaybackGroup;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Visual;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.utils.PointF;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

/**
 * Real Android GameScene proof of the owner's movement-only playback rule.
 * This is a disposable new run. It never changes an existing player save.
 */
@RunWith(AndroidJUnit4.class)
public class MovementOnlyPlaybackDeviceTest {

    // Includes character sprite/attack, every effect group, wells, gas, status and terrain.
    // No GameScene Group may scale the global frame delta.
    private static final String[] NORMAL_LAYERS = {
            "terrain", "ripples", "customTiles", "customTerrain", "floorEmitters",
            "heaps", "emitters", "effects", "healthIndicators", "emoicons",
            "overFogEffects", "mobs", "customWalls", "gases", "spells",
            "checkedCells", "targetedCells", "statuses"
    };
    private static final float[] SPEEDS = {
            .5f, 1f, 2f, 4f, 8f, 16f, 32f, 64f, 100f, 1000f
    };

    @Test
    public void actualDungeonNeverAcceleratesNonMovementAnimationGroups() throws Exception {
        try (ActivityScenario<AndroidLauncher> app = ActivityScenario.launch(AndroidLauncher.class)) {
            SearchContextDeviceTest.waitFor(() -> Game.scene() != null && PixelScene.uiCamera != null);
            try (SearchContextDeviceTest.OwnedFixture fixture =
                         new SearchContextDeviceTest.OwnedFixture("movement-only-playback")) {
                fixture.start(HeroClass.WARRIOR, false);
                SearchContextDeviceTest.readyFrame();
                final int[] previousSelection = {-1};
                SearchContextDeviceTest.render(() -> {
                    previousSelection[0] = SPDSettings.playbackSpeed();
                    assertTrue("Real new dungeon exists", Game.scene() instanceof GameScene);
                    assertNotNull("Real hero exists", Dungeon.hero);
                    GameScene scene = (GameScene) Game.scene();
                    for (String fieldName : NORMAL_LAYERS) {
                        Field field = GameScene.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        Object layer = field.get(scene);
                        assertTrue(fieldName + " must remain a real group", layer instanceof Group);
                        assertFalse(fieldName + " must not inherit accelerated playback",
                                layer instanceof PlaybackGroup);
                    }
                    for (int i = 0; i < SPEEDS.length; i++) {
                        PlaybackGroup.speed(i);
                        assertEquals("Walking speed at mode " + i,
                                .1f / SPEEDS[i],
                                PlaybackGroup.movementDuration(.1f), .000001f);
                        // The actual movement implementation constructs a PosTweener
                        // using this duration; effects and enemy attacks are in 1x layers.
                        PosTweener movement = new PosTweener(
                                new Visual(0, 0, 16, 16), new PointF(16, 0),
                                PlaybackGroup.movementDuration(.1f));
                        assertEquals("Tween interval at mode " + i,
                                .1f / SPEEDS[i], movement.interval, .000001f);
                    }
                    PlaybackGroup.speed(previousSelection[0]);
                });
                TopScoresDeviceTest.screenshot("movement-only-real-android-dungeon.png");
                SearchContextDeviceTest.render(() -> {
                    assertEquals("Restore original playback choice",
                            previousSelection[0], SPDSettings.playbackSpeed());
                });
                fixture.assertOriginalFiles();
            }
        }
    }
}

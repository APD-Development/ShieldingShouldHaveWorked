/* ADHD Pixel Dungeon (C) 2026 Chris Charlton, GPL-3.0-or-later */
package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.*;

import android.app.Instrumentation;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.apd.ApdFeatures;
import com.shatteredpixel.shatteredpixeldungeon.apd.PlaybackGroup;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Native Android proof of Developer-only visual layers and unchanged Alpha.
 * Does not claim physical-device subjective motion or a real-time frame capture. */
@RunWith(AndroidJUnit4.class)
public class PlaybackProfileDeviceTest {

    private static Group group(GameScene scene, String name) throws Exception {
        Field f=GameScene.class.getDeclaredField(name);
        f.setAccessible(true);
        return (Group)f.get(scene);
    }

    private static void expectGroup(GameScene scene, String name, boolean sped) throws Exception {
        Group group=group(scene,name);
        assertNotNull(name,group);
        assertEquals(name+(sped ? " must run at selected playback speed" : " must remain 1x"),
                sped ? PlaybackGroup.class : Group.class,group.getClass());
    }

    @Test public void realDungeonKeepsAmbientNormalAndPersonalCombatScopedAtSixteenX() throws Exception {
        final int[] originalSpeed={1};
        AtomicReference<SearchContextDeviceTest.OwnedFixture> owned=new AtomicReference<>();
        try (ActivityScenario<AndroidLauncher> app=ActivityScenario.launch(AndroidLauncher.class)) {
            SearchContextDeviceTest.waitFor(()->Game.scene()!=null&&PixelScene.uiCamera!=null);
            SearchContextDeviceTest.render(()->{
                originalSpeed[0]=SPDSettings.playbackSpeed();
                owned.set(new SearchContextDeviceTest.OwnedFixture("playback"));
            });
            try {
                owned.get().start(HeroClass.WARRIOR,false);
                SearchContextDeviceTest.render(()->SPDSettings.playbackSpeed(5));
                SearchContextDeviceTest.waitFor(()->{
                    AtomicBoolean matches=new AtomicBoolean(false);
                    SearchContextDeviceTest.render(()->matches.set(
                        Game.scene() instanceof GameScene &&
                        Camera.main.visualTimeScale == (ApdFeatures.testBuild()?16f:1f)));
                    return matches.get();
                });
                SearchContextDeviceTest.render(()->{
                    assertNotNull("Actual loaded hero",Dungeon.hero);
                    GameScene scene=(GameScene)Game.scene();
                    boolean personal=ApdFeatures.testBuild();
                    expectGroup(scene,"mobs",false);
                    expectGroup(scene,"terrain",false);
                    expectGroup(scene,"ripples",false);
                    expectGroup(scene,"gases",false);
                    expectGroup(scene,"emoicons",false);
                    expectGroup(scene,"statuses",false);
                    expectGroup(scene,"effects",false);
                    expectGroup(scene,"floorEmitters",false);
                    for(String n:new String[]{"heaps","spells","checkedCells","targetedCells",
                            "healthIndicators","combatEmitters"}) expectGroup(scene,n,personal);
                    if(personal) {
                        expectGroup(scene,"combatStatuses",true);
                        assertNotSame("Combat labels must be separate from 1x statuses",
                                group(scene,"statuses"),group(scene,"combatStatuses"));
                    } else {
                        assertSame("Alpha combat labels preserve their original 1x group",
                                group(scene,"statuses"),group(scene,"combatStatuses"));
                    }
                    assertEquals(personal?16f:1f,PlaybackGroup.personalFactor(),0f);
                });
                TopScoresDeviceTest.screenshot("apd-personal-16x-dungeon-layers.png");
                owned.get().assertOriginalFiles();
            } finally {
                SearchContextDeviceTest.render(()->SPDSettings.playbackSpeed(originalSpeed[0]));
                owned.get().close();
            }
        }
    }
}

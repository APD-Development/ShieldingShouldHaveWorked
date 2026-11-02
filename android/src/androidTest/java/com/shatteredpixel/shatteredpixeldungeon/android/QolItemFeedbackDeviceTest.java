/*
 * ADHD Pixel Dungeon
 * Copyright (C) 2026 Chris Charlton
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.*;
import android.os.SystemClock;
import android.view.*;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.windows.*;
import com.watabou.noosa.Game;
import com.watabou.utils.Point;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Native controls and actual renderer, using a newly created disposable emulator run. */
@RunWith(AndroidJUnit4.class)
public class QolItemFeedbackDeviceTest {
    private static void waitFor(Callable<Boolean> condition)throws Exception{
        long end=SystemClock.uptimeMillis()+120000;
        while(SystemClock.uptimeMillis()<end){if(condition.call())return;Thread.sleep(100);}fail("QoL fixture timed out");
    }
    private static void touch(Point p)throws Exception{
        android.app.Instrumentation in=InstrumentationRegistry.getInstrumentation();long down=SystemClock.uptimeMillis();
        for(int a:new int[]{MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP}){
            MotionEvent e=MotionEvent.obtain(down,SystemClock.uptimeMillis(),a,p.x,p.y,0);e.setSource(InputDevice.SOURCE_TOUCHSCREEN);in.sendPointerSync(e);e.recycle();Thread.sleep(100);
        }
        Thread.sleep(250);
    }
    private static void showItem(Item item,String expected,String image)throws Exception{
        AtomicReference<WndUseItem> shown=new AtomicReference<>();
        TopScoresDeviceTest.render(()->{
            assertTrue(item.info(),item.info().contains(expected));WndUseItem w=new WndUseItem(null,item);GameScene.show(w);shown.set(w);
            try{for(RedButton b:TopScoresDeviceTest.find(w,RedButton.class)){
                Point a=b.camera().cameraToScreen(b.left(),b.top()),z=b.camera().cameraToScreen(b.right(),b.bottom());
                assertTrue("Item action remains visible",a.x>=0&&a.y>=0&&z.x<=Game.width&&z.y<=Game.height);
            }}catch(Exception e){throw new RuntimeException(e);}
        });
        Thread.sleep(300);TopScoresDeviceTest.screenshot(image);TopScoresDeviceTest.render(()->shown.get().hide());
    }
    @Test public void grassDewControlsRingBreakdownsSearchedWallAndOfflineLicenseRenderNatively()throws Exception{
        boolean intro=SPDSettings.intro(),auto=SPDSettings.autoCollectGrassDew(),enemies=SPDSettings.autoCollectDewWithEnemies();
        int slot=GamesInProgress.curSlot;HeroClass selected=GamesInProgress.selectedClass;
        try(ActivityScenario<AndroidLauncher> scenario=ActivityScenario.launch(AndroidLauncher.class)){
            waitFor(()->Game.instance!=null&&Game.scene()!=null);
            TopScoresDeviceTest.render(()->Game.switchScene(TitleScene.class));waitFor(()->Game.scene() instanceof TitleScene);
            TopScoresDeviceTest.render(()->{
                SPDSettings.intro(false);int free=GamesInProgress.firstEmpty(HeroClass.WARRIOR);assertTrue(free>0);
                GamesInProgress.curSlot=free;GamesInProgress.selectedClass=HeroClass.WARRIOR;Dungeon.hero=null;
                Dungeon.daily=false;Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();
                InterlevelScene.mode=InterlevelScene.Mode.DESCEND;Game.switchScene(InterlevelScene.class);
            });
            waitFor(()->Game.scene() instanceof GameScene&&Dungeon.hero!=null&&Dungeon.hero.ready);
            AtomicReference<WndGrassDewSettings> settings=new AtomicReference<>();Point[] p=new Point[1];
            TopScoresDeviceTest.render(()->{try{
                SPDSettings.autoCollectGrassDew(true);SPDSettings.autoCollectDewWithEnemies(false);
                WndGrassDewSettings w=new WndGrassDewSettings();GameScene.show(w);settings.set(w);
                java.util.List<CheckBox> controls=TopScoresDeviceTest.find(w,CheckBox.class);assertEquals(2,controls.size());
                CheckBox enemy=controls.get(1);assertFalse(enemy.checked());p[0]=enemy.camera().cameraToScreen(enemy.left()+enemy.width()/2,enemy.top()+enemy.height()/2);
            }catch(Exception e){throw new RuntimeException(e);}});
            touch(p[0]);TopScoresDeviceTest.render(()->assertTrue(SPDSettings.autoCollectDewWithEnemies()));
            TopScoresDeviceTest.screenshot("qol-dew-settings.png");TopScoresDeviceTest.render(()->settings.get().hide());
            Sword sword=new Sword();sword.levelKnown=true;sword.cursedKnown=true;
            TopScoresDeviceTest.render(()->{RingOfForce r=new RingOfForce();r.anonymize();r.levelKnown=true;r.cursedKnown=true;Dungeon.hero.belongings.ring=r;r.activate(Dungeon.hero);});
            showItem(sword,"Force, base armed damage","qol-force-weapon.png");
            ThrowingKnife knife=new ThrowingKnife();knife.levelKnown=true;knife.cursedKnown=true;
            TopScoresDeviceTest.render(()->{RingOfSharpshooting r=new RingOfSharpshooting();r.anonymize();r.levelKnown=true;r.cursedKnown=true;Dungeon.hero.belongings.misc=r;r.activate(Dungeon.hero);});
            showItem(knife,"2-6 -> 3-8","qol-sharpshooting-weapon.png");
            TopScoresDeviceTest.render(()->{
                int cell=Dungeon.hero.pos+1;assertTrue(Dungeon.level.insideMap(cell));Level.set(cell,Terrain.WALL);
                Dungeon.level.heroFOV[cell]=true;Dungeon.level.visited[cell]=true;
                assertTrue(Dungeon.level.searchedWalls.mark(Dungeon.level,cell));GameScene.updateMap(cell);
                assertTrue(Dungeon.level.searchedWalls.shows(Dungeon.level,cell));
            });
            Thread.sleep(400);TopScoresDeviceTest.screenshot("qol-searched-wall.png");
            TopScoresDeviceTest.render(WndGplLicense::show);Thread.sleep(300);TopScoresDeviceTest.screenshot("qol-offline-license.png");
            TopScoresDeviceTest.render(()->Game.switchScene(TitleScene.class));waitFor(()->Game.scene() instanceof TitleScene);
        }finally{SPDSettings.intro(intro);SPDSettings.autoCollectGrassDew(auto);SPDSettings.autoCollectDewWithEnemies(enemies);GamesInProgress.curSlot=slot;GamesInProgress.selectedClass=selected;}
    }
}

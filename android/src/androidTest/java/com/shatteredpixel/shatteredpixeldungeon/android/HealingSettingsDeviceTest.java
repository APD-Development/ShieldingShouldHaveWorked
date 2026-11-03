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
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.windows.*;
import com.watabou.noosa.Game;
import com.watabou.utils.Point;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Real taps through the settings entry and threshold chooser, not direct editor construction. */
@RunWith(AndroidJUnit4.class)
public class HealingSettingsDeviceTest {
    private static void waitFor(String message,Callable<Boolean> condition)throws Exception{
        long end=SystemClock.uptimeMillis()+60000;
        while(SystemClock.uptimeMillis()<end){if(condition.call())return;Thread.sleep(100);}fail(message);
    }
    private static void frames()throws Exception{
        long before=com.badlogic.gdx.Gdx.graphics.getFrameId();
        waitFor("renderer advances",()->com.badlogic.gdx.Gdx.graphics.getFrameId()>=before+4);
    }
    private static Object field(Object target,String name)throws Exception{
        for(Class<?> c=target.getClass();c!=null;c=c.getSuperclass())try{
            java.lang.reflect.Field f=c.getDeclaredField(name);f.setAccessible(true);return f.get(target);
        }catch(NoSuchFieldException missing){}throw new NoSuchFieldException(name);
    }
    private static <T> T window(Class<T> type)throws Exception{
        java.util.List<T> windows=TopScoresDeviceTest.find(Game.scene(),type);
        return windows.isEmpty()?null:windows.get(windows.size()-1);
    }
    private static void tap(Point p)throws Exception{
        android.app.Instrumentation in=InstrumentationRegistry.getInstrumentation();long down=SystemClock.uptimeMillis();
        for(int action:new int[]{MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP}){
            MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,p.x,p.y,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            in.sendPointerSync(event);event.recycle();Thread.sleep(100);
        }frames();
    }
    private static void button(Object parent,String text)throws Exception{
        frames();AtomicReference<Point> point=new AtomicReference<>();
        TopScoresDeviceTest.render(()->{try{
            for(RedButton b:TopScoresDeviceTest.find((com.watabou.noosa.Group)parent,RedButton.class))if(text.equals(b.text())){
                point.set(b.camera().cameraToScreen(b.left()+b.width()/2,b.top()+b.height()/2));break;
            }
            assertNotNull("visible button "+text,point.get());
        }catch(Exception e){throw new RuntimeException(e);}});tap(point.get());
    }
    @Test public void actualSettingsChooserSavesThresholdAndGatesRealPotionUse()throws Exception{
        int previous=SPDSettings.healingConfirmationThreshold(),slot=GamesInProgress.curSlot;boolean intro=SPDSettings.intro();HeroClass selected=GamesInProgress.selectedClass;
        try(ActivityScenario<AndroidLauncher> scenario=ActivityScenario.launch(AndroidLauncher.class)){
            waitFor("activity ready",()->Game.instance!=null&&Game.scene()!=null);
            TopScoresDeviceTest.render(()->{
                SPDSettings.healingConfirmationThreshold(-1);SPDSettings.intro(false);
                int free=GamesInProgress.firstEmpty(HeroClass.WARRIOR);assertTrue(free>0);GamesInProgress.curSlot=free;GamesInProgress.selectedClass=HeroClass.WARRIOR;
                Dungeon.hero=null;Dungeon.daily=false;Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();
                InterlevelScene.mode=InterlevelScene.Mode.DESCEND;Game.switchScene(InterlevelScene.class);
            });
            waitFor("game ready",()->Game.scene() instanceof GameScene&&Dungeon.hero!=null&&Dungeon.hero.ready);frames();
            AtomicReference<WndAPDSettings> parent=new AtomicReference<>();
            TopScoresDeviceTest.render(()->{WndAPDSettings w=new WndAPDSettings(com.shatteredpixel.shatteredpixeldungeon.apd.settings.ApdSettingsSection.GAMEPLAY);GameScene.show(w);parent.set(w);});
            TopScoresDeviceTest.render(()->{try{
                APDSettingsPane pane=(APDSettingsPane)field(parent.get(),"pane");RedButton b=(RedButton)field(pane,"btnHealingConfirmation");
                ((ScrollPane)field(parent.get(),"scroll")).scrollTo(0,Math.max(0,b.top()-8));
            }catch(Exception e){throw new RuntimeException(e);}});
            button(parent.get(),"Healing confirmation");waitFor("healing options opens",()->window(WndOptions.class)!=null);
            TopScoresDeviceTest.screenshot("healing-settings-entry.png");button(window(WndOptions.class),"Choose HP threshold");
            waitFor("Choose HP threshold opens editor",()->window(WndHealingConfirmation.ThresholdWindow.class)!=null);
            WndHealingConfirmation.ThresholdWindow editor=window(WndHealingConfirmation.ThresholdWindow.class);frames();
            AtomicReference<Point> sliderPoint=new AtomicReference<>();
            TopScoresDeviceTest.render(()->{try{
                OptionSlider slider=(OptionSlider)field(editor,"percentage");sliderPoint.set(slider.camera().cameraToScreen(slider.left()+slider.width()/2,slider.bottom()-7));
            }catch(Exception e){throw new RuntimeException(e);}});tap(sliderPoint.get());
            TopScoresDeviceTest.render(()->{try{assertEquals(50,((OptionSlider)field(editor,"percentage")).getSelectedValue());assertEquals(-1,SPDSettings.healingConfirmationThreshold());}catch(Exception e){throw new RuntimeException(e);}});
            TopScoresDeviceTest.screenshot("healing-settings-editor.png");button(editor,"Save");waitFor("threshold saved",()->SPDSettings.healingConfirmationThreshold()==50);
            TopScoresDeviceTest.render(()->parent.get().hide());frames();
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED);scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED);frames();assertEquals(50,SPDSettings.healingConfirmationThreshold());
            PotionOfHealing potion=new PotionOfHealing();potion.quantity(2);potion.setKnown();final float[] before={0};final int[] hp={0};
            TopScoresDeviceTest.render(()->{Dungeon.hero.HT=100;Dungeon.hero.HP=90;assertTrue(potion.collect(Dungeon.hero.belongings.backpack));before[0]=Dungeon.hero.cooldown();hp[0]=Dungeon.hero.HP;potion.execute(Dungeon.hero,PotionOfHealing.AC_DRINK);});
            waitFor("saved threshold opens drink warning",()->window(WndOptions.class)!=null);TopScoresDeviceTest.screenshot("healing-settings-confirm.png");button(window(WndOptions.class),"CANCEL");
            TopScoresDeviceTest.render(()->{assertEquals(2,potion.quantity());assertEquals(before[0],Dungeon.hero.cooldown(),0);assertEquals(hp[0],Dungeon.hero.HP);potion.execute(Dungeon.hero,PotionOfHealing.AC_DRINK);});
            waitFor("warning reopens",()->window(WndOptions.class)!=null);button(window(WndOptions.class),"USE HEALING ITEM");
            TopScoresDeviceTest.render(()->{assertEquals(1,potion.quantity());assertNotNull(Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing.class));});
            waitFor("hero ready",()->Dungeon.hero.ready);TopScoresDeviceTest.render(()->Game.switchScene(TitleScene.class));waitFor("title ready",()->Game.scene() instanceof TitleScene);frames();
        }finally{SPDSettings.healingConfirmationThreshold(previous);SPDSettings.intro(intro);GamesInProgress.curSlot=slot;GamesInProgress.selectedClass=selected;}
    }
}

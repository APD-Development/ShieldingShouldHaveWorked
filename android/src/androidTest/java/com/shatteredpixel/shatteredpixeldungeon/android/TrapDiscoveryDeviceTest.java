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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.apd.settings.ApdSettingsSection;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.OozeTrap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.windows.*;
import com.watabou.noosa.*;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/** Disposable-emulator checks. CI force-stops the app between a and b to prove disk persistence. */
@RunWith(AndroidJUnit4.class) @FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TrapDiscoveryDeviceTest {
    private static void waitFor(Callable<Boolean> condition)throws Exception{long until=SystemClock.uptimeMillis()+120000;while(SystemClock.uptimeMillis()<until){if(condition.call())return;Thread.sleep(100);}fail("Trap-discovery fixture timed out");}
    private static void title()throws Exception{waitFor(()->Game.scene()!=null&&PixelScene.uiCamera!=null);TopScoresDeviceTest.render(()->ShatteredPixelDungeon.switchNoFade(TitleScene.class));waitFor(()->Game.scene() instanceof TitleScene);}
    private static Object field(Object object,String name)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
    private static WndAPDSettings settings()throws Exception{AtomicReference<WndAPDSettings> ref=new AtomicReference<>();TopScoresDeviceTest.render(()->{ref.set(new WndAPDSettings(ApdSettingsSection.GAMEPLAY));Game.scene().addToFront(ref.get());});Thread.sleep(500);return ref.get();}
    private static CheckBox checkbox(WndAPDSettings window)throws Exception{APDSettingsPane pane=TopScoresDeviceTest.find(window,APDSettingsPane.class).get(0);assertEquals(ApdSettingsSection.GAMEPLAY,pane.section());return (CheckBox)field(pane,"chkContinueTrapDiscovery");}
    private static void revealControl(WndAPDSettings window,CheckBox box)throws Exception{
        ScrollPane scroll=TopScoresDeviceTest.find(window,ScrollPane.class).get(0);
        TopScoresDeviceTest.render(()->{((APDSettingsPane)box.parent).reveal(box);scroll.scrollTo(0,Math.max(0,box.top()-20));});Thread.sleep(400);
    }
    private static void touchControl(WndAPDSettings window,CheckBox box)throws Exception{
        revealControl(window,box);AtomicReference<Point> point=new AtomicReference<>();
        TopScoresDeviceTest.render(()->point.set(box.camera().cameraToScreen(box.left()+box.width()/2,box.top()+box.height()/2)));
        android.app.Instrumentation in=InstrumentationRegistry.getInstrumentation();long down=SystemClock.uptimeMillis();for(int action:new int[]{MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP}){MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,point.get().x,point.get().y,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);in.sendPointerSync(event);event.recycle();Thread.sleep(120);}Thread.sleep(500);
    }
    @Test public void a_controlWritesEnabledPreference()throws Exception{
        try(ActivityScenario<AndroidLauncher> app=ActivityScenario.launch(AndroidLauncher.class)){title();assertFalse("Fresh disposable emulator keeps the original default",SPDSettings.continueOnTrapDiscovery());WndAPDSettings window=settings();CheckBox box=checkbox(window);assertFalse(box.checked());touchControl(window,box);assertTrue(box.checked());assertTrue(SPDSettings.continueOnTrapDiscovery());System.out.println("TRAP_PERSIST_SEED_PID="+android.os.Process.myPid());TopScoresDeviceTest.screenshot("trap-discovery-setting-on.png");TopScoresDeviceTest.render(window::hide);}
        // Intentionally leaves On for b, which CI runs after an actual force-stop/new process.
    }
    @Test public void b_freshProcessReadsEnabledAndCanDisable()throws Exception{
        try(ActivityScenario<AndroidLauncher> app=ActivityScenario.launch(AndroidLauncher.class)){title();assertTrue("Enabled state must come from the previous process's saved preferences",SPDSettings.continueOnTrapDiscovery());WndAPDSettings window=settings();CheckBox box=checkbox(window);assertTrue(box.checked());System.out.println("TRAP_PERSIST_VERIFY_PID="+android.os.Process.myPid());revealControl(window,box);TopScoresDeviceTest.screenshot("trap-discovery-setting-restarted.png");touchControl(window,box);assertFalse(box.checked());assertFalse(SPDSettings.continueOnTrapDiscovery());TopScoresDeviceTest.render(()->{window.hide();GameSettings.set(null);});assertFalse("Fresh Android preference wrapper reads the saved Off value",SPDSettings.continueOnTrapDiscovery());WndAPDSettings reopened=settings();CheckBox offBox=checkbox(reopened);assertFalse(offBox.checked());revealControl(reopened,offBox);TopScoresDeviceTest.screenshot("trap-discovery-setting-off.png");TopScoresDeviceTest.render(reopened::hide);}
    }
    @Test public void c_runtimeDiscoveryRouteAndActualTrapEffect()throws Exception{
        final boolean oldContinue=SPDSettings.continueOnTrapDiscovery(),oldConfirm=SPDSettings.confirmKnownTraps(),oldIntro=SPDSettings.intro();final int oldSlot=GamesInProgress.curSlot;final HeroClass oldClass=GamesInProgress.selectedClass;final int[] created={-1};
        try(ActivityScenario<AndroidLauncher> app=ActivityScenario.launch(AndroidLauncher.class)){
            title();TopScoresDeviceTest.render(()->{SPDSettings.intro(false);created[0]=GamesInProgress.firstEmpty(HeroClass.WARRIOR);assertTrue(created[0]>0);GamesInProgress.curSlot=created[0];GamesInProgress.selectedClass=HeroClass.WARRIOR;Dungeon.hero=null;Dungeon.daily= false;Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();InterlevelScene.mode=InterlevelScene.Mode.DESCEND;Game.switchScene(InterlevelScene.class);});
            waitFor(()->Game.scene() instanceof GameScene&&Dungeon.hero!=null&&Dungeon.hero.ready);
            TopScoresDeviceTest.render(()->{try{
                Hero hero=Dungeon.hero;Level level=Dungeon.level;int width=level.width(),anchor=width*(level.height()/2)+width/2;
                for(Mob mob:new ArrayList<>(level.mobs)){Actor.remove(mob);if(mob.sprite!=null)mob.sprite.killAndErase();}level.mobs.clear();
                for(int y=-9;y<=9;y++)for(int x=-9;x<=9;x++){int cell=anchor+y*width+x;assertTrue(cell>=0&&cell<level.length());Level.set(cell,Terrain.EMPTY);level.visited[cell]=true;level.heroFOV[cell]=true;level.discoverable[cell]=true;}
                hero.pos=anchor;hero.sprite.place(anchor);hero.fieldOfView=level.heroFOV;hero.curAction=null;Buff.affect(hero,Foresight.class);SPDSettings.confirmKnownTraps(false);
                Method closer=Hero.class.getDeclaredMethod("getCloser",int.class);closer.setAccessible(true);Field path=Char.class.getDeclaredField("path");path.setAccessible(true);
                for(boolean enabled:new boolean[]{false,true}){
                    hero.pos=anchor;hero.sprite.place(anchor);hero.ready=true;SPDSettings.continueOnTrapDiscovery(enabled);OozeTrap trap=new OozeTrap();trap.set(anchor+width+2);level.traps.put(trap.pos,trap);Level.set(trap.pos,Terrain.SECRET_TRAP);HeroAction.Move action=new HeroAction.Move(anchor+6,HeroAction.Move.Mode.TRAVEL);hero.curAction=action;hero.lastAction=null;path.set(hero,null);
                    assertTrue((Boolean)closer.invoke(hero,action.dst));assertEquals(anchor+1,hero.pos);assertTrue(trap.visible);assertTrue(trap.active);assertNull(hero.buff(Ooze.class));if(enabled)assertSame(action,hero.curAction);else assertNull(hero.curAction);hero.curAction=null;hero.lastAction=null;
                }
                hero.pos=anchor;hero.sprite.place(anchor);OozeTrap routeTrap=new OozeTrap();routeTrap.set(anchor+2);level.traps.put(routeTrap.pos,routeTrap);Level.set(routeTrap.pos,Terrain.SECRET_TRAP);HeroAction.Move walk=new HeroAction.Move(anchor+6,HeroAction.Move.Mode.TRAVEL);hero.curAction=walk;path.set(hero,null);assertTrue((Boolean)closer.invoke(hero,walk.dst));assertSame(walk,hero.curAction);assertTrue(routeTrap.visible);assertFalse(level.passable[routeTrap.pos]);assertTrue((Boolean)closer.invoke(hero,walk.dst));assertNotEquals(routeTrap.pos,hero.pos);assertTrue(routeTrap.active);assertSame(walk,hero.curAction);
                OozeTrap stepped=new OozeTrap();stepped.set(hero.pos+1);level.traps.put(stepped.pos,stepped);Level.set(stepped.pos,Terrain.SECRET_TRAP);level.discover(stepped.pos);hero.curAction=new HeroAction.Move(stepped.pos);path.set(hero,null);assertTrue((Boolean)closer.invoke(hero,stepped.pos));assertEquals(stepped.pos,hero.pos);assertNull(hero.curAction);assertFalse(stepped.active);assertNotNull("The actual Ooze trap must apply its actual effect",hero.buff(Ooze.class));hero.lastAction=null;GameScene.updateMap();
            }catch(Exception error){throw new RuntimeException(error);}});
            Thread.sleep(500);TopScoresDeviceTest.screenshot("trap-discovery-runtime-actual-ooze.png");
        }finally{SPDSettings.continueOnTrapDiscovery(oldContinue);SPDSettings.confirmKnownTraps(oldConfirm);SPDSettings.intro(oldIntro);if(created[0]>0)Dungeon.deleteGame(created[0],true);GamesInProgress.curSlot=oldSlot;GamesInProgress.selectedClass=oldClass;}
    }
}

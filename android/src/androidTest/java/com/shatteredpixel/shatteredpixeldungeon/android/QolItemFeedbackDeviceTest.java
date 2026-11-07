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
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
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
        for(boolean useWindow:new boolean[]{false,true}){
            AtomicReference<WndInfoItem> shown=new AtomicReference<>();
            float[] time=new float[1];int[] hp=new int[1];String[] saved=new String[1];
            TopScoresDeviceTest.render(()->{
                time[0]=Dungeon.hero.cooldown();hp[0]=Dungeon.hero.HP;
                com.watabou.utils.Bundle before=new com.watabou.utils.Bundle();item.storeInBundle(before);saved[0]=before.toString();
                assertTrue(item.info(),item.info().contains(expected));
                WndInfoItem window=useWindow?new WndUseItem(null,item):new WndInfoItem(item);
                GameScene.show(window);shown.set(window);
            });
            Thread.sleep(300);
            TopScoresDeviceTest.render(()->{try{
                WndInfoItem window=shown.get();assertDescriptionViewport(window);
                ScrollPane scroll=TopScoresDeviceTest.find(window,ScrollPane.class).get(0);
                boolean found=false;
                for(RenderedTextBlock text:TopScoresDeviceTest.find(scroll.content(),RenderedTextBlock.class)){
                    if(text.text().equals(item.info())){found=true;assertTrue(text.text().contains(expected));}
                }
                assertTrue("Approved ordinary description remains intact",found);
                java.util.List<RedButton> buttons=TopScoresDeviceTest.find(window,RedButton.class);
                for(RedButton button:buttons){
                    assertNotEquals("No unauthorized item expander","Show calculations",button.text());
                    assertNotEquals("No unauthorized item expander","Hide calculations",button.text());
                    Point a=button.camera().cameraToScreen(button.left(),button.top()),z=button.camera().cameraToScreen(button.right(),button.bottom());
                    assertTrue("Item action remains visible",a.x>=0&&a.y>=0&&z.x<=Game.width&&z.y<=Game.height);
                }
                int expectedButtons=useWindow&&Dungeon.hero.isAlive()&&Dungeon.hero.belongings.contains(item)?item.actions(Dungeon.hero).size():0;
                assertEquals("Only existing item actions appear",expectedButtons,buttons.size());
                assertEquals(time[0],Dungeon.hero.cooldown(),0);assertEquals(hp[0],Dungeon.hero.HP);
                com.watabou.utils.Bundle after=new com.watabou.utils.Bundle();item.storeInBundle(after);assertEquals(saved[0],after.toString());
            }catch(Exception e){throw new RuntimeException(e);}});
            if(useWindow)TopScoresDeviceTest.screenshot(image);
            TopScoresDeviceTest.render(()->shown.get().hide());
        }
    }
    private static void calculationWindow(java.util.concurrent.Callable<Window> factory,String expected,String screenshot)throws Exception{
        AtomicReference<Window> shown=new AtomicReference<>();
        TopScoresDeviceTest.render(()->{try{Window window=factory.call();GameScene.show(window);shown.set(window);}catch(Exception e){throw new RuntimeException(e);}});
        checkCalculationWindow(shown.get(),expected,screenshot);
    }
    private static void assertDescriptionViewport(Window window){try{
        ScrollPane scroll=TopScoresDeviceTest.find(window,ScrollPane.class).get(0);
        Point origin=scroll.camera().cameraToScreen(scroll.left(),scroll.top());
        assertEquals("Description viewport follows the resized/offset window horizontally",origin.x,scroll.content().camera.x,0);
        assertEquals("Description viewport follows the resized/offset window vertically",origin.y,scroll.content().camera.y,0);
        assertEquals(scroll.camera().zoom,scroll.content().camera.zoom,0);
        assertEquals((int)scroll.width(),scroll.content().camera.width,0);
        assertEquals((int)scroll.height(),scroll.content().camera.height,0);
        for(RenderedTextBlock text:TopScoresDeviceTest.find(scroll.content(),RenderedTextBlock.class))assertSame("Body uses the clipped content camera",scroll.content().camera,text.camera());
    }catch(Exception e){throw new RuntimeException(e);}}
    private static void checkCalculationWindow(Window window,String expected,String screenshot)throws Exception{
        AtomicReference<RedButton> expander=new AtomicReference<>();Point[] at=new Point[1];float[] time=new float[1],plainHeight=new float[1];int[] hp=new int[1],size=new int[2];
        java.util.Map<Button,float[]> controls=new java.util.IdentityHashMap<>();
        TopScoresDeviceTest.render(()->{try{
            time[0]=Dungeon.hero.cooldown();hp[0]=Dungeon.hero.HP;
            java.lang.reflect.Field width=Window.class.getDeclaredField("width"),height=Window.class.getDeclaredField("height");width.setAccessible(true);height.setAccessible(true);size[0]=width.getInt(window);size[1]=height.getInt(window);
            Point first=window.camera().cameraToScreen(0,0),last=window.camera().cameraToScreen(size[0],size[1]);
            assertTrue("Calculation window content fits the screen",first.x>=0&&first.y>=0&&last.x<=Game.width&&last.y<=Game.height);
            for(Button button:TopScoresDeviceTest.find(window,Button.class)){
                if(button instanceof RedButton&&((RedButton)button).text().equals("Show calculations")){expander.set((RedButton)button);at[0]=button.camera().cameraToScreen(button.left()+button.width()/2,button.top()+button.height()/2);}
                else controls.put(button,new float[]{button.left(),button.top(),button.width(),button.height()});
            }
            for(RenderedTextBlock text:TopScoresDeviceTest.find(window,RenderedTextBlock.class))plainHeight[0]=Math.max(plainHeight[0],text.height());
            assertNotNull("The actual information window offers calculations",expander.get());
        }catch(Exception e){throw new RuntimeException(e);}});
        touch(at[0]);TopScoresDeviceTest.render(()->{try{
            assertEquals("Pointer opens advanced explanation","Hide calculations",expander.get().text());
            boolean found=false;for(RenderedTextBlock text:TopScoresDeviceTest.find(window,RenderedTextBlock.class))if(text.text().contains(expected))found=true;assertTrue("Rendered calculation contains live explanation: "+expected,found);
            for(java.util.Map.Entry<Button,float[]> entry:controls.entrySet()){Button b=entry.getKey();float[] before=entry.getValue();assertEquals(before[0],b.left(),0);assertEquals(before[1],b.top(),0);assertEquals(before[2],b.width(),0);assertEquals(before[3],b.height(),0);}
            java.lang.reflect.Field width=Window.class.getDeclaredField("width"),height=Window.class.getDeclaredField("height");width.setAccessible(true);height.setAccessible(true);assertEquals(size[0],width.getInt(window));assertEquals(size[1],height.getInt(window));
            assertEquals(time[0],Dungeon.hero.cooldown(),0);assertEquals(hp[0],Dungeon.hero.HP);
            ScrollPane scroll=TopScoresDeviceTest.find(window,ScrollPane.class).get(0);scroll.scrollTo(0,Math.min(plainHeight[0],Math.max(0,scroll.content().height()-scroll.height())));
        }catch(Exception e){throw new RuntimeException(e);}});
        Thread.sleep(250);TopScoresDeviceTest.render(()->assertDescriptionViewport(window));
        Point originalOffset=window.getOffset();TopScoresDeviceTest.render(()->window.offset(originalOffset.x+1,originalOffset.y+1));
        Thread.sleep(100);TopScoresDeviceTest.render(()->{assertDescriptionViewport(window);window.offset(originalOffset);});
        Thread.sleep(100);TopScoresDeviceTest.render(()->assertDescriptionViewport(window));
        TopScoresDeviceTest.screenshot(screenshot);touch(at[0]);
        TopScoresDeviceTest.render(()->{assertEquals("Pointer closes advanced explanation","Show calculations",expander.get().text());assertEquals(time[0],Dungeon.hero.cooldown(),0);assertEquals(hp[0],Dungeon.hero.HP);window.hide();});
    }
    private static void calculationWindowsRenderAndRespond()throws Exception{
        calculationWindow(()->{com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows cloak=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows();cloak.levelKnown=cloak.cursedKnown=true;return new WndInfoItem(cloak);},"4 scheduled updates","qol-cloak-explanation-expanded.png");
        calculationWindow(()->{com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose rose=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose();rose.levelKnown=rose.cursedKnown=true;return new WndInfoItem(rose);},"remaining health percentage instead of this meter","qol-rose-explanation-expanded.png");

        RingOfArcana arcana=new RingOfArcana();
        TopScoresDeviceTest.render(()->{arcana.level(3);arcana.identify();});
        showItem(arcana,"90.61%","qol-arcana-percentage.png");
        TopScoresDeviceTest.render(()->{
            for(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff buff:new java.util.ArrayList<>(Dungeon.hero.buffs(Ring.RingBuff.class)))buff.detach();
            Dungeon.hero.belongings.ring=arcana;arcana.activate(Dungeon.hero);
        });
        AtomicReference<WndInfoItem> luckyWindow=new AtomicReference<>();float[] luckyTime=new float[1];
        TopScoresDeviceTest.render(()->{
            Sword lucky=new Sword();lucky.levelKnown=lucky.cursedKnown=true;lucky.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Lucky());
            assertTrue(lucky.info(),lucky.info().contains("10% to 19.06%"));assertTrue(lucky.info().contains("adds 9.06 percentage points"));
            luckyTime[0]=Dungeon.hero.cooldown();WndInfoItem window=new WndInfoItem(lucky);GameScene.show(window);luckyWindow.set(window);
        });
        Thread.sleep(300);
        TopScoresDeviceTest.render(()->{try{ScrollPane scroll=TopScoresDeviceTest.find(luckyWindow.get(),ScrollPane.class).get(0);scroll.scrollTo(0,Math.max(0,scroll.content().height()-scroll.height()));}catch(Exception e){throw new RuntimeException(e);}});
        Thread.sleep(200);TopScoresDeviceTest.screenshot("qol-lucky-arcana-increase.png");
        TopScoresDeviceTest.render(()->{assertEquals(luckyTime[0],Dungeon.hero.cooldown(),0);luckyWindow.get().hide();});
        calculationWindow(()->{com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor armor=new com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor();armor.level(3);armor.levelKnown=armor.cursedKnown=true;armor.glyph=new com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Obfuscation();return new WndInfoItem(armor);},"stealth","qol-obfuscation-calculations-retained.png");

        calculationWindow(()->{com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier b=new com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier();b.setShield(17);return new WndInfoBuff(b);},"17 shield points","qol-mechanics-buff-expanded.png");
        calculationWindow(()->new WndInfoTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.ENHANCED_RINGS,1,null),"= 3 turns","qol-mechanics-talent-expanded.png");
        calculationWindow(()->new WndInfoArmorAbility(HeroClass.WARRIOR,new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap()),"Actual current ability cost:","qol-mechanics-ability-expanded.png");
        AtomicReference<WndClericSpells> menu=new AtomicReference<>();Point[] spellAt=new Point[1];
        TopScoresDeviceTest.render(()->{try{
            WndClericSpells w=new WndClericSpells(new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome(),Dungeon.hero,true);GameScene.show(w);menu.set(w);
            for(WndClericSpells.SpellButton b:TopScoresDeviceTest.find(w,WndClericSpells.SpellButton.class)){java.lang.reflect.Field f=b.getClass().getDeclaredField("spell");f.setAccessible(true);if(f.get(b)==com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon.INSTANCE)spellAt[0]=b.camera().cameraToScreen(b.left()+b.width()/2,b.top()+b.height()/2);}
            assertNotNull("Actual spell information button exists",spellAt[0]);
        }catch(Exception e){throw new RuntimeException(e);}});
        touch(spellAt[0]);AtomicReference<Window> spellWindow=new AtomicReference<>();
        TopScoresDeviceTest.render(()->{try{java.util.List<WndTitledMessage> windows=TopScoresDeviceTest.find(Game.scene(),WndTitledMessage.class);assertEquals("Actual spell button opens its information",1,windows.size());spellWindow.set(windows.get(0));}catch(Exception e){throw new RuntimeException(e);}});
        checkCalculationWindow(spellWindow.get(),"multiplied by weapon enchantment power and rounded","qol-mechanics-spell-expanded.png");TopScoresDeviceTest.render(()->menu.get().hide());
    }
    static void combinedOriginsAndRatPreferenceRenderAndRespond()throws Exception{
        boolean previous=SPDSettings.ratKingQuickSearch();AtomicReference<WndAPDSettings> shown=new AtomicReference<>();Point[] at=new Point[1];
        try{
            TopScoresDeviceTest.render(()->{
                SPDSettings.ratKingQuickSearch(false);WndAPDSettings window=new WndAPDSettings(com.shatteredpixel.shatteredpixeldungeon.apd.settings.ApdSettingsSection.GAMEPLAY);GameScene.show(window);shown.set(window);
                try{
                    java.lang.reflect.Field f=WndAPDSettings.class.getDeclaredField("pane");f.setAccessible(true);APDSettingsPane pane=(APDSettingsPane)f.get(window);
                    f=APDSettingsPane.class.getDeclaredField("chkRatKingSearch");f.setAccessible(true);CheckBox box=(CheckBox)f.get(pane);assertFalse(box.checked());
                    ScrollPane scroll=TopScoresDeviceTest.find(window,ScrollPane.class).get(0);scroll.scrollTo(0,Math.max(0,box.top()-8));
                    at[0]=box.camera().cameraToScreen(box.left()+box.width()/2,box.top()+box.height()/2);
                }catch(Exception e){throw new RuntimeException(e);}
            });Thread.sleep(300);touch(at[0]);TopScoresDeviceTest.render(()->assertTrue("Actual Quick Search checkbox tap enables preference",SPDSettings.ratKingQuickSearch()));
            TopScoresDeviceTest.screenshot("qol-rat-quick-search-setting.png");touch(at[0]);TopScoresDeviceTest.render(()->{assertFalse(SPDSettings.ratKingQuickSearch());shown.get().hide();shown.set(null);});
            Sword unknown=new Sword();unknown.levelKnown=false;unknown.cursedKnown=false;
            unknown.apdFoundOrigin=new com.shatteredpixel.shatteredpixeldungeon.apd.qol.ItemOrigins.Origin(8,0,com.shatteredpixel.shatteredpixeldungeon.apd.stats.GenerationEvents.C_LOCKED_CHEST,"TreasuryRoom");
            com.shatteredpixel.shatteredpixeldungeon.apd.qol.GenerationUpgradeRanges.set(unknown,0,3);
            showItem(unknown,"Possible upgrade level: +0 to +3","qol-unidentified-origin-range.png");
            TopScoresDeviceTest.render(()->{assertTrue(unknown.info().contains("Floor 8"));assertTrue(unknown.info().contains("Golden Key"));unknown.identify();assertFalse(unknown.info().contains("Found on"));assertFalse(unknown.info().contains("Possible upgrade level:"));
                try{AttackIndicator attack=TopScoresDeviceTest.find(Game.scene(),AttackIndicator.class).get(0);assertTrue("Attack control remains visible",attack.visible);}catch(Exception e){throw new RuntimeException(e);}
            });
        }finally{TopScoresDeviceTest.render(()->{if(shown.get()!=null)shown.get().hide();SPDSettings.ratKingQuickSearch(previous);});}
    }
    static void verifyOldBookshelfMarksAreHidden(int cell)throws Exception{
        // Real descent can award Pacifist; wait for its normal banner to expire before visual evidence.
        waitFor(()->{
            java.util.concurrent.atomic.AtomicBoolean expired=new java.util.concurrent.atomic.AtomicBoolean();
            TopScoresDeviceTest.render(()->{
                try { expired.set(TopScoresDeviceTest.find(Game.scene(),com.shatteredpixel.shatteredpixeldungeon.effects.BadgeBanner.class)
                        .stream().noneMatch(banner->banner.exists&&banner.visible)); }
                catch(Exception error){throw new RuntimeException(error);}
            });
            return expired.get();
        });
        TopScoresDeviceTest.render(()->{
            try {
                int active=0;
                for(com.shatteredpixel.shatteredpixeldungeon.effects.BadgeBanner banner:TopScoresDeviceTest.find(Game.scene(),com.shatteredpixel.shatteredpixeldungeon.effects.BadgeBanner.class))
                    if(banner.exists&&banner.visible)active++;
                android.util.Log.i("APD_CAPTURE_BANNERS","globalReferences="+com.shatteredpixel.shatteredpixeldungeon.effects.BadgeBanner.showing.size()+" activeCurrentScene="+active);
                assertEquals("no achievement overlay in current render tree",0,active);
            }catch(Exception error){throw new RuntimeException(error);}
        });
        TopScoresDeviceTest.render(()->{
            Level.set(cell,Terrain.BOOKSHELF);com.watabou.utils.Bundle legacy=new com.watabou.utils.Bundle();legacy.put("apd_searched_walls_v1",new int[]{cell});legacy.put("apd_door_checks_v1",new int[]{cell});
            Dungeon.level.searchedWalls.restore(legacy,Dungeon.level.length());assertTrue("legacy attempt retained",Dungeon.level.searchedWalls.attempted(cell));
            assertFalse("bookcase cannot be a hidden door",com.shatteredpixel.shatteredpixeldungeon.apd.qol.DoorSearchEligibility.candidate(Dungeon.level,cell));
            assertEquals(-1,com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchedWallMarkers.visual(Dungeon.level,cell));
            assertEquals(-1,com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchedWallMarkers.visual(Dungeon.level,cell-Dungeon.level.width()));GameScene.updateMap();
        });Thread.sleep(300);TopScoresDeviceTest.screenshot("qol-door-ineligible-bookshelf.png");
        TopScoresDeviceTest.render(()->{
            Level.set(cell,Terrain.WALL_DECO);com.watabou.utils.Bundle legacy=new com.watabou.utils.Bundle();legacy.put("apd_searched_walls_v1",new int[]{cell});Dungeon.level.searchedWalls.restore(legacy,Dungeon.level.length());
            assertFalse("decorated walls never hide doors",com.shatteredpixel.shatteredpixeldungeon.apd.qol.DoorSearchEligibility.candidate(Dungeon.level,cell));assertEquals(-1,com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchedWallMarkers.visual(Dungeon.level,cell));GameScene.updateMap();
        });Thread.sleep(300);TopScoresDeviceTest.screenshot("qol-door-ineligible-decoration.png");TopScoresDeviceTest.render(()->{Level.set(cell,Terrain.WALL);GameScene.updateMap();});
    }
    /** Actual floor-one exclusion followed by a real transition to a searchable floor. */
    static void prepareSearchableFloor()throws Exception{
        TopScoresDeviceTest.render(()->{
            assertEquals(1,Dungeon.depth);assertFalse(com.shatteredpixel.shatteredpixeldungeon.apd.qol.DoorSearchEligibility.supportsDoors(Dungeon.level));
            com.watabou.utils.Bundle oldMarks=new com.watabou.utils.Bundle();java.util.ArrayList<Integer> walls=new java.util.ArrayList<>();
            for(int p=0;p<Dungeon.level.length();p++)if(com.shatteredpixel.shatteredpixeldungeon.apd.qol.DoorSearchEligibility.wallDisguise(Dungeon.level.map[p])&&Dungeon.level.heroFOV[p])walls.add(p);
            assertFalse("actual visible floor-one walls",walls.isEmpty());int[] cells=new int[walls.size()];for(int n=0;n<cells.length;n++)cells[n]=walls.get(n);
            oldMarks.put("apd_searched_walls_v1",cells);Dungeon.level.searchedWalls.restore(oldMarks,Dungeon.level.length());GameScene.updateMap();
            for(int p=0;p<Dungeon.level.length();p++)assertEquals("no old or new Floor 1 checkmarks",-1,com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchedWallMarkers.visual(Dungeon.level,p));
        });TopScoresDeviceTest.screenshot("qol-doorless-floor1.png");
        TopScoresDeviceTest.render(()->{
            InterlevelScene.curTransition=Dungeon.level.getTransition(com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition.Type.REGULAR_EXIT);
            assertNotNull("real downstairs transition",InterlevelScene.curTransition);InterlevelScene.mode=InterlevelScene.Mode.DESCEND;Game.switchScene(InterlevelScene.class);
        });
        waitFor(()->Game.scene() instanceof GameScene&&Dungeon.depth==2&&Dungeon.hero.ready&&!Game.switchingScene());
        TopScoresDeviceTest.render(()->assertTrue(com.shatteredpixel.shatteredpixeldungeon.apd.qol.DoorSearchEligibility.supportsDoors(Dungeon.level)));
    }
    @Test public void grassDewControlsRingBreakdownsSearchedWallAndOfflineLicenseRenderNatively()throws Exception{
        boolean intro=SPDSettings.intro(),auto=SPDSettings.autoCollectGrassDew(),enemies=SPDSettings.autoCollectDewWithEnemies();
        int slot=GamesInProgress.curSlot;HeroClass selected=GamesInProgress.selectedClass;
        try(ActivityScenario<AndroidLauncher> scenario=ActivityScenario.launch(AndroidLauncher.class)){
            waitFor(()->Game.instance!=null&&Game.scene()!=null);
            TopScoresDeviceTest.render(()->Game.switchScene(TitleScene.class));waitFor(()->Game.scene() instanceof TitleScene);
            com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdModel parked=new com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdModel("fixture","fixture");
            parked.load(new java.io.File(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext().getCacheDir(),"qol-memory-fixture"));parked.awaitLoad(30000);
            assertNotNull(parked.catalog());
            TopScoresDeviceTest.render(()->{try{
                for(String name:new String[]{"model","mainModel"}){java.lang.reflect.Field f=UimsdScene.class.getDeclaredField(name);f.setAccessible(true);f.set(null,parked);}
            }catch(Exception e){throw new RuntimeException(e);}});
            TopScoresDeviceTest.render(()->{
                SPDSettings.intro(false);int free=GamesInProgress.firstEmpty(HeroClass.WARRIOR);assertTrue(free>0);
                GamesInProgress.curSlot=free;GamesInProgress.selectedClass=HeroClass.WARRIOR;Dungeon.hero=null;
                Dungeon.daily=false;Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();
                InterlevelScene.mode=InterlevelScene.Mode.DESCEND;Game.switchScene(InterlevelScene.class);
            });
            waitFor(()->Game.scene() instanceof GameScene&&Dungeon.hero!=null&&Dungeon.hero.ready);
            assertNull("gameplay releases the parked full catalog",parked.catalog());
            assertNull("gameplay releases its query result",parked.result());
            combinedOriginsAndRatPreferenceRenderAndRespond();
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
            showItem(sword,"Force changes this weapon’s base armed damage","qol-force-weapon.png");
            ThrowingKnife knife=new ThrowingKnife();knife.levelKnown=true;knife.cursedKnown=true;
            TopScoresDeviceTest.render(()->{RingOfSharpshooting r=new RingOfSharpshooting();r.anonymize();r.levelKnown=true;r.cursedKnown=true;Dungeon.hero.belongings.misc=r;r.activate(Dungeon.hero);});
            showItem(knife,"2-6 -> 3-8","qol-sharpshooting-weapon.png");
            calculationWindowsRenderAndRespond();
            prepareSearchableFloor();
            final int[] center={0};
            TopScoresDeviceTest.render(()->{
                int w=Dungeon.level.width();center[0]=(Dungeon.level.height()/2)*w+w/2;
                Dungeon.hero.HT=100000;Dungeon.hero.HP=100000;
                for(int y=-2;y<=2;y++)for(int x=-2;x<=2;x++){
                    int c=center[0]+y*w+x;Level.set(c,Terrain.WALL);Dungeon.level.heroFOV[c]=true;Dungeon.level.visited[c]=true;Dungeon.level.discoverable[c]=true;
                }
                for(int delta:new int[]{-2*w,2*w,-2,2}){
                    int behind=center[0]+delta;Dungeon.level.heroFOV[behind]=false;Dungeon.level.visited[behind]=false;Dungeon.level.mapped[behind]=false;
                }
                Level.set(center[0],Terrain.EMPTY);Dungeon.hero.fieldOfView=Dungeon.level.heroFOV;Dungeon.hero.pos=center[0];Dungeon.hero.sprite.place(center[0]);
                // Controlled terrain fixture, not a generated room: center the renderer before proof images.
                com.watabou.noosa.Camera.main.snapTo(Dungeon.hero.sprite.center());
                com.watabou.utils.Point heroScreen=com.watabou.noosa.Camera.main.cameraToScreen(Dungeon.hero.sprite.center().x,Dungeon.hero.sprite.center().y);
                assertTrue("controlled fixture hero is inside the viewport",heroScreen.x>0&&heroScreen.x<Game.width&&heroScreen.y>0&&heroScreen.y<Game.height);
            });
            verifyOldBookshelfMarksAreHidden(center[0]-Dungeon.level.width());
            TopScoresDeviceTest.render(()->{
                int w=Dungeon.level.width();
                Dungeon.hero.search(true);GameScene.updateMap();
                assertTrue("north front face",com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchedWallMarkers.visual(Dungeon.level,center[0]-w)>0);
                assertEquals("south lip drawn on visible floor above",com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchedWallMarkers.OVERHANG,com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchedWallMarkers.visual(Dungeon.level,center[0]));
                assertEquals("west partial corner end is unmarked",-1,com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchedWallMarkers.visual(Dungeon.level,center[0]-1));
                assertEquals("east partial corner end is unmarked",-1,com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchedWallMarkers.visual(Dungeon.level,center[0]+1));
            });
            Thread.sleep(1800);TopScoresDeviceTest.screenshot("qol-searched-wall.png");
            SearchedWallColorDeviceChecks.chooseColorThroughSettingsAndVerifyLiveLayer();
            TopScoresDeviceTest.render(()->{
                int w=Dungeon.level.width();Level.set(center[0]-w,Terrain.EMPTY);Level.set(center[0]+w,Terrain.EMPTY);
                Dungeon.level.heroFOV[center[0]-w]=true;Dungeon.level.heroFOV[center[0]+w]=true;
                GameScene.updateMap();
                assertEquals("straight west section remains marked",com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchedWallMarkers.RIGHT,com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchedWallMarkers.visual(Dungeon.level,center[0]-1));
                assertEquals("straight east section remains marked",com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchedWallMarkers.LEFT,com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchedWallMarkers.visual(Dungeon.level,center[0]+1));
                Level.set(center[0]-w,Terrain.WALL);Level.set(center[0]+w,Terrain.WALL);GameScene.updateMap();
            });
            for(int i=0;i<12;i++){
                waitFor(()->Dungeon.hero.ready);TopScoresDeviceTest.render(()->Dungeon.hero.search(true));Thread.sleep(150);
            }
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED);Thread.sleep(100);scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED);
            waitFor(()->Game.scene() instanceof GameScene&&!Game.switchingScene());
            TopScoresDeviceTest.render(()->assertTrue("searched south wall survives resume",Dungeon.level.searchedWalls.shows(Dungeon.level,center[0]+Dungeon.level.width())));
            System.gc();Thread.sleep(300);Runtime runtime=Runtime.getRuntime();
            android.util.Log.i("APD_MEMORY_PROBE","fixed-search-resume used="+(runtime.totalMemory()-runtime.freeMemory())+" limit="+runtime.maxMemory());
            TopScoresDeviceTest.render(WndGplLicense::show);Thread.sleep(300);TopScoresDeviceTest.screenshot("qol-offline-license.png");
            TopScoresDeviceTest.render(()->Game.switchScene(TitleScene.class));waitFor(()->Game.scene() instanceof TitleScene);
        }finally{SPDSettings.intro(intro);SPDSettings.autoCollectGrassDew(auto);SPDSettings.autoCollectDewWithEnemies(enemies);GamesInProgress.curSlot=slot;GamesInProgress.selectedClass=selected;}
    }
}

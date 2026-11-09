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
        showItem(item,expected,image,false);
    }
    private static void showItem(Item item,String expected,String image,boolean captureBottom)throws Exception{
        showItem(item,expected,image,captureBottom,false);
    }
    private static void showItem(Item item,String expected,String image,boolean captureBottom,boolean retainedPanel)throws Exception{
        for(boolean useWindow:new boolean[]{false,true}){
            AtomicReference<WndInfoItem> shown=new AtomicReference<>();boolean[] temporarilyOwned={false};
            float[] time=new float[1];int[] hp=new int[1];String[] saved=new String[1];
            TopScoresDeviceTest.render(()->{
                time[0]=Dungeon.hero.cooldown();hp[0]=Dungeon.hero.HP;
                com.watabou.utils.Bundle before=new com.watabou.utils.Bundle();item.storeInBundle(before);saved[0]=before.toString();
                assertTrue(item.info(),item.info().contains(expected));
                if(useWindow&&!Dungeon.hero.belongings.contains(item)){Dungeon.hero.belongings.backpack.items.add(item);temporarilyOwned[0]=true;}
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
                for(RenderedTextBlock body:TopScoresDeviceTest.find(scroll.content(),RenderedTextBlock.class)){
                    assertTrue("Description reserves space below its final line",scroll.content().height()>=Math.ceil(body.height())+2);
                    float bottomScroll=Math.max(0,scroll.content().height()-scroll.content().camera.height);
                    assertTrue("Last line is reachable without clipping",body.bottom()-bottomScroll<=scroll.content().camera.height-2);
                }

                java.util.List<RedButton> buttons=TopScoresDeviceTest.find(window,RedButton.class);
                int retainedExpanders=0;
                for(RedButton button:buttons){
                    if(retainedPanel&&button.text().equals("Show calculations"))retainedExpanders++;
                    else assertNotEquals("No unnecessary expander on these ordinary summaries","Show calculations",button.text());
                    assertNotEquals("No unnecessary expander on these ordinary summaries","Hide calculations",button.text());
                    Point a=button.camera().cameraToScreen(button.left(),button.top()),z=button.camera().cameraToScreen(button.right(),button.bottom());
                    assertTrue("Item action remains visible",a.x>=0&&a.y>=0&&z.x<=Game.width&&z.y<=Game.height);
                }
                int expectedButtons=useWindow&&Dungeon.hero.isAlive()&&Dungeon.hero.belongings.contains(item)?item.actions(Dungeon.hero).size():0;
                assertEquals("Only the explicitly retained panel appears",retainedPanel?1:0,retainedExpanders);
                assertEquals("Only existing item actions and the retained control appear",expectedButtons+(retainedPanel?1:0),buttons.size());
                assertEquals(time[0],Dungeon.hero.cooldown(),0);assertEquals(hp[0],Dungeon.hero.HP);
                com.watabou.utils.Bundle after=new com.watabou.utils.Bundle();item.storeInBundle(after);assertEquals(saved[0],after.toString());
            }catch(Exception e){throw new RuntimeException(e);}});
            if(useWindow){
                TopScoresDeviceTest.screenshot(image);
                if(captureBottom){
                    TopScoresDeviceTest.render(()->{try{
                        ScrollPane scroll=TopScoresDeviceTest.find(shown.get(),ScrollPane.class).get(0);
                        scroll.scrollTo(0,Math.max(0,scroll.content().height()-scroll.height()));
                    }catch(Exception e){throw new RuntimeException(e);}});
                    Thread.sleep(200);TopScoresDeviceTest.screenshot(image.replace(".png","-bottom.png"));
                }
            }
            TopScoresDeviceTest.render(()->{shown.get().hide();if(temporarilyOwned[0])Dungeon.hero.belongings.backpack.items.remove(item);});
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
    private static void inventoryOrderShowsAutomatically()throws Exception{
        final java.util.ArrayList<Item> original=new java.util.ArrayList<>();
        final java.util.ArrayList<com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag> bags=new java.util.ArrayList<>();
        final java.util.ArrayList<java.util.List<Item>> expected=new java.util.ArrayList<>();
        TopScoresDeviceTest.render(()->{
            com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag backpack=Dungeon.hero.belongings.backpack;original.addAll(backpack.items);backpack.items.clear();
            Item low=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword().level(8).identify();
            Item middle=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword().identify();
            Item plus=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Spear().level(3).identify();Item high=new Sword().identify();Item unknown=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword().level(-2);
            backpack.items.addAll(java.util.Arrays.asList(high,unknown,plus,middle,low));bags.add(backpack);expected.add(java.util.Arrays.asList(low,middle,plus,high,unknown));
            com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster holster=new com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster();
            Item knife=new ThrowingKnife().level(8).identify(),club=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingClub().identify(),clubPlus=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingClub().level(3).identify();
            Item wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost().identify(),wandPlus=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile().level(4).identify(),unknownWand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost().level(-3);
            holster.owner=Dungeon.hero;backpack.items.add(holster);holster.items.addAll(java.util.Arrays.asList(unknownWand,wandPlus,clubPlus,wand,club,knife));bags.add(holster);expected.add(java.util.Arrays.asList(knife,club,clubPlus,wand,wandPlus,unknownWand));
            com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier bandolier=new com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier();
            Item few=new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing().identify().quantity(1),many=new com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding().identify().quantity(8);
            bandolier.owner=Dungeon.hero;backpack.items.add(bandolier);bandolier.items.addAll(java.util.Arrays.asList(many,few));bags.add(bandolier);expected.add(java.util.Arrays.asList(few,many));
        });
        try{
            for(int i=0;i<bags.size()+1;i++){
                final int index=i<bags.size()?i:0;
                if(i==bags.size())TopScoresDeviceTest.render(()->{Item moved=expected.get(0).get(4);moved.identify();java.util.ArrayList<Item> newOrder=new java.util.ArrayList<>(expected.get(0));newOrder.remove(moved);newOrder.add(0,moved);expected.set(0,newOrder);});AtomicReference<WndBag> window=new AtomicReference<>();
                TopScoresDeviceTest.render(()->{window.set(new WndBag(bags.get(index)));GameScene.show(window.get());});Thread.sleep(200);
                TopScoresDeviceTest.render(()->{try{
                    java.lang.reflect.Field item=ItemSlot.class.getDeclaredField("item");item.setAccessible(true);
                    java.util.List<InventorySlot> slots=TopScoresDeviceTest.find(window.get(),InventorySlot.class);slots.sort(java.util.Comparator.comparingDouble((InventorySlot slot)->slot.top()).thenComparingDouble(slot->slot.left()));
                    java.util.ArrayList<Item> actual=new java.util.ArrayList<>();for(InventorySlot slot:slots){Item value=(Item)item.get(slot);if(expected.get(index).contains(value))actual.add(value);}
                    assertEquals("Actual inventory slots sort automatically without a button",expected.get(index),actual);
                }catch(Exception e){throw new AssertionError(e);}});
                TopScoresDeviceTest.screenshot("qol-automatic-inventory-"+i+".png");TopScoresDeviceTest.render(()->window.get().hide());
            }
        }finally{TopScoresDeviceTest.render(()->{Dungeon.hero.belongings.backpack.items.clear();Dungeon.hero.belongings.backpack.items.addAll(original);});}
    }
    private static void calculationWindowsRenderAndRespond()throws Exception{
        calculationWindow(()->{com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows cloak=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows();cloak.levelKnown=cloak.cursedKnown=true;return new WndInfoItem(cloak);},"every 4 turns","qol-cloak-explanation-expanded.png");
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
            assertTrue(lucky.info(),lucky.info().contains("_19.06%_ (_10%_ without Arcana)"));assertEquals(1,lucky.info().split("19.06%",-1).length-1);
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
    private static void ordinaryEffectsMatchOwnerScreenshots()throws Exception{
        KindofMisc[] previous=new KindofMisc[2];
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Whip whip=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Whip();
        com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherArmor armor=new com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherArmor();
        com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand fire=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast();
        com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand growth=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth();
        try{
            TopScoresDeviceTest.render(()->{
                previous[0]=Dungeon.hero.belongings.ring;previous[1]=Dungeon.hero.belongings.misc;
                for(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff buff:new java.util.ArrayList<>(Dungeon.hero.buffs(Ring.RingBuff.class)))buff.detach();
                RingOfArcana arcana=new RingOfArcana();arcana.identify();arcana.level(0);
                Dungeon.hero.belongings.ring=arcana;Dungeon.hero.belongings.misc=null;arcana.activate(Dungeon.hero);
                whip.level(3);whip.identify();whip.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Elastic());assertTrue(whip.collect());
                armor.level(2);armor.identify();armor.inscribe(new com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity());assertTrue(armor.collect());
                fire.curChargeKnown=false;growth.curChargeKnown=false;assertTrue(fire.collect());assertTrue(growth.collect());
                assertTrue(whip.info(),whip.info().contains("2 tiles"));assertFalse(whip.info().contains("Arcana changes"));
                assertTrue(armor.info(),armor.info().contains("10%"));assertFalse(armor.info().contains("defer power"));assertFalse(armor.info().contains("qualifying"));
            });
            showItem(whip,"58.75%","qol-elastic-concise.png",true);
            showItem(armor,"44.06%","qol-viscosity-concise.png",true);
            showItem(fire,"cone of fire","qol-fireblast-unknown-charges.png");
            showItem(growth,"plants in a cone","qol-regrowth-unknown-charges.png");
        }finally{
            TopScoresDeviceTest.render(()->{
                Dungeon.hero.belongings.backpack.items.remove(whip);Dungeon.hero.belongings.backpack.items.remove(armor);
                Dungeon.hero.belongings.backpack.items.remove(fire);Dungeon.hero.belongings.backpack.items.remove(growth);fire.stopCharging();growth.stopCharging();
                for(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff buff:new java.util.ArrayList<>(Dungeon.hero.buffs(Ring.RingBuff.class)))buff.detach();
                Dungeon.hero.belongings.ring=(Ring)previous[0];Dungeon.hero.belongings.misc=previous[1];
                if(previous[0]!=null)previous[0].activate(Dungeon.hero);if(previous[1] instanceof Ring)previous[1].activate(Dungeon.hero);
            });
        }
    }
    private static void ordinaryConsumableDescriptionsRender()throws Exception{
        com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion haste=new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste();
        com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion shielding=new com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding();
        Item noisemaker=new com.shatteredpixel.shatteredpixeldungeon.items.bombs.Noisemaker(),regrowthBomb=new com.shatteredpixel.shatteredpixeldungeon.items.bombs.RegrowthBomb();
        int previous=Dungeon.challenges;
        try{
            TopScoresDeviceTest.render(()->{haste.anonymize();shielding.anonymize();assertTrue(haste.collect());assertTrue(shielding.collect());assertTrue(noisemaker.collect());assertTrue(regrowthBomb.collect());Dungeon.challenges=0;});
            showItem(haste,"turns","qol-haste-concise.png");
            showItem(shielding,"shielding","qol-shielding-concise.png");
            showItem(noisemaker,"steps onto its tile","qol-noisemaker-trigger-description.png",true);
            showItem(regrowthBomb,"instead of exploding","qol-regrowth-bomb-description.png",true);
            TopScoresDeviceTest.render(()->Dungeon.challenges=Challenges.NO_HEALING);
            showItem(shielding,"poisons you instead","qol-shielding-challenge-warning.png");
            showItem(regrowthBomb,"poisons you instead","qol-regrowth-bomb-challenge-warning.png",true);
        }finally{
            TopScoresDeviceTest.render(()->{Dungeon.challenges=previous;Dungeon.hero.belongings.backpack.items.remove(haste);Dungeon.hero.belongings.backpack.items.remove(shielding);Dungeon.hero.belongings.backpack.items.remove(noisemaker);Dungeon.hero.belongings.backpack.items.remove(regrowthBomb);});
        }
    }
    private static void retainedTrinketDescriptionsRender()throws Exception{
        Item[] items={new com.shatteredpixel.shatteredpixeldungeon.items.trinkets.PetrifiedSeed(),new com.shatteredpixel.shatteredpixeldungeon.items.trinkets.MossyClump(),new com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ExoticCrystals(),new com.shatteredpixel.shatteredpixeldungeon.items.trinkets.FerretTuft(),new com.shatteredpixel.shatteredpixeldungeon.items.trinkets.EyeOfNewt(),new com.shatteredpixel.shatteredpixeldungeon.items.trinkets.CrackedSpyglass(),new com.shatteredpixel.shatteredpixeldungeon.items.trinkets.VialOfBlood(),new com.shatteredpixel.shatteredpixeldungeon.items.trinkets.SaltCube(),new com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion()};
        String[] expected={"seeds or runestones","Excludes Floor 1","cannot be spent as alchemy energy","harder to hit","sense enemies through walls","Search can uncover it","maximum health","slows natural healing","once for each type"};
        for(int i=0;i<items.length;i++)for(boolean use:new boolean[]{false,true}){
            Item item=items[i];String required=expected[i];AtomicReference<WndInfoItem> shown=new AtomicReference<>();boolean[] temporarilyOwned={false};
            TopScoresDeviceTest.render(()->{item.level(3);item.levelKnown=item.cursedKnown=true;assertTrue(item.info(),item.info().contains(required));if(use&&!Dungeon.hero.belongings.contains(item)){Dungeon.hero.belongings.backpack.items.add(item);temporarilyOwned[0]=true;}
                WndInfoItem window=use?new WndUseItem(null,item):new WndInfoItem(item);GameScene.show(window);shown.set(window);});Thread.sleep(300);TopScoresDeviceTest.render(()->assertDescriptionViewport(shown.get()));
            if(use)TopScoresDeviceTest.screenshot("qol-retained-trinket-"+i+"-normal-description.png");
            TopScoresDeviceTest.render(()->{shown.get().hide();if(temporarilyOwned[0])Dungeon.hero.belongings.backpack.items.remove(item);});
        }
    }
    private static void retainedArtifactDescriptionsRender()throws Exception{
        com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc previous=Dungeon.hero.belongings.misc;
        Item[] items={new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass(),new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.UnstableSpellbook(),new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MasterThievesArmband(),new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SandalsOfNature(),new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose(),new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.EtherealChains(),new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty(),new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood(),new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight(),new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AlchemistsToolkit(),new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SkeletonKey(),new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows(),new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome(),new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.LloydsBeacon(),new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CapeOfThorns()};
        String[] expected={"makes you invulnerable","random scroll effect","one chance at loot","Trampling tall grass"};
        String[] curseExpected={"10% chance","except Remove Curse and Anti-Magic","20% chance","stops normal seed","hostile wraith","slow your movement","33% less filling","50% longer","Manual Search still works","alchemy pots","5 out of 6","Cannot activate","Cannot cast spells","teleport actions still work","deflection effects still work"};
        try{for(int i=0;i<items.length;i++)for(boolean cursed:(i<4?new boolean[]{false,true}:new boolean[]{true}))for(boolean use:new boolean[]{false,true}){
            Item item=items[i];String required=cursed?curseExpected[i]:expected[i];AtomicReference<WndInfoItem> shown=new AtomicReference<>();
            TopScoresDeviceTest.render(()->{Dungeon.hero.belongings.misc=(com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc)item;item.levelKnown=item.cursedKnown=true;item.cursed=cursed;assertTrue(item.info(),item.info().contains(required));assertFalse(item.info().contains("base action time"));WndInfoItem window=use?new WndUseItem(null,item):new WndInfoItem(item);GameScene.show(window);shown.set(window);});Thread.sleep(300);TopScoresDeviceTest.render(()->assertDescriptionViewport(shown.get()));
            if(use)TopScoresDeviceTest.screenshot("qol-retained-artifact-"+i+(cursed?"-cursed":"-normal")+"-description.png");
            TopScoresDeviceTest.render(()->shown.get().hide());
        }}finally{TopScoresDeviceTest.render(()->Dungeon.hero.belongings.misc=previous);}
    }
    private static void magicMissileDescriptionsRender()throws Exception{
        com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile();
        TopScoresDeviceTest.render(()->{wand.level(3);wand.levelKnown=wand.cursedKnown=wand.curChargeKnown=true;});showItem(wand,"next zap","qol-magic-missile-concise.png",true);
        TopScoresDeviceTest.render(()->wand.levelKnown=false);showItem(wand,"weaker wand","qol-magic-missile-unknown.png",true);
        AtomicReference<Window> shown=new AtomicReference<>();
        TopScoresDeviceTest.render(()->{
            com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile.MagicCharge buff=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile.MagicCharge();com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();buff.storeInBundle(saved);saved.put("level",5);saved.put("time",com.shatteredpixel.shatteredpixeldungeon.actors.Actor.now()+4);buff.restoreFromBundle(saved);
            assertTrue(buff.desc(),buff.desc().contains("+5"));assertTrue(buff.desc(),buff.desc().contains("one zap"));Window window=new WndInfoBuff(buff);GameScene.show(window);shown.set(window);
        });Thread.sleep(300);TopScoresDeviceTest.render(()->assertDescriptionViewport(shown.get()));TopScoresDeviceTest.screenshot("qol-magic-charge-one-zap.png");TopScoresDeviceTest.render(()->shown.get().hide());
    }
    private static void corrosionWandDescriptionsRender()throws Exception{
        com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorrosion wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorrosion();
        TopScoresDeviceTest.render(()->{wand.level(3);wand.levelKnown=wand.cursedKnown=wand.curChargeKnown=true;});showItem(wand,"base damage per turn","qol-corrosion-wand-concise.png",true);
        TopScoresDeviceTest.render(()->wand.levelKnown=false);showItem(wand,"can harm you and allies","qol-corrosion-wand-unknown.png",true);
        HeroClass previousClass=Dungeon.hero.heroClass;com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass previousSubclass=Dungeon.hero.subClass;
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff staff=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff();
        try{
            TopScoresDeviceTest.render(()->{try{Dungeon.hero.heroClass=HeroClass.MAGE;Dungeon.hero.subClass=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.BATTLEMAGE;wand.levelKnown=true;wand.charge(Dungeon.hero);staff.level(3);staff.levelKnown=staff.cursedKnown=true;java.lang.reflect.Field field=staff.getClass().getDeclaredField("wand");field.setAccessible(true);field.set(staff,wand);}catch(Exception e){throw new RuntimeException(e);}});
            showItem(staff,"Standing in water washes it off","qol-corrosion-battlemage-concise.png",true);
        }finally{TopScoresDeviceTest.render(()->{wand.stopCharging();Dungeon.hero.heroClass=previousClass;Dungeon.hero.subClass=previousSubclass;});}
    }
    private static void disintegrationWandDescriptionsRender()throws Exception{
        com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration();
        TopScoresDeviceTest.render(()->{wand.level(3);wand.levelKnown=wand.cursedKnown=wand.curChargeKnown=true;});showItem(wand,"Can hit allies too","qol-disintegration-wand-concise.png",true);
        TopScoresDeviceTest.render(()->wand.levelKnown=false);showItem(wand,"Unupgraded example (+0)","qol-disintegration-wand-unknown.png",true);
        HeroClass previousClass=Dungeon.hero.heroClass;com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass previousSubclass=Dungeon.hero.subClass;
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff staff=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff();
        try{
            TopScoresDeviceTest.render(()->{try{Dungeon.hero.heroClass=HeroClass.MAGE;Dungeon.hero.subClass=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.BATTLEMAGE;wand.levelKnown=true;wand.charge(Dungeon.hero);staff.level(3);staff.levelKnown=staff.cursedKnown=true;java.lang.reflect.Field field=staff.getClass().getDeclaredField("wand");field.setAccessible(true);field.set(staff,wand);}catch(Exception e){throw new RuntimeException(e);}});
            showItem(staff,"strike enemies farther away","qol-disintegration-battlemage-concise.png",true);
        }finally{TopScoresDeviceTest.render(()->{wand.stopCharging();Dungeon.hero.heroClass=previousClass;Dungeon.hero.subClass=previousSubclass;});}
    }
    private static void lightningAndRegrowthDescriptionsRender()throws Exception{
        com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning lightning=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning();
        com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth regrowth=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth();
        com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast fireblast=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast();
        TopScoresDeviceTest.render(()->{for(com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand wand:new com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand[]{lightning,regrowth,fireblast}){wand.level(3);wand.levelKnown=wand.cursedKnown=wand.curChargeKnown=true;wand.curCharges=1;}});
        showItem(lightning,"allies are safe unless directly targeted","qol-lightning-concise.png",true);
        TopScoresDeviceTest.render(()->lightning.levelKnown=false);showItem(lightning,"Unupgraded example (+0)","qol-lightning-unknown.png",true);
        showItem(fireblast,"Charge cost: _1_","qol-fireblast-one-charge.png",true);
        showItem(regrowth,"Charge cost: _1_","qol-regrowth-one-charge.png",true,true);
        TopScoresDeviceTest.render(()->{regrowth.level(5);regrowth.curCharges=7;});showItem(regrowth,"Charge cost: _3_","qol-regrowth-three-charges.png",true,true);
        HeroClass previousClass=Dungeon.hero.heroClass;com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass previousSubclass=Dungeon.hero.subClass;
        com.watabou.utils.Bundle previousRules=new com.watabou.utils.Bundle();
        TopScoresDeviceTest.render(()->Dungeon.apdStoreCustomRules(previousRules));
        try{
            TopScoresDeviceTest.render(()->{Dungeon.hero.heroClass=HeroClass.MAGE;Dungeon.hero.subClass=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.BATTLEMAGE;lightning.levelKnown=true;});
            TopScoresDeviceTest.render(()->{regrowth.level(3);regrowth.curCharges=regrowth.maxCharges;});
            for(com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand wand:new com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand[]{lightning,regrowth}){
                com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff staff=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff();
                TopScoresDeviceTest.render(()->{try{wand.charge(Dungeon.hero);staff.level(3);staff.levelKnown=staff.cursedKnown=true;java.lang.reflect.Field field=staff.getClass().getDeclaredField("wand");field.setAccessible(true);field.set(staff,wand);}catch(Exception e){throw new RuntimeException(e);}});
                try{
                    showItem(staff,wand==lightning?"adds 1 tile":"Stay still","qol-"+(wand==lightning?"lightning":"regrowth")+"-battlemage.png",true);
                    if(wand==regrowth){
                        TopScoresDeviceTest.render(()->{com.watabou.utils.Bundle all=new com.watabou.utils.Bundle(),settings=new com.watabou.utils.Bundle();com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules.defaults().with(com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules.Slider.HEALING,0).store(settings);all.put("apd_custom_rules",settings);Dungeon.apdRestoreCustomRules(all);});
                        showItem(staff,"rules prevent the Battlemage healing","qol-regrowth-battlemage-healing-zero.png",true);
                    }
                }finally{TopScoresDeviceTest.render(()->wand.stopCharging());}
            }
            AtomicReference<Window> shown=new AtomicReference<>();
            TopScoresDeviceTest.render(()->{com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning.LightningCharge buff=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning.LightningCharge();Window window=new WndInfoBuff(buff);GameScene.show(window);shown.set(window);});
            Thread.sleep(300);TopScoresDeviceTest.render(()->assertDescriptionViewport(shown.get()));TopScoresDeviceTest.screenshot("qol-lightning-charge-concise.png");TopScoresDeviceTest.render(()->shown.get().hide());
        }finally{TopScoresDeviceTest.render(()->{Dungeon.apdRestoreCustomRules(previousRules);Dungeon.hero.heroClass=previousClass;Dungeon.hero.subClass=previousSubclass;});}
    }
    private static void livingEarthAndPrismaticDescriptionsRender()throws Exception{
        com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth earth=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth();
        com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight light=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight();
        TopScoresDeviceTest.render(()->{earth.level(3);light.level(3);for(com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand wand:new com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand[]{earth,light})wand.levelKnown=wand.cursedKnown=wand.curChargeKnown=true;});
        showItem(earth,"restore its health instead","qol-living-earth-concise.png",true);
        showItem(light,"hidden doors and traps along the beam","qol-prismatic-concise.png",true);
        TopScoresDeviceTest.render(()->{earth.levelKnown=false;light.levelKnown=false;});
        showItem(earth,"Unupgraded example (+0)","qol-living-earth-unknown.png",true);showItem(light,"Unupgraded example (+0)","qol-prismatic-unknown.png",true);
        AtomicReference<Window> shown=new AtomicReference<>();
        TopScoresDeviceTest.render(()->{com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth.RockArmor buff=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth.RockArmor();com.watabou.utils.Bundle state=new com.watabou.utils.Bundle();buff.storeInBundle(state);state.put("armor",11);state.put("wand_level",3);buff.restoreFromBundle(state);assertTrue(buff.desc(),buff.desc().contains("_11_"));assertTrue(buff.desc(),buff.desc().contains("_20_"));Window window=new WndInfoBuff(buff);GameScene.show(window);shown.set(window);});
        Thread.sleep(300);TopScoresDeviceTest.render(()->assertDescriptionViewport(shown.get()));TopScoresDeviceTest.screenshot("qol-rock-armor-reserve-concise.png");TopScoresDeviceTest.render(()->shown.get().hide());
        HeroClass priorClass=Dungeon.hero.heroClass;com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass priorSubclass=Dungeon.hero.subClass;
        try{
            TopScoresDeviceTest.render(()->{Dungeon.hero.heroClass=HeroClass.MAGE;Dungeon.hero.subClass=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.BATTLEMAGE;earth.levelKnown=light.levelKnown=true;});
            for(com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand wand:new com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand[]{earth,light}){
                com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff staff=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff();
                TopScoresDeviceTest.render(()->{try{wand.charge(Dungeon.hero);staff.level(3);staff.levelKnown=staff.cursedKnown=true;java.lang.reflect.Field field=staff.getClass().getDeclaredField("wand");field.setAccessible(true);field.set(staff,wand);}catch(Exception e){throw new RuntimeException(e);}});
                try{showItem(staff,wand==earth?"restore your guardian's health":"slow enemy movement","qol-"+(wand==earth?"earth":"prismatic")+"-battlemage.png",true);}finally{TopScoresDeviceTest.render(()->wand.stopCharging());}
            }
        }finally{TopScoresDeviceTest.render(()->{Dungeon.hero.heroClass=priorClass;Dungeon.hero.subClass=priorSubclass;});}
        for(int kind=0;kind<3;kind++){
            final int type=kind;AtomicReference<Window> buffWindow=new AtomicReference<>();
            TopScoresDeviceTest.render(()->{com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff buff;com.watabou.utils.Bundle state=new com.watabou.utils.Bundle();
                if(type==0){buff=new com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot.Armor();buff.storeInBundle(state);state.put("level",11);state.put("pos",Dungeon.hero.pos);buff.restoreFromBundle(state);}
                else if(type==1){buff=new com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass.Health();buff.storeInBundle(state);state.put("level",11);state.put("pos",Dungeon.hero.pos);buff.restoreFromBundle(state);}
                else{com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle.TimeBubble time=new com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle.TimeBubble();time.reset();time.processTime(1.25f);buff=time;}
                Window window=new WndInfoBuff(buff);GameScene.show(window);buffWindow.set(window);
            });Thread.sleep(300);TopScoresDeviceTest.render(()->assertDescriptionViewport(buffWindow.get()));TopScoresDeviceTest.screenshot("qol-herbal-time-buff-"+type+".png");TopScoresDeviceTest.render(()->buffWindow.get().hide());
        }
        com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth.EarthGuardian guardian=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth.EarthGuardian();
        com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth.Lotus lotus=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth.Lotus();
        TopScoresDeviceTest.render(()->{guardian.setInfo(Dungeon.hero,3,25);com.watabou.utils.Bundle state=new com.watabou.utils.Bundle();lotus.storeInBundle(state);state.put("wand_lvl",5);lotus.restoreFromBundle(state);lotus.HP=lotus.HT=40;});
        for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[]{guardian,lotus}){
            AtomicReference<Window> mobWindow=new AtomicReference<>();float[] time=new float[1];String[] before=new String[1];
            TopScoresDeviceTest.render(()->{com.shatteredpixel.shatteredpixeldungeon.actors.Actor.add(mob);time[0]=Dungeon.hero.cooldown();com.watabou.utils.Bundle state=new com.watabou.utils.Bundle();mob.storeInBundle(state);before[0]=state.toString();assertTrue(mob.description(),mob.description().contains(mob==guardian?"_3-12 damage_":"_60% chance_"));Window window=new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoMob(mob);GameScene.show(window);mobWindow.set(window);});
            Thread.sleep(300);
            TopScoresDeviceTest.render(()->{Window window=mobWindow.get();for(RenderedTextBlock text:TopScoresDeviceTest.find(window,RenderedTextBlock.class)){Point a=text.camera().cameraToScreen(text.left(),text.top()),b=text.camera().cameraToScreen(text.right(),text.bottom());assertTrue("NPC text fits screen",a.x>=0&&a.y>=0&&b.x<=Game.width&&b.y<=Game.height);}assertEquals(time[0],Dungeon.hero.cooldown(),0);com.watabou.utils.Bundle after=new com.watabou.utils.Bundle();mob.storeInBundle(after);assertEquals(before[0],after.toString());});
            TopScoresDeviceTest.screenshot(mob==guardian?"qol-earth-guardian-concise.png":"qol-golden-lotus-concise.png");
            TopScoresDeviceTest.render(()->{mobWindow.get().hide();com.shatteredpixel.shatteredpixeldungeon.actors.Actor.remove(mob);});
        }
    }
    private static void passiveGlyphArcanaDescriptionsRender()throws Exception{
        KindofMisc[] previous=new KindofMisc[2];
        try{
            TopScoresDeviceTest.render(()->{previous[0]=Dungeon.hero.belongings.ring;previous[1]=Dungeon.hero.belongings.misc;for(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff buff:new java.util.ArrayList<>(Dungeon.hero.buffs(Ring.RingBuff.class)))buff.detach();RingOfArcana ring=new RingOfArcana();ring.level(3);ring.identify();Dungeon.hero.belongings.ring=ring;Dungeon.hero.belongings.misc=null;ring.activate(Dungeon.hero);});
            for(String name:new String[]{"Flow","Swiftness","Camouflage","AntiMagic"}){
                com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor armor=new com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor();
                TopScoresDeviceTest.render(()->{try{armor.level(3);armor.levelKnown=armor.cursedKnown=true;armor.inscribe((com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph)Class.forName("com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs."+name).getDeclaredConstructor().newInstance());}catch(Exception e){throw new RuntimeException(e);}});
                showItem(armor,"without Arcana","qol-passive-glyph-"+name+"-arcana.png",true,true);
            }
        }finally{TopScoresDeviceTest.render(()->{for(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff buff:new java.util.ArrayList<>(Dungeon.hero.buffs(Ring.RingBuff.class)))buff.detach();Dungeon.hero.belongings.ring=(Ring)previous[0];Dungeon.hero.belongings.misc=previous[1];if(previous[0]!=null)previous[0].activate(Dungeon.hero);if(previous[1] instanceof Ring)previous[1].activate(Dungeon.hero);});}
    }
    private static void unidentifiedWandExamplesRender()throws Exception{
        for(String name:new String[]{"BlastWave","Corrosion","Corruption","Disintegration","Fireblast","Frost","Lightning","LivingEarth","MagicMissile","PrismaticLight","Regrowth","Transfusion","Warding"}){
            com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand wand=(com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand)Class.forName("com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOf"+name).getDeclaredConstructor().newInstance();
            TopScoresDeviceTest.render(()->{wand.level(7);wand.levelKnown=false;wand.cursedKnown=wand.curChargeKnown=true;wand.curCharges=2;});
            showItem(wand,name.equals("Corruption")?"Attempts to turn an enemy":name.equals("Regrowth")?"charges":"Unupgraded example (+0)","qol-wand-zero-example-"+name+".png",true);
        }
    }
    private static void staffImbueConfirmationsRender()throws Exception{
        int[][] cases={{0,2,1,2},{3,3,1,4},{3,2,1,3},{3,8,0,3}};
        for(int index=0;index<cases.length;index++){
            int[] values=cases[index];AtomicReference<WndOptions> shown=new AtomicReference<>();
            TopScoresDeviceTest.render(()->{try{
                com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff staff=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff();staff.level(values[0]);staff.levelKnown=staff.cursedKnown=true;
                com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile();wand.level(values[1]);wand.levelKnown=values[2]==1;wand.cursedKnown=wand.curChargeKnown=true;
                com.watabou.utils.Bundle beforeStaff=new com.watabou.utils.Bundle(),beforeWand=new com.watabou.utils.Bundle();staff.storeInBundle(beforeStaff);wand.storeInBundle(beforeWand);
                java.util.List<WndOptions> before=TopScoresDeviceTest.find(Game.scene(),WndOptions.class);
                java.lang.reflect.Field selector=staff.getClass().getDeclaredField("itemSelector");selector.setAccessible(true);((WndBag.ItemSelector)selector.get(staff)).onSelect(wand);
                java.util.List<WndOptions> created=TopScoresDeviceTest.find(Game.scene(),WndOptions.class);created.removeAll(before);assertEquals(1,created.size());WndOptions window=created.get(0);shown.set(window);
                boolean found=false;for(RenderedTextBlock text:TopScoresDeviceTest.find(window,RenderedTextBlock.class))if(text.text().contains("Replace the staff")){
                    assertTrue(text.text(),text.text().contains("if the staff was already upgraded"));assertTrue(text.text(),text.text().contains("+"+values[3]+" staff"));
                    if(values[2]==0){assertTrue(text.text(),text.text().contains("at least"));assertFalse(text.text(),text.text().contains("+8"));assertFalse(text.text(),text.text().contains("+9"));}
                    for(RedButton button:TopScoresDeviceTest.find(window,RedButton.class))assertTrue("Confirmation text does not overlap actions",text.bottom()<button.top());found=true;
                }assertTrue("Actual imbue confirmation body is present",found);
                com.watabou.utils.Bundle afterStaff=new com.watabou.utils.Bundle(),afterWand=new com.watabou.utils.Bundle();staff.storeInBundle(afterStaff);wand.storeInBundle(afterWand);assertEquals(beforeStaff.toString(),afterStaff.toString());assertEquals(beforeWand.toString(),afterWand.toString());
            }catch(Exception e){throw new RuntimeException(e);}});
            Thread.sleep(300);TopScoresDeviceTest.screenshot("qol-staff-imbue-confirmation-"+index+".png");TopScoresDeviceTest.render(()->shown.get().hide());
        }
    }
    private static void ordinaryFlailSpinDescriptionsRender()throws Exception{
        for(int count=1;count<=3;count++){
            final int spins=count;AtomicReference<Window> shown=new AtomicReference<>();
            TopScoresDeviceTest.render(()->{
                com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Flail.SpinAbilityTracker buff=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Flail.SpinAbilityTracker();buff.spins=spins;com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();buff.storeInBundle(saved);saved.put("time",com.shatteredpixel.shatteredpixeldungeon.actors.Actor.now()+3);buff.restoreFromBundle(saved);
                assertTrue(buff.desc(),buff.desc().contains("Spins: "+spins+"/3"));Window window=new WndInfoBuff(buff);GameScene.show(window);shown.set(window);
            });Thread.sleep(300);TopScoresDeviceTest.render(()->assertDescriptionViewport(shown.get()));
            TopScoresDeviceTest.screenshot("qol-flail-spin-"+count+"-normal.png");TopScoresDeviceTest.render(()->shown.get().hide());
        }
    }
    private static void ordinaryWarriorShieldDescriptionsRender()throws Exception{
        for(int reserve:new int[]{0,12})for(int time:new int[]{-25,0,40}){
            AtomicReference<Window> shown=new AtomicReference<>();
            TopScoresDeviceTest.render(()->{try{
                com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal.WarriorShield shield=new com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal.WarriorShield();shield.setShield(reserve);
                java.lang.reflect.Field cooldown=shield.getClass().getDeclaredField("cooldown");cooldown.setAccessible(true);cooldown.setInt(shield,time);
                assertFalse(shield.desc(),shield.desc().contains("game-time units"));Window window=new WndInfoBuff(shield);GameScene.show(window);shown.set(window);
            }catch(Exception e){throw new RuntimeException(e);}});
            Thread.sleep(300);TopScoresDeviceTest.render(()->assertDescriptionViewport(shown.get()));
            TopScoresDeviceTest.screenshot("qol-warrior-shield-"+reserve+"-"+time+"-normal.png");
            TopScoresDeviceTest.render(()->shown.get().hide());
        }
    }
    private static void ordinaryExoticPotionDescriptionsRender()throws Exception{
        String[] names={"PotionOfCorrosiveGas","PotionOfDivineInspiration","PotionOfMastery","PotionOfStormClouds"};
        for(String name:names){
            Item item=(Item)Class.forName("com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic."+name).getDeclaredConstructor().newInstance();TopScoresDeviceTest.render(()->item.identify());
            for(boolean use:new boolean[]{false,true}){
                AtomicReference<WndInfoItem> shown=new AtomicReference<>();boolean[] temporarilyOwned={false};
                TopScoresDeviceTest.render(()->{if(use&&!Dungeon.hero.belongings.contains(item)){Dungeon.hero.belongings.backpack.items.add(item);temporarilyOwned[0]=true;}
                WndInfoItem window=use?new WndUseItem(null,item):new WndInfoItem(item);GameScene.show(window);shown.set(window);});
                Thread.sleep(300);TopScoresDeviceTest.render(()->assertDescriptionViewport(shown.get()));
                if(use)TopScoresDeviceTest.screenshot("qol-ordinary-potion-"+name+"-description.png");
                TopScoresDeviceTest.render(()->{shown.get().hide();if(temporarilyOwned[0])Dungeon.hero.belongings.backpack.items.remove(item);});
            }
        }
    }
    private static void ordinaryDartDescriptionsRender()throws Exception{
        String[] names={"Dart","AdrenalineDart","BlindingDart","ChillingDart","CleansingDart","DisplacingDart","HealingDart","HolyDart","IncendiaryDart","ParalyticDart","PoisonDart","RotDart","ShockingDart"};
        for(String name:names){
            Item item=(Item)Class.forName("com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts."+name).getDeclaredConstructor().newInstance();
            for(boolean use:new boolean[]{false,true}){
                AtomicReference<WndInfoItem> shown=new AtomicReference<>();boolean[] temporarilyOwned={false};
                TopScoresDeviceTest.render(()->{item.levelKnown=item.cursedKnown=true;if(use&&!Dungeon.hero.belongings.contains(item)){Dungeon.hero.belongings.backpack.items.add(item);temporarilyOwned[0]=true;}
                WndInfoItem window=use?new WndUseItem(null,item):new WndInfoItem(item);GameScene.show(window);shown.set(window);assertFalse(item.info(),item.info().contains("scheduled units"));assertFalse(item.info().contains("scaling floor"));if(name.equals("RotDart")){assertTrue(item.info(),item.info().contains("Sharpshooting damage"));assertFalse(item.info(),item.info().contains("Sharpshooting durability"));}});
                Thread.sleep(300);TopScoresDeviceTest.render(()->assertDescriptionViewport(shown.get()));
                if(use)TopScoresDeviceTest.screenshot("qol-ordinary-dart-"+name+"-description.png");
                TopScoresDeviceTest.render(()->{shown.get().hide();if(temporarilyOwned[0])Dungeon.hero.belongings.backpack.items.remove(item);});
            }
        }
    }
    private static void additionalDuelistDescriptionsRender()throws Exception{
        HeroClass previous=Dungeon.hero.heroClass;
        try{
            TopScoresDeviceTest.render(()->Dungeon.hero.heroClass=HeroClass.DUELIST);
            Item[] items={new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Crossbow(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sickle(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WarScythe(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Whip(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dirk(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.AssassinsBlade(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Flail(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Gloves(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sai(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Gauntlet(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Quarterstaff(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RoundShield(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatshield(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RunicBlade(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scimitar() };
            String[] expected={"+7 base damage","base damage per turn","base damage per turn","every enemy within reach","visible empty tile","visible empty tile","visible empty tile","first costs a charge","uses up the combo","uses up the combo","uses up the combo","triples your evasion","After blocking an attack","After blocking an attack","enchantment power","attack speed"};
            for(int i=0;i<items.length;i++){
                Item item=items[i];TopScoresDeviceTest.render(()->{item.level(3);item.levelKnown=item.cursedKnown=true;});showItem(item,expected[i],"qol-duelist-"+i+"-concise.png",true);
                TopScoresDeviceTest.render(()->{item.levelKnown=false;com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon weapon=(com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon)item;weapon.augment=com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Augment.DAMAGE;assertTrue(item.info(),item.info().contains("_"+weapon.augment.damageFactor(weapon.min(0))+"-"+weapon.augment.damageFactor(weapon.max(0))+" damage_"));});showItem(item,"Unupgraded example (+0)","qol-duelist-"+i+"-unknown-example.png",true);
            }
        }finally{TopScoresDeviceTest.render(()->Dungeon.hero.heroClass=previous);}
    }
    private static void missileEffectDescriptionsRender()throws Exception{
        Item[] items={new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Bolas(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ForceCube(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.HeavyBoomerang(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear()};
        String[] expected={"instant throw","movement speed","including you and allies","Stay there to catch it","Against piranhas"};
        for(int i=0;i<items.length;i++){items[i].levelKnown=items[i].cursedKnown=true;showItem(items[i],expected[i],"qol-missile-"+i+"-concise.png",true);}
    }
    private static void unknownRingMissileDescriptionsRender()throws Exception{
        com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring old=Dungeon.hero.belongings.ring;
        java.util.Set<com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff> before=new java.util.HashSet<>(Dungeon.hero.buffs());
        com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting unknown=new com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting();
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Tomahawk axe=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Tomahawk();
        try{
            TopScoresDeviceTest.render(()->{
                unknown.anonymize();unknown.levelKnown=unknown.cursedKnown=false;Dungeon.hero.belongings.ring=unknown;unknown.activate(Dungeon.hero);axe.levelKnown=axe.cursedKnown=true;axe.level(3);
                unknown.level(0);String baseline=axe.info();unknown.level(9);assertEquals("Unknown ring cannot change ordinary weapon text",baseline,axe.info());
            });
            showItem(axe,"Weapon alone:","qol-tomahawk-unknown-ring-safe.png",true);
            TopScoresDeviceTest.render(()->unknown.levelKnown=unknown.cursedKnown=true);
            showItem(axe,"damage per turn","qol-tomahawk-known-ring.png",true);
        }finally{TopScoresDeviceTest.render(()->{for(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff buff:Dungeon.hero.buffs())if(!before.contains(buff)&&buff instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring.RingBuff)buff.detach();Dungeon.hero.belongings.ring=old;});}
    }
    private static void tenacityDescriptionsRender()throws Exception{
        com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity ring=new com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity();
        for(boolean cursed:new boolean[]{false,true})for(boolean use:new boolean[]{false,true}){
            AtomicReference<WndInfoItem> shown=new AtomicReference<>();
            TopScoresDeviceTest.render(()->{
                ring.anonymize();ring.level(cursed?0:3);ring.levelKnown=ring.cursedKnown=true;ring.cursed=cursed;
                assertTrue(ring.info(),ring.info().contains(cursed?"more damage taken":"less damage"));
                WndInfoItem window=use?new WndUseItem(null,ring):new WndInfoItem(ring);GameScene.show(window);shown.set(window);
            });Thread.sleep(300);TopScoresDeviceTest.render(()->assertDescriptionViewport(shown.get()));
            if(use)TopScoresDeviceTest.screenshot(cursed?"qol-tenacity-curse-concise.png":"qol-tenacity-protection-concise.png");
            TopScoresDeviceTest.render(()->shown.get().hide());
        }
    }
    private static void questDescriptionsRender()throws Exception{
        com.shatteredpixel.shatteredpixeldungeon.items.quest.EscapeCrystal escape=new com.shatteredpixel.shatteredpixeldungeon.items.quest.EscapeCrystal();
        TopScoresDeviceTest.render(()->{
            escape.storedItems=new com.watabou.utils.Bundle();
            com.watabou.utils.Bundle gear=new com.watabou.utils.Bundle(),slots=new com.watabou.utils.Bundle();
            Dungeon.hero.belongings.storeInBundle(gear);Dungeon.quickslot.storePlaceholders(slots);
            escape.storedItems.put(com.shatteredpixel.shatteredpixeldungeon.items.quest.EscapeCrystal.BELONGINGS,gear);
            escape.storedItems.put(com.shatteredpixel.shatteredpixeldungeon.items.quest.EscapeCrystal.QUICKSLOTS,slots);
            escape.storedItems.put(com.shatteredpixel.shatteredpixeldungeon.items.quest.EscapeCrystal.GOLD,Dungeon.gold);
            escape.storedItems.put(com.shatteredpixel.shatteredpixeldungeon.items.quest.EscapeCrystal.ENERGY,Dungeon.energy);
        });
        Item[] items={new com.shatteredpixel.shatteredpixeldungeon.items.quest.CeremonialCandle(),new com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust(),escape};
        String[] expected={"north, east, south and west","banishes its wraiths","returns your original equipment"};
        for(int i=0;i<items.length;i++)showItem(items[i],expected[i],"qol-quest-"+i+"-concise.png",true);
    }
    private static void utilityDescriptionsRender()throws Exception{
        Item[] items={new com.shatteredpixel.shatteredpixeldungeon.items.ArcaneResin(),new com.shatteredpixel.shatteredpixeldungeon.items.Stylus(),new com.shatteredpixel.shatteredpixeldungeon.items.TengusMask(),new com.shatteredpixel.shatteredpixeldungeon.items.KingsCrown()};
        String[] expected={"one level","random glyph","choose a subclass","choose an armor ability"};
        for(int i=0;i<items.length;i++)showItem(items[i],expected[i],"qol-utility-"+i+"-concise.png",true);
    }
    private static void remainsAndLoreDescriptionsRender()throws Exception{
        int hp=Dungeon.hero.HP;
        com.watabou.utils.Bundle rules=new com.watabou.utils.Bundle();
        TopScoresDeviceTest.render(()->{Dungeon.apdStoreCustomRules(rules);Dungeon.hero.HP=1;});
        try{
            Item page=new com.shatteredpixel.shatteredpixeldungeon.items.remains.TornPage();
            showItem(page,"Would restore","qol-torn-page-current-healing.png",true);
            TopScoresDeviceTest.render(()->{
                com.watabou.utils.Bundle all=new com.watabou.utils.Bundle(),settings=new com.watabou.utils.Bundle();
                com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules.defaults().with(com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules.Slider.HEALING,0).store(settings);all.put("apd_custom_rules",settings);Dungeon.apdRestoreCustomRules(all);
            });
            showItem(page,"0 HP","qol-torn-page-zero-healing.png",true);
            showItem(new com.shatteredpixel.shatteredpixeldungeon.items.journal.RegionLorePage.Prison(),"read it in your journal","qol-prison-lore-concise.png");
        }finally{TopScoresDeviceTest.render(()->{Dungeon.hero.HP=hp;Dungeon.apdRestoreCustomRules(rules);});}
    }
    private static void craftedSpellVariantsRender()throws Exception{
        com.shatteredpixel.shatteredpixeldungeon.items.spells.SummonElemental spell=new com.shatteredpixel.shatteredpixeldungeon.items.spells.SummonElemental();
        Class<?>[] variants={com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental.AllyNewBornElemental.class,com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental.FireElemental.class,com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental.FrostElemental.class,com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental.ShockElemental.class,com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental.ChaosElemental.class};
        String[] expected={"no ranged attack","50% chance","one-in-three","including you and your allies","random cursed wand effects"};
        try{TopScoresDeviceTest.render(()->assertTrue(spell.collect()));
            for(int i=0;i<variants.length;i++){final Class<?> variant=variants[i];TopScoresDeviceTest.render(()->{com.watabou.utils.Bundle state=new com.watabou.utils.Bundle();spell.storeInBundle(state);state.put("summon_class",variant);spell.restoreFromBundle(state);});showItem(spell,expected[i],"qol-summon-"+i+"-concise.png",true);}
        }finally{TopScoresDeviceTest.render(()->Dungeon.hero.belongings.backpack.items.remove(spell));}
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
            com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage mirror=new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage();
            TopScoresDeviceTest.render(()->{mirror.identify();mirror.quantity(2);assertTrue(mirror.collect());});
            showItem(mirror,"disappear when damaged","qol-mirror-scroll-full-text.png");
            Sword sword=new Sword();sword.levelKnown=true;sword.cursedKnown=true;
            TopScoresDeviceTest.render(()->{RingOfForce r=new RingOfForce();r.anonymize();r.levelKnown=true;r.cursedKnown=true;Dungeon.hero.belongings.ring=r;r.activate(Dungeon.hero);});
            TopScoresDeviceTest.render(()->{
                String info=sword.info();
                assertFalse(info.contains("Attack time is measured"));
                assertFalse(info.contains("Combined identified rings only"));
                assertFalse(info.contains("Enemy evasion and other effects determine"));
                assertTrue(info.contains("Attack time:"));
                assertTrue(info.contains("Accuracy:"));
            });
            showItem(sword,"Force damage:","qol-force-weapon.png");
            ThrowingKnife knife=new ThrowingKnife();knife.levelKnown=true;knife.cursedKnown=true;
            TopScoresDeviceTest.render(()->{RingOfSharpshooting r=new RingOfSharpshooting();r.anonymize();r.levelKnown=true;r.cursedKnown=true;Dungeon.hero.belongings.misc=r;r.activate(Dungeon.hero);});
            showItem(knife,"2-6 → 3-8","qol-sharpshooting-weapon.png");
            com.shatteredpixel.shatteredpixeldungeon.items.armor.ScaleArmor scale=new com.shatteredpixel.shatteredpixeldungeon.items.armor.ScaleArmor();
            scale.level(6);scale.levelKnown=scale.cursedKnown=true;
            final int[] priorStrength={0};final com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor[] priorArmor={null};
            TopScoresDeviceTest.render(()->{
                priorStrength[0]=Dungeon.hero.STR;priorArmor[0]=Dungeon.hero.belongings.armor;
                Dungeon.hero.STR=16;Dungeon.hero.belongings.armor=scale;
                assertFalse(scale.info().contains("Armor subtracts"));assertFalse(scale.info().contains("points short"));
            });
            showItem(scale,"6-32","qol-scale-armor-concise.png");
            TopScoresDeviceTest.render(()->{Dungeon.hero.STR=priorStrength[0];Dungeon.hero.belongings.armor=priorArmor[0];});
            calculationWindowsRenderAndRespond();
            ordinaryEffectsMatchOwnerScreenshots();
            ordinaryConsumableDescriptionsRender();
            craftedSpellVariantsRender();
            remainsAndLoreDescriptionsRender();
            utilityDescriptionsRender();
            questDescriptionsRender();
            tenacityDescriptionsRender();
            unknownRingMissileDescriptionsRender();
            missileEffectDescriptionsRender();
            additionalDuelistDescriptionsRender();
            retainedArtifactDescriptionsRender();
            retainedTrinketDescriptionsRender();
            ordinaryDartDescriptionsRender();
            ordinaryExoticPotionDescriptionsRender();
            ordinaryWarriorShieldDescriptionsRender();
            ordinaryFlailSpinDescriptionsRender();
            staffImbueConfirmationsRender();
            magicMissileDescriptionsRender();
            corrosionWandDescriptionsRender();
            disintegrationWandDescriptionsRender();
            lightningAndRegrowthDescriptionsRender();
            livingEarthAndPrismaticDescriptionsRender();
            passiveGlyphArcanaDescriptionsRender();
            unidentifiedWandExamplesRender();
            inventoryOrderShowsAutomatically();
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

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
package com.shatteredpixel.shatteredpixeldungeon.apd;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.MechanicsDetails;
import com.shatteredpixel.shatteredpixeldungeon.apd.qol.GrassDewPickupTest;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Catalog-wide inventory inspection, independent of rendering and without using items. */
public class ItemDescriptionPurityTest {
    private com.watabou.noosa.Game previousGame;
    private Hero previousHero;
    private Level previousLevel;
    private int previousDepth, previousChallenges;
    private Object previousNotes;
    private Bundle previousRules;
    @Before public void prepare() throws Exception {
        HeadlessTestSupport.start();
        previousGame=com.watabou.noosa.Game.instance;
        java.lang.reflect.Field unsafeField=Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);Object unsafe=unsafeField.get(null);
        // A catalog context exists without constructing renderer/GL resources.
        com.watabou.noosa.Game.instance=(com.watabou.noosa.Game)unsafe.getClass().getMethod("allocateInstance",Class.class).invoke(unsafe,com.watabou.noosa.Game.class);
        previousHero=Dungeon.hero; previousLevel=Dungeon.level;
        previousDepth=Dungeon.depth; previousChallenges=Dungeon.challenges;
        previousRules=new Bundle(); Dungeon.apdStoreCustomRules(previousRules);
        Dungeon.apdRestoreCustomRules(new Bundle()); Dungeon.challenges=0; Dungeon.depth=6;
        java.lang.reflect.Field notes=com.shatteredpixel.shatteredpixeldungeon.journal.Notes.class.getDeclaredField("records");
        notes.setAccessible(true);previousNotes=notes.get(null);com.shatteredpixel.shatteredpixeldungeon.journal.Notes.reset();
        Hero hero=new Hero();hero.heroClass=HeroClass.WARRIOR;Talent.initClassTalents(hero);
        hero.HP=hero.HT=20;hero.pos=24;Dungeon.hero=hero;
        Level level=new GrassDewPickupTest.TestLevel();Dungeon.level=level;level.setSize(7,7);
        level.blobs=new HashMap<>();level.mobs=new HashSet<>();
        level.heaps=new com.watabou.utils.SparseArray<>();level.traps=new com.watabou.utils.SparseArray<>();
        level.plants=new com.watabou.utils.SparseArray<>();level.customTiles=new ArrayList<>();level.customWalls=new ArrayList<>();
        Arrays.fill(level.map,com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY);level.buildFlagMaps();
    }
    @After public void restore() throws Exception {
        com.watabou.noosa.Game.instance=previousGame;
        Dungeon.hero=previousHero;Dungeon.level=previousLevel;Dungeon.depth=previousDepth;Dungeon.challenges=previousChallenges;
        Dungeon.apdRestoreCustomRules(previousRules);
        java.lang.reflect.Field notes=com.shatteredpixel.shatteredpixeldungeon.journal.Notes.class.getDeclaredField("records");
        notes.setAccessible(true);notes.set(null,previousNotes);
    }
    private static SortedMap<String,Class<?>> classes(Collection<Catalog> catalogs){
        SortedMap<String,Class<?>> result=new TreeMap<>();
        for(Catalog catalog:catalogs)for(Class<?> type:catalog.items())
            if(Item.class.isAssignableFrom(type))result.put(type.getName(),type);
        return result;
    }
    private static Item item(Class<?> type)throws Exception{
        java.lang.reflect.Constructor<?> constructor=type.getDeclaredConstructor();constructor.setAccessible(true);
        Item item=(Item)constructor.newInstance();
        // Actual vault-created crystals always have their transfer bundle before serialization.
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.quest.EscapeCrystal)
            ((com.shatteredpixel.shatteredpixeldungeon.items.quest.EscapeCrystal)item).storedItems=new Bundle();
        // Local catalog knowledge without identifying anything in the player's journal.
        if(item instanceof Ring)((Ring)item).anonymize();
        if(item instanceof Potion)((Potion)item).anonymize();
        if(item instanceof Scroll)((Scroll)item).anonymize();
        return item;
    }
    private static String inspect(Item item){
        boolean temporarilyOwned=!Dungeon.hero.belongings.contains(item);
        if(temporarilyOwned)Dungeon.hero.belongings.backpack.items.add(item);
        try{String plain=item.info();return plain+"\n"+MechanicsDetails.item(item,plain);}
        finally{if(temporarilyOwned)Dungeon.hero.belongings.backpack.items.remove(item);}
    }
    @Test public void catalogInventoryInspectionPreservesSavedItemStateAndRandomness(){
        List<String> failures=new ArrayList<>();int inspected=0;
        Random.pushGenerator(7719);
        try{
            for(Class<?> type:classes(Arrays.asList(Catalog.values())).values()){
                try{
                    Item item=item(type);item.levelKnown=item.cursedKnown=true;
                    Bundle before=new Bundle();item.storeInBundle(before);
                    Random.pushGenerator(10471);float expected=Random.Float();Random.popGenerator();
                    String description;float afterRandom;
                    Random.pushGenerator(10471);
                    try{description=inspect(item);afterRandom=Random.Float();}finally{Random.popGenerator();}
                    Bundle after=new Bundle();item.storeInBundle(after);
                    if(description.trim().isEmpty())failures.add(type.getSimpleName()+": empty description");
                    if(!before.toString().equals(after.toString()))failures.add(type.getSimpleName()+": inspection changed saved state");
                    if(Float.floatToIntBits(expected)!=Float.floatToIntBits(afterRandom))failures.add(type.getSimpleName()+": inspection consumed randomness");
                    inspected++;
                }catch(Throwable problem){failures.add(type.getSimpleName()+": "+problem);}
            }
        }finally{Random.popGenerator();}
        System.out.println("CATALOG_DESCRIPTION_PURITY inspected="+inspected+" failures="+failures.size());
        assertTrue("Expected substantial actual catalog coverage",inspected>100);
        assertEquals(failures.toString(),Collections.emptyList(),failures);
    }
    @Test public void unidentifiedCatalogEquipmentDoesNotExposeActualUpgradeLevel(){
        List<String> failures=new ArrayList<>();int inspected=0;
        Random.pushGenerator(17819);
        try{
            for(Class<?> type:classes(Arrays.asList(Catalog.MELEE_WEAPONS,Catalog.ARMOR,Catalog.THROWN_WEAPONS,Catalog.WANDS,Catalog.RINGS)).values()){
                try{
                    Item item=item(type);item.level(0);item.levelKnown=item.cursedKnown=false;
                    String baseline=inspect(item);item.level(3);
                    if(!baseline.equals(inspect(item)))failures.add(type.getSimpleName()+": hidden +0/+3 descriptions differ");
                    inspected++;
                }catch(Throwable problem){failures.add(type.getSimpleName()+": "+problem);}
            }
        }finally{Random.popGenerator();}
        System.out.println("CATALOG_UNKNOWN_LEVEL inspected="+inspected+" failures="+failures.size());
        assertTrue("Expected real equipment catalog coverage",inspected>50);
        assertEquals(failures.toString(),Collections.emptyList(),failures);
    }
    @Test public void equippedCatalogInspectionDoesNotSpendReadyBenefitsOrChangeTheHero(){
        List<String> failures=new ArrayList<>();int inspected=0;
        Random.pushGenerator(38992);
        try{
            for(Class<?> type:classes(Arrays.asList(Catalog.MELEE_WEAPONS,Catalog.ARMOR,Catalog.RINGS,Catalog.ARTIFACTS)).values()){
                try{
                    Hero hero=new Hero();hero.heroClass=HeroClass.WARRIOR;Talent.initClassTalents(hero);
                    hero.pos=24;hero.HP=hero.HT=20;Dungeon.hero=hero;
                    hero.talents.get(1).put(Talent.LIQUID_AGILITY,1);
                    hero.belongings.weapon=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword();
                    hero.belongings.weapon.levelKnown=hero.belongings.weapon.cursedKnown=true;
                    hero.belongings.armor=new com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor();
                    hero.belongings.armor.levelKnown=hero.belongings.armor.cursedKnown=true;
                    Item item=item(type);item.level(3);item.levelKnown=item.cursedKnown=true;
                    if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor){
                        hero.belongings.armor=(com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor)item;
                        ((com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor)item).activate(hero);
                    }else if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon){
                        hero.belongings.weapon=(com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon)item;
                        ((com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon)item).activate(hero);
                    }else if(item instanceof Ring){hero.belongings.ring=(Ring)item;((Ring)item).activate(hero);
                    }else if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact){
                        hero.belongings.artifact=(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact)item;
                        ((com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact)item).activate(hero);
                    }
                    com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(hero,Talent.LethalMomentumTracker.class);
                    Talent.LiquidAgilACCTracker agility=com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(hero,Talent.LiquidAgilACCTracker.class,5);agility.uses=2;
                    Bundle before=new Bundle();hero.storeInBundle(before);
                    Random.pushGenerator(911);float expected=Random.Float();Random.popGenerator();
                    float observed;Random.pushGenerator(911);
                    try{inspect(item);observed=Random.Float();}finally{Random.popGenerator();}
                    Bundle after=new Bundle();hero.storeInBundle(after);
                    if(!before.toString().equals(after.toString()))failures.add(type.getSimpleName()+": inspection changed saved hero/equipment state");
                    if(hero.buff(Talent.LethalMomentumTracker.class)==null||agility.uses!=2)failures.add(type.getSimpleName()+": ready attack benefit spent");
                    if(Float.floatToIntBits(expected)!=Float.floatToIntBits(observed))failures.add(type.getSimpleName()+": inspection consumed randomness");
                    inspected++;
                }catch(Throwable problem){failures.add(type.getSimpleName()+": "+problem);}
            }
        }finally{Random.popGenerator();}
        System.out.println("CATALOG_EQUIPPED_PURITY inspected="+inspected+" failures="+failures.size());
        assertTrue("Expected actual equipped families",inspected>40);
        assertEquals(failures.toString(),Collections.emptyList(),failures);
    }
    @Test public void allCatalogWandsKeepInspectionPureDuringWildMagic(){
        Hero hero=new Hero();hero.heroClass=HeroClass.MAGE;Talent.initClassTalents(hero);
        hero.HP=hero.HT=100;hero.pos=24;Dungeon.hero=hero;
        hero.talents.get(3).put(Talent.WILD_POWER,4);
        com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WildMagic.WildMagicTracker.class);
        List<String> failures=new ArrayList<>();int inspected=0;Random.pushGenerator(38991);
        try{
            for(Class<?> type:classes(Collections.singletonList(Catalog.WANDS)).values()){
                try{
                    Item wand=item(type);wand.level(3);wand.levelKnown=wand.cursedKnown=true;
                    hero.belongings.backpack.items.add(wand);
                    Bundle before=new Bundle();hero.storeInBundle(before);
                    Random.pushGenerator(371);float expected=Random.Float();Random.popGenerator();
                    float actual;Random.pushGenerator(371);
                    try{inspect(wand);actual=Random.Float();}finally{Random.popGenerator();}
                    Bundle after=new Bundle();hero.storeInBundle(after);
                    if(!before.toString().equals(after.toString()))failures.add(type.getSimpleName()+": Wild Magic inspection changed state");
                    if(Float.floatToIntBits(expected)!=Float.floatToIntBits(actual))failures.add(type.getSimpleName()+": Wild Magic inspection consumed randomness");
                    hero.belongings.backpack.items.remove(wand);inspected++;
                }catch(Throwable problem){failures.add(type.getSimpleName()+": "+problem);}
            }
        }finally{Random.popGenerator();}
        System.out.println("CATALOG_WILD_MAGIC_PURITY inspected="+inspected+" failures="+failures.size());
        assertEquals(13,inspected);assertEquals(failures.toString(),Collections.emptyList(),failures);
    }
    @Test public void catalogInspectionDoesNotNormalizeSpiritFormThroughIndirectStatQueries(){
        Dungeon.hero.talents.get(3).put(Talent.SPIRIT_FORM,1);
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.SpiritForm.SpiritFormBuff form=
                com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(Dungeon.hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.SpiritForm.SpiritFormBuff.class);
        List<String> failures=new ArrayList<>();int inspected=0;Random.pushGenerator(9179);
        try{
            for(Class<?> type:classes(Arrays.asList(Catalog.values())).values()){
                try{
                    Item item=item(type);item.levelKnown=item.cursedKnown=true;
                    com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana ring=new com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana();
                    ring.anonymize();ring.levelKnown=ring.cursedKnown=true;ring.level(7);form.setEffect(ring);
                    Bundle before=new Bundle();form.storeInBundle(before);
                    inspect(item);
                    Bundle after=new Bundle();form.storeInBundle(after);
                    if(!before.toString().equals(after.toString()))failures.add(type.getSimpleName()+": indirect inspection rewrote Spirit Form cache");
                    inspected++;
                }catch(Throwable problem){failures.add(type.getSimpleName()+": "+problem);}
            }
        }finally{Random.popGenerator();}
        System.out.println("CATALOG_SPIRIT_FORM_PURITY inspected="+inspected+" failures="+failures.size());
        assertEquals(289,inspected);assertEquals(failures.toString(),Collections.emptyList(),failures);
    }
    @Test @SuppressWarnings("unchecked") public void pureSpiritRingQueriesMatchLegacyGetterMathAcrossAllRingFamilies()throws Exception{
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.SpiritForm.SpiritFormBuff form=
                com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(Dungeon.hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.SpiritForm.SpiritFormBuff.class);
        java.lang.reflect.Field buffType=Ring.class.getDeclaredField("buffClass");buffType.setAccessible(true);
        int combinations=0;Random.pushGenerator(58731);
        try{
            for(Class<?> type:classes(Collections.singletonList(Catalog.RINGS)).values())
                for(int rank=0;rank<=4;rank++)for(boolean cursed:new boolean[]{false,true})
                    for(boolean enhanced:new boolean[]{false,true})for(boolean degraded:new boolean[]{false,true})
                        for(boolean owned:new boolean[]{false,true}){
                            com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.detach(Dungeon.hero,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnhancedRings.class);
                            com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.detach(Dungeon.hero,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade.class);
                            Dungeon.hero.talents.get(3).put(Talent.SPIRIT_FORM,rank);
                            Ring ring=(Ring)item(type);ring.cursed=cursed;ring.levelKnown=ring.cursedKnown=true;form.setEffect(ring);
                            if(owned)Dungeon.hero.belongings.backpack.items.add(ring);
                            if(enhanced)com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(Dungeon.hero,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnhancedRings.class);
                            if(degraded)com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(Dungeon.hero,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade.class);
                            ring.level(7);Class<? extends Ring.RingBuff> stat=(Class<? extends Ring.RingBuff>)buffType.get(ring);
                            int actual=Ring.getBonus(Dungeon.hero,stat),buffed=Ring.getBuffedBonus(Dungeon.hero,stat);
                            assertEquals("passive query must not normalize cached level",7,ring.level());
                            // The existing explicit gameplay getter and actual solo formulas remain the oracle.
                            Ring legacy=form.ring();assertEquals(legacy.soloBonus(),actual);
                            assertEquals(legacy.soloBuffedBonus(),buffed);
                            if(owned)Dungeon.hero.belongings.backpack.items.remove(ring);
                            combinations++;
                        }
        }finally{Random.popGenerator();}
        System.out.println("SPIRIT_RING_LEGACY_EQUIVALENCE combinations="+combinations);
        assertEquals(960,combinations);
    }
    @Test @SuppressWarnings("unchecked") public void pureSpiritRingQueriesPreserveZeroSumFallbackAndMagicImmunity()throws Exception{
        Dungeon.hero.talents.get(3).put(Talent.SPIRIT_FORM,2);
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.SpiritForm.SpiritFormBuff form=
                com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(Dungeon.hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.SpiritForm.SpiritFormBuff.class);
        com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy virtual=new com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy();form.setEffect(virtual);virtual.level(7);
        java.lang.reflect.Field field=Ring.class.getDeclaredField("buffClass");field.setAccessible(true);
        Class<? extends Ring.RingBuff> type=(Class<? extends Ring.RingBuff>)field.get(virtual);
        assertEquals(3,Ring.getBonus(Dungeon.hero,type));
        com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy first=new com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy();
        Dungeon.hero.belongings.ring=first;first.activate(Dungeon.hero);assertEquals(1,Ring.getBonus(Dungeon.hero,type));
        com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy second=new com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy();second.level(1);second.cursed=true;
        Dungeon.hero.belongings.misc=second;second.activate(Dungeon.hero);assertEquals(3,Ring.getBonus(Dungeon.hero,type));assertEquals(3,Ring.getBuffedBonus(Dungeon.hero,type));
        com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(Dungeon.hero,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune.class);
        assertEquals(0,Ring.getBonus(Dungeon.hero,type));assertEquals(0,Ring.getBuffedBonus(Dungeon.hero,type));assertEquals(7,virtual.level());
    }
}

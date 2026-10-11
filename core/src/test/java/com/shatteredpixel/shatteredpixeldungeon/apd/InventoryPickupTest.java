package com.shatteredpixel.shatteredpixeldungeon.apd;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BattleAxe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Vorpal;
import org.junit.Test;
import static org.junit.Assert.*;

public class InventoryPickupTest {
    private interface Check { void run(Hero hero); }
    private void withHero(Check check) {
        HeadlessTestSupport.start();
        Hero previous = Dungeon.hero;
        try {
            Hero hero = new Hero(); com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.initClassTalents(hero); hero.HP = 0; Dungeon.hero = hero;
            check.run(hero);
        } finally { Dungeon.hero = previous; }
    }
    private BattleAxe axe() { BattleAxe axe = new BattleAxe(); axe.enchant(new Vorpal()); return axe; }
    private void fill(Hero hero, int slots) { for(int i=0;i<slots;i++) hero.belongings.backpack.items.add(new Sword()); }
    @Test public void vorpalAxeCollectsIntoLastAvailableBackpackSlot() { withHero(h -> {
        fill(h,19); BattleAxe axe=axe();
        assertEquals(20,h.belongings.backpack.capacity());
        assertTrue(axe.collect(h.belongings.backpack));
        assertSame(axe,h.belongings.backpack.items.get(h.belongings.backpack.items.indexOf(axe)));
        assertEquals(20,h.belongings.backpack.items.size());
    }); }
    @Test public void fullBackpackRejectsWithoutConsumingOrChangingAxe() { withHero(h -> {
        fill(h,20); BattleAxe axe=axe(); int quantity=axe.quantity();
        assertFalse(axe.collect(h.belongings.backpack));
        assertEquals(20,h.belongings.backpack.items.size());assertEquals(quantity,axe.quantity());
        assertTrue(axe.enchantment instanceof Vorpal);assertFalse(h.belongings.backpack.contains(axe));
    }); }
    @Test public void emptySpecializedContainersCannotAcceptMeleeWeaponOrExpandBackpack() { withHero(h -> {
        fill(h,20);
        for(Bag bag:new Bag[]{new VelvetPouch(),new ScrollHolder(),new PotionBandolier(),new MagicalHolster()}) {
            bag.owner=h;h.belongings.backpack.items.add(bag);
            assertFalse(bag.canHold(axe()));assertTrue(bag.items.isEmpty());
        }
        assertEquals(24,h.belongings.backpack.capacity());
        assertFalse(axe().collect(h.belongings.backpack));
        assertFalse(new Pasty().collect(h.belongings.backpack));
        h.belongings.backpack.items.remove(0);
        assertTrue(axe().collect(h.belongings.backpack));
    }); }
    @Test public void secondaryWeaponStillConsumesOneBackpackSlot() { withHero(h -> {
        h.belongings.secondWep=new Sword();fill(h,19);
        assertEquals(19,h.belongings.backpack.capacity());assertFalse(axe().collect(h.belongings.backpack));
        assertFalse(new Pasty().collect(h.belongings.backpack));
        h.belongings.backpack.items.remove(0);assertTrue(axe().collect(h.belongings.backpack));
    }); }
    @Test public void pastyCollectsIntoAvailableSlotAndStacksWhenFull() { withHero(h -> {
        fill(h,19); Pasty first=new Pasty();assertTrue(first.collect(h.belongings.backpack));
        int before=first.quantity();assertTrue(new Pasty().collect(h.belongings.backpack));
        assertEquals(before+1,first.quantity());assertEquals(20,h.belongings.backpack.items.size());
    }); }
    @Test public void lostInventoryBlocksNewAxeEvenWithEmptyBackpack() { withHero(h -> {
        LostInventory lost=new LostInventory();assertTrue(lost.attachTo(h));
        assertTrue(h.belongings.backpack.items.isEmpty());assertFalse(axe().collect(h.belongings.backpack));
        assertFalse(new Pasty().collect(h.belongings.backpack));
        lost.detach();assertTrue(axe().collect(h.belongings.backpack));
    }); }
    @Test public void lostInventoryIsPersistentNotATimedDebuffAndDetachRestoresExistingItems() { withHero(h -> {
        BattleAxe existing=axe(); h.belongings.backpack.items.add(existing);
        LostInventory lost=new LostInventory();assertTrue(lost.attachTo(h));
        assertTrue(h.belongings.lostInventory());assertSame(existing,h.belongings.backpack.items.get(0));
        assertTrue(lost.act());assertSame(lost,h.buff(LostInventory.class));
        assertTrue(h.belongings.lostInventory());
        lost.detach();assertNull(h.buff(LostInventory.class));assertFalse(h.belongings.lostInventory());
        assertSame(existing,h.belongings.backpack.items.get(0));assertTrue(new Pasty().collect(h.belongings.backpack));
    }); }
    @Test public void saveRoundtripKeepsLostInventoryAndItemsThenAllowsRecovery() { withHero(h -> {
        BattleAxe existing=axe();h.belongings.backpack.items.add(existing);
        LostInventory lost=new LostInventory();assertTrue(lost.attachTo(h));
        com.watabou.utils.Bundle bundle=new com.watabou.utils.Bundle();h.storeInBundle(bundle);
        Hero restored=new Hero();Dungeon.hero=restored;restored.restoreFromBundle(bundle);
        assertNotNull(restored.buff(LostInventory.class));assertTrue(restored.belongings.lostInventory());
        assertEquals(1,restored.belongings.backpack.items.size());
        assertFalse(new Pasty().collect(restored.belongings.backpack));
        restored.buff(LostInventory.class).detach();assertFalse(restored.belongings.lostInventory());
        assertTrue(new Pasty().collect(restored.belongings.backpack));lost.detach();
    }); }

}

/* ADHD Pixel Dungeon (C) 2026 Chris Charlton, GPL-3.0-or-later */
package com.shatteredpixel.shatteredpixeldungeon.apd.qol;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/** The picker and the final commit must agree even after a stale selection. */
public class FullPackSwapSelectionTest {

    @Before public void init() {
        HeadlessTestSupport.start();
    }

    private static class ConditionalDrop extends Food {
        boolean canDrop = true;

        @Override public ArrayList<String> actions(Hero hero) {
            ArrayList<String> actions = new ArrayList<>();
            if (canDrop) actions.add(Item.AC_DROP);
            return actions;
        }
    }

    @Test public void previouslySelectableItemThatLosesDropActionIsRefused() {
        Hero hero = new Hero();
        Bag pack = hero.belongings.backpack;
        ConditionalDrop item = new ConditionalDrop();
        pack.items.add(item);

        assertTrue(FullPackSwap.canSwapOut(hero, pack, item));
        item.canDrop = false;
        assertFalse("A stale UI selection cannot force an illegal drop",
                FullPackSwap.canSwapOut(hero, pack, item));
        assertTrue("Rejected selection leaves the inventory intact", pack.items.contains(item));
        item.canDrop = true;
        assertTrue(FullPackSwap.canSwapOut(hero, pack, item));
    }

    @Test public void noLongerOwnedOrNullSelectionIsRejected() {
        Hero hero = new Hero();
        Bag pack = hero.belongings.backpack;
        ConditionalDrop item = new ConditionalDrop();
        pack.items.add(item);
        assertTrue(FullPackSwap.canSwapOut(hero, pack, item));
        pack.items.remove(item);
        assertFalse(FullPackSwap.canSwapOut(hero, pack, item));
        assertFalse(FullPackSwap.canSwapOut(hero, pack, null));
        assertFalse(FullPackSwap.canSwapOut(null, pack, item));
        assertFalse(FullPackSwap.canSwapOut(hero, null, item));
    }

    @Test public void backpackContainersAreNeverDropCandidates() {
        Hero hero = new Hero();
        Bag pack = hero.belongings.backpack;
        assertFalse(FullPackSwap.canSwapOut(hero, pack, pack));
    }
}

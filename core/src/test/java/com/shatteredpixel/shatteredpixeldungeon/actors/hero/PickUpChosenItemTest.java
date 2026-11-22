package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import org.junit.Test;
import static org.junit.Assert.*;

/** Chris, Nov 22: standing on a pile, any item in the "Items on tile" list can be picked up, not just the top one. */
public class PickUpChosenItemTest {
	private static Heap pile(Item... items){
		Heap h = new Heap();
		for (Item i : items) h.items.add(i);
		return h;
	}

	@Test public void picksTheChosenItemNotTheTop(){
		Item a = new Item(), b = new Item(), c = new Item();
		Heap h = pile(a, b, c);
		assertSame(b, Hero.apdChosenPickup(h, new HeroAction.PickUpItem(5, b)));
		Hero.apdRemovePicked(h, b);
		assertEquals(2, h.items.size());
		assertSame(a, h.items.get(0));
		assertSame(c, h.items.get(1));
	}

	@Test public void topItemStillRemovedCorrectly(){
		Item a = new Item(), b = new Item(), c = new Item();
		Heap h = pile(a, b, c);
		Hero.apdRemovePicked(h, a);
		assertSame(b, h.peek());
		assertEquals(2, h.items.size());
	}

	@Test public void goneItemOrShopOrPlainPickUpGivesNothing(){
		Item a = new Item(), b = new Item(), gone = new Item();
		Heap h = pile(a, b);
		assertNull("item no longer in the pile", Hero.apdChosenPickup(h, new HeroAction.PickUpItem(5, gone)));
		assertNull("ordinary pickup is not a chosen pickup", Hero.apdChosenPickup(h, new HeroAction.PickUp(5)));
		h.type = Heap.Type.FOR_SALE;
		assertNull("shop items are never taken this way", Hero.apdChosenPickup(h, new HeroAction.PickUpItem(5, a)));
		h.type = Heap.Type.CHEST;
		assertNull("containers are never taken this way", Hero.apdChosenPickup(h, new HeroAction.PickUpItem(5, a)));
	}
}

package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.apd.qol.ItemOrigins;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.GenerationEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ItemEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.TelemetryVersion;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

//"Found in" says what the item's own window says (qol.ItemOrigins), and "How you got it" how it first reached the hero's hands.
public class PublicItemOriginsTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void theSourceNumbersAreTheGamesOwn(){
		assertEquals(ItemOrigins.PICKUP, PublicItemOrigins.PICKUP);
		assertEquals(ItemOrigins.MIXED, PublicItemOrigins.MIXED);
		assertEquals(ItemOrigins.CRAFTED, PublicItemOrigins.CRAFTED);
	}

	private static Row item( Dataset items, String uidSuffix ){
		for (Row r : items.rows) if (r.id.endsWith("/" + uidSuffix)) return r;
		throw new AssertionError("no item " + uidSuffix);
	}

	private static String text( Dataset d, Row r, String col ){
		int i = d.def.indexOf(col);
		return r.isKnown(i) ? r.text(i) : null;
	}

	@Test
	public void foundInAndHowYouGotItFollowTheGame(){
		new SyntheticLog(tmp.getRoot(), "a", TelemetryVersion.CURRENT, 100).start("a", 0, "WARRIOR", "r", 0, 30, false).enter(3)
				.generated("sword", GenerationEvents.C_CHEST, "").seen("sword", "sword", ItemEvents.CAT_WEAPON)
				.acquiredVia("sword", "sword", ItemEvents.CAT_WEAPON, ItemEvents.ACQ_PICKUP)
				.acquiredVia("mail", "mail armor", ItemEvents.CAT_ARMOR, ItemEvents.ACQ_SHOP)
				.generated("potion", GenerationEvents.C_CHEST, "").seen("potion", "amber potion", ItemEvents.CAT_POTION)
				.acquiredVia("potion", "amber potion", ItemEvents.CAT_POTION, ItemEvents.ACQ_PICKUP)
				.acquiredVia("dagger", "dagger", ItemEvents.CAT_WEAPON, ItemEvents.ACQ_START_KIT)
				.acquiredVia("odd", "odd ring", ItemEvents.CAT_RING, ItemEvents.ACQ_RECONCILED)
				.end(LifecycleEvents.OUTCOME_DIED, "rat", 3, 1, 10).close(true);
		Catalog cat = new DatasetStore(tmp.getRoot()).load();
		Dataset items = cat.dataset(Datasets.ITEMS);

		Row sword = item(items, "sword");
		assertEquals("Chest", text(items, sword, "item.foundSource"));
		assertEquals(3, sword.num(items.def.indexOf("item.foundFloor")));
		assertEquals("Picked up", text(items, sword, "item.acquiredHow"));

		Row mail = item(items, "mail");
		assertEquals("Shop", text(items, mail, "item.foundSource"));
		assertEquals("Bought", text(items, mail, "item.acquiredHow"));

		//the game keeps origins for gear and wands only
		Row potion = item(items, "potion");
		assertEquals(null, text(items, potion, "item.foundSource"));
		assertEquals("Picked up", text(items, potion, "item.acquiredHow"));

		Row dagger = item(items, "dagger");
		assertEquals(null, text(items, dagger, "item.foundSource"));
		assertEquals("Starting kit", text(items, dagger, "item.acquiredHow"));

		//caught up with, not seen arriving: how is not known, and nothing is made up
		Row odd = item(items, "odd");
		assertEquals(null, text(items, odd, "item.acquiredHow"));
		assertEquals(null, text(items, odd, "item.foundSource"));

		assertFalse(items.def.column("item.foundSource").isInternal());
		assertTrue(items.def.column("item.container").isInternal());
	}

	@Test
	public void mergedFindsAreMixedAndCraftedItemsAreCrafted(){
		PublicItemOrigins.Origin a = new PublicItemOrigins.Origin(2, 0, GenerationEvents.C_CHEST);
		PublicItemOrigins.Origin b = new PublicItemOrigins.Origin(4, 0, GenerationEvents.C_TOMB);
		assertEquals(PublicItemOrigins.MIXED, PublicItemOrigins.combine(a, b).source);
		assertEquals(a, PublicItemOrigins.combine(a, new PublicItemOrigins.Origin(2, 0, GenerationEvents.C_CHEST)));
		assertEquals(PublicItemOrigins.MIXED, PublicItemOrigins.combine(a, null).source);
		assertEquals("Crafted", PublicItemOrigins.sourceLabel(PublicItemOrigins.CRAFTED));
	}
}

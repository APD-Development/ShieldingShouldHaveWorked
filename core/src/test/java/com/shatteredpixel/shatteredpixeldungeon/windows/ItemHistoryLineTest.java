package com.shatteredpixel.shatteredpixeldungeon.windows;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ItemHistory;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfBlink;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;

import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Constructor;

//Chris, Nov 25: "Anything that can't be equipped needs to be deleted": the history line shows on equipment only
public class ItemHistoryLineTest {

	static ItemHistory history;

	@BeforeClass
	public static void setup() throws Exception {
		HeadlessTestSupport.start();
		Constructor<ItemHistory> c = ItemHistory.class.getDeclaredConstructor(int.class);
		c.setAccessible(true);
		history = c.newInstance(5);
	}

	@Test
	public void equipmentShowsTheLine(){
		for (Item i : new Item[]{ new Sword(), new LeatherArmor() }){
			String line = WndInfoItem.historyLine(i, history);
			assertTrue(i.getClass().getSimpleName() + ": " + line, line.contains("Your history"));
		}
	}

	@Test
	public void anythingThatCantBeEquippedShowsNothing(){
		for (Item i : new Item[]{ new ScrollOfRemoveCurse(), new PotionOfHealing(), new Food(), new StoneOfBlink(),
				new WandOfMagicMissile() })
			assertEquals(i.getClass().getSimpleName(), "", WndInfoItem.historyLine(i, history));
	}
}

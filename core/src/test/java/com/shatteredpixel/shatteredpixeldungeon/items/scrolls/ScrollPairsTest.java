package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import static org.junit.Assert.*;

import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.watabou.utils.Reflection;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

/** Every regular scroll has one exotic twin and turns into its own runestone, and nothing else does. */
public class ScrollPairsTest {
	@Test public void everyScrollHasExactlyOneExoticTwin(){
		HeadlessTestSupport.start();
		assertEquals(12, ExoticScroll.regToExo.size());
		assertEquals(12, new HashSet<>(ExoticScroll.regToExo.values()).size());
		for (Map.Entry<Class<? extends Scroll>, Class<? extends ExoticScroll>> e : ExoticScroll.regToExo.entrySet())
			assertEquals(e.getKey(), ExoticScroll.exoToReg.get(e.getValue()));
	}

	@SuppressWarnings("unchecked")
	@Test public void everyScrollMakesItsOwnStone() throws Exception {
		HeadlessTestSupport.start();
		Field f = Scroll.ScrollToStone.class.getDeclaredField("stones");
		f.setAccessible(true);
		Map<Class<? extends Scroll>, Class<? extends Runestone>> stones = (Map<Class<? extends Scroll>, Class<? extends Runestone>>) f.get(null);
		assertEquals(ExoticScroll.regToExo.keySet(), stones.keySet());
		assertEquals(12, new HashSet<>(stones.values()).size());
		Scroll.ScrollToStone r = new Scroll.ScrollToStone();
		for (Class<? extends Scroll> c : stones.keySet()){
			ArrayList<Item> in = new ArrayList<>();
			in.add(Reflection.newInstance(c));
			assertTrue(c.getSimpleName(), r.testIngredients(in));
			assertEquals(0, r.cost(in));
		}
		for (Class<? extends ExoticScroll> c : ExoticScroll.exoToReg.keySet()){
			ArrayList<Item> in = new ArrayList<>();
			in.add(Reflection.newInstance(c));
			assertFalse(c.getSimpleName() + " should not make a stone", r.testIngredients(in));
		}
	}
}

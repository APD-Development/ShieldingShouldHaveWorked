package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.watabou.utils.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/** Chris, Nov 21: a +21 Ring of Wealth dropped a +11 War Hammer, but its info showed the normal +0 to +2. */
public class WealthGearRangeTest {
	private Hero oldHero; private int oldDepth;

	@Before public void setUp(){
		HeadlessTestSupport.start(); Generator.fullReset();
		oldHero = Dungeon.hero; oldDepth = Dungeon.depth;
		Dungeon.hero = new Hero(); Dungeon.depth = 13;
	}
	@After public void tearDown(){ Dungeon.hero = oldHero; Dungeon.depth = oldDepth; }

	@Test public void aPlus21RingMakesPlus11GearAndTheInfoSaysSo(){
		// a +21 ring works at 22 (worn uncursed rings work one level up); the drop code gets 22 - 1 = 21
		Random.pushGenerator(5);
		try {
			int gear = 0;
			for (int i = 0; i < 300; i++){
				Item it = RingOfWealth.genEquipmentDrop(21);
				if (it instanceof Artifact || !it.isUpgradable()) continue;
				gear++;
				assertEquals(it.name(), 11, it.level());
				assertNotNull(it.apdGenerationRange);
				assertEquals(11, it.apdGenerationRange.min);
				assertEquals(11, it.apdGenerationRange.max);
				assertFalse(it.cursed);
			}
			assertTrue(gear > 100);
		} finally { Random.popGenerator(); }
	}

	@Test public void aLowRingShowsARealRange(){
		Random.pushGenerator(9);
		try {
			for (int i = 0; i < 200; i++){
				Item it = RingOfWealth.genEquipmentDrop(2); // a +2 ring: works at 3, minimum (2+1)/2 = 1
				if (it instanceof Artifact || !it.isUpgradable()) continue;
				assertEquals(1, it.apdGenerationRange.min);
				assertEquals(2, it.apdGenerationRange.max);
				assertTrue(it.level() >= 1 && it.level() <= 2);
			}
		} finally { Random.popGenerator(); }
	}
}

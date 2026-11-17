package com.shatteredpixel.shatteredpixeldungeon.items;

import static org.junit.Assert.*;

import org.junit.Test;

public class RingDustTest {
	@Test public void costsMatchArcaneResinUpToPlusThree(){
		assertEquals(3, RingDust.applicationLevelLimit());
		assertEquals(ArcaneResin.applicationLevelLimit(), RingDust.applicationLevelLimit());
		for (int level = 0; level < 3; level++) assertEquals(ArcaneResin.applicationCost(level), RingDust.applicationCost(level));
		assertEquals("a -1 ring still costs at least one dust", 1, RingDust.applicationCost(-1));
	}

	@Test public void yieldIsTwoPerLevelPlusOneAndNeverZero(){
		assertEquals(2, RingDust.Recipe.dustQuantity(0));
		assertEquals(4, RingDust.Recipe.dustQuantity(1));
		assertEquals(8, RingDust.Recipe.dustQuantity(3));
		assertEquals(2, RingDust.Recipe.dustQuantity(-2));
		assertEquals(5, RingDust.Recipe.energyCost());
	}

	@Test public void onlyRingsQualifyForTheRecipe(){
		java.util.ArrayList<Item> one = new java.util.ArrayList<>();
		one.add(new ArcaneResin());
		assertFalse(new RingDust.Recipe().testIngredients(one));
		assertFalse(new RingDust.Recipe().testIngredients(new java.util.ArrayList<>()));
		assertFalse(RingDust.canApply(new ArcaneResin()));
		assertFalse(RingDust.canApply(null));
	}

	@Test public void dustIsStackableAndAlwaysIdentified(){
		RingDust d = new RingDust();
		assertTrue(d.stackable);
		assertTrue(d.isIdentified());
		assertFalse(d.isUpgradable());
		assertEquals(90, d.quantity(3).value());
	}
}

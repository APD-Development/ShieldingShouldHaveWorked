package com.shatteredpixel.shatteredpixeldungeon.items;

import static org.junit.Assert.*;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;

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

	private static Ring ring(int level, boolean identified, boolean cursed){
		HeadlessTestSupport.start();
		RingOfArcana r = new RingOfArcana();
		r.anonymize();
		r.level(level);
		r.levelKnown = identified;
		r.cursedKnown = identified;
		r.cursed = cursed;
		return r;
	}

	@Test public void dustRaisesARealRingOneLevelAndSpendsTheCost(){
		com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero oldHero = Dungeon.hero;
		Dungeon.hero = null; // stack is not in a backpack
		try {
			Ring r = ring(1, true, false);
			RingDust dust = new RingDust();
			dust.quantity(5);
			assertTrue(RingDust.canApply(r));
			assertTrue(dust.applyTo(r));
			assertEquals(2, r.level());
			assertEquals("+1 to +2 costs 2", 3, dust.quantity());
			assertTrue(dust.applyTo(r));
			assertEquals(3, r.level());
			assertEquals(0, dust.quantity());
			assertFalse("capped at +3", RingDust.canApply(r));
		} finally { Dungeon.hero = oldHero; }
	}

	@Test public void refusesCursedUnidentifiedAndUnaffordable(){
		RingDust dust = new RingDust();
		dust.quantity(1);
		assertFalse(RingDust.canApply(ring(0, false, false)));
		assertFalse(RingDust.canApply(ring(0, true, true)));
		Ring two = ring(2, true, false);
		assertFalse("needs 3, has 1", dust.applyTo(two));
		assertEquals(2, two.level());
		assertEquals(1, dust.quantity());
	}

	@Test public void recipeTurnsAKnownUncursedRingIntoDust(){
		Ring r = ring(1, true, false);
		java.util.ArrayList<Item> in = new java.util.ArrayList<>();
		in.add(r);
		RingDust.Recipe recipe = new RingDust.Recipe();
		assertTrue(recipe.testIngredients(in));
		Item out = recipe.brew(in);
		assertTrue(out instanceof RingDust);
		assertEquals(4, out.quantity());
		assertEquals("ring consumed", 0, r.quantity());
		in.clear(); in.add(ring(0, true, true));
		assertFalse("cursed rings are refused", recipe.testIngredients(in));
	}

	@Test public void ringsCanActuallyGoInTheAlchemyPot(){
		//bug found Nov 18: rings are equipment, and equipment other than thrown weapons was refused by the pot
		assertTrue(Recipe.usableInRecipe(ring(0, true, false)));
		assertFalse("cursed rings stay out", Recipe.usableInRecipe(ring(0, true, true)));
		assertTrue("unknown rings go in like unknown potions (Chris, Nov 20)", Recipe.usableInRecipe(ring(0, false, false)));
		assertFalse("an unknown ring that is actually cursed still stays out", Recipe.usableInRecipe(ring(0, false, true)));
		java.util.ArrayList<Item> in = new java.util.ArrayList<>();
		in.add(ring(1, true, false));
		java.util.ArrayList<Recipe> found = Recipe.findRecipes(in);
		assertEquals(1, found.size());
		assertTrue(found.get(0) instanceof RingDust.Recipe);
	}

	@Test public void dissolvingAnUnknownRingIdentifiesItsTypeAndGivesItsRealDust(){ // Chris, Nov 20
		Ring r = ring(2, false, false);
		assertFalse(r.isKnown());
		java.util.ArrayList<Item> in = new java.util.ArrayList<>(); in.add(r);
		RingDust.Recipe recipe = new RingDust.Recipe();
		assertTrue(recipe.testIngredients(in));
		Item out = recipe.brew(in);
		assertEquals("real level +2 gives 6", 6, out.quantity());
		assertTrue("ring type is now known", r.isKnown());
		assertTrue(new RingOfArcana().isKnown());
	}

	@Test public void aWornRingCannotGoInThePot(){ // bug, Chris Nov 20: a worn ring was dissolved but stayed equipped
		Ring r = ring(0, true, false);
		com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero prev = Dungeon.hero;
		Dungeon.hero = new com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero();
		Dungeon.hero.belongings.ring = r;
		try { assertFalse(Recipe.usableInRecipe(r));
			java.util.ArrayList<Item> in = new java.util.ArrayList<>(); in.add(r);
			assertFalse(new RingDust.Recipe().testIngredients(in));
		} finally { Dungeon.hero = prev; }
	}

	@Test public void aUsedUpWornRingFromAnOlderSaveIsClearedOnLoad(){
		Ring r = ring(0, true, false);
		com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero prev = Dungeon.hero;
		Dungeon.hero = new com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero();
		try {
		Dungeon.hero.belongings.ring = r; r.quantity(0);
		com.watabou.utils.Bundle b = new com.watabou.utils.Bundle();
		Dungeon.hero.belongings.storeInBundle(b);
		Dungeon.hero.belongings.ring = null;
		Dungeon.hero.belongings.restoreFromBundle(b);
		assertNull(Dungeon.hero.belongings.ring);
		} finally { Dungeon.hero = prev; }
	}
}

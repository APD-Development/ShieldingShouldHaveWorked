package com.shatteredpixel.shatteredpixeldungeon.items;

import static org.junit.Assert.*;

import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;

import org.junit.Test;

/** Every loot table lines up: one weight per item, no negative weights, and every listed item belongs to its category. */
public class GeneratorTablesTest {
	private static void checkDeck(Generator.Category c, float[] probs, String name){
		if (probs == null) return;
		assertEquals(c + " " + name + " length", c.classes.length, probs.length);
		for (float p : probs) assertTrue(c + " " + name + " has a negative weight", p >= 0);
	}

	@Test public void tablesLineUp(){
		HeadlessTestSupport.start();
		Generator.fullReset();
		for (Generator.Category c : Generator.Category.values()){
			assertNotNull(c + " has no classes", c.classes);
			if (c.classes.length == 0) continue; // WEAPON / MISSILE parents hand off to their tier tables
			checkDeck(c, c.probs, "probs");
			checkDeck(c, c.defaultProbs, "defaultProbs");
			checkDeck(c, c.defaultProbs2, "defaultProbs2");
			checkDeck(c, c.defaultProbsTotal, "defaultProbsTotal");
			for (Class<?> k : c.classes){
				assertTrue(k.getSimpleName() + " listed under " + c, c.superClass.isAssignableFrom(k));
			}
		}
	}
}

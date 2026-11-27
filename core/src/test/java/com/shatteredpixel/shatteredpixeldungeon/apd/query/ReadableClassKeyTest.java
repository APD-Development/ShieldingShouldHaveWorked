package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

//Trial jurors, Nov 27: "actors.mobs.Piranha", "actors.mobs.Snake" and "Bomb$Conjure" showed on screen as raw code names
public class ReadableClassKeyTest {

	@Test
	public void classKeysInTextColumnsShowAsGameNames(){
		assertEquals("Piranha", CellFormatter.readable("actors.mobs.Piranha"));
		assertEquals("Snake", CellFormatter.readable("actors.mobs.Snake"));
		assertEquals("Great Crab", CellFormatter.readable("com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GreatCrab@1f2e"));
		assertEquals("Conjured Bomb", CellFormatter.readable("Bomb$ConjuredBomb"));
		assertEquals("Bomb Conjure", CellFormatter.readable("Bomb$Conjure"));
	}

	@Test
	public void ordinaryTextIsLeftAlone(){
		for (String s : new String[]{"gnoll scout", "Piranha", "crazy thief", "Rat", "floor 3", "ring of haste", "", "+3 sword"}){
			assertEquals(s, CellFormatter.readable(s));
		}
	}

	@Test
	public void nothingShownLooksLikeCode(){
		for (String s : new String[]{"actors.mobs.Piranha", "actors.mobs.Snake", "Bomb$Conjure", "items.bombs.Bomb$ConjuredBomb",
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Snake@abc"}){
			String shown = CellFormatter.readable(s);
			assertFalse(s + " -> " + shown, shown.contains(".") || shown.contains("$") || shown.contains("@"));
		}
	}

	//A filter made by tapping a "Killed by" cell keeps the raw key; the status line describing it must not show code
	@Test
	public void filterDescriptionShowsGameName(){
		String d = StatusText.describe(Datasets.deaths(), Filter.text("death.cause", Op.EQ, "actors.mobs.Piranha"));
		assertEquals("Killed by is \"Piranha\"", d);
	}
}

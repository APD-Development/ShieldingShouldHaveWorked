package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TextCheckTest {

	@Test public void catchesCodeNamesAndMarkers(){
		assertNotNull(TextCheck.problem("You were killed by actors.mobs.Piranha"));
		assertNotNull(TextCheck.problem("Bomb$Conjure exploded"));
		assertNotNull(TextCheck.problem("see com.shatteredpixel.foo"));
		assertNotNull(TextCheck.problem("!!!NO TEXT FOUND!!!"));
		assertNotNull(TextCheck.problem("It deals %d damage"));
	}

	@Test public void catchesWrongPlurals(){
		assertNotNull(TextCheck.problem("This lasts 1 turns."));
		assertNotNull(TextCheck.problem("You have 3 charge left."));
	}

	@Test public void acceptsNormalText(){
		assertNull(TextCheck.problem("You found 12 gold."));
		assertNull(TextCheck.problem("This lasts 1 turn."));
		assertNull(TextCheck.problem("You have 3 charges left."));
		assertNull(TextCheck.problem("A 2 turn delay."));
		assertNull(TextCheck.problem("The sewer snake bites you."));
	}

	@Test public void cleanModeSwears(){
		assertNotNull(TextCheck.swear("Well, shit."));
		assertNotNull(TextCheck.swear("Get Fucked"));
		assertNull(TextCheck.swear("The cockatrice and the scunthorpe pass by."));
		assertNull(TextCheck.swear("A shitake? No, a mushroom."));
	}
}

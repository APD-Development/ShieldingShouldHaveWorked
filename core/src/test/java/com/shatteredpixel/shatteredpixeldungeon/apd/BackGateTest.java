package com.shatteredpixel.shatteredpixeldungeon.apd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.watabou.input.BackGate;

import org.junit.Test;

//One press of Back is one Back, however many routes deliver it; two real presses are two.
public class BackGateTest {

	private static final long MS = 1_000_000L;

	@Test
	public void theSamePressByTwoRoutesCountsOnce(){
		BackGate.reset();
		assertTrue(BackGate.accept(1000 * MS));
		assertFalse("the second route delivering the same press", BackGate.accept(1000 * MS + 5 * MS));
		assertFalse(BackGate.accept(1000 * MS + 60 * MS));
	}

	@Test
	public void twoRealPressesAreTwoBacks(){
		BackGate.reset();
		assertTrue(BackGate.accept(5000 * MS));
		assertTrue("a person cannot press twice this fast, so this is a second press", BackGate.accept(5000 * MS + 200 * MS));
		assertTrue(BackGate.accept(5000 * MS + 600 * MS));
	}

	@Test
	public void theFirstPressIsAlwaysAccepted(){
		BackGate.reset();
		assertTrue(BackGate.accept(0));
	}
}

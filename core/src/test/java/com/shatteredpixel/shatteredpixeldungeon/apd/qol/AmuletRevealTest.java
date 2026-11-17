package com.shatteredpixel.shatteredpixeldungeon.apd.qol;

import static org.junit.Assert.*;

import org.junit.Test;

public class AmuletRevealTest {
	@Test public void nothingToRevealWithoutALevelOrHero(){
		assertEquals(0, AmuletReveal.reveal(null));
		assertFalse(AmuletReveal.applies(null));
	}
}

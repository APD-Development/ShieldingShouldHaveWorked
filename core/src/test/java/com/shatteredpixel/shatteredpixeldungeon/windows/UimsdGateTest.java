package com.shatteredpixel.shatteredpixeldungeon.windows;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.shatteredpixel.shatteredpixeldungeon.apd.demo.UimsdUnlock;

import org.junit.Test;

//The demo-unlock warning is the owner's own wording; it must not drift.
public class UimsdGateTest {

	@Test
	public void theWarningAndBothChoicesAreWordForWord(){
		assertEquals("ARE YOU SURE ABOUT THIS?", UimsdGate.WARNING_TITLE);
		assertEquals("UIMSD is an absurdly detailed statistics database. You can unlock it immediately with 10 demo runs, but there's a very good chance you'll have absolutely no idea what you're looking at.\n\nI strongly recommend playing 10 actual runs first.",
				UimsdGate.WARNING_TEXT);
		assertEquals("I LIKE CONFUSION (generate demo runs)", UimsdGate.CHOICE_CONFUSION);
		assertEquals("I'LL PLAY 10 RUNS (decline and return to normal gameplay)", UimsdGate.CHOICE_PLAY);
		assertEquals(10, UimsdUnlock.DEMO_RUNS);
		assertEquals(10, UimsdUnlock.TEN_RUNS);
	}

	@Test
	public void theGateIsOffUntilTheOwnerSwitchesItOn(){
		assertFalse(UimsdUnlock.ENFORCED);
	}
}

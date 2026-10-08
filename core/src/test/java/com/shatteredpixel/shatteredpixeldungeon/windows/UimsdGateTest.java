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
		assertEquals("I LIKE CONFUSION", UimsdGate.CHOICE_CONFUSION);
		assertEquals("I'LL PLAY 10 RUNS", UimsdGate.CHOICE_PLAY);
		assertEquals("THIS BUTTON IS EFFECTIVELY USELESS TILL 10 RUNS HAVE BEEN COMPLETED.", UimsdGate.LOCKED_ARROW_TEXT);
		assertEquals(10, UimsdUnlock.DEMO_RUNS);
		assertEquals(10, UimsdUnlock.TEN_RUNS);
	}

	@Test
	public void theGateIsOffUntilTheOwnerSwitchesItOn(){
		assertFalse(UimsdUnlock.ENFORCED);
	}

	@Test
	public void theTitleArrowKeepsItsUsualTextWhileTheGateIsOff(){
		assertEquals("HEY, GO HERE FOR DRUGS", UimsdGate.titleAnnotation("HEY, GO HERE FOR DRUGS"));
		assertEquals("the guided tour is open too", true, UimsdGate.tourAvailable());
	}
}

package com.shatteredpixel.shatteredpixeldungeon.windows;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.shatteredpixel.shatteredpixeldungeon.apd.demo.UimsdUnlock;

import java.io.File;

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

	private static final class Mem implements UimsdUnlock.Flags {
		boolean unlocked, demoUsed, tour, pending, shown;
		public boolean unlocked(){ return unlocked; }
		public void setUnlocked(){ unlocked = true; }
		public boolean demoUsed(){ return demoUsed; }
		public void setDemoUsed(){ demoUsed = true; }
		public boolean tourUnlocked(){ return tour; }
		public void setTourUnlocked(){ tour = true; }
		public boolean promptPending(){ return pending; }
		public void setPromptPending(){ pending = true; }
		public boolean promptShown(){ return shown; }
		public void setPromptShown(){ shown = true; }
	}

	@Test
	public void theArrowSaysTheLockedMessageThenTheTenthRunPromptOnceThenItsUsualText() throws Exception {
		assertEquals("HEY, YOU SHOULD PROBABLY GO HERE NOW.", UimsdGate.TENTH_RUN_ARROW_TEXT);
		File empty = java.nio.file.Files.createTempDirectory("apd-arrow").toFile();
		Mem flags = new Mem();
		assertEquals("fewer than ten runs: the locked message, exactly",
				"THIS BUTTON IS EFFECTIVELY USELESS TILL 10 RUNS HAVE BEEN COMPLETED.", UimsdGate.annotationFor(flags, empty, true, "USUAL"));
		assertEquals("gate off: always the usual text", "USUAL", UimsdGate.annotationFor(flags, empty, false, "USUAL"));
		//the tenth run queued the prompt: it shows once
		flags.setUnlocked();
		flags.setPromptPending();
		assertEquals("HEY, YOU SHOULD PROBABLY GO HERE NOW.", UimsdGate.annotationFor(flags, empty, true, "USUAL"));
		assertEquals("never again", "USUAL", UimsdGate.annotationFor(flags, empty, true, "USUAL"));
		assertEquals("USUAL", UimsdGate.annotationFor(flags, empty, true, "USUAL"));
		//opened through the demo shortcut: no locked message any more
		Mem demo = new Mem();
		demo.setUnlocked();
		demo.setDemoUsed();
		assertEquals("USUAL", UimsdGate.annotationFor(demo, empty, true, "USUAL"));
	}
}

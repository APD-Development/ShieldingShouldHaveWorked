package com.shatteredpixel.shatteredpixeldungeon.apd.qol;

import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import org.junit.Test;
import static org.junit.Assert.*;

/** Chris, Nov 23: dev-build voice controls turn phrases into the same actions as keys. */
public class VoiceCommandsTest {
	private static Object act(String s){ VoiceCommands.Match m = VoiceCommands.parse(s); return m == null ? null : (m.stop ? "STOP" : m.action); }

	@Test public void directionsAndDiagonals(){
		assertEquals(SPDAction.N, act("North"));
		assertEquals(SPDAction.N, act("go north"));
		assertEquals(SPDAction.NE, act("north east"));
		assertEquals(SPDAction.NE, act("Northeast."));
		assertEquals(SPDAction.SW, act("down left"));
		assertEquals(SPDAction.W, act("left"));
		assertEquals(SPDAction.E, act("move right"));
	}
	@Test public void actionsAndQuickslots(){
		assertEquals(SPDAction.WAIT_OR_PICKUP, act("pick up"));
		assertEquals(SPDAction.TAG_ATTACK, act("attack"));
		assertEquals(SPDAction.REST, act("rest"));
		assertEquals(SPDAction.WAIT, act("wait"));
		assertEquals(SPDAction.QUICKSLOT_3, act("quick slot three"));
		assertEquals(SPDAction.QUICKSLOT_2, act("slot to"));
		assertEquals(SPDAction.QUICKSLOT_4, act("quickslot 4"));
		assertEquals(SPDAction.BACK, act("cancel"));
		assertEquals("STOP", act("stop listening"));
	}
	@Test public void wordsInsideOtherWordsDontTrigger(){
		assertNull(act("upstairs"));
		assertNull(act("weather"));
		assertNull(act(""));
	}
	@Test public void voiceKeysAreBoundOnlyOnceAPlatformExists(){
		VoiceCommands.setPlatform(new VoiceCommands.Platform(){ public void start(){} public void stop(){} });
		try {
			int code = VoiceCommands.BASE_CODE; // N is first
			assertEquals(SPDAction.N, KeyBindings.getActionForKey(new KeyEvent(code, true)));
			assertTrue(KeyBindings.isKeyBound(code));
		} finally {
			KeyEvent.discardPendingEvents();
			VoiceCommands.setPlatform(null);
		}
	}

	//Chris, Nov 23: "go" then a breath then "east" acted on "go" alone, then heard the direction as "eat"
	@org.junit.Test
	public void aFirstWordAloneWaitsForTheRest(){
		org.junit.Assert.assertTrue(VoiceCommands.waitsForMore("go"));
		org.junit.Assert.assertTrue(VoiceCommands.waitsForMore("Go."));
		org.junit.Assert.assertTrue(VoiceCommands.waitsForMore("attack"));
		org.junit.Assert.assertFalse(VoiceCommands.waitsForMore("go east"));
		org.junit.Assert.assertFalse(VoiceCommands.waitsForMore("east"));
		org.junit.Assert.assertFalse(VoiceCommands.waitsForMore("rest"));
		org.junit.Assert.assertFalse(VoiceCommands.waitsForMore(""));
	}
}

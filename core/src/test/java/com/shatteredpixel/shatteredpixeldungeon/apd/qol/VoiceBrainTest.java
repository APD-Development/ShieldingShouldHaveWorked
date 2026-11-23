package com.shatteredpixel.shatteredpixeldungeon.apd.qol;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

/** Chris, Nov 23: the on-phone model that understands everyday voice phrasing. */
public class VoiceBrainTest {
	@Test public void cleansNumbersDiagonalsAndMishearings(){
		assertEquals("north 3 times", VoiceBrain.clean("North, three times!"));
		assertEquals("northeast", VoiceBrain.clean("north east"));
		assertEquals("north", VoiceBrain.clean("nort"));         // one letter off snaps to a game word
		assertEquals("attack the rat", VoiceBrain.clean("atack the rat"));
		assertEquals("north 2 times", VoiceBrain.clean("north two times"));
		assertEquals("north 2 times", VoiceBrain.clean("north to times"));
		assertEquals("downstairs", VoiceBrain.clean("down stairs"));
		assertEquals("slot 4", VoiceBrain.clean("quick slot for"));
	}
	@Test public void splitsMultiStepPhrases(){
		assertEquals(Arrays.asList("east", "north"), VoiceBrain.clauses(VoiceBrain.clean("go east then north")));
		assertEquals(Arrays.asList("drink healing", "attack"), VoiceBrain.clauses(VoiceBrain.clean("drink healing and then attack")));
		assertEquals(Arrays.asList("north 3 times", "west"), VoiceBrain.clauses(VoiceBrain.clean("north three times and west")));
	}
	@Test public void repeatCountsAreCappedForSafety(){
		assertEquals(3, VoiceBrain.count("north 3 times"));
		assertEquals(4, VoiceBrain.count("4 north"));
		assertEquals(1, VoiceBrain.count("north"));
		assertEquals(9, VoiceBrain.count("north 50 times"));
	}
	@Test public void directionsIncludingSynonymsAndDiagonals(){
		assertArrayEquals(new int[]{0,-1}, VoiceBrain.direction("north"));
		assertArrayEquals(new int[]{1,-1}, VoiceBrain.direction("northeast"));
		assertArrayEquals(new int[]{-1,1}, VoiceBrain.direction("down left"));
		assertArrayEquals(new int[]{1,0}, VoiceBrain.direction("right 2 times"));
		assertNull(VoiceBrain.direction("attack"));
	}
	@Test public void itemAndEnemyNamesMatchOnWhatIsShown(){
		List<String> healing = VoiceBrain.keywords("a healing potion");
		assertTrue(VoiceBrain.nameScore(healing, "Potion of Healing") > 0);
		assertEquals(0, VoiceBrain.nameScore(healing, "Potion of Strength"));
		assertTrue("unidentified potions go by their colour", VoiceBrain.nameScore(VoiceBrain.keywords("the amber one"), "amber potion") > 0);
		assertTrue("small slips still match", VoiceBrain.nameScore(VoiceBrain.keywords("gnol"), "gnoll scout") > 0);
		assertEquals(0, VoiceBrain.nameScore(VoiceBrain.keywords("crab"), "marsupial rat"));
	}
	@Test public void outsideARunOnlySimpleKeysAreUnderstood(){
		VoiceBrain.Plan p = VoiceBrain.plan("inventory");
		assertNotNull(p);
		assertEquals(1, p.steps.size());
		assertTrue(VoiceBrain.plan("stop listening").stop);
		assertNull(VoiceBrain.plan("banana pancakes"));
		assertNull("needs a run to know where the stairs are", VoiceBrain.plan("go downstairs"));
	}
}

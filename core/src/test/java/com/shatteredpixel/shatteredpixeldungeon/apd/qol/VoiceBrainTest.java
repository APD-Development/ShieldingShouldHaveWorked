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
		assertEquals("eat the meat", VoiceBrain.clean("eat the meat"));   // item names are not snapped to verbs
		assertEquals("read bread", VoiceBrain.clean("read bread"));
	}
	@Test public void commandWordsAreNotSnappedToNeighbours(){
		assertEquals("cast wand at rat", VoiceBrain.clean("cast wand at rat"));
		assertNull("cast must not walk east", VoiceBrain.direction(VoiceBrain.clean("cast wand at rat")));
		assertEquals("look is one letter from loot: the search phrase must survive", "look around", VoiceBrain.clean("look around"));
		assertEquals("look for secrets", VoiceBrain.clean("look for secrets"));
		assertEquals("loot the chest", VoiceBrain.clean("loot the chest"));
		assertEquals("right is one letter from fight: it must stay a direction", "down right", VoiceBrain.clean("down right"));
		assertEquals("till is one letter from kill: kept so 'till dead' is stripped from the target", "attack rat till dead", VoiceBrain.clean("attack rat till dead"));
		assertTrue("one more time repeats (time was snapped to times)", VoiceBrain.isRepeat(VoiceBrain.clean("one more time")));
		assertEquals("times still snaps", "north 2 times", VoiceBrain.clean("north 2 tmes"));
		assertArrayEquals("down right steps south-east", new int[]{1,1}, VoiceBrain.direction(VoiceBrain.clean("down right")));
		assertArrayEquals(new int[]{1,0}, VoiceBrain.direction(VoiceBrain.clean("right")));
		assertEquals("near is one letter from wear", "attack the near rat", VoiceBrain.clean("attack the near rat"));
		assertEquals("attack rat until dead", VoiceBrain.clean("attack rat until dead"));
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
		assertEquals("documented \"north x3\" form", 3, VoiceBrain.count(VoiceBrain.clean("north x3")));
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
		assertNull(VoiceBrain.plan("kill the rat"));
		assertNull(VoiceBrain.plan("ability"));
	}
	@Test public void pickUpIsNotAStepNorth(){
		assertNull("in a run, \"pick up\" walked north", VoiceBrain.moveDirection("pick up"));
		assertNull(VoiceBrain.moveDirection("pick it up"));
		assertNull(VoiceBrain.moveDirection("slot 2 up"));
		assertArrayEquals(new int[]{0,-1}, VoiceBrain.moveDirection("go up"));
		assertArrayEquals(new int[]{1,0}, VoiceBrain.moveDirection("east 3 times"));
	}
	@Test public void enemyDirectionCones(){
		assertTrue(VoiceBrain.inDirection(0, -3, new int[]{0,-1}));
		assertTrue("slightly off straight north still counts as north", VoiceBrain.inDirection(1, -3, new int[]{0,-1}));
		assertFalse(VoiceBrain.inDirection(3, -1, new int[]{0,-1}));
		assertTrue(VoiceBrain.inDirection(2, -1, new int[]{1,-1}));
		assertFalse(VoiceBrain.inDirection(-2, -1, new int[]{1,-1}));
		assertTrue(VoiceBrain.inDirection(-4, 1, new int[]{-1,0}));
	}
	@Test public void qualifierWordsAreNotTreatedAsNames(){
		assertEquals(Arrays.asList("rat"), VoiceBrain.keywords("the far rat to the north"));
		assertTrue(VoiceBrain.keywords("the other one on the left").isEmpty());
	}
	@Test public void wakeWordFiltersAndStrips(){
		assertEquals(Arrays.asList("north"), VoiceCommands.wake(Arrays.asList("turn north", "Dungeon, north"), true));
		assertTrue(VoiceCommands.wake(Arrays.asList("north"), true).isEmpty());
		assertEquals(Arrays.asList("north"), VoiceCommands.wake(Arrays.asList("north"), false));
	}
	@Test public void fastEngineWordListCoversTheCommands(){
		VoiceBrain.refreshVocabulary();
		java.util.Set<String> words = new java.util.HashSet<>(Arrays.asList(VoiceBrain.vocabulary.split(" ")));
		for (String w : "north southwest attack kill ability drink read eat zap throw slot three times then stop again dungeon listening downstairs door search pick up".split(" "))
			assertTrue(w, words.contains(w));
		assertFalse("only plain words, the engine can't take digits or brackets", VoiceBrain.vocabulary.matches(".*[^a-z ].*"));
	}
}

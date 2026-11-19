package com.shatteredpixel.shatteredpixeldungeon.apd.community;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class ReviewSetupTest {

	@Test public void capturedSetupReadsBackExactly(){
		ReviewSetup s = ReviewSetup.parse(ReviewSetup.capture(HeroClass.HUNTRESS, 37, 4, true, 2, false));
		assertNotNull(s);
		assertEquals(HeroClass.HUNTRESS, s.heroClass);
		assertEquals(37, s.challenges);
		assertEquals(4, s.difficulty);
		assertEquals(2, s.exotic);
		assertFalse(s.customRules);
	}

	@Test public void unknownDifficultyOrCustomRulesAreNeverGuessed(){
		assertNull(ReviewSetup.parse(ReviewSetup.capture(HeroClass.WARRIOR, 0, 3, false, 0, false)));
		assertNull(ReviewSetup.parse(ReviewSetup.capture(HeroClass.WARRIOR, 0, 3, true, 0, true)));
		assertFalse(ReviewSetup.reviewable(false, false));
		assertFalse(ReviewSetup.reviewable(true, true));
		assertTrue(ReviewSetup.reviewable(true, false));
	}

	@Test public void malformedSetupsAreRefused(){
		Map<String, String> m = ReviewSetup.capture(HeroClass.MAGE, 0, 3, true, 0, false);
		m.put("heroClass", "NOT_A_CLASS"); assertNull(ReviewSetup.parse(m));
		m = ReviewSetup.capture(HeroClass.MAGE, 0, 3, true, 0, false);
		m.put("difficulty", "9"); assertNull(ReviewSetup.parse(m));
		m = ReviewSetup.capture(HeroClass.MAGE, 0, 3, true, 0, false);
		m.put("challenges", "-1"); assertNull(ReviewSetup.parse(m));
		m = ReviewSetup.capture(HeroClass.MAGE, 0, 3, true, 0, false);
		m.remove("exotic"); assertNull(ReviewSetup.parse(m));
		assertNull(ReviewSetup.parse(null));
		// Old two-field setups (sent before this change) have no difficulty: refused, not defaulted.
		m = new java.util.HashMap<>(); m.put("heroClass", "MAGE"); m.put("challenges", "0");
		assertNull(ReviewSetup.parse(m));
	}

	@Test public void reachingTheDeathFloorCounts(){
		assertTrue(ReviewSetup.survived(7, 7));
		assertTrue(ReviewSetup.survived(9, 7));
		assertFalse(ReviewSetup.survived(6, 7));
	}
}

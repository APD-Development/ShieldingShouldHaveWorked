package com.shatteredpixel.shatteredpixeldungeon.apd.community;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class ReviewSetupTest {

	@Test public void capturedSetupReadsBackExactly(){
		ReviewSetup s = ReviewSetup.parse(ReviewSetup.capture(HeroClass.HUNTRESS, 37, 4, true, 2, null));
		assertNotNull(s);
		assertEquals(HeroClass.HUNTRESS, s.heroClass);
		assertEquals(37, s.challenges);
		assertEquals(4, s.difficulty);
		assertEquals(2, s.exotic);
		assertFalse(s.customRules);
	}

	@Test public void unknownDifficultyOrCustomRulesAreNeverGuessed(){
		assertNull(ReviewSetup.parse(ReviewSetup.capture(HeroClass.WARRIOR, 0, 3, false, 0, null)));
		assertFalse(ReviewSetup.reviewable(false));
		assertTrue(ReviewSetup.reviewable(true));
		// An old flag-only custom-rules setup has no settings to lock to: refused
		Map<String, String> old = ReviewSetup.capture(HeroClass.WARRIOR, 0, 3, true, 0, null);
		old.put("customRules", "1"); assertNull(ReviewSetup.parse(old));
		old.remove("customRules"); assertNull(ReviewSetup.parse(old));
	}

	@Test public void customRulesTravelExactlyAndAreLocked(){
		com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules rules =
				com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules.defaults()
						.with(com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules.Slider.UPGRADE_SCROLLS, 10);
		Map<String, String> m = ReviewSetup.capture(HeroClass.ROGUE, 5, 3, true, 1, rules);
		ReviewSetup s = ReviewSetup.parse(m);
		assertTrue(s.customRules);
		assertTrue(java.util.Arrays.equals(rules.values(), s.rules.values()));
		assertEquals(5, s.challenges);
		m.put("customRules", "1,999"); assertNull("incomplete rules refused", ReviewSetup.parse(m));
		assertNull(ReviewSetup.parse(ReviewSetup.capture(HeroClass.ROGUE, 0, 3, true, 0, null)).rules);
	}

	@Test public void malformedSetupsAreRefused(){
		Map<String, String> m = ReviewSetup.capture(HeroClass.MAGE, 0, 3, true, 0, null);
		m.put("heroClass", "NOT_A_CLASS"); assertNull(ReviewSetup.parse(m));
		m = ReviewSetup.capture(HeroClass.MAGE, 0, 3, true, 0, null);
		m.put("difficulty", "9"); assertNull(ReviewSetup.parse(m));
		m = ReviewSetup.capture(HeroClass.MAGE, 0, 3, true, 0, null);
		m.put("challenges", "-1"); assertNull(ReviewSetup.parse(m));
		m = ReviewSetup.capture(HeroClass.MAGE, 0, 3, true, 0, null);
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

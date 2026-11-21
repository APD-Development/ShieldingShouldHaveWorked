package com.shatteredpixel.shatteredpixeldungeon.apd.qol;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/** Chris, Nov 21: the Duelist lock-on stayed on a far, sleeping bat while another bat came right at him. */
public class DuelistLockOnTest {
	private final Map<String, Integer> dist = new HashMap<>();
	private final Set<String> reach = new HashSet<>();

	private String pick(String last, String... visible){
		List<String> v = Arrays.asList(visible);
		return DuelistLockOn.pick(last, v, reach::contains, dist::get);
	}

	@Test public void chrisCaseLastTargetOutOfReachLosesTheLockToTheBatInReach(){
		dist.put("leftBat", 6); dist.put("northBat", 1);
		reach.add("northBat");
		assertEquals("northBat", pick("leftBat", "leftBat", "northBat"));
	}

	@Test public void lastTargetInReachKeepsTheLockEvenIfAnotherIsCloser(){
		dist.put("a", 2); dist.put("b", 1);
		reach.add("a"); reach.add("b");
		assertEquals("a", pick("a", "a", "b"));
	}

	@Test public void closestInReachWinsOverACloserOneOutOfReach(){
		// e.g. a rapier lunge can't hit an adjacent enemy
		dist.put("adjacent", 1); dist.put("twoAway", 2);
		reach.add("twoAway");
		assertEquals("twoAway", pick(null, "adjacent", "twoAway"));
	}

	@Test public void nobodyInReachLocksTheClosestVisibleEnemy(){
		dist.put("far", 7); dist.put("near", 3);
		assertEquals("near", pick("far", "far", "near"));
		assertEquals("near", pick(null, "far", "near"));
	}

	@Test public void tiesKeepTheLastTargetThenTheFirstListed(){
		dist.put("a", 2); dist.put("b", 2);
		assertEquals("b", pick("b", "a", "b"));
		assertEquals("a", pick(null, "a", "b"));
		reach.add("a"); reach.add("b");
		assertEquals("a", pick(null, "a", "b"));
	}

	@Test public void lastTargetNoLongerVisibleIsIgnoredAndNoEnemiesMeansNoLock(){
		dist.put("gone", 1); dist.put("other", 4);
		reach.add("gone");
		assertEquals("other", pick("gone", "other"));
		assertNull(pick("gone"));
		assertNull(pick(null));
	}
}

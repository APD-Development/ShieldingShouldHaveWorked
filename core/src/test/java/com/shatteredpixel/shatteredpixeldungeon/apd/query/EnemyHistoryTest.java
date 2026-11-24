package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.CombatEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.TelemetryVersion;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EnemyHistoryTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void countsFinishedRunsOnlyAndSkipsTheRunBeingPlayed(){
		//run a: killed two rats, then a gnoll killed the hero
		new SyntheticLog(tmp.getRoot(), "a", TelemetryVersion.CURRENT, 100).start("a", 0, "WARRIOR", "r", 0, 30, false).enter(1)
				.heroKills("rat", "Rat", 1).heroKills("rat", "Rat", 1).damageToHero("gnoll", "Gnoll", 5)
				.heroDies("gnoll", CombatEvents.SRC_CHAR).end(LifecycleEvents.OUTCOME_DIED, "Gnoll", 1, 1, 10).close(true);
		//run b: killed one rat, then a gnoll killed the hero again
		new SyntheticLog(tmp.getRoot(), "b", TelemetryVersion.CURRENT, 200).start("b", 0, "MAGE", "r", 0, 30, false).enter(1)
				.heroKills("rat", "Rat", 1).damageToHero("gnoll", "Gnoll", 5)
				.heroDies("gnoll", CombatEvents.SRC_CHAR).end(LifecycleEvents.OUTCOME_DIED, "Gnoll", 1, 1, 10).close(true);
		//run c: still being played (not sealed), killed many rats: never counted
		new SyntheticLog(tmp.getRoot(), "c", TelemetryVersion.CURRENT, 300).start("c", 0, "ROGUE", "r", 0, 30, false).enter(1)
				.heroKills("rat", "Rat", 1).heroKills("rat", "Rat", 1).heroKills("rat", "Rat", 1).close(false);

		EnemyHistory h = EnemyHistory.of(new DatasetStore(tmp.getRoot()).load(), "c");
		assertEquals(2, h.finishedRuns);
		assertEquals(3, h.get("Rat").kills);
		assertEquals(2, h.get("rat").runs.size());
		assertEquals(0, h.get("rat").heroDeaths);
		assertEquals(2, h.get("Gnoll").heroDeaths);
		assertEquals("Your history: met in 2 of your 2 finished runs; you killed 0, it killed you 2 times.", h.line("gnoll"));
		assertEquals("Your history: not met in your 2 finished runs.", h.line("crab"));
		assertNull(h.get(null));
	}

	@Test
	public void noHistoryMeansNoLine(){
		assertEquals("", EnemyHistory.of(new DatasetStore(tmp.getRoot()).load(), null).line("rat"));
		assertEquals("", EnemyHistory.of(null, null).line("rat"));
	}
}

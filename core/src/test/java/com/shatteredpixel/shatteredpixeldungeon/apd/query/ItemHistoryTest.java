package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ItemEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.TelemetryVersion;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ItemHistoryTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final int WEAPON = ItemEvents.CAT_WEAPON;

	private SyntheticLog sword( SyntheticLog l, String uid, boolean acquire, boolean equip ){
		l.registered(uid, "items.weapon.melee.Sword", 1, false, ItemEvents.ORIGIN_GENERATED).seen(uid, "sword", WEAPON);
		if (acquire) l.acquired(uid, "sword", WEAPON, false, 0);
		if (equip) l.equip(uid, "sword", WEAPON).tick(10);
		return l;
	}

	@Test
	public void countsFinishedRunsOnlyAndSkipsTheRunBeingPlayed(){
		//run a: saw a sword, picked it up and equipped it
		sword(new SyntheticLog(tmp.getRoot(), "a", TelemetryVersion.CURRENT, 100).start("a", 0, "WARRIOR", "r", 0, 30, false).enter(1), "a1", true, true)
				.end(LifecycleEvents.OUTCOME_DIED, "Rat", 1, 1, 10).close(true);
		//run b: saw two swords, left both behind
		SyntheticLog b = new SyntheticLog(tmp.getRoot(), "b", TelemetryVersion.CURRENT, 200).start("b", 0, "MAGE", "r", 0, 30, false).enter(1);
		sword(sword(b, "b1", false, false), "b2", false, false).end(LifecycleEvents.OUTCOME_DIED, "Rat", 1, 1, 10).close(true);
		//run c: still being played (not sealed): never counted
		sword(new SyntheticLog(tmp.getRoot(), "c", TelemetryVersion.CURRENT, 300).start("c", 0, "ROGUE", "r", 0, 30, false).enter(1), "c1", true, true)
				.close(false);

		ItemHistory h = ItemHistory.of(new DatasetStore(tmp.getRoot()).load(), "c");
		assertEquals(2, h.finishedRuns);
		assertEquals(2, h.get("Sword").seen.size());
		assertEquals(1, h.get("sword").acquired.size());
		assertEquals(1, h.get("sword").equipped.size());
		assertEquals("Your history: seen in 2 of your 2 finished runs; picked up in 1, equipped in 1.", h.line("Sword"));
		assertEquals("Your history: not seen in your 2 finished runs.", h.line("glaive"));
		assertNull(h.get(null));
	}

	@Test
	public void noHistoryMeansNoLine(){
		assertEquals("", ItemHistory.of(new DatasetStore(tmp.getRoot()).load(), null).line("sword"));
		assertEquals("", ItemHistory.of(null, null).line("sword"));
	}
}

package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.GameFacts;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.TelemetryVersion;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Arrays;

public class RunReplayTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final String MAP = "####" + "#<.#" + "#.>#" + "####";

	@Test
	public void finishedRunGivesEachFloorsMapAndPathInOrder(){
		new SyntheticLog(tmp.getRoot(), "a", TelemetryVersion.CURRENT, 100).start("a", 0, "WARRIOR", "r", 0, 30, false).enter(1)
				.factTags(GameFacts.FLOOR_MAP, "", new String[]{ MAP }, 1, 0, 4, 4)
				.factSeries(GameFacts.MOVE_STEP, "", new long[][]{ { 5, 4, 1 }, { 6, 4, 1 }, { 10, 4, 1 } })
				.leave().enter(2)
				.factTags(GameFacts.FLOOR_MAP, "", new String[]{ "#<>#" }, 2, 0, 2, 2)
				//a second map of the same floor (a reloaded session) is ignored, a broken one too
				.factTags(GameFacts.FLOOR_MAP, "", new String[]{ "...." }, 2, 0, 2, 2)
				.factSeries(GameFacts.MOVE_STEP, "", new long[][]{ { 2, 2, 1 } })
				.end(LifecycleEvents.OUTCOME_DIED, "Rat", 2, 1, 10).close(true);

		RunReplay r = RunReplay.of(new DatasetStore(tmp.getRoot()).load(), "a");
		assertTrue(r.available);
		assertEquals(2, r.floors.size());
		RunReplay.Floor f1 = r.floors.get(0);
		assertEquals(1, f1.depth);
		assertEquals(MAP, f1.cells);
		assertEquals('<', f1.cell(1, 1));
		assertEquals(Arrays.asList(5, 6, 10), f1.path);
		assertEquals("#<>#", r.floors.get(1).cells);
		assertEquals(Arrays.asList(2), r.floors.get(1).path);
	}

	@Test
	public void runStillBeingPlayedOrUnknownGivesNothing(){
		new SyntheticLog(tmp.getRoot(), "c", TelemetryVersion.CURRENT, 300).start("c", 0, "ROGUE", "r", 0, 30, false).enter(1)
				.factTags(GameFacts.FLOOR_MAP, "", new String[]{ MAP }, 1, 0, 4, 4).close(false);
		Catalog cat = new DatasetStore(tmp.getRoot()).load();
		assertFalse(RunReplay.of(cat, "c").available);
		assertFalse(RunReplay.of(cat, "nope").available);
		assertFalse(RunReplay.of(null, "c").available);
		assertTrue(RunReplay.of(cat, "c").floors.isEmpty());
	}
}

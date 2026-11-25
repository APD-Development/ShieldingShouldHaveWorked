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

	@Test
	public void frameMarksStepsSoFarAndTheHeroOnTheLatest(){
		new SyntheticLog(tmp.getRoot(), "f", TelemetryVersion.CURRENT, 400).start("f", 0, "MAGE", "r", 0, 30, false).enter(1)
				.factTags(GameFacts.FLOOR_MAP, "", new String[]{ MAP }, 1, 0, 4, 4)
				.factSeries(GameFacts.MOVE_STEP, "", new long[][]{ { 5, 4, 1 }, { 6, 4, 1 }, { 10, 4, 1 } })
				.end(LifecycleEvents.OUTCOME_DIED, "Rat", 1, 1, 10).close(true);
		RunReplay.Floor f = RunReplay.of(new DatasetStore(tmp.getRoot()).load(), "f").floors.get(0);
		assertEquals(MAP, new String(f.frame(0)));
		assertEquals("#####@.##.>#####", new String(f.frame(1)));
		assertEquals("#####o@##.>#####", new String(f.frame(2)));
		assertEquals("#####oo##.@#####", new String(f.frame(99)));
		assertEquals(MAP, new String(f.frame(-3)));
		assertEquals(MAP, f.cells);
		assertEquals("Floor 1: 3 steps", f.title());
	}

	@Test
	public void oneRunLoadGivesTheSameReplayAsTheWholeHistory(){
		new SyntheticLog(tmp.getRoot(), "g", TelemetryVersion.CURRENT, 500).start("g", 0, "WARRIOR", "r", 0, 30, false).enter(1)
				.factTags(GameFacts.FLOOR_MAP, "", new String[]{ MAP }, 1, 0, 4, 4)
				.factSeries(GameFacts.MOVE_STEP, "", new long[][]{ { 5, 4, 1 }, { 6, 4, 1 } })
				.end(LifecycleEvents.OUTCOME_DIED, "Rat", 1, 1, 10).close(true);
		new SyntheticLog(tmp.getRoot(), "h", TelemetryVersion.CURRENT, 600).start("h", 0, "MAGE", "r", 0, 30, false).enter(1)
				.factTags(GameFacts.FLOOR_MAP, "", new String[]{ "#<>#" }, 1, 0, 2, 2)
				.end(LifecycleEvents.OUTCOME_DIED, "Rat", 1, 1, 10).close(true);
		DatasetStore store = new DatasetStore(tmp.getRoot());
		RunReplay all = RunReplay.of(store.load(), "g"), one = RunReplay.of(store.loadRun("g"), "g");
		assertTrue(one.available);
		assertEquals(all.floors.size(), one.floors.size());
		assertEquals(all.floors.get(0).cells, one.floors.get(0).cells);
		assertEquals(all.floors.get(0).path, one.floors.get(0).path);
		assertEquals(Arrays.asList(5, 6), one.floors.get(0).path);
	}

	//Chris, Nov 25: a run's replay began at floor 9 because floors 1 to 8 were played before maps were kept. Their walks are still shown.
	@Test
	public void floorsWithStepsButNoMapStillAppearInOrder(){
		new SyntheticLog(tmp.getRoot(), "u", TelemetryVersion.CURRENT, 700).start("u", 0, "WARRIOR", "r", 0, 30, false).enter(1)
				.factSeries(GameFacts.MOVE_STEP, "", new long[][]{ { 5, 4, 1 }, { 6, 4, 1 }, { 10, 4, 1 } })
				.leave().enter(2)
				.factTags(GameFacts.FLOOR_MAP, "", new String[]{ MAP }, 2, 0, 4, 4)
				.factSeries(GameFacts.MOVE_STEP, "", new long[][]{ { 5, 4, 1 } })
				.end(LifecycleEvents.OUTCOME_DIED, "Rat", 2, 1, 10).close(true);
		RunReplay r = RunReplay.of(new DatasetStore(tmp.getRoot()).load(), "u");
		assertTrue(r.available);
		assertEquals(2, r.floors.size());
		RunReplay.Floor f1 = r.floors.get(0);
		assertEquals(1, f1.depth);
		assertFalse(f1.mapped);
		assertEquals(4, f1.width);
		assertEquals(3, f1.height);
		assertEquals(Arrays.asList(5, 6, 10), f1.path);
		assertEquals("Floor 1: 3 steps, no map recorded", f1.title());
		assertTrue(r.floors.get(1).mapped);
		assertEquals("Floor 2: 1 step", r.floors.get(1).title());
	}

	//Chris, Nov 25: "Floor 19 branch 1? The fuck does that mean?"
	@Test
	public void sideAreasAreNamedNotNumbered(){
		assertEquals("", RunReplay.Floor.place(9, 0));
		assertEquals(" (Blacksmith floor)", RunReplay.Floor.place(12, 1));
		assertEquals(" (Imp floor)", RunReplay.Floor.place(19, 1));
		assertEquals(" (side floor)", RunReplay.Floor.place(3, 2));
	}

	//Chris, Nov 25: floors must be in the order they were played, even when a floor's map was recorded on a later visit
	@Test
	public void floorsAreInTheOrderTheyWereFirstWalked(){
		new SyntheticLog(tmp.getRoot(), "o", TelemetryVersion.CURRENT, 800).start("o", 0, "WARRIOR", "r", 0, 30, false).enter(1)
				.factSeries(GameFacts.MOVE_STEP, "", new long[][]{ { 5, 4, 1 } })
				.leave().enter(2)
				.factTags(GameFacts.FLOOR_MAP, "", new String[]{ MAP }, 2, 0, 4, 4)
				.factSeries(GameFacts.MOVE_STEP, "", new long[][]{ { 6, 4, 1 } })
				.leave().enter(1)
				//floor 1's map only now, on the way back up
				.factTags(GameFacts.FLOOR_MAP, "", new String[]{ MAP }, 1, 0, 4, 4)
				.factSeries(GameFacts.MOVE_STEP, "", new long[][]{ { 9, 4, 1 } })
				.end(LifecycleEvents.OUTCOME_DIED, "Rat", 1, 1, 10).close(true);
		RunReplay r = RunReplay.of(new DatasetStore(tmp.getRoot()).load(), "o");
		assertEquals(2, r.floors.size());
		assertEquals(1, r.floors.get(0).depth);
		assertEquals(2, r.floors.get(1).depth);
		assertEquals(Arrays.asList(5, 9), r.floors.get(0).path);
	}
}

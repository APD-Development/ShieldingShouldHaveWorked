package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.apd.demo.DemoData;
import com.shatteredpixel.shatteredpixeldungeon.apd.demo.DemoGenerator;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.Ruleset;
import com.shatteredpixel.shatteredpixeldungeon.apd.replay.FloorReplay;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ReplayFloor;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.Fact;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//Seed floors (Phase B) and the recorded-vs-canonical comparison (Phase C): provenance on every row, recorded floors authoritative.
public class SeedFloorsTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final Ruleset RULES = Ruleset.of(30, 0);

	@Before
	public void setUp(){
		HeadlessTestSupport.start();
		FloorReplay.setLiveGameCheck(null);
		com.shatteredpixel.shatteredpixeldungeon.apd.replay.SeedFloorsRunner.clearCache();
	}

	private static ReplayFloor floor( int depth, String... classes ){
		ReplayFloor f = new ReplayFloor();
		f.depth = depth;
		f.levelClass = "levels.SewerLevel";
		for (String c : classes){
			ReplayFloor.Item i = new ReplayFloor.Item();
			i.trueClass = c;
			i.container = "HEAP";
			i.quantity = 1;
			f.items.add(i);
		}
		ReplayFloor.Mob m = new ReplayFloor.Mob();
		m.mobClass = "actors.mobs.Rat";
		m.hp = 8;
		f.mobs.add(m);
		return f;
	}

	private static SeedFloors.Recorded rec( int floor, String cls ){
		SeedFloors.Recorded r = new SeedFloors.Recorded();
		r.floor = floor;
		r.trueClass = cls;
		r.container = "Heap";
		return r;
	}

	private static SeedFloors.Spec spec( int reached ){
		SeedFloors.Spec s = new SeedFloors.Spec();
		s.runId = "run-x";
		s.seed = 5;
		s.heroClass = "WARRIOR";
		s.reached = reached;
		s.ruleset = RULES.id;
		return s;
	}

	private static int basisOf( Row r, DatasetDef d, String col ){
		return (int) r.num(d.indexOf(col));
	}

	@Test
	public void recordedFloorsAreAuthoritativeAndOthersAreLabelledCanonical() {
		List<ReplayFloor> canonical = new ArrayList<>();
		for (int d = 1; d <= 6; d++) canonical.add(floor(d, "items.food.Food", "items.potions.PotionOfHealing"));
		List<SeedFloors.Recorded> recorded = new ArrayList<>();
		recorded.add(rec(1, "items.food.Food"));
		recorded.add(rec(1, "items.weapon.melee.Sword"));
		recorded.add(rec(2, "items.food.Food"));
		SeedFloors.Built b = SeedFloors.assemble(spec(4), recorded, canonical);

		assertEquals(2, b.recordedFloors);
		assertEquals(4, b.canonicalFloors);
		DatasetDef d = b.items.def;
		int recordedRows = 0;
		for (Row r : b.items.rows){
			int floor = (int) r.num(d.indexOf("sf.floor"));
			int basis = basisOf(r, d, "sf.basis");
			assertTrue(r.finished);
			if (floor <= 2){
				//a recorded floor holds only what the log says: no canonical item may stand in for it
				assertEquals(SeedFloors.BASIS_RECORDED, basis);
				assertEquals(Fact.PROV_RECORDED, r.prov(d.indexOf("sf.item")));
				recordedRows++;
			} else if (floor <= 4){
				assertEquals(SeedFloors.BASIS_UNRECORDED, basis);
				assertEquals(Fact.PROV_INFERRED, r.prov(d.indexOf("sf.item")));
			} else {
				assertEquals(SeedFloors.BASIS_UNREACHED, basis);
				assertEquals(Fact.PROV_INFERRED, r.prov(d.indexOf("sf.item")));
			}
		}
		assertEquals(3, recordedRows);
		//creatures: canonical floors only, every one labelled
		for (Row r : b.mobs.rows){
			assertTrue(r.num(b.mobs.def.indexOf("sm.floor")) >= 3);
			assertTrue(basisOf(r, b.mobs.def, "sm.basis") != SeedFloors.BASIS_RECORDED);
		}
		assertEquals(4, b.mobs.rows.size());
	}

	@Test
	public void unknownReachIsSaidSoNeverCalledUnreached() {
		List<ReplayFloor> canonical = new ArrayList<>();
		canonical.add(floor(1, "items.food.Food"));
		SeedFloors.Built b = SeedFloors.assemble(spec(-1), new ArrayList<SeedFloors.Recorded>(), canonical);
		assertEquals(SeedFloors.BASIS_REACH_UNKNOWN, basisOf(b.items.rows.get(0), b.items.def, "sf.basis"));
	}

	@Test
	public void comparisonCountsWhatIsTheSameAndWhatIsNot() {
		List<ReplayFloor> canonical = new ArrayList<>();
		canonical.add(floor(1, "items.food.Food", "items.potions.PotionOfHealing", "items.food.Food"));
		List<SeedFloors.Recorded> recorded = new ArrayList<>();
		recorded.add(rec(1, "items.food.Food"));
		recorded.add(rec(1, "items.food.Food"));
		recorded.add(rec(1, "items.weapon.melee.Sword"));
		SeedFloors.Built b = SeedFloors.assemble(spec(3), recorded, canonical);
		assertEquals(1, b.compare.rows.size());
		Row r = b.compare.rows.get(0);
		DatasetDef d = b.compare.def;
		assertEquals(3, r.num(d.indexOf("sc.recorded")));
		assertEquals(3, r.num(d.indexOf("sc.canonical")));
		assertEquals(2, r.num(d.indexOf("sc.same")));
		assertEquals(1, r.num(d.indexOf("sc.onlyRecorded")));
		assertEquals(1, r.num(d.indexOf("sc.onlyCanonical")));
		assertEquals(6666, r.num(d.indexOf("sc.agree")));
		assertEquals(SeedFloors.BASIS_COMPARISON, basisOf(r, d, "sc.basis"));
		//recorded counts are recorded facts, canonical counts are marked as worked out
		assertEquals(Fact.PROV_RECORDED, r.prov(d.indexOf("sc.recorded")));
		assertEquals(Fact.PROV_INFERRED, r.prov(d.indexOf("sc.canonical")));
	}

	@Test
	public void everySeedFloorsViewRunsThroughTheQueryEngine() {
		List<ReplayFloor> canonical = new ArrayList<>();
		for (int d = 1; d <= 3; d++) canonical.add(floor(d, "items.food.Food"));
		SeedFloors.Built b = SeedFloors.assemble(spec(1), new ArrayList<SeedFloors.Recorded>(), canonical);
		Catalog cat = new Catalog();
		SeedFloors.install(cat, b);
		for (String id : new String[]{ SeedFloors.ITEMS, SeedFloors.MOBS, SeedFloors.COMPARE }){
			ViewDef v = cat.view(id);
			assertNotNull(id, v);
			QueryResult r = QueryEngine.run(cat.datasetOf(v), v.initialQuery().withShowInternal(true), RULES.id);
			if (!id.equals(SeedFloors.COMPARE)) assertFalse(id + " empty", r.rows.isEmpty());
		}
	}

	@Test
	public void spec_refusesRunsThatCannotBeReplayed() throws Exception {
		File sampleRoot = DemoData.sampleRootFor(new File(tmp.newFolder("data"), "apd"));
		DemoGenerator.generateSample(sampleRoot, RULES, 1_800_000_000_000L);
		Catalog cat = new DatasetStore(sampleRoot).load();
		Dataset runs = cat.dataset(Datasets.RUNS);
		Row finished = null;
		for (Row r : runs.rows) if (r.finished){ finished = r; break; }
		assertNotNull(finished);
		SeedFloors.Spec s = SeedFloors.spec(cat, finished);
		assertEquals(finished.runId, s.runId);
		assertFalse(s.heroClass.isEmpty());
		//an unfinished run is never replayed (its hidden information must stay hidden)
		finished.finished = false;
		try {
			SeedFloors.spec(cat, finished);
			fail("unfinished run accepted");
		} catch (SeedFloors.NotAvailable expected){
			assertTrue(expected.getMessage().contains("not finished"));
		} finally {
			finished.finished = true;
		}
	}

	@Test
	public void specRefusesARunWhoseRulesChangedAndOneWhoseRulesWereNeverRecorded() throws Exception {
		File sampleRoot = DemoData.sampleRootFor(new File(tmp.newFolder("data3"), "apd"));
		DemoGenerator.generateSample(sampleRoot, RULES, 1_800_000_000_000L);
		Catalog cat = new DatasetStore(sampleRoot).load();
		Row run = null;
		for (Row r : cat.dataset(Datasets.RUNS).rows) if (r.finished){ run = r; break; }
		String was = run.ruleset;
		run.ruleset = Row.RULESET_MIXED;
		try {
			SeedFloors.spec(cat, run);
			fail("a run whose rules changed has no single set of rules to replay");
		} catch (SeedFloors.NotAvailable expected){
			assertTrue(expected.getMessage().contains("rules changed"));
		} finally {
			run.ruleset = was;
		}
		run.imported = true;
		try {
			SeedFloors.spec(cat, run);
			fail("an imported record is not a recorded run");
		} catch (SeedFloors.NotAvailable expected){
			assertTrue(expected.getMessage().contains("imported"));
		} finally {
			run.imported = false;
		}
	}

	@Test
	public void aFinishedRunOfTheSampleHistoryBuildsAndOpensThroughTheModel() throws Exception {
		File sampleRoot = DemoData.sampleRootFor(new File(tmp.newFolder("data2"), "apd"));
		DemoGenerator.generateSample(sampleRoot, RULES, 1_800_000_000_000L);
		Catalog cat = new DatasetStore(sampleRoot).load();
		Row run = null;
		DatasetDef d = cat.dataset(Datasets.RUNS).def;
		for (Row r : cat.dataset(Datasets.RUNS).rows){
			if (r.finished && r.isKnown(d.indexOf("run.seed")) && r.isKnown(d.indexOf("run.heroClass"))){
				run = r;
				break;
			}
		}
		assertNotNull("the sample history has a replayable run", run);

		UimsdModel m = new UimsdModel(RULES.id, "sample");
		m.setCatalog(cat);
		m.setSeedFloorsProvider(com.shatteredpixel.shatteredpixeldungeon.apd.replay.SeedFloorsRunner.INSTANCE);
		m.selectView("runs");
		assertTrue(m.canSeedFloors(run));
		m.startSeedFloors(run);
		long end = System.currentTimeMillis() + 60000;
		while (!SeedFloors.ITEMS.equals(m.viewId()) && System.currentTimeMillis() < end){
			Thread.sleep(50);
		}
		assertEquals("notice: " + m.notice(), SeedFloors.ITEMS, m.viewId());
		assertTrue(m.query().showInternal);
		assertFalse(m.result().rows.isEmpty());
		assertEquals("", m.notice());
		//Back leads to the run table again
		assertTrue(m.back());
		assertEquals("runs", m.viewId());
	}

	@Test
	public void aLiveGameMakesItANoticeNotACrash() throws Exception {
		File sampleRoot = DemoData.sampleRootFor(new File(tmp.newFolder("data3"), "apd"));
		DemoGenerator.generateSample(sampleRoot, RULES, 1_800_000_000_000L);
		Catalog cat = new DatasetStore(sampleRoot).load();
		Row run = null;
		DatasetDef d = cat.dataset(Datasets.RUNS).def;
		for (Row r : cat.dataset(Datasets.RUNS).rows){
			if (r.finished && r.isKnown(d.indexOf("run.seed")) && r.isKnown(d.indexOf("run.heroClass"))){
				run = r;
				break;
			}
		}
		FloorReplay.setLiveGameCheck(new FloorReplay.LiveGameCheck(){
			@Override
			public String liveGame(){
				return "a game is loaded";
			}
		});
		try {
			UimsdModel m = new UimsdModel(RULES.id, "sample");
			m.setCatalog(cat);
		m.setSeedFloorsProvider(com.shatteredpixel.shatteredpixeldungeon.apd.replay.SeedFloorsRunner.INSTANCE);
			m.selectView("runs");
			m.startSeedFloors(run);
			long end = System.currentTimeMillis() + 30000;
			while (m.notice().startsWith("Replaying") && System.currentTimeMillis() < end) Thread.sleep(50);
			assertTrue(m.notice(), m.notice().contains("a game is loaded"));
			assertEquals("runs", m.viewId());
		} finally {
			FloorReplay.setLiveGameCheck(null);
		}
	}
}

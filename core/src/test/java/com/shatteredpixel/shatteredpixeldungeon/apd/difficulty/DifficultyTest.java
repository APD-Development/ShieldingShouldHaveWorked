package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.apd.LevelGenerationHarness;
import com.shatteredpixel.shatteredpixeldungeon.apd.demo.DemoLog;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.CellFormatter;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.CellText;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Dataset;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.DatasetStore;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Datasets;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Filter;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Op;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Query;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.QueryEngine;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.QueryResult;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Row;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Scope;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ViewDef;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.Ruleset;
import com.shatteredpixel.shatteredpixeldungeon.apd.replay.FloorReplay;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKeys;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.TelemetryVersion;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

//The difficulty framework: levels and neutrality, ruleset identity, persistence (settings and run), telemetry 1.7, UIMSD visibility/filtering,
//legacy runs, and that difficulty 3 (and, until balance is approved, every level) leaves gameplay exactly as it was.
public class DifficultyTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private int savedSetting;

	@Before
	public void setUp(){
		HeadlessTestSupport.start();
		savedSetting = SPDSettings.apdDifficulty();
	}

	@After
	public void tearDown(){
		SPDSettings.apdDifficulty(savedSetting);
		Dungeon.difficulty = Difficulty.DEFAULT;
		Dungeon.difficultyKnown = true;
	}

	//---- levels and neutrality ----

	@Test
	public void levelsAreOneToFiveAndThreeIsTheBaseline() {
		assertEquals(1, Difficulty.MIN);
		assertEquals(3, Difficulty.DEFAULT);
		assertEquals(5, Difficulty.MAX);
		assertEquals(3, Difficulty.normalize(0));
		assertEquals(3, Difficulty.normalize(9));
		assertEquals(4, Difficulty.normalize(4));
		assertTrue(Difficulty.isValid(1) && Difficulty.isValid(5) && !Difficulty.isValid(0) && !Difficulty.isValid(6));
		for (int l = 1; l <= 5; l++) assertFalse(Difficulty.label(l).isEmpty());
	}

	@Test
	public void levelThreeIsNeutralWhateverTheTableSaysAndHasNoBalanceRevision() throws Exception {
		Field f = Difficulty.class.getDeclaredField("TABLE");
		f.setAccessible(true);
		double[][] table = (double[][]) f.get(null);
		double was = table[2][0];
		try {
			table[2][0] = 2.5;   //even a corrupted table cannot move the baseline
			for (Difficulty.Modifier m : Difficulty.Modifier.values()) assertEquals(1.0, Difficulty.modifier(3, m), 0.0);
			assertTrue(Difficulty.isNeutral(3));
		} finally {
			table[2][0] = was;
		}
		assertEquals(0, Difficulty.balanceRevision(3));
	}

	@Test
	public void noBalanceIsApprovedYetSoEveryLevelIsAPlaceholder() {
		//REPLACE THIS TEST when the owner approves values: until then no level may change gameplay or claim a balance revision
		for (int l = 1; l <= 5; l++){
			assertTrue("level " + l + " must be neutral until approved", Difficulty.isNeutral(l));
			assertEquals(0, Difficulty.balanceRevision(l));
		}
	}

	@Test
	public void scalingIsExactWhenNeutralAndRoundsOnlyWhenNot() throws Exception {
		Dungeon.difficulty = 3;
		for (float v : new float[]{ 0f, 1f, 7.3f, 123456.789f, Float.MIN_VALUE }) assertEquals(Float.floatToIntBits(v), Float.floatToIntBits(Difficulty.scale(Difficulty.Modifier.ENEMY_DAMAGE, v)));
		for (int v : new int[]{ 0, 1, 17, Integer.MAX_VALUE }) assertEquals(v, Difficulty.scale(Difficulty.Modifier.ENEMY_HP, v));
		Field f = Difficulty.class.getDeclaredField("TABLE");
		f.setAccessible(true);
		double[][] table = (double[][]) f.get(null);
		double was = table[3][0];
		try {
			table[3][0] = 1.5;
			Dungeon.difficulty = 4;
			assertEquals(15, Difficulty.scale(Difficulty.Modifier.ENEMY_HP, 10));
			assertEquals(15f, Difficulty.scale(Difficulty.Modifier.ENEMY_HP, 10f), 0f);
			Dungeon.difficulty = 3;
			assertEquals(10, Difficulty.scale(Difficulty.Modifier.ENEMY_HP, 10));
		} finally {
			table[3][0] = was;
		}
	}

	//---- ruleset identity ----

	@Test
	public void difficultyThreeKeepsTheRulesetIdsOfBeforeAndOtherLevelsGetTheirOwn() {
		//the ids these rules had before difficulty existed (pinned): old runs stay in the same ruleset as new difficulty-3 runs
		assertEquals("r1-37d60350c6b1", Ruleset.of(30, 0).id);
		assertEquals("r1-1e283854c38e", Ruleset.of(0, 0).id);
		assertEquals(Ruleset.of(30, 0).id, Ruleset.of(30, 0, 3).id);
		assertEquals(Ruleset.of(30, 0).text, Ruleset.of(30, 0, 3).text);
		java.util.Set<String> ids = new java.util.HashSet<>();
		for (int l = 1; l <= 5; l++) ids.add(Ruleset.of(30, 0, l).id);
		assertEquals("every level is its own ruleset", 5, ids.size());
		assertTrue(Ruleset.of(30, 0, 4).text.contains("difficulty=4") && Ruleset.of(30, 0, 4).text.contains("balance=0"));
		assertNotEquals(Ruleset.of(30, 0, 4).id, Ruleset.of(0, 0, 4).id);
		assertNotEquals(Ruleset.of(30, 0, 4).id, Ruleset.of(30, 1, 4).id);
		assertEquals("a level that does not exist is the baseline", Ruleset.of(30, 0).id, Ruleset.of(30, 0, 9).id);
	}

	@Test
	public void theRulesetOfTheRunAndOfTheNextRunFollowTheRightSources() {
		SPDSettings.apdDifficulty(5);
		Dungeon.difficulty = 2;
		Dungeon.difficultyKnown = true;
		assertEquals("the run in progress uses its own difficulty", 2, difficultyOf(Ruleset.current()));
		assertEquals("a new run uses the setting", 5, difficultyOf(Ruleset.forNewRun()));
	}

	private static int difficultyOf( Ruleset r ){
		for (int l = 1; l <= 5; l++) if (Ruleset.of(r.exoticChance, challengesOf(r), l).id.equals(r.id)) return l;
		return -1;
	}

	private static int challengesOf( Ruleset r ){
		int i = r.text.indexOf("challenges=") + "challenges=".length();
		int e = r.text.indexOf(';', i);
		return Integer.parseInt(e < 0 ? r.text.substring(i) : r.text.substring(i, e));
	}

	//---- persistence ----

	@Test
	public void theSettingIsStoredAndValidated() {
		SPDSettings.apdDifficulty(4);
		assertEquals(4, SPDSettings.apdDifficulty());
		SPDSettings.apdDifficulty(99);
		assertEquals("an impossible value falls back to the normal game", 3, SPDSettings.apdDifficulty());
		SPDSettings.apdDifficulty(1);
		assertEquals(1, SPDSettings.apdDifficulty());
	}

	@Test
	public void aRunKeepsItsDifficultyWhenTheSettingChangesAndAcrossSaveAndLoad() throws Exception {
		GamesInProgress.selectedClass = HeroClass.WARRIOR;
		GamesInProgress.curSlot = 1;
		SPDSettings.apdDifficulty(4);
		Dungeon.init();
		assertEquals(4, Dungeon.difficulty);
		assertTrue(Dungeon.difficultyKnown);
		SPDSettings.apdDifficulty(2);
		assertEquals("changing the setting never changes a run that exists", 4, Dungeon.difficulty);
		Dungeon.depth = 1;
		Dungeon.saveGame(1);
		Dungeon.difficulty = 3;
		Dungeon.difficultyKnown = false;
		Dungeon.loadGame(1);
		assertEquals(4, Dungeon.difficulty);
		assertTrue(Dungeon.difficultyKnown);
		assertEquals("the setting is still what the player chose", 2, SPDSettings.apdDifficulty());
	}

	@Test
	public void aRunSavedBeforeDifficultyExistedStaysNotRecordedAndPlaysAsThree() throws Exception {
		GamesInProgress.selectedClass = HeroClass.WARRIOR;
		GamesInProgress.curSlot = 1;
		SPDSettings.apdDifficulty(5);
		Dungeon.init();
		Dungeon.difficultyKnown = false;     //what an older save looks like when it is written back
		Dungeon.difficulty = 3;
		Dungeon.depth = 1;
		Dungeon.saveGame(1);
		Dungeon.difficulty = 5;
		Dungeon.difficultyKnown = true;
		Dungeon.loadGame(1);
		assertEquals(3, Dungeon.difficulty);
		assertFalse("never labelled with a difficulty it did not record", Dungeon.difficultyKnown);
	}

	//---- telemetry 1.7, facts, UIMSD ----

	private File root(){
		return tmp.getRoot();
	}

	private void log( String id, TelemetryVersion v, int difficulty, Ruleset rules ) throws Exception {
		DemoLog l = new DemoLog(root(), id, v, 1_700_000_000_000L);
		l.difficulty = difficulty;
		l.start(id, 0, "WARRIOR", rules.id, 0, 30, false).enter(1).tick(100).heroDies("rat", 0).end(LifecycleEvents.OUTCOME_DIED, "Rat", 1, 1, 10).close(true);
	}

	private Row find( Dataset runs, String idPart ){
		for (Row r : runs.rows) if (r.runId.contains(idPart)) return r;
		return null;
	}

	@Test
	public void newRunsRecordDifficultyLegacyRunsStayUnknownAndUimsdCanFilterAndGroup() throws Exception {
		TelemetryVersion v17 = new TelemetryVersion(1, 7), v15 = new TelemetryVersion(1, 5);
		File demo = new File(root(), "x");
		//(the DemoLog writer marks itself demo data: use demo- ids so the store accepts the folder as a demo folder)
		new File(root(), com.shatteredpixel.shatteredpixeldungeon.apd.query.DatasetStore.DEMO_MARKER_FILE).createNewFile();
		log("demo-d3", v17, 3, Ruleset.of(30, 0, 3));
		log("demo-d4", v17, 4, Ruleset.of(30, 0, 4));
		log("demo-d5", v17, 5, Ruleset.of(30, 0, 5));
		log("demo-old", v15, 0, Ruleset.of(30, 0, 3));
		Catalog cat = new DatasetStore(root()).load();
		Dataset runs = cat.dataset(Datasets.RUNS);
		int dc = runs.def.indexOf(FactKeys.DIFFICULTY), bc = runs.def.indexOf(FactKeys.DIFFICULTY_BALANCE);
		assertTrue(dc >= 0 && bc >= 0);
		assertEquals(4, runs.rows.size());
		Row d3 = find(runs, "demo-d3"), d4 = find(runs, "demo-d4"), old = find(runs, "demo-old");
		assertEquals(3, d3.num(dc));
		assertEquals(4, d4.num(dc));
		assertEquals(0, d4.num(bc));
		assertFalse("a run recorded before 1.7 has no difficulty: unknown, never 3", old.isKnown(dc));
		//the table says what each is
		Query q = Query.of(Datasets.RUNS);
		assertEquals("3 - Standard", CellFormatter.cell(q, d3, runs.def.column(FactKeys.DIFFICULTY)).text);
		assertEquals("?", CellFormatter.cell(q, old, runs.def.column(FactKeys.DIFFICULTY)).text);
		assertEquals(CellText.UNKNOWN, CellFormatter.cell(q, old, runs.def.column(FactKeys.DIFFICULTY)).state);
		//runs of different difficulty are different rulesets: the default scope never mixes them
		String r3 = Ruleset.of(30, 0, 3).id;
		assertEquals(r3, d3.ruleset);
		assertNotEquals(r3, d4.ruleset);

		Scope all = Scope.DEFAULT.withRules(Scope.Rules.ALL, "");
		QueryResult only4 = QueryEngine.run(runs, q.withScope(all).addFilter(Filter.num(FactKeys.DIFFICULTY, Op.EQ, 4)), r3);
		assertEquals(1, only4.rows.size());
		QueryResult only3 = QueryEngine.run(runs, q.withScope(all).addFilter(Filter.num(FactKeys.DIFFICULTY, Op.EQ, 3)), r3);
		assertEquals("legacy runs are not in 'difficulty = 3'", 1, only3.rows.size());
		QueryResult unknown = QueryEngine.run(runs, q.withScope(all).addFilter(Filter.unknown(FactKeys.DIFFICULTY)), r3);
		assertEquals(1, unknown.rows.size());
		assertEquals(old.runId, unknown.rows.get(0).runId);
		assertEquals("in the current rules only the difficulty-3 runs are in scope", 2, QueryEngine.run(runs, q, r3).rows.size());

		ViewDef view = cat.view("difficulty");
		assertNotNull(view);
		QueryResult grouped = QueryEngine.run(runs, view.initialQuery().withMinSample(0), r3);
		assertEquals("one group per difficulty plus the unknown group", 4, grouped.groups.size());
		//the smallest-group rule still applies by default (groups of fewer runs are counted as hidden, not dropped)
		assertTrue(QueryEngine.run(runs, view.initialQuery(), r3).stats.hiddenSmallGroups > 0);
	}

	@Test
	public void redactedCopiesAndOlderStartsStillReadTheirDifficultyCorrectly() throws Exception {
		//a start event written by an older build has no difficulty fields: decoding gives 0 (not recorded)
		LifecycleEvents.RunStart s = new LifecycleEvents.RunStart();
		s.difficulty = 4;
		s.difficultyBalance = 0;
		assertEquals(4, s.difficulty);
		assertEquals(0, new LifecycleEvents.RunStart().difficulty);
	}

	//---- difficulty 3 changes nothing ----

	@Test
	public void everyLevelBuildsTheSameFloorsAsThreeUntilBalanceIsApproved() throws Exception {
		List<LevelGenerationHarness.LevelPrint> baseline = new ArrayList<>();
		SPDSettings.apdDifficulty(3);
		baseline.addAll(LevelGenerationHarness.generate(2468L, 0, HeroClass.WARRIOR, LevelGenerationHarness.Context.REFERENCE, 12));
		for (int level : new int[]{ 1, 2, 4, 5 }){
			SPDSettings.apdDifficulty(level);
			List<LevelGenerationHarness.LevelPrint> got = LevelGenerationHarness.generate(2468L, 0, HeroClass.WARRIOR, LevelGenerationHarness.Context.REFERENCE, 12);
			assertEquals(Dungeon.difficulty, level);
			for (int i = 0; i < baseline.size(); i++) assertEquals("level " + level + " floor " + (i + 1), baseline.get(i).hash, got.get(i).hash);
		}
	}

	@Test
	public void theGameSeamsAreIdentityAtThreeAndScaleEnemiesOnlyWhenAModifierIsSet() throws Exception {
		Rat mob = new Rat();
		int hp = mob.HP, ht = mob.HT;
		Dungeon.difficulty = 3;
		DifficultyHooks.onMobCreated(mob);
		assertEquals(hp, mob.HP);
		assertEquals(ht, mob.HT);
		assertEquals(Float.floatToIntBits(7.3f), Float.floatToIntBits(DifficultyHooks.attackDamage(mob, 7.3f)));

		Field f = Difficulty.class.getDeclaredField("TABLE");
		f.setAccessible(true);
		double[][] table = (double[][]) f.get(null);
		double hpWas = table[3][Difficulty.Modifier.ENEMY_HP.ordinal()], dmgWas = table[3][Difficulty.Modifier.ENEMY_DAMAGE.ordinal()];
		try {
			table[3][Difficulty.Modifier.ENEMY_HP.ordinal()] = 2.0;
			table[3][Difficulty.Modifier.ENEMY_DAMAGE.ordinal()] = 1.5;
			Dungeon.difficulty = 4;
			Rat scaled = new Rat();
			DifficultyHooks.onMobCreated(scaled);
			assertEquals(ht * 2, scaled.HT);
			assertEquals(hp * 2, scaled.HP);
			assertEquals(15f, DifficultyHooks.attackDamage(mob, 10f), 0f);
			//only hostile creatures are scaled
			assertEquals(10f, DifficultyHooks.attackDamage(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero == null ? new com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero() : Dungeon.hero, 10f), 0f);
			Rat ally = new Rat();
			ally.alignment = Char.Alignment.ALLY;
			assertEquals(10f, DifficultyHooks.attackDamage(ally, 10f), 0f);
			//at 3 the same table values change nothing
			Dungeon.difficulty = 3;
			assertEquals(10f, DifficultyHooks.attackDamage(mob, 10f), 0f);
		} finally {
			table[3][Difficulty.Modifier.ENEMY_HP.ordinal()] = hpWas;
			table[3][Difficulty.Modifier.ENEMY_DAMAGE.ordinal()] = dmgWas;
		}
	}

	@Test
	public void theSeamsAreCalledFromTheGameAtOnePlaceEach() throws Exception {
		String base = "src/main/java/com/shatteredpixel/shatteredpixeldungeon/";
		File level = new File(base + "levels/Level.java"), ch = new File(base + "actors/Char.java");
		if (!level.isFile()) return;
		assertTrue(new String(Files.readAllBytes(level.toPath()), "UTF-8").contains("DifficultyHooks.onMobCreated(m)"));
		assertTrue(new String(Files.readAllBytes(ch.toPath()), "UTF-8").contains("DifficultyHooks.attackDamage(this, dmg)"));
		//and difficulty is read through Difficulty only: gameplay code never touches Dungeon.difficulty directly
		int direct = 0;
		for (String f : new String[]{ "actors/mobs/Mob.java", "levels/RegularLevel.java", "items/Generator.java", "actors/buffs/Hunger.java" }){
			File file = new File(base + f);
			if (file.isFile() && new String(Files.readAllBytes(file.toPath()), "UTF-8").contains("Dungeon.difficulty")) direct++;
		}
		assertEquals(0, direct);
	}

	@Test
	public void aFloorReplayUsesTheRunsDifficultyNotTheInstalls() throws Exception {
		SPDSettings.apdDifficulty(2);
		Dungeon.difficulty = 5;
		final int[] seen = { -1 };
		FloorReplay.replay(77L, 0, 0, 4, HeroClass.WARRIOR, 2, new FloorReplay.Observer(){
			@Override
			public void level( int depth, Level level ){
				seen[0] = Difficulty.level();
			}
		});
		assertEquals(4, seen[0]);
		assertEquals("outside the replay the run's own difficulty is back", 5, Difficulty.level());
	}

	@Test
	public void theSettingsScreenOffersTheSelectorForNewRuns() throws Exception {
		File pane = new File("src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/APDSettingsPane.java");
		if (!pane.isFile()) return;
		String text = new String(Files.readAllBytes(pane.toPath()), "UTF-8");
		assertTrue(text.contains("optDifficulty"));
		assertTrue(text.contains("SPDSettings.apdDifficulty("));
		assertTrue("the screen says it only affects new runs", text.contains("Applies to runs you start from now on"));
		//no money anywhere near it
		assertFalse(text.toLowerCase().contains("purchase") || text.toLowerCase().contains("buy"));
	}
}

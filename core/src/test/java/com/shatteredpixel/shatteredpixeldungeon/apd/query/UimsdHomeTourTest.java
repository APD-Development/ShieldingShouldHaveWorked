package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.apd.demo.DemoData;
import com.shatteredpixel.shatteredpixeldungeon.apd.demo.DemoGenerator;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdRecorder;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.Ruleset;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

//The Home page, the guide topics and the interactive tour, on the generated sample history.
public class UimsdHomeTourTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final Ruleset RULES = Ruleset.of(30, 0);

	private File realRoot, sampleRoot;
	private Catalog cat;

	@Before
	public void setUp() throws Exception {
		realRoot = new File(tmp.newFolder("data"), "apd");
		sampleRoot = DemoData.sampleRootFor(realRoot);
		DemoGenerator.generateSample(sampleRoot, RULES, 1_800_000_000_000L);
		cat = new DatasetStore(sampleRoot).load();
	}

	@After
	public void tearDown(){
		ApdRecorder.reset();
	}

	private UimsdModel model() {
		UimsdModel m = new UimsdModel(RULES.id, "sample");
		m.setCatalog(cat);
		return m;
	}

	@Test
	public void sampleIsMarkedAndSmall() throws Exception {
		assertTrue(sampleRoot.getName().equals(DemoData.SAMPLE_DIR_NAME));
		assertEquals(DemoGenerator.SAMPLE_RUNS, cat.dataset(Datasets.RUNS).rows.size());
		DemoData.delete(sampleRoot);
		assertFalse(sampleRoot.exists());
	}

	@Test
	public void homeShowsTheMainCards() {
		UimsdHome h = UimsdHome.build(cat, RULES.id, true);
		assertFalse(h.isEmpty());
		assertTrue(h.runs > 0);
		for (String id : new String[]{ "runs", "characters", "gear", "enemies", "recent", "records" }) assertNotNull(id, h.card(id));
		assertFalse(h.text().isEmpty());
		//every link goes to a view that exists
		for (UimsdHome.Card c : h.cards) for (UimsdHome.Link l : c.links) assertNotNull(l.viewId, cat.view(l.viewId));
	}

	@Test
	public void homeWithNoRunsIsEmptyNotBroken() {
		Catalog empty = new DatasetStore(new File(tmp.getRoot(), "nothing")).load();
		UimsdHome h = UimsdHome.build(empty, RULES.id, false);
		assertTrue(h.isEmpty());
	}

	@Test
	public void guideTopicsPointAtRealTourSteps() {
		assertTrue(UimsdGuide.topics().size() >= 10);
		Set<String> ids = new HashSet<>();
		for (UimsdGuide.Topic t : UimsdGuide.topics()){
			assertTrue(ids.add(t.id));
			//a topic is either shown on the real screen by a tour step, or has none ("")
			assertTrue(t.id, t.tourStep.isEmpty() || UimsdTutorial.indexOf(t.tourStep) >= 0);
			assertFalse(t.text.isEmpty());
		}
		String marks = UimsdGuide.topic("marks").text;
		for (String s : new String[]{ "?", ">=", "~" }) assertTrue(s, marks.contains(s));
	}

	@Test
	public void guideCoversTheNewerFeaturesAndTheMarksTheyUse() {
		for (String id : new String[]{ "reports", "seedfloors", "difficulty" }){
			UimsdGuide.Topic t = UimsdGuide.topic(id);
			assertNotNull(id, t);
			assertTrue(id, t.text.length() > 200);
		}
		assertTrue(UimsdGuide.topic("seedfloors").text.contains("CANONICAL"));
		assertTrue(UimsdGuide.topic("seedfloors").text.contains("not what the run would have met"));
		assertTrue(UimsdGuide.topic("difficulty").text.contains("?"));
		assertTrue(UimsdGuide.topic("reports").text.contains("hidden information"));
		assertTrue(UimsdTutorial.indexOf("reports") > UimsdTutorial.indexOf("coverage"));
		assertTrue(UimsdTutorial.indexOf("reports") < UimsdTutorial.indexOf("home"));
	}

	@Test
	public void homeShowsTheDifficultyCardWhenThereIsSomethingToCompare() {
		UimsdHome h = UimsdHome.build(cat, RULES.id, false);
		UimsdHome.Card c = h.card("difficulty");
		assertNotNull("the sample history has several difficulties", c);
		assertTrue(c.lines.size() >= 2);
		assertEquals("difficulty", c.links.get(0).viewId);
		boolean unknownGroup = false;
		for (UimsdHome.Line l : c.lines) if (l.label.startsWith("Not recorded")) unknownGroup = true;
		assertTrue("older runs are labelled not recorded, never as a level", unknownGroup);
		assertTrue(h.scopeNote, h.scopeNote.isEmpty() || !h.scopeNote.contains("balance revision"));
	}

	@Test
	public void tourRunsOnItsOwnWhenThePlayerDoesTheThings() {
		UimsdModel m = model();
		UimsdTutorial.prepare(m, 0);
		UimsdTutorial t = new UimsdTutorial(m, 0);
		//next through the intro steps
		while (t.step().expect == UimsdTutorial.Expect.NEXT) t.next();
		assertEquals(UimsdTutorial.Expect.VIEW, t.step().expect);
		assertFalse(t.check());
		m.showRoot("weapons");
		assertTrue(t.check());
		while (t.step().expect == UimsdTutorial.Expect.NEXT) t.next();
		assertEquals(UimsdTutorial.Expect.DRILL, t.step().expect);
		m.openGroupRows(m.result().groups.get(0));
		assertTrue(t.check());
		while (t.step().expect == UimsdTutorial.Expect.NEXT) t.next();
		assertEquals(UimsdTutorial.Expect.BACK, t.step().expect);
		assertTrue(m.back());
		assertTrue(t.check());
		//run the rest by skipping: it finishes
		int guard = 0;
		while (!t.isFinished() && guard++ < 100) t.next();
		assertTrue(t.isFinished());
	}

	@Test
	public void startingInTheMiddlePreparesTheScreen() {
		int deeper = UimsdTutorial.indexOf("deeper");
		UimsdModel m = model();
		UimsdTutorial.prepare(m, deeper);
		assertEquals("weapons", m.viewId());
		assertTrue(m.depth() > 0);
		UimsdTutorial t = new UimsdTutorial(m, deeper);
		assertEquals("deeper", t.step().id);
		UimsdModel m2 = model();
		UimsdTutorial.prepare(m2, UimsdTutorial.indexOf("filter"));
		assertEquals("weapons", m2.viewId());
		assertEquals(0, m2.depth());
	}

	@Test
	public void tourNeverChangesTheRealHistory() throws Exception {
		File before = new File(realRoot, "x");
		assertFalse(before.exists());
		assertFalse(new File(sampleRoot, "x").exists());
		//the sample folder is not the real folder, and only the marked folder may be deleted
		assertTrue(!sampleRoot.equals(realRoot));
		File fake = new File(tmp.newFolder("other"), DemoData.SAMPLE_DIR_NAME);
		assertTrue(fake.mkdirs());
		DemoData.delete(fake);
		assertTrue("unmarked folder must survive", fake.exists());
	}

	@Test
	public void panelPlacementKeepsOffTheTarget() {
		//no target: low position
		assertEquals(160f, GuideGeometry.placeTop(20, 200, 40, 0, 0, 0, 0, false), 0.001f);
		//target in the low area: panel goes up
		assertEquals(20f, GuideGeometry.placeTop(20, 200, 40, 10, 170, 50, 10, true), 0.001f);
		//target in the upper area: stays low
		assertEquals(160f, GuideGeometry.placeTop(20, 200, 40, 10, 30, 50, 10, true), 0.001f);
		//the panel is never taller than the room
		assertEquals(20f, GuideGeometry.placeTop(20, 60, 100, 0, 0, 0, 0, false), 0.001f);
		assertTrue(GuideGeometry.panelHeight(50, 8, 14, 200) <= 200 * 0.45f);
		assertTrue(GuideGeometry.panelHeight(0, 8, 14, 200) >= GuideGeometry.MIN_PANEL);
	}
}

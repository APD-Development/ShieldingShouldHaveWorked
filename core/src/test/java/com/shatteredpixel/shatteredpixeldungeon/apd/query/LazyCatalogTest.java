package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//Reading heavy datasets only when asked for (and keeping them compactly) must give exactly the rows building everything at once gives.
public class LazyCatalogTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	static void assertSameRow( String where, Row a, Row b ){
		assertEquals(where + " id", a.id, b.id);
		assertEquals(where + " mergeKey", a.mergeKey, b.mergeKey);
		assertEquals(where + " run", a.runId, b.runId);
		assertEquals(where + " finished", a.finished, b.finished);
		assertEquals(where + " public", a.publicRow, b.publicRow);
		assertEquals(where + " ruleset", a.ruleset, b.ruleset);
		assertEquals(where + " duplicate", a.duplicate, b.duplicate);
		assertEquals(where + " imported", a.imported, b.imported);
		assertEquals(where + " telemetry", a.telemetry, b.telemetry);
		assertEquals(where + " size", a.size(), b.size());
		for (int c = 0; c < a.size(); c++){
			assertEquals(where + " meta " + c, a.rawMeta(c), b.rawMeta(c));
			assertEquals(where + " num " + c, a.num(c), b.num(c));
			assertEquals(where + " text " + c, a.rawText(c), b.rawText(c));
		}
	}

	static void assertSameCatalog( Catalog eager, Catalog lazy ){
		List<Dataset> e = eager.datasets(), l = lazy.datasets();
		assertEquals(e.size(), l.size());
		for (int d = 0; d < e.size(); d++){
			Dataset a = e.get(d), b = l.get(d);
			assertEquals(a.def.id, b.def.id);
			assertEquals(a.def.id + " count", a.rows.size(), b.rowCount());
			assertEquals(a.def.id + " rows", a.rows.size(), b.rows.size());
			assertEquals(a.def.id + " empty", a.rows.isEmpty(), b.rows.isEmpty());
			for (int i = 0; i < a.rows.size(); i++) assertSameRow(a.def.id + "[" + i + "]", a.rows.get(i), b.rows.get(i));
			assertEquals(a.def.id + " runs", a.runs.size(), b.runs.size());
			for (int i = 0; i < a.runs.size(); i++){
				assertEquals(a.runs.get(i).runId, b.runs.get(i).runId);
				assertEquals(a.runs.get(i).covered, b.runs.get(i).covered);
				assertEquals(a.runs.get(i).finished, b.runs.get(i).finished);
			}
		}
		assertEquals(eager.views().size(), lazy.views().size());
		assertEquals(eager.rulesets.size(), lazy.rulesets.size());
	}

	@Test
	public void heavyRowsReadWhenAskedForAreExactlyTheRowsBuiltAtOnce() throws Exception {
		File root = tmp.getRoot();
		LongHistory.write(root, 3);
		Catalog eager = new DatasetStore(root).buildEverythingAtOnce().load();
		//from the caches the eager load wrote
		Catalog cached = new DatasetStore(root).load();
		assertSameCatalog(eager, cached);
		//and from nothing: no caches, so the lazy reads rebuild from the raw logs
		new DatasetStore(root).deleteCache();
		Catalog fresh = new DatasetStore(root).load();
		new DatasetStore(root).deleteCache();
		assertSameCatalog(eager, fresh);
		assertEquals(LongHistory.STEPS * 3, eager.dataset(FactDatasets.PREFIX + "move.step").rows.size());
	}

	@Test
	public void openingHomeReadsNoHeavyDatasetAndOneRunReadsOnlyItsOwnLogs() throws Exception {
		File root = tmp.getRoot();
		LongHistory.write(root, 3);
		Catalog cat = new DatasetStore(root).load();
		UimsdHome home = UimsdHome.build(cat, null, true);
		assertTrue(home.runs > 0);
		Questions.askable(cat);
		for (Dataset d : cat.datasets()){
			if (DatasetStore.light(d.def.id)) assertFalse(d.def.id, d.rows instanceof LazyRows);
			else {
				assertTrue(d.def.id, d.rows instanceof LazyRows);
				assertFalse(d.def.id + " was read just by opening UIMSD", ((LazyRows) d.rows).isLoaded());
			}
		}
		Dataset steps = cat.dataset(FactDatasets.PREFIX + "move.step");
		List<Row> one = steps.rowsOf("long1");
		assertEquals(LongHistory.STEPS, one.size());
		for (Row r : one) assertEquals("long1", r.runId);
		assertFalse("one run's rows do not read the whole dataset", ((LazyRows) steps.rows).isLoaded());
		List<Row> filtered = new ArrayList<>();
		for (Row r : steps.rows) if (r.runId.equals("long1")) filtered.add(r);
		assertEquals(filtered.size(), one.size());
		for (int i = 0; i < one.size(); i++) assertSameRow("run row " + i, filtered.get(i), one.get(i));
		assertEquals(1, ((LazyRows) steps.rows).loads());
		//a row read twice is the same row, so "is it in that list" still works
		assertEquals(steps.rows.get(5), steps.rows.get(5));
		assertTrue(steps.rows.contains(filtered.get(3)));
	}

	@Test
	public void runTurnBrowserStillShowsHeavyObservationsForTheRun() throws Exception {
		File root = tmp.getRoot();
		LongHistory.write(root, 2);
		Catalog cat = new DatasetStore(root).load();
		Row run = null;
		for (Row r : cat.dataset(Datasets.RUNS).rows) if (r.runId.equals("long0")) run = r;
		assertNotNull(run);
		RunTurnBrowser browser = new RunTurnBrowser(cat, run);
		assertTrue(browser.lastTurn >= LongHistory.TURNS);
	}
}

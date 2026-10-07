package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static com.shatteredpixel.shatteredpixeldungeon.apd.query.QuerySupport.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.Fact;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//The view model and the table it feeds, driven with a made-up dataset that is not one of the shipped ones: nothing in them may
//depend on Runs, Items or any other particular dataset.
public class UimsdModelTest {

	private static final String R = "r-now";
	private final DatasetDef things = QuerySupport.def();

	//a second made-up dataset, linked from the first
	private DatasetDef parts(){
		DatasetDef d = new DatasetDef("parts", "Parts", "made up");
		d.add(ColumnDef.text("thing", "Thing").shown());
		d.add(ColumnDef.num("weight", "Weight").shown());
		d.add(ColumnDef.num("hiddenWeight", "Hidden weight").internal());
		return d;
	}

	private Catalog catalog(){
		List<Row> rows = new ArrayList<>();
		for (int i = 0; i < 120; i++){
			Row r = row(things, "t" + i, "thing-" + i, (long) (i % 17), (long) (i % 3), (long) i, i % 10 != 0, R);
			if (i % 10 == 0) r.finished = false;
			rows.add(r);
		}
		DatasetDef p = parts();
		List<Row> prows = new ArrayList<>();
		for (int i = 0; i < 6; i++){
			Row r = new Row("p" + i, null, "run-t" + (i % 2 == 0 ? "1" : "2"), p.size());
			r.finished = true;
			r.ruleset = R;
			r.setText(0, "thing-" + (i % 2 == 0 ? 1 : 2), Fact.PROV_RECORDED);
			r.setNum(1, 5 + i, Fact.PROV_RECORDED);
			r.setNum(2, 50 + i, Fact.PROV_RECORDED);
			prows.add(r);
		}
		things.link(new LinkDef("Its parts", "parts", "name", "thing"));
		Catalog c = new Catalog();
		c.add(dataset(things, rows));
		c.add(dataset(p, prows));
		c.add(new ViewDef("all", "All things", "every thing", Query.of("things").withScope(Scope.DEFAULT.withUnfinished(true))
				.withSorts(Arrays.asList(new Sort("score", true)))));
		c.add(new ViewDef("kinds", "By kind", "grouped", Query.of("things").withScope(Scope.DEFAULT.withUnfinished(true))
				.withGroup(new GroupBy(Arrays.asList("kind"), Arrays.asList(Aggregate.count(), Aggregate.of(Aggregate.Fn.AVG, "score"))))
				.withMinSample(5)));
		c.add(new ViewDef("parts", "Parts", "the parts", Query.of("parts")));
		return c;
	}

	private UimsdModel model(){
		UimsdModel m = new UimsdModel(R, "made up rules");
		m.setCatalog(catalog());
		return m;
	}

	@Test
	public void theModelOpensTheFirstViewAndLaysOutAPage(){
		UimsdModel m = model();
		assertEquals(UimsdModel.State.READY, m.state());
		assertEquals("all", m.viewId());
		ResultTable t = m.table();
		assertEquals(120, t.total);
		assertEquals(50, t.lines.size());
		assertEquals(3, t.pageCount());
		assertEquals("name, score, kind and depth are shown by default; secret and note are hidden", 4, t.headers.size());
	}

	@Test
	public void pagingNeverRunsOffTheEnd(){
		UimsdModel m = model();
		m.setPage(2);
		assertEquals(20, m.table().lines.size());
		m.setPage(99);
		assertEquals(2, m.page());
		m.setPage(0);
		assertEquals(50, m.table().lines.size());
		assertEquals(0, m.table().pageStart);
	}

	@Test
	public void headerClicksCycleTheSortAndTheHeaderSaysSo(){
		UimsdModel m = model();
		ResultTable t = m.table();
		ResultTable.Header score = null;
		for (ResultTable.Header h : t.headers) if (h.id.equals("score")) score = h;
		assertEquals(-1, score.sortDirection);   //the view starts sorted by score, descending
		assertEquals(1, score.sortRank);
		m.cycleSort("score");                    //descending -> off
		for (ResultTable.Header h : m.table().headers) if (h.id.equals("score")) assertEquals(0, h.sortDirection);
		m.cycleSort("name");
		m.cycleSort("name");
		for (ResultTable.Header h : m.table().headers) if (h.id.equals("name")) assertEquals(-1, h.sortDirection);
	}

	@Test
	public void filtersCanBeAddedEditedAndRemovedAndTheStatusSaysWhatTheyCost(){
		UimsdModel m = model();
		m.addFilter(Filter.num("score", Op.GE, 10));
		int matching = m.result().stats.matching;
		assertTrue(matching < 120 && matching > 0);
		m.addFilter(Filter.num("kind", Op.EQ, 1));
		assertTrue(m.result().stats.matching < matching);
		m.replaceFilter(0, Filter.num("score", Op.GE, 0));
		assertEquals(2, m.query().filters.size());
		m.removeFilter(1);
		assertEquals(1, m.query().filters.size());
		m.clearFilters();
		assertEquals(120, m.result().stats.matching);
	}

	@Test
	public void columnsCanBeToggledButNeverEmptied(){
		UimsdModel m = model();
		int before = m.table().headers.size();
		m.toggleColumn("depth");
		assertEquals(before + (m.query().columns.contains("depth") ? 1 : -1), m.table().headers.size());
		List<String> cols = new ArrayList<>(m.query().columns);
		for (String c : cols) m.toggleColumn(c);
		assertTrue(m.table().headers.size() >= 1);
	}

	@Test
	public void hiddenColumnsAreOnlyOfferedWhenHiddenInformationIsOnAndGoWhenItIsTurnedOff(){
		UimsdModel m = model();
		for (ColumnDef c : m.offeredColumns()) assertFalse(c.id, c.isInternal());
		m.setShowInternal(true);
		boolean sawSecret = false;
		for (ColumnDef c : m.offeredColumns()) if (c.id.equals("secret")) sawSecret = true;
		assertTrue(sawSecret);
		m.addFilter(Filter.num("secret", Op.GE, 3));
		m.setSorts(Arrays.asList(new Sort("secret", false), new Sort("name", false)));
		m.toggleColumn("secret");
		m.setShowInternal(false);
		assertTrue("filters on hidden columns are removed with them", m.query().filters.isEmpty());
		assertEquals(Arrays.asList(new Sort("name", false)), m.query().sorts);
		assertFalse(m.query().columns.contains("secret"));
		assertTrue(m.result().problems.isEmpty());
	}

	@Test
	public void switchingViewsKeepsOnlyTheHiddenInformationChoice(){
		UimsdModel m = model();
		m.setShowInternal(true);
		m.addFilter(Filter.num("score", Op.GE, 3));
		m.selectView("kinds");
		assertTrue(m.query().showInternal);
		assertTrue(m.query().filters.isEmpty());
		assertTrue(m.result().isGrouped());
		assertTrue(m.canGoBack());
		assertTrue(m.back());
		assertEquals("all", m.viewId());
		assertEquals(1, m.query().filters.size());   //back is exactly where the player was
	}

	@Test
	public void aLinkOpensTheMatchingRowsOfAnotherDatasetWithTheRowsOwnScope(){
		UimsdModel m = model();
		Row thing = null;
		for (Row r : m.result().rows) if (r.id.equals("t1")) thing = r;
		assertNotNull(thing);
		m.openLink(thing, things.links.get(0));
		assertEquals("parts", m.viewId());
		assertEquals(3, m.result().rows.size());   //the three parts of thing-1
		assertEquals(1, m.query().filters.size());
		assertTrue(m.back());
		assertEquals("all", m.viewId());
	}

	@Test
	public void aLinkFromAnUnfinishedRowWidensTheScopeSoItIsNotEmpty(){
		UimsdModel m = model();
		Row open = null;
		for (Row r : m.result().rows) if (!r.finished) open = r;
		assertNotNull(open);
		Scope s = Scope.DEFAULT;
		assertFalse(s.includeUnfinished);
		//the target's own scope is the default (finished only): the link must widen it
		m.selectView("all");
		m.openLink(open, new LinkDef("same", "all", "name", "name"));
		assertTrue(m.query().scope.includeUnfinished);
	}

	@Test
	public void statusActionsTurnTheScopeSwitchesOn(){
		UimsdModel m = model();
		m.setScope(Scope.DEFAULT);
		assertTrue(m.result().stats.unfinished > 0);
		boolean found = false;
		for (StatusText.Line l : m.status()) if (l.action.equals(StatusText.ACT_UNFINISHED)) found = true;
		assertTrue(found);
		m.applyAction(StatusText.ACT_UNFINISHED);
		assertTrue(m.query().scope.includeUnfinished);
		assertEquals(0, m.result().stats.unfinished);
	}

	@Test
	public void groupedViewsListSmallGroupsAsHiddenAndTheStatusLineCanShowThem(){
		UimsdModel m = model();
		m.selectView("kinds");
		assertEquals(3, m.result().groups.size());
		m.setMinSample(1000);
		assertEquals(0, m.result().groups.size());
		assertEquals(3, m.result().stats.hiddenSmallGroups);
		m.applyAction(StatusText.ACT_SMALL_GROUPS);
		assertEquals(3, m.result().groups.size());
		ResultTable t = m.table();
		assertTrue(t.grouped);
		assertEquals("Kind", t.headers.get(0).label);
		assertEquals("Rows", t.headers.get(1).label);
		assertEquals("Avg Score", t.headers.get(2).label);
	}

	@Test
	public void theHeaderShowsHowMuchOfAColumnIsKnown(){
		UimsdModel m = model();
		ResultTable t = m.table();
		for (ResultTable.Header h : t.headers) if (h.id.equals("score")) assertEquals("120/120", h.coverage);
		m.addFilter(Filter.num("score", Op.LT, 3));
		for (ResultTable.Header h : m.table().headers) if (h.id.equals("score")) assertTrue(h.coverage, h.coverage.startsWith(m.result().stats.matching + "/"));
	}

	@Test
	public void theStatusStripExplainsEverythingThatWasLeftOut(){
		UimsdModel m = model();
		m.setScope(Scope.DEFAULT);
		m.addFilter(Filter.num("depth", Op.GE, 1));   //depth is unknown for every row
		StringBuilder all = new StringBuilder();
		for (StatusText.Line l : m.status()) all.append(l.text).append('\n');
		String text = all.toString();
		assertTrue(text, text.contains("not known"));
		assertTrue(text, text.contains("not finished"));
		assertTrue(text, text.contains("All things"));
	}

	@Test
	public void theModelCanBeLoadedFromAnEmptyHistoryOnAThread() throws Exception {
		java.io.File dir = java.nio.file.Files.createTempDirectory("uimsd").toFile();
		UimsdModel m = new UimsdModel(R, "");
		final boolean[] told = new boolean[1];
		m.setListener(new UimsdModel.Listener(){
			@Override
			public void changed(){
				told[0] = true;
			}
		});
		m.load(dir);
		m.awaitLoad(10000);
		assertEquals(UimsdModel.State.READY, m.state());
		assertTrue(told[0]);
		assertEquals(0, m.result().rows.size());
		dir.delete();
	}

	@Test
	public void theGenericPartsOfUimsdNeverNameAShippedDatasetOrColumn() throws Exception {
		String[] generic = { "QueryEngine", "Query", "Filter", "Sort", "Aggregate", "GroupBy", "Scope", "Op", "ColumnDef", "DatasetDef", "Row",
				"RowMerger", "CellFormatter", "CellText", "ResultTable", "StatusText", "UimsdModel", "QueryCodec", "Dataset", "QueryResult" };
		java.io.File dir = new java.io.File(System.getProperty("apd.query.src"));
		for (String name : generic){
			String text = new String(java.nio.file.Files.readAllBytes(new java.io.File(dir, name + ".java").toPath()), "UTF-8");
			for (String bad : new String[]{"FactKeys", "\"run.", "\"item.", "\"enemy.", "\"death.", "Datasets."}){
				assertFalse(name + " mentions " + bad, text.contains(bad));
			}
		}
	}
}

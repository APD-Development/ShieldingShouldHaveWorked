package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.apd.query.DatasetDef;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Datasets;
import org.junit.Test;
import static org.junit.Assert.*;

/** Nov 23 deep dive: the Sort menu showed raw ids ("run.id", "run.ascended") for sort keys the table wasn't showing. */
public class UimsdSortLabelTest {
	private static DatasetDef runs(){
		for (DatasetDef d : Datasets.defs()) if (d.id.equals(Datasets.RUNS)) return d;
		throw new AssertionError("no runs dataset");
	}

	@Test public void hiddenSortKeysUseTheDatasetLabel(){
		assertEquals("Ascended", WndUimsdSort.labelOf(null, runs(), "run.ascended"));
		assertNotEquals("run.id", WndUimsdSort.labelOf(null, runs(), "run.id"));
		assertEquals("an unknown id still shows something", "no.such", WndUimsdSort.labelOf(null, runs(), "no.such"));
	}
}

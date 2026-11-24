package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class QueryShapesTest {

	@Rule public TemporaryFolder tmp = new TemporaryFolder();

	private static Question big( long floor ){
		return Question.about("runs").withPer(PerUnit.Unit.FLOOR)
				.withGroupBy(Arrays.asList("hero", "outcome"))
				.withFilters(Arrays.asList(Filter.num("depth", Op.GE, floor), Filter.num("turns", Op.GT, 100), Filter.text("killer", Op.EQ, "rat")));
	}

	@Test
	public void reactsOnlyToComplexNovelShapesAndRarely() throws Exception {
		QueryShapes s = new QueryShapes();
		Question simple = Question.about("runs").withFilters(Arrays.asList(Filter.num("depth", Op.GE, 3), Filter.num("turns", Op.GT, 9)));
		assertTrue(simple.complexity() < QueryShapes.COMPLEXITY_FLOOR);
		assertFalse("novel but simple: no reaction", s.observe(simple, 1000));
		Question q = big(5);
		assertTrue(q.complexity() >= QueryShapes.COMPLEXITY_FLOOR);
		assertTrue(s.observe(q, 2000));
		assertFalse("same shape, different values: not novel", s.observe(big(9), 3000));
		Question other = big(5).withGroupBy(Arrays.asList("hero", "outcome", "difficulty"));
		assertFalse("novel and complex but within cooldown", s.observe(other, 4000));
		Question third = other.withPer(PerUnit.Unit.TURN);
		assertTrue(s.observe(third, 4000 + QueryShapes.COOLDOWN_MS));

		File f = new File(tmp.getRoot(), QueryShapes.FILE_NAME);
		s.store(f);
		QueryShapes back = QueryShapes.load(f);
		assertEquals(s.size(), back.size());
		assertTrue(back.seen(QueryShapes.shape(big(1))));
		Question fourth = third.withPer(PerUnit.Unit.HOUR);
		assertFalse("cooldown survives a reload", back.observe(fourth, 5000 + QueryShapes.COOLDOWN_MS));
	}

	@Test
	public void lineNeverChangesMeaningAndStaysLocal(){
		Question q = big(5);
		String clean = QueryShapes.line(q, false), adult = QueryShapes.line(q, true);
		assertTrue(clean.contains(q.complexity() + " things"));
		assertFalse(clean.toLowerCase().contains("damn"));
		assertFalse(clean.toLowerCase().contains("nobody") || clean.toLowerCase().contains("no player"));
		assertTrue(adult.contains(q.complexity() + " things"));
		assertEquals(QueryShapes.shape(Question.about("runs")), QueryShapes.shape(Question.about("runs")));
	}

	@Test
	public void unreadableFileStartsFresh() throws Exception {
		assertEquals(0, QueryShapes.load(new File(tmp.getRoot(), "missing")).size());
	}
}

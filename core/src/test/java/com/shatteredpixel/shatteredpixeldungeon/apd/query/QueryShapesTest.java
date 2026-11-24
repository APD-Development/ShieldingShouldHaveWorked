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
		String clean = QueryShapes.line(q);
		assertTrue(clean.contains(q.complexity() + " things"));
		assertFalse(clean.toLowerCase().contains("damn"));
		assertFalse(clean.toLowerCase().contains("nobody") || clean.toLowerCase().contains("no player"));
		assertEquals(QueryShapes.shape(Question.about("runs")), QueryShapes.shape(Question.about("runs")));
	}

	@Test
	public void secondStoreReplacesTheFileAndKeepsTheCooldown() throws Exception {
		File f = new File(tmp.getRoot(), QueryShapes.FILE_NAME);
		QueryShapes s = new QueryShapes();
		s.observe(Question.about("runs"), 1000);
		s.store(f);
		assertTrue(s.observe(big(5), 2000));
		s.store(f);
		assertFalse(new File(f.getPath() + ".tmp").exists());
		QueryShapes back = QueryShapes.load(f);
		assertEquals(2, back.size());
		assertFalse("cooldown from the second store survives", back.observe(big(5).withPer(PerUnit.Unit.TURN), 3000));
	}

	@Test
	public void unreadableFileStartsFresh() throws Exception {
		assertEquals(0, QueryShapes.load(new File(tmp.getRoot(), "missing")).size());
	}

	@Test
	public void regularShapeSurvivesTheCap(){
		QueryShapes s = new QueryShapes();
		Question regular = big(5);
		s.observe(regular, 1000);
		for (int i = 0; i < QueryShapes.MAX_SHAPES - 1; i++){
			s.observe(Question.about("runs").withFilters(Arrays.asList(Filter.num("c" + i, Op.GE, 1))), 1000);
		}
		assertEquals(QueryShapes.MAX_SHAPES, s.size());
		assertFalse(s.observe(regular, 2000));   //asked again: now the most recent
		s.observe(Question.about("runs").withFilters(Arrays.asList(Filter.num("newest", Op.GE, 1))), 3000);
		assertEquals(QueryShapes.MAX_SHAPES, s.size());
		assertTrue("a regularly asked shape is not forgotten", s.seen(QueryShapes.shape(regular)));
		assertFalse("the least recently asked one is", s.seen(QueryShapes.shape(Question.about("runs").withFilters(Arrays.asList(Filter.num("c0", Op.GE, 1))))));
	}
}

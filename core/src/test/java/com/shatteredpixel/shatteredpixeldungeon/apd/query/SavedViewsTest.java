/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SavedViewsTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static Query q( String dataset, int minSample ){
		return new Query(dataset, Scope.DEFAULT, new ArrayList<Filter>(), new ArrayList<Sort>(), new ArrayList<String>(), null, false, minSample);
	}

	@Test
	public void aSavedViewComesBackAsTheSameQuestion() throws Exception {
		File f = SavedViews.fileIn(tmp.getRoot());
		SavedViews s = new SavedViews();
		assertEquals(SavedViews.Result.SAVED, s.save("  My eleven filters ", q("runs", 5), 100));
		assertEquals(SavedViews.Result.SAVED, s.save("weapons", q("items", 0), 200));
		s.store(f);
		assertFalse(new File(f.getPath() + ".tmp").exists());
		SavedViews back = SavedViews.load(f);
		assertEquals(0, back.skippedOnLoad);
		assertEquals(2, back.list().size());
		assertEquals("a name is found whatever its case", "runs", back.get("my ELEVEN filters").query().dataset);
		assertEquals(QueryCodec.encode(q("runs", 5)), QueryCodec.encode(back.get("My eleven filters").query()));
		assertEquals(100, back.get("My eleven filters").createdAtMillis);
	}

	@Test
	public void namesAreValidatedReplacedRenamedAndDeleted(){
		SavedViews s = new SavedViews();
		assertEquals(SavedViews.Result.BAD_NAME, s.save("", q("runs", 0), 1));
		assertEquals(SavedViews.Result.BAD_NAME, s.save("a\nb", q("runs", 0), 1));
		StringBuilder big = new StringBuilder();
		for (int i = 0; i <= SavedViews.MAX_NAME; i++) big.append('x');
		assertEquals(SavedViews.Result.BAD_NAME, s.save(big.toString(), q("runs", 0), 1));
		assertEquals(SavedViews.Result.SAVED, s.save("a", q("runs", 0), 1));
		assertEquals("the same name replaces that view", SavedViews.Result.REPLACED, s.save("A", q("items", 0), 2));
		assertEquals(1, s.list().size());
		assertEquals("items", s.get("a").query().dataset);
		s.save("b", q("runs", 0), 3);
		assertFalse("a name that is taken cannot be used", s.rename("a", "B"));
		assertTrue(s.rename("a", "c"));
		assertNull(s.get("a"));
		assertNotNull(s.get("c"));
		assertTrue(s.delete("C"));
		assertFalse(s.delete("C"));
		assertEquals(1, s.list().size());
	}

	@Test
	public void thereIsALimit(){
		SavedViews s = new SavedViews();
		for (int i = 0; i < SavedViews.MAX_VIEWS; i++) assertEquals(SavedViews.Result.SAVED, s.save("v" + i, q("runs", 0), i));
		assertEquals(SavedViews.Result.TOO_MANY, s.save("one more", q("runs", 0), 0));
		assertEquals("replacing is still allowed at the limit", SavedViews.Result.REPLACED, s.save("v1", q("items", 0), 0));
	}

	@Test
	public void missingOrDamagedFilesLoseOnlyTheDamage() throws Exception {
		File f = SavedViews.fileIn(tmp.getRoot());
		assertEquals(0, SavedViews.load(f).list().size());
		SavedViews s = new SavedViews();
		s.save("one", q("runs", 0), 1);
		s.save("two", q("items", 0), 2);
		s.store(f);
		String[] lines = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8).split("\n");
		Files.write(f.toPath(), (lines[0] + "\nnot a line\n" + lines[1] + "\n" + lines[1] + "\n").getBytes(StandardCharsets.UTF_8));
		SavedViews back = SavedViews.load(f);
		assertEquals(2, back.list().size());
		assertEquals("the garbage line and the repeated name", 2, back.skippedOnLoad);
	}

	@Test
	public void aViewThisBuildCannotReadIsReportedNotCrashed(){
		SavedViews s = new SavedViews();
		s.save("ok", q("runs", 0), 1);
		assertNotNull(s.get("ok").query());
		List<SavedViews.View> l = s.list();
		assertEquals(1, l.size());
	}

	@Test
	public void savedCombinedQueryReopensAndDeletionSurvivesReload() throws Exception {
		Query original = Query.of("runs")
				.addFilter(Filter.text("run.class", Op.EQ, "WARRIOR"))
				.withSorts(java.util.Arrays.asList(new Sort("run.score", true)))
				.withColumns(java.util.Arrays.asList("run.score", "run.class", "run.type", "run.modded"));
		File file = SavedViews.fileIn(tmp.getRoot());
		SavedViews views = new SavedViews();
		assertEquals(SavedViews.Result.SAVED, views.save("Warriors", original, 100));
		views.store(file);
		SavedViews reopened = SavedViews.load(file);
		assertEquals(QueryCodec.encode(original), QueryCodec.encode(reopened.get("Warriors").query()));
		assertTrue(reopened.delete("Warriors"));
		reopened.store(file);
		assertTrue(SavedViews.load(file).list().isEmpty());
	}
}

/*
 * ADHD Pixel Dungeon
 * Copyright (C) 2026 Chris Charlton
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
import java.util.Arrays;

//Exact question persistence is intentionally separate from the derived table query:
//per-unit wording and the player's friendly choices must survive a restart unchanged.
public class QuestionCodecTest {

	@Rule public TemporaryFolder tmp = new TemporaryFolder();

	private Question detailed(){
		return Question.about("test").withMeasure(Question.Measure.TOTAL, "a")
				.withPer(PerUnit.Unit.THOUSAND_TURNS)
				.withGroupBy(Arrays.asList("name", "a"))
				.addFilter(Filter.text("name", Op.EQ, ""))
				.addFilter(Filter.num("a", Op.GE, 7))
				.withScope(new Scope(Scope.Rules.SPECIFIC, "rules-2", true, true, true, true, true))
				.withShowInternal(true);
	}

	@Test public void exactQuestionRoundTripsIncludingPerScopeAndEmptyText(){
		Question original = detailed();
		Question restored = QuestionCodec.decode(QuestionCodec.encode(original));
		assertEquals(original.dataset, restored.dataset);
		assertEquals(original.measure, restored.measure);
		assertEquals(original.column, restored.column);
		assertEquals(original.per, restored.per);
		assertEquals(original.groupBy, restored.groupBy);
		assertEquals(original.scope.rules, restored.scope.rules);
		assertEquals(original.scope.ruleset, restored.scope.ruleset);
		assertEquals(original.scope.includeUnfinished, restored.scope.includeUnfinished);
		assertEquals(original.scope.includeUnknownRuleset, restored.scope.includeUnknownRuleset);
		assertEquals(original.scope.includeMixedRuleset, restored.scope.includeMixedRuleset);
		assertEquals(original.scope.includeDuplicates, restored.scope.includeDuplicates);
		assertEquals(original.scope.finishedOnly, restored.scope.finishedOnly);
		assertTrue(restored.showInternal);
		assertEquals(2, restored.filters.size());
		assertArrayEquals(new String[]{""}, restored.filters.get(0).texts);
	}

	@Test public void everyFriendlyMeasureRoundTrips(){
		for (Question.Measure measure : Question.Measure.values()){
			Question original = Question.about("test").withMeasure(measure, measure.needsColumn() ? "a" : "");
			Question restored = QuestionCodec.decode(QuestionCodec.encode(original));
			assertEquals(measure, restored.measure);
			assertEquals(measure.needsColumn() ? "a" : "", restored.column);
		}
	}

	@Test public void futureVersionAndBrokenPayloadFailClosed(){
		for (String bad : new String[]{"question\t2\nper\t\nquery\tQQ==\n", "question\t1\nper\tRUN\nquery\tnot-base64\n"}){
			try {
				QuestionCodec.decode(bad);
				fail("unsupported question bytes must not be guessed");
			} catch (IllegalArgumentException expected){
				//preserved by SavedViews as unreadable optional metadata
			}
		}
	}

	@Test public void namedQuestionReopensExactlyAlongsideExecutableQuery() throws Exception {
		DatasetDef def = QuerySupport.def();
		Question original = Question.about(def.id).withMeasure(Question.Measure.TOTAL, "a")
				.withPer(PerUnit.Unit.RUN).withGroupBy(Arrays.asList("name"))
				.addFilter(Filter.text("name", Op.EQ, "visible"));
		File file = SavedViews.fileIn(tmp.getRoot());
		SavedViews views = new SavedViews();
		assertEquals(SavedViews.Result.SAVED, views.saveQuestion("Exact question", original, def, 123));
		views.store(file);
		SavedViews.View restored = SavedViews.load(file).get("exact QUESTION");
		assertNotNull(restored);
		assertNotNull(restored.query());
		assertNotNull(restored.question());
		assertEquals(original.measure, restored.question().measure);
		assertEquals(original.per, restored.question().per);
		assertEquals(original.groupBy, restored.question().groupBy);
		assertEquals(QueryCodec.encode(Questions.toQuery(original, def)), QueryCodec.encode(restored.query()));
	}

	@Test public void legacyTableViewsRemainReadableAndReplacementClearsQuestionMetadata() throws Exception {
		DatasetDef def = QuerySupport.def();
		SavedViews views = new SavedViews();
		views.saveQuestion("same", Question.about(def.id).withPer(PerUnit.Unit.HOUR), def, 1);
		assertNotNull(views.get("same").question());
		views.save("same", Query.of(def.id), 2);
		assertNull(views.get("same").question());
		File file = SavedViews.fileIn(tmp.getRoot());
		views.store(file);
		SavedViews.View restored = SavedViews.load(file).get("same");
		assertNotNull(restored.query());
		assertNull(restored.question());
	}
}

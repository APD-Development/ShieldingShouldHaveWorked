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

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.Fact;
import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public class BossesFinishedScopeTest {
	private ViewDef bosses(){
		for (ViewDef view : Datasets.allViews()) if (view.id.equals("bosses")) return view;
		throw new AssertionError("Bosses view missing");
	}

	@Test public void summaryAndInstancesExcludeUnfinishedEvenAfterScopeChanges(){
		DatasetDef def = Datasets.enemies();
		Row finished = row(def, "finished", true);
		Row live = row(def, "live", false);
		Dataset data = QuerySupport.dataset(def, Arrays.asList(finished, live));
		for (Query original : Arrays.asList(bosses().initialQuery(), bosses().instanceQuery())){
			Scope scope = original.scope.withRules(Scope.Rules.ALL, "").withUnfinished(true)
					.withUnknownRuleset(true).withMixedRuleset(true).withDuplicates(true);
			assertTrue(scope.finishedOnly);
			assertFalse(scope.includeUnfinished);
			QueryResult result = QueryEngine.run(data, original.withScope(scope), "rules");
			assertEquals(1, result.stats.matching);
			assertEquals(1, result.stats.unfinished);
		}
	}

	private Row row(DatasetDef def, String id, boolean finished){
		Row row = new Row(id, id, id, def.size());
		row.ruleset = "rules";
		row.finished = finished;
		row.setNum(def.indexOf("enemy.boss"), 1, Fact.PROV_RECORDED);
		row.setText(def.indexOf("enemy.class"), "Goo", Fact.PROV_RECORDED);
		return row;
	}

	@Test public void aSavedBossQueryKeepsItsFinishedOnlyRestriction(){
		Query original = bosses().initialQuery();
		Query reopened = QueryCodec.decode(QueryCodec.encode(original));
		assertTrue(reopened.scope.finishedOnly);
		assertFalse(reopened.scope.withUnfinished(true).includeUnfinished);
		assertEquals(original.scope, reopened.scope);
	}

	@Test public void unrelatedViewsCanStillIncludeUnfinishedRuns(){
		assertTrue(Query.of("runs").scope.withUnfinished(true).includeUnfinished);
		assertFalse(Query.of("runs").scope.finishedOnly);
	}

	@Test public void aFreshScopeFromTheOptionsCannotRemoveTheViewRestriction(){
		UimsdModel model = new UimsdModel("rules", "");
		Catalog catalog = new Catalog();
		catalog.add(QuerySupport.dataset(Datasets.enemies(), java.util.Collections.emptyList()));
		catalog.add(bosses());
		model.setCatalog(catalog);
		model.setScope(new Scope(Scope.Rules.ALL, "", true, true, true, true));
		assertTrue(model.query().scope.finishedOnly);
		assertFalse(model.query().scope.includeUnfinished);
	}
}

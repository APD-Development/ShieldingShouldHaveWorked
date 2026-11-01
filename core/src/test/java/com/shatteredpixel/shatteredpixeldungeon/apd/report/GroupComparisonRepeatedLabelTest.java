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

package com.shatteredpixel.shatteredpixeldungeon.apd.report;

import com.shatteredpixel.shatteredpixeldungeon.apd.query.Aggregate;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ColumnDef;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Dataset;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.DatasetDef;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.GroupBy;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.GroupRow;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Query;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.QueryEngine;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.QueryResult;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Row;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.StatLine;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.Fact;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GroupComparisonRepeatedLabelTest {

	private static Row row( DatasetDef def, String id, String group, long first, long second ){
		Row row = new Row(id, null, id, def.size());
		row.finished = true;
		row.ruleset = "rules";
		row.setText(0, group, Fact.PROV_RECORDED);
		row.setNum(1, first, Fact.PROV_RECORDED);
		row.setNum(2, second, Fact.PROV_RECORDED);
		return row;
	}

	@Test
	public void repeatedSheetLabelsKeepTheirOwnValuesInGroupComparisons(){
		DatasetDef def = new DatasetDef("comparison", "Comparison", "")
				.add(ColumnDef.text("group", "Group").shown())
				.add(ColumnDef.num("first", "First"))
				.add(ColumnDef.num("second", "Second"));
		def.stat(StatLine.section("First values"));
		def.stat(StatLine.of("Average", Aggregate.of(Aggregate.Fn.AVG, "first"), ""));
		def.stat(StatLine.section("Second values"));
		def.stat(StatLine.of("Average", Aggregate.of(Aggregate.Fn.AVG, "second"), ""));

		Dataset dataset = new Dataset(def, Arrays.asList(
				row(def, "a", "A", 10, 100),
				row(def, "b", "B", 20, 200)), Collections.emptyList());
		Query query = Query.of(def.id).withGroup(new GroupBy(
				Collections.singletonList("group"), Collections.singletonList(Aggregate.count())));
		QueryResult result = QueryEngine.run(dataset, query, "rules");

		GroupRow a = null, b = null;
		for (GroupRow group : result.groups){
			if ("A".equals(group.sample.text(0))) a = group;
			if ("B".equals(group.sample.text(0))) b = group;
		}
		assertNotNull(a);
		assertNotNull(b);

		Report.Table table = ReportBuilder.compareGroups(result, a, b, "", 0, "test").firstTable();
		assertEquals(2, table.rows.size());
		assertEquals("Average", table.rows.get(0)[0].text);
		assertEquals("20", table.rows.get(0)[2].text);
		assertEquals("Average", table.rows.get(1)[0].text);
		assertEquals("200", table.rows.get(1)[2].text);
	}
}

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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ResultTableBoundaryTest {

	private QueryResult result( int count ){
		DatasetDef def = QuerySupport.def();
		List<Row> rows = new ArrayList<>();
		for (int i = 0; i < count; i++){
			rows.add(QuerySupport.row(def, Integer.toString(i), "name-" + i, (long) i, (long) (i % 3), null, true, "r"));
		}
		return QueryEngine.run(QuerySupport.dataset(def, rows), QuerySupport.allScopes(Query.of("things")), "r");
	}

	@Test
	public void directPagingClampsBothEndsAndTreatsNonpositiveSizeAsOne(){
		QueryResult result = result(5);

		ResultTable first = ResultTable.build(result, -7, 2);
		assertEquals(5, first.total);
		assertEquals(0, first.pageStart);
		assertEquals(2, first.pageSize);
		assertEquals(0, first.page());
		assertEquals(3, first.pageCount());
		assertEquals(2, first.lines.size());
		assertEquals("name-0", first.lines.get(0).cells[0].text);

		ResultTable last = ResultTable.build(result, 99, 2);
		assertEquals(4, last.pageStart);
		assertEquals(2, last.page());
		assertEquals(1, last.lines.size());
		assertEquals("name-4", last.lines.get(0).cells[0].text);

		ResultTable one = ResultTable.build(result, 99, 0);
		assertEquals(1, one.pageSize);
		assertEquals(5, one.pageCount());
		assertEquals(4, one.page());
		assertEquals(1, one.lines.size());
	}

	@Test
	public void anEmptyResultStillHasOneEmptyPageAndItsHeaders(){
		ResultTable empty = ResultTable.build(result(0), 99, 3);

		assertEquals(0, empty.total);
		assertEquals(0, empty.pageStart);
		assertEquals(0, empty.page());
		assertEquals(1, empty.pageCount());
		assertTrue(empty.lines.isEmpty());
		assertEquals(4, empty.headers.size());
	}

	@Test
	public void groupedPagesKeepCellsDetailsAndGroupsParallel(){
		QueryResult base = result(5);
		Query groupedQuery = QuerySupport.allScopes(Query.of("things")).withGroup(new GroupBy(
				Arrays.asList("name"),
				Arrays.asList(Aggregate.count(), Aggregate.of(Aggregate.Fn.AVG, "score"))));
		QueryResult grouped = QueryEngine.run(new Dataset(base.def, base.rows, new ArrayList<Dataset.RunInfo>()),
				groupedQuery, "r");
		ResultTable table = ResultTable.build(grouped, 1, 2);

		assertTrue(table.grouped);
		assertEquals(5, table.total);
		assertEquals(2, table.pageStart);
		assertEquals(2, table.lines.size());
		assertEquals(3, table.headers.size());
		for (ResultTable.Line line : table.lines){
			assertNull(line.row);
			assertNotNull(line.group);
			assertEquals(table.headers.size(), line.cells.length);
			assertEquals(table.headers.size(), line.details.length);
			assertEquals("", line.details[0]);
			assertEquals("", line.details[1]);
			assertFalse(line.details[2].isEmpty());
		}
	}
}

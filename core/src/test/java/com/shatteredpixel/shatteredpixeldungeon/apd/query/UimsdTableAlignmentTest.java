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
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
public class UimsdTableAlignmentTest {
 private QueryResult result(boolean grouped){
  DatasetDef def=QuerySupport.def();
  Dataset ds=QuerySupport.dataset(def,Arrays.asList(QuerySupport.row(def,"a","same",12L,1L,null,true,"r"),QuerySupport.row(def,"b","same",null,0L,null,true,"r")));
  Query q=Query.of("things");
  if(grouped)q=q.withGroup(new GroupBy(Arrays.asList("name"),Arrays.asList(Aggregate.count(),Aggregate.of(Aggregate.Fn.AVG,"score"))));
  return QueryEngine.run(ds,q,"r");
 }
 @Test public void numericColumnsAlignWithoutTreatingEnumsAsNumbersOrUnknownAsZero(){
  QueryResult r=result(false);ResultTable t=ResultTable.build(r,0,50);
  assertFalse(t.headers.get(0).numeric);assertTrue(t.headers.get(1).numeric);assertFalse(t.headers.get(2).numeric);
  assertEquals("12",t.lines.get(0).cells[1].text);
  assertEquals(CellText.UNKNOWN,t.lines.get(1).cells[1].state);
  assertEquals(2,r.rows.size());assertEquals(2,t.total);
 }
 @Test public void groupedCountsAndAggregatesAlignButRetainExactValuesAndCoverage(){
  QueryResult r=result(true);ResultTable t=ResultTable.build(r,0,50);
  assertFalse(t.headers.get(0).numeric);assertTrue(t.headers.get(1).numeric);assertTrue(t.headers.get(2).numeric);
  assertEquals("2",t.lines.get(0).cells[1].text);
  assertEquals(CellFormatter.aggregate(r.def,r.aggregates.get(1),r.groups.get(0).aggregates.get(1)).compact,t.lines.get(0).cells[2].text);
  assertFalse(t.lines.get(0).details[2].isEmpty());
 }
}

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
import static org.junit.Assert.*;
public class QueryEmptyTextPersistenceTest {
 @Test public void aKnownEmptyTextFilterSurvivesSaving(){
  Query q=Query.of("runs").withFilters(java.util.Collections.singletonList(new Filter("run.customSeed",Op.EQ,new long[0],new String[]{""})));
  Query restored=QueryCodec.decode(QueryCodec.encode(q));
  assertArrayEquals(new String[]{""},restored.filters.get(0).texts);
 }
 @Test public void emptyListsAndEscapedValuesRemainDistinct(){
  for(String[] values:new String[][]{new String[0],new String[]{""},new String[]{"",""},new String[]{"","a|b","tab\tline\nslash\\","日本"}}){
   Query q=Query.of("runs").withFilters(java.util.Collections.singletonList(new Filter("run.customSeed",Op.IN,new long[0],values)));
   assertArrayEquals(values,QueryCodec.decode(QueryCodec.encode(q)).filters.get(0).texts);
  }
 }
 @Test public void oldSavedFiltersWithoutCountsStillLoad(){
  Query q=QueryCodec.decode("dataset\truns\nfilter\trun.customSeed\tEQ\t\tABC\n");
  assertArrayEquals(new String[]{"ABC"},q.filters.get(0).texts);
  assertTrue(QueryCodec.encode(q).contains("filter\trun.customSeed\tEQ\t\tABC\n"));
  assertEquals(0,QueryCodec.decode("dataset\truns\nfilter\trun.customSeed\tEQ\t\t\n").filters.get(0).texts.length);
 }
 @Test public void damagedCountsAreRejectedInsteadOfChangingTheFilter(){
  try{QueryCodec.decode("dataset\truns\nfilter\trun.customSeed\tIN\t\tABC\t2\n");fail("invalid count accepted");}
  catch(IllegalArgumentException expected){}
 }
 @Test public void anEmptyTextFilterSurvivesActualSavedViewStorage() throws Exception {
  java.io.File file=java.nio.file.Files.createTempFile("apd-query-empty", ".txt").toFile();
  try {
   Query q=Query.of("runs").withFilters(java.util.Collections.singletonList(new Filter("run.customSeed",Op.EQ,new long[0],new String[]{""})));
   SavedViews views=new SavedViews(); views.save("Empty seed",q,123L); views.store(file);
   SavedViews loaded=SavedViews.load(file);
   assertEquals(0,loaded.skippedOnLoad);
   assertArrayEquals(new String[]{""},loaded.get("Empty seed").query().filters.get(0).texts);
  } finally { file.delete(); new java.io.File(file.getPath()+".tmp").delete(); }
 }
}

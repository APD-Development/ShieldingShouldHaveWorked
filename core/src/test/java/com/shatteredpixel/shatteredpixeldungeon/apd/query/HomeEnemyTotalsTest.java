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
import java.util.*;
import static org.junit.Assert.*;

public class HomeEnemyTotalsTest {
 private UimsdHome.Card card(String[] names, Long[] totals, int[] provenance) throws Exception {
  DatasetDef d=Datasets.enemies();List<Row> rows=new ArrayList<>();
  for(int i=0;i<names.length;i++){Row r=new Row("row-"+i,null,"run-"+i,d.size());r.finished=true;r.ruleset="r";
   r.setText(d.indexOf("enemy.name"),names[i],Fact.PROV_RECORDED);r.setText(d.indexOf("enemy.run"),r.runId,Fact.PROV_RECORDED);
   if(totals[i]!=null)r.setNum(d.indexOf("enemy.heroDeaths"),totals[i],provenance[i]);rows.add(r);}
  Catalog cat=new Catalog();cat.add(new Dataset(d,rows,Collections.<Dataset.RunInfo>emptyList()));
  for(ViewDef v:Datasets.allViews())cat.add(v);
  java.lang.reflect.Method method=UimsdHome.class.getDeclaredMethod("enemies",Catalog.class,Scope.class,String.class);method.setAccessible(true);
  return (UimsdHome.Card)method.invoke(null,cat,Scope.DEFAULT,"r");
 }
 private List<UimsdHome.Line> deaths(UimsdHome.Card c){List<UimsdHome.Line> out=new ArrayList<>();boolean take=false;
  for(UimsdHome.Line l:c.lines){if(l.heading){take=l.label.equals("Ended your runs");continue;}if(take)out.add(l);}return out;}
 @Test public void cachedTotalsPreserveDescendingOrderAndStableTies() throws Exception {
  List<UimsdHome.Line> lines=deaths(card(new String[]{"First tie","Second tie","Rare greatest","Zero","Unknown"},
   new Long[]{4L,4L,5L,0L,null},new int[]{1,1,1,1,1}));
  assertEquals(3,lines.size());assertEquals("Rare greatest",lines.get(0).label);assertEquals("5 deaths",lines.get(0).value);
  assertEquals("First tie",lines.get(1).label);assertEquals("Second tie",lines.get(2).label);
 }
 @Test public void lowerAndInferredTotalsKeepTheirDisclosureMarks() throws Exception {
  List<UimsdHome.Line> lines=deaths(card(new String[]{"Lower","Inferred"},new Long[]{2L,1L},new int[]{Fact.PROV_INCOMPLETE,Fact.PROV_INFERRED}));
  assertEquals(2,lines.size());assertEquals(CellText.LOWER,lines.get(0).state);assertTrue(lines.get(0).value.startsWith(">="));
  assertEquals(CellText.INFERRED,lines.get(1).state);assertTrue(lines.get(1).value.startsWith("~"));
 }
 @Test public void unknownAndZeroTotalsDoNotInventADeathRanking() throws Exception {
  assertTrue(deaths(card(new String[]{"Unknown","Zero"},new Long[]{null,0L},new int[]{1,1})).isEmpty());
 }
}

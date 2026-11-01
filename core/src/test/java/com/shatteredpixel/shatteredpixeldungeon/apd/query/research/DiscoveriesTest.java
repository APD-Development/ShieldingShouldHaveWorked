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

package com.shatteredpixel.shatteredpixeldungeon.apd.query.research;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.*;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.Fact;
import org.junit.Test;
import static org.junit.Assert.*;
public class DiscoveriesTest {
    @Test public void supportedContrastHasTheExactContributingRuns(){ResearchFixture f=new ResearchFixture();for(int i=0;i<25;i++){f.add("a"+i,"mage",1,0);f.add("b"+i,"warrior",0,0);}Discoveries.Result r=Discoveries.build(f.result());assertEquals(1,r.findings.size());Discoveries.Finding d=r.findings.get(0);assertEquals(25,d.a.rows.size());assertEquals(25,d.b.rows.size());assertEquals(100,Math.abs(d.difference),0);assertTrue(d.separated());assertEquals(1,r.comparisons);}
    @Test public void smallSamplesAndUnknownOutcomesNeverInventFindings(){ResearchFixture f=new ResearchFixture();for(int i=0;i<19;i++){f.add("a"+i,"mage",1,0);f.add("b"+i,"warrior",0,0);}Row unknown=f.add("unknown","mage",1,0);unknown.clear(f.def.indexOf("result.outcome"));assertTrue(Discoveries.build(f.result()).findings.isEmpty());assertEquals(1,Discoveries.build(f.result()).excluded);}
    @Test public void versionsAndExactRulesRemainSeparate(){ResearchFixture f=new ResearchFixture();for(int i=0;i<25;i++){f.add("a"+i,"mage",1,0);Row r=f.add("b"+i,"warrior",0,0);f.text(r,"run.apdVersion","other");}assertTrue(Discoveries.build(f.result()).findings.isEmpty());for(Row r:f.rows)if(r.id.startsWith("b")){f.text(r,"run.apdVersion","version-1");r.ruleset="different";}assertTrue(Discoveries.build(f.result()).findings.isEmpty());}
    @Test public void abandonedRunsRemainNonWinsAndUncertainImportsAreExcluded(){ResearchFixture f=new ResearchFixture();for(int i=0;i<25;i++){f.add("a"+i,"mage",1,0);f.add("b"+i,"warrior",2,0);}f.add("import","mage",1,0).imported=true;Row incomplete=f.add("partial","mage",1,0);incomplete.setNum(f.def.indexOf("result.outcome"),1,Fact.PROV_INCOMPLETE);Discoveries.Result r=Discoveries.build(f.result());assertEquals(2,r.excluded);assertEquals(1,r.findings.size());assertEquals(2,f.rows.get(1).num(f.def.indexOf("result.outcome")));}
    @Test public void wilsonBoundsIncludeSamplingUncertaintyEvenAtZeroAndOne(){double[] zero=Discoveries.wilson(0,20,1.96),one=Discoveries.wilson(20,20,1.96);assertEquals(0,zero[0],1e-12);assertTrue(zero[1]>.1);assertTrue(one[0]<.9);assertEquals(1,one[1],1e-12);assertEquals(1.959963984540,Discoveries.normalQuantile(.975),1e-6);}
}

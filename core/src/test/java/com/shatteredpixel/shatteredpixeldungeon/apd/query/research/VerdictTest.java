package com.shatteredpixel.shatteredpixeldungeon.apd.query.research;
import org.junit.Test;
import static org.junit.Assert.*;
public class VerdictTest {
    private PopulationStudy.Sample s(ResearchFixture f,String cls){return PopulationStudy.sample(f.result(f.forClass(cls)),"result.score");}
    @Test public void clearGapSaysWhichSideIsHigherWithoutACause(){ResearchFixture f=new ResearchFixture();for(int i=0;i<20;i++){f.add("a"+i,"mage",0,10+i);f.add("b"+i,"warrior",0,30+i);}
        String v=Verdict.compare(PopulationStudy.compare(s(f,"mage"),s(f,"warrior")),"score","A","B",1);
        assertTrue(v,v.startsWith("In short: B has a higher score than A by about 20.00"));assertTrue(v.contains("doesn't say why"));}
    @Test public void overlappingRangeIsNoClearDifference(){ResearchFixture f=new ResearchFixture();for(int i=0;i<20;i++){f.add("a"+i,"mage",0,i);f.add("b"+i,"warrior",0,i+1);}
        String v=Verdict.compare(PopulationStudy.compare(s(f,"mage"),s(f,"warrior")),"score","A","B",1);assertTrue(v,v.contains("no clear difference"));}
    @Test public void smallSamplesSayNotEnoughRuns(){ResearchFixture f=new ResearchFixture();f.add("a","mage",0,5);f.add("b","warrior",0,9);
        String v=Verdict.compare(PopulationStudy.compare(s(f,"mage"),s(f,"warrior")),"score","A","B",1);assertTrue(v,v.contains("B averages 4.00 more score than A"));assertTrue(v.contains("aren't enough comparable runs (1 vs 1)"));}
    @Test public void emptySideAndNullAreHonest(){ResearchFixture f=new ResearchFixture();f.add("a","mage",0,5);
        assertTrue(Verdict.compare(PopulationStudy.compare(s(f,"mage"),s(f,"warrior")),"score","A","B",1).contains("no exact score on B's side"));
        assertEquals("In short: these two can't be compared.",Verdict.compare(null,"score","A","B",1));}
    @Test public void studyDocumentLeadsWithTheVerdict(){ResearchFixture f=new ResearchFixture();for(int i=0;i<20;i++){f.add("a"+i,"mage",0,10+i);f.add("b"+i,"warrior",0,30+i);}
        PopulationStudy.Comparison c=PopulationStudy.compare(s(f,"mage"),s(f,"warrior"));
        ResearchDocument d=ResearchDocument.study(f.result(f.forClass("mage")),f.result(f.forClass("warrior")),c);assertTrue(d.notes.get(0).startsWith("In short:"));assertEquals(c.note,d.notes.get(1));}
    @Test public void luckMetricRestatesThePercentile(){
        assertEquals("generated items (12): more than most, above 90% of 14 comparable runs",Verdict.luckMetric("Generated items",12L,14,90.4));
        assertEquals("generated items (3): fewer than most, above 10% of 14 comparable runs",Verdict.luckMetric("Generated items",3L,14,10));
        assertTrue(Verdict.luckMetric("Generated items",7L,14,50).contains("about as many as usual"));
        assertEquals("generated items (7) can't be ranked yet: 4 comparable runs, 10 needed",Verdict.luckMetric("Generated items",7L,4,Double.NaN));
        assertEquals("this run's generated items isn't known exactly",Verdict.luckMetric("Generated items",null,14,Double.NaN));
        assertEquals("In short: this run can't be checked.",Verdict.luck(null));}
}

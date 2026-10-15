package com.shatteredpixel.shatteredpixeldungeon.apd.query;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.util.*;
import static org.junit.Assert.*;

public class ScaledAggregateDisplayTest {
 @Rule public TemporaryFolder tmp=new TemporaryFolder();
 private static final String RS="r1-aaaaaaaaaaaa";
 private static final FactKind KIND=FactKinds.register(FactKind.builder("test.scaled.display","Scaled observation")
  .value("hundredths","Hundredths","",FactKind.HUNDREDTHS)
  .value("thousandths","Thousandths","",FactKind.THOUSANDTHS).build());
 private Catalog history(){
  new SyntheticLog(tmp.getRoot(),"scaled",TelemetryVersion.CURRENT,100)
   .start("run-scaled",0,"CLERIC",RS,0,30,false).enter(1)
   .fact(KIND,"first",125,1250).fact(KIND,"second",375,3750)
   .tick(10).leave().end(LifecycleEvents.OUTCOME_DIED,"rat",1,1,10).close(true);
  return new DatasetStore(tmp.getRoot()).load();
 }
 private Question question(String column){return Question.about(FactDatasets.idOf(KIND))
  .withScope(new Scope(Scope.Rules.ALL,"",true,true,true,true)).withMeasure(Question.Measure.AVERAGE,column);}
 @Test public void hundredthsMeansAndMediansUseRecordedDisplayUnits(){checkEngine(history(),"fact.v.hundredths",250);}
 @Test public void thousandthsMeansAndMediansUseRecordedDisplayUnits(){checkEngine(history(),"fact.v.thousandths",2500);}
 private void checkEngine(Catalog c,String column,double raw){
  Dataset ds=c.dataset(FactDatasets.idOf(KIND));
  for(Aggregate.Fn fn:new Aggregate.Fn[]{Aggregate.Fn.AVG,Aggregate.Fn.MEDIAN}){
   Query q=Questions.toQuery(question(column),ds.def).withGroup(new GroupBy(Collections.emptyList(),Arrays.asList(Aggregate.of(fn,column))));
   QueryResult r=QueryEngine.run(ds,q,RS);assertEquals(1,r.groups.size());
   AggValue v=r.groups.get(0).aggregates.get(0);assertEquals(raw,v.value,0);
   assertEquals("2.5",CellFormatter.aggregate(ds.def,r.aggregates.get(0),v).text);
  }
 }
 @Test public void customQuestionsUseTheSameUnitsForTotalsAveragesMediansAndPerRunRates(){
  Catalog c=history();
  for(String col:new String[]{"fact.v.hundredths","fact.v.thousandths"}){
   for(Question.Measure m:new Question.Measure[]{Question.Measure.AVERAGE,Question.Measure.TYPICAL}){
    Questions.Line l=Questions.answer(c,question(col).withMeasure(m,col),RS).lines.get(0);
    assertEquals("2.5",l.value);assertEquals(2.5,l.number,0);
   }
   Questions.Line total=Questions.answer(c,question(col).withMeasure(Question.Measure.TOTAL,col),RS).lines.get(0);
   assertEquals(5.0,total.number,0);
   Questions.Line per=Questions.answer(c,question(col).withMeasure(Question.Measure.TOTAL,col).withPer(PerUnit.Unit.RUN),RS).lines.get(0);
   assertEquals("5.00",per.value);assertEquals(5.0,per.number,0);
  }
 }
 @Test public void ordinaryCountsUnknownsAndProvenanceStayUnchanged(){
  DatasetDef d=new DatasetDef("values","Values","");d.add(ColumnDef.num("raw","Raw"));d.add(ColumnDef.num("scaled","Scaled").format(ColumnDef.Format.HUNDREDTHS));
  AggValue v=new AggValue(true,250,0,2,2,AggValue.LOWER|AggValue.INFERRED);
  assertEquals(">=~250",CellFormatter.aggregate(d,Aggregate.of(Aggregate.Fn.AVG,"raw"),v).text);
  assertEquals(">=~2.5",CellFormatter.aggregate(d,Aggregate.of(Aggregate.Fn.AVG,"scaled"),v).text);
  assertEquals("?",CellFormatter.aggregate(d,Aggregate.of(Aggregate.Fn.AVG,"scaled"),new AggValue(false,0,0,0,2,0)).text);
  Catalog c=history();Questions.Line count=Questions.answer(c,Question.about(FactDatasets.idOf(KIND)).withScope(new Scope(Scope.Rules.ALL,"",true,true,true,true)),RS).lines.get(0);
  assertEquals("2",count.value);assertEquals(2,count.number,0);
 }
 @Test public void roundedLowerBoundsMustNotOverstateScaledOrOrdinaryMeans(){
  DatasetDef d=new DatasetDef("bounds","Bounds","");
  d.add(ColumnDef.num("raw","Raw"));
  d.add(ColumnDef.num("scaled","Scaled").format(ColumnDef.Format.HUNDREDTHS));
  for(Aggregate.Fn fn:new Aggregate.Fn[]{Aggregate.Fn.AVG,Aggregate.Fn.MEDIAN}){
   assertEquals(">=2.4",CellFormatter.aggregate(d,Aggregate.of(fn,"scaled"),new AggValue(true,246,0,1,1,AggValue.LOWER)).text);
   assertEquals(">=-2.5",CellFormatter.aggregate(d,Aggregate.of(fn,"scaled"),new AggValue(true,-246,0,1,1,AggValue.LOWER)).text);
   assertEquals(">=2.4",CellFormatter.aggregate(d,Aggregate.of(fn,"raw"),new AggValue(true,2.46,0,1,1,AggValue.LOWER)).text);
   assertEquals("2.5",CellFormatter.aggregate(d,Aggregate.of(fn,"raw"),new AggValue(true,2.46,0,1,1,0)).text);
  }
 }
}

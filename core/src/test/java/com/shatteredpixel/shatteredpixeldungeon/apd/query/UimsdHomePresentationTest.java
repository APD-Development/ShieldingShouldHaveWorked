package com.shatteredpixel.shatteredpixeldungeon.apd.query;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import java.util.ArrayList;
import java.io.File;
import static org.junit.Assert.*;
import com.shatteredpixel.shatteredpixeldungeon.apd.demo.DemoGenerator;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.Ruleset;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKeys;
public class UimsdHomePresentationTest {
 @Rule public TemporaryFolder tmp = new TemporaryFolder();
 @Test public void zeroAbandonedRunsRemainVisibleAndOutcomeDrilldownsMatchRecords() throws Exception {
  File root = com.shatteredpixel.shatteredpixeldungeon.apd.demo.DemoData.sampleRootFor(new File(tmp.newFolder("data"),"apd")); Ruleset rules = Ruleset.of(30,0);
  DemoGenerator.generateSample(root,rules,1800000000000L);
  Catalog original = new DatasetStore(root).load(), cat = new Catalog();
  for(Dataset ds : original.datasets()){
   if(!ds.def.id.equals(Datasets.RUNS)) {cat.add(ds);continue;}
   ArrayList<Row> keep = new ArrayList<>();
   for(Row row : ds.rows) if(row.isKnown(ds.def.indexOf(FactKeys.OUTCOME))&&row.num(ds.def.indexOf(FactKeys.OUTCOME))!=2)keep.add(row);
   cat.add(new Dataset(ds.def,keep,ds.runs));
  }
  for(ViewDef view : original.views())cat.add(view);
  cat.rulesets.addAll(original.rulesets);
  UimsdHome home=UimsdHome.build(cat,rules.id,true);
  UimsdHome.Line abandoned=null;
  for(UimsdHome.Line line:home.card("runs").lines)if(line.label.equals("Abandoned"))abandoned=line;
  assertNotNull(abandoned);assertEquals("0",abandoned.value);assertEquals(CellText.EXACT,abandoned.state);
  for(int outcome=0;outcome<3;outcome++){
   QueryResult result=QueryEngine.run(cat.dataset(Datasets.RUNS),UimsdHomeNavigation.outcome("!outcome:"+outcome,true),rules.id);
   for(Row row:result.rows)assertEquals(outcome,row.num(result.def.indexOf(FactKeys.OUTCOME)));
   if(outcome==2)assertTrue(result.rows.isEmpty());
  }
 }
}

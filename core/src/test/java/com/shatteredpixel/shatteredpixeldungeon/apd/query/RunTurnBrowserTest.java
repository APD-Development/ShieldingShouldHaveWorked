package com.shatteredpixel.shatteredpixeldungeon.apd.query;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKeys;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.Fact;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class RunTurnBrowserTest {
    private RunTurnBrowser example() {
        Catalog cat=new Catalog();
        DatasetDef runs=Datasets.runs();
        Row run=new Row("run-one",null,"id-one",runs.size());
        run.setNum(runs.indexOf(FactKeys.TURNS),5000,Fact.PROV_RECORDED);
        cat.add(new Dataset(runs,Arrays.asList(run),new ArrayList<>()));
        DatasetDef def=EventDatasets.attacks();
        Row r=new Row("event-one",null,"id-one",def.size());
        r.setNum(def.indexOf("attack.turn"),21,Fact.PROV_RECORDED);
        int attacker=def.indexOf("attack.attacker");
        if(attacker>=0)r.setText(attacker,"Rat",Fact.PROV_RECORDED);
        cat.add(new Dataset(def,Arrays.asList(r),new ArrayList<>()));
        return new RunTurnBrowser(cat,run);
    }
    @Test public void startsAtOneAndPreservesSparseEmptyTurns(){RunTurnBrowser b=example();assertEquals(1,b.turn());assertTrue(b.events().isEmpty());assertEquals(5000,b.lastTurn);}
    @Test public void correctTurnAndCustomNavigation(){RunTurnBrowser b=example();b.go(21);assertEquals(1,b.events().size());b.step(100);b.next();assertEquals(121,b.turn());b.previous();assertEquals(21,b.turn());b.go(999999);assertEquals(5000,b.turn());}
    @Test public void zeroStepRejected(){RunTurnBrowser b=example();try{b.step(0);fail();}catch(IllegalArgumentException expected){}}
    @Test public void importedRunRejected(){Catalog cat=new Catalog();DatasetDef d=Datasets.runs();Row r=new Row("x",null,"x",d.size());r.imported=true;try{new RunTurnBrowser(cat,r);fail();}catch(IllegalArgumentException expected){}}
}

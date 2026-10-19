package com.shatteredpixel.shatteredpixeldungeon.apd.stats;

import com.shatteredpixel.shatteredpixeldungeon.apd.archive.*;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.io.*;
import java.nio.file.Files;
import static org.junit.Assert.*;

public class RunOutcomePersistenceTest {
    @Rule public TemporaryFolder tmp=new TemporaryFolder();
    private File log(File root,int outcome,boolean old,boolean ascended)throws Exception {
        File file=new File(root,"runs/result.apdlog");
        RunLogWriter w=RunLogWriter.create(file,TelemetryVersion.CURRENT,"result","test",1);
        LifecycleEvents.RunStart start=new LifecycleEvents.RunStart();start.runId="result";
        LifecycleEvents.emitRunStart(w,0,false,start);
        // A death event does not prove final death: an Ankh can revive the hero.
        CombatEvents.emitHeroDeath(w,1,new CombatEvents.HeroDeath());
        if(old)w.event(EventTypes.RUN_END).clock(2).unsigned(outcome).string("").unsigned(1).unsigned(1)
                .unsigned(1).signed(0).unsigned(2).unsigned(0).emit();
        else {
            LifecycleEvents.RunEnd end=new LifecycleEvents.RunEnd();end.outcome=outcome;
            end.ascendedKnown=true;end.ascended=ascended;LifecycleEvents.emitRunEnd(w,2,end);
        }
        assertTrue(w.close(true));return file;
    }
    @Test public void oldOutcomesRemainUnchangedAndUnrecordedAscensionStaysUnknown()throws Exception {
        for(int outcome=0;outcome<=2;outcome++){
            File root=tmp.newFolder();File file=log(root,outcome,true,false);byte[] before=Files.readAllBytes(file.toPath());
            RunFacts facts=RunFactsBuilder.fromLog(RunLogReader.read(file),"result");
            assertEquals(outcome,facts.numOr(FactKeys.OUTCOME,-1));assertFalse(facts.isKnown("run.ascended"));
            new DatasetStore(root).load();new DerivedStore(root).load();
            assertArrayEquals("Reading/rebuilding never migrates uncertain raw history",before,Files.readAllBytes(file.toPath()));
        }
    }
    @Test public void everyOutcomeAndAscensionSurviveArchiveExportImportAndCaches()throws Exception {
        for(int variant=0;variant<4;variant++){
            int outcome=variant==3?LifecycleEvents.OUTCOME_WON:variant;boolean ascended=variant==3;
            File root=tmp.newFolder();File original=log(root,outcome,false,ascended);
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();ArchiveExporter.export(root,bytes,"test",1);
            File dest=tmp.newFolder();ArchiveImporter.Report report=ArchiveImporter.importArchive(dest,new ByteArrayInputStream(bytes.toByteArray()));
            assertTrue(report.summary(),report.ok);
            File copy=new File(dest,"runs/result.apdlog");assertArrayEquals(Files.readAllBytes(original.toPath()),Files.readAllBytes(copy.toPath()));
            for(int pass=0;pass<2;pass++){
                Dataset runs=new DatasetStore(dest).load().dataset(Datasets.RUNS);Row row=runs.rows.get(0);
                assertEquals(outcome,row.num(runs.def.indexOf(FactKeys.OUTCOME)));
                assertEquals(ascended?1:0,row.num(runs.def.indexOf("run.ascended")));
                assertEquals(outcome,new DerivedStore(dest).load().apdRows.get(0).numOr(FactKeys.OUTCOME,-1));
            }
        }
    }
}

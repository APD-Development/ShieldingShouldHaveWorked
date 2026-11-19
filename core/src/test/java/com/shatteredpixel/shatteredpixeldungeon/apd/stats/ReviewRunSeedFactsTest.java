package com.shatteredpixel.shatteredpixeldungeon.apd.stats;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import static org.junit.Assert.*;
/** Run reviews: a review run's log start carries no seed, and its facts leave the seed unknown (never 0), so
 *  seed history and Seed floors can't rebuild or show it. Ordinary runs keep their recorded seed. */
public class ReviewRunSeedFactsTest {
    @Rule public TemporaryFolder tmp=new TemporaryFolder();
    private RunFacts recorded(int seedType,long seed,String text) throws Exception {
        File file=tmp.newFile(); assertTrue(file.delete());
        RunLogWriter writer=RunLogWriter.create(file,TelemetryVersion.CURRENT,"review-run","test",1);
        LifecycleEvents.RunStart start=new LifecycleEvents.RunStart();
        start.runId="review-run"; start.heroClass="WARRIOR"; start.seed=seed; start.seedType=seedType; start.customSeedText=text;
        assertTrue(LifecycleEvents.emitRunStart(writer,0,false,start));
        assertTrue(writer.close());
        return RunFactsBuilder.fromLog(RunLogReader.read(file),"review-run");
    }
    @Test public void reviewRunSeedIsUnknownButOrdinaryRunsKeepTheirs() throws Exception {
        RunFacts review=recorded(LifecycleEvents.SEED_REVIEW,0,"");
        assertFalse(review.isKnown(FactKeys.SEED));
        assertFalse(review.isKnown(FactKeys.CUSTOM_SEED));
        assertEquals(0,review.numOr(FactKeys.DAILY,-1));
        RunFacts normal=recorded(LifecycleEvents.SEED_RANDOM,12345,"");
        assertEquals(12345,normal.numOr(FactKeys.SEED,-1));
    }
}

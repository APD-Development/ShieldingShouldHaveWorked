/* ADHD Pixel Dungeon (C) 2026 Chris Charlton, GPL-3.0-or-later */
package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.watabou.utils.Bundle;
import org.junit.Test;
import static org.junit.Assert.*;

public class SeedReuseScoreTest {
    private static Bundle legacy(int version,long base,int attempts,int prior,int current,int bps) {
        Bundle data=new Bundle();
        data.put(SeedReuseScore.VERSION_KEY,version);
        data.put(SeedReuseScore.BASE_KEY,base);
        data.put(SeedReuseScore.EARNED_KEY,base*bps/10000);
        data.put(SeedReuseScore.ATTEMPTS_KEY,attempts);
        data.put(SeedReuseScore.PRIOR_DEPTH_KEY,prior);
        data.put(SeedReuseScore.CURRENT_DEPTH_KEY,current);
        data.put(SeedReuseScore.RETENTION_KEY,bps);
        data.put(SeedReuseScore.KNOWN_KEY,true);
        return data;
    }

    @Test public void exactOwnerFormulaSevenPreviousAttemptsFloorNineToTwentySix() {
        SeedReuseScore.Result result=SeedReuseScore.calculate(330393,7,9,26,true);
        // R = 100 - 75 * (9 / 25)^3 - 5 * (7 - 1) = 66.5008%.
        assertEquals(6650,result.retentionBasisPoints);
        assertEquals(219713,result.earned);
        assertEquals(110680,result.deduction);
        assertEquals(330393,result.before);
        assertEquals(7,result.priorAttempts);
        assertEquals(9,result.deepestBefore);
        assertEquals(26,result.currentDepth);
        Rankings.Record rec=new Rankings.Record();
        rec.score=330393;
        rec.customSeed="ABC-DEF-GHI";
        rec.gameID="owner-run";
        rec.gameData=new Bundle();
        result.store(rec.gameData);
        assertEquals(219713,rec.earnedScore());
        Rankings.Record lesser=new Rankings.Record();
        lesser.score=200000;
        lesser.customSeed=rec.customSeed;
        lesser.gameID="lower-run";
        assertTrue("Rankings must sort by corrected earned score",
                Rankings.scoreComparator.compare(rec,lesser)<0);
        Bundle saved=new Bundle();
        rec.storeInBundle(saved);
        Rankings.Record loaded=new Rankings.Record();
        loaded.restoreFromBundle(saved);
        assertEquals(219713,loaded.earnedScore());
        assertEquals(330393,loaded.score);
    }

    @Test public void previousDepthAndAttemptsNotCurrentDepthDetermineFormula() {
        assertEquals(SeedReuseScore.calculate(330393,7,9,9,true).earned,
                     SeedReuseScore.calculate(330393,7,9,26,true).earned);
        assertEquals(SeedReuseScore.calculate(330393,7,9,10,true).earned,
                     SeedReuseScore.calculate(330393,7,9,26,true).earned);
        assertEquals(215416,SeedReuseScore.calculate(330393,7,10,26,true).earned);
        assertEquals(0,SeedReuseScore.calculate(330393,7,25,26,true).earned);
    }

    @Test public void cubicDepthAndReplayCountExactlyMatchOwnerEquation() {
        assertEquals(10000,SeedReuseScore.calculate(2000,0,-1,26,true).retentionBasisPoints);
        assertEquals(10000,SeedReuseScore.calculate(2000,1,0,0,true).retentionBasisPoints);
        assertEquals(9500,SeedReuseScore.calculate(2000,2,0,0,true).retentionBasisPoints);
        assertEquals(9000,SeedReuseScore.calculate(2000,3,0,0,true).retentionBasisPoints);
        assertEquals(8500,SeedReuseScore.calculate(2000,4,0,0,true).retentionBasisPoints);
        assertEquals(8000,SeedReuseScore.calculate(2000,5,0,0,true).retentionBasisPoints);
        assertEquals(9940,SeedReuseScore.calculate(2000,1,5,5,true).retentionBasisPoints);
        assertEquals(1988,SeedReuseScore.calculate(2000,1,5,5,true).earned);
        assertEquals(9020,SeedReuseScore.calculate(2000,2,10,10,true).retentionBasisPoints);
        assertEquals(1804,SeedReuseScore.calculate(2000,2,10,10,true).earned);
        assertEquals(2500,SeedReuseScore.calculate(2000,1,25,25,true).retentionBasisPoints);
        assertEquals(500,SeedReuseScore.calculate(2000,1,25,25,true).earned);
        assertEquals(0,SeedReuseScore.calculate(2000,100,25,26,true).earned);
        assertEquals(10000,SeedReuseScore.calculate(2000,7,25,26,false).retentionBasisPoints);
    }

    @Test public void retroactivelyMigrateOriginalZeroV1Receipt() {
        Bundle old=legacy(1,330393,7,9,26,0);
        assertEquals(219713,SeedReuseScore.earnedScore(old,330393));
        assertTrue(SeedReuseScore.migrate(old));
        assertEquals(3,old.getInt(SeedReuseScore.VERSION_KEY));
        assertEquals(219713,old.getLong(SeedReuseScore.EARNED_KEY));
        assertEquals(6650,old.getInt(SeedReuseScore.RETENTION_KEY));
        assertFalse(SeedReuseScore.migrate(old));
        assertEquals(219713,SeedReuseScore.earnedScore(old,330393));
        assertEquals(330392,SeedReuseScore.earnedScore(old,330392));
    }

    @Test public void retroactivelyMigrateIncorrectV2FormulaWithoutRepeatingPenalty() {
        Bundle old=legacy(2,330393,7,9,26,1700);
        assertEquals(56166,old.getLong(SeedReuseScore.EARNED_KEY));
        assertEquals(219713,SeedReuseScore.earnedScore(old,330393));
        assertTrue(SeedReuseScore.migrate(old));
        assertEquals(3,old.getInt(SeedReuseScore.VERSION_KEY));
        assertEquals(219713,old.getLong(SeedReuseScore.EARNED_KEY));
        assertFalse(SeedReuseScore.migrate(old));
        assertEquals(219713,SeedReuseScore.earnedScore(old,330393));
    }

    @Test public void correctNewReceiptSurvivesRankingBundleRoundTrip() {
        Rankings.Record record=new Rankings.Record();
        record.score=2000;
        record.heroClass=HeroClass.WARRIOR;
        record.customSeed="ABC-DEF-GHI";
        record.gameID="saved-run";
        record.date="2026-11-14";
        record.version="v4.0.8";
        record.gameData=new Bundle();
        SeedReuseScore.calculate(2000,2,10,10,true).store(record.gameData);
        assertEquals(1804,record.earnedScore());
        Bundle archived=new Bundle();
        record.storeInBundle(archived);
        Rankings.Record loaded=new Rankings.Record();
        loaded.restoreFromBundle(archived);
        assertEquals(1804,loaded.earnedScore());
        assertEquals(2000,loaded.score);
    }

    @Test public void damagedOrFabricatedOldReceiptsRemainUntouched() {
        Bundle forged=legacy(2,330393,7,9,26,1700);
        forged.put(SeedReuseScore.EARNED_KEY,56167L);
        assertNull(SeedReuseScore.restore(forged));
        assertFalse(SeedReuseScore.migrate(forged));
        assertEquals(330393,SeedReuseScore.earnedScore(forged,330393));
        Bundle bogus=legacy(1,330393,7,9,26,2500);
        assertNull(SeedReuseScore.restore(bogus));
        assertFalse(SeedReuseScore.migrate(bogus));
    }

    @Test public void absentHistoryAndOldRunsDoNotInventPenalties() {
        Bundle old=new Bundle();
        assertEquals(932,SeedReuseScore.earnedScore(old,932));
        assertFalse(SeedReuseScore.migrate(old));
        SeedReuseScore.Result unknown=SeedReuseScore.calculate(932,7,-1,26,false);
        assertEquals(932,unknown.earned);
        unknown.store(old);
        assertEquals(932,SeedReuseScore.earnedScore(old,932));
    }

    @Test public void futureSeedAttemptsDoNotRepriceRecordedSnapshot() {
        Bundle frozen=new Bundle();
        SeedReuseScore.calculate(10000,7,9,26,true).store(frozen);
        assertEquals(6650,SeedReuseScore.restore(frozen).retentionBasisPoints);
        assertEquals(6650,SeedReuseScore.earnedScore(frozen,10000));
        assertEquals(6650,SeedReuseScore.earnedScore(frozen,10000));
        assertEquals(9999,SeedReuseScore.earnedScore(frozen,9999));
    }
}

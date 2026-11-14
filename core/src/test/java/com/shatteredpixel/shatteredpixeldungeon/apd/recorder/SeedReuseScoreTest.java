/* ADHD Pixel Dungeon (C) 2026 Chris Charlton, GPL-3.0-or-later */
package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.watabou.utils.Bundle;
import org.junit.Test;

import static org.junit.Assert.*;

public class SeedReuseScoreTest {
    @Test public void approvedAttemptTiersApplyToActualEarnedScore() {
        assertEquals(10000, SeedReuseScore.calculate(2000,0,-1,3,false).retentionBasisPoints);
        assertEquals(10000, SeedReuseScore.calculate(2000,0,3,3,true).retentionBasisPoints);
        assertEquals(7500, SeedReuseScore.calculate(2000,1,0,0,true).retentionBasisPoints);
        assertEquals(5000, SeedReuseScore.calculate(2000,2,0,0,true).retentionBasisPoints);
        assertEquals(2500, SeedReuseScore.calculate(2000,3,0,0,true).retentionBasisPoints);
        assertEquals(0, SeedReuseScore.calculate(2000,4,0,0,true).retentionBasisPoints);
        assertEquals(0, SeedReuseScore.calculate(2000,10,0,0,true).earned);
    }

    @Test public void earlyDeathIsCheaperThanYogAndNewDepthEarnsMore() {
        SeedReuseScore.Result early=SeedReuseScore.calculate(2000,1,5,5,true);
        SeedReuseScore.Result yog=SeedReuseScore.calculate(2000,1,25,25,true);
        SeedReuseScore.Result newDepth=SeedReuseScore.calculate(2000,1,5,10,true);
        assertEquals(6000,early.retentionBasisPoints);
        assertEquals(1200,early.earned);
        assertEquals(800,early.deduction);
        assertEquals(0,yog.earned);
        assertTrue(newDepth.earned>early.earned);
        assertEquals(7500,newDepth.retentionBasisPoints);
        assertEquals(1500,newDepth.earned);
    }

    @Test public void genuineScoreSnapshotPowersRankingScoreAndSurvivesBundle() {
        Rankings.Record record=new Rankings.Record();
        record.score=2000;
        record.heroClass=HeroClass.WARRIOR;
        record.customSeed="ABC-DEF-GHI";
        record.gameID="seed-reuse-score-fixture";
        record.date="2026-11-11";
        record.version="v4.0.8";
        record.gameData=new Bundle();
        SeedReuseScore.Result result=SeedReuseScore.calculate(2000,2,10,10,true);
        assertEquals(3000,result.retentionBasisPoints);
        result.store(record.gameData);
        assertEquals(600,record.earnedScore());
        assertEquals(1400,SeedReuseScore.restore(record.gameData).deduction);
        Bundle archive=new Bundle();
        record.storeInBundle(archive);
        Rankings.Record loaded=new Rankings.Record();
        loaded.restoreFromBundle(archive);
        assertEquals(600,loaded.earnedScore());
        assertEquals(2000,loaded.score);
    }

    @Test public void oldAndUnknownHistoryDoNotInventPenalties() {
        Bundle old=new Bundle();
        assertEquals(932,SeedReuseScore.earnedScore(old,932));
        SeedReuseScore.Result unverified=SeedReuseScore.calculate(932,0,-1,3,false);
        assertEquals(932,unverified.earned);
        assertEquals(0,unverified.deduction);
        unverified.store(old);
        assertEquals(932,SeedReuseScore.earnedScore(old,932));
    }

    @Test public void damagedSnapshotCannotSilentlyChangeRankingScore() {
        Bundle data=new Bundle();
        SeedReuseScore.Result valid=SeedReuseScore.calculate(1934,1,5,5,true);
        valid.store(data);
        assertNotNull(SeedReuseScore.restore(data));
        assertEquals(valid.earned, SeedReuseScore.earnedScore(data,1934));
        data.put(SeedReuseScore.EARNED_KEY,valid.earned+1);
        assertNull("An earned-score mismatch invalidates the entire snapshot",
                SeedReuseScore.restore(data));
        assertEquals("Do not reprice a corrupt snapshot",1934,SeedReuseScore.earnedScore(data,1934));
        valid.store(data);
        data.put(SeedReuseScore.RETENTION_KEY,valid.retentionBasisPoints-1000);
        assertNull("A retention mismatch invalidates the snapshot",SeedReuseScore.restore(data));
        valid.store(data);
        assertEquals("Undamaged v1 snapshot remains byte-for-byte compatible",
                valid.earned,SeedReuseScore.earnedScore(data,1934));
    }

    @Test public void snapshotNeverRepricesWhenCurrentHistoryChanges() {
        Bundle saved=new Bundle();
        SeedReuseScore.calculate(10000,1,5,5,true).store(saved);
        assertEquals(6000,SeedReuseScore.earnedScore(saved,10000));
        assertEquals(6000,SeedReuseScore.earnedScore(saved,10000));
        assertEquals(9999,SeedReuseScore.earnedScore(saved,9999));
    }

    @Test public void sevenEarlierAttemptsFloorNineToTwentySixRecoverActualRankingPoints() {
        SeedReuseScore.Result result=SeedReuseScore.calculate(330393,7,9,26,true);
        assertEquals(1700,result.retentionBasisPoints);
        assertEquals(56166,result.earned);
        assertEquals(274227,result.deduction);
        Rankings.Record run=new Rankings.Record();
        run.score=330393;
        run.gameID="deep-progress";
        run.customSeed="SEED-EXAMPLE";
        run.gameData=new Bundle();
        result.store(run.gameData);
        assertEquals(56166,run.earnedScore());
        Rankings.Record lower=new Rankings.Record();
        lower.score=50000;
        lower.customSeed="SEED-EXAMPLE";
        lower.gameID="lower";
        assertTrue("Real ranking order uses earned points",Rankings.scoreComparator.compare(run,lower)<0);
    }

    @Test public void replayDeathsAndNewDepthProgression() {
        assertEquals(0,SeedReuseScore.calculate(330393,7,9,9,true).earned);
        SeedReuseScore.Result small=SeedReuseScore.calculate(330393,7,9,10,true);
        assertEquals(100,small.retentionBasisPoints);
        assertEquals(3303,small.earned);
        assertEquals(56166,SeedReuseScore.calculate(330393,7,9,26,true).earned);
        SeedReuseScore.Result early=SeedReuseScore.calculate(330393,1,5,5,true);
        SeedReuseScore.Result deep=SeedReuseScore.calculate(330393,1,25,25,true);
        assertTrue(early.earned>deep.earned);
        assertEquals(3303,SeedReuseScore.calculate(330393,7,25,26,true).earned);
    }

    @Test public void oldZeroReceiptRetroactivelyUpgradesAndPersistsWithoutRecounting() {
        Bundle old=new Bundle();
        SeedReuseScore.Result original=SeedReuseScore.calculate(330393,7,9,9,true);
        original.store(old);
        old.put(SeedReuseScore.VERSION_KEY,1);
        old.put(SeedReuseScore.CURRENT_DEPTH_KEY,26);
        assertEquals(56166,SeedReuseScore.earnedScore(old,330393));
        assertTrue(SeedReuseScore.migrate(old));
        assertEquals(SeedReuseScore.VERSION,old.getInt(SeedReuseScore.VERSION_KEY));
        assertEquals(56166,old.getLong(SeedReuseScore.EARNED_KEY));
        assertEquals(1700,old.getInt(SeedReuseScore.RETENTION_KEY));
        assertFalse(SeedReuseScore.migrate(old));
        assertEquals(56166,SeedReuseScore.earnedScore(old,330393));
        // The saved original record is still authoritative and must not be recomputed
        // from today's local seed history.
        assertEquals(56166,SeedReuseScore.earnedScore(old,330393));
        assertEquals(330392,SeedReuseScore.earnedScore(old,330392));
    }

    @Test public void forgedLegacyReceiptDoesNotMigrateOrOverwriteHistoricalScore() {
        Bundle forged=new Bundle();
        SeedReuseScore.calculate(330393,7,9,9,true).store(forged);
        forged.put(SeedReuseScore.VERSION_KEY,1);
        forged.put(SeedReuseScore.CURRENT_DEPTH_KEY,26);
        forged.put(SeedReuseScore.RETENTION_KEY,2500);
        assertNull(SeedReuseScore.restore(forged));
        assertFalse(SeedReuseScore.migrate(forged));
    }
}

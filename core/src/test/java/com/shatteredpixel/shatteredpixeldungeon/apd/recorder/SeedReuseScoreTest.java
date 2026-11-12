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

    @Test public void snapshotNeverRepricesWhenCurrentHistoryChanges() {
        Bundle saved=new Bundle();
        SeedReuseScore.calculate(10000,1,5,5,true).store(saved);
        assertEquals(6000,SeedReuseScore.earnedScore(saved,10000));
        assertEquals(6000,SeedReuseScore.earnedScore(saved,10000));
        assertEquals(9999,SeedReuseScore.earnedScore(saved,9999));
    }
}

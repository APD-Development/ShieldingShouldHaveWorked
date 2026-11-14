/* ADHD Pixel Dungeon (C) 2026 Chris Charlton, GPL-3.0-or-later */
package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.watabou.utils.Bundle;
import org.junit.Test;
import static org.junit.Assert.*;

public class SeedReuseScoreTest {
    private static final long BASE=330393;

    private static Bundle legacy(int version,long before,int attempts,int prior,int current,int bps,long earned) {
        Bundle data=new Bundle();
        data.put(SeedReuseScore.VERSION_KEY,version);
        data.put(SeedReuseScore.BASE_KEY,before);
        data.put(SeedReuseScore.EARNED_KEY,earned);
        data.put(SeedReuseScore.ATTEMPTS_KEY,attempts);
        data.put(SeedReuseScore.PRIOR_DEPTH_KEY,prior);
        data.put(SeedReuseScore.CURRENT_DEPTH_KEY,current);
        data.put(SeedReuseScore.RETENTION_KEY,bps);
        data.put(SeedReuseScore.KNOWN_KEY,true);
        return data;
    }

    @Test public void sevenAttemptsFloorNineToTwentySixConservativeFallbackIs216026() {
        SeedReuseScore.Result r=SeedReuseScore.calculate(BASE,7,9,26,true);
        assertEquals(6538,r.retentionBasisPoints);
        assertEquals(216026,r.earned);
        assertEquals(114367,r.deduction);
        assertFalse(r.individualDepthsKnown);
    }

    @Test public void verifiedSixFloorFourDeathsAndOneFloorNineAward263679() {
        int[] depths={4,4,4,4,4,4,9};
        SeedReuseScore.Result r=SeedReuseScore.calculate(BASE,7,9,26,true,depths);
        assertEquals(7981,r.retentionBasisPoints);
        assertEquals(263679,r.earned);
        assertEquals(66714,r.deduction);
        assertTrue(r.individualDepthsKnown);
        assertEquals(7,r.individualDepths.length);
        Bundle saved=new Bundle();
        r.store(saved);
        assertEquals(263679,SeedReuseScore.earnedScore(saved,BASE));
        assertArrayEquals(depths,saved.getIntArray(SeedReuseScore.DEPTHS_KEY));
    }

    @Test public void actualEarnedRankingScoreAndBundleRoundTripAgree() {
        Rankings.Record record=new Rankings.Record();
        record.score=(int)BASE;
        record.heroClass=HeroClass.WARRIOR;
        record.gameID="seed-review";
        record.customSeed="ABC-DEF-GHI";
        record.date="2026-11-14";
        record.version="v4.0.8";
        record.gameData=new Bundle();
        SeedReuseScore.calculate(BASE,7,9,26,true).store(record.gameData);
        assertEquals(216026,record.earnedScore());
        Rankings.Record lower=new Rankings.Record();
        lower.score=200000;
        lower.gameID="lower";
        lower.customSeed=record.customSeed;
        assertTrue(Rankings.scoreComparator.compare(record,lower)<0);
        Bundle archived=new Bundle();
        record.storeInBundle(archived);
        Rankings.Record loaded=new Rankings.Record();
        loaded.restoreFromBundle(archived);
        assertEquals(216026,loaded.earnedScore());
        assertEquals((int)BASE,loaded.score);
    }

    @Test public void sameDepthRepeatedRunsApplyOriginalReplayTiersPerFloor() {
        for (int attempts=0;attempts<=6;attempts++) {
            int[] depths=new int[attempts];
            java.util.Arrays.fill(depths,26);
            long expected=attempts==0 ? 10000 : Math.max(0,10000-attempts*2500);
            SeedReuseScore.Result result=SeedReuseScore.calculate(10000,attempts,
                    attempts==0?-1:26,26,true,depths);
            assertEquals(expected,result.retentionBasisPoints);
            assertEquals(expected,result.earned);
        }
        assertEquals(247795,SeedReuseScore.calculate(BASE,1,26,26,true,new int[]{26}).earned);
        assertEquals(165197,SeedReuseScore.calculate(BASE,2,26,26,true,new int[]{26,26}).earned);
    }

    @Test public void twentyOneEarlyDeathsCannotEraseUnexploredFloors() {
        int[] depths=new int[21];
        java.util.Arrays.fill(depths,1);
        SeedReuseScore.Result r=SeedReuseScore.calculate(BASE,21,1,26,true,depths);
        assertEquals(9615,r.retentionBasisPoints);
        assertEquals(317686,r.earned);
        assertEquals(0,SeedReuseScore.calculate(BASE,21,1,1,true,depths).earned);
        assertEquals(317686,SeedReuseScore.calculate(BASE,21,1,26,true).earned);
    }

    @Test public void deepeningCannotDecreaseRetentionAndExtraAttemptsCannotRaiseIt() {
        for (int n=0;n<=10;n++) {
            int[] previous=new int[n];
            java.util.Arrays.fill(previous,4);
            int max=n==0?-1:4;
            int earlier=-1;
            for (int current=1;current<=26;current++) {
                SeedReuseScore.Result result=SeedReuseScore.calculate(50003,n,max,current,true,previous);
                if (earlier>=0) assertTrue(result.retentionBasisPoints>=earlier);
                earlier=result.retentionBasisPoints;
                assertTrue(result.earned>=0 && result.earned<=50003);
                if (n>0) {
                    int[] more=new int[n+1];
                    java.util.Arrays.fill(more,4);
                    assertTrue(SeedReuseScore.calculate(50003,n+1,4,current,true,more).earned<=result.earned);
                }
            }
        }
    }

    @Test public void summaryFallbackNeverExceedsKnownFullDepthCredit() {
        for (int n=1;n<=12;n++) {
            int[] previous=new int[n];
            java.util.Arrays.fill(previous,4);
            previous[n-1]=9;
            for (int current=1;current<=26;current++) {
                long full=SeedReuseScore.calculate(BASE,n,9,current,true,previous).earned;
                long fallback=SeedReuseScore.calculate(BASE,n,9,current,true).earned;
                assertTrue("Conservative summary must not over-reward",fallback<=full);
            }
        }
    }

    @Test public void migrateV1ZeroScoreToConservativeFloorWeightedResult() {
        Bundle old=legacy(1,BASE,7,9,26,0,0);
        assertEquals(216026,SeedReuseScore.earnedScore(old,BASE));
        assertTrue(SeedReuseScore.migrate(old));
        assertEquals(4,old.getInt(SeedReuseScore.VERSION_KEY));
        assertEquals(216026,old.getLong(SeedReuseScore.EARNED_KEY));
        assertFalse(old.getBoolean(SeedReuseScore.EXACT_KEY));
        assertFalse(SeedReuseScore.migrate(old));
        assertEquals(216026,SeedReuseScore.earnedScore(old,BASE));
        assertEquals(BASE-1,SeedReuseScore.earnedScore(old,BASE-1));
    }

    @Test public void migrateV2UnauthorizedFloorCreditAndV3CubicWithoutFabricatingDepths() {
        Bundle v2=legacy(2,BASE,7,9,26,1700,56166);
        Bundle v3=legacy(3,BASE,7,9,26,6650,219713);
        for (Bundle old:new Bundle[]{v2,v3}) {
            assertEquals(216026,SeedReuseScore.earnedScore(old,BASE));
            assertTrue(SeedReuseScore.migrate(old));
            assertEquals(216026,old.getLong(SeedReuseScore.EARNED_KEY));
            assertEquals(4,old.getInt(SeedReuseScore.VERSION_KEY));
            assertFalse(old.getBoolean(SeedReuseScore.EXACT_KEY));
            assertFalse(SeedReuseScore.migrate(old));
            assertEquals(216026,SeedReuseScore.earnedScore(old,BASE));
        }
    }

    @Test public void corruptOldAndExactDepthSnapshotsAreRejectedWithoutGuessing() {
        Bundle forged=legacy(3,BASE,7,9,26,6650,219714);
        assertNull(SeedReuseScore.restore(forged));
        assertFalse(SeedReuseScore.migrate(forged));
        assertEquals(BASE,SeedReuseScore.earnedScore(forged,BASE));
        Bundle valid=new Bundle();
        SeedReuseScore.calculate(BASE,7,9,26,true,new int[]{4,4,4,4,4,4,9}).store(valid);
        valid.put(SeedReuseScore.DEPTHS_KEY,new int[]{9,9,9,9,9,9,9});
        assertNull(SeedReuseScore.restore(valid));
        assertEquals(BASE,SeedReuseScore.earnedScore(valid,BASE));
    }

    @Test public void absentUnknownAndFirstUseHistoryAreNeverPenalized() {
        Bundle empty=new Bundle();
        assertEquals(BASE,SeedReuseScore.earnedScore(empty,BASE));
        assertFalse(SeedReuseScore.migrate(empty));
        assertEquals(BASE,SeedReuseScore.calculate(BASE,7,-1,26,false).earned);
        assertEquals(BASE,SeedReuseScore.calculate(BASE,0,-1,26,true,new int[0]).earned);
        assertEquals(0,SeedReuseScore.calculate(0,7,9,26,true).earned);
        assertEquals(BASE,SeedReuseScore.calculate(BASE,7,-1,26,true).earned);
    }

    @Test public void noFabricatedIndividualDepthsAndScoreMathIsIdempotent() {
        try {
            SeedReuseScore.calculate(BASE,7,9,26,true,new int[]{9});
            fail("Missing attempts cannot masquerade as a full prior-depth list");
        } catch (IllegalArgumentException expected) {}
        Bundle frozen=new Bundle();
        SeedReuseScore.calculate(BASE,7,9,26,true).store(frozen);
        assertEquals(216026,SeedReuseScore.earnedScore(frozen,BASE));
        assertEquals(216026,SeedReuseScore.earnedScore(frozen,BASE));
        assertFalse(SeedReuseScore.migrate(frozen));
    }
}

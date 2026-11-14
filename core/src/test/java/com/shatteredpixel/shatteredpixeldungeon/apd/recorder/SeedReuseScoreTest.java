/* ADHD Pixel Dungeon (C) 2026 Chris Charlton, GPL-3.0-or-later */
package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.watabou.utils.Bundle;
import org.junit.Test;
import static org.junit.Assert.*;

public class SeedReuseScoreTest {
    private static final long BASE=330393;

    private static Bundle legacy(int version,long score,int attempts,int prior,int current,
                                 int bps,long earned) {
        Bundle b=new Bundle();
        b.put(SeedReuseScore.VERSION_KEY,version);
        b.put(SeedReuseScore.BASE_KEY,score);
        b.put(SeedReuseScore.EARNED_KEY,earned);
        b.put(SeedReuseScore.ATTEMPTS_KEY,attempts);
        b.put(SeedReuseScore.PRIOR_DEPTH_KEY,prior);
        b.put(SeedReuseScore.CURRENT_DEPTH_KEY,current);
        b.put(SeedReuseScore.RETENTION_KEY,bps);
        b.put(SeedReuseScore.KNOWN_KEY,true);
        return b;
    }

    @Test public void exactExponentialEquationSevenPreviousNineToTwentySix() {
        SeedReuseScore.Result r=SeedReuseScore.calculate(BASE,7,9,26,true);
        assertEquals(6643,r.retentionBasisPoints);
        assertEquals(219480,r.earned);
        assertEquals(110913,r.deduction);
        assertEquals(BASE,r.before);
        assertFalse(r.individualDepthsKnown);
    }

    @Test public void individualHistorySixFloorFourDeathsOneFloorNine() {
        int[] depths={4,4,4,4,4,4,9};
        SeedReuseScore.Result r=SeedReuseScore.calculate(BASE,7,9,26,true,depths);
        assertEquals(7751,r.retentionBasisPoints);
        assertEquals(256098,r.earned);
        assertEquals(74295,r.deduction);
        assertTrue(r.individualDepthsKnown);
        Bundle b=new Bundle();
        r.store(b);
        assertArrayEquals(depths,b.getIntArray(SeedReuseScore.DEPTHS_KEY));
        assertEquals(256098,SeedReuseScore.earnedScore(b,BASE));
    }

    @Test public void allFloorsAlreadySeenUseSmoothExponentialNotHardZero() {
        for(int attempts=0;attempts<=8;attempts++) {
            int[] depths=new int[attempts];
            java.util.Arrays.fill(depths,26);
            SeedReuseScore.Result r=SeedReuseScore.calculate(BASE,attempts,
                    attempts==0?-1:26,26,true,depths);
            long expected=new java.math.BigDecimal(Double.toString(Math.exp(-0.5d*attempts)))
                    .multiply(java.math.BigDecimal.valueOf(BASE))
                    .setScale(0,java.math.RoundingMode.HALF_UP).longValueExact();
            assertEquals(expected,r.earned);
        }
        assertEquals(200393,SeedReuseScore.calculate(BASE,1,26,26,true,new int[]{26}).earned);
        assertEquals(121545,SeedReuseScore.calculate(BASE,2,26,26,true,new int[]{26,26}).earned);
        assertEquals(44714,SeedReuseScore.calculate(BASE,4,26,26,true,
                new int[]{26,26,26,26}).earned);
    }

    @Test public void twentyOneFloorOneDeathsPreserveUnseenDepth() {
        int[] depths=new int[21];
        java.util.Arrays.fill(depths,1);
        SeedReuseScore.Result r=SeedReuseScore.calculate(BASE,21,1,26,true,depths);
        assertEquals(9615,r.retentionBasisPoints);
        assertEquals(317686,r.earned);
        assertEquals(317686,SeedReuseScore.calculate(BASE,21,1,26,true).earned);
        assertTrue(SeedReuseScore.calculate(BASE,21,1,1,true,depths).earned>0);
    }

    @Test public void moreActualDepthCannotDecreaseRetentionAndMoreReplaysCannotIncreaseIt() {
        for(int n=1;n<=12;n++){
            int[] depths=new int[n],more=new int[n+1];
            java.util.Arrays.fill(depths,4);
            java.util.Arrays.fill(more,4);
            int last=-1;
            for(int current=1;current<=26;current++){
                SeedReuseScore.Result r=SeedReuseScore.calculate(BASE,n,4,current,true,depths);
                if(last>=0) assertTrue(r.retentionBasisPoints>=last);
                last=r.retentionBasisPoints;
                assertTrue(SeedReuseScore.calculate(BASE,n+1,4,current,true,more).earned<=r.earned);
            }
        }
    }

    @Test public void summaryFallbackIsConservativeAndDoesNotInventEarlierDepths() {
        for(int n=1;n<=12;n++){
            int[] d=new int[n]; java.util.Arrays.fill(d,4);d[n-1]=9;
            for(int current=1;current<=26;current++){
                long full=SeedReuseScore.calculate(BASE,n,9,current,true,d).earned;
                long summary=SeedReuseScore.calculate(BASE,n,9,current,true).earned;
                assertTrue(summary<=full);
            }
        }
        try {
            SeedReuseScore.calculate(BASE,7,9,26,true,new int[]{9});
            fail("Partial history cannot be treated as complete");
        } catch(IllegalArgumentException expected){}
    }

    @Test public void realRankingsUseCorrectFinalEarnedScoreAfterSaveReload() {
        Rankings.Record record=new Rankings.Record();
        record.score=(int)BASE;
        record.heroClass=HeroClass.WARRIOR;
        record.gameID="exponential";
        record.customSeed="ABC-DEF-GHI";
        record.date="2026-11-14";
        record.version="v4.0.8";
        record.gameData=new Bundle();
        SeedReuseScore.calculate(BASE,7,9,26,true).store(record.gameData);
        assertEquals(219480,record.earnedScore());
        Rankings.Record low=new Rankings.Record();
        low.gameID="low";low.customSeed=record.customSeed;low.score=200000;
        assertTrue(Rankings.scoreComparator.compare(record,low)<0);
        Bundle save=new Bundle();record.storeInBundle(save);
        Rankings.Record loaded=new Rankings.Record();loaded.restoreFromBundle(save);
        assertEquals(219480,loaded.earnedScore());
        assertEquals((int)BASE,loaded.score);
    }

    @Test public void authenticV1V2V3MigrateToExponentialConservativeFallback() {
        Bundle v1=legacy(1,BASE,7,9,26,0,0);
        Bundle v2=legacy(2,BASE,7,9,26,1700,56166);
        Bundle v3=legacy(3,BASE,7,9,26,6650,219713);
        for(Bundle saved:new Bundle[]{v1,v2,v3}){
            assertEquals(219480,SeedReuseScore.earnedScore(saved,BASE));
            assertTrue(SeedReuseScore.migrate(saved));
            assertEquals(5,saved.getInt(SeedReuseScore.VERSION_KEY));
            assertEquals(219480,saved.getLong(SeedReuseScore.EARNED_KEY));
            assertFalse(saved.getBoolean(SeedReuseScore.EXACT_KEY));
            assertFalse(SeedReuseScore.migrate(saved));
            assertEquals(219480,SeedReuseScore.earnedScore(saved,BASE));
        }
    }

    @Test public void authenticV4WithExactDepthsMigratesWithoutDiscardingHistory() {
        Bundle old=legacy(4,BASE,7,9,26,7981,263679);
        old.put(SeedReuseScore.EXACT_KEY,true);
        old.put(SeedReuseScore.DEPTHS_KEY,new int[]{4,4,4,4,4,4,9});
        assertEquals(256098,SeedReuseScore.earnedScore(old,BASE));
        assertTrue(SeedReuseScore.migrate(old));
        assertEquals(5,old.getInt(SeedReuseScore.VERSION_KEY));
        assertArrayEquals(new int[]{4,4,4,4,4,4,9},old.getIntArray(SeedReuseScore.DEPTHS_KEY));
        assertTrue(old.getBoolean(SeedReuseScore.EXACT_KEY));
        assertEquals(256098,old.getLong(SeedReuseScore.EARNED_KEY));
        assertFalse(SeedReuseScore.migrate(old));
    }

    @Test public void authenticV4SummaryOnlyMigratesConservatively() {
        Bundle old=legacy(4,BASE,7,9,26,6538,216026);
        old.put(SeedReuseScore.EXACT_KEY,false);
        assertEquals(219480,SeedReuseScore.earnedScore(old,BASE));
        assertTrue(SeedReuseScore.migrate(old));
        assertEquals(219480,old.getLong(SeedReuseScore.EARNED_KEY));
        assertFalse(old.getBoolean(SeedReuseScore.EXACT_KEY));
    }

    @Test public void corruptedSnapshotsRejectedRatherThanFabricatingRunHistory() {
        Bundle v4=legacy(4,BASE,7,9,26,7981,263680);
        v4.put(SeedReuseScore.EXACT_KEY,true);
        v4.put(SeedReuseScore.DEPTHS_KEY,new int[]{4,4,4,4,4,4,9});
        assertNull(SeedReuseScore.restore(v4));
        assertFalse(SeedReuseScore.migrate(v4));
        Bundle valid=new Bundle();
        SeedReuseScore.calculate(BASE,7,9,26,true,new int[]{4,4,4,4,4,4,9}).store(valid);
        valid.put(SeedReuseScore.DEPTHS_KEY,new int[]{9,9,9,9,9,9,9});
        assertNull(SeedReuseScore.restore(valid));
        assertEquals(BASE,SeedReuseScore.earnedScore(valid,BASE));
    }

    @Test public void unknownSeedAndMissingOldReceiptsRemainNeutral() {
        Bundle empty=new Bundle();
        assertEquals(BASE,SeedReuseScore.earnedScore(empty,BASE));
        assertFalse(SeedReuseScore.migrate(empty));
        assertEquals(BASE,SeedReuseScore.calculate(BASE,7,-1,26,false).earned);
        assertEquals(BASE,SeedReuseScore.calculate(BASE,0,-1,26,true,new int[0]).earned);
        assertEquals(BASE,SeedReuseScore.calculate(BASE,7,-1,26,true).earned);
        assertEquals(0,SeedReuseScore.calculate(0,7,9,26,true).earned);
    }
}

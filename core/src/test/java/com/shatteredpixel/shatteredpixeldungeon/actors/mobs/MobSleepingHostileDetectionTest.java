package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.IdentityHashMap;

import static org.junit.Assert.*;

/** A sleeping mob must assess the most detectable eligible hostile. */
public class MobSleepingHostileDetectionTest {

    private static class WatchRat extends Rat {
        final IdentityHashMap<Char, Float> chances = new IdentityHashMap<>();

        WatchRat() {
            pos = 11;
            fieldOfView = new boolean[64];
        }

        Char select(Char... candidates) {
            Sleeping state = new Sleeping() {
                @Override
                protected float detectionChance(Char candidate) {
                    return chances.get(candidate);
                }
            };
            return state.mostDetectableHostile(Arrays.asList(candidates));
        }

        Rat hostile(int position, float chance) {
            Rat candidate = new Rat();
            candidate.pos = position;
            candidate.alignment = Char.Alignment.ALLY;
            fieldOfView[position] = true;
            chances.put(candidate, chance);
            return candidate;
        }
    }

    private WatchRat watcher;

    @Before
    public void setUp() {
        HeadlessTestSupport.start();
        watcher = new WatchRat();
    }

    @Test
    public void highestDetectionChanceWinsRegardlessOfEncounterOrder() {
        Rat low = watcher.hostile(12, 0.2f);
        Rat high = watcher.hostile(13, 0.8f);

        assertSame(high, watcher.select(low, high));
        assertSame(high, watcher.select(high, low));
    }

    @Test
    public void equalDetectionChanceKeepsExistingFirstSeenTieBehavior() {
        Rat first = watcher.hostile(12, 0.5f);
        Rat second = watcher.hostile(13, 0.5f);

        assertSame(first, watcher.select(first, second));
        assertSame(second, watcher.select(second, first));
    }

    @Test
    public void zeroDetectionChanceStillSelectsAnEligibleHostile() {
        Rat undetectable = watcher.hostile(12, 0f);

        // Preserve the existing single detection roll in Sleeping.act when a
        // visible hostile has zero probability, rather than silently skipping it.
        assertSame(undetectable, watcher.select(undetectable));
    }

    @Test
    public void invisibleUnseenFriendlyNeutralAndSuppressedTargetsRemainExcluded() {
        Rat invisible = watcher.hostile(12, 0.99f);
        invisible.invisible = 1;
        Rat unseen = watcher.hostile(13, 0.98f);
        watcher.fieldOfView[unseen.pos] = false;
        Rat friendly = watcher.hostile(14, 0.97f);
        friendly.alignment = watcher.alignment;
        Rat neutral = watcher.hostile(15, 0.96f);
        neutral.alignment = Char.Alignment.NEUTRAL;
        Rat suppressed = watcher.hostile(16, Float.POSITIVE_INFINITY);
        Rat valid = watcher.hostile(17, 0.25f);

        assertSame(valid, watcher.select(invisible, unseen, friendly, neutral, suppressed, valid));
    }
}

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.watabou.utils.Bundle;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/** Preserves generated vault patrols while tolerating malformed restored waypoint data. */
public class MobVaultPatrolWaypointSafetyTest {

    private static class PatrolRat extends Rat {
        PatrolRat() {
            activateSteathGameplayBehaviour();
            pos = 11;
        }

        int nextWaypoint() {
            return ((Wandering) WANDERING).randomDestination();
        }
    }

    @Before
    public void setUp() {
        HeadlessTestSupport.start();
    }

    @Test
    public void validWaypointIndexKeepsExistingPatrolRoute() {
        PatrolRat rat = new PatrolRat();
        rat.setupStealthGameplayWanderPositions(new int[]{12, 13}, 1);
        assertEquals(13, rat.nextWaypoint());
    }

    @Test
    public void negativeWaypointIndexFallsBackToFirstWaypoint() {
        PatrolRat rat = new PatrolRat();
        rat.setupStealthGameplayWanderPositions(new int[]{12, 13}, -1);
        assertEquals(12, rat.nextWaypoint());
    }

    @Test
    public void tooLargeWaypointIndexFallsBackToFirstWaypoint() {
        PatrolRat rat = new PatrolRat();
        rat.setupStealthGameplayWanderPositions(new int[]{12, 13}, Integer.MAX_VALUE);
        assertEquals(12, rat.nextWaypoint());
    }

    @Test
    public void emptyWaypointArrayUsesExistingStationaryFallback() {
        PatrolRat rat = new PatrolRat();
        rat.setupStealthGameplayWanderPositions(new int[0], 7);
        Bundle saved = new Bundle();
        rat.storeInBundle(saved);
        assertFalse("Empty patrol routes must not be serialized as valid waypoints",
                saved.contains("wander_positions"));
    }

    @Test
    public void restoredInvalidIndexCannotCrashNextPatrolWaypoint() {
        PatrolRat original = new PatrolRat();
        original.setupStealthGameplayWanderPositions(new int[]{12, 13}, 1);
        Bundle saved = new Bundle();
        original.storeInBundle(saved);
        saved.put("wander_pos_idx", Integer.MAX_VALUE);

        PatrolRat reloaded = new PatrolRat();
        reloaded.restoreFromBundle(saved);
        reloaded.pos = 11;
        assertEquals(12, reloaded.nextWaypoint());
    }

    @Test
    public void restoredValidWaypointIndexIsPreserved() {
        PatrolRat original = new PatrolRat();
        original.setupStealthGameplayWanderPositions(new int[]{12, 13}, 1);
        Bundle saved = new Bundle();
        original.storeInBundle(saved);

        PatrolRat reloaded = new PatrolRat();
        reloaded.restoreFromBundle(saved);
        reloaded.pos = 11;
        assertEquals(13, reloaded.nextWaypoint());
    }
}

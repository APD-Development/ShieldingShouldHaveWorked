package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * A hunting retry must use the replacement target's sight state, never the
 * obsolete enemyInFOV flag passed for the target it could not approach.
 */
public class MobUnreachableRetargetVisibilityTest {

    private static class RetargetingRat extends Rat {
        Char replacement;
        boolean retried;
        boolean visibilityAtRetry;

        @Override
        protected Char chooseEnemy() {
            return replacement;
        }

        boolean retryAfterUnreachable(Char previous, boolean previousInFOV) {
            enemy = previous;
            Hunting recorder = new Hunting() {
                @Override
                public boolean act(boolean enemyInFOV, boolean justAlerted) {
                    retried = true;
                    visibilityAtRetry = enemyInFOV;
                    return true;
                }
            };
            return recorder.handleUnreachableTarget(previousInFOV, false);
        }

        Char currentTarget() {
            return enemy;
        }

        boolean throwOnNextRetry;
        int persistentRetryCalls;
        final Hunting persistentRetryState = new Hunting() {
            @Override
            public boolean act(boolean enemyInFOV, boolean justAlerted) {
                persistentRetryCalls++;
                if (throwOnNextRetry) {
                    throwOnNextRetry = false;
                    throw new IllegalStateException("injected hunting retry failure");
                }
                return true;
            }
        };

        boolean retryUsingPersistentState(Char previous) {
            enemy = previous;
            return persistentRetryState.handleUnreachableTarget(true, false);
        }
    }

    private RetargetingRat hunter;

    @Before
    public void setUp() {
        HeadlessTestSupport.start();
        hunter = new RetargetingRat();
        hunter.pos = 11;
        hunter.fieldOfView = new boolean[25];
    }

    private Rat ratAt(int position) {
        Rat rat = new Rat();
        rat.pos = position;
        return rat;
    }

    @Test
    public void newlyVisibleTargetIsNotTreatedAsPreviouslyUnseenEnemy() {
        Rat previous = ratAt(12);
        Rat replacement = ratAt(13);
        hunter.replacement = replacement;
        hunter.fieldOfView[replacement.pos] = true;

        assertTrue(hunter.retryAfterUnreachable(previous, false));
        assertTrue(hunter.retried);
        assertTrue(hunter.visibilityAtRetry);
        assertSame(replacement, hunter.currentTarget());
    }

    @Test
    public void replacementOutsideSightDoesNotInheritPriorTargetVisibility() {
        Rat previous = ratAt(12);
        Rat replacement = ratAt(13);
        hunter.replacement = replacement;
        hunter.fieldOfView[replacement.pos] = false;

        assertTrue(hunter.retryAfterUnreachable(previous, true));
        assertTrue(hunter.retried);
        assertFalse(hunter.visibilityAtRetry);
        assertSame(replacement, hunter.currentTarget());
    }

    @Test
    public void invisibleReplacementDoesNotInheritPriorTargetVisibility() {
        Rat previous = ratAt(12);
        Rat replacement = ratAt(13);
        replacement.invisible = 1;
        hunter.replacement = replacement;
        hunter.fieldOfView[replacement.pos] = true;

        assertTrue(hunter.retryAfterUnreachable(previous, true));
        assertTrue(hunter.retried);
        assertFalse(hunter.visibilityAtRetry);
        assertSame(replacement, hunter.currentTarget());
    }

    @Test
    public void deadReplacementDoesNotInheritPriorTargetVisibility() {
        Rat previous = ratAt(12);
        Rat replacement = ratAt(13);
        replacement.HP = 0;
        hunter.replacement = replacement;
        hunter.fieldOfView[replacement.pos] = true;

        assertTrue(hunter.retryAfterUnreachable(previous, true));
        assertTrue(hunter.retried);
        assertFalse(hunter.visibilityAtRetry);
        assertSame(replacement, hunter.currentTarget());
    }
    @Test
    public void retryGuardRecoversAfterActThrowsAndAllowsNextRetry() {
        Rat previous = ratAt(12);
        Rat replacement = ratAt(13);
        hunter.replacement = replacement;
        hunter.fieldOfView[replacement.pos] = true;
        hunter.throwOnNextRetry = true;

        try {
            hunter.retryUsingPersistentState(previous);
            fail("Expected injected retry failure");
        } catch (IllegalStateException expected) {
            assertEquals("injected hunting retry failure", expected.getMessage());
        }

        assertEquals(1, hunter.persistentRetryCalls);
        assertTrue(hunter.retryUsingPersistentState(previous));
        assertEquals("The same hunting state must permit a new retry", 2, hunter.persistentRetryCalls);
        assertSame(replacement, hunter.currentTarget());
    }

}

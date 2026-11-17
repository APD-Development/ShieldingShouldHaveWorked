package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tengu has its own Hunting retry, separate from Mob.Hunting. Retargeting
 * must use the new target's visibility rather than the old target's flag.
 */
public class TenguUnreachableRetargetVisibilityTest {

    private static class RetargetingTengu extends Tengu {
        Char replacement;
        Char attackedTarget;
        int attacks;

        @Override
        protected Char chooseEnemy() {
            return replacement;
        }

        @Override
        protected boolean canAttack(Char target) {
            // An unreachable prior target must provoke the retargeting path.
            return target == replacement;
        }

        @Override
        public boolean canUseAbility() {
            // Exercise targeting without ability cooldowns or audiovisual effects.
            return false;
        }

        @Override
        protected boolean doAttack(Char target) {
            attackedTarget = target;
            attacks++;
            return true;
        }

        boolean huntAfterUnreachable(Char previous, boolean previousVisible) {
            enemy = previous;
            return HUNTING.act(previousVisible, false);
        }

        Char currentTarget() {
            return enemy;
        }

        boolean targetSeen() {
            return enemySeen;
        }
    }

    private RetargetingTengu tengu;

    @Before
    public void setUp() {
        HeadlessTestSupport.start();
        tengu = new RetargetingTengu();
        tengu.pos = 11;
        tengu.fieldOfView = new boolean[25];
    }

    private Rat ratAt(int pos) {
        Rat rat = new Rat();
        rat.pos = pos;
        return rat;
    }

    @Test
    public void visibleReplacementIsAttackableAfterUnseenOldTarget() {
        Rat oldTarget = ratAt(12);
        Rat replacement = ratAt(13);
        tengu.replacement = replacement;
        tengu.fieldOfView[replacement.pos] = true;

        assertTrue(tengu.huntAfterUnreachable(oldTarget, false));
        assertSame(replacement, tengu.currentTarget());
        assertTrue(tengu.targetSeen());
        assertSame(replacement, tengu.attackedTarget);
        assertEquals(1, tengu.attacks);
    }

    @Test
    public void unseenReplacementDoesNotInheritOldTargetVisibility() {
        Rat oldTarget = ratAt(12);
        Rat replacement = ratAt(13);
        tengu.replacement = replacement;

        assertTrue(tengu.huntAfterUnreachable(oldTarget, true));
        assertSame(replacement, tengu.currentTarget());
        assertFalse(tengu.targetSeen());
        assertNull(tengu.attackedTarget);
        assertEquals(0, tengu.attacks);
    }

    @Test
    public void invisibleReplacementCannotAttackUsingOldTargetVisibility() {
        Rat oldTarget = ratAt(12);
        Rat replacement = ratAt(13);
        replacement.invisible = 1;
        tengu.replacement = replacement;
        tengu.fieldOfView[replacement.pos] = true;

        assertTrue(tengu.huntAfterUnreachable(oldTarget, true));
        assertSame(replacement, tengu.currentTarget());
        assertFalse(tengu.targetSeen());
        assertNull(tengu.attackedTarget);
        assertEquals(0, tengu.attacks);
    }

    @Test
    public void deadReplacementCannotAttackUsingOldTargetVisibility() {
        Rat oldTarget = ratAt(12);
        Rat replacement = ratAt(13);
        replacement.HP = 0;
        tengu.replacement = replacement;
        tengu.fieldOfView[replacement.pos] = true;

        assertTrue(tengu.huntAfterUnreachable(oldTarget, true));
        assertSame(replacement, tengu.currentTarget());
        assertFalse(tengu.targetSeen());
        assertNull(tengu.attackedTarget);
        assertEquals(0, tengu.attacks);
    }

    private static class CyclingTengu extends Tengu {
        Char first;
        Char second;
        int selectionCalls;

        @Override
        protected Char chooseEnemy() {
            selectionCalls++;
            if (selectionCalls > 2) {
                throw new AssertionError("Tengu repeatedly retried unreachable targets");
            }
            return selectionCalls == 1 ? second : first;
        }

        @Override
        protected boolean canAttack(Char target) {
            return false;
        }

        @Override
        public boolean canUseAbility() {
            return false;
        }

        boolean hunt(Char previous) {
            enemy = previous;
            return HUNTING.act(false, false);
        }

        Char currentTarget() {
            return enemy;
        }
    }

    @Test
    public void alternatingUnreachableTargetsOnlyRetryOnce() {
        CyclingTengu cycling = new CyclingTengu();
        cycling.pos = 11;
        cycling.fieldOfView = new boolean[25];
        cycling.first = ratAt(12);
        cycling.second = ratAt(13);

        assertTrue(cycling.hunt(cycling.first));
        assertEquals(1, cycling.selectionCalls);
        assertSame(cycling.second, cycling.currentTarget());
    }
}

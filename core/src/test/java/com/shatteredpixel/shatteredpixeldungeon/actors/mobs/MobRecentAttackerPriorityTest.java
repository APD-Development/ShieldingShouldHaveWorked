package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/** Several recent attackers must not overwrite a better target just by appearing later. */
public class MobRecentAttackerPriorityTest {

    private static class EmptyLevel extends Level {
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
    }

    private static class Pursuer extends Rat {
        final Set<Char> attackable = new HashSet<>();

        @Override
        protected boolean canAttack(Char target) {
            return attackable.contains(target);
        }

        void currentTarget(Char target) {
            enemy = target;
            this.target = target == null ? -1 : target.pos;
        }

        boolean consider(Char... attackers) {
            recentlyAttackedBy.addAll(Arrays.asList(attackers));
            return ((Hunting) HUNTING).handleRecentAttackers();
        }

        Char selected() {
            return enemy;
        }

        boolean pendingCleared() {
            return recentlyAttackedBy.isEmpty();
        }
    }

    private Hero previousHero;
    private Level previousLevel;
    private Hero hero;
    private Pursuer pursuer;

    @Before
    public void setUp() {
        HeadlessTestSupport.start();
        previousHero = Dungeon.hero;
        previousLevel = Dungeon.level;
        Actor.clear();

        EmptyLevel level = new EmptyLevel();
        level.setSize(10, 10);
        level.mobs = new HashSet<>();
        Arrays.fill(level.map, Terrain.EMPTY);
        Arrays.fill(level.passable, true);

        hero = new Hero();
        hero.heroClass = HeroClass.WARRIOR;
        hero.alignment = Char.Alignment.ALLY;
        hero.pos = 85;
        hero.HP = hero.HT = 20;
        Dungeon.level = level;
        Dungeon.hero = hero;
        Actor.add(hero);

        pursuer = new Pursuer();
        pursuer.pos = 55;
        pursuer.fieldOfView = new boolean[level.length()];
        pursuer.currentTarget(hero);
    }

    @After
    public void tearDown() {
        Actor.clear();
        Dungeon.hero = previousHero;
        Dungeon.level = previousLevel;
    }

    private Rat attacker(int position, boolean attackable) {
        Rat candidate = new Rat();
        candidate.pos = position;
        candidate.alignment = Char.Alignment.ALLY;
        candidate.HP = candidate.HT;
        pursuer.fieldOfView[position] = true;
        Actor.add(candidate);
        if (attackable) pursuer.attackable.add(candidate);
        return candidate;
    }

    @Test
    public void attackableTargetBeatsLaterCloserNonattackableAttacker() {
        Rat farAttackable = attacker(22, true);
        Rat nearUnattackable = attacker(56, false);
        assertTrue(pursuer.consider(farAttackable, nearUnattackable));
        assertSame(farAttackable, pursuer.selected());
        assertEquals(farAttackable.pos, pursuer.target);
        assertTrue(pursuer.pendingCleared());
    }

    @Test
    public void closestOfTwoAttackableAttackersWinsInEitherOrder() {
        Rat near = attacker(54, true);
        Rat far = attacker(22, true);
        assertTrue(pursuer.consider(near, far));
        assertSame(near, pursuer.selected());

        pursuer.currentTarget(hero);
        assertTrue(pursuer.consider(far, near));
        assertSame(near, pursuer.selected());
    }

    @Test
    public void closestEligibleNonattackableAttackerWins() {
        Rat farther = attacker(33, false);
        Rat nearer = attacker(56, false);
        assertTrue(pursuer.consider(farther, nearer));
        assertSame(nearer, pursuer.selected());
    }

    @Test
    public void nonattackableAttackerBeyondOriginalTargetDoesNotReplaceIt() {
        Rat farther = attacker(11, false);
        assertFalse(pursuer.consider(farther));
        assertSame(hero, pursuer.selected());
        assertTrue(pursuer.pendingCleared());
    }

    @Test
    public void equalDistanceKeepsFirstEligibleAttacker() {
        Rat first = attacker(54, true);
        Rat second = attacker(56, true);
        assertTrue(pursuer.consider(first, second));
        assertSame(first, pursuer.selected());
    }
}

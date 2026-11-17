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

import static org.junit.Assert.*;

/**
 * The zero-allocation roster guard for recent attackers must preserve the
 * original registered-character and sight restrictions.
 */
public class MobRecentAttackerRosterTest {

    private static class EmptyLevel extends Level {
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
    }

    private static class Pursuer extends Rat {
        @Override
        protected boolean canAttack(Char target) {
            return true;
        }

        boolean consider(Char attacker) {
            recentlyAttackedBy.add(attacker);
            return ((Hunting) HUNTING).handleRecentAttackers();
        }

        Char chosen() {
            return enemy;
        }

        boolean pendingListIsEmpty() {
            return recentlyAttackedBy.isEmpty();
        }
    }

    private Hero previousHero;
    private Level previousLevel;
    private Hero hero;
    private Pursuer mob;

    @Before
    public void setUp() {
        HeadlessTestSupport.start();
        previousHero = Dungeon.hero;
        previousLevel = Dungeon.level;
        Actor.clear();

        EmptyLevel level = new EmptyLevel();
        level.setSize(5, 5);
        level.mobs = new HashSet<>();
        Arrays.fill(level.map, Terrain.EMPTY);
        Arrays.fill(level.passable, true);

        hero = new Hero();
        hero.heroClass = HeroClass.WARRIOR;
        hero.alignment = Char.Alignment.ALLY;
        hero.pos = 12;
        hero.HP = hero.HT = 20;
        Dungeon.level = level;
        Dungeon.hero = hero;
        Actor.add(hero);

        mob = new Pursuer();
        mob.pos = 13;
        mob.fieldOfView = new boolean[level.length()];
        mob.fieldOfView[hero.pos] = true;
    }

    @After
    public void tearDown() {
        Actor.clear();
        Dungeon.hero = previousHero;
        Dungeon.level = previousLevel;
    }

    @Test
    public void registeredVisibleAttackerCanBeSelected() {
        assertTrue(Actor.containsChar(hero));
        assertTrue(mob.consider(hero));
        assertSame(hero, mob.chosen());
        assertTrue(mob.pendingListIsEmpty());
    }

    @Test
    public void removedButLivingAttackerCannotBeSelected() {
        Actor.remove(hero);
        assertTrue(hero.isActive());
        assertFalse(Actor.containsChar(hero));
        assertFalse(mob.consider(hero));
        assertNull(mob.chosen());
        assertTrue(mob.pendingListIsEmpty());
    }

    @Test
    public void registeredAttackerStillRequiresVisibility() {
        mob.fieldOfView[hero.pos] = false;
        assertTrue(Actor.containsChar(hero));
        assertFalse(mob.consider(hero));
        assertNull(mob.chosen());
        assertTrue(mob.pendingListIsEmpty());
    }
}

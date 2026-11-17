package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

/** The Mob target-presence check must match the prior copied-roster query. */
public class ActorMembershipSnapshotParityTest {
    private static final class Empty extends Level {
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
    }

    private Level previousLevel;
    private Hero previousHero;

    @Before public void setUp() {
        HeadlessTestSupport.start();
        previousLevel = Dungeon.level;
        previousHero = Dungeon.hero;
        Actor.clear();
        Empty level = new Empty();
        level.setSize(5, 5);
        level.mobs = new HashSet<>();
        Arrays.fill(level.map, Terrain.EMPTY);
        Arrays.fill(level.passable, true);
        Hero hero = new Hero();
        hero.heroClass = HeroClass.WARRIOR;
        hero.pos = 12;
        Dungeon.level = level;
        Dungeon.hero = hero;
        Actor.add(hero);
    }

    @After public void tearDown() {
        Actor.clear();
        Dungeon.level = previousLevel;
        Dungeon.hero = previousHero;
    }

    @Test public void lookupMatchesCopiedRosterAcrossRegistrationAndRemoval() {
        Rat rat = new Rat();
        rat.pos = 13;
        assertFalse(Actor.containsChar(rat));
        assertEquals(Actor.chars().contains(rat), Actor.containsChar(rat));

        Actor.add(rat);
        assertTrue(Actor.containsChar(rat));
        assertEquals(Actor.chars().contains(rat), Actor.containsChar(rat));

        Actor.remove(rat);
        assertFalse(Actor.containsChar(rat));
        assertEquals(Actor.chars().contains(rat), Actor.containsChar(rat));

        Actor.add(rat);
        assertTrue(Actor.containsChar(rat));
        assertEquals(Actor.chars().contains(rat), Actor.containsChar(rat));
    }

    @Test public void unregisteredQueriesDoNotAssignIdsOrModifyRoster() {
        Rat unregistered = new Rat();
        assertEquals(0, unregistered.apdPeekId());
        int before = Actor.chars().size();
        assertFalse(Actor.containsChar(unregistered));
        assertFalse(Actor.containsChar(null));
        assertEquals(0, unregistered.apdPeekId());
        assertEquals(before, Actor.chars().size());
        assertTrue(Actor.containsChar(Dungeon.hero));
    }
}

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

import static org.junit.Assert.assertEquals;

/** Regression for the -180/+180 directional seam in vault stealth patrol sight cones. */
public class MobStealthAngleSeamTest {

    private static final int WIDTH = 13;

    private static int at(int x, int y) {
        return y * WIDTH + x;
    }

    private static class EmptyLevel extends Level {
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
    }

    private static class Patroller extends Rat {
        void faceWest() {
            pos = at(6, 6);
            previousPos = at(7, 6);
            activateSteathGameplayBehaviour();
        }

        float detectionFor(Char target) {
            return ((StealthGameplayWandering) WANDERING).detectionChance(target);
        }
    }

    private Hero oldHero;
    private Level oldLevel;
    private Hero hero;
    private Patroller patroller;

    @Before
    public void setUp() {
        HeadlessTestSupport.start();
        oldHero = Dungeon.hero;
        oldLevel = Dungeon.level;
        Actor.clear();

        EmptyLevel level = new EmptyLevel();
        level.setSize(WIDTH, WIDTH);
        level.mobs = new HashSet<>();
        Arrays.fill(level.map, Terrain.EMPTY);
        Arrays.fill(level.passable, true);
        Dungeon.level = level;

        hero = new Hero();
        hero.heroClass = HeroClass.WARRIOR;
        hero.HP = hero.HT = 20;
        hero.pos = at(4, 5);
        Dungeon.hero = hero;
        Actor.add(hero);

        patroller = new Patroller();
        patroller.faceWest();
    }

    @After
    public void tearDown() {
        Actor.clear();
        Dungeon.hero = oldHero;
        Dungeon.level = oldLevel;
    }

    @Test
    public void frontConeIsSymmetricAcrossSignedAngleSeam() {
        // Westward movement is +180 degrees; northwest is -153.4 degrees.
        // The original raw absolute difference incorrectly classified northwest as behind.
        hero.pos = at(4, 5);
        float expected = 1f / (patroller.distance(hero) / 2f + hero.stealth());
        assertEquals(expected, patroller.detectionFor(hero), 0.0001f);

        hero.pos = at(4, 7);
        assertEquals(expected, patroller.detectionFor(hero), 0.0001f);
    }

    @Test
    public void outerVisionConeIsSymmetricAcrossSignedAngleSeam() {
        // Both diagonal cells are 45 degrees from west, beyond the 37.5-degree
        // front half-cone but inside the existing 75-degree outer half-cone.
        hero.pos = at(5, 5);
        float expected = 1f / (patroller.distance(hero) + hero.stealth());
        assertEquals(expected, patroller.detectionFor(hero), 0.0001f);

        hero.pos = at(5, 7);
        assertEquals(expected, patroller.detectionFor(hero), 0.0001f);
    }

    @Test
    public void enemyBehindPatrollerRetainsLowDetectionChance() {
        hero.pos = at(8, 6);
        float base = patroller.distance(hero) + hero.stealth();
        float expected = 1f / (base * base * base);
        if (expected < 0.1f) expected = 0f;
        assertEquals(expected, patroller.detectionFor(hero), 0.0001f);
    }
}

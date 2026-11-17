package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.PathFinder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertSame;

/** Regressions for keeping the mob AI's current best path distance in sync. */
public class MobNearestTargetDistanceTest {

    private Hero previousHero;
    private Level previousLevel;

    private static class Picker extends Rat {
        final Set<Char> attackable = new HashSet<>();

        @Override
        protected boolean canAttack(Char target) {
            return attackable.contains(target);
        }

        Char pick(Char... candidates) {
            LinkedHashSet<Char> ordered = new LinkedHashSet<>(Arrays.asList(candidates));
            return chooseClosestEnemy(ordered);
        }
    }

    @Before
    public void setUp() {
        HeadlessTestSupport.start();
        previousHero = Dungeon.hero;
        previousLevel = Dungeon.level;
        PathFinder.setMapSize(20, 20);
        Arrays.fill(PathFinder.distance, Integer.MAX_VALUE);
    }

    @After
    public void tearDown() {
        Dungeon.hero = previousHero;
        Dungeon.level = previousLevel;
        if (previousLevel != null) {
            PathFinder.setMapSize(previousLevel.width(), previousLevel.height());
        } else {
            PathFinder.setMapSize(1, 1);
        }
    }

    private Rat ratAt(int pos, int pathDistance) {
        Rat rat = new Rat();
        rat.pos = pos;
        PathFinder.distance[pos + 1] = pathDistance;
        return rat;
    }

    @Test
    public void laterIntermediateDistanceCannotReplaceTheActualNearestTarget() {
        Picker picker = new Picker();
        Rat first = ratAt(63, 10);
        Rat nearest = ratAt(210, 3);
        Rat later = ratAt(336, 7);

        // Original loop forgot to change closestDist after selecting nearest,
        // so a later distance of 7 was incorrectly compared against first's 10.
        assertSame(nearest, picker.pick(first, nearest, later));
    }

    @Test
    public void heroStillWinsAnEqualDistanceTie() {
        Picker picker = new Picker();
        Rat other = ratAt(63, 4);
        Hero hero = new Hero();
        hero.pos = 210;
        Dungeon.hero = hero;
        PathFinder.distance[hero.pos + 1] = 4;

        assertSame(hero, picker.pick(other, hero));
    }

    @Test
    public void currentlyAttackableEnemyKeepsItsExistingPriority() {
        Picker picker = new Picker();
        Rat close = ratAt(63, 1);
        Rat attackable = ratAt(210, 8);
        Rat later = ratAt(336, 2);
        picker.attackable.add(attackable);

        assertSame(attackable, picker.pick(close, attackable, later));
    }

    @Test
    public void fartherAttackableTargetCannotReplaceANearerAttackableTarget() {
        Picker picker = new Picker();
        Rat nearer = ratAt(63, 2);
        Rat farther = ratAt(210, 7);
        picker.attackable.add(nearer);
        picker.attackable.add(farther);

        // Both are immediately attackable, so the existing nearest-target
        // rule must not replace the nearer first candidate with a farther one.
        assertSame(nearer, picker.pick(nearer, farther));
    }

    @Test
    public void equallyDistantAttackableHeroStaysPreferredAfterAMob() {
        Picker picker = new Picker();
        Hero hero = new Hero();
        hero.pos = 210;
        Dungeon.hero = hero;
        PathFinder.distance[hero.pos + 1] = 4;
        Rat mob = ratAt(336, 4);
        picker.attackable.add(hero);
        picker.attackable.add(mob);

        // A later attackable mob must not reverse the existing hero tie rule.
        assertSame(hero, picker.pick(hero, mob));
    }

    @Test
    public void otherAttackableTargetsRetainTheirExistingEqualDistanceTieOrder() {
        Picker picker = new Picker();
        Rat first = ratAt(63, 4);
        Rat later = ratAt(210, 4);
        picker.attackable.add(first);
        picker.attackable.add(later);

        assertSame(later, picker.pick(first, later));
    }
}

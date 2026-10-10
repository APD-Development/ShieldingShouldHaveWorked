package com.shatteredpixel.shatteredpixeldungeon.apd.simulation;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.watabou.utils.Random;
import org.junit.Test;
import static org.junit.Assert.*;

public class AuditedSewersMeleeBoundsTest {
    @Test public void inheritedRareEnemyRollsUseAuditedParentWithoutGuessingUnknownSubclasses() throws Exception {
        assertEquals(Rat.class, Albino.class.getMethod("damageRoll").getDeclaringClass());
        assertEquals(Rat.class, FetidRat.class.getMethod("damageRoll").getDeclaringClass());
        assertEquals(Gnoll.class, GnollTrickster.class.getMethod("damageRoll").getDeclaringClass());
        assertEquals(Crab.class, GreatCrab.class.getMethod("damageRoll").getDeclaringClass());
        assertEquals(4, AuditedSewersMeleeBounds.maximum(new Albino(), 1));
        assertEquals(4, AuditedSewersMeleeBounds.maximum(new FetidRat(), 2));
        assertEquals(6, AuditedSewersMeleeBounds.maximum(new GnollTrickster(), 3));
        assertEquals(10, AuditedSewersMeleeBounds.maximum(new GnollExile(), 3));
        assertEquals(GnollExile.class, GnollExile.class.getMethod("damageRoll").getDeclaringClass());
        assertEquals(7, AuditedSewersMeleeBounds.maximum(new GreatCrab(), 4));
        assertEquals(0, AuditedSewersMeleeBounds.maximum(null, 1));
        try { AuditedSewersMeleeBounds.maximum(new Rat(){}, 1); fail("unknown subclass accepted"); }
        catch (IllegalStateException expected) { }
    }
    @Test public void actualWraithRollBoundsTrackItsStoredLevelRatherThanCurrentFloor() {
        for (int level : new int[]{1, 2, 4, 10, 25}) {
            Wraith wraith = new Wraith();
            wraith.adjustStats(level);
            int maximum = AuditedSewersMeleeBounds.maximum(wraith, 2);
            assertEquals(level + 2, maximum);
            Random.pushGenerator(8301 + level);
            try {
                for (int i = 0; i < 1000; i++) {
                    int roll = wraith.damageRoll();
                    assertTrue(roll >= 1 + level / 2 && roll <= maximum);
                }
            } finally { Random.popGenerator(); }
        }
    }
    @Test public void observingBoundsDoesNotRollDamageOrAdvanceRandomGenerator() {
        Wraith wraith = new Wraith(); wraith.adjustStats(3);
        Mob[] mobs = {new Rat(), new Albino(), new Snake(), new Swarm(), new Gnoll(), new GnollExile(), new Crab(), new Piranha(), wraith, new Mimic()};
        Random.pushGenerator(83219);
        int expected;
        try { expected = Random.Int(); } finally { Random.popGenerator(); }
        Random.pushGenerator(83219);
        try {
            for (Mob mob : mobs) AuditedSewersMeleeBounds.maximum(mob, 3);
            assertEquals(expected, Random.Int());
        } finally { Random.popGenerator(); }
    }
    @Test public void mimicVariantsUseActualStoredLevelAndInheritedRolls() throws Exception {
        assertEquals(Mimic.class, GoldenMimic.class.getMethod("damageRoll").getDeclaringClass());
        assertEquals(CrystalMimic.class, CrystalMimic.class.getMethod("damageRoll").getDeclaringClass());
        assertEquals(Mimic.class, GoldenMimic.class.getMethod("attackSkill", Char.class).getDeclaringClass());
        assertEquals(Mimic.class, CrystalMimic.class.getMethod("attackSkill", Char.class).getDeclaringClass());
        for (Mimic mimic : new Mimic[]{new Mimic(),new GoldenMimic(),new CrystalMimic()}) {
            for (int level : new int[]{1,4,10}) {
                mimic.setLevel(level);
                int maximum = AuditedSewersMeleeBounds.maximum(mimic, 1);
                mimic.alignment = Char.Alignment.NEUTRAL;
                int neutral = mimic.damageRoll();
                if (mimic instanceof CrystalMimic) assertTrue(neutral <= maximum);
                else assertEquals("neutral roll is the exact maximum", maximum, neutral);
                assertEquals(Char.Alignment.NEUTRAL, mimic.alignment);
                mimic.alignment = Char.Alignment.ENEMY;
                Random.pushGenerator(1910 + level);
                try {
                    for (int i=0; i<1000; i++) assertTrue(mimic.damageRoll() <= maximum);
                } finally { Random.popGenerator(); }
            }
        }
    }
}

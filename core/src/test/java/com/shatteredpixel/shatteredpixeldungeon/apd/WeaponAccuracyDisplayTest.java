package com.shatteredpixel.shatteredpixeldungeon.apd;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.HeavyBoomerang;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import java.util.LinkedHashMap;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sickle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BattleAxe;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.watabou.utils.Random;
import org.junit.Test;
import static org.junit.Assert.*;
public class WeaponAccuracyDisplayTest {
    private static class DisplaySword extends Sword {
        String description() { return accuracyDescription(); }
    }
    private static class DisplayBoomerang extends HeavyBoomerang {
        String description() { return accuracyDescription(); }
    }
    @Test public void strengthDeficitUsesCombatPenaltyWithoutInventingHitChance() {
        assertEquals(1f, Weapon.strengthAccuracyModifier(1f,11,11),0f);
        assertEquals(1f, Weapon.strengthAccuracyModifier(1f,11,15),0f);
        assertEquals((float)(1/Math.pow(1.5,4)),Weapon.strengthAccuracyModifier(1f,15,11),0.000001f);
    }
    @Test public void unknownMeleeAndMissileUpgradesUseVisibleBaseRequirement() {
        for (Weapon weapon : new Weapon[]{new Sword(),new HeavyBoomerang()}) {
            int base = weapon.STRReq(0);
            weapon.level(3);
            weapon.levelKnown = false;
            assertEquals(base,weapon.displayedStrengthRequirement());
            weapon.levelKnown = true;
            assertEquals(weapon.STRReq(),weapon.displayedStrengthRequirement());
            assertTrue(weapon.displayedStrengthRequirement() < base);
        }
    }
    @Test public void disclosedPercentageMatchesActualStrengthPenaltyAcrossWeaponTypes() {
        Hero hero = new Hero();
        for (Weapon weapon : new Weapon[]{new Sword(), new Sickle(), new BattleAxe()}) {
            weapon.levelKnown = true;
            for (int strength = 5; strength <= 20; strength++) {
                hero.STR = strength;
                float expected = Weapon.strengthAccuracyModifier(weapon.ACC, weapon.displayedStrengthRequirement(), hero.STR());
                assertEquals(expected, weapon.accuracyFactor(hero, null), 0.000001f);
            }
        }
    }
    @Test public void descriptionsDoNotRevealHiddenUpgradesOrConsumeRngOrResources() {
        HeadlessTestSupport.start();
        Messages.setup(Languages.ENGLISH);
        Hero previous = Dungeon.hero;
        try {
            Dungeon.hero = new Hero();
            Dungeon.hero.STR = 11;
            DisplaySword sword = new DisplaySword();
            sword.levelKnown = false;
            sword.level(0);
            String before = sword.description();
            sword.level(8);
            assertEquals(before, sword.description());
            assertTrue(before.contains("not hit chance"));
            assertTrue(before.contains("until upgrades are identified"));
            int hp = Dungeon.hero.HP, quantity = sword.quantity();
            Random.pushGenerator(91021);
            int expected;
            try { expected = Random.Int(); } finally { Random.popGenerator(); }
            Random.pushGenerator(91021);
            try { sword.description(); assertEquals(expected, Random.Int()); } finally { Random.popGenerator(); }
            assertEquals(hp, Dungeon.hero.HP);
            assertEquals(quantity, sword.quantity());
            sword.levelKnown = true;
            assertNotEquals(before, sword.description());
            assertFalse(sword.description().contains("until upgrades are identified"));
            assertTrue("compact explanation", sword.description().length() < 200);
        } finally { Dungeon.hero = previous; }
    }
    @Test public void throwingDescriptionComputesBothRangesWithoutInventingHitChance() {
        HeadlessTestSupport.start();
        Messages.setup(Languages.ENGLISH);
        Hero previous = Dungeon.hero;
        try {
            Dungeon.hero = new Hero();
            Dungeon.hero.STR = 20;
            DisplayBoomerang weapon = new DisplayBoomerang();
            weapon.levelKnown = true;
            String text = weapon.description();
            assertTrue(text, text.contains("150.0%"));
            assertTrue(text, text.contains("50.0%"));
            assertTrue(text, text.contains("not hit chance"));
            assertTrue(text.length() < 200);
        } finally { Dungeon.hero = previous; }
    }
    @Test public void rangedValuesMatchActualCombatAtBothDistancesAndPointBlankLevels() {
        HeadlessTestSupport.start();
        Messages.setup(Languages.ENGLISH);
        Hero previousHero = Dungeon.hero;
        Level previousLevel = Dungeon.level;
        try {
            Dungeon.hero = new Hero();
            Dungeon.hero.pos = 6;
            Dungeon.level = new SewerLevel();
            Dungeon.level.setSize(5, 5);
            Dungeon.hero.talents.add(new LinkedHashMap<>());
            DisplayBoomerang weapon = new DisplayBoomerang();
            weapon.levelKnown = true;
            Rat target = new Rat();
            for (int points = 0; points <= 2; points++) {
                Dungeon.hero.talents.get(0).put(Talent.POINT_BLANK, points);
                for (int strength : new int[]{11, 20}) {
                    Dungeon.hero.STR = strength;
                    target.pos = 7;
                    float adjacent = weapon.accuracyFactor(Dungeon.hero, target) * 100;
                    target.pos = 18;
                    float ranged = weapon.accuracyFactor(Dungeon.hero, target) * 100;
                    String expected = Messages.get(Weapon.class, "apd_accuracy_ranged", ranged, adjacent);
                    assertEquals("\n\n" + expected, weapon.description());
                }
            }
            weapon.levelKnown = false;
            weapon.level(0);
            String hidden = weapon.description();
            weapon.level(8);
            assertEquals(hidden, weapon.description());
        } finally { Dungeon.hero = previousHero; Dungeon.level = previousLevel; }
    }
}

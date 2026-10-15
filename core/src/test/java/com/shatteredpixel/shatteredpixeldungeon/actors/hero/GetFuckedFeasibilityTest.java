package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.LayOnHands;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWard;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ShieldOfLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.apd.LevelGenerationHarness;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.Random;
import com.watabou.utils.PathFinder;
import org.junit.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/** Bounded scenarios in the real engine. No action policy, pathfinding, bot, or full-run claim. */
public class GetFuckedFeasibilityTest {
    private static class TestHero extends Hero {
        int deaths;
        boolean reviveAfterDeath;
        @Override public void interrupt() { curAction = null; resting = false; }
        //Only replace final game-over presentation. Health and defenses use production code.
        @Override public void die(Object source) {
            deaths++;
            if (reviveAfterDeath) restoreHealthForRevival(true);
        }
    }
    private static class SilentSprite extends CharSprite {
        @Override public void showStatusWithIcon(int color, String text, int icon, Object... args) {}
    }
    private TestHero hero;
    private Hero savedHero;
    private com.shatteredpixel.shatteredpixeldungeon.levels.Level savedLevel;
    private int savedChallenges, savedDepth, savedAscent;
    private boolean savedIntro, savedLocked;

    @BeforeClass public static void initialize() throws Exception {
        HeadlessTestSupport.start();
        LevelGenerationHarness.generate(12345L, 0, HeroClass.CLERIC,
                LevelGenerationHarness.Context.REFERENCE, 1);
    }
    @Before public void setup() {
        savedHero = Dungeon.hero;
        savedLevel = Dungeon.level;
        savedChallenges = Dungeon.challenges;
        savedDepth = Dungeon.depth;
        savedAscent = Statistics.highestAscent;
        savedIntro = SPDSettings.intro();
        savedLocked = Dungeon.level.locked;
        Actor.clear();
        Dungeon.challenges = Challenges.GET_FUCKED;
        Dungeon.depth = 1;
        Dungeon.level.locked = false;
        SPDSettings.intro(false);
        hero = new TestHero();
        hero.heroClass = HeroClass.CLERIC;
        Talent.initClassTalents(hero);
        hero.belongings.armor = new ClothArmor();
        hero.pos = Dungeon.level.entrance();
        Dungeon.hero = hero;
        Random.pushGenerator(12345L);
    }
    @After public void cleanup() {
        Random.popGenerator();
        Actor.clear();
        Dungeon.hero = savedHero;
        Dungeon.level = savedLevel;
        Dungeon.challenges = savedChallenges;
        Dungeon.depth = savedDepth;
        Statistics.highestAscent = savedAscent;
        SPDSettings.intro(savedIntro);
        Dungeon.level.locked = savedLocked;
    }
    private static void set(Object target, Class<?> owner, String name, Object value) throws Exception {
        Field f = owner.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
    private static int charge(Artifact tome) throws Exception {
        Field f = Artifact.class.getDeclaredField("charge");
        f.setAccessible(true);
        return f.getInt(tome);
    }
    private Barrier barrier(int value) {
        Barrier b = Buff.affect(hero, Barrier.class);
        b.setShield(value);
        return b;
    }
    private Hunger hunger(float value) throws Exception {
        Hunger h = Buff.affect(hero, Hunger.class);
        set(h, Hunger.class, "level", value);
        return h;
    }
    private HolyTome tome(int level) {
        HolyTome t = new HolyTome();
        for (int i = 0; i < level; i++) t.upgrade();
        hero.belongings.artifact = t;
        t.activate(hero);
        return t;
    }

    @Test public void ordinaryRatMaximumIsCompletelyBlockedByWardAndTwoPointLight() {
        hero.talents.get(0).put(Talent.SHIELD_OF_LIGHT, 2);
        Buff.affect(hero, HolyWard.HolyArmBuff.class);
        Rat rat = new Rat();
        Buff.affect(hero, ShieldOfLight.ShieldOfLightTracker.class).object = rat.id();
        //All rat damage values, repeatedly exercising the actual random Shield of Light roll.
        for (int i = 0; i < 1000; i++) {
            for (int raw = 1; raw <= 4; raw++) {
                assertEquals(0, hero.defenseProc(rat, raw));
            }
        }
        assertEquals(1, hero.HP);
        assertEquals(1, hero.HT);
    }

    @Test public void layOnHandsActuallyAddsFlatShieldAndCapsAt75() throws Exception {
        hero.lvl = 15;
        hero.subClass = HeroSubClass.PALADIN;
        Talent.initSubclassTalents(hero);
        hero.talents.get(2).put(Talent.LAY_ON_HANDS, 3);
        assertTrue(LayOnHands.INSTANCE.canCast(hero));
        Barrier b = barrier(0);
        //Invoke the real effect method, skipping only target selection and animation.
        Method effect = LayOnHands.class.getDeclaredMethod("affectChar", Hero.class,
                com.shatteredpixel.shatteredpixeldungeon.actors.Char.class);
        effect.setAccessible(true);
        hero.sprite = new SilentSprite();
        for (int expected : new int[]{25, 50, 75, 75}) {
            effect.invoke(LayOnHands.INSTANCE, hero, hero);
            assertEquals(expected, b.shielding());
            assertEquals(1, hero.HP);
            assertEquals(1, hero.HT);
        }
        hero.sprite = null;
        System.out.println("FEASIBILITY Lay on Hands: 25/50/75/75 barrier at 1 HP; isolated effect, not full casting flow");
    }

    @Test public void barrier75DecaysTo20In55ActualTicks() {
        Barrier b = barrier(75);
        for (int i = 0; i < 55; i++) b.act();
        assertEquals(20, b.shielding());
        assertEquals(1, hero.HP);
        System.out.println("FEASIBILITY 75 barrier -> 20 after 55 actual Barrier.act ticks");
    }

    @Test public void fullBarrierAbsorbs75ButOneMoreCombatDamageKills() {
        barrier(75);
        hero.damage(75, new Object());
        assertEquals(1, hero.HP);
        assertEquals(0, hero.deaths);
        hero.damage(1, new Object());
        assertEquals(0, hero.HP);
        assertEquals(1, hero.deaths);
    }

    private int refill(int level, int capacity) throws Exception {
        HolyTome t = tome(level);
        t.directCharge(100);
        assertEquals(capacity, charge(t));
        t.spendCharge(capacity);
        assertEquals(0, charge(t));
        HolyTome.TomeRecharge recharge = hero.buff(HolyTome.TomeRecharge.class);
        assertNotNull(recharge);
        int ticks = 0;
        while (charge(t) < capacity && ticks < 500) { recharge.act(); ticks++; }
        assertEquals(capacity, charge(t));
        System.out.println("FEASIBILITY Tome +" + level + " empty -> full: " + ticks + " actual recharge ticks");
        return ticks;
    }
    @Test public void startingTomeRefillConsumesAbout130Ticks() throws Exception {
        int ticks = refill(0, 3);
        assertTrue(ticks >= 129 && ticks <= 134);
    }
    @Test public void maximumTomeRefillConsumesMoreThanOneRationBudget() throws Exception {
        int ticks = refill(10, 10);
        assertTrue(ticks >= 345 && ticks <= 355);
        assertTrue(ticks > 300);
    }

    @Test public void bossRechargeStopsWhenThePassiveAllowanceRunsOut() throws Exception {
        HolyTome t = tome(0);
        t.spendCharge(3);
        Dungeon.level.locked = true;
        LockedFloor lock = Buff.affect(hero, LockedFloor.class);
        HolyTome.TomeRecharge recharge = hero.buff(HolyTome.TomeRecharge.class);
        for (int i = 0; i < 51; i++) { lock.act(); recharge.act(); }
        assertFalse(Regeneration.regenOn());
        int before = charge(t);
        for (int i = 0; i < 200; i++) recharge.act();
        assertEquals(before, charge(t));
    }

    @Test public void threeWorstCaseOrdinaryTenguHitsFit15BarrierWithChainAndWard() {
        hero.lvl = 10;
        hero.STR = 14;
        MailArmor armor = new MailArmor();
        armor.level(6);
        hero.belongings.armor = armor;
        assertEquals(6, armor.DRMin());
        assertEquals(24, armor.DRMax());
        assertTrue(hero.STR() >= armor.STRReq());
        Buff.affect(hero, HolyWard.HolyArmBuff.class);
        Barrier b = barrier(15);
        Tengu enemy = new Tengu();
        for (int i = 0; i < 3; i++) {
            int net = Math.max(0, hero.defenseProc(enemy, 12) - armor.DRMin());
            assertEquals(5, net);
            hero.damage(net, enemy);
        }
        assertEquals(0, b.shielding());
        assertEquals(1, hero.HP);
        assertEquals(0, hero.deaths);
        System.out.println("FEASIBILITY Three worst-case ordinary Tengu hits absorbed; excludes hazards, decay and elapsed turns");
    }

    @Test public void faithMakesTheSameWorstCaseTenguSequenceFatalOnSecondHit() {
        hero.lvl = 10;
        hero.STR = 14;
        Dungeon.challenges |= Challenges.NO_ARMOR;
        MailArmor armor = new MailArmor();
        armor.level(6);
        hero.belongings.armor = armor;
        assertEquals(0, armor.DRMin());
        assertEquals(10, armor.DRMax());
        assertTrue(hero.STR() >= armor.STRReq());
        Buff.affect(hero, HolyWard.HolyArmBuff.class);
        barrier(15);
        Tengu enemy = new Tengu();
        for (int i = 0; i < 2; i++) hero.damage(
                Math.max(0, hero.defenseProc(enemy, 12) - armor.DRMin()), enemy);
        assertEquals(0, hero.HP);
        assertEquals(1, hero.deaths);
    }

    @Test public void eatingBeforeTheThresholdSurvivesButShieldDoesNotSaveTheThreshold() throws Exception {
        Hunger h = hunger(Hunger.STARVING - 1);
        h.satisfy(300);
        h.act();
        assertEquals(150, h.hunger());
        assertEquals(1, hero.HP);
        barrier(75);
        set(h, Hunger.class, "level", Hunger.STARVING - 1);
        h.act();
        assertEquals(0, hero.HP);
        assertEquals(1, hero.deaths);
        assertEquals(75, hero.shielding());
    }

    @Test public void ascensionDamageIsShieldableAndAQualifiedKillStopsItAtSevenStacks() throws Exception {
        AscensionChallenge curse = Buff.affect(hero, AscensionChallenge.class);
        set(curse, AscensionChallenge.class, "stacks", 8f);
        Barrier b = barrier(2);
        //Isolate the curse from generated mobs' alert animations, which need a rendered scene.
        java.util.HashSet<com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob> mobs =
                new java.util.HashSet<>(Dungeon.level.mobs);
        Dungeon.level.mobs.clear();
        try {
            curse.act();
            assertEquals(1, b.shielding());
            assertEquals(1, hero.HP);
            AscensionChallenge.processEnemyKill(new Rat());
            for (int i = 0; i < 10; i++) curse.act();
            assertEquals(1, b.shielding());
            assertEquals(1, hero.HP);
        } finally {
            Dungeon.level.mobs.addAll(mobs);
        }
        System.out.println("FEASIBILITY Ascension damage shielded at 8 stacks; eligible kill to 7 stops periodic damage");
    }

    @Test public void allFourAscentBossCheckpointsRestoreHungerWithoutRaisingHp() throws Exception {
        AscensionChallenge curse = Buff.affect(hero, AscensionChallenge.class);
        Hunger h = hunger(400);
        for (int depth : new int[]{20, 15, 10, 5}) {
            Dungeon.depth = depth;
            Statistics.highestAscent = depth + 1;
            set(h, Hunger.class, "level", 400f);
            curse.onLevelSwitch();
            assertEquals(0, h.hunger());
            assertEquals(1, hero.HP);
            assertEquals(1, hero.HT);
        }
    }

    @Test public void gooOozeStillCostsOneShieldEvenWhenStandingInWater() {
        Dungeon.depth = 5;
        boolean oldWater = Dungeon.level.water[hero.pos];
        Dungeon.level.water[hero.pos] = true;
        try {
            Barrier b = barrier(2);
            Ooze ooze = Buff.affect(hero, Ooze.class);
            ooze.set(Ooze.DURATION);
            ooze.act();
            assertEquals(1, b.shielding());
            assertEquals(1, hero.HP);
            assertNull(hero.buff(Ooze.class));
        } finally {
            Dungeon.level.water[hero.pos] = oldWater;
        }
        System.out.println("FEASIBILITY Goo ooze: first tick costs 1 shield even in water, then detaches");
    }

    @Test public void prismaticAllyRetainsLevelBasedHpDespiteHeroCap() {
        hero.lvl = 30;
        hero.updateHT(true);
        assertEquals(1, hero.HT);
        int hp = PrismaticGuard.maxHP(hero);
        PrismaticImage image = new PrismaticImage();
        image.duplicate(hero, hp);
        assertEquals(85, image.HT);
        assertEquals(85, image.HP);
        assertEquals(1, hero.HT);
        System.out.println("FEASIBILITY Level-30 prismatic image retains 85 HP while hero remains 1 HP");
    }

    @Test public void blessedRevivalRetainsTheLongOneHpStarvationDamageInterval() throws Exception {
        //Stub only the death callback; the existing tests cover revival HP itself.
        //No claim that an unlimited number of Ankhs exists or that this is an item-flow test.
        hero.reviveAfterDeath = true;
        Hunger h = hunger(Hunger.STARVING - 1);
        h.act();
        assertEquals(1, hero.deaths);
        assertEquals(1, hero.HP);
        assertTrue(h.isStarving());
        int ticks = 0;
        while (hero.deaths == 1 && ticks < 1100) { h.act(); ticks++; }
        assertEquals(2, hero.deaths);
        assertTrue(ticks >= 1000 && ticks <= 1002);
        System.out.println("FEASIBILITY Post-blessed-revival starvation interval at 1 HT: "+ticks
                +" hunger ticks before next lethal damage event; finite Ankh consumption still required");
    }

    @Test public void ascentKillXpCanChargeAHornAlongsideTheTomeAtLevel30() throws Exception {
        hero.lvl = 30;
        HolyTome tome = tome(0);
        HornOfPlenty horn = new HornOfPlenty();
        horn.level(10);
        hero.belongings.misc = horn;
        horn.activate(hero);
        assertTrue(tome.isEquipped(hero));
        assertTrue(horn.isEquipped(hero));
        AscensionChallenge curse = Buff.affect(hero, AscensionChallenge.class);
        int generatedCharges = 0;
        for (int i = 0; i < 40; i++) {
            set(curse, AscensionChallenge.class, "stacks", 1f);
            AscensionChallenge.processEnemyKill(new Rat());
            generatedCharges += charge(horn);
            //Measure uncapped production; eating itself is not simulated in this hook test.
            set(horn, Artifact.class, "charge", 0);
        }
        assertEquals(19, generatedCharges);
        assertEquals(30, hero.lvl);
        assertEquals(1, hero.HT);
        assertEquals(1, hero.HP);
        System.out.println("FEASIBILITY Forty full curse reductions at level 30 generate "+generatedCharges
                +" whole +10 Horn charges (1710 satiety at ordinary settings), if promptly consumed to prevent capping");
    }

    @Test public void measureStaticTravelLowerBoundsAndPlacedFoodWithoutPlaying() throws Exception {
        //Topology measurements only. No actor acts, hero decisions, item use, or run outcome.
        for (long seed : new long[]{1L, 12345L, 54321L, 8675309L, 31415926L, 27182818L}) {
            final int[] totals = new int[7]; //floors, reachable, steps, food count, satiety, doors-reachable, doors-steps
            final int[] regionSteps = new int[5];
            LevelGenerationHarness.levelObserver = (depth, level) -> {
                if (depth > 24 || depth % 5 == 0) return;
                totals[0]++;
                PathFinder.setMapSize(level.width(), level.height());
                PathFinder.buildDistanceMap(level.entrance(), level.passable);
                int distance = PathFinder.distance[level.exit()];
                if (distance < Integer.MAX_VALUE) {
                    totals[1]++;
                    totals[2] += distance;
                    regionSteps[(depth-1)/5] += distance;
                }
                boolean[] opened = level.passable.clone();
                for (int cell = 0; cell < opened.length; cell++) {
                    int terrain = level.map[cell];
                    if (terrain == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.SECRET_DOOR
                            || terrain == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.LOCKED_DOOR
                            || terrain == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.CRYSTAL_DOOR
                            || terrain == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.LOCKED_EXIT) opened[cell] = true;
                }
                PathFinder.buildDistanceMap(level.entrance(), opened);
                int openedDistance = PathFinder.distance[level.exit()];
                if (openedDistance < Integer.MAX_VALUE) {
                    totals[5]++;
                    totals[6] += openedDistance;
                }
                if (distance == Integer.MAX_VALUE) System.out.println("TOPOLOGY_BLOCK seed="+seed
                        +" depth="+depth+" entrance_terrain="+level.map[level.entrance()]
                        +" exit_terrain="+level.map[level.exit()]+" doors_open_distance="+openedDistance);
                for (Heap heap : level.heaps.valueList()) for (Item item : heap.items) {
                    if (item instanceof Food) {
                        totals[3] += item.quantity();
                        totals[4] += Math.round(((Food)item).energy * item.quantity());
                    }
                }
            };
            try {
                LevelGenerationHarness.generate(seed, Challenges.GET_FUCKED, HeroClass.CLERIC,
                        LevelGenerationHarness.Context.REFERENCE, 26);
            } finally {
                LevelGenerationHarness.levelObserver = null;
            }
            assertEquals(20, totals[0]);
            System.out.println("TOPOLOGY seed="+seed+" reachable="+totals[1]+"/20 stair_steps="+totals[2]
                    +" region_steps="+java.util.Arrays.toString(regionSteps)+" placed_food="+totals[3]
                    +" placed_food_satiety="+totals[4]
                    +" doors_open_reachable="+totals[5]+"/20 doors_open_steps="+totals[6]
                    +"; ignores hazards, mobs, exploration, item access, branches and boss traversal");
        }
    }
}

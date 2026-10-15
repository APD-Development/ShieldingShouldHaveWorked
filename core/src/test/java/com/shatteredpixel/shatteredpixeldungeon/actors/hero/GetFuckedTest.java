package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.TimeStasis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WellFed;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.apd.LevelGenerationHarness;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.Bundle;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class GetFuckedTest {

	//Intercept only presentation/final game-over handling. Damage, hunger, HP calculation,
	//buffs and revival HP use production code.
	private static class TestHero extends Hero {
		int deaths;
		Object cause;
		boolean revive;
		boolean resetHunger;
		@Override public void interrupt() {
			//The headless fixture has no CellSelector or keyboard state.
			curAction = null;
			resting = false;
		}
		@Override public void die(Object source) {
			deaths++;
			cause = source;
			assertEquals(0, HP);
			if (revive) restoreHealthForRevival(true);
			if (resetHunger) ((Hunger)source).satisfy(Hunger.STARVING);
		}
		void restoreRevival(boolean blessed) { restoreHealthForRevival(blessed); }
	}

	private static class TestShield extends ShieldBuff {
		TestShield(int value) { setShield(value); }
	}

	private TestHero hero;
	private int savedChallenges;
	private boolean savedIntro;
	private Hero savedHero;
	private Level savedLevel;
	private int savedSetting;

	@BeforeClass public static void headless() throws Exception {
		HeadlessTestSupport.start();
		LevelGenerationHarness.generate(12345L, 0, HeroClass.WARRIOR,
				LevelGenerationHarness.Context.REFERENCE, 1);
	}

	@Before public void setup() {
		savedChallenges = Dungeon.challenges;
		savedIntro = SPDSettings.intro();
		savedSetting = SPDSettings.challenges();
		savedHero = Dungeon.hero;
		savedLevel = Dungeon.level;
		Actor.clear();
		Dungeon.challenges = Challenges.GET_FUCKED;
		SPDSettings.intro(false);
		Dungeon.level.locked = false;
		hero = new TestHero();
		hero.heroClass = HeroClass.WARRIOR;
		Talent.initClassTalents(hero);
		hero.pos = Dungeon.level.entrance();
		Dungeon.hero = hero;
	}

	@After public void cleanup() {
		Actor.clear();
		Dungeon.challenges = savedChallenges;
		Dungeon.hero = savedHero;
		Dungeon.level = savedLevel;
		SPDSettings.challenges(savedSetting);
		SPDSettings.intro(savedIntro);
	}

	private static void field(Object object, Class<?> owner, String name, Object value) throws Exception {
		Field f = owner.getDeclaredField(name);
		f.setAccessible(true);
		f.set(object, value);
	}

	private Hunger hunger(float level, float partial) throws Exception {
		Hunger h = new Hunger();
		field(h, Hunger.class, "level", level);
		field(h, Hunger.class, "partialDamage", partial);
		assertTrue(h.attachTo(hero));
		return h;
	}

	@Test public void healthIsFixedThroughLevelAndItemBonuses() {
		assertEquals(1, hero.HP);
		assertEquals(1, hero.HT);
		for (int level = 1; level <= 30; level++) {
			hero.lvl = level;
			hero.HTBoost = 500;
			hero.updateHT(true);
			assertEquals(1, hero.HT);
			assertEquals(1, hero.HP);
		}
		RingOfMight ring = new RingOfMight();
		ring.level(10);
		ring.activate(hero);
		ElixirOfMight.HTBoost boost = new ElixirOfMight.HTBoost();
		boost.attachTo(hero);
		boost.reset();
		hero.updateHT(true);
		assertEquals(1, hero.HT);
		assertEquals(1, hero.HP);
		hero.HP = 0;
		hero.updateHT(true);
		assertEquals("HP recalculation must not revive a dead hero", 0, hero.HP);
	}

	@Test public void loadingClampsBeforeRestoringBuffsAndDoesNotReviveTheDead() {
		Bundle save = new Bundle();
		hero.storeInBundle(save);
		save.put("HT", 120);
		save.put("HP", 90);
		Hero loaded = new Hero();
		loaded.restoreFromBundle(save);
		assertEquals(1, loaded.HT);
		assertEquals(1, loaded.HP);
		save.put("HP", 0);
		loaded.restoreFromBundle(save);
		assertEquals(1, loaded.HT);
		assertEquals(0, loaded.HP);
	}

	@Test public void bothRevivalPathsRestoreExactlyOneHp() {
		for (boolean blessed : new boolean[]{false, true}) {
			hero.HP = 0;
			hero.restoreRevival(blessed);
			assertEquals(1, hero.HT);
			assertEquals(1, hero.HP);
		}
	}

	@Test public void ordinaryResurrectionRetainsItsExistingHungerReset() throws Exception {
		hunger(Hunger.STARVING, 0);
		hero.HP = 0;
		hero.resurrect();
		assertEquals(1, hero.HT);
		assertEquals(1, hero.HP);
		assertEquals(0, hero.buff(Hunger.class).hunger());
	}

	@Test public void viewingAnOrdinaryRankingAfterGetFuckedDoesNotClampItsHero() throws Exception {
		LevelGenerationHarness.generate(54321L, 0, HeroClass.WARRIOR,
				LevelGenerationHarness.Context.REFERENCE, 1);
		Rankings.Record record = new Rankings.Record();
		Rankings.INSTANCE.saveGameData(record);
		Dungeon.challenges = Challenges.GET_FUCKED;
		Rankings.INSTANCE.loadGameData(record);
		assertEquals(0, Dungeon.challenges);
		assertEquals(20, Dungeon.hero.HT);
		assertEquals(20, Dungeon.hero.HP);
	}

	@Test public void regenerationCannotTemporarilyOverhealADeathlessHero() throws Exception {
		Berserk b = activeBerserk();
		hero.HP = 0;
		Regeneration regen = new Regeneration();
		regen.attachTo(hero);
		field(regen, Regeneration.class, "partialRegen", 2.5f);
		regen.act();
		assertEquals(1, hero.HP);
		assertEquals(1, hero.HT);
		assertSame(b, hero.buff(Berserk.class));
	}

	@Test public void starvationBypassesShieldsInvulnerabilityAndStasis() {
		TestShield shield = new TestShield(100);
		shield.attachTo(hero);
		new Invulnerability().attachTo(hero);
		new TimeStasis().attachTo(hero);
		Hunger source = new Hunger();
		hero.damage(1, source);
		assertEquals(0, hero.HP);
		assertEquals(1, hero.deaths);
		assertSame(source, hero.cause);
		assertEquals("combat shields are bypassed, not consumed", 100, shield.shielding());
		hero.damage(1, source);
		assertEquals("a dead hero cannot die a second time", 1, hero.deaths);
	}

	private Berserk activeBerserk() throws Exception {
		Berserk b = new Berserk();
		Field state = Berserk.class.getDeclaredField("state");
		state.setAccessible(true);
		@SuppressWarnings({"unchecked", "rawtypes"})
		Object active = Enum.valueOf((Class)state.getType(), "BERSERK");
		state.set(b, active);
		b.setShield(20);
		b.attachTo(hero);
		return b;
	}

	@Test public void starvationOverridesAnActiveDeathlessFury() throws Exception {
		activeBerserk();
		hero.HP = 0;
		assertTrue("ordinary deathless combat survival is retained", hero.isAlive());
		hero.damage(1, new Hunger());
		assertEquals(1, hero.deaths);
		assertFalse(hero.isAlive());
		assertNull(hero.buff(Berserk.class));
	}

	@Test public void combatStillUsesShielding() {
		TestShield shield = new TestShield(20);
		shield.attachTo(hero);
		hero.damage(1, new Object());
		assertEquals(19, shield.shielding());
		assertEquals(1, hero.HP);
		assertEquals(0, hero.deaths);
	}

	@Test public void aNormalHungerTickAtTheThresholdIsLethal() throws Exception {
		Hunger h = hunger(Hunger.STARVING - 1, 0);
		h.act();
		assertEquals(1, hero.deaths);
		assertSame(h, hero.cause);
		assertTrue(h.isStarving());
	}

	@Test public void partialDamageAccumulatesOnItsExistingSchedule() throws Exception {
		Hunger h = hunger(Hunger.STARVING, 0);
		h.act();
		assertEquals(0, hero.deaths);
		field(h, Hunger.class, "partialDamage", 0.9998f);
		h.act();
		assertEquals(1, hero.deaths);
	}

	@Test public void directHungerChangeCausesOnlyOneDeathEvenWhenBothDamageSitesApply() throws Exception {
		Hunger h = hunger(Hunger.STARVING - 1, 1.1f);
		hero.revive = true;
		h.affectHunger(-2);
		assertEquals(1, hero.deaths);
		assertEquals(1, hero.HP);
		assertTrue("blessed revival does not grant a new hunger reset", h.isStarving());
	}

	@Test public void hungerDoesNotOverwriteAResurrectionCallback() throws Exception {
		Hunger h = hunger(Hunger.STARVING - 1, 0);
		hero.revive = true;
		hero.resetHunger = true;
		h.act();
		assertEquals(1, hero.deaths);
		assertEquals(0, h.hunger());
	}

	@Test public void existingHungerPausesArePreserved() throws Exception {
		Hunger h = hunger(Hunger.STARVING - 1, 0);
		Dungeon.level.locked = true;
		h.act();
		Dungeon.level.locked = false;
		Buff.affect(hero, WellFed.class);
		h.act();
		Buff.detach(hero, WellFed.class);
		Buff.affect(hero, ScrollOfChallenge.ChallengeArena.class);
		h.act();
		Buff.detach(hero, ScrollOfChallenge.ChallengeArena.class);
		SPDSettings.intro(true);
		h.act();
		assertEquals(0, hero.deaths);
		assertEquals((int)Hunger.STARVING - 1, h.hunger());
	}

	@Test public void otherChallengesKeepTheirFlagsAndCanCombineWithGetFucked() {
		assertEquals(512, Challenges.GET_FUCKED);
		assertEquals(9, Challenges.activeChallenges(511));
		assertEquals(10, Challenges.activeChallenges(Challenges.MAX_VALUE));
		SPDSettings.challenges(Challenges.MAX_VALUE);
		assertEquals(Challenges.MAX_VALUE, SPDSettings.challenges());
		Dungeon.challenges = Challenges.MAX_VALUE;
		hero.updateHT(true);
		assertEquals(1, hero.HT);
	}

	@Test public void ordinaryHealthAndShieldFirstStarvationAreUnchanged() {
		Dungeon.challenges = 0;
		hero = new TestHero();
		Dungeon.hero = hero;
		assertEquals(20, hero.HT);
		hero.lvl = 10;
		hero.updateHT(true);
		assertEquals(65, hero.HT);
		hero.restoreRevival(true);
		assertEquals(16, hero.HP);
		hero.restoreRevival(false);
		assertEquals(65, hero.HP);
		TestShield shield = new TestShield(5);
		shield.attachTo(hero);
		hero.damage(1, new Hunger());
		assertEquals(4, shield.shielding());
		assertEquals(65, hero.HP);
		assertEquals(0, hero.deaths);
	}

	@Test public void allClassesAndBossFloorsGenerateWithoutChangingTheHpCap() throws Exception {
		for (HeroClass c : HeroClass.values()) {
			assertEquals(26, LevelGenerationHarness.generate(12345L, Challenges.GET_FUCKED,
					c, LevelGenerationHarness.Context.REFERENCE, 26).size());
			assertEquals(1, Dungeon.hero.HT);
			assertEquals(1, Dungeon.hero.HP);
		}
		assertEquals(26, LevelGenerationHarness.generate(12345L, Challenges.MAX_VALUE,
				HeroClass.WARRIOR, LevelGenerationHarness.Context.REFERENCE, 26).size());
		assertEquals(1, Dungeon.hero.HT);
	}
}

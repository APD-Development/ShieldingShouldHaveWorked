package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/** Script mods, game triggers (work plan step a): hits, zaps, damage taken and kills reach mod effects as plain facts. */
public class ScriptTriggersTest {

	private Hero oldHero, hero;

	@Before public void setUp(){
		HeadlessTestSupport.start();
		ScriptModRuntime.clear();
		oldHero = Dungeon.hero;
		hero = new Hero();
		hero.HT = 50; hero.HP = 10;
		Dungeon.hero = hero;
	}

	@After public void tearDown(){
		ScriptModRuntime.clear();
		Dungeon.hero = oldHero;
	}

	static final String MOD =
			"APD.effect({ id: 'fx', name: 'Trigger Test', turns: 5,\n" +
			"  onMeleeHit(h, dmg, target, weapon){ if (dmg === 4 && target === 'rat' && weapon === 'sword') h.heal(1); },\n" +
			"  onThrownHit(h, dmg, target, weapon){ if (typeof dmg === 'number') h.heal(2); },\n" +
			"  onWandZap(h, wand){ if (wand === 'Wand of Test') h.heal(3); },\n" +
			"  onKill(h, target){ if (target === 'rat') h.heal(4); },\n" +
			"  onDamageTaken(h, dmg, source){ h.heal(5); } });\n";

	private ScriptEffect giveEffect(){
		ScriptModRuntime.LoadedMod m = ScriptModRuntime.load("test.triggers", MOD);
		assertFalse(m.disabledReason(), m.disabled());
		return ScriptEffect.give(hero, ScriptModRuntime.effect("test.triggers", "fx"), 5);
	}

	@Test public void eachTriggerGetsPlainFacts(){
		giveEffect();
		ScriptEffect.fire(hero, "onMeleeHit", 4.0, "rat", "sword");
		assertEquals(11, hero.HP);
		ScriptEffect.fire(hero, "onThrownHit", 2.0, "rat", "dart");
		assertEquals(13, hero.HP);
		ScriptEffect.fire(hero, "onWandZap", "Wand of Test");
		assertEquals(16, hero.HP);
		ScriptEffect.fire(hero, "onKill", "rat");
		assertEquals(20, hero.HP);
		ScriptEffect.fire(hero, "onDamageTaken", 3.0, "Rat");
		assertEquals(25, hero.HP);
	}

	@Test public void triggerWithoutHandlerOrEffectDoesNothing(){
		ScriptModRuntime.load("test.triggers", MOD);
		ScriptEffect.fire(hero, "onKill", "rat"); // no effect on the hero
		assertEquals(10, hero.HP);
		ScriptEffect.fire(null, "onKill", "rat");
		assertEquals(10, hero.HP);
	}

	@Test public void noActiveModMeansNoScriptRuns(){
		ScriptEffect.fire(hero, "onKill", "rat");
		assertEquals(10, hero.HP);
	}

	@Test public void eachEffectReactsOncePerTrigger(){
		ScriptModRuntime.LoadedMod m = ScriptModRuntime.load("test.once",
				"APD.effect({ id: 'fx', name: 'Once', turns: 5, onDamageTaken(h, dmg){ h.heal(1); } });");
		assertFalse(m.disabledReason(), m.disabled());
		ScriptEffect.give(hero, ScriptModRuntime.effect("test.once", "fx"), 5);
		ScriptEffect.give(hero, ScriptModRuntime.effect("test.once", "fx"), 5); // refresh, not a second copy
		ScriptEffect.fire(hero, "onDamageTaken", 1.0, "x");
		assertEquals(11, hero.HP);
	}

	@Test public void namesArePlainStrings(){
		assertEquals("", ScriptEffect.nameOf(null));
		assertEquals("String", ScriptEffect.nameOf(String.class));
	}
}

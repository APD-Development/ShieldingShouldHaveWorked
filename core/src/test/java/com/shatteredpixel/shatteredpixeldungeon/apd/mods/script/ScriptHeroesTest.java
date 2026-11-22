package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.apd.mods.HeroMod;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

/** Script mods, heroes hook (declarations): APD.hero is checked with the same rules as the old data-only hero mods. */
public class ScriptHeroesTest {
	@Before public void setUp(){ HeadlessTestSupport.start(); ScriptModRuntime.clear(); }
	@After public void tearDown(){ ScriptModRuntime.clear(); }

	static String tier(HeroClass hc, int tier){
		ArrayList<LinkedHashMap<Talent, Integer>> t = new ArrayList<>();
		Talent.initClassTalents(hc, t);
		StringBuilder b = new StringBuilder("[");
		for (Talent x : t.get(tier - 1).keySet()){ if (b.length() > 1) b.append(','); b.append('\'').append(x.name()).append('\''); }
		return b.append(']').toString();
	}

	static String hero(String extra){
		return "APD.item({ id: 'brew', kind: 'potion', name: 'Test Brew', onUse(h){ h.heal(1); } });\n"
				+ "APD.hero({ id: 'knight', name: 'Test Knight', description: 'A \"quoted\" knight', baseClass: 'warrior',"
				+ " hp: 24, str: 11, startingItems: [ { item: 'weapon.melee.Sword', level: 1 }, { modItem: 'brew', quantity: 2 } ],"
				+ " talents: { tier1: " + tier(HeroClass.WARRIOR, 1) + ", tier2: " + tier(HeroClass.WARRIOR, 2)
				+ ", tier3: " + tier(HeroClass.MAGE, 3) + " }, armorAbilities: ['HeroicLeap'],"
				+ " onStart(h){ h.message('hi'); }" + extra + " });\n";
	}

	@Test public void declaresAHeroCheckedLikeADataMod(){
		ScriptModRuntime.LoadedMod m = ScriptModRuntime.load("test.heroes", hero(""));
		assertFalse(m.disabledReason(), m.disabled());
		ScriptDefs.HeroDef d = ScriptModRuntime.hero("test.heroes", "knight");
		assertNotNull(d);
		assertEquals("test.heroes.knight", d.mod.id);
		assertEquals("Test Knight", d.mod.name);
		assertEquals("test.heroes", d.mod.author);
		assertEquals("A \"quoted\" knight", d.mod.description);
		assertEquals(HeroClass.WARRIOR, d.mod.baseClass);
		assertEquals(24, d.mod.hp); assertEquals(11, d.mod.str);
		assertEquals(1, d.mod.startingItems.size());
		assertEquals(Sword.class, d.mod.startingItems.get(0).type);
		assertEquals(1, d.mod.startingItems.get(0).level);
		assertEquals(Integer.valueOf(2), d.modItems.get("brew"));
		assertEquals(HeroicLeap.class, d.mod.armorAbilities.get(0));
		assertTrue(d.has("onStart")); assertFalse(d.has("onKill"));
		// the stored text rebuilds the same hero (saves keep it, like the old data mods)
		try { assertEquals(d.mod.name, HeroMod.parse(d.json).name); } catch (HeroMod.Invalid e){ fail(e.getMessage()); }
	}

	@Test public void spriteUsesAnExistingHeroLookAndSurvivesTheSave(){
		ScriptModRuntime.LoadedMod m = ScriptModRuntime.load("test.heroes", hero("").replace("baseClass: 'warrior',", "baseClass: 'warrior', sprite: 'mage',"));
		assertFalse(m.disabledReason(), m.disabled());
		ScriptDefs.HeroDef d = ScriptModRuntime.hero("test.heroes", "knight");
		assertEquals(HeroClass.WARRIOR, d.mod.baseClass);
		assertEquals(HeroClass.MAGE, d.mod.look);
		try { assertEquals(HeroClass.MAGE, HeroMod.parse(d.json).look); } catch (HeroMod.Invalid e){ fail(e.getMessage()); }
		// no sprite: the base class look, as before
		ScriptModRuntime.clear();
		ScriptModRuntime.load("test.heroes", hero(""));
		assertNull(ScriptModRuntime.hero("test.heroes", "knight").mod.look);
		// not an existing hero: the mod is turned off with a plain reason
		ScriptModRuntime.clear();
		ScriptModRuntime.LoadedMod bad = ScriptModRuntime.load("test.heroes", hero("").replace("baseClass: 'warrior',", "baseClass: 'warrior', sprite: 'RatSprite',"));
		assertTrue(bad.disabled());
		assertTrue(bad.disabledReason(), bad.disabledReason().contains("isn't an existing hero"));
		// the dungeon/avatar sprite follows the look only while that hero's run is active
		try {
			com.shatteredpixel.shatteredpixeldungeon.apd.mods.HeroModRun.active = d.mod;
			assertEquals(HeroClass.MAGE, com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite.lookClass(HeroClass.WARRIOR));
			com.shatteredpixel.shatteredpixeldungeon.apd.mods.HeroModRun.active = null;
			assertEquals(HeroClass.WARRIOR, com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite.lookClass(HeroClass.WARRIOR));
		} finally { com.shatteredpixel.shatteredpixeldungeon.apd.mods.HeroModRun.active = null; }
	}

	@Test public void mistakesTurnTheModOff(){
		String[][] bad = {
				{hero("").replace("'warrior'", "'wizard'"), "baseClass must be one of"},
				{hero("").replace("hp: 24", "hp: 5000"), "\"hp\" must be between 1 and 999"},
				{hero("").replace("['HeroicLeap']", "['Nope']"), "unknown armor ability"},
				{hero("").replace("'weapon.melee.Sword'", "'Amulet'"), "can't be a starting item"},
				{hero("").replace("modItem: 'brew'", "modItem: 'missing'"), "never declares"},
				{hero("").replace("{ modItem: 'brew', quantity: 2 }", "{ modItem: 'brew', item: 'Gold' }"), "exactly one of"},
				{hero(", onExplode(h){}"), "unknown trigger"},
				{hero("").replace("talents: {", "talents: { tier9: [], ").replace(tier(HeroClass.MAGE, 3), "['HEARTY_MEAL']"), "tier3"},
		};
		for (String[] b : bad){
			ScriptModRuntime.clear();
			ScriptModRuntime.LoadedMod m = ScriptModRuntime.load("test.heroes", b[0]);
			assertTrue(b[1], m.disabled());
			assertTrue(m.disabledReason() + " / wanted " + b[1], m.disabledReason().contains(b[1]));
			assertNull(ScriptModRuntime.hero("test.heroes", "knight"));
		}
	}

	@Test public void atMostEightHeroes(){
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < 9; i++) s.append(hero("").replaceFirst("APD.item\\(.*\\n", "").replace("'knight'", "'k" + i + "'").replace("modItem: 'brew'", "modItem: 'brew2'"));
		ScriptModRuntime.LoadedMod m = ScriptModRuntime.load("test.heroes", "APD.item({ id: 'brew2', kind: 'potion', name: 'B', onUse(h){} });\n" + s);
		assertTrue(m.disabled());
		assertTrue(m.disabledReason(), m.disabledReason().contains("more than 8 heroes"));
	}
}

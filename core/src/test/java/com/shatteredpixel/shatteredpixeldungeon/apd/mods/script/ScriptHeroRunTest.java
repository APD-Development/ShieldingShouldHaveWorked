package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.apd.mods.HeroModRun;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import static org.junit.Assert.*;

/** Script mods, heroes hook (run start): a picked script hero becomes the run's hero through HeroModRun, gets its
 *  mod items, runs onStart, and comes back from a save. A wrong class or a mod not picked gives a normal hero. */
public class ScriptHeroRunTest {
	@Rule public TemporaryFolder tmp = new TemporaryFolder();
	private Hero oldHero, hero;

	@Before public void setUp() throws Exception {
		HeadlessTestSupport.start();
		ScriptModRun.reset();
		HeroModRun.beginRun();
		File root = tmp.newFolder("scriptmods");
		File d = new File(root, "heroes"); assertTrue(d.mkdir());
		Files.write(new File(d, "mod.json").toPath(),
				"{\"format\":1,\"id\":\"test.heroes\",\"name\":\"Heroes\",\"author\":\"Tester\"}".getBytes(StandardCharsets.UTF_8));
		// onStart gives a shield, so the test can see it ran
		String script = ScriptHeroesTest.hero("").replace("onStart(h){ h.message('hi'); }", "onStart(h){ h.shield(7); }, onLevelUp(h, lvl){ h.shield(100 * lvl); }, onTurn(h){ h.shield(1); }, onKill(h, name){ h.shield(500); }");
		Files.write(new File(d, "main.js").toPath(), script.getBytes(StandardCharsets.UTF_8));
		ScriptModRun.dirOverride = root;
		oldHero = Dungeon.hero;
		hero = new Hero();
		hero.heroClass = HeroClass.WARRIOR;
		Dungeon.hero = hero;
	}

	@After public void tearDown(){
		ScriptModRun.reset();
		HeroModRun.beginRun();
		ScriptModRun.dirOverride = null;
		Dungeon.hero = oldHero;
	}

	private void pick(){
		ScriptModRun.choose(Collections.singletonList("test.heroes"));
		ScriptModRun.chooseHero("test.heroes", "knight");
	}

	private int count(String name){
		int n = 0;
		for (Item i : hero.belongings.backpack.items) if (i instanceof ScriptItem && name.equals(i.name())) n += i.quantity();
		return n;
	}

	@Test public void pickedScriptHeroBecomesTheRunsHero(){
		pick();
		ScriptModRun.beginRun(HeroClass.WARRIOR);
		assertNotNull(ScriptModRun.activeHero());
		assertNotNull(HeroModRun.active);
		assertEquals("Test Knight", HeroModRun.modName());
		assertEquals(24, HeroModRun.baseHT(20));
		assertTrue(HeroModRun.isModRun());
		ScriptModRun.applyStart(hero);
		assertEquals(2, count("Test Brew"));
		assertEquals(7, hero.shielding());
	}

	@Test public void wrongClassOrUnpickedModGivesANormalHero(){
		pick();
		ScriptModRun.beginRun(HeroClass.MAGE);
		assertNull(ScriptModRun.activeHero());
		assertNull(HeroModRun.active);

		HeroModRun.beginRun();
		ScriptModRun.chooseHero("test.heroes", "knight"); // mod itself not picked
		ScriptModRun.beginRun(HeroClass.WARRIOR);
		assertNull(ScriptModRun.activeHero());
		assertNull(HeroModRun.active);
		assertFalse(HeroModRun.isModRun());
	}

	@Test public void scriptHeroComesBackFromASave(){
		pick();
		ScriptModRun.beginRun(HeroClass.WARRIOR);
		Bundle b = new Bundle();
		HeroModRun.store(b);
		ScriptModRun.store(b);
		ScriptModRun.reset();
		HeroModRun.beginRun();
		assertNull(ScriptModRun.activeHero());
		HeroModRun.restore(b);
		ScriptModRun.restore(b);
		assertEquals("Test Knight", HeroModRun.modName());
		assertNotNull(ScriptModRun.activeHero());
		assertEquals("knight", ScriptModRun.activeHero().id);
	}

	@Test public void levelUpTurnAndKillTriggersRun(){
		pick();
		ScriptModRun.beginRun(HeroClass.WARRIOR);
		ScriptModRun.applyStart(hero);
		assertEquals(7, hero.shielding());
		ScriptModRun.HeroTurns turns = hero.buff(ScriptModRun.HeroTurns.class);
		assertNotNull("onTurn hero gets the turn ticker", turns);
		turns.act();
		assertEquals(8, hero.shielding());
		hero.earnExp(hero.maxExp(), null); // level 1 -> 2
		assertEquals(2, hero.lvl);
		assertEquals(208, hero.shielding());
		ScriptModRun.heroTrigger(hero, "onKill", "Rat");
		assertEquals(708, hero.shielding());
		// a normal hero: nothing runs
		ScriptModRun.reset();
		HeroModRun.beginRun();
		ScriptModRun.heroTrigger(hero, "onKill", "Rat");
		turns.act();
		assertEquals(708, hero.shielding());
	}

	@Test public void heroSelectPickFlow(){
		ScriptModLibrary lib = ScriptModLibrary.scan(ScriptModRun.dir());
		ScriptModLibrary.Installed inst = lib.find("test.heroes");
		assertNotNull(inst);
		// listing a mod's heroes runs its script outside any run
		assertEquals(1, ScriptModRun.heroesOf(inst).size());
		assertEquals("knight", ScriptModRun.heroesOf(inst).get(0).id);
		assertFalse(ScriptModRuntime.active());

		ScriptModRun.pickHero("test.heroes", "knight");
		assertTrue(ScriptModRun.isPicked("test.heroes")); // picking the hero turns its mod on
		assertTrue(ScriptModRun.isPickedHero("test.heroes", "knight"));
		ScriptModRun.confirmForStart(lib);
		ScriptModRun.beginRun(HeroClass.WARRIOR);
		assertNotNull(ScriptModRun.activeHero());

		// turning the mod off drops its hero
		HeroModRun.beginRun();
		ScriptModRun.togglePick("test.heroes");
		assertFalse(ScriptModRun.isPickedHero("test.heroes", "knight"));
		ScriptModRun.togglePick("test.heroes");
		ScriptModRun.confirmForStart(lib);
		ScriptModRun.beginRun(HeroClass.WARRIOR);
		assertTrue(ScriptModRun.isScriptRun());
		assertNull(ScriptModRun.activeHero());
	}
}

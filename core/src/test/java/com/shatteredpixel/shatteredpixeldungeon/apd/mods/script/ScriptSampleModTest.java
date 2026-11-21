package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/** Script mods step d: the dev-build sample mod (android/apdSampleMods) is valid and does what it says. */
public class ScriptSampleModTest {

	private Hero oldHero, hero;

	@Before public void setUp(){
		HeadlessTestSupport.start();
		ScriptModRuntime.clear();
		oldHero = Dungeon.hero;
		hero = new Hero();
		hero.HT = 50; hero.HP = 20;
		Dungeon.hero = hero;
	}

	@After public void tearDown(){
		ScriptModRuntime.clear();
		Dungeon.hero = oldHero;
	}

	static File samples(){
		// core tests run in core/src/main/assets; walk up to the repo root
		File dir = new File("").getAbsoluteFile();
		while (dir != null){
			File f = new File(dir, "android/apdSampleMods/apd/scriptmods");
			if (f.isDirectory()) return f;
			dir = dir.getParentFile();
		}
		fail("sample mod folder android/apdSampleMods not found");
		return null;
	}

	@Test public void sampleModLoadsAndItsItemsWork(){
		ScriptModLibrary lib = ScriptModLibrary.scan(samples());
		assertTrue(lib.refused.toString(), lib.refused.isEmpty());
		assertEquals(ScriptModRun.SAMPLES.length, lib.mods.size());
		ScriptModLibrary.Installed i = lib.find("sample.items");
		assertNotNull(i);
		ScriptModRuntime.LoadedMod m = ScriptModRuntime.load(i.id, i.script);
		assertFalse(m.disabledReason(), m.disabled());
		assertEquals(2, m.items().size());
		assertEquals("the sample also shows the monsters hook", 1, m.monsters().size());
		ScriptMob crab = new ScriptMob(ScriptModRuntime.monster("sample.items", "sample_crab"));
		assertEquals("Sample Crab", crab.name());
		assertTrue(crab.createLoot() instanceof ScriptItem);
		assertEquals(1, m.effects().size());
		assertEquals(2, ScriptModRuntime.startingItems().size());

		ScriptItem.use(hero, ScriptModRuntime.item("sample.items", "sample_potion"));
		assertEquals(25, hero.HP);
		assertNotNull(hero.buff(Barrier.class));
		assertEquals(5, hero.buff(Barrier.class).shielding());

		ScriptItem.use(hero, ScriptModRuntime.item("sample.items", "sample_scroll"));
		assertNotNull(hero.buff(Haste.class));
		ScriptEffect fx = hero.buff(ScriptEffect.class);
		assertNotNull(fx);
		assertEquals("Sample Effect", fx.name());
		for (int t = 0; t < 5; t++) fx.act();
		assertEquals(30, hero.HP);
		assertNull(hero.buff(ScriptEffect.class));
	}
}

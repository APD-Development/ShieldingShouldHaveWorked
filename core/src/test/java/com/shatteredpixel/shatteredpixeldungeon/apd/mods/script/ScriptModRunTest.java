package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.apd.mods.HeroModRun;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/** Script mods step c (core): picked mods load at run start, are saved by id, re-read on load, and make a mod run. */
public class ScriptModRunTest {

	@Rule public TemporaryFolder tmp = new TemporaryFolder();
	private Hero oldHero, hero;

	static final String SCRIPT = "APD.item({ id: 'kit', kind: 'stone', name: 'Kit Stone', startCount: 2, dropWeight: 0,\n" +
			"  onUse(h){ h.heal(1); } });";

	@Before public void setUp() throws Exception {
		HeadlessTestSupport.start();
		ScriptModRun.reset();
		File root = tmp.newFolder("scriptmods");
		File d = new File(root, "kit"); assertTrue(d.mkdir());
		Files.write(new File(d, "mod.json").toPath(),
				"{\"format\":1,\"id\":\"test.kit\",\"name\":\"Kit\",\"author\":\"Tester\"}".getBytes(StandardCharsets.UTF_8));
		Files.write(new File(d, "main.js").toPath(), SCRIPT.getBytes(StandardCharsets.UTF_8));
		ScriptModRun.dirOverride = root;
		oldHero = Dungeon.hero;
		hero = new Hero();
		Dungeon.hero = hero;
	}

	@After public void tearDown(){
		ScriptModRun.reset();
		ScriptModRun.dirOverride = null;
		Dungeon.hero = oldHero;
	}

	@Test public void normalRunHasNoScriptMods(){
		ScriptModRun.beginRun();
		assertFalse(ScriptModRun.isScriptRun());
		assertFalse(ScriptModRuntime.active());
		assertFalse(HeroModRun.isModRun());
	}

	@Test public void pickedModLoadsAndMakesAModRun(){
		ScriptModRun.choose(Collections.singletonList("test.kit"));
		ScriptModRun.beginRun();
		assertTrue(ScriptModRun.isScriptRun());
		assertTrue(ScriptModRuntime.active());
		assertNotNull(ScriptModRuntime.item("test.kit", "kit"));
		assertTrue(ScriptModRun.missingIds().isEmpty());
		assertTrue(HeroModRun.isModRun());
		// the pick is used once: the next new run is normal again
		ScriptModRun.beginRun();
		assertFalse(ScriptModRun.isScriptRun());
	}

	@Test public void startingKitIsGiven(){
		ScriptModRun.choose(Collections.singletonList("test.kit"));
		ScriptModRun.beginRun();
		assertEquals(1, ScriptModRuntime.startingItems().size());
		assertEquals(2, ScriptModRuntime.startingItems().get(0).quantity());
	}

	@Test public void savedIdsAreReReadOnLoad(){
		ScriptModRun.choose(Collections.singletonList("test.kit"));
		ScriptModRun.beginRun();
		Bundle b = new Bundle();
		ScriptModRun.store(b);
		ScriptModRun.reset();
		assertFalse(ScriptModRuntime.active());
		ScriptModRun.restore(b);
		assertEquals(Collections.singletonList("test.kit"), ScriptModRun.activeIds());
		assertNotNull(ScriptModRuntime.item("test.kit", "kit"));
	}

	@Test public void normalSaveLoadsAsNormalRun(){
		ScriptModRun.choose(Collections.singletonList("test.kit"));
		ScriptModRun.beginRun();
		ScriptModRun.restore(new Bundle());
		assertFalse(ScriptModRun.isScriptRun());
		assertFalse(ScriptModRuntime.active());
		Bundle b = new Bundle();
		ScriptModRun.store(b);
		assertFalse(b.contains("apd_script_mods"));
	}

	@Test public void uninstalledModIsMissingButStillAModRun(){
		ScriptModRun.choose(Arrays.asList("test.kit", "gone.mod", "test.kit"));
		ScriptModRun.beginRun();
		assertEquals(Arrays.asList("test.kit", "gone.mod"), ScriptModRun.activeIds());
		assertEquals(Collections.singletonList("gone.mod"), ScriptModRun.missingIds());
		assertTrue(HeroModRun.isModRun());
	}

	@Test public void cancellingThePickKeepsTheRunNormal(){
		ScriptModRun.choose(Collections.singletonList("test.kit"));
		HeroModRun.cancelStarting();
		ScriptModRun.beginRun();
		assertFalse(ScriptModRun.isScriptRun());
	}
}

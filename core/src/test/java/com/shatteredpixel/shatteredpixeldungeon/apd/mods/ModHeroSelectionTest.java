package com.shatteredpixel.shatteredpixeldungeon.apd.mods;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class ModHeroSelectionTest {
	@Rule public TemporaryFolder tmp = new TemporaryFolder();

	@After public void reset(){ ModHeroSelection.clear(); HeroModRun.active = null; }

	ModLibrary.Installed installed() throws Exception {
		File root = tmp.newFolder("mods");
		File d = new File(root, "a"); d.mkdirs();
		Files.write(new File(d, "hero.json").toPath(), HeroModTest.hero(null).getBytes(StandardCharsets.UTF_8));
		Files.write(new File(d, "sprite.png").toPath(), ModLibraryTest.pngHeader(256, 128));
		ModLibrary lib = ModHeroSelection.scan(root);
		assertEquals(1, lib.heroes.size());
		return lib.heroes.get(0);
	}

	@Test public void pickingAModQueuesItsExactJson_andTheRunStartsAsTheMod() throws Exception {
		ModLibrary.Installed m = installed();
		assertEquals(HeroClass.WARRIOR, ModHeroSelection.pick(m));
		assertTrue(m.json.contains(m.mod.id));
		ModHeroSelection.confirmForStart(HeroClass.WARRIOR);
		HeroModRun.beginRun();
		assertTrue(HeroModRun.isModRun());
		assertEquals(m.mod.id, HeroModRun.active.id);
		assertNull(ModHeroSelection.picked());
	}

	@Test public void switchingToAnotherClassOrClearingMeansANormalRun() throws Exception {
		ModLibrary.Installed m = installed();
		ModHeroSelection.pick(m);
		ModHeroSelection.confirmForStart(HeroClass.MAGE);
		HeroModRun.beginRun();
		assertFalse(HeroModRun.isModRun());

		ModHeroSelection.pick(m);
		ModHeroSelection.clear();
		ModHeroSelection.confirmForStart(HeroClass.WARRIOR);
		HeroModRun.beginRun();
		assertFalse(HeroModRun.isModRun());
	}

	@Test public void missingModIsBlocked_unlockedOneIsNot() throws Exception {
		assertNotNull(ModHeroSelection.blockedReason(null, true));
		assertNull(ModHeroSelection.blockedReason(installed(), true));
	}
}

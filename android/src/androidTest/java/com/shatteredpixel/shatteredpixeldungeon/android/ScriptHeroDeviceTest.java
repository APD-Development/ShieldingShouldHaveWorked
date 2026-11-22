package com.shatteredpixel.shatteredpixeldungeon.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.shatteredpixel.shatteredpixeldungeon.apd.mods.HeroModRun;
import com.shatteredpixel.shatteredpixeldungeon.apd.mods.script.ScriptDefs;
import com.shatteredpixel.shatteredpixeldungeon.apd.mods.script.ScriptModRuntime;
import com.watabou.utils.Bundle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/** Script mods, heroes hook on real Android: the sample mod's hero is checked (talents, abilities) and its hero
 *  record survives a real save write/read. */
@RunWith(AndroidJUnit4.class)
public class ScriptHeroDeviceTest {

	private static String asset(String path) throws Exception {
		try (InputStream in = InstrumentationRegistry.getInstrumentation().getContext().getAssets().open(path)){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[4096]; int n;
			while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
			return new String(out.toByteArray(), StandardCharsets.UTF_8);
		}
	}

	@Before public void setUp(){ ScriptModRuntime.clear(); HeroModRun.beginRun(); }
	@After public void tearDown(){ ScriptModRuntime.clear(); HeroModRun.beginRun(); }

	@Test public void sampleHeroLoadsAndSurvivesASave() throws Exception {
		ScriptModRuntime.LoadedMod m = ScriptModRuntime.load("sample.items", asset("apd/scriptmods/sample.items/main.js"));
		assertFalse(m.disabledReason(), m.disabled());
		ScriptDefs.HeroDef d = ScriptModRuntime.hero("sample.items", "sample_hero");
		assertNotNull(d);
		assertEquals("Sample Hero", d.mod.name);
		assertEquals(HeroClass.WARRIOR, d.mod.baseClass);
		assertEquals(HeroicLeap.class, d.mod.armorAbilities.get(0));
		assertEquals(3, d.mod.talentTiers.size());

		assertTrue(HeroModRun.activateScriptHero(d.mod, d.json));
		Bundle b = new Bundle();
		HeroModRun.store(b);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		assertTrue(Bundle.write(b, out));
		HeroModRun.beginRun();
		assertNull(HeroModRun.modName());
		HeroModRun.restore(Bundle.read(new ByteArrayInputStream(out.toByteArray())));
		assertEquals("Sample Hero", HeroModRun.modName());
		assertEquals(22, HeroModRun.baseHT(20));
	}
}

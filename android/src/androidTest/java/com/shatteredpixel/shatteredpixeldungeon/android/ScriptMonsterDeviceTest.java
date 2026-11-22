package com.shatteredpixel.shatteredpixeldungeon.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.apd.mods.script.ScriptDefs;
import com.shatteredpixel.shatteredpixeldungeon.apd.mods.script.ScriptMob;
import com.shatteredpixel.shatteredpixeldungeon.apd.mods.script.ScriptModRuntime;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrabSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/** Script mods, monsters hook on real Android: the sample mod's monster spawns, and survives a save. */
@RunWith(AndroidJUnit4.class)
public class ScriptMonsterDeviceTest {
	private Hero oldHero;

	private static String asset(String path) throws Exception {
		try (InputStream in = InstrumentationRegistry.getInstrumentation().getContext().getAssets().open(path)){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[4096]; int n;
			while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
			return new String(out.toByteArray(), StandardCharsets.UTF_8);
		}
	}

	@Before public void setUp(){ ScriptModRuntime.clear(); oldHero = Dungeon.hero; Dungeon.hero = new Hero(); }
	@After public void tearDown(){ ScriptModRuntime.clear(); Dungeon.hero = oldHero; }

	@Test public void sampleMonsterSpawnsAndSurvivesASave() throws Exception {
		ScriptModRuntime.LoadedMod m = ScriptModRuntime.load("sample.items", asset("apd/scriptmods/sample.items/main.js"));
		assertFalse(m.disabledReason(), m.disabled());
		ScriptDefs.MonsterDef d = ScriptModRuntime.monster("sample.items", "sample_crab");
		assertNotNull(d);
		assertEquals(0, ScriptModRuntime.monsterShare(11), 0);
		assertTrue(ScriptModRuntime.monsterShare(5) > 0);

		ScriptMob spawned = null;
		Random.pushGenerator(5);
		try {
			for (int i = 0; i < 500 && spawned == null; i++) spawned = ScriptModRuntime.maybeSpawn(5);
		} finally { Random.popGenerator(); }
		assertNotNull("sample crab never spawned on floor 5", spawned);
		assertEquals("Sample Crab", spawned.name());
		assertEquals(CrabSprite.class, spawned.spriteClass);

		spawned.HP = 11;

		Bundle b = new Bundle();
		b.put("mob", spawned);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		assertTrue(Bundle.write(b, out));
		Bundle back = Bundle.read(new ByteArrayInputStream(out.toByteArray()));
		ScriptMob restored = (ScriptMob) back.get("mob");
		assertEquals("Sample Crab", restored.name());
		assertEquals(11, restored.HP);
		assertEquals(CrabSprite.class, restored.spriteClass);

		ScriptModRuntime.clear();
		ScriptMob orphan = (ScriptMob) Bundle.read(new ByteArrayInputStream(out.toByteArray())).get("mob");
		assertEquals("missing mod: monster is inert, no crash", "mod monster", orphan.name());
	}
}

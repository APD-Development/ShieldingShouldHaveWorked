package com.shatteredpixel.shatteredpixeldungeon.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.shatteredpixel.shatteredpixeldungeon.apd.mods.script.ScriptModRuntime;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/** Script mods step d: the dev-build sample mod loads and declares its items and effect on real Android. */
@RunWith(AndroidJUnit4.class)
public class ScriptSampleModDeviceTest {

	private static String asset(String path) throws Exception {
		try (InputStream in = InstrumentationRegistry.getInstrumentation().getContext().getAssets().open(path)){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[4096]; int n;
			while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
			return new String(out.toByteArray(), StandardCharsets.UTF_8);
		}
	}

	@After public void tearDown(){ ScriptModRuntime.clear(); }

	@Test public void sampleModLoadsOnAndroid() throws Exception {
		assertTrue(asset("apd/scriptmods/sample.items/mod.json").contains("\"sample.items\""));
		ScriptModRuntime.LoadedMod m = ScriptModRuntime.load("sample.items", asset("apd/scriptmods/sample.items/main.js"));
		assertFalse(m.disabledReason(), m.disabled());
		assertEquals(2, m.items().size());
		assertEquals(1, m.effects().size());
		assertNotNull(ScriptModRuntime.item("sample.items", "sample_potion"));
		assertNotNull(ScriptModRuntime.item("sample.items", "sample_scroll"));
		assertNotNull(ScriptModRuntime.effect("sample.items", "sample_regen"));
	}
}

package com.shatteredpixel.shatteredpixeldungeon.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.shatteredpixel.shatteredpixeldungeon.apd.mods.script.ScriptSandbox;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Script mods step 1 (Chris, Nov 21): proves Rhino runs, sandboxed and limited, on real Android (API 26 and newer). */
@RunWith(AndroidJUnit4.class)
public class ScriptSandboxDeviceTest {
	private static String stopped(ScriptSandbox s, String fn){
		try { s.call(fn); fail("expected the call to be stopped"); }
		catch (ScriptSandbox.ScriptFailure f){ return f.getMessage(); }
		return null;
	}

	@Test public void rhinoRunsSandboxedAndLimitedOnAndroid() throws Exception {
		ScriptSandbox s = new ScriptSandbox("device.test");
		s.load("function add(a,b){ return a+b; }"
				+ " function noJava(){ return [typeof Packages, typeof java, typeof importClass, typeof eval].join(','); }"
				+ " function es6(){ const m = new Map(); m.set('k', [1,2,3].map(x => x*2)); let {a, b} = {a: 1, b: 2}; return `${m.get('k').join('-')}:${a+b}`; }");
		assertEquals(5.0, s.call("add", 2, 3));
		assertEquals("undefined,undefined,undefined,undefined", s.call("noJava"));
		assertEquals("2-4-6:3", s.call("es6"));

		ScriptSandbox loop = new ScriptSandbox("device.loop");
		loop.load("function spin(){ while(true){} }");
		long t0 = System.nanoTime();
		assertTrue(stopped(loop, "spin").contains("turned off"));
		assertTrue("endless loop stopped within 5 s on device", (System.nanoTime() - t0) < 5_000_000_000L);

		ScriptSandbox deep = new ScriptSandbox("device.deep");
		deep.load("function r(n){ return r(n+1)+1; }");
		assertTrue(stopped(deep, "r").contains("turned off"));

		ScriptSandbox big = new ScriptSandbox("device.big");
		big.load("function f(){ return 'x'.repeat(100000000).length; }");
		assertTrue(stopped(big, "f").contains("turned off"));

		// the good mod still works after the others were turned off
		assertEquals(7.0, s.call("add", 3, 4));
	}
}

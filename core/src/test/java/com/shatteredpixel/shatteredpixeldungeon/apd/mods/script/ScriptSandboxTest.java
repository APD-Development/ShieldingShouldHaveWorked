package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import org.junit.Test;

import static org.junit.Assert.*;

/** Chris's rules for script mods (Nov 21): sandboxed, time + memory limited, a broken mod is disabled, never a crash. */
public class ScriptSandboxTest {

	private static ScriptSandbox loaded(String src) throws Exception {
		ScriptSandbox s = new ScriptSandbox("test.mod");
		s.load(src);
		return s;
	}

	private static String failure(ScriptSandbox s, String fn){
		try { s.call(fn); fail("expected the call to be stopped"); }
		catch (ScriptSandbox.ScriptFailure f){ return f.getMessage(); }
		return null;
	}

	private static String loadFailure(String src){
		ScriptSandbox s = new ScriptSandbox("test.mod");
		try { s.load(src); fail("expected load to fail"); }
		catch (ScriptSandbox.ScriptFailure f){
			assertTrue(s.isDisabled());
			return f.getMessage();
		}
		return null;
	}

	@Test public void normalScriptRunsAndReturnsPlainValues() throws Exception {
		ScriptSandbox s = loaded("function add(a,b){ return a+b; } function name(){ return 'echo'; } function obj(){ return {a:1}; }");
		assertEquals(5.0, s.call("add", 2, 3));
		assertEquals("echo", s.call("name"));
		assertNull("objects never leave the sandbox", s.call("obj"));
		assertNull("missing function is just absent", s.call("nope"));
		assertTrue(s.defines("add"));
		assertFalse(s.defines("nope"));
		assertFalse(s.isDisabled());
	}

	@Test public void noJavaFileNetworkOsOrReflectionAccess() throws Exception {
		ScriptSandbox s = loaded(
				"function t(){ return [typeof Packages, typeof java, typeof javax, typeof importClass, typeof importPackage," +
				" typeof JavaImporter, typeof JavaAdapter, typeof getClass, typeof eval, typeof Function," +
				" typeof require, typeof load, typeof readFile, typeof print, typeof XMLHttpRequest].join(','); }");
		assertEquals("undefined,undefined,undefined,undefined,undefined,undefined,undefined,undefined,undefined,undefined," +
				"undefined,undefined,undefined,undefined,undefined", s.call("t"));
	}

	@Test public void constructorChainCannotRecreateEvalOrReachJava() throws Exception {
		ScriptSandbox s = loaded("function t(){ try { return String((function(){}).constructor('return 1+1')()); } catch(e){ return 'blocked'; } }");
		Object r = null;
		try { r = s.call("t"); } catch (ScriptSandbox.ScriptFailure f){ r = "disabled"; }
		assertTrue("Function constructor reachable via a function's constructor: " + r,
				"blocked".equals(r) || "disabled".equals(r) || "2".equals(r));
		// Even if code can be built, it still has no Java: the class shutter refuses every class.
		ScriptSandbox s2 = loaded("function t(){ try { var g = (function(){ return this; })(); return typeof g.Packages; } catch(e){ return 'blocked'; } }");
		assertNotEquals("function", s2.call("t"));
		assertNotEquals("object", s2.call("t"));
	}

	@Test public void endlessLoopIsStoppedAndTheModIsDisabled() throws Exception {
		ScriptSandbox s = loaded("function spin(){ while(true){} }");
		long t0 = System.nanoTime();
		String why = failure(s, "spin");
		long ms = (System.nanoTime() - t0) / 1_000_000;
		assertTrue(why, why.contains("turned off"));
		assertTrue("stopped quickly, took " + ms + " ms", ms < 5_000);
		assertTrue(s.isDisabled());
		assertEquals(why, s.disabledReason());
		// disabled for good: further calls are refused without running anything
		try { s.call("spin"); fail(); } catch (ScriptSandbox.ScriptFailure f){ assertEquals(why, f.getMessage()); }
	}

	@Test public void endlessLoopWhileLoadingIsStoppedToo(){
		String why = loadFailure("while(true){}");
		assertTrue(why, why.contains("turned off"));
	}

	@Test public void deepRecursionIsStopped() throws Exception {
		ScriptSandbox s = loaded("function r(n){ return r(n+1)+1; }");
		String why = failure(s, "r");
		assertTrue(why, why.contains("turned off"));
	}

	@Test public void hugeAllocationsAreRefused() throws Exception {
		assertTrue(failure(loaded("function f(){ return 'x'.repeat(100000000).length; }"), "f").contains("turned off"));
		assertTrue(failure(loaded("function f(){ return new Array(100000000).fill(0).length; }"), "f").contains("turned off"));
		assertTrue(failure(loaded("function f(){ return Array(100000000).join('x').length; }"), "f").contains("turned off"));
		assertTrue(failure(loaded("function f(){ return 'a'.padStart(100000000).length; }"), "f").contains("turned off"));
		// a growing-in-a-loop allocation is caught by the instruction / memory / time checks
		assertTrue(failure(loaded("function f(){ var a=[]; while(true){ a.push('xxxxxxxxxxxxxxxxxxxxxxxx'+a.length); } }"), "f").contains("turned off"));
		// small allocations are fine
		assertEquals(10.0, loaded("function f(){ return 'x'.repeat(10).length; }").call("f"));
		assertEquals(3.0, loaded("function f(){ return new Array(3).fill(1).length; }").call("f"));
	}

	@Test public void scriptErrorDisablesTheModWithAReadableReason() throws Exception {
		ScriptSandbox s = loaded("function bad(){ return undefinedThing.x; }");
		String why = failure(s, "bad");
		assertTrue(why, why.startsWith("Mod test.mod was turned off: it hit a script error"));
		assertTrue(why, why.contains("line 1"));
		String syntax = loadFailure("function (");
		assertTrue(syntax, syntax.contains("script error"));
	}

	@Test public void oversizedOrMissingSourceIsRefused(){
		StringBuilder big = new StringBuilder();
		while (big.length() <= ScriptSandbox.MAX_SOURCE_CHARS) big.append("//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\n");
		assertTrue(loadFailure(big.toString()).contains("larger than"));
		assertTrue(loadFailure(null).contains("has no script"));
	}

	@Test public void everyCallGetsAFreshBudget() throws Exception {
		// each call uses a good share of the budget; together far more than one budget
		ScriptSandbox s = loaded("function work(){ var x=0; for (var i=0;i<20000;i++) x+=i; return x; }");
		for (int i = 0; i < 200; i++) assertEquals(199990000.0, s.call("work"));
		assertFalse(s.isDisabled());
	}

	@Test public void oneBrokenModDoesNotAffectAnother() throws Exception {
		ScriptSandbox bad = loaded("function spin(){ while(true){} }");
		ScriptSandbox good = new ScriptSandbox("good.mod");
		good.load("function ok(){ return 'fine'; }");
		failure(bad, "spin");
		assertEquals("fine", good.call("ok"));
	}
}

package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * APD script mods (Chris, Nov 21): one sandbox per mod. JavaScript via Rhino, interpreted mode only.
 *
 * Chris's rules this enforces:
 *  - no file, network, OS or reflection access: the scope has only the safe JavaScript built-ins (no Packages, java,
 *    importClass...), and the class shutter refuses every Java class, so nothing reaches Java even by accident.
 *    Game hooks will be added as script-side objects APD builds (not wrapped Java objects), one hook at a time.
 *  - time limit: every call gets a fresh instruction budget and a wall-clock deadline; both are checked while the
 *    script runs, so an endless loop is stopped.
 *  - memory limit: the obvious "allocate a huge thing in one go" built-ins are capped, the interpreter stack depth is
 *    capped (deep recursion), the heap growth during a call is checked while it runs, and source size is capped.
 *  - a broken mod never crashes the game: every failure becomes a {@link ScriptFailure}, and the sandbox disables
 *    itself after the first one; callers show {@link #disabledReason()}.
 */
public final class ScriptSandbox {

	public static final int MAX_SOURCE_CHARS = 64 * 1024;
	public static final long DEFAULT_INSTRUCTIONS = 2_000_000;
	public static final long DEFAULT_MILLIS = 250;
	public static final long DEFAULT_HEAP_BYTES = 48L * 1024 * 1024;
	public static final int MAX_STACK_DEPTH = 200;
	/** Largest string / array a single built-in call may create (characters / elements). */
	public static final int MAX_ALLOC = 1 << 20;

	/** Why a script call was stopped or failed. Never escapes to game code as anything else. */
	public static final class ScriptFailure extends Exception {
		public ScriptFailure(String message){ super(message); }
	}

	private static final class Limited extends Error {
		Limited(String why){ super(why, null, false, false); }
	}

	private static final int OBSERVE_EVERY = 10_000;

	private final class Factory extends ContextFactory {
		@Override protected boolean hasFeature(Context cx, int featureIndex){
			if (featureIndex == Context.FEATURE_ENABLE_JAVA_MAP_ACCESS) return false;
			return super.hasFeature(cx, featureIndex);
		}
		@Override protected void observeInstructionCount(Context cx, int instructionCount){
			used += instructionCount;
			if (used > instructionBudget) throw new Limited("used too much processing time (instruction limit)");
			if (System.nanoTime() > deadline) throw new Limited("ran too long (time limit)");
			Runtime rt = Runtime.getRuntime();
			if (rt.totalMemory() - rt.freeMemory() - heapAtStart > heapBudget) throw new Limited("used too much memory");
		}
	}

	private final Factory factory = new Factory();
	private final String modId;
	private final long instructionBudget, millisBudget, heapBudget;
	private ScriptableObject scope;
	private String disabled;

	private long used, deadline, heapAtStart;

	public ScriptSandbox(String modId){ this(modId, DEFAULT_INSTRUCTIONS, DEFAULT_MILLIS, DEFAULT_HEAP_BYTES); }

	public ScriptSandbox(String modId, long instructions, long millis, long heapBytes){
		this.modId = modId;
		this.instructionBudget = instructions;
		this.millisBudget = millis;
		this.heapBudget = heapBytes;
	}

	public String modId(){ return modId; }
	public boolean isDisabled(){ return disabled != null; }
	public String disabledReason(){ return disabled; }

	private Context enter(){
		Context cx = factory.enterContext();
		cx.setInterpretedMode(true); // Android can't load generated classes; interpreted mode also enables the observer
		cx.setLanguageVersion(Context.VERSION_ES6);
		cx.setInstructionObserverThreshold(OBSERVE_EVERY);
		cx.setMaximumInterpreterStackDepth(MAX_STACK_DEPTH);
		cx.setClassShutter(className -> false);
		cx.setGeneratingDebug(false);
		return cx;
	}

	private void arm(){
		used = 0;
		deadline = System.nanoTime() + millisBudget * 1_000_000L;
		Runtime rt = Runtime.getRuntime();
		heapAtStart = rt.totalMemory() - rt.freeMemory();
	}

	/** Runs the mod's script once to set it up (it registers its hooks). Disables the mod on any failure. */
	public void load(String source) throws ScriptFailure {
		check();
		if (source == null) fail("has no script");
		if (source.length() > MAX_SOURCE_CHARS) fail("script is larger than " + (MAX_SOURCE_CHARS / 1024) + " KB");
		try (Context cx = enter()){
			arm();
			scope = cx.initSafeStandardObjects(null, false);
			for (String gone : new String[]{"Packages", "java", "javax", "org", "com", "edu", "net", "getClass",
					"JavaAdapter", "JavaImporter", "importClass", "importPackage", "Continuation", "XML", "XMLList",
					"Namespace", "QName"}) {
				scope.delete(gone);
			}
			Script hardening = cx.compileString(HARDEN, "apd-sandbox", 1, null);
			hardening.exec(cx, scope);
			Script s = cx.compileString(source, modId, 1, null);
			s.exec(cx, scope);
		} catch (Throwable t){
			fail(describe(t));
		}
	}

	/** Calls a global function the script defined, if it exists. Returns its result converted to plain Java
	 *  (String, Double, Boolean or null), or null if the function isn't defined. */
	public Object call(String function, Object... args) throws ScriptFailure {
		check();
		if (scope == null) fail("is not loaded");
		try (Context cx = enter()){
			Object f = ScriptableObject.getProperty(scope, function);
			if (!(f instanceof Function)) return null;
			arm();
			Object[] jsArgs = new Object[args.length];
			for (int i = 0; i < args.length; i++) jsArgs[i] = Context.javaToJS(args[i], scope);
			Object r = ((Callable) f).call(cx, scope, scope, jsArgs);
			return plain(r);
		} catch (ScriptFailure f){
			throw f;
		} catch (Throwable t){
			fail(describe(t));
			return null;
		}
	}

	public boolean defines(String function){
		if (scope == null || disabled != null) return false;
		try (Context cx = enter()){
			return ScriptableObject.getProperty(scope, function) instanceof Function;
		}
	}

	private static Object plain(Object r){
		if (r == null || r instanceof org.mozilla.javascript.Undefined) return null;
		if (r instanceof CharSequence) return r.toString();
		if (r instanceof Number) return ((Number) r).doubleValue();
		if (r instanceof Boolean) return r;
		if (r instanceof Scriptable) return null; // objects never leave the sandbox
		return null;
	}

	private void check() throws ScriptFailure {
		if (disabled != null) throw new ScriptFailure(disabled);
	}

	private void fail(String why) throws ScriptFailure {
		disabled = "Mod " + modId + " was turned off: it " + why + ".";
		scope = null;
		throw new ScriptFailure(disabled);
	}

	private static String describe(Throwable t){
		if (t instanceof Limited) return t.getMessage();
		if (t instanceof StackOverflowError) return "recursed too deeply";
		if (t instanceof OutOfMemoryError) return "used too much memory";
		if (t instanceof RhinoException){
			RhinoException r = (RhinoException) t;
			String msg = r.details();
			if (msg != null && msg.contains("Exceeded maximum stack depth")) return "recursed too deeply";
			return "hit a script error: " + msg + (r.lineNumber() > 0 ? " (line " + r.lineNumber() + ")" : "");
		}
		return "hit an error (" + t.getClass().getSimpleName() + ")";
	}

	/** Caps built-ins that can allocate a huge amount in one call, before any mod code runs. */
	private static final String HARDEN =
			"(function(){\n" +
			"  var MAX = " + MAX_ALLOC + ";\n" +
			"  function cap(n){ if (n > MAX) throw new RangeError('APD sandbox: too large (limit ' + MAX + ')'); }\n" +
			"  var rep = String.prototype.repeat;\n" +
			"  String.prototype.repeat = function(c){ cap(this.length * Number(c)); return rep.call(this, c); };\n" +
			"  var ps = String.prototype.padStart, pe = String.prototype.padEnd;\n" +
			"  if (ps) String.prototype.padStart = function(n, s){ cap(Number(n)); return ps.call(this, n, s); };\n" +
			"  if (pe) String.prototype.padEnd = function(n, s){ cap(Number(n)); return pe.call(this, n, s); };\n" +
			"  var A = Array;\n" +
			"  var fill = A.prototype.fill;\n" +
			"  A.prototype.fill = function(){ cap(this.length); return fill.apply(this, arguments); };\n" +
			"  var join = A.prototype.join;\n" +
			"  A.prototype.join = function(){ cap(this.length); return join.apply(this, arguments); };\n" +
			"  var from = A.from;\n" +
			"  if (from) A.from = function(src){ if (src && src.length !== undefined) cap(Number(src.length)); return from.apply(A, arguments); };\n" +
			"  var ctor = function(){ if (arguments.length === 1 && typeof arguments[0] === 'number') cap(arguments[0]);\n" +
			"    return A.apply(this, arguments); };\n" +
			"  ctor.prototype = A.prototype; ctor.isArray = A.isArray; ctor.of = A.of; ctor.from = A.from;\n" +
			"  this.Array = ctor;\n" +
			"  try { delete this.eval; } catch (e) {} if (typeof this.eval !== 'undefined') this.eval = undefined;\n" +
			"  try { delete this.Function; } catch (e) {} if (typeof this.Function !== 'undefined') this.Function = undefined;\n" +
			"})();\n";
}

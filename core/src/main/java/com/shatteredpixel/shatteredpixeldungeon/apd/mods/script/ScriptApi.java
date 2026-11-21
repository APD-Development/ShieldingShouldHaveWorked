package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

/**
 * Building blocks for the objects APD hands to scripts. They are plain JavaScript objects whose functions are
 * written in Java here; scripts can call those functions and nothing else (no Java object is ever wrapped).
 */
public final class ScriptApi {
	private ScriptApi(){}

	/** A JavaScript object built by APD for scripts. The only non-plain value allowed into a script. */
	public static class ApiObject extends ScriptableObject {
		private final String className;
		public ApiObject(Scriptable scope, String className){
			this.className = className;
			setParentScope(scope);
			setPrototype(ScriptableObject.getObjectPrototype(scope));
		}
		@Override public String getClassName(){ return className; }

		/** Adds a read-only function. */
		public ApiObject fn(String name, Body body){
			defineProperty(name, function(this, name, body), READONLY | PERMANENT);
			return this;
		}
		/** Adds a read-only plain value. */
		public ApiObject value(String name, Object v){
			defineProperty(name, v, READONLY | PERMANENT);
			return this;
		}
	}

	public interface Body { Object call(Object[] args); }

	public static BaseFunction function(Scriptable scope, String name, Body body){
		BaseFunction f = new BaseFunction(){
			@Override public Object call(Context cx, Scriptable s, Scriptable thisObj, Object[] args){
				Object r = body.call(args);
				return r == null ? Undefined.instance : r;
			}
			@Override public String getFunctionName(){ return name; }
			@Override public int getArity(){ return 0; }
		};
		ScriptRuntimeHelp.setup(f, scope);
		return f;
	}

	// ---- argument helpers (scripts can pass anything; APD only accepts what it expects) ----

	public static double num(Object[] args, int i, double fallback){
		if (i >= args.length || args[i] == null || args[i] instanceof Undefined) return fallback;
		double d = Context.toNumber(args[i]);
		return Double.isNaN(d) ? fallback : d;
	}

	/** A whole number clamped into [min, max]. */
	public static int intIn(Object[] args, int i, int fallback, int min, int max){
		double d = num(args, i, fallback);
		if (Double.isInfinite(d)) d = d > 0 ? max : min;
		long v = Math.round(d);
		return (int) Math.max(min, Math.min(max, v));
	}

	public static String str(Object[] args, int i, String fallback, int maxLen){
		if (i >= args.length || args[i] == null || args[i] instanceof Undefined) return fallback;
		String s = Context.toString(args[i]);
		s = s.replaceAll("[\\p{Cntrl}&&[^\n]]", "");
		return s.length() > maxLen ? s.substring(0, maxLen) : s;
	}

	/** Small helper so BaseFunction gets the right parent/prototype without wrapping Java. */
	static final class ScriptRuntimeHelp {
		static void setup(BaseFunction f, Scriptable scope){
			f.setParentScope(scope);
			f.setPrototype(ScriptableObject.getFunctionPrototype(scope));
		}
	}
}

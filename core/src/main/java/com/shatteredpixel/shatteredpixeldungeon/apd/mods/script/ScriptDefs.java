package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * What a script mod declares with APD.item({...}) and APD.effect({...}), read once, checked, and kept in Java.
 * The mod's own functions (onUse, triggers) stay inside its sandbox and are only ever called through it.
 */
public final class ScriptDefs {
	private ScriptDefs(){}

	public static final Pattern ID = Pattern.compile("[a-z0-9][a-z0-9_-]{0,31}");
	public static final int MAX_NAME = 40, MAX_DESC = 600;
	/** Drop weight cap per item: 3 = as common as a Potion of Healing in its category (Chris, Nov 21: a sane cap). */
	public static final double MAX_DROP_WEIGHT = 3;
	public static final int MAX_START = 5;
	public static final int MAX_ITEMS = 32, MAX_EFFECTS = 32;
	public static final int MAX_TURNS = 500;

	public enum Kind {
		POTION("drink", "DRINK"), SCROLL("read", "READ"), STONE("use", "USE");
		public final String verb, action;
		Kind(String verb, String action){ this.verb = verb; this.action = action; }
	}

	/** Effect triggers a mod can react to (wired into the game one by one; see ScriptModRuntime). */
	public static final String[] TRIGGERS = {"onStart", "onTurn", "onEnd", "onMeleeHit", "onThrownHit", "onWandZap",
			"onDamageTaken", "onKill"};

	public static final class ItemDef {
		public final String modId, id, name, description, sprite;
		public final Kind kind;
		/** 0 = never drops naturally. */
		public final double dropWeight;
		/** How many the hero starts a run with (0 = not in the starting kit). */
		public final int startCount;
		final Function onUse;
		ItemDef(String modId, String id, Kind kind, String name, String description, String sprite,
		        double dropWeight, int startCount, Function onUse){
			this.modId = modId; this.id = id; this.kind = kind; this.name = name; this.description = description;
			this.sprite = sprite; this.dropWeight = dropWeight; this.startCount = startCount; this.onUse = onUse;
		}
		public String key(){ return modId + ":" + id; }
	}

	public static final class EffectDef {
		public final String modId, id, name, description;
		public final int turns;
		final Map<String, Function> triggers;
		EffectDef(String modId, String id, String name, String description, int turns, Map<String, Function> triggers){
			this.modId = modId; this.id = id; this.name = name; this.description = description; this.turns = turns;
			this.triggers = Collections.unmodifiableMap(triggers);
		}
		public boolean has(String trigger){ return triggers.containsKey(trigger); }
		public String key(){ return modId + ":" + id; }
	}

	/** A definition the mod got wrong. The message is shown to the player; the mod is turned off. */
	public static final class BadDef extends RuntimeException {
		BadDef(String m){ super(m, null, false, false); }
	}

	static Object get(Scriptable o, String name){
		Object v = ScriptableObject.getProperty(o, name);
		return v == Scriptable.NOT_FOUND || v instanceof Undefined ? null : v;
	}

	static String text(Scriptable o, String name, boolean required, int max){
		Object v = get(o, name);
		if (v == null){
			if (required) throw new BadDef("is missing \"" + name + "\"");
			return null;
		}
		if (!(v instanceof CharSequence)) throw new BadDef("\"" + name + "\" must be text");
		String s = v.toString().replaceAll("[\\p{Cntrl}&&[^\n]]", "").trim();
		if (required && s.isEmpty()) throw new BadDef("\"" + name + "\" is empty");
		if (s.length() > max) throw new BadDef("\"" + name + "\" is longer than " + max + " characters");
		return s;
	}

	static String id(Scriptable o){
		String id = text(o, "id", true, 32);
		if (!ID.matcher(id).matches()) throw new BadDef("id \"" + id + "\" may only use a-z, 0-9, _ and -");
		return id;
	}

	static double number(Scriptable o, String name, double fallback, double min, double max){
		Object v = get(o, name);
		if (v == null) return fallback;
		if (!(v instanceof Number)) throw new BadDef("\"" + name + "\" must be a number");
		double d = Context.toNumber(v);
		if (Double.isNaN(d) || d < min || d > max) throw new BadDef("\"" + name + "\" must be between " + fmt(min) + " and " + fmt(max));
		return d;
	}

	static Function function(Scriptable o, String name, boolean required){
		Object v = get(o, name);
		if (v == null){
			if (required) throw new BadDef("is missing the function \"" + name + "\"");
			return null;
		}
		if (!(v instanceof Function)) throw new BadDef("\"" + name + "\" must be a function");
		return (Function) v;
	}

	private static String fmt(double d){ return d == Math.rint(d) ? String.valueOf((long) d) : String.valueOf(d); }

	static ItemDef item(String modId, Object arg){
		if (!(arg instanceof Scriptable) || arg instanceof Function) throw new BadDef("APD.item needs an object { id, kind, name, ... }");
		Scriptable o = (Scriptable) arg;
		String id = id(o);
		String kindText = text(o, "kind", true, 10);
		Kind kind;
		try { kind = Kind.valueOf(kindText.toUpperCase(java.util.Locale.ROOT)); }
		catch (IllegalArgumentException e){ throw new BadDef("item " + id + ": kind must be potion, scroll or stone"); }
		String name = text(o, "name", true, MAX_NAME);
		String desc = text(o, "description", false, MAX_DESC);
		String sprite = text(o, "sprite", false, 40);
		if (sprite != null && !ScriptSprites.allowed(kind, sprite))
			throw new BadDef("item " + id + ": sprite \"" + sprite + "\" isn't one of the " + kind.name().toLowerCase() + " sprites");
		double weight;
		int start;
		try {
			weight = number(o, "dropWeight", 0, 0, MAX_DROP_WEIGHT);
			start = (int) number(o, "startCount", 0, 0, MAX_START);
		} catch (BadDef b){ throw new BadDef("item " + id + ": " + b.getMessage()); }
		if (number(o, "startCount", 0, 0, MAX_START) != start) throw new BadDef("item " + id + ": startCount must be a whole number");
		Function onUse;
		try { onUse = function(o, "onUse", true); }
		catch (BadDef b){ throw new BadDef("item " + id + " " + b.getMessage()); }
		return new ItemDef(modId, id, kind, name, desc == null ? "" : desc, sprite, weight, start, onUse);
	}

	static EffectDef effect(String modId, Object arg){
		if (!(arg instanceof Scriptable) || arg instanceof Function) throw new BadDef("APD.effect needs an object { id, name, turns, ... }");
		Scriptable o = (Scriptable) arg;
		String id = id(o);
		String name = text(o, "name", true, MAX_NAME);
		String desc = text(o, "description", false, MAX_DESC);
		int turns;
		try {
			double t = number(o, "turns", 10, 1, MAX_TURNS);
			if (t != Math.rint(t)) throw new BadDef("\"turns\" must be a whole number");
			turns = (int) t;
		} catch (BadDef b){ throw new BadDef("effect " + id + ": " + b.getMessage()); }
		Map<String, Function> triggers = new LinkedHashMap<>();
		for (String t : TRIGGERS){
			Function f;
			try { f = function(o, t, false); }
			catch (BadDef b){ throw new BadDef("effect " + id + ": " + b.getMessage()); }
			if (f != null) triggers.put(t, f);
		}
		for (Object k : o.getIds()){
			String key = String.valueOf(k);
			if (key.startsWith("on") && !triggers.containsKey(key))
				throw new BadDef("effect " + id + ": unknown trigger \"" + key + "\"");
		}
		return new EffectDef(modId, id, name, desc == null ? "" : desc, turns, triggers);
	}
}

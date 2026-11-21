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

	// ---- monsters hook ----
	public static final int MAX_MONSTERS = 32;
	public static final String[] MONSTER_TRIGGERS = {"onSpawn", "onTurn", "onAttack", "onHurt", "onDeath"};
	/** Spawn weight cap per monster; all mod monsters together are capped too (ScriptModRuntime.MAX_MOD_SHARE). */
	public static final double MAX_SPAWN_WEIGHT = 3;

	public static final class MonsterDef {
		public final String modId, id, name, description, sprite, loot;
		public final int hp, attackMin, attackMax, accuracy, evasion, armor, xp, floorFrom, floorTo;
		public final double speed, spawnWeight, lootChance;
		final Map<String, Function> triggers;
		MonsterDef(String modId, String id, String name, String description, String sprite, int hp, int attackMin,
		           int attackMax, int accuracy, int evasion, int armor, double speed, int xp, int floorFrom, int floorTo,
		           double spawnWeight, String loot, double lootChance, Map<String, Function> triggers){
			this.modId = modId; this.id = id; this.name = name; this.description = description; this.sprite = sprite;
			this.hp = hp; this.attackMin = attackMin; this.attackMax = attackMax; this.accuracy = accuracy;
			this.evasion = evasion; this.armor = armor; this.speed = speed; this.xp = xp; this.floorFrom = floorFrom;
			this.floorTo = floorTo; this.spawnWeight = spawnWeight; this.loot = loot; this.lootChance = lootChance;
			this.triggers = Collections.unmodifiableMap(triggers);
		}
		public boolean has(String t){ return triggers.containsKey(t); }
		public boolean spawnsOn(int depth){ return spawnWeight > 0 && depth >= floorFrom && depth <= floorTo; }
		public String key(){ return modId + ":" + id; }
	}

	static int whole(Scriptable o, String name, int fallback, int min, int max){
		double d = number(o, name, fallback, min, max);
		if (d != Math.rint(d)) throw new BadDef("\"" + name + "\" must be a whole number");
		return (int) d;
	}

	static MonsterDef monster(String modId, Object arg){
		if (!(arg instanceof Scriptable) || arg instanceof Function) throw new BadDef("APD.monster needs an object { id, name, sprite, hp, ... }");
		Scriptable o = (Scriptable) arg;
		String id = id(o);
		try {
			String name = text(o, "name", true, MAX_NAME);
			String desc = text(o, "description", false, MAX_DESC);
			String sprite = text(o, "sprite", true, 40);
			if (!ScriptMobSprites.allowed(sprite)) throw new BadDef("sprite \"" + sprite + "\" isn't an existing monster sprite (e.g. RatSprite)");
			int hp = whole(o, "hp", 10, 1, 2000);
			int[] atk = pair(o, "attack", 1, 4, 0, 500);
			int acc = whole(o, "accuracy", 10, 0, 500);
			int eva = whole(o, "evasion", 2, 0, 500);
			int armor = whole(o, "armor", 0, 0, 200);
			double speed = number(o, "speed", 1, 0.5, 2);
			int xp = whole(o, "xp", 1, 0, 100);
			int from = 1, to = 0;
			double weight = 0;
			Object sp = get(o, "spawn");
			if (sp != null){
				if (!(sp instanceof Scriptable) || sp instanceof Function) throw new BadDef("\"spawn\" must be { floors: [from, to], weight }");
				Scriptable spawn = (Scriptable) sp;
				int[] floors = pair(spawn, "floors", 1, 1, 1, 999);
				from = floors[0]; to = floors[1];
				weight = number(spawn, "weight", 1, 0, MAX_SPAWN_WEIGHT);
			}
			String loot = text(o, "loot", false, 32);
			double lootChance = number(o, "lootChance", loot == null ? 0 : 0.25, 0, 1);
			Map<String, Function> triggers = new LinkedHashMap<>();
			for (String t : MONSTER_TRIGGERS){ Function f = function(o, t, false); if (f != null) triggers.put(t, f); }
			for (Object k : o.getIds()){
				String key = String.valueOf(k);
				if (key.startsWith("on") && !triggers.containsKey(key)) throw new BadDef("unknown trigger \"" + key + "\"");
			}
			return new MonsterDef(modId, id, name, desc == null ? "" : desc, sprite, hp, atk[0], atk[1], acc, eva, armor,
					speed, xp, from, to, weight, loot, lootChance, triggers);
		} catch (BadDef b){ throw new BadDef("monster " + id + ": " + b.getMessage()); }
	}

	/** [min, max] from a 2-number array, or one number for both. */
	static int[] pair(Scriptable o, String name, int dMin, int dMax, int lo, int hi){
		Object v = get(o, name);
		if (v == null) return new int[]{dMin, dMax};
		if (v instanceof Number){ int n = (int) whole(o, name, dMin, lo, hi); return new int[]{n, n}; }
		if (v instanceof org.mozilla.javascript.NativeArray){
			org.mozilla.javascript.NativeArray a = (org.mozilla.javascript.NativeArray) v;
			if (a.getLength() == 2 && a.get(0) instanceof Number && a.get(1) instanceof Number){
				double x = Context.toNumber(a.get(0)), y = Context.toNumber(a.get(1));
				if (x != Math.rint(x) || y != Math.rint(y) || x < lo || y > hi || x > y)
					throw new BadDef("\"" + name + "\" must be [min, max] whole numbers between " + lo + " and " + hi);
				return new int[]{(int) x, (int) y};
			}
		}
		throw new BadDef("\"" + name + "\" must be [min, max]");
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

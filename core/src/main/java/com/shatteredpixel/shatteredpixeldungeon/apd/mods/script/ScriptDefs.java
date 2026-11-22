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

	// ---- heroes hook ----
	public static final int MAX_HEROES = 8;
	public static final String[] HERO_TRIGGERS = {"onStart", "onLevelUp", "onTurn", "onKill"};

	/** A script hero: the checked HeroMod (same rules as the old data mods: existing base class, talents of the right
	 *  tier, existing armor abilities, existing starting items) plus mod items for its kit and its triggers. */
	public static final class HeroDef {
		public final String modId, id;
		public final com.shatteredpixel.shatteredpixeldungeon.apd.mods.HeroMod mod;
		/** The hero.json-style text stored in the save, so a reload rebuilds the same hero. */
		public final String json;
		/** Mod item ids (this mod's own) and how many the hero starts with. */
		public final Map<String, Integer> modItems;
		final Map<String, Function> triggers;
		HeroDef(String modId, String id, com.shatteredpixel.shatteredpixeldungeon.apd.mods.HeroMod mod, String json,
		        Map<String, Integer> modItems, Map<String, Function> triggers){
			this.modId = modId; this.id = id; this.mod = mod; this.json = json;
			this.modItems = Collections.unmodifiableMap(modItems); this.triggers = Collections.unmodifiableMap(triggers);
		}
		public boolean has(String t){ return triggers.containsKey(t); }
		public String key(){ return modId + ":" + id; }
	}

	static HeroDef hero(String modId, Object arg){
		if (!(arg instanceof Scriptable) || arg instanceof Function) throw new BadDef("APD.hero needs an object { id, name, baseClass, ... }");
		Scriptable o = (Scriptable) arg;
		String id = id(o);
		try {
			StringBuilder j = new StringBuilder("{\"format\":1");
			jp(j, "id", modId + "." + id);
			jp(j, "name", text(o, "name", true, MAX_NAME));
			String author = text(o, "author", false, 32);
			jp(j, "author", author == null || author.isEmpty() ? modId : author);
			String desc = text(o, "description", false, MAX_DESC);
			if (desc != null && !desc.isEmpty()) jp(j, "description", desc);
			jp(j, "baseClass", text(o, "baseClass", true, 16).toUpperCase(java.util.Locale.ROOT));
			String look = text(o, "sprite", false, 16);
			if (look != null && !look.isEmpty()) jp(j, "look", look.toUpperCase(java.util.Locale.ROOT));
			j.append(",\"hp\":").append(whole(o, "hp", 20, 1, 999));
			j.append(",\"str\":").append(whole(o, "str", 10, 1, 99));
			Map<String, Integer> modItems = new LinkedHashMap<>();
			j.append(",\"startingItems\":[");
			Object si = get(o, "startingItems");
			if (si != null){
				if (!(si instanceof org.mozilla.javascript.NativeArray)) throw new BadDef("\"startingItems\" must be a list");
				org.mozilla.javascript.NativeArray a = (org.mozilla.javascript.NativeArray) si;
				if (a.getLength() > com.shatteredpixel.shatteredpixeldungeon.apd.mods.HeroMod.MAX_ITEMS) throw new BadDef("at most " + com.shatteredpixel.shatteredpixeldungeon.apd.mods.HeroMod.MAX_ITEMS + " starting items");
				boolean first = true;
				for (int i = 0; i < a.getLength(); i++){
					Object e = a.get(i);
					if (!(e instanceof Scriptable) || e instanceof Function) throw new BadDef("each starting item must be { item } or { modItem }");
					Scriptable it = (Scriptable) e;
					String mi = text(it, "modItem", false, 32), gi = text(it, "item", false, 80);
					if ((mi == null) == (gi == null)) throw new BadDef("each starting item needs exactly one of \"item\" or \"modItem\"");
					int qty = whole(it, "quantity", 1, 1, 99);
					if (mi != null){
						if (modItems.containsKey(mi)) throw new BadDef("modItem " + mi + " is listed twice");
						modItems.put(mi, qty);
						continue;
					}
					if (!first) j.append(','); first = false;
					j.append("{"); jv(j, "item", gi); j.append(",\"level\":").append(whole(it, "level", 0, 0, 20));
					j.append(",\"quantity\":").append(qty).append('}');
				}
			}
			j.append(']');
			Object t = get(o, "talents");
			if (!(t instanceof Scriptable) || t instanceof Function) throw new BadDef("\"talents\" must be { tier1: [...], tier2: [...], tier3: [...] }");
			j.append(",\"talents\":{");
			for (int tier = 1; tier <= 3; tier++){
				if (tier > 1) j.append(',');
				j.append("\"tier").append(tier).append("\":");
				names(j, (Scriptable) t, "tier" + tier);
			}
			j.append('}');
			j.append(",\"armorAbilities\":");
			names(j, o, "armorAbilities");
			j.append('}');
			Map<String, Function> triggers = new LinkedHashMap<>();
			for (String tr : HERO_TRIGGERS){ Function f = function(o, tr, false); if (f != null) triggers.put(tr, f); }
			for (Object k : o.getIds()){
				String key = String.valueOf(k);
				if (key.startsWith("on") && !triggers.containsKey(key)) throw new BadDef("unknown trigger \"" + key + "\"");
			}
			String json = j.toString();
			com.shatteredpixel.shatteredpixeldungeon.apd.mods.HeroMod mod;
			try { mod = com.shatteredpixel.shatteredpixeldungeon.apd.mods.HeroMod.parse(json); }
			catch (com.shatteredpixel.shatteredpixeldungeon.apd.mods.HeroMod.Invalid x){
				String m = x.getMessage();
				if (m != null && m.startsWith("look ")) m = "sprite \"" + look + "\" isn't an existing hero (" + java.util.Arrays.toString(com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.values()) + ")";
				throw new BadDef(m);
			}
			return new HeroDef(modId, id, mod, json, modItems, triggers);
		} catch (BadDef b){ throw new BadDef("hero " + id + ": " + b.getMessage()); }
	}

	/** A list of names (talents, armor abilities) as a JSON array; HeroMod checks the names themselves. */
	private static void names(StringBuilder j, Scriptable o, String name){
		Object v = get(o, name);
		if (!(v instanceof org.mozilla.javascript.NativeArray)) throw new BadDef("\"" + name + "\" must be a list of names");
		org.mozilla.javascript.NativeArray a = (org.mozilla.javascript.NativeArray) v;
		if (a.getLength() > 16) throw new BadDef("\"" + name + "\" is too long");
		j.append('[');
		for (int i = 0; i < a.getLength(); i++){
			Object e = a.get(i);
			if (!(e instanceof CharSequence)) throw new BadDef("\"" + name + "\" must only hold names");
			if (i > 0) j.append(',');
			quote(j, e.toString().trim());
		}
		j.append(']');
	}

	private static void jp(StringBuilder j, String key, String value){ j.append(','); jv(j, key, value); }
	private static void jv(StringBuilder j, String key, String value){ quote(j, key); j.append(':'); quote(j, value); }
	private static void quote(StringBuilder j, String s){
		j.append('"');
		for (int i = 0; i < s.length(); i++){
			char c = s.charAt(i);
			if (c == '"' || c == '\\') j.append('\\').append(c);
			else if (c < 0x20 || c == '\u2028' || c == '\u2029') j.append(String.format("\\u%04x", (int) c));
			else j.append(c);
		}
		j.append('"');
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

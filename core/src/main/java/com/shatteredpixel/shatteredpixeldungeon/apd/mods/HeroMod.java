package com.shatteredpixel.shatteredpixeldungeon.apd.mods;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A modded hero (Chris, Nov 19: Brotato-workshop-style mods; "they need to be able to add heroes they want").
 * Mods are DATA ONLY (Google Play bans downloaded code): a hero is built from pieces that already exist in APD.
 * Anything that needs new code is submitted to Chris and ships inside an app update instead.
 *
 * File format (hero.json, format 1):
 * {
 *   "format": 1, "id": "author.hero-name", "name": "Shown name", "author": "Name",
 *   "description": "one paragraph",
 *   "baseClass": "WARRIOR",            // whose class engine/subclasses this hero runs on
 *   "hp": 20, "str": 10,               // starting max HP and strength
 *   "startingItems": [ {"item": "weapon.melee.Sword", "level": 0, "quantity": 1} ],
 *   "talents": { "tier1": [4 names], "tier2": [names], "tier3": [names] },  // any class's talents OF THAT TIER
 *   "armorAbilities": ["HeroicLeap", ...]                              // existing armor abilities
 * }
 * Parsing never runs mod code and never touches game state; it only checks the file against what exists.
 */
public final class HeroMod {

	public static final int FORMAT = 1;
	public static final int MAX_ITEMS = 12;

	public final String id, name, author, description;
	public final HeroClass baseClass;
	public final int hp, str;
	public final List<StartingItem> startingItems;
	public final List<List<Talent>> talentTiers; // index 0 = tier 1
	public final List<Class<? extends ArmorAbility>> armorAbilities;

	public static final class StartingItem {
		public final Class<? extends Item> type;
		public final int level, quantity;
		StartingItem(Class<? extends Item> type, int level, int quantity){ this.type = type; this.level = level; this.quantity = quantity; }
	}

	/** Why a mod file was refused, in words a modder can act on. */
	public static final class Invalid extends Exception {
		public Invalid(String why){ super(why); }
	}

	private HeroMod(String id, String name, String author, String description, HeroClass baseClass, int hp, int str,
	                List<StartingItem> items, List<List<Talent>> tiers, List<Class<? extends ArmorAbility>> abilities){
		this.id = id; this.name = name; this.author = author; this.description = description;
		this.baseClass = baseClass; this.hp = hp; this.str = str;
		this.startingItems = Collections.unmodifiableList(items);
		this.talentTiers = Collections.unmodifiableList(tiers);
		this.armorAbilities = Collections.unmodifiableList(abilities);
	}

	public static HeroMod parse(String json) throws Invalid {
		JsonValue v;
		try { v = new JsonReader().parse(json); } catch (RuntimeException e){ throw new Invalid("hero.json is not valid JSON"); }
		if (v == null || !v.isObject()) throw new Invalid("hero.json must be one JSON object");
		if (v.getInt("format", -1) != FORMAT) throw new Invalid("unsupported format (this version reads format " + FORMAT + ")");

		String id = text(v, "id", 64);
		if (!id.matches("[a-z0-9][a-z0-9._-]*")) throw new Invalid("id may only use a-z, 0-9, '.', '_' and '-'");
		String name = text(v, "name", 32), author = text(v, "author", 32);
		String description = v.has("description") ? text(v, "description", 600) : "";

		HeroClass base;
		try { base = HeroClass.valueOf(text(v, "baseClass", 16)); }
		catch (IllegalArgumentException e){ throw new Invalid("baseClass must be one of " + java.util.Arrays.toString(HeroClass.values())); }

		int hp = number(v, "hp", 1, 999), str = number(v, "str", 1, 99); // sanity bounds only; balance is the modder's call

		List<StartingItem> items = new ArrayList<>();
		JsonValue it = v.get("startingItems");
		if (it != null){
			if (!it.isArray()) throw new Invalid("startingItems must be a list");
			if (it.size > MAX_ITEMS) throw new Invalid("at most " + MAX_ITEMS + " starting items");
			for (JsonValue e : it) items.add(item(e));
		}

		List<List<Talent>> tiers = new ArrayList<>();
		JsonValue t = v.get("talents");
		if (t == null || !t.isObject()) throw new Invalid("talents must list tier1, tier2 and tier3");
		List<Set<Talent>> allowed = talentsByTier();
		for (int tier = 1; tier <= 3; tier++){
			JsonValue list = t.get("tier" + tier);
			if (list == null || !list.isArray()) throw new Invalid("talents.tier" + tier + " must be a list of talent names");
			int want = tierSize(base, tier);
			if (list.size != want) throw new Invalid("talents.tier" + tier + " needs exactly " + want + " talents for a " + base + " hero");
			LinkedHashSet<Talent> picked = new LinkedHashSet<>();
			for (JsonValue n : list){
				Talent tal;
				try { tal = Talent.valueOf(n.asString()); } catch (RuntimeException e){ throw new Invalid("unknown talent: " + n); }
				if (!allowed.get(tier - 1).contains(tal)) throw new Invalid(tal + " is not a tier " + tier + " talent");
				if (!picked.add(tal)) throw new Invalid(tal + " is listed twice");
			}
			tiers.add(new ArrayList<>(picked));
		}

		List<Class<? extends ArmorAbility>> abilities = new ArrayList<>();
		JsonValue ab = v.get("armorAbilities");
		if (ab == null || !ab.isArray() || ab.size == 0) throw new Invalid("armorAbilities must list at least one ability");
		for (JsonValue n : ab){
			Class<? extends ArmorAbility> a = ability(n.asString());
			if (abilities.contains(a)) throw new Invalid(n.asString() + " is listed twice");
			abilities.add(a);
		}
		if (abilities.size() > 3) throw new Invalid("at most 3 armor abilities");

		return new HeroMod(id, name, author, description, base, hp, str, items, tiers, abilities);
	}

	private static String text(JsonValue v, String key, int max) throws Invalid {
		JsonValue f = v.get(key);
		if (f == null || !f.isString() || f.asString().trim().isEmpty()) throw new Invalid(key + " is required");
		String s = f.asString().trim();
		if (s.length() > max) throw new Invalid(key + " is longer than " + max + " characters");
		return s;
	}

	private static int number(JsonValue v, String key, int min, int max) throws Invalid {
		JsonValue f = v.get(key);
		if (f == null || !f.isNumber() || f.asDouble() != Math.rint(f.asDouble())) throw new Invalid(key + " must be a whole number");
		int n = f.asInt();
		if (n < min || n > max) throw new Invalid(key + " must be between " + min + " and " + max);
		return n;
	}

	private static StartingItem item(JsonValue e) throws Invalid {
		if (!e.isObject()) throw new Invalid("each starting item must be an object");
		String path = text(e, "item", 80);
		if (!path.matches("[a-z]+(\\.[a-z]+)*\\.[A-Z][A-Za-z0-9]*")) throw new Invalid("bad item name: " + path);
		Class<?> c;
		try { c = Class.forName("com.shatteredpixel.shatteredpixeldungeon.items." + path); }
		catch (ClassNotFoundException x){ throw new Invalid("no such item: " + path); }
		if (!Item.class.isAssignableFrom(c) || java.lang.reflect.Modifier.isAbstract(c.getModifiers()))
			throw new Invalid(path + " is not an item that can be given");
		int level = e.has("level") ? number(e, "level", 0, 20) : 0;
		int qty = e.has("quantity") ? number(e, "quantity", 1, 99) : 1;
		@SuppressWarnings("unchecked") Class<? extends Item> ic = (Class<? extends Item>) c;
		return new StartingItem(ic, level, qty);
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends ArmorAbility> ability(String simple) throws Invalid {
		for (HeroClass hc : HeroClass.values()) for (ArmorAbility a : hc.armorAbilities())
			if (a.getClass().getSimpleName().equals(simple)) return (Class<? extends ArmorAbility>) a.getClass();
		throw new Invalid("unknown armor ability: " + simple);
	}

	/** Every existing talent, grouped by the tier it belongs to (across all classes). */
	static List<Set<Talent>> talentsByTier(){
		List<Set<Talent>> out = new ArrayList<>();
		for (int i = 0; i < 3; i++) out.add(new LinkedHashSet<>());
		for (HeroClass hc : HeroClass.values()){
			ArrayList<LinkedHashMap<Talent, Integer>> t = new ArrayList<>();
			Talent.initClassTalents(hc, t);
			for (int i = 0; i < 3 && i < t.size(); i++) out.get(i).addAll(t.get(i).keySet());
		}
		return out;
	}

	static int tierSize(HeroClass hc, int tier){
		ArrayList<LinkedHashMap<Talent, Integer>> t = new ArrayList<>();
		Talent.initClassTalents(hc, t);
		return t.get(tier - 1).size();
	}
}

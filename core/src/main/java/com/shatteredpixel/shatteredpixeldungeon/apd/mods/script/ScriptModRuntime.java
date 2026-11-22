package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import org.mozilla.javascript.Function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The script mods active in the current run, and the items/effects they declared.
 *
 * Mod items follow Chris's Nov 21 rule: each item chooses natural drops (dropWeight), starting kit (startCount) or
 * both. Natural drops share their category's loot pool with a capped weight, and all mod items together can never
 * take more than {@link #MAX_MOD_SHARE} of a category's drops, so one mod can't flood the loot pool.
 * When no script mod is active nothing here touches the game or its random numbers (normal seeds stay identical).
 */
public final class ScriptModRuntime {
	private ScriptModRuntime(){}

	/** All mod items together: at most this share of their category's drops. */
	public static final double MAX_MOD_SHARE = 0.25;

	public static final class LoadedMod {
		public final String id;
		final ScriptSandbox sandbox;
		final Map<String, ScriptDefs.ItemDef> items = new LinkedHashMap<>();
		final Map<String, ScriptDefs.EffectDef> effects = new LinkedHashMap<>();
		final Map<String, ScriptDefs.MonsterDef> monsters = new LinkedHashMap<>();
		final Map<String, ScriptDefs.HeroDef> heroes = new LinkedHashMap<>();
		public List<ScriptDefs.HeroDef> heroes(){ return new ArrayList<>(heroes.values()); }
		public List<ScriptDefs.MonsterDef> monsters(){ return new ArrayList<>(monsters.values()); }
		LoadedMod(String id, ScriptSandbox sandbox){ this.id = id; this.sandbox = sandbox; }
		public boolean disabled(){ return sandbox.isDisabled(); }
		public String disabledReason(){ return sandbox.disabledReason(); }
		public List<ScriptDefs.ItemDef> items(){ return new ArrayList<>(items.values()); }
		public List<ScriptDefs.EffectDef> effects(){ return new ArrayList<>(effects.values()); }
	}

	private static final Map<String, LoadedMod> mods = new LinkedHashMap<>();

	public static boolean active(){ return !mods.isEmpty(); }
	public static List<LoadedMod> mods(){ return Collections.unmodifiableList(new ArrayList<>(mods.values())); }
	public static LoadedMod mod(String id){ return mods.get(id); }
	public static void clear(){ mods.clear(); }

	/** Loads one mod's script. On any problem the mod is listed as turned off with the reason; never throws. */
	public static LoadedMod load(String modId, String source){
		ScriptSandbox sb = new ScriptSandbox(modId);
		LoadedMod m = new LoadedMod(modId, sb);
		mods.put(modId, m);
		try {
			sb.load(source, (cx, scope) -> {
				ScriptApi.ApiObject apd = new ScriptApi.ApiObject(scope, "APD");
				apd.fn("item", args -> {
					if (m.items.size() >= ScriptDefs.MAX_ITEMS) throw new ScriptDefs.BadDef("declares more than " + ScriptDefs.MAX_ITEMS + " items");
					ScriptDefs.ItemDef d = ScriptDefs.item(modId, args.length > 0 ? args[0] : null);
					if (m.items.containsKey(d.id)) throw new ScriptDefs.BadDef("declares item " + d.id + " twice");
					m.items.put(d.id, d);
					return null;
				});
				apd.fn("monster", args -> {
					if (m.monsters.size() >= ScriptDefs.MAX_MONSTERS) throw new ScriptDefs.BadDef("declares more than " + ScriptDefs.MAX_MONSTERS + " monsters");
					ScriptDefs.MonsterDef d = ScriptDefs.monster(modId, args.length > 0 ? args[0] : null);
					if (m.monsters.containsKey(d.id)) throw new ScriptDefs.BadDef("declares monster " + d.id + " twice");
					m.monsters.put(d.id, d);
					return null;
				});
				apd.fn("hero", args -> {
					if (m.heroes.size() >= ScriptDefs.MAX_HEROES) throw new ScriptDefs.BadDef("declares more than " + ScriptDefs.MAX_HEROES + " heroes");
					ScriptDefs.HeroDef d = ScriptDefs.hero(modId, args.length > 0 ? args[0] : null);
					if (m.heroes.containsKey(d.id)) throw new ScriptDefs.BadDef("declares hero " + d.id + " twice");
					m.heroes.put(d.id, d);
					return null;
				});
				apd.fn("effect", args -> {
					if (m.effects.size() >= ScriptDefs.MAX_EFFECTS) throw new ScriptDefs.BadDef("declares more than " + ScriptDefs.MAX_EFFECTS + " effects");
					ScriptDefs.EffectDef d = ScriptDefs.effect(modId, args.length > 0 ? args[0] : null);
					if (m.effects.containsKey(d.id)) throw new ScriptDefs.BadDef("declares effect " + d.id + " twice");
					m.effects.put(d.id, d);
					return null;
				});
				apd.value("version", 1);
				scope.put("APD", scope, apd);
			});
		} catch (ScriptSandbox.ScriptFailure f){
			// the sandbox recorded the reason and turned itself off
		}
		// a hero's kit may name the mod's own items, declared anywhere in the script: check once it has run
		if (!m.disabled()) for (ScriptDefs.HeroDef h : m.heroes.values()) for (String item : h.modItems.keySet())
			if (!m.items.containsKey(item)){ sb.refuse("has hero " + h.id + " starting with modItem " + item + ", which it never declares"); break; }
		return m;
	}

	public static ScriptDefs.ItemDef item(String modId, String itemId){
		LoadedMod m = mods.get(modId);
		return m == null ? null : m.items.get(itemId);
	}

	public static ScriptDefs.HeroDef hero(String modId, String heroId){
		LoadedMod m = mods.get(modId);
		return m == null || m.disabled() ? null : m.heroes.get(heroId);
	}

	public static ScriptDefs.MonsterDef monster(String modId, String monsterId){
		LoadedMod m = mods.get(modId);
		return m == null ? null : m.monsters.get(monsterId);
	}

	/** Mod monsters, chance per spawn: each eligible monster's weight against a normal floor's rotation (10 equal
	 *  slots), all together capped at MAX_MOD_SHARE. 0 when none can spawn on this floor. */
	public static final double ROTATION_WEIGHT = 10;

	public static double monsterShare(int depth){
		if (mods.isEmpty()) return 0;
		double w = 0;
		for (LoadedMod m : mods.values()) if (!m.disabled())
			for (ScriptDefs.MonsterDef d : m.monsters.values()) if (d.spawnsOn(depth)) w += d.spawnWeight;
		if (w <= 0) return 0;
		return Math.min(MAX_MOD_SHARE, w / (w + ROTATION_WEIGHT));
	}

	/** Called when a floor creates a monster. Returns a mod monster instead, or null for the normal one.
	 *  Uses no random numbers unless a mod monster can spawn on this floor (normal seeds stay identical). */
	public static ScriptMob maybeSpawn(int depth){
		double share = monsterShare(depth);
		if (share <= 0) return null;
		if (Random.Float() >= share) return null;
		List<ScriptDefs.MonsterDef> defs = new ArrayList<>();
		List<Float> weights = new ArrayList<>();
		for (LoadedMod m : mods.values()) if (!m.disabled())
			for (ScriptDefs.MonsterDef d : m.monsters.values()) if (d.spawnsOn(depth)){ defs.add(d); weights.add((float) d.spawnWeight); }
		float[] w = new float[weights.size()];
		for (int i = 0; i < w.length; i++) w[i] = weights.get(i);
		int pick = Random.chances(w);
		return pick < 0 ? null : new ScriptMob(defs.get(pick));
	}

	public static ScriptDefs.EffectDef effect(String modId, String effectId){
		LoadedMod m = mods.get(modId);
		return m == null ? null : m.effects.get(effectId);
	}

	/** Runs a mod function; a failure turns that mod off and tells the player, and never reaches the game. */
	static Object run(String modId, Function f, Object... args){
		LoadedMod m = mods.get(modId);
		if (m == null || f == null || m.disabled()) return null;
		try {
			return m.sandbox.invoke(f, args);
		} catch (ScriptSandbox.ScriptFailure e){
			GLog.w(e.getMessage());
			return null;
		}
	}

	static <T> T build(String modId, java.util.function.BiFunction<org.mozilla.javascript.Context, org.mozilla.javascript.Scriptable, T> maker){
		LoadedMod m = mods.get(modId);
		if (m == null || m.disabled()) return null;
		try { return m.sandbox.build(maker); }
		catch (ScriptSandbox.ScriptFailure e){ return null; }
	}

	// ---- loot ----

	static ScriptDefs.Kind kindOf(Generator.Category cat){
		switch (cat){
			case POTION: return ScriptDefs.Kind.POTION;
			case SCROLL: return ScriptDefs.Kind.SCROLL;
			case STONE: return ScriptDefs.Kind.STONE;
			default: return null;
		}
	}

	static float categoryWeight(Generator.Category cat){
		float total = 0;
		if (cat.defaultProbs != null) for (float p : cat.defaultProbs) total += p;
		return total;
	}

	/** Chance that one roll in this category becomes a mod item instead (0 when no mod item can drop there). */
	public static double modShare(Generator.Category cat){
		ScriptDefs.Kind kind = kindOf(cat);
		if (kind == null || mods.isEmpty()) return 0;
		double w = 0;
		for (LoadedMod m : mods.values()) if (!m.disabled())
			for (ScriptDefs.ItemDef d : m.items.values()) if (d.kind == kind) w += d.dropWeight;
		if (w <= 0) return 0;
		return Math.min(MAX_MOD_SHARE, w / (w + categoryWeight(cat)));
	}

	/** Called at the start of a category roll. Returns a mod item to drop instead, or null to roll normally.
	 *  Uses no random numbers at all unless a mod item can drop in this category. */
	public static Item maybeDrop(Generator.Category cat){
		double share = modShare(cat);
		if (share <= 0) return null;
		if (Random.Float() >= share) return null;
		ScriptDefs.Kind kind = kindOf(cat);
		List<ScriptDefs.ItemDef> defs = new ArrayList<>();
		List<Float> weights = new ArrayList<>();
		for (LoadedMod m : mods.values()) if (!m.disabled())
			for (ScriptDefs.ItemDef d : m.items.values()) if (d.kind == kind && d.dropWeight > 0){ defs.add(d); weights.add((float) d.dropWeight); }
		float[] w = new float[weights.size()];
		for (int i = 0; i < w.length; i++) w[i] = weights.get(i);
		int pick = Random.chances(w);
		return pick < 0 ? null : new ScriptItem(defs.get(pick));
	}

	/** The starting kit part: every active mod's items with startCount > 0. */
	public static List<Item> startingItems(){
		List<Item> out = new ArrayList<>();
		for (LoadedMod m : mods.values()) if (!m.disabled())
			for (ScriptDefs.ItemDef d : m.items.values()) if (d.startCount > 0) out.add(new ScriptItem(d).quantity(d.startCount));
		return out;
	}

	public static void giveStartingItems(Hero hero){
		for (Item i : startingItems()) i.collect(hero.belongings.backpack);
	}

	static Hero hero(){ return Dungeon.hero; }
}

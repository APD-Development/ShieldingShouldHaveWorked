package com.shatteredpixel.shatteredpixeldungeon.apd.mods;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The hero mod this run is played as (Chris, Nov 19). The mod's full hero.json is stored INSIDE the save, so a
 * run keeps working (and plays exactly the same) even if the mod is later changed or deleted.
 */
public final class HeroModRun {

	private HeroModRun(){}

	/** Set before a new run starts (hero select); taken by the first hero init. */
	public static volatile HeroMod starting;
	/** The mod of the run in progress, or null for a normal hero. */
	public static volatile HeroMod active;
	private static volatile String activeJson;

	private static final String NODE = "apd_mod_hero";

	/** Save held a mod record that no longer parses: still a mod run (stays out of badges/Community), raw record kept. */
	private static volatile boolean damaged;

	public static boolean isModRun(){ return active != null || damaged; }

	/** True when this run's saved mod record could not be read. */
	public static boolean isDamaged(){ return damaged; }

	/** The mod hero's name for a mod run (marks its local rankings entry), otherwise null. */
	public static String modName(){ HeroMod m = active; if (m == null) return damaged ? "Unreadable mod" : null; return m.name; }

	/** A run that must be a normal hero (a Community review replays the original setup): drop any pending mod. */
	public static void cancelStarting(){ starting = null; startingJson = null; }

	/** New run: take the pending mod (if any). A normal new run clears any old mod state. */
	public static void beginRun(){
		damaged = false;
		active = starting;
		activeJson = startingJson;
		starting = null; startingJson = null;
	}
	private static volatile String startingJson;

	public static void choose(HeroMod mod, String json){ starting = mod; startingJson = json; }

	/** Talent tiers 1-3 for this hero: the mod's picks for a mod run, otherwise null (use the class lists). */
	public static boolean fillTalents(ArrayList<LinkedHashMap<Talent, Integer>> talents){
		HeroMod m = active;
		if (m == null) return false;
		while (talents.size() < Talent.MAX_TALENT_TIERS) talents.add(new LinkedHashMap<>());
		for (int i = 0; i < m.talentTiers.size(); i++){
			LinkedHashMap<Talent, Integer> tier = talents.get(i);
			for (Talent t : m.talentTiers.get(i)) if (!tier.containsKey(t)) tier.put(t, 0);
		}
		return true;
	}

	/** Armor abilities offered to this hero. */
	public static ArmorAbility[] armorAbilities(HeroClass cls){
		HeroMod m = active;
		if (m == null) return cls.armorAbilities();
		ArmorAbility[] out = new ArmorAbility[m.armorAbilities.size()];
		for (int i = 0; i < out.length; i++) out[i] = Reflection.newInstance(m.armorAbilities.get(i));
		return out;
	}

	/** Base max HP at level 1 (20 for normal heroes). */
	public static int baseHT(int normal){
		HeroMod m = active;
		return m == null ? normal : m.hp;
	}

	/** After the class's normal start: the mod's HP, strength and extra starting items. */
	public static void applyStart(Hero hero){
		HeroMod m = active;
		if (m == null) return;
		hero.updateHT(false);
		hero.HP = hero.HT;
		hero.STR = m.str;
		for (HeroMod.StartingItem s : m.startingItems){
			for (Item it : startingItems(s)){
				// a full pack still never loses a starting item: it goes straight into the backpack
				if (!it.collect()) hero.belongings.backpack.items.add(it);
			}
		}
	}

	/**
	 * The items one starting-item entry gives: one stack for stackable items, otherwise "quantity" separate copies
	 * (a non-stackable item with quantity 3 used to silently give just one).
	 */
	static List<Item> startingItems(HeroMod.StartingItem s){
		List<Item> out = new ArrayList<>();
		Item first = make(s);
		// same rule as the class kit: an item the run's challenges block is not given (e.g. Dewdrop under No Herbalism)
		if (first == null || com.shatteredpixel.shatteredpixeldungeon.Challenges.isItemBlocked(first)) return out;
		out.add(first);
		if (first.stackable) first.quantity(s.quantity);
		else for (int i = 1; i < s.quantity; i++){ Item more = make(s); if (more != null) out.add(more); }
		return out;
	}

	private static Item make(HeroMod.StartingItem s){
		Item it = Reflection.newInstance(s.type);
		if (it == null) return null;
		if (s.level > 0) it.upgrade(s.level);
		it.identify();
		return it;
	}

	public static void store(Bundle bundle){
		if (activeJson != null) bundle.put(NODE, activeJson);
	}

	/** Loading a hero: restores the mod from the save itself; a damaged record is reported, never guessed. */
	public static void restore(Bundle bundle){
		active = null; activeJson = null; damaged = false;
		if (bundle == null || !bundle.contains(NODE)) return;
		String json = bundle.getString(NODE);
		try { active = HeroMod.parse(json); activeJson = json; }
		catch (HeroMod.Invalid e){ active = null; activeJson = json; damaged = true; }
	}

	/** Shown once when a saved game is continued: a clear message for an unreadable mod record, otherwise null. */
	public static String loadMessage(){
		if (!damaged) return null;
		return "This save's hero mod could not be read. The run continues with its base class, stays a Mod run"
				+ " (local only, no normal badges) and the saved mod record is kept unchanged.";
	}

	/** The name shown for this run's hero (mod name for a mod run). */
	public static String heroName(String className){
		HeroMod m = active;
		return m == null ? className : m.name;
	}

	static List<String> debugIds(){ List<String> l = new ArrayList<>(); if (active != null) l.add(active.id); return l; }
}

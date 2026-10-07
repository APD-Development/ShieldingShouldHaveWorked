/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */


package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.CombatEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKeys;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ItemEvents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.shatteredpixel.shatteredpixeldungeon.apd.query.ColumnDef.Merge;

//The shipped datasets and views. This is the only place that knows what Runs, Items, Enemies and Deaths contain; the engine, the
//view model and the screen only read these definitions. Adding a dataset is adding a DatasetDef here, a row builder in
//RowBuilders, and (optionally) views.
//
//PUBLIC columns are built only from what the player could see. INTERNAL columns come from hidden truth and are never shown for a
//run that is not permanently finished.
public final class Datasets {

	private Datasets(){}

	public static final String RUNS = "runs", ITEMS = "items", ENEMIES = "enemies", DEATHS = "deaths";

	//---- shared words ----
	private static ColumnDef yesNo( ColumnDef c ){
		return c.label(0, "No").label(1, "Yes");
	}

	//---- Runs ----

	static DatasetDef runs(){
		DatasetDef d = new DatasetDef(RUNS, "Runs", "Every run: recorded by APD, or taken from APD's own rankings file.");
		d.add(ColumnDef.text(FactKeys.RUN_ID, "Run id").group("Run").describe("The run's identifier."));
		d.add(ColumnDef.num(FactKeys.START_MILLIS, "Started").format(ColumnDef.Format.DATE_MILLIS).shown().group("Run"));
		d.add(ColumnDef.text(FactKeys.HERO_CLASS, "Class").shown().group("Run"));
		d.add(ColumnDef.enumeration(FactKeys.OUTCOME, "Outcome").label(0, "Died").label(1, "Won").label(2, "Abandoned").shown()
				.merge(Merge.LAST).group("Result"));
		d.add(ColumnDef.num(FactKeys.DEEPEST, "Deepest").shown().merge(Merge.MAX).group("Result"));
		d.add(ColumnDef.num(FactKeys.HERO_LEVEL, "Level").shown().merge(Merge.LAST).group("Result"));
		d.add(ColumnDef.num(FactKeys.SCORE, "Score").shown().merge(Merge.LAST).group("Result"));
		d.add(ColumnDef.num(FactKeys.TURNS, "Turns").shown().merge(Merge.LAST).group("Result"));
		d.add(ColumnDef.num(FactKeys.DEPTH, "Ended on floor").merge(Merge.LAST).group("Result"));
		d.add(ColumnDef.text(FactKeys.CAUSE, "Ended by").merge(Merge.LAST).group("Result"));
		d.add(ColumnDef.enumeration("run.source", "Source").label(1, "Recorded").label(2, "Imported ranking").group("Run"));
		d.add(ColumnDef.text(FactKeys.STATUS, "Status").merge(Merge.LAST).group("Run"));
		d.add(ColumnDef.num("run.segments", "Log segments").merge(Merge.SUM).group("Run")
				.describe("A run recorded by several telemetry versions has several log segments; they are shown as one run."));
		d.add(ColumnDef.num(FactKeys.CHALLENGES, "Challenges").group("Rules").describe("The challenge bit mask."));
		d.add(ColumnDef.num(FactKeys.EXOTIC_CHANCE, "Exotic %").group("Rules"));
		d.add(ColumnDef.text(FactKeys.RULESET, "Ruleset").group("Rules"));
		d.add(yesNo(ColumnDef.enumeration(FactKeys.DAILY, "Daily")).group("Rules"));
		d.add(ColumnDef.text(FactKeys.APD_VERSION, "APD version").group("Run"));
		d.add(ColumnDef.text("run.telemetry", "Telemetry").group("Run").describe("The recording version(s) this run was recorded with."));
		d.add(ColumnDef.num(FactKeys.INITIAL_VERSION, "Game version code").group("Run"));
		d.add(ColumnDef.text(FactKeys.GAME_VERSION, "Game version").group("Run"));
		d.add(yesNo(ColumnDef.enumeration(FactKeys.PARTIAL, "Started mid-run")).merge(Merge.ANY).group("Run"));
		d.add(ColumnDef.num(FactKeys.ABANDONED_EVENTS, "Rolled-back events").merge(Merge.SUM).group("Run"));
		d.add(ColumnDef.num(FactKeys.ROLLBACKS, "Rollbacks").merge(Merge.SUM).group("Run"));
		d.add(yesNo(ColumnDef.enumeration(FactKeys.ASCENDING, "Ascended")).group("Result"));
		//journey
		d.add(ColumnDef.num(FactKeys.LEVELS_ENTERED, "Levels entered").merge(Merge.SUM).group("Journey"));
		d.add(ColumnDef.num(FactKeys.DEEPEST_ENTERED, "Deepest entered").merge(Merge.MAX).group("Journey"));
		d.add(ColumnDef.num(FactKeys.FINAL_HERO_LEVEL, "Final level").merge(Merge.LAST).group("Journey"));
		d.add(ColumnDef.num(FactKeys.FINAL_HP_MAX, "Final max HP").merge(Merge.LAST).group("Journey"));
		d.add(ColumnDef.num(FactKeys.FINAL_STRENGTH, "Final strength").merge(Merge.LAST).group("Journey"));
		d.add(ColumnDef.num(FactKeys.FINAL_GOLD, "Final gold").merge(Merge.LAST).group("Journey"));
		d.add(ColumnDef.text(FactKeys.FINAL_SUBCLASS, "Subclass").merge(Merge.LAST).group("Journey"));
		//combat (telemetry 1.3)
		String c = "Combat";
		d.add(ColumnDef.num(FactKeys.HERO_ATTACKS, "Hero attacks").merge(Merge.SUM).since(1, 3).group(c));
		d.add(ColumnDef.num(FactKeys.HERO_HITS, "Hero hits").merge(Merge.SUM).since(1, 3).group(c));
		d.add(ColumnDef.num(FactKeys.ATTACKS_ON_HERO, "Attacks on hero").merge(Merge.SUM).since(1, 3).group(c));
		d.add(ColumnDef.num(FactKeys.HITS_ON_HERO, "Hits on hero").merge(Merge.SUM).since(1, 3).group(c));
		d.add(ColumnDef.num(FactKeys.DAMAGE_DEALT, "Damage dealt").merge(Merge.SUM).since(1, 3).group(c));
		d.add(ColumnDef.num(FactKeys.DAMAGE_TAKEN, "Damage taken").merge(Merge.SUM).since(1, 3).group(c));
		d.add(ColumnDef.num(FactKeys.HERO_KILLS, "Hero kills").merge(Merge.SUM).since(1, 3).group(c));
		d.add(ColumnDef.num(FactKeys.ENEMY_DEATHS, "Enemy deaths").merge(Merge.SUM).since(1, 3).group(c));
		d.add(ColumnDef.num(FactKeys.XP_GAINED, "XP gained").merge(Merge.SUM).since(1, 3).group(c));
		d.add(ColumnDef.num(FactKeys.LEVEL_UPS, "Level ups").merge(Merge.SUM).since(1, 3).group(c));
		d.add(ColumnDef.num(FactKeys.HEALED, "Healed (inferred)").merge(Merge.SUM).since(1, 3).group(c)
				.describe("Healing is inferred from the hero's HP between turns, not recorded exactly."));
		d.add(ColumnDef.num(FactKeys.HERO_DEATHS, "Hero deaths").merge(Merge.SUM).since(1, 3).group(c));
		d.add(ColumnDef.num(FactKeys.REVIVALS, "Revivals").merge(Merge.SUM).since(1, 3).group(c));
		//items (telemetry 1.2)
		d.add(ColumnDef.num(FactKeys.ITEMS_ACQUIRED, "Items acquired").merge(Merge.LOWER).since(1, 2).group("Items"));
		d.add(ColumnDef.num(FactKeys.ITEMS_EQUIPPED, "Items equipped").merge(Merge.LOWER).since(1, 2).group("Items"));
		d.add(ColumnDef.num(FactKeys.EQUIP_EVENTS, "Equip actions").merge(Merge.SUM).since(1, 2).group("Items"));
		//hidden truth
		d.add(ColumnDef.num(FactKeys.ITEMS_REGISTERED, "Items in the run (all)").internal().merge(Merge.LOWER).since(1, 2).group("Hidden")
				.describe("Every item the run ever contained, seen or not."));
		d.add(ColumnDef.num(FactKeys.GEN_LEVELS, "Levels generated").internal().merge(Merge.SUM).since(1, 4).group("Hidden"));
		d.add(ColumnDef.num(FactKeys.GEN_ITEMS, "Items generated").internal().merge(Merge.SUM).since(1, 4).group("Hidden"));
		d.add(ColumnDef.num(FactKeys.GEN_MOBS, "Enemies generated").internal().merge(Merge.SUM).since(1, 4).group("Hidden"));
		d.add(ColumnDef.num(FactKeys.SEED, "Seed").internal().group("Hidden"));
		d.add(ColumnDef.text(FactKeys.CUSTOM_SEED, "Custom seed").internal().group("Hidden"));
		//what the rankings file kept (imported records only)
		String l = "Imported ranking data";
		String[][] legacy = {
				{"legacy.goldCollected", "Gold collected"}, {"legacy.deepestFloor", "Deepest floor (stats)"}, {"legacy.highestAscent", "Highest ascent"},
				{"legacy.enemiesSlain", "Enemies slain"}, {"legacy.foodEaten", "Food eaten"}, {"legacy.itemsCrafted", "Items crafted"},
				{"legacy.piranhasKilled", "Piranhas killed"}, {"legacy.hazardAssistedKills", "Hazard kills"}, {"legacy.ankhsUsed", "Ankhs used"},
				{"legacy.upgradesUsed", "Upgrades used"}, {"legacy.sneakAttacks", "Sneak attacks"}, {"legacy.thrownAssists", "Thrown assists"},
				{"legacy.progressScore", "Progress score"}, {"legacy.heldItemValue", "Held item value"}, {"legacy.treasureScore", "Treasure score"},
				{"legacy.exploreScore", "Explore score"}, {"legacy.bossScore", "Boss score"}, {"legacy.questScore", "Quest score"},
				{"legacy.totalScore", "Total score"}, {"legacy.winMultiplierPct", "Win multiplier %"}, {"legacy.chalMultiplierPct", "Challenge multiplier %"},
				{"legacy.itemTypesDiscovered", "Item types discovered"}, {"legacy.armorTier", "Armor tier"}, {"legacy.rankDepth", "Ranking depth"},
				{"legacy.experience", "Experience"}, {"loadout.packItems", "Items kept in pack"}};
		for (String[] n : legacy) d.add(ColumnDef.num(n[0], n[1]).group(l));
		d.add(ColumnDef.num("legacy.durationMilli", "Duration (turns)").format(ColumnDef.Format.THOUSANDTHS).group(l));
		d.add(yesNo(ColumnDef.enumeration("legacy.amuletObtained", "Amulet obtained")).group(l));
		d.add(yesNo(ColumnDef.enumeration("legacy.gameWon", "Game won")).group(l));
		d.add(yesNo(ColumnDef.enumeration("legacy.ascended", "Ascended (stats)")).group(l));
		for (String slot : new String[]{"weapon", "armor", "artifact", "misc", "ring", "second_wep"}){
			String name = slot.equals("second_wep") ? "Second weapon" : Character.toUpperCase(slot.charAt(0)) + slot.substring(1);
			d.add(ColumnDef.text("loadout." + slot, name).group("Final equipment"));
			d.add(ColumnDef.num("loadout." + slot + ".level", name + " level").group("Final equipment"));
		}
		d.link(new LinkDef("Items in this run", ITEMS, FactKeys.RUN_ID, "item.run"));
		d.link(new LinkDef("Enemies met in this run", ENEMIES, FactKeys.RUN_ID, "enemy.run"));
		d.link(new LinkDef("Deaths in this run", DEATHS, FactKeys.RUN_ID, "death.run"));
		return d;
	}

	//---- Items ----

	private static Map<Long, String> categories(){
		Map<Long, String> m = new LinkedHashMap<>();
		m.put((long) ItemEvents.CAT_OTHER, "Other");
		m.put((long) ItemEvents.CAT_WEAPON, "Weapon");
		m.put((long) ItemEvents.CAT_MISSILE, "Missile");
		m.put((long) ItemEvents.CAT_ARMOR, "Armor");
		m.put((long) ItemEvents.CAT_RING, "Ring");
		m.put((long) ItemEvents.CAT_ARTIFACT, "Artifact");
		m.put((long) ItemEvents.CAT_WAND, "Wand");
		m.put((long) ItemEvents.CAT_POTION, "Potion");
		m.put((long) ItemEvents.CAT_SCROLL, "Scroll");
		m.put((long) ItemEvents.CAT_SEED, "Seed");
		m.put((long) ItemEvents.CAT_STONE, "Stone");
		m.put((long) ItemEvents.CAT_FOOD, "Food");
		m.put((long) ItemEvents.CAT_BAG, "Bag");
		m.put((long) ItemEvents.CAT_TRINKET, "Trinket");
		m.put((long) ItemEvents.CAT_SPELL, "Spell");
		m.put((long) ItemEvents.CAT_BOMB, "Bomb");
		m.put((long) ItemEvents.CAT_TOOL, "Tool");
		return m;
	}

	public static final int FATE_NEVER_SEEN = 0, FATE_LEFT_BEHIND = 1, FATE_NEVER_EQUIPPED = 2, FATE_EQUIPPED = 3;

	static DatasetDef items(){
		DatasetDef d = new DatasetDef(ITEMS, "Items", "Every item of every run: what the player saw of it, and what became of it.");
		d.minTelemetry = new com.shatteredpixel.shatteredpixeldungeon.apd.stats.TelemetryVersion(1, 2);
		d.add(ColumnDef.text("item.run", "Run").group("Item").describe("The run the item belongs to."));
		d.add(ColumnDef.text("item.name", "Item").shown().merge(Merge.LAST).group("Item")
				.describe("What the game called it the last time the player looked: unidentified items keep their unidentified name."));
		ColumnDef cat = ColumnDef.enumeration("item.category", "Kind").shown().merge(Merge.LAST).group("Item");
		for (Map.Entry<Long, String> e : categories().entrySet()) cat.label(e.getKey(), e.getValue());
		d.add(cat);
		d.add(ColumnDef.enumeration("item.fate", "What happened").shown().merge(Merge.MAX).group("Journey")
				.label(FATE_NEVER_SEEN, "Never seen").label(FATE_LEFT_BEHIND, "Seen, left behind")
				.label(FATE_NEVER_EQUIPPED, "Acquired, never equipped").label(FATE_EQUIPPED, "Equipped"));
		d.add(ColumnDef.num("item.firstSeenDepth", "First seen on floor").shown().group("Journey"));
		d.add(ColumnDef.num("item.acquiredDepth", "Acquired on floor").group("Journey"));
		d.add(ColumnDef.num("item.acquiredTurn", "Acquired at turn").group("Journey"));
		d.add(ColumnDef.num("item.equipCount", "Times equipped").shown().merge(Merge.SUM).group("Equipment"));
		d.add(ColumnDef.num("item.turnsEquipped", "Turns equipped").shown().merge(Merge.SUM).group("Equipment")
				.describe("Time spent equipped. If the run was still going, only up to the last recorded moment (a lower bound)."));
		d.add(ColumnDef.num("item.floorsEquipped", "Floors equipped on").merge(Merge.LOWER).group("Equipment"));
		d.add(yesNo(ColumnDef.enumeration("item.seen", "Ever seen")).merge(Merge.ANY).group("Journey"));
		d.add(yesNo(ColumnDef.enumeration("item.acquired", "Ever acquired")).merge(Merge.ANY).group("Journey"));
		d.add(yesNo(ColumnDef.enumeration("item.equipped", "Ever equipped")).merge(Merge.ANY).group("Equipment"));
		d.add(ColumnDef.num("item.visibleLevel", "Level (as known)").merge(Merge.LAST).group("Item")
				.describe("Only when the player knew the item's level. Never revealed afterwards."));
		d.add(yesNo(ColumnDef.enumeration("item.identified", "Identified")).merge(Merge.LAST).group("Item"));
		//hidden truth
		d.add(ColumnDef.text("item.trueClass", "True kind").internal().group("Hidden"));
		d.add(ColumnDef.num("item.trueLevel", "True level").internal().merge(Merge.LAST).group("Hidden"));
		d.add(yesNo(ColumnDef.enumeration("item.cursed", "Truly cursed")).internal().merge(Merge.LAST).group("Hidden"));
		d.add(ColumnDef.text("item.enchant", "True enchantment").internal().merge(Merge.LAST).group("Hidden"));
		d.add(ColumnDef.enumeration("item.origin", "Origin").internal().group("Hidden")
				.label(ItemEvents.ORIGIN_OBSERVED, "Observed").label(ItemEvents.ORIGIN_START_KIT, "Start kit")
				.label(ItemEvents.ORIGIN_ADOPTED, "Adopted").label(ItemEvents.ORIGIN_SPLIT, "Split").label(ItemEvents.ORIGIN_COPY, "Copy")
				.label(ItemEvents.ORIGIN_TRANSFORMED, "Transformed").label(ItemEvents.ORIGIN_CRAFTED, "Crafted")
				.label(ItemEvents.ORIGIN_GENERATED, "Generated with a level"));
		d.add(ColumnDef.num("item.genDepth", "Generated on floor").internal().group("Hidden"));
		d.add(ColumnDef.text("item.container", "Placed in").internal().group("Hidden"));
		d.add(ColumnDef.text("item.room", "Room").internal().group("Hidden"));
		d.add(ColumnDef.text("item.drawKind", "How it was drawn").internal().group("Hidden"));
		d.add(ColumnDef.text("item.requestedCategory", "Category asked for").internal().group("Hidden"));
		d.link(new LinkDef("The run", RUNS, "item.run", FactKeys.RUN_ID));
		return d;
	}

	//---- Enemies ----

	static DatasetDef enemies(){
		DatasetDef d = new DatasetDef(ENEMIES, "Enemies", "Enemy types per run: how they fought you and what became of them.");
		d.minTelemetry = new com.shatteredpixel.shatteredpixeldungeon.apd.stats.TelemetryVersion(1, 3);
		d.add(ColumnDef.text("enemy.run", "Run").group("Enemy"));
		d.add(ColumnDef.text("enemy.name", "Enemy").shown().group("Enemy").describe("What the game called it when you met it."));
		d.add(ColumnDef.num("enemy.attacksOnHero", "Attacks on hero").shown().merge(Merge.SUM).group("Fights"));
		d.add(ColumnDef.num("enemy.hitsOnHero", "Hits on hero").shown().merge(Merge.SUM).group("Fights"));
		d.add(ColumnDef.num("enemy.attacksByHero", "Hero attacks on it").merge(Merge.SUM).group("Fights"));
		d.add(ColumnDef.num("enemy.hitsByHero", "Hero hits on it").merge(Merge.SUM).group("Fights"));
		d.add(ColumnDef.num("enemy.damageToHero", "Damage to hero").shown().merge(Merge.SUM).group("Fights"));
		d.add(ColumnDef.num("enemy.damageFromHero", "Damage from hero").shown().merge(Merge.SUM).group("Fights"));
		d.add(ColumnDef.num("enemy.kills", "Killed by hero").shown().merge(Merge.SUM).group("Fights"));
		d.add(ColumnDef.num("enemy.deaths", "Seen dying").merge(Merge.SUM).group("Fights"));
		d.add(ColumnDef.num("enemy.heroDeaths", "Times it killed the hero").merge(Merge.SUM).group("Fights"));
		d.add(ColumnDef.text("enemy.class", "True kind").internal().group("Hidden"));
		d.add(ColumnDef.num("enemy.spawned", "Generated (all)").internal().merge(Merge.SUM).since(1, 4).group("Hidden"));
		d.add(ColumnDef.num("enemy.expValue", "XP value").internal().group("Hidden"));
		d.link(new LinkDef("The run", RUNS, "enemy.run", FactKeys.RUN_ID));
		return d;
	}

	//---- Deaths ----

	static DatasetDef deaths(){
		DatasetDef d = new DatasetDef(DEATHS, "Deaths", "Every time the hero died, and what ended each run.");
		d.minTelemetry = new com.shatteredpixel.shatteredpixeldungeon.apd.stats.TelemetryVersion(1, 1);
		d.add(ColumnDef.text("death.run", "Run").group("Death"));
		d.add(ColumnDef.num("death.depth", "Floor").shown().group("Death"));
		d.add(ColumnDef.num("death.turn", "Turn").shown().group("Death"));
		d.add(ColumnDef.text("death.cause", "Killed by").shown().group("Death"));
		d.add(ColumnDef.enumeration("death.causeKind", "Cause kind").shown().group("Death")
				.label(CombatEvents.SRC_CHAR, "Creature").label(CombatEvents.SRC_BUFF, "Status effect").label(CombatEvents.SRC_BLOB, "Gas or terrain")
				.label(CombatEvents.SRC_TRAP, "Trap").label(CombatEvents.SRC_ITEM, "Item").label(CombatEvents.SRC_ENCHANT, "Enchantment")
				.label(CombatEvents.SRC_CLASS, "Hero ability").label(CombatEvents.SRC_OTHER, "Other"));
		d.add(yesNo(ColumnDef.enumeration("death.revived", "Revived")).shown().group("Death"));
		d.add(yesNo(ColumnDef.enumeration("death.final", "Ended the run")).shown().group("Death"));
		d.add(ColumnDef.num("death.heroLevel", "Hero level").group("Death"));
		d.link(new LinkDef("The run", RUNS, "death.run", FactKeys.RUN_ID));
		return d;
	}

	//---- views ----

	private static GroupBy runStats( String... keys ){
		return new GroupBy(Arrays.asList(keys), Arrays.asList(Aggregate.count(), Aggregate.rate(FactKeys.OUTCOME, 1),
				Aggregate.of(Aggregate.Fn.AVG, FactKeys.SCORE), Aggregate.of(Aggregate.Fn.AVG, FactKeys.DEEPEST),
				Aggregate.of(Aggregate.Fn.MAX, FactKeys.DEEPEST), Aggregate.of(Aggregate.Fn.AVG, FactKeys.HERO_LEVEL)));
	}

	private static ViewDef itemView( String id, String title, String description, long... categories ){
		Query q = Query.of(ITEMS).addFilter(Filter.num("item.category", Op.IN, categories));
		return new ViewDef(id, title, description, q);
	}

	public static final int DEFAULT_MIN_SAMPLE = 5;

	static List<ViewDef> views(){
		List<ViewDef> v = new ArrayList<>();
		v.add(new ViewDef("runs", "Runs", "Every run.", Query.of(RUNS).withSorts(Arrays.asList(new Sort(FactKeys.START_MILLIS, true)))));
		v.add(new ViewDef("items", "Items", "Every item of every run.", Query.of(ITEMS)));
		v.add(itemView("weapons", "Weapons", "Melee and thrown weapons.", ItemEvents.CAT_WEAPON, ItemEvents.CAT_MISSILE));
		v.add(itemView("armor", "Armor", "Armor.", ItemEvents.CAT_ARMOR));
		v.add(itemView("rings", "Rings", "Rings.", ItemEvents.CAT_RING));
		v.add(itemView("trinkets", "Trinkets", "Trinkets.", ItemEvents.CAT_TRINKET));
		v.add(itemView("artifacts", "Artifacts", "Artifacts.", ItemEvents.CAT_ARTIFACT));
		v.add(new ViewDef("enemies", "Enemies", "Enemy types per run.", Query.of(ENEMIES)));
		v.add(new ViewDef("deaths", "Deaths", "Every death.", Query.of(DEATHS)));
		v.add(new ViewDef("characters", "Characters", "Runs grouped by hero class.",
				Query.of(RUNS).withGroup(runStats(FactKeys.HERO_CLASS)).withMinSample(DEFAULT_MIN_SAMPLE)
						.withSorts(Arrays.asList(new Sort("count", true)))));
		//challenges and the exotic chance are exactly what makes up a ruleset, so grouping by them never mixes rulesets
		v.add(new ViewDef("challenges", "Challenges", "Runs grouped by challenge setup (all rulesets, each group is one ruleset).",
				Query.of(RUNS).withScope(Scope.DEFAULT.withRules(Scope.Rules.ALL, ""))
						.withGroup(runStats(FactKeys.CHALLENGES, FactKeys.EXOTIC_CHANCE)).withMinSample(DEFAULT_MIN_SAMPLE)
						.withSorts(Arrays.asList(new Sort("count", true)))));
		return Collections.unmodifiableList(v);
	}

	public static List<DatasetDef> defs(){
		List<DatasetDef> l = new ArrayList<>();
		l.add(runs());
		l.add(items());
		l.add(enemies());
		l.add(deaths());
		return l;
	}

	public static List<ViewDef> allViews(){
		return views();
	}
}

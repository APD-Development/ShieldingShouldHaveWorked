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

package com.shatteredpixel.shatteredpixeldungeon.apd.stats;

//The kinds of fact the game's own mechanics declare (see FactKinds). Declaring a kind is the whole job of making a mechanic's history
//queryable: UIMSD generates its dataset, columns, filters and aggregates from the declaration. Keys and field order are permanent.
//
//PUBLIC kinds hold only what the player could see or do at that moment. Anything that would give away hidden information (the true
//class of an unidentified item, an effect with no icon) is an INTERNAL kind, shown only once the run is over.
public final class GameFacts {

	private GameFacts(){}

	/** forces the kinds to be registered */
	static void init(){
	}

	//---- movement and exploring ----

	//the hero changed cell: one observation per step, at full resolution (delta-encoded in the log)
	public static final FactKind MOVE_STEP = FactKinds.register(FactKind.builder("move.step", "Step").area("Movement")
			.description("Every step the hero took.")
			.value("cell", "Cell", "The cell the hero stepped onto (row times floor width plus column).", FactKind.PLAIN)
			.value("width", "Floor width", "How many cells wide the floor is, to turn a cell into a row and column.", FactKind.PLAIN)
			.value("travelling", "Travelling", "1 when walking by travel or auto-explore, 0 for a single step.", FactKind.PLAIN)
			.series().build());

	//a turn spent resting or waiting
	public static final FactKind REST = FactKinds.register(FactKind.builder("explore.rest", "Rest turn").plural("Rest turns").area("Movement")
			.description("Every turn the hero waited or rested.")
			.value("full", "Resting", "1 for resting (searching as well), 0 for a single wait.", FactKind.PLAIN)
			.series().build());

	public static final FactKind SEARCH = FactKinds.register(FactKind.builder("explore.search", "Search").area("Movement")
			.description("Every time the hero searched, on purpose or just by walking about.")
			.value("intentional", "On purpose", "1 when the hero chose to search.", FactKind.PLAIN)
			.value("found", "Found something", "1 when it turned something up.", FactKind.PLAIN).build());

	//a chest, tomb, skeleton or other container was opened
	public static final FactKind OPEN = FactKinds.register(FactKind.builder("explore.open", "Container opened").plural("Containers opened").area("Movement")
			.description("Every chest, tomb, skeleton or pile of remains the hero opened.").subject("Container")
			.value("items", "Items inside", "How many items were in it when it was opened.", FactKind.PLAIN).build());

	public static final FactKind TRAP = FactKinds.register(FactKind.builder("trap.trigger", "Trap triggered").plural("Traps triggered").area("Movement")
			.description("Every trap that went off where the hero could see it.").subject("Trap").actor()
			.value("hero", "Hero set it off", "1 when the hero triggered it, 0 for someone else.", FactKind.PLAIN).build());

	//---- items ----

	//an item action chosen by the hero (drink, read, zap, eat, equip, throw, drop, ...). The subject is the item's name as the player sees it
	public static final FactKind ITEM_USE = FactKinds.register(FactKind.builder("item.use", "Item action").plural("Item actions").area("Items")
			.description("Every action chosen for an item: drink, read, zap, eat, throw, equip and the rest.").subject("Item")
			.value("quantity", "How many in the stack", "", FactKind.PLAIN)
			.tag("action", "Action").build());

	//the same action with the hidden truth: the true class, and the numbers the player may not know yet
	public static final FactKind ITEM_USE_TRUTH = FactKinds.register(FactKind.builder("item.useTruth", "Item action (true item)").plural("Item actions (true item)")
			.area("Items").description("The same item actions with what the item really was.").subject("True item").internal()
			.value("level", "True level", "", FactKind.PLAIN)
			.value("charges", "Charges", "-1 when the item has none.", FactKind.PLAIN)
			.tag("action", "Action").build());

	//---- status effects ----

	public static final FactKind STATUS_START = FactKinds.register(FactKind.builder("status.start", "Effect began").plural("Effects began").area("Status effects")
			.description("Every status effect with an icon that began on the hero, or on a creature the hero could see.").subject("Effect").actor()
			.value("duration", "Duration", "In turns; -1 when it has no fixed length.", FactKind.PLAIN).build());

	public static final FactKind STATUS_END = FactKinds.register(FactKind.builder("status.end", "Effect ended").plural("Effects ended").area("Status effects")
			.description("Every status effect with an icon that ended.").subject("Effect").actor()
			.value("remaining", "Turns left", "In turns; -1 when it has no fixed length.", FactKind.PLAIN).build());

	//the real class of every effect: some effects (ring and armor ones) can give away what an unidentified item is, so this is hidden information
	public static final FactKind STATUS_START_TRUTH = FactKinds.register(FactKind.builder("status.startTruth", "Effect began (true effect)").plural("Effects began (true effect)")
			.area("Status effects").description("Every status effect that began, including ones with no icon, by what it really was.").subject("True effect").actor().internal()
			.value("duration", "Duration", "", FactKind.PLAIN).build());

	public static final FactKind STATUS_END_TRUTH = FactKinds.register(FactKind.builder("status.endTruth", "Effect ended (true effect)").plural("Effects ended (true effect)")
			.area("Status effects").description("Every status effect that ended, including ones with no icon, by what it really was.").subject("True effect").actor().internal()
			.value("remaining", "Turns left", "", FactKind.PLAIN).build());

	//---- character ----

	public static final FactKind TALENT = FactKinds.register(FactKind.builder("talent.upgrade", "Talent point").plural("Talent points").area("Character")
			.description("Every talent point spent.").subject("Talent")
			.value("points", "Points in it now", "", FactKind.PLAIN)
			.value("tier", "Tier", "", FactKind.PLAIN).build());

	public static final FactKind SUBCLASS = FactKinds.register(FactKind.builder("subclass.choose", "Subclass chosen").plural("Subclasses chosen").area("Character")
			.description("Every time a subclass was taken or lost.").subject("Subclass").build());

	public static final FactKind ABILITY_CHOOSE = FactKinds.register(FactKind.builder("ability.choose", "Armor ability chosen").plural("Armor abilities chosen")
			.area("Character").description("Every time an armor ability was chosen.").subject("Ability").build());

	public static final FactKind SPELL_CAST = FactKinds.register(FactKind.builder("spell.cast", "Spell cast").plural("Spell casts").area("Abilities")
			.description("Every spell the hero cast.").subject("Spell")
			.value("cost", "Charge cost", "", FactKind.PLAIN).build());

	public static final FactKind ABILITY_USE = FactKinds.register(FactKind.builder("ability.use", "Armor ability used").plural("Armor abilities used")
			.area("Abilities").description("Every time the hero used the ability of their class armor.").subject("Ability")
			.value("cost", "Charge cost", "", FactKind.PLAIN).build());

	//---- economy ----

	public static final FactKind GOLD = FactKinds.register(FactKind.builder("economy.gold", "Gold change").plural("Gold changes").area("Economy")
			.description("Every change in the hero's gold, with where it came from or went.")
			.value("delta", "Gold gained or spent", "Negative when spent.", FactKind.PLAIN)
			.value("total", "Gold after", "", FactKind.PLAIN)
			.tag("cause", "Cause").build());

	public static final FactKind TRADE = FactKinds.register(FactKind.builder("economy.trade", "Shop trade").plural("Shop trades").area("Economy")
			.description("Every item bought from or sold to a shopkeeper.").subject("Item")
			.value("price", "Price", "", FactKind.PLAIN)
			.value("quantity", "Quantity", "", FactKind.PLAIN)
			.tag("side", "Bought or sold").build());

	//---- time ----

	//cumulative time actually spent playing (idle gaps are not counted): the "per hour" measure is built from it
	public static final FactKind TIME_ACTIVE = FactKinds.register(FactKind.builder("time.active", "Time played").plural("Time played").area("Time")
			.description("How long the run has been played so far, checked at every floor change and save.")
			.value("millis", "Milliseconds played", "", FactKind.PLAIN).build());
}

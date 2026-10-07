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

package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.GameFacts;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

import java.util.LinkedHashMap;

//Turns what the hero does and what happens around them into generic fact observations (see FactKinds, GameFacts): steps, rests,
//searches, item actions, status effects, talents, spells, abilities, gold, trades, traps, and the time actually spent playing.
//
//Reads the game, never changes it, never consumes a random number. Every method is called from a one-line hook in the game code (see
//ApdPlay) and swallows its own errors: a statistics problem must never reach gameplay.
final class PlayTracker {

	private final FactTracker facts;

	private int lastGold;
	private long lastNano;
	private long activeNanos;
	//idle gaps (the app in the background, a long pause) are not play time
	private static final long MAX_GAP_NANOS = 30_000_000_000L;
	private static final long NANOS_PER_MILLI = 1_000_000L;

	PlayTracker( FactTracker facts ){
		this.facts = facts;
		baseline();
	}

	//the state "as it is now" is the starting point: only changes after this are observations
	void baseline(){
		lastGold = Dungeon.gold;
		lastNano = System.nanoTime();
	}

	private static String name( Class<?> c ){
		return GameSnapshot.shortName(c);
	}

	// ---- the hero's turn ----

	void onHeroTurn(){
		long now = System.nanoTime();
		long gap = now - lastNano;
		lastNano = now;
		if (gap > 0) activeNanos += Math.min(gap, MAX_GAP_NANOS);
		pollGold("other");
	}

	void recordTime(){
		facts.record(GameFacts.TIME_ACTIVE, GameSnapshot.turn(), "", "", new long[]{ activeNanos / NANOS_PER_MILLI }, null);
	}

	// ---- movement and exploring ----

	void onHeroMoved( Hero hero, int to, boolean travelling ){
		facts.record(GameFacts.MOVE_STEP, GameSnapshot.turn(), "", "", new long[]{ to, Dungeon.level.width(), travelling ? 1 : 0 }, null);
	}

	void onHeroRested( boolean full ){
		facts.record(GameFacts.REST, GameSnapshot.turn(), "", "", new long[]{ full ? 1 : 0 }, null);
	}

	void onHeroSearched( boolean intentional, boolean found ){
		facts.record(GameFacts.SEARCH, GameSnapshot.turn(), "", "", new long[]{ intentional ? 1 : 0, found ? 1 : 0 }, null);
	}

	void onHeapOpened( com.shatteredpixel.shatteredpixeldungeon.items.Heap heap ){
		if (heap == null) return;
		facts.record(GameFacts.OPEN, GameSnapshot.turn(), String.valueOf(heap.type), "", new long[]{ heap.items.size() }, null);
	}

	void onTrapTriggered( Trap trap, Char who ){
		if (trap == null) return;
		boolean byHero = who == Dungeon.hero;
		if (!byHero && !CombatTracker.visible(who) && !(Dungeon.level.heroFOV != null && trap.pos >= 0 && trap.pos < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[trap.pos])) return;
		facts.record(GameFacts.TRAP, GameSnapshot.turn(), name(trap.getClass()), byHero || who == null ? "" : CombatTracker.nameOf(who), new long[]{ byHero ? 1 : 0 }, null);
	}

	// ---- items ----

	void onItemAction( Item item, String action ){
		if (item == null) return;
		String shown;
		try {
			shown = item.name();
		} catch (Throwable t){
			shown = "";
		}
		long clock = GameSnapshot.turn();
		facts.record(GameFacts.ITEM_USE, clock, shown, "", new long[]{ item.quantity() }, new String[]{ action == null ? "" : action });
		long charges = -1;
		if (item instanceof Wand) charges = ((Wand) item).curCharges;
		facts.record(GameFacts.ITEM_USE_TRUTH, clock, name(item.getClass()), "", new long[]{ item.level(), charges }, new String[]{ action == null ? "" : action });
	}

	// ---- status effects ----

	void onBuff( Buff buff, Char target, boolean began ){
		if (buff == null || target == null) return;
		if (target != Dungeon.hero && !CombatTracker.visible(target)) return;
		float turns = -1;
		if (buff instanceof FlavourBuff){
			try {
				turns = ((FlavourBuff) buff).cooldown();
			} catch (Throwable ignored){
			}
		}
		boolean shownToPlayer = buff.icon() != BuffIndicator.NONE;
		String who = target == Dungeon.hero ? "" : CombatTracker.nameOf(target);
		long[] v = new long[]{ Math.round(turns) };
		long clock = GameSnapshot.turn();
		//what the player saw: the effect's shown name, and only for an effect with an icon. The class says more (an armor glyph, a ring), so it
		//goes only in the hidden record, which has every effect.
		if (shownToPlayer){
			String shown;
			try {
				shown = buff.toString();
			} catch (Throwable t){
				shown = "";
			}
			facts.record(began ? GameFacts.STATUS_START : GameFacts.STATUS_END, clock, shown, who, v, null);
		}
		facts.record(began ? GameFacts.STATUS_START_TRUTH : GameFacts.STATUS_END_TRUTH, clock, name(buff.getClass()), who, v, null);
	}

	// ---- character ----

	void onTalentUpgraded( Hero hero, Talent talent ){
		int tierNo = 0, points = 0;
		for (int t = 0; t < hero.talents.size(); t++){
			LinkedHashMap<Talent, Integer> tier = hero.talents.get(t);
			if (tier.containsKey(talent)){
				tierNo = t + 1;
				points = tier.get(talent);
			}
		}
		facts.record(GameFacts.TALENT, GameSnapshot.turn(), talent.name(), "", new long[]{ points, tierNo }, null);
	}

	void onSubclass( Object subclass ){
		facts.record(GameFacts.SUBCLASS, GameSnapshot.turn(), String.valueOf(subclass), "", null, null);
	}

	void onAbilityChosen( Object ability ){
		facts.record(GameFacts.ABILITY_CHOOSE, GameSnapshot.turn(), ability == null ? "" : name(ability.getClass()), "", null, null);
	}

	void onSpell( Object spell, float cost ){
		facts.record(GameFacts.SPELL_CAST, GameSnapshot.turn(), name(spell.getClass()), "", new long[]{ Math.round(cost) }, null);
	}

	void onAbilityUsed( Object ability, float cost ){
		facts.record(GameFacts.ABILITY_USE, GameSnapshot.turn(), name(ability.getClass()), "", new long[]{ Math.round(cost) }, null);
	}

	// ---- economy ----

	void pollGold( String cause ){
		int now = Dungeon.gold;
		if (now == lastGold) return;
		facts.record(GameFacts.GOLD, GameSnapshot.turn(), "", "", new long[]{ now - lastGold, now }, new String[]{ cause });
		lastGold = now;
	}

	void onTrade( String side, Item item, int price, int quantity ){
		String shown;
		try {
			shown = item == null ? "" : item.name();
		} catch (Throwable t){
			shown = "";
		}
		facts.record(GameFacts.TRADE, GameSnapshot.turn(), shown, "", new long[]{ price, quantity }, new String[]{ side });
	}
}

/*
 * ADHD Pixel Dungeon
 * Copyright (C) 2026 Chris Charlton
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

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ApdEvent;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.EventTypes;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.GenerationEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ItemEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ReadResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//Where the player found an item, the same answer the game gives in the item's own window ("Found on floor 3, in a chest"). It is
//worked out from the log by the same rules the game uses when it rebuilds item origins from history (recorder.ItemOriginHistory):
//the generated place counts only when the player saw or picked up the item right there, a quest reward, enemy drop, mimic or
//statue counts on receipt, a first pickup or purchase elsewhere is "picked up" or "shop", a merge of different origins is
//"mixed", and a crafted item is "crafted". The game's room names and generation-rule details are left out here.
final class PublicItemOrigins {

	//the same numbers as qol.ItemOrigins (PublicItemOriginsTest keeps them equal); this library may not use game classes
	static final int PICKUP = 13, MIXED = 14, CRAFTED = 15;

	static final class Origin {
		final int floor, branch, source;

		Origin( int floor, int branch, int source ){
			this.floor = floor;
			this.branch = branch;
			this.source = source;
		}

		@Override
		public boolean equals( Object o ){
			if (!(o instanceof Origin)) return false;
			Origin x = (Origin) o;
			return floor == x.floor && branch == x.branch && source == x.source;
		}

		@Override
		public int hashCode(){
			return (floor * 31 + branch) * 31 + source;
		}
	}

	//Generation and lineage events are hidden information. A log of a run still going travels without them (archive.LogRedactor),
	//so for such a log only what the player's own events say is used: picked up, bought. The same log then gives the same rows
	//before and after it is exported. Once the run has ended, the full answer the game gave is used.
	private final boolean useHidden;

	PublicItemOrigins( boolean runEnded ){
		this.useHidden = runEnded;
	}

	final Map<String, Origin> origins = new HashMap<>();
	private final Map<String, GenerationEvents.ItemGenerated> generated = new HashMap<>();
	private final Set<String> acquiredBefore = new HashSet<>();
	//how each item first came into the hero's hands (ItemEvents.ACQ_*)
	final Map<String, Integer> firstAcquired = new HashMap<>();

	void accept( ApdEvent event, ReadResult log ){
		if (!useHidden && EventTypes.visibility(event.type) != EventTypes.Visibility.PUBLIC) return;
		switch (event.type){
			case EventTypes.ITEM_GENERATED: {
				GenerationEvents.ItemGenerated g = GenerationEvents.decodeItemGenerated(event, log);
				if (!generated.containsKey(g.uid)) generated.put(g.uid, g);
				break;
			}
			case EventTypes.ITEM_SEEN: {
				ItemEvents.Seen seen = ItemEvents.decodeSeen(event, log);
				GenerationEvents.ItemGenerated g = generated.get(seen.uid);
				if (g != null && seen.where.depth == g.where.depth && seen.where.branch == g.where.branch && seen.where.pos == g.pos)
					putIfAbsent(seen.uid, new Origin(g.where.depth, g.where.branch, g.container));
				break;
			}
			case EventTypes.ITEM_ACQUIRED: {
				ItemEvents.Acquired a = ItemEvents.decodeAcquired(event, log);
				boolean firstReceipt = acquiredBefore.add(a.uid);
				if (firstReceipt) firstAcquired.put(a.uid, a.context);
				GenerationEvents.ItemGenerated g = generated.get(a.uid);
				boolean counts = a.context != ItemEvents.ACQ_START_KIT && a.context != ItemEvents.ACQ_RECONCILED;
				if (counts && g != null && a.where.depth == g.where.depth && a.where.branch == g.where.branch && a.where.pos == g.pos)
					putIfAbsent(a.uid, new Origin(g.where.depth, g.where.branch, g.container));
				if (counts && g != null && a.where.depth == g.where.depth && a.where.branch == g.where.branch
						&& (g.container == GenerationEvents.C_QUEST_REWARD || g.container == GenerationEvents.C_MOB_LOOT
						|| g.container == GenerationEvents.C_MIMIC || g.container == GenerationEvents.C_STATUE))
					putIfAbsent(a.uid, new Origin(g.where.depth, g.where.branch, g.container));
				if (firstReceipt && (a.context == ItemEvents.ACQ_SHOP || a.context == ItemEvents.ACQ_PICKUP))
					putIfAbsent(a.uid, new Origin(a.where.depth, a.where.branch,
							a.context == ItemEvents.ACQ_SHOP ? GenerationEvents.C_FOR_SALE : PICKUP));
				break;
			}
			case EventTypes.ITEM_LINEAGE: {
				ItemEvents.Lineage l = ItemEvents.decodeLineage(event, log);
				Origin origin = origins.get(l.to);
				if (l.kind == ItemEvents.LIN_MERGE){
					for (String uid : l.from) origin = combine(origin, origins.get(uid));
				} else if (l.kind == ItemEvents.LIN_SPLIT || l.kind == ItemEvents.LIN_COPY || l.kind == ItemEvents.LIN_TRANSFORM){
					if (l.from.size() == 1) origin = origins.get(l.from.get(0));
				} else if (l.kind == ItemEvents.LIN_CRAFT){
					origin = new Origin(l.where.depth, l.where.branch, CRAFTED);
				}
				if (origin != null) origins.put(l.to, origin);
				break;
			}
			default: break;
		}
	}

	private void putIfAbsent( String uid, Origin o ){
		if (!origins.containsKey(uid)) origins.put(uid, o);
	}

	static Origin combine( Origin a, Origin b ){
		if (a == null ? b == null : a.equals(b)) return a;
		return new Origin(0, 0, MIXED);
	}

	//The game only keeps origins for gear and wands (qol.ItemOrigins.eligible): weapons, thrown weapons, armor, rings, artifacts, wands.
	static boolean eligible( int category ){
		return category == ItemEvents.CAT_WEAPON || category == ItemEvents.CAT_MISSILE || category == ItemEvents.CAT_ARMOR
				|| category == ItemEvents.CAT_RING || category == ItemEvents.CAT_ARTIFACT || category == ItemEvents.CAT_WAND;
	}

	static String sourceLabel( int source ){
		switch (source){
			case GenerationEvents.C_HEAP: return "On the floor";
			case GenerationEvents.C_CHEST: return "Chest";
			case GenerationEvents.C_LOCKED_CHEST: return "Golden chest";
			case GenerationEvents.C_CRYSTAL_CHEST: return "Crystal chest";
			case GenerationEvents.C_TOMB: return "Tomb";
			case GenerationEvents.C_SKELETON: return "Skeleton";
			case GenerationEvents.C_REMAINS: return "Remains";
			case GenerationEvents.C_FOR_SALE: return "Shop";
			case GenerationEvents.C_MIMIC: return "Mimic";
			case GenerationEvents.C_STATUE: return "Statue";
			case GenerationEvents.C_QUEST_REWARD: return "Quest reward";
			case GenerationEvents.C_MOB_LOOT: return "Enemy drop";
			case GenerationEvents.C_BONUS_DROP: return "Bonus drop";
			case PICKUP: return "Picked up";
			case MIXED: return "Mixed";
			case CRAFTED: return "Crafted";
			default: return "";
		}
	}

	static String acquiredLabel( int context ){
		switch (context){
			case ItemEvents.ACQ_PICKUP: return "Picked up";
			case ItemEvents.ACQ_START_KIT: return "Starting kit";
			case ItemEvents.ACQ_SHOP: return "Bought";
			case ItemEvents.ACQ_CRAFT: return "Crafted";
			case ItemEvents.ACQ_TRANSMUTE: return "Transmuted";
			case ItemEvents.ACQ_OTHER: return "Other";
			default: return "";   //ACQ_RECONCILED: the recorder caught up with an item it had not seen arrive, so how is not known
		}
	}
}

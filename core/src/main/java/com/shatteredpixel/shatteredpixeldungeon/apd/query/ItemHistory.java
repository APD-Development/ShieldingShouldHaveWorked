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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

//In-run history for an item's info window (UIMSD deep dive item 8, approved Nov 23): a light index, one small record per
//item name as the player saw it, read once from the finished runs' "Items" rows. Facts only (runs it was seen in, picked
//up in, equipped in); no advice. The run being played is never counted, and imported, duplicate or hidden rows are skipped.
//Callers must not ask about an item whose kind the player doesn't know yet (unknown potions, scrolls, rings): those names
//are shuffled every run, so a count across runs would mean nothing.
public final class ItemHistory {

	public static final class Entry {
		public final String name;
		public final Set<String> seen = new HashSet<>(), acquired = new HashSet<>(), equipped = new HashSet<>();

		Entry( String name ){
			this.name = name;
		}
	}

	private final Map<String, Entry> byName = new HashMap<>();
	public final int finishedRuns;

	private ItemHistory( int finishedRuns ){
		this.finishedRuns = finishedRuns;
	}

	/** Never throws; an unreadable or empty catalog gives an index that knows nothing. */
	public static ItemHistory of( Catalog cat, String skipRunId ){
		try {
			return build(cat, skipRunId);
		} catch (RuntimeException e){
			return new ItemHistory(0);
		}
	}

	private static ItemHistory build( Catalog cat, String skip ){
		Dataset d = cat == null ? null : cat.dataset(Datasets.ITEMS);
		if (d == null) return new ItemHistory(0);
		Set<String> finished = new HashSet<>();
		for (Dataset.RunInfo ri : d.runs){
			if (ri.finished && !ri.duplicate && ri.runId != null && !ri.runId.equals(skip)) finished.add(ri.runId);
		}
		ItemHistory h = new ItemHistory(finished.size());
		DatasetDef def = d.def;
		int nameC = def.indexOf("item.name"), seenC = def.indexOf("item.seen"),
				acqC = def.indexOf("item.acquired"), eqC = def.indexOf("item.equipped");
		if (nameC < 0) return h;
		for (Row r : d.rows){
			if (r.imported || r.duplicate || !r.publicRow || !finished.contains(r.runId)) continue;
			if (!known(r, nameC)) continue;
			String name = CellFormatter.readable(r.asText(nameC));
			if (name.isEmpty()) continue;
			boolean seen = yes(r, seenC), acq = yes(r, acqC), eq = yes(r, eqC);
			if (!seen && !acq && !eq) continue;
			String key = key(name);
			Entry e = h.byName.get(key);
			if (e == null) h.byName.put(key, e = new Entry(name));
			e.seen.add(r.runId);
			if (acq) e.acquired.add(r.runId);
			if (eq) e.equipped.add(r.runId);
		}
		return h;
	}

	private static boolean known( Row r, int c ){
		return c >= 0 && c < r.size() && r.isKnown(c);
	}

	private static boolean yes( Row r, int c ){
		return known(r, c) && r.kind(c) == Row.K_NUM && r.num(c) == 1;
	}

	static String key( String name ){
		return name.trim().toLowerCase(Locale.ROOT);
	}

	public Entry get( String name ){
		return name == null ? null : byName.get(key(name));
	}

	/** One plain line for the info window, or "" when there are no finished runs to read. */
	public String line( String name ){
		return line(name, true);
	}

	/** Chris, Nov 25: "equipped in" only for things that can be equipped (a scroll can't be). */
	public String line( String name, boolean equippable ){
		if (finishedRuns == 0) return "";
		Entry e = get(name);
		String runs = finishedRuns == 1 ? "1 finished run" : finishedRuns + " finished runs";
		if (e == null) return "Your history: not seen in your " + runs + ".";
		return "Your history: seen in " + e.seen.size() + " of your " + runs + "; picked up in " + e.acquired.size()
				+ (equippable ? ", equipped in " + e.equipped.size() : "") + ".";
	}

	//---- the game's shared copy: built once off the render thread, rebuilt for a new run ----

	private static volatile ItemHistory shared;
	//The run an index was built for travels with the index itself, so a reader can never pair one build's index
	//with another build's run id (two separate volatile fields could be read across a rebuild).
	private volatile String builtFor;
	private static volatile boolean building;

	/** The cached index if it is ready for this run; otherwise starts building it and returns null. */
	public static ItemHistory cached( final File history, final String currentRun ){
		ItemHistory h = shared;
		String forRun = currentRun == null ? "" : currentRun;
		if (h != null && forRun.equals(h.builtFor)) return h;
		if (history == null || building) return null;
		building = true;
		Thread t = new Thread(new Runnable(){
			@Override
			public void run(){
				try {
					ItemHistory built = of(new DatasetStore(history).load(), currentRun);
					built.builtFor = forRun;
					shared = built;
				} catch (Throwable unreadable){
					//leave the old copy; the window simply shows no history line
				} finally {
					building = false;
				}
			}
		}, "apd-item-history");
		t.setDaemon(true);
		t.start();
		return null;
	}
}

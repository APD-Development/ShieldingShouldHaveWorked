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

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FloorMaps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//UIMSD item 10, run replay: for a FINISHED run, each floor's true layout (the internal floor.map fact) with the hero's recorded steps on
//it, in the order they were taken. Never for a run still being played (the map would give away the floor). Facts only; never throws.
public final class RunReplay {

	public static final class Floor {
		public final int depth, branch, width, height;
		public final String cells;
		//the cells the hero stepped onto on this floor, in order (every visit)
		public final List<Integer> path = new ArrayList<>();
		long firstTurn;

		Floor( int depth, int branch, int width, int height, String cells, long firstTurn ){
			this.depth = depth;
			this.branch = branch;
			this.width = width;
			this.height = height;
			this.cells = cells;
			this.firstTurn = firstTurn;
		}

		public char cell( int x, int y ){
			return cells.charAt(y * width + x);
		}
	}

	public final String runId;
	//false when the run is not finished or has no maps (recorded before maps were kept)
	public final boolean available;
	public final List<Floor> floors = new ArrayList<>();

	private RunReplay( String runId, boolean available ){
		this.runId = runId;
		this.available = available;
	}

	public static RunReplay of( Catalog cat, String runId ){
		try {
			RunReplay r = build(cat, runId);
			if (r != null) return r;
		} catch (RuntimeException ignored){
		}
		return new RunReplay(runId == null ? "" : runId, false);
	}

	private static boolean num( Row r, int c ){
		return c >= 0 && c < r.size() && r.isKnown(c) && r.kind(c) == Row.K_NUM;
	}

	private static RunReplay build( Catalog cat, String runId ){
		if (cat == null || runId == null || runId.isEmpty()) return null;
		Dataset maps = cat.dataset(FactDatasets.PREFIX + "floor.map");
		if (maps == null) return null;
		boolean finished = false;
		for (Dataset.RunInfo ri : maps.runs){
			if (runId.equals(ri.runId) && ri.finished && !ri.duplicate) finished = true;
		}
		if (!finished) return null;
		DatasetDef md = maps.def;
		int tC = md.indexOf(FactDatasets.TURN), dC = md.indexOf("fact.v.depth"), bC = md.indexOf("fact.v.branch"),
				wC = md.indexOf("fact.v.width"), hC = md.indexOf("fact.v.height"), cC = md.indexOf("fact.t.cells");
		Map<Long, Floor> byFloor = new LinkedHashMap<>();
		for (Row r : maps.rowsOf(runId)){
			if (r.imported || r.duplicate || !r.finished) continue;
			if (!num(r, dC) || !num(r, bC) || !num(r, wC) || !num(r, hC) || cC < 0 || !r.isKnown(cC)) continue;
			long key = r.num(dC) * 1000L + r.num(bC);
			long turn = num(r, tC) ? r.num(tC) : 0;
			Floor old = byFloor.get(key);
			if (old != null && old.firstTurn <= turn) continue;   //the first map of a floor wins
			String cells = r.asText(cC);
			if (!FloorMaps.valid(cells, r.num(wC), r.num(hC))) continue;
			byFloor.put(key, new Floor((int) r.num(dC), (int) r.num(bC), (int) r.num(wC), (int) r.num(hC), cells, turn));
		}
		if (byFloor.isEmpty()) return null;
		RunReplay out = new RunReplay(runId, true);
		out.floors.addAll(byFloor.values());
		Collections.sort(out.floors, new Comparator<Floor>(){
			@Override
			public int compare( Floor a, Floor b ){
				return a.firstTurn != b.firstTurn ? (a.firstTurn < b.firstTurn ? -1 : 1) : (a.depth != b.depth ? a.depth - b.depth : a.branch - b.branch);
			}
		});
		out.steps(cat, byFloor);
		return out;
	}

	private void steps( Catalog cat, Map<Long, Floor> byFloor ){
		Dataset d = cat.dataset(FactDatasets.PREFIX + "move.step");
		if (d == null) return;
		DatasetDef dd = d.def;
		int tC = dd.indexOf(FactDatasets.TURN), dC = dd.indexOf(FactDatasets.DEPTH), bC = dd.indexOf(FactDatasets.BRANCH),
				cC = dd.indexOf("fact.v.cell"), wC = dd.indexOf("fact.v.width");
		List<Row> rows = new ArrayList<>();
		for (Row r : d.rowsOf(runId)){
			if (r.imported || r.duplicate || !num(r, dC) || !num(r, bC) || !num(r, cC)) continue;
			rows.add(r);
		}
		final int t = tC;
		Collections.sort(rows, new Comparator<Row>(){
			@Override
			public int compare( Row a, Row b ){
				long x = num(a, t) ? a.num(t) : 0, y = num(b, t) ? b.num(t) : 0;
				return x < y ? -1 : x > y ? 1 : 0;
			}
		});
		for (Row r : rows){
			Floor f = byFloor.get(r.num(dC) * 1000L + r.num(bC));
			if (f == null) continue;
			if (num(r, wC) && r.num(wC) != f.width) continue;   //a step on a floor of another shape is not this map's
			long c = r.num(cC);
			if (c < 0 || c >= f.cells.length()) continue;
			f.path.add((int) c);
		}
	}
}

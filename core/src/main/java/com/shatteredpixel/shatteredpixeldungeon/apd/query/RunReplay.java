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
		//the turn of each step in path
		final List<Long> pathTurns = new ArrayList<>();
		//creature snapshots on this floor (floor.actors), in turn order: every creature and where it stood from that turn on
		final List<Long> actorTurns = new ArrayList<>();
		final List<String> actorTexts = new ArrayList<>();
		//false when this floor's layout was never recorded (played on a build from before maps were kept): only the walk is drawn
		public final boolean mapped;
		long firstTurn;

		Floor( int depth, int branch, int width, int height, String cells, long firstTurn ){
			this.depth = depth;
			this.branch = branch;
			this.width = width;
			this.height = height;
			this.cells = cells;
			this.firstTurn = firstTurn;
			this.mapped = true;
		}

		//a floor known only from its steps: every cell starts as wall, and the walk shows where the hero went
		Floor( int depth, int branch, int width, int height, long firstTurn ){
			this.depth = depth;
			this.branch = branch;
			this.width = width;
			this.height = height;
			char[] blank = new char[width * height];
			java.util.Arrays.fill(blank, FloorMaps.WALL);
			this.cells = new String(blank);
			this.firstTurn = firstTurn;
			this.mapped = false;
		}

		public char cell( int x, int y ){
			return cells.charAt(y * width + x);
		}

		public static final char VISITED = 'o', HERO = '@';

		public static final class Creature {
			public final int cell;
			//H hostile, B boss or unique, F friendly NPC, A ally, D neutral
			public final char kind;
			//the recorded class name, e.g. "actors.mobs.Gnoll"
			public final String type;
			Creature( int cell, char kind, String type ){ this.cell = cell; this.kind = kind; this.type = type; }
		}

		//every creature type seen on this floor, in the order they first appear
		public List<String> creatureTypes(){
			java.util.LinkedHashSet<String> types = new java.util.LinkedHashSet<>();
			for (String text : actorTexts){
				if (text.isEmpty()) continue;
				for (String part : text.split(";")){
					String[] f = part.split(":", 3);
					if (f.length == 3) types.add(f[2]);
				}
			}
			return new ArrayList<>(types);
		}

		public boolean hasCreatures(){
			return !actorTexts.isEmpty();
		}

		//the playback order (Chris, Nov 25: creatures move when they moved, even while the hero stands still): every step and every creature
		//snapshot by turn, a snapshot before a step on the same turn. Entry >= 0 is a step index, entry < 0 is snapshot -(entry + 1).
		private int[] timeline;

		public int[] timeline(){
			if (timeline != null) return timeline;
			int n = path.size(), m = actorTexts.size();
			int[] out = new int[n + m];
			int i = 0, j = 0, k = 0;
			while (i < n || j < m){
				boolean takeSnapshot = j < m && (i >= n || actorTurns.get(j) <= (i < pathTurns.size() ? pathTurns.get(i) : Long.MAX_VALUE));
				out[k++] = takeSnapshot ? -(j++) - 1 : i++;
			}
			return timeline = out;
		}

		//every creature in snapshot number `snapshot` (clamped); empty when none were recorded
		public List<Creature> creaturesAt( int snapshot ){
			List<Creature> out = new ArrayList<>();
			if (actorTexts.isEmpty()) return out;
			String text = actorTexts.get(Math.max(0, Math.min(snapshot, actorTexts.size() - 1)));
			if (text.isEmpty()) return out;
			for (String part : text.split(";")){
				String[] f = part.split(":", 3);
				if (f.length != 3 || f[1].length() != 1) continue;
				try {
					int c = Integer.parseInt(f[0]);
					if (c >= 0 && c < cells.length()) out.add(new Creature(c, f[1].charAt(0), f[2]));
				} catch (NumberFormatException ignored){
				}
			}
			return out;
		}

		//every creature on the floor at the moment of the shown-th step (with no steps shown: the first snapshot). Empty when none were recorded.
		public List<Creature> creatures( int shown ){
			List<Creature> out = new ArrayList<>();
			if (actorTexts.isEmpty()) return out;
			int n = Math.max(0, Math.min(shown, path.size()));
			long turn = n > 0 && n - 1 < pathTurns.size() ? pathTurns.get(n - 1) : actorTurns.get(0);
			int pick = 0;
			for (int i = 0; i < actorTurns.size(); i++) if (actorTurns.get(i) <= turn) pick = i;
			String text = actorTexts.get(pick);
			if (text.isEmpty()) return out;
			for (String part : text.split(";")){
				String[] f = part.split(":", 3);
				if (f.length != 3 || f[1].length() != 1) continue;
				try {
					int c = Integer.parseInt(f[0]);
					if (c >= 0 && c < cells.length()) out.add(new Creature(c, f[1].charAt(0), f[2]));
				} catch (NumberFormatException ignored){
				}
			}
			return out;
		}

		//the floor after the first `shown` steps (clamped to 0..path size): cells stepped on become VISITED, the latest one HERO.
		//With no steps shown nothing is marked. Pure; used by the replay window.
		public char[] frame( int shown ){
			char[] out = cells.toCharArray();
			int n = Math.max(0, Math.min(shown, path.size()));
			for (int i = 0; i < n; i++) out[path.get(i)] = VISITED;
			if (n > 0) out[path.get(n - 1)] = HERO;
			return out;
		}

		//"Floor 3: 412 steps"; quest floors say whose they are ("Floor 12 (Blacksmith floor)", "Floor 19 (Imp floor)")
		public String title(){
			return "Floor " + depth + place(depth, branch) + ": " + path.size() + (path.size() == 1 ? " step" : " steps")
					+ (mapped ? "" : ", no map recorded");
		}

		static String place( int depth, int branch ){
			if (branch == 0) return "";
			if (branch == 1 && depth >= 11 && depth <= 14) return " (Blacksmith floor)";
			if (branch == 1 && depth >= 16 && depth <= 19) return " (Imp floor)";
			return " (side floor)";
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
		Dataset stepSet = cat.dataset(FactDatasets.PREFIX + "move.step");
		if (maps == null && stepSet == null) return null;
		boolean finished = false;
		for (Dataset ds : new Dataset[]{ maps, stepSet }){
			if (ds == null) continue;
			for (Dataset.RunInfo ri : ds.runs){
				if (runId.equals(ri.runId) && ri.finished && !ri.duplicate) finished = true;
			}
		}
		if (!finished) return null;
		Map<Long, Floor> byFloor = new LinkedHashMap<>();
		if (maps != null) mapsOf(maps, runId, byFloor);
		RunReplay out = new RunReplay(runId, true);
		out.steps(cat, byFloor);
		if (byFloor.isEmpty()) return null;
		creaturesOf(cat, runId, byFloor);
		out.floors.addAll(byFloor.values());
		Collections.sort(out.floors, new Comparator<Floor>(){
			@Override
			public int compare( Floor a, Floor b ){
				return a.firstTurn != b.firstTurn ? (a.firstTurn < b.firstTurn ? -1 : 1) : (a.depth != b.depth ? a.depth - b.depth : a.branch - b.branch);
			}
		});
		return out;
	}

	private static void mapsOf( Dataset maps, String runId, Map<Long, Floor> byFloor ){
		DatasetDef md = maps.def;
		int tC = md.indexOf(FactDatasets.TURN), dC = md.indexOf("fact.v.depth"), bC = md.indexOf("fact.v.branch"),
				wC = md.indexOf("fact.v.width"), hC = md.indexOf("fact.v.height"), cC = md.indexOf("fact.t.cells");
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
	}

	private static void creaturesOf( Catalog cat, String runId, Map<Long, Floor> byFloor ){
		Dataset d = cat.dataset(FactDatasets.PREFIX + "floor.actors");
		if (d == null) return;
		DatasetDef dd = d.def;
		int tC = dd.indexOf(FactDatasets.TURN), dC = dd.indexOf("fact.v.depth"), bC = dd.indexOf("fact.v.branch"),
				wC = dd.indexOf("fact.v.width"), aC = dd.indexOf("fact.t.actors");
		List<Row> rows = new ArrayList<>();
		for (Row r : d.rowsOf(runId)){
			if (r.imported || r.duplicate || !num(r, dC) || !num(r, bC) || aC < 0) continue;
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
			if (f == null || (num(r, wC) && r.num(wC) != f.width)) continue;
			f.actorTurns.add(num(r, t) ? r.num(t) : 0L);
			f.actorTexts.add(r.isKnown(aC) ? r.asText(aC) : "");
		}
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
		//floors played before maps were kept: size them from their steps (width is on each step; height from the lowest cell)
		Map<Long, long[]> unmapped = new LinkedHashMap<>();   //key -> {width, highest cell, first turn}
		for (Row r : rows){
			long key = r.num(dC) * 1000L + r.num(bC);
			if (byFloor.containsKey(key) || !num(r, wC) || r.num(wC) <= 0) continue;
			long c = r.num(cC), w = r.num(wC), turn = num(r, tC) ? r.num(tC) : 0;
			if (c < 0) continue;
			long[] u = unmapped.get(key);
			if (u == null) unmapped.put(key, new long[]{ w, c, turn });
			else if (u[0] == w) u[1] = Math.max(u[1], c);
		}
		for (Map.Entry<Long, long[]> e : unmapped.entrySet()){
			long[] u = e.getValue();
			long h = u[1] / u[0] + 1;
			if (u[0] * h > FloorMaps.MAX_CELLS) continue;
			byFloor.put(e.getKey(), new Floor((int) (e.getKey() / 1000L), (int) (e.getKey() % 1000L), (int) u[0], (int) h, u[2]));
		}
		for (Row r : rows){
			Floor f = byFloor.get(r.num(dC) * 1000L + r.num(bC));
			if (f == null) continue;
			if (num(r, wC) && r.num(wC) != f.width) continue;   //a step on a floor of another shape is not this map's
			long c = r.num(cC);
			if (c < 0 || c >= f.cells.length()) continue;
			//order floors by when they were first walked: a map is only recorded when a session loads the floor, so a floor
			//first played on an older build and revisited later got its map late and sorted after floors played after it
			if (f.path.isEmpty() && num(r, t)) f.firstTurn = Math.min(f.firstTurn, r.num(t));
			f.path.add((int) c);
			f.pathTurns.add(num(r, t) ? r.num(t) : 0L);
		}
	}
}

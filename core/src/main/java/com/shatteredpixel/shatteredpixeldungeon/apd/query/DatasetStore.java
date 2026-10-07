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

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.Fact;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKeys;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ReadResult;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunIndex;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunLogReader;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.TelemetryVersion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Builds the Catalog from the raw logs: runs/*.apdlog (recorded runs) and legacy/*.apdlog (imported ranking records).
//
//The raw logs are the only source of truth. Each log's rows are cached under derived/rows/<log>.apdr, valid only while that log has
//exactly the size it had (logs are append-only) and the builder is the same version, so a new run or a run that went on costs
//one log to read, not the whole history. A missing, stale, damaged or older cache is rebuilt silently from the log, and deleting
//the whole folder loses nothing. Rows are assembled and merged here every time (cheap), never cached merged.
public final class DatasetStore {

	public static final String ROWS_DIR = "derived/rows";

	private final File root;

	public DatasetStore( File root ){
		this.root = root;
	}

	public File rowsDir(){
		return new File(root, ROWS_DIR);
	}

	private static Map<String, DatasetDef> defs(){
		Map<String, DatasetDef> m = new LinkedHashMap<>();
		for (String id : new String[]{Datasets.RUNS, Datasets.ITEMS, Datasets.ENEMIES, Datasets.DEATHS}) m.put(id, RowBuilders.def(id));
		return m;
	}

	private static List<File> logs( File dir ){
		List<File> out = new ArrayList<>();
		File[] files = dir.listFiles();
		if (files != null){
			Arrays.sort(files);
			for (File f : files) if (f.isFile() && f.getName().endsWith(RunIndex.LOG_SUFFIX)) out.add(f);
		}
		return out;
	}

	private static String idOf( File f ){
		return f.getName().substring(0, f.getName().length() - RunIndex.LOG_SUFFIX.length());
	}

	public void deleteCache(){
		File[] files = rowsDir().listFiles();
		if (files != null) for (File f : files) f.delete();
	}

	//Never throws.
	public Catalog load(){
		long t0 = System.nanoTime();
		Catalog cat = new Catalog();
		Map<String, DatasetDef> defs = defs();
		List<LogRows> recorded = new ArrayList<>();
		List<LogRows> imported = new ArrayList<>();
		for (File f : logs(new File(root, "runs"))){
			LogRows lr = rowsFor(f, false, defs, cat);
			if (lr != null) recorded.add(lr);
		}
		for (File f : logs(new File(root, "legacy"))){
			LogRows lr = rowsFor(f, true, defs, cat);
			if (lr != null) imported.add(lr);
		}
		assemble(cat, defs, recorded, imported);
		for (ViewDef v : Datasets.allViews()) cat.add(v);
		cat.millis = (System.nanoTime() - t0) / 1000000L;
		return cat;
	}

	private LogRows rowsFor( File log, boolean legacy, Map<String, DatasetDef> defs, Catalog cat ){
		String id = idOf(log);
		long length = log.length();
		File cache = new File(rowsDir(), id + ".apdr");
		if (cache.isFile()){
			try {
				LogRows lr = RowCodec.decode(readAll(cache), length, defs);
				if (lr != null){
					cat.logsCached++;
					return lr;
				}
			} catch (Throwable ignored){
				//rebuilt below
			}
		}
		try {
			ReadResult r = RunLogReader.read(log);
			LogRows lr = legacy ? RowBuilders.fromLegacyLog(r, id) : RowBuilders.fromLog(r, id);
			cat.logsRebuilt++;
			writeCache(cache, RowCodec.encode(lr, length, defs));
			return lr;
		} catch (Throwable t){
			return null;
		}
	}

	private static byte[] readAll( File f ) throws IOException {
		FileInputStream in = new FileInputStream(f);
		try {
			byte[] data = new byte[(int) f.length()];
			int off = 0;
			while (off < data.length){
				int n = in.read(data, off, data.length - off);
				if (n < 0) throw new IOException("short read");
				off += n;
			}
			return data;
		} finally {
			in.close();
		}
	}

	private void writeCache( File cache, byte[] data ){
		File tmp = new File(cache.getPath() + ".tmp");
		try {
			File dir = cache.getParentFile();
			if (!dir.isDirectory() && !dir.mkdirs()) return;
			FileOutputStream out = new FileOutputStream(tmp);
			try {
				out.write(data);
			} finally {
				out.close();
			}
			if (cache.exists() && !cache.delete()) return;
			tmp.renameTo(cache);
		} catch (Throwable ignored){
			tmp.delete();   //the cache is optional
		}
	}

	//---- assembly ----

	private static final class RunGroup {
		final String runId;
		final List<LogRows> logs = new ArrayList<>();
		boolean finished;
		String ruleset;
		boolean mixed;

		RunGroup( String runId ){
			this.runId = runId;
		}
	}

	private static void assemble( Catalog cat, Map<String, DatasetDef> defs, List<LogRows> recorded, List<LogRows> imported ){
		//segments of one run, in the order they were recorded
		Map<String, RunGroup> groups = new LinkedHashMap<>();
		for (LogRows lr : recorded){
			RunGroup g = groups.get(lr.meta.runId);
			if (g == null){
				g = new RunGroup(lr.meta.runId);
				groups.put(lr.meta.runId, g);
			}
			g.logs.add(lr);
		}
		for (RunGroup g : groups.values()){
			java.util.Collections.sort(g.logs, new Comparator<LogRows>(){
				@Override
				public int compare( LogRows a, LogRows b ){
					if (a.meta.createdAtMillis != b.meta.createdAtMillis) return a.meta.createdAtMillis < b.meta.createdAtMillis ? -1 : 1;
					return a.meta.segment - b.meta.segment;
				}
			});
			//a run is permanently finished only if its newest segment ended: a later segment is play that has not ended
			g.finished = g.logs.get(g.logs.size() - 1).meta.hasRunEnd;
			String ruleset = null;
			boolean first = true, unknown = false, mixed = false;
			for (LogRows lr : g.logs){
				String rs = lr.meta.ruleset;
				if (rs == null){ unknown = true; continue; }
				if (rs.equals(Row.RULESET_MIXED)){ mixed = true; continue; }
				if (first){ ruleset = rs; first = false; }
				else if (!ruleset.equals(rs)) mixed = true;
			}
			g.ruleset = mixed ? Row.RULESET_MIXED : unknown ? null : ruleset;
			if (g.logs.size() > 1 && unknown && !mixed) g.ruleset = null;
		}

		Map<String, List<Row>> byDataset = new LinkedHashMap<>();
		for (String id : defs.keySet()) byDataset.put(id, new ArrayList<Row>());
		for (RunGroup g : groups.values()){
			for (LogRows lr : g.logs){
				for (Map.Entry<String, List<Row>> e : lr.rows.entrySet()){
					List<Row> out = byDataset.get(e.getKey());
					if (out == null) continue;
					for (Row r : e.getValue()){
						r.finished = g.finished;
						r.ruleset = g.ruleset;
						out.add(r);
					}
				}
			}
		}
		Map<String, List<Row>> merged = new LinkedHashMap<>();
		for (Map.Entry<String, List<Row>> e : byDataset.entrySet()) merged.put(e.getKey(), RowMerger.mergeAll(defs.get(e.getKey()), e.getValue()));

		//imported ranking records (each once), and which of them are the same run as a recorded one
		DatasetDef runsDef = defs.get(Datasets.RUNS);
		List<Row> runRows = merged.get(Datasets.RUNS);
		List<Row> legacy = new ArrayList<>();
		Set<String> seen = new HashSet<>();
		for (LogRows lr : imported){
			for (Row r : lr.rows.containsKey(Datasets.RUNS) ? lr.rows.get(Datasets.RUNS) : new ArrayList<Row>()){
				if (seen.add(r.id)) legacy.add(r);
			}
		}
		markDuplicates(runsDef, runRows, legacy, groups);
		runRows.addAll(legacy);

		//ruleset list
		Map<String, int[]> rulesetRuns = new LinkedHashMap<>();
		Map<String, String> rulesetLabels = new LinkedHashMap<>();
		int ci = runsDef.indexOf(FactKeys.CHALLENGES), ei = runsDef.indexOf(FactKeys.EXOTIC_CHANCE);
		for (Row r : runRows){
			if (r.ruleset == null || r.ruleset.equals(Row.RULESET_MIXED)) continue;
			int[] n = rulesetRuns.get(r.ruleset);
			if (n == null){
				n = new int[1];
				rulesetRuns.put(r.ruleset, n);
				rulesetLabels.put(r.ruleset, "challenges " + (r.isKnown(ci) ? r.num(ci) : 0) + ", exotic " + (r.isKnown(ei) ? r.num(ei) : 0) + "%");
			}
			n[0]++;
		}
		for (Map.Entry<String, int[]> e : rulesetRuns.entrySet()) cat.rulesets.add(new Catalog.RulesetInfo(e.getKey(), rulesetLabels.get(e.getKey()), e.getValue()[0]));

		//datasets, with the runs each one can have rows for
		for (Map.Entry<String, List<Row>> e : merged.entrySet()){
			DatasetDef def = defs.get(e.getKey());
			List<Dataset.RunInfo> runs = new ArrayList<>();
			for (RunGroup g : groups.values()){
				boolean covered = def.minTelemetry == null;
				for (LogRows lr : g.logs){
					if (lr.meta.unreadable) continue;
					if (def.minTelemetry == null || TelemetryVersion.parse(lr.meta.telemetry).compareTo(def.minTelemetry) >= 0) covered = true;
				}
				runs.add(new Dataset.RunInfo(g.runId, g.finished, g.ruleset, covered, false));
			}
			for (Row r : legacy) runs.add(new Dataset.RunInfo(r.runId, true, null, e.getKey().equals(Datasets.RUNS), r.duplicate));
			cat.add(new Dataset(def, e.getValue(), runs));
		}
	}

	//An imported record is the same run as a recorded one when SPD's record id was noted by the run (exact), or, for older runs,
	//when exactly one finished run has the same class, outcome, deepest floor, level and score (probable). Ambiguous ones are not linked.
	private static void markDuplicates( DatasetDef def, List<Row> runRows, List<Row> legacy, Map<String, RunGroup> groups ){
		Set<String> rankingIds = new HashSet<>();
		for (RunGroup g : groups.values()) for (LogRows lr : g.logs) if (!lr.meta.rankingId.isEmpty()) rankingIds.add(lr.meta.rankingId);
		Set<String> linkedRuns = new HashSet<>();
		int cls = def.indexOf(FactKeys.HERO_CLASS), out = def.indexOf(FactKeys.OUTCOME), deep = def.indexOf(FactKeys.DEEPEST),
				lvl = def.indexOf(FactKeys.HERO_LEVEL), sc = def.indexOf(FactKeys.SCORE);
		//runs that already have an exact link cannot be a probable match for another record
		Map<String, String> runByRanking = new LinkedHashMap<>();
		for (RunGroup g : groups.values()) for (LogRows lr : g.logs) if (!lr.meta.rankingId.isEmpty()) runByRanking.put(lr.meta.rankingId, g.runId);
		for (Row leg : legacy){
			String gameId = leg.id.substring("legacy:".length());
			if (runByRanking.containsKey(gameId)){
				leg.duplicate = true;
				linkedRuns.add(runByRanking.get(gameId));
			}
		}
		for (Row leg : legacy){
			if (leg.duplicate) continue;
			Row only = null;
			int candidates = 0;
			for (Row r : runRows){
				if (linkedRuns.contains(r.runId) || hasRanking(groups, r.runId)) continue;
				if (!same(leg, r, cls, true) || !same(leg, r, out, false) || !same(leg, r, deep, false) || !same(leg, r, lvl, false)
						|| !same(leg, r, sc, false)) continue;
				candidates++;
				only = r;
			}
			if (candidates == 1){
				leg.duplicate = true;
				linkedRuns.add(only.runId);
			}
		}
	}

	private static boolean hasRanking( Map<String, RunGroup> groups, String runId ){
		RunGroup g = groups.get(runId);
		if (g == null) return false;
		for (LogRows lr : g.logs) if (!lr.meta.rankingId.isEmpty()) return true;
		return false;
	}

	private static boolean same( Row a, Row b, int col, boolean text ){
		if (col < 0 || !a.isKnown(col) || !b.isKnown(col)) return false;
		return text ? a.text(col).equals(b.text(col)) : a.num(col) == b.num(col);
	}
}

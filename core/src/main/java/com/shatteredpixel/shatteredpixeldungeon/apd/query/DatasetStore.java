/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Builds the Catalog from the raw logs: runs/*.apdlog (recorded runs) and legacy/*.apdlog (imported ranking records).
//
//The raw logs are the only source of truth. Each log's rows are cached under derived/rows/<log>.apdr, valid only while that log has
//the same byte length and SHA-256 content it had and the builder is the same version, so a new run or a run that went on costs
//one log to read, not the whole history. A missing, stale, damaged or older cache is rebuilt silently from the log, and deleting
//the whole folder loses nothing. Rows are assembled and merged here every time (cheap), never cached merged.
public final class DatasetStore {

	public static final String ROWS_DIR = "derived/rows";

	//INDEV-only synthetic demo data (see the demo package) is kept in its own folder, marked by this file, with logs named like this.
	//A folder of real history never reads a log named like demo data, and a demo folder reads nothing else, so the two never mix.
	public static final String DEMO_MARKER_FILE = "SYNTHETIC-DEMO-DATA.txt";
	public static final String DEMO_LOG_PREFIX = "demo-";

	private final File root;
	//Heavy datasets (play-by-play events and per-observation facts) are read from the row caches only when asked for (LazyRows).
	//Tests turn this off to compare against building everything at once.
	private boolean lazyHeavy = true;

	public DatasetStore( File root ){
		this.root = root;
	}

	DatasetStore buildEverythingAtOnce(){
		lazyHeavy = false;
		return this;
	}

	private static final RowCodec.Keep LIGHT = new RowCodec.Keep(){
		@Override
		public boolean keep( String datasetId ){
			return light(datasetId);
		}
	};

	//The datasets every UIMSD screen opens with. Everything else is heavy and read when asked for.
	static boolean light( String datasetId ){
		return Datasets.RUNS.equals(datasetId) || Datasets.ITEMS.equals(datasetId)
				|| Datasets.ENEMIES.equals(datasetId) || Datasets.DEATHS.equals(datasetId);
	}

	public File rowsDir(){
		return new File(root, ROWS_DIR);
	}

	private static Map<String, DatasetDef> defs(){
		Map<String, DatasetDef> m = new LinkedHashMap<>();
		for (String id : new String[]{Datasets.RUNS, Datasets.ITEMS, Datasets.ENEMIES, Datasets.DEATHS}) m.put(id, RowBuilders.def(id));
		for (String id : EventDatasets.IDS) m.put(id, EventDatasets.def(id));
		for (DatasetDef d : FactDatasets.defs()) m.put(d.id, d);
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
		// Invalidation must not remove staging files owned by an in-flight writer.
        // Otherwise that writer can delete a newer committed cache and fail its own rename.
        if (files != null) for (File f : files) if (f.getName().endsWith(".apdr")) f.delete();
	}

	//Never throws.
    public interface Cancellation {boolean cancelled();}
    public Catalog load(){return load(() -> false);}
    public Catalog load(Cancellation cancelled){return load(cancelled, null);}
    /** Only the one recorded run's log (no ranking imports): what a single-run window such as "What happened" needs,
     *  without reading every log in the history. Unknown or empty ids give an empty catalog. Never throws. */
    public Catalog loadRun(String runId){return load(() -> false, runId == null ? "" : runId);}
    private Catalog load(Cancellation cancelled, String onlyRun){
        checkCancelled(cancelled);
		long t0 = System.nanoTime();
		Catalog cat = new Catalog();
		Map<String, DatasetDef> defs = defs();
		byte[] fingerprintBuffer = new byte[32 * 1024];
		List<LogRows> recorded = new ArrayList<>();
		List<LogRows> imported = new ArrayList<>();
		boolean demoFolder = new File(root, DEMO_MARKER_FILE).isFile();
		cat.synthetic = demoFolder;
		for (File f : logs(new File(root, "runs"))){
            checkCancelled(cancelled);
			if (idOf(f).startsWith(DEMO_LOG_PREFIX) != demoFolder) continue;
			if (onlyRun != null && !idOf(f).equals(onlyRun)) continue;
			LogRows lr = rowsFor(f, false, defs, cat, fingerprintBuffer);
			if (lr != null) recorded.add(lr);
		}
		for (File f : onlyRun != null ? new ArrayList<File>() : logs(new File(root, "legacy"))){
            checkCancelled(cancelled);
			if (idOf(f).startsWith(DEMO_LOG_PREFIX) != demoFolder) continue;
			LogRows lr = rowsFor(f, true, defs, cat, fingerprintBuffer);
			if (lr != null) imported.add(lr);
		}
        checkCancelled(cancelled);
		assemble(cat, defs, recorded, imported);
        checkCancelled(cancelled);
		for (ViewDef v : Datasets.allViews()) cat.add(v);
		//every generated dataset that has rows gets a table, under the topic it belongs to
		for (Dataset ds : cat.datasets()){
			if (ds.rows.isEmpty()) continue;
			if (EventDatasets.isEventDataset(ds.def.id)) cat.add(Datasets.generatedView(ds.def, "Play by play"));
			else if (FactDatasets.isFactDataset(ds.def.id)){
				com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKind k = com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKinds.get(ds.def.id.substring(FactDatasets.PREFIX.length()));
				cat.add(Datasets.generatedView(ds.def, "Details: " + (k == null ? "Other" : k.area)));
			}
		}
		cat.millis = (System.nanoTime() - t0) / 1000000L;
		return cat;
	}

    private static void checkCancelled(Cancellation cancelled){
        if(cancelled.cancelled())throw new java.util.concurrent.CancellationException("Catalog load retired for gameplay");
    }

	private LogRows rowsFor( File log, boolean legacy, Map<String, DatasetDef> defs, Catalog cat, byte[] fingerprintBuffer ){
		String id = idOf(log);
		long length = log.length();
		byte[] sourceFingerprint = null;
		try { sourceFingerprint = fingerprint(log, fingerprintBuffer); } catch (IOException ignored) { }
		// Recorded and ranking-import logs may legitimately share a filename.
		File cache = new File(rowsDir(), (legacy ? "legacy-" : "runs-") + id + ".apdr");
		if (sourceFingerprint != null && cache.isFile()){
			try {
				byte[] stored = readAll(cache);
				boolean compressed = isGzip(stored);
				LogRows lr = RowCodec.decode(compressed ? gunzip(stored) : stored, length, defs, sourceFingerprint,
						lazyHeavy ? LIGHT : null);
				if (lr != null){
					remember(lr, log, id, legacy, sourceFingerprint, length);
					cat.logsCached++;
					//APD: older uncompressed caches are rewritten compressed the first time they are used
					if (!compressed) writeCache(cache, gzip(stored));
					return lr;
				}
			} catch (IOException | RuntimeException ignored){
				//rebuilt below
			}
		}
		try {
			ReadResult r = RunLogReader.read(log);
			LogRows lr = legacy ? RowBuilders.fromLegacyLog(r, id) : RowBuilders.fromLog(r, id);
			cat.logsRebuilt++;
			// Do not bind rows to a different snapshot if the raw log changed while being read.
			try {
				if (sourceFingerprint != null && length == log.length()
						&& Arrays.equals(sourceFingerprint, fingerprint(log, fingerprintBuffer)))
					writeCache(cache, gzip(RowCodec.encode(lr, length, defs, sourceFingerprint)));
			} catch (IOException ignored) { /* The cache is optional; retain the actual parsed rows. */ }
			remember(lr, log, id, legacy, sourceFingerprint, length);
			if (lazyHeavy){
				for (java.util.Iterator<Map.Entry<String, List<Row>>> it = lr.rows.entrySet().iterator(); it.hasNext(); ){
					Map.Entry<String, List<Row>> e = it.next();
					lr.counts.put(e.getKey(), e.getValue().size());
					if (!light(e.getKey())) it.remove();
				}
			}
			return lr;
		} catch (IOException | RuntimeException t){
			return null;
		}
	}

	private static void remember( LogRows lr, File log, String id, boolean legacy, byte[] fingerprint, long length ){
		lr.source = log;
		lr.fileId = id;
		lr.legacy = legacy;
		lr.fingerprint = fingerprint;
		lr.length = length;
	}

	private static byte[] fingerprint(File file, byte[] buffer) throws IOException {
		java.security.MessageDigest digest;
		try { digest = java.security.MessageDigest.getInstance("SHA-256"); }
		catch (java.security.NoSuchAlgorithmException unavailable) { throw new IOException(unavailable); }
		try (FileInputStream in = new FileInputStream(file)) {
			int count;
			while ((count = in.read(buffer)) != -1) {
				if (count != 0) digest.update(buffer, 0, count);
			}
		}
		return digest.digest();
	}

	//APD: row caches are rebuildable copies, stored gzip-compressed (plain caches still load)
	static byte[] gzip( byte[] data ){
		try {
			java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream(Math.max(1024, data.length / 4));
			java.util.zip.GZIPOutputStream zip = new java.util.zip.GZIPOutputStream(bytes, 64 * 1024){
				{ def.setLevel(java.util.zip.Deflater.BEST_COMPRESSION); }
			};
			zip.write(data);
			zip.finish();
			zip.close();
			return bytes.toByteArray();
		} catch (IOException impossible){
			return data;
		}
	}

	/** The decoded cache bytes, whether stored compressed or plain. */
	public static byte[] plainCache( byte[] stored ) throws IOException {
		return isGzip(stored) ? gunzip(stored) : stored;
	}

	public static boolean isGzip( byte[] data ){
		return data.length >= 2 && (data[0] & 0xff) == 0x1f && (data[1] & 0xff) == 0x8b;
	}

	// Decompresses into one array of exactly the right size. The gzip trailer's last 4 bytes give the uncompressed size
	// (mod 4 GiB); trusting it only as a starting size and checking the real length keeps a damaged trailer safe.
	// The old growable-buffer version doubled and copied as it grew and copied once more at the end, so a big run's cache
	// briefly needed 3-4x its size and ran the 256 MB heap out (Chris's diagnostic, Nov 18: 13 MB -> 222 MB in 0.2 s).
	public static byte[] gunzip( byte[] data ) throws IOException {
		long trailer = data.length >= 4 ? ((data[data.length - 4] & 0xffL) | (data[data.length - 3] & 0xffL) << 8
				| (data[data.length - 2] & 0xffL) << 16 | (data[data.length - 1] & 0xffL) << 24) : 0;
		if (trailer > Integer.MAX_VALUE - 16) throw new IOException("row cache too large");
		// Deflate can't expand more than ~1032x, so a bigger claim means a damaged trailer: don't pre-allocate it
		// (a forged size used to allocate hundreds of MB and crash with OutOfMemoryError; Nov 18 CI).
		if (trailer > data.length * 1032L + 1024) trailer = 0;
		try (java.util.zip.GZIPInputStream in = new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(data), 64 * 1024)) {
			byte[] out = new byte[(int) trailer];
			int off = 0, n;
			while (off < out.length && (n = in.read(out, off, out.length - off)) > 0) off += n;
			if (off == out.length && in.read() == -1) return out;
			// The trailer was wrong (damaged or concatenated stream): fall back to growing, from what was read so far.
			java.io.ByteArrayOutputStream grow = new java.io.ByteArrayOutputStream(Math.max(1024, off * 2));
			grow.write(out, 0, off);
			if (off == out.length) { int b = in.read(); if (b != -1) grow.write(b); }
			byte[] buffer = new byte[64 * 1024];
			while ((n = in.read(buffer)) != -1) grow.write(buffer, 0, n);
			return grow.toByteArray();
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
		File tmp = null;
		try {
			File dir = cache.getParentFile();
			if (!dir.isDirectory() && !dir.mkdirs() && !dir.isDirectory()) return;
			tmp = File.createTempFile(cache.getName() + ".", ".tmp", dir);
			FileOutputStream out = new FileOutputStream(tmp);
			try {
				out.write(data);
			} finally {
				out.close();
			}
			if (cache.exists() && !cache.delete()) return;
			tmp.renameTo(cache);
		} catch (IOException | RuntimeException ignored){
			// The cache is optional; no writer may remove another writer's temporary file.
		} finally {
			if (tmp != null) try { tmp.delete(); } catch (SecurityException ignored) { }
		}
	}

	//---- assembly ----

	private static final class RunGroup {
		final String runId;
		final List<LogRows> logs = new ArrayList<>();
		boolean finished;
		String ruleset;

		RunGroup( String runId ){
			this.runId = runId;
		}
	}

	private void assemble( Catalog cat, Map<String, DatasetDef> defs, List<LogRows> recorded, List<LogRows> imported ){
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
		Map<String, Row> byRun = new HashMap<>();
		for (Row r : runRows) byRun.put(r.runId, r);
		copyRunFacts(defs, merged, runsDef, byRun);
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
		int ci = runsDef.indexOf(FactKeys.CHALLENGES), ei = runsDef.indexOf(FactKeys.EXOTIC_CHANCE), di = runsDef.indexOf(FactKeys.DIFFICULTY);
		Map<String, java.util.TreeSet<Long>> rulesetLevels = new LinkedHashMap<>();
		java.util.Set<String> rulesetUnrecorded = new java.util.HashSet<>();
		for (Row r : runRows){
			if (r.ruleset == null || r.ruleset.equals(Row.RULESET_MIXED)) continue;
			int[] n = rulesetRuns.get(r.ruleset);
			if (n == null){
				n = new int[1];
				rulesetRuns.put(r.ruleset, n);
				rulesetLabels.put(r.ruleset, "challenges " + (r.isKnown(ci) ? r.num(ci) : 0) + ", exotic " + (r.isKnown(ei) ? r.num(ei) : 0) + "%");
			}
			n[0]++;
			//difficulty is part of the ruleset: name the recorded ones (runs of the standard difficulty share their ruleset with older runs
			//that never recorded one, so those show as "not recorded")
			if (di >= 0){
				java.util.TreeSet<Long> set = rulesetLevels.get(r.ruleset);
				if (set == null) rulesetLevels.put(r.ruleset, set = new java.util.TreeSet<>());
				if (r.isKnown(di)) set.add(r.num(di)); else rulesetUnrecorded.add(r.ruleset);
			}
		}
		for (Map.Entry<String, int[]> e : rulesetRuns.entrySet()){
			String label = rulesetLabels.get(e.getKey());
			java.util.TreeSet<Long> levels = rulesetLevels.get(e.getKey());
			if (levels != null && !levels.isEmpty()){
				StringBuilder d = new StringBuilder();
				for (Long l : levels) d.append(d.length() > 0 ? "/" : "").append(l);
				label += ", difficulty " + d + (rulesetUnrecorded.contains(e.getKey()) ? " (older runs: not recorded)" : "");
			}
			cat.rulesets.add(new Catalog.RulesetInfo(e.getKey(), label, e.getValue()[0]));
		}

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
			if (lazyHeavy && !light(def.id)){
				int expected = 0;
				for (RunGroup g : groups.values()) for (LogRows lr : g.logs) expected += lr.count(def.id);
				HeavySource src = new HeavySource(def, defs, rowsDir(), new ArrayList<>(groups.values()),
						RunFactCopier.forDataset(def, runsDef, byRun));
				cat.add(new Dataset(def, new LazyRows(expected, src), runs));
			} else cat.add(new Dataset(def, e.getValue(), runs));
		}
		//the catalog's rows are in their datasets now; what each log keeps is only what reading it again needs
		for (RunGroup g : groups.values()) for (LogRows lr : g.logs) lr.rows.clear();
	}

	//Reads one heavy dataset's rows from the row caches, exactly as building everything at once would have assembled them:
	//the same logs in the same order, the run's finished state and ruleset, merged, with the run's own facts copied in.
	private static final class HeavySource implements LazyRows.Source {
		private final DatasetDef def;
		private final Map<String, DatasetDef> defs;
		private final File rowsDir;
		private final List<RunGroup> groups;
		private final RunFactCopier copier;

		HeavySource( DatasetDef def, Map<String, DatasetDef> defs, File rowsDir, List<RunGroup> groups, RunFactCopier copier ){
			this.def = def;
			this.defs = defs;
			this.rowsDir = rowsDir;
			this.groups = groups;
			this.copier = copier;
		}

		@Override
		public List<Row> load( String onlyRun ){
			ColumnarRows.Builder compact = new ColumnarRows.Builder(def.size());
			List<Row> plain = null;
			for (RunGroup g : groups){
				if (onlyRun != null && !onlyRun.equals(g.runId)) continue;
				for (LogRows lr : g.logs){
					if (lr.count(def.id) == 0) continue;
					for (Row r : reread(lr)){
						r.finished = g.finished;
						r.ruleset = g.ruleset;
						if (copier != null) copier.apply(r);
						if (plain == null && (r.mergeKey != null || !compact.add(r))){
							plain = new ArrayList<>(compact.build());
							compact = null;
						}
						if (plain != null) plain.add(r);
					}
				}
			}
			if (plain == null) return compact.build();
			//rows that merge across segments (or a cell the compact store can't keep): the plain path, as building at once does
			plain = RowMerger.mergeAll(def, plain);
			if (copier != null) for (Row r : plain) copier.apply(r);
			ColumnarRows c = ColumnarRows.of(def.size(), plain);
			return c == null ? plain : c;
		}

		private List<Row> reread( LogRows lr ){
			if (lr.fingerprint != null){
				File cache = new File(rowsDir, (lr.legacy ? "legacy-" : "runs-") + lr.fileId + ".apdr");
				if (cache.isFile()){
					try {
						LogRows c = RowCodec.decode(plainCache(readAll(cache)), lr.length, defs, lr.fingerprint, new RowCodec.Keep(){
							@Override
							public boolean keep( String datasetId ){
								return def.id.equals(datasetId);
							}
						});
						if (c != null){
							List<Row> l = c.rows.get(def.id);
							return l == null ? new ArrayList<Row>() : l;
						}
					} catch (IOException | RuntimeException ignored){
						//read from the raw log below
					}
				}
			}
			try {
				ReadResult r = RunLogReader.read(lr.source);
				LogRows built = lr.legacy ? RowBuilders.fromLegacyLog(r, lr.fileId) : RowBuilders.fromLog(r, lr.fileId);
				List<Row> l = built.rows.get(def.id);
				return l == null ? new ArrayList<Row>() : l;
			} catch (IOException | RuntimeException unreadable){
				return new ArrayList<>();
			}
		}
	}

	//The run facts a dataset's rows carry (see copyRunFacts), worked out once per dataset.
	static final class RunFactCopier {
		private final Map<String, Row> byRun;
		private final int[] to, from;
		private final boolean[] numOnly;

		private RunFactCopier( Map<String, Row> byRun, int[] to, int[] from, boolean[] numOnly ){
			this.byRun = byRun;
			this.to = to;
			this.from = from;
			this.numOnly = numOnly;
		}

		static RunFactCopier forDataset( DatasetDef d, DatasetDef runsDef, Map<String, Row> byRun ){
			if (d == null || d.id.equals(Datasets.RUNS)) return null;
			String prefix = null;
			for (ColumnDef c : d.columns()){
				if (c.id.endsWith(".runClass")){
					prefix = c.id.substring(0, c.id.length() - ".runClass".length());
					break;
				}
			}
			if (prefix == null) return null;
			List<int[]> pairs = new ArrayList<>();
			for (String[] fact : Datasets.RUN_FACTS){
				int t = d.indexOf(prefix + "." + fact[0]);
				int f = runsDef.indexOf(fact[1]);
				if (t >= 0 && f >= 0) pairs.add(new int[]{ t, f, 0 });
			}
			int t = d.indexOf(prefix + ".runDifficulty");
			int f = runsDef.indexOf(FactKeys.DIFFICULTY);
			if (t >= 0 && f >= 0) pairs.add(new int[]{ t, f, 1 });
			int[] to = new int[pairs.size()], from = new int[pairs.size()];
			boolean[] numOnly = new boolean[pairs.size()];
			for (int i = 0; i < pairs.size(); i++){
				to[i] = pairs.get(i)[0];
				from[i] = pairs.get(i)[1];
				numOnly[i] = pairs.get(i)[2] == 1;
			}
			return new RunFactCopier(byRun, to, from, numOnly);
		}

		void apply( Row r ){
			Row run = byRun.get(r.runId);
			if (run == null) return;
			for (int i = 0; i < to.length; i++){
				if (!run.isKnown(from[i])) continue;
				if (!numOnly[i] && run.kind(from[i]) == Row.K_TEXT) r.setText(to[i], run.text(from[i]), run.prov(from[i]));
				else r.setNum(to[i], run.num(from[i]), run.prov(from[i]));
			}
		}
	}

	//Item and enemy rows carry a few facts of their run (how it ended, the class, the deepest floor, the challenges) so that the
	//outcome of the runs they belong to can be asked of the rows themselves. They are the run's own values with the run's own
	//provenance, and unknown wherever the run's are.
	private static void copyRunFacts( Map<String, DatasetDef> defs, Map<String, List<Row>> merged, DatasetDef runsDef, Map<String, Row> byRun ){
		for (Map.Entry<String, List<Row>> e : merged.entrySet()){
			RunFactCopier copier = RunFactCopier.forDataset(defs.get(e.getKey()), runsDef, byRun);
			if (copier != null) for (Row r : e.getValue()) copier.apply(r);
		}
	}

	//An imported record is the same run as a recorded one when SPD's record id was noted by the run (exact), or, for older runs,
	//when exactly one finished run has the same class, outcome, deepest floor, level and score (probable). Ambiguous ones are not linked.
	private static void markDuplicates( DatasetDef def, List<Row> runRows, List<Row> legacy, Map<String, RunGroup> groups ){
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


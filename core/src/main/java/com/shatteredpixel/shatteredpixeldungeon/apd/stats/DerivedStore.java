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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//The derived fact table and its cache file (derived/runfacts.apdd).
//
//The raw logs (runs/ and legacy/) are the only source of truth. The cache is a convenience: it is only trusted while the
//size of every source file is exactly what it was when the cache was written. A missing, stale, damaged or older-version
//cache is silently rebuilt from the raw files, and a rebuilt table is identical to a cached one. The cache can be deleted
//at any time.
public final class DerivedStore {

	//bump when facts are derived differently, so every old cache is rebuilt
	public static final int DERIVATION_VERSION = 4;

	public static final String CACHE_DIR = "derived";
	public static final String CACHE_FILE = "runfacts.apdd";
	public static final String LEGACY_DIR = "legacy";

	public enum Source { CACHE, REBUILT_NO_CACHE, REBUILT_STALE, REBUILT_BAD_CACHE }

	private final File root;
	private Source lastSource;
	private long lastMillis;

	public DerivedStore( File root ){
		this.root = root;
	}

	public File cacheFile(){
		return new File(new File(root, CACHE_DIR), CACHE_FILE);
	}

	public File legacyDir(){
		return new File(root, LEGACY_DIR);
	}

	public Source lastSource(){
		return lastSource;
	}

	public long lastMillis(){
		return lastMillis;
	}

	public void deleteCache(){
		cacheFile().delete();
	}

	private List<File> logs( File dir ){
		List<File> out = new ArrayList<>();
		File[] files = dir.listFiles();
		if (files != null){
			Arrays.sort(files);
			for (File f : files){
				if (f.isFile() && f.getName().endsWith(RunIndex.LOG_SUFFIX)) out.add(f);
			}
		}
		return out;
	}

	private List<String> fingerprints(){
		List<String> out = new ArrayList<>();
		for (File f : logs(new File(root, "runs"))) out.add("run\t" + f.getName() + "\t" + f.length());
		for (File f : logs(legacyDir())) out.add("legacy\t" + f.getName() + "\t" + f.length());
		return out;
	}

	//The table, from the cache when it is current, otherwise rebuilt (and the cache rewritten). Never throws.
	public synchronized FactTable load(){
		long t0 = System.nanoTime();
		List<String> prints = fingerprints();
		File cache = cacheFile();
		FactTable table = null;
		if (!cache.isFile()){
			lastSource = Source.REBUILT_NO_CACHE;
		} else {
			try {
				table = readCache(cache, prints);
				lastSource = table != null ? Source.CACHE : Source.REBUILT_STALE;
			} catch (Throwable t){
				table = null;
				lastSource = Source.REBUILT_BAD_CACHE;
			}
		}
		if (table == null){
			table = build();
			writeCache(table, prints);
		}
		lastMillis = (System.nanoTime() - t0) / 1000000L;
		return table;
	}

	//Always derives from the raw files and rewrites the cache.
	public synchronized FactTable rebuild(){
		long t0 = System.nanoTime();
		List<String> prints = fingerprints();
		FactTable table = build();
		writeCache(table, prints);
		lastSource = Source.REBUILT_STALE;
		lastMillis = (System.nanoTime() - t0) / 1000000L;
		return table;
	}

	//Derives the table from the raw files without touching the cache.
	public synchronized FactTable build(){
		FactTable table = new FactTable();
		for (File f : logs(new File(root, "runs"))){
			String id = f.getName().substring(0, f.getName().length() - RunIndex.LOG_SUFFIX.length());
			try {
				table.apdRows.add(RunFactsBuilder.fromLog(RunLogReader.read(f), id));
			} catch (Throwable t){
				RunFacts bad = new RunFacts(RunFacts.SOURCE_APD, id);
				bad.text(FactKeys.LOG_ID, id, Fact.PROV_RECORDED);
				bad.text(FactKeys.STATUS, "unreadable", Fact.PROV_RECORDED);
				table.apdRows.add(bad);
			}
		}
		readLegacy(table);
		table.link();
		return table;
	}

	//only the imported records, totals and daily scores (a few small files): cheap enough to ask for at any time
	public synchronized FactTable buildLegacyOnly(){
		FactTable table = new FactTable();
		readLegacy(table);
		return table;
	}

	private void readLegacy( FactTable table ){
		LegacyEvents.Totals latest = null;
		java.util.Set<String> seenIds = new java.util.HashSet<>();
		java.util.Set<Long> seenDays = new java.util.HashSet<>();
		for (File f : logs(legacyDir())){
			String id = f.getName().substring(0, f.getName().length() - RunIndex.LOG_SUFFIX.length());
			ReadResult r;
			try {
				r = RunLogReader.read(f);
			} catch (Throwable t){
				continue;
			}
			for (ApdEvent e : r.events){
				try {
					if (e.type == EventTypes.LEGACY_RECORD){
						LegacyEvents.Record rec = LegacyEvents.decodeRecord(e, r);
						if (seenIds.add(rec.gameId)) table.legacyRows.add(LegacyFacts.fromRecord(rec, id));
					} else if (e.type == EventTypes.LEGACY_TOTALS){
						LegacyEvents.Totals t = LegacyEvents.decodeTotals(e, r);
						if (latest == null || t.importedAtMillis >= latest.importedAtMillis) latest = t;
					} else if (e.type == EventTypes.LEGACY_DAILY_SCORE){
						LegacyEvents.DailyScore d = LegacyEvents.decodeDailyScore(e, r);
						if (seenDays.add(d.day)) table.dailyScores.add(new long[]{d.day, d.score});
					}
				} catch (RuntimeException bad){
					//skip an event this reader cannot decode
				}
			}
		}
		if (latest != null) table.legacyTotals = LegacyFacts.fromTotals(latest);
	}

	//---- cache file ----------------------------------------------------------------------------------------------

	private static String esc( String s ){
		StringBuilder b = new StringBuilder(s.length() + 4);
		for (int i = 0; i < s.length(); i++){
			char c = s.charAt(i);
			switch (c){
				case '\\': b.append("\\\\"); break;
				case '\t': b.append("\\t"); break;
				case '\n': b.append("\\n"); break;
				case '\r': b.append("\\r"); break;
				default: b.append(c);
			}
		}
		return b.toString();
	}

	private static String unesc( String s ){
		if (s.indexOf('\\') < 0) return s;
		StringBuilder b = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++){
			char c = s.charAt(i);
			if (c == '\\' && i + 1 < s.length()){
				char n = s.charAt(++i);
				switch (n){
					case 't': b.append('\t'); break;
					case 'n': b.append('\n'); break;
					case 'r': b.append('\r'); break;
					default: b.append(n);
				}
			} else {
				b.append(c);
			}
		}
		return b.toString();
	}

	private static void writeFact( Writer w, String tag, String key, Fact f ) throws IOException {
		w.write(tag + "\t" + esc(key) + "\t" + f.kind + "\t" + f.prov + "\t" + (f.isNum() ? Long.toString(f.num) : esc(f.text)) + "\n");
	}

	private void writeCache( FactTable t, List<String> prints ){
		File cache = cacheFile();
		File tmp = new File(cache.getPath() + ".tmp");
		try {
			File dir = cache.getParentFile();
			if (!dir.isDirectory() && !dir.mkdirs()) return;
			Writer w = new OutputStreamWriter(new FileOutputStream(tmp), "UTF-8");
			try {
				w.write("APDD\t1\t" + DERIVATION_VERSION + "\n");
				for (String p : prints) w.write("S\t" + p + "\n");
				if (t.legacyTotals != null){
					for (java.util.Map.Entry<String, Fact> e : t.legacyTotals.all().entrySet()) writeFact(w, "T", e.getKey(), e.getValue());
				}
				for (long[] d : t.dailyScores) w.write("D\t" + d[0] + "\t" + d[1] + "\n");
				for (RunFacts r : t.apdRows) writeRow(w, r);
				for (RunFacts r : t.legacyRows) writeRow(w, r);
				w.write("END\n");
			} finally {
				w.close();
			}
			if (cache.exists() && !cache.delete()) return;
			tmp.renameTo(cache);
		} catch (Throwable ignored){
			tmp.delete();  //the cache is optional
		}
	}

	private static void writeRow( Writer w, RunFacts r ) throws IOException {
		w.write("R\t" + r.source + "\t" + esc(r.id) + "\n");
		for (java.util.Map.Entry<String, Fact> e : r.all().entrySet()) writeFact(w, "F", e.getKey(), e.getValue());
	}

	//null when the cache does not describe the current files, an exception when it is damaged
	private FactTable readCache( File cache, List<String> prints ) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(cache), java.nio.charset.StandardCharsets.UTF_8.newDecoder()
				.onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
				.onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)));
		try {
			String header = in.readLine();
			if (header == null || !header.equals("APDD\t1\t" + DERIVATION_VERSION)){
				if (header != null && header.startsWith("APDD\t1\t")) return null;  //an older derivation: rebuild
				throw new IOException("not a fact cache");
			}
			List<String> cached = new ArrayList<>();
			FactTable t = new FactTable();
			RunFacts current = null;
			RunFacts totals = null;
			boolean ended = false;
			String line;
			while ((line = in.readLine()) != null){
				if (line.equals("END")){
					ended = true;
					break;
				}
				String[] p = line.split("\t", -1);
				switch (p[0].charAt(0)){
					case 'S':
						cached.add(line.substring(2));
						break;
					case 'T':
						if (totals == null) totals = new RunFacts(RunFacts.SOURCE_LEGACY, "legacy-totals");
						totals.put(unesc(p[1]), readFact(p));
						break;
					case 'D':
						t.dailyScores.add(new long[]{Long.parseLong(p[1]), Long.parseLong(p[2])});
						break;
					case 'R':
						current = new RunFacts(Integer.parseInt(p[1]), unesc(p[2]));
						if (current.source == RunFacts.SOURCE_APD) t.apdRows.add(current);
						else t.legacyRows.add(current);
						break;
					case 'F':
						if (current == null) throw new IOException("fact before row");
						current.put(unesc(p[1]), readFact(p));
						break;
					default:
						throw new IOException("bad line");
				}
			}
			if (!ended) throw new IOException("truncated cache");
			if (!cached.equals(prints)) return null;
			t.legacyTotals = totals;
			return t;
		} finally {
			in.close();
		}
	}

	private static Fact readFact( String[] p ){
		int kind = Integer.parseInt(p[2]);
		int prov = Integer.parseInt(p[3]);
		if (kind == Fact.NUM) return Fact.num(Long.parseLong(p[4]), prov);
		if (kind == Fact.TEXT) return Fact.text(unesc(p[4]), prov);
		throw new IllegalArgumentException("bad fact kind");
	}
}


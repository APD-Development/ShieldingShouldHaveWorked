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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//The rows of one dataset kept compactly. Every cell is kept exactly (its kind, provenance and value); nothing is summarised.
//
//A plain Row costs about 340 bytes on a phone's heap (an object, three arrays and its own id string), so twenty long runs of
//step-by-step facts took 247 MB (measured, Nov 23). Here each column keeps a dictionary of the distinct cells it holds and one
//small code per row (a byte while the column has at most 256 distinct cells, then a char, then an int), the row's run, version,
//ruleset and flags are one shared header code, and the id is kept as its shared prefix and suffix plus the row's number
//("run/f/move.step/" + 812 + "@log"). A Row is built fresh each time one is read, so reading is a little slower and writing to a
//row read from here changes nothing: rows are never changed once a catalog has been assembled.
final class ColumnarRows extends AbstractList<Row> {

	private final int size, cols;
	private final String[] strings;
	private final Header[] headers;
	private final Codes headerCodes;
	private final int[] ordinals;
	private final Codes[] codes;
	private final byte[][] entryMeta;
	private final long[][] entryValue;

	private ColumnarRows( Builder b ){
		size = b.size;
		cols = b.cols;
		strings = b.strings.toArray(new String[0]);
		headers = b.headers.toArray(new Header[0]);
		headerCodes = b.headerCodes.trim();
		ordinals = java.util.Arrays.copyOf(b.ordinals, b.size);
		codes = new Codes[cols];
		entryMeta = new byte[cols][];
		entryValue = new long[cols][];
		for (int c = 0; c < cols; c++){
			codes[c] = b.codes[c].trim();
			entryMeta[c] = java.util.Arrays.copyOf(b.dicts[c].meta, b.dicts[c].count);
			entryValue[c] = java.util.Arrays.copyOf(b.dicts[c].value, b.dicts[c].count);
		}
	}

	@Override
	public int size(){
		return size;
	}

	@Override
	public Row get( int i ){
		if (i < 0 || i >= size) throw new IndexOutOfBoundsException("row " + i + " of " + size);
		Header h = headers[headerCodes.get(i)];
		int ord = ordinals[i];
		String id = ord < 0 ? h.prefix : h.prefix + ord + h.suffix;
		Row r = new Row(id, h.mergeKey, h.runId, cols);
		r.finished = (h.flags & 1) != 0;
		r.publicRow = (h.flags & 2) != 0;
		r.imported = (h.flags & 4) != 0;
		r.duplicate = (h.flags & 8) != 0;
		r.ruleset = h.ruleset;
		r.telemetry = h.telemetry;
		for (int c = 0; c < cols; c++){
			int e = codes[c].get(i);
			byte m = entryMeta[c][e];
			if (m == 0) continue;
			long v = entryValue[c][e];
			if ((m & 3) == Row.K_TEXT) r.setRaw(c, m, 0, strings[(int) v]);
			else r.setRaw(c, m, v, null);
		}
		return r;
	}

	//What one row costs here, roughly, in bytes: for the memory gate's report.
	long approximateBytes(){
		long bytes = 64L + ordinals.length * 4L + headerCodes.bytes();
		for (int c = 0; c < cols; c++) bytes += codes[c].bytes() + entryMeta[c].length + entryValue[c].length * 8L + 48;
		for (String s : strings) bytes += 40 + (s == null ? 0 : s.length());
		bytes += headers.length * 48L;
		return bytes;
	}

	//---- building ----

	/** The rows as a compact list, or null when one of them holds a cell this store cannot keep exactly (the caller keeps the plain list). */
	static ColumnarRows of( int cols, List<Row> rows ){
		Builder b = new Builder(cols);
		for (Row r : rows) if (!b.add(r)) return null;
		return b.build();
	}

	static final class Builder {
		final int cols;
		int size;
		final List<String> strings = new ArrayList<>();
		final Map<String, Integer> stringIndex = new HashMap<>();
		final List<Header> headers = new ArrayList<>();
		final Map<Header, Integer> headerIndex = new HashMap<>();
		final Codes headerCodes = new Codes();
		int[] ordinals = new int[64];
		final Codes[] codes;
		final CellDict[] dicts;
		private final String[] split = new String[2];

		Builder( int cols ){
			this.cols = cols;
			codes = new Codes[cols];
			dicts = new CellDict[cols];
			for (int c = 0; c < cols; c++){
				codes[c] = new Codes();
				dicts[c] = new CellDict();
			}
		}

		int size(){
			return size;
		}

		/** Adds one row. False (and nothing added) if the row is not one this store keeps exactly. */
		boolean add( Row r ){
			if (r.size() != cols) return false;
			for (int c = 0; c < cols; c++){
				byte m = r.rawMeta(c);
				int k = m & 3;
				if (k == Row.K_TEXT){
					if (r.num(c) != 0 || r.rawText(c) == null) return false;
				} else if (k == Row.K_NUM){
					if (r.rawText(c) != null) return false;
				} else if (k == Row.K_UNKNOWN){
					if (m != 0 || r.num(c) != 0 || r.rawText(c) != null) return false;
				} else return false;
			}
			int ord = split(r.id, split);
			byte flags = (byte) ((r.finished ? 1 : 0) | (r.publicRow ? 2 : 0) | (r.imported ? 4 : 0) | (r.duplicate ? 8 : 0));
			Header h = new Header(split[0], split[1], r.mergeKey, r.runId, r.telemetry, r.ruleset, flags);
			Integer hc = headerIndex.get(h);
			if (hc == null){
				hc = headers.size();
				headers.add(h);
				headerIndex.put(h, hc);
			}
			headerCodes.add(hc);
			if (size == ordinals.length) ordinals = java.util.Arrays.copyOf(ordinals, ordinals.length * 2);
			ordinals[size] = ord;
			for (int c = 0; c < cols; c++){
				byte m = r.rawMeta(c);
				long v = (m & 3) == Row.K_TEXT ? string(r.rawText(c)) : r.num(c);
				codes[c].add(dicts[c].code(m, v));
			}
			size++;
			return true;
		}

		private int string( String s ){
			Integer i = stringIndex.get(s);
			if (i == null){
				i = strings.size();
				strings.add(s);
				stringIndex.put(s, i);
			}
			return i;
		}

		ColumnarRows build(){
			return new ColumnarRows(this);
		}
	}

	//"run/f/move.step/812@log" -> prefix "run/f/move.step/", suffix "@log", 812. An id without such a number is kept whole (-1).
	static int split( String id, String[] out ){
		out[1] = "";
		if (id == null){
			out[0] = null;
			return -1;
		}
		int at = id.lastIndexOf('@');
		int end = at < 0 ? id.length() : at;
		int start = end;
		while (start > 0 && id.charAt(start - 1) >= '0' && id.charAt(start - 1) <= '9') start--;
		int digits = end - start;
		if (digits == 0 || digits > 9 || (digits > 1 && id.charAt(start) == '0')){
			out[0] = id;
			return -1;
		}
		out[0] = id.substring(0, start);
		out[1] = id.substring(end);
		return Integer.parseInt(id.substring(start, end));
	}

	static final class Header {
		final String prefix, suffix, mergeKey, runId, telemetry, ruleset;
		final byte flags;
		private final int hash;

		Header( String prefix, String suffix, String mergeKey, String runId, String telemetry, String ruleset, byte flags ){
			this.prefix = prefix;
			this.suffix = suffix;
			this.mergeKey = mergeKey;
			this.runId = runId;
			this.telemetry = telemetry;
			this.ruleset = ruleset;
			this.flags = flags;
			hash = Objects.hash(prefix, suffix, mergeKey, runId, telemetry, ruleset, flags);
		}

		@Override
		public boolean equals( Object o ){
			if (!(o instanceof Header)) return false;
			Header h = (Header) o;
			return hash == h.hash && flags == h.flags && Objects.equals(prefix, h.prefix) && Objects.equals(suffix, h.suffix)
					&& Objects.equals(mergeKey, h.mergeKey) && Objects.equals(runId, h.runId)
					&& Objects.equals(telemetry, h.telemetry) && Objects.equals(ruleset, h.ruleset);
		}

		@Override
		public int hashCode(){
			return hash;
		}
	}

	//One small code per row: bytes while every code fits in one, then chars, then ints.
	static final class Codes {
		private byte[] b = new byte[64];
		private char[] c;
		private int[] i;
		private int n;

		void add( int code ){
			if (i != null){
				if (n == i.length) i = java.util.Arrays.copyOf(i, i.length * 2);
				i[n++] = code;
				return;
			}
			if (c != null){
				if (code > 0xFFFF){
					i = new int[Math.max(64, c.length)];
					for (int k = 0; k < n; k++) i[k] = c[k];
					c = null;
					add(code);
					return;
				}
				if (n == c.length) c = java.util.Arrays.copyOf(c, c.length * 2);
				c[n++] = (char) code;
				return;
			}
			if (code > 0xFF){
				c = new char[Math.max(64, b.length)];
				for (int k = 0; k < n; k++) c[k] = (char) (b[k] & 0xFF);
				b = null;
				add(code);
				return;
			}
			if (n == b.length) b = java.util.Arrays.copyOf(b, b.length * 2);
			b[n++] = (byte) code;
		}

		int get( int k ){
			return i != null ? i[k] : c != null ? c[k] : b[k] & 0xFF;
		}

		Codes trim(){
			if (i != null) i = java.util.Arrays.copyOf(i, n);
			else if (c != null) c = java.util.Arrays.copyOf(c, n);
			else b = java.util.Arrays.copyOf(b, n);
			return this;
		}

		long bytes(){
			return 16L + (i != null ? i.length * 4L : c != null ? c.length * 2L : b.length);
		}
	}

	//The distinct cells of one column: (meta byte, value) -> code, an open-addressing table with no boxing per cell.
	static final class CellDict {
		byte[] meta = new byte[16];
		long[] value = new long[16];
		int count;
		private int[] table = new int[32];   //code + 1, 0 = empty

		int code( byte m, long v ){
			int mask = table.length - 1;
			int h = mix(m, v) & mask;
			while (true){
				int t = table[h];
				if (t == 0) break;
				if (meta[t - 1] == m && value[t - 1] == v) return t - 1;
				h = (h + 1) & mask;
			}
			if (count == meta.length){
				meta = java.util.Arrays.copyOf(meta, count * 2);
				value = java.util.Arrays.copyOf(value, count * 2);
			}
			meta[count] = m;
			value[count] = v;
			table[h] = ++count;
			if (count * 2 > table.length) rehash();
			return count - 1;
		}

		private void rehash(){
			int[] t = new int[table.length * 2];
			int mask = t.length - 1;
			for (int e = 0; e < count; e++){
				int h = mix(meta[e], value[e]) & mask;
				while (t[h] != 0) h = (h + 1) & mask;
				t[h] = e + 1;
			}
			table = t;
		}

		private static int mix( byte m, long v ){
			long x = v * 0x9E3779B97F4A7C15L + m;
			x ^= x >>> 31;
			x *= 0xBF58476D1CE4E5B9L;
			return (int) (x ^ (x >>> 29));
		}
	}
}

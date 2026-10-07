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

//Turns cells and aggregates into text. Plain ASCII markers only (the game's pixel font has few symbols):
//  ?      unknown: the game never recorded it
//  hidden hidden information that may not be shown (unfinished run, or hidden information off)
//  >=12   only a lower bound (the log has gaps or older telemetry could not count it)
//  ~6     inferred rather than recorded exactly
public final class CellFormatter {

	private CellFormatter(){}

	public static final String UNKNOWN_TEXT = "?";
	public static final String HIDDEN_TEXT = "hidden";

	public static CellText cell( Query q, Row row, ColumnDef col ){
		if (!QueryEngine.open(q, row, col)) return new CellText(HIDDEN_TEXT, CellText.HIDDEN);
		return cellOpen(row, col);
	}

	//a cell that is known to be open
	public static CellText cellOpen( Row row, ColumnDef col ){
		int i = col.index;
		if (!row.isKnown(i)) return new CellText(UNKNOWN_TEXT, CellText.UNKNOWN);
		int prov = row.prov(i);
		String body = row.kind(i) == Row.K_TEXT ? (col.format == ColumnDef.Format.CLASS_NAME ? className(row.text(i)) : col.format == ColumnDef.Format.TITLE_CASE ? titleCase(row.text(i)) : row.text(i)) : value(col, row.num(i));
		switch (prov){
			case Fact.PROV_INCOMPLETE: return new CellText(">=" + body, CellText.LOWER);
			case Fact.PROV_INFERRED: return new CellText("~" + body, CellText.INFERRED);
			case Fact.PROV_LEGACY: return new CellText(body, CellText.LEGACY);
			default: return new CellText(body, CellText.EXACT);
		}
	}

	//A stable class key as a readable name: "items.weapon.melee.WarHammer" is "War Hammer", "items.rings.RingOfHaste" is "Ring of Haste".
	//A key that is only the name the player saw ("name:ruby ring", used when the real kind was not known) is shown as that name.
	public static String className( String key ){
		if (key.startsWith(NAME_KEY_PREFIX)) return key.substring(NAME_KEY_PREFIX.length());
		String simple = key.substring(key.lastIndexOf('.') + 1);
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < simple.length(); i++){
			char c = simple.charAt(i);
			boolean boundary = i > 0 && Character.isUpperCase(c) && (Character.isLowerCase(simple.charAt(i - 1))
					|| (Character.isUpperCase(simple.charAt(i - 1)) && i + 1 < simple.length() && Character.isLowerCase(simple.charAt(i + 1))));
			if (boundary) b.append(' ');
			b.append(c);
		}
		return b.toString().replace(" Of ", " of ").replace(" The ", " the ").replace(" In ", " in ");
	}

	//the start of a type key made from a shown name (see RowBuilders)
	public static final String NAME_KEY_PREFIX = "name:";

	public static String value( ColumnDef col, long n ){
		if (col.type == ColumnDef.Type.ENUM) return col.labelFor(n);
		switch (col.format){
			case DATE_MILLIS: return date(n);
			case HUNDREDTHS: return fixed(n, 100, 2);
			case THOUSANDTHS: return fixed(n, 1000, 1);
			default: return Long.toString(n);
		}
	}

	private static String fixed( long n, int div, int decimals ){
		boolean neg = n < 0;
		long a = Math.abs(n);
		long whole = a / div;
		long frac = a % div;
		if (decimals == 1) frac = frac / (div / 10);
		String f = Long.toString(frac);
		while (f.length() < decimals) f = "0" + f;
		return (neg ? "-" : "") + whole + "." + f;
	}

	private static long floorDiv( long a, long b ){
		long q = a / b;
		return (a % b != 0 && ((a < 0) != (b < 0))) ? q - 1 : q;
	}

	//yyyy-MM-dd in UTC, without any locale or time zone machinery
	public static String date( long millis ){
		long days = floorDiv(millis, 86400000L);
		long z = days + 719468;
		long era = floorDiv(z, 146097L);
		long doe = z - era * 146097;
		long yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365;
		long y = yoe + era * 400;
		long doy = doe - (365 * yoe + yoe / 4 - yoe / 100);
		long mp = (5 * doy + 2) / 153;
		long d = doy - (153 * mp + 2) / 5 + 1;
		long m = mp < 10 ? mp + 3 : mp - 9;
		if (m <= 2) y++;
		return y + "-" + (m < 10 ? "0" : "") + m + "-" + (d < 10 ? "0" : "") + d;
	}

	//"WARRIOR" -> "Warrior", "DEAD_END" -> "Dead end": a key of capital words shown as words
	static String titleCase( String key ){
		if (key == null || key.isEmpty()) return "";
		String t = key.replace('_', ' ').toLowerCase(java.util.Locale.ROOT);
		return Character.toUpperCase(t.charAt(0)) + t.substring(1);
	}

	//what a cell's state means, in a sentence
	public static String explain( int state ){
		switch (state){
			case CellText.LOWER: return "at least this much: the log has gaps, or an older recording version could not count all of it";
			case CellText.INFERRED: return "inferred, not recorded exactly";
			case CellText.LEGACY: return "taken from an imported ranking record";
			case CellText.UNKNOWN: return "unknown: it was never recorded";
			case CellText.HIDDEN: return "hidden information";
			default: return "recorded exactly";
		}
	}

	//What a player typed for a column, as the number stored: plain numbers, dates as yyyy-MM-dd, decimals for columns shown with
	//decimals. Null when it does not parse.
	public static Long parse( ColumnDef col, String text ){
		if (text == null) return null;
		String t = text.trim().replace(",", "");
		if (t.isEmpty()) return null;
		try {
			switch (col.format){
				case DATE_MILLIS: {
					if (t.length() != 10 || t.charAt(4) != '-' || t.charAt(7) != '-') return null;
					long y = Long.parseLong(t.substring(0, 4)), m = Long.parseLong(t.substring(5, 7)), d = Long.parseLong(t.substring(8, 10));
					if (m < 1 || m > 12 || d < 1 || d > 31) return null;
					y -= m <= 2 ? 1 : 0;
					long era = (y >= 0 ? y : y - 399) / 400;
					long yoe = y - era * 400;
					long doy = (153 * (m + (m > 2 ? -3 : 9)) + 2) / 5 + d - 1;
					long doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;
					return (era * 146097 + doe - 719468) * 86400000L;
				}
				case HUNDREDTHS: return Math.round(Double.parseDouble(t) * 100);
				case THOUSANDTHS: return Math.round(Double.parseDouble(t) * 1000);
				default: return Long.parseLong(t);
			}
		} catch (NumberFormatException e){
			return null;
		}
	}

	//the value of an aggregate, and in 'detail' how many values it is made of
	public static final class AggText {
		public final String text;
		public final String detail;
		public final int state;
		public final boolean small;
		//the same value as short as it can be for a dense table: a rate is just its percentage ("64%", the counts are in 'detail')
		public final String compact;

		AggText( String text, String detail, int state, boolean small ){
			this(text, text, detail, state, small);
		}

		AggText( String text, String compact, String detail, int state, boolean small ){
			this.compact = compact;
			this.text = text;
			this.detail = detail;
			this.state = state;
			this.small = small;
		}
	}

	//an aggregate of fewer known values than this is shown as low-confidence
	public static final int SMALL_SAMPLE = 5;

	public static AggText aggregate( DatasetDef def, Aggregate a, AggValue v ){
		if (!v.known) return new AggText(UNKNOWN_TEXT, "0 of " + v.total + " known" + (v.isUnsure() ? ", some rows could not be judged" : ""), CellText.UNKNOWN, false);
		String prefix = "";
		int state = CellText.EXACT;
		if (v.isLowerBound()){
			prefix = ">=";
			state = CellText.LOWER;
		}
		if (v.isInferred()){
			prefix = prefix + "~";
			if (state == CellText.EXACT) state = CellText.INFERRED;
		}
		String body;
		String detail = v.contributing + " of " + v.total + " known";
		ColumnDef c = a.column.isEmpty() ? null : def.column(a.column);
		switch (a.fn){
			case COUNT:
				body = Long.toString((long) v.value);
				detail = v.total + " rows";
				break;
			case COUNT_KNOWN:
				body = Long.toString((long) v.value);
				break;
			case COUNT_DISTINCT:
				body = Long.toString((long) v.value);
				break;
			case RATE:
			case RATIO:
				body = Math.round(v.value * 100) + "% (" + v.hits + "/" + (a.fn == Aggregate.Fn.RATE ? v.contributing : v.outOf) + ")";
				break;
			case AVG:
			case MEDIAN:
				body = oneDecimal(v.value);
				break;
			default:
				body = c != null && c.type == ColumnDef.Type.NUM ? value(c, Math.round(v.value)) : Long.toString(Math.round(v.value));
		}
		if (v.isUnsure()) detail += "; some rows could not be judged";
		boolean small = a.fn != Aggregate.Fn.COUNT && v.contributing < SMALL_SAMPLE;
		if (small) detail += "; small sample";
		String compactBody = body;
		if (a.fn == Aggregate.Fn.RATE || a.fn == Aggregate.Fn.RATIO) compactBody = Math.round(v.value * 100) + "%";
		return new AggText(prefix + body, prefix + compactBody, detail, state, small);
	}

	//whole numbers without a decimal, others with one
	private static String oneDecimal( double v ){
		long tenths = Math.round(v * 10);
		return tenths % 10 == 0 ? Long.toString(tenths / 10) : fixed(tenths, 10, 1);
	}

	//a short name for an aggregate column header
	public static String aggregateLabel( DatasetDef def, Aggregate a ){
		if (!a.label.isEmpty()) return a.label;
		ColumnDef c = a.column.isEmpty() ? null : def.column(a.column);
		String col = c == null ? "" : c.label;
		switch (a.fn){
			case COUNT: return "Rows";
			case COUNT_KNOWN: return "Known " + col;
			case SUM: return "Total " + col;
			case AVG: return "Avg " + col;
			case MIN: return "Min " + col;
			case MAX: return "Max " + col;
			case MEDIAN: return "Median " + col;
			case COUNT_DISTINCT: return "Distinct " + col;
			case RATIO: return col + " per " + (def.column(a.over) == null ? a.over : def.column(a.over).label);
			default: return col + " rate";
		}
	}
}

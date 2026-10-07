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


package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.CellFormatter;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ColumnDef;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Filter;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Op;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.StatusText;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdModel;

import java.util.ArrayList;
import java.util.List;

//APD: adding and editing a UIMSD filter, step by step: column, operator, value. Every step is chosen from the column's own
//definition (its type, its words), so it works for any dataset.
public final class WndUimsdFilter {

	private WndUimsdFilter(){}

	private static void show( com.shatteredpixel.shatteredpixeldungeon.ui.Window w ){
		ShatteredPixelDungeon.scene().addToFront(w);
	}

	//a new filter
	public static void add( final UimsdModel m ){
		final List<ColumnDef> cols = m.offeredColumns();
		List<String> labels = new ArrayList<>();
		for (ColumnDef c : cols) labels.add(c.label);
		show(new WndUimsdList("Filter on", "Choose a column. A filter never matches a row where the value is unknown.", labels, null,
				new WndUimsdList.Picker(){
					@Override
					public void onPick( int index, boolean checked ){
						pickOp(m, -1, cols.get(index));
					}
				}));
	}

	//what to do with a filter that is already there
	public static void chip( final UimsdModel m, final int index ){
		if (index < 0 || index >= m.query().filters.size()) return;
		final Filter f = m.query().filters.get(index);
		List<String> options = new ArrayList<>();
		options.add("Change it");
		options.add("Remove it");
		show(new WndUimsdList(StatusText.describe(m.dataset().def, f), "", options, null, new WndUimsdList.Picker(){
			@Override
			public void onPick( int i, boolean checked ){
				if (i == 1){
					m.removeFilter(index);
				} else {
					ColumnDef c = m.dataset().def.column(f.column);
					if (c != null) pickOp(m, index, c);
				}
			}
		}));
	}

	private static void pickOp( final UimsdModel m, final int index, final ColumnDef col ){
		final Op[] ops = Op.ops(col.type);
		List<String> labels = new ArrayList<>();
		for (Op o : ops) labels.add(col.label + " " + o.word);
		show(new WndUimsdList(col.label, "", labels, null, new WndUimsdList.Picker(){
			@Override
			public void onPick( int i, boolean checked ){
				Op op = ops[i];
				if (!op.takesValue()) finish(m, index, new Filter(col.id, op, null, null));
				else pickValue(m, index, col, op);
			}
		}));
	}

	private static void pickValue( final UimsdModel m, final int index, final ColumnDef col, final Op op ){
		if (col.type == ColumnDef.Type.ENUM && !col.labels.isEmpty()){
			final List<Long> values = new ArrayList<>(col.labels.keySet());
			final List<String> labels = new ArrayList<>();
			for (Long v : values) labels.add(col.labels.get(v));
			if (op == Op.IN){
				final List<Boolean> checks = new ArrayList<>();
				for (int i = 0; i < values.size(); i++) checks.add(false);
				show(new WndUimsdList(col.label + " is one of", "Tick the values, then Done.", labels, checks, new WndUimsdList.Picker(){
					@Override
					public void onPick( int i, boolean checked ){
						checks.set(i, checked);
					}
				}){
					@Override
					public void hide(){
						super.hide();
						List<Long> chosen = new ArrayList<>();
						for (int i = 0; i < checks.size(); i++) if (checks.get(i)) chosen.add(values.get(i));
						if (chosen.isEmpty()) return;
						long[] nums = new long[chosen.size()];
						for (int i = 0; i < nums.length; i++) nums[i] = chosen.get(i);
						finish(m, index, Filter.num(col.id, Op.IN, nums));
					}
				});
			} else {
				show(new WndUimsdList(col.label + " " + op.word, "", labels, null, new WndUimsdList.Picker(){
					@Override
					public void onPick( int i, boolean checked ){
						finish(m, index, Filter.num(col.id, op, values.get(i)));
					}
				}));
			}
			return;
		}
		if (col.type == ColumnDef.Type.TEXT){
			ask(col.label + " " + op.word, op == Op.IN ? "Values, separated by commas" : "Text", new Answer(){
				@Override
				public void got( String s ){
					if (op == Op.IN){
						String[] parts = s.split(",");
						List<String> t = new ArrayList<>();
						for (String p : parts) if (!p.trim().isEmpty()) t.add(p.trim());
						if (t.isEmpty()) return;
						finish(m, index, Filter.text(col.id, Op.IN, t.toArray(new String[0])));
					} else {
						if (s.trim().isEmpty()) return;
						finish(m, index, Filter.text(col.id, op, s.trim()));
					}
				}
			});
			return;
		}
		//numbers (dates are typed as yyyy-MM-dd)
		final String hint = col.format == ColumnDef.Format.DATE_MILLIS ? "A date, like 2026-10-07" : "A number";
		if (op == Op.BETWEEN){
			ask(col.label + " between", "The lower value. " + hint, new Answer(){
				@Override
				public void got( final String lo ){
					ask(col.label + " between", "The upper value. " + hint, new Answer(){
						@Override
						public void got( String hi ){
							Long a = CellFormatter.parse(col, lo), b = CellFormatter.parse(col, hi);
							if (a == null || b == null){
								ShatteredPixelDungeon.scene().addToFront(new WndMessage("That is not a valid value."));
								return;
							}
							finish(m, index, Filter.num(col.id, Op.BETWEEN, a, b));
						}
					});
				}
			});
		} else {
			ask(col.label + " " + op.word, hint, new Answer(){
				@Override
				public void got( String s ){
					Long a = CellFormatter.parse(col, s);
					if (a == null){
						ShatteredPixelDungeon.scene().addToFront(new WndMessage("That is not a valid value."));
						return;
					}
					finish(m, index, Filter.num(col.id, op, a));
				}
			});
		}
	}

	private interface Answer {
		void got( String text );
	}

	private static void ask( String title, String body, final Answer answer ){
		show(new WndTextInput(title, body, "", 40, false, "OK", "Cancel"){
			@Override
			public void onSelect( boolean positive, String text ){
				if (positive) answer.got(text);
			}
		});
	}

	private static void finish( UimsdModel m, int index, Filter f ){
		if (index < 0) m.addFilter(f); else m.replaceFilter(index, f);
	}
}

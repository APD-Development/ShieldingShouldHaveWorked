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
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ResultTable;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Sort;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdModel;

import java.util.ArrayList;
import java.util.List;

//APD: the sort keys of a UIMSD table (up to three). Unknown values always sort last, whichever way a key goes.
public final class WndUimsdSort {

	private WndUimsdSort(){}

	private static final String[] WAYS = { "lowest first", "highest first" };

	private static String labelOf( ResultTable t, String id ){
		for (ResultTable.Header h : t.headers) if (h.id.equals(id)) return h.label;
		return id;
	}

	public static void show( final UimsdModel m ){
		final ResultTable table = m.table();
		if (table == null) return;
		final List<Sort> sorts = new ArrayList<>(m.query().sorts);
		final List<String> labels = new ArrayList<>();
		for (int i = 0; i < sorts.size(); i++){
			labels.add((i + 1) + ". " + labelOf(table, sorts.get(i).column) + ", " + WAYS[sorts.get(i).descending ? 1 : 0]);
		}
		final int addIndex = sorts.size() < 3 ? labels.size() : -1;
		if (addIndex >= 0) labels.add("Add a sort key");
		final int clearIndex = sorts.isEmpty() ? -1 : labels.size();
		if (clearIndex >= 0) labels.add("Clear sorting");
		ShatteredPixelDungeon.scene().addToFront(new WndUimsdList("Sort", "Unknown values always come last.", labels, null, new WndUimsdList.Picker(){
			@Override
			public void onPick( int i, boolean checked ){
				if (i == clearIndex){
					m.setSorts(new ArrayList<Sort>());
				} else if (i == addIndex){
					pickColumn(m, table, sorts);
				} else {
					editKey(m, sorts, i);
				}
			}
		}));
	}

	private static void editKey( final UimsdModel m, final List<Sort> sorts, final int index ){
		List<String> options = new ArrayList<>();
		options.add("Reverse it");
		options.add("Remove it");
		ShatteredPixelDungeon.scene().addToFront(new WndUimsdList("Sort key " + (index + 1), "", options, null, new WndUimsdList.Picker(){
			@Override
			public void onPick( int i, boolean checked ){
				List<Sort> next = new ArrayList<>(sorts);
				if (i == 0) next.set(index, new Sort(sorts.get(index).column, !sorts.get(index).descending));
				else next.remove(index);
				m.setSorts(next);
			}
		}));
	}

	private static void pickColumn( final UimsdModel m, final ResultTable table, final List<Sort> sorts ){
		final List<ResultTable.Header> options = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		for (ResultTable.Header h : table.headers){
			boolean used = false;
			for (Sort s : sorts) if (s.column.equals(h.id)) used = true;
			if (h.sortable && !used){
				options.add(h);
				labels.add(h.label);
			}
		}
		ShatteredPixelDungeon.scene().addToFront(new WndUimsdList("Sort by", "", labels, null, new WndUimsdList.Picker(){
			@Override
			public void onPick( int i, boolean checked ){
				final String id = options.get(i).id;
				List<String> ways = new ArrayList<>();
				ways.add(options.get(i).label + ", lowest first");
				ways.add(options.get(i).label + ", highest first");
				ShatteredPixelDungeon.scene().addToFront(new WndUimsdList("Sort order", "", ways, null, new WndUimsdList.Picker(){
					@Override
					public void onPick( int w, boolean c ){
						List<Sort> next = new ArrayList<>(sorts);
						next.add(new Sort(id, w == 1));
						m.setSorts(next);
					}
				}));
			}
		}));
	}
}

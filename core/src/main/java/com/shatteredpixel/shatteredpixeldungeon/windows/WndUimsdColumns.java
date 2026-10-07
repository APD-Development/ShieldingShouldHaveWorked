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
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ColumnDef;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdModel;

import java.util.ArrayList;
import java.util.List;

//APD: which columns the table shows. Columns made of hidden information are only listed while hidden information is on.
public final class WndUimsdColumns {

	private WndUimsdColumns(){}

	public static void show( final UimsdModel m ){
		if (m.dataset() == null) return;
		if (m.result() != null && m.result().isGrouped()){
			ShatteredPixelDungeon.scene().addToFront(new WndMessage("A grouped table shows its group columns and its totals. "
					+ "Use Options, Group by, to change them, or open another view."));
			return;
		}
		final List<ColumnDef> cols = m.offeredColumns();
		List<String> shown = m.query().columns.isEmpty() ? m.dataset().def.defaultColumnIds() : m.query().columns;
		final List<String> labels = new ArrayList<>();
		final List<Boolean> checks = new ArrayList<>();
		for (ColumnDef c : cols){
			labels.add(c.label + (c.group.isEmpty() ? "" : "  (" + c.group + ")"));
			checks.add(shown.contains(c.id));
		}
		ShatteredPixelDungeon.scene().addToFront(new WndUimsdList("Columns", "Tick the columns to show. Tap Done when finished.", labels, checks,
				new WndUimsdList.Picker(){
					@Override
					public void onPick( int i, boolean checked ){
						m.toggleColumn(cols.get(i).id);
					}
				}));
	}
}

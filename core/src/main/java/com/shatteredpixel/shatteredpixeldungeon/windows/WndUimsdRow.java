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
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Aggregate;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.CellFormatter;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.CellText;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ColumnDef;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.DatasetDef;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.GroupRow;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.LinkDef;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.QueryResult;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ResultTable;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Row;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdModel;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.List;

//APD: everything about one row (or one group) of a UIMSD table, value by value, with what kind of knowledge each value is, and
//the links to the matching rows of other datasets. Built only from the dataset's definition.
public class WndUimsdRow extends Window {

	private static final int MARGIN = 2;

	public WndUimsdRow( final UimsdModel m, ResultTable.Line line ){
		super();
		int width = PixelScene.landscape() ? WndUimsdList.WIDTH_L : WndUimsdList.WIDTH_P;
		QueryResult r = m.result();
		DatasetDef def = r.def;

		StringBuilder body = new StringBuilder();
		final List<LinkDef> links = new ArrayList<>();
		final Row row = line.row;
		String title;
		if (row != null){
			title = r.def.title;
			String lastGroup = null;
			for (ColumnDef c : def.columns()){
				if (c.isInternal() && !r.query.showInternal) continue;
				CellText t = CellFormatter.cell(r.query, row, c);
				if (!c.group.equals(lastGroup)){
					body.append(lastGroup == null ? "" : "\n").append("_").append(c.group.isEmpty() ? "Details" : c.group).append("_\n");
					lastGroup = c.group;
				}
				body.append(c.label).append(": ").append(t.text);
				if (t.state != CellText.EXACT) body.append("  (").append(CellFormatter.explain(t.state)).append(")");
				body.append('\n');
			}
			if (!row.telemetry.isEmpty()) body.append("\nRecorded with telemetry ").append(row.telemetry).append('\n');
			for (LinkDef l : def.links){
				if (m.catalog() != null && m.catalog().view(l.targetView) != null && row.isKnown(def.indexOf(l.ourColumn))) links.add(l);
			}
		} else {
			GroupRow g = line.group;
			title = "Group";
			for (int i = 0; i < r.groupKeys.size(); i++){
				ColumnDef c = r.groupKeys.get(i);
				CellText t = CellFormatter.cell(r.query, g.sample, c);
				body.append(c.label).append(": ").append(t.text);
				if (t.state != CellText.EXACT) body.append("  (").append(CellFormatter.explain(t.state)).append(")");
				body.append('\n');
			}
			body.append("Rows in the group: ").append(g.n).append('\n');
			for (int i = 0; i < r.aggregates.size(); i++){
				Aggregate a = r.aggregates.get(i);
				if (a.fn == Aggregate.Fn.COUNT) continue;
				CellFormatter.AggText at = CellFormatter.aggregate(def, a, g.aggregates.get(i));
				body.append(CellFormatter.aggregateLabel(def, a)).append(": ").append(at.text).append("  (").append(at.detail);
				if (g.aggregates.get(i).isLowerBound()) body.append("; at least this much, some values were lower bounds");
				if (g.aggregates.get(i).isInferred()) body.append("; includes inferred values");
				body.append(")\n");
			}
		}

		RenderedTextBlock tt = PixelScene.renderTextBlock(title, 9);
		tt.hardlight(TITLE_COLOR);
		tt.maxWidth(width - 2 * MARGIN);
		tt.setPos(MARGIN, MARGIN);
		add(tt);
		float pos = tt.bottom() + 2 * MARGIN;

		float buttonsH = links.size() * 18f;
		final Component content = new Component();
		RenderedTextBlock text = PixelScene.renderTextBlock(body.toString(), 6);
		text.maxWidth(width - 2 * MARGIN - 2);
		text.setPos(MARGIN, 0);
		content.add(text);
		content.setSize(width, text.bottom() + 2);
		ScrollPane pane = new ScrollPane(content);
		add(pane);
		float maxH = PixelScene.uiCamera.height * 0.8f - pos - buttonsH;
		float h = Math.min(content.height(), Math.max(40, maxH));
		pane.setRect(0, pos, width, h);
		pos += h + MARGIN;

		for (final LinkDef l : links){
			RedButton b = new RedButton(l.label){
				@Override
				protected void onClick(){
					WndUimsdRow.this.hide();
					m.openLink(row, l);
				}
			};
			b.setRect(0, pos, width, 16);
			add(b);
			pos += 18;
		}
		resize(width, (int) pos);
	}

	public static void show( UimsdModel m, ResultTable.Line line ){
		ShatteredPixelDungeon.scene().addToFront(new WndUimsdRow(m, line));
	}
}

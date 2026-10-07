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


package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Filter;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ResultTable;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.StatusText;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdModel;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ViewDef;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdRecorder;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.Ruleset;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.TitleBackground;
import com.shatteredpixel.shatteredpixeldungeon.ui.UimsdTable;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdColumns;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdFilter;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdRow;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdSort;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdStatus;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Group;
import com.watabou.utils.RectF;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//APD: UIMSD, the Ultimate Infinite Mega Statistics Database. This scene is only a screen for a UimsdModel: it draws the model's
//status, filters and table and passes taps back to it. It knows nothing about any particular dataset.
//
//For now it is opened from APD Settings (the title screen stays as it is until the retarget of its button, a later phase).
public class UimsdScene extends PixelScene {

	public static Class<? extends PixelScene> backTo = TitleScene.class;
	//the model outlives the scene so a screen rotation (which rebuilds the scene) keeps where the player was
	private static UimsdModel model;

	private volatile boolean dirty = true;
	private Group ui;
	private ExitButton btnExit;

	//Opens UIMSD from a screen. 'from' is where Back returns to.
	public static void open( Class<? extends PixelScene> from ){
		backTo = from == GameScene.class ? GameScene.class : TitleScene.class;
		File root = ApdRecorder.historyRoot();
		Ruleset rules = from == GameScene.class ? Ruleset.current() : Ruleset.of(SPDSettings.apdExoticChance(), SPDSettings.challenges());
		model = new UimsdModel(rules.id, "challenges " + SPDSettings.challenges() + ", exotic " + rules.exoticChance + "%");
		if (root == null){
			model = null;
		} else {
			ApdRecorder.flushForReading();
			model.load(root);
		}
		ShatteredPixelDungeon.switchScene(UimsdScene.class);
	}

	@Override
	public void create(){
		super.create();
		uiCamera.visible = false;

		int w = Camera.main.width;
		int h = Camera.main.height;
		TitleBackground bg = new TitleBackground(w, h);
		add(bg);
		add(new com.watabou.noosa.ColorBlock(w, h, 0x66000000));

		ui = new Group();
		add(ui);
		btnExit = new ExitButton();
		RectF insets = getCommonInsets();
		btnExit.setPos(insets.left + (w - insets.left - insets.right) - btnExit.width(), insets.top);
		add(btnExit);

		if (model != null){
			model.setListener(new UimsdModel.Listener(){
				@Override
				public void changed(){
					dirty = true;
				}
			});
		}
		dirty = true;
	}

	@Override
	public synchronized void update(){
		if (dirty){
			dirty = false;
			rebuild();
		}
		super.update();
	}

	private void rebuild(){
		erase(ui);
		ui.destroy();
		ui = new Group();
		add(ui);
		bringToFront(btnExit);
		build();
	}

	//---- drawing the model ----

	private float x0, contentW, y;

	private RenderedTextBlock text( String s, int size, int color, float maxW ){
		RenderedTextBlock t = renderTextBlock(s, size);
		t.hardlight(color);
		t.maxWidth((int) maxW);
		ui.add(t);
		return t;
	}

	private void build(){
		int w = Camera.main.width;
		int h = Camera.main.height;
		RectF insets = getCommonInsets();
		final float margin = 4;
		x0 = insets.left + margin;
		contentW = w - insets.left - insets.right - 2 * margin;
		y = insets.top + 4;

		RenderedTextBlock title = text("UIMSD", 12, Window.TITLE_COLOR, contentW);
		title.setPos(x0, y);
		y = title.bottom() + 3;

		if (model == null){
			RenderedTextBlock t = text("History recording is not set up, so there is nothing to show yet.", 7, 0xFFFFFF, contentW);
			t.setPos(x0, y);
			return;
		}
		UimsdModel.State state = model.state();
		if (state == UimsdModel.State.LOADING || state == UimsdModel.State.EMPTY){
			RenderedTextBlock t = text("Reading your history...", 8, 0xCCCCCC, contentW);
			t.setPos(x0, y + 20);
			return;
		}
		if (state == UimsdModel.State.FAILED){
			RenderedTextBlock t = text("UIMSD could not read the history:\n" + model.failure(), 7, 0xFF8888, contentW);
			t.setPos(x0, y + 20);
			return;
		}

		//view and back
		ViewDef view = model.view();
		float bw = model.canGoBack() ? contentW - 34 : contentW;
		RedButton viewBtn = new RedButton((view == null ? "View" : view.title) + "  (change)", 8){
			@Override
			protected void onClick(){
				pickView();
			}
		};
		viewBtn.setRect(x0, y, bw - (model.canGoBack() ? 2 : 0), 16);
		ui.add(viewBtn);
		if (model.canGoBack()){
			RedButton back = new RedButton("Back", 8){
				@Override
				protected void onClick(){
					model.back();
				}
			};
			back.setRect(x0 + bw, y, contentW - bw, 16);
			ui.add(back);
		}
		y += 18;

		//status
		List<StatusText.Line> lines = model.status();
		StringBuilder brief = new StringBuilder();
		for (int i = 0; i < lines.size() && i < 2; i++) brief.append(i > 0 ? "\n" : "").append(lines.get(i).text);
		RenderedTextBlock st = text(brief.toString(), 5, 0xDDDDDD, contentW - 46);
		st.setPos(x0, y);
		int notes = lines.size() - 2;
		RedButton statusBtn = new RedButton(notes > 0 ? "Notes (" + notes + ")" : "Notes", 6){
			@Override
			protected void onClick(){
				WndUimsdStatus.show(model);
			}
		};
		statusBtn.setRect(x0 + contentW - 44, y, 44, 14);
		ui.add(statusBtn);
		y = Math.max(st.bottom(), y + 14) + 3;

		//filter chips
		float cx = x0;
		final List<Filter> filters = model.query().filters;
		for (int i = 0; i < filters.size(); i++){
			final int index = i;
			String label = StatusText.describe(model.dataset().def, filters.get(i));
			if (label.length() > 34) label = label.substring(0, 32) + "..";
			float cw = Math.min(contentW, label.length() * 4.2f + 10);
			if (cx + cw > x0 + contentW){
				cx = x0;
				y += 15;
			}
			RedButton chip = new RedButton(label, 6){
				@Override
				protected void onClick(){
					WndUimsdFilter.chip(model, index);
				}
			};
			chip.setRect(cx, y, cw, 13);
			ui.add(chip);
			cx += cw + 2;
		}
		y += filters.isEmpty() ? 0 : 15;

		//bottom controls first, so the table gets what is left
		final ResultTable table = model.table();
		float bottom = h - insets.bottom - 4;
		float bar = 17;
		float barTop = bottom - bar;
		String[] names = { "Filter", "Sort", "Columns", "Options" };
		float bwid = (contentW - 6) / 4f;
		for (int i = 0; i < 4; i++){
			final int which = i;
			RedButton b = new RedButton(names[i], 8){
				@Override
				protected void onClick(){
					switch (which){
						case 0: WndUimsdFilter.add(model); break;
						case 1: WndUimsdSort.show(model); break;
						case 2: WndUimsdColumns.show(model); break;
						default: WndUimsdOptions.show(model);
					}
				}
			};
			b.setRect(x0 + i * (bwid + 2), barTop, bwid, bar - 1);
			ui.add(b);
		}
		float tableBottom = barTop - 2;
		if (table != null && table.pageCount() > 1){
			float py = barTop - 16;
			RedButton prev = new RedButton("<", 8){
				@Override
				protected void onClick(){
					model.setPage(model.page() - 1);
				}
			};
			prev.setRect(x0, py, 20, 14);
			prev.enable(table.page() > 0);
			ui.add(prev);
			RedButton next = new RedButton(">", 8){
				@Override
				protected void onClick(){
					model.setPage(model.page() + 1);
				}
			};
			next.setRect(x0 + contentW - 20, py, 20, 14);
			next.enable(table.page() < table.pageCount() - 1);
			ui.add(next);
			int from = table.pageStart + 1, to = table.pageStart + table.lines.size();
			RenderedTextBlock pg = text("Rows " + from + "-" + to + " of " + table.total + "  (page " + (table.page() + 1) + "/" + table.pageCount() + ")", 6, 0xDDDDDD, contentW - 46);
			pg.setPos(x0 + (contentW - pg.width()) / 2f, py + (14 - pg.height()) / 2f);
			tableBottom = py - 2;
		}

		if (table == null) return;
		UimsdTable t = new UimsdTable();
		t.setListener(new UimsdTable.Listener(){
			@Override
			public void onSort( String columnId ){
				model.cycleSort(columnId);
			}

			@Override
			public void onRow( ResultTable.Line line ){
				WndUimsdRow.show(model, line);
			}
		});
		ui.add(t);
		t.setRect(x0, y, contentW, Math.max(40, tableBottom - y));
		t.setTable(table);
		if (table.lines.isEmpty()){
			RenderedTextBlock none = text("Nothing matches. The notes above say what was left out.", 7, 0xCCCCCC, contentW);
			none.setPos(x0, y + TableGeometryHeader() + 6);
		}
	}

	private static float TableGeometryHeader(){
		return com.shatteredpixel.shatteredpixeldungeon.apd.query.TableGeometry.HEADER_HEIGHT;
	}

	private void pickView(){
		final List<ViewDef> views = model.catalog() == null ? new ArrayList<ViewDef>() : model.catalog().views();
		List<String> labels = new ArrayList<>();
		for (ViewDef v : views) labels.add(v.title + " - " + v.description);
		ShatteredPixelDungeon.scene().addToFront(new WndUimsdList("Views", "", labels, null, new WndUimsdList.Picker(){
			@Override
			public void onPick( int i, boolean checked ){
				model.selectView(views.get(i).id);
			}
		}));
	}

	@Override
	protected void onBackPressed(){
		if (model != null && model.canGoBack()){
			model.back();
			return;
		}
		if (model != null) model.setListener(null);
		ShatteredPixelDungeon.switchScene(backTo);
	}
}

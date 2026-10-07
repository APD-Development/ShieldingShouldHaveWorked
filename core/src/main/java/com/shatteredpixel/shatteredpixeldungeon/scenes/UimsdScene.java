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
import com.shatteredpixel.shatteredpixeldungeon.apd.demo.DemoData;
import com.shatteredpixel.shatteredpixeldungeon.apd.demo.DemoGenerator;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Filter;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.GuideGeometry;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdHome;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdTutorial;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ResultTable;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.StatusText;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.TitleFit;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdModel;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ViewDef;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdRecorder;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.Ruleset;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.TitleBackground;
import com.shatteredpixel.shatteredpixeldungeon.ui.UimsdHomePane;
import com.shatteredpixel.shatteredpixeldungeon.ui.UimsdTable;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdColumns;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdFilter;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdGuide;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdRow;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdSort;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdStatus;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Group;
import com.watabou.noosa.PointerArea;
import com.watabou.utils.RectF;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//APD: UIMSD, the Ultimate Infinite Mega Statistics Database. This scene is only a screen for a UimsdModel: it draws the model's
//status, filters and table and passes taps back to it. It knows nothing about any particular dataset.
//
//It is opened by the UIMSD button of the title screen (the primary entry point) and, for testing, by "Open UIMSD (preview)" in
//APD Settings. It opens on the Home page (the summary: UimsdHome); the advanced tables are one tap away and Back from one returns to
//Home. The Guide and the interactive tour (UimsdTutorial) are reachable from both.
public class UimsdScene extends PixelScene {

	//UIMSD is the short name (buttons, navigation); the header of this screen shows the full name
	public static final String FULL_NAME = "Ultimate Infinite Mega Statistics Database";

	public static Class<? extends PixelScene> backTo = TitleScene.class;
	//the model outlives the scene so a screen rotation (which rebuilds the scene) keeps where the player was
	private static UimsdModel model;
	//INDEV only: the screen is showing the synthetic demo data instead of the real history
	private static boolean showingDemo;
	//the player's own model (the one the screen shows unless the tour is running on a model of its own)
	private static UimsdModel mainModel;
	private static File mainRoot;
	private static String rulesId = "", rulesLabel = "";
	//Home or the advanced tables; and whether Home counts runs under all rules
	private static volatile boolean atHome = true;
	private static boolean homeAllRules;
	private static UimsdHome cachedHome;
	private static Catalog cachedCatalog;
	private static boolean cachedAll;
	//the interactive tour: runs on a model of its own, on the player's data or on a small sample history kept in a folder of its own
	private static volatile UimsdTutorial tutorial;
	private static volatile boolean tutorialPreparing;
	private static volatile boolean showingSample;
	private static File sampleRoot;
	private boolean offerPending;
	private final Map<String, float[]> targets = new HashMap<>();

	private volatile boolean dirty = true;
	private Group ui;
	private ExitButton btnExit;

	//Opens UIMSD from a screen. 'from' is where Back returns to.
	public static void open( Class<? extends PixelScene> from ){
		backTo = from == GameScene.class ? GameScene.class : TitleScene.class;
		showingDemo = DemoData.available() && DemoData.viewingDemo();
		File root = ApdRecorder.historyRoot();
		if (showingDemo){
			root = DemoData.demoRootFor(root);
			if (!DemoData.isSyntheticRoot(root)) root = null;   //no demo data has been generated
		}
		Ruleset rules = from == GameScene.class ? Ruleset.current() : Ruleset.of(SPDSettings.apdExoticChance(), SPDSettings.challenges());
		rulesId = rules.id;
		rulesLabel = "challenges " + SPDSettings.challenges() + ", exotic " + rules.exoticChance + "%";
		endTutorial();
		removeLeftoverSample();
		atHome = true;
		model = new UimsdModel(rulesId, rulesLabel);
		if (root == null){
			model = null;
		} else {
			ApdRecorder.flushForReading();
			model.load(root);
		}
		mainModel = model;
		mainRoot = root;
		//from the title screen it switches the way the other title screens do (no fade through black)
		if (backTo == TitleScene.class) ShatteredPixelDungeon.switchNoFade(UimsdScene.class);
		else ShatteredPixelDungeon.switchScene(UimsdScene.class);
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
		//the tour is offered once, the first time UIMSD is opened; the Guide is always there
		if (!SPDSettings.apdUimsdGuideSeen() && mainModel != null && tutorial == null){
			SPDSettings.apdUimsdGuideSeen(true);
			offerPending = true;
		}
	}

	@Override
	public synchronized void update(){
		if (offerPending){
			offerPending = false;
			addToFront(new WndOptions("New to UIMSD?", "Take a short tour of the real screens. You can skip any step, and the Guide button brings it back whenever you want.",
					"Start the tour", "Later"){
				@Override
				protected void onSelect( int index ){
					if (index == 0) startTour(0);
				}
			});
		}
		UimsdTutorial t = tutorial;
		if (t != null && !tutorialPreparing){
			if (t.isFinished()){
				endTutorial();
				atHome = true;
				dirty = true;
			} else if (t.check()){
				dirty = true;
			}
		}
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

	//remembers where an element of the screen is, so the tour can point at it
	private void reg( String id, float x, float y, float w, float h ){
		targets.put(id, new float[]{ x, y, w, h });
	}

	private void build(){
		targets.clear();
		int w = Camera.main.width;
		int h = Camera.main.height;
		RectF insets = getCommonInsets();
		final float margin = 4;
		x0 = insets.left + margin;
		contentW = w - insets.left - insets.right - 2 * margin;
		y = insets.top + 4;

		//the header is the full name, sized to fit in the room left of the exit button (UIMSD stays the short name elsewhere)
		float room = contentW - Math.max(0, btnExit.width() - margin) - 2;
		RenderedTextBlock title = text(FULL_NAME, TitleFit.size(room, FULL_NAME), Window.TITLE_COLOR, room);
		title.setPos(x0, y);
		y = title.bottom() + 1;

		//which build this is, so a device test can tell what is installed
		RenderedTextBlock build = text("build " + com.watabou.noosa.Game.version, 5, 0x999999, contentW);
		build.setPos(x0, y);
		y = build.bottom() + 3;

		if (showingSample){
			RenderedTextBlock sample = text("SAMPLE DATA - made up for the tour, NOT your history", 6, 0xFF6666, contentW);
			sample.setPos(x0, y);
			y = sample.bottom() + 2;
		} else if (showingDemo){
			RenderedTextBlock demo = text("DEMO DATA - made up for testing, NOT your real history", 6, 0xFF6666, contentW);
			demo.setPos(x0, y);
			y = demo.bottom() + 2;
		}

		if (tutorialPreparing){
			RenderedTextBlock t = text("Preparing the tour...", 8, 0xCCCCCC, contentW);
			t.setPos(x0, y + 20);
			return;
		}
		if (model == null){
			RenderedTextBlock t = text(showingDemo ? "No demo data yet. Generate it in APD Settings." : "History recording is not set up, so there is nothing to show yet.", 7, 0xFFFFFF, contentW);
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

		float bottomLimit;
		if (atHome) bottomLimit = buildHome(h, insets);
		else bottomLimit = buildAdvanced(h, insets);
		buildTour(bottomLimit, insets);
	}

	//---- the starter the Guide uses ----

	private final WndUimsdGuide.Starter starter = new WndUimsdGuide.Starter(){
		@Override
		public void startTour( int stepIndex ){
			UimsdScene.this.startTour(stepIndex);
		}
	};

	private void openGuide(){
		WndUimsdGuide.show(starter);
	}

	//---- Home ----

	private UimsdHome homeData(){
		Catalog c = model.catalog();
		if (c == null) return null;
		if (cachedHome == null || cachedCatalog != c || cachedAll != homeAllRules){
			cachedHome = UimsdHome.build(c, model.currentRuleset(), homeAllRules);
			cachedCatalog = c;
			cachedAll = homeAllRules;
		}
		return cachedHome;
	}

	private void openView( String viewId ){
		model.showRoot(viewId);
		atHome = false;
		dirty = true;
	}

	//draws the Home page; returns the bottom of the room it used (what the tour panel may sit above)
	private float buildHome( int h, RectF insets ){
		float rowH = 16;
		float guideW = 36;
		float gap = 2;
		float each = (contentW - guideW - 2 * gap) / 2f;
		RedButton guide = new RedButton("Guide", 8){
			@Override
			protected void onClick(){
				openGuide();
			}
		};
		guide.setRect(x0, y, guideW, rowH);
		ui.add(guide);
		reg("guide", x0, y, guideW, rowH);
		RedButton rules = new RedButton(homeAllRules ? "Rules: all" : "Rules: current", 7){
			@Override
			protected void onClick(){
				homeAllRules = !homeAllRules;
				dirty = true;
			}
		};
		rules.setRect(x0 + guideW + gap, y, each, rowH);
		ui.add(rules);
		RedButton tables = new RedButton("All tables", 8){
			@Override
			protected void onClick(){
				atHome = false;
				dirty = true;
			}
		};
		tables.setRect(x0 + guideW + gap + each + gap, y, each, rowH);
		ui.add(tables);
		reg("tables", x0 + guideW + gap + each + gap, y, each, rowH);
		y += rowH + 2;

		UimsdHome home = homeData();
		float bottom = h - insets.bottom - 4;
		if (home == null) return bottom;
		RenderedTextBlock note = text(home.scopeNote, 5, 0xDDDDDD, contentW);
		note.setPos(x0, y);
		y = note.bottom() + 3;

		if (home.isEmpty()){
			RenderedTextBlock none = text("No finished runs to show here yet. Play a run, or take the tour to see how UIMSD works on sample data.", 7, 0xFFFFFF, contentW);
			none.setPos(x0, y + 4);
			y = none.bottom() + 6;
			RedButton tour = new RedButton("Take the tour", 8){
				@Override
				protected void onClick(){
					startTour(0);
				}
			};
			tour.setRect(x0, y, Math.min(contentW, 90), 16);
			ui.add(tour);
			return bottom;
		}

		final UimsdHomePane pane = new UimsdHomePane();
		pane.setListener(new UimsdHomePane.Listener(){
			@Override
			public void onLink( String viewId ){
				openView(viewId);
			}
		});
		ui.add(pane);
		pane.setHome(home);
		pane.setRect(x0, y, contentW, Math.max(40, bottom - y));
		reg("home.cards", x0, y, contentW, Math.max(40, bottom - y));
		//while the tour asks for the Gear card's Weapons button, bring it into view and point at it
		UimsdTutorial t = tutorial;
		float[] box = pane.linkBox("weapons");
		if (box != null && t != null && "home.gear".equals(t.step().target)){
			pane.scrollTo(0, Math.max(0, box[1] - 24));
			float sy = pane.content().camera.scroll.y;
			reg("home.gear", x0 + box[0], y + box[1] - sy, box[2], box[3]);
		}
		return bottom;
	}

	//---- the advanced tables (everything UIMSD could always do) ----

	private float buildAdvanced( int h, RectF insets ){
		//home, view, back and guide
		ViewDef view = model.view();
		float homeW = 30, helpW = 14, backW = model.canGoBack() ? 32 : 0, gap = 2;
		float viewW = contentW - homeW - helpW - backW - gap * (model.canGoBack() ? 3 : 2);
		float cx0 = x0;
		RedButton homeBtn = new RedButton("Home", 7){
			@Override
			protected void onClick(){
				atHome = true;
				dirty = true;
			}
		};
		homeBtn.setRect(cx0, y, homeW, 16);
		ui.add(homeBtn);
		reg("home", cx0, y, homeW, 16);
		cx0 += homeW + gap;
		RedButton viewBtn = new RedButton((view == null ? "View" : view.title) + "  (change)", 8){
			@Override
			protected void onClick(){
				pickView();
			}
		};
		viewBtn.setRect(cx0, y, viewW, 16);
		ui.add(viewBtn);
		reg("view", cx0, y, viewW, 16);
		cx0 += viewW + gap;
		if (model.canGoBack()){
			RedButton back = new RedButton("Back", 8){
				@Override
				protected void onClick(){
					model.back();
				}
			};
			back.setRect(cx0, y, backW, 16);
			ui.add(back);
			reg("back", cx0, y, backW, 16);
			cx0 += backW + gap;
		}
		RedButton help = new RedButton("?", 8){
			@Override
			protected void onClick(){
				openGuide();
			}
		};
		help.setRect(cx0, y, helpW, 16);
		ui.add(help);
		reg("guide", cx0, y, helpW, 16);
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
		reg("notes", x0 + contentW - 44, y, 44, 14);
		reg("status", x0, y, contentW - 46, Math.max(st.height(), 10));
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
		String[] ids = { "filter", "sort", "columns", "options" };
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
			reg(ids[i], x0 + i * (bwid + 2), barTop, bwid, bar - 1);
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

		if (table == null) return barTop;
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
		reg("table.header", x0, y, contentW, com.shatteredpixel.shatteredpixeldungeon.apd.query.TableGeometry.HEADER_HEIGHT);
		reg("table.firstRow", x0, y + com.shatteredpixel.shatteredpixeldungeon.apd.query.TableGeometry.HEADER_HEIGHT, contentW,
				com.shatteredpixel.shatteredpixeldungeon.apd.query.TableGeometry.ROW_HEIGHT);
		if (table.lines.isEmpty()){
			RenderedTextBlock none = text("Nothing matches. The notes above say what was left out.", 7, 0xCCCCCC, contentW);
			none.setPos(x0, y + TableGeometryHeader() + 6);
		}
		return barTop;
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

	//---- the interactive tour ----

	//the tour panel and the highlight, over everything else
	private void buildTour( float bottomLimit, RectF insets ){
		UimsdTutorial t = tutorial;
		if (t == null || t.isFinished()) return;
		UimsdTutorial.Step step = t.step();
		float pw = contentW;
		RenderedTextBlock title = text("Tour " + (t.index() + 1) + "/" + t.count() + ": " + step.title, 7, Window.TITLE_COLOR, pw - 8);
		RenderedTextBlock body = text(step.text, 6, 0xFFFFFF, pw - 8);
		float buttonsH = 14;
		float ph = Math.max(GuideGeometry.MIN_PANEL, 4 + title.height() + 2 + body.height() + 3 + buttonsH + 4);
		ph = Math.min(ph, (Camera.main.height - insets.top - insets.bottom) * 0.5f);
		float[] target = step.target.isEmpty() ? null : targets.get(step.target);
		float topLimit = insets.top + 4 + 16;
		float py = GuideGeometry.placeTop(topLimit, bottomLimit, ph, target == null ? 0 : target[0], target == null ? 0 : target[1],
				target == null ? 0 : target[2], target == null ? 0 : target[3], target != null);

		//the highlight
		if (target != null){
			float tx = target[0] - 2, ty = target[1] - 2, tw = target[2] + 4, th = target[3] + 4;
			int c = 0xFFFFFF00;
			ColorBlock[] edges = {
					new ColorBlock(tw, 2, c), new ColorBlock(tw, 2, c), new ColorBlock(2, th, c), new ColorBlock(2, th, c) };
			edges[0].x = tx; edges[0].y = ty;
			edges[1].x = tx; edges[1].y = ty + th - 2;
			edges[2].x = tx; edges[2].y = ty;
			edges[3].x = tx + tw - 2; edges[3].y = ty;
			for (ColorBlock e : edges) ui.add(e);
		}

		//the panel: its own area swallows taps, so they do not reach the table underneath
		PointerArea block = new PointerArea(x0, py, pw, ph);
		block.blockLevel = PointerArea.ALWAYS_BLOCK;
		ui.add(block);
		ColorBlock bg = new ColorBlock(pw, ph, 0xEE0A1420);
		bg.x = x0;
		bg.y = py;
		ui.add(bg);
		ColorBlock edge = new ColorBlock(pw, 1, 0xFFFFFF88);
		edge.x = x0;
		edge.y = py;
		ui.add(edge);
		bringToFrontOf(title);
		bringToFrontOf(body);
		title.setPos(x0 + 4, py + 4);
		body.setPos(x0 + 4, title.bottom() + 2);
		float by = py + ph - buttonsH - 4;
		boolean last = t.index() >= t.count() - 1;
		float bw = (pw - 12) / 3f;
		RedButton next = new RedButton(step.expect == UimsdTutorial.Expect.NEXT ? (last ? "Finish" : "Next") : "Skip step", 7){
			@Override
			protected void onClick(){
				UimsdTutorial tt = tutorial;
				if (tt != null){
					tt.next();
					dirty = true;
				}
			}
		};
		next.setRect(x0 + 4, by, bw, buttonsH);
		ui.add(next);
		RedButton prev = new RedButton("Previous", 7){
			@Override
			protected void onClick(){
				UimsdTutorial tt = tutorial;
				if (tt != null){
					tt.previous();
					dirty = true;
				}
			}
		};
		prev.setRect(x0 + 6 + bw, by, bw, buttonsH);
		prev.enable(t.index() > 0);
		ui.add(prev);
		RedButton exit = new RedButton("Exit tour", 7){
			@Override
			protected void onClick(){
				endTutorial();
				atHome = true;
				dirty = true;
			}
		};
		exit.setRect(x0 + 8 + 2 * bw, by, bw, buttonsH);
		ui.add(exit);
	}

	//puts a text block (created earlier, under the panel) on top of the panel
	private void bringToFrontOf( RenderedTextBlock t ){
		ui.remove(t);
		ui.add(t);
	}

	//the tour can use the player's own runs when there are enough of them to show every kind of screen
	private boolean ownDataIsEnough(){
		if (mainModel == null || mainModel.state() != UimsdModel.State.READY || mainModel.catalog() == null) return false;
		UimsdHome home = UimsdHome.build(mainModel.catalog(), rulesId, false);
		return home.runs >= 5 && home.card("gear") != null;
	}

	private void startTour( final int stepIndex ){
		if (mainModel == null || ApdRecorder.historyRoot() == null && mainRoot == null) return;
		if (tutorial != null) endTutorial();
		if (ownDataIsEnough()){
			addToFront(new WndOptions("Tour", "Which data should the tour use? Your own runs, or a small made-up sample history (nothing in your history is changed either way).",
					"My own runs", "Sample data"){
				@Override
				protected void onSelect( int index ){
					beginTour(stepIndex, index == 0);
				}
			});
		} else {
			beginTour(stepIndex, false);
		}
	}

	private void beginTour( final int stepIndex, final boolean own ){
		tutorialPreparing = true;
		dirty = true;
		Thread worker = new Thread(new Runnable(){
			@Override
			public void run(){
				try {
					File root;
					if (own){
						root = mainRoot;
					} else {
						root = DemoData.sampleRootFor(ApdRecorder.historyRoot() != null ? ApdRecorder.historyRoot() : mainRoot);
						DemoGenerator.generateSample(root, Ruleset.of(SPDSettings.apdExoticChance(), SPDSettings.challenges()), System.currentTimeMillis());
						sampleRoot = root;
					}
					UimsdModel tm = new UimsdModel(rulesId, rulesLabel);
					tm.load(root);
					tm.awaitLoad(30000);
					if (tm.state() != UimsdModel.State.READY) throw new IllegalStateException("the tour's data could not be read");
					UimsdTutorial.prepare(tm, stepIndex);
					UimsdTutorial t = new UimsdTutorial(tm, stepIndex);
					showingSample = !own;
					atHome = stepIndex <= UimsdTutorial.indexOf("view");
					model = tm;
					tm.setListener(new UimsdModel.Listener(){
						@Override
						public void changed(){
							dirty = true;
						}
					});
					tutorial = t;
				} catch (Throwable e){
					tutorial = null;
					showingSample = false;
					model = mainModel;
					deleteSampleLater();
				} finally {
					tutorialPreparing = false;
					dirty = true;
				}
			}
		}, "apd-uimsd-tour");
		worker.setDaemon(true);
		worker.start();
	}

	//stops the tour: back to the player's own model, and the sample history (if any) is deleted
	private static void endTutorial(){
		tutorial = null;
		tutorialPreparing = false;
		showingSample = false;
		if (mainModel != null && model != mainModel){
			if (model != null) model.setListener(null);
			model = mainModel;
		}
		deleteSampleLater();
	}

	private static void deleteSampleLater(){
		final File r = sampleRoot;
		sampleRoot = null;
		if (r == null) return;
		Thread t = new Thread(new Runnable(){
			@Override
			public void run(){
				DemoData.delete(r);
			}
		}, "apd-uimsd-sample-delete");
		t.setDaemon(true);
		t.start();
	}

	//a sample folder left by an app that was closed in the middle of a tour
	private static void removeLeftoverSample(){
		File history = ApdRecorder.historyRoot();
		final File s = history == null ? null : DemoData.sampleRootFor(history);
		if (s == null || !s.exists()) return;
		Thread t = new Thread(new Runnable(){
			@Override
			public void run(){
				DemoData.delete(s);
			}
		}, "apd-uimsd-sample-delete");
		t.setDaemon(true);
		t.start();
	}

	@Override
	protected void onBackPressed(){
		if (tutorialPreparing) return;
		if (!atHome && model != null){
			if (model.canGoBack()){
				model.back();
			} else {
				atHome = true;
				dirty = true;
			}
			return;
		}
		endTutorial();
		if (model != null) model.setListener(null);
		if (backTo == TitleScene.class) ShatteredPixelDungeon.switchNoFade(backTo);
		else ShatteredPixelDungeon.switchScene(backTo);
	}
}

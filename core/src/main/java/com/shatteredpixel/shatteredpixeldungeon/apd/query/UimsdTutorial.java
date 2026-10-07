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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//The interactive tour of UIMSD: a list of steps shown over the real screen, each of which either just explains something (the player
//presses Next) or asks the player to do something and notices when they have, by looking at the model (the same one the screen draws).
//It never changes the model itself and never touches the player's history: it runs on a model of its own, loaded from the player's
//data or from a small sample history. Pure logic, so the whole tour can be tested without a screen.
public final class UimsdTutorial {

	//what the player has to do for a step to finish by itself
	public enum Expect { NEXT, VIEW, DRILL, BACK, FILTER, SORT, COLUMNS }

	public static final class Step {
		public final String id;
		public final String title;
		public final String text;
		public final Expect expect;
		//which element of the screen to point at ("" for none): the scene knows where each one is
		public final String target;

		Step( String id, String title, String text, Expect expect, String target ){
			this.id = id;
			this.title = title;
			this.text = text;
			this.expect = expect;
			this.target = target;
		}
	}

	private static final List<Step> STEPS;
	static {
		List<Step> s = new ArrayList<>();
		s.add(new Step("welcome", "Welcome", "This is UIMSD, the record of your runs. The Home page shows the numbers most people want. "
				+ "This tour walks you through the real screens in a few minutes. You can skip any step.", Expect.NEXT, "home.cards"));
		s.add(new Step("view", "Open a view", "Each card has buttons that open the full table behind it. Tap Weapons on the Gear card.", Expect.VIEW, "home.gear"));
		s.add(new Step("table", "Reading the table", "Each row is one TYPE of weapon, with numbers about all of them. "
				+ "The columns can be sorted by tapping a heading. Tap Next.", Expect.NEXT, "table.header"));
		s.add(new Step("open", "Open a row", "Tap the first row. In the window that opens, choose the button that lists its individual items.", Expect.DRILL, "table.firstRow"));
		s.add(new Step("deeper", "One level down", "You are now looking at the individual items of that type, not the type itself. "
				+ "The line under the view button says where you are.", Expect.NEXT, "status"));
		s.add(new Step("back", "Back", "Tap Back to go up one level.", Expect.BACK, "back"));
		s.add(new Step("filter", "A filter", "Tap Filter, choose a column, how to compare and a value. Only rows that match stay.", Expect.FILTER, "filter"));
		s.add(new Step("filter2", "Stacked filters", "Add a second filter. Filters stack: a row must pass all of them. "
				+ "Tap a filter's button under the view button to change or remove it.", Expect.FILTER, "filter"));
		s.add(new Step("sort", "Sorting", "Tap a column heading to sort by it. Tap again to reverse it.", Expect.SORT, "table.header"));
		s.add(new Step("columns", "Columns", "Tap Columns and switch a column on or off.", Expect.COLUMNS, "columns"));
		s.add(new Step("marks", "The marks", "? means unknown (never recorded, not zero). >=12 means at least 12. ~6 means inferred, worked out rather "
				+ "than recorded exactly. A number with no mark was recorded exactly. Hidden means it belongs to a run you are still playing.", Expect.NEXT, "table.firstRow"));
		s.add(new Step("sample", "Sample size", "Numbers based on fewer than 5 values are shown dimmed: a 100% win rate from 2 runs means little.", Expect.NEXT, "table.header"));
		s.add(new Step("coverage", "Coverage", "The Notes button says what you are looking at, what was left out, and how much of the data "
				+ "each column is based on. Older runs lack some details, which is why some cells show ?.", Expect.NEXT, "notes"));
		s.add(new Step("reports", "Sharing a report", "Options has 'Share a report of this view': the table you are looking at, with its filters, as text, Markdown, "
				+ "a web page or CSV. Home has a Report button for your overall numbers. Reports keep the marks and say what is canonical.", Expect.NEXT, "options"));
		s.add(new Step("home", "Home and Guide", "The Home button returns to the summary. The ? button opens the Guide, where you can read any topic "
				+ "again or take this tour again.", Expect.NEXT, "home"));
		STEPS = Collections.unmodifiableList(s);
	}

	public static List<Step> steps(){
		return STEPS;
	}

	public static int indexOf( String stepId ){
		for (int i = 0; i < STEPS.size(); i++) if (STEPS.get(i).id.equals(stepId)) return i;
		return -1;
	}

	//Puts a model where a step expects it, for a tour started in the middle (from a Guide topic): a table to look at, one level down for
	//the steps about being down there. Uses only what the player could do with the screen's own buttons.
	public static void prepare( UimsdModel m, int stepIndex ){
		if (m.catalog() == null) return;
		if (stepIndex >= indexOf("table") && !"weapons".equals(m.viewId()) && m.catalog().view("weapons") != null) m.showRoot("weapons");
		if (stepIndex >= indexOf("deeper") && stepIndex <= indexOf("back") && m.canOpenGroups()){
			QueryResult r = m.result();
			if (r != null && r.isGrouped() && !r.groups.isEmpty()) m.openGroupRows(r.groups.get(0));
		}
	}

	//---- one run of the tour ----

	private static final class Snapshot {
		final String view;
		final int depth, filters;
		final List<Sort> sorts;
		final List<String> columns;

		Snapshot( UimsdModel m ){
			Query q = m.query();
			view = m.viewId();
			depth = m.depth();
			filters = q.filters.size();
			sorts = new ArrayList<>(q.sorts);
			columns = new ArrayList<>(q.columns);
		}
	}

	private final UimsdModel model;
	private int index;
	private Snapshot base;
	private boolean finished;

	public UimsdTutorial( UimsdModel model, int startAt ){
		this.model = model;
		this.index = Math.max(0, Math.min(STEPS.size() - 1, startAt));
		this.base = new Snapshot(model);
	}

	public UimsdTutorial( UimsdModel model ){
		this(model, 0);
	}

	public boolean isFinished(){
		return finished;
	}

	public int index(){
		return index;
	}

	public int count(){
		return STEPS.size();
	}

	public Step step(){
		return STEPS.get(index);
	}

	//moves on (the Next and Skip buttons)
	public void next(){
		if (finished) return;
		if (index >= STEPS.size() - 1){
			finished = true;
			return;
		}
		index++;
		base = new Snapshot(model);
	}

	public void previous(){
		if (finished || index == 0) return;
		index--;
		base = new Snapshot(model);
	}

	//Looks at the model: if the player has done what the step asked, moves on and says true. Called whenever the model changed.
	public synchronized boolean check(){
		if (finished) return false;
		Step s = step();
		Snapshot now = new Snapshot(model);
		boolean done;
		switch (s.expect){
			case VIEW: done = !now.view.equals(base.view); break;
			case DRILL: done = now.depth > base.depth; break;
			case BACK: done = now.depth < base.depth; break;
			case FILTER: done = now.filters > base.filters; break;
			case SORT: done = !now.sorts.equals(base.sorts); break;
			case COLUMNS: done = !now.columns.equals(base.columns); break;
			default: done = false;
		}
		if (!done) return false;
		next();
		return true;
	}
}

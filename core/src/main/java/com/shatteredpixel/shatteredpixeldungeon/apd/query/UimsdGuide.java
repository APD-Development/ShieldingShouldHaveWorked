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

//The topics of UIMSD's Guide: short plain explanations, each optionally tied to a step of the interactive tour so a player can read it
//or be shown it on the real screen. Pure data (the wording is functional for now; a dedicated language pass comes after all of UIMSD's
//features are finished). The markers explained here are the ones CellFormatter really draws.
public final class UimsdGuide {

	private UimsdGuide(){}

	public static final class Topic {
		public final String id;
		public final String title;
		public final String text;
		//the tour step that shows this on the real screen, or ""
		public final String tourStep;

		Topic( String id, String title, String text, String tourStep ){
			this.id = id;
			this.title = title;
			this.text = text;
			this.tourStep = tourStep;
		}
	}

	private static final List<Topic> TOPICS;
	static {
		List<Topic> t = new ArrayList<>();
		t.add(new Topic("what", "What UIMSD is",
				"UIMSD keeps a record of every run you play and lets you look through it. You do not need to know anything about databases: "
						+ "start on the Home page, which shows the numbers most players want, and tap any card's buttons to go deeper.\n\n"
						+ "Nothing here changes your game. It only reads what was recorded.", "welcome"));
		t.add(new Topic("home", "The Home page",
				"Home sums up your finished runs in a few cards: how many you played and won, how each character does, the gear you use, "
						+ "what ended your runs, your recent runs and your records.\n\n"
						+ "Every card has buttons that open the full table behind it. The Home button on the other screens brings you back.\n\n"
						+ "Home shows runs played under your current rules by default. 'All rules' includes runs played under other rules.", "home"));
		t.add(new Topic("datasets", "Datasets and views",
				"A dataset is one kind of thing UIMSD keeps: Runs, Items, Enemies or Deaths. A view is a way of looking at a dataset: "
						+ "Weapons and Armor are views of Items, Characters is a view of Runs.\n\n"
						+ "Tap the view button at the top (it shows the name of the view you are in, with 'change') to pick another one.", "view"));
		t.add(new Topic("rows", "Rows and types",
				"A table has one row for each thing it lists. In views like Weapons, each row is a TYPE of thing, for example every War Hammer "
						+ "together, with numbers about all of them: how many you met, how often you picked them up, how often you won with them.\n\n"
						+ "Tap a column heading to sort by it. Tap a row to open it.", "table"));
		t.add(new Topic("drill", "Opening a row: drilling down",
				"Tapping a row opens a window with that row's statistics. From there you can go one level down: to the individual items of that "
						+ "type, or to the runs they were in. Each level down is a new table you can sort and filter too.\n\n"
						+ "The line under the view button says where you are.", "open"));
		t.add(new Topic("back", "Back and Home",
				"Back goes up one level, back to where you were before you opened a row. From the top of a view, Back takes you to Home, "
						+ "and from Home it leaves UIMSD.\n\nThe Home button jumps straight to the Home page.", "back"));
		t.add(new Topic("filters", "Filters",
				"A filter keeps only the rows that match a rule, for example 'Class is Mage' or 'Floor is at least 5'. Tap Filter, pick a "
						+ "column, how to compare, and a value. Each filter appears as a button under the view button; tap it to change or remove it.", "filter"));
		t.add(new Topic("stacked", "Several filters at once",
				"Filters stack: a row has to pass ALL of them to stay. Add a second filter and the table gets smaller again. "
						+ "Remove a filter to widen the table.\n\nThe notes line tells you how many rows each filter left out.", "filter2"));
		t.add(new Topic("sort", "Sorting",
				"Tap a column heading to sort by it. Tap again to reverse, and again to go back to the original order. "
						+ "Rows where the value is unknown (marked ?) go to the end, so unknown never looks like zero.", "sort"));
		t.add(new Topic("columns", "Columns",
				"Tap Columns to choose which columns the table shows. Some tables offer many more columns than fit on the screen, "
						+ "grouped by topic. Your choice only changes what you see.", "columns"));
		t.add(new Topic("marks", "The marks: ?, >=, ~ and hidden",
				"?   unknown: the game never recorded this. It is not zero.\n"
						+ ">=  at least: the real number could be higher, because the recording has gaps or an older version of APD could not count it. "
						+ "For example >=12 means 12 or more.\n"
						+ "~   inferred: worked out indirectly rather than recorded exactly.\n"
						+ "hidden   hidden information of a run you are still playing. It is shown once that run is over.\n\n"
						+ "A number with no mark was recorded exactly.", "marks"));
		t.add(new Topic("sample", "Sample size",
				"A number based on very few rows can be misleading: a 100% win rate from 2 runs means little. Numbers resting on fewer than 5 "
						+ "values are shown dimmed. Small groups are left out of grouped tables unless you ask for them under Options.", "sample"));
		t.add(new Topic("coverage", "Coverage and the Notes button",
				"Coverage is how much of the data a number is based on. Runs recorded with older versions of APD lack some details, so some cells "
						+ "show ?. The Notes button lists what you are looking at: which runs are included, what was left out and why, "
						+ "and how many of the shown rows know each column. Lines marked > can be tapped to include what was left out.", "coverage"));
		TOPICS = Collections.unmodifiableList(t);
	}

	public static List<Topic> topics(){
		return TOPICS;
	}

	public static Topic topic( String id ){
		for (Topic t : TOPICS) if (t.id.equals(id)) return t;
		return null;
	}
}

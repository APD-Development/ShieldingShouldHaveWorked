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
import java.util.List;

//The lines of the status strip above a UIMSD table: how many rows there are, what the scope and the filters left out, and how
//much of the data is actually known. Plain sentences, so a screen only has to draw them. A line may carry an action (what tapping
//it does) so "4 unfinished runs hidden" can be turned into "include them".
public final class StatusText {

	private StatusText(){}

	public static final int INFO = 0, NOTE = 1, WARN = 2;

	public static final String ACT_NONE = "", ACT_UNFINISHED = "unfinished", ACT_UNKNOWN_RULESET = "unknownRuleset",
			ACT_MIXED_RULESET = "mixedRuleset", ACT_OTHER_RULESETS = "otherRulesets", ACT_DUPLICATES = "duplicates",
			ACT_SMALL_GROUPS = "smallGroups", ACT_HIDDEN_INFO = "hiddenInfo";

	public static final class Line {
		public final String text;
		public final int kind;
		public final String action;

		Line( String text, int kind, String action ){
			this.text = text;
			this.kind = kind;
			this.action = action;
		}
	}

	private static String plural( int n, String one, String many ){
		return n + " " + (n == 1 ? one : many);
	}

	//rulesetLabel: how the player's current ruleset reads ("challenges 0, exotic 30%"), may be empty
	public static List<Line> lines( QueryResult r, String viewTitle, String rulesetLabel ){
		List<Line> out = new ArrayList<>();
		QueryResult.Stats s = r.stats;
		Query q = r.query;
		if (r.isGrouped()){
			out.add(new Line(viewTitle + ": " + plural(r.groups.size(), "group", "groups") + " from " + s.matching + " of "
					+ plural(s.total, "row", "rows"), INFO, ACT_NONE));
		} else {
			out.add(new Line(viewTitle + ": " + s.matching + " of " + plural(s.total, "row", "rows")
					+ (s.matching == s.inScope ? "" : " (" + s.inScope + " in scope)"), INFO, ACT_NONE));
		}
		String rules;
		switch (q.scope.rules){
			case ALL: rules = "all rulesets"; break;
			case SPECIFIC: rules = "ruleset " + q.scope.ruleset; break;
			default: rules = "your current ruleset" + (rulesetLabel == null || rulesetLabel.isEmpty() ? "" : " (" + rulesetLabel + ")");
		}
		out.add(new Line("Showing " + rules + (q.scope.includeUnfinished ? ", unfinished runs included" : ", finished runs only"), INFO, ACT_NONE));

		if (s.otherRulesets > 0) out.add(new Line(plural(s.otherRulesets, "row is", "rows are") + " from other rulesets (another difficulty, challenges or exotic setting) and left out.", NOTE, ACT_OTHER_RULESETS));
		if (s.unknownRuleset > 0){
			out.add(new Line(plural(s.unknownRuleset, "row has", "rows have") + " an unknown ruleset and "
					+ (s.unknownRuleset == 1 ? "is" : "are") + " left out (tap to include).", NOTE, ACT_UNKNOWN_RULESET));
		}
		if (s.mixedRuleset > 0){
			out.add(new Line(plural(s.mixedRuleset, "run", "runs") + " changed rules while being played and "
					+ (s.mixedRuleset == 1 ? "is" : "are") + " left out (tap to include).", NOTE, ACT_MIXED_RULESET));
		}
		if (s.unfinished > 0){
			out.add(new Line(plural(s.unfinished, "row belongs", "rows belong") + " to a run that is not finished and "
					+ (s.unfinished == 1 ? "is" : "are") + " left out (tap to include).", NOTE, ACT_UNFINISHED));
		}
		if (s.duplicates > 0){
			out.add(new Line(plural(s.duplicates, "imported record is", "imported records are") + " the same run as a recorded one and "
					+ (s.duplicates == 1 ? "is" : "are") + " counted once (tap to show them too).", NOTE, ACT_DUPLICATES));
		}
		if (s.hiddenRows > 0){
			out.add(new Line(plural(s.hiddenRows, "row exists", "rows exist") + " only as hidden information of finished runs. "
					+ "Turn hidden information on to see " + (s.hiddenRows == 1 ? "it" : "them") + ".", NOTE, ACT_HIDDEN_INFO));
		}
		for (QueryResult.FilterReport f : s.filters){
			int lost = f.unknown + f.uncertain + f.hidden;
			if (lost == 0) continue;
			StringBuilder b = new StringBuilder(describe(r.def, f.filter)).append(": ");
			boolean first = true;
			if (f.unknown > 0){
				b.append(f.unknown).append(" not known");
				first = false;
			}
			if (f.uncertain > 0){
				b.append(first ? "" : ", ").append(f.uncertain).append(" undecidable (only a lower bound)");
				first = false;
			}
			if (f.hidden > 0) b.append(first ? "" : ", ").append(f.hidden).append(" hidden");
			b.append(" - left out, not counted as a mismatch.");
			out.add(new Line(b.toString(), NOTE, ACT_NONE));
		}
		if (r.isGrouped() && s.hiddenSmallGroups > 0){
			out.add(new Line(plural(s.hiddenSmallGroups, "group is", "groups are") + " hidden because they have fewer than " + q.minSample
					+ " rows (" + s.hiddenSmallGroupRows + " rows). Tap to show all.", NOTE, ACT_SMALL_GROUPS));
		}
		if (s.runsInScope > 0 && s.runsCovered < s.runsInScope){
			out.add(new Line("Rows come from " + s.runsCovered + " of " + plural(s.runsInScope, "run", "runs") + " in scope; the others were recorded before this data existed.", WARN, ACT_NONE));
		}
		if (q.showInternal) out.add(new Line("Hidden information is on. It is shown for finished runs only, never for a run that is still going.", INFO, ACT_NONE));
		for (String p : r.problems) out.add(new Line(p, WARN, ACT_NONE));
		return out;
	}

	public static String describe( DatasetDef def, Filter f ){
		ColumnDef c = def.column(f.column);
		String col = c == null ? f.column : c.label;
		StringBuilder b = new StringBuilder(col).append(' ').append(f.op.word);
		if (f.op.takesValue()){
			if (c != null && c.type == ColumnDef.Type.TEXT){
				if (f.texts.length > 3){
					b.append(' ').append(f.texts.length).append(" values");   //"runs containing it" can name hundreds
				} else {
					for (int i = 0; i < f.texts.length; i++){
						String t = c.format == ColumnDef.Format.CLASS_NAME ? CellFormatter.className(f.texts[i]) : f.texts[i];
						b.append(i > 0 ? ", " : " ").append('"').append(t).append('"');
					}
				}
			} else {
				for (int i = 0; i < f.nums.length; i++){
					String v = c == null ? Long.toString(f.nums[i]) : CellFormatter.value(c, f.nums[i]);
					b.append(i > 0 ? (f.op == Op.BETWEEN ? " and " : ", ") : " ").append(v);
				}
			}
		}
		return b.toString();
	}
}

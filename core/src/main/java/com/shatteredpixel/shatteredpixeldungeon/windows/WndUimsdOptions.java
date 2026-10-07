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
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ColumnDef;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.GroupBy;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Query;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.QueryEngine;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.QueryResult;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Scope;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdModel;

import java.util.ArrayList;
import java.util.List;

//APD: the options of a UIMSD screen: hidden information, which rulesets and which runs are in scope, the minimum sample size,
//grouping, and the explanations of coverage and markers. Tapping an option applies it and reopens this list with the new state.
public final class WndUimsdOptions {

	private WndUimsdOptions(){}

	private static String box( boolean on, String text ){
		return (on ? "[x] " : "[ ] ") + text;
	}

	private static String count( int n ){
		return n > 0 ? " (" + n + " left out)" : "";
	}

	public static void show( final UimsdModel m ){
		final QueryResult r = m.result();
		if (r == null) return;
		final Query q = m.query();
		final Scope s = q.scope;
		final List<String> labels = new ArrayList<>();
		final List<Runnable> actions = new ArrayList<>();

		labels.add(box(q.showInternal, "Show hidden information (finished runs only)"));
		actions.add(new Runnable(){ public void run(){ m.setShowInternal(!q.showInternal); show(m); } });

		String rules = s.rules == Scope.Rules.ALL ? "all rulesets" : s.rules == Scope.Rules.SPECIFIC ? s.ruleset : "your current ruleset";
		labels.add("Rules in scope: " + rules + count(r.stats.otherRulesets));
		actions.add(new Runnable(){ public void run(){ pickRuleset(m); } });

		labels.add(box(s.includeUnfinished, "Include unfinished runs") + count(r.stats.unfinished));
		actions.add(new Runnable(){ public void run(){ m.setScope(s.withUnfinished(!s.includeUnfinished)); show(m); } });
		labels.add(box(s.includeUnknownRuleset, "Include rows with an unknown ruleset") + count(r.stats.unknownRuleset));
		actions.add(new Runnable(){ public void run(){ m.setScope(s.withUnknownRuleset(!s.includeUnknownRuleset)); show(m); } });
		labels.add(box(s.includeMixedRuleset, "Include runs whose rules changed mid-run") + count(r.stats.mixedRuleset));
		actions.add(new Runnable(){ public void run(){ m.setScope(s.withMixedRuleset(!s.includeMixedRuleset)); show(m); } });
		labels.add(box(s.includeDuplicates, "Include imported records that are also recorded runs") + count(r.stats.duplicates));
		actions.add(new Runnable(){ public void run(){ m.setScope(s.withDuplicates(!s.includeDuplicates)); show(m); } });

		labels.add("Smallest group shown: " + (q.minSample <= 1 ? "any" : q.minSample + " rows"));
		actions.add(new Runnable(){ public void run(){ pickMinSample(m); } });
		labels.add(q.group == null ? "Group by..." : "Grouping: change or remove...");
		actions.add(new Runnable(){ public void run(){ pickGroup(m); } });
		labels.add("How much is known, per column");
		actions.add(new Runnable(){ public void run(){ coverage(m); } });
		labels.add("What the markers mean");
		actions.add(new Runnable(){ public void run(){ legend(); } });
		labels.add("Reset this view");
		actions.add(new Runnable(){ public void run(){ m.resetView(); } });

		ShatteredPixelDungeon.scene().addToFront(new WndUimsdList("Options",
				"Hidden information is shown only for runs that are finished, never for a run still going.", labels, null,
				new WndUimsdList.Picker(){
					@Override
					public void onPick( int i, boolean checked ){
						actions.get(i).run();
					}
				}));
	}

	private static void pickRuleset( final UimsdModel m ){
		final Catalog cat = m.catalog();
		final List<String> ids = new ArrayList<>();
		final List<String> labels = new ArrayList<>();
		ids.add("*current");
		labels.add("Your current ruleset");
		ids.add("*all");
		labels.add("All rulesets");
		if (cat != null){
			for (Catalog.RulesetInfo ri : cat.rulesets){
				ids.add(ri.id);
				labels.add(ri.label + "  (" + ri.runs + (ri.runs == 1 ? " run)" : " runs)"));
			}
		}
		ShatteredPixelDungeon.scene().addToFront(new WndUimsdList("Rules in scope",
				"Runs played under different rules are never mixed unless you choose all rulesets.", labels, null, new WndUimsdList.Picker(){
			@Override
			public void onPick( int i, boolean checked ){
				Scope s = m.query().scope;
				String id = ids.get(i);
				if (id.equals("*current")) m.setScope(s.withRules(Scope.Rules.CURRENT, ""));
				else if (id.equals("*all")) m.setScope(s.withRules(Scope.Rules.ALL, ""));
				else m.setScope(s.withRules(Scope.Rules.SPECIFIC, id));
				show(m);
			}
		}));
	}

	private static void pickMinSample( final UimsdModel m ){
		final int[] sizes = { 0, 3, 5, 10, 20, 50 };
		List<String> labels = new ArrayList<>();
		for (int n : sizes) labels.add(n == 0 ? "Show every group" : n + " rows or more");
		ShatteredPixelDungeon.scene().addToFront(new WndUimsdList("Smallest group shown",
				"Groups with fewer rows are listed as hidden, not dropped.", labels, null, new WndUimsdList.Picker(){
			@Override
			public void onPick( int i, boolean checked ){
				m.setMinSample(sizes[i]);
				show(m);
			}
		}));
	}

	private static void pickGroup( final UimsdModel m ){
		final List<ColumnDef> cols = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		labels.add("No grouping");
		for (ColumnDef c : m.offeredColumns()){
			cols.add(c);
			labels.add("Group by " + c.label);
		}
		ShatteredPixelDungeon.scene().addToFront(new WndUimsdList("Group by", "Rows with the same value are counted together.", labels, null,
				new WndUimsdList.Picker(){
					@Override
					public void onPick( int i, boolean checked ){
						if (i == 0){
							m.setGroup(null);
						} else {
							ColumnDef key = cols.get(i - 1);
							List<Aggregate> aggs = new ArrayList<>();
							aggs.add(Aggregate.count());
							int numeric = 0;
							for (ColumnDef c : m.offeredColumns()){
								if (c != key && c.type == ColumnDef.Type.NUM && c.defaultVisible && numeric < 3){
									aggs.add(Aggregate.of(Aggregate.Fn.AVG, c.id));
									numeric++;
								}
							}
							List<String> keys = new ArrayList<>();
							keys.add(key.id);
							m.setGroup(new GroupBy(keys, aggs));
						}
						show(m);
					}
				}));
	}

	private static void coverage( UimsdModel m ){
		if (m.dataset() == null) return;
		List<String> ids = new ArrayList<>();
		for (ColumnDef c : m.offeredColumns()) ids.add(c.id);
		QueryResult r = QueryEngine.run(m.dataset(), m.query().withColumns(ids).withGroup(null), m.currentRuleset());
		StringBuilder b = new StringBuilder();
		b.append(r.stats.matching).append(" rows in the current result.\n\n");
		for (QueryResult.ColumnCoverage c : r.coverage){
			b.append(c.column.label).append(": ").append(c.known).append(" of ").append(c.total).append(" known");
			if (c.lowerBound > 0) b.append(", ").append(c.lowerBound).append(" only a lower bound");
			if (c.inferred > 0) b.append(", ").append(c.inferred).append(" inferred");
			if (c.hidden > 0) b.append(", ").append(c.hidden).append(" hidden");
			b.append('\n');
		}
		ShatteredPixelDungeon.scene().addToFront(new WndUimsdText("How much is known", b.toString()));
	}

	private static void legend(){
		ShatteredPixelDungeon.scene().addToFront(new WndUimsdText("What the markers mean",
				"?  unknown: it was never recorded. This is not the same as zero.\n"
						+ "hidden  hidden information that cannot be shown (the run is not finished, or hidden information is off).\n"
						+ ">=12  at least 12: the log has gaps, or an older recording version could not count everything.\n"
						+ "~6  inferred, not recorded exactly (healing, for example).\n"
						+ "Blue values come from an imported ranking record.\n\n"
						+ "Totals and averages use only the values that are known, and say how many that was. "
						+ "A total that includes a lower bound is itself a lower bound."));
	}
}

package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdRecorder;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.DerivedStore;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKeys;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactTable;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunFacts;
import com.watabou.noosa.Game;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

//Runs played, deepest floor, win rate and top death causes, by class, aggression and intelligence (from the simulator's own UIMSD).
final class WndSimSummary extends WndSimText {

	WndSimSummary(){
		super("Simulator summary", "Reading the simulator's UIMSD...", null);
		new Thread(() -> {
			final String text = build();
			Game.runOnRenderThread(() -> {
				hide();
				Game.scene().add(new WndSimText("Simulator summary", text, null));
			});
		}, "apdsim-summary").start();
	}

	static final class Group {
		int runs, wins, deepest;
		long floors;
		final Map<String, Integer> causes = new HashMap<>();
	}

	static String build(){
		try {
			File root = ApdRecorder.historyRoot();
			if (root == null) return "The simulator's UIMSD is not set up.";
			FactTable t = new DerivedStore(root).load();
			//runs started from a checkpoint are tests (boss fights and deep starts): counted apart, never in the win rates
			List<RunFacts> rows = new ArrayList<>(), tests = new ArrayList<>();
			for (RunFacts f : t.apdRows) (f.textOr("sim.settings", "").contains("checkpoint=") ? tests : rows).add(f);
			StringBuilder b = new StringBuilder();
			b.append(rows.size()).append(" simulated runs recorded")
					.append(tests.isEmpty() ? "" : ", plus " + tests.size() + " TEST runs from checkpoints that are left out of everything below").append(".\n").append(Digest.readiness()).append('\n');
			section(b, "By class", rows, FactKeys.HERO_CLASS);
			section(b, "By aggression", rows, "sim.aggression");
			section(b, "By intelligence", rows, "sim.intelligence");
			section(b, "By aggression and intelligence", rows, null);
			return b.toString();
		} catch (Throwable e){
			return "Could not read the simulator's UIMSD: " + e;
		}
	}

	private static void section(StringBuilder b, String title, List<RunFacts> rows, String key){
		Map<String, Group> groups = new TreeMap<>();
		for (RunFacts f : rows){
			String k = key == null ? f.textOr("sim.aggression", "?") + " / " + f.textOr("sim.intelligence", "?") : f.textOr(key, "not recorded");
			Group g = groups.computeIfAbsent(k, x -> new Group());
			g.runs++;
			if (f.numOr(FactKeys.OUTCOME, -1) == LifecycleEvents.OUTCOME_WON) g.wins++;
			int d = (int) f.numOr(FactKeys.DEEPEST, 0);
			g.deepest = Math.max(g.deepest, d);
			g.floors += d;
			if (f.numOr(FactKeys.OUTCOME, -1) == LifecycleEvents.OUTCOME_DIED){
				String cause = f.textOr(FactKeys.CAUSE, "unknown");
				g.causes.merge(cause, 1, Integer::sum);
			}
		}
		b.append("\n").append(title.toUpperCase(Locale.ROOT)).append('\n');
		for (Map.Entry<String, Group> e : groups.entrySet()){
			Group g = e.getValue();
			b.append(e.getKey()).append(": ").append(g.runs).append(g.runs == 1 ? " run" : " runs")
					.append(String.format(Locale.ROOT, ", win rate %.1f%%", 100.0 * g.wins / Math.max(1, g.runs)))
					.append(", deepest floor ").append(g.deepest)
					.append(String.format(Locale.ROOT, ", average floor %.1f", (double) g.floors / Math.max(1, g.runs))).append('\n');
			List<Map.Entry<String, Integer>> top = new ArrayList<>(g.causes.entrySet());
			top.sort((a, c) -> c.getValue() - a.getValue());
			for (int i = 0; i < Math.min(3, top.size()); i++){
				b.append("   killed by ").append(top.get(i).getKey()).append(": ").append(top.get(i).getValue()).append('\n');
			}
		}
	}
}

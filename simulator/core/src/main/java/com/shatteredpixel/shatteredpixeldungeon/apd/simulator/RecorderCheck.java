package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdRecorder;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.DerivedStore;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKeys;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactTable;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunFacts;
import com.watabou.noosa.Game;

import java.io.File;

//After a finished run, reads it back from the simulator's own UIMSD and checks it says what the game did.
final class RecorderCheck {

	private RecorderCheck(){}

	static void later(final SimRuntime sim, final String logId, final HeroClass cls, final int deepest, final int depth, final int heroLevel){
		if (logId == null || logId.isEmpty()) return;
		Thread t = new Thread(() -> {
			try {
				Thread.sleep(3000); //let the recorder seal the log
				File root = ApdRecorder.historyRoot();
				if (root == null) return;
				FactTable table = new DerivedStore(root).load();
				RunFacts row = null;
				for (RunFacts f : table.apdRows){
					if (logId.equals(f.textOr(FactKeys.LOG_ID, ""))) row = f;
				}
				final StringBuilder problems = new StringBuilder();
				if (row == null){
					problems.append("The finished run (log ").append(logId).append(") is missing from the simulator's UIMSD. ");
				} else {
					String recordedClass = row.textOr(FactKeys.HERO_CLASS, "");
					if (cls != null && !recordedClass.isEmpty() && !recordedClass.equalsIgnoreCase(cls.name()))
						problems.append("class: game ").append(cls.name()).append(", UIMSD ").append(recordedClass).append(". ");
					long recDeepest = row.numOr(FactKeys.DEEPEST, -1);
					if (recDeepest >= 0 && recDeepest != deepest)
						problems.append("deepest floor: game ").append(deepest).append(", UIMSD ").append(recDeepest).append(". ");
					long recDepth = row.numOr(FactKeys.DEPTH, -1);
					if (recDepth >= 0 && recDepth != depth)
						problems.append("final floor: game ").append(depth).append(", UIMSD ").append(recDepth).append(". ");
					long recLevel = row.numOr(FactKeys.HERO_LEVEL, -1);
					if (recLevel >= 0 && recLevel != heroLevel)
						problems.append("hero level: game ").append(heroLevel).append(", UIMSD ").append(recLevel).append(". ");
					if (!row.isKnown(FactKeys.OUTCOME)) problems.append("the run's outcome was not recorded. ");
				}
				if (problems.length() > 0){
					final String text = problems.toString();
					Game.runOnRenderThread(() -> sim.report(BugReport.Kind.RECORDER, "recmismatch:" + text.replaceAll("[0-9]+", "#"),
							"The simulator's UIMSD disagrees with the game about a finished run.", text));
				}
			} catch (Throwable e){
				Game.runOnRenderThread(() -> sim.botProblem("reading the run back from the UIMSD", e));
			}
		}, "apdsim-recorder-check");
		t.setDaemon(true);
		t.start();
	}
}

package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.badlogic.gdx.files.FileHandle;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;
import com.watabou.utils.GameSettings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//One notification a day with the day's totals (runs, new bugs, deepest floor), on top of the per-bug notifications.
//Also works out launch readiness: how many runs in a row this build has played with no crash, exception or save-loss bug.
final class Digest {
	private Digest(){}

	private static String today(){
		return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(new Date());
	}

	static void newBug(){
		GameSettings.put("apdsim_day_bugs", GameSettings.getInt("apdsim_day_bugs", 0) + 1);
	}

	static void runEnded(SimRuntime sim, int deepest){
		String day = GameSettings.getString("apdsim_day", "");
		if (!day.isEmpty() && !day.equals(today())){
			int runs = GameSettings.getInt("apdsim_day_runs", 0);
			int bugs = GameSettings.getInt("apdsim_day_bugs", 0);
			int deep = GameSettings.getInt("apdsim_day_deepest", 0);
			sim.platform.notifyBug(8, "APD Simulator: daily digest for " + day,
					runs + (runs == 1 ? " run" : " runs") + ", " + bugs + " new " + (bugs == 1 ? "bug" : "bugs") + ", deepest floor " + deep
							+ ". " + readiness());
			GameSettings.put("apdsim_day_runs", 0);
			GameSettings.put("apdsim_day_bugs", 0);
			GameSettings.put("apdsim_day_deepest", 0);
		}
		GameSettings.put("apdsim_day", today());
		GameSettings.put("apdsim_day_runs", GameSettings.getInt("apdsim_day_runs", 0) + 1);
		GameSettings.put("apdsim_day_deepest", Math.max(deepest, GameSettings.getInt("apdsim_day_deepest", 0)));
	}

	//"Launch readiness: 212 clean runs in a row on this build (target 1000)."
	static final int READY_AFTER = 1000;

	static String readiness(){
		int clean = 0, total = 0;
		try {
			FileHandle f = FileUtils.getFileHandle("simruns.txt");
			if (f.exists()){
				String build = "| build " + Game.version + " |";
				for (String line : f.readString("UTF-8").split("\n")){
					if (!line.contains(build)) continue;
					total++;
					if (line.contains("| crashes 0 |")) clean++; else clean = 0;
				}
			}
		} catch (Exception ignored){}
		return "Launch readiness: " + clean + " clean " + (clean == 1 ? "run" : "runs") + " in a row on this build (of " + total + " played; fit to ship at "
				+ READY_AFTER + ")" + (clean >= READY_AFTER ? ": READY." : ".");
	}
}

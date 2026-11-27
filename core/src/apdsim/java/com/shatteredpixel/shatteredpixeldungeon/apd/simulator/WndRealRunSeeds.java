package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKeys;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ReadResult;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunFacts;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunFactsBuilder;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunLogReader;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.noosa.ui.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Lists seeds of YOUR real runs, reading the real history's log files only (nothing is written there, no index, no cache).
final class WndRealRunSeeds extends Window {

	//where the real game's history is; set by the simulator's launcher
	static File realHistoryRoot;

	WndRealRunSeeds(final SimProfile p){
		super();
		int width = PixelScene.landscape() ? 240 : 150;
		int height = (int) (PixelScene.uiCamera.height * 0.7f);
		ScrollPane pane = new ScrollPane(new Component());
		add(pane);
		Component c = pane.content();
		float y = 0;
		for (final String[] run : runs()){
			RedButton b = new RedButton(run[1], 6){
				@Override protected void onClick(){
					p.seedMode = SimProfile.SeedMode.REAL_RUN;
					p.seedText = run[0];
					p.save();
					hide();
					ShatteredPixelDungeon.switchNoFade(SimHomeScene.class);
				}
			};
			b.multiline = true;
			b.setRect(0, y, width, 22);
			c.add(b);
			y += 24;
		}
		if (y == 0){
			RedButton none = new RedButton("No real runs with a seed were found.", 7);
			none.enable(false);
			none.setRect(0, 0, width, 20);
			c.add(none);
			y = 22;
		}
		c.setSize(width, y);
		pane.setRect(0, 0, width, height);
		resize(width, height);
	}

	static List<String[]> runs(){
		List<String[]> out = new ArrayList<>();
		if (realHistoryRoot == null) return out;
		File[] logs = new File(realHistoryRoot, "runs").listFiles();
		if (logs == null) return out;
		Arrays.sort(logs, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
		java.util.Set<Long> seen = new java.util.HashSet<>();
		for (File f : logs){
			if (out.size() >= 60) break;
			if (!f.isFile() || !f.getName().endsWith(".apdlog")) continue;
			try {
				ReadResult read = RunLogReader.read(f);
				RunFacts facts = RunFactsBuilder.fromLog(read, f.getName());
				long type = facts.numOr(FactKeys.SEED_TYPE, -1);
				if (type != LifecycleEvents.SEED_RANDOM && type != LifecycleEvents.SEED_CUSTOM) continue;
				if (!facts.isKnown(FactKeys.SEED)) continue;
				long seed = facts.numOr(FactKeys.SEED, 0);
				if (!seen.add(seed)) continue;
				String code = DungeonSeed.convertToCode(seed);
				out.add(new String[]{code, code + ": " + facts.textOr(FactKeys.HERO_CLASS, "?") + ", floor " + facts.numOr(FactKeys.DEEPEST, 0)});
			} catch (Exception unreadable){
				//skipped
			}
		}
		return out;
	}
}

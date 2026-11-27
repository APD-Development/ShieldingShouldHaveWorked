package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//Floor audit: generates whole dungeons (floors 1-26) without playing them and checks every floor against the real map
//(FloorTruth): sealed areas, missing or unreachable exits, key chains, unreachable items, Chris's floor rules.
//Must not run while a simulated run is in progress (the game keeps one dungeon at a time).
public final class FloorAudit {

	private FloorAudit(){}

	public static final class Finding {
		public final long seed;
		public final HeroClass heroClass;
		public final int depth;
		public final String problem;
		Finding(long seed, HeroClass c, int depth, String problem){ this.seed = seed; this.heroClass = c; this.depth = depth; this.problem = problem; }
		public String line(){
			return "seed " + DungeonSeed.convertToCode(seed) + " (" + seed + "), " + heroClass.name() + ", floor " + depth + ": " + problem;
		}
	}

	public interface Progress { boolean floorDone(int floorsChecked, int findings); }

	//one whole dungeon for one seed; returns what was wrong
	public static List<Finding> auditSeed(long seed, HeroClass cls, int maxDepth){
		List<Finding> out = new ArrayList<>();
		Dungeon.daily = Dungeon.dailyReplay = false;
		Dungeon.customSeedText = "";
		Dungeon.seed = seed;
		GamesInProgress.selectedClass = cls;
		GamesInProgress.randomizedClass = false;
		Dungeon.hero = null;
		Dungeon.init();
		for (int depth = 1; depth <= maxDepth; depth++){
			Dungeon.depth = depth;
			Dungeon.branch = 0;
			Level level;
			try {
				level = Dungeon.newLevel();
			} catch (Throwable t){
				out.add(new Finding(seed, cls, depth, "floor generation crashed: " + t));
				break;
			}
			Dungeon.level = level;
			for (String p : FloorTruth.check(level, depth, -1).problems) out.add(new Finding(seed, cls, depth, p));
		}
		return out;
	}

	//many random dungeons; stops early when progress returns false
	public static List<Finding> run(int seeds, long rngSeed, Progress progress){
		Random rng = new Random(rngSeed);
		List<Finding> all = new ArrayList<>();
		HeroClass[] classes = HeroClass.values();
		int floors = 0;
		for (int i = 0; i < seeds; i++){
			long seed = Math.floorMod(rng.nextLong(), DungeonSeed.TOTAL_SEEDS);
			List<Finding> f = auditSeed(seed, classes[i % classes.length], 26);
			all.addAll(f);
			floors += 26;
			if (progress != null && !progress.floorDone(floors, all.size())) break;
		}
		return all;
	}
}

package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.utils.FileUtils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

//Uniformly random seeds (fair for balancing) that the simulator has never played before.
final class SeedPicker {
	private SeedPicker(){}

	private static Set<Long> played;

	private static FileHandle file(){ return FileUtils.getFileHandle("simseeds.txt"); }

	private static synchronized Set<Long> played(){
		if (played == null){
			played = new HashSet<>();
			try {
				if (file().exists()) for (String line : file().readString("UTF-8").split("\n")){
					line = line.trim();
					if (!line.isEmpty()) played.add(Long.parseLong(line));
				}
			} catch (Exception ignored){}
		}
		return played;
	}

	static synchronized long freshRandomSeed(Random rng){
		long s;
		do { s = Math.floorMod(rng.nextLong(), DungeonSeed.TOTAL_SEEDS); } while (played().contains(s));
		return s;
	}

	static synchronized void remember(long seed){
		if (played().add(seed)){
			try { file().writeString(seed + "\n", true, "UTF-8"); } catch (Exception ignored){}
		}
	}

	static synchronized void forget(){
		played = new HashSet<>();
		file().delete();
	}
}

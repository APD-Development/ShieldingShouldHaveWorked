package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//Checkpoints: when a run reaches a new floor, the game is saved and a copy of the save is kept, so later runs can start deep in the
//dungeon and floors 20+ get as many hours as floors 1 to 5. Runs started from a checkpoint are tagged and kept apart in the stats.
//Stored only in the simulator's own folder (simcheckpoints/), the newest few per class and floor.
final class Checkpoints {
	private Checkpoints(){}

	static final String DIR = "simcheckpoints/";
	static final int KEEP_PER_FLOOR = 3;
	static final int KEEP_PER_BOSS_FLOOR = 40;

	private static FileHandle dir(){
		FileHandle d = FileUtils.getFileHandle(DIR);
		d.mkdirs();
		return d;
	}

	//called on the bot's first turn on a new deepest floor
	static void save(String heroClass, int depth){
		try {
			Dungeon.saveAll();
			FileHandle slot = FileUtils.getFileHandle(GamesInProgress.gameFolder(1));
			if (!slot.exists()) return;
			String name = heroClass + "-d" + (depth < 10 ? "0" : "") + depth + "-" + DungeonSeed.convertToCode(Dungeon.seed) + "-" + System.currentTimeMillis();
			FileHandle target = dir().child(name);
			slot.copyTo(target.parent());
			target.parent().child(slot.name()).moveTo(target);
			//what the hero carried, readable without loading the save
			try { target.child("checkpoint.txt").writeString("floor " + depth + ", " + heroClass + ", health " + Dungeon.hero.HP + "/" + Dungeon.hero.HT
					+ ", " + SimBot.gearSummary() + "\n", false, "UTF-8"); } catch (Throwable ignored){}
			prune(heroClass, depth);
		} catch (Throwable ignored){
			//a checkpoint is a convenience; a failure never stops the run
		}
	}

	private static void prune(String heroClass, int depth){
		List<FileHandle> same = new ArrayList<>();
		String prefix = heroClass + "-d" + (depth < 10 ? "0" : "") + depth + "-";
		for (FileHandle f : dir().list()) if (f.isDirectory() && f.name().startsWith(prefix)) same.add(f);
		same.sort((a, b) -> b.name().compareTo(a.name()));
		//boss floors keep many more: they are what boss tests start from
		int keep = depth % 5 == 0 ? KEEP_PER_BOSS_FLOOR : KEEP_PER_FLOOR;
		for (int i = keep; i < same.size(); i++) same.get(i).deleteDirectory();
	}

	static List<FileHandle> all(){
		List<FileHandle> out = new ArrayList<>();
		for (FileHandle f : dir().list()) if (f.isDirectory()) out.add(f);
		out.sort((a, b) -> a.name().compareTo(b.name()));
		return out;
	}

	//picks one (optionally for a class and at least a floor); null when there is none
	static FileHandle pick(Random rng, String heroClass, int minDepth){
		return pick(rng, heroClass, minDepth, false, null);
	}

	//exact: only checkpoints saved on that very floor. nameHas: only checkpoints whose name contains this text
	//(a class, a seed code or a whole snapshot name), so one chosen snapshot can be played over and over.
	static FileHandle pick(Random rng, String heroClass, int minDepth, boolean exact, String nameHas){
		List<FileHandle> ok = new ArrayList<>();
		for (FileHandle f : all()){
			String[] p = f.name().split("-");
			if (heroClass != null && !p[0].equals(heroClass)) continue;
			if (nameHas != null && !nameHas.isEmpty() && !f.name().contains(nameHas)) continue;
			int d = Integer.parseInt(p[1].substring(1));
			if (d < minDepth || (exact && d != minDepth)) continue;
			ok.add(f);
		}
		return ok.isEmpty() ? null : ok.get(rng.nextInt(ok.size()));
	}

	//puts the checkpoint into save slot 1; the caller then continues that game
	static boolean restore(FileHandle cp){
		try {
			FileHandle slot = FileUtils.getFileHandle(GamesInProgress.gameFolder(1));
			if (slot.exists()) slot.deleteDirectory();
			slot.mkdirs();
			for (FileHandle f : cp.list()) if (!f.name().equals("checkpoint.txt")) f.copyTo(slot);
			GamesInProgress.setUnknown(1);
			return GamesInProgress.check(1) != null;
		} catch (Throwable t){
			return false;
		}
	}
}

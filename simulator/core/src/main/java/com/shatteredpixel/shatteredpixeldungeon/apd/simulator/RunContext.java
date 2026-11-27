package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.apd.replay.ReplayContext;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;

//Level generation also reads the journal (which guide and lore pages were found). So that every simulated run is a pure
//function of its seed (and a bug replays exactly however many runs later), each run starts from the game's own reference journal,
//the one floor replays use: every Adventurer's Guide and Alchemy Guide page found and read, no lore page found. Only the
//simulator's own journal is touched. Shop bag ties are broken the deterministic way the game's tests use.
final class RunContext {
	private RunContext(){}

	static void apply(){
		ReplayContext.forceDeterministicBags = true;
		//a dead hero's remains change the next runs' floors; each simulated run starts without any so it replays exactly
		//(known gap: the simulator therefore never meets remains; a later mode can play with recorded ones)
		try { com.watabou.utils.FileUtils.deleteFile("bones.dat"); } catch (Throwable ignored){}
		//the game also remembers remains in memory after a death in this session
		try {
			Class<?> bones = com.shatteredpixel.shatteredpixeldungeon.Bones.class;
			for (java.lang.reflect.Field f : bones.getDeclaredFields()){
				if (!java.lang.reflect.Modifier.isStatic(f.getModifiers()) || java.lang.reflect.Modifier.isFinal(f.getModifiers())) continue;
				f.setAccessible(true);
				if (f.getType() == int.class) f.setInt(null, -1);
				else if (!f.getType().isPrimitive()) f.set(null, null);
			}
		} catch (Throwable ignored){}
		//the journal must be loaded BEFORE it is set to the reference state: a later first load would overwrite it from disk
		com.shatteredpixel.shatteredpixeldungeon.journal.Journal.loadGlobal();
		for (Document doc : Document.values()){
			for (String page : doc.pageNames().toArray(new String[0])){
				if (ReplayContext.pageFound(doc)){
					doc.findPage(page);
					doc.readPage(page);
				} else {
					doc.deletePage(page);
				}
			}
		}
		com.shatteredpixel.shatteredpixeldungeon.journal.Journal.saveGlobal(true);
	}
}

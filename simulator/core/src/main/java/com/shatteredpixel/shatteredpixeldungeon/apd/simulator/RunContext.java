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
	}
}

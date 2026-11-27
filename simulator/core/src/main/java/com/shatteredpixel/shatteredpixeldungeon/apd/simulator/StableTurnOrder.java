package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedHashSet;

//The game keeps everything that takes turns in a HashSet ordered by memory address, so when two actors are due at the same
//moment, which goes first changes from launch to launch: the same seed and the same decisions can play out differently.
//Inside the SIMULATOR'S copy of the game only, the sets keep the order actors were added in, so runs replay exactly.
//(Whether the real game should do the same is Chris's decision.)
final class StableTurnOrder {
	private StableTurnOrder(){}

	static boolean installed;

	//the floor's monster set is a HashSet too (iterated for noise, targeting and more): same treatment for each new floor
	static void forLevel(com.shatteredpixel.shatteredpixeldungeon.levels.Level level){
		if (level == null || level.mobs instanceof LinkedHashSet) return;
		level.mobs = new LinkedHashSet<>(level.mobs);
	}

	@SuppressWarnings("unchecked")
	static synchronized void install(){
		if (installed) return;
		try {
			for (String name : new String[]{"all", "chars"}){
				Field f = Actor.class.getDeclaredField(name);
				f.setAccessible(true);
				HashSet<Object> old = (HashSet<Object>) f.get(null);
				f.set(null, new LinkedHashSet<>(old));
			}
			installed = true;
		} catch (Throwable t){
			installed = false;
		}
	}
}

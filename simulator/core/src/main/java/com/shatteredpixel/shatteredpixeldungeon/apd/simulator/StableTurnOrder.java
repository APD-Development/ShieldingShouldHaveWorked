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

	//Called on the render thread when a floor's scene has just been created, before anyone acts. While the floor loaded, the
	//engine filled its sets in memory-address order; here they are put into a fixed order (by position, then kind) so the
	//same floor always takes its turns the same way.
	@SuppressWarnings("unchecked")
	static void forLevel(com.shatteredpixel.shatteredpixeldungeon.levels.Level level){
		if (level == null) return;
		try {
			java.util.List<com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob> mobs = new java.util.ArrayList<>(level.mobs);
			mobs.sort(java.util.Comparator.comparingInt((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m) -> m.pos)
					.thenComparing(m -> m.getClass().getName()));
			level.mobs = new LinkedHashSet<>(mobs);
			synchronized (Actor.class){
				for (String name : new String[]{"all", "chars"}){
					Field f = Actor.class.getDeclaredField(name);
					f.setAccessible(true);
					java.util.List<Object> list = new java.util.ArrayList<>((java.util.Collection<Object>) f.get(null));
					list.sort(java.util.Comparator.comparing(StableTurnOrder::key));
					f.set(null, new LinkedHashSet<>(list));
				}
			}
		} catch (Throwable t){
			installed = false;
		}
	}

	//characters by position; effects by who they are on; everything else by kind (the sort is stable, so equal keys keep their order)
	private static String key(Object a){
		if (a instanceof com.shatteredpixel.shatteredpixeldungeon.actors.Char)
			return String.format("1 %07d %s", ((com.shatteredpixel.shatteredpixeldungeon.actors.Char) a).pos, a.getClass().getName());
		if (a instanceof com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff){
			com.shatteredpixel.shatteredpixeldungeon.actors.Char t = ((com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff) a).target;
			return String.format("2 %07d %s", t == null ? -1 : t.pos, a.getClass().getName());
		}
		return "0 " + a.getClass().getName();
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

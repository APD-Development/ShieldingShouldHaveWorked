package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;
import java.util.List;

//What is TRUE about a floor, using full knowledge (hidden doors, every key), unlike the bot which only knows what it saw.
//Used to tell "the bot was dumb" from "the game is broken", and by the floor audit and seed fairness checks.
public final class FloorTruth {

	private FloorTruth(){}

	public static final class Result {
		public boolean exitReachable = true;
		public boolean entranceToExit = true;
		public final List<String> problems = new ArrayList<>();
	}

	//cells reachable from start; locked doors open only if a matching key can itself be reached (key chains are followed)
	public static boolean[] reach(Level level, int start, boolean keysAnywhere){
		return reach(level, start, keysAnywhere, false);
	}

	//itemsMode: also allows what a prepared player can do on purpose to reach a puzzle room: levitate over chasms, burn barricades,
	//put out magical fire (blocked floor tiles). Exits must be reachable without any of that.
	public static boolean[] reach(Level level, int start, boolean keysAnywhere, boolean itemsMode){
		int len = level.length(), w = level.width();
		boolean[] seen = new boolean[len];
		if (start < 0 || start >= len) return seen;
		boolean iron = keysAnywhere, crystal = keysAnywhere;
		boolean changed = true;
		while (changed){
			changed = false;
			seen = new boolean[len];
			int[] q = new int[len];
			int h = 0, t = 0;
			q[t++] = start;
			seen[start] = true;
			while (h < t){
				int c = q[h++];
				for (int d : PathFinder.NEIGHBOURS8){
					int n = c + d;
					if (n < 0 || n >= len || seen[n] || Math.abs(n % w - c % w) > 1) continue;
					if (!walkable(level, n, iron, crystal, itemsMode)) continue;
					int mc = level.map[c], mn = level.map[n];
					boolean diagonal = d != 1 && d != -1 && d != w && d != -w;
					if (diagonal && (isDoorLike(mc) || isDoorLike(mn))) continue;
					seen[n] = true;
					q[t++] = n;
				}
			}
			//picking up a key found in the reachable area opens more doors
			for (Heap heap : level.heaps.valueList()){
				if (!seen[heap.pos] || heap.type == Heap.Type.LOCKED_CHEST || heap.type == Heap.Type.CRYSTAL_CHEST) continue;
				for (Item i : heap.items){
					if (i instanceof IronKey && !iron){ iron = true; changed = true; }
					if (i instanceof CrystalKey && !crystal){ crystal = true; changed = true; }
				}
			}
		}
		return seen;
	}

	static boolean isDoorLike(int t){
		return t == Terrain.DOOR || t == Terrain.OPEN_DOOR || t == Terrain.SECRET_DOOR || t == Terrain.LOCKED_DOOR
				|| t == Terrain.CRYSTAL_DOOR || t == Terrain.HERO_LKD_DR;
	}

	static boolean walkable(Level level, int c, boolean iron, boolean crystal, boolean itemsMode){
		int t = level.map[c];
		if (itemsMode){
			if (t == Terrain.CHASM || t == Terrain.BARRICADE || t == Terrain.BOOKSHELF || level.flamable[c]) return true;
			if ((Terrain.flags[t] & Terrain.PASSABLE) != 0) return true;
		}
		if (t == Terrain.SECRET_DOOR) return true;
		if (t == Terrain.LOCKED_DOOR || t == Terrain.HERO_LKD_DR) return iron;
		if (t == Terrain.CRYSTAL_DOOR) return crystal;
		if (t == Terrain.LOCKED_EXIT) return true; //boss floors: opens when the boss dies
		if (level.pit[c]) return false;
		//traps (and other "avoid" tiles) can be walked onto; only the AI shies away from them
		return level.passable[c] || level.avoid[c];
	}

	//checks one generated floor; heroPos -1 means "from the entrance"
	public static Result check(Level level, int depth, int heroPos){
		Result r = new Result();
		LevelTransition exit = level.getTransition(LevelTransition.Type.REGULAR_EXIT);
		LevelTransition entrance = level.getTransition(LevelTransition.Type.REGULAR_ENTRANCE);
		int from = heroPos >= 0 ? heroPos : (entrance != null ? entrance.cell() : -1);
		if (from < 0){
			r.problems.add("the floor has no entrance");
			return r;
		}
		boolean[] reach = reach(level, from, false);
		//boss floors open their way down through the fight itself (arenas, sealed rooms, the boss's key): not checked here
		boolean bossFloor = depth % 5 == 0;
		if (bossFloor){
			//nothing
		} else if (exit == null){
			if (depth < 26) r.problems.add("the floor has no way down");
		} else if (!reach[exit.cell()]){
			r.exitReachable = false;
			boolean[] withKeys = reach(level, from, true);
			r.problems.add(withKeys[exit.cell()]
					? "the way down is only behind a locked door whose key cannot be reached (key chain problem)"
					: "the way down cannot be reached even with every hidden door found and every door unlocked (sealed area)");
		}
		//items nobody can ever pick up (the floor 16 pit kind of thing)
		boolean[] all = reach(level, from, true, true);
		for (Heap heap : level.heaps.valueList()){
			if (heap.type == Heap.Type.FOR_SALE) continue;
			boolean near = all[heap.pos];
			if (!near){
				for (int d : PathFinder.NEIGHBOURS8){
					int n = heap.pos + d;
					if (n >= 0 && n < level.length() && all[n]) { near = true; break; }
				}
			}
			if (!near) r.problems.add("an item pile at cell " + heap.pos + " (" + heap.peek().getClass().getSimpleName() + ") can never be reached");
		}
		//a key that opens nothing reachable, or a locked door whose key never appears
		int lockedDoors = 0, ironKeys = 0;
		for (int i = 0; i < level.length(); i++) if (level.map[i] == Terrain.LOCKED_DOOR) lockedDoors++;
		for (Heap heap : level.heaps.valueList()) for (Item i : heap.items) if (i instanceof IronKey) ironKeys++;
		if (!bossFloor && lockedDoors > 0 && ironKeys == 0 && !hasKeyElsewhere(level)) r.problems.add(lockedDoors + " locked door(s) but no iron key on the floor");
		if (depth == 1){
			for (int i = 0; i < level.length(); i++) if (level.map[i] == Terrain.SECRET_DOOR){
				r.problems.add("floor 1 has a hidden door (rule: none on floor 1)");
				break;
			}
		}
		return r;
	}

	//keys can also be carried by enemies or sit in a chest; count those as present
	private static boolean hasKeyElsewhere(Level level){
		for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m : level.mobs){
			Object loot = SimUi.field(m, "loot");
			if (loot instanceof IronKey || loot == IronKey.class) return true;
			//mimics carry their items in a list
			Object items = SimUi.field(m, "items");
			if (items instanceof java.util.Collection) for (Object i : (java.util.Collection<?>) items) if (i instanceof IronKey) return true;
		}
		for (Heap heap : level.heaps.valueList()) for (Item i : heap.items) if (i instanceof GoldenKey) return true;
		return false;
	}
}

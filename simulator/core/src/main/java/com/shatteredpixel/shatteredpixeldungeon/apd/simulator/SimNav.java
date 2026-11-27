package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.watabou.utils.PathFinder;

//Breadth-first paths over what the hero knows about the floor (seen or mapped cells), avoiding known traps, chasms and fire.
final class SimNav {

	final int width, length;
	final boolean[] walk;
	final int[] dist;
	final int[] from;
	final int origin;
	//set before building, by the bot (Cautious and Normal keep off a red sentry's floor)
	static boolean avoidSentries;
	private boolean[] waterEdges;

	SimNav(Level level, int origin, boolean throughMobs){
		this(level, origin, throughMobs, true);
	}

	//avoidTraps false: walks over traps it can see (only traps already triggered are avoided, as they are spent)
	SimNav(Level level, int origin, boolean throughMobs, boolean avoidTraps){
		this(level, origin, throughMobs, avoidTraps, null);
	}

	SimNav(Level level, int origin, boolean throughMobs, boolean avoidTraps, java.util.Set<Integer> refused){
		this.width = level.width();
		this.length = level.length();
		this.origin = origin;
		walk = new boolean[length];
		//already standing in gas or fire: crossing more of it is allowed, to get out
		boolean escaping = origin >= 0 && origin < length && danger(level, origin);
		for (int i = 0; i < length; i++){
			boolean known = level.visited[i] || level.mapped[i] || level.heroFOV[i];
			if (!known) continue;
			int t = level.map[i];
			boolean pass = level.passable[i] || t == Terrain.DOOR || t == Terrain.OPEN_DOOR || (level.avoid[i] && !level.pit[i]);
			if (t == Terrain.LOCKED_DOOR || t == Terrain.CRYSTAL_DOOR || t == Terrain.LOCKED_EXIT || t == Terrain.HERO_LKD_DR) pass = false;
			if (level.pit[i] || t == Terrain.CHASM) pass = false;
			Trap trap = level.traps.get(i);
			if (avoidTraps && trap != null && trap.visible && trap.active) pass = false;
			if (!escaping && danger(level, i)) pass = false;
			walk[i] = pass;
		}
		//keep out of the water while piranhas are around (not for a Clueless bot, which doesn't know)
		if (avoidTraps){
			for (Char c : Actor.chars()){
				if (c instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Piranha && c.isAlive() && c.pos >= 0 && c.pos < length && level.heroFOV[c.pos]){
					for (int i = 0; i < length; i++) if (level.water[i] && level.map[i] == Terrain.WATER && i != origin) walk[i] = false;
					//and keep a step away from deep water, where they bite from (the next pass lets edges back in if that is the only way)
					boolean[] edge = new boolean[length];
					for (int i = 0; i < length; i++){
						if (!walk[i] || i == origin) continue;
						for (int d : PathFinder.NEIGHBOURS8){
							int n = i + d;
							if (n >= 0 && n < length && level.map[n] == Terrain.WATER){ edge[i] = true; break; }
						}
					}
					waterEdges = edge;
					break;
				}
			}
		}
		if (!throughMobs){
			for (Char c : Actor.chars()){
				if (c != Dungeon.hero && c.pos >= 0 && c.pos < length && level.heroFOV[c.pos] && !(c instanceof Mob && ((Mob) c).alignment == Char.Alignment.ALLY)){
					walk[c.pos] = false;
				}
			}
		}
		if (refused != null) for (int c : refused) if (c >= 0 && c < length) walk[c] = false;
		//red sentries fire on anyone standing on their room's special floor
		if (avoidSentries){
			for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m : level.mobs){
				if (!(m instanceof com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SentryRoom.Sentry)) continue;
				if (!(level.visited[m.pos] || level.mapped[m.pos] || level.heroFOV[m.pos])) continue;
				for (int i = 0; i < length; i++){
					if (i != origin && level.map[i] == Terrain.EMPTY_SP && level.distance(i, m.pos) <= 16) walk[i] = false;
				}
			}
		}
		if (origin >= 0 && origin < length) walk[origin] = true;
		dist = new int[length];
		from = new int[length];
		java.util.Arrays.fill(dist, -1);
		int[] queue = new int[length];
		int head = 0, tail = 0;
		if (origin >= 0 && origin < length){
			dist[origin] = 0;
			from[origin] = origin;
			queue[tail++] = origin;
		}
		int[] deferred = new int[length * 8];
		int dHead = 0, dTail = 0;
		while (head < tail || dHead < dTail){
			if (head >= tail){
				//only water-edge cells are left: allow them now
				int c = deferred[dHead++];
				if (dist[c] >= 0) continue;
				int best = -1;
				for (int d : PathFinder.NEIGHBOURS8){
					int n = c + d;
					if (n >= 0 && n < length && dist[n] >= 0 && Math.abs((n % width) - (c % width)) <= 1 && (best < 0 || dist[n] < dist[best])) best = n;
				}
				if (best < 0) continue;
				dist[c] = dist[best] + 1;
				from[c] = best;
				queue[tail++] = c;
				continue;
			}
			int c = queue[head++];
			for (int d : PathFinder.NEIGHBOURS8){
				int n = c + d;
				if (n < 0 || n >= length || dist[n] >= 0) continue;
				if (Math.abs((n % width) - (c % width)) > 1) continue;
				if (!walk[n]) continue;
				//no diagonal moves through doors (the game forbids them too)
				if (d != -1 && d != 1 && d != -width && d != width){
					if (isDoor(level.map[n]) || isDoor(level.map[c])) continue;
				}
				if (waterEdges != null && waterEdges[n]){
					deferred[dTail++] = n;
					continue;
				}
				dist[n] = dist[c] + 1;
				from[n] = c;
				queue[tail++] = n;
			}
		}
	}

	static boolean isDoor(int t){
		return t == Terrain.DOOR || t == Terrain.OPEN_DOOR;
	}

	static boolean danger(Level level, int cell){
		for (Class<? extends Blob> c : new Class[]{Fire.class, ToxicGas.class, CorrosiveGas.class}){
			Blob b = level.blobs.get(c);
			if (b != null && b.volume > 0 && b.cur != null && b.cur[cell] > 0) return true;
		}
		return false;
	}

	boolean reachable(int cell){
		return cell >= 0 && cell < length && dist[cell] >= 0;
	}

	//the first step towards cell, or -1
	int firstStep(int cell){
		if (!reachable(cell) || cell == origin) return -1;
		int c = cell;
		while (from[c] != origin) c = from[c];
		return c;
	}

	//the closest reachable cell that is next to (or is) target, so things that block movement can still be approached
	int approach(Level level, int target){
		if (reachable(target)) return target;
		int best = -1;
		for (int d : PathFinder.NEIGHBOURS8){
			int n = target + d;
			if (n < 0 || n >= length) continue;
			if (Math.abs((n % width) - (target % width)) > 1) continue;
			if (reachable(n) && (best < 0 || dist[n] < dist[best])) best = n;
		}
		return best;
	}
}

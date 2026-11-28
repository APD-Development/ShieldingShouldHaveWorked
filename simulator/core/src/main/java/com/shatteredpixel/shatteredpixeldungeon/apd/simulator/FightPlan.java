package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//The fight plan, worked out in fixed steps before and during every fight (hand-written rules and arithmetic only):
// Step 0  read the arena: how open the hero's cell is, corridors and doors nearby, water, traps, stairs to escape by
// Step 1  read each enemy: speed, damage, health, whether it attacks from range, turns until it reaches the hero
// Step 2  is there a door or corridor the hero can stand in so they come one at a time, reachable before they arrive?
// Step 3  compare the health the fight should cost here, at that spot, and against what the hero has left;
//         decide: fight here, reposition, use an item, or flee
// Step 4  pick the item with the best outcome
//An enemy faster than the hero is never run from across open floor: a retreat is planned only when the stairs are
//reached before the fast enemy catches up.
final class FightPlan {

	enum Kind { FIGHT, REPOSITION, ITEM, FLEE }

	Kind kind = Kind.FIGHT;
	int cell = -1;
	Item item;
	String action;
	//aim thrown items here
	int aimAt = -1;
	String why = "";
	//already standing in a corridor or doorway against a group: stay and let them come
	boolean hold;

	//step 0
	static final class Arena {
		int openHere;          //walkable cells around the hero (2 or fewer = corridor or doorway)
		int chokeCell = -1;    //best one-at-a-time spot within reach
		int chokeSteps;
		int stairs = -1;       //closest way off the floor
		int stairsSteps;
		boolean water;         //the hero stands in water
		int trapsNear;         //visible traps within 2 cells
	}

	//step 1
	static final class Enemy {
		Mob mob;
		FightJudge.Verdict verdict;
		float speed;
		boolean ranged;
		int steps;             //walking distance to the hero
		float turnsToMelee;
	}

	//monsters that hurt from a distance: a doorway does not stop them
	private static final String[] RANGED = {"Shaman", "CrystalWisp", "GnollExile", "GnollTrickster", "Eye", "DM100", "Warlock",
			"Necromancer", "Tengu", "Scorpio", "Elemental", "FungalSentry", "YogFist"};

	static boolean ranged(Mob m){
		for (Class<?> c = m.getClass(); c != null && c != Mob.class; c = c.getSuperclass()){
			for (String r : RANGED) if (c.getSimpleName().equals(r)) return true;
		}
		return false;
	}

	static int open(Level level, int c){
		int n = 0;
		for (int d : PathFinder.NEIGHBOURS8){
			int x = c + d;
			if (x >= 0 && x < level.length() && Math.abs(x % level.width() - c % level.width()) <= 1
					&& (level.passable[x] || level.avoid[x]) && !level.pit[x]) n++;
		}
		return n;
	}

	static Arena readArena(Level level, Hero h, SimNav nav, boolean levelLocked){
		Arena a = new Arena();
		a.openHere = open(level, h.pos);
		a.water = level.water[h.pos];
		for (int c = 0; c < level.length(); c++){
			if (level.traps.get(c) != null && level.traps.get(c).visible && level.traps.get(c).active && level.distance(c, h.pos) <= 2) a.trapsNear++;
			if (!nav.reachable(c) || nav.dist[c] > 8) continue;
			//a corridor or doorway: two walkable neighbours or fewer, so they can only come from in front and behind
			if (open(level, c) <= 2 && (a.chokeCell < 0 || nav.dist[c] < a.chokeSteps)){ a.chokeCell = c; a.chokeSteps = nav.dist[c]; }
		}
		if (!levelLocked){
			for (LevelTransition t : level.transitions){
				//only the way back up: running down the stairs from a fight lands a hurt hero on a harder floor
				if (t.type != LevelTransition.Type.REGULAR_ENTRANCE) continue;
				int c = t.cell();
				int steps = c == h.pos ? 0 : nav.reachable(c) ? nav.dist[c] : -1;
				if (steps >= 0 && steps <= 15 && (a.stairs < 0 || steps < a.stairsSteps)){ a.stairs = c; a.stairsSteps = steps; }
			}
		}
		return a;
	}

	static List<Enemy> readEnemies(Level level, Hero h, List<Mob> foes, SimNav throughMobs){
		List<Enemy> out = new ArrayList<>();
		for (Mob m : foes){
			Enemy e = new Enemy();
			e.mob = m;
			e.verdict = FightJudge.judge(m);
			float sp;
			try { sp = m.speed(); } catch (Throwable t){ sp = 1f; }
			e.speed = sp;
			e.ranged = ranged(m);
			e.steps = throughMobs.reachable(m.pos) ? throughMobs.dist[m.pos] : level.distance(m.pos, h.pos);
			e.turnsToMelee = e.speed <= 0 ? Float.MAX_VALUE : Math.max(0, e.steps - 1) / e.speed;
			out.add(e);
		}
		return out;
	}

	//health the fight should cost when at most `atOnce` of them can swing at the hero together
	static float cost(List<Enemy> enemies, int atOnce){
		List<Enemy> left = new ArrayList<>(enemies);
		//kill first whoever gives back the most health per swing spent
		left.sort((a, b) -> Float.compare(b.verdict.perTurn / Math.max(0.01f, b.verdict.turnsToKill), a.verdict.perTurn / Math.max(0.01f, a.verdict.turnsToKill)));
		float cost = 0;
		while (!left.isEmpty()){
			Enemy target = left.get(0);
			if (target.verdict.cannotWin()) return Float.MAX_VALUE;
			//the ones at range keep shooting whatever the hero stands in; of the rest only `atOnce` reach
			float pressure = 0;
			int melee = 0;
			for (Enemy e : left){
				if (e.ranged) pressure += e.verdict.perTurn;
				else if (melee < atOnce){ pressure += e.verdict.perTurn; melee++; }
			}
			cost += target.verdict.turnsToKill * pressure;
			left.remove(0);
		}
		return cost;
	}

	//can the hero get to `cell` (`steps` away) without being caught from behind on the way?
	//Enemies no faster than the hero only follow; a faster one must not be able to close the gap in that time.
	static boolean reachableInTime(List<Enemy> enemies, int steps, float heroSpeed){
		float travel = steps / Math.max(0.1f, heroSpeed);
		for (Enemy e : enemies){
			if (e.speed <= heroSpeed) continue;
			float catchUp = Math.max(0, e.steps - 1) / (e.speed - heroSpeed);
			if (catchUp < travel) return false;
		}
		return true;
	}

	private static boolean thrownAtGroup(Potion p){
		return p instanceof PotionOfLiquidFlame || p instanceof PotionOfToxicGas || p instanceof PotionOfParalyticGas
				|| p instanceof PotionOfFrost || p instanceof PotionOfCorrosiveGas;
	}

	static FightPlan make(Level level, Hero h, List<Mob> foes, SimNav nav, SimNav throughMobs, List<Item> pack, boolean levelLocked, boolean mayFlee){
		FightPlan p = new FightPlan();
		if (foes.isEmpty()) return p;
		float heroSpeed;
		try { heroSpeed = h.speed(); } catch (Throwable t){ heroSpeed = 1f; }

		Arena arena = readArena(level, h, nav, levelLocked);                 //step 0
		List<Enemy> enemies = readEnemies(level, h, foes, throughMobs);      //step 1

		int meleeFoes = 0;
		Enemy nearest = enemies.get(0);
		for (Enemy e : enemies){
			if (!e.ranged) meleeFoes++;
			if (e.steps < nearest.steps) nearest = e;
		}
		p.aimAt = nearest.mob.pos;

		//step 2: a one-at-a-time spot that can be reached before the fast ones arrive
		float costHere = cost(enemies, Math.max(1, Math.min(meleeFoes, arena.openHere)));
		float costChoke = Float.MAX_VALUE;
		boolean chokeOk = meleeFoes >= 2 && arena.chokeCell >= 0 && arena.chokeCell != h.pos && arena.openHere > 2
				&& reachableInTime(enemies, arena.chokeSteps, heroSpeed);
		if (chokeOk) costChoke = cost(enemies, 1);

		//step 3: compare
		float best = Math.min(costHere, costChoke);
		boolean saves = chokeOk && costHere - costChoke >= Math.max(3f, h.HP * 0.15f);
		String numbers = String.format(Locale.ROOT, "%d enemies, fighting here costs about %s, health %d", enemies.size(), show(costHere), h.HP);

		p.hold = meleeFoes >= 2 && arena.openHere <= 2;
		if (best < h.HP * 0.75f){
			if (saves){
				p.kind = Kind.REPOSITION;
				p.cell = arena.chokeCell;
				p.why = numbers + "; in the corridor " + arena.chokeSteps + " steps away about " + show(costChoke);
			} else {
				p.why = numbers;
			}
			return p;
		}

		//step 4: the fight costs most of what is left. Pick the item with the best outcome.
		//thrown gas, fire or frost before they arrive hits the whole group
		if (level.distance(h.pos, nearest.mob.pos) >= 2){
			for (Item i : pack){
				if (i instanceof Potion && ((Potion) i).isKnown() && thrownAtGroup((Potion) i)){
					p.kind = Kind.ITEM; p.item = i; p.action = Item.AC_THROW;
					p.why = numbers + "; throw " + SimBot.nameOf(i) + " first";
					return p;
				}
			}
		}
		if (best >= h.HP){
			//expected to lose: copies of the hero to share the hits, monsters put to sleep or scared off, or gone from here
			for (Item i : pack){
				if (!(i instanceof Scroll) || !((Scroll) i).isKnown()) continue;
				if (i instanceof ScrollOfMirrorImage || i instanceof ScrollOfLullaby || i instanceof ScrollOfTerror){
					p.kind = Kind.ITEM; p.item = i; p.action = Scroll.AC_READ;
					p.why = numbers + "; expected to lose, read " + SimBot.nameOf(i);
					return p;
				}
			}
			//flee: only by stairs, and only when nothing fast can catch the hero on the way
			if (mayFlee && arena.stairs >= 0 && reachableInTime(enemies, arena.stairsSteps, heroSpeed)){
				p.kind = Kind.FLEE;
				p.cell = arena.stairs;
				p.why = numbers + "; expected to lose, stairs " + arena.stairsSteps + " steps away can be reached first";
				return p;
			}
			for (Item i : pack){
				if (i instanceof ScrollOfTeleportation && ((Scroll) i).isKnown()){
					p.kind = Kind.ITEM; p.item = i; p.action = Scroll.AC_READ;
					p.why = numbers + "; expected to lose and nowhere to run, teleport away";
					return p;
				}
			}
		}
		if (saves){
			p.kind = Kind.REPOSITION;
			p.cell = arena.chokeCell;
			p.why = numbers + "; in the corridor " + arena.chokeSteps + " steps away about " + show(costChoke);
			return p;
		}
		p.why = numbers + "; nothing better than fighting here";
		return p;
	}

	private static String show(float cost){
		return cost >= 100000f ? "more than can be won" : String.valueOf(Math.round(cost));
	}

	//used by the bot's low-health retreat too
	static boolean canFleeTo(Level level, Hero h, List<Mob> foes, SimNav throughMobs, int steps){
		float heroSpeed;
		try { heroSpeed = h.speed(); } catch (Throwable t){ heroSpeed = 1f; }
		return reachableInTime(readEnemies(level, h, foes, throughMobs), steps, heroSpeed);
	}

	private FightPlan(){}
}

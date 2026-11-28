package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KingsCrown;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

//The bot's decisions. Every action goes through the same calls a player's tap or menu choice makes:
//hero.handle(cell) for moving/attacking/opening/talking, item.execute(hero, action) for item menus, hero.rest/search,
//hero.upgradeTalent for the talent screen. It never edits game state directly.
final class SimBot {

	final Random rng;
	private final Map<String, Integer> attempts = new HashMap<>();
	private final Set<Integer> doneFrontier = new HashSet<>();
	private final Set<Integer> searchedSpots = new HashSet<>();
	private final Map<Integer, Float> npcTalkedAt = new HashMap<>();
	private int levelKey = Integer.MIN_VALUE;
	private int restsThisFloor, searchesThisFloor;
	private long restTurnsThisFloor, restStartTurn = -1;
	private int idleThisFloor, desperation;
	//a piranha was seen on this floor: its water stays off limits even when it is out of sight
	private boolean piranhaFloor;
	//the most health a single fight on this floor was judged to cost (Smart and better rest up to it)
	private float floorFightCost;
	private int lastSeenHP = -1;
	private float noRestUntil = -1;
	private boolean[] piranhaWater;
	//goals are kept until reached or no longer valid, so the bot does not flip between two equally near targets
	private int searchGoal = -1, exploreGoal = -1;
	float floorStartTurn;
	int focusCell = -1;
	String lastProblem = "";

	final SimProfile.Aggression aggression;
	final SimProfile.Intelligence intelligence;
	//what the settings mean for play (all hand-written rules)
	final float healAt, backOffAt, tryUnknownChance, restTo;
	//rest only below this share of health, and at most this many turns per floor
	final float restBelow;
	final int restBudget;
	final boolean fullExplore, dive, optionalFights, avoidKnownTraps, wearUnidentified;

	SimBot(long seed, SimProfile.Aggression aggression, SimProfile.Intelligence intelligence){
		rng = new Random(seed);
		FightJudge.reset();
		FightJudge.enabled = !SimRuntime.forcedNoJudge;
		this.aggression = aggression;
		this.intelligence = intelligence;
		switch (aggression){
			case CAUTIOUS:   restTo = 0.95f; healAt = 0.50f; backOffAt = 0.40f; tryUnknownChance = 0.15f; fullExplore = true;  dive = false; optionalFights = false; wearUnidentified = false; break;
			case AGGRESSIVE: restTo = 0.50f; healAt = 0.25f; backOffAt = 0f;    tryUnknownChance = 0.85f; fullExplore = false; dive = false; optionalFights = true;  wearUnidentified = true;  break;
			case RECKLESS:   restTo = 0.30f; healAt = 0.12f; backOffAt = 0f;    tryUnknownChance = 1.00f; fullExplore = false; dive = true;  optionalFights = true;  wearUnidentified = true;  break;
			default:         restTo = 0.80f; healAt = 0.35f; backOffAt = 0.20f; tryUnknownChance = 0.50f; fullExplore = true;  dive = false; optionalFights = true;  wearUnidentified = false; break;
		}
		avoidKnownTraps = intelligence != SimProfile.Intelligence.CLUELESS;
		switch (aggression){
			case CAUTIOUS:   restBelow = 0.70f; restBudget = 250; break;
			case AGGRESSIVE: restBelow = 0.30f; restBudget = 80;  break;
			case RECKLESS:   restBelow = 0f;    restBudget = 0;   break;
			default:         restBelow = 0.50f; restBudget = 150; break;
		}
	}

	boolean smart(){ return intelligence.ordinal() >= SimProfile.Intelligence.SMART.ordinal(); }
	boolean clueless(){ return intelligence == SimProfile.Intelligence.CLUELESS; }

	//cells the game refused to let the hero step onto this floor (the move took no time and the hero stayed put)
	private final Set<Integer> refused = new HashSet<>();
	private int lastStepFrom = -1, lastStepTo = -1;
	private float lastStepTime = -1;

	private SimNav nav(boolean throughMobs){
		//the sentry's room is a side room with a prize, never the way on: only Reckless and Clueless bots go in
		SimNav.avoidSentries = !clueless() && aggression != SimProfile.Aggression.RECKLESS;
		SimNav.avoidWater = desperation < 1;
		SimNav.piranhaWater = piranhaWater;
		SimNav n = new SimNav(level(), hero().pos, throughMobs, avoidKnownTraps && desperation < 2, refused);
		n.careful = avoidKnownTraps;
		return n;
	}

	private Hero hero(){ return Dungeon.hero; }
	private Level level(){ return Dungeon.level; }

	//what the hero carried when first reaching floor 5 (the first boss), for the run summary
	String gearAtFirstBoss = "";

	//weapon, armor, strength and what is still unused in the pack, in one line
	static String gearSummary(){
		Hero h = Dungeon.hero;
		if (h == null) return "";
		StringBuilder b = new StringBuilder();
		Item w = h.belongings.weapon(), a = h.belongings.armor();
		b.append("weapon ").append(w == null ? "none" : nameOf(w) + (w instanceof MeleeWeapon ? " T" + ((MeleeWeapon) w).tier : "") + " +" + w.level());
		b.append(", armor ").append(a == null ? "none" : nameOf(a) + " T" + ((Armor) a).tier + " +" + a.level());
		b.append(", STR ").append(h.STR()).append(", level ").append(h.lvl);
		int weapons = 0, armors = 0, upgrades = 0, unknownPotions = 0, unknownScrolls = 0, healing = 0;
		for (Item i : h.belongings){
			if (i.isEquipped(h)) continue;
			if (i instanceof MeleeWeapon) weapons++;
			else if (i instanceof Armor) armors++;
			else if (i instanceof ScrollOfUpgrade && ((Scroll) i).isKnown()) upgrades += i.quantity();
			else if (i instanceof PotionOfHealing && ((Potion) i).isKnown()) healing += i.quantity();
			else if (i instanceof Potion && !((Potion) i).isKnown()) unknownPotions += i.quantity();
			else if (i instanceof Scroll && !((Scroll) i).isKnown()) unknownScrolls += i.quantity();
		}
		b.append(", in pack: ").append(weapons).append(" weapons, ").append(armors).append(" armors, ").append(upgrades).append(" upgrade scrolls, ")
				.append(healing).append(" healing, ").append(unknownPotions).append(" unknown potions, ").append(unknownScrolls).append(" unknown scrolls");
		return b.toString();
	}

	private void newFloorCheck(){
		int key = Dungeon.depth * 100 + Dungeon.branch;
		if (key != levelKey){
			if (Dungeon.depth == 5 && gearAtFirstBoss.isEmpty()) gearAtFirstBoss = gearSummary();
			levelKey = key;
			attempts.clear();
			doneFrontier.clear();
			searchedSpots.clear();
			npcTalkedAt.clear();
			refused.clear();
			bannedGoals.clear();
			restsThisFloor = 0;
			restTurnsThisFloor = 0;
			idleThisFloor = 0;
			desperation = 0;
			piranhaFloor = false;
			piranhaWater = null;
			doorwaySpot = -1;
			floorFightCost = 0;
			restStartTurn = -1;
			searchesThisFloor = 0;
			searchGoal = exploreGoal = -1;
			floorStartTurn = Actor.now();
		}
	}

	private boolean tried(String what, int limit){
		int n = attempts.getOrDefault(what, 0);
		if (n >= limit) return true;
		attempts.put(what, n + 1);
		return false;
	}

	static String cell(int c){
		if (Dungeon.level == null) return String.valueOf(c);
		return "(" + (c % Dungeon.level.width()) + "," + (c / Dungeon.level.width()) + ")";
	}

	static String nameOf(Object o){
		try {
			if (o instanceof Item) return ((Item) o).name();
			if (o instanceof Char) return ((Char) o).name();
		} catch (Throwable ignored){}
		return o == null ? "nothing" : o.getClass().getSimpleName();
	}

	//---- what the hero can see -------------------------------------------------------------------------------

	List<Mob> hostiles(){
		List<Mob> out = new ArrayList<>();
		for (Mob m : level().mobs.toArray(new Mob[0])){
			if (m == null || !m.isAlive() || m.alignment != Char.Alignment.ENEMY) continue;
			if (m instanceof NPC) continue;
			if (m.pos < 0 || m.pos >= level().length() || !level().heroFOV[m.pos]) continue;
			if (m.invisible > 0) continue;
			//passive enemies (statues) are an optional fight: only bold simulators start it
			//(walking past one does not wake it, so being next to it is no reason to start)
			if (m.state == m.PASSIVE
					&& !(optionalFights && aggression.ordinal() >= SimProfile.Aggression.AGGRESSIVE.ordinal())) continue;
			if (m instanceof Mimic && m.alignment != Char.Alignment.ENEMY) continue;
			//a red sentry cannot be killed and only fires at whoever stands on its room's floor: nothing to fight,
			//the bot just keeps off that floor (a Clueless bot walks in)
			if (m instanceof com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SentryRoom.Sentry && !clueless()) continue;
			//the Great Crab blocks every hit from whoever it is chasing and walks slowly: a fight the hero can't win head on,
			//so anyone but a Clueless bot keeps moving instead of trading blows with it
			if (m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GreatCrab && !clueless()) continue;
			//a piranha in water can't be reached; only fight it when it is right next to the hero
			if (m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Piranha && !clueless() && !Dungeon.level.adjacent(m.pos, hero().pos)) continue;
			out.add(m);
		}
		out.sort(Comparator.comparingInt((Mob m) -> level().distance(hero().pos, m.pos)).thenComparingInt(m -> m.pos));
		return out;
	}

	private <T extends Item> T find(Class<T> c){
		for (Item i : hero().belongings){
			if (c.isInstance(i)) return c.cast(i);
		}
		return null;
	}

	private List<Item> items(){
		List<Item> out = new ArrayList<>();
		for (Item i : hero().belongings) if (!(i instanceof Bag)) out.add(i);
		return out;
	}

	private boolean has(Item i, String action){
		try { return i.actions(hero()).contains(action); } catch (Throwable t){ return false; }
	}

	private Item lastUsed;

	private String use(Item i, String action){
		lastUsed = i;
		i.execute(hero(), action);
		return action.toLowerCase() + " " + nameOf(i);
	}

	private String tap(int cell, String what){
		hero().handle(cell);
		hero().next();
		return what + " " + cell(cell);
	}

	//goals the bot was caught going round in circles for on this floor
	private final Set<String> bannedGoals = new HashSet<>();
	private String lastGoal = "";

	private String stepToward(SimNav nav, int target, String why){
		String goal = why.replaceAll("[0-9]", "") + "@" + target;
		if (bannedGoals.contains(goal)) return null;
		int step = nav.firstStep(target);
		if (step < 0) return null;
		lastGoal = goal;
		//only runs of consecutive moves count towards walking in circles (fighting or resting in place is fine)
		lastWasMove = true;
		recent.addLast(hero().pos);
		while (recent.size() > 40) recent.removeFirst();
		lastStepFrom = hero().pos;
		lastStepTo = step;
		lastStepTime = Actor.now();
		return tap(step, "move (" + why + ") to");
	}

	//---- the decision ----------------------------------------------------------------------------------------

	//Called when it is the hero's turn and nothing is waiting for an answer. Returns what was done, or null.
	//recent positions, to notice walking back and forth between a couple of cells
	private final java.util.ArrayDeque<Integer> recent = new java.util.ArrayDeque<>();
	String oscillation = null;
	private boolean lastWasMove;

	String act(){
		newFloorCheck();
		if (!lastWasMove) recent.clear();
		lastWasMove = false;
		if (recent.size() >= 40 && new HashSet<>(recent).size() <= 3){
			oscillation = "walking back and forth between " + new HashSet<>(recent).size() + " cells at " + cell(hero().pos);
			//give up whatever it was heading for on this floor and pick something else
			if (exploreGoal >= 0) doneFrontier.add(exploreGoal);
			if (searchGoal >= 0) searchedSpots.add(searchGoal);
			for (int c : new HashSet<>(recent)) for (int d : PathFinder.NEIGHBOURS9) attempts.put("door" + (c + d), 99);
			exploreGoal = searchGoal = -1;
			if (!lastGoal.isEmpty()) bannedGoals.add(lastGoal);
			recent.clear();
		}
		if (lastStepTo >= 0 && hero().pos == lastStepFrom && Actor.now() == lastStepTime) refused.add(lastStepTo);
		lastStepTo = -1;
		lastProblem = "";
		Hero h = hero();

		String free = freeActions();
		if (free != null) return free;
		markBossDanger();

		List<Mob> foes = hostiles();
		Hunger hunger = h.buff(Hunger.class);
		if (!clueless()){
			for (Mob m : level().mobs){
				if (!(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Piranha) || !m.isAlive() || !level().heroFOV[m.pos]) continue;
				if (piranhaWater != null && piranhaWater[m.pos]) continue;
				//remember this piranha's whole pool: the water connected to where it is
				if (piranhaWater == null) piranhaWater = new boolean[level().length()];
				piranhaFloor = true;
				java.util.ArrayDeque<Integer> q = new java.util.ArrayDeque<>();
				q.add(m.pos);
				piranhaWater[m.pos] = true;
				while (!q.isEmpty()){
					int c = q.poll();
					for (int d : PathFinder.NEIGHBOURS8){
						int n = c + d;
						if (n < 0 || n >= level().length() || piranhaWater[n] || Math.abs(n % level().width() - c % level().width()) > 1) continue;
						if (level().map[n] == Terrain.WATER){ piranhaWater[n] = true; q.add(n); }
					}
				}
			}
		}

		//On fire (not Clueless). Water puts it out at once. With no water in reach the fire burns out by itself in a few
		//turns, unless the hero keeps walking through grass, which catches and sets them alight again: so stand on
		//bare floor and wait it out.
		if (!clueless() && h.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning.class) != null && !level().water[h.pos]){
			SimNav toSafe = nav(true);
			int water = -1, bare = -1;
			for (int c = 0; c < level().length(); c++){
				if (!toSafe.reachable(c) || toSafe.dist[c] > 8 || (c != h.pos && Actor.findChar(c) != null)) continue;
				if (level().water[c] && (piranhaWater == null || !piranhaWater[c]) && (water < 0 || toSafe.dist[c] < toSafe.dist[water])) water = c;
				if (!level().flamable[c] && !SimNav.danger(level(), c) && (bare < 0 || toSafe.dist[c] < toSafe.dist[bare])) bare = c;
			}
			if (water >= 0){
				String step = stepToward(toSafe, water, "into water to put the fire out");
				if (step != null) return step;
			}
			if (foes.isEmpty() && bare >= 0){
				if (bare == h.pos){
					h.rest(false);
					return "stand on bare floor until the fire burns out";
				}
				String step = stepToward(toSafe, bare, "to bare floor so the fire burns out");
				if (step != null) return step;
			}
		}

		//caustic ooze (Goo, fetid rat, slimes) burns every turn until washed off: walk to water, fight or no fight (not Clueless)
		if (!clueless() && foes.isEmpty() && h.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze.class) != null && !level().water[h.pos]){
			SimNav toWater = nav(false);
			int best = -1;
			for (int c = 0; c < level().length(); c++){
				if (level().water[c] && toWater.reachable(c) && toWater.dist[c] <= 10 && (piranhaWater == null || !piranhaWater[c])
						&& (best < 0 || toWater.dist[c] < toWater.dist[best])) best = c;
			}
			if (best >= 0){
				String step = stepToward(toWater, best, "into water to wash the ooze off");
				if (step != null) return step;
			}
		}

		//standing in fire or poison gas: get out first (a Clueless bot doesn't notice)
		if (!clueless() && SimNav.danger(level(), h.pos)){
			SimNav out = nav(true);
			int best = -1;
			for (int c = 0; c < level().length(); c++){
				if (out.reachable(c) && !SimNav.danger(level(), c) && (best < 0 || out.dist[c] < out.dist[best])) best = c;
			}
			if (best >= 0){
				String step = stepToward(out, best, "out of the gas or fire");
				if (step != null) return step;
			}
		}

		//doorway ambush under way and the enemy is out of sight behind the wall: finish the walk, then wait for it
		if (doorwaySpot >= 0 && foes.isEmpty()){
			if (Actor.now() > doorwayUntil) doorwaySpot = -1;
			else if (h.pos == doorwaySpot){
				h.rest(false);
				return "wait beside the door for what was following";
			} else {
				String step = stepToward(nav(false), doorwaySpot, "through a door to ambush what is following");
				if (step != null) return step;
				doorwaySpot = -1;
			}
		}
		if (!foes.isEmpty()){
			String fight = fight(foes);
			if (fight != null) return fight;
		}

		if (hunger != null && (hunger.isStarving() || (!clueless() && hunger.isHungry() && foes.isEmpty()))){
			Food food = bestFood();
			if (food != null) return use(food, Food.AC_EAT);
		}

		if (foes.isEmpty()){
			String manage = manageItems();
			if (manage != null) return manage;
		}

		SimNav nav = nav(false);

		if (h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.Amulet.class) != null){
			String up = ascend(nav);
			if (up != null) return up;
		}

		//resting heals slowly and costs food: only below a health line, never while hungry, and within a turn budget per floor
		if (restStartTurn >= 0){
			restTurnsThisFloor += com.shatteredpixel.shatteredpixeldungeon.Statistics.apdTurn() - restStartTurn;
			restStartTurn = -1;
		}
		//dew in the waterskin heals for free: drink before resting (not Clueless)
		if (foes.isEmpty() && h.HP < h.HT * 0.6f){
			String dew = drinkDew();
			if (dew != null) return dew;
		}
		//food in the pack buys more resting time (resting only costs hunger)
		int foodHeld = 0;
		for (Item i : items()) if (i instanceof Food) foodHeld += i.quantity();
		//Smart and better know resting only costs food, so they allow themselves more of it (never while hungry)
		int restBudget = this.restBudget == 0 ? 0 : this.restBudget + 200 * foodHeld + (smart() ? 250 : 0);
		//Smart and better also rest when the hardest fight met on this floor would cost more than they have left
		boolean tooHurtForThisFloor = smart() && restBudget > 0 && h.HP < Math.min(h.HT * 0.9f, floorFightCost * 1.5f);
		//resting while something is hurting the hero (an enemy the bot leaves alone, ooze, poison) only loses health:
		//after a drop in health no resting for a while, and never with the Great Crab close by
		if (lastSeenHP >= 0 && h.HP < lastSeenHP) noRestUntil = Actor.now() + 15;
		lastSeenHP = h.HP;
		//a sealed boss floor gives no health back for resting, and resting "until healed" there never ends
		boolean restUnsafe = Actor.now() < noRestUntil || level().locked
				|| h.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor.class) != null;
		for (Mob m : level().mobs){
			if (m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GreatCrab && m.isAlive() && level().heroFOV[m.pos]
					&& level().distance(m.pos, h.pos) <= 6) restUnsafe = true;
		}
		//a boss is next (this floor is a boss floor not yet sealed, or the floor above one): go in close to full health.
		//Not Clueless, not Reckless; still never while hungry.
		boolean bossNext = !clueless() && aggression != SimProfile.Aggression.RECKLESS
				&& (Dungeon.depth % 5 == 0 || (Dungeon.depth + 1) % 5 == 0) && Dungeon.depth < 26;
		if (bossNext && h.HP < h.HT * 0.9f){
			restBudget = Math.max(restBudget, 900);
			tooHurtForThisFloor = true;
		}
		if (foes.isEmpty() && !restUnsafe && (h.HP < h.HT * restBelow || tooHurtForThisFloor) && restTurnsThisFloor < restBudget
				//hungry is no reason to face a boss hurt: health still comes back until the hero is starving
				&& (hunger == null || !hunger.isHungry() || (bossNext && h.HP < h.HT * 0.9f && !hunger.isStarving()))){
			restStartTurn = com.shatteredpixel.shatteredpixeldungeon.Statistics.apdTurn();
			h.rest(true);
			return "rest until healed (" + restTurnsThisFloor + " of " + restBudget + " rest turns used on this floor)";
		}

		String loot = loot(nav);
		if (loot != null) return loot;

		if (optionalFights){
			String npc = talk(nav);
			if (npc != null) return npc;
		}

		String unlock = unlock(nav);
		if (unlock != null) return unlock;

		boolean exitKnown = exitReachable(nav);
		//hungry with nothing to eat: food is deeper, so stop wandering and take the stairs (a Clueless bot only when starving)
		boolean starvingNoFood = hunger != null && bestFood() == null && (hunger.isStarving() || (!clueless() && hunger.isHungry()));
		boolean skipExplore = exitKnown && (dive || starvingNoFood || (!fullExplore && Actor.now() - floorStartTurn > 300));
		if (!skipExplore){
			String explore = explore(nav);
			if (explore != null) return explore;
		}

		String down = descend(nav);
		if (down != null) return down;

		String search = search(nav);
		if (search != null) return search;

		//nothing left to do with the careful rules: relax them step by step before giving up
		if (++idleThisFloor % 5 == 0 && desperation < 3){
			desperation++;
			doneFrontier.clear();
			bannedGoals.clear();
			refused.clear();
			attempts.clear();
			exploreGoal = searchGoal = -1;
			if (desperation >= 3) searchesThisFloor = 0;
			h.rest(false);
			return "wait, then try again less carefully (step " + desperation + " of 3: "
					+ (desperation == 1 ? "forget dead ends, allow water and guarded floors" : desperation == 2 ? "walk over known traps" : "search everywhere again") + ")";
		}
		lastProblem = noWayForward(nav);
		if (TurnLog.echo && !tried("mapdump", 1)) System.out.println("APDSIM_MAP\n" + asciiMap(nav));
		h.rest(false);
		return "wait (nothing left to do: " + lastProblem + ")";
	}

	//what the bot knows of the floor, for the trace: @ hero, > exit, < entrance, + door, L locked, ~ water, # wall,
	//. reachable floor, x known floor it will not or cannot walk on, M monster in view, ? never seen
	private String asciiMap(SimNav nav){
		StringBuilder b = new StringBuilder();
		Level l = level();
		for (int c = 0; c < l.length(); c++){
			char ch;
			int t = l.map[c];
			boolean known = l.visited[c] || l.mapped[c] || l.heroFOV[c];
			if (c == hero().pos) ch = '@';
			else if (!known) ch = '?';
			else if (l.heroFOV[c] && Actor.findChar(c) != null) ch = 'M';
			else if (t == Terrain.EXIT || t == Terrain.UNLOCKED_EXIT || t == Terrain.LOCKED_EXIT) ch = '>';
			else if (t == Terrain.ENTRANCE || t == Terrain.ENTRANCE_SP) ch = '<';
			else if (t == Terrain.LOCKED_DOOR || t == Terrain.CRYSTAL_DOOR || t == Terrain.HERO_LKD_DR) ch = 'L';
			else if (t == Terrain.DOOR || t == Terrain.OPEN_DOOR) ch = nav.reachable(c) ? '+' : 'x';
			else if (l.solid[c]) ch = '#';
			else if (nav.reachable(c)) ch = l.water[c] ? '~' : '.';
			else ch = 'x';
			b.append(ch);
			if (c % l.width() == l.width() - 1) b.append('\n');
		}
		return b.toString();
	}

	//talents and subclass choices cost no turn
	private String freeActions(){
		Hero h = hero();
		for (int tier = 1; tier <= Math.min(4, h.talents.size()); tier++){
			if (h.talentPointsAvailable(tier) <= 0) continue;
			List<Talent> open = new ArrayList<>();
			for (Map.Entry<Talent, Integer> e : h.talents.get(tier - 1).entrySet()){
				if (e.getValue() < e.getKey().maxPoints()) open.add(e.getKey());
			}
			if (open.isEmpty()) continue;
			Talent t = open.get(rng.nextInt(open.size()));
			h.upgradeTalent(t);
			return "learn talent " + t.title() + " (tier " + tier + ")";
		}
		TengusMask mask = find(TengusMask.class);
		if (mask != null && !tried("mask", 3)) return use(mask, firstAction(mask));
		KingsCrown crown = find(KingsCrown.class);
		if (crown != null && !tried("crown", 3)) return use(crown, firstAction(crown));
		return null;
	}

	private String firstAction(Item i){
		for (String a : i.actions(hero())){
			if (!a.equals(Item.AC_DROP) && !a.equals(Item.AC_THROW)) return a;
		}
		return i.defaultAction();
	}

	//---- combat ----------------------------------------------------------------------------------------------

	private String fight(List<Mob> foes){
		Hero h = hero();
		Mob near = foes.get(0);
		focusCell = near.pos;
		holdGround = false;

		//piranhas can't leave the water: step away from it instead of trading bites at the edge (not Clueless)
		if (!clueless() && near instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Piranha){
			SimNav nav = nav(false);
			int best = -1;
			for (int c = 0; c < level().length(); c++){
				if (!nav.reachable(c) || nav.dist[c] > 3) continue;
				boolean wet = false;
				for (int d : PathFinder.NEIGHBOURS9){
					int n = c + d;
					if (n >= 0 && n < level().length() && (piranhaWater != null ? piranhaWater[n] : level().map[n] == Terrain.WATER)){ wet = true; break; }
				}
				if (!wet && (best < 0 || nav.dist[c] < nav.dist[best])) best = c;
			}
			if (best >= 0 && best != h.pos){
				String step = stepToward(nav, best, "away from the piranha's water");
				if (step != null) return step;
			}
		}

		//the fight judge (not Clueless): what the monsters next to the hero can do this turn at worst, what they do on
		//average, and what finishing this fight should cost
		float worstNow = 0, fightCost = 0;
		if (!clueless() && FightJudge.enabled){
			for (Mob m : foes){
				float worst = FightJudge.judge(m).worstHit;
				//a pumped-up Goo hits for three times as much and reaches two cells
				boolean pumped = false;
				if (m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo){
					Object pu = SimUi.field(m, "pumpedUp");
					pumped = pu instanceof Integer && (Integer) pu > 0;
				}
				if (pumped) worst *= 3f;
				if (level().distance(m.pos, h.pos) <= (pumped ? 2 : 1)) worstNow += worst;
			}
			fightCost = FightJudge.costOfAll(foes);
			if (smart()) floorFightCost = Math.max(floorFightCost, Math.min(fightCost, h.HT));
		}
		//when to heal. Clueless: below a fixed share of health. Average: that, or when the next hits could kill.
		//Smart and better: only when the next hits could kill, or when the rest of the fight costs more than is left,
		//so potions are not spent on fights that are already won.
		boolean couldDieNow = !clueless() && h.HP <= worstNow;
		boolean losing = !clueless() && h.HP < h.HT * 0.6f && fightCost >= h.HP;
		boolean healNow = !FightJudge.enabled ? h.HP < h.HT * healAt : smart() ? (couldDieNow || losing) : (h.HP < h.HT * healAt || couldDieNow);
		if (healNow){
			String dew = drinkDew();
			if (dew != null) return dew;
			PotionOfHealing heal = find(PotionOfHealing.class);
			if (heal != null && heal.isKnown()) return use(heal, Potion.AC_DRINK);
			CloakOfShadows cloak = find(CloakOfShadows.class);
			if (cloak != null && cloak.isEquipped(h) && has(cloak, CloakOfShadows.AC_STEALTH) && !tried("cloak@" + (int) Actor.now(), 1))
				return use(cloak, CloakOfShadows.AC_STEALTH);
			//gambling on an unknown potion at low health kills as often as it saves: only Clueless or Reckless bots do it
			if (clueless() || aggression == SimProfile.Aggression.RECKLESS){
				for (Item i : items()){
					if (i instanceof Potion && !((Potion) i).isKnown() && !tried("gamble" + i.getClass().getName(), 1)) return use(i, Potion.AC_DRINK);
				}
			}
		}

		if (!clueless()){
			String tengu = tenguRules(foes);
			if (tengu != null) return tengu;
		}
		//Goo (floor 5 boss), not Clueless: step out of reach while it pumps up its big hit, and pick where to stand
		if (!clueless()){
			String goo = gooRules(foes);
			if (goo != null) return goo;
			//ooze in a fight: water one or two steps away, on a free cell, is worth the walk
			if (h.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze.class) != null && !level().water[h.pos] && !tried("oozewash", 20)){
				SimNav toWater = nav(false);
				int best = -1;
				for (int c = 0; c < level().length(); c++){
					if (level().water[c] && toWater.reachable(c) && toWater.dist[c] <= 2 && Actor.findChar(c) == null
							&& (piranhaWater == null || !piranhaWater[c]) && (best < 0 || toWater.dist[c] < toWater.dist[best])) best = c;
				}
				if (best >= 0){
					String step = stepToward(toWater, best, "into water to wash the ooze off");
					if (step != null) return step;
				}
			}
		}

		//the fight plan (Smart and better): read the arena, read the enemies, look for a corridor to take them one at a
		//time, compare the cost with the health left, then fight here, reposition, use an item or flee (see FightPlan)
		if (smart() && FightJudge.enabled && !tried("plan@" + (int) Actor.now(), 2)){
			FightPlan plan = FightPlan.make(level(), h, foes, nav(false), nav(true), items(), level().locked,
					aggression != SimProfile.Aggression.RECKLESS);
			if (plan.hold && !tried("planhold", 40)) holdGround = true;
			if (plan.kind == FightPlan.Kind.REPOSITION && !tried("planmove", 30)){
				String step = stepToward(nav(false), plan.cell, "to a corridor so they come one at a time; " + plan.why);
				if (step != null) return step;
			}
			if (plan.kind == FightPlan.Kind.ITEM && !tried("planitem" + plan.item.getClass().getName(), 2)){
				focusCell = plan.aimAt;
				return use(plan.item, plan.action) + " (" + plan.why + ")";
			}
			if (plan.kind == FightPlan.Kind.FLEE && com.shatteredpixel.shatteredpixeldungeon.Statistics.apdTurn() - lastEscapeTurn >= 50){
				if (plan.cell == h.pos){
					lastEscapeTurn = com.shatteredpixel.shatteredpixeldungeon.Statistics.apdTurn();
					return tap(plan.cell, "flee by the stairs (" + plan.why + ") at");
				}
				String step = stepToward(nav(true), plan.cell, "flee to the stairs; " + plan.why);
				if (step != null) return step;
			}
		}

		//armor ability when crowded or against a boss
		ClassArmor armor = h.belongings.armor() instanceof ClassArmor ? (ClassArmor) h.belongings.armor() : null;
		if (armor != null && h.armorAbility != null && (foes.size() >= 2 || Char.hasProp(near, Char.Property.BOSS))
				&& armor.charge >= h.armorAbility.chargeUse(h) && !tried("ability@" + (int) Actor.now(), 1)){
			return use(armor, "ABILITY");
		}

		//Clueless: uses whatever it is holding at random now and then
		if (clueless() && rng.nextInt(10) == 0){
			List<Item> all = items();
			if (!all.isEmpty()){
				Item i = all.get(rng.nextInt(all.size()));
				List<String> acts = new ArrayList<>(i.actions(h));
				acts.remove(Item.AC_DROP);
				if (!acts.isEmpty() && !tried("random@" + (int) Actor.now(), 1)) return use(i, acts.get(rng.nextInt(acts.size())));
			}
		}

		//an attack the game refused (no time passed, target unharmed): leave that target alone for a while
		if (lastAttack != null && lastAttack.isAlive() && Actor.now() == lastAttackTime){
			refusedAttack.put(lastAttack.id(), Actor.now() + 20);
			refusedAttackNote = attackDiagnosis(lastAttack);
		}
		lastAttack = null;
		Mob reach = null;
		for (Mob m : foes){
			if (!h.canAttack(m)) continue;
			//a charmed hero can't attack whoever charmed them (game rule)
			if (h.isCharmedBy(m)) continue;
			Float until = refusedAttack.get(m.id());
			if (until != null && Actor.now() < until) continue;
			//Clueless hits the closest; Average the weakest in reach; Smart and better the one whose death saves the
			//most health for the fewest swings (the fight judge's numbers)
			if (reach == null) reach = m;
			else if (smart() && FightJudge.enabled){
				FightJudge.Verdict a = FightJudge.judge(m), b = FightJudge.judge(reach);
				if (!a.cannotWin() && (b.cannotWin() || a.perTurn / a.turnsToKill > b.perTurn / b.turnsToKill)) reach = m;
			} else if (!clueless() && m.HP < reach.HP) reach = m;
		}

		//Smart and better: a fight that should cost more health than is left is not started if stairs are close
		if (smart() && reach == null && fightCost >= h.HP && aggression != SimProfile.Aggression.RECKLESS){
			String away = retreat(foes);
			if (away != null) return away;
		}

		//backing off: low health, nothing to heal with, and the settings say retreat
		boolean someoneClose = false;
		for (Mob m : foes) if (level().distance(m.pos, h.pos) <= 3) someoneClose = true;
		if (backOffAt > 0 && h.HP < h.HT * backOffAt && !clueless() && someoneClose){
			String away = retreat(foes);
			if (away != null) return away;
		}

		//Smart and Expert: against a group, step into a spot where fewer of them can reach
		//(the fight plan does this properly when the judge is on; this older rule is kept for judge-off runs, and only a
		//few times per floor: with enemies in view that never come it used to trade places with looting forever)
		if (smart() && foes.size() >= 2 && reach == null && !FightJudge.enabled && !tried("choke", 8)){
			String choke = chokePoint(foes);
			if (choke != null) return choke;
		}

		//Duelist weapon ability, Cleric spells: now and then, the way a player uses them
		if (reach != null && !clueless() && rng.nextInt(smart() ? 2 : 5) == 0){
			if (h.belongings.weapon() instanceof MeleeWeapon && has(h.belongings.weapon(), MeleeWeapon.AC_ABILITY)
					&& !tried("wepab@" + (int) Actor.now(), 1)){
				focusCell = reach.pos;
				return use(h.belongings.weapon(), MeleeWeapon.AC_ABILITY);
			}
			HolyTome tome = find(HolyTome.class);
			if (tome != null && tome.isEquipped(h) && has(tome, HolyTome.AC_CAST) && !tried("tome@" + (int) Actor.now(), 1)){
				focusCell = reach.pos;
				return use(tome, HolyTome.AC_CAST);
			}
		}

		//magic cannot miss: against something next to the hero that dodges most swings, a staff or wand charge is worth
		//far more than a swing (not Clueless)
		if (reach != null && !clueless() && FightJudge.enabled){
			FightJudge.Verdict rv = FightJudge.judge(reach);
			if (!rv.surprise && rv.heroHits < 0.5f){
				MagesStaff staff = find(MagesStaff.class);
				if (staff != null && staffCharges(staff) > 0 && has(staff, MagesStaff.AC_ZAP)){
					focusCell = reach.pos;
					return use(staff, MagesStaff.AC_ZAP);
				}
				for (Item i : items()){
					if (i instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.DamageWand && ((Wand) i).curCharges > 0 && has(i, Wand.AC_ZAP)){
						focusCell = reach.pos;
						return use(i, Wand.AC_ZAP);
					}
				}
			}
		}
		//on the way to a doorway ambush: keep going, then wait beside the door until it steps into the doorway unaware
		if (doorwaySpot >= 0){
			Mob target = foes.size() == 1 && foes.get(0).id() == doorwayFor ? foes.get(0) : null;
			if (target == null || Actor.now() > doorwayUntil || foes.size() != 1) doorwaySpot = -1;
			else if (h.pos != doorwaySpot){
				String step = stepToward(nav(false), doorwaySpot, "through a door to ambush " + nameOf(target) + " from beside it");
				if (step != null) return step;
				doorwaySpot = -1;
			} else if (!level().adjacent(h.pos, target.pos)){
				h.rest(false);
				return "wait beside the door for " + nameOf(target);
			} else doorwaySpot = -1;
		}
		//one hard-to-hit enemy next to the hero: step out of its sight so it walks into a surprise hit
		if (reach != null && !clueless() && FightJudge.enabled && foes.size() == 1){
			String corner = cornerTrick(reach);
			if (corner != null) return corner;
		}
		if (reach != null){
			focusCell = reach.pos;
			lastAttack = reach;
			lastAttackTime = Actor.now();
			return tap(reach.pos, "attack " + nameOf(reach) + " (" + reach.HP + "/" + reach.HT + " health" + (clueless() || !FightJudge.enabled ? "" : "; " + FightJudge.describe(reach)) + ") at");
		}

		//ranged options for an enemy that can't be hit yet
		boolean dangerous = smart() && fightCost >= h.HP / 3f;
		boolean clear = !clueless() && new Ballistica(h.pos, near.pos, Ballistica.PROJECTILE).collisionPos == near.pos;
		if (clear){
			SpiritBow bow = find(SpiritBow.class);
			if (bow != null && has(bow, SpiritBow.AC_SHOOT)) return use(bow, SpiritBow.AC_SHOOT);
			MagesStaff staff = find(MagesStaff.class);
			//the last staff charge is kept for something that is hard to hit or dangerous (the judge's numbers)
			boolean keepLast = FightJudge.enabled && staff != null && staffCharges(staff) == 1 && !dangerous
					&& FightJudge.judge(near).heroHits >= 0.5f && !Char.hasProp(near, Char.Property.BOSS);
			if (staff != null && staffCharges(staff) > 0 && has(staff, MagesStaff.AC_ZAP) && !keepLast) return use(staff, MagesStaff.AC_ZAP);
			for (Item i : items()){
				//wands and thrown weapons: now and then in an easy fight, every time in one that costs a third of the health left
				if (i instanceof Wand && ((Wand) i).curCharges > 0 && has(i, Wand.AC_ZAP) && (dangerous || Char.hasProp(near, Char.Property.BOSS) || rng.nextInt(2) == 0)) return use(i, Wand.AC_ZAP);
			}
			for (Item i : items()){
				if (i instanceof Potion && ((Potion) i).isKnown() && harmful((Potion) i) && level().distance(h.pos, near.pos) >= 2) return use(i, Item.AC_THROW);
			}
			for (Item i : items()){
				if (i instanceof MissileWeapon && !i.isEquipped(h) && (dangerous || Char.hasProp(near, Char.Property.BOSS) || rng.nextInt(2) == 0)) return use(i, Item.AC_THROW);
			}
		}

		//just stepped around a corner: wait there for it to come
		if (Actor.now() < cornerWaitUntil && !level().adjacent(h.pos, near.pos)) holdGround = true;
		if (holdGround){
			h.rest(false);
			return "hold this spot and let " + nameOf(near) + " come";
		}
		SimNav nav = nav(false);
		int goal = nav.approach(level(), near.pos);
		if (goal >= 0 && goal != h.pos){
			String step = stepToward(nav, goal, "to fight " + nameOf(near));
			if (step != null) return step;
		}
		//can't reach it: let it come, unless that is hopeless
		if (!tried("waitfor" + near.id(), 6)){
			h.rest(false);
			return "wait for " + nameOf(near) + " at " + cell(near.pos);
		}
		return null;
	}

	private boolean holdGround;
	private float cornerWaitUntil = -1;

	//The corner trick, the way players kill snakes. An enemy that comes round a corner or through a door has not seen
	//the hero yet, and the game lets a swing at an unaware enemy always land. So against one enemy the hero can
	//barely hit, step to a cell next door that it cannot see, let it follow, hit it, and repeat.
	private String cornerTrick(Mob m){
		Hero h = hero();
		FightJudge.Verdict v = FightJudge.judge(m);
		if (v.surprise || v.heroHits >= 0.4f || tried("corner" + m.id(), 14)) return null;
		float speed;
		try { speed = m.speed(); } catch (Throwable t){ speed = 1f; }
		//something faster gets a free hit on the way; something at range does not need to follow
		if (speed > h.speed() || FightPlan.ranged(m)) return null;
		boolean[] sees = new boolean[level().length()];
		try { level().updateFieldOfView(m, sees); } catch (Throwable t){ return null; }
		SimNav nav = nav(false);
		int best = -1;
		for (int d : PathFinder.NEIGHBOURS8){
			int c = h.pos + d;
			if (c < 0 || c >= level().length() || !nav.reachable(c) || nav.dist[c] != 1) continue;
			if (sees[c] || Actor.findChar(c) != null || level().adjacent(c, m.pos)) continue;
			if (best < 0 || level().distance(c, m.pos) < level().distance(best, m.pos)) best = c;
		}
		if (best >= 0){
			cornerWaitUntil = Actor.now() + 3;
			return tap(best, "step out of " + nameOf(m) + "'s sight so it walks into a surprise hit, to");
		}
		//no blind cell right here: use a doorway. Go through the nearest door and stand beside it on the far side,
		//off the line of the passage; whatever follows has to step into the doorway without having seen the hero.
		if (tried("doorway" + m.id(), 2)) return null;
		int w = level().width();
		int spot = -1;
		for (int door = 0; door < level().length(); door++){
			int t = level().map[door];
			if ((t != Terrain.DOOR && t != Terrain.OPEN_DOOR) || !nav.reachable(door) || nav.dist[door] > 6 || door == m.pos) continue;
			for (int d : new int[]{-w - 1, -w + 1, w - 1, w + 1}){
				int c = door + d;
				if (c < 0 || c >= level().length() || Math.abs(c % w - door % w) != 1) continue;
				//beside the door on the far side: two steps past it by the path (through, then sideways)
				if (!nav.reachable(c) || nav.dist[c] != nav.dist[door] + 2 || Actor.findChar(c) != null) continue;
				if (spot < 0 || nav.dist[c] < nav.dist[spot]) spot = c;
			}
		}
		if (spot < 0) return null;
		doorwaySpot = spot;
		doorwayFor = m.id();
		doorwayUntil = Actor.now() + nav.dist[spot] + 6;
		return stepToward(nav, spot, "through a door to ambush " + nameOf(m) + " from beside it");
	}

	private int doorwaySpot = -1, doorwayFor = -1;
	private float doorwayUntil = -1;

	//Tengu's bomb (everything within 2 cells when it goes off), his flames and his shock field: cells to get out of
	//and stay out of. Read from what is on screen: the bomb's place, the burning and crackling cells.
	private void markBossDanger(){
		SimNav.extraDanger = null;
		SimNav.extraAvoid = null;
		if (clueless() || level() == null) return;
		//Tengu's first phase: each time he jumps the game lays dart traps in his cell and shows them for a moment
		//before they fade from view. A player remembers where they were; so does the bot (not Clueless).
		if (level() instanceof com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel
				&& ((com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel) level()).state()
				== com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel.State.FIGHT_START){
			boolean[] avoid = new boolean[level().length()];
			for (com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap t : level().traps.valueList()){
				if (t.active && t.pos >= 0 && t.pos < avoid.length) avoid[t.pos] = true;
			}
			SimNav.extraAvoid = avoid;
		}
		boolean[] mark = null;
		for (Mob m : level().mobs){
			if (!(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu)) continue;
			for (com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff b : m.buffs()){
				if (b instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu.BombAbility){
					int at = ((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu.BombAbility) b).bombPos;
					if (at < 0 || at >= level().length()) continue;
					//the bomb counts 3, 2, 1 on screen: its blast area is safe to cross until the count is nearly out
					Object timer = SimUi.field(b, "timer");
					if (timer instanceof Integer && (Integer) timer > 1) continue;
					if (mark == null) mark = new boolean[level().length()];
					for (int c = 0; c < level().length(); c++) if (level().distance(c, at) <= 2) mark[c] = true;
				}
				//the shock field flips every turn between the straight and the diagonal cells around the shocker,
				//so every cell next to it is unsafe to stop on
				if (b instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu.ShockerAbility){
					int at = ((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu.ShockerAbility) b).shockerPos;
					if (at < 0 || at >= level().length()) continue;
					if (mark == null) mark = new boolean[level().length()];
					for (int c = 0; c < level().length(); c++) if (level().distance(c, at) <= 1) mark[c] = true;
				}
			}
		}
		for (Class<? extends com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob> k : java.util.Arrays.asList(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu.FireAbility.FireBlob.class,
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu.ShockerAbility.ShockerBlob.class)){
			com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob b = level().blobs.get(k);
			if (b == null || b.volume <= 0 || b.cur == null) continue;
			if (mark == null) mark = new boolean[level().length()];
			for (int c = 0; c < level().length(); c++) if (b.cur[c] > 0) mark[c] = true;
		}
		SimNav.extraDanger = mark;
	}

	//Tengu aims twice as well at range as up close (the game gives him accuracy 20 at range, 10 next to the hero),
	//so the hero closes in instead of trading shots from across the room
	private String tenguRules(List<Mob> foes){
		Mob tengu = null;
		for (Mob m : foes) if (m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu) tengu = m;
		if (tengu == null || level().adjacent(tengu.pos, hero().pos) || tried("tenguchase", 600)) return null;
		SimNav nav = nav(false);
		int goal = nav.approach(level(), tengu.pos);
		if (goal < 0 || goal == hero().pos) return null;
		//more than two steps away with a shot that cannot miss or a bow in hand: shooting now beats walking under fire
		if (nav.dist[goal] > 2 && new Ballistica(hero().pos, tengu.pos, Ballistica.PROJECTILE).collisionPos == tengu.pos){
			MagesStaff staff = find(MagesStaff.class);
			if (staff != null && staffCharges(staff) > 0) return null;
			SpiritBow bow = find(SpiritBow.class);
			if (bow != null && has(bow, SpiritBow.AC_SHOOT)) return null;
			for (Item i : items()) if (i instanceof Wand && ((Wand) i).curCharges > 0) return null;
		}
		return stepToward(nav, goal, "to get next to Tengu (his aim is half as good up close)");
	}

	private String gooRules(List<Mob> foes){
		Hero h = hero();
		com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo goo = null;
		for (Mob m : foes) if (m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo) goo = (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo) m;
		if (goo == null) return null;
		Object p = SimUi.field(goo, "pumpedUp");
		int pumped = p instanceof Integer ? (Integer) p : 0;
		SimNav nav = nav(false);
		//Pumping up takes Goo two turns, then it hits for triple at up to 2 cells. If the hero is more than 2 cells away
		//when it would swing, the pump is wasted. So: right after it starts (two moves left) or one turn in (one move
		//left), step away only if that many moves really end more than 2 cells from it. Otherwise running just gives up
		//swings and still takes the hit, so the hero stays and keeps hitting (healing first if the big hit could kill).
		if (pumped > 0 && level().distance(h.pos, goo.pos) <= 2){
			int movesLeft = pumped >= 2 ? 1 : 2;
			int best = -1;
			for (int c = 0; c < level().length(); c++){
				if (!nav.reachable(c) || nav.dist[c] < 1 || nav.dist[c] > movesLeft || level().distance(c, goo.pos) <= 2) continue;
				//water first (ooze), then the shorter walk
				int rank = (level().water[c] ? 0 : 10) + nav.dist[c];
				if (best < 0 || rank < (level().water[best] ? 0 : 10) + nav.dist[best]) best = c;
			}
			if (best >= 0){
				String step = stepToward(nav, best, "out of reach while Goo pumps up");
				if (step != null) return step;
			}
			//no way out in time: do not wander off to a better spot now, fight
			return null;
		}
		//Where to stand. Ooze is what kills here: it burns every turn for 20 turns, straight through armor, and one hit
		//in three puts it back. Water under the hero ends it at once, so the hero fights from water and stays there.
		//Goo healing in water (1 a turn) matters less, so dry floor around the hero's puddle is only a bonus.
		boolean oozed = h.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze.class) != null;
		if (oozed && !level().water[h.pos] && !tried("goowash", 80)){
			int best = -1;
			for (int c = 0; c < level().length(); c++){
				if (!level().water[c] || !nav.reachable(c) || nav.dist[c] > 6 || Actor.findChar(c) != null) continue;
				if (best < 0 || nav.dist[c] < nav.dist[best]) best = c;
			}
			if (best >= 0){
				String step = stepToward(nav, best, "into water to wash the ooze off");
				if (step != null) return step;
			}
		}
		int here = gooSpot(h.pos, goo.pos);
		if (!level().water[h.pos] && !tried("goospot", 60)){
			int best = -1, bestScore = here;
			for (int c = 0; c < level().length(); c++){
				if (!nav.reachable(c) || nav.dist[c] > 8 || c == goo.pos || Actor.findChar(c) != null) continue;
				int score = gooSpot(c, goo.pos) + nav.dist[c];
				if (score < bestScore - 3){ bestScore = score; best = c; }
			}
			if (best >= 0){
				String step = stepToward(nav, best, "to fight Goo standing in water");
				if (step != null) return step;
			}
		}
		//standing in water: let Goo come instead of walking out to meet it
		holdGround = level().water[h.pos] && !level().adjacent(h.pos, goo.pos) && !tried("goohold", 60);
		return null;
	}

	//lower is better: 30 points if the cell is dry (ooze stays on the hero), 3 for each water cell next to it (Goo
	//would stand there and heal)
	private int gooSpot(int c, int gooPos){
		int score = level().water[c] ? 0 : 30;
		for (int d : PathFinder.NEIGHBOURS8){
			int n = c + d;
			if (n < 0 || n >= level().length() || !level().passable[n]) continue;
			if (level().water[n]) score += 3;
		}
		return score;
	}

	private int openNeighbours(int c){
		int n = 0;
		for (int d : PathFinder.NEIGHBOURS8){
			int x = c + d;
			if (x >= 0 && x < level().length() && level().passable[x]) n++;
		}
		return n;
	}

	//running away from something next to you only gives it free hits; the bot escapes only by stairs that are close
	private long lastEscapeTurn = -1000;

	private String retreat(List<Mob> foes){
		//one stair escape at a time: taking stairs costs no turns, so escaping again at once would loop between two floors
		if (com.shatteredpixel.shatteredpixeldungeon.Statistics.apdTurn() - lastEscapeTurn < 50) return null;
		//a sealed boss floor has no stairs to escape by
		if (level().locked) return null;
		SimNav nav = nav(false);
		int best = -1;
		for (LevelTransition t : level().transitions){
			//only the way back up: running down the stairs from a fight lands a hurt hero on a harder floor
			if (t.type != LevelTransition.Type.REGULAR_ENTRANCE || Dungeon.depth <= 1) continue;
			int c = t.cell();
			if (c == hero().pos && !level().locked){
				lastEscapeTurn = com.shatteredpixel.shatteredpixeldungeon.Statistics.apdTurn();
				return tap(c, "escape (low health) by the stairs at");
			}
			if (nav.reachable(c) && nav.dist[c] <= 6 && (best < 0 || nav.dist[c] < nav.dist[best])) best = c;
		}
		//never a retreat that needs outrunning something faster than the hero
		if (best >= 0 && FightJudge.enabled && !FightPlan.canFleeTo(level(), hero(), foes, nav(true), nav.dist[best])) return null;
		if (best >= 0) return stepToward(nav, best, "escape to the stairs");
		//no stairs close by: running across open floor only gives the enemy free hits, so stand and fight
		return null;
	}

	private String chokePoint(List<Mob> foes){
		int here = openNeighbours(hero().pos);
		if (here <= 2) return null;
		SimNav nav = nav(false);
		int best = -1, bestOpen = here;
		for (int d : PathFinder.NEIGHBOURS8){
			int c = hero().pos + d;
			if (!nav.reachable(c) || nav.dist[c] != 1) continue;
			int open = openNeighbours(c);
			if (open < bestOpen){ bestOpen = open; best = c; }
		}
		if (best < 0 || bestOpen > 2) return null;
		return tap(best, "step into a corridor to fight one at a time at");
	}

	private static int staffCharges(MagesStaff staff){
		Object w = SimUi.field(staff, "wand");
		return w instanceof Wand ? ((Wand) w).curCharges : 0;
	}

	private Mob lastAttack;
	private float lastAttackTime;
	private final Map<Integer, Float> refusedAttack = new HashMap<>();
	String refusedAttackNote;

	//everything that decides whether an attack can happen, for reports
	String attackDiagnosis(Mob m){
		Hero h = hero();
		StringBuilder b = new StringBuilder();
		b.append("target ").append(m.getClass().getSimpleName()).append(" at ").append(cell(m.pos)).append(" alive=").append(m.isAlive())
				.append(" alignment=").append(m.alignment).append(" state=").append(m.state == null ? "null" : m.state.getClass().getSimpleName())
				.append(" invisible=").append(m.invisible).append(" HP ").append(m.HP).append('/').append(m.HT).append('\n');
		b.append("hero at ").append(cell(h.pos)).append(" canAttack=").append(h.canAttack(m)).append(" charmedBy=").append(h.isCharmedBy(m))
				.append(" heroSeesIt=").append(h.fieldOfView != null && h.fieldOfView[m.pos]).append(" adjacent=").append(level().adjacent(h.pos, m.pos))
				.append(" rooted=").append(h.rooted).append(" paralysed=").append(h.paralysed).append('\n');
		b.append("hero effects: ");
		for (com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff x : h.buffs()) b.append(x.getClass().getSimpleName()).append(' ');
		b.append("\ntarget effects: ");
		for (com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff x : m.buffs()) b.append(x.getClass().getSimpleName()).append(' ');
		b.append("\nterrain: hero ").append(level().map[h.pos]).append(", target ").append(level().map[m.pos]);
		b.append("\nweapon: ").append(h.belongings.attackingWeapon() == null ? "none" : h.belongings.attackingWeapon().getClass().getSimpleName());
		return b.toString();
	}

	private String drinkDew(){
		if (clueless()) return null;
		com.shatteredpixel.shatteredpixeldungeon.items.Waterskin skin = find(com.shatteredpixel.shatteredpixeldungeon.items.Waterskin.class);
		if (skin == null) return null;
		Object v = SimUi.field(skin, "volume");
		int dew = v instanceof Integer ? (Integer) v : 0;
		//each drop heals about 5% of max health; worth a turn from 3 drops up
		if (dew < 3 || !has(skin, "DRINK") || tried("dew@" + (int) Actor.now(), 1)) return null;
		return use(skin, "DRINK");
	}

	private static boolean harmful(Potion p){
		return p instanceof PotionOfLiquidFlame || p instanceof PotionOfToxicGas || p instanceof PotionOfParalyticGas
				|| p instanceof PotionOfFrost || p instanceof PotionOfCorrosiveGas;
	}

	//---- items -----------------------------------------------------------------------------------------------

	private Food bestFood(){
		Food best = null;
		for (Item i : items()){
			if (!(i instanceof Food)) continue;
			//raw blandfruit can't be eaten (the game says so); it needs cooking first
			if (i instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.Blandfruit
					&& ((com.shatteredpixel.shatteredpixeldungeon.items.food.Blandfruit) i).potionAttrib == null) continue;
			if (best == null || ((Food) i).energy > best.energy) best = (Food) i;
		}
		return best;
	}

	//Average judges by tier like a new player; Smart and Expert compare the real numbers
	private int weaponValue(MeleeWeapon w){
		if (!smart()) return w.tier * 10;
		try { return w.min() + w.max(); } catch (Throwable t){ return w.tier * 10; }
	}

	private int armorValue(Armor a){
		if (!smart()) return a.tier * 10;
		try { return a.DRMax(); } catch (Throwable t){ return a.tier * 10; }
	}

	private String manageItems(){
		Hero h = hero();
		//equipment
		if (!(h.heroClass == HeroClass.MAGE && h.belongings.weapon() instanceof MagesStaff)){
			MeleeWeapon better = null;
			MeleeWeapon cur = h.belongings.weapon() instanceof MeleeWeapon ? (MeleeWeapon) h.belongings.weapon() : null;
			for (Item i : items()){
				if (!(i instanceof MeleeWeapon) || i.isEquipped(h) || (i.cursedKnown && i.cursed)) continue;
				//found weapons and armor are tried on unidentified, the way players do (a Cautious bot waits until it knows)
				if (!i.isIdentified() && aggression == SimProfile.Aggression.CAUTIOUS && !clueless()) continue;
				MeleeWeapon m = (MeleeWeapon) i;
				if (!clueless() && m.STRReq() > h.STR()) continue;
				if (weaponValue(m) > (cur == null ? 0 : weaponValue(cur)) && (better == null || weaponValue(m) > weaponValue(better))) better = m;
			}
			if (better != null && !tried("equip" + better.getClass().getName(), 2)) return use(better, EquipableItem.AC_EQUIP);
		}
		Armor cur = h.belongings.armor();
		for (Item i : items()){
			if (!(i instanceof Armor) || i.isEquipped(h) || (i.cursedKnown && i.cursed)) continue;
			//found weapons and armor are tried on unidentified, the way players do (a Cautious bot waits until it knows)
				if (!i.isIdentified() && aggression == SimProfile.Aggression.CAUTIOUS && !clueless()) continue;
			if (!clueless() && ((Armor) i).STRReq() > h.STR()) continue;
			if (armorValue((Armor) i) > (cur == null ? 0 : armorValue(cur)) && !tried("equip" + i.getClass().getName(), 2)) return use(i, EquipableItem.AC_EQUIP);
		}
		for (Item i : items()){
			if ((i instanceof Ring || i instanceof Artifact) && !i.isEquipped(h) && !(i.cursedKnown && i.cursed)
					&& (i.isIdentified() || wearUnidentified || clueless())
					&& (h.belongings.ring() == null || h.belongings.misc() == null || (i instanceof Artifact && h.belongings.artifact() == null))
					&& !tried("equip" + i.getClass().getName(), 2)) return use(i, EquipableItem.AC_EQUIP);
		}
		//known good consumables
		for (Item i : items()){
			if (i instanceof PotionOfStrength && ((Potion) i).isKnown()) return use(i, Potion.AC_DRINK);
			if (i instanceof PotionOfExperience && ((Potion) i).isKnown()) return use(i, Potion.AC_DRINK);
			if (i instanceof ScrollOfUpgrade && ((Scroll) i).isKnown()){
				//not Clueless: scrolls put into starting cloth are lost when real armor turns up, so they keep them
				//for the first three floors while still in tier 1 armor (a Mage's go into the staff either way)
				boolean hold = !clueless() && Dungeon.depth < 4 && !(h.belongings.weapon() instanceof MagesStaff)
						&& (h.belongings.armor() == null || h.belongings.armor().tier <= 1);
				if (!hold) return use(i, Scroll.AC_READ);
			}
		}
		//identify by use, like a player: read unknown scrolls, drink unknown potions at full health
		for (Item i : items()){
			if (i instanceof Scroll && !((Scroll) i).isKnown() && has(i, Scroll.AC_READ) && !tried("read" + i.getClass().getName(), 2)
					&& rng.nextFloat() < tryUnknownChance) return use(i, Scroll.AC_READ);
		}
		if (h.HP >= h.HT * (aggression == SimProfile.Aggression.RECKLESS ? 0.3f : 0.9f)){
			//Smart and Expert test unknown potions standing in water (fire goes out at once), when safe water is close
			boolean unknownPotion = false;
			for (Item i : items()) if (i instanceof Potion && !((Potion) i).isKnown() && attempts.getOrDefault("drink" + i.getClass().getName(), 0) < 2) unknownPotion = true;
			if (unknownPotion && smart() && !level().water[h.pos] && !tried("towater", 40)){
				SimNav wn = nav(false);
				int best = -1;
				for (int c = 0; c < level().length(); c++){
					if (level().water[c] && wn.reachable(c) && wn.dist[c] <= 10 && (best < 0 || wn.dist[c] < wn.dist[best])) best = c;
				}
				if (best >= 0){
					String step = stepToward(wn, best, "to water before testing a potion");
					if (step != null) return step;
				}
			}
			for (Item i : items()){
				if (i instanceof Potion && !((Potion) i).isKnown() && has(i, Potion.AC_DRINK) && !tried("drink" + i.getClass().getName(), 2)
						&& rng.nextFloat() < tryUnknownChance) return use(i, Potion.AC_DRINK);
			}
		}
		//known scrolls that do something useful out of combat (identify, remove curse, mapping, ...): read surplus ones
		for (Item i : items()){
			if (i instanceof Scroll && ((Scroll) i).isKnown() && !(i instanceof ScrollOfUpgrade) && i.quantity() > 1 && has(i, Scroll.AC_READ)
					&& !tried("surplus" + i.getClass().getName() + i.quantity(), 1)) return use(i, Scroll.AC_READ);
		}
		return null;
	}

	//---- the floor -------------------------------------------------------------------------------------------

	private boolean wantHeap(Heap heap){
		if (heap == null || heap.isEmpty() || heap.hidden) return false;
		//the early guidebook pages are placed with deliberately unseeded randomness (so meta progression never changes level
		//generation); going for them would make the same seed play differently each time, so the bot leaves them
		Item top = heap.peek();
		if (top instanceof com.shatteredpixel.shatteredpixeldungeon.items.journal.Guidebook
				|| top instanceof com.shatteredpixel.shatteredpixeldungeon.items.journal.GuidePage) return false;
		switch (heap.type){
			case TOMB:
				//opening a tomb wakes wraiths: an optional fight, for bold bots in good health only (Clueless opens everything)
				return clueless() || (optionalFights && aggression.ordinal() >= SimProfile.Aggression.AGGRESSIVE.ordinal()
						&& hero().HP >= hero().HT * 0.8f);
			case SKELETON:
			case REMAINS:
				//may hold a cursed item that summons a wraith: not for Cautious bots, and not when hurt
				//a wraith dodges about nine melee swings in ten, so a weak hero loses that fight: Normal bots wait until
				//they have some health to spend (level 5 and up, nearly full health); bold bots just need to be healthy
				if (clueless()) return true;
				if (aggression == SimProfile.Aggression.CAUTIOUS) return false;
				if (aggression == SimProfile.Aggression.NORMAL) return hero().lvl >= 5 && hero().HP >= hero().HT * 0.9f;
				return hero().HP >= hero().HT * 0.6f;
			case FOR_SALE:
				return wantToBuy(heap);
			case LOCKED_CHEST:
				return Notes.keyCount(new GoldenKey(Dungeon.depth)) > 0;
			case CRYSTAL_CHEST:
				return Notes.keyCount(new CrystalKey(Dungeon.depth)) > 0;
			default:
				return true;
		}
	}

	private boolean wantToBuy(Heap heap){
		Item i = heap.peek();
		if (i == null) return false;
		int price = Shopkeeper.sellPrice(i);
		if (price > Dungeon.gold) return false;
		if (i instanceof Bag || i instanceof Food || i instanceof Ankh) return true;
		if (i instanceof PotionOfHealing) return true;
		if (i instanceof Armor && hero().belongings.armor() != null && ((Armor) i).tier > hero().belongings.armor().tier) return true;
		return Dungeon.gold > price * 3;
	}

	private String loot(SimNav nav){
		Heap best = null;
		int bestDist = Integer.MAX_VALUE;
		for (Heap heap : level().heaps.valueList()){
			if (!wantHeap(heap)) continue;
			if (!(level().visited[heap.pos] || level().mapped[heap.pos] || level().heroFOV[heap.pos])) continue;
			if (attempts.getOrDefault("heap" + heap.pos + ":" + heap.size(), 0) >= 3) continue;
			//loot in or beside a piranha's water is bait: left alone until the careful rules are relaxed
			if (piranhaFloor && desperation < 1 && nearDeepWater(heap.pos)) continue;
			int target = heap.type == Heap.Type.HEAP || heap.type == Heap.Type.FOR_SALE ? heap.pos : nav.approach(level(), heap.pos);
			if (target < 0 || !nav.reachable(target)) continue;
			int d = nav.dist[target];
			if (d < bestDist){ bestDist = d; best = heap; }
		}
		if (best == null) return null;
		tried("heap" + best.pos + ":" + best.size(), 3);
		if (best.pos == hero().pos || level().adjacent(best.pos, hero().pos)){
			return tap(best.pos, (best.type == Heap.Type.FOR_SALE ? "buy " : best.type == Heap.Type.HEAP ? "pick up " : "open ") + nameOf(best.peek()) + " at");
		}
		attempts.put("heap" + best.pos + ":" + best.size(), attempts.get("heap" + best.pos + ":" + best.size()) - 1);
		int goal = best.type == Heap.Type.HEAP || best.type == Heap.Type.FOR_SALE ? best.pos : nav.approach(level(), best.pos);
		return stepToward(nav, goal, "to " + nameOf(best.peek()));
	}

	private String talk(SimNav nav){
		for (Mob m : level().mobs.toArray(new Mob[0])){
			if (!(m instanceof NPC) || m instanceof Shopkeeper || !m.isAlive() || !level().heroFOV[m.pos]) continue;
			if (!m.heroShouldInteract()) continue;
			Float last = npcTalkedAt.get(m.id());
			if (last != null && Actor.now() - last < 400) continue;
			if (level().adjacent(m.pos, hero().pos)){
				npcTalkedAt.put(m.id(), Actor.now());
				return tap(m.pos, "talk to " + nameOf(m) + " at");
			}
			int goal = nav.approach(level(), m.pos);
			if (goal >= 0 && goal != hero().pos){
				String s = stepToward(nav, goal, "to talk to " + nameOf(m));
				if (s != null) return s;
			}
		}
		return null;
	}

	private String unlock(SimNav nav){
		boolean iron = Notes.keyCount(new IronKey(Dungeon.depth)) > 0;
		boolean crystal = Notes.keyCount(new CrystalKey(Dungeon.depth)) > 0;
		//the boss drops a worn key that opens the locked way down
		boolean worn = Notes.keyCount(new com.shatteredpixel.shatteredpixeldungeon.items.keys.WornKey(Dungeon.depth)) > 0;
		if (!iron && !crystal && !worn) return null;
		int best = -1, bestDist = Integer.MAX_VALUE;
		for (int i = 0; i < level().length(); i++){
			int t = level().map[i];
			if (!(level().visited[i] || level().mapped[i])) continue;
			if (!((t == Terrain.LOCKED_DOOR && iron) || (t == Terrain.CRYSTAL_DOOR && crystal) || (t == Terrain.LOCKED_EXIT && worn))) continue;
			int a = nav.approach(level(), i);
			if (a < 0 || attempts.getOrDefault("door" + i, 0) >= 4) continue;
			if (nav.dist[a] < bestDist){ bestDist = nav.dist[a]; best = i; }
		}
		if (best < 0) return null;
		if (level().adjacent(best, hero().pos)){
			tried("door" + best, 4);
			return tap(best, "unlock door at");
		}
		return stepToward(nav, nav.approach(level(), best), "to a locked door");
	}

	private boolean unknown(int c){
		return !level().visited[c] && !level().mapped[c];
	}

	private boolean isFrontier(int c){
		int w = level().width();
		for (int d : PathFinder.NEIGHBOURS8){
			int n = c + d;
			if (n < 0 || n >= level().length() || Math.abs(n % w - c % w) > 1) continue;
			if (unknown(n)) return true;
		}
		return false;
	}

	private String explore(SimNav nav){
		if (exploreGoal >= 0 && exploreGoal != hero().pos && nav.reachable(exploreGoal) && !doneFrontier.contains(exploreGoal) && isFrontier(exploreGoal)
				&& attempts.getOrDefault("front" + exploreGoal, 0) <= 40){
			tried("front" + exploreGoal, 1000);
			String step = stepToward(nav, exploreGoal, "exploring");
			if (step != null) return step;
		}
		if (exploreGoal == hero().pos) doneFrontier.add(exploreGoal);
		exploreGoal = -1;
		int best = -1, bestDist = Integer.MAX_VALUE;
		int w = level().width();
		for (int c = 0; c < level().length(); c++){
			if (!nav.reachable(c) || doneFrontier.contains(c)) continue;
			boolean frontier = false;
			for (int d : PathFinder.NEIGHBOURS8){
				int n = c + d;
				if (n < 0 || n >= level().length() || Math.abs(n % w - c % w) > 1) continue;
				if (unknown(n)){ frontier = true; break; }
			}
			if (!frontier) continue;
			if (nav.dist[c] < bestDist){ bestDist = nav.dist[c]; best = c; }
		}
		if (best < 0) return null;
		if (best == hero().pos){
			doneFrontier.add(best);
			return explore(nav);
		}
		if (attempts.getOrDefault("front" + best, 0) > 40){
			doneFrontier.add(best);
			return explore(nav);
		}
		tried("front" + best, 1000);
		exploreGoal = best;
		return stepToward(nav, best, "exploring");
	}

	private boolean nearDeepWater(int c){
		if (piranhaWater == null) return false;
		for (int d : PathFinder.NEIGHBOURS9){
			int n = c + d;
			if (n >= 0 && n < level().length() && piranhaWater[n]) return true;
		}
		return false;
	}

	private boolean exitReachable(SimNav nav){
		LevelTransition exit = level().getTransition(LevelTransition.Type.REGULAR_EXIT);
		return exit != null && (level().visited[exit.cell()] || level().mapped[exit.cell()]) && nav.reachable(exit.cell()) && !level().locked;
	}

	private String descend(SimNav nav){
		LevelTransition exit = level().getTransition(LevelTransition.Type.REGULAR_EXIT);
		if (exit == null) return null;
		int cell = exit.cell();
		if (!(level().visited[cell] || level().mapped[cell])) return null;
		if (level().locked) return null;
		if (hero().pos == cell || level().adjacent(hero().pos, cell)){
			//a boss waits on every fifth floor: go down healthy (not Clueless, not Reckless), as long as food allows
			Hunger hg = hero().buff(Hunger.class);
			if ((Dungeon.depth + 1) % 5 == 0 && !clueless() && aggression != SimProfile.Aggression.RECKLESS
					&& hero().HP < hero().HT * 0.9f && (hg == null || !hg.isHungry()) && Actor.now() >= noRestUntil && !tried("bossrest", 6)){
				hero().rest(true);
				return "rest before the boss floor";
			}
			if (tried("descend", 30)) return null;
			return tap(cell, "go down the stairs at");
		}
		if (!nav.reachable(cell)) return null;
		return stepToward(nav, cell, "to the stairs down");
	}

	private String ascend(SimNav nav){
		LevelTransition entrance = level().getTransition(LevelTransition.Type.REGULAR_ENTRANCE);
		if (entrance == null) return null;
		int cell = entrance.cell();
		if (hero().pos == cell || level().adjacent(hero().pos, cell)){
			if (tried("ascend", 30)) return null;
			return tap(cell, "go up the stairs (carrying the Amulet) at");
		}
		if (!nav.reachable(cell)) return null;
		return stepToward(nav, cell, "to the stairs up");
	}

	//search next to walls that might hide something, nearest first; a few per floor
	private String search(SimNav nav){
		if (searchesThisFloor >= 25) return null;
		if (searchGoal >= 0 && nav.reachable(searchGoal) && !searchedSpots.contains(searchGoal)){
			if (searchGoal == hero().pos) return searchHere();
			String step = stepToward(nav, searchGoal, "to search");
			if (step != null) return step;
		}
		searchGoal = -1;
		int best = -1, bestDist = Integer.MAX_VALUE;
		int w = level().width();
		for (int c = 0; c < level().length(); c++){
			if (!nav.reachable(c) || searchedSpots.contains(c)) continue;
			int walls = 0;
			for (int d : PathFinder.NEIGHBOURS4){
				int n = c + d;
				if (n < 0 || n >= level().length() || Math.abs(n % w - c % w) > 1) continue;
				if (level().solid[n]) walls++;
			}
			if (walls < 3) continue; //dead ends first
			if (nav.dist[c] < bestDist){ bestDist = nav.dist[c]; best = c; }
		}
		if (best < 0){
			for (int c = 0; c < level().length(); c++){
				if (!nav.reachable(c) || searchedSpots.contains(c)) continue;
				if (nav.dist[c] < bestDist && nav.dist[c] % 4 == 0){ bestDist = nav.dist[c]; best = c; }
			}
		}
		if (best < 0) return null;
		searchGoal = best;
		if (best == hero().pos) return searchHere();
		return stepToward(nav, best, "to search");
	}

	private String searchHere(){
		int c = hero().pos;
		for (int d : PathFinder.NEIGHBOURS9) searchedSpots.add(c + d);
		searchGoal = -1;
		searchesThisFloor++;
		hero().search(true);
		return "search at " + cell(c);
	}

	//why the bot can't continue, for stuck reports
	String noWayForward(SimNav nav){
		StringBuilder b = new StringBuilder();
		LevelTransition exit = level().getTransition(LevelTransition.Type.REGULAR_EXIT);
		if (exit == null) b.append("this floor has no way down; ");
		else {
			int cell = exit.cell();
			boolean known = level().visited[cell] || level().mapped[cell];
			b.append("stairs down at ").append(cell(cell)).append(known ? " (seen)" : " (never seen)")
					.append(nav.reachable(cell) ? " reachable" : " NOT reachable").append("; ");
		}
		if (level().locked) b.append("floor is sealed (locked); ");
		int iron = Notes.keyCount(new IronKey(Dungeon.depth)), golden = Notes.keyCount(new GoldenKey(Dungeon.depth)), crystal = Notes.keyCount(new CrystalKey(Dungeon.depth));
		if (iron + golden + crystal > 0) b.append("keys held: iron ").append(iron).append(", golden ").append(golden).append(", crystal ").append(crystal).append("; ");
		int locked = 0, lockedReachable = 0;
		for (int i = 0; i < level().length(); i++){
			if ((level().map[i] == Terrain.LOCKED_DOOR || level().map[i] == Terrain.CRYSTAL_DOOR) && (level().visited[i] || level().mapped[i])){
				locked++;
				if (nav.approach(level(), i) >= 0) lockedReachable++;
			}
		}
		if (locked > 0) b.append(locked).append(" locked door(s) seen, ").append(lockedReachable).append(" reachable; ");
		b.append(searchesThisFloor).append(" searches done on this floor");
		return b.toString();
	}

	//---- answering the game's questions ----------------------------------------------------------------------

	Item pickItem(WndBag.ItemSelector selector){
		List<Item> ok = new ArrayList<>();
		for (Item i : hero().belongings){
			try { if (selector.itemSelectable(i)) ok.add(i); } catch (Throwable ignored){}
		}
		if (ok.isEmpty()) return null;
		//Scroll of Upgrade (not Clueless): the Mage's staff for a Mage; otherwise the worn armor, shared with a good weapon.
		if (!clueless() && selector.getClass().getName().contains("InventoryScroll") && lastUsed instanceof ScrollOfUpgrade){
			Item staff = hero().belongings.weapon() instanceof MagesStaff ? hero().belongings.weapon() : null;
			Item target = staff != null ? staff : hero().belongings.armor();
			//a weapon worth keeping (tier 2 or better) shares the scrolls: whichever of the two is behind gets the next one
			Item wep = hero().belongings.weapon();
			if (staff == null && wep instanceof MeleeWeapon && ((MeleeWeapon) wep).tier >= 2 && !(wep.cursedKnown && wep.cursed)
					&& (target == null || wep.level() < target.level()) && ok.contains(wep)) target = wep;
			if (target != null && ok.contains(target)) return target;
		}
		Item best = null;
		int bestScore = Integer.MIN_VALUE;
		for (Item i : ok){
			int s = rng.nextInt(3);
			if (i.isEquipped(hero())) s += 6;
			if (!i.isIdentified()) s += 3;
			if (i.cursedKnown && i.cursed) s += 4;
			if (i instanceof Bag) s -= 10;
			if (s > bestScore){ bestScore = s; best = i; }
		}
		return best;
	}

	int pickCell(){
		List<Mob> foes = hostiles();
		if (focusCell >= 0 && Actor.findChar(focusCell) instanceof Mob && ((Mob) Actor.findChar(focusCell)).alignment == Char.Alignment.ENEMY) return focusCell;
		if (!foes.isEmpty()) return foes.get(0).pos;
		return -1;
	}

	//picks a button: a "yes" kind of answer most of the time, sometimes another one so other paths get played too
	int pickButton(List<String> labels){
		List<Integer> positive = new ArrayList<>(), safe = new ArrayList<>();
		for (int i = 0; i < labels.size(); i++){
			String l = labels.get(i).toLowerCase(java.util.Locale.ROOT);
			if (l.matches(".*\\b(quit|main menu|give up|delete|restart|abandon|exit game|forget|reset|erase|leave|cancel|no)\\b.*")) continue;
			safe.add(i);
			if (l.matches(".*\\b(yes|ok|okay|accept|continue|buy|take|read|drink|equip|confirm|i'll|i will|agree|sure|choose|select|wear|use|transfer|keep|claim|reward|go)\\b.*")) positive.add(i);
		}
		if (!positive.isEmpty() && rng.nextInt(6) != 0) return positive.get(rng.nextInt(positive.size()));
		if (!safe.isEmpty()) return safe.get(rng.nextInt(safe.size()));
		return labels.isEmpty() ? -1 : 0;
	}
}

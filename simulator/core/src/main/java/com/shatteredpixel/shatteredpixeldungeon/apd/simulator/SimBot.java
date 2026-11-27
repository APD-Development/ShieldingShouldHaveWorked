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
	//goals are kept until reached or no longer valid, so the bot does not flip between two equally near targets
	private int searchGoal = -1, exploreGoal = -1;
	float floorStartTurn;
	int focusCell = -1;
	String lastProblem = "";

	final SimProfile.Aggression aggression;
	final SimProfile.Intelligence intelligence;
	//what the settings mean for play (all hand-written rules)
	final float healAt, backOffAt, tryUnknownChance, restTo;
	final boolean fullExplore, dive, optionalFights, avoidKnownTraps, wearUnidentified;

	SimBot(long seed, SimProfile.Aggression aggression, SimProfile.Intelligence intelligence){
		rng = new Random(seed);
		this.aggression = aggression;
		this.intelligence = intelligence;
		switch (aggression){
			case CAUTIOUS:   restTo = 0.95f; healAt = 0.50f; backOffAt = 0.40f; tryUnknownChance = 0.15f; fullExplore = true;  dive = false; optionalFights = false; wearUnidentified = false; break;
			case AGGRESSIVE: restTo = 0.50f; healAt = 0.25f; backOffAt = 0f;    tryUnknownChance = 0.85f; fullExplore = false; dive = false; optionalFights = true;  wearUnidentified = true;  break;
			case RECKLESS:   restTo = 0.30f; healAt = 0.12f; backOffAt = 0f;    tryUnknownChance = 1.00f; fullExplore = false; dive = true;  optionalFights = true;  wearUnidentified = true;  break;
			default:         restTo = 0.80f; healAt = 0.35f; backOffAt = 0.20f; tryUnknownChance = 0.50f; fullExplore = true;  dive = false; optionalFights = true;  wearUnidentified = false; break;
		}
		avoidKnownTraps = intelligence != SimProfile.Intelligence.CLUELESS;
	}

	boolean smart(){ return intelligence.ordinal() >= SimProfile.Intelligence.SMART.ordinal(); }
	boolean clueless(){ return intelligence == SimProfile.Intelligence.CLUELESS; }

	//cells the game refused to let the hero step onto this floor (the move took no time and the hero stayed put)
	private final Set<Integer> refused = new HashSet<>();
	private int lastStepFrom = -1, lastStepTo = -1;
	private float lastStepTime = -1;

	private SimNav nav(boolean throughMobs){
		SimNav.avoidSentries = !clueless() && aggression.ordinal() <= SimProfile.Aggression.NORMAL.ordinal();
		SimNav n = new SimNav(level(), hero().pos, throughMobs, avoidKnownTraps, refused);
		return n;
	}

	private Hero hero(){ return Dungeon.hero; }
	private Level level(){ return Dungeon.level; }

	private void newFloorCheck(){
		int key = Dungeon.depth * 100 + Dungeon.branch;
		if (key != levelKey){
			levelKey = key;
			attempts.clear();
			doneFrontier.clear();
			searchedSpots.clear();
			npcTalkedAt.clear();
			refused.clear();
			restsThisFloor = 0;
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
			if (m.state == m.PASSIVE && !Dungeon.level.adjacent(m.pos, hero().pos)
					&& !(optionalFights && aggression.ordinal() >= SimProfile.Aggression.AGGRESSIVE.ordinal())) continue;
			if (m instanceof Mimic && m.alignment != Char.Alignment.ENEMY) continue;
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

	private String use(Item i, String action){
		i.execute(hero(), action);
		return action.toLowerCase() + " " + nameOf(i);
	}

	private String tap(int cell, String what){
		hero().handle(cell);
		hero().next();
		return what + " " + cell(cell);
	}

	private String stepToward(SimNav nav, int target, String why){
		int step = nav.firstStep(target);
		if (step < 0) return null;
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

	String act(){
		newFloorCheck();
		recent.addLast(hero().pos);
		while (recent.size() > 40) recent.removeFirst();
		if (recent.size() == 40 && new HashSet<>(recent).size() <= 3){
			oscillation = "walking back and forth between " + new HashSet<>(recent).size() + " cells at " + cell(hero().pos);
			//give up whatever it was heading for on this floor and pick something else
			if (exploreGoal >= 0) doneFrontier.add(exploreGoal);
			if (searchGoal >= 0) searchedSpots.add(searchGoal);
			for (int c : new HashSet<>(recent)) for (int d : PathFinder.NEIGHBOURS9) attempts.put("door" + (c + d), 99);
			exploreGoal = searchGoal = -1;
			recent.clear();
		}
		if (lastStepTo >= 0 && hero().pos == lastStepFrom && Actor.now() == lastStepTime) refused.add(lastStepTo);
		lastStepTo = -1;
		lastProblem = "";
		Hero h = hero();

		String free = freeActions();
		if (free != null) return free;

		List<Mob> foes = hostiles();
		Hunger hunger = h.buff(Hunger.class);

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

		if (foes.isEmpty() && h.HP < h.HT * restTo && restsThisFloor < 40 && (hunger == null || !hunger.isStarving())){
			restsThisFloor++;
			h.rest(true);
			return "rest until healed";
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
		boolean starvingNoFood = hunger != null && hunger.isStarving() && bestFood() == null;
		boolean skipExplore = exitKnown && (dive || starvingNoFood || (!fullExplore && Actor.now() - floorStartTurn > 300));
		if (!skipExplore){
			String explore = explore(nav);
			if (explore != null) return explore;
		}

		String down = descend(nav);
		if (down != null) return down;

		String search = search(nav);
		if (search != null) return search;

		lastProblem = noWayForward(nav);
		h.rest(false);
		return "wait (nothing left to do: " + lastProblem + ")";
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

		//piranhas can't leave the water: step away from it instead of trading bites at the edge (not Clueless)
		if (!clueless() && near instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Piranha){
			SimNav nav = nav(false);
			int best = -1;
			for (int c = 0; c < level().length(); c++){
				if (!nav.reachable(c) || nav.dist[c] > 3) continue;
				boolean wet = false;
				for (int d : PathFinder.NEIGHBOURS9){
					int n = c + d;
					if (n >= 0 && n < level().length() && level().map[n] == Terrain.WATER){ wet = true; break; }
				}
				if (!wet && (best < 0 || nav.dist[c] < nav.dist[best])) best = c;
			}
			if (best >= 0 && best != h.pos){
				String step = stepToward(nav, best, "away from the piranha's water");
				if (step != null) return step;
			}
		}

		//low health: heal if a known Healing potion is held, otherwise gamble on an unknown potion
		if (h.HP < h.HT * healAt){
			PotionOfHealing heal = find(PotionOfHealing.class);
			if (heal != null && heal.isKnown()) return use(heal, Potion.AC_DRINK);
			CloakOfShadows cloak = find(CloakOfShadows.class);
			if (cloak != null && cloak.isEquipped(h) && has(cloak, CloakOfShadows.AC_STEALTH) && !tried("cloak@" + (int) Actor.now(), 1))
				return use(cloak, CloakOfShadows.AC_STEALTH);
			for (Item i : items()){
				if (i instanceof Potion && !((Potion) i).isKnown() && !tried("gamble" + i.getClass().getName(), 1)) return use(i, Potion.AC_DRINK);
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
			//Clueless hits the closest; others pick the weakest in reach
			if (reach == null || (!clueless() && m.HP < reach.HP)) reach = m;
		}

		//backing off: low health, nothing to heal with, and the settings say retreat
		boolean someoneClose = false;
		for (Mob m : foes) if (level().distance(m.pos, h.pos) <= 3) someoneClose = true;
		if (backOffAt > 0 && h.HP < h.HT * backOffAt && !clueless() && someoneClose){
			String away = retreat(foes);
			if (away != null) return away;
		}

		//Smart and Expert: against a group, step into a spot where fewer of them can reach
		if (smart() && foes.size() >= 2 && reach == null){
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

		if (reach != null){
			focusCell = reach.pos;
			lastAttack = reach;
			lastAttackTime = Actor.now();
			return tap(reach.pos, "attack " + nameOf(reach) + " at");
		}

		//ranged options for an enemy that can't be hit yet
		boolean clear = !clueless() && new Ballistica(h.pos, near.pos, Ballistica.PROJECTILE).collisionPos == near.pos;
		if (clear){
			SpiritBow bow = find(SpiritBow.class);
			if (bow != null && has(bow, SpiritBow.AC_SHOOT)) return use(bow, SpiritBow.AC_SHOOT);
			MagesStaff staff = find(MagesStaff.class);
			if (staff != null && staffCharges(staff) > 0 && has(staff, MagesStaff.AC_ZAP)) return use(staff, MagesStaff.AC_ZAP);
			for (Item i : items()){
				if (i instanceof Wand && ((Wand) i).curCharges > 0 && has(i, Wand.AC_ZAP) && rng.nextInt(2) == 0) return use(i, Wand.AC_ZAP);
			}
			for (Item i : items()){
				if (i instanceof Potion && ((Potion) i).isKnown() && harmful((Potion) i) && level().distance(h.pos, near.pos) >= 2) return use(i, Item.AC_THROW);
			}
			for (Item i : items()){
				if (i instanceof MissileWeapon && !i.isEquipped(h) && rng.nextInt(2) == 0) return use(i, Item.AC_THROW);
			}
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
		SimNav nav = nav(false);
		int best = -1;
		for (LevelTransition t : level().transitions){
			int c = t.cell();
			if (c == hero().pos && !level().locked){
				lastEscapeTurn = com.shatteredpixel.shatteredpixeldungeon.Statistics.apdTurn();
				return tap(c, "escape (low health) by the stairs at");
			}
			if (nav.reachable(c) && nav.dist[c] <= 6 && (best < 0 || nav.dist[c] < nav.dist[best])) best = c;
		}
		if (best >= 0) return stepToward(nav, best, "escape to the stairs");
		boolean adjacent = false;
		for (Mob m : foes) if (level().adjacent(m.pos, hero().pos)) adjacent = true;
		if (adjacent) return null;
		best = -1; int bestScore = Integer.MIN_VALUE;
		for (int d : PathFinder.NEIGHBOURS8){
			int c = hero().pos + d;
			if (!nav.reachable(c) || nav.dist[c] != 1) continue;
			int score = Integer.MAX_VALUE;
			for (Mob m : foes) score = Math.min(score, level().distance(c, m.pos));
			if (score > bestScore){ bestScore = score; best = c; }
		}
		int now = Integer.MAX_VALUE;
		for (Mob m : foes) now = Math.min(now, level().distance(hero().pos, m.pos));
		if (best < 0 || bestScore <= now) return null;
		return tap(best, "back off (low health) to");
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
				if (!i.isIdentified() && !wearUnidentified && !clueless()) continue;
				MeleeWeapon m = (MeleeWeapon) i;
				if (!clueless() && m.STRReq() > h.STR()) continue;
				if (weaponValue(m) > (cur == null ? 0 : weaponValue(cur)) && (better == null || weaponValue(m) > weaponValue(better))) better = m;
			}
			if (better != null && !tried("equip" + better.getClass().getName(), 2)) return use(better, EquipableItem.AC_EQUIP);
		}
		Armor cur = h.belongings.armor();
		for (Item i : items()){
			if (!(i instanceof Armor) || i.isEquipped(h) || (i.cursedKnown && i.cursed)) continue;
			if (!i.isIdentified() && !wearUnidentified && !clueless()) continue;
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
			if (i instanceof ScrollOfUpgrade && ((Scroll) i).isKnown()) return use(i, Scroll.AC_READ);
		}
		//identify by use, like a player: read unknown scrolls, drink unknown potions at full health
		for (Item i : items()){
			if (i instanceof Scroll && !((Scroll) i).isKnown() && has(i, Scroll.AC_READ) && !tried("read" + i.getClass().getName(), 2)
					&& rng.nextFloat() < tryUnknownChance) return use(i, Scroll.AC_READ);
		}
		if (h.HP >= h.HT * (aggression == SimProfile.Aggression.RECKLESS ? 0.3f : 0.9f)){
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
		if (!iron && !crystal) return null;
		int best = -1, bestDist = Integer.MAX_VALUE;
		for (int i = 0; i < level().length(); i++){
			int t = level().map[i];
			if (!(level().visited[i] || level().mapped[i])) continue;
			if (!((t == Terrain.LOCKED_DOOR && iron) || (t == Terrain.CRYSTAL_DOOR && crystal))) continue;
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

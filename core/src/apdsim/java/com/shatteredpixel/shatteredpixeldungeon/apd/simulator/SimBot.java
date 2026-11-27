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
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfCorrosiveGas;
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
	float floorStartTurn;
	int focusCell = -1;
	String lastProblem = "";

	SimBot(long seed){
		rng = new Random(seed);
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
			restsThisFloor = 0;
			searchesThisFloor = 0;
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
			if (m.state == m.PASSIVE && !Dungeon.level.adjacent(m.pos, hero().pos)) continue;
			if (m instanceof Mimic && m.alignment != Char.Alignment.ENEMY) continue;
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
		return tap(step, "move (" + why + ") to");
	}

	//---- the decision ----------------------------------------------------------------------------------------

	//Called when it is the hero's turn and nothing is waiting for an answer. Returns what was done, or null.
	String act(){
		newFloorCheck();
		lastProblem = "";
		Hero h = hero();

		String free = freeActions();
		if (free != null) return free;

		List<Mob> foes = hostiles();
		Hunger hunger = h.buff(Hunger.class);

		if (!foes.isEmpty()){
			String fight = fight(foes);
			if (fight != null) return fight;
		}

		if (hunger != null && (hunger.isStarving() || (hunger.isHungry() && foes.isEmpty()))){
			Food food = bestFood();
			if (food != null) return use(food, Food.AC_EAT);
		}

		if (foes.isEmpty()){
			String manage = manageItems();
			if (manage != null) return manage;
		}

		SimNav nav = new SimNav(level(), h.pos, false);

		if (h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.Amulet.class) != null){
			String up = ascend(nav);
			if (up != null) return up;
		}

		if (foes.isEmpty() && h.HP < h.HT * 0.6f && restsThisFloor < 40 && (hunger == null || !hunger.isStarving())){
			restsThisFloor++;
			h.rest(true);
			return "rest until healed";
		}

		String loot = loot(nav);
		if (loot != null) return loot;

		String npc = talk(nav);
		if (npc != null) return npc;

		String unlock = unlock(nav);
		if (unlock != null) return unlock;

		String explore = explore(nav);
		if (explore != null) return explore;

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

		//low health: heal if a known Healing potion is held, otherwise gamble on an unknown potion
		if (h.HP < h.HT * 0.35f){
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

		Mob reach = null;
		for (Mob m : foes){
			if (h.canAttack(m) && (reach == null || m.HP < reach.HP)) reach = m;
		}

		//Duelist weapon ability, Cleric spells: now and then, the way a player uses them
		if (reach != null && rng.nextInt(4) == 0){
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
			return tap(reach.pos, "attack " + nameOf(reach) + " at");
		}

		//ranged options for an enemy that can't be hit yet
		boolean clear = new Ballistica(h.pos, near.pos, Ballistica.PROJECTILE).collisionPos == near.pos;
		if (clear){
			SpiritBow bow = find(SpiritBow.class);
			if (bow != null && has(bow, SpiritBow.AC_SHOOT)) return use(bow, SpiritBow.AC_SHOOT);
			MagesStaff staff = find(MagesStaff.class);
			if (staff != null && staff.wand() != null && staff.wand().curCharges > 0 && has(staff, MagesStaff.AC_ZAP)) return use(staff, MagesStaff.AC_ZAP);
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

		SimNav nav = new SimNav(level(), h.pos, false);
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

	private static boolean harmful(Potion p){
		return p instanceof PotionOfLiquidFlame || p instanceof PotionOfToxicGas || p instanceof PotionOfParalyticGas
				|| p instanceof PotionOfFrost || p instanceof PotionOfCorrosiveGas;
	}

	//---- items -----------------------------------------------------------------------------------------------

	private Food bestFood(){
		Food best = null;
		for (Item i : items()){
			if (i instanceof Food && (best == null || ((Food) i).energy > best.energy)) best = (Food) i;
		}
		return best;
	}

	private String manageItems(){
		Hero h = hero();
		//equipment
		if (!(h.heroClass == HeroClass.MAGE && h.belongings.weapon() instanceof MagesStaff)){
			MeleeWeapon better = null;
			int curTier = h.belongings.weapon() instanceof MeleeWeapon ? ((MeleeWeapon) h.belongings.weapon()).tier : 0;
			for (Item i : items()){
				if (i instanceof MeleeWeapon && !i.isEquipped(h) && !(i.cursedKnown && i.cursed)
						&& ((MeleeWeapon) i).tier > curTier && ((MeleeWeapon) i).STRReq() <= h.STR()
						&& (better == null || ((MeleeWeapon) i).tier > better.tier)) better = (MeleeWeapon) i;
			}
			if (better != null && !tried("equip" + better.getClass().getName(), 2)) return use(better, EquipableItem.AC_EQUIP);
		}
		Armor cur = h.belongings.armor();
		for (Item i : items()){
			if (i instanceof Armor && !i.isEquipped(h) && !(i.cursedKnown && i.cursed)
					&& ((Armor) i).tier > (cur == null ? 0 : cur.tier) && ((Armor) i).STRReq() <= h.STR()
					&& !tried("equip" + i.getClass().getName(), 2)) return use(i, EquipableItem.AC_EQUIP);
		}
		for (Item i : items()){
			if ((i instanceof Ring || i instanceof Artifact) && !i.isEquipped(h) && !(i.cursedKnown && i.cursed)
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
			if (i instanceof Scroll && !((Scroll) i).isKnown() && has(i, Scroll.AC_READ) && !tried("read" + i.getClass().getName(), 2)) return use(i, Scroll.AC_READ);
		}
		if (h.HP >= h.HT * 0.9f){
			for (Item i : items()){
				if (i instanceof Potion && !((Potion) i).isKnown() && has(i, Potion.AC_DRINK) && !tried("drink" + i.getClass().getName(), 2)) return use(i, Potion.AC_DRINK);
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

	private String explore(SimNav nav){
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
		return stepToward(nav, best, "exploring");
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
		if (best == hero().pos){
			for (int d : PathFinder.NEIGHBOURS9){
				searchedSpots.add(best + d);
			}
			searchesThisFloor++;
			hero().search(true);
			return "search at " + cell(best);
		}
		return stepToward(nav, best, "to search");
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

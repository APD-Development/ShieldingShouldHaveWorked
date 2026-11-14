package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.noosa.Game;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

//Test-source-only scripted driver for real APD state and combat code.
final class HeadlessSimulationHarness {

	private HeadlessSimulationHarness(){}

	static List<String> run( long seed ) throws Exception {
		return run(seed, HeroClass.WARRIOR);
	}

	static List<String> run( long seed, HeroClass heroClass ) throws Exception {
		Game previousGame = Game.instance;
		com.watabou.utils.PlatformSupport previousPlatform = Game.platform;
		new Game(null, previousPlatform){
			{
				requestedReset = false;
			}
		};
		RecorderTestSupport.Session session = new RecorderTestSupport.Session();
		try {
				RecorderTestSupport.newRun(session, seed, heroClass);
			Random.pushGenerator(seed);
			try {
			RecorderTestSupport.enterLevel(1);

			Hero hero = Dungeon.hero;
			new ActionSprite(hero);
			hero.damageInterrupt = false;
			Level level = Dungeon.level;
			List<String> transcript = new ArrayList<>();
						transcript.add("run:class=" + heroClass.name() + ":seed=" + seed);
			PotionOfHealing startingPotion = new PotionOfHealing();
			startingPotion.identify();
			startingPotion.collect();
			for (int i = 0; i < 4; i++){
				PotionOfHealing potion = new PotionOfHealing();
				potion.identify();
				potion.collect();
			}
			transcript.add("resource:healing-potions=" + hero.belongings.getItem(PotionOfHealing.class).quantity());
			ScrollOfUpgrade weaponUpgrade = new ScrollOfUpgrade();
			weaponUpgrade.identify();
			weaponUpgrade.collect();
			ScrollOfUpgrade armorUpgrade = new ScrollOfUpgrade();
			armorUpgrade.identify();
			armorUpgrade.collect();
			PotionOfExperience experiencePotion = new PotionOfExperience();
			experiencePotion.identify();
			experiencePotion.collect();

			int moveTo = openNeighbour(level, hero.pos);
			int from = hero.pos;
			hero.move(moveTo, true);
			Dungeon.observe();
			transcript.add("move:" + from + ">" + hero.pos + ":visible=" + level.heroFOV[hero.pos]);

			Food food = new Food();
			Heap heap = ItemTestSupport.putInSight(food, hero.pos);
			if (!food.doPickUp(hero)) throw new AssertionError("food pickup failed");
			heap.pickUp();
			transcript.add("pickup:food=" + hero.belongings.getItem(Food.class).quantity());

			Rat rat = new Rat();
			rat.pos = openNeighbour(level, hero.pos);
			new ActionSprite(rat);
			level.mobs.add(rat);
			Actor.add(rat);
			level.heroFOV[rat.pos] = true;
			transcript.add("encounter:rat=" + rat.HP);

			int turn = 0;
			while (rat.isAlive() && hero.isAlive() && turn < 100){
				int heroBefore = hero.HP;
				int ratBefore = rat.HP;
				boolean hit = hero.attack(rat);
				int dealt = ratBefore - rat.HP;
				if (rat.isAlive()) rat.attack(hero);
				int taken = heroBefore - hero.HP;
				transcript.add("turn:" + turn + ":attack:hit=" + hit + ":dealt=" + dealt + ":taken=" + taken
						+ ":hero=" + hero.HP + ":rat=" + rat.HP);
				turn++;
			}
			if (rat.isAlive() && hero.isAlive()) throw new AssertionError("rat encounter did not terminate");
			transcript.add("outcome:floor=1:heroAlive=" + hero.isAlive() + ":ratAlive=" + rat.isAlive() + ":turns=" + turn);

			for (int depth = 2; depth <= 5; depth++){
				RecorderTestSupport.enterLevel(depth);
				Dungeon.observe();
				transcript.add("explore:depth=" + depth + ":visible=" + visibleCells(Dungeon.level));
			}
			if (!(Dungeon.level instanceof SewerBossLevel)) throw new AssertionError("depth 5 did not generate the Sewer Boss level");

			Goo goo = null;
			for (Mob mob : Dungeon.level.mobs){
				if (mob instanceof Goo){
					goo = (Goo) mob;
					break;
				}
			}
			if (goo == null) throw new AssertionError("generated Sewer Boss level has no Goo");
			new ActionSprite(goo);
			transcript.add("boss:depth=5:gooHP=" + goo.HP);
			weaponUpgrade.setCurrent(hero);
			weaponUpgrade.detach(hero.belongings.backpack);
			weaponUpgrade.upgradeItem(hero.belongings.weapon);
			armorUpgrade.setCurrent(hero);
			armorUpgrade.detach(hero.belongings.backpack);
			armorUpgrade.upgradeItem(hero.belongings.armor);
			experiencePotion.execute(hero, Potion.AC_DRINK);
			transcript.add("resource:weapon-upgrades=" + hero.belongings.weapon.level()
					+ ":armor-upgrades=" + hero.belongings.armor.level() + ":hero-level=" + hero.lvl);

			int movementTurn = 0;
			while (!Dungeon.level.adjacent(hero.pos, goo.pos) && movementTurn < Dungeon.level.length()){
				Dungeon.observe();
				PathFinder.Path path = pathToGoo(goo);
				if (path == null || path.isEmpty()) throw new AssertionError("no path to Goo");
				int fromBossApproach = hero.pos;
				int next = path.removeFirst();
				if (!Dungeon.level.adjacent(fromBossApproach, next)) throw new AssertionError("path contains a non-adjacent step");
				hero.move(next, true);
				Dungeon.observe();
				transcript.add("turn:" + movementTurn + ":move:depth=5:from=" + fromBossApproach + ":to=" + hero.pos + ":hero=" + hero.HP);
				movementTurn++;
			}
			if (!hero.canAttack(goo)) throw new AssertionError("simulator did not reach Goo attack range");

			turn = 0;
			int heroDamageTaken = 0;
			while (goo.isAlive() && hero.isAlive() && turn < 300){
				int dealt = 0;
				boolean hit = false;
				String action;
				PotionOfHealing healingPotion = hero.belongings.getItem(PotionOfHealing.class);
				if (hero.HP <= hero.HT / 2 && healingPotion != null){
					healingPotion.execute(hero, Potion.AC_DRINK);
					action = "drink-healing";
				} else if (Dungeon.level.distance(hero.pos, goo.pos) <= 2
						&& goo.attackSkill(hero) > (goo.HP * 2 <= goo.HT ? 15 : 10)){
					Dungeon.observe();
					PathFinder.Path path = pathToSafeDistance(goo);
					if (path == null || path.isEmpty()) throw new AssertionError("agent cannot evade Goo's pumped attack");
					int heroFromStep = hero.pos;
					int next = path.removeFirst();
					if (!Dungeon.level.adjacent(heroFromStep, next)) throw new AssertionError("agent selected an illegal dodge step");
					hero.move(next, true);
					Dungeon.observe();
					action = "dodge:" + heroFromStep + ">" + hero.pos;
				} else if (Dungeon.level.water[goo.pos] && goo.state != goo.SLEEPING){
					Dungeon.observe();
					PathFinder.Path path = pathToDryGround(goo);
					if (path == null || path.isEmpty()) throw new AssertionError("agent cannot lure Goo onto dry ground");
					int heroFromStep = hero.pos;
					int next = path.removeFirst();
					if (!Dungeon.level.adjacent(heroFromStep, next)) throw new AssertionError("agent selected an illegal retreat step");
					hero.move(next, true);
					Dungeon.observe();
					action = "retreat:" + heroFromStep + ">" + hero.pos;
				} else if (hero.canAttack(goo)){
					int gooBefore = goo.HP;
					hit = hero.attack(goo);
					dealt = gooBefore - goo.HP;
					action = "attack";
				} else {
					Dungeon.observe();
					PathFinder.Path path = Dungeon.level.water[goo.pos] ? pathToDryGround(goo) : pathToGoo(goo);
					if (path == null || path.isEmpty()) throw new AssertionError("agent cannot reach its selected Goo position");
					int heroFromStep = hero.pos;
					int next = path.removeFirst();
					if (!Dungeon.level.adjacent(heroFromStep, next)) throw new AssertionError("agent selected an illegal movement step");
					hero.move(next, true);
					Dungeon.observe();
					action = "move:" + heroFromStep + ">" + hero.pos;
				}
				transcript.add("turn:" + turn + ":hero-" + action + ":hit=" + hit + ":dealt=" + dealt
						+ ":hero=" + hero.HP + ":goo=" + goo.HP + ":food=" + hero.belongings.getItem(Food.class).quantity());

				if (goo.isAlive() && hero.isAlive()){
					Healing healing = hero.buff(Healing.class);
					if (healing != null) healing.act();
					if (goo.fieldOfView == null || goo.fieldOfView.length != Dungeon.level.length()){
						goo.fieldOfView = new boolean[Dungeon.level.length()];
					}
					Dungeon.level.updateFieldOfView(goo, goo.fieldOfView);
					int heroBefore = hero.HP;
					goo.act();
					int taken = heroBefore - hero.HP;
					heroDamageTaken += taken;
					Dungeon.observe();
						transcript.add("turn:" + turn + ":goo-act:taken=" + taken + ":hero=" + hero.HP
								+ ":goo=" + goo.HP + ":gooPos=" + goo.pos + ":gooWater=" + Dungeon.level.water[goo.pos]
								+ ":state=" + goo.state.getClass().getSimpleName());
				}
				turn++;
			}
			if (goo.isAlive() && hero.isAlive()) throw new AssertionError("Goo encounter did not terminate:\n" + String.join("\n", transcript));
			transcript.add("outcome:depth=5:heroAlive=" + hero.isAlive() + ":gooAlive=" + goo.isAlive()
					+ ":turns=" + turn + ":heroDamageTaken=" + heroDamageTaken);
			return transcript;
			} finally {
				Random.popGenerator();
			}
		} finally {
			Game.instance = previousGame;
			Game.platform = previousPlatform;
			ApdRecorder.reset();
		}
	}

	private static int openNeighbour( Level level, int cell ){
		for (int offset : PathFinder.NEIGHBOURS8){
			int candidate = cell + offset;
			if (level.insideMap(candidate) && level.adjacent(cell, candidate)
					&& safeForHeadlessMove(level, candidate)
					&& Actor.findChar(candidate) == null && level.heaps.get(candidate) == null
					){
				return candidate;
			}
		}
		throw new IllegalStateException("no open neighbouring cell at " + cell);
	}

	private static PathFinder.Path pathToGoo( Goo goo ){
		Level level = Dungeon.level;
		PathFinder.Path shortest = null;
		for (int offset : PathFinder.NEIGHBOURS8){
			int target = goo.pos + offset;
			if (!level.insideMap(target) || !level.adjacent(goo.pos, target)
					|| !safeForHeadlessMove(level, target) || Actor.findChar(target) != null){
				continue;
			}
			boolean[] walkable = level.passable.clone();
			for (int cell = 0; cell < walkable.length; cell++){
				if (!safeForHeadlessMove(level, cell)) walkable[cell] = false;
			}
			PathFinder.Path path = Dungeon.findPath(Dungeon.hero, target, walkable, level.heroFOV, true);
			if (path != null && (shortest == null || path.size() < shortest.size())) shortest = path;
		}
		return shortest;
	}

	private static PathFinder.Path pathToDryGround( Goo goo ){
		Level level = Dungeon.level;
		PathFinder.Path shortest = null;
		boolean[] walkable = level.passable.clone();
		for (int cell = 0; cell < walkable.length; cell++){
			if (!safeForHeadlessMove(level, cell)) walkable[cell] = false;
		}
		for (int cell = 0; cell < level.length(); cell++){
			int distance = level.distance(cell, goo.pos);
			if (!walkable[cell] || level.water[cell] || Actor.findChar(cell) != null || distance < 2 || distance > 4) continue;
			PathFinder.Path path = Dungeon.findPath(Dungeon.hero, cell, walkable, level.heroFOV, true);
			if (path != null && !path.isEmpty() && (shortest == null || path.size() < shortest.size())) shortest = path;
		}
		return shortest;
	}

	private static PathFinder.Path pathToSafeDistance( Goo goo ){
		Level level = Dungeon.level;
		PathFinder.Path shortest = null;
		boolean[] walkable = level.passable.clone();
		for (int cell = 0; cell < walkable.length; cell++){
			if (!safeForHeadlessMove(level, cell)) walkable[cell] = false;
		}
		for (int cell = 0; cell < level.length(); cell++){
			int distance = level.distance(cell, goo.pos);
			if (!walkable[cell] || Actor.findChar(cell) != null || distance < 3 || distance > 5) continue;
			PathFinder.Path path = Dungeon.findPath(Dungeon.hero, cell, walkable, level.heroFOV, true);
			if (path != null && !path.isEmpty() && (shortest == null || path.size() < shortest.size())) shortest = path;
		}
		return shortest;
	}

	private static boolean safeForHeadlessMove( Level level, int cell ){
		return level.passable[cell] && !level.pit[cell] && level.traps.get(cell) == null
				&& level.map[cell] != Terrain.WELL;
	}

	private static int visibleCells( Level level ){
		int visible = 0;
		for (boolean cell : level.heroFOV) if (cell) visible++;
		return visible;
	}

	private static final class ActionSprite extends QuietSprite {
		private final Char owner;

		ActionSprite( Char owner ){
			super(owner);
			this.owner = owner;
		}

		@Override
		public void attack( int cell ){
			owner.onAttackComplete();
		}

		@Override
		public void move( int from, int to ){
		}

		@Override
		public void place( int cell ){
		}

		@Override
		public void turnTo( int from, int to ){
		}
	}
}